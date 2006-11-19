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

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts from Date to String.
 * 
 * @author Eelco Hillenius
 */
public final class NumberToStringConverter extends AbstractConverter
{
	private static final long serialVersionUID = 1L;

	/** The date format to use */
	private Map numberFormats = new HashMap();

	/**
	 * Construct.
	 */
	public NumberToStringConverter()
	{
	}

	/**
	 * @param locale 
	 * @return Returns the numberFormat.
	 */
	public final NumberFormat getNumberFormat(Locale locale)
	{
		NumberFormat numberFormat = (NumberFormat)numberFormats.get(locale);
		if (numberFormat == null)
		{
			numberFormat = NumberFormat.getInstance(locale);
			numberFormats.put(locale, numberFormat);
		}
		return numberFormat;
	}

	/**
	 * @param locale 
	 * 			  The Locale that was used for this NumberFormat
	 * @param numberFormat
	 *            The numberFormat to set.
	 */
	public final void setNumberFormat(final Locale locale, final NumberFormat numberFormat)
	{
		numberFormats.put(locale,numberFormat);
	}

	/**
	 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object,java.util.Locale)
	 */
	public Object convert(final Object value, Locale locale)
	{
		NumberFormat fmt = getNumberFormat(locale);
		if (fmt != null)
		{
			return fmt.format(value);
		}
		return value.toString();
	}

	/**
	 * @see wicket.util.convert.converters.AbstractConverter#getTargetType()
	 */
	protected Class getTargetType()
	{
		return String.class;
	}
}
