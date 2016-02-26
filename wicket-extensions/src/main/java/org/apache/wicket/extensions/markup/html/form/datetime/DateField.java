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
package org.apache.wicket.extensions.markup.html.form.datetime;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.FormatStyle;

import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Args;

/**
 * A TextField that is mapped to a <code>java.time.LocalDate</code> object and that uses java.time time to
 * parse and format values.
 * <p>
 * You should use on of the factory methods to construct the kind you want or use the public
 * constructor and pass in the converter to use.
 * </p>
 * <p>
 * This component tries to apply the time zone difference between the client and server. See the
 * {@link ZonedDateTimeConverter#getApplyTimeZoneDifference() date converter} of this package for more
 * information on that.
 * </p>
 * 
 * @see StyleZonedDateTimeConverter
 * @see java.time.ZonedDateTime
 * @see java.time.format.DateTimeFormatter
 * @see java.time.ZoneId
 * 
 * @author eelcohillenius
 */
public class DateField extends TextField<LocalDate> implements ITextFormatProvider
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new DateField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param datePattern
	 *            The pattern to use. Must be not null. See {@link SimpleDateFormat} for available
	 *            patterns.
	 * @return DateField
	 */
	public static DateField forDatePattern(String id, IModel<LocalDate> model, String datePattern)
	{
		return new DateField(id, model, new PatternDateConverter(datePattern));
	}

	/**
	 * Creates a new DateField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param datePattern
	 *            The pattern to use. Must be not null. See {@link SimpleDateFormat} for available
	 *            patterns.
	 * @return DateField
	 */
	public static DateField forDatePattern(String id, String datePattern)
	{
		return forDatePattern(id, null, datePattern);
	}

	/**
	 * Creates a new DateField using the provided date style.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param dateStyle
	 *            Date style to use. The first character is the date style, and the second character
	 *            is the time style. Specify a character of 'S' for short style, 'M' for medium, 'L'
	 *            for long, and 'F' for full. A date or time may be ommitted by specifying a style
	 *            character '-'. See {@link org.joda.time.DateTimeFormat#forStyle(String)}.
	 * @return DateField
	 */
	public static DateField forDateStyle(String id, IModel<LocalDate> model, String dateStyle)
	{
		return new DateField(id, model, new StyleDateConverter(dateStyle));
	}

	/**
	 * Creates a new DateField using the provided date style.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param dateStyle
	 *            Date style to use. The first character is the date style, and the second character
	 *            is the time style. Specify a character of 'S' for short style, 'M' for medium, 'L'
	 *            for long, and 'F' for full. A date or time may be ommitted by specifying a style
	 *            character '-'. See {@link org.joda.time.DateTimeFormat#forStyle(String)}.
	 * @return DateField
	 */
	public static DateField forDateStyle(String id, String dateStyle)
	{
		return forDateStyle(id, null, dateStyle);
	}

	/**
	 * Creates a new DateField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @return DateField
	 */
	public static DateField forShortStyle(String id)
	{
		return forShortStyle(id, null);
	}

	/**
	 * Creates a new DateField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @return DateField
	 */
	public static DateField forShortStyle(String id, IModel<LocalDate> model)
	{
		return new DateField(id, model, new StyleDateConverter());
	}

	/**
	 * Creates a new DateField using the provided converter.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param converter
	 *            the date converter
	 * @return DateField
	 */
	public static DateField withConverter(String id, LocalDateConverter converter)
	{
		return withConverter(id, null, converter);
	}

	/**
	 * Creates a new DateField using the provided converter.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param converter
	 *            the date converter
	 * @return DateField
	 */
	public static DateField withConverter(String id, IModel<LocalDate> model, LocalDateConverter converter)
	{
		return new DateField(id, model, converter);
	}

	/**
	 * The converter for the TextField
	 */
	private final LocalDateConverter converter;

	/**
	 * Construct with a converter.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param converter
	 *            The converter to use
	 */
	public DateField(String id, IModel<LocalDate> model, LocalDateConverter converter)
	{
		super(id, model, LocalDate.class);

		Args.notNull(converter, "converter");
		this.converter = converter;
	}

	/**
	 * Construct with a converter, and a null model.
	 * 
	 * @param id
	 *            The component id
	 * @param converter
	 *            The converter to use
	 */
	public DateField(String id, LocalDateConverter converter)
	{
		this(id, null, converter);
	}

	/**
	 * @return The specialized converter.
	 * @see org.apache.wicket.Component#createConverter(java.lang.Class)
	 */
	@Override
	protected IConverter<?> createConverter(Class<?> clazz)
	{
		if (LocalDate.class.isAssignableFrom(clazz))
		{
			return converter;
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider#getTextFormat()
	 */
	@Override
	public final String getTextFormat()
	{
		return converter.getPattern(getLocale());
	}

	public static FormatStyle parseFormatStyle(char style)
	{
		switch (style)
		{
			case 'S':
				return FormatStyle.SHORT;
			case 'M':
				return FormatStyle.MEDIUM;
			case 'L':
				return FormatStyle.LONG;
			case 'F':
				return FormatStyle.FULL;
			default:
				return null;
		}
	}
}
