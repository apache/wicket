/*
 * $Id: PageableListViewNavigationWithMargin.java,v 1.2 2005/02/12 22:02:48
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

/**
 * Assuming a PageableListView has 1000 entries and not more than 10 lines shall
 * be printed per page. A standard navigation bar would have 100 entries.
 * Because this is not feasible. PageableListViewNavigationWithMargin provides a
 * pageable navigation bar displaying only some page links.
 * <p>
 * The page links displayed are automatically adjusted based on the number of
 * page links to be displayed and a margin. The margin makes sure that the page
 * link pointing to the current page is not at the left or right end of the page
 * links currently printed and thus provinding a better user experience.
 * 
 * @author Juergen Donnerstag
 */
public class PageableListViewNavigationWithMargin extends PageableListViewNavigation
{
	/**
	 * Number of navigation cells on the left and/or right to keep the current
	 * cell somewhere near the middle.
	 */
	private int margin;

	/** Default separator between page numbers. Null: no separator. */
	private String separator = null;

	/**
	 * Construct.
	 * 
	 * @param name
	 *            The name of the component
	 * @param pageableListView
	 *            The PageableListView to navigate
	 */
	public PageableListViewNavigationWithMargin(final String name,
			final PageableListView pageableListView)
	{
		super(name, pageableListView);
	}

	/**
	 * Gets the margin.
	 * 
	 * @return the margin
	 */
	public int getMargin()
	{
		return margin;
	}

	/**
	 * Gets the seperator.
	 * 
	 * @return the seperator
	 */
	public String getSeparator()
	{
		return separator;
	}

	/**
	 * Sets the margin.
	 * 
	 * @param margin
	 *            the margin
	 */
	public void setMargin(int margin)
	{
		this.margin = margin;
	}

	/**
	 * Sets the seperator. Null meaning, no separator at all.
	 * 
	 * @param separator
	 *            the seperator
	 */
	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	/**
	 * Renders this component.
	 * 
	 * @see wicket.markup.html.list.ListView#onRender()
	 */
	protected void onRender()
	{
		// PageableListViewNavigation itself (as well as the PageableListView)
		// may have pages.

		// The index of the first page link depends on the PageableListView's
		// page
		// current printed.
		this.setStartIndex();

		// continue with default
		super.onRender();
	}

	/**
	 * Renders the page link. Add the separator if not the last page link
	 * 
	 * @param listItem
	 *            The current page link to render
	 * @param lastItem
	 *            True, if last page link to render
	 */
	protected void renderItem(final ListItem listItem, final boolean lastItem)
	{
		// call default implementation
		super.renderItem(listItem, lastItem);

		// add separator if not last page
		if ((separator != null) && !lastItem)
		{
			getResponse().write(separator);
		}
	}

	/**
	 * Get the first page link to render. Adjust the first page link based on
	 * the current PageableListView page displayed.
	 */
	protected void setStartIndex()
	{
		// Which startIndex are we currently using
		int firstListItem = this.getStartIndex();

		// How many page links shall be displayed
		int viewSize = this.getViewSize();

		// What is the PageableListView's page index to be displayed
		int currentPage = pageableListView.getCurrentPage();

		// Make sure the current page link index is within the current
		// window taking the left and right margin into account
		if (currentPage < (firstListItem + this.margin))
		{
			firstListItem = currentPage - viewSize + margin;
		}
		else if (currentPage >= (firstListItem + viewSize - this.margin))
		{
			firstListItem = currentPage - margin;
		}

		// Make sure the first index is >= 0 and the last index is <=
		// than the last page link index.
		if ((firstListItem + viewSize) >= pageableListView.getPageCount())
		{
			firstListItem = pageableListView.getPageCount() - viewSize;
		}

		if (firstListItem < 0)
		{
			firstListItem = 0;
		}

		// Tell the ListView what the new start index shall be
		this.setStartIndex(firstListItem);
	}
}
