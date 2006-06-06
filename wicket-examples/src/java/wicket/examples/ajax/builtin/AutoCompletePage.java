/*
 * $Id: AjaxApplication.java 4860 2006-03-12 08:57:48Z ivaynberg $ $Revision:
 * 4860 $ $Date: 2006-03-12 09:57:48 +0100 (So, 12 Mrz 2006) $
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
package wicket.examples.ajax.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import wicket.markup.html.form.Form;
import wicket.model.Model;
import wicket.util.string.Strings;

/**
 * Page that demos the ajax auto complete text field
 * 
 * @author ivaynberg
 */
public class AutoCompletePage extends BasePage
{
	/**
	 * Constructor
	 */
	public AutoCompletePage()
	{
		Form form = new Form(this, "form");

		new AutoCompleteTextField<String>(form, "ac", new Model<String>(""))
		{
			@Override
			protected Iterator getChoices(String input)
			{
				if (Strings.isEmpty(input))
				{
					return Collections.EMPTY_LIST.iterator();
				}

				List<String> choices = new ArrayList<String>(10);
				Locale[] locales = Locale.getAvailableLocales();

				for (final Locale locale : locales)
				{
					final String country = locale.getDisplayCountry();
					if (country.toUpperCase().startsWith(input.toUpperCase()))
					{
						choices.add(country);
						if (choices.size() == 10)
						{
							break;
						}
					}
				}

				return choices.iterator();
			}
		};
	}
}
