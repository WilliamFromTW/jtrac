package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class ItemUserDto implements Serializable {
    private long id;
    private long itemId;
    private long userId;
    private Integer type;

    public ItemUserDto() {}

    public ItemUserDto(long id, long itemId, long userId, Integer type) {
        this.id = id;
        this.itemId = itemId;
        this.userId = userId;
        this.type = type;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getItemId() { return itemId; }
    public void setItemId(long itemId) { this.itemId = itemId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
}
