package info.jtrac.util;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SmartCharsetDetectorTest {

    @Test
    public void testUtf8WithoutBom() {
        String original = "JTrac 知識管理與追蹤系統 UTF-8 測試";
        byte[] bytes = original.getBytes(StandardCharsets.UTF_8);
        String decoded = SmartCharsetDetector.decode(bytes, 50000);
        Assert.assertEquals(original, decoded);
    }

    @Test
    public void testUtf8WithBom() {
        String original = "帶有 BOM 的 UTF-8 文件測試";
        byte[] raw = original.getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[raw.length + 3];
        withBom[0] = (byte) 0xEF;
        withBom[1] = (byte) 0xBB;
        withBom[2] = (byte) 0xBF;
        System.arraycopy(raw, 0, withBom, 3, raw.length);

        String decoded = SmartCharsetDetector.decode(withBom, 50000);
        Assert.assertEquals(original, decoded);
    }

    @Test
    public void testBig5Fallback() {
        if (!Charset.isSupported("Big5")) {
            return;
        }
        String original = "這是繁體中文 Big5 編碼測試，絕無亂碼。";
        byte[] bytes = original.getBytes(Charset.forName("Big5"));
        String decoded = SmartCharsetDetector.decode(bytes, 50000);
        Assert.assertEquals(original, decoded);
    }

    @Test
    public void testGbkFallback() {
        if (!Charset.isSupported("GBK")) {
            return;
        }
        String original = "这是简体中文 GBK 编码测试，没有任何乱码。";
        byte[] bytes = original.getBytes(Charset.forName("GBK"));
        String decoded = SmartCharsetDetector.decode(bytes, 50000);
        Assert.assertEquals(original, decoded);
    }

    @Test
    public void testTruncation() {
        String text = "1234567890ABCDEFGHIJ";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String decoded = SmartCharsetDetector.decode(bytes, 10);
        Assert.assertEquals("1234567890", decoded);
        Assert.assertEquals(10, decoded.length());
    }

    @Test
    public void testControlCharacterSanitization() {
        String rawWithControls = "Hello\u0000World\u0007Test\tEnd\nLine";
        byte[] bytes = rawWithControls.getBytes(StandardCharsets.UTF_8);
        String decoded = SmartCharsetDetector.decode(bytes, 50000);
        // Controls \u0000 and \u0007 replaced by spaces, \t and \n preserved
        Assert.assertTrue(decoded.contains("Hello"));
        Assert.assertTrue(decoded.contains("World"));
        Assert.assertTrue(decoded.contains("\t"));
        Assert.assertTrue(decoded.contains("\n"));
        Assert.assertFalse(decoded.contains("\u0000"));
        Assert.assertFalse(decoded.contains("\u0007"));
    }
}
