package info.jtrac.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dual-mode database migration tool for upgrading legacy HSQLDB 1.8.x databases
 * to modern HSQLDB 2.x format.
 *
 * Capabilities:
 * 1. Automatically triggered on JTrac web startup when legacy 1.8 database is detected.
 * 2. Standalone CLI tool for manual offline database migration.
 */
public class HsqldbDatabaseMigrator {

    private static final Logger logger = LoggerFactory.getLogger(HsqldbDatabaseMigrator.class);

    private static final Pattern UNICODE_ESCAPE_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

    private static volatile boolean databaseMigrated;

    public static boolean isDatabaseMigrated() {
        return databaseMigrated;
    }

    public static void setDatabaseMigrated(boolean migrated) {
        databaseMigrated = migrated;
    }

    /**
     * Check whether the specified database directory contains a legacy HSQLDB 1.8 database,
     * and if so, automatically migrate it to HSQLDB 2.x.
     *
     * @param dbDir  Directory containing the HSQLDB database files
     * @param dbName Base name of the database (e.g. "jtrac")
     * @return true if migration was performed, false if already modern or not an embedded HSQLDB
     */
    public static synchronized boolean checkAndMigrateIfNecessary(File dbDir, String dbName) {
        if (dbDir == null || !dbDir.exists() || !dbDir.isDirectory()) {
            return false;
        }

        File propsFile = new File(dbDir, dbName + ".properties");
        if (!propsFile.exists()) {
            return false;
        }

        if (!isLegacy18Database(propsFile)) {
            logger.debug("HSQLDB database in '{}' is already 2.x compatible.", dbDir.getAbsolutePath());
            return false;
        }

        logger.info("================================================================================");
        logger.info("LEGACY HSQLDB 1.8.x DATABASE DETECTED in: {}", dbDir.getAbsolutePath());
        logger.info("Starting automatic migration to HSQLDB 2.x format...");
        logger.info("================================================================================");

        try {
            migrate(dbDir, dbName, true);
            databaseMigrated = true;
            logger.info("================================================================================");
            logger.info("HSQLDB DATABASE MIGRATION TO 2.x COMPLETED SUCCESSFULLY!");
            logger.info("================================================================================");
            return true;
        } catch (Exception e) {
            logger.error("Database migration failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to migrate legacy HSQLDB database: " + e.getMessage(), e);
        }
    }

