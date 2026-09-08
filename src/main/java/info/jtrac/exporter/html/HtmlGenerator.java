package info.jtrac.exporter.html;

import info.jtrac.exporter.config.ExportConfig;
import info.jtrac.exporter.i18n.I18nMessages;
import info.jtrac.exporter.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class HtmlGenerator {

    private final ExportConfig config;
    private final I18nMessages i18n;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Set<String> copiedAttachments = new HashSet<>();

    public HtmlGenerator(ExportConfig config) {
        this.config = config;
        this.i18n = new I18nMessages(config.getLang());
    }

    public void generate(List<SpaceDto> spaces) throws IOException {
        File outDir = config.getOutputDir();
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        File attachmentsOutDir = new File(outDir, "attachments");
        if (!attachmentsOutDir.exists()) {
            attachmentsOutDir.mkdirs();
        }

        // 1. 產生 index.html
        generateIndexHtml(spaces, outDir);

        // 2. 產生各 Space 討論串頁面
        for (SpaceDto space : spaces) {
            generateSpaceHtml(space, outDir, attachmentsOutDir);
        }

        System.out.println("HTML 靜態報表產出完成！輸出目錄: " + outDir.getAbsolutePath());
    }

    public String generateIndexHtmlString(List<SpaceDto> spaces) {
        StringBuilder sb = new StringBuilder();

        int totalIssues = 0;
        for (SpaceDto s : spaces) {
            totalIssues += s.getItems().size();
        }

        appendHtmlHeader(sb, i18n.get("nav.spaces") + " - " + i18n.get("app.title"));

        sb.append("<div class='container'>\n");
        sb.append("  <header class='header-bar'>\n");
        sb.append("    <div class='header-top'>\n");
        sb.append("      <h1>🏷️ ").append(HtmlEscaper.escape(i18n.get("app.title"))).append("</h1>\n");
        sb.append("      <button type='button' class='btn-theme' onclick='toggleTheme()' title='")
                .append(HtmlEscaper.escape(i18n.get("action.toggle_theme"))).append("'>🌓 ")
                .append(HtmlEscaper.escape(i18n.get("action.toggle_theme"))).append("</button>\n");
        sb.append("    </div>\n");
        sb.append("    <p class='subtitle'>").append(HtmlEscaper.escape(i18n.get("nav.spaces"))).append("</p>\n");
        sb.append("  </header>\n");

        sb.append("  <div class='stats-grid'>\n");
        sb.append("    <div class='stat-card'>\n");
        sb.append("      <div class='stat-val'>").append(spaces.size()).append("</div>\n");
        sb.append("      <div class='stat-label'>").append(HtmlEscaper.escape(i18n.get("nav.spaces"))).append("</div>\n");
        sb.append("    </div>\n");
        sb.append("    <div class='stat-card'>\n");
        sb.append("      <div class='stat-val'>").append(totalIssues).append("</div>\n");
        sb.append("      <div class='stat-label'>").append(HtmlEscaper.escape(i18n.get("table.total_items"))).append("</div>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        sb.append("  <div class='card'>\n");
        sb.append("    <table class='data-table'>\n");
        sb.append("      <thead>\n");
        sb.append("        <tr>\n");
        sb.append("          <th width='15%'>").append(HtmlEscaper.escape(i18n.get("table.prefix"))).append("</th>\n");
        sb.append("          <th width='30%'>").append(HtmlEscaper.escape(i18n.get("table.name"))).append("</th>\n");
        sb.append("          <th>").append(HtmlEscaper.escape(i18n.get("table.description"))).append("</th>\n");
        sb.append("          <th width='12%' class='text-center'>").append(HtmlEscaper.escape(i18n.get("table.total_items"))).append("</th>\n");
        sb.append("          <th width='15%' class='text-center'>").append(HtmlEscaper.escape(i18n.get("table.actions"))).append("</th>\n");
        sb.append("        </tr>\n");
        sb.append("      </thead>\n");
        sb.append("      <tbody>\n");

        if (spaces.isEmpty()) {
            sb.append("        <tr><td colspan='5' class='text-center text-muted'>")
                    .append(HtmlEscaper.escape(i18n.get("issue.no_items")))
                    .append("</td></tr>\n");
        } else {
            for (SpaceDto s : spaces) {
                sb.append("        <tr>\n");
                sb.append("          <td><span class='badge badge-prefix'>").append(HtmlEscaper.escape(s.getPrefixCode())).append("</span></td>\n");
                sb.append("          <td><strong>").append(HtmlEscaper.escape(s.getName())).append("</strong></td>\n");
                sb.append("          <td>").append(HtmlEscaper.escape(s.getDescription() != null ? s.getDescription() : "-")).append("</td>\n");
                sb.append("          <td class='text-center font-bold'>").append(s.getItems().size()).append("</td>\n");
                sb.append("          <td class='text-center'><a href='").append(s.getPrefixCode()).append(".html' class='btn btn-primary'>")
                        .append(HtmlEscaper.escape(i18n.get("action.view_threads"))).append("</a></td>\n");
                sb.append("        </tr>\n");
            }
        }

        sb.append("      </tbody>\n");
        sb.append("    </table>\n");
        sb.append("  </div>\n");

        appendHtmlFooter(sb);
        sb.append("</div>\n");
        sb.append("</body></html>\n");

        return sb.toString();
    }

    private void generateIndexHtml(List<SpaceDto> spaces, File outDir) throws IOException {
        File file = new File(outDir, "index.html");
        String content = generateIndexHtmlString(spaces);
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    public String generateSpaceHtmlString(SpaceDto space, File attachmentsOutDir) {
        StringBuilder sb = new StringBuilder();

        appendHtmlHeader(sb, space.getName() + " [" + space.getPrefixCode() + "] - " + i18n.get("app.title"));

        sb.append("<div class='container'>\n");
        sb.append("  <header class='header-bar'>\n");
        sb.append("    <div class='header-top'>\n");
        sb.append("      <a href='index.html' class='back-link'>").append(HtmlEscaper.escape(i18n.get("nav.back_to_index"))).append("</a>\n");
        sb.append("      <button type='button' class='btn-theme' onclick='toggleTheme()' title='")
                .append(HtmlEscaper.escape(i18n.get("action.toggle_theme"))).append("'>🌓 ")
                .append(HtmlEscaper.escape(i18n.get("action.toggle_theme"))).append("</button>\n");
        sb.append("    </div>\n");
        sb.append("    <h1>").append(HtmlEscaper.escape(space.getName()))
                .append(" <span class='badge badge-prefix'>").append(HtmlEscaper.escape(space.getPrefixCode())).append("</span></h1>\n");
        if (space.getDescription() != null && !space.getDescription().trim().isEmpty()) {
            sb.append("    <p class='subtitle'>").append(HtmlEscaper.escape(space.getDescription())).append("</p>\n");
        }
        sb.append("    <div class='meta-count'>").append(HtmlEscaper.escape(i18n.get("table.total_items")))
                .append(": <strong>").append(space.getItems().size()).append("</strong></div>\n");
        sb.append("  </header>\n");

        if (space.getItems().isEmpty()) {
            sb.append("  <div class='card text-center text-muted'>")
                    .append(HtmlEscaper.escape(i18n.get("issue.no_items")))
                    .append("</div>\n");
        } else {
            // 依序產出每個議題討論串 Section
            for (ItemDto item : space.getItems()) {
                appendIssueThreadSection(sb, item, attachmentsOutDir);
            }
        }

        appendHtmlFooter(sb);
        sb.append("</div>\n");
        sb.append("</body></html>\n");

        return sb.toString();
    }

    private void generateSpaceHtml(SpaceDto space, File outDir, File attachmentsOutDir) throws IOException {
        String fileName = space.getPrefixCode() + ".html";
        File file = new File(outDir, fileName);
        String content = generateSpaceHtmlString(space, attachmentsOutDir);
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    private void appendIssueThreadSection(StringBuilder sb, ItemDto item, File attachmentsOutDir) {
        String refId = item.getRefId();
        sb.append("  <section class='issue-thread-section' id='").append(HtmlEscaper.escape(refId)).append("'>\n");

        // 票證頂部主卡片
        sb.append("    <div class='issue-main-card'>\n");
        sb.append("      <div class='issue-header'>\n");
        sb.append("        <div class='issue-title-row'>\n");
        sb.append("          <a href='#").append(HtmlEscaper.escape(refId)).append("' class='issue-badge-link'>")
                .append(HtmlEscaper.escape(refId)).append("</a>\n");
        sb.append("          <h2 class='issue-title'>").append(HtmlEscaper.escape(item.getSummary())).append("</h2>\n");
        sb.append("        </div>\n");

        sb.append("        <div class='badge-group'>\n");
        sb.append("          <span class='badge ").append(getStatusBadgeClass(item.getStatus())).append("'>")
                .append(HtmlEscaper.escape(i18n.getStatusLabel(item.getStatus()))).append("</span>\n");
        if (item.getSeverity() != null) {
            sb.append("          <span class='badge badge-meta'>").append(HtmlEscaper.escape(i18n.get("issue.severity")))
                    .append(": ").append(item.getSeverity()).append("</span>\n");
        }
        if (item.getPriority() != null) {
            sb.append("          <span class='badge badge-meta'>").append(HtmlEscaper.escape(i18n.get("issue.priority")))
                    .append(": ").append(item.getPriority()).append("</span>\n");
        }
        sb.append("        </div>\n");
        sb.append("      </div>\n");
        sb.append("      <div class='issue-content-body'>\n");

        // 元數據網格
        sb.append("        <div class='issue-meta-grid'>\n");
        sb.append("          <div><span class='meta-title'>").append(HtmlEscaper.escape(i18n.get("issue.logged_by"))).append(":</span> ")
                .append(HtmlEscaper.escape(item.getLoggedBy() != null ? item.getLoggedBy().getName() : "-")).append("</div>\n");
        sb.append("          <div><span class='meta-title'>").append(HtmlEscaper.escape(i18n.get("issue.assigned_to"))).append(":</span> ")
                .append(HtmlEscaper.escape(item.getAssignedTo() != null ? item.getAssignedTo().getName() : "-")).append("</div>\n");
        sb.append("          <div><span class='meta-title'>").append(HtmlEscaper.escape(i18n.get("issue.timestamp"))).append(":</span> ")
                .append(item.getTimeStamp() != null ? dateFormat.format(item.getTimeStamp()) : "-").append("</div>\n");
        if (item.getPlannedEffort() != null) {
            sb.append("          <div><span class='meta-title'>").append(HtmlEscaper.escape(i18n.get("issue.planned_effort"))).append(":</span> ")
                    .append(item.getPlannedEffort()).append("</div>\n");
        }
        sb.append("        </div>\n");

        // 詳細說明
        if (item.getDetail() != null && !item.getDetail().trim().isEmpty()) {
            sb.append("        <div class='issue-detail-box'>\n");
            sb.append("          <div class='box-title'>").append(HtmlEscaper.escape(i18n.get("issue.detail"))).append("</div>\n");
            sb.append("          <div class='box-content'>").append(HtmlEscaper.escapeWithBreaks(item.getDetail())).append("</div>\n");
            sb.append("        </div>\n");
        }

        // 議題本身直接關聯之附件
        if (!item.getAttachmentList().isEmpty()) {
            sb.append("        <div class='attachments-container'>\n");
            sb.append("          <div class='box-title'>📎 ").append(HtmlEscaper.escape(i18n.get("attachment.title"))).append("</div>\n");
            sb.append("          <div class='attachments-list'>\n");
            for (AttachmentDto att : item.getAttachmentList()) {
                appendAttachmentHtml(sb, att, attachmentsOutDir);
            }
            sb.append("          </div>\n");
            sb.append("        </div>\n");
        }

        sb.append("      </div>\n"); // 結束 issue-content-body
        sb.append("    </div>\n"); // 結束 issue-main-card

        // ===================== 後續留言討論串 (Follow-up History) =====================
        if (item.getHistoryList() != null && !item.getHistoryList().isEmpty()) {
            sb.append("    <div class='discussion-thread-container'>\n");
            sb.append("      <div class='thread-header'>\n");
            sb.append("        <span class='thread-title'>💬 ").append(HtmlEscaper.escape(i18n.get("thread.title"))).append("</span>\n");
            sb.append("        <span class='thread-count-badge'>").append(item.getHistoryList().size())
                    .append(" ").append(HtmlEscaper.escape(i18n.get("thread.updates"))).append("</span>\n");
            sb.append("      </div>\n");

            sb.append("      <div class='timeline'>\n");
            int replyIndex = 1;
            for (HistoryDto h : item.getHistoryList()) {
                sb.append("        <div class='comment-card'>\n");
                sb.append("          <div class='comment-header'>\n");
                sb.append("            <div class='commenter-info'>\n");
                sb.append("              <span class='reply-idx'>#").append(replyIndex++).append("</span>\n");
                sb.append("              <strong class='commenter-name'>")
                        .append(HtmlEscaper.escape(h.getLoggedBy() != null ? h.getLoggedBy().getName() : "-"))
                        .append("</strong>\n");
                if (h.getTimeStamp() != null) {
                    sb.append("              <span class='comment-time'>").append(dateFormat.format(h.getTimeStamp())).append("</span>\n");
                }
                sb.append("            </div>\n");

                if (h.getStatus() != null) {
                    sb.append("            <div class='comment-status'>\n");
                    sb.append("              <span class='text-muted small'>").append(HtmlEscaper.escape(i18n.get("thread.status_changed_to")))
                            .append("</span> <span class='badge ").append(getStatusBadgeClass(h.getStatus())).append("'>")
                            .append(HtmlEscaper.escape(i18n.getStatusLabel(h.getStatus()))).append("</span>\n");
                    sb.append("            </div>\n");
                }
                sb.append("          </div>\n"); // 結束 comment-header

                // 留言內文
                if (h.getComment() != null && !h.getComment().trim().isEmpty()) {
                    sb.append("          <div class='comment-body'>")
                            .append(HtmlEscaper.escapeWithBreaks(h.getComment()))
                            .append("</div>\n");
                }

                // 歷史記錄關聯之附件
                if (h.getAttachment() != null) {
                    sb.append("          <div class='comment-attachment'>\n");
                    appendAttachmentHtml(sb, h.getAttachment(), attachmentsOutDir);
                    sb.append("          </div>\n");
                }

                sb.append("        </div>\n"); // 結束 comment-card
            }
            sb.append("      </div>\n"); // 結束 timeline
            sb.append("    </div>\n"); // 結束 discussion-thread-container
        }
        sb.append("  </section>\n"); // 結束 issue-thread-section
    }

    private void appendAttachmentHtml(StringBuilder sb, AttachmentDto att, File attachmentsOutDir) {
        String physicalName = att.getPhysicalFileName();
        File srcFile = null;
        boolean exists = false;

        if (config.getAttachmentsDir() != null) {
            srcFile = new File(config.getAttachmentsDir(), physicalName);
            if (srcFile.exists() && srcFile.isFile()) {
                exists = true;
            }
        }

        if (exists) {
            // 複製檔案至 output attachments/
            File destFile = new File(attachmentsOutDir, physicalName);
            if (!copiedAttachments.contains(physicalName)) {
                try {
                    Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    copiedAttachments.add(physicalName);
                } catch (IOException e) {
                    System.err.println("[警告] 複製附件檔案失敗: " + physicalName + " (" + e.getMessage() + ")");
                }
            }

            String relPath = "attachments/" + physicalName;
            if (att.isImage()) {
                sb.append("  <div class='attachment-item image-preview'>\n");
                sb.append("    <a href='").append(HtmlEscaper.escape(relPath)).append("' target='_blank'>\n");
                sb.append("      <img src='").append(HtmlEscaper.escape(relPath)).append("' alt='")
                        .append(HtmlEscaper.escape(att.getFileName())).append("' class='thumb-img'/>\n");
                sb.append("    </a>\n");
                sb.append("    <div class='att-caption'><a href='").append(HtmlEscaper.escape(relPath))
                        .append("' target='_blank'>📎 ").append(HtmlEscaper.escape(att.getFileName())).append("</a></div>\n");
                sb.append("  </div>\n");
            } else {
                sb.append("  <div class='attachment-item file-download'>\n");
                sb.append("    <a href='").append(HtmlEscaper.escape(relPath)).append("' download class='file-download-link'>\n");
                sb.append("      📎 <strong>").append(HtmlEscaper.escape(att.getFileName())).append("</strong>\n");
                sb.append("      <span class='btn-download-tag'>").append(HtmlEscaper.escape(i18n.get("attachment.download"))).append("</span>\n");
                sb.append("    </a>\n");
                sb.append("  </div>\n");
            }
        } else {
            // 檔案缺失或未指定目錄時的優雅容錯
            sb.append("  <div class='attachment-item file-missing'>\n");
            sb.append("    📎 <span class='att-missing-name'>").append(HtmlEscaper.escape(att.getFileName())).append("</span>\n");
            sb.append("    <span class='badge-missing'>(").append(HtmlEscaper.escape(i18n.get("attachment.missing"))).append(")</span>\n");
            sb.append("  </div>\n");
        }
    }

    private String getStatusBadgeClass(Integer status) {
        if (status == null) return "badge-open";
        if (status == 99) return "badge-closed";
        if (status == 0) return "badge-new";
        return "badge-open";
    }

    private void appendHtmlHeader(StringBuilder sb, String title) {
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang='").append(config.getLang()).append("'>\n<head>\n");
        sb.append("  <meta charset='UTF-8'>\n");
        sb.append("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        sb.append("  <title>").append(HtmlEscaper.escape(title)).append("</title>\n");
        sb.append("  <style>\n");
        sb.append(getEmbeddedCss());
        sb.append("  </style>\n");
        sb.append("  <script>\n");
        sb.append("    (function() {\n");
        sb.append("      var t = localStorage.getItem('jtrac-theme');\n");
        sb.append("      if (t) { document.documentElement.setAttribute('data-theme', t);\n");
        sb.append("      } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {\n");
        sb.append("        document.documentElement.setAttribute('data-theme', 'dark');\n");
        sb.append("      }\n");
        sb.append("    })();\n");
        sb.append("    function toggleTheme() {\n");
        sb.append("      var html = document.documentElement;\n");
        sb.append("      var current = html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';\n");
        sb.append("      html.setAttribute('data-theme', current);\n");
        sb.append("      try { localStorage.setItem('jtrac-theme', current); } catch(e){}\n");
        sb.append("    }\n");
        sb.append("  </script>\n");
        sb.append("</head>\n<body>\n");
    }

    private void appendHtmlFooter(StringBuilder sb) {
        sb.append("  <footer class='page-footer'>\n");
        sb.append("    <p>").append(HtmlEscaper.escape(i18n.get("footer.powered_by"))).append("</p>\n");
        sb.append("    <p class='text-muted small'>").append(HtmlEscaper.escape(i18n.get("footer.exported_at")))
                .append(": ").append(dateFormat.format(new Date())).append("</p>\n");
        sb.append("  </footer>\n");
    }

    private String getEmbeddedCss() {
        return "  :root {\n" +
                "    --primary: #2563eb;\n" +
                "    --primary-hover: #1d4ed8;\n" +
                "    --bg-main: #f1f5f9;\n" +
                "    --card-bg: #ffffff;\n" +
                "    --card-header-bg: #f8fafc;\n" +
                "    --border: #cbd5e1;\n" +
                "    --border-strong: #94a3b8;\n" +
                "    --border-accent: #2563eb;\n" +
                "    --text: #0f172a;\n" +
                "    --text-muted: #64748b;\n" +
                "    --meta-bg: #f8fafc;\n" +
                "    --box-bg: #ffffff;\n" +
                "    --thread-bg: #f8fafc;\n" +
                "    --comment-bg: #ffffff;\n" +
                "    --table-header: #f8fafc;\n" +
                "    --table-hover: #f1f5f9;\n" +
                "    --green-bg: #dcfce7; --green-text: #166534;\n" +
                "    --gray-bg: #e2e8f0; --gray-text: #334155;\n" +
                "    --blue-bg: #dbeafe; --blue-text: #1e40af;\n" +
                "    --shadow: 0 4px 16px rgba(0,0,0,0.08);\n" +
                "    --shadow-hover: 0 8px 24px rgba(0,0,0,0.12);\n" +
                "  }\n" +
                "  [data-theme='dark'] {\n" +
                "    --primary: #3b82f6;\n" +
                "    --primary-hover: #60a5fa;\n" +
                "    --bg-main: #0b0f19;\n" +
                "    --card-bg: #1e293b;\n" +
                "    --card-header-bg: #172033;\n" +
                "    --border: #334155;\n" +
                "    --border-strong: #64748b;\n" +
                "    --border-accent: #3b82f6;\n" +
                "    --text: #f8fafc;\n" +
                "    --text-muted: #94a3b8;\n" +
                "    --meta-bg: #131d2e;\n" +
                "    --box-bg: #0f172a;\n" +
                "    --thread-bg: #141d2c;\n" +
                "    --comment-bg: #1e293b;\n" +
                "    --table-header: #172033;\n" +
                "    --table-hover: #26354a;\n" +
                "    --green-bg: #064e3b; --green-text: #6ee7b7;\n" +
                "    --gray-bg: #334155; --gray-text: #cbd5e1;\n" +
                "    --blue-bg: #1e3a8a; --blue-text: #bfdbfe;\n" +
                "    --shadow: 0 4px 16px rgba(0,0,0,0.5);\n" +
                "    --shadow-hover: 0 8px 24px rgba(0,0,0,0.7);\n" +
                "  }\n" +
                "  * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
                "  body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;\n" +
                "         background-color: var(--bg-main); color: var(--text); line-height: 1.6; padding: 24px 16px;\n" +
                "         transition: background-color 0.2s, color 0.2s; }\n" +
                "  .container { max-width: 1080px; margin: 0 auto; }\n" +
                "  .header-bar { background: var(--card-bg); padding: 24px 32px; border-radius: 12px; border: 2px solid var(--border);\n" +
                "                margin-bottom: 24px; box-shadow: var(--shadow); }\n" +
                "  .header-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }\n" +
                "  .header-bar h1 { font-size: 1.75rem; color: var(--text); display: flex; align-items: center; gap: 12px; }\n" +
                "  .btn-theme { display: inline-flex; align-items: center; gap: 8px; background: var(--card-bg); color: var(--text);\n" +
                "               border: 1.5px solid var(--border-strong); padding: 7px 15px; border-radius: 8px; font-size: 0.9rem;\n" +
                "               font-weight: 600; cursor: pointer; transition: all 0.2s ease; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }\n" +
                "  .btn-theme:hover { background: var(--card-header-bg); border-color: var(--primary); color: var(--primary); }\n" +
                "  .subtitle { color: var(--text-muted); margin-top: 6px; font-size: 1rem; }\n" +
                "  .meta-count { margin-top: 12px; font-size: 0.95rem; color: var(--text-muted); }\n" +
                "  .back-link { display: inline-block; color: var(--primary); text-decoration: none; font-size: 0.95rem; font-weight: 600; }\n" +
                "  .back-link:hover { text-decoration: underline; }\n" +
                "  .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }\n" +
                "  .stat-card { background: var(--card-bg); padding: 20px; border-radius: 12px; border: 2px solid var(--border);\n" +
                "               text-align: center; box-shadow: var(--shadow); }\n" +
                "  .stat-val { font-size: 2.2rem; font-weight: 700; color: var(--primary); }\n" +
                "  .stat-label { color: var(--text-muted); font-size: 0.9rem; margin-top: 4px; }\n" +
                "  .card { background: var(--card-bg); border-radius: 12px; border: 2px solid var(--border);\n" +
                "          padding: 24px; box-shadow: var(--shadow); margin-bottom: 24px; }\n" +
                "  .data-table { width: 100%; border-collapse: collapse; }\n" +
                "  .data-table th, .data-table td { padding: 14px 16px; border-bottom: 1px solid var(--border); text-align: left; }\n" +
                "  .data-table th { background: var(--table-header); font-weight: 600; color: var(--text-muted); font-size: 0.9rem; }\n" +
                "  .data-table tr:hover { background: var(--table-hover); }\n" +
                "  .btn { display: inline-block; padding: 6px 14px; border-radius: 6px; font-size: 0.88rem; text-decoration: none;\n" +
                "         font-weight: 500; transition: all 0.15s ease; }\n" +
                "  .btn-primary { background: var(--primary); color: white; border: none; }\n" +
                "  .btn-primary:hover { background: var(--primary-hover); }\n" +
                "  .badge { display: inline-block; padding: 4px 10px; border-radius: 9999px; font-size: 0.8rem; font-weight: 600; }\n" +
                "  .badge-prefix { background: #ede9fe; color: #6d28d9; }\n" +
                "  .badge-open { background: var(--green-bg); color: var(--green-text); }\n" +
                "  .badge-closed { background: var(--gray-bg); color: var(--gray-text); }\n" +
                "  .badge-new { background: var(--blue-bg); color: var(--blue-text); }\n" +
                "  .badge-meta { background: var(--meta-bg); border: 1px solid var(--border); color: var(--text-muted); font-weight: 500; }\n" +
                "  .badge-missing { color: #ef4444; font-size: 0.82rem; font-style: italic; }\n" +
                "  /* Issue Thread Section (強化大外框與層次) */\n" +
                "  .issue-thread-section { background: var(--card-bg); border-radius: 14px; border: 2px solid var(--border-strong);\n" +
                "                         border-left: 10px solid var(--border-accent); margin-bottom: 48px; box-shadow: var(--shadow);\n" +
                "                         overflow: hidden; transition: box-shadow 0.2s, border-color 0.2s; }\n" +
                "  .issue-thread-section:hover { box-shadow: var(--shadow-hover); }\n" +
                "  .issue-main-card { padding: 0; border-bottom: 2px solid var(--border); }\n" +
                "  .issue-header { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 14px;\n" +
                "                  padding: 18px 24px; background: var(--card-header-bg); border-bottom: 2px solid var(--border); }\n" +
                "  .issue-title-row { display: flex; align-items: center; gap: 14px; flex: 1; min-width: 280px; }\n" +
                "  .issue-badge-link { font-size: 1.25rem; font-weight: 800; color: #ffffff !important; background: var(--primary);\n" +
                "                      padding: 6px 14px; border-radius: 8px; text-decoration: none; letter-spacing: 0.5px;\n" +
                "                      box-shadow: 0 2px 4px rgba(0,0,0,0.15); display: inline-block; }\n" +
                "  .issue-badge-link:hover { background: var(--primary-hover); text-decoration: none; }\n" +
                "  .issue-title { font-size: 1.4rem; font-weight: 700; color: var(--text); word-break: break-word; margin: 0; }\n" +
                "  .badge-group { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }\n" +
                "  .issue-content-body { padding: 24px; }\n" +
                "  .issue-meta-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px;\n" +
                "                     background: var(--meta-bg); border: 1px solid var(--border); padding: 14px 18px; border-radius: 8px;\n" +
                "                     font-size: 0.92rem; margin-bottom: 18px; color: var(--text); }\n" +
                "  .meta-title { color: var(--text-muted); font-weight: 600; }\n" +
                "  .box-title { font-weight: 700; font-size: 0.95rem; color: var(--text); margin-bottom: 8px; }\n" +
                "  .issue-detail-box { margin-bottom: 18px; }\n" +
                "  .box-content { background: var(--box-bg); padding: 14px 18px; border: 1.5px solid var(--border); border-radius: 8px;\n" +
                "                white-space: pre-wrap; font-size: 0.95rem; color: var(--text); line-height: 1.6; }\n" +
                "  .attachments-container { margin-top: 14px; padding-top: 12px; border-top: 1px dashed var(--border); }\n" +
                "  .attachments-list { display: flex; flex-wrap: wrap; gap: 14px; align-items: center; }\n" +
                "  .attachment-item { font-size: 0.9rem; }\n" +
                "  .thumb-img { max-width: 180px; max-height: 120px; border-radius: 6px; border: 1px solid var(--border);\n" +
                "               object-fit: cover; transition: transform 0.2s; background: var(--card-bg); }\n" +
                "  .thumb-img:hover { transform: scale(1.03); }\n" +
                "  .file-download-link { display: inline-flex; align-items: center; gap: 8px; background: var(--meta-bg); padding: 7px 14px;\n" +
                "                       border-radius: 6px; text-decoration: none; color: var(--text); border: 1px solid var(--border); }\n" +
                "  .file-download-link:hover { border-color: var(--primary); color: var(--primary); }\n" +
                "  .btn-download-tag { font-size: 0.75rem; background: var(--primary); color: white; padding: 2px 6px; border-radius: 4px; }\n" +
                "  /* Discussion Thread */\n" +
                "  .discussion-thread-container { padding: 24px; background: var(--thread-bg); border-top: 2px dashed var(--border); }\n" +
                "  .thread-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }\n" +
                "  .thread-title { font-size: 1.1rem; font-weight: 700; color: var(--text); }\n" +
                "  .thread-count-badge { background: var(--meta-bg); border: 1px solid var(--border); padding: 4px 10px; border-radius: 9999px; font-size: 0.85rem; color: var(--text-muted); }\n" +
                "  .timeline { display: flex; flex-direction: column; gap: 14px; }\n" +
                "  .comment-card { background: var(--comment-bg); border: 1.5px solid var(--border); border-radius: 10px; padding: 16px 20px;\n" +
                "                  box-shadow: 0 1px 3px rgba(0,0,0,0.04); color: var(--text); }\n" +
                "  .comment-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;\n" +
                "                    border-bottom: 1px solid var(--border); padding-bottom: 8px; }\n" +
                "  .commenter-info { display: flex; align-items: center; gap: 8px; font-size: 0.95rem; }\n" +
                "  .reply-idx { font-weight: 700; color: var(--primary); font-size: 0.9rem; }\n" +
                "  .comment-time { color: var(--text-muted); font-size: 0.85rem; }\n" +
                "  .comment-body { font-size: 0.95rem; color: var(--text); line-height: 1.6; }\n" +
                "  .comment-attachment { margin-top: 10px; padding-top: 8px; border-top: 1px dotted var(--border); }\n" +
                "  .thread-empty { padding: 12px 0; font-size: 0.9rem; }\n" +
                "  .page-footer { text-align: center; margin-top: 36px; padding: 18px 0; color: var(--text-muted); font-size: 0.88rem; }\n" +
                "  .text-center { text-align: center; }\n" +
                "  .text-muted { color: var(--text-muted); }\n" +
                "  .small { font-size: 0.85rem; }\n" +
                "  .font-bold { font-weight: 700; }\n";
    }
}
