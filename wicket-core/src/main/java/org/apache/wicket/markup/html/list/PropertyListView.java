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

import java.util.List;

import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;


/**
 * Simple ListVew subclass that wraps its item models in a {@link CompoundPropertyModel}. Useful for
 * lists where the item components will be mapped through property expressions.
 * 
 * @author Nathan Hamblen
 * 
 * @param <T>
 *            Model object type
 */
public abstract class PropertyListView<T> extends ListView<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct without model, assume bound externally.
	 * 
	 * @param id
	 *            Wicket id
	 */
	public PropertyListView(final String id)
	{
		super(id);
	}

	/**
	 * Construct with a model.
	 * 
	 * @param id
	 *            Wicket id
	 * @param model
	 *            wrapping a List
	 */
	public PropertyListView(final String id, final IModel<? extends List<T>> model)
	{
		super(id, model);
	}

	/**
	 * Construct with a "small," unmodeled List. The object can not be detached and will reside in
	 * the session, but is convenient for lists of a limited size.
	 * 
	 * @param id
	 *            Wicket id
	 * @param list
	 *            unmodeled List
	 */
	public PropertyListView(final String id, final List<T> list)
	{
		super(id, list);
	}

	/**
	 * Wraps a ListItemModel in a CompoundPropertyModel.
	 * 
	 * @param model
	 * @param index
	 * @return a CompoundPropertyModel wrapping a ListItemModel
	 */
	@Override
	protected IModel<T> getListItemModel(final IModel<? extends List<T>> model, final int index)
	{
		return new CompoundPropertyModel<T>(super.getListItemModel(model, index));
	}
}
