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

import wicket.util.convert.IConverter;
import wicket.util.convert.IStringConverter;

/**
 * Abstract converter class.
 *
 * @author Eelco Hillenius
 */
public abstract class AbstractConverter implements IConverter, IStringConverter
{
	/**
	 * Construct.
	 */
	public AbstractConverter()
	{
	}

	/**
	 * @see wicket.util.convert.IStringConverter#toString(java.lang.Object)
	 */
	public String toString(Object value)
	{
		if(value == null)
		{
			return null;
		}
		return value.toString();
	}


	/**
	 * @see wicket.util.convert.IStringConverter#valueOf(java.lang.String)
	 */
	public Object valueOf(String string)
	{
		return convert(string, null);
	}
}