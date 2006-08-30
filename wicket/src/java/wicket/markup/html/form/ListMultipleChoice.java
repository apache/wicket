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
import java.util.List;
import java.util.StringTokenizer;

import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.util.convert.ConversionException;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * A multiple choice list component.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Martijn Dashorst
 */
public class ListMultipleChoice extends AbstractChoice
{
	private static final long serialVersionUID = 1L;

	/** The default maximum number of rows to display. */
	private static int defaultMaxRows = 8;

	/** The maximum number of rows to display. */
	private int maxRows = defaultMaxRows;

	/**
	 * Gets the default maximum number of rows to display.
	 * 
	 * @return Returns the defaultMaxRows.
	 */
	protected static int getDefaultMaxRows()
	{
		return defaultMaxRows;
	}

	/**
	 * Sets the default maximum number of rows to display.
	 * 
	 * @param defaultMaxRows
	 *            The defaultMaxRows to set.
	 */
	protected static void setDefaultMaxRows(final int defaultMaxRows)
	{
		ListMultipleChoice.defaultMaxRows = defaultMaxRows;
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String)
	 */
	public ListMultipleChoice(final String id)
	{
		super(id);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, List)
	 */
	public ListMultipleChoice(final String id, final List choices)
	{
		super(id, choices);
	}

	/**
	 * Creates a multiple choice list with a maximum number of visible rows.
	 * 
	 * @param id
	 *            component id
	 * @param choices
	 *            list of choices
	 * @param maxRows
	 *            the maximum number of visible rows.
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, List)
	 */
	public ListMultipleChoice(final String id, final List choices, final int maxRows)
	{
		super(id, choices);
		this.maxRows = maxRows;
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      List,IChoiceRenderer)
	 */
	public ListMultipleChoice(final String id, final List choices, final IChoiceRenderer renderer)
	{
		super(id, choices, renderer);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      IModel, List)
	 */
	public ListMultipleChoice(final String id, IModel object, final List choices)
	{
		super(id, object, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      IModel, List,IChoiceRenderer)
	 */
	public ListMultipleChoice(final String id, IModel object, final List choices,
			final IChoiceRenderer renderer)
	{
		super(id, object, choices, renderer);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      IModel)
	 */
	public ListMultipleChoice(String id, IModel choices)
	{
		super(id, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      IModel,IModel)
	 */
	public ListMultipleChoice(String id, IModel model, IModel choices)
	{
		super(id, model, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      IModel,IChoiceRenderer)
	 */
	public ListMultipleChoice(String id, IModel choices, IChoiceRenderer renderer)
	{
		super(id, choices, renderer);
	}


	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      IModel, IModel,IChoiceRenderer)
	 */
	public ListMultipleChoice(String id, IModel model, IModel choices, IChoiceRenderer renderer)
	{
		super(id, model, choices, renderer);
	}


	/**
	 * Sets the number of visible rows in the listbox.
	 * 
	 * @param maxRows
	 *            the number of visible rows
	 * @return this
	 */
	public final ListMultipleChoice setMaxRows(final int maxRows)
	{
		this.maxRows = maxRows;
		return this;
	}

	/**
	 * @see FormComponent#getModelValue()
	 */
	public final String getModelValue()
	{
		// Get the list of selected values
		final Collection selectedValues = (Collection)getModelObject();
		final AppendingStringBuffer buffer = new AppendingStringBuffer();
		if (selectedValues != null)
		{
			final List choices = getChoices();
			for (final Iterator iterator = selectedValues.iterator(); iterator.hasNext();)
			{
				final Object object = iterator.next();

				int index = choices.indexOf(object);
				buffer.append(getChoiceRenderer().getIdValue(object, index));
				buffer.append(VALUE_SEPARATOR);
			}
		}
		return buffer.toString();
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#isSelected(Object,int,
	 *      String)
	 */
	protected final boolean isSelected(Object choice, int index, String selected)
	{
		// Have a value at all?
		if (selected != null)
		{
			// Loop through ids
			for (final StringTokenizer tokenizer = new StringTokenizer(selected, VALUE_SEPARATOR); tokenizer
					.hasMoreTokens();)
			{
				final String id = tokenizer.nextToken();
				if (id.equals(getChoiceRenderer().getIdValue(choice, index)))
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
		tag.put("multiple", "multiple");
		
		if (!tag.getAttributes().containsKey("size"))
		{
			tag.put("size", Math.min(maxRows, getChoices().size()));
		}
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#convertValue(String[])
	 */
	protected Object convertValue(String[] ids) throws ConversionException
	{
		ArrayList selectedValues = new ArrayList();

		// If one or more ids is selected
		if (ids != null && ids.length > 0 && !Strings.isEmpty(ids[0]))
		{
			// Get values that could be selected
			final List choices = getChoices();

			// Loop through selected indices
			for (int i = 0; i < ids.length; i++)
			{
				for (int index = 0; index < choices.size(); index++)
				{
					// Get next choice
					final Object choice = choices.get(index);
					if (getChoiceRenderer().getIdValue(choice, index).equals(ids[i]))
					{
						selectedValues.add(choice);
						break;
					}
				}
			}
		}
		return selectedValues;
	}
	
	/**
	 * @see FormComponent#updateModel()
	 */
	public final void updateModel()
	{
		Collection selectedValues = (Collection)getModelObject();
		if (selectedValues != null)
		{
			modelChanging();
			selectedValues.clear();
			selectedValues.addAll((Collection)getConvertedInput());
			modelChanged();
		}
		else
		{
			selectedValues = (Collection)getConvertedInput();
			setModelObject(selectedValues);
		}
	}
}