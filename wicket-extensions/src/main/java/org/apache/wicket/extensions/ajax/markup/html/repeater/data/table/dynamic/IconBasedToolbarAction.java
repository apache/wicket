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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.icon.IIcon;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * An action on the whole table shown as an icon button in its navigation, its tooltip naming what
 * it does, for example adding a row. The tooltip is written escaped, as the button's {@code title}
 * and {@code aria-label}.
 *
 * @param <T>
 *            the type of the rows
 * @param <K>
 *            the type of the keys of the rows
 * @see IconBasedRowAction
 * @since 11.0.0
 */
public abstract class IconBasedToolbarAction<T, K extends Serializable>
	implements
		IDynamicToolbarAction<T, K>
{
	private static final long serialVersionUID = 1L;

	private final IIcon icon;

	private final IModel<String> tooltip;

	/**
	 * @param icon
	 *            the icon of the button
	 * @param tooltip
	 *            what the action does, shown as the button's tooltip and read by screen readers
	 */
	protected IconBasedToolbarAction(IIcon icon, IModel<String> tooltip)
	{
		this.icon = Args.notNull(icon, "icon");
		this.tooltip = Args.notNull(tooltip, "tooltip");
	}

	/**
	 * Returns the icon of the button, the one given to the constructor by default. Override to
	 * choose it when the button is rendered.
	 *
	 * @return the icon of the button
	 */
	public IIcon getIcon()
	{
		return icon;
	}

	/**
	 * @return what the action does, the button's tooltip
	 */
	public IModel<String> getTooltip()
	{
		return tooltip;
	}

	@Override
	public Component newComponent(String id, DynamicDataTable<T, K> table)
	{
		return new IconToolbarButton<>(id, table, this);
	}

	/**
	 * Carries out the action.
	 *
	 * @param table
	 *            the table
	 * @param target
	 *            the Ajax request target
	 */
	protected abstract void onClick(DynamicDataTable<T, K> table, AjaxRequestTarget target);
}
