/*
 * $Id: PagingNavigationLink.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May
 * 2006) eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25
 * May 2006) $
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
package wicket.markup.html.navigation.paging;

import wicket.MarkupContainer;
import wicket.Page;
import wicket.markup.html.link.Link;

/**
 * A link to a page of a PageableListView.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Martijn Dashorst
 */
public class PagingNavigationLink extends Link
{
	private static final long serialVersionUID = 1L;

	/** The pageable list view. */
	protected final IPageable pageable;

	/** The page of the PageableListView this link is for. */
	private final int pageNumber;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component for this page link
	 * @param pageNumber
	 *            The page number in the PageableListView that this link links
	 *            to. Negative pageNumbers are relative to the end of the list.
	 */
	public PagingNavigationLink(MarkupContainer parent, final String id, final IPageable pageable,
			final int pageNumber)
	{
		super(parent, id);
		setAutoEnable(true);
		this.pageNumber = pageNumber;
		this.pageable = pageable;
	}

	/**
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public void onClick()
	{
		pageable.setCurrentPage(getPageNumber());

		// We do not need to redirect
		setRedirect(false);
		setResponsePage(getPage());
	}

	/**
	 * Get pageNumber.
	 * 
	 * @return pageNumber.
	 */
	public final int getPageNumber()
	{
		int idx = pageNumber;
		if (idx < 0)
		{
			idx = pageable.getPageCount() + idx;
		}

		if (idx > (pageable.getPageCount() - 1))
		{
			idx = pageable.getPageCount() - 1;
		}

		if (idx < 0)
		{
			idx = 0;
		}

		return idx;
	}

	/**
	 * @return True if this page is the first page of the containing
	 *         PageableListView
	 */
	public final boolean isFirst()
	{
		return getPageNumber() == 0;
	}

	/**
	 * @return True if this page is the last page of the containing
	 *         PageableListView
	 */
	public final boolean isLast()
	{
		return getPageNumber() == (pageable.getPageCount() - 1);
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
	@Override
	public final boolean linksTo(final Page page)
	{
		return getPageNumber() == pageable.getCurrentPage();
	}
}