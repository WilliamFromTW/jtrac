package info.jtrac.domain;

import org.junit.Assert;
import org.junit.Test;

public class BatchInfoTest {

    @Test
    public void testInitialState() {
        BatchInfo info = new BatchInfo();
        Assert.assertFalse(info.isComplete());
        Assert.assertNull(info.getErrorMessage());
        Assert.assertEquals(0, info.getTotalSize());
        Assert.assertEquals(0, info.getCurrentPosition());
        Assert.assertEquals(0, info.getPercent());
        Assert.assertEquals("0% [0 / 0]", info.getProgressText());
    }

    @Test
    public void testProgressCalculation() {
        BatchInfo info = new BatchInfo();
        info.setTotalSize(200);
        info.setCurrentPosition(50);
        Assert.assertEquals(25, info.getPercent());
        Assert.assertEquals("25% [50 / 200]", info.getProgressText());

        info.incrementPosition();
        Assert.assertEquals(51, info.getCurrentPosition());
        Assert.assertEquals("26% [51 / 200]", info.getProgressText());
    }

    @Test
    public void testCompletionAndError() {
        BatchInfo info = new BatchInfo();
        info.setTotalSize(10);
        info.setCurrentPosition(10);
        info.setComplete(true);
        Assert.assertTrue(info.isComplete());

        info.setErrorMessage("Lucene lock timeout");
        Assert.assertEquals("Lucene lock timeout", info.getErrorMessage());
    }
}
