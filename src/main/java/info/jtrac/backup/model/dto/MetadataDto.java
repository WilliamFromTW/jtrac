package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class MetadataDto implements Serializable {
    private long id;
    private int version;
    private Integer type;
    private String name;
    private String description;
    private Long parentId;
    private String xmlString;

    public MetadataDto() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getXmlString() { return xmlString; }
    public void setXmlString(String xmlString) { this.xmlString = xmlString; }
}
