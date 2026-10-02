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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * A row above or below the rows of a {@link DynamicDataTable}, such as its navigation, rendered by
 * Wicket. A toolbar is a panel attached to a {@code <tr>}; its markup holds the cells, usually one
 * spanning all columns, see {@link #newColspan()}.
 * <p>
 * Add a toolbar of your own with {@link DynamicDataTable#addTopToolbar(AbstractDynamicToolbar)} or
 * {@link DynamicDataTable#addBottomToolbar(AbstractDynamicToolbar)}. The table re-renders its
 * toolbars via Ajax whenever its rows change, see {@link #refresh(IPartialPageRequestHandler)}.
 *
 * @since 11.0.0
 */
public abstract class AbstractDynamicToolbar extends Panel
{
	private static final long serialVersionUID = 1L;

	private final DynamicDataTable<?, ?> table;

	/**
	 * Constructor for a toolbar added to a table, with an id the table generates.
	 *
	 * @param table
	 *            the table the toolbar belongs to
	 */
	protected AbstractDynamicToolbar(DynamicDataTable<?, ?> table)
	{
		this(Args.notNull(table, "table").newToolbarId(), table);
	}

	/**
	 * @param id
	 *            component id
	 * @param table
	 *            the table the toolbar belongs to
	 */
	protected AbstractDynamicToolbar(String id, DynamicDataTable<?, ?> table)
	{
		super(id);
		this.table = Args.notNull(table, "table");
		setOutputMarkupPlaceholderTag(true);
	}

	/**
	 * @return the table the toolbar belongs to
	 */
	public DynamicDataTable<?, ?> getTable()
	{
		return table;
	}

	/**
	 * @return a behavior setting the {@code colspan} of a cell to the number of shown columns
	 */
	protected Behavior newColspan()
	{
		return AttributeModifier.replace("colspan",
			(IModel<String>)() -> String.valueOf(table.getVisibleColumns().size()));
	}

	/**
	 * Re-renders the toolbar after the rows of the table changed, for example after paging. Adds
	 * the toolbar to the target by default.
	 *
	 * @param target
	 *            the handler of the current Ajax or web socket request
	 */
	public void refresh(IPartialPageRequestHandler target)
	{
		target.add(this);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		checkComponentTag(tag, "tr");
		super.onComponentTag(tag);
	}
}
