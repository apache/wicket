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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

/**
 * A column of a {@link DynamicDataTable}: a header rendered by Wicket and a template the browser
 * evaluates against each row's JSON to produce the cell.
 * <p>
 * <strong>The template is markup, inserted into the page as is.</strong> It must be authored by
 * the developer and never built from user input. Only the values it refers to are data: with the
 * {@link BuiltInTemplateEngine default engine} {@code {{path}}} writes a value HTML-escaped and
 * {@code {{{path}}}} writes it unescaped, which is only safe for values the application trusts
 * to be markup. Other engines apply their own escaping rules.
 * <p>
 * Besides the row's data, a template can refer to row variables, written with a leading
 * {@code @} with the built-in engine and Handlebars alike: {@code {{@number}}}, the 1-based
 * position of the row across all pages, {@code {{@key}}}, the row's key as the browser shows it,
 * and {@code {{@selected}}}, whether the row is selected.
 * <p>
 * An element in the template carrying a {@code data-dt-action} attribute becomes an action:
 * clicking it calls {@link DynamicDataTable#onAction} with the attribute's value and the row, for
 * example {@code <a href="#" data-dt-action="delete">Delete</a>}.
 *
 * @param <T>
 *            the type of the rows
 * @see DynamicColumn
 * @since 11.0.0
 */
public interface IDynamicColumn<T> extends IDetachable
{
	/**
	 * @return the header text, rendered escaped
	 */
	IModel<String> getHeader();

	/**
	 * Creates the component showing the header, attached to a {@code <span>}: a label of
	 * {@link #getHeader()} by default.
	 *
	 * @param id
	 *            the id the component must have
	 * @return the component
	 */
	default Component newHeader(String id)
	{
		return new Label(id, getHeader());
	}

	/**
	 * @return the cell template, trusted markup authored by the developer
	 */
	String getTemplate();

	/**
	 * Returns the cell template as rendered in the given table, which may depend on its locale,
	 * style or variation. Returns {@link #getTemplate()} by default.
	 *
	 * @param table
	 *            the table the column is rendered in
	 * @return the cell template, trusted markup authored by the developer
	 */
	default String getTemplate(DynamicDataTable<T, ?> table)
	{
		return getTemplate();
	}

	/**
	 * @return the CSS class put on the header and on every cell of the column, or {@code null}
	 */
	default String getCssClass()
	{
		return null;
	}

	/**
	 * @return whether the user can resize the column, when the table's
	 *         {@link DynamicDataTable#setColumnResizing column resizing} is on; {@code true} by
	 *         default
	 */
	default boolean isResizable()
	{
		return true;
	}

	/**
	 * @return whether the column has a value to export, for example by a
	 *         {@link CsvExportToolbarAction}; {@code false} by default
	 */
	default boolean isExportable()
	{
		return false;
	}

	/**
	 * @param row
	 *            a row
	 * @return the value of the column for the row as exported, for example to a CSV file;
	 *         {@code null} by default
	 */
	default Object getExportValue(T row)
	{
		return null;
	}

	@Override
	default void detach()
	{
		IModel<String> header = getHeader();
		if (header != null)
		{
			header.detach();
		}
	}
}
