package info.jtrac.exporter.model;

import java.util.ArrayList;
import java.util.List;

public class SpaceDto {
    private long id;
    private String prefixCode;
    private String name;
    private String description;
    private Long metadataId;
    private final List<ItemDto> items = new ArrayList<>();

    public SpaceDto() {}

    public SpaceDto(long id, String prefixCode, String name, String description, Long metadataId) {
        this.id = id;
        this.prefixCode = prefixCode;
        this.name = name;
        this.description = description;
        this.metadataId = metadataId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPrefixCode() {
        return prefixCode;
    }

    public void setPrefixCode(String prefixCode) {
        this.prefixCode = prefixCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(Long metadataId) {
        this.metadataId = metadataId;
    }

    public List<ItemDto> getItems() {
        return items;
    }
}
