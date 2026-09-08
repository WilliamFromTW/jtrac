package info.jtrac.wicket;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleAllocatePanelTest {

    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester();
    }

    @After
    public void tearDown() {
        if (tester != null) {
            tester.destroy();
        }
    }

    @Test
    public void testRoleAllocatePanelSelectionAndChoices() {
        RoleAllocatePanel panel = new RoleAllocatePanel("panel");
        List<String> roles = Arrays.asList("DEFAULT", "ROLE_ADMIN");
        panel.setChoices(roles);

        Assert.assertNotNull(panel.getSelected());
        Assert.assertTrue(panel.getSelected().isEmpty());

        panel.setSelected(Arrays.asList("DEFAULT"));
        Assert.assertEquals(1, panel.getSelected().size());
        Assert.assertEquals("DEFAULT", panel.getSelected().get(0));

        // Test Collection setter
        Set<String> set = new HashSet<>(Arrays.asList("DEFAULT", "ROLE_ADMIN"));
        panel.setSelected(set);
        Assert.assertEquals(2, panel.getSelected().size());

        // Test that re-setting choices clears selection
        panel.setChoices(Arrays.asList("NEW_ROLE"));
        Assert.assertTrue(panel.getSelected().isEmpty());
    }
}
