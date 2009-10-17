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
package org.apache.wicket.markup.html.list;

import org.apache.wicket.model.IModel;

/**
 * A very simple Item. Usually it is used as based class for more advanced Items.
 * 
 * @author Juergen Donnerstag
 */
public class LoopItem extends AbstractItem
{
	private static final long serialVersionUID = 1L;

	/** The index of the ListItem in the parent ListView */
	private int index;

	/**
	 * Constructor
	 * 
	 * @param index
	 *            The index of the item
	 */
	public LoopItem(final int index)
	{
		super(index);
		this.index = index;
	}

	/**
	 * A constructor which uses the index and the list provided to create a ListItem. This
	 * constructor is the default one.
	 * 
	 * @param index
	 *            The index of the item
	 * @param model
	 *            The model object of the item
	 */
	public LoopItem(final int index, final IModel<?> model)
	{
		super(index, model);
		this.index = index;
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param index
	 *            relative index of this item in the pageable view
	 * @param model
	 *            model for this item
	 */
	public LoopItem(final String id, int index, final IModel<?> model)
	{
		super(id, model);
		this.index = index;
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param index
	 *            relative index of this item in the pageable view
	 */
	public LoopItem(final String id, final int index)
	{
		super(id);
		this.index = index;
	}

	/**
	 * Gets the index of the listItem in the parent listView.
	 * 
	 * @return The index of this listItem in the parent listView
	 */
	public final int getIndex()
	{
		return index;
	}

	/**
	 * Sets the index of this item
	 * 
	 * @param index
	 *            new index
	 */
	public final void setIndex(int index)
	{
		if (this.index != index)
		{
			if (isVersioned())
			{
				addStateChange();
			}
			this.index = index;
		}
	}
}