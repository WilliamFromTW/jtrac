package info.jtrac.util;

import info.jtrac.JtracDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Map;

/**
 * Migrates legacy flat attachments into space-partitioned directories (Option C: pure spaceId).
 * Any attachments without space association in the database are moved to attachments/0_ORPHAN/.
 * Once completed, a .attachment_migrated marker is created to avoid repeated scans.
 */
public class AttachmentStorageMigrator {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentStorageMigrator.class);

    public static final String MIGRATED_MARKER_FILE = ".attachment_migrated";

    public static synchronized boolean migrate(String jtracHome, JtracDao dao) {
        if (jtracHome == null || dao == null) {
            logger.warn("jtracHome or dao is null, skipping attachment storage migration");
            return false;
        }

        File attachmentsDir = new File(jtracHome, "attachments");
        if (!attachmentsDir.exists()) {
            attachmentsDir.mkdirs();
        }

        File marker = new File(attachmentsDir, MIGRATED_MARKER_FILE);
        if (marker.exists()) {
            logger.debug("Attachment storage migration marker exists, skipping migration");
            return false;
        }

        logger.info("Starting attachment storage migration to space-partitioned directories...");
        File[] flatFiles = attachmentsDir.listFiles(File::isFile);
        if (flatFiles == null || flatFiles.length == 0) {
            writeMarker(marker);
            logger.info("No legacy attachment files found in root attachments directory. Marked as migrated.");
            return true;
        }

        Map<Long, Long> prefixToSpaceMap = dao.findAttachmentFilePrefixToSpaceIdMap();
        int migratedCount = 0;
        int orphanCount = 0;

        for (File file : flatFiles) {
            String name = file.getName();
            if (name.equals(MIGRATED_MARKER_FILE) || name.startsWith(".")) {
                continue;
            }

            Long prefix = extractFilePrefix(name);
            File destDir;
            if (prefix != null && prefixToSpaceMap != null && prefixToSpaceMap.containsKey(prefix)) {
                Long spaceId = prefixToSpaceMap.get(prefix);
                destDir = new File(attachmentsDir, String.valueOf(spaceId));
                migratedCount++;
            } else {
                destDir = new File(attachmentsDir, AttachmentUtils.ORPHAN_DIR_NAME);
                orphanCount++;
            }

            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            File destFile = new File(destDir, name);
            try {
                moveFileSafely(file.toPath(), destFile.toPath());
            } catch (Exception e) {
                logger.error("Failed to migrate attachment file " + name + " to " + destFile.getAbsolutePath(), e);
            }
        }

        writeMarker(marker);
        logger.info("Attachment storage migration completed: {} migrated to space folders, {} quarantined to orphan folder",
                migratedCount, orphanCount);
        return true;
    }

    public static Long extractFilePrefix(String fileName) {
        if (fileName == null) {
            return null;
        }
        int idx = fileName.indexOf('_');
        if (idx > 0) {
            try {
                return Long.parseLong(fileName.substring(0, idx));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static void moveFileSafely(Path src, Path dest) throws IOException {
        try {
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Fallback for cross-device moves
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
            Files.delete(src);
        }
    }

    private static void writeMarker(File marker) {
        try {
            Files.write(marker.toPath(), ("migrated at " + new Date()).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.warn("Could not write attachment migration marker file: " + marker.getAbsolutePath(), e);
        }
    }
}
