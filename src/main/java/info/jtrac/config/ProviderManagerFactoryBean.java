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

package info.jtrac.config;

import info.jtrac.Jtrac;
import info.jtrac.acegi.JtracLdapAuthenticationProvider;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Spring Security 認證管理器 FactoryBean
 * 支援有條件動態掛載 LDAP 驗證並回退至資料庫驗證
 */
public class ProviderManagerFactoryBean implements FactoryBean<ProviderManager> {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private Jtrac jtrac;    
    private String ldapUrl;
    private String activeDirectoryDomain;
    private String searchBase;
    private AuthenticationProvider authenticationProvider;   

    public void setJtrac(Jtrac jtrac) {
        this.jtrac = jtrac;
    }    
    
    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public void setActiveDirectoryDomain(String activeDirectoryDomain) {
        this.activeDirectoryDomain = activeDirectoryDomain;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }    
    
    @Override
    public ProviderManager getObject() throws Exception {        
        List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();
        if (ldapUrl != null && ldapUrl.trim().length() > 0) {
            logger.info("switching on ldap authentication provider");
            JtracLdapAuthenticationProvider ldapProvider = new JtracLdapAuthenticationProvider();
            ldapProvider.setLdapUrl(ldapUrl);            
            ldapProvider.setActiveDirectoryDomain(activeDirectoryDomain);        
            ldapProvider.setSearchBase(searchBase);
            ldapProvider.setJtrac(jtrac);
            // 手動觸發 InitializingBean 生命週期
            ldapProvider.afterPropertiesSet();
            // 優先嘗試 LDAP，失敗再回退至資料庫認證
            providers.add(ldapProvider);
        } else {
            logger.info("not using ldap authentication");
        }
        // 加入資料庫 DaoAuthenticationProvider
        if (authenticationProvider != null) {
            providers.add(authenticationProvider);
        }
        return new ProviderManager(providers);
    }

    @Override
    public Class<?> getObjectType() {
        return ProviderManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
