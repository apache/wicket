/*
 * $Id: PagingNavigator.java,v 1.3 2005/02/12 22:02:48 jonathanlocke
 * Exp $ $Revision$ $Date$
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

import wicket.markup.html.panel.Panel;

/**
 * A Wicket panel component to draw and maintain a complete page navigator,
 * meant to be easily added to any PageableListView. A navigation which contains
 * links to the first and last page, the current page +- some increment and
 * which supports paged navigation bars (@see
 * PageableListViewNavigationWithMargin).
 * 
 * @author Juergen Donnerstag
 */
public class PagingNavigator extends Panel
{
	/** The navigation bar to be printed, e.g. 1 | 2 | 3 etc. */
	private final PagingNavigation pageableListViewNavigation;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component the page links are referring to.
	 */
	public PagingNavigator(final String id, final IPageable pageable)
	{
		this(id,pageable,null);
	}
	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component the page links are referring to.
	 * @param labelProvider 
	 * 			  The label provider for the link text.
	 */
	public PagingNavigator(final String id, final IPageable pageable, final IPagingLabelProvider labelProvider)
	{
		super(id);

		
		// Get the navigation bar and add it to the hierarchy
		this.pageableListViewNavigation = newNavigation(pageable, labelProvider);
		add(pageableListViewNavigation);

		// Add additional page links
		add(new PagingNavigationLink("first", pageable, 0));
		add(new PagingNavigationIncrementLink("prev", pageable, -1));
		add(new PagingNavigationIncrementLink("next", pageable, 1));
		add(new PagingNavigationLink("last", pageable, -1));
	}

	/**
	 * Create a new PagingNavigation. May be subclassed to make us of
	 * specialized PagingNavigation.
	 * 
	 * @param pageable
	 *            the pageable component
	 * @param labelProvider 
	 * 			  The label provider for the link text.
	 * @return the navigation object
	 */
	protected PagingNavigation newNavigation(final IPageable pageable, final IPagingLabelProvider labelProvider)
	{
		return new PagingNavigation("navigation", pageable,labelProvider);
	}

	/**
	 * Gets the pageable navigation component for configuration purposes.
	 * @return the associated pageable navigation.
	 */
	public final PagingNavigation getPageableListViewNavigation()
	{
		return pageableListViewNavigation;
	}
}