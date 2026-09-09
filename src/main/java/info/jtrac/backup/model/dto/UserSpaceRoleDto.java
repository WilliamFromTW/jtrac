package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class UserSpaceRoleDto implements Serializable {
    private long id;
    private long userId;
    private Long spaceId;
    private String roleKey;

    public UserSpaceRoleDto() {}

    public UserSpaceRoleDto(long id, long userId, Long spaceId, String roleKey) {
        this.id = id;
        this.userId = userId;
        this.spaceId = spaceId;
        this.roleKey = roleKey;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }

    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
}
