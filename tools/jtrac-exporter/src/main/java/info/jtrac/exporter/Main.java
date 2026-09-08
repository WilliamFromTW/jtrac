package info.jtrac.exporter;

import info.jtrac.exporter.config.ExportConfig;
import info.jtrac.exporter.db.DatabaseReader;
import info.jtrac.exporter.html.HtmlGenerator;
import info.jtrac.exporter.model.SpaceDto;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        ExportConfig config = ExportConfig.parse(args);

        if (config.isHelp() || config.getDbUrl() == null || config.getDbUrl().trim().isEmpty()) {
            printHelp();
            if (config.getDbUrl() == null && !config.isHelp()) {
                System.err.println("\n[錯誤] 缺少必要參數: --db-url=<JDBC_URL>");
                System.exit(1);
            }
            return;
        }

        long start = System.currentTimeMillis();
        System.out.println("=================================================");
        System.out.println("  JTrac Standalone HTML Exporter (CLI)");
        System.out.println("  語言模式: " + config.getLang());
        System.out.println("  匯出目標: " + config.getOutputDir().getAbsolutePath());
        if (config.getAttachmentsDir() != null) {
            System.out.println("  附件目錄: " + config.getAttachmentsDir().getAbsolutePath());
        } else {
            System.out.println("  附件目錄: 未指定 (若有附件將標記提示，不中斷匯出)");
        }
        System.out.println("=================================================");

        try (DatabaseReader reader = new DatabaseReader(config)) {
            reader.connect();
            List<SpaceDto> spaces = reader.readAllData();

            HtmlGenerator generator = new HtmlGenerator(config);
            generator.generate(spaces);

            long elapsed = System.currentTimeMillis() - start;
            System.out.println("=================================================");
            System.out.println("✅ 匯出作業順利完成！耗時: " + elapsed + " 毫秒");
            System.out.println("👉 請使用瀏覽器開啟: " + config.getOutputDir().toPath().resolve("index.html").toUri());
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("\n❌ 匯出過程發生錯誤: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void printHelp() {
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
        System.out.println("  --lang=<語系>            介面語言: zh-TW (預設) | en | zh-CN | ja | vi");
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
    }
}
