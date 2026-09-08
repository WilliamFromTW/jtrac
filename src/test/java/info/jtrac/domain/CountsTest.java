package info.jtrac.domain;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;

public class CountsTest {

	@Test
    public void testCountsLogic() {
        Counts c = new Counts(false);
        c.add(Counts.ASSIGNED_TO_ME, 1, 5);
        Assert.assertEquals(0, c.getTotal());
        Assert.assertEquals(5, c.getAssignedToMe());
    }
}
