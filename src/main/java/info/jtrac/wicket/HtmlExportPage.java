package info.jtrac.wicket;

import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.exporter.ZipStreamExporter;
import info.jtrac.exporter.config.ExportConfig;
import info.jtrac.exporter.db.DatabaseReader;
import info.jtrac.exporter.model.SpaceDto;
import org.acegisecurity.AccessDeniedException;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
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
        private List<String> spaces = new ArrayList<String>();
        private String lang = "zh-TW";
        private boolean includeAttachments = true;

        public List<String> getSpaces() {
            return spaces;
        }

        public void setSpaces(List<String> spaces) {
            this.spaces = spaces;
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

        final List<Space> permittedSpaces;
        if (user.isSuperUser()) {
            permittedSpaces = getJtrac().findAllSpaces();
        } else {
            permittedSpaces = new ArrayList<Space>(user.getSpaces());
        }

        // 依照名稱排序
        Collections.sort(permittedSpaces, new Comparator<Space>() {
            public int compare(Space o1, Space o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        // Guardrail: 若使用者沒有任何專案空間權限且非 SuperUser，阻擋並重定向至首頁
        if (permittedSpaces.isEmpty()) {
            logger.warn("User {} has no permitted spaces, redirecting to Dashboard", user.getLoginName());
            setResponsePage(DashboardPage.class);
            return;
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

        List<String> prefixCodes = new ArrayList<String>();
        final Map<String, Space> spaceMap = new HashMap<String, Space>();
        final Set<String> permittedPrefixCodesUpper = new HashSet<String>();
        for (Space s : permittedSpaces) {
            prefixCodes.add(s.getPrefixCode());
            spaceMap.put(s.getPrefixCode(), s);
            permittedPrefixCodesUpper.add(s.getPrefixCode().toUpperCase());
        }

        // 預設選取：若當前已位於特定空間且在授權清單內，預設選該空間；否則預設全選
        if (currentSpace != null && spaceMap.containsKey(currentSpace.getPrefixCode())) {
            optionModel.getSpaces().add(currentSpace.getPrefixCode());
        } else {
            optionModel.getSpaces().addAll(prefixCodes);
        }

        Form form = new Form("form", new CompoundPropertyModel(optionModel));
        add(form);

        form.add(new FeedbackPanel("feedback"));

        // 1. 專案空間複選核取方塊 (CheckBoxMultipleChoice)
        CheckBoxMultipleChoice spacesChoice = new CheckBoxMultipleChoice("spaces", prefixCodes, new IChoiceRenderer() {
            public Object getDisplayValue(Object o) {
                String code = (String) o;
                Space s = spaceMap.get(code);
                return s != null ? s.getName() + " [" + s.getPrefixCode() + "]" : code;
            }

            public String getIdValue(Object o, int i) {
                return (String) o;
            }
        });
        spacesChoice.setPrefix("<div style='margin: 3px 0;'>");
        spacesChoice.setSuffix("</div>");
        form.add(spacesChoice);

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
                final List<String> selectedSpaces = optionModel.getSpaces();
                if (selectedSpaces == null || selectedSpaces.isEmpty()) {
                    error(localize("html_export.error.no_space_selected"));
                    return;
                }

                // 後端防禦深度安全檢核 (Defense-in-Depth Guardrail)
                if (!user.isSuperUser()) {
                    for (String code : selectedSpaces) {
                        if (!permittedPrefixCodesUpper.contains(code.toUpperCase())) {
                            logger.error("Security violation: User {} attempted unauthorized export of space {}",
                                    user.getLoginName(), code);
                            throw new AccessDeniedException("您未被授權存取專案空間: " + code);
                        }
                    }
                }

                final String selectedLang = optionModel.getLang();
                final boolean includeAtt = optionModel.isIncludeAttachments();
                final String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
                final String zipFileName = selectedSpaces.size() == 1
                        ? "jtrac-export-" + selectedSpaces.get(0) + "-" + dateStr + ".zip"
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
                            exportConfig.setTargetSpacePrefixCodes(selectedSpaces);

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
