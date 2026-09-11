package info.jtrac.mail;

import info.jtrac.Jtrac;
import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import org.junit.Test;

import javax.mail.Flags;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.lang.reflect.Proxy;
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
                        return Collections.emptyList();
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
}
