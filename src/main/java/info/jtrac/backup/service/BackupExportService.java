package info.jtrac.backup.service;

import info.jtrac.JtracDao;
import info.jtrac.backup.model.BackupManifest;
import info.jtrac.backup.model.SystemBackupData;
import info.jtrac.backup.model.dto.*;
import info.jtrac.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Service responsible for reading database entities, generating
 * cross-database JSON data models, manifest metadata, and ANSI SQL dump scripts.
 */
public class BackupExportService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private JtracDao dao;
    private String jtracVersion;
    private String jtracHome;
    private DataSource dataSource;

    public BackupExportService() {}

    public BackupExportService(JtracDao dao, String jtracVersion, String jtracHome) {
        this.dao = dao;
        this.jtracVersion = jtracVersion;
        this.jtracHome = jtracHome;
    }

    public BackupExportService(JtracDao dao, String jtracVersion, String jtracHome, DataSource dataSource) {
        this.dao = dao;
        this.jtracVersion = jtracVersion;
        this.jtracHome = jtracHome;
        this.dataSource = dataSource;
    }

    public void setDao(JtracDao dao) { this.dao = dao; }
    public void setJtracVersion(String jtracVersion) { this.jtracVersion = jtracVersion; }
    public void setJtracHome(String jtracHome) { this.jtracHome = jtracHome; }
    public void setDataSource(DataSource dataSource) { this.dataSource = dataSource; }
    public DataSource getDataSource() { return dataSource; }

    /**
     * Export all system entities into a portable SystemBackupData model.
     */
    public SystemBackupData exportSystemData() {
        logger.info("Beginning system backup data export...");
        SystemBackupData data = new SystemBackupData();

        // 1. Config
        List<Config> configs = dao.findAllConfig();
        for (Config c : configs) {
            data.getConfigs().add(new ConfigDto(c.getParam(), c.getValue()));
        }

        // 2. StoredSearch
        List<StoredSearch> searches = dao.findAllStoredSearch();
        for (StoredSearch ss : searches) {
            StoredSearchDto dto = new StoredSearchDto();
            dto.setId(ss.getId());
            dto.setName(ss.getName());
            dto.setQuery(ss.getQuery());
            dto.setNewWindow(ss.getNewWindow());
            data.getStoredSearches().add(dto);
        }

        // 3. Metadata
        List<Metadata> metadatas = dao.findAllMetadata();
        for (Metadata m : metadatas) {
            MetadataDto dto = new MetadataDto();
            dto.setId(m.getId());
            dto.setVersion(m.getVersion());
            dto.setType(m.getType());
            dto.setName(m.getName());
            dto.setDescription(m.getDescription());
            if (m.getParent() != null) {
                dto.setParentId(m.getParent().getId());
            }
            dto.setXmlString(m.getXmlString());
            data.getMetadatas().add(dto);
        }

        // 4. Spaces
        List<Space> spaces = dao.findAllSpaces();
        for (Space s : spaces) {
            SpaceDto dto = new SpaceDto();
            dto.setId(s.getId());
            dto.setVersion(s.getVersion());
            dto.setType(s.getType());
            dto.setPrefixCode(s.getPrefixCode());
            dto.setName(s.getName());
            dto.setDescription(s.getDescription());
            dto.setGuestAllowed(s.isGuestAllowed());
            dto.setIsActive(s.getIsActive());
            if (s.getMetadata() != null) {
                dto.setMetadataId(s.getMetadata().getId());
            }
            data.getSpaces().add(dto);
        }

        // 5. SpaceSequence
        List<SpaceSequence> spaceSequences = dao.findAllSpaceSequences();
        for (SpaceSequence ss : spaceSequences) {
            data.getSpaceSequences().add(new SpaceSequenceDto(ss.getId(), ss.getNextSeqNum()));
        }

        // 6. Users
        List<User> users = dao.findAllUsers();
        for (User u : users) {
            UserDto dto = new UserDto();
            dto.setId(u.getId());
            dto.setType(u.getType());
            if (u.getParent() != null) {
                dto.setParentId(u.getParent().getId());
            }
            dto.setLoginName(u.getLoginName());
            dto.setName(u.getName());
            dto.setPassword(u.getPassword());
            dto.setEmail(u.getEmail());
            dto.setLocale(u.getLocale());
            dto.setLocked(u.isLocked());
            dto.setPrettyDates(u.isPrettyDates());
            dto.setInfo(u.getInfo());
            if (u.getMetadata() != null) {
                dto.setMetadataId(u.getMetadata().getId());
            }
            data.getUsers().add(dto);
        }

        // 7. UserSpaceRole
        List<UserSpaceRole> usrList = dao.findAllUserSpaceRoles();
        for (UserSpaceRole usr : usrList) {
            Long spaceId = usr.getSpace() != null ? usr.getSpace().getId() : null;
            data.getUserSpaceRoles().add(new UserSpaceRoleDto(usr.getId(), usr.getUser().getId(), spaceId, usr.getRoleKey()));
        }

        // 8. Tags
        List<Tag> tags = dao.findAllTags();
        for (Tag t : tags) {
            TagDto dto = new TagDto();
            dto.setId(t.getId());
            dto.setType(t.getType());
            dto.setName(t.getName());
            dto.setDescription(t.getDescription());
            data.getTags().add(dto);
        }

        // 9. Attachments
        List<Attachment> attachments = dao.findAllAttachments();
        for (Attachment att : attachments) {
            Long prevId = att.getPrevious() != null ? att.getPrevious().getId() : null;
            data.getAttachments().add(new AttachmentDto(att.getId(), prevId, att.getFileName(), att.getFilePrefix()));
        }

        // 10. Items
        List<Item> items = dao.findAllItems();
        for (Item item : items) {
            ItemDto dto = new ItemDto();
            dto.setId(item.getId());
            dto.setVersion(item.getVersion());
            dto.setType(item.getType());
            if (item.getParent() != null) {
                dto.setParentId(item.getParent().getId());
            }
            dto.setSpaceId(item.getSpace().getId());
            dto.setSequenceNum(item.getSequenceNum());
            dto.setTimeStamp(item.getTimeStamp());
            dto.setPlannedEffort(item.getPlannedEffort());
            dto.setLoggedById(item.getLoggedBy().getId());
            if (item.getAssignedTo() != null) {
                dto.setAssignedToId(item.getAssignedTo().getId());
            }
            dto.setSummary(item.getSummary());
            dto.setDetail(item.getDetail());
            dto.setStatus(item.getStatus());
            dto.setSeverity(item.getSeverity());
            dto.setPriority(item.getPriority());

            dto.setCusDbl01(item.getCusDbl01());
            dto.setCusDbl02(item.getCusDbl02());
            dto.setCusDbl03(item.getCusDbl03());
            dto.setCusInt01(item.getCusInt01());
            dto.setCusInt02(item.getCusInt02());
            dto.setCusInt03(item.getCusInt03());
            dto.setCusInt04(item.getCusInt04());
            dto.setCusInt05(item.getCusInt05());
            dto.setCusInt06(item.getCusInt06());
            dto.setCusInt07(item.getCusInt07());
            dto.setCusInt08(item.getCusInt08());
            dto.setCusInt09(item.getCusInt09());
            dto.setCusInt10(item.getCusInt10());
            dto.setCusStr01(item.getCusStr01());
            dto.setCusStr02(item.getCusStr02());
            dto.setCusStr03(item.getCusStr03());
            dto.setCusStr04(item.getCusStr04());
            dto.setCusStr05(item.getCusStr05());
            dto.setCusTim01(item.getCusTim01());
            dto.setCusTim02(item.getCusTim02());
            dto.setCusTim03(item.getCusTim03());

            data.getItems().add(dto);

            if (item.getItemUsers() != null) {
                for (ItemUser iu : item.getItemUsers()) {
                    data.getItemUsers().add(new ItemUserDto(iu.getId(), item.getId(), iu.getUser().getId(), iu.getType()));
                }
            }

            if (item.getItemTags() != null) {
                for (ItemTag it : item.getItemTags()) {
                    data.getItemTags().add(new ItemTagDto(it.getId(), item.getId(), it.getTag().getId(), it.getType()));
                }
            }
        }

        // 11. ItemItems (Related items)
        List<ItemItem> itemItems = dao.findAllItemItems();
        for (ItemItem ii : itemItems) {
            data.getItemItems().add(new ItemItemDto(ii.getId(), ii.getItem().getId(), ii.getRelatedItem().getId(), ii.getType()));
        }

        // 12. History
        List<History> histories = dao.findAllHistories();
        for (History h : histories) {
            HistoryDto dto = new HistoryDto();
            dto.setId(h.getId());
            dto.setVersion(h.getVersion());
            dto.setType(h.getType());
            dto.setItemId(h.getParent().getId());
            dto.setActualEffort(h.getActualEffort());
            if (h.getAttachment() != null) {
                dto.setAttachmentId(h.getAttachment().getId());
            }
            dto.setComment(h.getComment());
            dto.setTimeStamp(h.getTimeStamp());
            dto.setLoggedById(h.getLoggedBy().getId());
            if (h.getAssignedTo() != null) {
                dto.setAssignedToId(h.getAssignedTo().getId());
            }
            dto.setSummary(h.getSummary());
            dto.setDetail(h.getDetail());
            dto.setStatus(h.getStatus());
            dto.setSeverity(h.getSeverity());
            dto.setPriority(h.getPriority());

            dto.setCusDbl01(h.getCusDbl01());
            dto.setCusDbl02(h.getCusDbl02());
            dto.setCusDbl03(h.getCusDbl03());
            dto.setCusInt01(h.getCusInt01());
            dto.setCusInt02(h.getCusInt02());
            dto.setCusInt03(h.getCusInt03());
            dto.setCusInt04(h.getCusInt04());
            dto.setCusInt05(h.getCusInt05());
            dto.setCusInt06(h.getCusInt06());
            dto.setCusInt07(h.getCusInt07());
            dto.setCusInt08(h.getCusInt08());
            dto.setCusInt09(h.getCusInt09());
            dto.setCusInt10(h.getCusInt10());
            dto.setCusStr01(h.getCusStr01());
            dto.setCusStr02(h.getCusStr02());
            dto.setCusStr03(h.getCusStr03());
            dto.setCusStr04(h.getCusStr04());
            dto.setCusStr05(h.getCusStr05());
            dto.setCusTim01(h.getCusTim01());
            dto.setCusTim02(h.getCusTim02());
            dto.setCusTim03(h.getCusTim03());

            data.getHistories().add(dto);
        }

        logger.info("System backup data export completed successfully. (Items: {}, Histories: {}, Spaces: {}, Users: {})",
                data.getItems().size(), data.getHistories().size(), data.getSpaces().size(), data.getUsers().size());
        return data;
    }

    /**
     * Create manifest describing the backup bundle.
     */
    public BackupManifest createManifest(SystemBackupData data, String operatorLoginName) {
        BackupManifest manifest = new BackupManifest();
        manifest.setJtracVersion(jtracVersion != null ? jtracVersion : "2.3.3");
        manifest.setBackupTimestamp(new Date());
        manifest.setOperatorLoginName(operatorLoginName);

        manifest.getTableCounts().put("configs", data.getConfigs().size());
        manifest.getTableCounts().put("storedSearches", data.getStoredSearches().size());
        manifest.getTableCounts().put("metadatas", data.getMetadatas().size());
        manifest.getTableCounts().put("spaces", data.getSpaces().size());
        manifest.getTableCounts().put("spaceSequences", data.getSpaceSequences().size());
        manifest.getTableCounts().put("users", data.getUsers().size());
        manifest.getTableCounts().put("userSpaceRoles", data.getUserSpaceRoles().size());
        manifest.getTableCounts().put("tags", data.getTags().size());
        manifest.getTableCounts().put("items", data.getItems().size());
        manifest.getTableCounts().put("itemItems", data.getItemItems().size());
        manifest.getTableCounts().put("itemUsers", data.getItemUsers().size());
        manifest.getTableCounts().put("itemTags", data.getItemTags().size());
        manifest.getTableCounts().put("attachments", data.getAttachments().size());
        manifest.getTableCounts().put("histories", data.getHistories().size());

        // Count physical attachment files
        if (jtracHome != null) {
            File attachDir = new File(jtracHome, "attachments");
            if (attachDir.exists() && attachDir.isDirectory()) {
                File[] files = attachDir.listFiles();
                if (files != null) {
                    long totalBytes = 0;
                    long fileCount = 0;
                    for (File f : files) {
                        if (f.isFile()) {
                            fileCount++;
                            totalBytes += f.length();
                        }
                    }
                    manifest.setTotalAttachmentFiles(fileCount);
                    manifest.setTotalAttachmentBytes(totalBytes);
                }
            }
        }

        return manifest;
    }

    /**
     * Detect database product name and version if DataSource is configured.
     */
    public String detectDatabaseInfo() {
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                return meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
            } catch (Exception e) {
                logger.debug("Could not inspect database metadata: {}", e.getMessage());
            }
        }
        return "Standard SQL-92 (Generic)";
    }

    /**
     * Generate complete ANSI SQL dump containing DDL, multi-database dialect comments,
     * topologically ordered INSERT statements, and sequence calibration tips.
     */
    public String generateSqlDump(SystemBackupData data) {
        return generateSqlDump(data, detectDatabaseInfo());
    }

    /**
     * Generate complete ANSI SQL dump with explicit source database info.
     */
    public String generateSqlDump(SystemBackupData data, String sourceDatabaseInfo) {
        StringBuilder sb = new StringBuilder(65536);
        String version = jtracVersion != null ? jtracVersion : "2.3.3";
        String nowStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // ---------------------------------------------------------------------
        // Header
        // ---------------------------------------------------------------------
        sb.append("-- =============================================================================\n");
        sb.append("-- JTrac Database Dump (jtrac-dump.sql)\n");
        sb.append("-- Compatible with: MySQL, PostgreSQL, HSQLDB, Microsoft SQL Server, Oracle\n");
        sb.append("-- Generated by: JTrac ").append(version).append("\n");
        sb.append("-- Timestamp: ").append(nowStr).append("\n");
        sb.append("-- Source Database: ").append(sourceDatabaseInfo != null ? sourceDatabaseInfo : "Standard SQL-92").append("\n");
        sb.append("-- Encoding: UTF-8\n");
        sb.append("-- =============================================================================\n");
        sb.append("-- Notice:\n");
        sb.append("-- 1. Section 1 provides standard ANSI SQL DDL with database dialect comments.\n");
        sb.append("-- 2. Section 2 provides INSERT statements in strict forward topological order.\n");
        sb.append("-- 3. Section 3 provides post-import Sequence and Auto-Increment calibration tips.\n");
        sb.append("-- =============================================================================\n\n");

        // ---------------------------------------------------------------------
        // Section 1: DDL Schema
        // ---------------------------------------------------------------------
        appendDdlSection(sb);

        // ---------------------------------------------------------------------
        // Section 2: Table Data (Topological Order)
        // ---------------------------------------------------------------------
        sb.append("-- =============================================================================\n");
        sb.append("-- SECTION 2: TABLE DATA (INSERT STATEMENTS IN TOPOLOGICAL ORDER)\n");
        sb.append("-- =============================================================================\n\n");

        appendConfigInserts(sb, data.getConfigs());
        appendTagInserts(sb, data.getTags());
        appendStoredSearchInserts(sb, data.getStoredSearches());
        appendMetadataInserts(sb, data.getMetadatas());
        appendSpaceSequenceInserts(sb, data.getSpaceSequences());
        appendSpaceInserts(sb, data.getSpaces());
        appendUserInserts(sb, data.getUsers());
        appendUserSpaceRoleInserts(sb, data.getUserSpaceRoles());
        appendAttachmentInserts(sb, data.getAttachments());
        appendItemInserts(sb, data.getItems());
        appendItemItemInserts(sb, data.getItemItems());
        appendItemUserInserts(sb, data.getItemUsers());
        appendItemTagInserts(sb, data.getItemTags());
        appendHistoryInserts(sb, data.getHistories());

        // ---------------------------------------------------------------------
        // Section 3: Sequence Calibration
        // ---------------------------------------------------------------------
        appendSequenceCalibration(sb, data);

        return sb.toString();
    }

    private void appendDdlSection(StringBuilder sb) {
        sb.append("-- =============================================================================\n");
        sb.append("-- SECTION 1: DDL SCHEMA (CREATE TABLE STATEMENTS)\n");
        sb.append("-- =============================================================================\n\n");

        // 1. config
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 1: config\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE config (param VARCHAR(255) NOT NULL PRIMARY KEY, value VARCHAR(255));\n");
        sb.append("-- MySQL:      CREATE TABLE config (param VARCHAR(255) NOT NULL PRIMARY KEY, value VARCHAR(255)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE config (param VARCHAR(255) NOT NULL PRIMARY KEY, value VARCHAR(255));\n");
        sb.append("CREATE TABLE config (\n");
        sb.append("    param VARCHAR(255) NOT NULL PRIMARY KEY,\n");
        sb.append("    value VARCHAR(255)\n");
        sb.append(");\n\n");

        // 2. tags
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 2: tags\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE tags (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, type INT, name VARCHAR(255) NOT NULL UNIQUE, description CLOB);\n");
        sb.append("-- MySQL:      CREATE TABLE tags (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, type INT, name VARCHAR(255) NOT NULL UNIQUE, description TEXT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE tags (id BIGSERIAL PRIMARY KEY, type INT, name VARCHAR(255) NOT NULL UNIQUE, description TEXT);\n");
        sb.append("CREATE TABLE tags (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    type INT,\n");
        sb.append("    name VARCHAR(255) NOT NULL UNIQUE,\n");
        sb.append("    description CLOB\n");
        sb.append(");\n\n");

        // 3. storedsearch
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 3: storedsearch\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE storedsearch (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, name VARCHAR(255), query CLOB, new_window BOOLEAN);\n");
        sb.append("-- MySQL:      CREATE TABLE storedsearch (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), query TEXT, new_window BOOLEAN) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE storedsearch (id BIGSERIAL PRIMARY KEY, name VARCHAR(255), query TEXT, new_window BOOLEAN);\n");
        sb.append("CREATE TABLE storedsearch (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    name VARCHAR(255),\n");
        sb.append("    query CLOB,\n");
        sb.append("    new_window BOOLEAN\n");
        sb.append(");\n\n");

        // 4. metadata
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 4: metadata\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE metadata (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, version INT, type INT, name VARCHAR(50), description CLOB, parent_id BIGINT, xml_string CLOB);\n");
        sb.append("-- MySQL:      CREATE TABLE metadata (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, version INT, type INT, name VARCHAR(50), description TEXT, parent_id BIGINT, xml_string TEXT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE metadata (id BIGSERIAL PRIMARY KEY, version INT, type INT, name VARCHAR(50), description TEXT, parent_id BIGINT, xml_string TEXT);\n");
        sb.append("CREATE TABLE metadata (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    version INT,\n");
        sb.append("    type INT,\n");
        sb.append("    name VARCHAR(50),\n");
        sb.append("    description CLOB,\n");
        sb.append("    parent_id BIGINT,\n");
        sb.append("    xml_string CLOB\n");
        sb.append(");\n\n");

        // 5. space_sequence
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 5: space_sequence\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE space_sequence (id BIGINT NOT NULL PRIMARY KEY, next_seq_num BIGINT);\n");
        sb.append("-- MySQL:      CREATE TABLE space_sequence (id BIGINT NOT NULL PRIMARY KEY, next_seq_num BIGINT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE space_sequence (id BIGINT NOT NULL PRIMARY KEY, next_seq_num BIGINT);\n");
        sb.append("CREATE TABLE space_sequence (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    next_seq_num BIGINT\n");
        sb.append(");\n\n");

        // 6. spaces
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 6: spaces\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE spaces (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, version INT, type INT, prefix_code VARCHAR(10) NOT NULL UNIQUE, name VARCHAR(50), description CLOB, guest_allowed BOOLEAN, is_active BOOLEAN, metadata_id BIGINT);\n");
        sb.append("-- MySQL:      CREATE TABLE spaces (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, version INT, type INT, prefix_code VARCHAR(10) NOT NULL UNIQUE, name VARCHAR(50), description TEXT, guest_allowed BOOLEAN, is_active BOOLEAN, metadata_id BIGINT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE spaces (id BIGSERIAL PRIMARY KEY, version INT, type INT, prefix_code VARCHAR(10) NOT NULL UNIQUE, name VARCHAR(50), description TEXT, guest_allowed BOOLEAN, is_active BOOLEAN, metadata_id BIGINT);\n");
        sb.append("CREATE TABLE spaces (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    version INT,\n");
        sb.append("    type INT,\n");
        sb.append("    prefix_code VARCHAR(10) NOT NULL UNIQUE,\n");
        sb.append("    name VARCHAR(50),\n");
        sb.append("    description CLOB,\n");
        sb.append("    guest_allowed BOOLEAN,\n");
        sb.append("    is_active BOOLEAN,\n");
        sb.append("    metadata_id BIGINT\n");
        sb.append(");\n\n");

        // 7. users
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 7: users\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE users (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, type INT, parent BIGINT, login_name VARCHAR(50) NOT NULL UNIQUE, name VARCHAR(50), password VARCHAR(255), email VARCHAR(50), locale VARCHAR(20), locked BOOLEAN, prettyDates BOOLEAN, info VARCHAR(50), metadata_id BIGINT);\n");
        sb.append("-- MySQL:      CREATE TABLE users (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, type INT, parent BIGINT, login_name VARCHAR(50) NOT NULL UNIQUE, name VARCHAR(50), password VARCHAR(255), email VARCHAR(50), locale VARCHAR(20), locked BOOLEAN, prettyDates BOOLEAN, info VARCHAR(50), metadata_id BIGINT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE users (id BIGSERIAL PRIMARY KEY, type INT, parent BIGINT, login_name VARCHAR(50) NOT NULL UNIQUE, name VARCHAR(50), password VARCHAR(255), email VARCHAR(50), locale VARCHAR(20), locked BOOLEAN, prettyDates BOOLEAN, info VARCHAR(50), metadata_id BIGINT);\n");
        sb.append("CREATE TABLE users (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    type INT,\n");
        sb.append("    parent BIGINT,\n");
        sb.append("    login_name VARCHAR(50) NOT NULL UNIQUE,\n");
        sb.append("    name VARCHAR(50),\n");
        sb.append("    password VARCHAR(255),\n");
        sb.append("    email VARCHAR(50),\n");
        sb.append("    locale VARCHAR(20),\n");
        sb.append("    locked BOOLEAN,\n");
        sb.append("    prettyDates BOOLEAN,\n");
        sb.append("    info VARCHAR(50),\n");
        sb.append("    metadata_id BIGINT\n");
        sb.append(");\n\n");

        // 8. user_space_roles
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 8: user_space_roles\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE user_space_roles (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, user_id BIGINT, space_id BIGINT, role_key VARCHAR(50) NOT NULL);\n");
        sb.append("-- MySQL:      CREATE TABLE user_space_roles (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, user_id BIGINT, space_id BIGINT, role_key VARCHAR(50) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE user_space_roles (id BIGSERIAL PRIMARY KEY, user_id BIGINT, space_id BIGINT, role_key VARCHAR(50) NOT NULL);\n");
        sb.append("CREATE TABLE user_space_roles (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    user_id BIGINT,\n");
        sb.append("    space_id BIGINT,\n");
        sb.append("    role_key VARCHAR(50) NOT NULL\n");
        sb.append(");\n\n");

        // 9. attachments
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 9: attachments\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE attachments (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, previous_id BIGINT, file_name VARCHAR(255), file_prefix BIGINT);\n");
        sb.append("-- MySQL:      CREATE TABLE attachments (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, previous_id BIGINT, file_name VARCHAR(255), file_prefix BIGINT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE attachments (id BIGSERIAL PRIMARY KEY, previous_id BIGINT, file_name VARCHAR(255), file_prefix BIGINT);\n");
        sb.append("CREATE TABLE attachments (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    previous_id BIGINT,\n");
        sb.append("    file_name VARCHAR(255),\n");
        sb.append("    file_prefix BIGINT\n");
        sb.append(");\n\n");

        // 10. items
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 10: items\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE items (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, version INT, type INT, parent_id BIGINT, space_id BIGINT NOT NULL, sequence_num INT, time_stamp TIMESTAMP, planned_effort DOUBLE, logged_by BIGINT NOT NULL, assigned_to BIGINT, summary VARCHAR(255), detail CLOB, status INT, severity INT, priority INT, cus_dbl_01 DOUBLE, cus_dbl_02 DOUBLE, cus_dbl_03 DOUBLE, cus_int_01 INT, cus_int_02 INT, cus_int_03 INT, cus_int_04 INT, cus_int_05 INT, cus_int_06 INT, cus_int_07 INT, cus_int_08 INT, cus_int_09 INT, cus_int_10 INT, cus_str_01 VARCHAR(255), cus_str_02 VARCHAR(255), cus_str_03 VARCHAR(255), cus_str_04 VARCHAR(255), cus_str_05 VARCHAR(255), cus_tim_01 TIMESTAMP, cus_tim_02 TIMESTAMP, cus_tim_03 TIMESTAMP);\n");
        sb.append("-- MySQL:      CREATE TABLE items (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, version INT, type INT, parent_id BIGINT, space_id BIGINT NOT NULL, sequence_num INT, time_stamp TIMESTAMP NULL, planned_effort DOUBLE, logged_by BIGINT NOT NULL, assigned_to BIGINT, summary VARCHAR(255), detail TEXT, status INT, severity INT, priority INT, cus_dbl_01 DOUBLE, cus_dbl_02 DOUBLE, cus_dbl_03 DOUBLE, cus_int_01 INT, cus_int_02 INT, cus_int_03 INT, cus_int_04 INT, cus_int_05 INT, cus_int_06 INT, cus_int_07 INT, cus_int_08 INT, cus_int_09 INT, cus_int_10 INT, cus_str_01 VARCHAR(255), cus_str_02 VARCHAR(255), cus_str_03 VARCHAR(255), cus_str_04 VARCHAR(255), cus_str_05 VARCHAR(255), cus_tim_01 TIMESTAMP NULL, cus_tim_02 TIMESTAMP NULL, cus_tim_03 TIMESTAMP NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE items (id BIGSERIAL PRIMARY KEY, version INT, type INT, parent_id BIGINT, space_id BIGINT NOT NULL, sequence_num INT, time_stamp TIMESTAMP, planned_effort DOUBLE PRECISION, logged_by BIGINT NOT NULL, assigned_to BIGINT, summary VARCHAR(255), detail TEXT, status INT, severity INT, priority INT, cus_dbl_01 DOUBLE PRECISION, cus_dbl_02 DOUBLE PRECISION, cus_dbl_03 DOUBLE PRECISION, cus_int_01 INT, cus_int_02 INT, cus_int_03 INT, cus_int_04 INT, cus_int_05 INT, cus_int_06 INT, cus_int_07 INT, cus_int_08 INT, cus_int_09 INT, cus_int_10 INT, cus_str_01 VARCHAR(255), cus_str_02 VARCHAR(255), cus_str_03 VARCHAR(255), cus_str_04 VARCHAR(255), cus_str_05 VARCHAR(255), cus_tim_01 TIMESTAMP, cus_tim_02 TIMESTAMP, cus_tim_03 TIMESTAMP);\n");
        sb.append("CREATE TABLE items (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    version INT,\n");
        sb.append("    type INT,\n");
        sb.append("    parent_id BIGINT,\n");
        sb.append("    space_id BIGINT NOT NULL,\n");
        sb.append("    sequence_num INT,\n");
        sb.append("    time_stamp TIMESTAMP,\n");
        sb.append("    planned_effort DOUBLE PRECISION,\n");
        sb.append("    logged_by BIGINT NOT NULL,\n");
        sb.append("    assigned_to BIGINT,\n");
        sb.append("    summary VARCHAR(255),\n");
        sb.append("    detail CLOB,\n");
        sb.append("    status INT,\n");
        sb.append("    severity INT,\n");
        sb.append("    priority INT,\n");
        sb.append("    cus_dbl_01 DOUBLE PRECISION,\n");
        sb.append("    cus_dbl_02 DOUBLE PRECISION,\n");
        sb.append("    cus_dbl_03 DOUBLE PRECISION,\n");
        sb.append("    cus_int_01 INT,\n");
        sb.append("    cus_int_02 INT,\n");
        sb.append("    cus_int_03 INT,\n");
        sb.append("    cus_int_04 INT,\n");
        sb.append("    cus_int_05 INT,\n");
        sb.append("    cus_int_06 INT,\n");
        sb.append("    cus_int_07 INT,\n");
        sb.append("    cus_int_08 INT,\n");
        sb.append("    cus_int_09 INT,\n");
        sb.append("    cus_int_10 INT,\n");
        sb.append("    cus_str_01 VARCHAR(255),\n");
        sb.append("    cus_str_02 VARCHAR(255),\n");
        sb.append("    cus_str_03 VARCHAR(255),\n");
        sb.append("    cus_str_04 VARCHAR(255),\n");
        sb.append("    cus_str_05 VARCHAR(255),\n");
        sb.append("    cus_tim_01 TIMESTAMP,\n");
        sb.append("    cus_tim_02 TIMESTAMP,\n");
        sb.append("    cus_tim_03 TIMESTAMP\n");
        sb.append(");\n\n");

        // 11. item_items
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 11: item_items\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE item_items (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, item_id BIGINT NOT NULL, related_item_id BIGINT NOT NULL, type INT);\n");
        sb.append("-- MySQL:      CREATE TABLE item_items (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, item_id BIGINT NOT NULL, related_item_id BIGINT NOT NULL, type INT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE item_items (id BIGSERIAL PRIMARY KEY, item_id BIGINT NOT NULL, related_item_id BIGINT NOT NULL, type INT);\n");
        sb.append("CREATE TABLE item_items (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    item_id BIGINT NOT NULL,\n");
        sb.append("    related_item_id BIGINT NOT NULL,\n");
        sb.append("    type INT\n");
        sb.append(");\n\n");

        // 12. item_users
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 12: item_users\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE item_users (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, item_id BIGINT NOT NULL, user_id BIGINT NOT NULL, type INT);\n");
        sb.append("-- MySQL:      CREATE TABLE item_users (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, item_id BIGINT NOT NULL, user_id BIGINT NOT NULL, type INT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE item_users (id BIGSERIAL PRIMARY KEY, item_id BIGINT NOT NULL, user_id BIGINT NOT NULL, type INT);\n");
        sb.append("CREATE TABLE item_users (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    item_id BIGINT NOT NULL,\n");
        sb.append("    user_id BIGINT NOT NULL,\n");
        sb.append("    type INT\n");
        sb.append(");\n\n");

        // 13. item_tags
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 13: item_tags\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE item_tags (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, item_id BIGINT NOT NULL, tag_id BIGINT NOT NULL, type INT);\n");
        sb.append("-- MySQL:      CREATE TABLE item_tags (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, item_id BIGINT NOT NULL, tag_id BIGINT NOT NULL, type INT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE item_tags (id BIGSERIAL PRIMARY KEY, item_id BIGINT NOT NULL, tag_id BIGINT NOT NULL, type INT);\n");
        sb.append("CREATE TABLE item_tags (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    item_id BIGINT NOT NULL,\n");
        sb.append("    tag_id BIGINT NOT NULL,\n");
        sb.append("    type INT\n");
        sb.append(");\n\n");

        // 14. history
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- Table 14: history\n");
        sb.append("-- -----------------------------------------------------------------------------\n");
        sb.append("-- [Dialect References]\n");
        sb.append("-- HSQLDB:     CREATE TABLE history (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, version INT, type INT, item_id BIGINT NOT NULL, actual_effort DOUBLE, attachment_id BIGINT, comment CLOB, time_stamp TIMESTAMP, logged_by BIGINT NOT NULL, assigned_to BIGINT, summary VARCHAR(255), detail CLOB, status INT, severity INT, priority INT, cus_dbl_01 DOUBLE, cus_dbl_02 DOUBLE, cus_dbl_03 DOUBLE, cus_int_01 INT, cus_int_02 INT, cus_int_03 INT, cus_int_04 INT, cus_int_05 INT, cus_int_06 INT, cus_int_07 INT, cus_int_08 INT, cus_int_09 INT, cus_int_10 INT, cus_str_01 VARCHAR(255), cus_str_02 VARCHAR(255), cus_str_03 VARCHAR(255), cus_str_04 VARCHAR(255), cus_str_05 VARCHAR(255), cus_tim_01 TIMESTAMP, cus_tim_02 TIMESTAMP, cus_tim_03 TIMESTAMP);\n");
        sb.append("-- MySQL:      CREATE TABLE history (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, version INT, type INT, item_id BIGINT NOT NULL, actual_effort DOUBLE, attachment_id BIGINT, comment TEXT, time_stamp TIMESTAMP NULL, logged_by BIGINT NOT NULL, assigned_to BIGINT, summary VARCHAR(255), detail TEXT, status INT, severity INT, priority INT, cus_dbl_01 DOUBLE, cus_dbl_02 DOUBLE, cus_dbl_03 DOUBLE, cus_int_01 INT, cus_int_02 INT, cus_int_03 INT, cus_int_04 INT, cus_int_05 INT, cus_int_06 INT, cus_int_07 INT, cus_int_08 INT, cus_int_09 INT, cus_int_10 INT, cus_str_01 VARCHAR(255), cus_str_02 VARCHAR(255), cus_str_03 VARCHAR(255), cus_str_04 VARCHAR(255), cus_str_05 VARCHAR(255), cus_tim_01 TIMESTAMP NULL, cus_tim_02 TIMESTAMP NULL, cus_tim_03 TIMESTAMP NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("-- PostgreSQL: CREATE TABLE history (id BIGSERIAL PRIMARY KEY, version INT, type INT, item_id BIGINT NOT NULL, actual_effort DOUBLE PRECISION, attachment_id BIGINT, comment TEXT, time_stamp TIMESTAMP, logged_by BIGINT NOT NULL, assigned_to BIGINT, summary VARCHAR(255), detail TEXT, status INT, severity INT, priority INT, cus_dbl_01 DOUBLE PRECISION, cus_dbl_02 DOUBLE PRECISION, cus_dbl_03 DOUBLE PRECISION, cus_int_01 INT, cus_int_02 INT, cus_int_03 INT, cus_int_04 INT, cus_int_05 INT, cus_int_06 INT, cus_int_07 INT, cus_int_08 INT, cus_int_09 INT, cus_int_10 INT, cus_str_01 VARCHAR(255), cus_str_02 VARCHAR(255), cus_str_03 VARCHAR(255), cus_str_04 VARCHAR(255), cus_str_05 VARCHAR(255), cus_tim_01 TIMESTAMP, cus_tim_02 TIMESTAMP, cus_tim_03 TIMESTAMP);\n");
        sb.append("CREATE TABLE history (\n");
        sb.append("    id BIGINT NOT NULL PRIMARY KEY,\n");
        sb.append("    version INT,\n");
        sb.append("    type INT,\n");
        sb.append("    item_id BIGINT NOT NULL,\n");
        sb.append("    actual_effort DOUBLE PRECISION,\n");
        sb.append("    attachment_id BIGINT,\n");
        sb.append("    comment CLOB,\n");
        sb.append("    time_stamp TIMESTAMP,\n");
        sb.append("    logged_by BIGINT NOT NULL,\n");
        sb.append("    assigned_to BIGINT,\n");
        sb.append("    summary VARCHAR(255),\n");
        sb.append("    detail CLOB,\n");
        sb.append("    status INT,\n");
        sb.append("    severity INT,\n");
        sb.append("    priority INT,\n");
        sb.append("    cus_dbl_01 DOUBLE PRECISION,\n");
        sb.append("    cus_dbl_02 DOUBLE PRECISION,\n");
        sb.append("    cus_dbl_03 DOUBLE PRECISION,\n");
        sb.append("    cus_int_01 INT,\n");
        sb.append("    cus_int_02 INT,\n");
        sb.append("    cus_int_03 INT,\n");
        sb.append("    cus_int_04 INT,\n");
        sb.append("    cus_int_05 INT,\n");
        sb.append("    cus_int_06 INT,\n");
        sb.append("    cus_int_07 INT,\n");
        sb.append("    cus_int_08 INT,\n");
        sb.append("    cus_int_09 INT,\n");
        sb.append("    cus_int_10 INT,\n");
        sb.append("    cus_str_01 VARCHAR(255),\n");
        sb.append("    cus_str_02 VARCHAR(255),\n");
        sb.append("    cus_str_03 VARCHAR(255),\n");
        sb.append("    cus_str_04 VARCHAR(255),\n");
        sb.append("    cus_str_05 VARCHAR(255),\n");
        sb.append("    cus_tim_01 TIMESTAMP,\n");
        sb.append("    cus_tim_02 TIMESTAMP,\n");
        sb.append("    cus_tim_03 TIMESTAMP\n");
        sb.append(");\n\n");
    }

    private void appendConfigInserts(StringBuilder sb, List<ConfigDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: config (").append(list.size()).append(" rows)\n");
        for (ConfigDto c : list) {
            sb.append("INSERT INTO config (param, value) VALUES (")
              .append(formatString(c.getParam())).append(", ")
              .append(formatString(c.getValue())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendTagInserts(StringBuilder sb, List<TagDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: tags (").append(list.size()).append(" rows)\n");
        for (TagDto t : list) {
            sb.append("INSERT INTO tags (id, type, name, description) VALUES (")
              .append(formatNumber(t.getId())).append(", ")
              .append(formatNumber(t.getType())).append(", ")
              .append(formatString(t.getName())).append(", ")
              .append(formatString(t.getDescription())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendStoredSearchInserts(StringBuilder sb, List<StoredSearchDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: storedsearch (").append(list.size()).append(" rows)\n");
        for (StoredSearchDto ss : list) {
            sb.append("INSERT INTO storedsearch (id, name, query, new_window) VALUES (")
              .append(formatNumber(ss.getId())).append(", ")
              .append(formatString(ss.getName())).append(", ")
              .append(formatString(ss.getQuery())).append(", ")
              .append(formatBoolean(ss.getNewWindow())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendMetadataInserts(StringBuilder sb, List<MetadataDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: metadata (").append(list.size()).append(" rows)\n");
        for (MetadataDto m : list) {
            sb.append("INSERT INTO metadata (id, version, type, name, description, parent_id, xml_string) VALUES (")
              .append(formatNumber(m.getId())).append(", ")
              .append(formatNumber(m.getVersion())).append(", ")
              .append(formatNumber(m.getType())).append(", ")
              .append(formatString(m.getName())).append(", ")
              .append(formatString(m.getDescription())).append(", ")
              .append(formatNumber(m.getParentId())).append(", ")
              .append(formatString(m.getXmlString())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendSpaceSequenceInserts(StringBuilder sb, List<SpaceSequenceDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: space_sequence (").append(list.size()).append(" rows)\n");
        for (SpaceSequenceDto ss : list) {
            sb.append("INSERT INTO space_sequence (id, next_seq_num) VALUES (")
              .append(formatNumber(ss.getId())).append(", ")
              .append(formatNumber(ss.getNextSeqNum())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendSpaceInserts(StringBuilder sb, List<SpaceDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: spaces (").append(list.size()).append(" rows)\n");
        for (SpaceDto s : list) {
            sb.append("INSERT INTO spaces (id, version, type, prefix_code, name, description, guest_allowed, is_active, metadata_id) VALUES (")
              .append(formatNumber(s.getId())).append(", ")
              .append(formatNumber(s.getVersion())).append(", ")
              .append(formatNumber(s.getType())).append(", ")
              .append(formatString(s.getPrefixCode())).append(", ")
              .append(formatString(s.getName())).append(", ")
              .append(formatString(s.getDescription())).append(", ")
              .append(formatBoolean(s.isGuestAllowed())).append(", ")
              .append(formatBoolean(s.getIsActive())).append(", ")
              .append(formatNumber(s.getMetadataId())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendUserInserts(StringBuilder sb, List<UserDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: users (").append(list.size()).append(" rows)\n");
        for (UserDto u : list) {
            sb.append("INSERT INTO users (id, type, parent, login_name, name, password, email, locale, locked, prettyDates, info, metadata_id) VALUES (")
              .append(formatNumber(u.getId())).append(", ")
              .append(formatNumber(u.getType())).append(", ")
              .append(formatNumber(u.getParentId())).append(", ")
              .append(formatString(u.getLoginName())).append(", ")
              .append(formatString(u.getName())).append(", ")
              .append(formatString(u.getPassword())).append(", ")
              .append(formatString(u.getEmail())).append(", ")
              .append(formatString(u.getLocale())).append(", ")
              .append(formatBoolean(u.isLocked())).append(", ")
              .append(formatBoolean(u.getPrettyDates())).append(", ")
              .append(formatString(u.getInfo())).append(", ")
              .append(formatNumber(u.getMetadataId())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendUserSpaceRoleInserts(StringBuilder sb, List<UserSpaceRoleDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: user_space_roles (").append(list.size()).append(" rows)\n");
        for (UserSpaceRoleDto usr : list) {
            sb.append("INSERT INTO user_space_roles (id, user_id, space_id, role_key) VALUES (")
              .append(formatNumber(usr.getId())).append(", ")
              .append(formatNumber(usr.getUserId())).append(", ")
              .append(formatNumber(usr.getSpaceId())).append(", ")
              .append(formatString(usr.getRoleKey())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendAttachmentInserts(StringBuilder sb, List<AttachmentDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: attachments (").append(list.size()).append(" rows)\n");
        for (AttachmentDto a : list) {
            sb.append("INSERT INTO attachments (id, previous_id, file_name, file_prefix) VALUES (")
              .append(formatNumber(a.getId())).append(", ")
              .append(formatNumber(a.getPreviousId())).append(", ")
              .append(formatString(a.getFileName())).append(", ")
              .append(formatNumber(a.getFilePrefix())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendItemInserts(StringBuilder sb, List<ItemDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: items (").append(list.size()).append(" rows)\n");
        for (ItemDto i : list) {
            sb.append("INSERT INTO items (id, version, type, parent_id, space_id, sequence_num, time_stamp, planned_effort, logged_by, assigned_to, summary, detail, status, severity, priority, cus_dbl_01, cus_dbl_02, cus_dbl_03, cus_int_01, cus_int_02, cus_int_03, cus_int_04, cus_int_05, cus_int_06, cus_int_07, cus_int_08, cus_int_09, cus_int_10, cus_str_01, cus_str_02, cus_str_03, cus_str_04, cus_str_05, cus_tim_01, cus_tim_02, cus_tim_03) VALUES (")
              .append(formatNumber(i.getId())).append(", ")
              .append(formatNumber(i.getVersion())).append(", ")
              .append(formatNumber(i.getType())).append(", ")
              .append(formatNumber(i.getParentId())).append(", ")
              .append(formatNumber(i.getSpaceId())).append(", ")
              .append(formatNumber(i.getSequenceNum())).append(", ")
              .append(formatTimestamp(i.getTimeStamp())).append(", ")
              .append(formatNumber(i.getPlannedEffort())).append(", ")
              .append(formatNumber(i.getLoggedById())).append(", ")
              .append(formatNumber(i.getAssignedToId())).append(", ")
              .append(formatString(i.getSummary())).append(", ")
              .append(formatString(i.getDetail())).append(", ")
              .append(formatNumber(i.getStatus())).append(", ")
              .append(formatNumber(i.getSeverity())).append(", ")
              .append(formatNumber(i.getPriority())).append(", ")
              .append(formatNumber(i.getCusDbl01())).append(", ")
              .append(formatNumber(i.getCusDbl02())).append(", ")
              .append(formatNumber(i.getCusDbl03())).append(", ")
              .append(formatNumber(i.getCusInt01())).append(", ")
              .append(formatNumber(i.getCusInt02())).append(", ")
              .append(formatNumber(i.getCusInt03())).append(", ")
              .append(formatNumber(i.getCusInt04())).append(", ")
              .append(formatNumber(i.getCusInt05())).append(", ")
              .append(formatNumber(i.getCusInt06())).append(", ")
              .append(formatNumber(i.getCusInt07())).append(", ")
              .append(formatNumber(i.getCusInt08())).append(", ")
              .append(formatNumber(i.getCusInt09())).append(", ")
              .append(formatNumber(i.getCusInt10())).append(", ")
              .append(formatString(i.getCusStr01())).append(", ")
              .append(formatString(i.getCusStr02())).append(", ")
              .append(formatString(i.getCusStr03())).append(", ")
              .append(formatString(i.getCusStr04())).append(", ")
              .append(formatString(i.getCusStr05())).append(", ")
              .append(formatTimestamp(i.getCusTim01())).append(", ")
              .append(formatTimestamp(i.getCusTim02())).append(", ")
              .append(formatTimestamp(i.getCusTim03())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendItemItemInserts(StringBuilder sb, List<ItemItemDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: item_items (").append(list.size()).append(" rows)\n");
        for (ItemItemDto ii : list) {
            sb.append("INSERT INTO item_items (id, item_id, related_item_id, type) VALUES (")
              .append(formatNumber(ii.getId())).append(", ")
              .append(formatNumber(ii.getItemId())).append(", ")
              .append(formatNumber(ii.getRelatedItemId())).append(", ")
              .append(formatNumber(ii.getType())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendItemUserInserts(StringBuilder sb, List<ItemUserDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: item_users (").append(list.size()).append(" rows)\n");
        for (ItemUserDto iu : list) {
            sb.append("INSERT INTO item_users (id, item_id, user_id, type) VALUES (")
              .append(formatNumber(iu.getId())).append(", ")
              .append(formatNumber(iu.getItemId())).append(", ")
              .append(formatNumber(iu.getUserId())).append(", ")
              .append(formatNumber(iu.getType() != null ? iu.getType() : 0)).append(");\n");
        }
        sb.append("\n");
    }

    private void appendItemTagInserts(StringBuilder sb, List<ItemTagDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: item_tags (").append(list.size()).append(" rows)\n");
        for (ItemTagDto it : list) {
            sb.append("INSERT INTO item_tags (id, item_id, tag_id, type) VALUES (")
              .append(formatNumber(it.getId())).append(", ")
              .append(formatNumber(it.getItemId())).append(", ")
              .append(formatNumber(it.getTagId())).append(", ")
              .append(formatNumber(it.getType())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendHistoryInserts(StringBuilder sb, List<HistoryDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append("-- Table: history (").append(list.size()).append(" rows)\n");
        for (HistoryDto h : list) {
            sb.append("INSERT INTO history (id, version, type, item_id, actual_effort, attachment_id, comment, time_stamp, logged_by, assigned_to, summary, detail, status, severity, priority, cus_dbl_01, cus_dbl_02, cus_dbl_03, cus_int_01, cus_int_02, cus_int_03, cus_int_04, cus_int_05, cus_int_06, cus_int_07, cus_int_08, cus_int_09, cus_int_10, cus_str_01, cus_str_02, cus_str_03, cus_str_04, cus_str_05, cus_tim_01, cus_tim_02, cus_tim_03) VALUES (")
              .append(formatNumber(h.getId())).append(", ")
              .append(formatNumber(h.getVersion())).append(", ")
              .append(formatNumber(h.getType())).append(", ")
              .append(formatNumber(h.getItemId())).append(", ")
              .append(formatNumber(h.getActualEffort())).append(", ")
              .append(formatNumber(h.getAttachmentId())).append(", ")
              .append(formatString(h.getComment())).append(", ")
              .append(formatTimestamp(h.getTimeStamp())).append(", ")
              .append(formatNumber(h.getLoggedById())).append(", ")
              .append(formatNumber(h.getAssignedToId())).append(", ")
              .append(formatString(h.getSummary())).append(", ")
              .append(formatString(h.getDetail())).append(", ")
              .append(formatNumber(h.getStatus())).append(", ")
              .append(formatNumber(h.getSeverity())).append(", ")
              .append(formatNumber(h.getPriority())).append(", ")
              .append(formatNumber(h.getCusDbl01())).append(", ")
              .append(formatNumber(h.getCusDbl02())).append(", ")
              .append(formatNumber(h.getCusDbl03())).append(", ")
              .append(formatNumber(h.getCusInt01())).append(", ")
              .append(formatNumber(h.getCusInt02())).append(", ")
              .append(formatNumber(h.getCusInt03())).append(", ")
              .append(formatNumber(h.getCusInt04())).append(", ")
              .append(formatNumber(h.getCusInt05())).append(", ")
              .append(formatNumber(h.getCusInt06())).append(", ")
              .append(formatNumber(h.getCusInt07())).append(", ")
              .append(formatNumber(h.getCusInt08())).append(", ")
              .append(formatNumber(h.getCusInt09())).append(", ")
              .append(formatNumber(h.getCusInt10())).append(", ")
              .append(formatString(h.getCusStr01())).append(", ")
              .append(formatString(h.getCusStr02())).append(", ")
              .append(formatString(h.getCusStr03())).append(", ")
              .append(formatString(h.getCusStr04())).append(", ")
              .append(formatString(h.getCusStr05())).append(", ")
              .append(formatTimestamp(h.getCusTim01())).append(", ")
              .append(formatTimestamp(h.getCusTim02())).append(", ")
              .append(formatTimestamp(h.getCusTim03())).append(");\n");
        }
        sb.append("\n");
    }

    private void appendSequenceCalibration(StringBuilder sb, SystemBackupData data) {
        sb.append("-- =============================================================================\n");
        sb.append("-- SECTION 3: SEQUENCE / AUTO-INCREMENT CALIBRATION TIPS\n");
        sb.append("-- =============================================================================\n");
        sb.append("-- Run the commands below after importing data into your target database\n");
        sb.append("-- to calibrate auto-increment / sequence identity counters:\n--\n");

        String[] tables = {
            "tags", "storedsearch", "metadata", "spaces", "users",
            "user_space_roles", "attachments", "items", "item_items",
            "item_users", "item_tags", "history"
        };

        long[] maxIds = {
            getMaxId(data.getTags()),
            getMaxId(data.getStoredSearches()),
            getMaxId(data.getMetadatas()),
            getMaxId(data.getSpaces()),
            getMaxId(data.getUsers()),
            getMaxId(data.getUserSpaceRoles()),
            getMaxId(data.getAttachments()),
            getMaxId(data.getItems()),
            getMaxId(data.getItemItems()),
            getMaxId(data.getItemUsers()),
            getMaxId(data.getItemTags()),
            getMaxId(data.getHistories())
        };

        sb.append("-- --- [1. For MySQL / MariaDB] ---\n");
        for (int i = 0; i < tables.length; i++) {
            long nextId = Math.max(maxIds[i] + 1, 1);
            sb.append("-- ALTER TABLE ").append(tables[i]).append(" AUTO_INCREMENT = ").append(nextId).append(";\n");
        }

        sb.append("\n-- --- [2. For PostgreSQL] ---\n");
        for (int i = 0; i < tables.length; i++) {
            long maxVal = Math.max(maxIds[i], 1);
            sb.append("-- SELECT setval(pg_get_serial_sequence('").append(tables[i]).append("', 'id'), ").append(maxVal).append(", true);\n");
        }

        sb.append("\n-- --- [3. For HSQLDB 2.x] ---\n");
        for (int i = 0; i < tables.length; i++) {
            long nextId = Math.max(maxIds[i] + 1, 1);
            sb.append("-- ALTER TABLE ").append(tables[i]).append(" ALTER COLUMN id RESTART WITH ").append(nextId).append(";\n");
        }
        sb.append("-- =============================================================================\n");
    }

    private <T> long getMaxId(List<T> list) {
        if (list == null || list.isEmpty()) return 0;
        long max = 0;
        for (T item : list) {
            Long id = null;
            if (item instanceof TagDto) id = ((TagDto) item).getId();
            else if (item instanceof StoredSearchDto) id = ((StoredSearchDto) item).getId();
            else if (item instanceof MetadataDto) id = ((MetadataDto) item).getId();
            else if (item instanceof SpaceDto) id = ((SpaceDto) item).getId();
            else if (item instanceof UserDto) id = ((UserDto) item).getId();
            else if (item instanceof UserSpaceRoleDto) id = ((UserSpaceRoleDto) item).getId();
            else if (item instanceof AttachmentDto) id = ((AttachmentDto) item).getId();
            else if (item instanceof ItemDto) id = ((ItemDto) item).getId();
            else if (item instanceof ItemItemDto) id = ((ItemItemDto) item).getId();
            else if (item instanceof ItemUserDto) id = ((ItemUserDto) item).getId();
            else if (item instanceof ItemTagDto) id = ((ItemTagDto) item).getId();
            else if (item instanceof HistoryDto) id = ((HistoryDto) item).getId();

            if (id != null && id > max) {
                max = id;
            }
        }
        return max;
    }

    private String formatString(String s) {
        if (s == null) return "NULL";
        return "'" + s.replace("'", "''") + "'";
    }

    private String formatNumber(Number n) {
        if (n == null) return "NULL";
        return n.toString();
    }

    private String formatBoolean(Boolean b) {
        if (b == null) return "NULL";
        return b ? "TRUE" : "FALSE";
    }

    private String formatTimestamp(Date d) {
        if (d == null) return "NULL";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "'" + sdf.format(d) + "'";
    }
}
