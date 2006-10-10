/*
 * $Id: AbstractDecimalConverter.java,v 1.2 2005/02/09 04:55:38 jonathanlocke
 * Exp $ $Revision$ $Date$
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

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Base class for all number converters.
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractDecimalConverter extends AbstractNumberConverter
{
	/** The date format to use */
	private Map<Locale, NumberFormat> numberFormats = new HashMap<Locale, NumberFormat>();


	/**
	 * @param locale
	 * @return Returns the numberFormat.
	 */
	@Override
	public NumberFormat getNumberFormat(Locale locale)
	{
		NumberFormat numberFormat = numberFormats.get(locale);
		if (numberFormat == null)
		{
			numberFormat = NumberFormat.getInstance(locale);
			numberFormats.put(locale, numberFormat);
		}
		return numberFormat;
	}

	/**
	 * @param locale
	 *            The Locale that was used for this NumberFormat
	 * @param numberFormat
	 *            The numberFormat to set.
	 */
	public final void setNumberFormat(final Locale locale, final NumberFormat numberFormat)
	{
		numberFormats.put(locale, numberFormat);
	}
}
