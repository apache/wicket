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

import wicket.Page;
import wicket.PageParameters;

/**
 * Renders a stable link which can be cached in a web browser and used at a
 * later time.
 * 
 * @author Jonathan Locke
 */
public final class BookmarkablePageLink extends Link
{
	/** Serial Version ID */
	private static final long serialVersionUID = 2396751463296314926L;

	/** The page class that this link links to. */
	private final Class pageClass;

	/** The parameters to pass to the class constructor when instantiated. */
	private final PageParameters parameters;

	/**
	 * Constructor.
	 * 
	 * @param componentName
	 *            The name of this component
	 * @param pageClass
	 *            The class of page to link to
	 */
	public BookmarkablePageLink(final String componentName, final Class pageClass)
	{
		this(componentName, pageClass, new PageParameters());
	}

	/**
	 * Constructor.
	 * 
	 * @param componentName
	 *            The name of this component
	 * @param pageClass
	 *            The class of page to link to
	 * @param parameters
	 *            The parameters to pass to the new page when the link is
	 *            clicked
	 */
	public BookmarkablePageLink(final String componentName, final Class pageClass,
			final PageParameters parameters)
	{
		super(componentName);
		this.pageClass = pageClass;
		this.parameters = parameters;
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
	 * Called when a link is clicked; this is here to satisfy the interface, as
	 * bookmarkable links will be dispatched by the handling servlet.
	 * 
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	public void onClick()
	{
		// Bookmarkable links do not have a click handler.
		// Instead they are dispatched by the request handling servlet.
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
	protected String getURL()
	{
		// add href using url to the dispatcher
		return getRequestCycle().urlFor(pageClass, parameters);
	}
}
