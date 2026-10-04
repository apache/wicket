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

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IModel;

class ColumnChooser<T, K extends Serializable> extends Panel
{
	private static final long serialVersionUID = 1L;

	private final DynamicDataTable<T, K> table;

	private final ColumnChooserToolbarAction<T, K> action;

	ColumnChooser(String id, DynamicDataTable<T, K> table, ColumnChooserToolbarAction<T, K> action)
	{
		super(id);
		this.table = table;
		this.action = action;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		IModel<String> label = action.getLabel();
		IModel<String> assigned = label instanceof IComponentAssignedModel<String> resource
			? resource.wrapOnAssignment(this) : label;
		WebMarkupContainer toggle = new WebMarkupContainer("toggle");
		toggle.add(AttributeModifier.replace("title", assigned),
			AttributeModifier.replace("aria-label", assigned));
		toggle.add(new Label("icon", () -> action.getIcon().getMarkup()).setEscapeModelStrings(false)
			.setRenderBodyOnly(true));
		add(toggle);

		WebMarkupContainer menu = new WebMarkupContainer("menu");
		menu.add(AttributeModifier.replace("aria-label", assigned));
		add(menu);
		menu.add(new ListView<>("columns", this::hideableColumns)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<IDynamicColumn<T>> item)
			{
				IDynamicColumn<T> column = item.getModelObject();
				WebMarkupContainer button = new WebMarkupContainer("column");
				button.add(AttributeModifier.replace("data-dt-column",
					table.getColumns().indexOf(column)));
				button.add(AttributeModifier.replace("aria-pressed",
					String.valueOf(table.isColumnVisible(column))));
				button.add(new Label("label", column.getHeader()));
				item.add(button);
			}
		});
	}

	private List<IDynamicColumn<T>> hideableColumns()
	{
		return table.getColumns().stream().filter(table::isHideable).toList();
	}
}
