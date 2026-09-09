package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class UserDto implements Serializable {
    private long id;
    private Integer type;
    private Long parentId;
    private String loginName;
    private String name;
    private String password;
    private String email;
    private String locale;
    private boolean locked;
    private Boolean prettyDates = true;
    private String info;
    private Long metadataId;

    public UserDto() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public Boolean getPrettyDates() { return prettyDates; }
    public void setPrettyDates(Boolean prettyDates) { this.prettyDates = prettyDates; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public Long getMetadataId() { return metadataId; }
    public void setMetadataId(Long metadataId) { this.metadataId = metadataId; }
}
