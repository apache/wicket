/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.convert.converters;

import java.util.Locale;

/**
 * Converts from Object to Short.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class ShortConverter extends AbstractIntegerConverter
{
	/**
	 * Constructor
	 */
	public ShortConverter()
	{
	}

	/**
	 * Constructor
	 * 
	 * @param locale
	 *            The locale for this converter
	 */
	public ShortConverter(final Locale locale)
	{
		super(locale);
	}

	/**
	 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object)
	 */
	public Object convert(final Object value)
	{
		final Number number = value instanceof Number ? (Number)value : parse(value,
				Short.MIN_VALUE, Short.MAX_VALUE);
		return new Short(number.shortValue());
	}

	/**
	 * @see wicket.util.convert.converters.AbstractConverter#getTargetType()
	 */
	protected Class getTargetType()
	{
		return Short.class;
	}
}
