package info.jtrac.exporter;

import info.jtrac.exporter.config.ExportConfig;
import info.jtrac.exporter.html.HtmlGenerator;
import info.jtrac.exporter.model.AttachmentDto;
import info.jtrac.exporter.model.HistoryDto;
import info.jtrac.exporter.model.ItemDto;
import info.jtrac.exporter.model.SpaceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Streams generated static HTML pages and optional attachments directly into a ZipOutputStream.
 * Operates entirely in memory and on-the-fly, leaving no temporary files on disk.
 */
public class ZipStreamExporter {

    private static final Logger logger = LoggerFactory.getLogger(ZipStreamExporter.class);

    private final List<SpaceDto> spaces;
    private final ExportConfig config;
    private final boolean includeAttachments;
    private final File attachmentsDir;
    private final OutputStream outputStream;

    public ZipStreamExporter(List<SpaceDto> spaces, ExportConfig config, boolean includeAttachments,
                             File attachmentsDir, OutputStream outputStream) {
        this.spaces = spaces != null ? spaces : Collections.emptyList();
        this.config = config != null ? config : new ExportConfig();
        this.includeAttachments = includeAttachments;
        this.attachmentsDir = attachmentsDir;
        this.outputStream = outputStream;
    }

    public void export() throws IOException {
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        HtmlGenerator htmlGen = new HtmlGenerator(config);

        // 1. 寫入 index.html
        String indexHtml = htmlGen.generateIndexHtmlString(spaces);
        ZipEntry indexEntry = new ZipEntry("index.html");
        zos.putNextEntry(indexEntry);
        zos.write(indexHtml.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        // 2. 寫入各專案空間之 <SpacePrefix>.html
        for (SpaceDto space : spaces) {
            String spaceHtml = htmlGen.generateSpaceHtmlString(space, null);
            ZipEntry spaceEntry = new ZipEntry(space.getPrefixCode() + ".html");
            zos.putNextEntry(spaceEntry);
            zos.write(spaceHtml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        // 3. 處理實體附件（若使用者選擇包含且實體附件目錄存在）
        if (includeAttachments && attachmentsDir != null && attachmentsDir.exists()) {
            Set<String> addedAttachments = new HashSet<>();
            byte[] buffer = new byte[8192];

            for (SpaceDto space : spaces) {
                for (ItemDto item : space.getItems()) {
                    // 議題直接關聯附件
                    for (AttachmentDto att : item.getAttachmentList()) {
                        writeAttachmentToZip(zos, att, addedAttachments, buffer);
                    }
                    // 討論串歷史更新中之附件
                    for (HistoryDto history : item.getHistoryList()) {
                        if (history.getAttachment() != null) {
                            writeAttachmentToZip(zos, history.getAttachment(), addedAttachments, buffer);
                        }
                    }
                }
            }
        }

        zos.finish();
        zos.flush();
    }

    private void writeAttachmentToZip(ZipOutputStream zos, AttachmentDto att, Set<String> addedAttachments, byte[] buffer) {
        String physicalName = att.getPhysicalFileName();
        if (addedAttachments.contains(physicalName)) {
            return;
        }
        addedAttachments.add(physicalName);

        File srcFile = new File(attachmentsDir, physicalName);
        if (!srcFile.exists() || !srcFile.isFile()) {
            logger.warn("附件實體檔案不存在，略過打包: {}", srcFile.getAbsolutePath());
            return;
        }

        try {
            ZipEntry entry = new ZipEntry("attachments/" + physicalName);
            entry.setTime(srcFile.lastModified());
            zos.putNextEntry(entry);

            try (InputStream fis = new BufferedInputStream(new FileInputStream(srcFile))) {
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
            }
            zos.closeEntry();
        } catch (IOException e) {
            logger.error("寫入附件至 ZIP 串流失敗: " + physicalName, e);
        }
    }
}
