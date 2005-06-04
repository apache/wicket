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
import wicket.ResourceReference;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.StaticResourceReference;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Datepicker component.
 * TODO doc more
 * <p>
 * This component is based on Dynarch's JSCalendar component, which can be found
 * at <a href="http://www.dynarch.com/">the Dynarch site</a>.
 * </p>
 *
 * @author Eelco Hillenius
 * @author Mihai Bazon (creator of the JSCalendar component)
 */
public class DatePicker extends Panel
{

	// the packaged icon images

	/** button icon for the date picker; refers to 'calendar_icon_1.jpg' in this package. */
	public static final StaticResourceReference BUTTON_ICON_1 =
		new StaticResourceReference(DatePicker.class, "calendar_icon_1.jpg");

	/** button icon for the date picker; refers to 'calendar_icon_2.jpg' in this package. */
	public static final StaticResourceReference BUTTON_ICON_2 =
		new StaticResourceReference(DatePicker.class, "calendar_icon_2.jpg");

	/** button icon for the date picker; refers to 'calendar_icon_3.jpg' in this package. */
	public static final StaticResourceReference BUTTON_ICON_3 =
		new StaticResourceReference(DatePicker.class, "calendar_icon_3.jpg");

	// the packages styles (comes with the date picker javascript widget)
	
	/** date picker style aqua. */
	public static final StaticResourceReference STYLE_AQUA =
		new StaticResourceReference(DatePicker.class, "style/aqua/theme.css");

	/** date picker style winter. */
	public static final StaticResourceReference STYLE_WINTER =
		new StaticResourceReference(DatePicker.class, "style/calendar-blue.css");

	/** date picker style blue2. */
	public static final StaticResourceReference STYLE_BLUE =
		new StaticResourceReference(DatePicker.class, "style/calendar-blue2.css");

	/** date picker style summer. */
	public static final StaticResourceReference STYLE_SUMMER =
		new StaticResourceReference(DatePicker.class, "style/calendar-brown.css");

	/** date picker style green. */
	public static final StaticResourceReference STYLE_GREEN =
		new StaticResourceReference(DatePicker.class, "style/calendar-green.css");

	/** date picker style system. */
	public static final StaticResourceReference STYLE_SYSTEM =
		new StaticResourceReference(DatePicker.class, "style/calendar-system.css");

	/** date picker style tas. */
	public static final StaticResourceReference STYLE_TAS =
		new StaticResourceReference(DatePicker.class, "style/calendar-tas.css");

	/** date picker style win2k. */
	public static final StaticResourceReference STYLE_WIN2K =
		new StaticResourceReference(DatePicker.class, "style/calendar-win2k.css");

	/** date picker style win2k-1. */
	public static final StaticResourceReference STYLE_WIN2K_1 =
		new StaticResourceReference(DatePicker.class, "style/calendar-win2k-1.css");

	/** date picker style win2k-2. */
	public static final StaticResourceReference STYLE_WIN2K_2 =
		new StaticResourceReference(DatePicker.class, "style/calendar-win2k-2.css");

	/** date picker style win2k-cold-1. */
	public static final StaticResourceReference STYLE_WIN2K_COLD_1 =
		new StaticResourceReference(DatePicker.class, "style/calendar-win2k-cold-1.css");

	/** date picker style win2k-cold-2. */
	public static final StaticResourceReference STYLE_WIN2K_COLD_2 =
		new StaticResourceReference(DatePicker.class, "style/calendar-win2k-cold-2.css");

	// register dependent images so that they can be loaded by the css files

	static
	{
		new StaticResourceReference(DatePicker.class, "style/menuarrow.gif");
		new StaticResourceReference(DatePicker.class, "style/menuarrow2.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/active-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/dark-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/hover-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/menuarrow.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/normal-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/rowhover-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/status-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/title-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/today-bg.gif");
	}

	/**
	 * Reference to a packaged script file.
	 */
	private final static class DatePickerResourceReference extends WebMarkupContainer
	{
		/**
		 * Construct.
		 * @param id component id
		 * @param file relative location of the packaged file
		 * @param attributeToReplace the attribute to replace of the target tag
		 */
		public DatePickerResourceReference(String id, String file, String attributeToReplace)
		{
			super(id);

			final StaticResourceReference ref = new StaticResourceReference(
					DatePicker.class, file);

			IModel srcReplacement = new Model()
			{
				public Object getObject(Component component)
				{
					String url = getPage().urlFor(ref.getPath());
					return url;
				};
			};
			add(new AttributeModifier(attributeToReplace, true, srcReplacement));
		}

