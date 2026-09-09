package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class ItemItemDto implements Serializable {
    private long id;
    private long itemId;
    private long relatedItemId;
    private Integer type;

    public ItemItemDto() {}

    public ItemItemDto(long id, long itemId, long relatedItemId, Integer type) {
        this.id = id;
        this.itemId = itemId;
        this.relatedItemId = relatedItemId;
        this.type = type;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getItemId() { return itemId; }
    public void setItemId(long itemId) { this.itemId = itemId; }

    public long getRelatedItemId() { return relatedItemId; }
    public void setRelatedItemId(long relatedItemId) { this.relatedItemId = relatedItemId; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
}
