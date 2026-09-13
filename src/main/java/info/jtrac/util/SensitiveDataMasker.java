package info.jtrac.util;

import java.util.regex.Pattern;

/**
 * Utility to mask confidential credentials, passwords, tokens, and secret keys
 * with '***' while strictly preserving user accounts, login names, and system IDs.
 */
public final class SensitiveDataMasker {

    private SensitiveDataMasker() {
    }

    // Key-value password patterns: password=123, passwd: abc, pwd = xyz, 密碼：123, etc.
    // Preserves the key/delimiter, any trailing whitespace, and any quotes, masks only the secret value
    private static final Pattern KEY_VALUE_SECRET_PATTERN = Pattern.compile(
            "(?i)(?:^|\\b|(?<=[^a-zA-Z0-9_]))(password|passwd|pwd|pass|secret|api[_-]?key|access[_-]?token|bearer[_-]?token|auth[_-]?token|密碼|密钥|密匙)\\s*([:=：])(\\s*)([\"']?)([^\\s,;\"'<>]+)([\"']?)",
            Pattern.UNICODE_CASE
    );

    // Bearer token: Authorization: Bearer abcdef123456...
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
            "(?i)\\b(bearer\\s+)([a-zA-Z0-9_\\-\\.~+/=]{8,})"
    );

    // URL with username and password: e.g. https://admin:secret123@host:8080 or postgres://user:pass@host/db
    // Strictly preserves username! Masks password with ***
    private static final Pattern URL_CREDENTIALS_PATTERN = Pattern.compile(
            "(?i)\\b([a-zA-Z][a-zA-Z0-9+.-]*://[^:\\s/@]+:)([^@\\s]+)(@)"
    );

    // Private key blocks: -----BEGIN ... PRIVATE KEY----- ... -----END ... PRIVATE KEY-----
    private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile(
            "-----BEGIN [A-Z0-9 _-]+ PRIVATE KEY-----[\\s\\S]*?-----END [A-Z0-9 _-]+ PRIVATE KEY-----"
    );

    /**
     * Masks any sensitive credentials in the given string with '***'.
     * Usernames, account IDs, and non-secret configuration parameters remain untouched.
     *
     * @param text input string possibly containing confidential credentials
     * @return sanitized string with passwords and secrets masked
     */
    public static String maskSecrets(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // 1. Mask private key blocks
        result = PRIVATE_KEY_PATTERN.matcher(result).replaceAll("***");

        // 2. Mask URL credentials (preserve user, mask password)
        result = URL_CREDENTIALS_PATTERN.matcher(result).replaceAll("$1***$3");

        // 3. Mask Bearer tokens
        result = BEARER_TOKEN_PATTERN.matcher(result).replaceAll("$1***");

        // 4. Mask key-value secrets
        result = KEY_VALUE_SECRET_PATTERN.matcher(result).replaceAll("$1$2$3$4***$6");

        return result;
    }
}
