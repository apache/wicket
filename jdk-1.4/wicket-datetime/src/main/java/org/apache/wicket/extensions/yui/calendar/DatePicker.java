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
package org.apache.wicket.extensions.yui.calendar;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.YuiLib;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converters.DateConverter;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.template.PackagedTextTemplate;
import org.apache.wicket.util.template.TextTemplate;
import org.joda.time.DateTime;


/**
 * Pops up a YUI calendar component so that the user can select a date. On
 * selection, the date is set in the component it is coupled to, after which the
 * popup is closed again. This behavior can only be used with components that
 * either implement {@link ITextFormatProvider} or that use
 * {@link DateConverter} configured with an instance of {@link SimpleDateFormat}
 * (like Wicket's default configuration has).
 * 
 * To use, simply add a new instance to your component, which would typically a
 * TextField, like {@link DateTextField}.
 * 
 * @author eelcohillenius
 */
public class DatePicker extends AbstractBehavior implements IHeaderContributor
{
	/**
	 * Exception thrown when the bound component does not produce a format this
	 * date picker can work with.
	 */
	private static final class UnableToDetermineFormatException extends WicketRuntimeException
	{
		private static final long serialVersionUID = 1L;

		public UnableToDetermineFormatException()
		{
			super("This behavior can only be added to components that either implement "
					+ ITextFormatProvider.class.getName()
					+ " AND produce a non-null format, or that use"
					+ " converters that this datepicker can use to determine"
					+ " the pattern being used. Alternatively, you can extend "
					+ " the date picker and override getDatePattern to provide your own");
		}
	}

	/**
	 * Format to be used when configuring YUI calendar. Can be used when using
	 * the &quot;selected&quot; property.
	 */
	public static final DateFormat FORMAT_DATE = new SimpleDateFormat("MM/dd/yyyy");

	/**
	 * For specifying which page (month/year) to show in the calendar, use this
	 * format for the date. This is to be used together with the property
	 * &quot;pagedate&quot;
	 */
	public static final DateFormat FORMAT_PAGEDATE = new SimpleDateFormat("MM/yyyy");

	private static final long serialVersionUID = 1L;

	/** The target component. */
	private Component component;

	/**
	 * Construct.
	 */
	public DatePicker()
	{
	}

	/**
	 * @see org.apache.wicket.behavior.AbstractBehavior#bind(org.apache.wicket.Component)
	 */
	public void bind(Component component)
	{
		this.component = component;
		checkComponentProvidesDateFormat(component);
		component.setOutputMarkupId(true);
	}

