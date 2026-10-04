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

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;

/**
 * A column of a {@link DynamicDataTable} whose template is read from an {@code .html} resource,
 * found the way a component's markup is: for the table's style, variation and locale, so a
 * {@code MyColumn_de.html} next to {@code MyColumn.html} serves German users. As with a panel,
 * the template is the content of the file's {@code <wicket:panel>}, or the whole file if it has
 * none.
 * <p>
 * A subclass reads the template named after itself, {@code MyColumn.html} next to
 * {@code MyColumn.java}, or after its superclasses; an instance of this class reads a named
 * template from a given package. The {@link DynamicDataTable} caches the templates it reads.
 * <p>
 * The template is written into the page as markup, so the resource has to be authored by the
 * developer; see {@link IDynamicColumn}.
 *
 * @param <T>
 *            the type of the rows
 * @since 11.0.0
 */
public class ResourceDynamicColumn<T> extends AbstractDynamicColumn<T>
{
	private static final long serialVersionUID = 1L;

	private final ResourceTemplate template;

	/**
	 * Reads the template named after this column's class.
	 *
	 * @param header
	 *            the header text, rendered escaped
	 */
	protected ResourceDynamicColumn(IModel<String> header)
	{
		super(header);
		template = ResourceTemplate.forClass(getClass(), ResourceDynamicColumn.class);
	}

	/**
	 * Reads the template {@code name.html} in the package of {@code scope}.
	 *
	 * @param header
	 *            the header text, rendered escaped
	 * @param scope
	 *            the class whose package holds the template
	 * @param name
	 *            the name of the template, without the extension
	 */
	public ResourceDynamicColumn(IModel<String> header, Class<?> scope, String name)
	{
		super(header);
		template = ResourceTemplate.named(scope, name);
	}

	/**
	 * Reads the template for the session's style and locale, uncached.
	 */
	@Override
	public String getTemplate()
	{
		Session session = Session.get();
		return template.load(session.getStyle(), null, session.getLocale());
	}

	/**
	 * Reads the template for the table's style, variation and locale, through the table's
	 * cache.
	 */
	@Override
	public String getTemplate(DynamicDataTable<T, ?> table)
	{
		return table.getTemplate(template);
	}
}
