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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Base class for all number converters.
 * 
 * @author Jonathan Locke
 */
public abstract class DecimalConverter extends NumberConverter 
{
    /**
     * Constructor
     */
    public DecimalConverter()
    {
    }
    
    /**
     * Constructor
     * @param locale The locale for this converter
     */
    public DecimalConverter(final Locale locale)
    {
        super(locale);
    }
    
    /**
	 * @return Returns the numberFormat.
	 */
	public final NumberFormat getNumberFormat()
	{
        if (numberFormat == null)
        {
            numberFormat = DecimalFormat.getInstance(getLocale());
        }
		return numberFormat;
	}       
}
