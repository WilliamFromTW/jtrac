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


import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

import org.springframework.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * logout page.  the session invalidation code is in HeaderPanel
 */
public class LogoutPage extends WebPage {              
    
    private static final Logger logger = LoggerFactory.getLogger(LogoutPage.class);
    
    public LogoutPage(PageParameters params) {
        String locale = params.get("locale").toOptionalString();
        if(locale != null) {
            getSession().setLocale(StringUtils.parseLocaleString(locale));
        }
        setVersioned(false);
        add(new IndividualHeadPanel().setRenderBodyOnly(true));
        add(new Label("title", getLocalizer().getString("logout.title", this)));
        String jtracVersion = JtracApplication.get().getJtrac().getReleaseVersion();
        add(new Label("version", jtracVersion));
    }

    @Override
    public void renderHead(org.apache.wicket.markup.head.IHeaderResponse response) {
        super.renderHead(response);
        String cp = getRequest().getContextPath();
        String version = JtracApplication.get().getJtrac().getReleaseVersion();
        String timestamp = (JtracApplication.get().getJtrac().getReleaseTimestamp() != null) ? JtracApplication.get().getJtrac().getReleaseTimestamp().replaceAll("[^0-9]", "") : "";
        String versionParam = timestamp.isEmpty() ? version : (version + "&b=" + timestamp);
        response.render(org.apache.wicket.markup.head.CssHeaderItem.forUrl((cp != null && !cp.isEmpty() ? cp : "") + "/resources/jtrac.css?v=" + versionParam));
        response.render(org.apache.wicket.markup.head.JavaScriptHeaderItem.forUrl((cp != null && !cp.isEmpty() ? cp : "") + "/resources/theme.js?v=" + versionParam));
    }
}
