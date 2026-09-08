package info.jtrac.exporter.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemDto {
    private long id;
    private long spaceId;
    private SpaceDto space;
    private long sequenceNum;
    private String summary;
    private String detail;
    private Integer status;
    private Integer severity;
    private Integer priority;
    private long loggedById;
    private UserDto loggedBy;
    private Long assignedToId;
    private UserDto assignedTo;
    private Date timeStamp;
    private Double plannedEffort;
    private final List<HistoryDto> historyList = new ArrayList<>();
    private final List<AttachmentDto> attachmentList = new ArrayList<>();

    public ItemDto() {}

    public String getRefId() {
        if (space != null && space.getPrefixCode() != null) {
            return space.getPrefixCode() + "-" + sequenceNum;
        }
        return String.valueOf(sequenceNum);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(long spaceId) {
        this.spaceId = spaceId;
    }

    public SpaceDto getSpace() {
        return space;
    }

    public void setSpace(SpaceDto space) {
        this.space = space;
    }

    public long getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(long sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
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

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Double getPlannedEffort() {
        return plannedEffort;
    }

    public void setPlannedEffort(Double plannedEffort) {
        this.plannedEffort = plannedEffort;
    }

    public List<HistoryDto> getHistoryList() {
        return historyList;
    }

    public List<AttachmentDto> getAttachmentList() {
        return attachmentList;
    }
}
