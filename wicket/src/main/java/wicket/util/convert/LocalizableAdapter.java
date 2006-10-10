/*
 * $Id: org.eclipse.jdt.ui.prefs,v 1.6 2006/02/06 08:27:03 ivaynberg Exp $
 * $Revision: 1.6 $ $Date: 2006/02/06 08:27:03 $
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
package wicket.util.convert;

import java.util.Locale;

/**
 * Adapter class for the {@link ILocalizable} interface.
 * 
 * @author Eelco Hillenius
 */
public abstract class LocalizableAdapter implements ILocalizable
{
	/** The locale. */
	private Locale locale;

	/**
	 * @see wicket.util.convert.ILocalizable#setLocale(java.util.Locale)
	 */
	public final void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * @see wicket.util.convert.ILocalizable#getLocale()
	 */
	public final Locale getLocale()
	{
		return locale;
	}

}
