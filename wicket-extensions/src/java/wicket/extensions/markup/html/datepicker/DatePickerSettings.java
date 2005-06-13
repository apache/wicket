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

import java.io.Serializable;
import java.util.Locale;

import wicket.ResourceReference;
import wicket.Session;

/**
 * The settings of the date picker component. Use this to customize the datepicker
 * (e.g. the icon, locale, format, etc).
 *
 * @author Eelco Hillenius
 */
public class DatePickerSettings implements Serializable
{
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
	private ResourceReference style = DatePicker.STYLE_AQUA;

	/** the button icon. */
	private ResourceReference icon = DatePicker.BUTTON_ICON_1;

	/** the language. */
	private ResourceReference language = DatePicker.LANGUAGE_EN;

	/**
	 * Construct.
	 */
	public DatePickerSettings()
	{
	}

	/**
	 * Return the properties as a script.
	 * @return the properties as a script
	 */
	public String toScript()
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
		String ifFormat = getIfFormat();
		// if null, try some heuristics
		if (ifFormat == null)
		{
			// get the short date format object for the current locale
			ifFormat = getDatePattern();
		}
		b.append("\n\t\tifFormat : \"").append(ifFormat).append("\"");
		
		return b.toString();
	}

	/**
	 * When property ifFormat is not set, this method is called to get the date pattern.
	 * This method gets the current locale, and returns the pattern based on that.
	 * <p>
	 * This locale/pattern map is by far complete. Override this method or set
	 * ifFormat if this doesnt work for you. Any contributions are welcome.
	 * </p>
	 * @return the pattern
	 */
	protected String getDatePattern()
	{
		// TODO this is a very shallow implementation; see if there is anything smarter
		// to do with the date pattern

		Locale locale = Session.get().getLocale();

		// now, just try a few that I know of

		if (Locale.GERMAN.equals(locale))
		{
			return "%d.%m.%Y";
		}

		if ("nl".equals(locale.getLanguage()))
		{
			return "%d-%m-%Y";
		}

		// return US pattern by default
		return "%Y/%m/%d";		
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
	 * @return ifFormat
	 */
	public String getIfFormat()
	{
		return ifFormat;
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
	 * @return language
	 */
	public ResourceReference getLanguage()
	{
		return language;
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