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
package org.apache.wicket.markup.html.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;


/**
 * Abstract base class for all choice (html select) options.
 * <p>
 *     This component uses String concatenation to keep its memory footprint light.
 *     Use Select, SelectOptions and SelectOption from wicket-extensions for more
 *     sophisticated needs.
 * </p>
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 * 
 * @param <T>
 *            The model object type
 * 
 * @param <E>
 *            class of a single element in the choices list
 */
public abstract class AbstractChoice<T, E> extends FormComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * An enumeration of possible positions of the label for a choice
	 */
	public enum LabelPosition
	{
		/**
		 * will render the label before the choice
		 */
		BEFORE {
			@Override
			void before(AppendingStringBuffer buffer, String idAttr, StringBuilder extraLabelAttributes, CharSequence renderValue)
			{
				buffer.append("<label for=\"")
				.append(Strings.escapeMarkup(idAttr))
				.append('"')
				.append(extraLabelAttributes)
				.append('>')
				.append(renderValue)
				.append("</label>");
			}
		},

		/**
		 * will render the label after the choice
		 */
		AFTER {
			@Override
			void after(AppendingStringBuffer buffer, String idAttr, StringBuilder extraLabelAttributes, CharSequence renderValue)
			{
				buffer.append("<label for=\"")
				.append(Strings.escapeMarkup(idAttr))
				.append('"')
				.append(extraLabelAttributes)
				.append('>')
				.append(renderValue)
				.append("</label>");
			}
		},

		/**
		 * render the label around and the text will be before the the choice
		 */
		WRAP_BEFORE {
			@Override
			void before(AppendingStringBuffer buffer, String idAttr, StringBuilder extraLabelAttributes, CharSequence renderValue)
			{
				buffer.append("<label")
				.append(extraLabelAttributes)
				.append('>')
				.append(renderValue)
				.append(' ');
			}
			
			@Override
			void after(AppendingStringBuffer buffer, String idAttr, StringBuilder extraLabelAttributes, CharSequence renderValue)
			{
				buffer.append("</label>");
			}
		},

		/**
		 * render the label around and the text will be after the the choice
		 */
		WRAP_AFTER {
			@Override
			void before(AppendingStringBuffer buffer, String idAttr, StringBuilder extraLabelAttributes, CharSequence renderValue)
			{
				buffer.append("<label")
				.append(extraLabelAttributes)
				.append('>');
			}
			
			@Override
			void after(AppendingStringBuffer buffer, String idAttr, StringBuilder extraLabelAttributes, CharSequence renderValue)
			{
				buffer.append(' ')
				.append(renderValue)
				.append("</label>");
			}
		};
		
		void before(AppendingStringBuffer buffer, String idAttr, StringBuilder extraLabelAttributes, CharSequence renderValue) {
		}
		
		void after(AppendingStringBuffer buffer, String idAttr, StringBuilder extraLabelAttributes, CharSequence renderValue) {
		}
	}

	/** The list of objects. */
	private IModel<? extends List<? extends E>> choices;

	/** The renderer used to generate display/id values for the objects. */
	private IChoiceRenderer<? super E> renderer;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 */
	public AbstractChoice(final String id)
	{
		this(id, new ListModel<>(new ArrayList<E>()), new ChoiceRenderer<E>());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AbstractChoice(final String id, final List<? extends E> choices)
	{
		this(id, new ListModel<>(choices), new ChoiceRenderer<E>());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param renderer
	 *            The rendering engine
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AbstractChoice(final String id, final List<? extends E> choices,
		final IChoiceRenderer<? super E> renderer)
	{
		this(id, new ListModel<>(choices), renderer);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AbstractChoice(final String id, IModel<T> model, final List<? extends E> choices)
	{
		this(id, model, new ListModel<>(choices), new ChoiceRenderer<>());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The drop down choices
	 * @param renderer
	 *            The rendering engine
	 */
	public AbstractChoice(final String id, IModel<T> model, final List<? extends E> choices,
		final IChoiceRenderer<? super E> renderer)
	{
		this(id, model, new ListModel<>(choices), renderer);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AbstractChoice(final String id, final IModel<? extends List<? extends E>> choices)
	{
		this(id, choices, new ChoiceRenderer<E>());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param renderer
	 *            The rendering engine
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AbstractChoice(final String id, final IModel<? extends List<? extends E>> choices,
		final IChoiceRenderer<? super E> renderer)
	{
		super(id);
		this.choices = wrap(choices);
		setChoiceRenderer(renderer);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AbstractChoice(final String id, IModel<T> model,
		final IModel<? extends List<? extends E>> choices)
	{
		this(id, model, choices, new ChoiceRenderer<>());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param renderer
	 *            The rendering engine
	 * @param choices
	 *            The drop down choices
	 */
	public AbstractChoice(final String id, IModel<T> model,
		final IModel<? extends List<? extends E>> choices, final IChoiceRenderer<? super E> renderer)
	{
		super(id, model);
		this.choices = wrap(choices);
		setChoiceRenderer(renderer);
	}

	/**
	 * @return The collection of object that this choice has
	 */
	public final List<? extends E> getChoices()
	{
		IModel<? extends List<? extends E>> choicesModel = getChoicesModel();
		List<? extends E> choices = (choicesModel != null) ? choicesModel.getObject() : null;
		if (choices == null)
		{
			throw new NullPointerException(
				"List of choices is null - Was the supplied 'Choices' model empty?");
		}
		return choices;
	}

	/**
	 * @return The model with the choices for this component
	 */
	public IModel<? extends List<? extends  E>> getChoicesModel()
	{
		return this.choices;
	}

	/**
	 * Sets the list of choices
	 * 
	 * @param choices
	 *            model representing the list of choices
	 * @return this for chaining
	 */
	public final AbstractChoice<T, E> setChoices(IModel<? extends List<? extends E>> choices)
	{
		if (this.choices != null && this.choices != choices)
		{
			if (isVersioned())
			{
				addStateChange();
			}
		}
		this.choices = wrap(choices);
		return this;
	}

	/**
	 * Sets the list of choices.
	 * 
	 * @param choices
	 *            the list of choices
	 * @return this for chaining
	 */
	public final AbstractChoice<T, E> setChoices(List<? extends E> choices)
	{
		if ((this.choices != null))
		{
			if (isVersioned())
			{
				addStateChange();
			}
		}
		this.choices = new ListModel<>(choices);
		return this;
	}

	/**
	 * @return The IChoiceRenderer used for rendering the data objects
	 */
	public final IChoiceRenderer<? super E> getChoiceRenderer()
	{
		return renderer;
	}

	/**
	 * Set the choice renderer to be used.
	 * 
	 * @param renderer
	 *              The IChoiceRenderer used for rendering the data objects
	 * @return this for chaining
	 */
	public final AbstractChoice<T, E> setChoiceRenderer(IChoiceRenderer<? super E> renderer)
	{
		if (renderer == null)
		{
			renderer = new ChoiceRenderer<>();
		}
		this.renderer = renderer;
		return this;
	}

	@Override
	protected void detachModel()
	{
		super.detachModel();

		if (choices != null)
		{
			choices.detach();
		}
	}

	/**
	 * Get a default choice to be rendered additionally to the choices available in the model.
	 * 
	 * @param selectedValue
	 *            The currently selected value
	 * @return Any default choice, such as "Choose One", depending on the subclass
	 * @see #setChoices(IModel)
	 */
	protected CharSequence getDefaultChoice(final String selectedValue)
	{
		return "";
	}

	/**
	 * Gets whether the given value represents the current selection.
	 * 
	 * @param object
	 *            The object to check
	 * @param index
	 *            The index in the choices collection this object is in.
	 * @param selected
	 *            The currently selected string value
	 * @return Whether the given value represents the current selection
	 */
	protected abstract boolean isSelected(final E object, int index, String selected);

	/**
	 * Gets whether the given value is disabled. This default implementation always returns false.
	 * 
	 * @param object
	 *            The object to check
	 * @param index
	 *            The index in the choices collection this object is in.
	 * @param selected
	 *            The currently selected string value
	 * @return Whether the given value represents the current selection
	 */
	protected boolean isDisabled(final E object, int index, String selected)
	{
		return false;
	}

	/**
	 * Handle the container's body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		List<? extends E> choices = getChoices();
		final AppendingStringBuffer buffer = new AppendingStringBuffer((choices.size() * 50) + 16);
		final String selectedValue = getValue();

		// Append default option
		buffer.append(getDefaultChoice(selectedValue));

		for (int index = 0; index < choices.size(); index++)
		{
			final E choice = choices.get(index);
			appendOptionHtml(buffer, choice, index, selectedValue);
		}

		buffer.append('\n');
		replaceComponentTagBody(markupStream, openTag, buffer);
	}

	/**
	 * Generates and appends html for a single choice into the provided buffer
	 * 
	 * @param buffer
	 *            Appending string buffer that will have the generated html appended
	 * @param choice
	 *            Choice object
	 * @param index
	 *            The index of this option
	 * @param selected
	 *            The currently selected string value
	 */
	protected void appendOptionHtml(AppendingStringBuffer buffer, E choice, int index, String selected)
	{
		CharSequence renderValue = renderValue(choice);

		buffer.append("\n<option ");
		setOptionAttributes(buffer, choice, index, selected);
		buffer.append('>');
		buffer.append(renderValue);
		buffer.append("</option>");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	CharSequence renderValue(E choice)
	{
		Object objectValue = renderer.getDisplayValue(choice);
		Class<?> objectClass = (objectValue == null ? null : objectValue.getClass());

		String displayValue = "";
		if (objectClass != null && objectClass != String.class)
		{
			IConverter converter = getConverter(objectClass);
			displayValue = converter.convertToString(objectValue, getLocale());
		}
		else if (objectValue != null)
		{
			displayValue = objectValue.toString();
		}
		
		if (localizeDisplayValues())
		{
			displayValue = getLocalizer().getString(getId() + "." + displayValue, this, "");
			if (Strings.isEmpty(displayValue)) {
				displayValue = getLocalizer().getString(displayValue, this, displayValue);
			}
		}
		
		if (getEscapeModelStrings())
		{
			return escapeOptionHtml(displayValue);
		}
		
		return displayValue;
	}

	/**
	 * Sets the attributes of a single choice into the provided buffer.
	 *
	 * @param buffer
	 *            Appending string buffer that will have the generated html appended
	 * @param choice
	 *            Choice object
	 * @param index
	 *            The index of this option
	 * @param selected
	 *            The currently selected string value
	 */
	protected void setOptionAttributes(AppendingStringBuffer buffer, E choice, int index, String selected)
	{
		if (isSelected(choice, index, selected))
		{
			buffer.append("selected=\"selected\" ");
		}

		if (isDisabled(choice, index, selected))
		{
			buffer.append("disabled=\"disabled\" ");
		}

		buffer.append("value=\"");
		buffer.append(Strings.escapeMarkup(renderer.getIdValue(choice, index)));
		buffer.append('"');
	}

	/**
	 * Method to override if you want special escaping of the options html.
	 * 
	 * @param displayValue
	 * @return The escaped display value
	 */
	protected CharSequence escapeOptionHtml(String displayValue)
	{
		return Strings.escapeMarkup(displayValue);
	}

	/**
	 * Override this method if you want to localize the display values of the generated options. By
	 * default false is returned so that the display values of options are not tested if they have a
	 * i18n key.
	 * 
	 * @return true If you want to localize the display values, default == false
	 */
	protected boolean localizeDisplayValues()
	{
		return false;
	}

	@Override
	public final FormComponent<T> setType(Class<?> type)
	{
		throw new UnsupportedOperationException(
			"This class does not support type-conversion because it is performed "
				+ "exclusively by the IChoiceRenderer assigned to this component");
	}
	
	@Override
	protected void onDetach()
	{
		renderer.detach();
		
		super.onDetach();
	};
}
