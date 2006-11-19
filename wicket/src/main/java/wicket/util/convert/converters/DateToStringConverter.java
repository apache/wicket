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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts from Date to String.
 * 
 * @author Eelco Hillenius
 */
public final class DateToStringConverter extends AbstractConverter
{
	private static final long serialVersionUID = 1L;

	/** The date format to use */
	private Map dateFormats = new HashMap();

	/**
	 * @param locale 
	 * 			  The locale for this dateformat
	 * @param dateFormat
	 *            The numberFormat to set.
	 */
	public final void setDateFormat(final Locale locale, final DateFormat dateFormat)
	{
		dateFormats.put(locale,dateFormat);
	}

	/**
	 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object,java.util.Locale)
	 */
	public Object convert(final Object value,Locale locale)
	{
		final DateFormat dateFormat = getDateFormat(locale);
		if (dateFormat != null)
		{
			return dateFormat.format(value);
		}
		return value.toString();
	}

	/**
	 * @see wicket.util.convert.converters.AbstractConverter#getTargetType()
	 */
	/**
	 * @param locale 
	 * @return Returns the dateFormat.
	 */
	public final DateFormat getDateFormat(Locale locale)
	{
		DateFormat dateFormat = (DateFormat)dateFormats.get(locale);
		if (dateFormat == null)
		{
			dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			dateFormats.put(locale, dateFormat);
		}
		return dateFormat;
	}
	
	protected Class getTargetType()
	{
		return String.class;
	}

}
