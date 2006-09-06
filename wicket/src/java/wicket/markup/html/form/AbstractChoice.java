/*
 * $Id: AbstractChoice.java 4827 2006-03-08 12:45:16 -0800 (Wed, 08 Mar 2006)
 * joco01 $ $Revision$ $Date: 2006-03-08 12:45:16 -0800 (Wed, 08 Mar
 * 2006) $
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
package wicket.markup.html.form;

import java.util.ArrayList;
import java.util.List;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;
import wicket.version.undo.Change;

/**
 * Abstract base class for all choice (html select) options.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 * @param <M>
 *            class of the model object, usually the same as <code>E</code>
 * @param <E>
 *            class of a single element in the choices list
 */
abstract class AbstractChoice<M, E> extends FormComponent<M>
{
	/** The list of objects. */
	private IModel<List<E>> choices;

	/** The renderer used to generate display/id values for the objects. */
	private IChoiceRenderer<E> renderer;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public AbstractChoice(MarkupContainer parent, final String id)
	{
		this(parent, id, new Model<List<E>>(new ArrayList<E>()), new ChoiceRenderer<E>());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public AbstractChoice(MarkupContainer parent, final String id, final List<E> choices)
	{
		this(parent, id, new Model<List<E>>(choices), new ChoiceRenderer<E>());
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
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public AbstractChoice(MarkupContainer parent, final String id, final List<E> choices,
			final IChoiceRenderer<E> renderer)
	{
		this(parent, id, new Model<List<E>>(choices), renderer);
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
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public AbstractChoice(MarkupContainer parent, final String id, IModel<M> model,
			final List<E> choices)
	{
		this(parent, id, model, new Model<List<E>>(choices), new ChoiceRenderer<E>());
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
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public AbstractChoice(MarkupContainer parent, final String id, IModel<M> model,
			final List<E> choices, final IChoiceRenderer<E> renderer)
	{
		this(parent, id, model, new Model<List<E>>(choices), renderer);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public AbstractChoice(MarkupContainer parent, final String id, final IModel<List<E>> choices)
	{
		this(parent, id, choices, new ChoiceRenderer<E>());
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
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public AbstractChoice(MarkupContainer parent, final String id, final IModel<List<E>> choices,
			final IChoiceRenderer<E> renderer)
	{
		super(parent, id);
		this.choices = choices;
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
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public AbstractChoice(MarkupContainer parent, final String id, IModel<M> model,
			final IModel<List<E>> choices)
	{
		this(parent, id, model, choices, new ChoiceRenderer<E>());
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
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public AbstractChoice(MarkupContainer parent, final String id, IModel<M> model,
			final IModel<List<E>> choices, final IChoiceRenderer<E> renderer)
	{
		super(parent, id, model);
		this.choices = choices;
		setChoiceRenderer(renderer);
	}


	/**
	 * @return The collection of object that this choice has
	 */
	public List<E> getChoices()
	{
		List<E> choices = (this.choices != null) ? this.choices.getObject() : null;
		if (choices == null)
		{
			throw new NullPointerException(
					"List of choices is null - Was the supplied 'Choices' model empty?");
		}
		return choices;
	}


	/**
	 * Sets the list of choices
	 * 
	 * @param choices
	 *            model representing the list of choices
	 */
	public final void setChoices(IModel<List<E>> choices)
	{
		if (this.choices != null && this.choices != choices)
		{
			if (isVersioned())
			{
				addStateChange(new ChoicesListChange());
			}
		}
		this.choices = choices;
	}

	/**
	 * Sets the list of choices.
	 * 
	 * @param choices
	 *            the list of choices
	 */
	public final void setChoices(List<E> choices)
	{
		if ((this.choices != null))
		{
			if (isVersioned())
			{
				addStateChange(new ChoicesListChange());
			}
		}
		this.choices = new Model<List<E>>(choices);
	}

	/**
	 * @return The IChoiceRenderer used for rendering the data objects
	 */
	public final IChoiceRenderer<E> getChoiceRenderer()
	{
		return renderer;
	}

	/**
	 * Set the choice renderer to be used.
	 * 
	 * @param renderer
	 */
	public final void setChoiceRenderer(IChoiceRenderer<E> renderer)
	{
		if (renderer == null)
		{
			renderer = new ChoiceRenderer<E>();
		}
		this.renderer = renderer;
	}

	/**
	 * @see wicket.Component#detachModel()
	 */
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
	 * 
	 * @param selected
	 *            The object that's currently selected
	 * @return Any default choice, such as "Choose One", depending on the
	 *         subclass
	 */
	protected CharSequence getDefaultChoiceMarkup(final Object selected)
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
	 * Handle the container's body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		List<E> choices = getChoices();
		final AppendingStringBuffer buffer = new AppendingStringBuffer((choices.size() * 50) + 16);
		final String selected = getValue();

		// Append default option
		buffer.append(getDefaultChoiceMarkup(selected));

		for (int index = 0; index < choices.size(); index++)
		{
			final E choice = choices.get(index);
			appendOptionHtml(buffer, choice, index, selected);
		}

		buffer.append("\n");
		replaceComponentTagBody(markupStream, openTag, buffer);
	}

	/**
	 * Generats and appends html for a single choice into the provided buffer
	 * 
	 * @param buffer
	 *            Appending string buffer that will have the generated html
	 *            appended
	 * @param choice
	 *            Choice object
	 * @param index
	 *            The index of this option
	 * @param selected
	 *            The currently selected string value
	 */
	protected void appendOptionHtml(AppendingStringBuffer buffer, E choice, int index,
			String selected)
	{
		Object objectValue = renderer.getDisplayValue(choice);
		Class objectClass = objectValue == null ? null : objectValue.getClass();
		final String displayValue = getConverter(objectClass).convertToString(objectValue,
				getLocale());
		buffer.append("\n<option ");
		if (isSelected(choice, index, selected))
		{
			buffer.append("selected=\"selected\" ");
		}
		buffer.append("value=\"");
		buffer.append(renderer.getIdValue(choice, index));
		buffer.append("\">");

		String display = displayValue;
		if (localizeDisplayValues())
		{
			display = getLocalizer().getString(displayValue, this, displayValue);
		}
		CharSequence escaped = Strings.escapeMarkup(display, false, true);
		buffer.append(escaped);
		buffer.append("</option>");
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#supportsPersistence()
	 */
	@Override
	protected boolean supportsPersistence()
	{
		return true;
	}

	/**
	 * Override this method if you want to localize the display values of the
	 * generated options. By default false is returned so that the display
	 * values of options are not tested if they have a i18n key.
	 * 
	 * @return true If you want to localize the display values, default == false
	 */
	protected boolean localizeDisplayValues()
	{
		return false;
	}

	/**
	 * Change object to represent the change of the choices property
	 * 
	 * @author ivaynberg
	 */
	private class ChoicesListChange extends Change
	{
		private static final long serialVersionUID = 1L;

		private final IModel<List<E>> oldChoices;

		/**
		 * Construct.
		 */
		public ChoicesListChange()
		{
			oldChoices = choices;
		}

		/**
		 * @see wicket.version.undo.Change#undo()
		 */
		@Override
		public void undo()
		{
			choices = oldChoices;
		}

		/**
		 * Make debugging easier
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "ChoiceListChange[component: " + getPath() + ", old choices: " + oldChoices
					+ "]";
		}


	}

}
