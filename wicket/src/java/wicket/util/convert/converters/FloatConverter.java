/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.convert.converters;

import wicket.util.convert.ConversionException;

/**
 * Converts to and from Float objects.
 * 
 * @author Eelco Hillenius
 */
public final class FloatConverter extends AbstractConverter
{
	/**
	 * Construct.
	 */
	public FloatConverter()
	{
	}

	/**
	 * @see wicket.util.convert.IConverter#convert(java.lang.Object, java.lang.Class)
	 */
	public Object convert(Object value, Class c)
	{
		if (value == null)
		{
			return null;
		}
		if(c == CONVERT_TO_DEFAULT_TYPE || Number.class.isAssignableFrom(c)
				|| c == Float.TYPE)
		{
			if (value instanceof Float)
			{
				return (value);
			}
			else if (value instanceof Number)
			{
				return new Float(((Number)value).floatValue());
			}

			try
			{
				return (new Float(value.toString()));
			}
			catch (Exception e)
			{
				throw new ConversionException(e);
			}
		}
		else if(String.class.isAssignableFrom(c))
		{
			return toString(value);
		}
		throw new ConversionException(this +
				" cannot handle conversions of type " + c);
	}
}
