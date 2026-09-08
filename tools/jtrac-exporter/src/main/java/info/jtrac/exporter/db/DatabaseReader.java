package info.jtrac.exporter.db;

import info.jtrac.exporter.config.ExportConfig;
import info.jtrac.exporter.i18n.I18nMessages;
import info.jtrac.exporter.model.*;

import java.sql.*;
import java.util.*;

public class DatabaseReader implements AutoCloseable {

    private final ExportConfig config;
    private final I18nMessages i18n;
    private Connection connection;
    private boolean manageConnection = true;

    public DatabaseReader(ExportConfig config) {
        this.config = config;
        this.i18n = new I18nMessages(config != null ? config.getLang() : "en");
        this.manageConnection = true;
    }

    public DatabaseReader(Connection connection, ExportConfig config) {
        this.connection = connection;
        this.config = config != null ? config : new ExportConfig();
        this.i18n = new I18nMessages(this.config.getLang());
        this.manageConnection = false;
    }

    public void connect() throws Exception {
        String url = config.getDbUrl();
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("未指定 JDBC 連線字串 (--db-url 參數為必要項)");
        }

        // HSQLDB 實體檔案路徑防呆檢查
        if (url.toLowerCase().startsWith("jdbc:hsqldb:file:")) {
            String pathPart = url.substring("jdbc:hsqldb:file:".length());
            int semicolon = pathPart.indexOf(';');
            if (semicolon != -1) {
                pathPart = pathPart.substring(0, semicolon);
            }
            pathPart = pathPart.trim();
            java.io.File scriptFile = new java.io.File(pathPart + ".script");
            java.io.File propFile = new java.io.File(pathPart + ".properties");
            java.io.File lckFile = new java.io.File(pathPart + ".lck");

            if (!scriptFile.exists() && !propFile.exists() && !lckFile.exists()) {
                String currentDir = new java.io.File(".").getAbsolutePath();
                throw new java.io.FileNotFoundException("\n[錯誤] 找不到指定的 HSQLDB 資料庫檔案！\n" +
                        "  -> 找不到資料庫檔案: " + pathPart + ".script 或 .properties\n" +
                        "  -> 目前所在工作目錄: " + currentDir + "\n" +
                        "  💡 請確認相對路徑或絕對路徑是否正確（例如 JTrac 預設資料庫通常位於 ../../../jtrac-2.3.3/data/db/jtrac）。");
            }

            if (!url.toLowerCase().contains("ifexists=")) {
                url = url + ";ifexists=true";
            }
        }

