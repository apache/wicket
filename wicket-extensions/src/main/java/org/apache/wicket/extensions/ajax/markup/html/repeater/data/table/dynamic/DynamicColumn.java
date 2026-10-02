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

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * A column of a {@link DynamicDataTable} with a fixed template. See {@link IDynamicColumn} for how
 * the template is evaluated, and why it has to be authored by the developer.
 *
 * @param <T>
 *            the type of the rows
 * @since 11.0.0
 */
public class DynamicColumn<T> extends AbstractDynamicColumn<T>
{
	private static final long serialVersionUID = 1L;

	private final String template;

	/**
	 * @param header
	 *            the header text, rendered escaped
	 * @param template
	 *            the cell template, trusted markup authored by the developer
	 */
	public DynamicColumn(IModel<String> header, String template)
	{
		super(header);
		this.template = Args.notNull(template, "template");
	}

	@Override
	public String getTemplate()
	{
		return template;
	}
}
