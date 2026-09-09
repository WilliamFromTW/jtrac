package info.jtrac.backup.model.dto;

import java.io.Serializable;

public class SpaceSequenceDto implements Serializable {
    private long id;
    private long nextSeqNum;

    public SpaceSequenceDto() {}

    public SpaceSequenceDto(long id, long nextSeqNum) {
        this.id = id;
        this.nextSeqNum = nextSeqNum;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getNextSeqNum() { return nextSeqNum; }
    public void setNextSeqNum(long nextSeqNum) { this.nextSeqNum = nextSeqNum; }
}
