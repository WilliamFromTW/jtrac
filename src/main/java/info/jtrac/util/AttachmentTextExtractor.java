package info.jtrac.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Text extractor for attachments with robust guardrails:
 * 1. Strict Whitelist (.xlsx, .docx, .pdf, .txt, .csv, .md, .log)
 * 2. Explicit Blacklist (.doc, .xls, .ppt, .zip, .exe, binaries)
 * 3. File size limit (default: 10MB) & max character extraction limit (default: 50,000 chars)
 * 4. Zero-dependency streaming OpenXML (.xlsx/.docx) parsing in standard UTF-8
 * 5. Smart charset detection for plain text and CSV
 * 6. Apache PDFBox integration for PDF documents
 */
public class AttachmentTextExtractor {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentTextExtractor.class);

    public static final int DEFAULT_MAX_SIZE_MB = 10;
    public static final int DEFAULT_MAX_CHARS = 50000;

    private static final Set<String> WHITELIST = new HashSet<String>(Arrays.asList(
            "xlsx", "docx", "pdf", "txt", "csv", "md", "log"
    ));

    private static final Set<String> BLACKLIST = new HashSet<String>(Arrays.asList(
            "doc", "xls", "ppt", "zip", "rar", "7z", "tar", "gz",
            "exe", "dll", "bin", "iso", "png", "jpg", "jpeg", "gif",
            "bmp", "webp", "mp3", "mp4", "avi", "mov", "wav", "class", "jar"
    ));

    /**
     * Checks if the given file extension is supported for full-text indexing.
     */
    public static boolean isSupported(String fileName) {
        if (fileName == null) {
            return false;
        }
        String ext = getExtension(fileName);
        if (ext.isEmpty() || BLACKLIST.contains(ext)) {
            return false;
        }
        return WHITELIST.contains(ext);
    }

    /**
     * Extract clean text content from an attachment file using default guardrail limits.
     */
    public static String extractText(File file) {
        return extractText(file, DEFAULT_MAX_SIZE_MB, DEFAULT_MAX_CHARS);
    }

    /**
     * Extract clean text content from an attachment file using specified limits.
     *
     * @param file          The file to extract from
     * @param maxSizeMb     Maximum allowed file size in MB
     * @param maxChars      Maximum number of characters to extract
     * @return Extracted plain text, or empty string if unsupported or failed
     */
    public static String extractText(File file, int maxSizeMb, int maxChars) {
        if (file == null || !file.exists() || !file.isFile()) {
            return "";
        }

        String fileName = file.getName();
        if (!isSupported(fileName)) {
            logger.debug("Skipping unsupported or blacklisted attachment: {}", fileName);
            return "";
        }

        long maxBytes = (long) (maxSizeMb > 0 ? maxSizeMb : DEFAULT_MAX_SIZE_MB) * 1024 * 1024;
        if (file.length() > maxBytes) {
            logger.info("Skipping attachment '{}': size ({} bytes) exceeds limit ({} MB)",
                    fileName, file.length(), maxSizeMb);
            return "";
        }

        int limitChars = maxChars > 0 ? maxChars : DEFAULT_MAX_CHARS;
        String ext = getExtension(fileName);

        try {
            if ("pdf".equals(ext)) {
                return extractPdfText(file, limitChars);
            } else if ("docx".equals(ext)) {
                return extractDocxText(file, limitChars);
            } else if ("xlsx".equals(ext)) {
                return extractXlsxText(file, limitChars);
            } else {
                // txt, csv, md, log
                return extractPlainText(file, limitChars);
            }
        } catch (Throwable t) {
            logger.warn("Non-fatal error extracting text from attachment '{}': {}", fileName, t.getMessage());
            return "";
        }
    }

    private static String extractPdfText(File file, int maxChars) throws Exception {
        try (PDDocument document = PDDocument.load(file)) {
            if (document.isEncrypted()) {
                logger.info("PDF attachment '{}' is encrypted/password-protected, skipping text extraction.", file.getName());
                return "";
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return truncate(text, maxChars);
        }
    }

    private static String extractDocxText(File file, int maxChars) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (InputStream fis = new FileInputStream(file);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry entry;
            XMLInputFactory xmlFactory = XMLInputFactory.newFactory();
            // Prevent XXE attacks
            xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

            while ((entry = zis.getNextEntry()) != null) {
                if ("word/document.xml".equalsIgnoreCase(entry.getName())) {
                    XMLStreamReader reader = xmlFactory.createXMLStreamReader(zis, "UTF-8");
                    while (reader.hasNext()) {
                        int event = reader.next();
                        if (event == XMLStreamConstants.CHARACTERS) {
                            String text = reader.getText();
                            if (text != null && !text.isEmpty()) {
                                sb.append(text).append(' ');
                                if (sb.length() >= maxChars) {
                                    break;
                                }
                            }
                        }
                    }
                    reader.close();
                    break;
                }
            }
        }
        return truncate(sb.toString(), maxChars);
    }

    private static String extractXlsxText(File file, int maxChars) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (InputStream fis = new FileInputStream(file);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry entry;
            XMLInputFactory xmlFactory = XMLInputFactory.newFactory();
            xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

            while ((entry = zis.getNextEntry()) != null) {
                if ("xl/sharedStrings.xml".equalsIgnoreCase(entry.getName())) {
                    XMLStreamReader reader = xmlFactory.createXMLStreamReader(zis, "UTF-8");
                    while (reader.hasNext()) {
                        int event = reader.next();
                        if (event == XMLStreamConstants.CHARACTERS) {
                            String text = reader.getText();
                            if (text != null && !text.isEmpty()) {
                                sb.append(text).append(' ');
                                if (sb.length() >= maxChars) {
                                    break;
                                }
                            }
                        }
                    }
                    reader.close();
                    break;
                }
            }
        }
        return truncate(sb.toString(), maxChars);
    }

    private static String extractPlainText(File file, int maxChars) throws Exception {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return SmartCharsetDetector.decode(bytes, maxChars);
    }

    private static String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot == -1 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String truncate(String text, int maxChars) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (maxChars > 0 && trimmed.length() > maxChars) {
            return trimmed.substring(0, maxChars).trim();
        }
        return trimmed;
    }
}
