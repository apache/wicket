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
 * Converts to and from Boolean objects.
 * 
 * @author Eelco Hillenius
 */
public final class BooleanConverter extends AbstractConverter
{
	/**
	 * Construct.
	 */
	public BooleanConverter()
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
		if(c == CONVERT_TO_DEFAULT_TYPE || Boolean.class.isAssignableFrom(c)
				|| c == Boolean.TYPE)
		{
			if (value instanceof Boolean)
			{
				return (value);
			}
			try
			{
				String stringValue = value.toString();

				if (stringValue.equalsIgnoreCase("yes") || stringValue.equalsIgnoreCase("y")
						|| stringValue.equalsIgnoreCase("true") || stringValue.equalsIgnoreCase("on")
						|| stringValue.equalsIgnoreCase("1"))
				{
					return (Boolean.TRUE);
				}
				else if (stringValue.equalsIgnoreCase("no") || stringValue.equalsIgnoreCase("n")
						|| stringValue.equalsIgnoreCase("false") || stringValue.equalsIgnoreCase("off")
						|| stringValue.equalsIgnoreCase("0"))
				{
					return (Boolean.FALSE);
				}
				else
				{
					throw new ConversionException(stringValue);
				}
			}
			catch (ClassCastException e)
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
