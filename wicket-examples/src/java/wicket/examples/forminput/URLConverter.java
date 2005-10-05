/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.examples.forminput;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import wicket.util.convert.ConversionException;
import wicket.util.convert.IConverter;

/**
 * Converts from and to URLs.
 *
 * @author Eelco Hillenius
 */
public class URLConverter implements IConverter
{
	/**
	 * Construct.
	 */
	public URLConverter()
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

		if (c == URL.class)
		{
			if (value.getClass() == URL.class)
			{
				return value;
			}

			try
			{
				return new URL(value.toString());
			}
			catch (MalformedURLException e)
			{
				throw new ConversionException("'" + value + "' is not a valid URL");
			}
		}
		return value.toString();
	}

	/**
	 * @see wicket.util.convert.ILocalizable#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale)
	{
	}

	/**
	 * @see wicket.util.convert.ILocalizable#getLocale()
	 */
	public Locale getLocale()
	{
		return Locale.getDefault();
	}
}