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

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Converts from Date to String.
 * 
 * @author Eelco Hillenius
 */
public final class NumberToStringConverter extends AbstractConverter
{
	/** The date format to use */
	private NumberFormat numberFormat;

	/**
	 * @see wicket.util.convert.converters.AbstractConverter#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale)
	{
		super.setLocale(locale);
		numberFormat = null;
	}

	/**
	 * @return Returns the numberFormat.
	 */
	public final NumberFormat getNumberFormat()
	{
		if (numberFormat == null && locale != null)
		{
			numberFormat = NumberFormat.getInstance(locale);
		}
		return numberFormat;
	}

	/**
	 * @param numberFormat The numberFormat to set.
	 */
	public final void setNumberFormat(final NumberFormat numberFormat)
	{
		this.numberFormat = numberFormat;
	}

	/**
	 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object)
	 */
	public Object convert(final Object value)
	{
		NumberFormat fmt = getNumberFormat();
		if (fmt != null)
		{
			return fmt.format(value);
		}
		return value.toString();
	}
}
