/*
 * $Id$
 * $Revision$ $Date$
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
import java.util.Collection;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.form.model.ChoiceList;
import wicket.markup.html.form.model.IChoice;
import wicket.markup.html.form.model.IChoiceList;

/**
 * Abstract base class for all choice (html select) options.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
abstract class AbstractChoice extends FormComponent
{
	/** String to display when the selected value is null and nullValid is false. */
	private static final String CHOOSE_ONE = "Choose One";

	/** Serial Version ID. */
	private static final long serialVersionUID = -8334966481181600604L;

	/** Is the null value a valid value? */
	private boolean nullValid = false;

	/** The list of choices. */
	private IChoiceList choices;

	/**
	 * @param name
	 *            See Component constructor
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(String)
	 */
	public AbstractChoice(String name, final IChoiceList choices)
	{
		super(name);
		this.choices = choices;
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(String)
	 */
	public AbstractChoice(String name, final Collection choices)
	{
		this(name, new ChoiceList(choices));
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public AbstractChoice(String name, Serializable object, final Collection choices)
	{
		this(name, object, new ChoiceList(choices));
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param choices
	 *            The drop down choices
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public AbstractChoice(String name, Serializable object, final IChoiceList choices)
	{
		super(name, object);
		this.choices = choices;
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param expression
	 *            See Component constructor
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public AbstractChoice(String name, Serializable object, String expression,
			final Collection choices)
	{
		this(name, object, new ChoiceList(choices));
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param expression
	 *            See Component constructor
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public AbstractChoice(String name, Serializable object, String expression,
			final IChoiceList choices)
	{
		super(name, object, expression);
		this.choices = choices;
	}

	/**
	 * Gets the list of choices.
	 * 
	 * @return The list of choices
	 */
	public IChoiceList getChoices()
	{
		choices.attach();
		return this.choices;
	}

	/**
	 * Is the <code>null</code> value a valid value?
	 * 
	 * @return <code>true</code> when the <code>null</code> value is
	 *         allowed.
	 */
	public boolean isNullValid()
	{
		return nullValid;
	}

	/**
	 * Is the <code>null</code> value a valid value?
	 * 
	 * @param emptyAllowed
	 *            The emptyAllowed to set.
	 */
	public void setNullValid(boolean emptyAllowed)
	{
		this.nullValid = emptyAllowed;
	}

	/**
	 * @see wicket.Component#detachModel()
	 */
	protected void detachModel()
	{
		super.detachModel();
		choices.detach();
	}

	/**
	 * Gets whether the given value represents the current selection.
	 * 
	 * @param currentValue
	 *            The current list value
	 * @return Whether the given value represents the current selection
	 */
	protected boolean isSelected(final Object currentValue)
	{
		return currentValue == getModelObject();
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "select");
		super.onComponentTag(tag);
	}

	/**
	 * Handle the container's body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	protected final void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		final StringBuffer options = new StringBuffer();
		final Object selected = getModelObject();
		final IChoiceList choices = getChoices();

		// Is null a valid selection value?
		if (nullValid)
		{
			// Null is valid, so look up the value for it
			final String option = getLocalizer().getString(getName() + ".null", this, "");

			// Add option tag
			options.append("\n<option");

			// If null is selected, indicate that
			if (selected == null)
			{
				options.append(" selected=\"selected\"");
			}

			// Add body of option tag
			options.append(" value=\"\">").append(option).append("</option>");
		}
		else
		{
			// Null is not valid. Is it selected anyway?
			if (selected == null)
			{
				// Force the user to pick a non-null value
				final String option = getLocalizer().getString(getName() + ".null", this,
						CHOOSE_ONE);
				options.append("\n<option selected=\"selected\" value=\"\">").append(option)
						.append("</option>");
			}
		}

		for (int i = 0; i < choices.size(); i++)
		{
			final IChoice choice = choices.get(i);
			if (choice != null)
			{
				final String displayValue = choice.getDisplayValue();
				options.append("\n<option ");
				if (isSelected(choice.getObject()))
				{
					options.append("selected=\"selected\"");
				}
				options.append("value=\"");
				options.append(choice.getId());
				options.append("\">");
				options.append(getLocalizer().getString(getName() + "." + displayValue, this,
						displayValue));
				options.append("</option>");
			}
			else
			{
				throw new IllegalArgumentException("Choice list has null value at index " + i);
			}
		}

		options.append("\n");
		replaceComponentTagBody(markupStream, openTag, options.toString());
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#supportsPersistence()
	 */
	protected boolean supportsPersistence()
	{
		return true;
	}

	/**
	 * Updates the model of this component from the request.
	 * 
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	protected abstract void updateModel();
}