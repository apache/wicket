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
 * Converts to and from Character objects.
 * 
 * @author Eelco Hillenius
 */
public final class CharacterConverter extends AbstractConverter
{
	/**
	 * Construct.
	 */
	public CharacterConverter()
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
		if(c == CONVERT_TO_DEFAULT_TYPE || Character.class.isAssignableFrom(c)
				|| c == Character.TYPE)
		{
			if (value instanceof Character)
			{
				return (value);
			}
			try
			{
				return (new Character(value.toString().charAt(0)));
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
