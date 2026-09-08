package info.jtrac.domain;

import info.jtrac.domain.Item;

import org.junit.*;

public class ItemTest {

	@Test
    public void testSetAndGetForCustomInteger() {
        Item item = new Item();
        item.setCusInt01(5);
        Assert.assertEquals(item.getCusInt01().intValue(), 5);
    }
    
}
