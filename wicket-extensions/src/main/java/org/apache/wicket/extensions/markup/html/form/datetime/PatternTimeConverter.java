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
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.wicket.util.lang.Args;


/**
 * Date converter that uses javax.time and can be configured to take the time zone difference between
 * clients and server into account. This converter is hard coded to use the provided custom date
 * pattern, no matter what current locale is used. See {@link SimpleDateFormat} for available
 * patterns.
 * <p>
 * This converter is especially suited on a per-component base.
 * </p>
 * 
 * @see SimpleDateFormat
 * @see StyleDateConverter
 * @see org.apache.wicket.extensions.markup.html.form.DateTextField
 * @see java.time.LocalTime
 * @see DateTimeFormatter
 * 
 * @author eelcohillenius
 */
public class PatternTimeConverter extends LocalTimeConverter
{

	private static final long serialVersionUID = 1L;

	/** pattern to use. */
	private final String timePattern;

	/**
	 * Construct.
	 * 
	 * @param timePattern
	 *            The pattern to use. Must be not null. See {@link SimpleDateFormat} for available
	 *            patterns.
	 * @throws IllegalArgumentException
	 *             in case the date pattern is null
	 */
	public PatternTimeConverter(String timePattern)
	{
		super();
		this.timePattern = Args.notNull(timePattern, "timePattern");
	}

	/**
	 * Gets the optional date pattern.
	 * 
	 * @return datePattern
	 */
	@Override
	public final String getPattern(Locale locale)
	{
		return timePattern;
	}

	/**
	 * @return formatter The formatter for the current conversion
	 */
	@Override
	public DateTimeFormatter getFormat(Locale locale)
	{
		return DateTimeFormatter.ofPattern(timePattern).withLocale(locale);
	}
}
