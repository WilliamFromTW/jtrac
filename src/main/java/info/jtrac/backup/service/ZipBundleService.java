package info.jtrac.backup.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import info.jtrac.backup.model.BackupManifest;
import info.jtrac.backup.model.SystemBackupData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility service for creating and extracting JTrac full backup ZIP archives.
 */
public class ZipBundleService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ObjectMapper objectMapper;

    public ZipBundleService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Bundle manifest, structured JSON data, and physical attachment files into a single ZIP stream.
     */
    public void createBackupZip(BackupManifest manifest, SystemBackupData data, String jtracHome, OutputStream out) throws IOException {
        logger.info("Starting ZIP bundle creation...");
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(out), StandardCharsets.UTF_8);

        try {
            // 1. Write manifest.json
            zos.putNextEntry(new ZipEntry("manifest.json"));
            byte[] manifestBytes = objectMapper.writeValueAsBytes(manifest);
            zos.write(manifestBytes);
            zos.closeEntry();

            // 2. Write data/system_data.json
            zos.putNextEntry(new ZipEntry("data/system_data.json"));
            byte[] dataBytes = objectMapper.writeValueAsBytes(data);
            zos.write(dataBytes);
            zos.closeEntry();

            // 3. Write physical attachments
            if (jtracHome != null) {
                File attachDir = new File(jtracHome, "attachments");
                if (attachDir.exists() && attachDir.isDirectory()) {
                    File[] files = attachDir.listFiles();
                    if (files != null) {
                        byte[] buffer = new byte[8192];
                        for (File f : files) {
                            if (f.isFile()) {
                                zos.putNextEntry(new ZipEntry("attachments/" + f.getName()));
                                try (InputStream fis = new BufferedInputStream(new FileInputStream(f))) {
                                    int len;
                                    while ((len = fis.read(buffer)) > 0) {
                                        zos.write(buffer, 0, len);
                                    }
                                }
                                zos.closeEntry();
                            }
                        }
                    }
                }
            }

            zos.finish();
            zos.flush();
            logger.info("ZIP bundle creation completed successfully.");
        } finally {
            // caller manages final close of target output stream if needed
        }
    }

    /**
     * Result of extracting a backup ZIP bundle.
     */
    public static class ZipExtractResult {
        private BackupManifest manifest;
        private SystemBackupData data;
        private File extractedDir;

        public ZipExtractResult(BackupManifest manifest, SystemBackupData data, File extractedDir) {
            this.manifest = manifest;
            this.data = data;
            this.extractedDir = extractedDir;
        }

        public BackupManifest getManifest() { return manifest; }
        public SystemBackupData getData() { return data; }
        public File getExtractedDir() { return extractedDir; }
    }

    /**
     * Extract and parse an uploaded backup ZIP archive.
     */
    public ZipExtractResult extractAndParseZip(InputStream in, File targetExtractDir) throws IOException {
        logger.info("Extracting and parsing backup ZIP to: {}", targetExtractDir.getAbsolutePath());
        if (!targetExtractDir.exists()) {
            targetExtractDir.mkdirs();
        }

        File manifestFile = null;
        File dataFile = null;

        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in), StandardCharsets.UTF_8);
        ZipEntry entry;
        byte[] buffer = new byte[8192];

        while ((entry = zis.getNextEntry()) != null) {
            String entryName = entry.getName().replace('\\', '/');

            // Security check against directory traversal (zip slip)
            File destFile = new File(targetExtractDir, entryName);
            String canonicalDest = destFile.getCanonicalPath();
            String canonicalTarget = targetExtractDir.getCanonicalPath();
            if (!canonicalDest.startsWith(canonicalTarget + File.separator) && !canonicalDest.equals(canonicalTarget)) {
                throw new IOException("Blocked unsafe zip entry path: " + entryName);
            }

            if (entry.isDirectory()) {
                destFile.mkdirs();
            } else {
                File parent = destFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(destFile))) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                if ("manifest.json".equals(entryName)) {
                    manifestFile = destFile;
                } else if ("data/system_data.json".equals(entryName)) {
                    dataFile = destFile;
                }
            }
            zis.closeEntry();
        }

        if (manifestFile == null || !manifestFile.exists()) {
            throw new IllegalArgumentException("Invalid backup bundle: manifest.json is missing");
        }
        if (dataFile == null || !dataFile.exists()) {
            throw new IllegalArgumentException("Invalid backup bundle: data/system_data.json is missing");
        }

        BackupManifest manifest = objectMapper.readValue(manifestFile, BackupManifest.class);
        SystemBackupData data = objectMapper.readValue(dataFile, SystemBackupData.class);

        logger.info("Successfully extracted and parsed backup bundle (JTrac version: {}, Operator: {})",
                manifest.getJtracVersion(), manifest.getOperatorLoginName());
        return new ZipExtractResult(manifest, data, targetExtractDir);
    }
}
