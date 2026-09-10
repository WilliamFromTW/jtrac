package info.jtrac.util;

import info.jtrac.domain.Attachment;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    @Test
    public void testIsValidUtf8() throws Exception {
        // 1. Non-existent and null file
        Assert.assertFalse(AttachmentUtils.isValidUtf8(null));
        Assert.assertFalse(AttachmentUtils.isValidUtf8(new File("non-existent-file-xyz.txt")));

        // 2. Empty file -> valid UTF-8
        File emptyFile = File.createTempFile("empty-", ".txt");
        emptyFile.deleteOnExit();
        Assert.assertTrue(AttachmentUtils.isValidUtf8(emptyFile));

        // 3. Pure ASCII text
        File asciiFile = File.createTempFile("ascii-", ".txt");
        asciiFile.deleteOnExit();
        Files.write(asciiFile.toPath(), "Hello world, this is pure ASCII text.\nLine 2.".getBytes(StandardCharsets.US_ASCII));
        Assert.assertTrue(AttachmentUtils.isValidUtf8(asciiFile));

        // 4. Valid UTF-8 text with Traditional Chinese
        File utf8File = File.createTempFile("utf8-", ".txt");
        utf8File.deleteOnExit();
        Files.write(utf8File.toPath(), "這是一段 UTF-8 繁體中文測試檔案。\n包括特殊符號：€、©、★。".getBytes(StandardCharsets.UTF_8));
        Assert.assertTrue(AttachmentUtils.isValidUtf8(utf8File));

        // 5. Big5 encoded text -> Should fail UTF-8 validation
        File big5File = File.createTempFile("big5-", ".txt");
        big5File.deleteOnExit();
        Files.write(big5File.toPath(), "這是一段 Big5 繁體中文測試檔案。".getBytes(Charset.forName("Big5")));
        Assert.assertFalse(AttachmentUtils.isValidUtf8(big5File));

        // 6. GBK encoded text -> Should fail UTF-8 validation
        File gbkFile = File.createTempFile("gbk-", ".txt");
        gbkFile.deleteOnExit();
        Files.write(gbkFile.toPath(), "这是一段 GBK 简体中文测试文件。".getBytes(Charset.forName("GBK")));
        Assert.assertFalse(AttachmentUtils.isValidUtf8(gbkFile));

        // 7. Binary file with NUL byte
        File binaryFile = File.createTempFile("binary-", ".bin");
        binaryFile.deleteOnExit();
        byte[] binaryData = new byte[] { 't', 'e', 's', 't', 0x00, 'b', 'i', 'n' };
        Files.write(binaryFile.toPath(), binaryData);
        Assert.assertFalse(AttachmentUtils.isValidUtf8(binaryFile));
    }
}
