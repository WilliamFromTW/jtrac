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

import info.jtrac.domain.ItemSearch;
import info.jtrac.domain.User;
import info.jtrac.exception.JtracSecurityException;
import info.jtrac.util.ItemUtils;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * item list page
 */
public class ItemListPage extends BasePage {               
        
    public ItemListPage(PageParameters params) {
        User user = getPrincipal();
        try {
            ItemSearch itemSearch = ItemUtils.getItemSearch(user, params, getJtrac());
            JtracSession.get().setItemSearch(itemSearch);
            addComponents(itemSearch);
        } catch (JtracSecurityException e) {
            logger.warn("Security exception accessing ItemListPage: " + e.getMessage());
            if (user == null || user.getId() == 0) {
                throw new RestartResponseAtInterceptPageException(LoginPage.class);
            } else {
                throw new RestartResponseAtInterceptPageException(new ErrorPage(e.getMessage()));
            }
        }
    }  
    
    public ItemListPage(ItemSearch itemSearch) {
        addComponents(itemSearch);
    }
    
    private void addComponents(ItemSearch itemSearch) {        
        add(new ItemListPanel("panel", itemSearch));
        add(new ItemRelatePanel("relate", false, itemSearch));        
    }
    
}
