package info.jtrac.mail;

import info.jtrac.Jtrac;
import info.jtrac.domain.Attachment;
import info.jtrac.domain.History;
import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import org.junit.Test;

import java.io.File;

import javax.mail.Flags;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class InboundMailReceiverTest {

    @Test
    public void testExtractMessageContentPlainText() throws Exception {
        MimeMessage msg = new MimeMessage((Session) null);
        msg.setText("Hello JTrac Copilot, how are you?");

        InboundMailReceiver receiver = new InboundMailReceiver(null, null);
        String content = receiver.extractMessageContent(msg);
        assertEquals("Hello JTrac Copilot, how are you?", content);
    }

    @Test
    public void testExtractMessageContentHtml() throws Exception {
        MimeMessage msg = new MimeMessage((Session) null);
        msg.setContent("<html><body><p>Hello <b>world</b></p><br/><div>Status update</div></body></html>", "text/html");
        msg.saveChanges();

        InboundMailReceiver receiver = new InboundMailReceiver(null, null);
        String content = receiver.extractMessageContent(msg);
        assertTrue(content.contains("Hello world"));
        assertTrue(content.contains("Status update"));
        assertFalse(content.contains("<html>"));
    }

    @Test
    public void testExtractMessageContentMultipart() throws Exception {
        MimeMessage msg = new MimeMessage((Session) null);
        MimeMultipart mp = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Inquiry about ticket PROJ-101");
        mp.addBodyPart(textPart);

        msg.setContent(mp);
        msg.saveChanges();

        InboundMailReceiver receiver = new InboundMailReceiver(null, null);
        String content = receiver.extractMessageContent(msg);
        assertTrue(content.contains("Inquiry about ticket PROJ-101"));
    }

    @Test
    public void testUnauthorizedSenderMarkedSeen() throws Exception {
        Jtrac dummyJtrac = (Jtrac) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Jtrac.class},
                (proxy, method, args) -> {
                    if ("findUserByEmail".equals(method.getName())) {
                        return null; // Unregistered user
                    }
                    if ("getDefaultLocale".equals(method.getName())) {
                        return "en";
                    }
                    return null;
                }
        );

        InboundMailReceiver receiver = new InboundMailReceiver(dummyJtrac, null);

        MimeMessage msg = new MimeMessage((Session) null);
        msg.setFrom(new InternetAddress("stranger@spammer.com"));
        msg.setSubject("Spam query");
        msg.setText("Send me details");

        receiver.processSingleMessage(msg, Collections.emptyMap());

        assertTrue(msg.isSet(Flags.Flag.SEEN));
        assertFalse(msg.isSet(Flags.Flag.DELETED));
    }

    @Test
    public void testLockedSenderMarkedSeen() throws Exception {
        User lockedUser = new User();
        lockedUser.setLoginName("locked_john");
        lockedUser.setEmail("john@company.com");
        lockedUser.setLocked(true);

        Jtrac dummyJtrac = (Jtrac) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Jtrac.class},
                (proxy, method, args) -> {
                    if ("findUserByEmail".equals(method.getName())) {
                        return lockedUser;
                    }
                    if ("getDefaultLocale".equals(method.getName())) {
                        return "en";
                    }
                    return null;
                }
        );

        InboundMailReceiver receiver = new InboundMailReceiver(dummyJtrac, null);

        MimeMessage msg = new MimeMessage((Session) null);
        msg.setFrom(new InternetAddress("john@company.com"));
        msg.setSubject("Inquiry from locked user");
        msg.setText("Please answer");

        receiver.processSingleMessage(msg, Collections.emptyMap());

        assertTrue(msg.isSet(Flags.Flag.SEEN));
        assertFalse(msg.isSet(Flags.Flag.DELETED));
    }

    @Test
    public void testAuthorizedSenderOfflineNoticeAndPurge() throws Exception {
        User activeUser = new User();
        activeUser.setLoginName("alice");
        activeUser.setEmail("alice@company.com");
        activeUser.setLocked(false);

        Space space = new Space();
        space.setId(1L);
        space.setPrefixCode("PROJ");
        space.setName("Project Alpha");
        activeUser.addSpaceWithRole(space, "ROLE_ADMIN");

        Jtrac dummyJtrac = (Jtrac) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Jtrac.class},
                (proxy, method, args) -> {
                    if ("findUserByEmail".equals(method.getName())) {
                        return activeUser;
                    }
                    if ("getDefaultLocale".equals(method.getName())) {
                        return "en";
                    }
                    if ("findItems".equals(method.getName())) {
                        Item dummyItem = new Item();
                        dummyItem.setId(101L);
                        dummyItem.setSequenceNum(101);
                        dummyItem.setSpace(space);
                        dummyItem.setSummary("What is the status of PROJ-101?");
                        return Collections.singletonList(dummyItem);
                    }
                    return null;
                }
        );

        final AtomicBoolean offlineNoticeSent = new AtomicBoolean(false);
        MailSender mockMailSender = new MailSender(Collections.emptyMap(), null, "en") {
            @Override
            public void sendAiOfflineNotice(String toEmail, String originalSubject, java.util.Locale locale) {
                assertEquals("alice@company.com", toEmail);
                offlineNoticeSent.set(true);
            }
        };

        InboundMailReceiver receiver = new InboundMailReceiver(dummyJtrac, mockMailSender);

        MimeMessage msg = new MimeMessage((Session) null);
        msg.setFrom(new InternetAddress("alice@company.com"));
        msg.setSubject("What is the status of PROJ-101?");
        msg.setText("Can you tell me about the bug?");

        Map<String, String> config = new HashMap<>();
        // Point to an unavailable local port so it fails fast
        config.put("llm.ollama.url", "http://127.0.0.1:59999");
        config.put("llm.ollama.timeout", "1");

        receiver.processSingleMessage(msg, config);

        assertTrue("Offline notice should be triggered", offlineNoticeSent.get());
        assertTrue("Processed message must be marked DELETED for expunging", msg.isSet(Flags.Flag.DELETED));
    }

    @Test
    public void testAuthorizedSenderZeroHitNoticeAndPurge() throws Exception {
        User activeUser = new User();
        activeUser.setLoginName("alice");
        activeUser.setEmail("alice@company.com");
        activeUser.setLocked(false);

        Space space = new Space();
        space.setId(1L);
        space.setPrefixCode("PROJ");
        space.setName("Project Alpha");
        activeUser.addSpaceWithRole(space, "ROLE_ADMIN");

        Jtrac dummyJtrac = (Jtrac) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Jtrac.class},
                (proxy, method, args) -> {
                    if ("findUserByEmail".equals(method.getName())) {
                        return activeUser;
                    }
                    if ("getDefaultLocale".equals(method.getName())) {
                        return "en";
                    }
                    if ("findItems".equals(method.getName())) {
                        return Collections.emptyList();
                    }
                    return null;
                }
        );

        final AtomicBoolean zeroHitNoticeSent = new AtomicBoolean(false);
        MailSender mockMailSender = new MailSender(Collections.emptyMap(), null, "en") {
            @Override
            public void sendAiZeroHitNotice(String toEmail, String originalSubject, java.util.Locale locale, java.util.Set<Space> authorizedSpaces) {
                assertEquals("alice@company.com", toEmail);
                zeroHitNoticeSent.set(true);
            }
        };

        InboundMailReceiver receiver = new InboundMailReceiver(dummyJtrac, mockMailSender);

        MimeMessage msg = new MimeMessage((Session) null);
        msg.setFrom(new InternetAddress("alice@company.com"));
        msg.setSubject("Unknown issue query");
        msg.setText("Are there any tickets about XYZ?");

        Map<String, String> config = new HashMap<>();
        config.put("llm.ollama.url", "http://127.0.0.1:59999");
        config.put("llm.ollama.timeout", "1");

        receiver.processSingleMessage(msg, config);

        assertTrue("Zero-hit notice must be triggered when no tickets match", zeroHitNoticeSent.get());
        assertTrue("Processed message must be marked DELETED for expunging", msg.isSet(Flags.Flag.DELETED));
    }

    @Test
    public void testParseKeywordsFromJsonNormal() {
        InboundMailReceiver receiver = new InboundMailReceiver(null, null);
        String json = "{\"keywords\": [\"備份\", \"機制\", \"backup\", \"rule\"]}";
        List<String> keywords = receiver.parseKeywordsFromJson(json);
        assertEquals(4, keywords.size());
        assertTrue(keywords.contains("備份"));
        assertTrue(keywords.contains("backup"));
    }

    @Test
    public void testParseKeywordsFromJsonWithMarkdownAndSurroundingText() {
        InboundMailReceiver receiver = new InboundMailReceiver(null, null);
        String text = "Here are the extracted keywords:\n```json\n{\n  \"keywords\": [\"database\", \"connection\", \"pool\"]\n}\n```\nHope this helps!";
        List<String> keywords = receiver.parseKeywordsFromJson(text);
        assertEquals(3, keywords.size());
        assertTrue(keywords.contains("database"));
        assertTrue(keywords.contains("pool"));
    }

    @Test
    public void testParseKeywordsFiltersInjectionStopwords() {
        InboundMailReceiver receiver = new InboundMailReceiver(null, null);
        String attackJson = "{\"keywords\": [\"normal\", \"ignore\", \"system\", \"jailbreak\", \"prompt\", \"<script>alert(1)</script>\", \"drop\"]}";
        List<String> keywords = receiver.parseKeywordsFromJson(attackJson);
        assertTrue(keywords.contains("normal"));
        assertFalse(keywords.contains("ignore"));
        assertFalse(keywords.contains("system"));
        assertFalse(keywords.contains("jailbreak"));
        assertFalse(keywords.contains("prompt"));
        assertFalse(keywords.contains("drop"));
        assertFalse(keywords.contains("<script>alert(1)</script>"));
    }

    @Test
    public void testExtractHeuristicKeywords() {
        InboundMailReceiver receiver = new InboundMailReceiver(null, null);
        List<String> keywords = receiver.extractHeuristicKeywords("Re: [PROJ] 系統備份機制 backup rule", "請問自動備份的排程在哪裡設定？");
        assertTrue(keywords.contains("系統備份機制"));
        assertTrue(keywords.contains("backup"));
        assertTrue(keywords.contains("rule"));
    }

    @Test
    public void testCalculateRelevanceScoreAndBilingualBonus() {
        InboundMailReceiver receiver = new InboundMailReceiver(null, null);
        Item item = new Item();
        item.setSummary("Database 備份機制 與 backup strategy");
        item.setDetail("Here is the detailed backup rule.");
        InboundMailReceiver.ItemCandidate candidate = new InboundMailReceiver.ItemCandidate(item, 0);

        List<String> keywords = Arrays.asList("備份", "backup", "rule");
        receiver.calculateRelevanceScore(candidate, keywords);

        // Summary hits: "備份" (+3), "backup" (+3) -> 6
        // Detail hits: "backup" (+1), "rule" (+1) -> 2
        // Mixed language bonus: has "備份" (non-ASCII) and "backup" (ASCII) -> +5
        // Total score = 6 + 2 + 5 = 13
        assertEquals(13, candidate.getScore());
    }

    @Test
    public void testBuildFallbackTicketSummary() {
        InboundMailReceiver receiver = new InboundMailReceiver(null, null);
        Item item = new Item();
        item.setSummary("Critical memory leak in thread pool");
        item.setStatus(1);
        item.setDetail("Heap dump shows 2GB held by byte buffers.");

        History h = new History();
        Attachment att = new Attachment();
        att.setFileName("heapdump_analysis.txt");
        h.setAttachment(att);
        item.add(h);

        String fallback = receiver.buildFallbackTicketSummary(item);
        assertTrue(fallback.contains("Critical memory leak in thread pool"));
        assertTrue(fallback.contains("Heap dump shows 2GB held by byte buffers."));
        assertTrue(fallback.contains("heapdump_analysis.txt"));
    }

    @Test
    public void testProcessTicketsToStagingFile() throws Exception {
        InboundMailReceiver receiver = new InboundMailReceiver(null, null);

        Space space = new Space();
        space.setPrefixCode("PROJ");
        space.setName("Project Alpha");

        Item item = new Item();
        item.setSpace(space);
        item.setSequenceNum(777);
        item.setSummary("Network timeout on gateway");
        item.setStatus(1);
        item.setDetail("Gateway resets connection after 60s.");

        File tempStaging = File.createTempFile("test_staging_", ".md");
        tempStaging.deleteOnExit();

        String staged = receiver.processTicketsToStaging(null, Collections.singletonList(item), "Network error", "Why is it dropping?", tempStaging);

        assertTrue(staged.contains("# JTrac AI Query Staging Digest"));
        assertTrue(staged.contains("[PROJ-777]"));
        assertTrue(staged.contains("Network timeout on gateway"));
        assertTrue(tempStaging.exists());
        assertTrue(tempStaging.length() > 0);

        boolean deleted = tempStaging.delete();
        assertTrue(deleted);
    }

    @Test
    public void testProcessTicketsToStagingWithMultiSpaceOrdering() throws Exception {
        InboundMailReceiver receiver = new InboundMailReceiver(null, null);

        Space spaceA = new Space();
        spaceA.setPrefixCode("AAA");
        spaceA.setName("App Alpha");

        Space spaceZ = new Space();
        spaceZ.setPrefixCode("ZZZ");
        spaceZ.setName("Zeta Core");

        Item i1 = new Item();
        i1.setId(10L);
        i1.setSequenceNum(10);
        i1.setSpace(spaceZ);
        i1.setSummary("Zeta low issue");

        Item i2 = new Item();
        i2.setId(99L);
        i2.setSequenceNum(99);
        i2.setSpace(spaceZ);
        i2.setSummary("Zeta high issue");

        Item i3 = new Item();
        i3.setId(5L);
        i3.setSequenceNum(5);
        i3.setSpace(spaceA);
        i3.setSummary("Alpha low issue");

        Item i4 = new Item();
        i4.setId(50L);
        i4.setSequenceNum(50);
        i4.setSpace(spaceA);
        i4.setSummary("Alpha high issue");

        // Intentionally mixed order
        List<Item> unsorted = Arrays.asList(i1, i2, i3, i4);
        List<Item> sorted = MailSender.sortItemsBySpaceAndIdDesc(unsorted);

        File tempStaging = File.createTempFile("test_multispace_", ".md");
        tempStaging.deleteOnExit();

        String staged = receiver.processTicketsToStaging(null, sorted, "Multi-space query", "Details", tempStaging);

        // Verify order in digest: AAA-50, AAA-5, ZZZ-99, ZZZ-10
        int idxA50 = staged.indexOf("[AAA-50]");
        int idxA5 = staged.indexOf("[AAA-5]");
        int idxZ99 = staged.indexOf("[ZZZ-99]");
        int idxZ10 = staged.indexOf("[ZZZ-10]");

        assertTrue("AAA-50 must exist", idxA50 >= 0);
        assertTrue("AAA-5 must exist", idxA5 >= 0);
        assertTrue("ZZZ-99 must exist", idxZ99 >= 0);
        assertTrue("ZZZ-10 must exist", idxZ10 >= 0);

        assertTrue("AAA-50 should appear before AAA-5", idxA50 < idxA5);
        assertTrue("AAA-5 should appear before ZZZ-99", idxA5 < idxZ99);
        assertTrue("ZZZ-99 should appear before ZZZ-10", idxZ99 < idxZ10);

        tempStaging.delete();
    }
}
