/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.wicket;

import info.jtrac.Jtrac;
import info.jtrac.domain.ItemSearch;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import org.apache.wicket.auth.roles.AuthenticatedWebSession;
import org.apache.wicket.auth.roles.Roles;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.springframework.util.StringUtils;
import org.apache.wicket.request.Request;
import org.apache.wicket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * custom wicket session for JTrac
 */
public class JtracSession extends AuthenticatedWebSession {   
    
    private static final Logger logger = LoggerFactory.getLogger(JtracSession.class);   
    
    private User user;
    private Space currentSpace;
    private ItemSearch itemSearch;
    
    public static JtracSession get() {        
        return (JtracSession) Session.get();
    }
    
    public JtracSession(Request request) {        
        super(request);
        logger.debug("JtracSession create: {}", getClass().getClassLoader());
        int timeOut = JtracApplication.get().getJtrac().getSessionTimeoutInMinutes();
        if (request instanceof ServletWebRequest) {
            ((ServletWebRequest) request).getContainerRequest().getSession().setMaxInactiveInterval(timeOut * 60);
        }
    }
    
    public void setUser(User user) {
        this.user = user;
        if (user.getLocale() == null) {
            // for downward compatibility, may be null in old JTrac versions
            user.setLocale(JtracApplication.get().getJtrac().getDefaultLocale());
        }
        // flip locale only if different from existing
        if (!getLocale().getDisplayName().equals(user.getLocale())) {
            setLocale(StringUtils.parseLocaleString(user.getLocale()));
        }
    }
    
    private Jtrac getJtrac() {
        return JtracApplication.get().getJtrac();
    }

    /* reload user details from database */
    public void refreshPrincipal() {
        // who knows, loginName could have changed, use id to get latest
        User temp = getJtrac().loadUser(getUser().getId());        
        // loadUserByUsername forces hibernate eager load
        // TODO make this suck less
        setUser((User) getJtrac().loadUserByUsername(temp.getLoginName())); 
    }
    
    /* only reload if passed in user is same as session user */
    public void refreshPrincipalIfSameAs(User temp) {
        if(user.getId() == temp.getId()) {
            refreshPrincipal();
        }
    }
    
    public User getUser() {
        return user;
    }
    
    @Override
    public boolean authenticate(String username, String password) {
        User u = JtracApplication.get().authenticate(username, password);
        if (u != null) {
            setUser(u);
            return true;
        }
        return false;
    }

    @Override
    public Roles getRoles() {
        Roles roles = new Roles();
        if (isSignedIn()) {
            roles.add(Roles.USER);
            if (user != null && user.isSuperUser()) {
                roles.add(Roles.ADMIN);
            }
        }
        return roles;
    }

    @Override
    public boolean isSignedIn() {
        return user != null;
    }

    public boolean isAuthenticated() {
        return isSignedIn();
    }

    public Space getCurrentSpace() {
        return currentSpace;
    }

    public void setCurrentSpace(Space currentSpace) {
        this.currentSpace = currentSpace;
    }    

    public ItemSearch getItemSearch() {
        return itemSearch;
    }

    public void setItemSearch(ItemSearch itemSearch) {
        if(itemSearch != null) {
            this.currentSpace = itemSearch.getSpace();
        }        
        this.itemSearch = itemSearch;
    }
    
}
