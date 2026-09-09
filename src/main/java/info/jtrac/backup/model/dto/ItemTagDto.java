package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class ItemTagDto implements Serializable {
    private long id;
    private long itemId;
    private long tagId;
    private Integer type;

    public ItemTagDto() {}

    public ItemTagDto(long id, long itemId, long tagId, Integer type) {
        this.id = id;
        this.itemId = itemId;
        this.tagId = tagId;
        this.type = type;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getItemId() { return itemId; }
    public void setItemId(long itemId) { this.itemId = itemId; }

    public long getTagId() { return tagId; }
    public void setTagId(long tagId) { this.tagId = tagId; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
}
