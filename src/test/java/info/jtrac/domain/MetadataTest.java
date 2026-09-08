package info.jtrac.domain;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;

import org.junit.*;

public class MetadataTest {
    
    private Metadata getMetadata() {
        Metadata metadata = new Metadata();
        String xmlString = "<metadata><fields>" 
                + "<field name='cusInt01' label='Test Label'/>"
                + "<field name='cusInt02' label='Test Label 2'/>"
                + "</fields></metadata>";
        metadata.setXmlString(xmlString);
        return metadata;
    }    
    
	@Test
    public void testGetFieldByName() {
        Metadata m = getMetadata();
        Field f = m.getField("cusInt01");
        Assert.assertEquals("Test Label", f.getLabel());
    }
    
	@Test
    public void testGetFieldsFromXml() {
        Metadata m = getMetadata();
        Map<Field.Name, Field> fields = m.getFields();
        Assert.assertTrue(fields.size() == 2);
        Field[] fa = fields.values().toArray(new Field[0]);
        Assert.assertEquals("cusInt01",  fa[0].getName() + "");
        Assert.assertEquals("Test Label",  fa[0].getLabel());
        Assert.assertEquals("cusInt02",  fa[1].getName() + "");
        Assert.assertEquals("Test Label 2",  fa[1].getLabel());
    }
    
	@Test
    public void testMetadataInheritance() {
        Metadata m1 = getMetadata();
        Metadata m2 = new Metadata();
        String xmlString = "<metadata><fields>" 
                + "<field name='cusInt03' label='Test Label 3'/>"
                + "<field name='cusInt04' label='Test Label 4'/>"
                + "</fields></metadata>";
        m2.setXmlString(xmlString);
        m2.setParent(m1);
        Map<Field.Name, Field> fields = m2.getFields();
        Assert.assertEquals(fields.size(), 4);
        Set<Field.Name> names = m2.getUnusedFieldNames();
        Assert.assertEquals(names.contains(Field.Name.CUS_INT_01), false);
        Assert.assertEquals(names.contains(Field.Name.CUS_INT_04), false);
        Assert.assertEquals(names.size(), Field.Name.values().length - 4);        
    }
    
	@Test
    public void testInitRolesThenAddRolesAndStates() {
        Metadata m = new Metadata();
        m.initRoles();
        Assert.assertEquals("New, Open and Closed available by default", 3, m.getStateCount());
        Assert.assertEquals("DEFAULT available by default", 1, m.getRoleCount());        
        Field f = new Field(Field.Name.CUS_INT_01);
        m.add(f);
        Assert.assertEquals(1, m.getFieldCount());
        Assert.assertEquals("New", m.getStatusValue(0));
        Assert.assertEquals("Open", m.getStatusValue(1));
        Assert.assertEquals("Closed", m.getStatusValue(99));
        Assert.assertEquals("", m.getStatusValue(50));        
    }    
        
	@Test
    public void testGetEditableFields() {
        Metadata m = new Metadata();
        m.initRoles();
        Field f = new Field(Field.Name.CUS_STR_01);
        m.add(f);
        // query for editable fields across all roles
        List<Field> fields = m.getEditableFields();
        Assert.assertEquals(0, fields.size());
        // query for editable fields for DEFAULT role and when status is OPEN
        fields = m.getEditableFields("DEFAULT", State.OPEN);
        Assert.assertEquals(0, fields.size());
        // now make the field editable for given state and role
        m.switchMask(State.OPEN, "DEFAULT", "cusStr01"); // should now be editable when status is open  
        fields = m.getEditableFields();
        Assert.assertEquals(1, fields.size());        
        fields = m.getEditableFields("DEFAULT", State.OPEN);
        Assert.assertEquals(1, fields.size());
    }
    
}
