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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.icon.IIcon;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.lang.Args;

/**
 * Lets the user choose the columns a {@link DynamicDataTable} shows: an icon button in the
 * navigation opens a list of the table's {@link IHideableColumn hideable} columns,
 * each with a tick while it is shown. Clicking a column toggles its tick; the apply button below
 * the list shows the ticked columns, hides the others and re-renders the table. Escape or a click
 * elsewhere closes the list without changing anything.
 * <p>
 * The columns are listed by their header text, escaped.
 *
 * @param <T>
 *            the type of the rows
 * @param <K>
 *            the type of the keys of the rows
 * @see DynamicDataTable#setColumnVisible(IDynamicColumn, boolean)
 * @since 11.0.0
 */
public class ColumnChooserToolbarAction<T, K extends Serializable>
	implements
		IDynamicToolbarAction<T, K>
{
	private static final long serialVersionUID = 1L;

	private static final IIcon COLUMNS = () -> "<svg aria-hidden=\"true\" focusable=\"false\" " +
		"viewBox=\"0 0 24 24\" width=\"18\" height=\"18\"><path fill=\"currentColor\" " +
		"d=\"M3 4h18v16H3zm2 2v12h4V6zm6 0v12h2V6zm4 0v12h4V6z\"/></svg>";

	private IModel<String> label = new ResourceModel("DynamicDataTable.chooseColumns", "Columns");

	private IIcon icon = COLUMNS;

	@Override
	public Component newComponent(String id, DynamicDataTable<T, K> table)
	{
		return new ColumnChooser<>(id, table, this);
	}

	/**
	 * @return the label of the button, its tooltip and accessible name
	 */
	public IModel<String> getLabel()
	{
		return label;
	}

	/**
	 * @param label
	 *            the label of the button, its tooltip and accessible name, written escaped;
	 *            {@code DynamicDataTable.chooseColumns} by default
	 * @return {@code this}
	 */
	public ColumnChooserToolbarAction<T, K> setLabel(IModel<String> label)
	{
		this.label = Args.notNull(label, "label");
		return this;
	}

	/**
	 * @return the icon of the button
	 */
	public IIcon getIcon()
	{
		return icon;
	}

	/**
	 * @param icon
	 *            the icon of the button, a grid of columns by default
	 * @return {@code this}
	 */
	public ColumnChooserToolbarAction<T, K> setIcon(IIcon icon)
	{
		this.icon = Args.notNull(icon, "icon");
		return this;
	}
}
