package info.jtrac.exporter.model;

import java.util.Date;

public class HistoryDto {
    private long id;
    private long itemId;
    private String comment;
    private Long attachmentId;
    private AttachmentDto attachment;
    private Date timeStamp;
    private long loggedById;
    private UserDto loggedBy;
    private Long assignedToId;
    private UserDto assignedTo;
    private Integer status;
    private Integer severity;
    private Integer priority;
    private Double actualEffort;

    public HistoryDto() {}

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public AttachmentDto getAttachment() {
        return attachment;
    }

    public void setAttachment(AttachmentDto attachment) {
        this.attachment = attachment;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getLoggedById() {
        return loggedById;
    }

    public void setLoggedById(long loggedById) {
        this.loggedById = loggedById;
    }

    public UserDto getLoggedBy() {
        return loggedBy;
    }

    public void setLoggedBy(UserDto loggedBy) {
        this.loggedBy = loggedBy;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public UserDto getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UserDto assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Double getActualEffort() {
        return actualEffort;
    }

    public void setActualEffort(Double actualEffort) {
        this.actualEffort = actualEffort;
    }
}
