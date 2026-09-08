package info.jtrac.domain;

import java.io.Serializable;

/**
 * Created by ncrappe on 8/09/2015.
 */
public class StoredSearch implements Serializable {

    private Long id;
    private String name;
    private String query;
    private Boolean newWindow = true;

    public StoredSearch() {
    }

    public StoredSearch(Long id, String name, String query, Boolean newWindow) {
        this.id = id;
        this.query = query;
        this.name = name;
        this.newWindow = newWindow != null ? newWindow : Boolean.TRUE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean getNewWindow() {
        return newWindow != null && newWindow;
    }

    public void setNewWindow (Boolean newWindow) {
        this.newWindow = newWindow != null ? newWindow : Boolean.TRUE;
    }

    public Long getId() {
        if (id == null) {
            return Long.valueOf(0);
        }
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
