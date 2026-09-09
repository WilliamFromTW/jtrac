package info.jtrac.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AttachmentTextExtractorTest {

    @Test
    public void testWhitelistAndBlacklistFilter() {
        // Whitelist
        Assert.assertTrue(AttachmentTextExtractor.isSupported("report.xlsx"));
        Assert.assertTrue(AttachmentTextExtractor.isSupported("spec.docx"));
        Assert.assertTrue(AttachmentTextExtractor.isSupported("manual.pdf"));
        Assert.assertTrue(AttachmentTextExtractor.isSupported("data.csv"));
        Assert.assertTrue(AttachmentTextExtractor.isSupported("notes.txt"));
        Assert.assertTrue(AttachmentTextExtractor.isSupported("README.md"));
        Assert.assertTrue(AttachmentTextExtractor.isSupported("server.log"));

        // Blacklist: legacy Office and binaries
        Assert.assertFalse(AttachmentTextExtractor.isSupported("legacy.doc"));
        Assert.assertFalse(AttachmentTextExtractor.isSupported("legacy.xls"));
        Assert.assertFalse(AttachmentTextExtractor.isSupported("presentation.ppt"));
        Assert.assertFalse(AttachmentTextExtractor.isSupported("package.zip"));
        Assert.assertFalse(AttachmentTextExtractor.isSupported("package.rar"));
        Assert.assertFalse(AttachmentTextExtractor.isSupported("installer.exe"));
        Assert.assertFalse(AttachmentTextExtractor.isSupported("image.png"));
        Assert.assertFalse(AttachmentTextExtractor.isSupported("photo.jpg"));
        Assert.assertFalse(AttachmentTextExtractor.isSupported(null));
        Assert.assertFalse(AttachmentTextExtractor.isSupported("no_extension"));
    }

    @Test
    public void testExtractPlainTextAndCsv() throws Exception {
        File txtFile = File.createTempFile("jtrac-test-", ".txt");
        txtFile.deleteOnExit();
        Files.write(txtFile.toPath(), "JTrac 全文檢索純文字抽取測試\n第二行內容".getBytes(StandardCharsets.UTF_8));

        String text = AttachmentTextExtractor.extractText(txtFile);
        Assert.assertTrue(text.contains("JTrac 全文檢索純文字抽取測試"));
        Assert.assertTrue(text.contains("第二行內容"));
    }

    @Test
    public void testExtractPdfText() throws Exception {
        File pdfFile = File.createTempFile("jtrac-test-", ".pdf");
        pdfFile.deleteOnExit();

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("JTrac PDF FullText Indexing Verification");
                cs.endText();
            }
            doc.save(pdfFile);
        }

        String extracted = AttachmentTextExtractor.extractText(pdfFile);
        Assert.assertTrue(extracted.contains("JTrac PDF FullText Indexing Verification"));
    }

    @Test
    public void testExtractDocxText() throws Exception {
        File docxFile = File.createTempFile("jtrac-test-", ".docx");
        docxFile.deleteOnExit();

        String documentXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n" +
                "  <w:body>\n" +
                "    <w:p><w:r><w:t>JTrac OpenXML Word Document Text Extraction</w:t></w:r></w:p>\n" +
                "  </w:body>\n" +
                "</w:document>";

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docxFile))) {
            zos.putNextEntry(new ZipEntry("word/document.xml"));
            zos.write(documentXml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        String extracted = AttachmentTextExtractor.extractText(docxFile);
        Assert.assertTrue(extracted.contains("JTrac OpenXML Word Document Text Extraction"));
    }

    @Test
    public void testExtractXlsxText() throws Exception {
        File xlsxFile = File.createTempFile("jtrac-test-", ".xlsx");
        xlsxFile.deleteOnExit();

        String sharedStringsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"2\" uniqueCount=\"2\">\n" +
                "  <si><t>Alpha Project Specification</t></si>\n" +
                "  <si><t>Beta Testing Metric 2026</t></si>\n" +
                "</sst>";

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(xlsxFile))) {
            zos.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
            zos.write(sharedStringsXml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        String extracted = AttachmentTextExtractor.extractText(xlsxFile);
        Assert.assertTrue(extracted.contains("Alpha Project Specification"));
        Assert.assertTrue(extracted.contains("Beta Testing Metric 2026"));
    }

    @Test
    public void testFileSizeGuardrail() throws Exception {
        File largeFile = File.createTempFile("jtrac-large-", ".txt");
        largeFile.deleteOnExit();

        byte[] chunk = new byte[1024 * 1024]; // 1MB
        Files.write(largeFile.toPath(), chunk);

        // When limit is 0MB (less than 1MB), should skip and return empty string
        String result = AttachmentTextExtractor.extractText(largeFile, 0, 100);
        Assert.assertEquals("", result);
    }

    @Test
    public void testBlacklistIgnoredGracefully() throws Exception {
        File docFile = File.createTempFile("jtrac-test-", ".doc");
        docFile.deleteOnExit();
        Files.write(docFile.toPath(), "legacy word content".getBytes(StandardCharsets.UTF_8));

        String result = AttachmentTextExtractor.extractText(docFile);
        Assert.assertEquals("", result);
    }

    @Test
    public void testCorruptedFileResilience() throws Exception {
        File corruptZip = File.createTempFile("jtrac-corrupt-", ".docx");
        corruptZip.deleteOnExit();
        Files.write(corruptZip.toPath(), "this is not a zip file".getBytes(StandardCharsets.UTF_8));

        // Must not throw any exception, should return empty string
        String result = AttachmentTextExtractor.extractText(corruptZip);
        Assert.assertEquals("", result);
    }
}
