/*
 * $Id: FormInput.java 5050 2006-03-21 12:46:29Z joco01 $ $Revision$
 * $Date: 2006-03-21 13:46:29 +0100 (Di, 21 Mrz 2006) $
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
package wicket.examples.displaytag;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import wicket.markup.html.form.ChoiceRenderer;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.Panel;
import wicket.model.PropertyModel;
import wicket.protocol.http.WebRequest;

/**
 * Example for form input.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class LocaleSelector extends Panel
{
	/** Relevant locales wrapped in a list. */
	private static final List LOCALES = Arrays.asList(new Locale[] { Locale.ENGLISH,
			new Locale("nl"), Locale.GERMAN, Locale.SIMPLIFIED_CHINESE });

	/**
	 * 
	 * @param id
	 */
	public LocaleSelector(final String id)
	{
		super(id);

		// Dropdown for selecting locale
		add(new LocaleDropDownChoice("localeSelect"));

		// Link to return to default locale
		add(new Link("defaultLocaleLink")
		{
			public void onClick()
			{
				WebRequest request = (WebRequest)getRequest();
				setLocale(request.getLocale());
			}
		});
	}

	/**
	 * Sets locale for the user's session (getLocale() is inherited from
	 * Component)
	 * 
	 * @param locale
	 *            The new locale
	 */
	public void setLocale(final Locale locale)
	{
		if (locale != null)
		{
			getSession().setLocale(locale);
		}
	}

	/**
	 * Dropdown with Locales.
	 */
	private final class LocaleDropDownChoice extends DropDownChoice
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 */
		public LocaleDropDownChoice(String id)
		{
			super(id, LOCALES, new LocaleChoiceRenderer());

			// set the model that gets the current locale, and that is used for
			// updating the current locale to property 'locale' of FormInput
			setModel(new PropertyModel(LocaleSelector.this, "locale"));
		}

		/**
		 * @see wicket.markup.html.form.DropDownChoice#wantOnSelectionChangedNotifications()
		 */
		protected boolean wantOnSelectionChangedNotifications()
		{
			// we want roundtrips when a the user selects another item
			return true;
		}

		/**
		 * @see wicket.markup.html.form.DropDownChoice#onSelectionChanged(java.lang.Object)
		 */
		public void onSelectionChanged(Object newSelection)
		{
			// note that we don't have to do anything here, as our property
			// model allready calls FormInput.setLocale when the model is
			// updated
			// setLocale((Locale)newSelection); // so we don't need to do this
		}
	}

	/**
	 * Choice for a locale.
	 */
	private final class LocaleChoiceRenderer extends ChoiceRenderer
	{
		/**
		 * Constructor.
		 */
		public LocaleChoiceRenderer()
		{
		}

		/**
		 * @see wicket.markup.html.form.IChoiceRenderer#getDisplayValue(Object)
		 */
		public Object getDisplayValue(Object object)
		{
			Locale locale = (Locale)object;
			String display = locale.getDisplayName(getLocale());
			return display;
		}
	}
}