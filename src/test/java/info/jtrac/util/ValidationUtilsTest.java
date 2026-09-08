package info.jtrac.util;

import org.junit.*;

public class ValidationUtilsTest {
    
	@Test
    public void testValidateSpaceKey() {
        Assert.assertTrue(ValidationUtils.isValidSpaceKey("ABCD"));
        Assert.assertTrue(ValidationUtils.isValidSpaceKey("AB123CD"));
        Assert.assertFalse(ValidationUtils.isValidSpaceKey("ABCD-ABCD"));
        Assert.assertFalse(ValidationUtils.isValidSpaceKey("AB CD"));        
    }
    
	@Test
    public void testValidateRoleKey() {
        Assert.assertTrue(ValidationUtils.isValidRoleKey("ABCD"));
        Assert.assertTrue(ValidationUtils.isValidRoleKey("AB123CD"));
        Assert.assertFalse(ValidationUtils.isValidRoleKey("ABCD-ABCD"));
        Assert.assertFalse(ValidationUtils.isValidRoleKey("ABcD_ABCD"));
        Assert.assertFalse(ValidationUtils.isValidRoleKey("AB CD")); 
        Assert.assertTrue(ValidationUtils.isValidRoleKey("ABCD_EFG"));
        Assert.assertFalse(ValidationUtils.isValidRoleKey("AB1CDE2_"));
        Assert.assertFalse(ValidationUtils.isValidRoleKey("_ABCDEF"));
        Assert.assertTrue(ValidationUtils.isValidRoleKey("1ABCD3_EFG2"));        
    }    
    
	@Test
    public void testValidateLoginName() {
        Assert.assertTrue(ValidationUtils.isValidLoginName("abcd"));
        Assert.assertTrue(ValidationUtils.isValidLoginName("abcd123"));
        Assert.assertTrue(ValidationUtils.isValidLoginName("ab-cd"));
        Assert.assertTrue(ValidationUtils.isValidLoginName("ab.cd"));
        Assert.assertTrue(ValidationUtils.isValidLoginName("ab_cd"));
        Assert.assertTrue(ValidationUtils.isValidLoginName("Ab-Cd"));
        Assert.assertTrue(ValidationUtils.isValidLoginName("ab@cd"));
        Assert.assertTrue(ValidationUtils.isValidLoginName("AB\\cd"));
        Assert.assertTrue(ValidationUtils.isValidLoginName("AB\\abc@def.com"));
        Assert.assertFalse(ValidationUtils.isValidLoginName("ab%cd"));
        Assert.assertFalse(ValidationUtils.isValidLoginName("ab:cd"));
        Assert.assertFalse(ValidationUtils.isValidLoginName("ab cd"));
    }
    
	@Test
    public void testValidateStateName() {
        Assert.assertTrue(ValidationUtils.isValidStateName("Abcd"));
        Assert.assertTrue(ValidationUtils.isValidStateName("Abcd-Efgh"));
        Assert.assertTrue(ValidationUtils.isValidStateName("Abcd-Efgh-Hijk"));
        Assert.assertFalse(ValidationUtils.isValidStateName("AbcdEfgh"));
        Assert.assertFalse(ValidationUtils.isValidStateName("Abcd123"));
        Assert.assertFalse(ValidationUtils.isValidStateName("8bcd"));
        Assert.assertFalse(ValidationUtils.isValidStateName("Ab-cd"));
        Assert.assertFalse(ValidationUtils.isValidStateName("Ab cd"));
    }
    
}
