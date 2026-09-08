package info.jtrac.domain;

import info.jtrac.domain.Field;
import info.jtrac.util.XmlUtils;

import org.junit.*;

import org.dom4j.Document;
import org.dom4j.Element;

public class FieldTest {
    
	@Test
    public void testSetValidName() {
        Field field = new Field();
        field.setName("cusInt01");
        Assert.assertEquals(field.getName().toString(), "cusInt01");        
    }
    
	@Test
    public void testSetInValidNameFails() {
        Field field = new Field();
        try {
            field.setName("foo");
            Assert.fail("How did we set an invalid name?");
        } catch (Exception e) {
            // expected
        }        
    }    
    
	@Test
    public void testConstructFromXml() {
        Document d = XmlUtils.parse("<field name='cusInt01' label='Test Label'/>");
        Field field = new Field(d.getRootElement());
        Assert.assertEquals("cusInt01", field.getName().toString());
        Assert.assertEquals("Test Label", field.getLabel());
        Assert.assertEquals(field.isOptional(), false);
    }
    
	@Test
    public void testConstructFromXmlWithOptionalAttribute() {
        Document d = XmlUtils.parse("<field name='cusInt01' label='Test Label' optional='true'/>");
        Field field = new Field(d.getRootElement());
        Assert.assertTrue(field.isOptional());
    }
    
	@Test
    public void testGetAsXml() {
        Field field = new Field();
        field.setName("cusInt01");
        field.setLabel("Test Label");        
        Element e = field.getAsElement();
        Assert.assertEquals("cusInt01", e.attributeValue("name"));
        Assert.assertEquals("Test Label", e.attributeValue("label"));    
    }  
    
}
