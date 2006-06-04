/*
 * $Id$ $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.datepicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.MarkupContainer;
import wicket.ResourceReference;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.resources.JavaScriptReference;
import wicket.markup.html.resources.StyleSheetReference;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.convert.IConverter;
import wicket.util.convert.converters.DateConverter;
import wicket.util.string.AppendingStringBuffer;

/**
 * Datepicker component.
 * <p>
 * Link your datepicker to a textfield like this:
 * </p>
 * <p>
 * (Java)
 * 
 * <pre>
 * TextField dateField = new TextField(&quot;dateField&quot;, Date.class);
 * add(dateField);
 * add(new DatePicker(&quot;dateFieldPicker&quot;, dateField));
 * </pre>
 * 
 * (html)
 * 
 * <pre>
 *           &lt;input type=&quot;text&quot; wicket:id=&quot;dateField&quot; size=&quot;10&quot; /&gt;
 *           &lt;span wicket:id=&quot;dateFieldPicker&quot; /&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * Your target doesn't have to be a text field however, attach to any tag that
 * is supported by JSCalendar.
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
 * @author Eelco Hillenius
 * @author Mihai Bazon (creator of the JSCalendar component)
 */
public class DatePicker extends Panel
{
	/**
	 * Outputs the Javascript initialization code.
	 */
	private final class InitScript extends WebComponent
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @see wicket.Component#Component(MarkupContainer,String)
		 */
		public InitScript(MarkupContainer parent, final String id)
		{
			super(parent, id);
		}

		/**
		 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
		 *      wicket.markup.ComponentTag)
		 */
		@Override
		protected void onComponentTagBody(final MarkupStream markupStream,
				final ComponentTag openTag)
		{
			replaceComponentTagBody(markupStream, openTag, getInitScript());
		}
	}

	/**
	 * Attribute modifier that modifies/ adds an attribute with value of the
	 * given component's path.
	 */
	private final static class PathAttributeModifier extends AttributeModifier
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param attribute
		 *            the attribute to modify
		 * @param pathProvider
		 *            the component that provides the path
		 */
		public PathAttributeModifier(String attribute, final Component pathProvider)
		{
			super(attribute, true, new Model<String>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String getObject(Component component)
				{
					// do this lazily, so we know for sure we have the whole
					// path including the page etc.
					return pathProvider.getPath();
				}
			});
		}
	}

	/**
	 * Button that triggers the popup.
	 */
	private final static class TriggerButton extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 *            component id
		 * @param resourceReference
		 *            button icon reference
		 */
		public TriggerButton(MarkupContainer parent, final String id,
				final wicket.ResourceReference resourceReference)
		{
			super(parent, id);
			add(new PathAttributeModifier("id", this));
			IModel<CharSequence> srcReplacement = new Model<CharSequence>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public CharSequence getObject(Component component)
				{
					return urlFor(resourceReference);
				};
			};
			add(new AttributeModifier("src", true, srcReplacement));
		}
	}

	private static final long serialVersionUID = 1L;

	private DateConverter dateConverter;

	/** settings for the javascript datepicker component. */
	private final DatePickerSettings settings;

	/** the receiving component. */
	private final Component target;

	/** the button that triggers the popup. */
	private TriggerButton triggerButton;

	/**
	 * Construct with a default button and style.
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            the component id
	 * @param target
	 *            the receiving component
	 */
	public DatePicker(MarkupContainer parent, final String id, Component target)
	{
		this(parent, id, target, new DatePickerSettings());
	}

	/**
	 * Construct with a default button and style.
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            the component id
	 * @param label
	 *            the label for target component.
	 * @param target
	 *            the receiving component
	 */
	public DatePicker(MarkupContainer parent, final String id, Component label, Component target)
	{
		this(parent, id, label, target, new DatePickerSettings());
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            the component id
	 * @param label
	 *            the label component (may be null)
	 * @param target
	 *            the receiving component
	 * @param settings
	 *            datepicker properties
	 */
	public DatePicker(MarkupContainer parent, final String id, final Component label,
			final Component target, final DatePickerSettings settings)
	{
		super(parent, id);

		if (settings == null)
		{
			throw new IllegalArgumentException(
					"Settings must be non null when using this constructor");
		}

		this.settings = settings;

		if (target == null)
		{
			throw new IllegalArgumentException("Target must be not null");
		}

		target.add(new PathAttributeModifier("id", target));
		this.target = target;

		if (label != null)
		{
			label.add(new PathAttributeModifier("for", target));
		}
		triggerButton = new TriggerButton(this, "trigger", settings.getIcon());
		new InitScript(this, "script");
		new JavaScriptReference(this, "calendarMain", DatePicker.class, "calendar.js");
		new JavaScriptReference(this, "calendarSetup", DatePicker.class, "calendar-setup.js");
		new JavaScriptReference(this, "calendarLanguage", new Model<ResourceReference>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public ResourceReference getObject(Component component)
			{
				return settings.getLanguage(DatePicker.this.getLocale());
			}
		});
		new StyleSheetReference(this, "calendarStyle", settings.getStyle());
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            the component id
	 * @param target
	 *            the receiving component
	 * @param settings
	 *            datepicker properties
	 */
	public DatePicker(MarkupContainer parent, final String id, final Component target,
			final DatePickerSettings settings)
	{
		this(parent, id, null, target, settings);
	}

	/**
	 * Sets the date converter to use for generating the javascript format
	 * string. If this is not set or set to null the default DateConverter will
	 * be used.
	 * 
	 * @param dateConverter
	 */
	public void setDateConverter(DateConverter dateConverter)
	{
		this.dateConverter = dateConverter;
	}

	/**
	 * Gets the initilization javascript.
	 * 
	 * @return the initilization javascript
	 */
	private CharSequence getInitScript()
	{
		String targetId = target.getPath();
		AppendingStringBuffer b = new AppendingStringBuffer("\nCalendar.setup(\n{");
		b.append("\n\t\tinputField : \"").append(targetId).append("\",");
		b.append("\n\t\tbutton : \"").append(triggerButton.getPath()).append("\",");

		String pattern = null;
		if (dateConverter == null)
		{
			// TODO this should be much easier and nicer to do in 2.0
			IConverter typeConverter = target.getConverter(Date.class);
			if (typeConverter instanceof DateConverter)
			{
				dateConverter = (DateConverter)typeConverter;
			}
			if (dateConverter == null)
			{
				dateConverter = new DateConverter();
			}
		}
		DateFormat df = dateConverter.getDateFormat(target.getLocale());
		if (df instanceof SimpleDateFormat)
		{
			pattern = ((SimpleDateFormat)df).toPattern();
		}
		b.append(settings.toScript(target.getLocale(), pattern));
		int last = b.length() - 1;
		if (',' == b.charAt(last))
		{
			b.deleteCharAt(last);
		}
		b.append("\n});");
		return b;
	}
}
