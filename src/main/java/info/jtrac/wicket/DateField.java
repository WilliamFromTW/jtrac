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

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * HTML5 Date input field for Wicket 9
 */
public class DateField extends TextField<Date> {
    private static final long serialVersionUID = 1L;

    public DateField(String id) {
        super(id, Date.class);
    }

    public DateField(String id, IModel<Date> model) {
        super(id, model, Date.class);
    }

    @Override
    protected void onComponentTag(org.apache.wicket.markup.ComponentTag tag) {
        super.onComponentTag(tag);
        tag.put("type", "date");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        if (Date.class.isAssignableFrom(type)) {
            return (IConverter<C>) new IConverter<Date>() {
                private static final long serialVersionUID = 1L;
                @Override
                public Date convertToObject(String value, Locale locale) throws ConversionException {
                    if (value == null || value.trim().isEmpty()) {
                        return null;
                    }
                    try {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        return df.parse(value.trim());
                    } catch (Exception e) {
                        throw new ConversionException(e);
                    }
                }

                @Override
                public String convertToString(Date value, Locale locale) {
                    if (value == null) {
                        return "";
                    }
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    return df.format(value);
                }
            };
        }
        return super.getConverter(type);
    }
}
