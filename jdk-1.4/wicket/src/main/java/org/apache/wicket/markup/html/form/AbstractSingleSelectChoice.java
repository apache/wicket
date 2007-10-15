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

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;


/**
 * Abstract base class for single-select choices.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
public abstract class AbstractSingleSelectChoice extends AbstractChoice
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** String to display when the selected value is null and nullValid is false. */
	private static final String CHOOSE_ONE = "Choose One";

	private static final String NO_SELECTION_VALUE = "-1";

	private static final String EMPTY_STRING = "";

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
	 * @see AbstractChoice#AbstractChoice(String, List)
	 */
	public AbstractSingleSelectChoice(final String id, final List choices)
	{
		super(id, choices);
	}

	/**
	 * @param id
	 * @param data
	 * @param renderer
	 * @see AbstractChoice#AbstractChoice(String, List ,IChoiceRenderer)
	 */
	public AbstractSingleSelectChoice(final String id, final List data,
			final IChoiceRenderer renderer)
	{
		super(id, data, renderer);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, IModel, List)
	 */
	public AbstractSingleSelectChoice(final String id, IModel model, final List data)
	{
		super(id, model, data);
	}

	/**
	 * @param id
	 * @param model
	 * @param data
	 * @param renderer
	 * @see AbstractChoice#AbstractChoice(String, IModel, List, IChoiceRenderer)
	 */
	public AbstractSingleSelectChoice(final String id, IModel model, final List data,
			final IChoiceRenderer renderer)
	{
		super(id, model, data, renderer);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel)
	 */
	public AbstractSingleSelectChoice(String id, IModel choices)
	{
		super(id, choices);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,IModel)
	 */
	public AbstractSingleSelectChoice(String id, IModel model, IModel choices)
	{
		super(id, model, choices);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      IModel,IChoiceRenderer)
	 */
	public AbstractSingleSelectChoice(String id, IModel choices, IChoiceRenderer renderer)
	{
		super(id, choices, renderer);
	}


	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,
	 *      IModel,IChoiceRenderer)
	 */
	public AbstractSingleSelectChoice(String id, IModel model, IModel choices,
			IChoiceRenderer renderer)
	{
		super(id, model, choices, renderer);
	}

	/**
	 * @see FormComponent#getModelValue()
	 */
	public final String getModelValue()
	{
		final Object object = getModelObject();
		if (object != null)
		{
			int index = getChoices().indexOf(object);
			return getChoiceRenderer().getIdValue(object, index);
		}
		return NO_SELECTION_VALUE;
	}

	/**
	 * Is the <code>null</code> value a valid value? If it is, it means that the null value will
	 * be displayed, typically to the user as 'choose one' or something similar. Note that this
	 * doesn't say anything about whether a null value (not selecting a value) is permitted; use
	 * {@link #setRequired(boolean)} for that.
	 * 
	 * @return <code>true</code> when the <code>null</code> value is allowed.
	 */
	public boolean isNullValid()
	{
		return nullValid;
	}

	/**
	 * Is the <code>null</code> value a valid value? If it is, it means that the null value will
	 * be displayed, typically to the user as 'choose one' or something similar. Note that this
	 * doesn't say anything about whether a null value (not selecting a value) is permitted; use
	 * {@link #setRequired(boolean)} for that.
	 * 
	 * @param nullValid
	 *            whether null is a valid value
	 * @return this for chaining
	 */
	public AbstractSingleSelectChoice setNullValid(boolean nullValid)
	{
		this.nullValid = nullValid;
		return this;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#convertValue(String[])
	 */
	protected final Object convertValue(final String[] value)
	{
		String tmp = value != null && value.length > 0 ? value[0] : null;
		return convertChoiceIdToChoice(tmp);
	}

	/**
	 * Converts submitted choice id string back to choice object.
	 * 
	 * @param id
	 *            string id of one of the choice objects in the choices list. can be null.
	 * @return choice object. null if none match the specified id.
	 */
	protected Object convertChoiceIdToChoice(String id)
	{
		final List choices = getChoices();
		final IChoiceRenderer renderer = getChoiceRenderer();
		for (int index = 0; index < choices.size(); index++)
		{
			// Get next choice
			final Object choice = choices.get(index);
			if (renderer.getIdValue(choice, index).equals(id))
			{
				return choice;
			}
		}
		return null;
	}

	/**
	 * The localizer will be ask for the property to display Depending on if null is allowed or not
	 * it will ask for:
	 * 
	 * <ul>
	 * <li>nullValid: when null is valid and by default it will show an empty string as a choice.</li>
	 * <li>null: when null is not a valid choice and it will make a choice with "Choose One"</li>
	 * </ul>
	 * 
	 * The choice for null is valid will always be returned. The choice when null is not valid will
	 * only be returned if the selected object is null.
	 * 
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#getDefaultChoice(Object)
	 */
	protected CharSequence getDefaultChoice(final Object selected)
	{
		// Is null a valid selection value?
		if (isNullValid())
		{
			// Null is valid, so look up the value for it
			String option = getLocalizer().getString(getId() + ".nullValid", this, "");
			if (Strings.isEmpty(option))
			{
				option = getLocalizer().getString("nullValid", this, "");
			}

			// The <option> tag buffer
			final AppendingStringBuffer buffer = new AppendingStringBuffer(32 + option.length());


			// Add option tag
			buffer.append("\n<option");

			// If null is selected, indicate that
			if (selected == null)
			{
				buffer.append(" selected=\"selected\"");
			}

			// Add body of option tag
			buffer.append(" value=\"\">").append(option).append("</option>");
			return buffer;
		}
		else
		{
			// Null is not valid. Is it selected anyway?
			if (selected == null || selected.equals(NO_SELECTION_VALUE) ||
					selected.equals(EMPTY_STRING))
			{
				// Force the user to pick a non-null value
				String option = getLocalizer().getString(getId() + ".null", this, "");
				if (Strings.isEmpty(option))
				{
					option = getLocalizer().getString("null", this, CHOOSE_ONE);
				}
				return new AppendingStringBuffer("\n<option selected=\"selected\" value=\"\">")
						.append(option).append("</option>");
			}
		}
		return "";
	}


	/**
	 * Gets whether the given value represents the current selection.
	 * 
	 * 
	 * aram object The object to check
	 * 
	 * @param index
	 *            The index of the object in the collection
	 * @param selected
	 *            The current selected id value
	 * @return Whether the given value represents the current selection
	 */
	protected boolean isSelected(final Object object, int index, String selected)
	{
		return selected != null && selected.equals(getChoiceRenderer().getIdValue(object, index));
	}
}