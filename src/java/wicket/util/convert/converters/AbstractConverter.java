/*
 * $Id$
 * $Revision$ $Date$
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

import java.util.Locale;

import wicket.util.convert.ILocalizable;
import wicket.util.convert.ITypeConverter;
import wicket.util.convert.converters.AbstractConverter;

/**
 * Base class for locale aware type converters.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractConverter implements ITypeConverter, ILocalizable 
{
	/** The current locale. */
	protected Locale locale;

	/**
	 * gets the locale.
	 * 
	 * @return the locale
	 */
	public final Locale getLocale()
	{
		return locale;
	}

	/**
	 * sets the locale.
	 * 
	 * @param locale
	 *            the locale
	 */
	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}
}
