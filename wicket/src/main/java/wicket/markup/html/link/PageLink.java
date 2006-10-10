/*
 * $Id$ $Revision$
 * $Date$
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

import wicket.MarkupContainer;
import wicket.Page;

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
	private static final long serialVersionUID = 1L;

	/** The delayed linking Page source. */
	private final IPageLink pageLink;

	/**
	 * Constructs a link that instantiates the given Page class when the link is
	 * clicked. The instantiated Page is used to render a response to the user.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param c
	 *            Page class
	 */
	public PageLink(MarkupContainer parent, final String id, final Class<? extends Page> c)
	{
		super(parent, id);

		// Ensure that c is a subclass of Page
		if (!Page.class.isAssignableFrom(c))
		{
			throw new IllegalArgumentException("Class " + c + " is not a subclass of Page");
		}

		this.pageLink = new IPageLink()
		{
			private static final long serialVersionUID = 1L;

			public Page getPage()
			{
				// Create page using page factory
				return PageLink.this.getPage().getPageFactory().newPage(c);
			}

			public Class getPageIdentity()
			{
				return c;
			}
		};
	}

	/**
	 * This constructor is ideal if a Page object was passed in from a previous
	 * Page. Construct a link to the Page.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See component
	 * @param page
	 *            The page
	 */
	public PageLink(MarkupContainer parent, final String id, final Page page)
	{
		super(parent, id);

		this.pageLink = new IPageLink()
		{
			private static final long serialVersionUID = 1L;

			public Page getPage()
			{
				// Create page using page factory
				return page;
			}

			public Class getPageIdentity()
			{
				return page.getClass();
			}
		};
	}

	/**
	 * This constructor is ideal for constructing pages lazily.
	 * 
	 * Constructs a link which invokes the getPage() method of the IPageLink
	 * interface when the link is clicked. Whatever Page objects is returned by
	 * this method will be rendered back to the user.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param pageLink
	 *            An implementation of IPageLink which will create the page
	 *            linked to if and when this hyperlink is clicked at a later
	 *            time.
	 */
	public PageLink(MarkupContainer parent, final String id, final IPageLink pageLink)
	{
		super(parent, id);
		this.pageLink = pageLink;
	}

	/**
	 * Returns true if the given page is of the same class as the (delayed)
	 * destination of this page link.
	 * 
	 * @see wicket.markup.html.link.Link#linksTo(wicket.Page)
	 */
	@Override
	public boolean linksTo(final Page page)
	{
		return page.getClass() == pageLink.getPageIdentity();
	}

	/**
	 * Handles a link click by asking for a concrete Page instance through the
	 * IPageLink.getPage() delayed linking interface. This call will normally
	 * cause the destination page to be created.
	 * 
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public void onClick()
	{
		// Set page source's page as response page
		setResponsePage(pageLink.getPage());
	}
}