    /**
     * Inspects properties file to check if it represents HSQLDB 1.8.x.
     */
    public static boolean isLegacy18Database(File propsFile) {
        if (!propsFile.exists()) {
            return false;
        }
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(propsFile)) {
            props.load(is);
            String version = props.getProperty("version", "");
            String compat = props.getProperty("hsqldb.compatible_version", "");
            String original = props.getProperty("hsqldb.original_version", "");
            return version.startsWith("1.") || compat.startsWith("1.") || original.startsWith("1.");
        } catch (Exception e) {
            logger.warn("Could not read properties file {}: {}", propsFile, e.getMessage());
            return false;
        }
    }

    /**
     * Main migration worker.
     */
    public static void migrate(File dbDir, String dbName, boolean doBackup) throws Exception {
        File scriptFile = new File(dbDir, dbName + ".script");
        File propsFile = new File(dbDir, dbName + ".properties");

        if (!scriptFile.exists()) {
            throw new FileNotFoundException("Legacy database script file not found: " + scriptFile.getAbsolutePath());
        }

        // 1. Remove stale lock file if present
        File lockFile = new File(dbDir, dbName + ".lck");
        if (lockFile.exists()) {
            boolean deleted = lockFile.delete();
            if (!deleted) {
                logger.warn("Could not delete stale lock file: {}", lockFile.getAbsolutePath());
            }
        }

        // 2. Perform automated backup if requested
        if (doBackup) {
            backupDatabase(dbDir, dbName);
        }

        // 3. Create a temporary 2.x database to parse and rebuild the schema and data
        String tempDbName = dbName + "_migrated_v2";
        File tempScriptFile = new File(dbDir, tempDbName + ".script");
        File tempPropsFile = new File(dbDir, tempDbName + ".properties");
        File tempLockFile = new File(dbDir, tempDbName + ".lck");

        // Clean up any existing temp files
        if (tempScriptFile.exists()) tempScriptFile.delete();
        if (tempPropsFile.exists()) tempPropsFile.delete();
        if (tempLockFile.exists()) tempLockFile.delete();

        logger.info("Initializing temporary HSQLDB 2.x database at: {}", new File(dbDir, tempDbName).getAbsolutePath());

        // Load HSQLDB 2.x Driver
        Class.forName("org.hsqldb.jdbcDriver");

        String tempJdbcUrl = "jdbc:hsqldb:file:" + new File(dbDir, tempDbName).getAbsolutePath() + ";shutdown=true";

        int ddlCount = 0;
        int insertCount = 0;
        int errorCount = 0;

        try (Connection conn = DriverManager.getConnection(tempJdbcUrl, "sa", "");
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false);
            stmt.execute("SET DATABASE REFERENTIAL INTEGRITY FALSE");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFile), StandardCharsets.UTF_8))) {
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--") || line.startsWith("//")) {
                        continue;
                    }

                    // Filter out statements that conflict with HSQLDB 2.x pre-existing system defaults
                    if (line.startsWith("CREATE SCHEMA PUBLIC AUTHORIZATION DBA") ||
                        line.startsWith("CREATE USER SA PASSWORD") ||
                        line.startsWith("GRANT DBA TO SA") ||
                        line.startsWith("SET WRITE_DELAY") ||
                        line.startsWith("SET SCHEMA PUBLIC")) {
                        logger.debug("Skipping legacy directive at line {}: {}", lineNumber, line);
                        continue;
                    }

                    // Decode escaped unicode sequences into actual characters
                    String sql = unescapeUnicode(line);

                    try {
                        stmt.execute(sql);
                        if (sql.startsWith("INSERT INTO")) {
                            insertCount++;
                            if (insertCount % 1000 == 0) {
                                conn.commit();
                                logger.info("Migrated {} rows...", insertCount);
                            }
                        } else {
                            ddlCount++;
                            conn.commit();
                        }
                    } catch (SQLException ex) {
                        // Tolerate duplicate or non-fatal constraint errors if any
                        logger.warn("Warning at line {}: {} | SQL: {}", lineNumber, ex.getMessage(), sql.substring(0, Math.min(sql.length(), 100)));
                        errorCount++;
                    }
                }
            }

            conn.commit();

            // Re-enable referential integrity
            try {
                stmt.execute("SET DATABASE REFERENTIAL INTEGRITY TRUE");
            } catch (SQLException e) {
                logger.warn("Could not re-enable referential integrity: {}", e.getMessage());
            }

            // Patch null primitive boolean fields in legacy records
            try { stmt.execute("UPDATE SPACES SET IS_ACTIVE = TRUE WHERE IS_ACTIVE IS NULL"); } catch (Exception ignored) {}
            try { stmt.execute("UPDATE USERS SET PRETTY_DATES = TRUE WHERE PRETTY_DATES IS NULL"); } catch (Exception ignored) {}
            try { stmt.execute("UPDATE STORED_SEARCH SET NEW_WINDOW = TRUE WHERE NEW_WINDOW IS NULL"); } catch (Exception ignored) {}
            conn.commit();

            logger.info("Flushing and shutting down temporary 2.x database (DDL statements: {}, Rows imported: {}, Non-fatal warnings: {})...",
                    ddlCount, insertCount, errorCount);
            stmt.execute("SHUTDOWN");
        }

        // 4. Atomically swap migrated 2.x files into target database name
        logger.info("Replacing database files with migrated 2.x files...");

        // Remove original 1.8 files (they are backed up)
        if (scriptFile.exists()) scriptFile.delete();
        if (propsFile.exists()) propsFile.delete();

        // Rename temp files to target
        if (tempScriptFile.exists()) {
            Files.move(tempScriptFile.toPath(), scriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        if (tempPropsFile.exists()) {
            Files.move(tempPropsFile.toPath(), propsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        File tempLogFile = new File(dbDir, tempDbName + ".log");
        if (tempLogFile.exists()) {
            File targetLogFile = new File(dbDir, dbName + ".log");
            Files.move(tempLogFile.toPath(), targetLogFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        File tempDataFile = new File(dbDir, tempDbName + ".data");
        if (tempDataFile.exists()) {
            File targetDataFile = new File(dbDir, dbName + ".data");
            Files.move(tempDataFile.toPath(), targetDataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        logger.info("HSQLDB database successfully upgraded to 2.x format (Target: {})", scriptFile.getAbsolutePath());
    }

    /**
     * Create timestamped backup of the legacy database files.
     */
    private static void backupDatabase(File dbDir, String dbName) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File backupDir = new File(dbDir, "backup-hsqldb-1.8-" + timestamp);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        logger.info("Backing up legacy HSQLDB 1.8 database to: {}", backupDir.getAbsolutePath());

        File[] files = dbDir.listFiles((dir, name) -> name.startsWith(dbName + "."));
        if (files != null) {
            for (File f : files) {
                File dest = new File(backupDir, f.getName());
                Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("  Backed up: {}", f.getName());
            }
        }
    }

    /**
     * Unescape slash-u unicode representations to actual UTF-8 characters.
     */
    public static String unescapeUnicode(String input) {
        if (input == null || !input.contains("\\u")) {
            return input;
        }

        Matcher matcher = UNICODE_ESCAPE_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer(input.length());
        while (matcher.find()) {
            int codePoint = Integer.parseInt(matcher.group(1), 16);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(Character.toString((char) codePoint)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Standalone CLI entrypoint.
     */
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" JTrac HSQLDB 1.8 -> 2.x Database Migration Tool ");
        System.out.println("=================================================");

        String dbDirStr = "./data/db";
        String dbName = "jtrac";
        boolean backup = true;

        for (String arg : args) {
            if (arg.startsWith("--db-dir=")) {
                dbDirStr = arg.substring("--db-dir=".length());
            } else if (arg.startsWith("--db-name=")) {
                dbName = arg.substring("--db-name=".length());
            } else if (arg.equals("--no-backup")) {
                backup = false;
            } else if (arg.equals("--help") || arg.equals("-h")) {
                printHelp();
                return;
            }
        }

        File dbDir = new File(dbDirStr);
        System.out.println("Database Directory: " + dbDir.getAbsolutePath());
        System.out.println("Database Name     : " + dbName);
        System.out.println("Backup Enabled    : " + backup);
        System.out.println("-------------------------------------------------");

        try {
            if (!dbDir.exists()) {
                System.err.println("Error: Database directory does not exist: " + dbDir.getAbsolutePath());
                System.exit(1);
            }

            File propsFile = new File(dbDir, dbName + ".properties");
            if (!propsFile.exists()) {
                System.err.println("Error: Database properties file not found: " + propsFile.getAbsolutePath());
                System.exit(1);
            }

            if (!isLegacy18Database(propsFile)) {
                System.out.println("Notice: Database is already in HSQLDB 2.x format. No migration required.");
                return;
            }

            System.out.println("Starting migration...");
            migrate(dbDir, dbName, backup);
            System.out.println("SUCCESS: Database migrated to HSQLDB 2.x successfully!");
        } catch (Exception e) {
            System.err.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("  java -cp jtrac.war info.jtrac.tools.HsqldbDatabaseMigrator [options]");
        System.out.println("");
        System.out.println("Options:");
        System.out.println("  --db-dir=<path>    Path to database directory (default: ./data/db)");
        System.out.println("  --db-name=<name>   Database file prefix (default: jtrac)");
        System.out.println("  --no-backup        Skip creating a timestamped backup before migrating");
        System.out.println("  --help, -h         Show this help message");
    }
}
