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

import info.jtrac.domain.Config;

import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ColorField;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.NumberField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.BoundCompoundPropertyModel;

/**
 * config value edit form
 */
public class ConfigFormPage extends BasePage {
    /**
     * Constructor
     *
     * @param param
     * @param value
     */
    public ConfigFormPage(String param, String value) {
        add(new ConfigForm("form", param, value));
    }

    /**
     * wicket form
     */
    private class ConfigForm extends Form {

        private String param;
        private String value;
		private boolean isBoolean;
		private boolean isColor;
		private boolean isNumber;

        public String getValue() { return value; }
        public void setValue (String value) { this.value = value; }

        public ConfigForm (String id, final String param, final String value) {
            super(id);

            this.param = param;
            this.value = value;
			this.isBoolean = Config.isBoolean(param);
			this.isNumber = Config.isNumber(param);
			this.isColor = Config.isColor(param);

            final BoundCompoundPropertyModel model = new BoundCompoundPropertyModel(this);
            setModel(model);

            add(new Label("heading", localize("config." + param)));
            add(new Label("param", param));

			// boolean settings use a JavaScript switch instead of a text field
			if (isBoolean) {
				// use the "switch" div instead of the value field
				add(HeaderContributor.forJavaScript("resources/jquery-3.6.0.min.js"));
				add(HeaderContributor.forJavaScript("resources/jquery.enhanced-switch.js"));
				add(HeaderContributor.forCss("resources/jquery.enhanced-switch-pingpong.css"));
				add(new HeaderContributor(new IHeaderContributor() {
					public void renderHead(IHeaderResponse response) {
						String js = "$('.switch').enhancedSwitch();\n" +
							((value!=null && value.equals("true")) ? "$('.switch').enhancedSwitch('setTrue');\n" : "") +
							"$('.switch').click(function() {\n" +
							"	var selectedSwitch = $(this);\n" +
							"	selectedSwitch.enhancedSwitch('toggle');\n" +
							"	//console.log(selectedSwitch.enhancedSwitch('state'));\n" +
							"	$('#valueField').val(selectedSwitch.enhancedSwitch('state'));\n" +
							"});";
						response.renderOnDomReadyJavascript(js);
					}
				}));

				Fragment f = new Fragment("field", "booleanField", ConfigFormPage.this);
				HiddenField hiddenField = new HiddenField("value");
				f.add(model.bind(hiddenField));
				add(f);

			} else if (isNumber) {
				// only difference is type="number" instead of type="text"
				Fragment f = new Fragment("field", "numberField", ConfigFormPage.this);
				NumberField numberField = new NumberField("value");
				f.add(model.bind(numberField));
				add(f);

			} else if (isColor) {
				// only difference is type="color" instead of type="text"
				Fragment f = new Fragment("field", "colorField", ConfigFormPage.this);
				ColorField colorField = new ColorField("value");
				f.add(model.bind(colorField));
				add(f);

			} else {
				// regular text input field
				Fragment f = new Fragment("field", "textField", ConfigFormPage.this);
				TextField textField = new TextField("value");
				f.add(model.bind(textField));
				add(f);
			}

            // cancel ==========================================================
            add(new Link("cancel") {
                public void onClick() {
                    setResponsePage(new ConfigListPage(param));
                }
            });
        }

        @Override
        protected void onSubmit() {
			getJtrac().storeConfig(new Config(param, value));
            setResponsePage(new ConfigListPage(param));
        }
    }
}
