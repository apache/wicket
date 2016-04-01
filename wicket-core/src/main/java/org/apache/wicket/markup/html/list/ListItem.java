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

import org.apache.wicket.IGenericComponent;
import org.apache.wicket.model.IModel;

/**
 * Container that holds components in a ListView.
 * 
 * @see ListView
 * @author Jonathan Locke
 * 
 * @param <T>
 *            Model object type
 */
public class ListItem<T> extends LoopItem implements IGenericComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 *            component id
	 * @param index
	 *            relative index of this item in the pageable view
	 * @param model
	 *            model for this item
	 */
	public ListItem(final String id, int index, final IModel<T> model)
	{
		super(id, index, model);
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
	public ListItem(final int index, final IModel<T> model)
	{
		super(index, model);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param index
	 *            relative index of this item in the pageable view
	 */
	public ListItem(final String id, final int index)
	{
		super(id, index);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	@Override
	public final void setModel(IModel<T> model)
	{
		setDefaultModel(model);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	@Override
	public final void setModelObject(T object)
	{
		setDefaultModelObject(object);
	}
}
