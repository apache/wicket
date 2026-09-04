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
package org.apache.wicket.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.util.lang.Args;

/**
 * For each wicket:head tag a HeaderPartContainer is created and added to the HtmlHeaderContainer
 * which has been added to the Page.
 * 
 * @author Juergen Donnerstag
 */
public final class HeaderPartContainer extends WebMarkupContainer implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/** The panel or bordered page the header part is associated with */
	private final MarkupContainer container;

	/** &lt;wicket:head scope="..."&gt;. A kind of namespace */
	private final String scope;

	/**
	 * @param id
	 *            The component id
	 * @param container
	 *            The Panel (or bordered page) the header part is associated with
	 * @param markup
	 */
	public HeaderPartContainer(final String id, final MarkupContainer container,
		final IMarkupFragment markup)
	{
		super(id);

		Args.notNull(container, "container");
		Args.notNull(markup, "markup");

		setMarkup(markup);

		this.container = container;

		scope = getScopeFromMarkup();

		setRenderBodyOnly(true);
	}

	/**
	 * 
	 * @return get "wicket:scope" attribute from &lt;wicket:head&gt; tag
	 */
	private String getScopeFromMarkup()
	{
		IMarkupFragment markup = getMarkup();
		String namespace = markup.getMarkupResourceStream().getWicketNamespace();
		ComponentTag tag = (ComponentTag)markup.get(0);
		return tag.getAttributes().getString(namespace + ":scope");
	}

	/**
	 * Get the scope of the header part
	 * 
	 * @return The scope name
	 */
	public final String getScope()
	{
		return scope;
	}

	/**
	 * The tag must be resolved against the panel and not against the page
	 */
	@Override
	public final Component resolve(final MarkupContainer container,
		final MarkupStream markupStream, final ComponentTag tag)
	{
		// The tag must be resolved against the panel and not against the page
		return this.container.get(tag.getId());
	}
}
