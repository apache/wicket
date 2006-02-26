/*
 * $Id$ $Revision$ $Date$
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
package wicket.extensions.markup.html.yui.calendar;

import wicket.Application;
import wicket.AttributeModifier;
import wicket.Component;
import wicket.IInitializer;
import wicket.RequestCycle;
import wicket.markup.html.PackageResource;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.resources.JavaScriptReference;
import wicket.markup.html.resources.StyleSheetReference;
import wicket.model.AbstractReadOnlyModel;

/**
 * Calendar component based on the Calendar of Yahoo UI Library.
 * 
 * @author Eelco Hillenius
 */
public class Calendar extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Initializer for this component; binds static resources.
	 */
	public final static class ComponentInitializer implements IInitializer
	{
		/**
		 * @see wicket.IInitializer#init(wicket.Application)
		 */
		public void init(Application application)
		{
			PackageResource.bind(application, Calendar.class, "calendar.css");
			PackageResource.bind(application, Calendar.class, "YAHOO.js");
			PackageResource.bind(application, Calendar.class, "dom.js");
			PackageResource.bind(application, Calendar.class, "event.js");
			PackageResource.bind(application, Calendar.class, "calendar.js");
			PackageResource.bind(application, Calendar.class, "config.js");
			PackageResource.bind(application, Calendar.class, "callt.gif");
			PackageResource.bind(application, Calendar.class, "calrt.gif");
		}
	}

	/**
	 * The container/ receiver of the javascript component.
	 */
	public static final class CalendarContainer extends FormComponent
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public CalendarContainer(String id)
		{
			super(id);
			add(new AttributeModifier("id", true, new AbstractReadOnlyModel()
			{
				private static final long serialVersionUID = 1L;

				public Object getObject(Component component)
				{
					return CalendarContainer.this.getPath().replace(':', '_');
				}
			}));
		}

		/**
		 * @see wicket.markup.html.form.FormComponent#updateModel()
		 */
		public void updateModel()
		{
		}
	}

	/** the receiving component. */
	private CalendarContainer calendarContainer;

	/**
	 * The DOM id of the element that hosts the javascript component.
	 */
	private String containerDomId;

	/**
	 * The JavaScript variable name of the calendar component.
	 */
	private String javaScriptComponentName;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 */
	public Calendar(String id)
	{
		super(id);
		add(new StyleSheetReference("calendarCSS", Calendar.class, "calendar.css"));
		add(new JavaScriptReference("YAHOO", Calendar.class, "YAHOO.js"));
		add(new JavaScriptReference("dom", Calendar.class, "dom.js"));
		add(new JavaScriptReference("event", Calendar.class, "event.js"));
		add(new JavaScriptReference("calendar", Calendar.class, "calendar.js"));
		Label initialization = new Label("initialization", new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see wicket.model.IModel#getObject(wicket.Component)
			 */
			public Object getObject(Component component)
			{
				return getJavaScriptComponentInitializationScript();
			}
		});
		initialization.setEscapeModelStrings(false);
		add(initialization);
		add(calendarContainer = new CalendarContainer("calendarContainer"));
	}

	/**
	 * @see wicket.Component#onBeginRequest()
	 */
	protected void onBeginRequest()
	{
		super.onBeginRequest();
		// rather do this in the constructor, but we can't safely until 1.3
		containerDomId = calendarContainer.getPath().replace(':', '_');
		javaScriptComponentName = "JsComp" + containerDomId;
	}

	/**
	 * @see wicket.Component#renderHead(wicket.markup.html.internal.HtmlHeaderContainer)
	 */
	public void renderHead(HtmlHeaderContainer container)
	{
		((WebPage)getPage()).getBodyContainer().addOnLoadModifier(
				"init" + javaScriptComponentName + "();");
		super.renderHead(container);
	}

	/**
	 * Gets the initilization script for the javascript component.
	 * 
	 * @return the initilization script
	 */
	protected String getJavaScriptComponentInitializationScript()
	{
		String leftImage = RequestCycle.get().urlFor(
				new PackageResourceReference(Calendar.class, "callt.gif"));
		String rightImage = RequestCycle.get().urlFor(
				new PackageResourceReference(Calendar.class, "calrt.gif"));

		StringBuffer b = new StringBuffer("\nvar ").append(javaScriptComponentName).append(";\n");
		b.append("function init").append(javaScriptComponentName).append("() {\n\t");
		b.append(javaScriptComponentName).append(" = new YAHOO.widget.Calendar(\"").append(
				javaScriptComponentName).append("\",\"").append(containerDomId).append("\");\n\t");
		b.append(javaScriptComponentName).append(".Options.NAV_ARROW_LEFT = \"").append(leftImage)
				.append("\";\n\t");
		b.append(javaScriptComponentName).append(".Options.NAV_ARROW_RIGHT = \"")
				.append(rightImage).append("\";\n\t");
		b.append(javaScriptComponentName).append(".render();\n");
		b.append("}\n");
		return b.toString();
	}
}
