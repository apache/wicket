/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.util.lang;

import java.io.Serializable;
import java.util.Locale;

import wicket.IConverterLocator;
import wicket.util.convert.IConverter;

/**
 * @author jcompagner
 */
public class PropertyResolverConverter implements Serializable
{
	private static final long serialVersionUID = 1L;


	private final IConverterLocator converterSupplier;
	private final Locale locale;

	/**
	 * Construct.
	 * 
	 * @param converterSupplier
	 * @param locale
	 */
	public PropertyResolverConverter(IConverterLocator converterSupplier, Locale locale)
	{
		this.converterSupplier = converterSupplier;
		this.locale = locale;
	}

	/**
	 * @param object
	 * @param clz
	 * @return The converted object
	 */
	public Object convert(Object object, Class<?> clz)
	{
		if (object == null)
		{
			return null;
		}
		if (clz.isAssignableFrom(object.getClass()))
		{
			return object;
		}
		IConverter converter = converterSupplier.getConverter(clz);
		if (object instanceof String)
		{
			return converter.convertToObject((String)object, locale);
		}
		else if (clz == String.class)
		{
			return converter.convertToString(object, locale);
		}
		else
		{
			try
			{
				return Objects.convertValue(object, clz);
			}
			catch (RuntimeException ex)
			{
				// ignore that try it the other way
			}
			// go through string to convert to the right object.
			String tmp = converter.convertToString(object, locale);
			return converter.convertToObject(tmp, locale);
		}
	}
}
