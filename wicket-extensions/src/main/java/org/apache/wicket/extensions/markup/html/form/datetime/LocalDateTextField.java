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

import java.time.LocalDate;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.LocalDateConverter;
import org.apache.wicket.util.string.Strings;

/**
 * A TextField that is mapped to a <code>java.time.LocalDate</code> object and that uses java.time time to
 * parse and format values.
 * 
 * @see java.time.format.DateTimeFormatter
 * 
 * @author eelcohillenius
 */
public class LocalDateTextField extends TextField<LocalDate> implements ITextFormatProvider
{
	private static final long serialVersionUID = 1L;

	/**
	 * The converter for the TextField
	 */
	private final TextFormatConverter converter;
	
	/**
	 * Construct with a pattern.
	 * 
	 * @param id
	 *            the component id
	 * @param pattern
	 *            the pattern to use
	 */
	public LocalDateTextField(String id, String pattern)
	{
		this(id, null, pattern);
	}
	
	/**
	 * Construct with a pattern.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the model
	 * @param pattern
	 *            the pattern to use
	 */
	public LocalDateTextField(String id, IModel<LocalDate> model, String pattern)
	{
		this(id, model, pattern, pattern);
	}

	/**
	 * Construct with pattern for formatting and parsing.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the model
	 * @param formatPattern
	 *            the pattern to use for formatting
	 * @param parsePattern
	 *            the pattern to use for parsing
	 */
	public LocalDateTextField(String id, IModel<LocalDate> model, String formatPattern, String parsePattern)
	{
		super(id, model, LocalDate.class);

		this.converter = new TextFormatConverter() {
			private static final long serialVersionUID = 1L;

			@Override
			protected DateTimeFormatter getDateTimeFormatter()
			{
				return DateTimeFormatter.ofPattern(formatPattern);
			}

			/**
			 * Overwritten to support a custom parse pattern.
			 */
			@Override
			public LocalDate convertToObject(final String value, Locale locale)
			{
				if (Strings.isEmpty(value))
				{
					return null;
				}

				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(parsePattern).withLocale(locale);
				TemporalAccessor temporalAccessor;
				try {
					temporalAccessor = dateTimeFormatter.parse(value);
				} catch (DateTimeParseException ex) {
					throw newConversionException("Cannot parse '" + value, value, locale);
				}
				
				return createTemporal(temporalAccessor);
			}

			@Override
			public String getTextFormat(Locale locale)
			{
				return formatPattern;
			}
		};
	}

	/**
	 * Construct with a style.
	 * 
	 * @param id
	 *            the component id
	 * @param dateStyle
	 *            the style to use
	 */
	public LocalDateTextField(String id, FormatStyle dateStyle)
	{
		this(id, null, dateStyle);
	}

	/**
	 * Construct with a style.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the model
	 * @param dateStyle
	 *            the style to use
	 */
	public LocalDateTextField(String id, IModel<LocalDate> model, FormatStyle dateStyle)
	{
		super(id, model, LocalDate.class);

		this.converter = new TextFormatConverter() {
			private static final long serialVersionUID = 1L;

			@Override
			protected DateTimeFormatter getDateTimeFormatter()
			{
				return DateTimeFormatter.ofLocalizedDate(dateStyle);
			}

			@Override
			public String getTextFormat(Locale locale)
			{
				return DateTimeFormatterBuilder.getLocalizedDateTimePattern(dateStyle, null, IsoChronology.INSTANCE, locale);
			}
		};
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
		return converter.getTextFormat(getLocale());
	}

	private abstract class TextFormatConverter extends LocalDateConverter {
		private static final long serialVersionUID = 1L;

		public abstract String getTextFormat(Locale locale);
	}
}
