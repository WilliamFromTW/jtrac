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

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;

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

            if (isBoolean && (this.value == null || this.value.trim().isEmpty())) {
                this.value = "security.privacy.headers.enabled".equals(param) ? "true" : "false";
            }

            final BoundCompoundPropertyModel model = new BoundCompoundPropertyModel(this);
            setModel(model);

            add(new Label("heading", localize("config." + param)));
            add(new Label("param", param));

			// boolean settings use a DropDownChoice selector
			if (isBoolean) {
				Fragment f = new Fragment("field", "booleanField", ConfigFormPage.this);
				List<String> options = Arrays.asList("true", "false");
				DropDownChoice<String> choice = new DropDownChoice<String>("value", options);
				choice.setNullValid(false);
				choice.setRequired(true);
				f.add(model.bind(choice));
				add(f);

			} else if (isNumber) {
				// only difference is type="number" instead of type="text"
				Fragment f = new Fragment("field", "numberField", ConfigFormPage.this);
				TextField<String> numberField = new TextField<String>("value") {
					@Override
					protected String[] getInputTypes() {
						return new String[] {"number", "text"};
					}

					@Override
					protected void onComponentTag(org.apache.wicket.markup.ComponentTag tag) {
						super.onComponentTag(tag);
						tag.put("type", "number");
					}
				};
				f.add(model.bind(numberField));
				add(f);

			} else if (isColor) {
				// only difference is type="color" instead of type="text"
				Fragment f = new Fragment("field", "colorField", ConfigFormPage.this);
				TextField<String> colorField = new TextField<String>("value") {
					@Override
					protected String[] getInputTypes() {
						return new String[] {"color", "text"};
					}

					@Override
					protected void onComponentTag(org.apache.wicket.markup.ComponentTag tag) {
						super.onComponentTag(tag);
						tag.put("type", "color");
					}
				};
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
