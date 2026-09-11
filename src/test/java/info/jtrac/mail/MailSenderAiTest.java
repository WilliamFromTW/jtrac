package info.jtrac.mail;

import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import org.junit.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class MailSenderAiTest {

    @Test
    public void testSendAiQueryResponseDoesNotThrow() {
        Map<String, String> config = new HashMap<>();
        config.put("mail.server.host", "localhost");
        config.put("mail.from", "jtrac@example.com");
        config.put("jtrac.url.base", "http://localhost/jtrac/");

        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");

        MailSender mailSender = new MailSender(config, messageSource, "en");

        Space space = new Space();
        space.setId(1L);
        space.setPrefixCode("PROJ");
        space.setName("Project Alpha");

        Item item = new Item();
        item.setSpace(space);
        item.setSequenceNum(42);
        item.setSummary("Critical DB deadlock");
        item.setStatus(1);

        List<Item> items = new ArrayList<>();
        items.add(item);

        Set<Space> spaces = new HashSet<>();
        spaces.add(space);

        Map<String, String> perTicketSummaries = new HashMap<>();
        perTicketSummaries.put("PROJ-42", "LLM Staged summary for ticket PROJ-42");

        // Test with perTicketSummaries overload
        mailSender.sendAiQueryResponse("user@example.com", "Help needed",
                "Based on the analysis, [PROJ-42] caused the deadlock.", items, perTicketSummaries, Locale.ENGLISH, spaces);

        // Test Traditional Chinese
        mailSender.sendAiQueryResponse("user@example.com", "請問資料庫狀況",
                "根據分析，工單 [PROJ-42] 造成鎖死情況。", items, perTicketSummaries, Locale.TAIWAN, spaces);

        // Test null safety
        mailSender.sendAiQueryResponse(null, "Subject", "Content", Collections.emptyList(), Locale.ENGLISH, Collections.emptySet());
        mailSender.sendAiQueryResponse("no", "Subject", "Content", Collections.emptyList(), Locale.ENGLISH, Collections.emptySet());
    }

    @Test
    public void testBuildStandaloneHtmlReportContentAndStyles() {
        Map<String, String> config = new HashMap<>();
        config.put("mail.server.host", "localhost");
        config.put("mail.from", "jtrac@example.com");
        config.put("jtrac.url.base", "http://localhost/jtrac/");

        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");

        MailSender mailSender = new MailSender(config, messageSource, "en");

        Space space = new Space();
        space.setId(1L);
        space.setPrefixCode("PROJ");
        space.setName("Project Alpha");

        Item item = new Item();
        item.setSpace(space);
        item.setSequenceNum(42);
        item.setSummary("Database Connection Timeout");
        item.setDetail("PostgreSQL connection pool exhausted under load.");
        item.setStatus(1);

        info.jtrac.domain.History h = new info.jtrac.domain.History();
        h.setTimeStamp(new java.util.Date());
        h.setComment("Increased max_connections to 200.");
        h.setStatus(2);
        Set<info.jtrac.domain.History> historySet = new HashSet<>();
        historySet.add(h);
        item.setHistory(historySet);

        info.jtrac.domain.Attachment att = new info.jtrac.domain.Attachment();
        att.setFileName("postgres-log-dump.txt");
        Set<info.jtrac.domain.Attachment> attSet = new HashSet<>();
        attSet.add(att);
        item.setAttachments(attSet);

        List<Item> items = Collections.singletonList(item);
        Set<Space> spaces = Collections.singleton(space);

        Map<String, String> summaries = new HashMap<>();
        summaries.put("PROJ-42", "The DB connection pool was exhausted due to unclosed sessions.");

        String report = mailSender.buildStandaloneHtmlReport("DB Performance Issues",
                "### Root Cause\nPool exhaustion caused timeouts.\n\n| Param | Value |\n|---|---|\n| Pool | HikariCP |",
                items, summaries, Locale.TAIWAN, spaces, new java.util.Date());

        assertNotNull(report);
        assertTrue("Must have HTML5 doctype", report.contains("<!DOCTYPE html>"));
        assertTrue("Must have table border CSS", report.contains("border-collapse: collapse"));
        assertTrue("Must have border rule", report.contains("border: 1px solid"));
        assertTrue("Must support dark mode", report.contains("@media (prefers-color-scheme: dark)"));
        assertTrue("Must support print media", report.contains("@media print"));
        assertTrue("Must render markdown table", report.contains("<th>Param</th>") || report.contains("HikariCP"));
        assertTrue("Must include ticket summary", report.contains("Database Connection Timeout"));
        assertTrue("Must include ticket ref id", report.contains("PROJ-42"));
        assertTrue("Must include ticket description", report.contains("PostgreSQL connection pool exhausted"));
        assertTrue("Must include history comments", report.contains("Increased max_connections to 200."));
        assertTrue("Must include attachment name", report.contains("postgres-log-dump.txt"));
        assertTrue("Must include per-ticket digest", report.contains("The DB connection pool was exhausted"));
        assertTrue("Must use details accordion", report.contains("<details class='ticket-card'>"));
    }

    @Test
    public void testSendAiOfflineNoticeDoesNotThrow() {
        Map<String, String> config = new HashMap<>();
        config.put("mail.server.host", "localhost");
        config.put("mail.from", "jtrac@example.com");

        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");

        MailSender mailSender = new MailSender(config, messageSource, "en");

        mailSender.sendAiOfflineNotice("user@example.com", "Original question", Locale.ENGLISH);
        mailSender.sendAiOfflineNotice("user@example.com", "原始問題", Locale.TAIWAN);

        // Test null/empty recipient safety
        mailSender.sendAiOfflineNotice(null, "Subject", Locale.ENGLISH);
        mailSender.sendAiOfflineNotice("no", "Subject", Locale.ENGLISH);
    }
}
