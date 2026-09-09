package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class ConfigDto implements Serializable {
    private String param;
    private String value;

    public ConfigDto() {}

    public ConfigDto(String param, String value) {
        this.param = param;
        this.value = value;
    }

    public String getParam() { return param; }
    public void setParam(String param) { this.param = param; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
