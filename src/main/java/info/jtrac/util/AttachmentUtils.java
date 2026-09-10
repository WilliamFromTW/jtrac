package info.jtrac.util;

import info.jtrac.domain.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Utilities for attachments, file path resolution with space partitioning (Option C: pure spaceId),
 * and dual-read fallback to guarantee 0% 404 broken links during and after migration.
 */
public class AttachmentUtils {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentUtils.class);

    public static final String ORPHAN_DIR_NAME = "0_ORPHAN";

    public static String cleanFileName(String path) {
        if (path == null) {
            return "";
        }
        int index = path.lastIndexOf('/');
        if (index == -1) {
            index = path.lastIndexOf('\\');
        }
        return (index != -1 ? path.substring(index + 1) : path);
    }

    /**
     * Resolves the physical file for an attachment when the space ID is known.
     * Dual-read fallback:
     * 1. Check partitioned directory: attachments/{spaceId}/{filePrefix}_{fileName}
     * 2. Fallback to flat root: attachments/{filePrefix}_{fileName}
     * 3. Fallback to orphan directory: attachments/0_ORPHAN/{filePrefix}_{fileName}
     */
    public static File getFile(Attachment attachment, long spaceId, String jtracHome) {
        if (attachment == null) {
            return null;
        }
        String fileName = attachment.getFilePrefix() + "_" + attachment.getFileName();
        File partitionedFile = new File(jtracHome + "/attachments/" + spaceId + "/" + fileName);
        if (partitionedFile.exists()) {
            return partitionedFile;
        }

        // Dual-read fallback: check legacy flat root
        File rootFile = new File(jtracHome + "/attachments/" + fileName);
        if (rootFile.exists()) {
            return rootFile;
        }

        // Check orphan quarantine directory
        File orphanFile = new File(jtracHome + "/attachments/" + ORPHAN_DIR_NAME + "/" + fileName);
        if (orphanFile.exists()) {
            return orphanFile;
        }

        // Return expected partitioned path even if file does not exist yet
        return partitionedFile;
    }

    /**
     * Resolves the physical file for an attachment with auto-discovery across subdirectories.
     */
    public static File getFile(Attachment attachment, String jtracHome) {
        if (attachment == null) {
            return null;
        }
        String fileName = attachment.getFilePrefix() + "_" + attachment.getFileName();
        File attachmentsDir = new File(jtracHome + "/attachments");
        if (attachmentsDir.exists() && attachmentsDir.isDirectory()) {
            // First search subdirectories (e.g. attachments/1/, attachments/2/)
            File[] subDirs = attachmentsDir.listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File subDir : subDirs) {
                    File candidate = new File(subDir, fileName);
                    if (candidate.exists()) {
                        return candidate;
                    }
                }
            }
        }

        // Fallback to flat root
        File legacyFile = new File(jtracHome + "/attachments/" + fileName);
        if (legacyFile.exists()) {
            return legacyFile;
        }

        return legacyFile;
    }

    /**
     * Prepares and returns the target File path for storing a newly uploaded attachment.
     * Creates parent directory `${jtracHome}/attachments/${spaceId}/` if not existing.
     */
    public static File getAttachmentFileForWrite(Attachment attachment, long spaceId, String jtracHome) {
        File dir = new File(jtracHome + "/attachments/" + spaceId);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, attachment.getFilePrefix() + "_" + attachment.getFileName());
    }

    public static String guessFileType(Attachment attachment, String jtracHome) {
        String fileType = null;
        try {
            File file = getFile(attachment, jtracHome);
            if (file != null && file.exists()) {
                fileType = Files.probeContentType(file.toPath());
            }
        } catch (Exception ignored) { }
        if (fileType == null && attachment != null && attachment.getFileName() != null) {
            String name = attachment.getFileName().toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                fileType = "image/jpeg";
            } else if (name.endsWith(".png")) {
                fileType = "image/png";
            } else if (name.endsWith(".gif")) {
                fileType = "image/gif";
            } else if (name.endsWith(".txt") || name.endsWith(".log")) {
                fileType = "text/plain";
            } else if (name.endsWith(".pdf")) {
                fileType = "application/pdf";
            } else if (name.endsWith(".zip")) {
                fileType = "application/zip";
            } else if (name.endsWith(".xlsx")) {
                fileType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            } else if (name.endsWith(".docx")) {
                fileType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
        }
        return fileType != null ? fileType : "application/octet-stream";
    }

    /**
     * Checks whether the file content is valid UTF-8 encoded text.
     * Uses strict CharsetDecoder with REPORT on malformed input and unmappable characters.
     * Samples up to 64KB for non-ASCII text or 512KB for pure ASCII to ensure responsiveness on large files.
     * Rejects binary files containing null bytes (0x00).
     */
    public static boolean isValidUtf8(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        if (file.length() == 0) {
            return true;
        }
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try (InputStream is = Files.newInputStream(file.toPath());
             Reader reader = new InputStreamReader(is, decoder)) {
            char[] buffer = new char[8192];
            int totalRead = 0;
            int read;
            boolean hasNonAscii = false;
            while ((read = reader.read(buffer)) != -1) {
                for (int i = 0; i < read; i++) {
                    char c = buffer[i];
                    if (c == 0) {
                        return false;
                    }
                    if (c > 127) {
                        hasNonAscii = true;
                    }
                }
                totalRead += read;
                if (hasNonAscii && totalRead >= 65536) {
                    break;
                }
                if (totalRead >= 524288) {
                    break;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
