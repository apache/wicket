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
package org.apache.wicket.extensions.markup.html.repeater.tree;

import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;

/**
 * An implementation of the TableTree that aims to solve the 90% usecase by using {@link Folder}s
 * and by adding navigation, headers and no-records-found toolbars to a standard {@link TableTree}.
 * 
 * @param <T>
 *            The node type
 * @param <S>
 *     the type of the sorting parameter
 * @author svenmeier
 */
public class DefaultTableTree<T, S> extends TableTree<T, S>
{

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param columns
	 *            columns for the {@link DataTable}
	 * @param provider
	 *            the provider of the tree
	 * @param rowsPerPage
	 *            rows to show on each page
	 */
	public DefaultTableTree(String id, List<IColumn<T, S>> columns, ISortableTreeProvider<T, S> provider,
		int rowsPerPage)
	{
		this(id, columns, provider, rowsPerPage, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param columns
	 *            columns for the {@link DataTable}
	 * @param provider
	 *            the provider of the tree
	 * @param rowsPerPage
	 *            rows to show on each page
	 * @param state
	 *            expansion state
	 */
	public DefaultTableTree(String id, List<IColumn<T, S>> columns, ISortableTreeProvider<T, S> provider,
		int rowsPerPage, IModel<Set<T>> state)
	{
		super(id, columns, provider, rowsPerPage, state);

		getTable().addTopToolbar(new NavigationToolbar(getTable()));
		getTable().addTopToolbar(new HeadersToolbar(getTable(), provider));
		getTable().addBottomToolbar(new NoRecordsToolbar(getTable()));

		add(new WindowsTheme());
	}

	/**
	 * Creates {@link Folder} for each node.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            the node model
	 */
	@Override
	protected Component newContentComponent(String id, IModel<T> model)
	{
		return new Folder<T>(id, this, model);
	}

	/**
	 * Creates an {@link OddEvenItem}.
	 * 
	 * @param id
	 *            component id
	 * @param node
	 *            the node model
	 */
	@Override
	protected Item<T> newRowItem(String id, int index, IModel<T> node)
	{
		return new OddEvenItem<T>(id, index, node);
	}
}