	/**
	 * @see org.apache.wicket.behavior.AbstractBehavior#onRendered(org.apache.wicket.Component)
	 */
	public void onRendered(Component component)
	{
		super.onRendered(component);
		// Append the span and img icon right after the rendering of the
		// component. Not as pretty as working with a panel etc, but works
		// for behaviors and is more efficient
		Response response = component.getResponse();
		response.write("\n<span class=\"yui-skin-sam\">&nbsp;<span style=\"");
		if (renderOnLoad())
		{
			response.write("display:block;");
		}
		else
		{
			response.write("display:none;");
			response.write("position:absolute;");
		}
		response.write("z-index: 99999;\" id=\"");
		response.write(getEscapedComponentMarkupId());
		response.write("Dp\"></span><img style=\"");
		response.write(getIconStyle());
		response.write("\" id=\"");
		response.write(getIconId());
		response.write("\" src=\"");
		CharSequence iconUrl = getIconUrl();
		response.write(Strings.escapeMarkup(iconUrl != null ? iconUrl.toString() : ""));
		response.write("\" />");
		if (renderOnLoad())
		{
			response.write("<br style=\"clear:left;\"/>");
		}
		response.write("</span>");
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderContributor#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
	 */
	public void renderHead(IHeaderResponse response)
	{
		YuiLib.load(response);
		if (enableMonthYearSelection())
		{
			response.renderCSSReference(new ResourceReference(YuiLib.class,
					"calendar/assets/wicket-calendar.css"));
			String idSelector = "#" + getEscapedComponentMarkupId() + "DpJs";
			new StringHeaderContributor("<style>" + idSelector + ".yui-calendar .calnavleft, "
					+ idSelector + ".yui-calendar .calnavright {display: none;}</style>")
					.renderHead(response);
		}

		// variables for the initialization script
		Map variables = new HashMap();
		String widgetId = getEscapedComponentMarkupId();
		variables.put("componentId", getComponentMarkupId());
		variables.put("widgetId", widgetId);
		variables.put("datePattern", getDatePattern());
		variables.put("fireChangeEvent", Boolean.valueOf(notifyComponentOnDateSelected()));
		variables.put("alignWithIcon", Boolean.valueOf(alignWithIcon()));
		// variables for YUILoader
		variables.put("pathToWicketDate", RequestCycle.get().urlFor(
				new JavascriptResourceReference(DatePicker.class, "wicket-date.js")));
		variables.put("basePath", RequestCycle.get().urlFor(
				new JavascriptResourceReference(YuiLib.class, "")));
		variables.put("enableMonthYearSelection", Boolean.valueOf(enableMonthYearSelection()));
		variables.put("hideOnSelect", Boolean.valueOf(hideOnSelect()));
		String script = getAdditionalJavascript();
		if (script != null)
		{
			variables.put("additionalJavascript", Strings.replaceAll(script, "${calendar}",
					"YAHOO.wicket." + widgetId + "DpJs"));
		}
		// print out the initialization properties
		Properties p = new Properties();
		configure(p);

		if (enableMonthYearSelection() && p.containsKey("pages")
				&& Objects.longValue(p.get("pages")) > 1)
		{
			throw new IllegalStateException(
					"You cannot use a CalendarGroup with month/year selection!");
		}

		// ${calendarInit}
		StringBuffer calendarInit = new StringBuffer();
		for (Iterator i = p.entrySet().iterator(); i.hasNext();)
		{
			Entry entry = (Entry)i.next();
			calendarInit.append(entry.getKey());
			Object value = entry.getValue();
			if (value instanceof CharSequence)
			{
				calendarInit.append(":\"");
				calendarInit.append(Strings.toEscapedUnicode(value.toString()));
				calendarInit.append("\"");
			}
			else if (value instanceof CharSequence[])
			{
				calendarInit.append(":[");
				CharSequence[] valueArray = (CharSequence[])value;
				for (int j = 0; j < valueArray.length; j++)
				{
					CharSequence tmpValue = valueArray[j];
					if (j > 0)
					{
						calendarInit.append(",");
					}
					if (tmpValue != null)
					{
						calendarInit.append("\"");
						calendarInit.append(Strings.toEscapedUnicode(tmpValue.toString()));
						calendarInit.append("\"");
					}
				}
				calendarInit.append("]");
			}
			else
			{
				calendarInit.append(":");
				calendarInit.append(Strings.toEscapedUnicode(String.valueOf(value)));
			}
			if (i.hasNext())
			{
				calendarInit.append(",");
			}
		}
		variables.put("calendarInit", calendarInit.toString());

		// render initialization script with the variables interpolated
		TextTemplate datePickerJs = new PackagedTextTemplate(DatePicker.class, "DatePicker.js");
		datePickerJs.interpolate(variables);
		response.renderOnDomReadyJavascript(datePickerJs.asString());
	}

	/**
	 * Check that this behavior can get a date format out of the component it is
	 * coupled to. It checks whether {@link #getDatePattern()} produces a
	 * non-null value. If that method returns null, and exception will be thrown
	 * 
	 * @param component
	 *            the component this behavior is being coupled to
	 * @throws UnableToDetermineFormatException
	 *             if this date picker is unable to determine a format.
	 */
	private final void checkComponentProvidesDateFormat(Component component)
	{
		if (getDatePattern() == null)
		{
			throw new UnableToDetermineFormatException();
		}
	}

	/**
	 * Set widget property if the array is null and has a length greater than 0.
	 * 
	 * @param widgetProperties
	 * @param key
	 * @param array
	 */
	private void setWidgetProperty(Map widgetProperties, String key, String[] array)
	{
		if (array != null && array.length > 0)
		{
			widgetProperties.put(key, array);
		}
	}

	/**
	 * Whether to position the date picker relative to the trigger icon.
	 * 
	 * @return If true, the date picker is aligned with the left position of the
	 *         icon, and with the top right under. If false, the date picker
	 *         will skip positioning and will let you do the positioning
	 *         yourself. Returns true by default.
	 */
	protected boolean alignWithIcon()
	{
		return true;
	}

	/**
	 * Append javascript to the initialization function for the YUI widget. Can
	 * be used by subclasses to conveniently extend configuration without having
	 * to write a separate contribution.
	 * 
	 * @param markupId
	 *            The markup id of the calendar component
	 * @param javascriptId
	 *            the non-name spaced javascript id of the widget
	 * @param javascriptWidgetId
	 *            the name space id of the widget
	 * @param b
	 *            the buffer to append the script to
	 */
	protected void appendToInit(String markupId, String javascriptId, String javascriptWidgetId,
			StringBuffer b)
	{
	}

	/**
	 * Gives overriding classes the option of adding (or even changing/
	 * removing) configuration properties for the javascript widget. See <a
	 * href="http://developer.yahoo.com/yui/calendar/">the widget's
	 * documentation</a> for the available options. If you want to override/
	 * remove properties, you should call
	 * {@link super#setWidgetProperties(Properties)} first. If you don't call
	 * that, be aware that you will have to call {@link #localize(Map)} manually
	 * if you like localized strings to be added.
	 * 
	 * @param widgetProperties
	 *            the current widget properties
	 */
	protected void configure(Map widgetProperties)
	{
		widgetProperties.put("close", Boolean.TRUE);
		widgetProperties.put("title", "&nbsp;");
		// TODO we might want to localize the title nicer in the future, but for
		// now, people can override this method or put "title" in the map in
		// localize.

		// localize date fields
		localize(widgetProperties);

		Object modelObject = component.getModelObject();
		// null and cast check
		if (modelObject instanceof Date)
		{
			Date date = (Date)modelObject;
			widgetProperties.put("selected", FORMAT_DATE.format(date));
			widgetProperties.put("pagedate", FORMAT_PAGEDATE.format(date));
		}
	}

	/**
	 * @deprecated Please use {@link #configure(Map)} instead.
	 */
	// TODO remove this very ugly named method
	protected final void configureWidgetProperties(Map widgetProperties)
	{
		throw new UnsupportedOperationException("");
	}

	/**
	 * Filter all empty elements (workaround for {@link DateFormatSymbols}
	 * returning arrays with empty elements).
	 * 
	 * @param array
	 *            array to filter
	 * @return filtered array (without null or empty string elements)
	 */
	protected final String[] filterEmpty(String[] array)
	{
		if (array == null)
		{
			return null;
		}
		List l = new ArrayList(array.length);
		for (int i = 0; i < array.length; i++)
		{
			if (!Strings.isEmpty(array[i]))
			{
				l.add(array[i]);
			}
		}
		return (String[])l.toArray(new String[l.size()]);
	}

	/**
	 * Gets the id of the component that the calendar widget will get attached
	 * to.
	 * 
	 * @return The DOM id of the component
	 */
	protected final String getComponentMarkupId()
	{
		return component.getMarkupId();
	}


	/**
	 * @return if true, the base path for all YUI components will be set to
	 *         /resources/org.apache.wicket.extensions.yui.YuiLib/. True by
	 *         default.
	 */
	protected boolean getConfigureYUIBasePath()
	{
		return true;
	}

	/**
	 * Gets the date pattern to use for putting selected values in the coupled
	 * component.
	 * 
	 * @return The date pattern
	 */
	protected String getDatePattern()
	{
		String format = null;
		if (component instanceof ITextFormatProvider)
		{
			format = ((ITextFormatProvider)component).getTextFormat();
			// it is possible that components implement ITextFormatProvider but
			// don't provide a format
		}

		if (format == null)
		{
			IConverter converter = component.getConverter(DateTime.class);
			if (!(converter instanceof DateConverter))
			{
				converter = component.getConverter(Date.class);
			}
			format = ((SimpleDateFormat)((DateConverter)converter).getDateFormat(component
					.getLocale())).toPattern();
		}

		return format;
	}

	/**
	 * Gets the escaped DOM id that the calendar widget will get attached to.
	 * All non word characters (\W) will be removed from the string.
	 * 
	 * @return The DOM id of the calendar widget - same as the component's
	 *         markup id + 'Dp'}
	 */
	protected final String getEscapedComponentMarkupId()
	{
		return component.getMarkupId().replaceAll("\\W", "");
	}

	/**
	 * Gets the id of the icon that triggers the popup.
	 * 
	 * @return The id of the icon
	 */
	protected final String getIconId()
	{
		return getEscapedComponentMarkupId() + "Icon";
	}

	/**
	 * Gets the style of the icon that triggers the popup.
	 * 
	 * @return The style of the icon, e.g. 'cursor: point' etc.
	 */
	protected String getIconStyle()
	{
		return "cursor: pointer; border: none;";
	}

	/**
	 * Gets the url for the popup button. Users can override to provide their
	 * own icon URL.
	 * 
	 * @return the url to use for the popup button/ icon
	 */
	protected CharSequence getIconUrl()
	{
		return RequestCycle.get().urlFor(new ResourceReference(DatePicker.class, "icon1.gif"));
	}

	/**
	 * Gets the locale that should be used to configure this widget.
	 * 
	 * @return By default the locale of the bound component.
	 */
	protected Locale getLocale()
	{
		return component.getLocale();
	}

	/**
	 * Configure the localized strings for the datepicker widget. This
	 * implementation uses {@link DateFormatSymbols} and some slight string
	 * manupilation to get the strings for months and week days. It should work
	 * well for most locales.
	 * <p>
	 * This method is called from {@link #configureWidgetProperties(Map)} and
	 * can be overriden if you want to customize setting up the localized
	 * strings but are happy with the rest of
	 * {@link #configureWidgetProperties(Map)}'s behavior. Note that you can
	 * call (overridable) method {@link #getLocale()} to get the locale that
	 * should be used for setting up the widget.
	 * </p>
	 * <p>
	 * See YUI Calendar's <a
	 * href="http://developer.yahoo.com/yui/examples/calendar/germany/1.html">
	 * German</a> and <a
	 * href="http://developer.yahoo.com/yui/examples/calendar/japan/1.html">Japanese</a>
	 * examples for more info.
	 * </p>
	 * 
	 * @param widgetProperties
	 *            the current widget properties
	 */
	protected void localize(Map widgetProperties)
	{
		DateFormatSymbols dfSymbols = new DateFormatSymbols(getLocale());
		setWidgetProperty(widgetProperties, "MONTHS_SHORT", filterEmpty(dfSymbols.getShortMonths()));
		setWidgetProperty(widgetProperties, "MONTHS_LONG", filterEmpty(dfSymbols.getMonths()));
		setWidgetProperty(widgetProperties, "WEEKDAYS_1CHAR", filterEmpty(substring(dfSymbols
				.getShortWeekdays(), 1)));
		setWidgetProperty(widgetProperties, "WEEKDAYS_SHORT", filterEmpty(substring(dfSymbols
				.getShortWeekdays(), 2)));
		setWidgetProperty(widgetProperties, "WEEKDAYS_MEDIUM", filterEmpty(dfSymbols
				.getShortWeekdays()));
		setWidgetProperty(widgetProperties, "WEEKDAYS_LONG", filterEmpty(dfSymbols.getWeekdays()));
	}

	/**
	 * Whether to notify the associated component when a date is selected.
	 * Notifying is done by calling the associated component's onchange
	 * Javascript event handler. You can for instance attach an
	 * {@link AjaxEventBehavior} to that component to get a call back to the
	 * server. The default is true.
	 * 
	 * @return if true, notifies the associated component when a date is
	 *         selected
	 */
	protected boolean notifyComponentOnDateSelected()
	{
		return true;
	}

	/**
	 * Makes a copy of the provided array and for each element copy the
	 * substring 0..len to the new array
	 * 
	 * @param array
	 *            array to copy from
	 * @param len
	 *            size of substring for each element to copy
	 * @return copy of the array filled with substrings.
	 */
	protected final String[] substring(String[] array, int len)
	{
		if (array != null)
		{
			String[] copy = new String[array.length];
			for (int i = 0; i < array.length; i++)
			{
				String el = array[i];
				if (el != null)
				{
					if (el.length() > len)
					{
						copy[i] = el.substring(0, len);
					}
					else
					{
						copy[i] = el;
					}
				}
			}
			return copy;
		}
		return null;
	}

	/**
	 * Indicates whether plain text is rendered or two select boxes are used to
	 * allow direct selection of month and year.
	 * 
	 * @return <code>true</code> if select boxes should be rendered to allow
	 *         month and year selection.<br/><code>false</code> to render
	 *         just plain text.
	 */
	protected boolean enableMonthYearSelection()
	{
		return false;
	}

	/**
	 * Indicates whether the calendar should be hidden after a date was
	 * selected.
	 * 
	 * @return <code>true</code> (default) if the calendar should be hidden
	 *         after the date selection <br/><code>false</code> if the
	 *         calendar should remain visible after the date selection.
	 */
	protected boolean hideOnSelect()
	{
		return true;
	}

	/**
	 * Indicates whether the calendar should be rendered after it has been
	 * loaded.
	 * 
	 * @return <code>true</code> if the calendar should be rendered after it
	 *         has been loaded.<br/><code>false</code> (default) if it's
	 *         initially hidden.
	 */
	protected boolean renderOnLoad()
	{
		return false;
	}

	/**
	 * Override this method to further customize the YUI Calendar with
	 * additional Javascript code. The code returned by this method is executed
	 * right after the Calendar has been constructed and initialized. To refer
	 * to the actual Calendar DOM object, use <code>${calendar}</code> in your
	 * code.<br/>See <a href="http://developer.yahoo.com/yui/calendar/">the
	 * widget's documentation</a> for more information about the YUI Calendar.<br/>
	 * Example:
	 * 
	 * <pre>
	 * protected String getAdditionalJavascript()
	 * {
	 * 	return &quot;${calendar}.addRenderer(\&quot;10/3\&quot;, ${calendar}.renderCellStyleHighlight1);&quot;;
	 * }
	 * </pre>
	 * 
	 * @return a String containing additional Javascript code
	 * 
	 */
	protected String getAdditionalJavascript()
	{
		return "";
	}
}
