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

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converters.DateConverter;


/**
 * A TextField that is mapped to a <code>java.util.Date</code> object.
 * 
 * If you provide a <code>SimpleDateFormat</code> pattern, it will both parse
 * and validate the text field according to it.
 * 
 * If you don't, it is the same as creating a <code>TextField</code> with
 * <code>java.util.Date</code> as it's type (it will get the pattern from the
 * user's locale)
 * 
 * @author Stefan Kanev
 * 
 */
public class DateTextField extends TextField implements ITextFormatProvider
{

	private static final long serialVersionUID = 1L;

	/**
	 * The date pattern of the text field
	 */
	private String datePattern = null;

	/**
	 * The converter for the TextField
	 */
	private IConverter converter = null;

	/**
	 * Creates a new DateTextField, without a specified pattern. This is the
	 * same as calling <code>new TextField(id, Date.class)</code>
	 * 
	 * @param id
	 *            The id of the text field
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public DateTextField(String id)
	{
		super(id, Date.class);
	}

	/**
	 * Creates a new DateTextField, without a specified pattern. This is the
	 * same as calling <code>new TextField(id, object, Date.class)</code>
	 * 
	 * @param id
	 *            The id of the text field
	 * @param object
	 *            The model
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public DateTextField(String id, IModel object)
	{
		super(id, object, Date.class);
	}

	/**
	 * Creates a new DateTextField bound with a specific
	 * <code>SimpleDateFormat</code> pattern.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param object
	 *            The model
	 * @param datePattern
	 *            A <code>SimpleDateFormat</code> pattern
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public DateTextField(String id, IModel object, String datePattern)
	{
		this(id, datePattern);
		setModel(object);
	}

	/**
	 * Creates a new DateTextField bound with a specific
	 * <code>SimpleDateFormat</code> pattern.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param datePattern
	 *            A <code>SimpleDateFormat</code> pattern
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public DateTextField(String id, String datePattern)
	{
		super(id, Date.class);
		this.datePattern = datePattern;
		this.converter = new DateConverter()
		{
			private static final long serialVersionUID = 1L;
			
			/**
			 * @see org.apache.wicket.util.convert.converters.DateConverter#getDateFormat(java.util.Locale)
			 */
			public DateFormat getDateFormat(Locale locale)
			{
				return new SimpleDateFormat(DateTextField.this.datePattern);
			}
		};
	}

	/**
	 * Returns the default converter if created without pattern; otherwise it
	 * returns a pattern-specific converter.
	 * 
	 * @param type The type for which the convertor should work 
	 * 
	 * @return A pattern-specific converter
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public IConverter getConverter(Class type)
	{
		if (converter == null)
		{
			return super.getConverter(type);
		}
		else
		{
			return converter;
		}
	}

	/**
	 * Returns the date pattern.
	 * 
	 * @see org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider#getTextFormat()
	 */
	public String getTextFormat()
	{
		return datePattern;
	}
}
