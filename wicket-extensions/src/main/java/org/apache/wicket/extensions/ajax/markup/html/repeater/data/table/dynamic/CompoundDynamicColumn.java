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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * A column whose template is the concatenation of its contributors' templates, in order.
 * <p>
 * Contributors that are {@link IAjaxActionColumnContributor}s are registered with the
 * {@link DynamicDataTable} the column is given to, which dispatches their actions to them.
 *
 * @param <T>
 *            the type of the rows
 * @since 11.0.0
 */
public class CompoundDynamicColumn<T> extends AbstractDynamicColumn<T>
{
	private static final long serialVersionUID = 1L;

	private final List<IDynamicColumnContributor<T>> contributors;

	/**
	 * @param header
	 *            the header text, rendered escaped
	 * @param contributors
	 *            the contributors making up the cell, in order
	 */
	public CompoundDynamicColumn(IModel<String> header,
		List<? extends IDynamicColumnContributor<T>> contributors)
	{
		super(header);
		this.contributors = new ArrayList<>(Args.notEmpty(contributors, "contributors"));
	}

	@Override
	public String getTemplate()
	{
		return contributors.stream()
			.map(IDynamicColumnContributor::getTemplate)
			.collect(Collectors.joining());
	}

	@Override
	public String getTemplate(DynamicDataTable<T, ?> table)
	{
		return contributors.stream()
			.map(contributor -> contributor.getTemplate(table))
			.collect(Collectors.joining());
	}

	/**
	 * @return the contributors making up the cell, in order
	 */
	public List<IDynamicColumnContributor<T>> getContributors()
	{
		return Collections.unmodifiableList(contributors);
	}

	@Override
	public void detach()
	{
		super.detach();
		for (IDynamicColumnContributor<T> contributor : contributors)
		{
			contributor.detach();
		}
	}
}
