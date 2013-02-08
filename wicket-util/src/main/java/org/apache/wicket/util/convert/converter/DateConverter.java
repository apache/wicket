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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.string.Strings;

/**
 * Converts from Object to Date.
 * 
 * @author Eelco Hillenius
 */
public class DateConverter extends AbstractConverter<Date>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,Locale)
	 */
	@Override
	public Date convertToObject(final String value, final Locale locale)
	{
		if ((value == null) || Strings.isEmpty(value))
		{
			return null;
		}
		else
		{
			return parse(getDateFormat(locale), value, locale);
		}
	}

	/**
	 * @see org.apache.wicket.util.convert.IConverter#convertToString(Object, java.util.Locale)
	 */
	@Override
	public String convertToString(final Date value, final Locale locale)
	{
		final DateFormat dateFormat = getDateFormat(locale);
		if (dateFormat != null)
		{
			return dateFormat.format(value);
		}
		return value.toString();
	}


	/**
	 * @param locale
	 * @return Returns the date format.
	 */
	public DateFormat getDateFormat(Locale locale)
	{
		if (locale == null)
		{
			locale = Locale.getDefault();
		}

		return DateFormat.getDateInstance(DateFormat.SHORT, locale);
	}

	/**
	 * @see org.apache.wicket.util.convert.converter.AbstractConverter#getTargetType()
	 */
	@Override
	protected Class<Date> getTargetType()
	{
		return Date.class;
	}
}