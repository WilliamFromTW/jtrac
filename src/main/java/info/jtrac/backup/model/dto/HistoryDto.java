package info.jtrac.backup.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

public class HistoryDto implements Serializable {
    private long id;
    private int version;
    private Integer type;
    private long itemId;
    private Double actualEffort;
    private Long attachmentId;
    private String comment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    private Date timeStamp;

    private long loggedById;
    private Long assignedToId;
    private String summary;
    private String detail;
    private Integer status;
    private Integer severity;
    private Integer priority;

    // Custom fields
    private Double cusDbl01;
    private Double cusDbl02;
    private Double cusDbl03;

    private Integer cusInt01;
    private Integer cusInt02;
    private Integer cusInt03;
    private Integer cusInt04;
    private Integer cusInt05;
    private Integer cusInt06;
    private Integer cusInt07;
    private Integer cusInt08;
    private Integer cusInt09;
    private Integer cusInt10;

    private String cusStr01;
    private String cusStr02;
    private String cusStr03;
    private String cusStr04;
    private String cusStr05;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    private Date cusTim01;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    private Date cusTim02;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    private Date cusTim03;

    public HistoryDto() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public long getItemId() { return itemId; }
    public void setItemId(long itemId) { this.itemId = itemId; }

    public Double getActualEffort() { return actualEffort; }
    public void setActualEffort(Double actualEffort) { this.actualEffort = actualEffort; }

    public Long getAttachmentId() { return attachmentId; }
    public void setAttachmentId(Long attachmentId) { this.attachmentId = attachmentId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getTimeStamp() { return timeStamp; }
    public void setTimeStamp(Date timeStamp) { this.timeStamp = timeStamp; }

    public long getLoggedById() { return loggedById; }
    public void setLoggedById(long loggedById) { this.loggedById = loggedById; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getSeverity() { return severity; }
    public void setSeverity(Integer severity) { this.severity = severity; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Double getCusDbl01() { return cusDbl01; }
    public void setCusDbl01(Double cusDbl01) { this.cusDbl01 = cusDbl01; }

    public Double getCusDbl02() { return cusDbl02; }
    public void setCusDbl02(Double cusDbl02) { this.cusDbl02 = cusDbl02; }

    public Double getCusDbl03() { return cusDbl03; }
    public void setCusDbl03(Double cusDbl03) { this.cusDbl03 = cusDbl03; }

    public Integer getCusInt01() { return cusInt01; }
    public void setCusInt01(Integer cusInt01) { this.cusInt01 = cusInt01; }

    public Integer getCusInt02() { return cusInt02; }
    public void setCusInt02(Integer cusInt02) { this.cusInt02 = cusInt02; }

    public Integer getCusInt03() { return cusInt03; }
    public void setCusInt03(Integer cusInt03) { this.cusInt03 = cusInt03; }

    public Integer getCusInt04() { return cusInt04; }
    public void setCusInt04(Integer cusInt04) { this.cusInt04 = cusInt04; }

    public Integer getCusInt05() { return cusInt05; }
    public void setCusInt05(Integer cusInt05) { this.cusInt05 = cusInt05; }

    public Integer getCusInt06() { return cusInt06; }
    public void setCusInt06(Integer cusInt06) { this.cusInt06 = cusInt06; }

    public Integer getCusInt07() { return cusInt07; }
    public void setCusInt07(Integer cusInt07) { this.cusInt07 = cusInt07; }

    public Integer getCusInt08() { return cusInt08; }
    public void setCusInt08(Integer cusInt08) { this.cusInt08 = cusInt08; }

    public Integer getCusInt09() { return cusInt09; }
    public void setCusInt09(Integer cusInt09) { this.cusInt09 = cusInt09; }

    public Integer getCusInt10() { return cusInt10; }
    public void setCusInt10(Integer cusInt10) { this.cusInt10 = cusInt10; }

    public String getCusStr01() { return cusStr01; }
    public void setCusStr01(String cusStr01) { this.cusStr01 = cusStr01; }

    public String getCusStr02() { return cusStr02; }
    public void setCusStr02(String cusStr02) { this.cusStr02 = cusStr02; }

    public String getCusStr03() { return cusStr03; }
    public void setCusStr03(String cusStr03) { this.cusStr03 = cusStr03; }

    public String getCusStr04() { return cusStr04; }
    public void setCusStr04(String cusStr04) { this.cusStr04 = cusStr04; }

    public String getCusStr05() { return cusStr05; }
    public void setCusStr05(String cusStr05) { this.cusStr05 = cusStr05; }

    public Date getCusTim01() { return cusTim01; }
    public void setCusTim01(Date cusTim01) { this.cusTim01 = cusTim01; }

    public Date getCusTim02() { return cusTim02; }
    public void setCusTim02(Date cusTim02) { this.cusTim02 = cusTim02; }

    public Date getCusTim03() { return cusTim03; }
    public void setCusTim03(Date cusTim03) { this.cusTim03 = cusTim03; }
}
