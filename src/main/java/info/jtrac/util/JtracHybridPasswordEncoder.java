package info.jtrac.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 雙模密碼編碼器 (Hybrid PasswordEncoder)
 * 支援舊版 32 碼十六進位 MD5 雜湊比對，並以現代標準 BCrypt 進行新密碼加密與比對。
 */
public class JtracHybridPasswordEncoder implements PasswordEncoder {

    private static final Logger logger = LoggerFactory.getLogger(JtracHybridPasswordEncoder.class);

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        // 1. 若為 BCrypt 格式 ($2a$, $2b$, $2y$)
        if (isBcryptHash(encodedPassword)) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }

        // 2. 若為舊版 32 碼 MD5
        if (isMd5Hash(encodedPassword)) {
            String rawMd5 = md5Hex(rawPassword.toString());
            return rawMd5.equalsIgnoreCase(encodedPassword);
        }

        // 3. 例外回退相容 (例如明文密碼測試)
        return rawPassword.toString().equals(encodedPassword);
    }

    /**
     * 檢查密碼是否需要升級 (若非 BCrypt 則需要升級)
     */
    public boolean isUpgradeRequired(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        return !isBcryptHash(encodedPassword);
    }

    public static boolean isBcryptHash(String hash) {
        return hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) && hash.length() >= 59;
    }

    public static boolean isMd5Hash(String hash) {
        if (hash == null || hash.length() != 32) {
            return false;
        }
        for (int i = 0; i < 32; i++) {
            char c = hash.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return false;
            }
        }
        return true;
    }

    public static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }
}
