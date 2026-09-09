package info.jtrac.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Intelligent character set detector and decoder for plain text, CSV, and log attachments.
 * Implements a 3-layer defense against mojibake (亂碼):
 * 1. Byte Order Mark (BOM) Detection
 * 2. Strict UTF-8 validation
 * 3. Smart fallback to system default / regional charsets (Big5, GBK, Shift-JIS, Windows-1252)
 */
public class SmartCharsetDetector {

    private static final Logger logger = LoggerFactory.getLogger(SmartCharsetDetector.class);

    private static final List<String> CANDIDATE_FALLBACK_CHARSETS = Arrays.asList(
            Charset.defaultCharset().name(),
            "Big5",
            "GB18030",
            "GBK",
            "Shift_JIS",
            "windows-1252",
            "ISO-8859-1"
    );

    /**
     * Detects charset and safely decodes the byte array into clean String.
     *
     * @param bytes    Raw file bytes
     * @param maxChars Maximum characters to return (<= 0 for no truncation)
     * @return Decoded clean text
     */
    public static String decode(byte[] bytes, int maxChars) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        // Layer 1: Check BOM
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return decodeWithCharset(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8, maxChars);
        }
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xFF) == 0xFF) {
            return decodeWithCharset(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE, maxChars);
        }
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xFE) {
            return decodeWithCharset(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE, maxChars);
        }

        // Layer 2: Strict UTF-8 verification
        int sampleLen = Math.min(bytes.length, 65536);
        try {
            CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            utf8Decoder.decode(ByteBuffer.wrap(bytes, 0, sampleLen));
            // Sample passed strict UTF-8 test, decode full content with UTF-8
            return decodeWithCharset(bytes, 0, bytes.length, StandardCharsets.UTF_8, maxChars);
        } catch (CharacterCodingException e) {
            logger.debug("Strict UTF-8 decoding failed, trying fallback charsets...");
        }

        // Layer 3: Smart fallback detection with heuristic scoring
        List<String> tried = new ArrayList<String>();
        String bestCharset = null;
        int bestScore = 0;

        for (String charsetName : CANDIDATE_FALLBACK_CHARSETS) {
            if (charsetName == null || tried.contains(charsetName.toUpperCase())) {
                continue;
            }
            tried.add(charsetName.toUpperCase());

            try {
                if (!Charset.isSupported(charsetName)) {
                    continue;
                }
                Charset charset = Charset.forName(charsetName);
                CharsetDecoder decoder = charset.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT);
                CharBuffer cb = decoder.decode(ByteBuffer.wrap(bytes, 0, sampleLen));
                int score = scoreText(cb.toString());
                if (score > bestScore) {
                    bestScore = score;
                    bestCharset = charsetName;
                }
            } catch (Exception ignored) {
                // Ignore decoding failures
            }
        }

        if (bestCharset != null) {
            logger.debug("Successfully detected charset '{}' with score {} for text content.", bestCharset, bestScore);
            return decodeWithCharset(bytes, 0, bytes.length, Charset.forName(bestCharset), maxChars);
        }

        // Layer 4: Absolute safe fallback with replacement
        CharsetDecoder lenientUtf8 = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        try {
            CharBuffer cb = lenientUtf8.decode(ByteBuffer.wrap(bytes));
            return truncateAndSanitize(cb.toString(), maxChars);
        } catch (Exception e) {
            logger.warn("Lenient decoding failed: {}", e.getMessage());
            return "";
        }
    }

    private static String decodeWithCharset(byte[] bytes, int offset, int length, Charset charset, int maxChars) {
        try {
            CharsetDecoder decoder = charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            CharBuffer cb = decoder.decode(ByteBuffer.wrap(bytes, offset, length));
            return truncateAndSanitize(cb.toString(), maxChars);
        } catch (Exception e) {
            logger.warn("Error decoding bytes with charset {}: {}", charset.name(), e.getMessage());
            return "";
        }
    }

    private static String truncateAndSanitize(String text, int maxChars) {
        if (text == null) {
            return "";
        }
        if (maxChars > 0 && text.length() > maxChars) {
            text = text.substring(0, maxChars);
        }

        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Keep tabs, newlines, carriage returns, and valid characters above 31
            if (c == '\t' || c == '\n' || c == '\r' || (c >= 32 && c != 127)) {
                sb.append(c);
            } else {
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }

    private static int scoreText(String text) {
        if (text == null || text.isEmpty()) {
            return -1;
        }
        int score = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\uFFFD') {
                return -1; // Contains unmappable replacement character
            }
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                score += 2;
            } else if (c >= 0x4E00 && c <= 0x9FFF) {
                // Common CJK Ideograph
                score += 5;
            } else if ((c >= 0x3000 && c <= 0x303F) || (c >= 0xFF01 && c <= 0xFF5E)) {
                // CJK Symbols / Punctuation
                score += 3;
            } else if (c >= 0xE000 && c <= 0xF8FF) {
                // Private Use Area -> penalty
                score -= 10;
            } else if (c < 32) {
                score -= 5;
            } else {
                score += 1;
            }
        }
        return score;
    }
}
