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
package wicket.examples.customcomponents;

import java.util.Date;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Datepicker component.
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
	}

	/**
	 * Button that triggers the popup.
	 */
	private final static class TriggerButton extends WebMarkupContainer
	{

		/**
		 * Construct.
		 * @param id component id
		 */
		public TriggerButton(String id)
		{
			super(id);
			add(new IdAttributeModifier(this));
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

	/** the text field. */
	private final DatePickerTextField datePickerTextField;

	/** the button that triggers the popup. */
	private final TriggerButton triggerButton;

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
		add(triggerButton = new TriggerButton("trigger"));
		add(new InitScript("script"));
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
		add(triggerButton = new TriggerButton("trigger"));
		add(new InitScript("script"));
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
}
