package info.jtrac.mail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Arrays;
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
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<String> INJECTION_STOPWORDS = new HashSet<>(Arrays.asList(
            "system", "prompt", "instruction", "instructions", "ignore", "jailbreak",
            "untrusted_user_query", "untrusted_ticket_context", "roleplay", "assistant",
            "password", "secret", "override", "bypass", "drop", "delete", "insert", "update"
    ));

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

        // Prepare Ollama client configuration
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

        int maxTickets = 50;
        String maxTicketsStr = config.get("llm.retrieval.max_tickets");
        if (StringUtils.hasText(maxTicketsStr)) {
            try {
                maxTickets = Integer.parseInt(maxTicketsStr.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        // 2. Phase 1: LLM-driven query expansion (with graceful heuristic fallback)
        List<String> keywords = extractKeywordsWithLlm(ollamaClient, subject, body);
        logger.info("Extracted query expansion keywords: {}", keywords);

        // 3. Phase 2: Programmatic hybrid weighted retrieval & ranking
        List<Item> contextItems = retrieveAuthorizedTickets(user, subject, body, keywords, maxTickets);

        // 4. Phase 3: Ollama synthesis
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

        // 5. Send formatted HTML response and mark original message for deletion
        mailSender.sendAiQueryResponse(senderEmail, subject, aiResponse, contextItems, userLocale, user.getSpaces());
        msg.setFlag(Flags.Flag.DELETED, true);
        logger.info("Successfully answered query from {} and marked original message for deletion", senderEmail);
    }

    /**
     * Phase 1: Invokes LLM to extract keywords in the query's original language and English translation/synonyms.
     * Defensively parses JSON and falls back to heuristic tokenization if LLM is unavailable or fails.
     */
    protected List<String> extractKeywordsWithLlm(OllamaClient ollamaClient, String subject, String body) {
        try {
            String systemPrompt = OllamaPromptBuilder.buildKeywordExtractionSystemPrompt();
            String userPrompt = OllamaPromptBuilder.buildKeywordExtractionUserPrompt(subject, body);
            String response = ollamaClient.chat(systemPrompt, userPrompt);
            List<String> keywords = parseKeywordsFromJson(response);
            if (keywords != null && !keywords.isEmpty()) {
                return keywords;
            }
        } catch (Exception e) {
            logger.warn("LLM keyword extraction failed or timed out, falling back to heuristic extraction: {}", e.getMessage());
        }
        return extractHeuristicKeywords(subject, body);
    }

    /**
     * Parses the LLM JSON response {"keywords": ["kw1", "kw2", ...]} safely with Jackson.
     */
    protected List<String> parseKeywordsFromJson(String response) {
        if (!StringUtils.hasText(response)) {
            return Collections.emptyList();
        }
        try {
            String jsonStr = response.trim();
            if (jsonStr.contains("```json")) {
                int start = jsonStr.indexOf("```json") + 7;
                int end = jsonStr.indexOf("```", start);
                if (end > start) {
                    jsonStr = jsonStr.substring(start, end).trim();
                }
            } else if (jsonStr.contains("```")) {
                int start = jsonStr.indexOf("```") + 3;
                int end = jsonStr.indexOf("```", start);
                if (end > start) {
                    jsonStr = jsonStr.substring(start, end).trim();
                }
            }

            int firstBrace = jsonStr.indexOf('{');
            int lastBrace = jsonStr.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                jsonStr = jsonStr.substring(firstBrace, lastBrace + 1);
            }

            JsonNode root = objectMapper.readTree(jsonStr);
            JsonNode keywordsNode = root.get("keywords");
            if (keywordsNode != null && keywordsNode.isArray()) {
                List<String> result = new ArrayList<>();
                Set<String> seen = new HashSet<>();
                for (JsonNode item : keywordsNode) {
                    if (item != null && item.isTextual()) {
                        String kw = item.asText().trim();
                        kw = kw.replaceAll("^[\\\"'\\s.,;]+|[\\\"'\\s.,;]+$", "").trim();
                        if (isValidKeyword(kw) && seen.add(kw.toLowerCase())) {
                            result.add(kw);
                        }
                    }
                }
                return result;
            }
        } catch (Exception e) {
            logger.debug("Failed to parse keywords JSON from LLM: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Validates and sanitizes individual keyword candidate.
     */
    protected static boolean isValidKeyword(String kw) {
        if (!StringUtils.hasText(kw)) {
            return false;
        }
        String trimmed = kw.trim();
        if (trimmed.length() < 1 || trimmed.length() > 50) {
            return false;
        }
        if (trimmed.contains("\n") || trimmed.contains("\r") || trimmed.contains("<") || trimmed.contains(">") || trimmed.contains("{") || trimmed.contains("}")) {
            return false;
        }
        if (INJECTION_STOPWORDS.contains(trimmed.toLowerCase())) {
            return false;
        }
        return true;
    }

    /**
     * Heuristic tokenization fallback extracting keywords from subject and body.
     */
    protected List<String> extractHeuristicKeywords(String subject, String body) {
        List<String> keywords = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        String cleanSubject = "";
        if (StringUtils.hasText(subject)) {
            cleanSubject = subject.replaceAll("(?i)\\b(re|fwd|fw):\\s*", "").trim();
        }

        if (StringUtils.hasText(cleanSubject)) {
            for (String token : cleanSubject.split("[\\s,;:.!?，。！？\\[\\]()（）/\\\\]+")) {
                String t = token.replaceAll("^[\\\"'\\s.,;]+|[\\\"'\\s.,;]+$", "").trim();
                if (isValidKeyword(t) && seen.add(t.toLowerCase())) {
                    keywords.add(t);
                }
            }
        }

        if (StringUtils.hasText(body)) {
            String[] lines = body.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.length() > 2 && !trimmed.startsWith(">") && !trimmed.startsWith("--")) {
                    for (String token : trimmed.split("[\\s,;:.!?，。！？\\[\\]()（）/\\\\]+")) {
                        String t = token.replaceAll("^[\\\"'\\s.,;]+|[\\\"'\\s.,;]+$", "").trim();
                        if (isValidKeyword(t) && seen.add(t.toLowerCase())) {
                            keywords.add(t);
                            if (keywords.size() >= 10) {
                                break;
                            }
                        }
                    }
                    if (keywords.size() >= 10) {
                        break;
                    }
                }
            }
        }

        return keywords;
    }

    /**
     * Backward-compatible ticket retrieval delegating to expanded retrieval.
     */
    public List<Item> retrieveAuthorizedTickets(User user, String subject, String body) {
        return retrieveAuthorizedTickets(user, subject, body, extractHeuristicKeywords(subject, body), 50);
    }

    /**
     * Phase 2: Programmatic hybrid weighted retrieval & ranking.
     * Searches up to maxTickets via Lucene, scores candidates based on keyword hits,
     * rewards bilingual matches, and returns the top 20 tickets enriched with attachment content.
     */
    public List<Item> retrieveAuthorizedTickets(User user, String subject, String body, List<String> keywords, int maxTickets) {
        if (maxTickets <= 0) {
            maxTickets = 50;
        }

        Set<Long> matchedIds = new HashSet<>();
        List<ItemCandidate> candidates = new ArrayList<>();

        // 1. Match explicit ticket reference IDs in subject or body (e.g. PROJ-123)
        String combinedText = subject + " " + (body != null ? body : "");
        Matcher matcher = REF_ID_PATTERN.matcher(combinedText);
        while (matcher.find()) {
            String refId = matcher.group(0).toUpperCase();
            try {
                Item item = jtrac.loadItemByRefId(refId);
                if (item != null && (user.isSuperUser() || user.isAllocatedToSpace(item.getSpace().getId()))) {
                    if (matchedIds.add(item.getId())) {
                        candidates.add(new ItemCandidate(item, 100)); // Base 100 for exact RefId match
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Lucene full-text search across authorized spaces using expanded keywords
        String searchQuery = (keywords != null && !keywords.isEmpty()) ? String.join(" ", keywords) : extractSearchKeywords(subject, body);
        if (StringUtils.hasText(searchQuery)) {
            ItemSearch itemSearch = new ItemSearch(user);
            itemSearch.setPageSize(maxTickets);
            itemSearch.setSearchText(searchQuery);

            List<Item> searchItems = jtrac.findItems(itemSearch);
            if (searchItems != null) {
                for (Item item : searchItems) {
                    if (matchedIds.add(item.getId())) {
                        candidates.add(new ItemCandidate(item, 0));
                    }
                }
            }
        }

        // 3. Score and rank candidates
        List<String> effectiveKeywords = (keywords != null && !keywords.isEmpty())
                ? keywords
                : extractHeuristicKeywords(subject, body);

        for (ItemCandidate candidate : candidates) {
            calculateRelevanceScore(candidate, effectiveKeywords);
        }

        // Sort descending by score
        candidates.sort((a, b) -> Integer.compare(b.score, a.score));

        // 4. Select top candidates (up to 20) for LLM context, and enrich with attachment text
        int topLimit = Math.min(20, candidates.size());
        List<Item> resultItems = new ArrayList<>(topLimit);
        for (int i = 0; i < topLimit; i++) {
            Item item = candidates.get(i).item;
            enrichItemWithAttachmentText(item);
            resultItems.add(item);
        }

        return resultItems;
    }

    protected void calculateRelevanceScore(ItemCandidate candidate, List<String> keywords) {
        if (candidate == null || candidate.item == null || keywords == null || keywords.isEmpty()) {
            return;
        }
        Item item = candidate.item;
        String summary = item.getSummary() != null ? item.getSummary().toLowerCase() : "";
        String detail = item.getDetail() != null ? item.getDetail().toLowerCase() : "";

        StringBuilder commentsBuilder = new StringBuilder();
        StringBuilder attachmentTextBuilder = new StringBuilder();
        if (item.getHistory() != null) {
            for (History h : item.getHistory()) {
                if (h.getComment() != null) {
                    commentsBuilder.append(" ").append(h.getComment().toLowerCase());
                }
                if (h.getAttachmentText() != null) {
                    attachmentTextBuilder.append(" ").append(h.getAttachmentText().toLowerCase());
                }
            }
        }
        String comments = commentsBuilder.toString();
        String attachments = attachmentTextBuilder.toString();

        boolean hasAsciiMatch = false;
        boolean hasNonAsciiMatch = false;

        for (String kw : keywords) {
            if (!StringUtils.hasText(kw)) {
                continue;
            }
            String kwLower = kw.trim().toLowerCase();
            boolean matched = false;

            if (summary.contains(kwLower)) {
                candidate.score += 3;
                matched = true;
            }
            if (detail.contains(kwLower)) {
                candidate.score += 1;
                matched = true;
            }
            if (comments.contains(kwLower)) {
                candidate.score += 1;
                matched = true;
            }
            if (attachments.contains(kwLower)) {
                candidate.score += 1;
                matched = true;
            }

            if (matched) {
                if (isPureAscii(kwLower)) {
                    hasAsciiMatch = true;
                } else {
                    hasNonAsciiMatch = true;
                }
            }
        }

        // Mixed-language bonus: reward cross-lingual matches across original language and English
        if (hasAsciiMatch && hasNonAsciiMatch) {
            candidate.score += 5;
        }
    }

    private static boolean isPureAscii(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    protected static class ItemCandidate {
        final Item item;
        int score;

        public ItemCandidate(Item item, int score) {
            this.item = item;
            this.score = score;
        }

        public Item getItem() {
            return item;
        }

        public int getScore() {
            return score;
        }
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
