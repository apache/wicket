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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import wicket.markup.ComponentTag;
import wicket.markup.html.form.model.IChoice;
import wicket.markup.html.form.model.IChoiceList;
import wicket.model.IModel;
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
	public ListMultipleChoice(final String id, final Collection choices)
	{
		super(id, choices);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, IChoiceList)
	 */
	public ListMultipleChoice(final String id, final IChoiceList choices)
	{
		super(id, choices);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, IModel, Collection)
	 */
	public ListMultipleChoice(final String id, IModel object, final Collection choices)
	{
		super(id, object, choices);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, IModel, IChoiceList)
	 */
	public ListMultipleChoice(final String id, IModel object, final IChoiceList choices)
	{
		super(id, object, choices);
	}

	/**
	 * @see FormComponent#getModelValue()
	 */
	public final String getModelValue()
	{
		// Get the list of selected values
		final Collection selectedValues = (Collection)getModelObject();
		final StringBuffer buffer = new StringBuffer();
		if (selectedValues != null)
		{
			final IChoiceList choices = getChoices();
			for (final Iterator iterator = selectedValues.iterator(); iterator.hasNext();)
			{
				final IChoice choice = choices.choiceForObject(iterator.next());
				buffer.append(choice.getId());
				buffer.append(";");
			}
		}
		return buffer.toString();
	}

	/**
	 * @see FormComponent#setModelValue(java.lang.String)
	 */
	public final void setModelValue(final String value)
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
	 * @see AbstractChoice#isSelected(IChoice)
	 */
	protected final boolean isSelected(IChoice choice)
	{
		// Get value of the form "id1;id2;id3"
		final String value = getValue();
		
		// Have a value at all?
		if (value != null)
		{
			// Loop through ids
			for (final StringTokenizer tokenizer = new StringTokenizer(value, ";"); tokenizer
					.hasMoreTokens();)
			{
				final String id = tokenizer.nextToken(); 
				if (id.equals(choice.getId()))
				{
					return true;
				}
			}

		}
		return false;
	}

	/**
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected final void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("multiple", true);
	}

	/**
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