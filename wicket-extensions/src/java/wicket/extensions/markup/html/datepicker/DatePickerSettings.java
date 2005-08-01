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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.ResourceReference;
import wicket.markup.html.StaticResourceReference;

/**
 * The settings of the date picker component. Use this to customize the datepicker
 * (e.g. the icon, locale, format, etc).
 *
 * @author Eelco Hillenius
 */
public class DatePickerSettings implements Serializable
{
	/** log. */
	private static Log log = LogFactory.getLog(DatePickerSettings.class);

	private static String LANGUAGE_AF = "lang/calendar-af.js";
	private static String LANGUAGE_AL = "lang/calendar-al.js";
	private static String LANGUAGE_BR = "lang/calendar-br.js";
	private static String LANGUAGE_CS = "lang/calendar-cs-utf8.js";
	private static String LANGUAGE_DA = "lang/calendar-da.js";
	private static String LANGUAGE_DE = "lang/calendar-de.js";
	private static String LANGUAGE_EL = "lang/calendar-el-utf8.js";
	private static String LANGUAGE_EN = "lang/calendar-en.js";
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
	private static String LANGUAGE_SV = "lang/calendar-sv-utf8-utf8.js";
	private static String LANGUAGE_TR = "lang/calendar-tr.js";
	private static String LANGUAGE_ZH = "lang/calendar-zh-utf8.js";

	/** locale to language map. */
	private static final Map localeToLanguageReference = new HashMap();

	static
	{
		// fill our default map. Note that new Locale("en", "", "").getLanguage() is to avoid
		// future breaks because of the instable standard (read about this in Locale.getLanguage()
		localeToLanguageReference.put(new Locale("af", "", "").getLanguage(), LANGUAGE_AF);
		localeToLanguageReference.put(new Locale("al", "", "").getLanguage(), LANGUAGE_AL);
		localeToLanguageReference.put(new Locale("br", "", "").getLanguage(), LANGUAGE_BR);
		localeToLanguageReference.put(new Locale("cs", "", "").getLanguage(), LANGUAGE_CS);
		localeToLanguageReference.put(new Locale("da", "", "").getLanguage(), LANGUAGE_DA);
		localeToLanguageReference.put(new Locale("de", "", "").getLanguage(), LANGUAGE_DE);
		localeToLanguageReference.put(new Locale("el", "", "").getLanguage(), LANGUAGE_EL);
		localeToLanguageReference.put(new Locale("en", "", "").getLanguage(), LANGUAGE_EN);
		localeToLanguageReference.put(new Locale("es", "", "").getLanguage(), LANGUAGE_ES);
		localeToLanguageReference.put(new Locale("eu", "", "").getLanguage(), LANGUAGE_EU);
		localeToLanguageReference.put(new Locale("fi", "", "").getLanguage(), LANGUAGE_FI);
		localeToLanguageReference.put(new Locale("fr", "", "").getLanguage(), LANGUAGE_FR);
		localeToLanguageReference.put(new Locale("he", "", "").getLanguage(), LANGUAGE_HE);
		localeToLanguageReference.put(new Locale("hr", "", "").getLanguage(), LANGUAGE_HR);
		localeToLanguageReference.put(new Locale("hu", "", "").getLanguage(), LANGUAGE_HU);
		localeToLanguageReference.put(new Locale("it", "", "").getLanguage(), LANGUAGE_IT);
		localeToLanguageReference.put(new Locale("ko", "", "").getLanguage(), LANGUAGE_KO);
		localeToLanguageReference.put(new Locale("lt", "", "").getLanguage(), LANGUAGE_LT);
		localeToLanguageReference.put(new Locale("lv", "", "").getLanguage(), LANGUAGE_LV);
		localeToLanguageReference.put(new Locale("nl", "", "").getLanguage(), LANGUAGE_NL);
		localeToLanguageReference.put(new Locale("no", "", "").getLanguage(), LANGUAGE_NO);
		localeToLanguageReference.put(new Locale("pl", "", "").getLanguage(), LANGUAGE_PL);
		localeToLanguageReference.put(new Locale("pt", "", "").getLanguage(), LANGUAGE_PT);
		localeToLanguageReference.put(new Locale("ro", "", "").getLanguage(), LANGUAGE_RO);
		localeToLanguageReference.put(new Locale("ru", "", "").getLanguage(), LANGUAGE_RU);
		localeToLanguageReference.put(new Locale("si", "", "").getLanguage(), LANGUAGE_SI);
		localeToLanguageReference.put(new Locale("sk", "", "").getLanguage(), LANGUAGE_SK);
		localeToLanguageReference.put(new Locale("sr", "", "").getLanguage(), LANGUAGE_SR);
		localeToLanguageReference.put(new Locale("sv", "", "").getLanguage(), LANGUAGE_SV);
		localeToLanguageReference.put(new Locale("tr", "", "").getLanguage(), LANGUAGE_TR);
		localeToLanguageReference.put(new Locale("zh", "", "").getLanguage(), LANGUAGE_ZH);
	}