		/**
		 * Construct.
		 * @param id component id
		 * @param resourceReference the reference to the resource
		 * @param attributeToReplace the attribute to replace of the target tag
		 */
		public DatePickerResourceReference(String id,
				final ResourceReference resourceReference,
				String attributeToReplace)
		{
			super(id);

			IModel srcReplacement = new Model()
			{
				public Object getObject(Component component)
				{
					String url = getPage().urlFor(resourceReference.getPath());
					return url;
				};
			};
			add(new AttributeModifier(attributeToReplace, true, srcReplacement));
		}
	}

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

	/** the receiving text field. */
	private final TextField targetTextField;

	/** the button that triggers the popup. */
	private TriggerButton triggerButton;

	/** properties for the javascript datepicker component. */
	private DatePickerProperties datePickerProperties;

	/**
	 * Construct with a default button and style.
	 * @param id the component id
	 * @param targetTextField the receiving text field
	 */
	public DatePicker(String id, TextField targetTextField)
	{
		this(id, targetTextField, BUTTON_ICON_1, STYLE_AQUA);
	}

	/**
	 * Construct with a default style.
	 * @param id the component id
	 * @param targetTextField the receiving text field
	 * @param buttonIcon icon image
	 */
	public DatePicker(String id, TextField targetTextField, ResourceReference buttonIcon)
	{
		this(id, targetTextField, buttonIcon, STYLE_AQUA);
	}

	/**
	 * Construct.
	 * @param id the component id
	 * @param targetTextField the receiving text field
	 * @param buttonIcon icon image
	 * @param style style of this date picker
	 */
	public DatePicker(String id, TextField targetTextField,
			ResourceReference buttonIcon, ResourceReference style)
	{
		super(id);

		if (targetTextField == null)
		{
			throw new NullPointerException("targetTextField must be not null");
		}

		targetTextField.add(new IdAttributeModifier(targetTextField));
		this.targetTextField = targetTextField;

		add(triggerButton = new TriggerButton("trigger", buttonIcon));
		add(new InitScript("script"));
		addToHeader(new DatePickerResourceReference("calendarMain", "calendar.js", "src"));
		addToHeader(new DatePickerResourceReference("calendarSetup", "calendar-setup.js", "src"));
		addToHeader(new DatePickerResourceReference("calendarLanguage", "lang/calendar-en.js", "src"));
		addToHeader(new DatePickerResourceReference("calendarStyle", style, "href"));

		new StaticResourceReference(DatePicker.class, "style/aqua/active-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/dark-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/hover-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/menuarrow.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/normal-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/rowhover-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/status-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/title-bg.gif");
		new StaticResourceReference(DatePicker.class, "style/aqua/today-bg.gif");
	}

	/**
	 * Gets the initilization javascript.
	 * @return the initilization javascript
	 */
	private String getInitScript()
	{
		StringBuffer b = new StringBuffer("\nCalendar.setup(\n{");
		b.append("\n\t\tinputField : \"").append(targetTextField.getPath()).append("\",");
		b.append("\n\t\tbutton : \"").append(triggerButton.getPath()).append("\",");
		DatePickerProperties properties = getDatePickerProperties();
		b.append(properties.toScript());
		b.append("\n});");
		return b.toString();
	}

	/**
	 * Gets the properties for the javascript datepicker component.
	 * @return the properties for the javascript datepicker component
	 */
	public final DatePickerProperties getDatePickerProperties()
	{
		// if null, lazily create the default object
		if (datePickerProperties == null)
		{
			datePickerProperties = new DatePickerProperties();
		}

		return datePickerProperties;
	}

	/**
	 * Sets the properties for the javascript datepicker component.
	 * @param datePickerProperties the properties for the javascript datepicker component
	 * @return This
	 */
	public final DatePicker setDatePickerProperties(DatePickerProperties datePickerProperties)
	{
		this.datePickerProperties = datePickerProperties;
		return this;
	}
}
