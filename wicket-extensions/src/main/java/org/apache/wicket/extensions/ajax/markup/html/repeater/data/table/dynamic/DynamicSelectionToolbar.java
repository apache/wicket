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

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * The bar offering to select every row of a {@link DynamicDataTable} once every row of its page is
 * selected, and to clear that selection again. Shown only then; see
 * {@link DynamicDataTable#getSelection()}.
 * <p>
 * The texts are the resources {@code DynamicDataTable.pageSelected},
 * {@code DynamicDataTable.selectAll}, {@code DynamicDataTable.allSelected} and
 * {@code DynamicDataTable.clearSelection}, with the variables {@code ${count}}, the number of rows
 * of the page, and {@code ${total}}, the number of all rows.
 *
 * @since 11.0.0
 */
public class DynamicSelectionToolbar extends AbstractDynamicToolbar
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 *            component id
	 * @param table
	 *            the table
	 */
	public DynamicSelectionToolbar(String id, DynamicDataTable<?, ?> table)
	{
		super(id, table);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		DynamicDataTable<?, ?> table = getTable();

		WebMarkupContainer span = new WebMarkupContainer("span");
		span.add(newColspan());
		add(span);

		IModel<Map<String, Long>> counts = () -> Map.of("count",
			(long)table.getCurrentPageKeys().size(), "total", table.getItemCount());

		WebMarkupContainer page = new WebMarkupContainer("page")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(table.isAllSelected() == false);
			}
		};
		span.add(page);
		page.add(new Label("message", new StringResourceModel("DynamicDataTable.pageSelected",
			this, counts).setDefaultValue("All ${count} rows on this page are selected.")));
		page.add(new AjaxLink<Void>("selectAll")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				table.selectAll(target, true);
			}
		}.setBody(new StringResourceModel("DynamicDataTable.selectAll", this, counts)
			.setDefaultValue("Select all ${total} rows")));

		WebMarkupContainer all = new WebMarkupContainer("all")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(table.isAllSelected());
			}
		};
		span.add(all);
		all.add(new Label("message", new StringResourceModel("DynamicDataTable.allSelected", this,
			counts).setDefaultValue("All ${total} rows are selected.")));
		all.add(new AjaxLink<Void>("clear")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				table.selectAll(target, false);
			}
		}.setBody(new ResourceModel("DynamicDataTable.clearSelection", "Clear selection")));
	}

	@Override
	protected void onConfigure()
	{
		super.onConfigure();
		DynamicDataTable<?, ?> table = getTable();
		setVisible(table.isAllSelected() || table.isPageSelected());
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.append("class", "dynamic-data-table-selection-toolbar", " ");
	}
}
