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
package wicket.extensions.yui.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.joda.time.DateTime;

import wicket.Component;
import wicket.RequestCycle;
import wicket.ResourceReference;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.behavior.AbstractBehavior;
import wicket.datetime.markup.html.form.DateTextField;
import wicket.extensions.yui.YuiLib;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.IHeaderResponse;
import wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import wicket.markup.html.resources.CompressedResourceReference;
import wicket.markup.html.resources.JavascriptResourceReference;
import wicket.util.convert.IConverter;
import wicket.util.convert.converters.DateConverter;
import wicket.util.string.JavascriptUtils;
import wicket.util.string.Strings;

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
	 * @see wicket.behavior.AbstractBehavior#bind(wicket.Component)
	 */
	public void bind(Component component)
	{
		checkComponentProvidesDateFormat(component);
		component.setOutputMarkupId(true);
		this.component = component;
	}

	/**
	 * @see wicket.behavior.AbstractBehavior#onRendered(wicket.Component)
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
		response.write(getCalendarMarkupId());
		response.write("\"></div><img style=\"");
		response.write(getIconStyle());
		response.write("\" id=\"");
		response.write(getIconId());
		response.write("\" src=\"");
		CharSequence iconUrl = getIconUrl();
		response.write(Strings.escapeMarkup(iconUrl != null ? iconUrl.toString() : ""));
		response.write("\" /></span><input type=\"hidden\"/>");
	}

	/**
	 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.markup.html.IHeaderResponse)
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

		// not pretty to look at, but cheaper than using a template
		String markupId = getCalendarMarkupId();
		String javascriptId = getCalendarJavascriptId();
		String javascriptWidgetId = "YAHOO.wicket." + getCalendarJavascriptId();

		StringBuffer buffer = new StringBuffer();
		// initialize wicket namespace and register the init function
		// for the YUI widget
		buffer.append("YAHOO.namespace(\"wicket\");\nfunction init");
		buffer.append(javascriptId);
		buffer.append("() {\n");

		// instantiate the calendar object
		buffer.append("  ");
		buffer.append(javascriptWidgetId);
		buffer.append(" = new YAHOO.widget.Calendar(\"");
		buffer.append(javascriptId);
		buffer.append("\",\"");
		buffer.append(markupId);

		// print out the initialization properties
		Properties p = new Properties();
		configureWidgetProperties(p);
		buffer.append("\", { ");
		for (Iterator i = p.entrySet().iterator(); i.hasNext();)
		{
			Entry entry = (Entry)i.next();
			buffer.append(entry.getKey());
			Object value = entry.getValue();
			if (value instanceof CharSequence)
			{
				buffer.append(":\"");
				buffer.append(value);
				buffer.append("\"");
			}
			else if (value instanceof CharSequence[])
			{
				buffer.append(":[");
				CharSequence[] valueArray = (CharSequence[])value;
				for (int j = 0; j < valueArray.length; j++)
				{
					CharSequence tmpValue = valueArray[j];
					buffer.append("\"");
					buffer.append(tmpValue);
					buffer.append("\"");
					if (j < valueArray.length - 1)
					{
						buffer.append(",");
					}
				}
				buffer.append("]");
			}
			else
			{
				buffer.append(":");
				buffer.append(value);
			}
			// TODO handle arrays
			if (i.hasNext())
			{
				buffer.append(",");
			}
		}
		buffer.append(" });\n");

		// add a listener to the calendar widget that fills in the value
		// of the passed in date text field when a selection is made,
		// after which the widget is hidden again (it starts out hidden)
		buffer.append("  YAHOO.util.Event.addListener(\"");
		String iconId = getIconId();
		buffer.append(iconId);
		buffer.append("\", \"click\", ");
		buffer.append(javascriptWidgetId);
		buffer.append(".show, ");
		buffer.append(javascriptWidgetId);
		buffer.append(", true);\n");
		buffer.append("  function selectHandler(type, args, cal) {\n");
		buffer.append("    var selDateArray = args[0][0];\n");
		buffer.append("    var yr = selDateArray[0];\n");
		buffer.append("    var month = selDateArray[1];\n");
		buffer.append("    var dt = selDateArray[2];\n");

		buffer.append("    var val = '");
		String datePattern = getDatePattern();
		// use the target component's pattern to fill in the date
		// it's quite rough (e.g. YY is still filled in as YYYY), but
		// should work without problems
		buffer.append(datePattern);
		buffer.append("'.replace(/d+/, dt).replace(/M+/, month)");
		buffer.append(".replace(/y+/, yr);\n    YAHOO.util.Dom.get(\"");
		buffer.append(component.getMarkupId());
		buffer.append("\").value = val;\n");
		buffer.append("    cal.hide();\n  }\n");
		buffer.append("  ");
		buffer.append(javascriptWidgetId);
		buffer.append(".selectEvent.subscribe(selectHandler, ");
		buffer.append(javascriptWidgetId);
		buffer.append(");\n");

		// append the javascript we want for our init function; call
		// this in an overridable method so that clients can add their
		// stuff without needing a big ass API
		appendToInit(markupId, javascriptId, javascriptWidgetId, buffer);

		// trigger rendering
		buffer.append("  ");
		buffer.append(javascriptWidgetId);
		buffer.append(".render();\n");
		buffer.append("}\n");

		buffer.insert(0, JavascriptUtils.SCRIPT_OPEN_TAG);
		buffer.append(JavascriptUtils.SCRIPT_CLOSE_TAG);
		response.renderString(buffer);

		// Initialize the calendar. This depends on how the datepicker is
		// used (AJAX or not).
		StringBuffer initBuffer = new StringBuffer();
		initBuffer.append("init");
		initBuffer.append(javascriptId);
		initBuffer.append("();");
		response.renderOnLoadJavascript(initBuffer.toString());
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
	 * Check that this behavior can get a date format out of the component it is
	 * coupled to. if you override this method to allow for other types (such as
	 * your own), you should override {@link #getDatePattern()} as well. This
	 * method should return normally if the component is accepted or throw a RTE
	 * when it is not.
	 * 
	 * @param component
	 *            the component this behavior is being coupled to
	 * @throws WicketRuntimeException
	 *             if the component is not support.
	 */
	protected void checkComponentProvidesDateFormat(Component component)
	{

		if (component instanceof ITextFormatProvider)
		{
			// were ok
			return;
		}

		IConverter converter = component.getConverter(DateTime.class);
		if (converter == null)
		{
			converter = component.getConverter(Date.class);
		}
		if (converter instanceof DateConverter)
		{

			return; // This is ok
		}
		throw new WicketRuntimeException(
				"this behavior can only be added to components that either implement "
						+ ITextFormatProvider.class.getName() + " or that use "
						+ DateConverter.class.getName() + " configured with an instance of "
						+ SimpleDateFormat.class.getName()
						+ " (like Wicket's default configuration has)");
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
	 * Gets the id of the javascript widget. Note that this is the
	 * non-namespaced id, so depending on what you want to do with it, you may
	 * need to prepend 'YAHOO.wicket.' to it. Or you can call
	 * {@link #getJavascriptWidgetId()}.
	 * 
	 * @return The javascript id
	 * @see #getJavascriptWidgetId()
	 */
	protected final String getCalendarJavascriptId()
	{
		return component.getMarkupId() + "DpJs";
	}

	/**
	 * Gets the markup id that the calendar widget will get attached to.
	 * 
	 * @return The markup id of the calendar widget
	 */
	protected final String getCalendarMarkupId()
	{
		return component.getMarkupId() + "Dp";
	}

	/**
	 * Gets the date pattern to use for putting selected values in the coupled
	 * component. If you override this method to support components that would
	 * otherwise not be supported, you should override
	 * {@link #checkComponentProvidesDateFormat(Component)} and let it return
	 * normally.
	 * 
	 * @return The date pattern
	 */
	protected String getDatePattern()
	{

		if (component instanceof ITextFormatProvider)
		{
			return ((ITextFormatProvider)component).getTextFormat();
		}
		else
		{
			// cast from hell, but we checked before whether we could
			IConverter converter = component.getConverter(DateTime.class);
			if (converter == null)
			{
				converter = component.getConverter(Date.class);
			}
			return ((SimpleDateFormat)((DateConverter)converter).getDateFormat(component
					.getLocale())).toPattern();
		}
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
}
