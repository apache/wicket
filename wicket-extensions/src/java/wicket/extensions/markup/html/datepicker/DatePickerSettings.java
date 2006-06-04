/*
 * $Id: Add.java 5883 2006-05-26 10:12:48Z joco01 $ $Revision: 5883 $ $Date:
 * 2006-05-26 00:46:21 +0200 (vr, 26 mei 2006) $
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
package wicket.extensions.markup.html.datepicker;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.ResourceReference;
import wicket.markup.html.PackageResourceReference;

/**
 * The settings of the date picker component. Use this to customize the
 * datepicker (e.g. the icon, locale, format, etc).
 * 
 * @author Eelco Hillenius
 */
public class DatePickerSettings implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** log. */
	private static Log log = LogFactory.getLog(DatePickerSettings.class);

	/** all date formats. */
	private static Properties dateformats = new Properties();
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
	 * The format string that will be used to enter the date in the input field.
	 * This format will be honored even if the input field is hidden. Use
	 * Javascript notation, like '%m/%d/%Y'.
	 */
	private String ifFormat = null;

	/**
	 * Wether the calendar is in ``single-click mode'' or ``double-click mode''.
	 * If true (the default) the calendar will be created in single-click mode.
	 */
	private boolean mode = true;

	/**
	 * Specifies which day is to be displayed as the first day of week. Possible
	 * values are 0 to 6; 0 means Sunday, 1 means Monday, ..., 6 means Saturday.
	 * The end user can easily change this too, by clicking on the day name in
	 * the calendar header.
	 */
	private int firstDay = -1;

	/**
	 * If ``true'' then the calendar will display week numbers.
	 */
	private boolean weekNumbers = true;

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
	 * If this is set to true then the calendar will also allow time selection.
	 */
	private boolean showsTime = false;

	/**
	 * Set this to ``12'' or ``24'' to configure the way that the calendar will
	 * display time.
	 */
	private String timeFormat = null;

	/**
	 * Set this to ``false'' if you want the calendar to update the field only
	 * when closed (by default it updates the field at each date change, even if
	 * the calendar is not closed).
	 */
	private boolean electric = true;

	/**
	 * If set to ``true'' then days belonging to months overlapping with the
	 * currently displayed month will also be displayed in the calendar (but in
	 * a ``faded-out'' color).
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

	/**
	 * create a button icon.
	 * 
	 * @return a button icon.
	 */
	public final PackageResourceReference newButtonIconRed()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"calendar_icon_1.gif");
	}

	/**
	 * create a button icon.
	 * 
	 * @return a button icon.
	 */
	public final PackageResourceReference newButtonIconPlain()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"calendar_icon_2.gif");
	}

	/**
	 * create a button icon.
	 * 
	 * @return a button icon.
	 */
	public final PackageResourceReference newButtonIconBlue()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"calendar_icon_3.gif");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final PackageResourceReference newStyleAqua()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"style/aqua/theme.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final PackageResourceReference newStyleWinter()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"style/calendar-blue.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final PackageResourceReference newStyleBlue()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"style/calendar-blue2.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final PackageResourceReference newStyleSummer()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"style/calendar-brown.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final PackageResourceReference newStyleGreen()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"style/calendar-green.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final PackageResourceReference newStyleSystem()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"style/calendar-system.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final PackageResourceReference newStyleTas()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"style/calendar-tas.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final PackageResourceReference newStyleWin2k1()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"style/calendar-win2k-1.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final PackageResourceReference newStyleWin2k2()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"style/calendar-win2k-2.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final PackageResourceReference newStyleWin2kCold1()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"style/calendar-win2k-cold-1.css");
	}

	/**
	 * Create a style
	 * 
	 * @return a style
	 */
	public final PackageResourceReference newStyleWin2kCold2()
	{
		return new PackageResourceReference(Application.get(), DatePickerSettings.class,
				"style/calendar-win2k-cold-2.css");
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
	 * Gets the electric.
	 * 
	 * @return electric
	 */
	public boolean isElectric()
	{
		return electric;
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
	 * Gets the firstDay.
	 * 
	 * @return firstDay
	 */
	public int getFirstDay()
	{
		return firstDay;
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
	 * Gets the mode.
	 * 
	 * @return mode
	 */
	public boolean isMode()
	{
		return mode;
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
	 * Gets the showOthers.
	 * 
	 * @return showOthers
	 */
	public boolean isShowOthers()
	{
		return showOthers;
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
	 * Gets the showsTime.
	 * 
	 * @return showsTime
	 */
	public boolean isShowsTime()
	{
		return showsTime;
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
	 * Gets the timeFormat.
	 * 
	 * @return timeFormat
	 */
	public String getTimeFormat()
	{
		return timeFormat;
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
	 * Gets the weekNumbers.
	 * 
	 * @return weekNumbers
	 */
	public boolean isWeekNumbers()
	{
		return weekNumbers;
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
		return DatePickerComponentInitializer.getLanguage(currentLocale);
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
	 * Sets the style.
	 * 
	 * @param style
	 *            style
	 */
	public void setStyle(ResourceReference style)
	{
		this.style = style;
	}
}