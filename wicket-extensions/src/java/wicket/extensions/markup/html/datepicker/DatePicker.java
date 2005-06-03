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

import java.util.Date;

import wicket.Application;
import wicket.AttributeModifier;
import wicket.Component;
import wicket.MarkupContainer;
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
 * @author Mihai Bazon (he created the JSCalendar component)
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
	 * Textfield for the date picker.
	 */
	private final static class DatePickerTextField extends TextField
	{
		/** model for substituting the id attribute of the text field. */
		private final class IdModel extends Model
		{
			/**
			 * @see wicket.model.IModel#getObject(wicket.Component)
			 */
			public Object getObject(Component component)
			{
				return DatePickerTextField.this.getPath();
			}
		}

		/**
		 * Construct.
		 * @param id component id
		 * @param type type for field validation and conversion
		 */
		public DatePickerTextField(String id, Class type)
		{
			super(id, type);
			add(new IdAttributeModifier(this));
		}

		/**
		 * Construct.
		 * @param id component id
		 * @param model the model
		 * @param type type for field validation and conversion
		 */
		public DatePickerTextField(String id, IModel model, Class type)
		{
			super(id, model, type);
			add(new IdAttributeModifier(this));
		}

		/**
		 * @see wicket.Component#initModel()
		 */
		protected IModel initModel()
		{
			// do not use our own model, but use the model of DatePicker instead.
			// We need to do a little trick though, as the parent model may be a
			// CompoundPropertyModel. In that case, setObject would be called with THIS
			// component, resulting in Ognl trying to resolve expression 'dateInput'
			// on the model. If we dispatch get/setObject to the parent alltogether, we
			// will never have that kind of issues

			return new Model()
			{
				/**
				 * Returns the model object of the parent.
				 * @see wicket.model.IModel#getObject(wicket.Component)
				 */
				public Object getObject(Component component)
				{
					MarkupContainer parent = getParent();
					IModel parentModel = parent.getModel();
					if (parentModel != null)
					{
						return parentModel.getObject(parent);
					}
					return null;
				};

				/**
				 * Sets the object on the parents' model.
				 * @see wicket.model.IModel#setObject(wicket.Component, java.lang.Object)
				 */
				public void setObject(Component component, Object object)
				{
					MarkupContainer parent = getParent();
					IModel parentModel = parent.getModel();
					if (parentModel != null)
					{
						parentModel.setObject(parent, object);						
					}
				};

			};
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
					resourceReference.bind(getApplication());
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

	private static final StaticResourceReference CALENDAR_ICON_1 =
		new StaticResourceReference(DatePicker.class, "calendar_icon_1.jpg");

	private static final StaticResourceReference CALENDAR_ICON_2 =
		new StaticResourceReference(DatePicker.class, "calendar_icon_2.jpg");

	private static final StaticResourceReference CALENDAR_ICON_3 =
		new StaticResourceReference(DatePicker.class, "calendar_icon_3.jpg");

	/** the text field. */
	private final DatePickerTextField datePickerTextField;

	/** the button that triggers the popup. */
	private TriggerButton triggerButton;

	/** properties for the javascript datepicker component. */
	private DatePickerProperties datePickerProperties;

	/**
	 * Construct.
	 * @param id the component id
	 */
	public DatePicker(String id)
	{
		super(id);
		add(datePickerTextField = new DatePickerTextField("dateInput", Date.class));
		init();
	}

	/**
	 * Construct.
	 * @param id the component id
	 * @param model the model
	 */
	public DatePicker(String id, IModel model)
	{
		super(id, model);
		add(datePickerTextField = new DatePickerTextField("dateInput", model, Date.class));
		init();
	}

	/**
	 * Add common components.
	 */
	private void init()
	{
		add(triggerButton = new TriggerButton("trigger", CALENDAR_ICON_1));
		add(new InitScript("script"));
		addToHeader(new ResourceReference("calendarMain", "calendar.js", "src"));
		addToHeader(new ResourceReference("calendarSetup", "calendar-setup.js", "src"));
		addToHeader(new ResourceReference("calendarLanguage", "lang/calendar-en.js", "src"));
		addToHeader(new ResourceReference("calendarStyle", "style/aqua/theme.css", "href"));

		// register packaged images as static available resources
		Application application = getApplication();

		new StaticResourceReference(DatePicker.class, "style/aqua/active-bg.gif").bind(application);
		new StaticResourceReference(DatePicker.class, "style/aqua/dark-bg.gif").bind(application);
		new StaticResourceReference(DatePicker.class, "style/aqua/hover-bg.gif").bind(application);
		new StaticResourceReference(DatePicker.class, "style/aqua/menuarrow.gif").bind(application);
		new StaticResourceReference(DatePicker.class, "style/aqua/normal-bg.gif").bind(application);
		new StaticResourceReference(DatePicker.class, "style/aqua/rowhover-bg.gif").bind(application);
		new StaticResourceReference(DatePicker.class, "style/aqua/status-bg.gif").bind(application);
		new StaticResourceReference(DatePicker.class, "style/aqua/title-bg.gif").bind(application);
		new StaticResourceReference(DatePicker.class, "style/aqua/today-bg.gif").bind(application);
	}

	/**
	 * Gets the initilization javascript.
	 * @return the initilization javascript
	 */
	private String getInitScript()
	{
		StringBuffer b = new StringBuffer("\nCalendar.setup(\n{");
		b.append("\n\t\tinputField : \"").append(datePickerTextField.getPath()).append("\",");
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

	/**
	 * Reference to a packaged script file.
	 */
	private final static class ResourceReference extends WebMarkupContainer
	{
		/**
		 * Construct.
		 * @param id
		 * @param file relative location of the packaged file
		 * @param attributeToReplace the attribute to replace of the target tag
		 */
		public ResourceReference(String id, String file, String attributeToReplace)
		{
			super(id);

			final StaticResourceReference ref = new StaticResourceReference(
					DatePicker.class, file);

			IModel srcReplacement = new Model()
			{
				public Object getObject(Component component)
				{
					ref.bind(getApplication());
					String url = getPage().urlFor(ref.getPath());
					return url;
				};
			};
			add(new AttributeModifier(attributeToReplace, true, srcReplacement));
		}
	}
}
