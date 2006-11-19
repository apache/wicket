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
package wicket.markup.html.list;

import java.util.List;

import wicket.Component;
import wicket.model.AbstractDetachableModel;
import wicket.model.IModel;

/**
 * Model for list items.
 */
public class ListItemModel extends AbstractDetachableModel
{
	private static final long serialVersionUID = 1L;
	
	// It is easy and cheap to re-build it if necessary.
	// Avoid synchronising it in a cluster
	private transient Object object;

	/** The ListView's list model */
	private final ListView listView;

	/* The list item's index */
	private final int index;

	/**
	 * Construct
	 * 
	 * @param listView
	 *            The ListView
	 * @param index
	 *            The index of this model
	 */
	public ListItemModel(final ListView listView, final int index)
	{
		this.listView = listView;
		this.index = index;
		attach();
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onAttach()
	 */
	protected void onAttach()
	{
		// Re-attach the model object based on index and ListView model object
		this.object = ((List)listView.getModelObject()).get(index);
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		this.object = null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onGetObject(wicket.Component)
	 */
	protected Object onGetObject(final Component component)
	{
		return object;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onSetObject(wicket.Component,
	 *      java.lang.Object)
	 */
	protected void onSetObject(final Component component, final Object object)
	{
		this.object = object;
	}
}
