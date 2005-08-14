/*
 * $Id: PageableListViewNavigationIncrementLink.java,v 1.3 2005/02/17 06:15:27
 * jonathanlocke Exp $ $Revision$ $Date$
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
package wicket.markup.html.list;

import wicket.Page;
import wicket.markup.html.link.IPageLink;
import wicket.markup.html.link.PageLink;

/**
 * An incremental link to a page of a PageableListView. Assuming your list view
 * navigation looks like
 * 
 * <pre>
 *  
 * 	 [first / &lt;&lt; / &lt;] 1 | 2 | 3 [&gt; / &gt;&gt; /last]
 * 	
 * </pre>
 * 
 * <p>
 * and "&lt;" meaning the previous and "&lt;&lt;" goto the "current page - 5",
 * than it is this kind of incremental page links which can easily be created.
 * 
 * @author Juergen Donnerstag
 */
public class PageableListViewNavigationIncrementLink extends PageLink
{
	/** The increment. */
	private final int increment;

	/** The PageableListView the page links are referring to. */
	private final IPageableComponent pageable;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component the page links are referring to
	 * @param increment
	 *            increment by
	 */
	public PageableListViewNavigationIncrementLink(final String id,
			final IPageableComponent pageable, final int increment)
	{
		super(id, new IPageLink()
		{
			/**
			 * @see wicket.markup.html.link.IPageLink#getPage()
			 */
			public Page getPage()
			{
				// Determine the page number based on the current
				// PageableListView page
				// and the increment
				int idx = pageable.getCurrentPage() + increment;
				if (idx < 0)
				{
					idx = 0;
				}
				else if (idx > (pageable.getItemCount() - 1))
				{
					idx = pageable.getItemCount() - 1;
				}

				// Tell the PageableListView which page to print next
				pageable.setCurrentPage(idx);

				// Return the PageableListView page the link is referring to
				return pageable.getPage();
			}

			/**
			 * @see wicket.markup.html.link.IPageLink#getPageIdentity()
			 */
			public Class getPageIdentity()
			{
				return pageable.getPage().getClass();
			}
		});

		this.increment = increment;
		this.pageable = pageable;
	}

	// TODO We need to explain this onClick method!
	/**
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	public void onClick()
	{
		// We do not need to redirect
		setRedirect(false);

		super.onClick();
	}

	/**
	 * @return True if it is referring to the first page of the underlying
	 *         PageableListView.
	 */
	public boolean isFirst()
	{
		return pageable.getCurrentPage() <= 0;
	}

	/**
	 * @return True if it is referring to the last page of the underlying
	 *         PageableListView.
	 */
	public boolean isLast()
	{
		return pageable.getCurrentPage() >= (pageable.getPageCount() - 1);
	}

	/**
	 * Returns true if the page link links to the given page.
	 * 
	 * @param page
	 *            The page to test
	 * @return True if this link links to the given page
	 * @see wicket.markup.html.link.PageLink#linksTo(wicket.Page)
	 */
	public boolean linksTo(final Page page)
	{
		int currentPage = pageable.getCurrentPage();
		if (((increment < 0) && (currentPage <= 0))
				|| ((increment > 0) && (currentPage >= (pageable.getPageCount() - 1))))
		{
			return true;
		}

		return false;
	}
}
