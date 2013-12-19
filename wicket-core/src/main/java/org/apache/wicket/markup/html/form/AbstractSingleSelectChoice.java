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

import org.apache.wicket.Localizer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;


/**
 * Abstract base class for single-select choices.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius nm
 * @author Johan Compagner
 * 
 * @param <T>
 *            The model object type
 */
public abstract class AbstractSingleSelectChoice<T> extends AbstractChoice<T, T>
{
	private static final long serialVersionUID = 1L;

	/** String to display when the selected value is null and nullValid is false. */
	private static final String CHOOSE_ONE = "Choose One";

	/** whether or not null will be offered as a choice once a nonnull value is saved */
	private boolean nullValid = false;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 */
	public AbstractSingleSelectChoice(final String id)
	{
		super(id);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AbstractSingleSelectChoice(final String id, final List<? extends T> choices)
	{
		super(id, choices);
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
	public AbstractSingleSelectChoice(final String id, final List<? extends T> choices,
		final IChoiceRenderer<? super T> renderer)
	{
		super(id, choices, renderer);
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
	public AbstractSingleSelectChoice(final String id, IModel<T> model,
		final List<? extends T> choices)
	{
		super(id, model, choices);
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
	public AbstractSingleSelectChoice(final String id, IModel<T> model,
		final List<? extends T> choices, final IChoiceRenderer<? super T> renderer)
	{
		super(id, model, choices, renderer);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AbstractSingleSelectChoice(String id, IModel<? extends List<? extends T>> choices)
	{
		super(id, choices);
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
	 */
	public AbstractSingleSelectChoice(String id, IModel<T> model,
		IModel<? extends List<? extends T>> choices)
	{
		super(id, model, choices);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The drop down choices
	 * @param renderer
	 *            The rendering engine
	 */
	public AbstractSingleSelectChoice(String id, IModel<? extends List<? extends T>> choices,
		IChoiceRenderer<? super T> renderer)
	{
		super(id, choices, renderer);
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
	public AbstractSingleSelectChoice(String id, IModel<T> model,
		IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer)
	{
		super(id, model, choices, renderer);
	}

	/**
	 * @see FormComponent#getModelValue()
	 */
	@Override
	public String getModelValue()
	{
		final T object = getModelObject();
		if (object != null)
		{
			int index = getChoices().indexOf(object);
			return getChoiceRenderer().getIdValue(object, index);
		}
		else
		{
			return "";
		}
	}

	/**
	 * Determines whether or not the null value should be included in the list of choices when the
	 * field's model value is nonnull, and whether or not the null_valid string property (e.g.
	 * "Choose One") should be displayed until a nonnull value is selected.
	 * 
	 * If set to false, then "Choose One" will be displayed when the value is null. After a value is
	 * selected, and that change is propagated to the underlying model, the user will no longer see
	 * the "Choose One" option, and there will be no way to reselect null as the value.
	 * 
	 * If set to true, the null string property (the empty string, by default) will always be
	 * displayed as an option, whether or not a nonnull value has ever been selected.
	 * 
	 * Note that this setting has no effect on validation; in order to guarantee that a value will
	 * be specified on form validation, {@link #setRequired(boolean)}. This is because even if
	 * setNullValid() is called with false, the user can fail to provide a value simply by never
	 * activating (i.e. clicking on) the component.
	 * 
	 * @return <code>true</code> when the <code>null</code> value is allowed.
	 */
	public boolean isNullValid()
	{
		return nullValid;
	}

	/**
	 * Determines whether or not the null value should be included in the list of choices when the
	 * field's model value is nonnull, and whether or not the null_valid string property (e.g.
	 * "Choose One") should be displayed until a nonnull value is selected.
	 * 
	 * If set to false, then "Choose One" will be displayed when the value is null. After a value is
	 * selected, and that change is propagated to the underlying model, the user will no longer see
	 * the "Choose One" option, and there will be no way to reselect null as the value.
	 * 
	 * If set to true, the null string property (the empty string, by default) will always be
	 * displayed as an option, whether or not a nonnull value has ever been selected.
	 * 
	 * Note that this setting has no effect on validation; in order to guarantee that a value will
	 * be specified on form validation, {@link #setRequired(boolean)}. This is because even if
	 * setNullValid() is called with false, the user can fail to provide a value simply by never
	 * activating (i.e. clicking on) the component.
	 * 
	 * @param nullValid
	 *            whether null is a valid value
	 * @return this for chaining
	 */
	public AbstractSingleSelectChoice<T> setNullValid(boolean nullValid)
	{
		this.nullValid = nullValid;
		return this;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#convertValue(String[])
	 */
	@Override
	protected final T convertValue(final String[] value)
	{
		String tmp = ((value != null) && (value.length > 0)) ? value[0] : null;
		return convertChoiceIdToChoice(tmp);
	}

	/**
	 * Converts submitted choice id string back to choice object.
	 * 
	 * @param id
	 *            string id of one of the choice objects in the choices list. can be null.
	 * @return choice object. null if none match the specified id.
	 */
	protected T convertChoiceIdToChoice(String id)
	{
		final List<? extends T> choices = getChoices();
		final IChoiceRenderer<? super T> renderer = getChoiceRenderer();
		for (int index = 0; index < choices.size(); index++)
		{
			// Get next choice
			final T choice = choices.get(index);
			if (renderer.getIdValue(choice, index).equals(id))
			{
				return choice;
			}
		}
		return null;
	}

	/**
	 * Asks the {@link Localizer} for the property to display for an additional default choice
	 * depending on {@link #isNullValid()}:
	 * 
	 * <ul>
	 * <li>
	 * "nullValid" if {@code null} is valid, defaulting to an empty string.</li>
	 * <li>
	 * "null" if {@code null} is not valid but no choice is selected (i.e. {@code selectedValue} is
	 * empty), defaulting to "Choose one".</li>
	 * </ul>
	 * 
	 * Otherwise no additional default choice will be returned.
	 * 
	 * @see #getNullValidKey()
	 * @see #getNullKey()
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#getDefaultChoice(String)
	 */
	@Override
	protected CharSequence getDefaultChoice(final String selectedValue)
	{
		// Is null a valid selection value?
		if (isNullValid())
		{
			// Null is valid, so look up the value for it
			String option = getNullValidDisplayValue();

			// The <option> tag buffer
			final AppendingStringBuffer buffer = new AppendingStringBuffer(64 + option.length());

			// Add option tag
			buffer.append("\n<option");

			// If null is selected, indicate that
			if ("".equals(selectedValue))
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
			if ("".equals(selectedValue))
			{
				// Force the user to pick a non-null value
				String option = getNullKeyDisplayValue();
				return "\n<option selected=\"selected\" value=\"\">" + option + "</option>";
			}
		}
		return "";
	}

	/**
	 * Returns the display value for the null value. The default behavior is to look the value up by
	 * using the key from <code>getNullValidKey()</code>.
	 *
	 * @return The value to display for null
	 */
	protected String getNullValidDisplayValue()
	{
		String option = getLocalizer().getStringIgnoreSettings(getNullValidKey(), this, null, null);
		if (Strings.isEmpty(option))
		{
			option = getLocalizer().getString("nullValid", this, "");
		}
		return option;
	}

	/**
	 * Return the localization key for nullValid value
	 * 
	 * @return getId() + ".nullValid"
	 */
	protected String getNullValidKey()
	{
		return getId() + ".nullValid";
	}

	/**
	 * Returns the display value if null is not valid but is selected. The default behavior is to
	 * look the value up by using the key from <code>getNullKey()</code>.
	 *
	 * @return The value to display if null is not value but selected, e.g. "Choose One"
	 */
	protected String getNullKeyDisplayValue()
	{
		String option = getLocalizer().getStringIgnoreSettings(getNullKey(), this, null, null);

		if (Strings.isEmpty(option))
		{
			option = getLocalizer().getString("null", this, CHOOSE_ONE);
		}
		return option;
	}

	/**
	 * Return the localization key for null value
	 * 
	 * @return getId() + ".null"
	 */
	protected String getNullKey()
	{
		return getId() + ".null";
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
	@Override
	protected boolean isSelected(final T object, int index, String selected)
	{
		return (selected != null) && selected.equals(getChoiceRenderer().getIdValue(object, index));
	}
}
