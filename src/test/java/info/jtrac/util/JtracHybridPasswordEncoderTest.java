package info.jtrac.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JtracHybridPasswordEncoderTest {

    private final JtracHybridPasswordEncoder encoder = new JtracHybridPasswordEncoder();

    @Test
    public void testLegacyMd5Matching() {
        // "admin" MD5: 21232f297a57a5a743894a0e4a801fc3
        String md5Hash = "21232f297a57a5a743894a0e4a801fc3";
        assertTrue(encoder.matches("admin", md5Hash));
        assertFalse(encoder.matches("wrongpassword", md5Hash));
        assertTrue(encoder.isUpgradeRequired(md5Hash));
    }

    @Test
    public void testModernBcryptEncodingAndMatching() {
        String rawPassword = "securePassword123";
        String encoded = encoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$") || encoded.startsWith("$2b$"));
        assertTrue(encoder.matches(rawPassword, encoded));
        assertFalse(encoder.matches("wrongPassword", encoded));
        assertFalse(encoder.isUpgradeRequired(encoded));
    }

    @Test
    public void testNullAndEmptyInputs() {
        assertFalse(encoder.matches(null, "somehash"));
        assertFalse(encoder.matches("password", null));
        assertFalse(encoder.isUpgradeRequired(null));
        assertFalse(encoder.isUpgradeRequired(""));
    }
}
