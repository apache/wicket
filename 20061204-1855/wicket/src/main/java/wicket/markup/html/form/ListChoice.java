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

import wicket.markup.ComponentTag;
import wicket.model.IModel;

/**
 * Essentially a drop down choice that doesn't drop down. Instead, it scrolls
 * and displays a given number of rows.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Eelco Hillenius
 */
public class ListChoice extends DropDownChoice
{
	private static final long serialVersionUID = 1L;
	
	/** The default maximum number of rows to display. */
	private static int defaultMaxRows = 8;
	
	/** The maximum number of rows to display. */
	private int maxRows;

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
	 *			  The defaultMaxRows to set.
	 */
	protected static void setDefaultMaxRows(final int defaultMaxRows)
	{
		ListChoice.defaultMaxRows = defaultMaxRows;
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String)
	 */
	public ListChoice(final String id)
	{
		super(id);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, List)
	 */
	public ListChoice(final String id, final List values)
	{
		super(id, values);
	}

	/**
	 * @param id
	 *			  See Component
	 * @param choices
	 *			  The collection of values in the list
	 * @param renderer
	 * 			  See AbstractChoice 
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, List,IChoiceRenderer)
	 */
	public ListChoice(final String id, final List choices,final IChoiceRenderer renderer)
	{
		super(id, choices,renderer);
	}

	/**
	 * @param id
	 *			  See Component
	 * @param model
	 *			  See Component
	 * @param choices
	 *			  The collection of values in the list
	 * @param renderer
	 * 			  See AbstractChoice 
	 * @see DropDownChoice#DropDownChoice(String, IModel, List)
	 */
	public ListChoice(final String id, final IModel model, final List choices,final IChoiceRenderer renderer)
	{
		this(id, model, choices,renderer, defaultMaxRows);
	}

	/**
	 * @param id
	 *			  See Component
	 * @param model
	 *			  See Component
	 * @param choices
	 *			  The collection of values in the list
	 * @param renderer
	 * 			  See AbstractChoice 
	 * @param maxRows
	 *			  Maximum number of rows to show
	 * @see DropDownChoice#DropDownChoice(String, IModel, List)
	 */
	public ListChoice(final String id, final IModel model, final List choices, final IChoiceRenderer renderer, final int maxRows)
	{
		super(id, model, choices,renderer);
		this.maxRows = maxRows;
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel)
	 */
	public ListChoice(String id, IModel choices)
	{
		super(id, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,IModel)
	 */
	public ListChoice(String id, IModel model, IModel choices)
	{
		super(id, model, choices);
	}
	
	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,IChoiceRenderer)
	 */
	public ListChoice(String id, IModel choices, IChoiceRenderer renderer)
	{
		super(id, choices, renderer);
	}


	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, IModel,IChoiceRenderer)
	 */
	public ListChoice(String id, IModel model, IModel choices, IChoiceRenderer renderer)
	{
		super(id, model, choices, renderer);
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
	 *			  the maximum number of rows to display
	 * @return This
	 */
	public final ListChoice setMaxRows(int maxRows)
	{
		this.maxRows = maxRows;
		return this;
	}

	/**
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected final void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("size", maxRows);
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#supportsPersistence()
	 */
	protected final boolean supportsPersistence()
	{
		return true;
	}

	/**
	 * @param id
	 *			  See Component
	 * @param model
	 *			  See Component
	 * @param choices
	 *			  The collection of values in the list
	 * @see DropDownChoice#DropDownChoice(String, IModel, List)
	 */
	public ListChoice(final String id, final IModel model, final List choices)
	{
		this(id, model, choices, defaultMaxRows);
	}

	/**
	 * @param id
	 *			  See Component
	 * @param model
	 *			  See Component
	 * @param choices
	 *			  The collection of values in the list
	 * @param maxRows
	 *			  Maximum number of rows to show
	 * @see DropDownChoice#DropDownChoice(String, IModel, List)
	 */
	public ListChoice(final String id, final IModel model, final List choices, final int maxRows)
	{
		super(id, model, choices);
		this.maxRows = maxRows;
	}
}
