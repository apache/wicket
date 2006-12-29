/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.datepicker;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.ResourceReference;

/**
 * The settings of the date picker component. Use this to customize the
 * datepicker (e.g. the icon, locale, format, etc).
 * 
 * @author Eelco Hillenius
 */
public class DatePickerSettings implements Serializable
{
	/** all date formats. */
	private static Properties dateformats = new Properties();

	/** locale to language map. */
	private static final Map<String, String> localeToLanguageReference = new HashMap<String, String>();

	/** log. */
	private static final Logger log = LoggerFactory.getLogger(DatePickerSettings.class);

	private static final long serialVersionUID = 1L;

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
		localeToLanguageReference.put(new Locale("da", "", "").toString(),
				"lang/calendar-da-utf8.js");
		localeToLanguageReference.put(new Locale("de", "", "").toString(), "lang/calendar-de.js");
		localeToLanguageReference.put(new Locale("el", "", "").toString(), "lang/calendar-el.js");
		localeToLanguageReference.put(new Locale("en", "", "").toString(), "lang/calendar-en.js");
		localeToLanguageReference.put(new Locale("en", "ZA", "").toString(),
				"lang/calendar-en_ZA.js");
		localeToLanguageReference.put(new Locale("es", "", "").toString(),
				"lang/calendar-es-utf8.js");
		localeToLanguageReference.put(new Locale("eu", "", "").toString(), "lang/calendar-eu.js");
		localeToLanguageReference.put(new Locale("fi", "", "").toString(), "lang/calendar-fi.js");
		localeToLanguageReference.put(new Locale("fr", "", "").toString(),
				"lang/calendar-fr-utf8.js");
		localeToLanguageReference.put(new Locale("he", "", "").toString(),
				"lang/calendar-he-utf8.js");
		localeToLanguageReference.put(new Locale("hr", "", "").toString(),
				"lang/calendar-hr-utf8.js");
		localeToLanguageReference.put(new Locale("hu", "", "").toString(),
				"lang/calendar-hu-utf8.js");
		localeToLanguageReference.put(new Locale("it", "", "").toString(),
				"lang/calendar-it-utf8.js");
		localeToLanguageReference.put(new Locale("ko", "", "").toString(),
				"lang/calendar-ko-utf8.js");
		localeToLanguageReference.put(new Locale("lt", "", "").toString(),
				"lang/calendar-lt-utf8.js");
		localeToLanguageReference.put(new Locale("lv", "", "").toString(),
				"lang/calendar-lv-utf8.js");
		localeToLanguageReference.put(new Locale("nl", "", "").toString(), "lang/calendar-nl.js");
		localeToLanguageReference.put(new Locale("no", "", "").toString(),
				"lang/calendar-no-utf8.js");
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
		localeToLanguageReference.put(new Locale("tr", "", "").toString(),
				"lang/calendar-tr-utf8.js");
		localeToLanguageReference.put(new Locale("zh", "", "").toString(),
				"lang/calendar-zh-utf8.js");
		localeToLanguageReference.put(new Locale("zh", "TW", "").toString(),
				"lang/calendar-zh_TW-utf8.js");
	}

	static
	{
		InputStream resourceAsStream = null;
		try
		{
			resourceAsStream = DatePickerSettings.class
					.getResourceAsStream("dateformats.properties");
			dateformats.load(resourceAsStream);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				if (resourceAsStream != null)
				{
					resourceAsStream.close();
				}
			}
			catch (IOException ex)
			{
				// ignore
			}
		}
	}

	/**
	 * Gets the language.
	 * 
	 * @param currentLocale
	 *            the current locale
	 * @return language
	 */
	public static ResourceReference getLanguageFromMap(Locale currentLocale)
	{
		// try to get the reference from our default mapping
		// first try the language and country
		String ref = (String)localeToLanguageReference.get(currentLocale.toString());
		if (ref != null)
		{
			return new ResourceReference(DatePickerSettings.class, ref);
		}
		// now try only the language
		ref = (String)localeToLanguageReference.get(currentLocale.getLanguage());
		if (ref != null)
		{
			return new ResourceReference(DatePickerSettings.class, ref);
		}

		// we didn't find a mapping; just return English
		return new ResourceReference(DatePickerSettings.class, "lang/calendar-en.js");
	}

	/**
	 * Alignment of the calendar, relative to the reference element. The
	 * reference element is dynamically chosen like this: if a displayArea is
	 * specified then it will be the reference element. Otherwise, the input
	 * field is the reference element.
	 * <p>
	 * Align may contain one or two characters. The first character dictates the
	 * vertical alignment, relative to the element, and the second character
	 * dictates the horizontal alignment. If the second character is missing it
	 * will be assumed "l" (the left margin of the calendar will be at the same
	 * horizontal position as the left margin of the element). The characters
	 * given for the align parameters are case sensitive. This function only
	 * makes sense when the calendar is in popup mode. After computing the
	 * position it uses Calendar.showAt to display the calendar there.
	 * </p>
	 * <p>
	 * <strong>Vertical alignment</strong> The first character in ``align'' can
	 * take one of the following values:
	 * <ul>
	 * <li>T -- completely above the reference element (bottom margin of the
	 * calendar aligned to the top margin of the element). </li>
	 * <li>t -- above the element but may overlap it (bottom margin of the
	 * calendar aligned to the bottom margin of the element). </li>
	 * <li>c -- the calendar displays vertically centered to the reference
	 * element. It might overlap it (that depends on the horizontal alignment).
	 * </li>
	 * <li>b -- below the element but may overlap it (top margin of the
	 * calendar aligned to the top margin of the element). </li>
	 * <li>B -- completely below the element (top margin of the calendar
	 * aligned to the bottom margin of the element). </li>
	 * </ul>
	 * </p>
	 * <p>
	 * <strong>Horizontal alignment</strong> The second character in ``align''
	 * can take one of the following values:
	 * <ul>
	 * <li>L -- completely to the left of the reference element (right margin
	 * of the calendar aligned to the left margin of the element). </li>
	 * <li>l -- to the left of the element but may overlap it (left margin of
	 * the calendar aligned to the left margin of the element). </li>
	 * <li>c -- horizontally centered to the element. Might overlap it,
	 * depending on the vertical alignment. </li>
	 * <li>r -- to the right of the element but may overlap it (right margin of
	 * the calendar aligned to the right margin of the element). </li>
	 * <li>R -- completely to the right of the element (left margin of the
	 * calendar aligned to the right margin of the element). </li>
	 * </ul>
	 * </p>
	 */
	private String align = null;

	/**
	 * Set this to ``false'' if you want the calendar to update the field only
	 * when closed (by default it updates the field at each date change, even if
	 * the calendar is not closed).
	 */
	private boolean electric = true;

	/**
	 * Specifies which day is to be displayed as the first day of week. Possible
	 * values are 0 to 6; 0 means Sunday, 1 means Monday, ..., 6 means Saturday.
	 * The end user can easily change this too, by clicking on the day name in
	 * the calendar header.
	 */
	private int firstDay = -1;

	/** the button icon. */
	private ResourceReference icon = null;

	/**
	 * The format string that will be used to enter the date in the input field.
	 * This format will be honored even if the input field is hidden. Use
	 * Javascript notation, like '%m/%d/%Y'.
	 */
	private String ifFormat = null;

	/** the language. */
	private ResourceReference language = null;

	/**
	 * Wether the calendar is in ``single-click mode'' or ``double-click mode''.
	 * If true (the default) the calendar will be created in single-click mode.
	 */
	private boolean mode = true;

	/**
	 * If set to ``true'' then days belonging to months overlapping with the
	 * currently displayed month will also be displayed in the calendar (but in
	 * a ``faded-out'' color).
	 */
	private boolean showOthers = false;

	/**
	 * If this is set to true then the calendar will also allow time selection.
	 */
	private boolean showsTime = false;

	/** the style. */
	private ResourceReference style = null;

	/**
	 * Set this to ``12'' or ``24'' to configure the way that the calendar will
	 * display time.
	 */
	private String timeFormat = null;

	/**
	 * If ``true'' then the calendar will display week numbers.
	 */
	private boolean weekNumbers = true;

	/**
	 * Construct.
	 */
	public DatePickerSettings()
	{
	}

	/**
	 * Gets the align.
	 * 
	 * @return align
	 */
	public String getAlign()
	{
		return align;
	}

	/**
	 * Gets the firstDay.
	 * 
	 * @return firstDay
	 */
	public int getFirstDay()
	{
		return firstDay;
	}

	/**
	 * Gets the icon.
	 * 
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
	 * Gets the format string that will be used to enter the date in the input
	 * field based on the provided locale. Should return Javascript notation,
	 * like '%m/%d/%Y'.
	 * 
	 * @param locale
	 *            The locale
	 * @return The date format
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
		return dateformats.getProperty(locale.toString());
	}

	/**
	 * Gets the language.
	 * 
	 * @param currentLocale
	 *            the current locale
	 * @return language
	 */
	public ResourceReference getLanguage(Locale currentLocale)
	{
		// if the language was set explicitly, return that
		if (language != null)
		{
			return language;
		}
		return getLanguageFromMap(currentLocale);
	}

	/**
	 * Gets the style.
	 * 
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
	 * Gets the timeFormat.
	 * 
	 * @return timeFormat
	 */
	public String getTimeFormat()
	{
		return timeFormat;
	}

	/**
	 * Gets the electric.
	 * 
	 * @return electric
	 */
	public boolean isElectric()
	{
		return electric;
	}

	/**
	 * Gets the mode.
	 * 
	 * @return mode
	 */
	public boolean isMode()
	{
		return mode;
	}

	/**
	 * Gets the showOthers.
	 * 
	 * @return showOthers
	 */
	public boolean isShowOthers()
	{
		return showOthers;
	}

	/**
	 * Gets the showsTime.
	 * 
	 * @return showsTime
	 */
	public boolean isShowsTime()
	{
		return showsTime;
	}

	/**
	 * Gets the weekNumbers.
	 * 
	 * @return weekNumbers
	 */
	public boolean isWeekNumbers()
	{
		return weekNumbers;
	}

	/**
	 * create a button icon.
	 * 
	 * @return a button icon.
	 */
	public final ResourceReference newButtonIconBlue()
	{
		return new ResourceReference(DatePickerSettings.class, "calendar_icon_3.gif");
	}

	/**
	 * create a button icon.
	 * 
	 * @return a button icon.
	 */
	public final ResourceReference newButtonIconPlain()
	{
		return new ResourceReference(DatePickerSettings.class, "calendar_icon_2.gif");
	}

	/**
	 * create a button icon.
	 * 
	 * @return a button icon.
	 */
	public final ResourceReference newButtonIconRed()
	{
		return new ResourceReference(DatePickerSettings.class, "calendar_icon_1.gif");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final ResourceReference newStyleAqua()
	{
		return new ResourceReference(DatePickerSettings.class, "style/aqua/theme.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final ResourceReference newStyleBlue()
	{
		return new ResourceReference(DatePickerSettings.class, "style/calendar-blue2.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final ResourceReference newStyleGreen()
	{
		return new ResourceReference(DatePickerSettings.class, "style/calendar-green.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final ResourceReference newStyleSummer()
	{
		return new ResourceReference(DatePickerSettings.class, "style/calendar-brown.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final ResourceReference newStyleSystem()
	{
		return new ResourceReference(DatePickerSettings.class, "style/calendar-system.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final ResourceReference newStyleTas()
	{
		return new ResourceReference(DatePickerSettings.class, "style/calendar-tas.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final ResourceReference newStyleWin2k1()
	{
		return new ResourceReference(DatePickerSettings.class, "style/calendar-win2k-1.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final ResourceReference newStyleWin2k2()
	{
		return new ResourceReference(DatePickerSettings.class, "style/calendar-win2k-2.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final ResourceReference newStyleWin2kCold1()
	{
		return new ResourceReference(DatePickerSettings.class, "style/calendar-win2k-cold-1.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final ResourceReference newStyleWin2kCold2()
	{
		return new ResourceReference(DatePickerSettings.class, "style/calendar-win2k-cold-2.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final ResourceReference newStyleWinter()
	{
		return new ResourceReference(DatePickerSettings.class, "style/calendar-blue.css");
	}

	/**
	 * Sets the align.
	 * 
	 * @param align
	 *            align
	 */
	public void setAlign(String align)
	{
		this.align = align;
	}

	/**
	 * Sets the electric.
	 * 
	 * @param electric
	 *            electric
	 */
	public void setElectric(boolean electric)
	{
		this.electric = electric;
	}

	/**
	 * Sets the firstDay.
	 * 
	 * @param firstDay
	 *            firstDay
	 */
	public void setFirstDay(int firstDay)
	{
		this.firstDay = firstDay;
	}

	/**
	 * Sets the icon.
	 * 
	 * @param icon
	 *            icon
	 */
	public void setIcon(ResourceReference icon)
	{
		this.icon = icon;
	}

	/**
	 * Sets the format string that will be used to enter the date in the input
	 * field. This format will be honored even if the input field is hidden. Use
	 * Javascript notation, like '%m/%d/%Y'.
	 * <p>
	 * Note: setting this field to a non-null value, overrides the lookup using
	 * dateformats.properties. To remove the override, pass null.
	 * </p>
	 * 
	 * @param ifFormat
	 *            the data format
	 * 
	 * @deprecated The format is extracted from the java datefomatter format
	 *             string
	 */
	@Deprecated
	public void setIfFormat(String ifFormat)
	{
		this.ifFormat = ifFormat;
	}

	/**
	 * Sets the language.
	 * 
	 * @param language
	 *            language
	 */
	public void setLanguage(ResourceReference language)
	{
		this.language = language;
	}

	/**
	 * Sets the mode.
	 * 
	 * @param mode
	 *            mode
	 */
	public void setMode(boolean mode)
	{
		this.mode = mode;
	}

	/**
	 * Sets the showOthers.
	 * 
	 * @param showOthers
	 *            showOthers
	 */
	public void setShowOthers(boolean showOthers)
	{
		this.showOthers = showOthers;
	}

	/**
	 * Sets the showsTime.
	 * 
	 * @param showsTime
	 *            showsTime
	 * 
	 * @deprecated The format is extracted from the java datefomatter format
	 *             string
	 */
	@Deprecated
	public void setShowsTime(boolean showsTime)
	{
		this.showsTime = showsTime;
	}

	/**
	 * Sets the style.
	 * 
	 * @param style
	 *            style
	 */
	public void setStyle(ResourceReference style)
	{
		this.style = style;
	}

	/**
	 * Sets the timeFormat.
	 * 
	 * @param timeFormat
	 *            timeFormat
	 * 
	 * @deprecated The format is extracted from the java datefomatter format
	 *             string
	 */
	@Deprecated
	public void setTimeFormat(String timeFormat)
	{
		this.timeFormat = timeFormat;
	}

	/**
	 * Sets the weekNumbers.
	 * 
	 * @param weekNumbers
	 *            weekNumbers
	 */
	public void setWeekNumbers(boolean weekNumbers)
	{
		this.weekNumbers = weekNumbers;
	}

	/**
	 * Return the properties as a script.
	 * 
	 * @param locale
	 *            the current locale
	 * @param format
	 * @return the properties as a script
	 */
	public String toScript(Locale locale, String format)
	{
		if (format != null)
		{
			boolean showTime = false;
			String timeFormat = "24";
			char prev = 0;
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < format.length(); i++)
			{
				char ch = format.charAt(i);
				if (ch == 'd')
				{
					if (prev != 'd')
					{
						sb.append("%d");
					}
					prev = ch;
				}
				else if (ch == 'M')
				{
					if (prev != 'M')
					{
						sb.append("%m");
					}
					prev = ch;
				}
				else if (ch == 'y')
				{
					if (prev != 'y')
					{
						sb.append("%Y");
					}
					prev = ch;
				}
				else if (ch == 'H')
				{
					showTime = true;
					if (prev != 'H')
					{
						sb.append("%H");
					}
					prev = ch;
				}
				else if (ch == 'h')
				{
					timeFormat = "12";
					showTime = true;
					if (prev != 'h')
					{
						sb.append("%I");
					}
					prev = ch;
				}
				else if (ch == 'm')
				{
					showTime = true;
					if (prev != 'm')
					{
						sb.append("%M");
					}
					prev = ch;
				}
				else if (ch == 's')
				{
					showTime = true;
					if (prev != 's')
					{
						sb.append("%S");
					}
					prev = ch;
				}
				else if (ch == 'a')
				{
					if (prev != 'a')
					{
						sb.append("%P");
					}
					prev = ch;
				}
				else if ("GwWDFEkKSzZ".indexOf(ch) != -1)
				{
					prev = 0;
				}
				else if (prev != 0)
				{
					sb.append(ch);
				}
			}
			setIfFormat(sb.toString().trim());
			setShowsTime(showTime);
			setTimeFormat(timeFormat);
		}

		StringBuffer b = new StringBuffer();
		// create the script that represents these properties. Only create
		// entries for
		// values that are different from the default value (save a bit
		// bandwith)

		if (!isMode())
		{
			b.append("\n\tmode : false,");
		}

		if (getFirstDay() != -1)
		{
			b.append("\n\tfirstDay : ").append(getFirstDay()).append(",");
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
			b.append("\n\ttimeFormat : ").append(getTimeFormat()).append(",");
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
}