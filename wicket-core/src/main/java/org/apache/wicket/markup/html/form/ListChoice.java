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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;


/**
 * Essentially a drop down choice that doesn't drop down. Instead, it scrolls and displays a given
 * number of rows.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Eelco Hillenius
 * 
 * @param <T>
 *            The model object type
 */
public class ListChoice<T> extends DropDownChoice<T>
{
	private static final long serialVersionUID = 1L;

	/** The default maximum number of rows to display. */
	private static final int DEFAULT_MAX_ROWS = 8;

	/** The maximum number of rows to display. */
	private int maxRows;

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String)
	 */
	public ListChoice(final String id)
	{
		this(id, null, (List<? extends T>)null, null, DEFAULT_MAX_ROWS);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, List)
	 */
	public ListChoice(final String id, final List<? extends T> choices)
	{
		this(id, null, choices, null, DEFAULT_MAX_ROWS);
	}

	/**
	 * @param id
	 *            See Component
	 * @param choices
	 *            The list of values in the list
	 * @param renderer
	 *            See AbstractChoice
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      List,ChoiceRenderer)
	 */
	public ListChoice(final String id, final List<? extends T> choices,
		final ChoiceRenderer<? super T> renderer)
	{
		this(id, null, choices, renderer, DEFAULT_MAX_ROWS);
	}


	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The list of values in the list
	 * @see DropDownChoice#DropDownChoice(String, IModel, List)
	 */
	public ListChoice(final String id, final IModel<T> model, final List<? extends T> choices)
	{
		this(id, model, choices, null, DEFAULT_MAX_ROWS);
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The list of values in the list
	 * @param maxRows
	 *            Maximum number of rows to show
	 * @see DropDownChoice#DropDownChoice(String, IModel, List)
	 */
	public ListChoice(final String id, final IModel<T> model, final List<? extends T> choices,
		final int maxRows)
	{
		this(id, model, choices, null, maxRows);
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The list of values in the list
	 * @param renderer
	 *            See AbstractChoice
	 * @see DropDownChoice#DropDownChoice(String, IModel, List)
	 */
	public ListChoice(final String id, final IModel<T> model, final List<? extends T> choices,
		final ChoiceRenderer<? super T> renderer)
	{
		this(id, model, choices, renderer, DEFAULT_MAX_ROWS);
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The list of values in the list
	 * @param renderer
	 *            See AbstractChoice
	 * @param maxRows
	 *            Maximum number of rows to show
	 * @see DropDownChoice#DropDownChoice(String, IModel, List)
	 */
	public ListChoice(final String id, final IModel<T> model, final List<? extends T> choices,
		final ChoiceRenderer<? super T> renderer, final int maxRows)
	{
		super(id, model, choices, renderer);
		this.maxRows = maxRows;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel)
	 */
	public ListChoice(String id, IModel<? extends List<? extends T>> choices)
	{
		this(id, null, choices, null, DEFAULT_MAX_ROWS);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,IModel)
	 */
	public ListChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices)
	{
		this(id, model, choices, null, DEFAULT_MAX_ROWS);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      IModel,ChoiceRenderer)
	 */
	public ListChoice(String id, IModel<? extends List<? extends T>> choices,
		ChoiceRenderer<? super T> renderer)
	{
		this(id, null, choices, renderer, DEFAULT_MAX_ROWS);
	}


	/**
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,
	 *      IModel,ChoiceRenderer)
	 */
	public ListChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices,
		ChoiceRenderer<? super T> renderer)
	{
		this(id, model, choices, renderer, DEFAULT_MAX_ROWS);
	}

	/**
	 * @param id
	 * @param model
	 * @param choices
	 * @param renderer
	 * @param maxRows
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,
	 *      IModel,ChoiceRenderer)
	 */
	public ListChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices,
		ChoiceRenderer<? super T> renderer, int maxRows)
	{
		super(id, model, choices, renderer);
		this.maxRows = maxRows;
	}

	/**
	 * Gets the maximum number of rows to display.
	 * 
	 * @return the maximum number of rows to display
	 */
	public final int getMaxRows()
	{
		return maxRows;
	}

	/**
	 * Sets the maximum number of rows to display.
	 * 
	 * @param maxRows
	 *            the maximum number of rows to display
	 * @return This
	 */
	public final ListChoice<T> setMaxRows(int maxRows)
	{
		this.maxRows = maxRows;
		return this;
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);
		if (!tag.getAttributes().containsKey("size"))
		{
			tag.put("size", maxRows);
		}
	}
}
