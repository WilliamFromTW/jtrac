package info.jtrac.exporter;

import info.jtrac.exporter.config.ExportConfig;
import info.jtrac.exporter.db.DatabaseReader;
import info.jtrac.exporter.html.HtmlGenerator;
import info.jtrac.exporter.model.SpaceDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExporterIntegrationTest {

    @Test
    public void testFullExportPipeline(@TempDir Path tempDir) throws Exception {
        String dbUrl = "jdbc:hsqldb:mem:testjtrac;DB_CLOSE_DELAY=-1";
        Path attachmentsDir = tempDir.resolve("attachments");
        Files.createDirectories(attachmentsDir);

        // 建立測試實體附件
        Path testFile = attachmentsDir.resolve("101_sample-patch.txt");
        Files.write(testFile, "This is a test patch content.".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // 初始化模擬 JTrac 資料庫結構
        Class.forName("org.hsqldb.jdbcDriver");
        try (Connection conn = DriverManager.getConnection(dbUrl, "sa", "");
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE spaces (id BIGINT PRIMARY KEY, prefix_code VARCHAR(10), name VARCHAR(50), description VARCHAR(255), metadata_id BIGINT)");
            stmt.execute("CREATE TABLE users (id BIGINT PRIMARY KEY, login_name VARCHAR(50), name VARCHAR(50), email VARCHAR(50))");
            stmt.execute("CREATE TABLE items (id BIGINT PRIMARY KEY, space_id BIGINT, sequence_num BIGINT, summary VARCHAR(255), detail VARCHAR(1000), status INT, severity INT, priority INT, logged_by BIGINT, assigned_to BIGINT, time_stamp TIMESTAMP, planned_effort DOUBLE)");
            stmt.execute("CREATE TABLE history (id BIGINT PRIMARY KEY, item_id BIGINT, comment VARCHAR(1000), attachment_id BIGINT, time_stamp TIMESTAMP, logged_by BIGINT, assigned_to BIGINT, status INT, severity INT, priority INT, actual_effort DOUBLE)");
            stmt.execute("CREATE TABLE attachments (id BIGINT PRIMARY KEY, item_id BIGINT, file_name VARCHAR(255), file_prefix BIGINT)");

            // 插入測試資料
            stmt.execute("INSERT INTO users VALUES (1, 'admin', '系統管理者', 'admin@example.com')");
            stmt.execute("INSERT INTO users VALUES (2, 'dev_william', '威廉工程師', 'william@example.com')");

            stmt.execute("INSERT INTO spaces VALUES (1, 'DEV', '核心研發專案', '主要核心模組開發追蹤空間', NULL)");

            stmt.execute("INSERT INTO items VALUES (10, 1, 1, '修正認證模組 NullPointerException', '在未輸入密碼時點擊登入會造成系統拋出異常。', 1, 1, 2, 1, 2, CURRENT_TIMESTAMP, 4.0)");

            stmt.execute("INSERT INTO attachments VALUES (101, 10, 'sample-patch.txt', 101)");

            stmt.execute("INSERT INTO history VALUES (1, 10, '問題已確認重現，建立議題追蹤。', NULL, CURRENT_TIMESTAMP, 1, 2, 1, 1, 2, NULL)");
            stmt.execute("INSERT INTO history VALUES (2, 10, '已撰寫防呆防護並提交修復補丁。', 101, CURRENT_TIMESTAMP, 2, 1, 99, 1, 2, 3.5)");
        }

        // 執行匯出
        Path outDir = tempDir.resolve("html-export");
        String[] args = new String[] {
                "--db-url=" + dbUrl,
                "--db-user=sa",
                "--db-password=",
                "--attachments-dir=" + attachmentsDir.toAbsolutePath(),
                "--out=" + outDir.toAbsolutePath(),
                "--lang=zh-TW"
        };

        ExportConfig config = ExportConfig.parse(args);
        assertEquals("zh-tw", config.getLang().toLowerCase());

        try (DatabaseReader reader = new DatabaseReader(config)) {
            reader.connect();
            List<SpaceDto> spaces = reader.readAllData();

            assertEquals(1, spaces.size());
            SpaceDto space = spaces.get(0);
            assertEquals("DEV", space.getPrefixCode());
            assertEquals(1, space.getItems().size());
            assertEquals(2, space.getItems().get(0).getHistoryList().size());

            HtmlGenerator generator = new HtmlGenerator(config);
            generator.generate(spaces);
        }

        // 驗證輸出產物
        Path indexHtml = outDir.resolve("index.html");
        Path spaceHtml = outDir.resolve("DEV.html");
        Path copiedAttachment = outDir.resolve("attachments").resolve("101_sample-patch.txt");

        assertTrue(Files.exists(indexHtml), "index.html 必須存在");
        assertTrue(Files.exists(spaceHtml), "DEV.html 必須存在");
        assertTrue(Files.exists(copiedAttachment), "附件必須成功複製到 attachments/ 目錄");

        String indexContent = new String(Files.readAllBytes(indexHtml), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(indexContent.contains("DEV"), "索引頁應包含空間代碼 DEV");
        assertTrue(indexContent.contains("核心研發專案"), "索引頁應包含空間名稱");

        String spaceContent = new String(Files.readAllBytes(spaceHtml), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(spaceContent.contains("id='DEV-1'"), "議題頁面應具備 Section ID DEV-1");
        assertTrue(spaceContent.contains("修正認證模組 NullPointerException"), "議題頁面應包含主題");
        assertTrue(spaceContent.contains("已撰寫防呆防護並提交修復補丁。"), "議題頁面應包含討論串留言");
        assertTrue(spaceContent.contains("sample-patch.txt"), "議題頁面應包含附件超連結");
    }
}
