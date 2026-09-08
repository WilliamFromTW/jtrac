package info.jtrac.domain;

import java.util.HashSet;
import java.util.Set;

import org.junit.*;

import org.acegisecurity.GrantedAuthority;

public class UserTest {    
    
	@Test
    public void testGetAuthoritiesFromUserSpaceRoles() {      
        Space s1 = new Space();
        s1.setPrefixCode("SPACE-ONE");                             

        User u = new User();
        u.setLoginName("test");        
        
        u.addSpaceWithRole(s1, "ROLE_ONE-ONE");
        u.addSpaceWithRole(s1, "ROLE_ONE-TWO");
        u.addSpaceWithRole(null, "ROLE_ADMIN");
        u.setId(1);
        
        GrantedAuthority[] gas = u.getAuthorities();
        
        Set<String> set = new HashSet<String>();
        for(GrantedAuthority ga : gas) {
            set.add(ga.getAuthority());
        }        
                
        Assert.assertEquals(3, gas.length);
        
        Assert.assertTrue(set.contains("ROLE_ONE-ONE:SPACE-ONE"));
        Assert.assertTrue(set.contains("ROLE_ONE-TWO:SPACE-ONE"));
        Assert.assertTrue(set.contains("ROLE_ADMIN"));
    }
    
	@Test
    public void testCheckIfAdminForAllSpaces() {
        User u = new User();
        u.setLoginName("test");
        u.addSpaceWithRole(null, "ROLE_ADMIN");
        Assert.assertTrue(u.isSuperUser());
    }
    
}
