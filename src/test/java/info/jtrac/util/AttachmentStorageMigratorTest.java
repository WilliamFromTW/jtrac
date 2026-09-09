package info.jtrac.util;

import info.jtrac.JtracDao;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class AttachmentStorageMigratorTest {

    @Test
    public void testExtractFilePrefix() {
        Assert.assertEquals(Long.valueOf(101), AttachmentStorageMigrator.extractFilePrefix("101_spec.xlsx"));
        Assert.assertEquals(Long.valueOf(5), AttachmentStorageMigrator.extractFilePrefix("5_photo.png"));
        Assert.assertNull(AttachmentStorageMigrator.extractFilePrefix("invalid-format.txt"));
        Assert.assertNull(AttachmentStorageMigrator.extractFilePrefix(null));
        Assert.assertNull(AttachmentStorageMigrator.extractFilePrefix("_leadingUnderscore.txt"));
    }

    @Test
    public void testMigrationAndOrphanIsolation() throws Exception {
        File tempHome = Files.createTempDirectory("jtrac-migrator-test-").toFile();
        tempHome.deleteOnExit();

        File attachmentsDir = new File(tempHome, "attachments");
        attachmentsDir.mkdirs();

        // Create legacy flat files in attachments/
        File file1 = new File(attachmentsDir, "101_spec.xlsx");
        Files.write(file1.toPath(), "doc101 content".getBytes(StandardCharsets.UTF_8));

        File file2 = new File(attachmentsDir, "102_notes.txt");
        Files.write(file2.toPath(), "doc102 content".getBytes(StandardCharsets.UTF_8));

        // Orphan file 1: valid prefix but not in DB mapping
        File orphanFile1 = new File(attachmentsDir, "999_abandoned.pdf");
        Files.write(orphanFile1.toPath(), "orphan pdf content".getBytes(StandardCharsets.UTF_8));

        // Orphan file 2: invalid format (no prefix)
        File orphanFile2 = new File(attachmentsDir, "random.doc");
        Files.write(orphanFile2.toPath(), "orphan doc content".getBytes(StandardCharsets.UTF_8));

        // Setup dynamic proxy for JtracDao returning prefix -> spaceId mapping
        Map<Long, Long> prefixMap = new HashMap<>();
        prefixMap.put(101L, 1L); // 101 belongs to Space 1
        prefixMap.put(102L, 2L); // 102 belongs to Space 2

        JtracDao mockDao = (JtracDao) Proxy.newProxyInstance(
                JtracDao.class.getClassLoader(),
                new Class<?>[]{JtracDao.class},
                (proxy, method, args) -> {
                    if ("findAttachmentFilePrefixToSpaceIdMap".equals(method.getName())) {
                        return prefixMap;
                    }
                    return null;
                }
        );

        // Execute migration
        boolean migrated = AttachmentStorageMigrator.migrate(tempHome.getAbsolutePath(), mockDao);
        Assert.assertTrue(migrated);

        // Verify mapped files moved to space directories
        File migratedFile1 = new File(attachmentsDir, "1/101_spec.xlsx");
        Assert.assertTrue("File 101 should be moved to space 1", migratedFile1.exists());
        Assert.assertFalse("Original file 101 should not be in root", file1.exists());

        File migratedFile2 = new File(attachmentsDir, "2/102_notes.txt");
        Assert.assertTrue("File 102 should be moved to space 2", migratedFile2.exists());
        Assert.assertFalse("Original file 102 should not be in root", file2.exists());

        // Verify orphan files quarantined to 0_ORPHAN/
        File quarantined1 = new File(attachmentsDir, AttachmentUtils.ORPHAN_DIR_NAME + "/999_abandoned.pdf");
        Assert.assertTrue("Orphan 999 should be in 0_ORPHAN", quarantined1.exists());
        Assert.assertFalse("Original orphan 999 should not be in root", orphanFile1.exists());

        File quarantined2 = new File(attachmentsDir, AttachmentUtils.ORPHAN_DIR_NAME + "/random.doc");
        Assert.assertTrue("Random file should be in 0_ORPHAN", quarantined2.exists());
        Assert.assertFalse("Original random file should not be in root", orphanFile2.exists());

        // Verify marker file created
        File marker = new File(attachmentsDir, AttachmentStorageMigrator.MIGRATED_MARKER_FILE);
        Assert.assertTrue(marker.exists());

        // Verify second run skips when marker exists
        boolean secondRun = AttachmentStorageMigrator.migrate(tempHome.getAbsolutePath(), mockDao);
        Assert.assertFalse("Second run should skip migration because marker exists", secondRun);
    }
}
