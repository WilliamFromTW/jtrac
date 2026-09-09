package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class TagDto implements Serializable {
    private long id;
    private Integer type;
    private String name;
    private String description;

    public TagDto() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
