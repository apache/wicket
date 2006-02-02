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
	private static String LANGUAGE_AF = "lang/calendar-af.js";
	private static String LANGUAGE_AL = "lang/calendar-al.js";
	private static String LANGUAGE_BR = "lang/calendar-br.js";
	private static String LANGUAGE_CS = "lang/calendar-cs-utf8.js";
	private static String LANGUAGE_DA = "lang/calendar-da.js";
	private static String LANGUAGE_DE = "lang/calendar-de.js";
	private static String LANGUAGE_EL = "lang/calendar-el.js";
	private static String LANGUAGE_EN = "lang/calendar-en.js";
	private static String LANGUAGE_EN_ZA = "lang/calendar-en_ZA.js";
	private static String LANGUAGE_ES = "lang/calendar-es.js";
	private static String LANGUAGE_EU = "lang/calendar-eu.js";
	private static String LANGUAGE_FI = "lang/calendar-fi.js";
	private static String LANGUAGE_FR = "lang/calendar-fr.js";
	private static String LANGUAGE_HE = "lang/calendar-he-utf8.js";
	private static String LANGUAGE_HR = "lang/calendar-hr-utf8.js";
	private static String LANGUAGE_HU = "lang/calendar-hu.js";
	private static String LANGUAGE_IT = "lang/calendar-it-utf8.js";
	private static String LANGUAGE_KO = "lang/calendar-ko-utf8.js";
	private static String LANGUAGE_LT = "lang/calendar-lt-utf8.js";
	private static String LANGUAGE_LV = "lang/calendar-lv.js";
	private static String LANGUAGE_NL = "lang/calendar-nl.js";
	private static String LANGUAGE_NO = "lang/calendar-no.js";
	private static String LANGUAGE_PL = "lang/calendar-pl-utf8.js";
	private static String LANGUAGE_PT = "lang/calendar-pt.js";
	private static String LANGUAGE_RO = "lang/calendar-ro-utf8.js";
	private static String LANGUAGE_RU = "lang/calendar-ru-utf8.js";
	private static String LANGUAGE_SI = "lang/calendar-si-utf8.js";
	private static String LANGUAGE_SK = "lang/calendar-sk-utf8.js";
	private static String LANGUAGE_SR = "lang/calendar-sr-utf8.js";
	private static String LANGUAGE_SV = "lang/calendar-sv-utf8.js";
	private static String LANGUAGE_TR = "lang/calendar-tr.js";
	private static String LANGUAGE_ZH = "lang/calendar-zh-utf8.js";

	/** locale to language map. */
	private static final Map localeToLanguageReference = new HashMap();

	static
	{
		// fill our default map. Note that new Locale("en", "", "").getLanguage() is to avoid
		// future breaks because of the instable standard (read about this in Locale.getLanguage()
		localeToLanguageReference.put(new Locale("af", "", "").toString(), LANGUAGE_AF);
		localeToLanguageReference.put(new Locale("al", "", "").toString(), LANGUAGE_AL);
		localeToLanguageReference.put(new Locale("br", "", "").toString(), LANGUAGE_BR);
		localeToLanguageReference.put(new Locale("cs", "", "").toString(), LANGUAGE_CS);
		localeToLanguageReference.put(new Locale("da", "", "").toString(), LANGUAGE_DA);
		localeToLanguageReference.put(new Locale("de", "", "").toString(), LANGUAGE_DE);
		localeToLanguageReference.put(new Locale("el", "", "").toString(), LANGUAGE_EL);
		localeToLanguageReference.put(new Locale("en", "", "").toString(), LANGUAGE_EN);
		localeToLanguageReference.put(new Locale("en", "ZA", "").toString(), LANGUAGE_EN_ZA);
		localeToLanguageReference.put(new Locale("es", "", "").toString(), LANGUAGE_ES);
		localeToLanguageReference.put(new Locale("eu", "", "").toString(), LANGUAGE_EU);
		localeToLanguageReference.put(new Locale("fi", "", "").toString(), LANGUAGE_FI);
		localeToLanguageReference.put(new Locale("fr", "", "").toString(), LANGUAGE_FR);
		localeToLanguageReference.put(new Locale("he", "", "").toString(), LANGUAGE_HE);
		localeToLanguageReference.put(new Locale("hr", "", "").toString(), LANGUAGE_HR);
		localeToLanguageReference.put(new Locale("hu", "", "").toString(), LANGUAGE_HU);
		localeToLanguageReference.put(new Locale("it", "", "").toString(), LANGUAGE_IT);
		localeToLanguageReference.put(new Locale("ko", "", "").toString(), LANGUAGE_KO);
		localeToLanguageReference.put(new Locale("lt", "", "").toString(), LANGUAGE_LT);
		localeToLanguageReference.put(new Locale("lv", "", "").toString(), LANGUAGE_LV);
		localeToLanguageReference.put(new Locale("nl", "", "").toString(), LANGUAGE_NL);
		localeToLanguageReference.put(new Locale("no", "", "").toString(), LANGUAGE_NO);
		localeToLanguageReference.put(new Locale("pl", "", "").toString(), LANGUAGE_PL);
		localeToLanguageReference.put(new Locale("pt", "", "").toString(), LANGUAGE_PT);
		localeToLanguageReference.put(new Locale("ro", "", "").toString(), LANGUAGE_RO);
		localeToLanguageReference.put(new Locale("ru", "", "").toString(), LANGUAGE_RU);
		localeToLanguageReference.put(new Locale("si", "", "").toString(), LANGUAGE_SI);
		localeToLanguageReference.put(new Locale("sk", "", "").toString(), LANGUAGE_SK);
		localeToLanguageReference.put(new Locale("sr", "", "").toString(), LANGUAGE_SR);
		localeToLanguageReference.put(new Locale("sv", "", "").toString(), LANGUAGE_SV);
		localeToLanguageReference.put(new Locale("tr", "", "").toString(), LANGUAGE_TR);
		localeToLanguageReference.put(new Locale("zh", "", "").toString(), LANGUAGE_ZH);
	}
	
	/**
	 * Gets the language.
	 * @param currentLocale the current locale
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
		return new PackageResourceReference(Application.get(), DatePickerSettings.class, LANGUAGE_EN);
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
		PackageResource.bind(application, DatePickerSettings.class, "style/calendar-win2k-cold-1.css");
		PackageResource.bind(application, DatePickerSettings.class, "style/calendar-win2k-cold-2.css");

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
