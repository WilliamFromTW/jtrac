package info.jtrac.backup.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manifest information describing a JTrac system full backup archive.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BackupManifest implements Serializable {

    private String jtracVersion;
    private String schemaVersion = "1.0";
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    private Date backupTimestamp;
    
    private String operatorLoginName;
    private String databaseDialect;
    private Map<String, Integer> tableCounts = new LinkedHashMap<String, Integer>();
    private long totalAttachmentFiles;
    private long totalAttachmentBytes;

    public BackupManifest() {
    }

    public String getJtracVersion() {
        return jtracVersion;
    }

    public void setJtracVersion(String jtracVersion) {
        this.jtracVersion = jtracVersion;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Date getBackupTimestamp() {
        return backupTimestamp;
    }

    public void setBackupTimestamp(Date backupTimestamp) {
        this.backupTimestamp = backupTimestamp;
    }

    public String getOperatorLoginName() {
        return operatorLoginName;
    }

    public void setOperatorLoginName(String operatorLoginName) {
        this.operatorLoginName = operatorLoginName;
    }

    public String getDatabaseDialect() {
        return databaseDialect;
    }

    public void setDatabaseDialect(String databaseDialect) {
        this.databaseDialect = databaseDialect;
    }

    public Map<String, Integer> getTableCounts() {
        return tableCounts;
    }

    public void setTableCounts(Map<String, Integer> tableCounts) {
        this.tableCounts = tableCounts;
    }

    public long getTotalAttachmentFiles() {
        return totalAttachmentFiles;
    }

    public void setTotalAttachmentFiles(long totalAttachmentFiles) {
        this.totalAttachmentFiles = totalAttachmentFiles;
    }

    public long getTotalAttachmentBytes() {
        return totalAttachmentBytes;
    }

    public void setTotalAttachmentBytes(long totalAttachmentBytes) {
        this.totalAttachmentBytes = totalAttachmentBytes;
    }
}
