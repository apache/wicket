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

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.YuiLib;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.DateConverter;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;
import org.joda.time.DateTime;

/**
 * Pops up a YUI calendar component so that the user can select a date. On selection, the date is
 * set in the component it is coupled to, after which the popup is closed again. This behavior can
 * only be used with components that either implement {@link ITextFormatProvider} or that use
 * {@link DateConverter} configured with an instance of {@link SimpleDateFormat} (like Wicket's
 * default configuration has).<br/>
 * 
 * To use, simply add a new instance to your component, which would typically a TextField, like
 * {@link DateTextField}.<br/>
 * 
 * The CalendarNavigator can be configured by overriding {@link #configure(java.util.Map, org.apache.wicket.markup.head.IHeaderResponse, java.util.Map)} and setting the
 * property or by returning <code>true</code> for {@link #enableMonthYearSelection()}.
 * 
 * @see <a
 *      href="http://developer.yahoo.com/yui/calendar/">http://developer.yahoo.com/yui/calendar/</a>
 * 
 * @author eelcohillenius
 */
public class DatePicker extends Behavior
{

	/**
	 * Exception thrown when the bound component does not produce a format this date picker can work
	 * with.
	 */
	private static final class UnableToDetermineFormatException extends WicketRuntimeException
	{
		private static final long serialVersionUID = 1L;

		public UnableToDetermineFormatException()
		{
			super("This behavior can only be added to components that either implement " +
				ITextFormatProvider.class.getName() +
				" AND produce a non-null format, or that use" +
				" converters that this DatePicker can use to determine" +
				" the pattern being used. Alternatively, you can extend " +
				" the DatePicker and override getDatePattern to provide your own.");
		}
	}

	/**
	 * Format to be used when configuring YUI calendar. Can be used when using the
	 * &quot;selected&quot; property.
	 */
	// See wicket-1988: SimpleDateFormat is not thread safe. Do not use static final
	// See wicket-2525: SimpleDateFormat consumes a lot of memory
	public static String FORMAT_DATE = "MM/dd/yyyy";

	/**
	 * For specifying which page (month/year) to show in the calendar, use this format for the date.
	 * This is to be used together with the property &quot;pagedate&quot;
	 */
	// See wicket-1988: SimpleDateFormat is not thread safe. Do not use static final
	// See wicket-2525: SimpleDateFormat consumes a lot of memory
	public static String FORMAT_PAGEDATE = "MM/yyyy";

	private static final ResourceReference YUI = new JavaScriptResourceReference(YuiLib.class, "");

	private static final ResourceReference WICKET_DATE = new JavaScriptResourceReference(
		DatePicker.class, "wicket-date.js");

	private static final long serialVersionUID = 1L;

	/** The target component. */
	private Component component;

	private boolean showOnFieldClick = false;

	/**
	 * A setting that decides whether to close the date picker when the user clicks somewhere else
	 * on the document.
	 */
	private boolean autoHide = false;