        String driver = config.getDbDriver();
        if (driver == null || driver.trim().isEmpty()) {
            driver = autoDetectDriver(url);
        }

        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                System.err.println("[警告] 找不到驅動類別: " + driver + "，嘗試直接透過 DriverManager 連線。");
            }
        }

        System.out.println(String.format(i18n.get("cli.connecting_db"), url, config.getDbUser()));
        this.connection = DriverManager.getConnection(url, config.getDbUser(), config.getDbPassword());
        System.out.println(i18n.get("cli.connected_db"));
    }

    public List<SpaceDto> readAllData() throws SQLException {
        // 1. 讀取使用者
        Map<Long, UserDto> usersMap = readUsers();
        System.out.println(String.format(i18n.get("cli.loaded_users"), usersMap.size()));

        // 2. 讀取附加檔案記錄
        Map<Long, AttachmentDto> attachmentsById = new HashMap<>();
        Map<Long, List<AttachmentDto>> attachmentsByItemId = new HashMap<>();
        readAttachments(attachmentsById, attachmentsByItemId);
        System.out.println(String.format(i18n.get("cli.loaded_attachments"), attachmentsById.size()));

        // 3. 讀取專案空間
        List<SpaceDto> spaces = readSpaces();
        System.out.println(String.format(i18n.get("cli.loaded_spaces"), spaces.size()));

        // 篩選 Space (若有指定)
        if (config.hasSpaceFilter()) {
            spaces.removeIf(s -> !config.isSpaceAllowed(s.getPrefixCode()));
            System.out.println(String.format(i18n.get("cli.space_filtered"), spaces.size()));
        }

        Map<Long, SpaceDto> spaceMap = new HashMap<>();
        for (SpaceDto s : spaces) {
            spaceMap.put(s.getId(), s);
        }

        // 4. 讀取議題
        Map<Long, ItemDto> itemsById = readItems(spaceMap, usersMap, attachmentsByItemId);
        int totalItems = itemsById.size();
        System.out.println(String.format(i18n.get("cli.loaded_issues"), totalItems));

        // 5. 讀取討論串歷史追蹤
        int historyCount = readHistory(itemsById, usersMap, attachmentsById);
        System.out.println(String.format(i18n.get("cli.loaded_history"), historyCount));

        return spaces;
    }

    private Map<Long, UserDto> readUsers() throws SQLException {
        Map<Long, UserDto> map = new HashMap<>();
        String sql = "SELECT id, login_name, name, email FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = executeQueryWithFallback(stmt, sql, "SELECT id, login_name, name, email FROM USERS")) {
            while (rs.next()) {
                long id = rs.getLong(findCol(rs, "id"));
                String login = rs.getString(findCol(rs, "login_name"));
                String name = rs.getString(findCol(rs, "name"));
                String email = rs.getString(findCol(rs, "email"));
                map.put(id, new UserDto(id, login, name, email));
            }
        } catch (SQLException e) {
            throw new SQLException("[錯誤] 無法讀取使用者資料表 (USERS)。請確認連線之資料庫是否為已初始化的 JTrac 資料庫。\n原因: " + e.getMessage(), e);
        }
        return map;
    }

    private void readAttachments(Map<Long, AttachmentDto> byId, Map<Long, List<AttachmentDto>> byItemId) throws SQLException {
        String sql = "SELECT id, item_id, file_name, file_prefix FROM attachments";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = executeQueryWithFallback(stmt, sql, "SELECT id, item_id, file_name, file_prefix FROM ATTACHMENTS")) {
            while (rs.next()) {
                long id = rs.getLong(findCol(rs, "id"));
                long itemId = rs.getLong(findCol(rs, "item_id"));
                String fileName = rs.getString(findCol(rs, "file_name"));
                long filePrefix = rs.getLong(findCol(rs, "file_prefix"));
                AttachmentDto att = new AttachmentDto(id, itemId, fileName, filePrefix);
                byId.put(id, att);
                byItemId.computeIfAbsent(itemId, k -> new ArrayList<>()).add(att);
            }
        } catch (SQLException e) {
            System.err.println("[提醒] 讀取 attachments 資料表時發生訊息 (可能無附件或表名不同): " + e.getMessage());
        }
    }

    private List<SpaceDto> readSpaces() throws SQLException {
        List<SpaceDto> list = new ArrayList<>();
        String sql = "SELECT id, prefix_code, name, description, metadata_id FROM spaces ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = executeQueryWithFallback(stmt, sql, "SELECT id, prefix_code, name, description, metadata_id FROM SPACES ORDER BY ID")) {
            while (rs.next()) {
                long id = rs.getLong(findCol(rs, "id"));
                String prefix = rs.getString(findCol(rs, "prefix_code"));
                String name = rs.getString(findCol(rs, "name"));
                String desc = rs.getString(findCol(rs, "description"));
                long meta = rs.getLong(findCol(rs, "metadata_id"));
                Long metaId = rs.wasNull() ? null : meta;
                list.add(new SpaceDto(id, prefix, name, desc, metaId));
            }
        }
        return list;
    }

    private Map<Long, ItemDto> readItems(Map<Long, SpaceDto> spaceMap,
                                        Map<Long, UserDto> usersMap,
                                        Map<Long, List<AttachmentDto>> attachmentsByItemId) throws SQLException {
        Map<Long, ItemDto> map = new HashMap<>();
        String sql = "SELECT id, space_id, sequence_num, summary, detail, status, severity, priority, " +
                "logged_by, assigned_to, time_stamp, planned_effort FROM items ORDER BY space_id, sequence_num";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = executeQueryWithFallback(stmt, sql,
                     "SELECT id, space_id, sequence_num, summary, detail, status, severity, priority, " +
                             "logged_by, assigned_to, time_stamp, planned_effort FROM ITEMS ORDER BY SPACE_ID, SEQUENCE_NUM")) {
            while (rs.next()) {
                long id = rs.getLong(findCol(rs, "id"));
                long spaceId = rs.getLong(findCol(rs, "space_id"));
                SpaceDto space = spaceMap.get(spaceId);
                if (space == null) {
                    continue; // 不在被選取的 Space 中
                }

                ItemDto item = new ItemDto();
                item.setId(id);
                item.setSpaceId(spaceId);
                item.setSpace(space);
                item.setSequenceNum(rs.getLong(findCol(rs, "sequence_num")));
                item.setSummary(rs.getString(findCol(rs, "summary")));
                item.setDetail(rs.getString(findCol(rs, "detail")));

                int status = rs.getInt(findCol(rs, "status"));
                item.setStatus(rs.wasNull() ? null : status);

                int severity = rs.getInt(findCol(rs, "severity"));
                item.setSeverity(rs.wasNull() ? null : severity);

                int priority = rs.getInt(findCol(rs, "priority"));
                item.setPriority(rs.wasNull() ? null : priority);

                long loggedBy = rs.getLong(findCol(rs, "logged_by"));
                item.setLoggedById(loggedBy);
                item.setLoggedBy(usersMap.get(loggedBy));

                long assignedTo = rs.getLong(findCol(rs, "assigned_to"));
                if (!rs.wasNull()) {
                    item.setAssignedToId(assignedTo);
                    item.setAssignedTo(usersMap.get(assignedTo));
                }

                Timestamp ts = rs.getTimestamp(findCol(rs, "time_stamp"));
                if (ts != null) {
                    item.setTimeStamp(new java.util.Date(ts.getTime()));
                }

                double effort = rs.getDouble(findCol(rs, "planned_effort"));
                if (!rs.wasNull()) {
                    item.setPlannedEffort(effort);
                }

                // 關聯附件
                List<AttachmentDto> atts = attachmentsByItemId.get(id);
                if (atts != null) {
                    item.getAttachmentList().addAll(atts);
                }

                space.getItems().add(item);
                map.put(id, item);
            }
        }
        return map;
    }

    private int readHistory(Map<Long, ItemDto> itemsById,
                            Map<Long, UserDto> usersMap,
                            Map<Long, AttachmentDto> attachmentsById) throws SQLException {
        int count = 0;
        String sql = "SELECT id, item_id, comment, attachment_id, time_stamp, logged_by, assigned_to, " +
                "status, severity, priority, actual_effort FROM history ORDER BY item_id, time_stamp, id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = executeQueryWithFallback(stmt, sql,
                     "SELECT id, item_id, comment, attachment_id, time_stamp, logged_by, assigned_to, " +
                             "status, severity, priority, actual_effort FROM HISTORY ORDER BY ITEM_ID, TIME_STAMP, ID")) {
            while (rs.next()) {
                long itemId = rs.getLong(findCol(rs, "item_id"));
                ItemDto item = itemsById.get(itemId);
                if (item == null) {
                    continue;
                }

                HistoryDto h = new HistoryDto();
                h.setId(rs.getLong(findCol(rs, "id")));
                h.setItemId(itemId);
                h.setComment(rs.getString(findCol(rs, "comment")));

                long attId = rs.getLong(findCol(rs, "attachment_id"));
                if (!rs.wasNull()) {
                    h.setAttachmentId(attId);
                    h.setAttachment(attachmentsById.get(attId));
                }

                Timestamp ts = rs.getTimestamp(findCol(rs, "time_stamp"));
                if (ts != null) {
                    h.setTimeStamp(new java.util.Date(ts.getTime()));
                }

                long loggedBy = rs.getLong(findCol(rs, "logged_by"));
                h.setLoggedById(loggedBy);
                h.setLoggedBy(usersMap.get(loggedBy));

                long assignedTo = rs.getLong(findCol(rs, "assigned_to"));
                if (!rs.wasNull()) {
                    h.setAssignedToId(assignedTo);
                    h.setAssignedTo(usersMap.get(assignedTo));
                }

                int status = rs.getInt(findCol(rs, "status"));
                h.setStatus(rs.wasNull() ? null : status);

                int severity = rs.getInt(findCol(rs, "severity"));
                h.setSeverity(rs.wasNull() ? null : severity);

                int priority = rs.getInt(findCol(rs, "priority"));
                h.setPriority(rs.wasNull() ? null : priority);

                double effort = rs.getDouble(findCol(rs, "actual_effort"));
                if (!rs.wasNull()) {
                    h.setActualEffort(effort);
                }

                item.getHistoryList().add(h);
                count++;
            }
        }

        // 清理各議題之初始無留言 OPEN 快照 (Eliminate initial empty-comment OPEN snapshot)
        for (ItemDto item : itemsById.values()) {
            List<HistoryDto> histories = item.getHistoryList();
            if (!histories.isEmpty()) {
                HistoryDto first = histories.get(0);
                boolean isInitialOpenSnapshot = (first.getStatus() != null && first.getStatus() == 1)
                        && (first.getComment() == null || first.getComment().trim().isEmpty());
                if (isInitialOpenSnapshot) {
                    histories.remove(0);
                }
            }
        }

        return count;
    }

    private ResultSet executeQueryWithFallback(Statement stmt, String primarySql, String fallbackSql) throws SQLException {
        try {
            return stmt.executeQuery(primarySql);
        } catch (SQLException e) {
            return stmt.executeQuery(fallbackSql);
        }
    }

    private int findCol(ResultSet rs, String colName) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int count = md.getColumnCount();
        for (int i = 1; i <= count; i++) {
            if (md.getColumnLabel(i).equalsIgnoreCase(colName) || md.getColumnName(i).equalsIgnoreCase(colName)) {
                return i;
            }
        }
        return rs.findColumn(colName);
    }

    public static String autoDetectDriver(String url) {
        if (url == null) return null;
        String lower = url.toLowerCase();
        if (lower.startsWith("jdbc:mysql:")) {
            return "com.mysql.cj.jdbc.Driver";
        } else if (lower.startsWith("jdbc:mariadb:")) {
            return "org.mariadb.jdbc.Driver";
        } else if (lower.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        } else if (lower.startsWith("jdbc:hsqldb:")) {
            return "org.hsqldb.jdbcDriver";
        } else if (lower.startsWith("jdbc:sqlserver:")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (lower.startsWith("jdbc:oracle:")) {
            return "oracle.jdbc.OracleDriver";
        }
        return null;
    }

    @Override
    public void close() {
        if (manageConnection && connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {}
        }
    }
}
