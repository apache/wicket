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
package wicket.extensions.markup.html.form;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import wicket.MarkupContainer;
import wicket.markup.html.form.TextField;
import wicket.model.IModel;
import wicket.util.convert.IConverter;
import wicket.util.convert.converters.DateConverter;

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
 */
public class DateTextField extends TextField<Date>
{

	private static final long serialVersionUID = 1L;

	/**
	 * The date pattern of the text field
	 */
	private SimpleDateFormat dateFormat = null;

	/**
	 * The converter for the TextField
	 */
	private IConverter converter = null;

	/**
	 * Creates a new DateTextField, without a specified pattern. This is the
	 * same as calling <code>new TextField(id, Date.class)</code>
	 * 
	 * @see wicket.markup.html.form.TextField#TextField(MarkupContainer, String)
	 */
	public DateTextField(MarkupContainer parent, final String id)
	{
		super(parent, id, Date.class);
	}

	/**
	 * Creates a new DateTextField, without a specified pattern. This is the
	 * same as calling <code>new TextField(id, object, Date.class)</code>
	 * 
	 * @see wicket.markup.html.form.TextField#TextField(MarkupContainer, String, IModel)
	 */
	public DateTextField(MarkupContainer parent, final String id, IModel<Date> object)
	{
		super(parent, id, object, Date.class);
	}

	/**
	 * Creates a new DateTextField bound with a specific
	 * <code>SimpleDateFormat</code> pattern.
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            The id of the text field
	 * @param datePattern
	 *            A <code>SimpleDateFormat</code> pattern
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public DateTextField(MarkupContainer parent, final String id, String datePattern)
	{
		super(parent, id, Date.class);
		this.dateFormat = new SimpleDateFormat(datePattern);
		this.converter = new DateConverter()
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see wicket.util.convert.converters.DateConverter#getDateFormat(java.util.Locale)
			 */
			@Override
			public DateFormat getDateFormat(Locale locale)
			{
				return dateFormat;
			}
		};
	}

	/**
	 * Creates a new DateTextField bound with a specific
	 * <code>SimpleDateFormat</code> pattern.
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            The id of the text field
	 * @param object
	 *            The model
	 * @param datePattern
	 *            A <code>SimpleDateFormat</code> pattern
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public DateTextField(MarkupContainer parent, final String id, IModel<Date> object, String datePattern)
	{
		this(parent, id, datePattern);
		setModel(object);
	}

	/**
	 * Returns the default converter if created without pattern; otherwise it
	 * returns a pattern-specific converter.
	 * 
	 * @param type
	 *            The type for wich a converter must be get.
	 * 
	 * @return A pattern-specific converter
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	@Override
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
}
