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
	/** Serial Version ID. */
	private static final long serialVersionUID = -8334966481181600604L;

	/** The list of choices. */
	private IChoiceList choices;

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
	 * @see wicket.Component#detachModel()
	 */
	protected void detachModel()
	{
		super.detachModel();
		choices.detach();
	}

	/**
	 * @param selected The object that's currently selected
	 * @return Any default choice, such as "Choose One", depending on the
	 *         subclass
	 */
	protected String getDefaultChoice(final Object selected)
	{
		return "";
	}

	/**
	 * Gets whether the given value represents the current selection.
	 * 
	 * @param choice
	 *            The choice to check
	 * @return Whether the given value represents the current selection
	 */
	protected abstract boolean isSelected(final IChoice choice);

	/**
	 * Handle the container's body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		final StringBuffer buffer = new StringBuffer();
		final Object selected = getModelObject();
		final IChoiceList choices = getChoices();

		// Append default option
		buffer.append(getDefaultChoice(selected));

		for (int i = 0; i < choices.size(); i++)
		{
			final IChoice choice = choices.get(i);
			if (choice != null)
			{
				final String displayValue = choice.getDisplayValue();
				buffer.append("\n<option ");
				if (isSelected(choice))
				{
					buffer.append("selected=\"selected\"");
				}
				buffer.append("value=\"");
				buffer.append(choice.getId());
				buffer.append("\">");
				buffer.append(getLocalizer().getString(getName() + "." + displayValue, this,
						displayValue));
				buffer.append("</option>");
			}
			else
			{
				throw new IllegalArgumentException("Choice list has null value at index " + i);
			}
		}

		buffer.append("\n");
		replaceComponentTagBody(markupStream, openTag, buffer.toString());
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