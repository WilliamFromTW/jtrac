package info.jtrac.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class SensitiveDataMaskerTest {

    @Test
    public void testMaskSecretsNullAndEmpty() {
        assertNull(SensitiveDataMasker.maskSecrets(null));
        assertEquals("", SensitiveDataMasker.maskSecrets(""));
    }

    @Test
    public void testMaskKeyValuePasswordsPreservingUsername() {
        String input = "Connecting with user: admin, password: MySecretPassword123, status: ok";
        String masked = SensitiveDataMasker.maskSecrets(input);

        assertTrue("Username admin must be preserved", masked.contains("user: admin"));
        assertFalse("Real password must not be present", masked.contains("MySecretPassword123"));
        assertTrue("Password must be masked with ***", masked.contains("password: ***"));
    }

    @Test
    public void testMaskChineseKeywordsAndQuotedPasswords() {
        String input = "系統帳號：operator99，密碼：'SecretPass!@#'，連線正常";
        String masked = SensitiveDataMasker.maskSecrets(input);

        assertTrue("Account operator99 must be preserved", masked.contains("系統帳號：operator99"));
        assertFalse("Real password must not be present", masked.contains("SecretPass!@#"));
        assertTrue("Password must be masked with ***", masked.contains("密碼：'***'"));
    }

    @Test
    public void testMaskUrlCredentialsPreservingUser() {
        String input = "Database URL: postgres://svc_billing:SuperSecretPass@192.168.1.10:5432/finance_db";
        String masked = SensitiveDataMasker.maskSecrets(input);

        assertTrue("User svc_billing must be preserved", masked.contains("postgres://svc_billing:***@192.168.1.10:5432/finance_db"));
        assertFalse("SuperSecretPass must not be present", masked.contains("SuperSecretPass"));
    }

    @Test
    public void testMaskBearerTokenAndApiKey() {
        String input = "Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.test and api_key=sk-proj-999888777";
        String masked = SensitiveDataMasker.maskSecrets(input);

        assertFalse("Bearer token must be masked", masked.contains("eyJhbGciOiJIUzI1NiJ9.test"));
        assertTrue("Bearer must have ***", masked.contains("Bearer ***"));

        assertFalse("api_key secret must be masked", masked.contains("sk-proj-999888777"));
        assertTrue("api_key must have ***", masked.contains("api_key=***"));
    }

    @Test
    public void testMaskPrivateKeyBlock() {
        String input = "Before\n-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEA0m...\n-----END RSA PRIVATE KEY-----\nAfter";
        String masked = SensitiveDataMasker.maskSecrets(input);

        assertTrue("Private key must be replaced with ***", masked.contains("Before\n***\nAfter"));
        assertFalse("Private key content must not be present", masked.contains("MIIEowIBAAKCAQEA0m"));
    }
}
