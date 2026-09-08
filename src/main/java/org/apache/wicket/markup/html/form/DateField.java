/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.IModel;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converters.AbstractConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * TextField doesn't permit the html <input type='date'> so this is a simple subclass to allow this
 * 
 * @author Ulf Dittmer
 */
public class DateField extends TextField
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id     component id
	 */
	public DateField(String id)
	{
		super(id, Date.class);
	}

	/**
	 * Construct.
	 * 
	 * @param id     see Component
	 * @param model  the model
	 */
	public DateField(String id, IModel model)
	{
		super(id, model, Date.class);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.TextField#getInputType()
	 */
	protected String getInputType()
	{
		return "date";
	}

	@Override
	public IConverter getConverter (Class clazz) {
		return new AbstractConverter() {
			private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			public Object convertToObject(String s, Locale locale) {
				if(s == null || s.trim().length() == 0) {
					return null;
				}
				try {
					return df.parse(s);
				} catch (Exception e) {
					throw new ConversionException(e);
				}
			}
			protected Class getTargetType() {
				return Date.class;
			}
			@Override
			public String convertToString(Object o, Locale locale) {
				Date d = (Date) o;
				return df.format(d);
			}
		};
	}
}
