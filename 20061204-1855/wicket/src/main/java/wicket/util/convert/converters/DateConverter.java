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
package wicket.util.convert.converters;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Converts from Object to Date.
 * 
 * @author Eelco Hillenius
 */
public class DateConverter extends AbstractConverter
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public DateConverter()
	{
	}

	/**
	 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object,java.util.Locale)
	 */
	public Object convert(final Object value, Locale locale)
	{
		return parse(getDateFormat(locale), value);
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
	 * @see wicket.util.convert.converters.AbstractConverter#getTargetType()
	 */
	protected Class getTargetType()
	{
		return Date.class;
	}
}