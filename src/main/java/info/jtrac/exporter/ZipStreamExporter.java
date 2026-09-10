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
                        writeAttachmentToZip(zos, att, space.getId(), addedAttachments, buffer);
                    }
                    // 討論串歷史更新中之附件
                    for (HistoryDto history : item.getHistoryList()) {
                        if (history.getAttachment() != null) {
                            writeAttachmentToZip(zos, history.getAttachment(), space.getId(), addedAttachments, buffer);
                        }
                    }
                }
            }
        }

        zos.finish();
        zos.flush();
    }

    public static File resolveAttachmentFile(File baseDir, Long spaceId, String physicalName) {
        if (baseDir == null || !baseDir.exists()) {
            return null;
        }
        // 1. 優先檢查依 Space ID 分區之子目錄: attachments/{spaceId}/{physicalName}
        if (spaceId != null && spaceId > 0) {
            File partitioned = new File(new File(baseDir, String.valueOf(spaceId)), physicalName);
            if (partitioned.exists() && partitioned.isFile()) {
                return partitioned;
            }
        }
        // 2. 向下相容檢查平鋪根目錄: attachments/{physicalName}
        File flat = new File(baseDir, physicalName);
        if (flat.exists() && flat.isFile()) {
            return flat;
        }
        // 3. 檢查孤兒隔離目錄: attachments/0_ORPHAN/{physicalName}
        File orphan = new File(new File(baseDir, "0_ORPHAN"), physicalName);
        if (orphan.exists() && orphan.isFile()) {
            return orphan;
        }
        // 4. 自動探索備援: 掃描 attachments 目錄下所有子目錄
        File[] subDirs = baseDir.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                File candidate = new File(subDir, physicalName);
                if (candidate.exists() && candidate.isFile()) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private void writeAttachmentToZip(ZipOutputStream zos, AttachmentDto att, Long spaceId, Set<String> addedAttachments, byte[] buffer) {
        String physicalName = att.getPhysicalFileName();
        if (addedAttachments.contains(physicalName)) {
            return;
        }
        addedAttachments.add(physicalName);

        Long effectiveSpaceId = (spaceId != null && spaceId > 0) ? spaceId : att.getSpaceId();
        File srcFile = resolveAttachmentFile(attachmentsDir, effectiveSpaceId, physicalName);
        if (srcFile == null || !srcFile.exists() || !srcFile.isFile()) {
            logger.warn("附件實體檔案不存在，略過打包: spaceId={}, fileName={}", effectiveSpaceId, physicalName);
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
