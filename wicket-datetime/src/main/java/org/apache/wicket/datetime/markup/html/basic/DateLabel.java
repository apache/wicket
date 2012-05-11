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
package org.apache.wicket.datetime.markup.html.basic;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.datetime.DateConverter;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;


/**
 * A label that is mapped to a <code>java.util.Date</code> object and that uses Joda time to format
 * values.
 * <p>
 * You can provide a date pattern in two of the constructors. When not provided,
 * {@link DateTimeFormat#shortDate()} will be used.
 * </p>
 * <p>
 * A special option is applyTimeZoneDifference which is an option that says whether to correct for
 * the difference between the client's time zone and server's time zone. This is true by default.
 * </p>
 * 
 * @see DateTime
 * @see DateTimeFormat
 * @see DateTimeZone
 * 
 * @author eelcohillenius
 */
public class DateLabel extends Label
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new DateLabel defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param datePattern
	 *            The pattern to use. Must be not null. See {@link SimpleDateFormat} for available
	 *            patterns.
	 * @return new instance
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public static DateLabel forDatePattern(String id, IModel<Date> model, String datePattern)
	{
		return new DateLabel(id, model, new PatternDateConverter(datePattern, true));
	}

	/**
	 * Creates a new DateLabel defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param datePattern
	 *            The pattern to use. Must be not null. See {@link SimpleDateFormat} for available
	 *            patterns.
	 * @return new instance
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public static DateLabel forDatePattern(String id, String datePattern)
	{
		return forDatePattern(id, null, datePattern);
	}

	/**
	 * Creates a new DateLabel defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param dateStyle
	 *            style to use in case no pattern is provided. Must be two characters from the set
	 *            {"S", "M", "L", "F", "-"}. Must be not null. See
	 *            {@link DateTimeFormat#forStyle(String)} for options.
	 * @return new instance
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public static DateLabel forDateStyle(String id, IModel<Date> model, String dateStyle)
	{
		return new DateLabel(id, model, new StyleDateConverter(dateStyle, true));
	}

	/**
	 * Creates a new DateLabel defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param dateStyle
	 *            style to use in case no pattern is provided. Must be two characters from the set
	 *            {"S", "M", "L", "F", "-"}. Must be not null. See
	 *            {@link DateTimeFormat#forStyle(String)} for options.
	 * @return new instance
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public static DateLabel forDateStyle(String id, String dateStyle)
	{
		return forDateStyle(id, null, dateStyle);
	}

	/**
	 * Creates a new DateLabel defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @return new instance
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public static DateLabel forShortStyle(String id)
	{
		return forShortStyle(id, null);
	}

	/**
	 * Creates a new DateLabel defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @return new instance
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public static DateLabel forShortStyle(String id, IModel<Date> model)
	{
		return new DateLabel(id, model, new StyleDateConverter(true));
	}

	/**
	 * Creates a new DateLabel using the provided converter.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param converter
	 *            the date converter
	 * @return new instance
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public static DateLabel withConverter(String id, DateConverter converter)
	{
		return withConverter(id, null, converter);
	}

	/**
	 * Creates a new DateLabel using the provided converter.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param converter
	 *            the date converter
	 * @return new instance
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField
	 */
	public static DateLabel withConverter(String id, IModel<Date> model, DateConverter converter)
	{
		return new DateLabel(id, model, converter);
	}

	/** optionally prepend to label. */
	private String after;

	/** optionally append to label. */
	private String before;

	/**
	 * The converter for the Label
	 */
	private final DateConverter converter;

	/**
	 * Construct with a converter.
	 * 
	 * @param id
	 *            The component id
	 * @param converter
	 *            The converter to use
	 */
	public DateLabel(String id, DateConverter converter)
	{
		this(id, null, converter);
	}

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
	public DateLabel(String id, IModel<Date> model, DateConverter converter)
	{
		super(id, model);
		if (converter == null)
		{
			throw new IllegalStateException("converter may not be null");
		}
		this.converter = converter;
	}

	/**
	 * @return after append to label or null
	 */
	public String getAfter()
	{
		return after;
	}

	/**
	 * @return before prepend to label or null
	 */
	public String getBefore()
	{
		return before;
	}

	/**
	 * Returns the specialized converter.
	 */
	@Override
	public <C> IConverter<C> getConverter(Class<C> clazz)
	{
		if (Date.class.isAssignableFrom(clazz))
		{
			@SuppressWarnings("unchecked")
			IConverter<C> result = (IConverter<C>)converter;
			return result;
		}
		else
		{
			return super.getConverter(clazz);
		}
	}

	/**
	 * @param after
	 *            append to label
	 */
	public void setAfter(String after)
	{
		this.after = after;
	}

	/**
	 * @param before
	 *            prepend to label
	 */
	public void setBefore(String before)
	{
		this.before = before;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		String s = getDefaultModelObjectAsString();
		if (before != null)
		{
			s = before + s;
		}
		if (after != null)
		{
			s = s + after;
		}
		replaceComponentTagBody(markupStream, openTag, s);
	}
}
