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

import org.apache.wicket.model.IDetachable;

/**
 * A piece of a {@link CompoundDynamicColumn}'s cell.
 * <p>
 * The template is trusted markup, inserted into the page as is, and has to be authored by the
 * developer; see {@link IDynamicColumn} for how it is evaluated.
 *
 * @param <T>
 *            the type of the rows
 * @since 11.0.0
 */
@FunctionalInterface
public interface IDynamicColumnContributor<T> extends IDetachable
{
	/**
	 * @return this contributor's part of the cell template
	 */
	String getTemplate();

	/**
	 * Returns this contributor's part of the cell template as rendered in the given table, which
	 * may depend on its locale, style or variation. Returns {@link #getTemplate()} by default.
	 *
	 * @param table
	 *            the table the contributor's column is rendered in
	 * @return this contributor's part of the cell template
	 */
	default String getTemplate(DynamicDataTable<T, ?> table)
	{
		return getTemplate();
	}

	@Override
	default void detach()
	{
	}
}
