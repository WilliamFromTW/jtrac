package info.jtrac.wicket;

import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.exporter.ZipStreamExporter;
import info.jtrac.exporter.config.ExportConfig;
import info.jtrac.exporter.db.DatabaseReader;
import info.jtrac.exporter.model.SpaceDto;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Confirmation and configuration page (Mode 2) for streaming HTML static export as a downloadable ZIP.
 */
public class HtmlExportPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(HtmlExportPage.class);

    public static class ExportOptionModel implements Serializable {
        private String scope = "ALL";
        private String lang = "zh-TW";
        private boolean includeAttachments = true;

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public boolean isIncludeAttachments() {
            return includeAttachments;
        }

        public void setIncludeAttachments(boolean includeAttachments) {
            this.includeAttachments = includeAttachments;
        }
    }

    public HtmlExportPage() {
        setVersioned(false);

        // Guardrail: 未登入訪客強制導向登入頁
        final User user = getPrincipal();
        if (user == null || user.getId() == 0) {
            throw new RestartResponseAtInterceptPageException(LoginPage.class);
        }

        final Space currentSpace = getCurrentSpace();
        final ExportOptionModel optionModel = new ExportOptionModel();

        // 預設語系依使用者偏好或系統繁中
        if (user.getLocale() != null) {
            String uLoc = user.getLocale().toLowerCase();
            if (uLoc.startsWith("zh_cn") || uLoc.startsWith("zh-cn")) {
                optionModel.setLang("zh-CN");
            } else if (uLoc.startsWith("ja")) {
                optionModel.setLang("ja");
            } else if (uLoc.startsWith("en")) {
                optionModel.setLang("en");
            } else if (uLoc.startsWith("vi")) {
                optionModel.setLang("vi");
            } else {
                optionModel.setLang("zh-TW");
            }
        }

        if (currentSpace != null) {
            optionModel.setScope(currentSpace.getPrefixCode());
        } else {
            optionModel.setScope("ALL");
        }

        Form form = new Form("form", new CompoundPropertyModel(optionModel));
        add(form);

        // 1. 範圍單選 (RadioChoice)
        List<String> scopeList = new ArrayList<String>();
        scopeList.add("ALL");
        if (currentSpace != null) {
            scopeList.add(currentSpace.getPrefixCode());
        }

        RadioChoice scopeChoice = new RadioChoice("scope", scopeList, new IChoiceRenderer() {
            public Object getDisplayValue(Object o) {
                String key = (String) o;
                if ("ALL".equals(key)) {
                    return localize("html_export.scope.all");
                } else if (currentSpace != null) {
                    return localize("html_export.scope.current", currentSpace.getName() + " [" + currentSpace.getPrefixCode() + "]");
                }
                return key;
            }

            public String getIdValue(Object o, int i) {
                return o.toString();
            }
        });
        scopeChoice.setRequired(true);
        form.add(scopeChoice);

        // 2. 語系下拉選單 (DropDownChoice)
        List<String> langList = Arrays.asList("zh-TW", "en", "zh-CN", "ja", "vi");
        DropDownChoice langChoice = new DropDownChoice("lang", langList, new IChoiceRenderer() {
            public Object getDisplayValue(Object o) {
                String code = (String) o;
                if ("zh-TW".equals(code)) return "繁體中文 (Traditional Chinese)";
                if ("en".equals(code)) return "English (English)";
                if ("zh-CN".equals(code)) return "简体中文 (Simplified Chinese)";
                if ("ja".equals(code)) return "日本語 (Japanese)";
                if ("vi".equals(code)) return "Tiếng Việt (Vietnamese)";
                return code;
            }

            public String getIdValue(Object o, int i) {
                return o.toString();
            }
        });
        langChoice.setRequired(true);
        form.add(langChoice);

        // 3. 包含實體附件核取方塊 (CheckBox)
        form.add(new CheckBox("includeAttachments"));

        // 4. 開始匯出按鈕 (Button)
        form.add(new Button("download") {
            @Override
            public void onSubmit() {
                final String selectedLang = optionModel.getLang();
                final String selectedScope = optionModel.getScope();
                final boolean includeAtt = optionModel.isIncludeAttachments();

                final String spaceFilter = "ALL".equals(selectedScope) ? null : selectedScope;
                final String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
                final String zipFileName = spaceFilter != null
                        ? "jtrac-export-" + spaceFilter + "-" + dateStr + ".zip"
                        : "jtrac-export-" + dateStr + ".zip";

                getRequestCycle().setRequestTarget(new IRequestTarget() {
                    public void detach(RequestCycle requestCycle) {}

                    public void respond(RequestCycle requestCycle) {
                        WebResponse r = (WebResponse) requestCycle.getResponse();
                        r.setContentType("application/zip");
                        r.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");

                        ApplicationContext ctx = JtracApplication.get().getApplicationContext();
                        DataSource dataSource = (DataSource) ctx.getBean("dataSource");

                        try (Connection conn = dataSource.getConnection()) {
                            ExportConfig exportConfig = new ExportConfig();
                            exportConfig.setLang(selectedLang);
                            exportConfig.setSpaceFilter(spaceFilter);

                            File attachmentsDir = new File(getJtrac().getJtracHome(), "attachments");
                            if (includeAtt && attachmentsDir.exists()) {
                                exportConfig.setAttachmentsDir(attachmentsDir);
                            }

                            DatabaseReader reader = new DatabaseReader(conn, exportConfig);
                            List<SpaceDto> spaces = reader.readAllData();

                            ZipStreamExporter exporter = new ZipStreamExporter(
                                    spaces, exportConfig, includeAtt, attachmentsDir, r.getOutputStream()
                            );
                            exporter.export();
                        } catch (Exception e) {
                            logger.error("串流打包 HTML 匯出 ZIP 檔失敗", e);
                            throw new RuntimeException("匯出失敗: " + e.getMessage(), e);
                        }
                    }
                });
            }
        });

        // 5. 取消返回連結 (Link)
        form.add(new Link("cancel") {
            public void onClick() {
                setResponsePage(DashboardPage.class);
            }
        });
    }
}
