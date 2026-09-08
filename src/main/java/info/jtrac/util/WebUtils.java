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

package info.jtrac.util;

import info.jtrac.Jtrac;
import info.jtrac.wicket.JtracApplication;

import javax.servlet.http.Cookie;

import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * utilities for http, web related stuff etc
 */
public class WebUtils {

    public static String getDebugStringForCookie(Cookie cookie) {
        return  "domain: '" + cookie.getDomain() + "', " 
                + "name: '" + cookie.getName() + "', " 
                + "path: '" + cookie.getPath() + "', " 
                + "value: '" + cookie.getValue() + "', " 
                + "secure: '" + cookie.getSecure() + "', " 
                + "version: '" + cookie.getVersion() + "', " 
                + "maxAge: '" + cookie.getMaxAge() + "', " 
                + "comment: '" + cookie.getComment() + "'";
    }
 
 	public static HeaderContributor getColorChangeHeaderContributor() {
		Jtrac jtrac = JtracApplication.get().getJtrac();
		String colorGray = jtrac.loadConfig("jtrac.color.gray", "#CCCCCC");
		String colorHeader = jtrac.loadConfig("jtrac.color.header", "#E1ECFE");
		String colorLightBlue = jtrac.loadConfig("jtrac.color.lightblue", "#E1ECFE");
		String colorMediumBlue = jtrac.loadConfig("jtrac.color.mediumblue", "#C3D9FF");
		String colorDarkBlue = jtrac.loadConfig("jtrac.color.darkblue", "#0000D9");
		String colorError = jtrac.loadConfig("jtrac.color.error", "#CC2200");
		String colorErrorBg = jtrac.loadConfig("jtrac.color.errorbg", "#FFB6C1");

		return new HeaderContributor(new IHeaderContributor() {
			public void renderHead(IHeaderResponse response) {
				String js = "var r = document.querySelector(':root');\n"
						+ "r.style.setProperty('--header', '"+colorHeader+"');\n"
						+ "r.style.setProperty('--lightblue', '"+colorLightBlue+"');\n"
						+ "r.style.setProperty('--mediumblue', '"+colorMediumBlue+"');\n"
						+ "r.style.setProperty('--darkblue', '"+colorDarkBlue+"');\n"
						+ "r.style.setProperty('--error', '"+colorError+"');\n"
						+ "r.style.setProperty('--errorbg', '"+colorErrorBg+"');\n"
						+ "r.style.setProperty('--gray', '"+colorGray+"');\n";
				response.renderOnDomReadyJavascript(js);
			}
		});
    }
}
