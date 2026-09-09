package info.jtrac.backup.service;

import info.jtrac.Jtrac;
import info.jtrac.JtracDao;
import info.jtrac.backup.model.BackupManifest;
import info.jtrac.backup.model.SystemBackupData;
import info.jtrac.backup.model.dto.*;
import info.jtrac.domain.Role;
import info.jtrac.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

/**
 * Core restore service implementing:
 * 1. Pre-restore safety snapshot
 * 2. Anti-lockout credential shield for operator
 * 3. Topological clean-and-restore database pipeline
 * 4. Attachments directory synchronization
 * 5. Background Lucene reindexing
 */
public class BackupRestoreService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DataSource dataSource;
    private JtracDao dao;
    private Jtrac jtrac;
    private BackupExportService backupExportService;
    private ZipBundleService zipBundleService;
    private String jtracHome;

    public BackupRestoreService() {}

    public BackupRestoreService(DataSource dataSource, JtracDao dao, Jtrac jtrac,
                                BackupExportService backupExportService,
                                ZipBundleService zipBundleService, String jtracHome) {
        this.dataSource = dataSource;
        this.dao = dao;
        this.jtrac = jtrac;
        this.backupExportService = backupExportService;
        this.zipBundleService = zipBundleService;
        this.jtracHome = jtracHome;
    }

    public void setDataSource(DataSource dataSource) { this.dataSource = dataSource; }
    public void setDao(JtracDao dao) { this.dao = dao; }
    public void setJtrac(Jtrac jtrac) { this.jtrac = jtrac; }
    public void setBackupExportService(BackupExportService backupExportService) { this.backupExportService = backupExportService; }
    public void setZipBundleService(ZipBundleService zipBundleService) { this.zipBundleService = zipBundleService; }
    public void setJtracHome(String jtracHome) { this.jtracHome = jtracHome; }

    /**
     * Create an emergency pre-restore safety snapshot on the server.
     */
    public File createSafetySnapshot(String operatorLoginName) throws IOException {
        logger.info("Creating pre-restore safety snapshot for operator: {}", operatorLoginName);
        File backupsDir = new File(jtracHome, "backups");
        if (!backupsDir.exists()) {
            backupsDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File snapshotFile = new File(backupsDir, "snapshot-before-restore-" + timestamp + ".zip");

        SystemBackupData currentData = jtrac != null ? jtrac.exportSystemData() : backupExportService.exportSystemData();
        BackupManifest manifest = backupExportService.createManifest(currentData, operatorLoginName);

        try (FileOutputStream fos = new FileOutputStream(snapshotFile)) {
            zipBundleService.createBackupZip(manifest, currentData, jtracHome, fos);
        }

        logger.info("Safety snapshot successfully created at: {}", snapshotFile.getAbsolutePath());
        return snapshotFile;
    }

    /**
     * Apply Anti-Lockout Credential Shield to ensure the currently logged-in SuperUser
     * can continue logging in with their current password and retains ROLE_ADMIN.
     */
    public void applyAntiLockoutShield(SystemBackupData data, User currentOperator) {
        if (currentOperator == null || currentOperator.getLoginName() == null) {
            logger.warn("No current operator provided, skipping anti-lockout credential shield");
            return;
        }

        String targetLogin = currentOperator.getLoginName().trim();
        logger.info("Applying anti-lockout credential shield for operator: '{}'", targetLogin);

        UserDto matchedUser = null;
        for (UserDto u : data.getUsers()) {
            if (u.getLoginName() != null && u.getLoginName().equalsIgnoreCase(targetLogin)) {
                matchedUser = u;
                break;
            }
        }

        if (matchedUser != null) {
            logger.info("Operator '{}' exists in backup data. Shielding password and ensuring ROLE_ADMIN.", targetLogin);
            // Retain existing ID (preserving historical FK from items and histories),
            // but overwrite password with currently active password hash
            matchedUser.setPassword(currentOperator.getPassword());
            matchedUser.setLocked(false);

            // Ensure ROLE_ADMIN exists for this user in userSpaceRoles
            boolean hasAdminRole = false;
            for (UserSpaceRoleDto usr : data.getUserSpaceRoles()) {
                if (usr.getUserId() == matchedUser.getId() && usr.getSpaceId() == null
                        && Role.ROLE_ADMIN.equals(usr.getRoleKey())) {
                    hasAdminRole = true;
                    break;
                }
            }
            if (!hasAdminRole) {
                long maxUsrId = 1;
                for (UserSpaceRoleDto usr : data.getUserSpaceRoles()) {
                    if (usr.getId() >= maxUsrId) maxUsrId = usr.getId() + 1;
                }
                data.getUserSpaceRoles().add(new UserSpaceRoleDto(maxUsrId, matchedUser.getId(), null, Role.ROLE_ADMIN));
            }
        } else {
            logger.info("Operator '{}' does NOT exist in backup data. Injecting as SuperUser.", targetLogin);
            long newUserId = 1;
            for (UserDto u : data.getUsers()) {
                if (u.getId() >= newUserId) newUserId = u.getId() + 1;
            }

            UserDto injected = new UserDto();
            injected.setId(newUserId);
            injected.setLoginName(currentOperator.getLoginName());
            injected.setName(currentOperator.getName() != null ? currentOperator.getName() : currentOperator.getLoginName());
            injected.setPassword(currentOperator.getPassword());
            injected.setEmail(currentOperator.getEmail());
            injected.setLocale(currentOperator.getLocale());
            injected.setLocked(false);
            injected.setPrettyDates(currentOperator.isPrettyDates());
            data.getUsers().add(injected);

            long maxUsrId = 1;
            for (UserSpaceRoleDto usr : data.getUserSpaceRoles()) {
                if (usr.getId() >= maxUsrId) maxUsrId = usr.getId() + 1;
            }
            data.getUserSpaceRoles().add(new UserSpaceRoleDto(maxUsrId, newUserId, null, Role.ROLE_ADMIN));
        }
    }

    /**
     * Restore relational database contents in strict topological dependency order.
     */
    public void restoreDatabase(SystemBackupData data) throws SQLException {
        logger.info("Starting database topological restore...");

        if (dao != null) {
            try {
                dao.clearSession();
            } catch (Exception ignored) {}
        }

        Connection conn = null;
        boolean isSpringManaged = false;
        boolean origAutoCommit = true;

        try {
            try {
                conn = org.springframework.jdbc.datasource.DataSourceUtils.getConnection(dataSource);
                isSpringManaged = org.springframework.jdbc.datasource.DataSourceUtils.isConnectionTransactional(conn, dataSource);
            } catch (Exception e) {
                conn = dataSource.getConnection();
            }

            if (!isSpringManaged) {
                origAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);
            }

            try {
                // 1. Wipe existing data in reverse dependency order
                wipeDatabase(conn);

                // 2. Insert new data in forward dependency order
                insertConfigs(conn, data.getConfigs());
                insertTags(conn, data.getTags());
                insertStoredSearches(conn, data.getStoredSearches());
                insertMetadatas(conn, data.getMetadatas());
                insertSpaceSequences(conn, data.getSpaceSequences());
                insertSpaces(conn, data.getSpaces());
                insertUsers(conn, data.getUsers());
                insertUserSpaceRoles(conn, data.getUserSpaceRoles());
                insertAttachments(conn, data.getAttachments());
                insertItems(conn, data.getItems());
                insertItemItems(conn, data.getItemItems());
                insertItemUsers(conn, data.getItemUsers());
                insertItemTags(conn, data.getItemTags());
                insertHistories(conn, data.getHistories());

                // 3. Calibrate database sequences
                calibrateSequences(conn);

                if (!isSpringManaged) {
                    conn.commit();
                }
                logger.info("Database topological restore successfully executed.");
            } catch (Exception ex) {
                if (!isSpringManaged) {
                    conn.rollback();
                }
                logger.error("Error during database restore", ex);
                throw new SQLException("Database restore failed: " + ex.getMessage(), ex);
            } finally {
                if (!isSpringManaged) {
                    conn.setAutoCommit(origAutoCommit);
                }
            }
        } finally {
            if (conn != null) {
                org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection(conn, dataSource);
            }
            if (dao != null) {
                try {
                    dao.clearSession();
                } catch (Exception ignored) {}
            }
        }
    }

    private void wipeDatabase(Connection conn) throws SQLException {
        logger.info("Wiping existing database tables in reverse dependency order...");
        String[] tables = {
                "item_tags", "item_users", "item_items", "history", "attachments",
                "items", "user_space_roles", "users", "space_sequence", "spaces",
                "metadata", "storedsearch", "tags", "config"
        };
        for (String table : tables) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM " + table);
            }
        }
    }

    private void insertConfigs(Connection conn, List<ConfigDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO config (param, value) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ConfigDto c : list) {
                ps.setString(1, c.getParam());
                ps.setString(2, c.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertTags(Connection conn, List<TagDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO tags (id, type, name, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (TagDto t : list) {
                ps.setLong(1, t.getId());
                setIntegerOrNull(ps, 2, t.getType());
                ps.setString(3, t.getName());
                ps.setString(4, t.getDescription());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertStoredSearches(Connection conn, List<StoredSearchDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO storedsearch (id, name, query, new_window) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (StoredSearchDto ss : list) {
                ps.setLong(1, ss.getId());
                ps.setString(2, ss.getName());
                ps.setString(3, ss.getQuery());
                ps.setBoolean(4, ss.getNewWindow() != null && ss.getNewWindow());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertMetadatas(Connection conn, List<MetadataDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO metadata (id, version, type, name, description, parent_id, xml_string) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (MetadataDto m : list) {
                ps.setLong(1, m.getId());
                ps.setInt(2, m.getVersion());
                setIntegerOrNull(ps, 3, m.getType());
                ps.setString(4, m.getName());
                ps.setString(5, m.getDescription());
                setLongOrNull(ps, 6, m.getParentId());
                ps.setString(7, m.getXmlString());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertSpaceSequences(Connection conn, List<SpaceSequenceDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO space_sequence (id, next_seq_num) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (SpaceSequenceDto ss : list) {
                ps.setLong(1, ss.getId());
                ps.setLong(2, ss.getNextSeqNum());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertSpaces(Connection conn, List<SpaceDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO spaces (id, version, type, prefix_code, name, description, guest_allowed, is_active, metadata_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (SpaceDto s : list) {
                ps.setLong(1, s.getId());
                ps.setInt(2, s.getVersion());
                setIntegerOrNull(ps, 3, s.getType());
                ps.setString(4, s.getPrefixCode());
                ps.setString(5, s.getName());
                ps.setString(6, s.getDescription());
                ps.setBoolean(7, s.isGuestAllowed());
                ps.setBoolean(8, s.getIsActive() != null && s.getIsActive());
                setLongOrNull(ps, 9, s.getMetadataId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertUsers(Connection conn, List<UserDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO users (id, type, parent, login_name, name, password, email, locale, locked, prettyDates, info, metadata_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (UserDto u : list) {
                ps.setLong(1, u.getId());
                setIntegerOrNull(ps, 2, u.getType());
                setLongOrNull(ps, 3, u.getParentId());
                ps.setString(4, u.getLoginName());
                ps.setString(5, u.getName());
                ps.setString(6, u.getPassword());
                ps.setString(7, u.getEmail());
                ps.setString(8, u.getLocale());
                ps.setBoolean(9, u.isLocked());
                ps.setBoolean(10, u.getPrettyDates() != null && u.getPrettyDates());
                ps.setString(11, u.getInfo());
                setLongOrNull(ps, 12, u.getMetadataId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertUserSpaceRoles(Connection conn, List<UserSpaceRoleDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO user_space_roles (id, user_id, space_id, role_key) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (UserSpaceRoleDto usr : list) {
                ps.setLong(1, usr.getId());
                ps.setLong(2, usr.getUserId());
                setLongOrNull(ps, 3, usr.getSpaceId());
                ps.setString(4, usr.getRoleKey());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertAttachments(Connection conn, List<AttachmentDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO attachments (id, previous_id, file_name, file_prefix) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (AttachmentDto a : list) {
                ps.setLong(1, a.getId());
                setLongOrNull(ps, 2, a.getPreviousId());
                ps.setString(3, a.getFileName());
                ps.setLong(4, a.getFilePrefix());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertItems(Connection conn, List<ItemDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO items (" +
                "id, version, type, parent_id, space_id, sequence_num, time_stamp, planned_effort, logged_by, assigned_to, summary, detail, status, severity, priority, " +
                "cus_dbl_01, cus_dbl_02, cus_dbl_03, " +
                "cus_int_01, cus_int_02, cus_int_03, cus_int_04, cus_int_05, cus_int_06, cus_int_07, cus_int_08, cus_int_09, cus_int_10, " +
                "cus_str_01, cus_str_02, cus_str_03, cus_str_04, cus_str_05, " +
                "cus_tim_01, cus_tim_02, cus_tim_03" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ItemDto i : list) {
                int col = 1;
                ps.setLong(col++, i.getId());
                ps.setInt(col++, i.getVersion());
                setIntegerOrNull(ps, col++, i.getType());
                setLongOrNull(ps, col++, i.getParentId());
                ps.setLong(col++, i.getSpaceId());
                ps.setLong(col++, i.getSequenceNum());
                setTimestampOrNull(ps, col++, i.getTimeStamp());
                setDoubleOrNull(ps, col++, i.getPlannedEffort());
                ps.setLong(col++, i.getLoggedById());
                setLongOrNull(ps, col++, i.getAssignedToId());
                ps.setString(col++, i.getSummary());
                ps.setString(col++, i.getDetail());
                setIntegerOrNull(ps, col++, i.getStatus());
                setIntegerOrNull(ps, col++, i.getSeverity());
                setIntegerOrNull(ps, col++, i.getPriority());

                setDoubleOrNull(ps, col++, i.getCusDbl01());
                setDoubleOrNull(ps, col++, i.getCusDbl02());
                setDoubleOrNull(ps, col++, i.getCusDbl03());

                setIntegerOrNull(ps, col++, i.getCusInt01());
                setIntegerOrNull(ps, col++, i.getCusInt02());
                setIntegerOrNull(ps, col++, i.getCusInt03());
                setIntegerOrNull(ps, col++, i.getCusInt04());
                setIntegerOrNull(ps, col++, i.getCusInt05());
                setIntegerOrNull(ps, col++, i.getCusInt06());
                setIntegerOrNull(ps, col++, i.getCusInt07());
                setIntegerOrNull(ps, col++, i.getCusInt08());
                setIntegerOrNull(ps, col++, i.getCusInt09());
                setIntegerOrNull(ps, col++, i.getCusInt10());

                ps.setString(col++, i.getCusStr01());
                ps.setString(col++, i.getCusStr02());
                ps.setString(col++, i.getCusStr03());
                ps.setString(col++, i.getCusStr04());
                ps.setString(col++, i.getCusStr05());

                setTimestampOrNull(ps, col++, i.getCusTim01());
                setTimestampOrNull(ps, col++, i.getCusTim02());
                setTimestampOrNull(ps, col++, i.getCusTim03());

                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertItemItems(Connection conn, List<ItemItemDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO item_items (id, item_id, related_item_id, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ItemItemDto ii : list) {
                ps.setLong(1, ii.getId());
                ps.setLong(2, ii.getItemId());
                ps.setLong(3, ii.getRelatedItemId());
                setIntegerOrNull(ps, 4, ii.getType());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertItemUsers(Connection conn, List<ItemUserDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO item_users (id, item_id, user_id, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ItemUserDto iu : list) {
                ps.setLong(1, iu.getId());
                ps.setLong(2, iu.getItemId());
                ps.setLong(3, iu.getUserId());
                ps.setInt(4, iu.getType() != null ? iu.getType() : 0);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertItemTags(Connection conn, List<ItemTagDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO item_tags (id, item_id, tag_id, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ItemTagDto it : list) {
                ps.setLong(1, it.getId());
                ps.setLong(2, it.getItemId());
                ps.setLong(3, it.getTagId());
                setIntegerOrNull(ps, 4, it.getType());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertHistories(Connection conn, List<HistoryDto> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO history (" +
                "id, version, type, item_id, actual_effort, attachment_id, comment, time_stamp, logged_by, assigned_to, summary, detail, status, severity, priority, " +
                "cus_dbl_01, cus_dbl_02, cus_dbl_03, " +
                "cus_int_01, cus_int_02, cus_int_03, cus_int_04, cus_int_05, cus_int_06, cus_int_07, cus_int_08, cus_int_09, cus_int_10, " +
                "cus_str_01, cus_str_02, cus_str_03, cus_str_04, cus_str_05, " +
                "cus_tim_01, cus_tim_02, cus_tim_03" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (HistoryDto h : list) {
                int col = 1;
                ps.setLong(col++, h.getId());
                ps.setInt(col++, h.getVersion());
                setIntegerOrNull(ps, col++, h.getType());
                ps.setLong(col++, h.getItemId());
                setDoubleOrNull(ps, col++, h.getActualEffort());
                setLongOrNull(ps, col++, h.getAttachmentId());
                ps.setString(col++, h.getComment());
                setTimestampOrNull(ps, col++, h.getTimeStamp());
                ps.setLong(col++, h.getLoggedById());
                setLongOrNull(ps, col++, h.getAssignedToId());
                ps.setString(col++, h.getSummary());
                ps.setString(col++, h.getDetail());
                setIntegerOrNull(ps, col++, h.getStatus());
                setIntegerOrNull(ps, col++, h.getSeverity());
                setIntegerOrNull(ps, col++, h.getPriority());

                setDoubleOrNull(ps, col++, h.getCusDbl01());
                setDoubleOrNull(ps, col++, h.getCusDbl02());
                setDoubleOrNull(ps, col++, h.getCusDbl03());

                setIntegerOrNull(ps, col++, h.getCusInt01());
                setIntegerOrNull(ps, col++, h.getCusInt02());
                setIntegerOrNull(ps, col++, h.getCusInt03());
                setIntegerOrNull(ps, col++, h.getCusInt04());
                setIntegerOrNull(ps, col++, h.getCusInt05());
                setIntegerOrNull(ps, col++, h.getCusInt06());
                setIntegerOrNull(ps, col++, h.getCusInt07());
                setIntegerOrNull(ps, col++, h.getCusInt08());
                setIntegerOrNull(ps, col++, h.getCusInt09());
                setIntegerOrNull(ps, col++, h.getCusInt10());

                ps.setString(col++, h.getCusStr01());
                ps.setString(col++, h.getCusStr02());
                ps.setString(col++, h.getCusStr03());
                ps.setString(col++, h.getCusStr04());
                ps.setString(col++, h.getCusStr05());

                setTimestampOrNull(ps, col++, h.getCusTim01());
                setTimestampOrNull(ps, col++, h.getCusTim02());
                setTimestampOrNull(ps, col++, h.getCusTim03());

                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void calibrateSequences(Connection conn) {
        String dbProduct = "";
        try {
            dbProduct = conn.getMetaData().getDatabaseProductName().toLowerCase();
        } catch (Exception ignored) {}

        String[] tables = {
                "items", "history", "users", "spaces", "metadata", "attachments",
                "user_space_roles", "tags", "storedsearch", "item_items", "item_users", "item_tags"
        };

        for (String table : tables) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM " + table)) {
                if (rs.next()) {
                    long maxId = rs.getLong(1);
                    long nextId = maxId + 1;
                    if (dbProduct.contains("hsql")) {
                        try (Statement alterStmt = conn.createStatement()) {
                            alterStmt.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH " + nextId);
                        }
                    } else if (dbProduct.contains("mysql") || dbProduct.contains("mariadb")) {
                        try (Statement alterStmt = conn.createStatement()) {
                            alterStmt.executeUpdate("ALTER TABLE " + table + " AUTO_INCREMENT = " + nextId);
                        }
                    } else if (dbProduct.contains("postgresql")) {
                        try (Statement alterStmt = conn.createStatement()) {
                            alterStmt.executeQuery("SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), " + maxId + ", true)");
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Sequence calibration for table {} not required or skipped: {}", table, e.getMessage());
            }
        }
    }

    /**
     * Restore physical attachment files by synchronizing the extracted attachments directory.
     */
    public void restoreAttachments(File extractedAttachmentsDir) throws IOException {
        if (jtracHome == null || extractedAttachmentsDir == null || !extractedAttachmentsDir.exists()) {
            return;
        }

        File targetAttachDir = new File(jtracHome, "attachments");
        if (!targetAttachDir.exists()) {
            targetAttachDir.mkdirs();
        }

        File[] files = extractedAttachmentsDir.listFiles();
        if (files != null) {
            logger.info("Restoring {} physical attachment files...", files.length);
            for (File src : files) {
                if (src.isFile()) {
                    File dest = new File(targetAttachDir, src.getName());
                    Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Asynchronously trigger Lucene full-text search index rebuild.
     */
    public void triggerLuceneReindex() {
        if (jtrac != null) {
            logger.info("Triggering asynchronous Lucene full-text search reindexing...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        info.jtrac.domain.BatchInfo batchInfo = new info.jtrac.domain.BatchInfo();
                        jtrac.rebuildIndexes(batchInfo);
                        logger.info("Lucene index rebuild successfully completed.");
                    } catch (Exception e) {
                        logger.error("Failed to rebuild Lucene indexes: " + e.getMessage(), e);
                    }
                }
            }, "JTrac-Restore-Reindexer").start();
        }
    }

    /**
     * Complete one-click full system restore workflow.
     */
    public void performFullRestore(InputStream zipIn, User currentOperator) throws Exception {
        logger.info("==================================================================");
        logger.info("STARTING JTRAC FULL SYSTEM RESTORE (Operator: {})",
                currentOperator != null ? currentOperator.getLoginName() : "unknown");
        logger.info("==================================================================");

        // 1. Take safety snapshot
        createSafetySnapshot(currentOperator != null ? currentOperator.getLoginName() : "system");

        // 2. Extract & Parse ZIP bundle to temporary directory
        File tempDir = Files.createTempDirectory("jtrac-restore-").toFile();
        try {
            ZipBundleService.ZipExtractResult extractResult = zipBundleService.extractAndParseZip(zipIn, tempDir);
            SystemBackupData data = extractResult.getData();

            // 3. Apply Anti-Lockout Credential Shield
            applyAntiLockoutShield(data, currentOperator);

            // 4. Restore database tables
            restoreDatabase(data);

            // 5. Restore physical attachment files
            File extractedAttachDir = new File(tempDir, "attachments");
            restoreAttachments(extractedAttachDir);

            // 6. Trigger background Lucene index rebuild
            triggerLuceneReindex();

            logger.info("FULL SYSTEM RESTORE COMPLETED SUCCESSFULLY.");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    // Helper methods for setting nullable JDBC parameters
    private void setLongOrNull(PreparedStatement ps, int idx, Long val) throws SQLException {
        if (val != null) ps.setLong(idx, val);
        else ps.setNull(idx, Types.BIGINT);
    }

    private void setIntegerOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val != null) ps.setInt(idx, val);
        else ps.setNull(idx, Types.INTEGER);
    }

    private void setDoubleOrNull(PreparedStatement ps, int idx, Double val) throws SQLException {
        if (val != null) ps.setDouble(idx, val);
        else ps.setNull(idx, Types.DOUBLE);
    }

    private void setTimestampOrNull(PreparedStatement ps, int idx, Date val) throws SQLException {
        if (val != null) ps.setTimestamp(idx, new Timestamp(val.getTime()));
        else ps.setNull(idx, Types.TIMESTAMP);
    }
}
