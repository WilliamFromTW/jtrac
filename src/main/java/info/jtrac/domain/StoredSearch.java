package info.jtrac.domain;

import java.io.Serializable;

/**
 * Created by ncrappe on 8/09/2015.
 */
public class StoredSearch implements Serializable {

    private Long id;
    private String name;
    private String query;
    private boolean newWindow;

    public StoredSearch() {
    }

    public StoredSearch(Long id, String name, String query, boolean newWindow) {
        this.id = id;
        this.query = query;
        this.name = name;
        this.newWindow = newWindow;
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
        return newWindow;
    }

    public void setNewWindow (boolean newWindow) {
        this.newWindow = newWindow;
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
