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

import java.time.LocalDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.LocalDateTimeConverter;

/**
 * A TextField that is mapped to a <code>java.time.LocalDateTime</code> object and that uses java.time time to
 * parse and format values.
 * 
 * @see java.time.format.DateTimeFormatter
 * 
 * @author eelcohillenius
 */
public class LocalDateTimeTextField extends TextField<LocalDateTime> implements ITextFormatProvider
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
	 * @param model
	 *            the model
	 * @param pattern
	 *            the pattern to use
	 */
	public LocalDateTimeTextField(String id, IModel<LocalDateTime> model, String dateTimePattern)
	{
		super(id, model, LocalDateTime.class);

		this.converter = new TextFormatConverter() {
			private static final long serialVersionUID = 1L;

			@Override
			public DateTimeFormatter getDateTimeFormatter(Locale locale)
			{
				return DateTimeFormatter.ofPattern(dateTimePattern).withLocale(locale);
			}

			@Override
			public String getTextFormat(Locale locale)
			{
				return dateTimePattern;
			}
		};
	}

	/**
	 * Construct with a pattern.
	 * 
	 * @param id
	 *            the component id
	 * @param pattern
	 *            the pattern to use
	 */
	public LocalDateTimeTextField(String id, String dateTimePattern)
	{
		this(id, null, dateTimePattern);
	}

	/**
	 * Construct with a style.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the model
	 * @param timeStyle
	 *            the style to use
	 */
	public LocalDateTimeTextField(String id, IModel<LocalDateTime> model, FormatStyle dateStyle, FormatStyle timeStyle)
	{
		super(id, model, LocalDateTime.class);

		this.converter = new TextFormatConverter() {
			private static final long serialVersionUID = 1L;

			@Override
			public DateTimeFormatter getDateTimeFormatter(Locale locale)
			{
				return DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle).withLocale(locale);
			}

			@Override
			public String getTextFormat(Locale locale)
			{
				return DateTimeFormatterBuilder.getLocalizedDateTimePattern(dateStyle, timeStyle, IsoChronology.INSTANCE, locale);
			}
		};
	}


	/**
	 * Construct with a style.
	 * 
	 * @param id
	 *            the component id
	 * @param timeStyle
	 *            the style to use
	 */
	public LocalDateTimeTextField(String id, FormatStyle dateStyle, FormatStyle timeStyle)
	{
		this(id, null, dateStyle, timeStyle);
	}

	/**
	 * @return The specialized converter.
	 * @see org.apache.wicket.Component#createConverter(java.lang.Class)
	 */
	@Override
	protected IConverter<?> createConverter(Class<?> clazz)
	{
		if (LocalDateTime.class.isAssignableFrom(clazz))
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

	private abstract class TextFormatConverter extends LocalDateTimeConverter {
		private static final long serialVersionUID = 1L;

		public abstract String getTextFormat(Locale locale);
	}
}
