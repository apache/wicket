/*
 * $Id: PageableListViewNavigationLink.java,v 1.2 2005/02/12 22:02:48
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
 * A link to a page of a PageableListView.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public final class PageableListViewNavigationLink extends PageLink
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 9064718260408332988L;

	/** The pageable list view. */
	private final PageableListView pageableListView;

	/** The page of the PageableListView this link is for. */
	private final int pageNumber;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageableListView
	 *            The list view for this page link
	 * @param pageNumber
	 *            The page number in the PageableListView that this link links
	 *            to
	 */
	public PageableListViewNavigationLink(final String id,
			final PageableListView pageableListView, final int pageNumber)
	{
		super(id, new IPageLink()
		{
			/** Serial Version ID. */
			private static final long serialVersionUID = 7836120196635892372L;

			public Page getPage()
			{
			    int idx = pageNumber;
				if (idx < 0)
				{
					idx = 0;
				}
				else if (idx > (pageableListView.getList().size() - 1))
				{
					idx = pageableListView.getList().size() - 1;
				}
			    
				pageableListView.setCurrentPage(idx);

				return pageableListView.getPage();
			}

			public Class getPageIdentity()
			{
				return pageableListView.getPage().getClass();
			}
		});

		this.pageNumber = (pageNumber < 0 ? 0 : pageNumber);
		this.pageableListView = pageableListView;
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
	 * Get pageNumber.
	 * 
	 * @return pageNumber.
	 */
	public final int getPageNumber()
	{
		return pageNumber;
	}

	/**
	 * @return True if this page is the first page of the containing
	 *         PageableListView
	 */
	public boolean isFirst()
	{
		return pageNumber == 0;
	}

	/**
	 * @return True if this page is the last page of the containing
	 *         PageableListView
	 */
	public boolean isLast()
	{
		return pageNumber == (pageableListView.size() - 1);
	}

	/**
	 * Returns true if this PageableListView navigation link links to the given
	 * page.
	 * 
	 * @param page
	 *            The page
	 * @return True if this link links to the given page
	 * @see wicket.markup.html.link.PageLink#linksTo(wicket.Page)
	 */
	public boolean linksTo(final Page page)
	{
		return pageNumber == pageableListView.getCurrentPage();
	}
}
