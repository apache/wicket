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
package wicket.datetime.markup.html.form;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import wicket.datetime.util.DateConverter;
import wicket.datetime.util.PatternDateConverter;
import wicket.datetime.util.StyleDateConverter;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import wicket.model.IModel;
import wicket.util.convert.IConverter;

/**
 * A TextField that is mapped to a <code>java.util.Date</code> object and that
 * uses Joda time to parse and format values.
 * <p>
 * You should use on of the factory methods to construct the kind you want or
 * use the public constructor and pass in the converter to use.
 * </p>
 * <p>
 * This component tries to apply the time zone difference between the client and
 * server. See the
 * {@link DateConverter#getApplyTimeZoneDifference() date converter} of this
 * package for more information on that.
 * </p>
 * 
 * @see StyleDateConverter
 * @see DateTime
 * @see DateTimeFormat
 * @see DateTimeZone
 * 
 * @author eelcohillenius
 */
public class DateTextField extends TextField implements ITextFormatProvider
{

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new DateTextField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param datePattern
	 *            The pattern to use. Must be not null. See
	 *            {@link SimpleDateFormat} for available patterns.
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public static DateTextField forDatePattern(String id, IModel model, String datePattern)
	{
		return new DateTextField(id, model, new PatternDateConverter(datePattern, true));
	}

	/**
	 * Creates a new DateTextField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param datePattern
	 *            The pattern to use. Must be not null. See
	 *            {@link SimpleDateFormat} for available patterns.
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public static DateTextField forDatePattern(String id, String datePattern)
	{
		return forDatePattern(id, null, datePattern);
	}

	/**
	 * Creates a new DateTextField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param dateStyle
	 *            style to use in case no pattern is provided. Must be two
	 *            characters from the set {"S", "M", "L", "F", "-"}. Must be not
	 *            null. See {@link DateTimeFormat#forStyle(String)} for options.
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public static DateTextField forDateStyle(String id, IModel model, String dateStyle)
	{
		return new DateTextField(id, model, new StyleDateConverter(dateStyle, true));
	}

	/**
	 * Creates a new DateTextField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param dateStyle
	 *            style to use in case no pattern is provided. Must be two
	 *            characters from the set {"S", "M", "L", "F", "-"}. Must be not
	 *            null. See {@link DateTimeFormat#forStyle(String)} for options.
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public static DateTextField forDateStyle(String id, String dateStyle)
	{
		return forDateStyle(id, null, dateStyle);
	}

	/**
	 * Creates a new DateTextField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public static DateTextField forShortStyle(String id)
	{
		return forShortStyle(id, null);
	}

	/**
	 * Creates a new DateTextField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public static DateTextField forShortStyle(String id, IModel model)
	{
		return new DateTextField(id, model, new StyleDateConverter(true));
	}

	/**
	 * Creates a new DateTextField using the provided converter.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param converter
	 *            the date converter
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public static DateTextField withConverter(String id, DateConverter converter)
	{
		return withConverter(id, null, converter);
	}

	/**
	 * Creates a new DateTextField using the provided converter.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param converter
	 *            the date converter
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public static DateTextField withConverter(String id, IModel model, DateConverter converter)
	{
		return new DateTextField(id, model, converter);
	}

	/**
	 * The converter for the TextField
	 */
	private final DateConverter converter;

	/**
	 * Construct with a converter.
	 * 
	 * @param The
	 *            component id
	 * @param The
	 *            model
	 * @param converter
	 *            The converter to use
	 */
	public DateTextField(String id, IModel model, DateConverter converter)
	{
		super(id, model, Date.class);
		if (converter == null)
		{
			throw new IllegalStateException("converter may not be null");
		}
		this.converter = converter;
	}

	/**
	 * @return The specialized converter.
	 * @see wicket.Component#getConverter(java.lang.Class)
	 */
	public final IConverter getConverter(Class clazz)
	{
		return converter;
	}

	/**
	 * @see wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider#getTextFormat()
	 */
	public final String getTextFormat()
	{
		return ((DateConverter)converter).getDatePattern();
	}
}