	/**
	 * Construct.
	 */
	public DatePicker()
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void bind(final Component component)
	{
		this.component = component;
		checkComponentProvidesDateFormat(component);
		component.setOutputMarkupId(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterRender(final Component component)
	{
		super.afterRender(component);

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
		response.write("\" alt=\"");
		CharSequence alt = getIconAltText();
		response.write(Strings.escapeMarkup((alt != null) ? alt.toString() : ""));
		response.write("\" title=\"");
		CharSequence title = getIconTitle();
		response.write(Strings.escapeMarkup((title != null) ? title.toString() : ""));
		response.write("\"/>");

		if (renderOnLoad())
		{
			response.write("<br style=\"clear:left;\"/>");
		}
		response.write("</span>");
	}

	/**
	 * Controls whether or not datepicker will contribute YUI libraries to the page as part of its
	 * rendering lifecycle.
	 * 
	 * There may be cases when the user wants to use their own version of YUI contribution code, in
	 * those cases this method should be overridden to return <code>false</code>.
	 * 
	 * @return a flag whether to contribute YUI libraries to the page. {@code true} by default.
	 */
	protected boolean includeYUILibraries()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		if (includeYUILibraries())
		{
			YuiLib.load(response);
		}

		renderHeadInit(response);

		// variables for the initialization script
		Map<String, Object> variables = new HashMap<>();
		String widgetId = getEscapedComponentMarkupId();
		variables.put("componentId", getComponentMarkupId());
		variables.put("widgetId", widgetId);
		variables.put("datePattern", getDatePattern());
		variables.put("fireChangeEvent", notifyComponentOnDateSelected());
		variables.put("alignWithIcon", alignWithIcon());
		variables.put("hideOnSelect", hideOnSelect());
		variables.put("showOnFieldClick", showOnFieldClick());
		variables.put("autoHide", autoHide());

		String script = getAdditionalJavaScript();
		if (script != null)
		{
			variables.put("additionalJavascript",
				Strings.replaceAll(script, "${calendar}", "YAHOO.wicket." + widgetId + "DpJs"));
		}

		// print out the initialization properties
		Map<String, Object> p = new LinkedHashMap<>();
		configure(p, response, variables);
		if (!p.containsKey("navigator") && enableMonthYearSelection())
		{
			p.put("navigator", Boolean.TRUE);
		}

		if (enableMonthYearSelection() && p.containsKey("pages") &&
			Objects.longValue(p.get("pages")) > 1)
		{
			throw new IllegalStateException(
				"You cannot use a CalendarGroup with month/year selection!");
		}

		// ${calendarInit}
		StringBuilder calendarInit = new StringBuilder();
		appendMapping(p, calendarInit);
		variables.put("calendarInit", calendarInit.toString());

		// render initialization script with the variables interpolated
		TextTemplate datePickerJs = new PackageTextTemplate(DatePicker.class, "DatePicker.js");
		datePickerJs.interpolate(variables);
		response.render(OnDomReadyHeaderItem.forScript(datePickerJs.asString()));

		// remove previously generated markup (see onRendered) via javascript in
		// ajax requests to not render the yui calendar multiple times
		AjaxRequestTarget target = component.getRequestCycle().find(AjaxRequestTarget.class);
		if (target != null)
		{
			String escapedComponentMarkupId = getEscapedComponentMarkupId();
			String javascript = "var e = Wicket.$('" + escapedComponentMarkupId + "Dp" +
				"'); if (e != null && typeof(e.parentNode) != 'undefined' && " +
				"typeof(e.parentNode.parentNode != 'undefined')) {" +
				"e.parentNode.parentNode.removeChild(e.parentNode);" + "YAHOO.wicket." +
				escapedComponentMarkupId + "DpJs.destroy(); delete YAHOO.wicket." +
				escapedComponentMarkupId + "DpJs;}";

			target.prependJavaScript(javascript);
		}
	}

	/**
	 * Renders yui & wicket calendar js module loading. It is done only once per page.
	 * 
	 * @param response
	 *            header response
	 */
	protected void renderHeadInit(IHeaderResponse response)
	{
		String key = "DatePickerInit.js";
		if (response.wasRendered(key))
		{
			return;
		}

		// variables for YUILoader
		Map<String, Object> variables = new HashMap<>();
		variables.put("basePath",
			Strings.stripJSessionId(RequestCycle.get().urlFor(YUI, null).toString()) + "/");
		variables.put("Wicket.DateTimeInit.DatePath", RequestCycle.get().urlFor(WICKET_DATE, null));

		if (Application.get().usesDevelopmentConfig())
		{
			variables.put("filter", "filter: \"RAW\",");
			variables.put("allowRollup", false);
		}
		else
		{
			variables.put("filter", "");
			variables.put("allowRollup", true);
		}

		TextTemplate template = new PackageTextTemplate(DatePicker.class, key);
		response.render(OnDomReadyHeaderItem.forScript(template.asString(variables)));

		response.markRendered(key);
	}

	/**
	 * Check that this behavior can get a date format out of the component it is coupled to. It
	 * checks whether {@link #getDatePattern()} produces a non-null value. If that method returns
	 * null, and exception will be thrown
	 * 
	 * @param component
	 *            the component this behavior is being coupled to
	 * @throws UnableToDetermineFormatException
	 *             if this date picker is unable to determine a format.
	 */
	private void checkComponentProvidesDateFormat(final Component component)
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
	private void setWidgetProperty(final Map<String, Object> widgetProperties, final String key,
		final String[] array)
	{
		if (array != null && array.length > 0)
		{
			widgetProperties.put(key, array);
		}
	}

	/**
	 * Whether to position the date picker relative to the trigger icon.
	 * 
	 * @return If true, the date picker is aligned with the left position of the icon, and with the
	 *         top right under. If false, the date picker will skip positioning and will let you do
	 *         the positioning yourself. Returns true by default.
	 */
	protected boolean alignWithIcon()
	{
		return true;
	}

	/**
	 * Gives overriding classes the option of adding (or even changing/ removing) configuration
	 * properties for the javascript widget. See <a
	 * href="http://developer.yahoo.com/yui/calendar/">the widget's documentation</a> for the
	 * available options. If you want to override/ remove properties, you should call
	 * super.configure(properties) first. If you don't call that, be aware that you will have to
	 * call {@link #localize(java.util.Map, org.apache.wicket.markup.head.IHeaderResponse, java.util.Map)} manually if you like localized strings to be added.
	 * 
	 * @param widgetProperties
	 *            the current widget properties
	 * @param response
	 *            the header response
	 * @param initVariables
	 *            variables passed to the Wicket.DateTime.init() js method
	 */
	protected void configure(final Map<String, Object> widgetProperties,
		final IHeaderResponse response, final Map<String, Object> initVariables)
	{
		widgetProperties.put("close", true);

		// localize date fields
		localize(widgetProperties, response, initVariables);

		Object modelObject = component.getDefaultModelObject();
		// null and cast check
		if (modelObject instanceof Date)
		{
			Date date = (Date)modelObject;
			widgetProperties.put("selected", new SimpleDateFormat(FORMAT_DATE).format(date));
			widgetProperties.put("pagedate", new SimpleDateFormat(FORMAT_PAGEDATE).format(date));
		}
	}

	/**
	 * Filter all empty elements (workaround for {@link DateFormatSymbols} returning arrays with
	 * empty elements).
	 * 
	 * @param stringArray
	 *            array to filter
	 * @return filtered array (without null or empty string elements)
	 */
	protected final String[] filterEmpty(String[] stringArray)
	{
		if (stringArray == null)
		{
			return null;
		}

		List<String> list = new ArrayList<>(stringArray.length);
		for (String string : stringArray)
		{
			if (!Strings.isEmpty(string))
			{
				list.add(string);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Gets the id of the component that the calendar widget will get attached to.
	 * 
	 * @return The DOM id of the component
	 */
	protected final String getComponentMarkupId()
	{
		return component.getMarkupId();
	}

	/**
	 * Gets the date pattern to use for putting selected values in the coupled component.
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
			IConverter<?> converter = component.getConverter(DateTime.class);
			if (!(converter instanceof DateConverter))
			{
				converter = component.getConverter(Date.class);
			}
			format = ((SimpleDateFormat)((DateConverter)converter).getDateFormat(component.getLocale())).toPattern();
		}

		return format;
	}

	/**
	 * Gets the escaped DOM id that the calendar widget will get attached to. All non word
	 * characters (\W) will be removed from the string.
	 * 
	 * @return The DOM id of the calendar widget - same as the component's markup id + 'Dp'}
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
	 * Gets the title attribute of the datepicker icon
	 * 
	 * @return text
	 */
	protected CharSequence getIconTitle()
	{
		return "";
	}

	/**
	 * Gets the icon alt text for the datepicker icon
	 * 
	 * @return text
	 */
	protected CharSequence getIconAltText()
	{
		return "";
	}

	/**
	 * Gets the url for the popup button. Users can override to provide their own icon URL.
	 * 
	 * @return the url to use for the popup button/ icon
	 */
	protected CharSequence getIconUrl()
	{
		return RequestCycle.get().urlFor(
			new ResourceReferenceRequestHandler(new PackageResourceReference(DatePicker.class,
				"icon1.gif")));
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
	 * Configure the localized strings for the datepicker widget. This implementation uses
	 * {@link DateFormatSymbols} and some slight string manipulation to get the strings for months
	 * and week days. Also, the first week day is set according to the {@link Locale} returned by
	 * {@link #getLocale()}. It should work well for most locales.
	 * <p>
	 * This method is called from {@link #configure(java.util.Map, org.apache.wicket.markup.head.IHeaderResponse, java.util.Map)} and can be overridden if
	 * you want to customize setting up the localized strings but are happy with the rest of
	 * {@link #configure(java.util.Map, org.apache.wicket.markup.head.IHeaderResponse, java.util.Map)}'s behavior. Note that you can call (overridable)
	 * method {@link #getLocale()} to get the locale that should be used for setting up the widget.
	 * </p>
	 * <p>
	 * See YUI Calendar's <a href="http://developer.yahoo.com/yui/examples/calendar/germany/1.html">
	 * German</a> and <a
	 * href="http://developer.yahoo.com/yui/examples/calendar/japan/1.html">Japanese</a> examples
	 * for more info.
	 * </p>
	 * 
	 * @param widgetProperties
	 *            the current widget properties
	 * @param response
	 *            the header response
	 * @param initVariables
	 *            variables passed to the Wicket.DateTime.init() js method
	 */
	protected void localize(Map<String, Object> widgetProperties, IHeaderResponse response,
		Map<String, Object> initVariables)
	{
		Locale locale = getLocale();
		String key = "Wicket.DateTimeInit.CalendarI18n[\"" + locale.toString() + "\"]";
		initVariables.put("i18n", key);

		if (response.wasRendered(key))
		{
			return;
		}

		DateFormatSymbols dfSymbols = DateFormatSymbols.getInstance(locale);
		if (dfSymbols == null)
		{
			dfSymbols = new DateFormatSymbols(locale);
		}

		Map<String, Object> i18nVariables = new LinkedHashMap<>();
		setWidgetProperty(i18nVariables, "MONTHS_SHORT", filterEmpty(dfSymbols.getShortMonths()));
		setWidgetProperty(i18nVariables, "MONTHS_LONG", filterEmpty(dfSymbols.getMonths()));
		setWidgetProperty(i18nVariables, "WEEKDAYS_MEDIUM",
			filterEmpty(dfSymbols.getShortWeekdays()));
		setWidgetProperty(i18nVariables, "WEEKDAYS_LONG", filterEmpty(dfSymbols.getWeekdays()));

		i18nVariables.put("START_WEEKDAY", getFirstDayOfWeek(locale));

		if (Locale.SIMPLIFIED_CHINESE.equals(locale) || Locale.TRADITIONAL_CHINESE.equals(locale))
		{
			setWidgetProperty(i18nVariables, "WEEKDAYS_1CHAR",
				filterEmpty(substring(dfSymbols.getShortWeekdays(), 2, 1)));
			i18nVariables.put("WEEKDAYS_SHORT",
				filterEmpty(substring(dfSymbols.getShortWeekdays(), 2, 1)));
		}
		else
		{
			setWidgetProperty(i18nVariables, "WEEKDAYS_1CHAR",
				filterEmpty(substring(dfSymbols.getShortWeekdays(), 0, 1)));
			setWidgetProperty(i18nVariables, "WEEKDAYS_SHORT",
				filterEmpty(substring(dfSymbols.getShortWeekdays(), 0, 2)));
		}

		StringBuilder i18n = new StringBuilder(key);
		i18n.append('=');
		appendMapping(i18nVariables, i18n);
		i18n.append(';');

		response.render(OnDomReadyHeaderItem.forScript(i18n.toString()));

		response.wasRendered(key);
	}

	/**
	  * Gets the first day of week of a given locale.
	  *
	  * @return By default the first day of week accordingly to Calendar class.
	  */
	protected int getFirstDayOfWeek(Locale locale)
	{
		return Calendar.getInstance(locale).getFirstDayOfWeek() - 1;
	}

	/**
	 * Whether to notify the associated component when a date is selected. Notifying is done by
	 * calling the associated component's onchange JavaScript event handler. You can for instance
	 * attach an {@link AjaxEventBehavior} to that component to get a call back to the server. The
	 * default is true.
	 * 
	 * @return if true, notifies the associated component when a date is selected
	 */
	protected boolean notifyComponentOnDateSelected()
	{
		return true;
	}

	/**
	 * Makes a copy of the provided array and for each element copy the substring 0..len to the new
	 * array
	 * 
	 * @param array
	 *            array to copy from
	 * @param len
	 *            size of substring for each element to copy
	 * @return copy of the array filled with substrings.
	 */
	protected final String[] substring(final String[] array, final int len)
	{
		return substring(array, 0, len);
	}

	/**
	 * Makes a copy of the provided array and for each element copy the substring 0..len to the new
	 * array
	 * 
	 * @param array
	 *            array to copy from
	 * @param start
	 *            start position of the substring
	 * @param len
	 *            size of substring for each element to copy
	 * @return copy of the array filled with substrings.
	 */
	protected final String[] substring(final String[] array, final int start, final int len)
	{
		if (array != null)
		{
			String[] copy = new String[array.length];
			for (int i = 0; i < array.length; i++)
			{
				String el = array[i];
				if (el != null)
				{
					if (el.length() > (start + len))
					{
						copy[i] = el.substring(start, start + len);
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
	 * Indicates whether plain text is rendered or two select boxes are used to allow direct
	 * selection of month and year.
	 * 
	 * @return <code>true</code> if select boxes should be rendered to allow month and year
	 *         selection.<br/>
	 *         <code>false</code> to render just plain text.
	 */
	protected boolean enableMonthYearSelection()
	{
		return false;
	}

	/**
	 * Indicates whether the calendar should be hidden after a date was selected.
	 * 
	 * @return <code>true</code> (default) if the calendar should be hidden after the date selection <br/>
	 *         <code>false</code> if the calendar should remain visible after the date selection.
	 */
	protected boolean hideOnSelect()
	{
		return true;
	}

	/**
	 * Indicates whether the calendar should be shown when corresponding text input is clicked.
	 * 
	 * @return <code>true</code> <br/>
	 *         <code>false</code> (default)
	 */
	protected boolean showOnFieldClick()
	{
		return showOnFieldClick;
	}

	/**
	 * @param show
	 *            a flag indicating whether to show the picker on click event
	 * @return {@code this} instance to be able to chain calls
	 * @see {@link #showOnFieldClick()}
	 */
	public DatePicker setShowOnFieldClick(boolean show)
	{
		showOnFieldClick = show;
		return this;
	}


	/**
	 * Indicates whether the calendar should be hidden when the user clicks on an area of the
	 * document outside of the dialog.
	 * 
	 * @return <code>true</code> <br/>
	 *         <code>false</code> (default)
	 */
	protected boolean autoHide()
	{
		return autoHide;
	}

	/**
	 * @param autoHide
	 *            a flag indicating whether to hide the picker on click event
	 * @return {@code this} instance to be able to chain calls
	 * @see {@link #autoHide()}
	 */
	public DatePicker setAutoHide(boolean autoHide)
	{
		this.autoHide = autoHide;
		return this;
	}

	/**
	 * Indicates whether the calendar should be rendered after it has been loaded.
	 * 
	 * @return <code>true</code> if the calendar should be rendered after it has been loaded.<br/>
	 *         <code>false</code> (default) if it's initially hidden.
	 */
	protected boolean renderOnLoad()
	{
		return false;
	}

	/**
	 * Override this method to further customize the YUI Calendar with additional JavaScript code.
	 * The code returned by this method is executed right after the Calendar has been constructed
	 * and initialized. To refer to the actual Calendar DOM object, use <code>${calendar}</code> in
	 * your code.<br/>
	 * See <a href="http://developer.yahoo.com/yui/calendar/">the widget's documentation</a> for
	 * more information about the YUI Calendar.<br/>
	 * Example:
	 * 
	 * <pre>
	 * protected String getAdditionalJavaScript()
	 * {
	 * 	return &quot;${calendar}.addRenderer(\&quot;10/3\&quot;, ${calendar}.renderCellStyleHighlight1);&quot;;
	 * }
	 * </pre>
	 * 
	 * @return a String containing additional JavaScript code
	 */
	protected String getAdditionalJavaScript()
	{
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled(final Component component)
	{
		return component.isEnabledInHierarchy();
	}

	/**
	 * 
	 * @param map
	 *            the key-value pairs to be serialized
	 * @param json
	 *            the buffer holding the constructed json
	 */
	private void appendMapping(final Map<String, Object> map, final StringBuilder json)
	{
		json.append('{');
		for (Iterator<Entry<String, Object>> i = map.entrySet().iterator(); i.hasNext();)
		{
			Entry<String, Object> entry = i.next();
			json.append(entry.getKey());
			Object value = entry.getValue();
			if (value instanceof CharSequence)
			{
				json.append(":\"");
				json.append(Strings.toEscapedUnicode(value.toString()));
				json.append('"');
			}
			else if (value instanceof CharSequence[])
			{
				json.append(":[");
				CharSequence[] valueArray = (CharSequence[])value;
				for (int j = 0; j < valueArray.length; j++)
				{
					CharSequence tmpValue = valueArray[j];
					if (j > 0)
					{
						json.append(',');
					}
					if (tmpValue != null)
					{
						json.append('"');
						json.append(Strings.toEscapedUnicode(tmpValue.toString()));
						json.append('"');
					}
				}
				json.append(']');
			}
			else if (value instanceof Map)
			{
				json.append(':');
				@SuppressWarnings("unchecked")
				Map<String, Object> nmap = (Map<String, Object>)value;
				appendMapping(nmap, json);
			}
			else
			{
				json.append(':');
				json.append(Strings.toEscapedUnicode(String.valueOf(value)));
			}
			if (i.hasNext())
			{
				json.append(',');
			}
		}
		json.append('}');
	}
}
