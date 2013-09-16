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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.Model;


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
	 * Synchronize ids collection from the palette's model
	 */
	private void initIds()
	{
		// construct the model string based on selection collection
		IChoiceRenderer<T> renderer = getPalette().getChoiceRenderer();
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
				modelStringBuffer.append(",");
			}
		}

		// set model and update ids array
		String modelString = modelStringBuffer.toString();
		setModelObject(modelString);
	}

	/**
	 * @return list over selected choices
	 */
	public List<T> getSelectedList()
	{
		final IChoiceRenderer<T> renderer = getPalette().getChoiceRenderer();
		final Collection<? extends T> choices = getPalette().getChoices();
		final List<T> selected = new ArrayList<>(choices.size());

		// reduce number of method calls by building a lookup table
		final Map<T, String> idForChoice = new HashMap<>(choices.size());
		for (final T choice : choices)
		{
			idForChoice.put(choice, renderer.getIdValue(choice, 0));
		}

		for (final String id : getValue().split(","))
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
	 * @return list over unselected choices
	 */
	public List<T> getUnselectedList()
	{
		final IChoiceRenderer<T> renderer = getPalette().getChoiceRenderer();
		final Collection<? extends T> choices = getPalette().getChoices();
		final List<T> unselected = new ArrayList<T>(choices.size());
		final String ids = getValue();

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