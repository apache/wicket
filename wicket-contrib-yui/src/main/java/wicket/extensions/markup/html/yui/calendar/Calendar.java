/*
 * $Id: Calendar.java 5044 2006-03-20 16:46:35 -0800 (Mon, 20 Mar 2006)
 * jonathanlocke $ $Revision: 5159 $ $Date: 2006-03-20 16:46:35 -0800 (Mon, 20
 * Mar 2006) $
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

import java.util.Map;
import java.util.regex.Pattern;

import wicket.Application;
import wicket.AttributeModifier;
import wicket.Component;
import wicket.IInitializer;
import wicket.RequestCycle;
import wicket.behavior.HeaderContributor;
import wicket.extensions.markup.html.yui.AbstractYuiPanel;
import wicket.extensions.util.resource.PackagedTextTemplate;
import wicket.markup.html.PackageResource;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.model.AbstractReadOnlyModel;
import wicket.util.collections.MiniMap;

/**
 * Calendar component based on the Calendar of Yahoo UI Library.
 * 
 * @author Eelco Hillenius
 */
public class Calendar extends AbstractYuiPanel
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
			// register all .js, .css, and .gif package resources
			PackageResource.bind(application, Calendar.class, Pattern
					.compile(".*\\.js|.*\\.css|.*\\.gif"));
		}
	}

	/**
	 * The container/ receiver of the javascript component.
	 */
	private final class CalendarElement extends FormComponent
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public CalendarElement(String id)
		{
			super(id);
			add(new AttributeModifier("id", true, new AbstractReadOnlyModel()
			{
				private static final long serialVersionUID = 1L;

				public Object getObject(Component component)
				{
					return elementId;
				}
			}));
		}

		/**
		 * @see wicket.markup.html.form.FormComponent#updateModel()
		 */
		public void updateModel()
		{
			Calendar.this.updateModel();
		}
	}

	/** the receiving component. */
	private CalendarElement calendarElement;

	/**
	 * The DOM id of the element that hosts the javascript component.
	 */
	private String elementId;

	/**
	 * The JavaScript variable name of the calendar component.
	 */
	private String javaScriptId;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 */
	public Calendar(String id)
	{
		super(id);
		add(HeaderContributor.forJavaScript(Calendar.class));
		add(HeaderContributor.forCss(Calendar.class));

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
		add(calendarElement = new CalendarElement("calendarContainer"));
	}

	/**
	 * TODO implement
	 */
	public void updateModel()
	{
	}

	/**
	 * @see wicket.Component#renderHead(wicket.markup.html.internal.HtmlHeaderContainer)
	 */
	public void renderHead(HtmlHeaderContainer container)
	{
		((WebPage)getPage()).getBodyContainer().addOnLoadModifier("init" + javaScriptId + "();");
		super.renderHead(container);
	}

	/**
	 * @see wicket.Component#onAttach()
	 */
	protected void onAttach()
	{
		super.onAttach();

		// initialize lazily
		if (elementId == null)
		{
			// assign the markup id
			String id = getMarkupId();
			elementId = id + "Element";
			javaScriptId = elementId + "JS";
		}
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

		Map variables = new MiniMap(4);
		variables.put("javaScriptId", javaScriptId);
		variables.put("elementId", elementId);
		variables.put("navigationArrowLeft", leftImage);
		variables.put("navigationArrowRight", rightImage);

		PackagedTextTemplate template = new PackagedTextTemplate(Calendar.class, "init.js");
		template.interpolate(variables);

		return template.toString();
	}
}
