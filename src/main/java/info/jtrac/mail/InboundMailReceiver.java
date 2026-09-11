package info.jtrac.mail;

import com.sun.mail.util.MailSSLSocketFactory;
import info.jtrac.Jtrac;
import info.jtrac.domain.History;
import info.jtrac.domain.Item;
import info.jtrac.domain.ItemSearch;
import info.jtrac.domain.User;
import info.jtrac.util.AttachmentTextExtractor;
import info.jtrac.util.AttachmentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import java.io.File;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to poll and receive inbound inquiry emails from an IMAP mailbox,
 * authenticate sender permissions, query authorized tickets and attachments,
 * synthesize answers via local/cloud Ollama, send HTML replies, and purge processed messages.
 */
public class InboundMailReceiver {

    private static final Logger logger = LoggerFactory.getLogger(InboundMailReceiver.class);
    private static final Pattern REF_ID_PATTERN = Pattern.compile("(?i)\\b([A-Z0-9]+)-([0-9]+)\\b");

    private final Jtrac jtrac;
    private final MailSender mailSender;

    public InboundMailReceiver(Jtrac jtrac, MailSender mailSender) {
        this.jtrac = jtrac;
        this.mailSender = mailSender;
    }

    /**
     * Poll the IMAP inbox for unread inquiry messages and process them.
     *
     * @return count of processed messages
     */
    public int receiveAndProcess() {
        Map<String, String> config = jtrac.loadAllConfig();
        if (!"true".equalsIgnoreCase(config.get("mail.inbound.enabled"))) {
            logger.debug("Inbound mail query service is disabled (mail.inbound.enabled != true)");
            return 0;
        }

        String host = config.get("mail.inbound.server.host");
        String username = config.get("mail.inbound.username");
        String password = config.get("mail.inbound.password");
        if (!StringUtils.hasText(host) || !StringUtils.hasText(username)) {
            logger.warn("Inbound mail service is enabled, but server host or username is not configured.");
            return 0;
        }

        boolean sslEnable = "true".equalsIgnoreCase(config.get("mail.inbound.ssl.enable"));
        boolean starttlsEnable = "true".equalsIgnoreCase(config.get("mail.inbound.starttls.enable"));
        boolean sslTrustAll = "true".equalsIgnoreCase(config.get("mail.inbound.ssl.trust.all"));

        int port = 993;
        String portStr = config.get("mail.inbound.server.port");
        if (StringUtils.hasText(portStr)) {
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                port = sslEnable ? 993 : 143;
            }
        } else {
            port = sslEnable ? 993 : 143;
        }

        String protocol = sslEnable ? "imaps" : "imap";
        Properties props = new Properties();
        props.put("mail.store.protocol", protocol);
        props.put("mail." + protocol + ".host", host);
        props.put("mail." + protocol + ".port", String.valueOf(port));
        props.put("mail." + protocol + ".timeout", "30000");
        props.put("mail." + protocol + ".connectiontimeout", "30000");

        if (starttlsEnable) {
            props.put("mail." + protocol + ".starttls.enable", "true");
        }

        if (sslTrustAll) {
            props.put("mail." + protocol + ".ssl.trust", "*");
            props.put("mail." + protocol + ".ssl.checkserveridentity", "false");
            try {
                MailSSLSocketFactory sf = new MailSSLSocketFactory();
                sf.setTrustAllHosts(true);
                props.put("mail." + protocol + ".ssl.socketFactory", sf);
            } catch (GeneralSecurityException gse) {
                logger.warn("Could not set MailSSLSocketFactory for trusting all certificates: " + gse.getMessage());
            }
        }

        Store store = null;
        Folder inbox = null;
        int processedCount = 0;

