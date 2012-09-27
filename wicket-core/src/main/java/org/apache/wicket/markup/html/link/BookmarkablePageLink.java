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
package org.apache.wicket.markup.html.link;

import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.core.util.lang.WicketObjects;

/**
 * Renders a stable link which can be cached in a web browser and used at a later time.
 * 
 * @author Jonathan Locke
 * @param <T>
 *            type of model object, if any
 */
public class BookmarkablePageLink<T> extends Link<T>
{
	private static final long serialVersionUID = 1L;

	/** The page class that this link links to. */
	private final String pageClassName;

	/** The parameters to pass to the class constructor when instantiated. */
	protected PageParameters parameters;

	/**
	 * Constructor.
	 * 
	 * @param <C>
	 *            type of page
	 * 
	 * @param id
	 *            The name of this component
	 * @param pageClass
	 *            The class of page to link to
	 */
	public <C extends Page> BookmarkablePageLink(final String id, final Class<C> pageClass)
	{
		this(id, pageClass, null);
	}

	/**
	 * @return page parameters
	 */
	public PageParameters getPageParameters()
	{
		if (parameters == null)
		{
			parameters = new PageParameters();
		}
		return parameters;
	}


	/**
	 * Constructor.
	 * 
	 * @param <C>
	 * 
	 * @param id
	 *            See Component
	 * @param pageClass
	 *            The class of page to link to
	 * @param parameters
	 *            The parameters to pass to the new page when the link is clicked
	 */
	public <C extends Page> BookmarkablePageLink(final String id, final Class<C> pageClass,
		final PageParameters parameters)
	{
		super(id);

		this.parameters = parameters;

		if (pageClass == null)
		{
			throw new IllegalArgumentException("Page class for bookmarkable link cannot be null");
		}
		else if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("Page class must be derived from " +
				Page.class.getName());
		}
		pageClassName = pageClass.getName();
	}

	/**
	 * Get the page class registered with the link
	 * 
	 * @return Page class
	 */
	public final Class<? extends Page> getPageClass()
	{
		return WicketObjects.resolveClass(pageClassName);
	}

	/**
	 * Whether this link refers to the given page.
	 * 
	 * @param page
	 *            the page
	 * @see org.apache.wicket.markup.html.link.Link#linksTo(org.apache.wicket.Page)
	 */
	@Override
	public boolean linksTo(final Page page)
	{
		return page.getClass() == getPageClass();
	}

	@Override
	protected boolean getStatelessHint()
	{
		return true;
	}

	/**
	 * THIS METHOD IS NOT USED! Bookmarkable links do not have a click handler. It is here to
	 * satisfy the interface only, as bookmarkable links will be dispatched by the handling servlet.
	 * 
	 * @see org.apache.wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public final void onClick()
	{
		// Bookmarkable links do not have a click handler.
		// Instead they are dispatched by the request handling servlet.
	}

	/**
	 * Gets the url to use for this link.
	 * 
	 * @return The URL that this link links to
	 * @see org.apache.wicket.markup.html.link.Link#getURL()
	 */
	@Override
	protected CharSequence getURL()
	{
		PageParameters parameters = getPageParameters();

		return urlFor(getPageClass(), parameters);
	}
}
