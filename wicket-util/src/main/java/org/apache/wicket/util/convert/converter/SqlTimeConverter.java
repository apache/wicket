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

import java.sql.Time;
import java.text.DateFormat;
import java.util.Locale;

/**
 * Converts to {@link Time}.
 */
public class SqlTimeConverter extends AbstractDateConverter<Time>
{
	private static final long serialVersionUID = 1L;

	@Override
	protected Time createDateLike(long date)
	{
		return new Time(date);
	}

	@Override
	public DateFormat getDateFormat(Locale locale)
	{
		if (locale == null)
		{
			locale = Locale.getDefault(Locale.Category.FORMAT);
		}

		// return a clone because DateFormat.getDateInstance uses a pool
		return (DateFormat) DateFormat.getTimeInstance(DateFormat.SHORT, locale).clone();
	}

	@Override
	protected Class<Time> getTargetType()
	{
		return Time.class;
	}
}
