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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import wicket.markup.ComponentTag;
import wicket.markup.html.form.model.IChoice;
import wicket.markup.html.form.model.IChoiceList;
import wicket.model.Model;
import wicket.model.PropertyModel;
import wicket.util.string.Strings;

/**
 * A multiple choice list component.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 */
public class ListMultipleChoice extends AbstractChoice
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -1000324612688307682L;

	/**
	 * @see AbstractChoice#AbstractChoice(String, Collection)
	 */
	public ListMultipleChoice(String name, final Collection values)
	{
		super(name, values);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, Serializable, Collection)
	 */
	public ListMultipleChoice(final String name, final Serializable model, final Collection values)
	{
		super(name, model, values);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, Serializable, String,
	 *      Collection)
	 */
	public ListMultipleChoice(final String name, final Serializable model, final String expression,
			final Collection values)
	{
		super(name, new PropertyModel(new Model(model), expression), values);
	}

	/**
	 * @see FormComponent#getValue()
	 */
	public final String getValue()
	{
		// Get the list of selected values
		final Collection selectedValues = (Collection)getModelObject();
		final StringBuffer value = new StringBuffer();
		if (selectedValues != null)
		{
			final IChoiceList choices = getChoices();
			for (final Iterator iterator = selectedValues.iterator(); iterator.hasNext() ;)
			{
				final IChoice choice = choices.choiceForObject(iterator.next());
				value.append(choice.getId());

				// NOTE ids can't have semicolons (should we escape it or
				// something?)
				value.append(";");
			}
		}
		return value.toString();
	}

	/**
	 * Sets the cookie value for this component.
	 * 
	 * @param value
	 *            the cookie value for this component
	 * @see FormComponent#setValue(java.lang.String)
	 */
	public final void setValue(final String value)
	{
		Collection selectedValues = (Collection)getModelObject();
		if (selectedValues == null)
		{
			selectedValues = new ArrayList();
			setModelObject(selectedValues);
		}
		else
		{
			selectedValues.clear();
		}
		final IChoiceList choices = getChoices();
		for (final StringTokenizer tokenizer = new StringTokenizer(value, ";"); tokenizer
				.hasMoreTokens();)
		{
			selectedValues.add(choices.choiceForId(tokenizer.nextToken()));
		}
	}

	/**
	 * Gets whether the given value represents the current selection.
	 * 
	 * @param currentValue
	 *            the current list value
	 * @return whether the given value represents the current selection
	 * @see wicket.markup.html.form.AbstractChoice#isSelected(java.lang.Object)
	 */
	protected final boolean isSelected(Object currentValue)
	{
		final Collection collection = (Collection)getModelObject();
		if (collection != null)
		{
			return collection.contains(currentValue);
		}
		return false;
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected final void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("multiple", true);
	}

	/**
	 * Updates this forms model from the request.
	 * 
	 * @see FormComponent#updateModel()
	 */
	protected final void updateModel()
	{
		// Get the list of selected values
		Collection selectedValues = (Collection)getModelObject();

		if (selectedValues != null)
		{
			selectedValues.clear();
		}
		else
		{
			selectedValues = new ArrayList();
			setModelObject(selectedValues);
		}

		// Get indices selected from request
		final String[] ids = inputAsStringArray();

		// If one or more ids is selected
		if (ids != null && ids.length > 0 && !Strings.isEmpty(ids[0]))
		{
			// Get values that could be selected
			final IChoiceList choices = getChoices();

			// Loop through selected indices
			for (int i = 0; i < ids.length; i++)
			{
				final IChoice choice = choices.choiceForId(ids[i]);
				selectedValues.add(choice.getObject());
			}
		}
	}
}