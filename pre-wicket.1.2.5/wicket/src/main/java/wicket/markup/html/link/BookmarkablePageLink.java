/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.link;

import wicket.AttributeModifier;
import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.model.Model;

/**
 * Renders a stable link which can be cached in a web browser and used at a
 * later time.
 * 
 * @author Jonathan Locke
 */
public class BookmarkablePageLink extends Link
{
	private static final long serialVersionUID = 1L;

	/** The page class that this link links to. */
	private final Class pageClass;

	/** Any page map for this link */
	private String pageMapName = null;

	/** The parameters to pass to the class constructor when instantiated. */
	private final PageParameters parameters;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            The name of this component
	 * @param pageClass
	 *            The class of page to link to
	 */
	public BookmarkablePageLink(final String id, final Class pageClass)
	{
		this(id, pageClass, new PageParameters());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageClass
	 *            The class of page to link to
	 * @param parameters
	 *            The parameters to pass to the new page when the link is
	 *            clicked
	 */
	public BookmarkablePageLink(final String id, final Class pageClass,
			final PageParameters parameters)
	{
		super(id);
		if (pageClass == null)
		{
			throw new IllegalArgumentException("Page class for bookmarkable link cannot be null");
		}
		else if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("Page class must be derived from "
					+ Page.class.getName());
		}
		this.pageClass = pageClass;
		this.parameters = parameters;
	}

	/**
	 * Get tge page class registered with the link
	 * 
	 * @return Page class
	 */
	public final Class getPageClass()
	{
		return this.pageClass;
	}

	/**
	 * @return Page map for this link
	 */
	public final PageMap getPageMap()
	{
		if (pageMapName != null)
		{
			return PageMap.forName(pageMapName);
		}
		else
		{
			return getPage().getPageMap();
		}
	}

	/**
	 * Whether this link refers to the given page.
	 * 
	 * @param page
	 *            the page
	 * @see wicket.markup.html.link.Link#linksTo(wicket.Page)
	 */
	public boolean linksTo(final Page page)
	{
		return page.getClass() == pageClass;
	}

	/**
	 * THIS METHOD IS NOT USED! Bookmarkable links do not have a click handler.
	 * It is here to satisfy the interface only, as bookmarkable links will be
	 * dispatched by the handling servlet.
	 * 
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	public final void onClick()
	{
		// Bookmarkable links do not have a click handler.
		// Instead they are dispatched by the request handling servlet.
	}

	/**
	 * @param pageMap
	 *            The pagemap for this link's destination
	 * @return This
	 */
	public final BookmarkablePageLink setPageMap(final PageMap pageMap)
	{
		this.pageMapName = pageMap.getName();
		if (pageMap != null)
		{
			add(new AttributeModifier("target", false, new Model(pageMapName)));
		}
		return this;
	}

	/**
	 * Adds a given page property value to this link.
	 * 
	 * @param property
	 *            The property
	 * @param value
	 *            The value
	 * @return This
	 */
	public BookmarkablePageLink setParameter(final String property, final int value)
	{
		parameters.put(property, Integer.toString(value));
		return this;
	}

	/**
	 * Adds a given page property value to this link.
	 * 
	 * @param property
	 *            The property
	 * @param value
	 *            The value
	 * @return This
	 */
	public BookmarkablePageLink setParameter(final String property, final long value)
	{
		parameters.put(property, Long.toString(value));
		return this;
	}

	/**
	 * Adds a given page property value to this link.
	 * 
	 * @param property
	 *            The property
	 * @param value
	 *            The value
	 * @return This
	 */
	public BookmarkablePageLink setParameter(final String property, final String value)
	{
		parameters.put(property, value);
		return this;
	}

	/**
	 * Gets the url to use for this link.
	 * 
	 * @return The URL that this link links to
	 * @see wicket.markup.html.link.Link#getURL()
	 */
	protected CharSequence getURL()
	{
		if (pageMapName != null && getPopupSettings() != null)
		{
			throw new IllegalStateException("You cannot specify popup settings and a page map");
		}

		if (getPopupSettings() != null)
		{
			return urlFor(getPopupSettings().getPageMap(this), pageClass, parameters);
		}
		else
		{
			return urlFor(getPageMap(), pageClass, parameters);
		}
	}
}
