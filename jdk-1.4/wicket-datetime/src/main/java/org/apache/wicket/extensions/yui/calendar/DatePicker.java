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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.YuiLib;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converters.DateConverter;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.template.TextTemplateHeaderContributor;
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
		response
				.write("\n<span>&nbsp;<div style=\"display:none;z-index: 99999;position:absolute;\" id=\"");
		response.write(getComponentMarkupId());
		response.write("Dp\"></div><img style=\"");
		response.write(getIconStyle());
		response.write("\" id=\"");
		response.write(getIconId());
		response.write("\" src=\"");
		CharSequence iconUrl = getIconUrl();
		response.write(Strings.escapeMarkup(iconUrl != null ? iconUrl.toString() : ""));
		response.write("\" /></span><input type=\"hidden\"/>");
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderContributor#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
	 */
	public void renderHead(IHeaderResponse response)
	{
		// add YUI contributions
		// NOTE JavascriptResourceReference takes care of stripping comments
		// when in deployment (production) mode
		response
				.renderJavascriptReference(new JavascriptResourceReference(YuiLib.class, "yahoo.js"));
		response
				.renderJavascriptReference(new JavascriptResourceReference(YuiLib.class, "event.js"));
		response.renderJavascriptReference(new JavascriptResourceReference(YuiLib.class, "dom.js"));
		response.renderJavascriptReference(new JavascriptResourceReference(DatePicker.class,
				"calendar.js"));
		response.renderCSSReference(new CompressedResourceReference(DatePicker.class,
				"assets/calendar.css"));
		response.renderJavascriptReference(new JavascriptResourceReference(DatePicker.class,
				"wicket-date.js"));

		// variables for the initialization script
		Map variables = new HashMap();
		String widgetId = getComponentMarkupId();
		variables.put("widgetId", widgetId);
		variables.put("datePattern", getDatePattern());
		variables.put("fireChangeEvent", Boolean.valueOf(notifyComponentOnDateSelected()));

		// print out the initialization properties
		Properties p = new Properties();
		configureWidgetProperties(p);
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
				calendarInit.append(value);
				calendarInit.append("\"");
			}
			else if (value instanceof CharSequence[])
			{
				calendarInit.append(":[");
				CharSequence[] valueArray = (CharSequence[])value;
				for (int j = 0; j < valueArray.length; j++)
				{
					CharSequence tmpValue = valueArray[j];
					calendarInit.append("\"");
					calendarInit.append(tmpValue);
					calendarInit.append("\"");
					if (j < valueArray.length - 1)
					{
						calendarInit.append(",");
					}
				}
				calendarInit.append("]");
			}
			else
			{
				calendarInit.append(":");
				calendarInit.append(value);
			}
			if (i.hasNext())
			{
				calendarInit.append(",");
			}
		}
		variables.put("calendarInit", calendarInit.toString());

		// render initialization script with the variables interpolated
		TextTemplateHeaderContributor.forJavaScript(DatePicker.class, "DatePicker.js",
				Model.valueOf(variables)).renderHead(response);

		// Initialize the calendar.
		StringBuffer initBuffer = new StringBuffer();
		initBuffer.append("init");
		initBuffer.append(widgetId + "DpJs");
		initBuffer.append("();");
		response.renderOnLoadJavascript(initBuffer.toString());
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
	 * remove properties, you obviously should call
	 * {@link super#setWidgetProperties(Properties)} first.
	 * 
	 * @param widgetProperties
	 *            the current widget properties
	 */
	protected void configureWidgetProperties(Map widgetProperties)
	{
		widgetProperties.put("close", Boolean.TRUE);
		// TODO localize
		widgetProperties.put("title", "Select a date:");

		Date date = (Date)component.getModelObject();
		if (date != null)
		{
			widgetProperties.put("selected", AbstractCalendar.FORMAT_DATE.format(date));
			widgetProperties.put("pagedate", AbstractCalendar.FORMAT_PAGEDATE.format(date));
		}
	}

	/**
	 * Gets the DOM id that the calendar widget will get attached to.
	 * 
	 * @return The DOM id of the calendar widget - same as the component's
	 *         markup id + 'Dp'}
	 */
	protected final String getComponentMarkupId()
	{
		return component.getMarkupId();
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
	 * Gets the id of the icon that triggers the popup.
	 * 
	 * @return The id of the icon
	 */
	protected final String getIconId()
	{
		return component.getMarkupId() + "Icon";
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
	 * Whether to notify the associated component when a date is selected.
	 * Notifying is done by calling the associated component's onchange
	 * Javascript event handler. You can for instance attach an
	 * {@link AjaxEventBehavior} to that component to get a call back to the
	 * server. Returns false by default.
	 * 
	 * @return if true, notifies the associated component when a date is
	 *         selected
	 */
	protected boolean notifyComponentOnDateSelected()
	{
		return false;
	}
}
