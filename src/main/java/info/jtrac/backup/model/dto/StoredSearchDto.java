package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class StoredSearchDto implements Serializable {
    private long id;
    private String name;
    private String query;
    private Boolean newWindow;

    public StoredSearchDto() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public Boolean getNewWindow() { return newWindow; }
    public void setNewWindow(Boolean newWindow) { this.newWindow = newWindow; }
}
