package info.jtrac.exporter.model;

public class AttachmentDto {
    private long id;
    private long itemId;
    private String fileName;
    private long filePrefix;
    private Long spaceId;

    public AttachmentDto() {}

    public AttachmentDto(long id, long itemId, String fileName, long filePrefix) {
        this.id = id;
        this.itemId = itemId;
        this.fileName = fileName;
        this.filePrefix = filePrefix;
    }

    public AttachmentDto(long id, long itemId, String fileName, long filePrefix, Long spaceId) {
        this.id = id;
        this.itemId = itemId;
        this.fileName = fileName;
        this.filePrefix = filePrefix;
        this.spaceId = spaceId;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFilePrefix() {
        return filePrefix;
    }

    public void setFilePrefix(long filePrefix) {
        this.filePrefix = filePrefix;
    }

    public String getPhysicalFileName() {
        return filePrefix + "_" + fileName;
    }

    public boolean isImage() {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp")
                || lower.endsWith(".svg");
    }
}
