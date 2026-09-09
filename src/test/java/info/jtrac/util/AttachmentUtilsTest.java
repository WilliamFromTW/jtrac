package info.jtrac.util;

import info.jtrac.domain.Attachment;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class AttachmentUtilsTest {

    @Test
    public void testCleanFileName() {
        Assert.assertEquals("test.txt", AttachmentUtils.cleanFileName("C:\\foo\\bar\\test.txt"));
        Assert.assertEquals("test.txt", AttachmentUtils.cleanFileName("/usr/var/test.txt"));
        Assert.assertEquals("test.txt", AttachmentUtils.cleanFileName("test.txt"));
    }

    @Test
    public void testPartitionedPathAndDualReadFallback() throws Exception {
        File tempHome = Files.createTempDirectory("jtrac-home-test-").toFile();
        tempHome.deleteOnExit();

        Attachment att = new Attachment();
        att.setFilePrefix(101L);
        att.setFileName("spec.xlsx");

        // 1. Prepare target file in write path
        File target = AttachmentUtils.getAttachmentFileForWrite(att, 1L, tempHome.getAbsolutePath());
        Assert.assertTrue(target.getAbsolutePath().contains("1" + File.separator + "101_spec.xlsx"));
        Files.write(target.toPath(), "test content".getBytes());
        Assert.assertTrue(target.exists());

        // 2. Read with spaceId -> Should return partitioned file
        File resolved = AttachmentUtils.getFile(att, 1L, tempHome.getAbsolutePath());
        Assert.assertTrue(resolved.exists());
        Assert.assertEquals(target.getAbsolutePath(), resolved.getAbsolutePath());

        // 3. Read without spaceId -> Auto-discovery across subfolders should find it
        File resolvedAuto = AttachmentUtils.getFile(att, tempHome.getAbsolutePath());
        Assert.assertTrue(resolvedAuto.exists());
        Assert.assertEquals(target.getAbsolutePath(), resolvedAuto.getAbsolutePath());

        // 4. Test fallback to legacy flat root
        Attachment legacyAtt = new Attachment();
        legacyAtt.setFilePrefix(202L);
        legacyAtt.setFileName("legacy.txt");

        File legacyFlat = new File(tempHome, "attachments/" + legacyAtt.getFilePrefix() + "_" + legacyAtt.getFileName());
        Files.write(legacyFlat.toPath(), "legacy content".getBytes());

        // Read with spaceId (which does not exist yet in subfolder) -> Must fallback to flat root!
        File resolvedLegacy = AttachmentUtils.getFile(legacyAtt, 5L, tempHome.getAbsolutePath());
        Assert.assertTrue(resolvedLegacy.exists());
        Assert.assertEquals(legacyFlat.getAbsolutePath(), resolvedLegacy.getAbsolutePath());

        // 5. Test fallback to orphan quarantine directory
        Attachment orphanAtt = new Attachment();
        orphanAtt.setFilePrefix(303L);
        orphanAtt.setFileName("orphan.txt");

        File orphanDir = new File(tempHome, "attachments/0_ORPHAN");
        orphanDir.mkdirs();
        File orphanFile = new File(orphanDir, orphanAtt.getFilePrefix() + "_" + orphanAtt.getFileName());
        Files.write(orphanFile.toPath(), "orphan content".getBytes());

        File resolvedOrphan = AttachmentUtils.getFile(orphanAtt, 99L, tempHome.getAbsolutePath());
        Assert.assertTrue(resolvedOrphan.exists());
        Assert.assertEquals(orphanFile.getAbsolutePath(), resolvedOrphan.getAbsolutePath());
    }
}
