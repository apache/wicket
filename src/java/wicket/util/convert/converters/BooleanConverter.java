/*
 * $Id$ $Revision:
 * 1.7 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import wicket.util.convert.ConversionException;
import wicket.util.convert.ITypeConverter;

/**
 * Converts from Object to Boolean.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class BooleanConverter implements ITypeConverter
{
	/**
	 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object)
	 */
	public Object convert(final Object value)
	{
		final String stringValue = value.toString();

		if (stringValue.equalsIgnoreCase("yes") || stringValue.equalsIgnoreCase("y")
				|| stringValue.equalsIgnoreCase("true") || stringValue.equalsIgnoreCase("on")
				|| stringValue.equalsIgnoreCase("1"))
		{
			return Boolean.TRUE;
		}
		else if (stringValue.equalsIgnoreCase("no") || stringValue.equalsIgnoreCase("n")
				|| stringValue.equalsIgnoreCase("false") || stringValue.equalsIgnoreCase("off")
				|| stringValue.equalsIgnoreCase("0"))
		{
			return Boolean.FALSE;
		}
		else
		{
			throw new ConversionException("Cannot convert '" + stringValue + "' to Boolean");
		}
	}
}