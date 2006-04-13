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
 * 
 */
public class AutoCompletePage extends BasePage
{
	/**
	 * Constructor
	 */
	public AutoCompletePage()
	{
		Form form = new Form("form");
		add(form);

		form.add(new AutoCompleteTextField("ac", new Model(""))
		{

			protected Iterator getChoices(String input)
			{
				if (Strings.isEmpty(input))
				{
					return Collections.EMPTY_LIST.iterator();
				}

				List choices = new ArrayList(10);

				Locale[] locales = Locale.getAvailableLocales();

				for (int i = 0; i < locales.length; i++)
				{
					final Locale locale = locales[i];
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

		});
	}
}
