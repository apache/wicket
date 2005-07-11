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
import java.util.Iterator;
import java.util.List;

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

	/** Is the null value a valid value? */
	private boolean nullValid = false;

	/**
	 * @see AbstractChoice#AbstractChoice(String)
	 */
	public AbstractSingleSelectChoice(final String id)
	{
		super(id);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, Collection)
	 */
	public AbstractSingleSelectChoice(final String id, final Collection choices)
	{
		super(id, choices);
	}

	/**
	 * @param id 
	 * @param renderer 
	 * @param data 
	 * @see AbstractChoice#AbstractChoice(String, IChoiceRenderer ,Collection)
	 */
	public AbstractSingleSelectChoice(final String id, final IChoiceRenderer renderer, final Collection data)
	{
		super(id, renderer,data);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, IModel, Collection)
	 */
	public AbstractSingleSelectChoice(final String id, IModel model, final Collection data)
	{
		super(id, model, data);
	}
	
	/**
	 * @param id 
	 * @param model 
	 * @param renderer 
	 * @param data 
	 * @see AbstractChoice#AbstractChoice(String, IModel, IChoiceRenderer, Collection)
	 */
	public AbstractSingleSelectChoice(final String id, IModel model, final IChoiceRenderer renderer, final Collection data)
	{
		super(id, model,renderer, data);
	}

	/**
	 * @see FormComponent#getModelValue()
	 */
	public final String getModelValue()
	{
		final Object object = getModelObject();
		if (object != null)
		{
			int index = -1;
			Collection collection = getChoices();
			if(collection instanceof List)
			{
				index = ((List)collection).indexOf(object);
			}
			else
			{
				Iterator it = collection.iterator();
				while(it.hasNext())
				{
					index++;
					if(it.next().equals(object))
					{
						break;
					}
				}
			}
			return getChoiceRenderer().getIdValue(object, index);
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
	 * @param nullValid whether null is a valid value
	 */
	public void setNullValid(boolean nullValid)
	{
		this.nullValid = nullValid;
	}

	/**
	 * @see FormComponent#setModelValue(java.lang.String)
	 */
	public final void setModelValue(final String value)
	{
		Iterator it = getChoices().iterator();
		int index = -1;
		while(it.hasNext())
		{
			index++;
			Object object = it.next();
			if(getChoiceRenderer().getIdValue(object, index).equals(value))
			{
				setModelObject(object);
				break;
			}
		}
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#getDefaultChoice(Object)
	 */
	protected String getDefaultChoice(final Object selected)
	{
		// The <option> tag buffer
		final StringBuffer buffer = new StringBuffer();
		
		// Is null a valid selection value?
		if (isNullValid())
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
	 * @param object
	 *            The object to check
	 * @param index 
	 * 			  The index of the object in the collection
	 * @return Whether the given value represents the current selection
	 */
	protected boolean isSelected(final Object object, int index)
	{
		final String value = getValue();
		return value != null && value.equals(getChoiceRenderer().getIdValue(object, index));
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
			setModelValue(id);
		}
	}
}