/*
 * $Id$ $Revision:
 * 1.13 $ $Date$
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
import wicket.WicketRuntimeException;

/**
 * Links to a given page via an object implementing the IPageLink delayed
 * linking interface. PageLinks can be constructed directly with an IPageLink
 * interface or with a Page Class object. In the latter case, an IPageLink
 * implementation is provided which constructs a Page of the given class when
 * the link is clicked. A default no-args constructor must be available in this
 * case or a WicketRuntimeException will be thrown when Wicket fails to
 * instantiate the class.
 * 
 * @see IPageLink
 * @author Jonathan Locke
 */
public class PageLink extends Link
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 8530958543148278216L;

	/** The delayed linking Page source. */
	private final IPageLink pageLink;

	/**
	 * Constructor.
	 * 
	 * @param componentName
	 *            The name of this component
	 * @param pageLink
	 *            An implementation of IPageLink which will create the page
	 *            linked to if and when this hyperlink is clicked at a later
	 *            time.
	 */
	public PageLink(final String componentName, final IPageLink pageLink)
	{
		super(componentName);
		this.pageLink = pageLink;
	}

	/**
	 * Constructor.
	 * 
	 * @param componentName
	 *            Name of this component
	 * @param c
	 *            Page class
	 */
	public PageLink(final String componentName, final Class c)
	{
		this(componentName, new IPageLink()
		{
			/** Serial Version ID */
			private static final long serialVersionUID = 319659497178801753L;

			public Page getPage()
			{
				try
				{
					return (Page)c.newInstance();
				}
				catch (InstantiationException e)
				{
					throw new WicketRuntimeException("Cannot instantiate page class " + c, e);
				}
				catch (IllegalAccessException e)
				{
					throw new WicketRuntimeException("Cannot instantiate page class " + c, e);
				}
			}

			public Class getPageIdentity()
			{
				return c;
			}
		});

		// Ensure that c is a subclass of Page
		if (!Page.class.isAssignableFrom(c))
		{
			throw new IllegalArgumentException("Class " + c + " is not a subclass of Page");
		}
	}

	/**
	 * Handles a link click by asking for a concrete Page instance through the
	 * IPageLink.getPage() delayed linking interface. This call will normally
	 * cause the destination page to be created.
	 * 
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	public void onClick()
	{
		// Set page source's page as response page
		getRequestCycle().setPage(pageLink.getPage());
	}

	/**
	 * Returns true if the given page is of the same class as the (delayed)
	 * destination of this page link.
	 * 
	 * @see wicket.markup.html.link.Link#linksTo(wicket.Page)
	 */
	public boolean linksTo(final Page page)
	{
		return page.getClass() == pageLink.getPageIdentity();
	}
}