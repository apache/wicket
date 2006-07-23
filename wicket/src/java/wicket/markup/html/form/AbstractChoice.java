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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
 */
abstract class AbstractChoice extends FormComponent
{
	/** The list of objects. */
	private IModel choices;

	/** The renderer used to generate display/id values for the objects. */
	private IChoiceRenderer renderer;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @see wicket.Component#Component(String)
	 */
	public AbstractChoice(final String id)
	{
		this(id, new Model(new ArrayList()), new ChoiceRenderer());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(String)
	 */
	public AbstractChoice(final String id, final List choices)
	{
		this(id, new Model((Serializable)choices), new ChoiceRenderer());
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
	 * @see wicket.Component#Component(String)
	 */
	public AbstractChoice(final String id, final List choices, final IChoiceRenderer renderer)
	{
		this(id, new Model((Serializable)choices), renderer);
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
	 * @see wicket.Component#Component(String, IModel)
	 */
	public AbstractChoice(final String id, IModel model, final List choices)
	{
		this(id, model, new Model((Serializable)choices), new ChoiceRenderer());
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
	 * @see wicket.Component#Component(String, IModel)
	 */
	public AbstractChoice(final String id, IModel model, final List choices,
			final IChoiceRenderer renderer)
	{
		this(id, model, new Model((Serializable)choices), renderer);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(String)
	 */
	public AbstractChoice(final String id, final IModel choices)
	{
		this(id, choices, new ChoiceRenderer());
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
	 * @see wicket.Component#Component(String)
	 */
	public AbstractChoice(final String id, final IModel choices, final IChoiceRenderer renderer)
	{
		super(id);
		this.choices = choices;
		this.renderer = renderer;
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
	 * @see wicket.Component#Component(String, IModel)
	 */
	public AbstractChoice(final String id, IModel model, final IModel choices)
	{
		this(id, model, choices, new ChoiceRenderer());
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
	 * @see wicket.Component#Component(String, IModel)
	 */
	public AbstractChoice(final String id, IModel model, final IModel choices,
			final IChoiceRenderer renderer)
	{
		super(id, model);
		this.choices = choices;
		this.renderer = renderer;
	}


	/**
	 * @return The collection of object that this choice has
	 */
	public List getChoices()
	{
		List choices = (this.choices != null) ? (List)this.choices.getObject(this) : null;
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
	public final void setChoices(IModel choices)
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
	public final void setChoices(List choices)
	{
		if ((this.choices != null))
		{
			if (isVersioned())
			{
				addStateChange(new ChoicesListChange());
			}
		}
		this.choices = new Model((Serializable)choices);
	}

	/**
	 * @return The IChoiceRenderer used for rendering the data objects
	 */
	public final IChoiceRenderer getChoiceRenderer()
	{
		return renderer;
	}

	/**
	 * Set the choice renderer to be used.
	 * 
	 * @param renderer
	 */
	public final void setChoiceRenderer(IChoiceRenderer renderer)
	{
		this.renderer = renderer;
	}

	/**
	 * @see wicket.Component#detachModel()
	 */
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
	protected CharSequence getDefaultChoice(final Object selected)
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
	protected abstract boolean isSelected(final Object object, int index, String selected);

	/**
	 * Handle the container's body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		List choices = getChoices();
		final AppendingStringBuffer buffer = new AppendingStringBuffer((choices.size() * 50) + 16);
		final String selected = getValue();

		// Append default option
		buffer.append(getDefaultChoice(selected));

		for (int index = 0; index < choices.size(); index++)
		{
			final Object choice = choices.get(index);
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
	protected void appendOptionHtml(AppendingStringBuffer buffer, Object choice, int index,
			String selected)
	{
		final String displayValue = (String)getConverter().convert(
				renderer.getDisplayValue(choice), String.class);
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

		private final IModel oldChoices;

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
		public void undo()
		{
			choices = oldChoices;
		}

		/**
		 * Make debugging easier
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "ChoiceListChange[component: " + getPath() + ", old choices: " + oldChoices
					+ "]";
		}


	}

}