        try {
            logger.debug("Connecting to IMAP mailbox at {}:{} ({}) as {}", host, port, protocol, username);
            Session session = Session.getInstance(props);
            store = session.getStore(protocol);
            store.connect(host, port, username, password != null ? password : "");

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            logger.info("Found {} unread messages in IMAP mailbox", messages.length);

            for (Message msg : messages) {
                try {
                    processSingleMessage(msg, config);
                    processedCount++;
                } catch (Exception e) {
                    logger.error("Error processing inbound message: " + e.getMessage(), e);
                }
            }

            // Close and purge messages marked DELETED
            inbox.close(true);
            inbox = null;
            store.close();
            store = null;
        } catch (Exception e) {
            logger.error("IMAP mail receiver connection error: " + e.getMessage(), e);
        } finally {
            if (inbox != null && inbox.isOpen()) {
                try {
                    inbox.close(false);
                } catch (Exception ignored) {
                }
            }
            if (store != null && store.isConnected()) {
                try {
                    store.close();
                } catch (Exception ignored) {
                }
            }
        }

        return processedCount;
    }

    /**
     * Processes a single inbound email message.
     */
    protected void processSingleMessage(Message msg, Map<String, String> config) throws Exception {
        Address[] froms = msg.getFrom();
        if (froms == null || froms.length == 0) {
            logger.warn("Inbound email has no sender, marking as SEEN");
            msg.setFlag(Flags.Flag.SEEN, true);
            return;
        }

        String senderEmail = null;
        if (froms[0] instanceof InternetAddress) {
            senderEmail = ((InternetAddress) froms[0]).getAddress();
        } else {
            senderEmail = froms[0].toString();
        }

        if (!StringUtils.hasText(senderEmail)) {
            logger.warn("Could not determine sender email address, marking SEEN");
            msg.setFlag(Flags.Flag.SEEN, true);
            return;
        }
        senderEmail = senderEmail.trim().toLowerCase();

        // 1. Authenticate sender against JTrac user database
        User user = jtrac.findUserByEmail(senderEmail);
        if (user == null || user.isLocked()) {
            logger.warn("Inbound email from unregistered or locked sender [{}], marking SEEN and ignoring.", senderEmail);
            msg.setFlag(Flags.Flag.SEEN, true);
            return;
        }

        Locale userLocale = (user.getLocale() != null)
                ? StringUtils.parseLocaleString(user.getLocale())
                : StringUtils.parseLocaleString(jtrac.getDefaultLocale());

        String subject = msg.getSubject() != null ? msg.getSubject() : "";
        String body = extractMessageContent(msg);

        logger.info("Processing authorized inquiry from user {} [{}] with subject '{}'", user.getLoginName(), senderEmail, subject);

        // 2. Scoped ticket & attachment retrieval
        List<Item> contextItems = retrieveAuthorizedTickets(user, subject, body);

        // 3. Ollama synthesis
        String ollamaUrl = config.get("llm.ollama.url");
        String ollamaModel = config.get("llm.ollama.model");
        String ollamaApiKey = config.get("llm.ollama.api.key");
        int timeout = 60;
        String timeoutStr = config.get("llm.ollama.timeout");
        if (StringUtils.hasText(timeoutStr)) {
            try {
                timeout = Integer.parseInt(timeoutStr.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        OllamaClient ollamaClient = new OllamaClient(ollamaUrl, ollamaModel, ollamaApiKey, timeout);
        String systemPrompt = OllamaPromptBuilder.buildSystemPrompt();
        String userPrompt = OllamaPromptBuilder.buildUserPrompt(subject, body, contextItems);

        String aiResponse = null;
        try {
            aiResponse = ollamaClient.chat(systemPrompt, userPrompt);
        } catch (Exception e) {
            logger.error("Ollama query failed for sender " + senderEmail + ": " + e.getMessage(), e);
            mailSender.sendAiOfflineNotice(senderEmail, subject, userLocale);
            msg.setFlag(Flags.Flag.DELETED, true);
            return;
        }

        // 4. Send formatted HTML response and mark original message for deletion
        mailSender.sendAiQueryResponse(senderEmail, subject, aiResponse, contextItems, userLocale, user.getSpaces());
        msg.setFlag(Flags.Flag.DELETED, true);
        logger.info("Successfully answered query from {} and marked original message for deletion", senderEmail);
    }

    /**
     * Queries up to 20 relevant tickets within user's authorized spaces (both open and closed),
     * and enriches tickets with comments and extracted attachment text.
     */
    protected List<Item> retrieveAuthorizedTickets(User user, String subject, String body) {
        Set<Long> matchedIds = new LinkedHashSet<>();
        List<Item> resultItems = new ArrayList<>();

        // Match explicit ticket reference IDs in subject or body (e.g. PROJ-123)
        String combinedText = subject + " " + (body != null ? body : "");
        Matcher matcher = REF_ID_PATTERN.matcher(combinedText);
        while (matcher.find()) {
            String refId = matcher.group(0).toUpperCase();
            try {
                Item item = jtrac.loadItemByRefId(refId);
                if (item != null && (user.isSuperUser() || user.isAllocatedToSpace(item.getSpace().getId()))) {
                    if (matchedIds.add(item.getId())) {
                        resultItems.add(item);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Lucene full-text and space-scoped search
        String searchQuery = extractSearchKeywords(subject, body);
        ItemSearch itemSearch = new ItemSearch(user);
        itemSearch.setPageSize(20);
        if (StringUtils.hasText(searchQuery)) {
            itemSearch.setSearchText(searchQuery);
        }

        List<Item> searchItems = jtrac.findItems(itemSearch);
        if (searchItems != null) {
            for (Item item : searchItems) {
                if (resultItems.size() >= 20) {
                    break;
                }
                if (matchedIds.add(item.getId())) {
                    resultItems.add(item);
                }
            }
        }

        // Enrich each item with full history and extracted attachment texts
        for (Item item : resultItems) {
            enrichItemWithAttachmentText(item);
        }

        return resultItems;
    }

    private void enrichItemWithAttachmentText(Item item) {
        if (item.getHistory() == null || item.getHistory().isEmpty()) {
            return;
        }
        for (History h : item.getHistory()) {
            if (h.getAttachment() != null && (!StringUtils.hasText(h.getAttachmentText()))) {
                try {
                    File file = AttachmentUtils.getFile(h.getAttachment(), item.getSpace().getId(), jtrac.getJtracHome());
                    if (file != null && file.exists()) {
                        String text = AttachmentTextExtractor.extractText(file,
                                jtrac.getAttachmentIndexMaxSizeInMb(),
                                jtrac.getAttachmentIndexMaxChars());
                        h.setAttachmentText(text);
                    }
                } catch (Throwable t) {
                    logger.debug("Failed extracting text for attachment {} on ticket {}", h.getAttachment().getFileName(), item.getRefId());
                }
            }
        }
    }

    private String extractSearchKeywords(String subject, String body) {
        if (StringUtils.hasText(subject)) {
            String clean = subject.replaceAll("(?i)\\b(re|fwd):\\s*", "").trim();
            if (clean.length() > 0) {
                return clean;
            }
        }
        if (StringUtils.hasText(body)) {
            String[] lines = body.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.length() > 2 && !trimmed.startsWith(">") && !trimmed.startsWith("--")) {
                    return trimmed;
                }
            }
        }
        return "";
    }

    /**
     * Extracts text content from a JavaMail Message Part.
     */
    protected String extractMessageContent(Part part) throws Exception {
        Object content = part.getContent();
        if (content instanceof String) {
            String str = (String) content;
            if (part.isMimeType("text/html") || str.toLowerCase().contains("<html") || str.toLowerCase().contains("<body")) {
                return str.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
            }
            return str;
        } else if (content instanceof Multipart) {
            Multipart mp = (Multipart) content;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                String partText = extractMessageContent(bp);
                if (partText != null && !partText.trim().isEmpty()) {
                    sb.append(partText).append("\n");
                }
            }
            return sb.toString().trim();
        }
        return "";
    }
}
