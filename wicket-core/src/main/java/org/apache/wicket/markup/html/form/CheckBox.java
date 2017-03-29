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

import java.util.Locale;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

/**
 * HTML checkbox input component.
 * <p>
 * Java:
 * 
 * <pre>
 * form.add(new CheckBox(&quot;bool&quot;));
 * </pre>
 * 
 * HTML:
 * 
 * <pre>
 *  &lt;input type=&quot;checkbox&quot; wicket:id=&quot;bool&quot; /&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * A CheckBox always has a valid therefore values from methods
 * {@link FormComponent#setRequired(boolean)} and {@link FormComponent#isRequired()} are not taken
 * into account.
 * </p>
 * 
 * @author Jonathan Locke
 */
public class CheckBox extends FormComponent<Boolean>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public CheckBox(final String id)
	{
		this(id, null);
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public CheckBox(final String id, IModel<Boolean> model)
	{
		super(id, model);
		setType(Boolean.class);
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "checkbox");

		final String value = getValue();
		final IConverter<Boolean> converter = getConverter(Boolean.class);
		final Boolean checked = converter.convertToObject(value, getLocale());

		if (Boolean.TRUE.equals(checked))
		{
			tag.put("checked", "checked");
		}
		else
		{
			// In case the attribute was added at design time
			tag.remove("checked");
		}

		// remove value attribute, because it overrides the browser's submitted value, eg a [input
		// type="checkbox" value=""] will always submit as false
		tag.remove("value");

		super.onComponentTag(tag);
	}

	@Override
	protected IConverter<?> createConverter(Class<?> type)
	{
		if (Boolean.class.equals(type))
		{
			return CheckBoxConverter.INSTANCE;
		}
		return null;
	}

	/**
	 * Converter specific to the check box
	 * 
	 * @author igor.vaynberg
	 */
	private static class CheckBoxConverter implements IConverter<Boolean>
	{
		private static final long serialVersionUID = 1L;

		private static final IConverter<Boolean> INSTANCE = new CheckBoxConverter();

		/**
		 * Constructor
		 */
		private CheckBoxConverter()
		{

		}

		/**
		 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,
		 *      java.util.Locale)
		 */
		@Override
		public Boolean convertToObject(String value, Locale locale)
		{
			if ("on".equals(value) || "true".equals(value))
			{
				return Boolean.TRUE;
			}
			else
			{
				return Boolean.FALSE;
			}
		}

		/**
		 * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object,
		 *      java.util.Locale)
		 */
		@Override
		public String convertToString(Boolean value, Locale locale)
		{
			return value.toString();
		}
	}

	@Override
	public boolean checkRequired()
	{
		// a checkbox always has a value so this check always passes
		return true;
	}

}