	/** all date formats. */
	private static Properties dateformats = new Properties();
	static
	{
		try
		{
			dateformats.load(DatePickerSettings.class.getResourceAsStream("dateformats.properties"));
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * The format string that will be used to enter the date in the input field. This
	 * format will be honored even if the input field is hidden.
	 */
	private String ifFormat = null;

	/**
	 * Wether the calendar is in ``single-click mode'' or ``double-click mode''. If
	 * true (the default) the calendar will be created in single-click mode.
	 */
	private boolean mode = true;

	/**
	 * Specifies which day is to be displayed as the first day of week. Possible
	 * values are 0 to 6; 0 means Sunday, 1 means Monday, ..., 6 means Saturday. The
	 * end user can easily change this too, by clicking on the day name in the
	 * calendar header.
	 */
	private int firstDay = 0;

	/**
	 * If ``true'' then the calendar will display week numbers.
	 */
	private boolean weekNumbers = true;

	/**
	 * Alignment of the calendar, relative to the reference element. The reference
	 * element is dynamically chosen like this: if a displayArea is specified then it
	 * will be the reference element. Otherwise, the input field is the reference
	 * element.
	 * <p>
	 * Align may contain one or two characters. The first character dictates the
	 * vertical alignment, relative to the element, and the second character dictates
	 * the horizontal alignment. If the second character is missing it will be assumed
	 * "l" (the left margin of the calendar will be at the same horizontal position as
	 * the left margin of the element). The characters given for the align parameters
	 * are case sensitive. This function only makes sense when the calendar is in
	 * popup mode. After computing the position it uses Calendar.showAt to display the
	 * calendar there.
	 * </p>
	 * <p>
	 * <strong>Vertical alignment</strong> The first character in ``align'' can take
	 * one of the following values:
	 * <ul>
	 * <li>T -- completely above the reference element (bottom margin of the calendar
	 * aligned to the top margin of the element). </li>
	 * <li>t -- above the element but may overlap it (bottom margin of the calendar
	 * aligned to the bottom margin of the element). </li>
	 * <li>c -- the calendar displays vertically centered to the reference element.
	 * It might overlap it (that depends on the horizontal alignment). </li>
	 * <li>b -- below the element but may overlap it (top margin of the calendar
	 * aligned to the top margin of the element). </li>
	 * <li>B -- completely below the element (top margin of the calendar aligned to
	 * the bottom margin of the element). </li>
	 * </ul>
	 * </p>
	 * <p>
	 * <strong>Horizontal alignment</strong> The second character in ``align'' can
	 * take one of the following values:
	 * <ul>
	 * <li>L -- completely to the left of the reference element (right margin of the
	 * calendar aligned to the left margin of the element). </li>
	 * <li>l -- to the left of the element but may overlap it (left margin of the
	 * calendar aligned to the left margin of the element). </li>
	 * <li>c -- horizontally centered to the element. Might overlap it, depending on
	 * the vertical alignment. </li>
	 * <li>r -- to the right of the element but may overlap it (right margin of the
	 * calendar aligned to the right margin of the element). </li>
	 * <li>R -- completely to the right of the element (left margin of the calendar
	 * aligned to the right margin of the element). </li>
	 * </ul>
	 * </p>
	 */
	private String align = null;

	/**
	 * If this is set to true then the calendar will also allow time selection.
	 */
	private boolean showsTime = false;

	/**
	 * Set this to ``12'' or ``24'' to configure the way that the calendar will
	 * display time.
	 */
	private String timeFormat = null;

	/**
	 * Set this to ``false'' if you want the calendar to update the field only when
	 * closed (by default it updates the field at each date change, even if the
	 * calendar is not closed).
	 */
	private boolean electric = true;

	/**
	 * If set to ``true'' then days belonging to months overlapping with the currently
	 * displayed month will also be displayed in the calendar (but in a ``faded-out''
	 * color).
	 */
	private boolean showOthers = false;

	/** the style. */
	private ResourceReference style = null;

	/** the button icon. */
	private ResourceReference icon = null;

	/** the language. */
	private ResourceReference language = null;

	/**
	 * Construct.
	 */
	public DatePickerSettings()
	{
		// register buttons
		new StaticResourceReference(DatePickerSettings.class, "style/menuarrow.gif");
		new StaticResourceReference(DatePickerSettings.class, "style/menuarrow2.gif");
	}

	/**
	 * Return the properties as a script.
	 * @param locale the current locale
	 * @return the properties as a script
	 */
	public String toScript(Locale locale)
	{
		StringBuffer b = new StringBuffer();
		// create the script that represents these properties. Only create entries for
		// values that are different from the default value (save a bit bandwith)

		if (!isMode())
		{
			b.append("\n\tmode : false,");
		}

		if (getFirstDay() != 0)
		{
			b.append("\n\tfistDay : ").append(getFirstDay()).append(",");
		}

		if (!isWeekNumbers())
		{
			b.append("\n\tweekNumbers : false,");
		}

		if (getAlign() != null)
		{
			b.append("\n\talign : ").append(getAlign()).append(",");
		}

		if (isShowsTime())
		{
			b.append("\n\tshowsTime : true,");
		}

		if (getTimeFormat() != null)
		{
			b.append("\n\timeFormat : ").append(getTimeFormat()).append(",");
		}

		if (!isElectric())
		{
			b.append("\n\telectric : false,");
		}

		if (isShowOthers())
		{
			b.append("\n\tshowOthers : true,");
		}

		// append date format
		String ifFormat = getIfFormat(locale);
		if (ifFormat != null)
		{
			b.append("\n\t\tifFormat : \"").append(ifFormat).append("\"");
		}
		
		return b.toString();
	}

	/**
	 * create a button icon.
	 * @return a button icon.
	 */
	public final StaticResourceReference newButtonIconRed()
	{
		return new StaticResourceReference(DatePickerSettings.class, "calendar_icon_1.jpg");
	}

	/**
	 * create a button icon.
	 * @return a button icon.
	 */
	public final StaticResourceReference newButtonIconPlain()
	{
		return new StaticResourceReference(DatePickerSettings.class, "calendar_icon_2.jpg");
	}

	/**
	 * create a button icon.
	 * @return a button icon.
	 */
	public final StaticResourceReference newButtonIconBlue()
	{
		return new StaticResourceReference(DatePickerSettings.class, "calendar_icon_3.jpg");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleAqua()
	{
		// register dependencies
		new StaticResourceReference(DatePickerSettings.class, "style/aqua/active-bg.gif");
		new StaticResourceReference(DatePickerSettings.class, "style/aqua/dark-bg.gif");
		new StaticResourceReference(DatePickerSettings.class, "style/aqua/hover-bg.gif");
		new StaticResourceReference(DatePickerSettings.class, "style/aqua/menuarrow.gif");
		new StaticResourceReference(DatePickerSettings.class, "style/aqua/normal-bg.gif");
		new StaticResourceReference(DatePickerSettings.class, "style/aqua/rowhover-bg.gif");
		new StaticResourceReference(DatePickerSettings.class, "style/aqua/status-bg.gif");
		new StaticResourceReference(DatePickerSettings.class, "style/aqua/title-bg.gif");
		new StaticResourceReference(DatePickerSettings.class, "style/aqua/today-bg.gif");

		return new StaticResourceReference(DatePickerSettings.class, "style/aqua/theme.css");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleWinter()
	{
		return new StaticResourceReference(DatePickerSettings.class, "style/calendar-blue.css");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleBlue()
	{
		return new StaticResourceReference(DatePickerSettings.class, "style/calendar-blue2.css");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleSummer()
	{
		return new StaticResourceReference(DatePickerSettings.class, "style/calendar-brown.css");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleGreen()
	{
		return new StaticResourceReference(DatePickerSettings.class, "style/calendar-green.css");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleSystem()
	{
		return new StaticResourceReference(DatePickerSettings.class, "style/calendar-system.css");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleTas()
	{
		return new StaticResourceReference(DatePickerSettings.class, "style/calendar-tas.css");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleWin2k()
	{
		return new StaticResourceReference(DatePickerSettings.class, "style/calendar-win2k.css");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleWin2k1()
	{
		return new StaticResourceReference(DatePickerSettings.class, "style/calendar-win2k-1.css");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleWin2k2()
	{
		return new StaticResourceReference(DatePickerSettings.class, "style/aqua/theme.css");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleWin2kCold1()
	{
		return new StaticResourceReference(DatePickerSettings.class, "style/calendar-win2k-cold-1.css");
	}

	/**
	 * Create a style
	 * @return a style
	 */
	public final StaticResourceReference newStyleWin2kCold2()
	{
		return new StaticResourceReference(DatePickerSettings.class, "style/calendar-win2k-cold-2.css");
	}

	/**
	 * Gets the align.
	 * @return align
	 */
	public String getAlign()
	{
		return align;
	}

	/**
	 * Sets the align.
	 * @param align align
	 */
	public void setAlign(String align)
	{
		this.align = align;
	}

	/**
	 * Gets the electric.
	 * @return electric
	 */
	public boolean isElectric()
	{
		return electric;
	}

	/**
	 * Sets the electric.
	 * @param electric electric
	 */
	public void setElectric(boolean electric)
	{
		this.electric = electric;
	}

	/**
	 * Gets the firstDay.
	 * @return firstDay
	 */
	public int getFirstDay()
	{
		return firstDay;
	}

	/**
	 * Sets the firstDay.
	 * @param firstDay firstDay
	 */
	public void setFirstDay(int firstDay)
	{
		this.firstDay = firstDay;
	}

	/**
	 * Gets the ifFormat.
	 * @param locale current locale
	 * @return ifFormat
	 */
	public String getIfFormat(Locale locale)
	{
		// when it was set explicitly, return that
		if (ifFormat != null)
		{
			return ifFormat;
		}

		// else, get it from our map - might be null, but our calling
		// function can handle that
		return dateformats.getProperty(locale.getLanguage());
	}

	/**
	 * Sets the ifFormat.
	 * @param ifFormat ifFormat
	 */
	public void setIfFormat(String ifFormat)
	{
		this.ifFormat = ifFormat;
	}

	/**
	 * Gets the mode.
	 * @return mode
	 */
	public boolean isMode()
	{
		return mode;
	}

	/**
	 * Sets the mode.
	 * @param mode mode
	 */
	public void setMode(boolean mode)
	{
		this.mode = mode;
	}

	/**
	 * Gets the showOthers.
	 * @return showOthers
	 */
	public boolean isShowOthers()
	{
		return showOthers;
	}

	/**
	 * Sets the showOthers.
	 * @param showOthers showOthers
	 */
	public void setShowOthers(boolean showOthers)
	{
		this.showOthers = showOthers;
	}

	/**
	 * Gets the showsTime.
	 * @return showsTime
	 */
	public boolean isShowsTime()
	{
		return showsTime;
	}

	/**
	 * Sets the showsTime.
	 * @param showsTime showsTime
	 */
	public void setShowsTime(boolean showsTime)
	{
		this.showsTime = showsTime;
	}

	/**
	 * Gets the timeFormat.
	 * @return timeFormat
	 */
	public String getTimeFormat()
	{
		return timeFormat;
	}

	/**
	 * Sets the timeFormat.
	 * @param timeFormat timeFormat
	 */
	public void setTimeFormat(String timeFormat)
	{
		this.timeFormat = timeFormat;
	}

	/**
	 * Gets the weekNumbers.
	 * @return weekNumbers
	 */
	public boolean isWeekNumbers()
	{
		return weekNumbers;
	}

	/**
	 * Sets the weekNumbers.
	 * @param weekNumbers weekNumbers
	 */
	public void setWeekNumbers(boolean weekNumbers)
	{
		this.weekNumbers = weekNumbers;
	}

	/**
	 * Gets the icon.
	 * @return icon
	 */
	public ResourceReference getIcon()
	{
		if (icon == null)
		{
			icon = newButtonIconRed();
		}

		return icon;
	}

	/**
	 * Sets the icon.
	 * @param icon icon
	 */
	public void setIcon(ResourceReference icon)
	{
		this.icon = icon;
	}

	/**
	 * Gets the language.
	 * @param currentLocale the current locale
	 * @return language
	 */
	public ResourceReference getLanguage(Locale currentLocale)
	{
		// if the language was set explicitly, return that
		if (language != null)
		{
			return language;
		}

		// try to get the reference from our default mapping
		String ref = (String)localeToLanguageReference.get(currentLocale.getLanguage());
		if (ref != null)
		{
			return new StaticResourceReference(DatePickerSettings.class, ref);
		}

		// we didn't find a mapping; just return English
		return new StaticResourceReference(DatePickerSettings.class, LANGUAGE_EN);
	}

	/**
	 * Sets the language.
	 * @param language language
	 */
	public void setLanguage(ResourceReference language)
	{
		this.language = language;
	}

	/**
	 * Gets the style.
	 * @return style
	 */
	public ResourceReference getStyle()
	{
		if (style == null)
		{
			style = newStyleAqua();
		}
		
		return style;
	}

	/**
	 * Sets the style.
	 * @param style style
	 */
	public void setStyle(ResourceReference style)
	{
		this.style = style;
	}
}