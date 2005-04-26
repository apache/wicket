/*
 * $Id: PageableListViewNavigator.java,v 1.3 2005/02/12 22:02:48 jonathanlocke
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
package wicket.markup.html.list;

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
public class PageableListViewNavigator extends Panel
{
	/** The navigation bar to be printed, e.g. 1 | 2 | 3 etc. */
	private final PageableListViewNavigation pageableListViewNavigation;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageableListView
	 *            The PageableListView the page links are referring to.
	 */
	public PageableListViewNavigator(final String id, final PageableListView pageableListView)
	{
		super(id);

		// Get the navigation bar and add it to the hierarchy
		this.pageableListViewNavigation = newNavigation(pageableListView);
		add(pageableListViewNavigation);

		// Add additional page links
		add(new PageableListViewNavigationLink("first", pageableListView, 0));
		add(new PageableListViewNavigationIncrementLink("prev", pageableListView, -1));
		add(new PageableListViewNavigationIncrementLink("next", pageableListView, 1));
		add(new PageableListViewNavigationLink("last", pageableListView, pageableListView
				.getPageCount() - 1));
	}

	/**
	 * Create a new PageableListViewNavigation. May be subclassed to make us of
	 * specialized PageableListViewNavigation.
	 * 
	 * @param pageableListView
	 *            the pageable list view
	 * @return the navigation object
	 */
	protected PageableListViewNavigation newNavigation(final PageableListView pageableListView)
	{
		return new PageableListViewNavigation("navigation", pageableListView);
	}

	/**
	 * Gets the pageable navigation component for configuration purposes.
	 * @return the associated pageable navigation.
	 */
	public final PageableListViewNavigation getPageableListViewNavigation()
	{
		return pageableListViewNavigation;
	}
}