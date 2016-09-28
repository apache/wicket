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
package org.apache.wicket.extensions.markup.html.form.palette.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;


/**
 * Component to keep track of selections on the html side. Also used for encoding and decoding those
 * selections between html and java.
 * 
 * @param <T>
 *            Type of the palette
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public class Recorder<T> extends HiddenField<String>
{
	private static final long serialVersionUID = 1L;

	/** parent palette object */
	private final Palette<T> palette;

	/**
	 * @param id
	 *            component id
	 * @param palette
	 *            parent palette object
	 */
	public Recorder(final String id, final Palette<T> palette)
	{
		super(id, new Model<String>());

		this.palette = palette;
		setOutputMarkupId(true);
	}

	/**
	 * @return parent Palette object
	 */
	public Palette<T> getPalette()
	{
		return palette;
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.html.form.AbstractTextComponent#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();

		initIds();
	}

	/**
	 * Synchronize the ids in this' model from the palette's model.
	 */
	private void initIds()
	{
		// construct the model string based on selection collection
		IChoiceRenderer<? super T> renderer = getPalette().getChoiceRenderer();
		StringBuilder modelStringBuffer = new StringBuilder();
		Collection<T> modelCollection = getPalette().getModelCollection();
		if (modelCollection == null)
		{
			throw new WicketRuntimeException(
				"Expected getPalette().getModelCollection() to return a non-null value."
					+ " Please make sure you have model object assigned to the palette");
		}
		Iterator<T> selection = modelCollection.iterator();

		int i = 0;
		while (selection.hasNext())
		{
			modelStringBuffer.append(renderer.getIdValue(selection.next(), i++));
			if (selection.hasNext())
			{
				modelStringBuffer.append(',');
			}
		}

		// set model and update ids array
		String modelString = modelStringBuffer.toString();
		setModelObject(modelString);
	}

	/**
	 * Get the selected choices based on the palette's available choices and the current model or
	 * input data entered by the user.
	 * 
	 * @return selected choices
	 * 
	 * @see #getValue()
	 */
	public List<T> getSelectedList()
	{
		final IChoiceRenderer<? super T> renderer = getPalette().getChoiceRenderer();
		final Collection<? extends T> choices = getPalette().getChoices();
		final List<T> selected = new ArrayList<>(choices.size());

		// reduce number of method calls by building a lookup table
		final Map<T, String> idForChoice = new HashMap<>(choices.size());
		for (final T choice : choices)
		{
			idForChoice.put(choice, renderer.getIdValue(choice, 0));
		}

		for (final String id : Strings.split(getValue(), ','))
		{
			for (final T choice : choices)
			{
				final String idValue = idForChoice.get(choice);
				if (id.equals(idValue)) // null-safe compare
				{
					selected.add(choice);
					break;
				}
			}
		}

		return selected;
	}

	/**
	 * Get the unselected choices based on the palette's available choices and the current model or
	 * input data entered by the user.
	 * 
	 * @return unselected choices
	 * 
	 * @see #getValue()
	 */
	public List<T> getUnselectedList()
	{
		final IChoiceRenderer<? super T> renderer = getPalette().getChoiceRenderer();
		final Collection<? extends T> choices = getPalette().getChoices();
		final List<T> unselected = new ArrayList<>(choices.size());
		final Set<String> ids = new TreeSet<>(Arrays.asList(Strings.split(getValue(), ',')));

		for (final T choice : choices)
		{
			final String choiceId = renderer.getIdValue(choice, 0);

			if (ids.contains(choiceId) == false)
			{
				unselected.add(choice);
			}
		}

		return unselected;
	}
}
