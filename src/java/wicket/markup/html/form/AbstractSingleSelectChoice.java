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

import java.util.Collection;

import wicket.markup.html.form.model.IChoice;
import wicket.markup.html.form.model.IChoiceList;
import wicket.model.IModel;
import wicket.util.string.Strings;

/**
 * Abstract base class for single-select choices.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
abstract class AbstractSingleSelectChoice extends AbstractChoice
{
	/** String to display when the selected value is null and nullValid is false. */
	private static final String CHOOSE_ONE = "Choose One";

	/** Serial Version ID. */
	private static final long serialVersionUID = -8334966481181600604L;

	/** Is the null value a valid value? */
	private boolean nullValid = false;

	/**
	 * @see AbstractChoice#AbstractChoice(String, Collection)
	 */
	public AbstractSingleSelectChoice(final String id, final Collection choices)
	{
		super(id, choices);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, IChoiceList)
	 */
	public AbstractSingleSelectChoice(final String id, final IChoiceList choices)
	{
		super(id, choices);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, IModel, Collection)
	 */
	public AbstractSingleSelectChoice(final String id, IModel model, final Collection choices)
	{
		super(id, model, choices);
	}
	
	/**
	 * @see AbstractChoice#AbstractChoice(String, IModel, IChoiceList)
	 */
	public AbstractSingleSelectChoice(final String id, IModel model, final IChoiceList choices)
	{
		super(id, model, choices);
	}

	/**
	 * @see FormComponent#getModelValue()
	 */
	public final String getModelValue()
	{
		final IChoice choice = getChoices().choiceForObject(getModelObject());
		if (choice != null)
		{
			return choice.getId();
		}
		return "-1";
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
	 * @see FormComponent#setModelValue(java.lang.String)
	 */
	public final void setModelValue(final String value)
	{
		setModelObject(getChoices().choiceForId(value).getObject());
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#getDefaultChoice(Object)
	 */
	protected String getDefaultChoice(final Object selected)
	{
		// The <option> tag buffer
		final StringBuffer buffer = new StringBuffer();
		
		// Is null a valid selection value?
		if (nullValid)
		{
			// Null is valid, so look up the value for it
			final String option = getLocalizer().getString(getId() + ".null", this, "");

			// Add option tag
			buffer.append("\n<option");

			// If null is selected, indicate that
			if (selected == null)
			{
				buffer.append(" selected=\"selected\"");
			}

			// Add body of option tag
			buffer.append(" value=\"\">").append(option).append("</option>");
		}
		else
		{
			// Null is not valid. Is it selected anyway?
			if (selected == null)
			{
				// Force the user to pick a non-null value
				final String option = getLocalizer().getString(getId() + ".null", this,
						CHOOSE_ONE);
				buffer.append("\n<option selected=\"selected\" value=\"\">").append(option)
						.append("</option>");
			}
		}
		
		return buffer.toString();
	}

	/**
	 * Gets whether the given value represents the current selection.
	 * 
	 * @param choice
	 *            The choice to check
	 * @return Whether the given value represents the current selection
	 */
	protected boolean isSelected(final IChoice choice)
	{
		final String value = getValue();
		return value != null && value.equals(choice.getId());
	}

	/**
	 * Updates this components' model from the request.
	 * 
	 * @see wicket.markup.html.form.AbstractChoice#updateModel()
	 */
	protected final void updateModel()
	{
		final String id = getInput();
		if (Strings.isEmpty(id))
		{
			setModelObject(null);
		}
		else
		{
			setModelObject(getChoices().choiceForId(id).getObject());
		}
	}
}