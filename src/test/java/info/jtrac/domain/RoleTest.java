package info.jtrac.domain;

import info.jtrac.util.XmlUtils;

import org.junit.*;

import org.dom4j.Document;

public class RoleTest {

	@Test
    public void testConstructFromXml() {
        Document d = XmlUtils.parse("<role name='TESTER'>" +
            "<state status='1'>" +
                "<transition status='2'/>" +
                "<transition status='3'/>" +
                "<field name='cusInt01' mask='1'/>" +
                "<field name='cusInt02' mask='2'/>" +
            "</state>" +
            "<state status='2'>" +
                "<transition status='3'/>" +
                "<field name='cusInt03' mask='1'/>" +
                "<field name='cusInt04' mask='2'/>" +
            "</state></role>");
        Role role = new Role(d.getRootElement());
        Assert.assertEquals("TESTER", role.getName());
        Assert.assertEquals(2, role.getStates().size());
        State s1 = role.getStates().get(1);
        Assert.assertEquals(2, s1.getTransitions().size());
        Assert.assertTrue(s1.getTransitions().contains(2));
        Assert.assertTrue(s1.getTransitions().contains(3));
        Assert.assertEquals(2 , s1.getFields().size());
        Assert.assertEquals(Integer.valueOf(1), s1.getFields().get(Field.Name.CUS_INT_01));
        Assert.assertEquals(Integer.valueOf(2), s1.getFields().get(Field.Name.CUS_INT_02));
    }

	@Test
    public void testForReservedRoleNames() {
        Assert.assertTrue(Role.isReservedRoleKey("ROLE_ADMIN"));
        Assert.assertTrue(Role.isReservedRoleKey("ROLE_GUEST"));
        Assert.assertFalse(Role.isReservedRoleKey("ROLE_FOO"));
    }

}
