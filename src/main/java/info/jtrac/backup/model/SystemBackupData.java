package info.jtrac.backup.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import info.jtrac.backup.model.dto.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates all relational database entities in a cross-database generic structure.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemBackupData implements Serializable {

    private List<ConfigDto> configs = new ArrayList<ConfigDto>();
    private List<StoredSearchDto> storedSearches = new ArrayList<StoredSearchDto>();
    private List<MetadataDto> metadatas = new ArrayList<MetadataDto>();
    private List<SpaceDto> spaces = new ArrayList<SpaceDto>();
    private List<SpaceSequenceDto> spaceSequences = new ArrayList<SpaceSequenceDto>();
    private List<UserDto> users = new ArrayList<UserDto>();
    private List<UserSpaceRoleDto> userSpaceRoles = new ArrayList<UserSpaceRoleDto>();
    private List<TagDto> tags = new ArrayList<TagDto>();
    private List<ItemDto> items = new ArrayList<ItemDto>();
    private List<ItemItemDto> itemItems = new ArrayList<ItemItemDto>();
    private List<ItemUserDto> itemUsers = new ArrayList<ItemUserDto>();
    private List<ItemTagDto> itemTags = new ArrayList<ItemTagDto>();
    private List<AttachmentDto> attachments = new ArrayList<AttachmentDto>();
    private List<HistoryDto> histories = new ArrayList<HistoryDto>();

    public SystemBackupData() {}

    public List<ConfigDto> getConfigs() { return configs; }
    public void setConfigs(List<ConfigDto> configs) { this.configs = configs; }

    public List<StoredSearchDto> getStoredSearches() { return storedSearches; }
    public void setStoredSearches(List<StoredSearchDto> storedSearches) { this.storedSearches = storedSearches; }

    public List<MetadataDto> getMetadatas() { return metadatas; }
    public void setMetadatas(List<MetadataDto> metadatas) { this.metadatas = metadatas; }

    public List<SpaceDto> getSpaces() { return spaces; }
    public void setSpaces(List<SpaceDto> spaces) { this.spaces = spaces; }

    public List<SpaceSequenceDto> getSpaceSequences() { return spaceSequences; }
    public void setSpaceSequences(List<SpaceSequenceDto> spaceSequences) { this.spaceSequences = spaceSequences; }

    public List<UserDto> getUsers() { return users; }
    public void setUsers(List<UserDto> users) { this.users = users; }

    public List<UserSpaceRoleDto> getUserSpaceRoles() { return userSpaceRoles; }
    public void setUserSpaceRoles(List<UserSpaceRoleDto> userSpaceRoles) { this.userSpaceRoles = userSpaceRoles; }

    public List<TagDto> getTags() { return tags; }
    public void setTags(List<TagDto> tags) { this.tags = tags; }

    public List<ItemDto> getItems() { return items; }
    public void setItems(List<ItemDto> items) { this.items = items; }

    public List<ItemItemDto> getItemItems() { return itemItems; }
    public void setItemItems(List<ItemItemDto> itemItems) { this.itemItems = itemItems; }

    public List<ItemUserDto> getItemUsers() { return itemUsers; }
    public void setItemUsers(List<ItemUserDto> itemUsers) { this.itemUsers = itemUsers; }

    public List<ItemTagDto> getItemTags() { return itemTags; }
    public void setItemTags(List<ItemTagDto> itemTags) { this.itemTags = itemTags; }

    public List<AttachmentDto> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentDto> attachments) { this.attachments = attachments; }

    public List<HistoryDto> getHistories() { return histories; }
    public void setHistories(List<HistoryDto> histories) { this.histories = histories; }
}
