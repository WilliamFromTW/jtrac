package info.jtrac.exporter;

import info.jtrac.exporter.config.ExportConfig;
import info.jtrac.exporter.db.DatabaseReader;
import info.jtrac.exporter.html.HtmlGenerator;
import info.jtrac.exporter.i18n.I18nMessages;
import info.jtrac.exporter.model.SpaceDto;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        ExportConfig config = ExportConfig.parse(args);
        I18nMessages i18n = new I18nMessages(config.getLang());

        if (config.isHelp() || config.getDbUrl() == null || config.getDbUrl().trim().isEmpty()) {
            printHelp(i18n);
            if (config.getDbUrl() == null && !config.isHelp()) {
                System.err.println("\n" + i18n.get("cli.error_missing_url"));
                System.exit(1);
            }
            return;
        }

        long start = System.currentTimeMillis();
        System.out.println("=================================================");
        System.out.println(i18n.get("cli.banner.title"));
        System.out.println(i18n.get("cli.lang_mode") + config.getLang());
        System.out.println(i18n.get("cli.export_target") + config.getOutputDir().getAbsolutePath());
        if (config.getAttachmentsDir() != null) {
            System.out.println(i18n.get("cli.attachments_dir") + config.getAttachmentsDir().getAbsolutePath());
        } else {
            System.out.println(i18n.get("cli.attachments_dir") + i18n.get("cli.attachments_unspecified"));
        }
        System.out.println("=================================================");

        try (DatabaseReader reader = new DatabaseReader(config)) {
            reader.connect();
            List<SpaceDto> spaces = reader.readAllData();

            HtmlGenerator generator = new HtmlGenerator(config);
            generator.generate(spaces);

            long elapsed = System.currentTimeMillis() - start;
            System.out.println("=================================================");
            System.out.println(String.format(i18n.get("cli.success"), elapsed));
            System.out.println(String.format(i18n.get("cli.open_browser"), config.getOutputDir().toPath().resolve("index.html").toUri()));
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("\n" + String.format(i18n.get("cli.error_prefix"), e.getMessage()));
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void printHelp(I18nMessages i18n) {
        if ("zh-tw".equals(i18n.getLang()) || "zh-cn".equals(i18n.getLang())) {
            System.out.println("================================================================================");
            System.out.println("  JTrac Standalone HTML Exporter (命令列靜態討論串匯出工具)");
            System.out.println("================================================================================");
            System.out.println("使用方式:");
            System.out.println("  java -jar tools/jtrac-exporter.jar --db-url=<JDBC_URL> [選項...]");
            System.out.println();
            System.out.println("必要參數:");
            System.out.println("  --db-url=<URL>           指定 JDBC 連線字串 (支援 MySQL, PostgreSQL, HSQLDB, SQLServer)");
            System.out.println();
            System.out.println("可選參數:");
            System.out.println("  --db-user=<帳號>         資料庫帳號 (預設為 'sa')");
            System.out.println("  --db-password=<密碼>     資料庫密碼 (預設為空字串 '')");
            System.out.println("  --db-driver=<類別名>     自訂 JDBC 驅動類別 (若無則由 URL 自動判定)");
            System.out.println("  --attachments-dir=<路徑> JTrac 實體附件目錄 (若有提供將自動複製並產生預覽與下載連結)");
            System.out.println("  --out=<路徑>             靜態 HTML 產出目錄 (預設為 './jtrac-html-export')");
            System.out.println("  --lang=<語系>            介面語言: zh-TW (預設) | en | zh-CN | ja | vi | de | es | fr");
            System.out.println("  --space=<Prefix>         僅匯出特定 Space 代碼 (如: DEFAULT)");
            System.out.println("  --help, -h               顯示本說明畫面");
            System.out.println();
            System.out.println("執行範例:");
            System.out.println("  # 範例 1：連線遠端 MySQL 資料庫");
            System.out.println("  java -jar tools/jtrac-exporter.jar \\");
            System.out.println("    --db-url=\"jdbc:mysql://192.168.1.100:3306/jtrac?useUnicode=true&characterEncoding=UTF-8\" \\");
            System.out.println("    --db-user=\"jtrac\" --db-password=\"secret\" --out=\"./export-mysql\" --lang=zh-TW");
            System.out.println();
            System.out.println("  # 範例 2：連線遠端 PostgreSQL 資料庫");
            System.out.println("  java -jar tools/jtrac-exporter.jar \\");
            System.out.println("    --db-url=\"jdbc:postgresql://db.company.internal:5432/jtrac\" \\");
            System.out.println("    --db-user=\"postgres\" --db-password=\"secret\" --out=\"./export-pg\" --lang=en");
            System.out.println();
            System.out.println("  # 範例 3：連線本地 HSQLDB (相對路徑，預設帳號 sa、無密碼)");
            System.out.println("  java -jar tools/jtrac-exporter.jar \\");
            System.out.println("    --db-url=\"jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true\" \\");
            System.out.println("    --attachments-dir=\"./data/attachments\" \\");
            System.out.println("    --out=\"./export-hsqldb\" --lang=zh-TW");
            System.out.println("================================================================================");
        } else {
            System.out.println("================================================================================");
            System.out.println("  JTrac Standalone HTML Exporter (CLI Tool)");
            System.out.println("================================================================================");
            System.out.println("Usage:");
            System.out.println("  java -jar tools/jtrac-exporter.jar --db-url=<JDBC_URL> [options...]");
            System.out.println();
            System.out.println("Required Options:");
            System.out.println("  --db-url=<URL>           JDBC connection URL (supports MySQL, PostgreSQL, HSQLDB, SQLServer)");
            System.out.println();
            System.out.println("Optional Parameters:");
            System.out.println("  --db-user=<username>     Database username (default: 'sa')");
            System.out.println("  --db-password=<password> Database password (default: '')");
            System.out.println("  --db-driver=<class>      Custom JDBC driver class (auto-detected if omitted)");
            System.out.println("  --attachments-dir=<dir>  JTrac physical attachments directory");
            System.out.println("  --out=<dir>              Output directory for static HTML (default: './jtrac-html-export')");
            System.out.println("  --lang=<language>        Language: en (default) | zh-TW | zh-CN | ja | vi | de | es | fr");
            System.out.println("  --space=<Prefix>         Export specific space prefix only (e.g. DEFAULT)");
            System.out.println("  --help, -h               Show this help message");
            System.out.println();
            System.out.println("Examples:");
            System.out.println("  # Example 1: Connect to remote MySQL database");
            System.out.println("  java -jar tools/jtrac-exporter.jar \\");
            System.out.println("    --db-url=\"jdbc:mysql://192.168.1.100:3306/jtrac?useUnicode=true&characterEncoding=UTF-8\" \\");
            System.out.println("    --db-user=\"jtrac\" --db-password=\"secret\" --out=\"./export-mysql\" --lang=en");
            System.out.println();
            System.out.println("  # Example 2: Connect to local HSQLDB database");
            System.out.println("  java -jar tools/jtrac-exporter.jar \\");
            System.out.println("    --db-url=\"jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true\" \\");
            System.out.println("    --attachments-dir=\"./data/attachments\" \\");
            System.out.println("    --out=\"./export-hsqldb\" --lang=en");
            System.out.println("================================================================================");
        }
    }
}
