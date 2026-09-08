package info.jtrac.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class HsqldbDatabaseMigratorTest {

    @Test
    public void testUnescapeUnicode() {
        String input = "INSERT INTO ATTACHMENTS VALUES(1,NULL,'\\u62db\\u624b.txt',1,NULL)";
        String result = HsqldbDatabaseMigrator.unescapeUnicode(input);
        assertEquals("INSERT INTO ATTACHMENTS VALUES(1,NULL,'招手.txt',1,NULL)", result);
    }

    @Test
    public void testLegacyDetectionAndMigration(@TempDir Path tempDir) throws Exception {
        File dbDir = tempDir.toFile();
        String dbName = "test_jtrac";

        // Create mock 1.8.0 properties
        File propsFile = new File(dbDir, dbName + ".properties");
        try (FileWriter fw = new FileWriter(propsFile)) {
            fw.write("#HSQL Database Engine\n");
            fw.write("version=1.8.0\n");
            fw.write("hsqldb.compatible_version=1.8.0\n");
            fw.write("hsqldb.original_version=1.8.0\n");
        }

        // Create mock 1.8.0 script
        File scriptFile = new File(dbDir, dbName + ".script");
        try (FileWriter fw = new FileWriter(scriptFile)) {
            fw.write("CREATE SCHEMA PUBLIC AUTHORIZATION DBA\n");
            fw.write("CREATE MEMORY TABLE USERS(ID BIGINT NOT NULL PRIMARY KEY, LOGIN_NAME VARCHAR(50) NOT NULL, NAME VARCHAR(50))\n");
            fw.write("CREATE USER SA PASSWORD \"\"\n");
            fw.write("GRANT DBA TO SA\n");
            fw.write("SET WRITE_DELAY 20\n");
            fw.write("SET SCHEMA PUBLIC\n");
            fw.write("INSERT INTO USERS VALUES(1,'admin','\\u7ba1\\u7406\\u54e1')\n");
        }

        // Verify detection
        assertTrue(HsqldbDatabaseMigrator.isLegacy18Database(propsFile));

        // Perform migration
        boolean migrated = HsqldbDatabaseMigrator.checkAndMigrateIfNecessary(dbDir, dbName);
        assertTrue(migrated);

        // Verify properties is now modern
        assertFalse(HsqldbDatabaseMigrator.isLegacy18Database(propsFile));

        // Connect with HSQLDB 2.x driver and verify data
        Class.forName("org.hsqldb.jdbcDriver");
        String url = "jdbc:hsqldb:file:" + new File(dbDir, dbName).getAbsolutePath() + ";shutdown=true";
        try (Connection conn = DriverManager.getConnection(url, "sa", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, LOGIN_NAME, NAME FROM USERS WHERE ID = 1")) {
            assertTrue(rs.next());
            assertEquals(1L, rs.getLong("ID"));
            assertEquals("admin", rs.getString("LOGIN_NAME"));
            assertEquals("管理員", rs.getString("NAME"));
        }
    }
}
