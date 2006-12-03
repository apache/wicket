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
package wicket.markup.html.form;

import java.util.List;

import wicket.MarkupContainer;
import wicket.model.IModel;
import wicket.util.string.AppendingStringBuffer;

/**
 * Abstract base class for single-select choices.
 * 
 * @param <T>
 *            The type
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
abstract class AbstractSingleSelectChoice<T> extends AbstractChoice<T, T>
{
	/** String to display when the selected value is null and nullValid is false. */
	private static final String CHOOSE_ONE = "Choose One";

	private static final String NO_SELECTION_VALUE = "-1";

	private static final String EMPTY_STRING = "";

	/** Is the null value a valid value? */
	private boolean nullValid = false;

	/**
	 * @see AbstractChoice#AbstractChoice(MarkupContainer,String)
	 */
	public AbstractSingleSelectChoice(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(MarkupContainer,String, List)
	 */
	public AbstractSingleSelectChoice(MarkupContainer parent, final String id, final List<T> choices)
	{
		super(parent, id, choices);
	}

	/**
	 * @param id
	 * @param data
	 * @param renderer
	 * @see AbstractChoice#AbstractChoice(MarkupContainer,String, List
	 *      ,IChoiceRenderer)
	 */
	public AbstractSingleSelectChoice(MarkupContainer parent, final String id, final List<T> data,
			final IChoiceRenderer<T> renderer)
	{
		super(parent, id, data, renderer);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(MarkupContainer,String, IModel, List)
	 */
	public AbstractSingleSelectChoice(MarkupContainer parent, final String id, IModel<T> model,
			final List<T> data)
	{
		super(parent, id, model, data);
	}

	/**
	 * @param id
	 * @param model
	 * @param data
	 * @param renderer
	 * @see AbstractChoice#AbstractChoice(MarkupContainer,String, IModel, List,
	 *      IChoiceRenderer)
	 */
	public AbstractSingleSelectChoice(MarkupContainer parent, final String id, IModel<T> model,
			final List<T> data, final IChoiceRenderer<T> renderer)
	{
		super(parent, id, model, data, renderer);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      IModel)
	 */
	public AbstractSingleSelectChoice(MarkupContainer parent, String id, IModel<List<T>> choices)
	{
		super(parent, id, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      IModel,IModel)
	 */
	public AbstractSingleSelectChoice(MarkupContainer parent, String id, IModel<T> model,
			IModel<List<T>> choices)
	{
		super(parent, id, model, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      IModel,IChoiceRenderer)
	 */
	public AbstractSingleSelectChoice(MarkupContainer parent, String id, IModel<List<T>> choices,
			IChoiceRenderer<T> renderer)
	{
		super(parent, id, choices, renderer);
	}


	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      IModel, IModel,IChoiceRenderer)
	 */
	public AbstractSingleSelectChoice(MarkupContainer parent, String id, IModel<T> model,
			IModel<List<T>> choices, IChoiceRenderer<T> renderer)
	{
		super(parent, id, model, choices, renderer);
	}

	/**
	 * @see FormComponent#getModelValue()
	 */
	@Override
	public final String getModelValue()
	{
		final T object = getModelObject();
		if (object != null)
		{
			int index = getChoices().indexOf(object);
			return getChoiceRenderer().getIdValue(object, index);
		}
		return NO_SELECTION_VALUE;
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
	 * @see wicket.markup.html.form.FormComponent#convertValue(String[])
	 */
	@Override
	protected final T convertValue(final String[] value)
	{
		String tmp = value != null && value.length > 0 ? value[0] : null;
		List<T> choices = getChoices();
		for (int index = 0; index < choices.size(); index++)
		{
			// Get next choice
			final T choice = choices.get(index);
			if (getChoiceRenderer().getIdValue(choice, index).equals(tmp))
			{
				return choice;
			}
		}
		return null;
	}

	/**
	 * The localizer will be ask for the property to display Depending on if
	 * null is allowed or not it will ask for:
	 * 
	 * <ul>
	 * <li>nullValid: when null is valid and by default it will show an empty
	 * string as a choice.</li>
	 * <li>null: when null is not a valid choice and it will make a choice with
	 * "Choose One"</li>
	 * </ul>
	 * 
	 * The choice for null is valid will always be returned. The choice when
	 * null is not valid will only be returned if the selected object is null.
	 * 
	 * @see wicket.markup.html.form.AbstractChoice#getDefaultChoiceMarkup(Object)
	 */
	@Override
	protected CharSequence getDefaultChoiceMarkup(final Object selected)
	{
		// Is null a valid selection value?
		if (isNullValid())
		{
			// Null is valid, so look up the value for it
			final String option = getDefaultChoiceText();

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
			if (selected == null || selected.equals(NO_SELECTION_VALUE)
					|| selected.equals(EMPTY_STRING))
			{
				// Force the user to pick a non-null value
				final String option = getLocalizer().getString("null", this, CHOOSE_ONE);
				return new AppendingStringBuffer("\n<option selected=\"selected\" value=\"\">")
						.append(option).append("</option>");
			}
		}
		return "";
	}

	/**
	 * Returns the string value of the default choice (the "Choose One" analog).
	 * This method is useful if you only need to override the string value of
	 * the default option and do not want to implement the construction of
	 * markup that is performed in the {@link #getDefaultChoiceMarkup(Object)}.
	 * 
	 * By default this method looks for a resource with key "nullValid"
	 * 
	 * @return string representation of the text of the default choice option
	 */
	protected String getDefaultChoiceText()
	{
		return getLocalizer().getString("nullValid", this, "");
	}


	/*
	 * Gets whether the given value represents the current selection.
	 * 
	 * 
	 * aram object The object to check @param index The index of the object in
	 * the collection @param selected The current selected id value @return
	 * Whether the given value represents the current selection
	 */
	@Override
	protected boolean isSelected(final T object, int index, String selected)
	{
		return selected != null && selected.equals(getChoiceRenderer().getIdValue(object, index));
	}
}