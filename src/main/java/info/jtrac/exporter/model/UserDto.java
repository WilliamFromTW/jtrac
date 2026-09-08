package info.jtrac.exporter.model;

public class UserDto {
    private long id;
    private String loginName;
    private String name;
    private String email;

    public UserDto() {}

    public UserDto(long id, String loginName, String name, String email) {
        this.id = id;
        this.loginName = loginName;
        this.name = name;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getName() {
        return (name != null && !name.trim().isEmpty()) ? name : loginName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
