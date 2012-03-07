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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.wicket.extensions.yui.YuiLib;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.request.resource.PackageResourceReference;


/**
 * Abstract calendar component based on the YUI (Yahoo User Interface library) javascript widget.
 * <p>
 * Although this component by itself is fully functional, it doesn't do much other than just
 * displaying the calendar. Hence, this class is abstract.
 * </p>
 * <p>
 * An easy way to build upon this component is to override
 * {@link #appendToInit(String, String, String, StringBuffer)} and add event handlers etc. in the
 * YUI widget's initialization function.
 * </p>
 * See <a href="http://developer.yahoo.com/yui/calendar/">YUI's calendar documentation</a> for more
 * info.
 * 
 * @author eelcohillenius
 * 
 * @see DatePicker
 */
// TODO provide localization strings (base them on the messages of
// JsDatePicker?)
public abstract class AbstractCalendar extends WebComponent
{
	private static final long serialVersionUID = 1L;

	private final boolean contributeDependencies;

	/**
	 * Construct. Contributes packaged dependencies.
	 * 
	 * @param id
	 *            The component id
	 */
	public AbstractCalendar(String id)
	{
		this(id, true);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param contributeDependencies
	 *            Whether to contribute the packaged dependencies. Pass false in case you want to
	 *            include the dependencies manually in your own page, e.g. when you want to keep
	 *            them in your web application dir. To contribute yourself (in case you want to pass
	 *            false), your page header should look like:
	 * 
	 *            <pre>
	 * 	 &lt;script type=&quot;text/javascript&quot; src=&quot;yahoo.js&quot;&gt;&lt;/script&gt;
	 * 	 &lt;script type=&quot;text/javascript&quot; src=&quot;dom.js&quot;&gt;&lt;/script&gt;
	 * 	 &lt;script type=&quot;text/javascript&quot; src=&quot;event.js&quot;&gt;&lt;/script&gt;
	 * 	 &lt;script type=&quot;text/javascript&quot; src=&quot;calendar.js&quot;&gt;&lt;/script&gt;
	 * 	 &lt;link rel=&quot;stylesheet&quot; type=&quot;text/css&quot; href=&quot;calendar.css&quot; /&gt;
	 * </pre>
	 */
	public AbstractCalendar(String id, boolean contributeDependencies)
	{

		super(id);
		setOutputMarkupId(true);
		this.contributeDependencies = contributeDependencies;
	}

	/**
	 * Gets the id of the javascript widget. Note that this is the non-namespaced id, so depending
	 * on what you want to do with it, you may need to prepend 'YAHOO.wicket.' to it. Or you can
	 * call {@link #getJavaScriptWidgetId()}.
	 * 
	 * @return The javascript id
	 * @see #getJavaScriptWidgetId()
	 */
	public final String getJavaScriptId()
	{
		return getMarkupId() + "Js";
	}

	/**
	 * The name spaced id of the widget.
	 * 
	 * @return The widget id
	 * @see #getJavaScriptId()
	 */
	public final String getJavaScriptWidgetId()
	{
		return "YAHOO.wicket." + getJavaScriptId();
	}

	/**
	 * add header contributions for packaged resources.
	 * 
	 * @param response
	 *            the header response to contribute to
	 */
	private void contributeDependencies(IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(
				YuiLib.class, "yahoodomevent/yahoo-dom-event.js")));
		response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(
			AbstractCalendar.class, "calendar-min.js")));
		response.render(CssHeaderItem.forReference(new PackageResourceReference(
			AbstractCalendar.class, "assets/skins/sam/calendar.css")));
	}

	/**
	 * Append javascript to the initialization function for the YUI widget. Can be used by
	 * subclasses to conveniently extend configuration without having to write a separate
	 * contribution.
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
		StringBuilder b)
	{
	}

	/**
	 * Gives overriding classes the option of adding (or even changing/ removing) configuration
	 * properties for the javascript widget. See <a
	 * href="http://developer.yahoo.com/yui/calendar/">the widget's documentation</a> for the
	 * available options. If you want to override/ remove properties, you obviously should call
	 * <code>super.configureWidgetProperties(properties)</code>.
	 * 
	 * @param widgetProperties
	 *            the current widget properties
	 */
	protected void configureWidgetProperties(Map<Object, Object> widgetProperties)
	{
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		if (contributeDependencies)
		{
			contributeDependencies(response);
		}


		// not pretty to look at, but cheaper than using a template
		String markupId = AbstractCalendar.this.getMarkupId();
		String javascriptId = getJavaScriptId();
		String javascriptWidgetId = getJavaScriptWidgetId();
		StringBuilder b = new StringBuilder();
		// initialize wicket namespace and register the init function
		// for the YUI widget
		b.append("YAHOO.namespace(\"wicket\");\nfunction init");
		b.append(javascriptId);
		b.append("() {\n");

		// instantiate the calendar object
		b.append("  ");
		b.append(javascriptWidgetId);
		b.append(" = new YAHOO.widget.Calendar(\"");
		b.append(javascriptId);
		b.append("\",\"");
		b.append(markupId);

		Properties p = new Properties();
		configureWidgetProperties(p);
		b.append("\", { ");
		for (Iterator<Entry<Object, Object>> i = p.entrySet().iterator(); i.hasNext();)
		{
			Entry<Object, Object> entry = i.next();
			b.append(entry.getKey());
			Object value = entry.getValue();
			if (value instanceof CharSequence)
			{
				b.append(":\"");
				b.append(value);
				b.append("\"");
			}
			else if (value instanceof CharSequence[])
			{
				b.append(":[");
				CharSequence[] valueArray = (CharSequence[])value;
				for (int j = 0; j < valueArray.length; j++)
				{
					CharSequence tmpValue = valueArray[j];
					b.append("\"");
					b.append(tmpValue);
					b.append("\"");
					if (j < valueArray.length - 1)
					{
						b.append(",");
					}
				}
				b.append("]");
			}
			else
			{
				b.append(":");
				b.append(value);
			}
			// TODO handle arrays
			if (i.hasNext())
			{
				b.append(",");
			}
		}

		b.append(" });\n");

		// append the javascript we want for our init function; call
		// this in an overridable method so that clients can add their
		// stuff without needing a big ass API
		appendToInit(markupId, javascriptId, javascriptWidgetId, b);

		// trigger rendering
		b.append("  ");
		b.append(javascriptWidgetId);
		b.append(".render();\n");

		b.append("}\n");
		// register the function for execution when the page is loaded
		b.append("YAHOO.util.Event.addListener(window, \"load\", init");
		b.append(javascriptId);
		b.append(");");

		response.render(JavaScriptHeaderItem.forScript(b, null));
	}
}
