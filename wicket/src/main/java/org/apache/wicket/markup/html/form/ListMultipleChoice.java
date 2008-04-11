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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A multiple choice list component.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Martijn Dashorst
 * 
 * @param <T>
 *            The model object type
 */
public class ListMultipleChoice<T> extends AbstractChoice<Collection<T>, T>
{
	private static final long serialVersionUID = 1L;

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(ListMultipleChoice.class);

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
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String)
	 */
	public ListMultipleChoice(final String id)
	{
		super(id);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, List)
	 */
	public ListMultipleChoice(final String id, final List<T> choices)
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
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, List)
	 */
	public ListMultipleChoice(final String id, final List<T> choices, final int maxRows)
	{
		super(id, choices);
		this.maxRows = maxRows;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      List,IChoiceRenderer)
	 */
	public ListMultipleChoice(final String id, final List<T> choices,
		final IChoiceRenderer<T> renderer)
	{
		super(id, choices, renderer);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, List)
	 */
	public ListMultipleChoice(final String id, IModel<Collection<T>> object, final List<T> choices)
	{
		super(id, object, choices);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,
	 *      List,IChoiceRenderer)
	 */
	public ListMultipleChoice(final String id, IModel<Collection<T>> object, final List<T> choices,
		final IChoiceRenderer<T> renderer)
	{
		super(id, object, choices, renderer);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel)
	 */
	public ListMultipleChoice(String id, IModel<List<T>> choices)
	{
		super(id, choices);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,IModel)
	 */
	public ListMultipleChoice(String id, IModel<Collection<T>> model, IModel<List<T>> choices)
	{
		super(id, model, choices);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      IModel,IChoiceRenderer)
	 */
	public ListMultipleChoice(String id, IModel<List<T>> choices, IChoiceRenderer<T> renderer)
	{
		super(id, choices, renderer);
	}


	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,
	 *      IModel,IChoiceRenderer)
	 */
	public ListMultipleChoice(String id, IModel<Collection<T>> model, IModel<List<T>> choices,
		IChoiceRenderer<T> renderer)
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
	public final ListMultipleChoice<T> setMaxRows(final int maxRows)
	{
		this.maxRows = maxRows;
		return this;
	}

	/**
	 * @see FormComponent#getModelValue()
	 */
	@Override
	public final String getModelValue()
	{
		// Get the list of selected values
		Object modelObject = getModelObject();
		if (modelObject != null && !(modelObject instanceof Collection))
		{
			throw new WicketRuntimeException(
				"Model object for a ListMultipleChoice must be a Collection (found " +
					modelObject.getClass() + ")");
		}
		final Collection<T> selectedValues = (Collection)modelObject;
		final AppendingStringBuffer buffer = new AppendingStringBuffer();
		if (selectedValues != null)
		{
			final List<T> choices = getChoices();
			for (final Iterator<T> iterator = selectedValues.iterator(); iterator.hasNext();)
			{
				final T object = iterator.next();

				int index = choices.indexOf(object);
				buffer.append(getChoiceRenderer().getIdValue(object, index));
				buffer.append(VALUE_SEPARATOR);
			}
		}
		return buffer.toString();
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#isSelected(Object,int, String)
	 */
	@Override
	protected final boolean isSelected(T choice, int index, String selected)
	{
		// Have a value at all?
		if (selected != null)
		{
			// Loop through ids
			for (final StringTokenizer tokenizer = new StringTokenizer(selected, VALUE_SEPARATOR); tokenizer.hasMoreTokens();)
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
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("multiple", "multiple");

		if (!tag.getAttributes().containsKey("size"))
		{
			tag.put("size", Math.min(maxRows, getChoices().size()));
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#convertValue(String[])
	 */
	@Override
	protected Collection<T> convertValue(String[] ids) throws ConversionException
	{
		if (ids != null && ids.length > 0 && !Strings.isEmpty(ids[0]))
		{
			return convertChoiceIdsToChoices(ids);
		}
		else
		{
			// TODO 1.3: check if its safe to return Collections.EMPTY_LIST here
			return new ArrayList();
		}
	}

	/**
	 * Converts submitted choice ids to choice objects.
	 * 
	 * @param ids
	 *            choice ids. this array is nonnull and always contains at least one id.
	 * @return list of choices.
	 */
	protected List<T> convertChoiceIdsToChoices(String[] ids)
	{
		ArrayList<T> selectedValues = new ArrayList<T>();

		// If one or more ids is selected
		if (ids != null && ids.length > 0 && !Strings.isEmpty(ids[0]))
		{
			// Get values that could be selected
			final List<T> choices = getChoices();

			// Loop through selected indices
			for (int i = 0; i < ids.length; i++)
			{
				for (int index = 0; index < choices.size(); index++)
				{
					// Get next choice
					final T choice = choices.get(index);
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
	 * If the model object exists, it is assumed to be a Collection, and it is modified in-place.
	 * Then {@link Model#setObject(Object)} is called with the same instance: it allows the Model to
	 * be notified of changes even when {@link Model#getObject()} returns a different
	 * {@link Collection} at every invocation.
	 * 
	 * @see FormComponent#updateModel()
	 * @throws UnsupportedOperationException
	 *             if the model object Collection cannot be modified
	 */
	@Override
	public void updateModel()
	{
		Collection<T> selectedValues = getModelObject();
		if (selectedValues != null)
		{
			if (getModelObject() != selectedValues)
			{
				throw new WicketRuntimeException(
					"Updating a ListMultipleChoice works by modifying the underlying model object in-place, so please make sure that getObject() always returns the same Collection instance!");
			}

			modelChanging();
			selectedValues.clear();
			selectedValues.addAll(getConvertedInput());
			modelChanged();
			// call model.setObject()
			try
			{
				getModel().setObject(selectedValues);
			}
			catch (Exception e)
			{
				// ignore this exception because it could be that there
				// is not setter for this collection.
				log.info("no setter for the property attached to " + this);
			}
		}
		else
		{
			selectedValues = getConvertedInput();
			setModelObject(selectedValues);
		}
	}
}