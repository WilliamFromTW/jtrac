package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class AttachmentDto implements Serializable {
    private long id;
    private Long previousId;
    private String fileName;
    private long filePrefix;

    public AttachmentDto() {}

    public AttachmentDto(long id, Long previousId, String fileName, long filePrefix) {
        this.id = id;
        this.previousId = previousId;
        this.fileName = fileName;
        this.filePrefix = filePrefix;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getPreviousId() { return previousId; }
    public void setPreviousId(Long previousId) { this.previousId = previousId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFilePrefix() { return filePrefix; }
    public void setFilePrefix(long filePrefix) { this.filePrefix = filePrefix; }
}
