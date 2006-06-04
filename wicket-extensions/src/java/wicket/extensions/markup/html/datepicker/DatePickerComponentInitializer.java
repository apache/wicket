/*
 * $Id$ $Revision$ $Date$
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
package wicket.extensions.markup.html.datepicker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import wicket.Application;
import wicket.IInitializer;
import wicket.ResourceReference;
import wicket.markup.html.PackageResource;
import wicket.markup.html.PackageResourceReference;

/**
 * Initializer for the datepicker component.
 * 
 * @author jcompagner
 * @author Eelco Hillenius
 */
public class DatePickerComponentInitializer implements IInitializer
{
	/** locale to language map. */
	private static final Map<String, String> localeToLanguageReference = new HashMap<String, String>();

	static
	{
		// fill our default map. Note that new Locale("en", "","").getLanguage()
		// is to avoid future breaks because of the instable standard
		// (read about this in Locale.getLanguage()
		localeToLanguageReference.put(new Locale("af", "", "").toString(), "lang/calendar-af.js");
		localeToLanguageReference.put(new Locale("al", "", "").toString(), "lang/calendar-al.js");
		localeToLanguageReference.put(new Locale("br", "", "").toString(), "lang/calendar-br.js");
		localeToLanguageReference.put(new Locale("cs", "", "").toString(),
				"lang/calendar-cs-utf8.js");
		localeToLanguageReference.put(new Locale("da", "", "").toString(), "lang/calendar-da.js");
		localeToLanguageReference.put(new Locale("de", "", "").toString(), "lang/calendar-de.js");
		localeToLanguageReference.put(new Locale("el", "", "").toString(), "lang/calendar-el.js");
		localeToLanguageReference.put(new Locale("en", "", "").toString(), "lang/calendar-en.js");
		localeToLanguageReference.put(new Locale("en", "ZA", "").toString(),
				"lang/calendar-en_ZA.js");
		localeToLanguageReference.put(new Locale("es", "", "").toString(), "lang/calendar-es.js");
		localeToLanguageReference.put(new Locale("eu", "", "").toString(), "lang/calendar-eu.js");
		localeToLanguageReference.put(new Locale("fi", "", "").toString(), "lang/calendar-fi.js");
		localeToLanguageReference.put(new Locale("fr", "", "").toString(), "lang/calendar-fr.js");
		localeToLanguageReference.put(new Locale("he", "", "").toString(),
				"lang/calendar-he-utf8.js");
		localeToLanguageReference.put(new Locale("hr", "", "").toString(),
				"lang/calendar-hr-utf8.js");
		localeToLanguageReference.put(new Locale("hu", "", "").toString(), "lang/calendar-hu.js");
		localeToLanguageReference.put(new Locale("it", "", "").toString(),
				"lang/calendar-it-utf8.js");
		localeToLanguageReference.put(new Locale("ko", "", "").toString(),
				"lang/calendar-ko-utf8.js");
		localeToLanguageReference.put(new Locale("lt", "", "").toString(),
				"lang/calendar-lt-utf8.js");
		localeToLanguageReference.put(new Locale("lv", "", "").toString(), "lang/calendar-lv.js");
		localeToLanguageReference.put(new Locale("nl", "", "").toString(), "lang/calendar-nl.js");
		localeToLanguageReference.put(new Locale("no", "", "").toString(), "lang/calendar-no.js");
		localeToLanguageReference.put(new Locale("pl", "", "").toString(),
				"lang/calendar-pl-utf8.js");
		localeToLanguageReference.put(new Locale("pt", "", "").toString(), "lang/calendar-pt.js");
		localeToLanguageReference.put(new Locale("ro", "", "").toString(),
				"lang/calendar-ro-utf8.js");
		localeToLanguageReference.put(new Locale("ru", "", "").toString(),
				"lang/calendar-ru-utf8.js");
		localeToLanguageReference.put(new Locale("si", "", "").toString(),
				"lang/calendar-si-utf8.js");
		localeToLanguageReference.put(new Locale("sk", "", "").toString(),
				"lang/calendar-sk-utf8.js");
		localeToLanguageReference.put(new Locale("sr", "", "").toString(),
				"lang/calendar-sr-utf8.js");
		localeToLanguageReference.put(new Locale("sv", "", "").toString(),
				"lang/calendar-sv-utf8.js");
		localeToLanguageReference.put(new Locale("tr", "", "").toString(), "lang/calendar-tr.js");
		localeToLanguageReference.put(new Locale("zh", "", "").toString(),
				"lang/calendar-zh-utf8.js");
		localeToLanguageReference.put(new Locale("zh", "TW", "").toString(),
				"lang/calendar-zh_TW-utf8.js");
	}

