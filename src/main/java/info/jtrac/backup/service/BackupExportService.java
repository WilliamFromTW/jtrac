package info.jtrac.backup.service;

import info.jtrac.JtracDao;
import info.jtrac.backup.model.BackupManifest;
import info.jtrac.backup.model.SystemBackupData;
import info.jtrac.backup.model.dto.*;
import info.jtrac.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Service responsible for reading database entities and constructing
 * cross-database JSON data models and manifest metadata.
 */
public class BackupExportService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private JtracDao dao;
    private String jtracVersion;
    private String jtracHome;

    public BackupExportService() {}

    public BackupExportService(JtracDao dao, String jtracVersion, String jtracHome) {
        this.dao = dao;
        this.jtracVersion = jtracVersion;
        this.jtracHome = jtracHome;
    }

    public void setDao(JtracDao dao) { this.dao = dao; }
    public void setJtracVersion(String jtracVersion) { this.jtracVersion = jtracVersion; }
    public void setJtracHome(String jtracHome) { this.jtracHome = jtracHome; }

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
}
