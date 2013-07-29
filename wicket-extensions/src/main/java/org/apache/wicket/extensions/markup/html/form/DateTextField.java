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
package org.apache.wicket.extensions.markup.html.form;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.DateConverter;


/**
 * A TextField that is mapped to a <code>java.util.Date</code> object.
 * 
 * If no date pattern is explicitly specified, the default <code>DateFormat.SHORT</code> pattern for
 * the current locale will be used.
 * 
 * @author Stefan Kanev
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class DateTextField extends TextField<Date> implements ITextFormatProvider
{

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_PATTERN = "MM/dd/yyyy";

	/**
	 * The date pattern of the text field
	 */
	private String datePattern = null;

	/**
	 * The converter for the TextField
	 */
	private final IConverter<Date> converter;

	/**
	 * Creates a new DateTextField, without a specified pattern. This is the same as calling
	 * <code>new TextField(id, Date.class)</code>
	 * 
	 * @param id
	 *            The id of the text field
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public DateTextField(final String id)
	{
		this(id, null, defaultDatePattern());
	}

	/**
	 * Creates a new DateTextField, without a specified pattern. This is the same as calling
	 * <code>new TextField(id, object, Date.class)</code>
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public DateTextField(final String id, final IModel<Date> model)
	{
		this(id, model, defaultDatePattern());
	}

	/**
	 * Creates a new DateTextField bound with a specific <code>SimpleDateFormat</code> pattern.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param datePattern
	 *            A <code>SimpleDateFormat</code> pattern
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public DateTextField(final String id, final String datePattern)
	{
		this(id, null, datePattern);
	}

	/**
	 * Creates a new DateTextField bound with a specific <code>SimpleDateFormat</code> pattern.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param datePattern
	 *            A <code>SimpleDateFormat</code> pattern
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public DateTextField(final String id, final IModel<Date> model, final String datePattern)
	{
		super(id, model, Date.class);
		this.datePattern = datePattern;
		converter = new DateConverter()
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.util.convert.converter.DateConverter#getDateFormat(java.util.Locale)
			 */
			@Override
			public DateFormat getDateFormat(Locale locale)
			{
				if (locale == null)
				{
					locale = Locale.getDefault();
				}
				return new SimpleDateFormat(DateTextField.this.datePattern, locale);
			}
		};
	}

	/**
	 * Returns the default converter if created without pattern; otherwise it returns a
	 * pattern-specific converter.
	 * 
	 * @param type
	 *            The type for which the convertor should work
	 * 
	 * @return A pattern-specific converter
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <C> IConverter<C> getConverter(final Class<C> type)
	{
		if (Date.class.isAssignableFrom(type))
		{
			return (IConverter<C>)converter;
		}
		else
		{
			return super.getConverter(type);
		}
	}

	/**
	 * Returns the date pattern.
	 * 
	 * @see org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider#getTextFormat()
	 */
	@Override
	public String getTextFormat()
	{
		return datePattern;
	}

	/**
	 * Try to get datePattern from user session locale. If it is not possible, it will return
	 * {@link #DEFAULT_PATTERN}
	 * 
	 * @return date pattern
	 */
	private static String defaultDatePattern()
	{
		// It is possible to retrieve from session?
		Locale locale = Session.get().getLocale();
		if (locale != null)
		{
			DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			if (format instanceof SimpleDateFormat)
			{
				return ((SimpleDateFormat)format).toPattern();
			}
		}
		return DEFAULT_PATTERN;
	}

	@Override
	protected String[] getInputTypes()
	{
		return new String[] { "date", "datetime", "datetime-local", "month", "time", "week" };
	}
}