	/**
	 * Gets the language.
	 * 
	 * @param currentLocale
	 *            the current locale
	 * @return language
	 */
	public static ResourceReference getLanguage(Locale currentLocale)
	{
		// try to get the reference from our default mapping
		// first try the language and country
		String ref = (String)localeToLanguageReference.get(currentLocale.toString());
		if (ref != null)
		{
			return new PackageResourceReference(Application.get(), DatePickerSettings.class, ref);
		}
		// now try only the language
		ref = (String)localeToLanguageReference.get(currentLocale.getLanguage());
		if (ref != null)
		{
			return new PackageResourceReference(Application.get(), DatePickerSettings.class, ref);
		}

		// we didn't find a mapping; just return English
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"lang/calendar-en.js");
	}

	/**
	 * @see wicket.IInitializer#init(wicket.Application)
	 */
	public void init(Application application)
	{
		// register dependencies
		PackageResource.bind(application, DatePickerSettings.class, "style/aqua/active-bg.gif");
		PackageResource.bind(application, DatePickerSettings.class, "style/aqua/dark-bg.gif");
		PackageResource.bind(application, DatePickerSettings.class, "style/aqua/hover-bg.gif");
		PackageResource.bind(application, DatePickerSettings.class, "style/aqua/menuarrow.gif");
		PackageResource.bind(application, DatePickerSettings.class, "style/aqua/normal-bg.gif");
		PackageResource.bind(application, DatePickerSettings.class, "style/aqua/rowhover-bg.gif");
		PackageResource.bind(application, DatePickerSettings.class, "style/aqua/status-bg.gif");
		PackageResource.bind(application, DatePickerSettings.class, "style/aqua/title-bg.gif");
		PackageResource.bind(application, DatePickerSettings.class, "style/aqua/today-bg.gif");

		// register buttons
		PackageResource.bind(application, DatePickerSettings.class, "style/menuarrow.gif");
		PackageResource.bind(application, DatePickerSettings.class, "style/menuarrow2.gif");

		PackageResource.bind(application, DatePickerSettings.class, "calendar_icon_1.gif");
		PackageResource.bind(application, DatePickerSettings.class, "calendar_icon_2.gif");
		PackageResource.bind(application, DatePickerSettings.class, "calendar_icon_3.gif");
		PackageResource.bind(application, DatePickerSettings.class, "style/aqua/theme.css");
		PackageResource.bind(application, DatePickerSettings.class, "style/calendar-blue.css");
		PackageResource.bind(application, DatePickerSettings.class, "style/calendar-blue2.css");
		PackageResource.bind(application, DatePickerSettings.class, "style/calendar-brown.css");
		PackageResource.bind(application, DatePickerSettings.class, "style/calendar-green.css");
		PackageResource.bind(application, DatePickerSettings.class, "style/calendar-system.css");
		PackageResource.bind(application, DatePickerSettings.class, "style/calendar-tas.css");
		PackageResource.bind(application, DatePickerSettings.class, "style/calendar-win2k-1.css");
		PackageResource.bind(application, DatePickerSettings.class, "style/calendar-win2k-2.css");
		PackageResource.bind(application, DatePickerSettings.class, "style/aqua/theme.css");
		PackageResource.bind(application, DatePickerSettings.class,
				"style/calendar-win2k-cold-1.css");
		PackageResource.bind(application, DatePickerSettings.class,
				"style/calendar-win2k-cold-2.css");

		PackageResource.bind(application, DatePicker.class, "calendar.js");
		PackageResource.bind(application, DatePicker.class, "calendar-setup.js");

		Iterator it = localeToLanguageReference.values().iterator();
		while (it.hasNext())
		{
			String path = (String)it.next();
			PackageResource.bind(application, DatePickerSettings.class, path);
		}
	}
}
