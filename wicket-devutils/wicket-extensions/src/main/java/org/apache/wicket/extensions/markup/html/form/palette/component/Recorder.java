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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
public class Recorder<T> extends HiddenField<Object>
{
	private static final long serialVersionUID = 1L;

	private static final String[] EMPTY_IDS = new String[0];

	/** conveniently maintained array of selected ids */
	private String[] ids;

	/** parent palette object */
	private final Palette<T> palette;

	private boolean attached = false;

	/**
	 * @return parent Palette object
	 */
	public Palette<T> getPalette()
	{
		return palette;
	}

	/**
	 * @param id
	 *            component id
	 * @param palette
	 *            parent palette object
	 */
	@SuppressWarnings("unchecked")
	public Recorder(String id, Palette<T> palette)
	{
		super(id);
		this.palette = palette;
		setDefaultModel(new Model<Serializable>());
		setOutputMarkupId(true);
	}

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();

		if (!getForm().hasError())
		{
			initIds();
		}
		attached = true;
	}

	/**
	 * Synchronize ids collection from the palette's model
	 */
	private void initIds()
	{
		// construct the model string based on selection collection
		IChoiceRenderer<T> renderer = getPalette().getChoiceRenderer();
		StringBuffer modelStringBuffer = new StringBuffer();
		Iterator<T> selection = getPalette().getModelCollection().iterator();

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
		setDefaultModel(new Model<String>(modelString));
		updateIds(modelString);
	}


	@Override
	protected void onValid()
	{
		super.onValid();
		if (attached)
			updateIds();
	}

	/**
	 * @return iterator over selected choices
	 */
	@SuppressWarnings("unchecked")
	public Iterator<T> getSelectedChoices()
	{
		IChoiceRenderer<T> renderer = getPalette().getChoiceRenderer();
		if (ids.length == 0)
		{
			return Collections.EMPTY_LIST.iterator();
		}

		List<T> selected = new ArrayList<T>(ids.length);
		for (int i = 0; i < ids.length; i++)
		{
			Iterator<? extends T> it = getPalette().getChoices().iterator();
			while (it.hasNext())
			{
				final T choice = it.next();
				if (renderer.getIdValue(choice, 0).equals(ids[i]))
				{
					selected.add(choice);
					break;
				}
			}
		}
		return selected.iterator();
	}

	/**
	 * @return iterator over unselected choices
	 */
	public Iterator<T> getUnselectedChoices()
	{
		IChoiceRenderer<T> renderer = getPalette().getChoiceRenderer();
		Collection<T> choices = getPalette().getChoices();

		if (choices.size() - ids.length == 0)
		{
			return Collections.<T>emptyList().iterator();
		}

		List<T> unselected = new ArrayList<T>(Math.max(1, choices.size() - ids.length));
		Iterator<T> it = choices.iterator();
		while (it.hasNext())
		{
			final T choice = it.next();
			final String choiceId = renderer.getIdValue(choice, 0);
			boolean selected = false;
			for (int i = 0; i < ids.length; i++)
			{
				if (ids[i].equals(choiceId))
				{
					selected = true;
					break;
				}
			}
			if (!selected)
			{
				unselected.add(choice);
			}
		}
		return unselected.iterator();
	}


	@Override
	protected void onInvalid()
	{
		super.onInvalid();
		if (attached)
			updateIds();
	}

	private void updateIds()
	{
		updateIds(getValue());
	}

	private void updateIds(String value)
	{
		if (Strings.isEmpty(value))
		{
			ids = EMPTY_IDS;
		}
		else
		{
			ids = value.split(",");
		}
	}

}
