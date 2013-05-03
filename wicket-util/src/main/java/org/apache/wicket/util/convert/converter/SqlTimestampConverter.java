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
package org.apache.wicket.util.convert.converter;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Locale;

/**
 * Converts to {@link Timestamp}.
 * 
 * @author eelcohillenius
 */
public class SqlTimestampConverter extends AbstractDateConverter<Timestamp>
{
	private static final long serialVersionUID = 1L;

	private final int dateFormat;
	private final int timeFormat;

	/**
	 * Construct.
	 */
	public SqlTimestampConverter()
	{
		this(DateFormat.SHORT, DateFormat.SHORT);
	}

	/**
	 * Construct.
	 * 
	 * @param dateFormat
	 *            See java.text.DateFormat for details. Defaults to DateFormat.SHORT
	 */
	public SqlTimestampConverter(final int dateFormat)
	{
		this(dateFormat, DateFormat.SHORT);
	}

	/**
	 * Construct.
	 * 
	 * @param dateFormat
	 *            See java.text.DateFormat for details. Defaults to DateFormat.SHORT * @param
	 *            timeFormat See java.text.DateFormat for details. Defaults to DateFormat.SHORT
	 * @param timeFormat
	 */
	public SqlTimestampConverter(final int dateFormat, final int timeFormat)
	{
		this.dateFormat = dateFormat;
		this.timeFormat = timeFormat;
	}

	@Override
	public DateFormat getDateFormat(Locale locale)
	{
		if (locale == null)
		{
			locale = Locale.getDefault();
		}

		// return a clone because DateFormat.getDateInstance uses a pool
		return (DateFormat) DateFormat.getDateTimeInstance(dateFormat, timeFormat, locale).clone();
	}

	@Override
	protected Timestamp createDateLike(long date)
	{
		return new Timestamp(date);
	}

	@Override
	protected Class<Timestamp> getTargetType()
	{
		return Timestamp.class;
	}

}
