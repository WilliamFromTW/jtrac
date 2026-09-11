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

        // Test English
        mailSender.sendAiQueryResponse("user@example.com", "Help needed",
                "Based on the analysis, [PROJ-42] caused the deadlock.", items, Locale.ENGLISH, spaces);

        // Test Traditional Chinese
        mailSender.sendAiQueryResponse("user@example.com", "請問資料庫狀況",
                "根據分析，工單 [PROJ-42] 造成鎖死情況。", items, Locale.TAIWAN, spaces);

        // Test null safety
        mailSender.sendAiQueryResponse(null, "Subject", "Content", Collections.emptyList(), Locale.ENGLISH, Collections.emptySet());
        mailSender.sendAiQueryResponse("no", "Subject", "Content", Collections.emptyList(), Locale.ENGLISH, Collections.emptySet());
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
