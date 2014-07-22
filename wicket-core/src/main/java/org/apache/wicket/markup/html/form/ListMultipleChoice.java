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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;


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

	/** Meta key for the retain disabled flag */
	static MetaDataKey<Boolean> RETAIN_DISABLED_META_KEY = new MetaDataKey<Boolean>()
	{
		private static final long serialVersionUID = 1L;
	};

	/** The default maximum number of rows to display. */
	private static final int DEFAULT_MAX_ROWS = 8;

	/** The maximum number of rows to display. */
	private int maxRows = DEFAULT_MAX_ROWS;

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
	public ListMultipleChoice(final String id, final List<? extends T> choices)
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
	public ListMultipleChoice(final String id, final List<? extends T> choices, final int maxRows)
	{
		super(id, choices);
		this.maxRows = maxRows;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      List,IChoiceRenderer)
	 */
	public ListMultipleChoice(final String id, final List<? extends T> choices,
		final IChoiceRenderer<? super T> renderer)
	{
		super(id, choices, renderer);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, List)
	 * 
	 * @param id
	 * @param object
	 * @param choices
	 */
	@SuppressWarnings("unchecked")
	public ListMultipleChoice(final String id, IModel<? extends Collection<T>> object,
		final List<? extends T> choices)
	{
		super(id, (IModel<Collection<T>>)object, choices);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,
	 *      List,IChoiceRenderer)
	 * 
	 * @param id
	 * @param object
	 * @param choices
	 * @param renderer
	 */
	@SuppressWarnings("unchecked")
	public ListMultipleChoice(final String id, IModel<? extends Collection<T>> object,
		final List<? extends T> choices, final IChoiceRenderer<? super T> renderer)
	{
		super(id, (IModel<Collection<T>>)object, choices, renderer);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel)
	 */
	public ListMultipleChoice(String id, IModel<? extends List<? extends T>> choices)
	{
		super(id, choices);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,IModel)
	 * 
	 * @param id
	 * @param model
	 * @param choices
	 */
	@SuppressWarnings("unchecked")
	public ListMultipleChoice(String id, IModel<? extends Collection<T>> model,
		IModel<? extends List<? extends T>> choices)
	{
		super(id, (IModel<Collection<T>>)model, choices);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      IModel,IChoiceRenderer)
	 */
	public ListMultipleChoice(String id, IModel<? extends List<? extends T>> choices,
		IChoiceRenderer<? super T> renderer)
	{
		super(id, choices, renderer);
	}


	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,
	 *      IModel,IChoiceRenderer)
	 * 
	 * @param id
	 * @param model
	 * @param choices
	 * @param renderer
	 */
	@SuppressWarnings("unchecked")
	public ListMultipleChoice(String id, IModel<? extends Collection<T>> model,
		IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer)
	{
		super(id, (IModel<Collection<T>>)model, choices, renderer);
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
		final AppendingStringBuffer buffer = new AppendingStringBuffer();

		final Collection<T> selectedValues = getModelObject();
		if (selectedValues != null)
		{
			final List<? extends T> choices = getChoices();
			for (T object : selectedValues)
			{
				if (buffer.length() > 0)
				{
					buffer.append(VALUE_SEPARATOR);
				}
				int index = choices.indexOf(object);
				buffer.append(getChoiceRenderer().getIdValue(object, index));
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
			String idValue = getChoiceRenderer().getIdValue(choice, index);

			// Loop through ids
			for (final StringTokenizer tokenizer = new StringTokenizer(selected, VALUE_SEPARATOR); tokenizer.hasMoreTokens();)
			{
				final String id = tokenizer.nextToken();
				if (id.equals(idValue))
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
			ArrayList<T> result = new ArrayList<T>();
			addRetainedDisabled(result);
			return result;
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
			final Map<String, T> choiceIds2choiceValues = createChoicesIdsMap();

			// Loop through selected indices
			for (String id : ids)
			{
				if (choiceIds2choiceValues.containsKey(id))
				{
					selectedValues.add(choiceIds2choiceValues.get(id));
				}
			}
		}
		addRetainedDisabled(selectedValues);

		return selectedValues;

	}

	/**
	 * Creates a map of choice IDs to choice values. This map can be used to speed up lookups e.g.
	 * in {@link #convertChoiceIdsToChoices(String[])}. <strong>Do not store the result of this
	 * method.</strong> The choices list can change between requests so this map <em>must</em> be
	 * regenerated.
	 * 
	 * @return a map.
	 */
	private Map<String, T> createChoicesIdsMap()
	{
		final List<? extends T> choices = getChoices();

		final Map<String, T> choiceIds2choiceValues = new HashMap<String, T>(choices.size(), 1);

		for (int index = 0; index < choices.size(); index++)
		{
			// Get next choice
			final T choice = choices.get(index);
			choiceIds2choiceValues.put(getChoiceRenderer().getIdValue(choice, index), choice);
		}
		return choiceIds2choiceValues;
	}

	private void addRetainedDisabled(ArrayList<T> selectedValues)
	{
		if (isRetainDisabledSelected())
		{
			Collection<T> unchangedModel = getModelObject();
			String selected;
			{
				StringBuilder builder = new StringBuilder();
				for (T t : unchangedModel)
				{
					builder.append(t);
					builder.append(';');
				}
				selected = builder.toString();
			}
			List<? extends T> choices = getChoices();
			for (int i = 0; i < choices.size(); i++)
			{
				final T choice = choices.get(i);
				if (isDisabled(choice, i, selected))
				{
					if (unchangedModel.contains(choice))
					{
						if (!selectedValues.contains(choice))
						{
							selectedValues.add(choice);
						}
					}
				}
			}
		}
	}

	/**
	 * See {@link FormComponent#updateCollectionModel(FormComponent)} for details on how the model
	 * is updated.
	 */
	@Override
	public void updateModel()
	{
		FormComponent.updateCollectionModel(this);
	}

	/**
	 * If true, choices that were selected in the model but disabled in rendering will be retained
	 * in the model after a form submit. Example: Choices are [1, 2, 3, 4]. Model collection is [2,
	 * 4]. In rendering, choices 2 and 3 are disabled ({@link #isDisabled(Object, int, String)}).
	 * That means that four checkboxes are rendered, checkboxes 2 and 4 are checked, but 2 and 3 are
	 * not clickable. User checks 1 and unchecks 4. If this flag is off, the model will be updated
	 * to [1]. This is because the browser does not re-submit a disabled checked checkbox: it only
	 * submits [1]. Therefore Wicket will only see the [1] and update the model accordingly. If you
	 * set this flag to true, Wicket will check the model before updating to find choices that were
	 * selected but disabled. These choices will then be retained, leading to a new model value of
	 * [1, 2] as (probably) expected by the user. Note that this will lead to additional calls to
	 * {@link #isDisabled(Object, int, String)}.
	 * 
	 * @return flag
	 */
	public boolean isRetainDisabledSelected()
	{
		Boolean flag = getMetaData(RETAIN_DISABLED_META_KEY);
		return (flag != null && flag);
	}

	/**
	 * @param retain
	 *            flag
	 * @return this
	 * @see #isRetainDisabledSelected()
	 */
	public ListMultipleChoice<T> setRetainDisabledSelected(boolean retain)
	{
		setMetaData(RETAIN_DISABLED_META_KEY, (retain) ? true : null);
		return this;
	}
}
