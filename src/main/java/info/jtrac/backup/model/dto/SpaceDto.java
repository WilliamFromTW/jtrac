package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class SpaceDto implements Serializable {
    private long id;
    private int version;
    private Integer type;
    private String prefixCode;
    private String name;
    private String description;
    private boolean guestAllowed;
    private Boolean isActive = true;
    private Long metadataId;

    public SpaceDto() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public String getPrefixCode() { return prefixCode; }
    public void setPrefixCode(String prefixCode) { this.prefixCode = prefixCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isGuestAllowed() { return guestAllowed; }
    public void setGuestAllowed(boolean guestAllowed) { this.guestAllowed = guestAllowed; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Long getMetadataId() { return metadataId; }
    public void setMetadataId(Long metadataId) { this.metadataId = metadataId; }
}
