/*
 * $Id$ $Revision:
 * 1.6 $ $Date$
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
package wicket.markup.html.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;

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

	/** The list of values. */
	private List values;

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param values
	 *            The collection of values in the dropdown
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public AbstractChoice(String name, Serializable object, final Collection values)
	{
		super(name, object);
		setValues(values);
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param expression
	 *            See Component constructor
	 * @param values
	 *            The collection of values in the dropdown
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public AbstractChoice(String name, Serializable object, String expression,
			final Collection values)
	{
		super(name, object, expression);
		setValues(values);
	}

	/**
	 * Gets the list of values.
	 * 
	 * @return the list of values
	 */
	public List getValues()
	{
		if (values instanceof IDetachableChoiceList)
		{
			((IDetachableChoiceList)values).attach();
		}
		return this.values;
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
	 * Sets the values to use for the dropdown.
	 * 
	 * @param values
	 *            values to set
	 * @return dropdown choice
	 */
	public AbstractChoice setValues(final Collection values)
	{
		if (values == null)
		{
			this.values = Collections.EMPTY_LIST;
		}
		else if (values instanceof List)
		{
			this.values = (List)values;
		}
		else
		{
			this.values = new ArrayList(values);
		}
		return this;
	}

	/**
	 * Updates the model of this component from the request.
	 * 
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public abstract void updateModel();

	/**
	 * @see wicket.Component#detachModel()
	 */
	protected void detachModel()
	{
		super.detachModel();
		if (values instanceof IDetachableChoiceList)
		{
			((IDetachableChoiceList)values).detach();
		}
	}

	/**
	 * Gets whether the given value represents the current selection.
	 * 
	 * @param currentValue
	 *            the current list value
	 * @return whether the given value represents the current selection
	 */
	protected boolean isSelected(Object currentValue)
	{
		Object modelObject = getModelObject();
		if (modelObject == null)
		{
			if (currentValue == null)
				return true;
			else
				return false;
		}
		boolean equals = currentValue.equals(modelObject);
		return equals;
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
		final List list = getValues();

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
			// Null is not valid.  Is it selected anyway?
			if (selected == null)
			{
				// Force the user to pick a non-null value
				final String option = getLocalizer().getString(getName() + ".null", this,
						CHOOSE_ONE);
				options.append("\n<option selected=\"selected\" value=\"\">").append(option)
						.append("</option>");
			}
		}

		for (int i = 0; i < list.size(); i++)
		{
			final Object value = list.get(i);

			if (value != null)
			{
				final String id;
				final String displayValue;
				if (list instanceof IDetachableChoiceList)
				{
					IDetachableChoiceList choiceList = (IDetachableChoiceList)list;
					id = choiceList.getId(i);
					displayValue = choiceList.getDisplayValue(i);

				}
				else
				{
					id = Integer.toString(i);
					displayValue = value.toString();
				}
				final boolean currentOptionIsSelected = isSelected(value);
				options.append("\n<option ");
				if (currentOptionIsSelected)
				{
					options.append("selected=\"selected\"");
				}
				options.append("value=\"");
				options.append(id);
				options.append("\">");
				options.append(getLocalizer().getString(getName() + "." + displayValue, this,
						displayValue));
				options.append("</option>");
			}
			else
			{
				throw new IllegalArgumentException(
						"Dropdown choice contains null value in values collection at index " + i);
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
}