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

import wicket.AttributeModifier;
import wicket.Component;
import wicket.extensions.markup.html.resources.JavaScriptReference;
import wicket.extensions.markup.html.resources.StyleSheetReference;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Datepicker component.
 * <p>
 * Link your datepicker to a textfield like this:
 * </p>
 * <p>
 * (Java)
 * <pre>
 * TextField dateField = new TextField("dateField", Date.class);
 * add(dateField);
 * add(new DatePicker("dateFieldPicker", dateField));
 * </pre>
 * (html)
 * <pre>
 * &lt;input type="text" wicket:id="dateField" size="10" /&gt;
 * &lt;span wicket:id="dateFieldPicker" /&gt;
 * </pre>
 * </p>
 * <p>
 * Your target doesn't have to be a text field however, attach to any tag that is
 * supported by JSCalendar.
 * </p>
 * <p>
 * Customize the looks, localization etc of the datepicker by providing a custom
 * {@link wicket.extensions.markup.html.datepicker.DatePickerSettings} object.
 * </p>
 * <p>
 * This component is based on Dynarch's JSCalendar component, which can be found
 * at <a href="http://www.dynarch.com/">the Dynarch site</a>.
 * </p>
 *
 * @see wicket.extensions.markup.html.datepicker.DatePickerSettings
 *
 * @author Eelco Hillenius
 * @author Mihai Bazon (creator of the JSCalendar component)
 */
public class DatePicker extends Panel
{
	/**
	 * Attribute modifier that modifies/ adds an id attribute with value of the
	 * given component's path.
	 */
	private final static class IdAttributeModifier extends AttributeModifier
	{

		/** model for substituting the id attribute of a component based on the component id. */
		private final static class IdModel extends Model
		{
			/**
			 * @see wicket.model.IModel#getObject(wicket.Component)
			 */
			public Object getObject(Component component)
			{
				return component.getPath();
			}
		}

		/** target component. */
		private final Component component;

		/**
		 * Construct.
		 * @param component the target component
		 */
		public IdAttributeModifier(Component component)
		{
			super("id", true, new IdModel());
			this.component = component;
		}
	}

	/**
	 * Button that triggers the popup.
	 */
	private final static class TriggerButton extends WebMarkupContainer
	{
		/**
		 * Construct.
		 * @param id component id
		 * @param resourceReference button icon reference
		 */
		public TriggerButton(final String id, final wicket.ResourceReference resourceReference)
		{
			super(id);
			add(new IdAttributeModifier(this));
			IModel srcReplacement = new Model()
			{
				public Object getObject(Component component)
				{
					String url = getPage().urlFor(resourceReference.getPath());
					return url;
				};
			};
			add(new AttributeModifier("src", true, srcReplacement));
		}
	}

	/**
	 * Outputs the Javascript initialization code.
	 */
	private final class InitScript extends WebComponent
	{
		/**
		 * Construct.
		 * @param id component id
		 */
		public InitScript(String id)
		{
			super(id);
		}

		/**
		 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream, wicket.markup.ComponentTag)
		 */
		protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
		{
			replaceComponentTagBody(markupStream, openTag, getInitScript());
		}
	}

	/** the receiving component. */
	private final Component target;

	/** the button that triggers the popup. */
	private TriggerButton triggerButton;

	/** settings for the javascript datepicker component. */
	private final DatePickerSettings settings;

	/**
	 * Construct with a default button and style.
	 * @param id the component id
	 * @param target the receiving component
	 */
	public DatePicker(String id, Component target)
	{
		this(id, target, new DatePickerSettings());
	}


	/**
	 * Construct.
	 * @param id the component id
	 * @param target the receiving component
	 * @param settings datepicker properties
	 */
	public DatePicker(final String id, final Component target, final DatePickerSettings settings)
	{
		super(id);

		if(settings == null)
		{
			throw new NullPointerException("settings must be non null when using this constructor");
		}

		this.settings = settings;
		
		if (target == null)
		{
			throw new NullPointerException("targetTextField must be not null");
		}

		target.add(new IdAttributeModifier(target));
		this.target = target;

		add(triggerButton = new TriggerButton("trigger", settings.getIcon()));
		add(new InitScript("script"));
		addToHeader(new JavaScriptReference("calendarMain", DatePicker.class, "calendar.js"));
		addToHeader(new JavaScriptReference("calendarSetup", DatePicker.class, "calendar-setup.js"));
		addToHeader(new JavaScriptReference("calendarLanguage", new Model()
		{
			public Object getObject(Component component)
			{
				return settings.getLanguage(DatePicker.this.getLocale());
			}
		}));
		addToHeader(new StyleSheetReference("calendarStyle", settings.getStyle()));
	}

	/**
	 * Gets the initilization javascript.
	 * @return the initilization javascript
	 */
	private String getInitScript()
	{
		StringBuffer b = new StringBuffer("\nCalendar.setup(\n{");
		b.append("\n\t\tinputField : \"").append(target.getPath()).append("\",");
		b.append("\n\t\tbutton : \"").append(triggerButton.getPath()).append("\",");
		b.append(settings.toScript(getLocale()));
		b.append("\n});");
		return b.toString();
	}
}
