/*
 * $Id: PageableListViewNavigation.java,v 1.3 2005/02/17 06:13:40 jonathanlocke
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

import wicket.markup.html.basic.Label;
import wicket.version.undo.Change;

/**
 * A navigation for a PageableListView that holds links to other pages of the
 * PageableListView.
 * <p>
 * For each row (one page of the list of pages) a
 * {@link PageableListViewNavigationLink}will be added that contains a
 * {@link Label}with the page number of that link (1..n).
 * 
 * <pre>
 * 
 *				 &lt;td wicket:id=&quot;navigation&quot;&gt;
 *					 &lt;a wicket:id=&quot;pageLink&quot; href=&quot;SearchCDPage.html&quot;&gt;
 *						&lt;span wicket:id=&quot;pageNumber&quot;&gt;1&lt;/&gt;
 *					 &lt;/a&gt;
 *				 &lt;/td&gt;
 *	
 * </pre>
 * 
 * thus renders like:
 * 
 * <pre>
 * 
 *				 1 |  2 |  3 |	4 |	 5 |  6 |  7 |	8 |	 9 |
 *	
 * </pre>
 * 
 * </p>
 * <p>
 * Override method populateItem to customize the rendering of the navigation.
 * For instance:
 * 
 * <pre>
 * 
 * protected void populateItem(ListItem listItem)
 * {
 *	final int page = ((Integer)listItem.getModelObject()).intValue();
 *	final PageableListViewNavigationLink link = new PageableListViewNavigationLink(&quot;pageLink&quot;,
 *			pageableListView, page);
 *	if (page &gt; 0)
 *	{
 *		listItem.add(new Label(&quot;separator&quot;, &quot;|&quot;));
 *	}
 *	else
 *	{
 *		listItem.add(new Label(&quot;separator&quot;, &quot;&quot;));
 *	}
 *	link.add(new Label(&quot;pageNumber&quot;, String.valueOf(page + 1)));
 *	link.add(new Label(&quot;pageLabel&quot;, &quot;page&quot;));
 *	listItem.add(link);
 * }
 * </pre>
 * 
 * With:
 * 
 * <pre>
 *	&lt;td wicket:id=&quot;navigation&quot;&gt;
 *	  &lt;span wicket:id=&quot;separator&quot;/&gt;
 *	  &lt;a wicket:id=&quot;pageLink&quot; href=&quot;#&quot;&gt;
 *	    &lt;span wicket:id=&quot;pageLabel&quot;/&gt;&lt;span wicket:id=&quot;pageNumber&quot;/&gt;
 *	  &lt;/a&gt;
 *	&lt;/td&gt;
 * </pre>
 * 
 * renders like:
 * 
 * <pre>
 * page1 | page2 | page3 | page4 | page5 | page6 | page7 | page8 | page9
 * </pre>
 * 
 * </p>
 * Assuming a PageableListView with 1000 entries and not more than 10 lines
 * shall be printed per page, the navigation bar would have 100 entries. Because
 * this is not feasible PageableListViewNavigation's navigation bar is pageable
 * as well.
 * <p>
 * The page links displayed are automatically adjusted based on the number of
 * page links to be displayed and a margin. The margin makes sure that the page
 * link pointing to the current page is not at the left or right end of the page
 * links currently printed and thus providing a better user experience.
 * <p>
 * Use setMargin() and setViewSize() to adjust the navigation's bar view size
 * and margin.
 * <p>
 * Please @see PageableListViewNavigator for a ready made component which already
 *		includes links to the first, previous, next and last page.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 */
public class PageableListViewNavigation extends Loop
{
	/**
	 * Undo change for navigation start index. Makes certain that back button works
	 * with paging in the navigator.
	 */
	private final class StartIndexChange extends Change
	{
		private int startIndex;
		
		/**
		 * Constructor, remembers the startIndex.
		 * @param startIndex the startIndex to remember.
		 */
		private StartIndexChange(int startIndex)
		{
			this.startIndex = startIndex;
		}
		
		/**
		 * @see wicket.version.undo.Change#undo()
		 */
		public final void undo()
		{
			PageableListViewNavigation.this.startIndex = startIndex;
		}
	}

	/** The PageableListView this navigation is navigating. */
	protected PageableListView pageableListView;

	/** Offset for the Loop */
	private int startIndex;

	/**
	 * Number of links on the left and/or right to keep the current page link
	 * somewhere near the middle.
	 */
	private int margin = -1;

	/** Default separator between page numbers. Null: no separator. */
	private String separator = null;

	/**
	 * The maximum number of page links to show.
	 */
	private int viewSize = 10;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *			  See Component
	 * @param pageableListView
	 *			  The underlying list view to navigate
	 */
	public PageableListViewNavigation(final String id, final PageableListView pageableListView)
	{
		super(id, pageableListView.getPageCount());
		this.pageableListView = pageableListView;
		startIndex = 0;
	}

	/**
	 * Gets the margin, default value is half the view size, unless explicitly set.
	 * 
	 * @return the margin
	 */
	public int getMargin()
	{
		if(margin == -1 && viewSize !=0)
		{			
			return viewSize/2;
		}
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
	 * Gets the view size (is fixed by user).
	 * @return view size
	 */
	public int getViewSize()
	{
		return viewSize;
	}

	/**
	 * view size of the navigation bar.
	 * 
	 * @param size
	 */
	public void setViewSize(final int size)
	{
		this.viewSize = size;
	}

	/**
	 * Sets the margin.
	 * 
	 * @param margin
	 *			  the margin
	 */
	public void setMargin(final int margin)
	{
		this.margin = margin;
	}

	/**
	 * Sets the seperator. Null meaning, no separator at all.
	 * 
	 * @param separator
	 *			  the seperator
	 */
	public void setSeparator(final String separator)
	{
		this.separator = separator;
	}

	/**
	 * @see wicket.Component#internalOnBeginRequest()
	 */
	protected void internalOnBeginRequest()
	{
		// PageableListViewNavigation itself (as well as the PageableListView)
		// may have pages.

		// The index of the first page link depends on the PageableListView's
		// page currently printed.
		this.setStartIndex();

		super.internalOnBeginRequest();
	}

	/**
	 * Allow subclasses replacing populateItem to calculate the current 
	 * page number
	 * 
	 * @return start index
	 */
	protected final int getStartIndex()
	{
		return this.startIndex;
	}
	
	/**
	 * Populate the current cell with a page link
	 * (PageableListViewNavigationLink) enclosing the page number the link is
	 * pointing to. Subclasses may provide there own implementation adding more
	 * sophisticated page links.
	 * 
	 * @see wicket.markup.html.list.Loop#populateItem(Loop.LoopItem)
	 */
	protected void populateItem(final Loop.LoopItem loopItem)
	{
		// Get the index of page this link shall point to
		final int pageIndex = getStartIndex() + loopItem.getIteration();

		// Add a page link pointing to the page
		final PageableListViewNavigationLink link = new PageableListViewNavigationLink("pageLink",
				pageableListView, pageIndex);
		loopItem.add(link);

		// Add a page number label to the list which is enclosed by the link
		link.add(new Label("pageNumber", String.valueOf(pageIndex + 1)));
	}

	/**
	 * Renders the page link. Add the separator if not the last page link
	 * 
	 * @see Loop#renderItem(Loop.LoopItem)
	 */
	protected void renderItem(final Loop.LoopItem loopItem)
	{
		// Call default implementation
		super.renderItem(loopItem);

		// Add separator if not last page
		if (separator != null && (loopItem.getIteration() != getIterations() - 1))
		{
			getResponse().write(separator);
		}
	}

	/**
	 * Get the first page link to render. Adjust the first page link based on
	 * the current PageableListView page displayed.
	 */
	private void setStartIndex()
	{
		// Which startIndex are we currently using
		int firstListItem = this.startIndex;

		// How many page links shall be displayed
		int viewSize = Math.min(getViewSize(), pageableListView.getPageCount());
		int margin = getMargin();

		// What is the PageableListView's page index to be displayed
		int currentPage = pageableListView.getCurrentPage();

		// Make sure the current page link index is within the current
		// window taking the left and right margin into account
		if (currentPage < (firstListItem + margin))
		{
			firstListItem = currentPage - margin;
		}
		else if((currentPage >= (firstListItem + viewSize - margin)))
		{

			firstListItem = (currentPage + margin + 1) - viewSize;
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

		if ((viewSize != getIterations()) || (this.startIndex != firstListItem))
		{
			this.modelChanging();

			// Tell the ListView what the new start index shall be
			addStateChange(new StartIndexChange(this.startIndex));
			this.startIndex = firstListItem;
			
			this.setIterations(Math.min(viewSize,pageableListView.getPageCount()));

			this.modelChanged();

			// force all children to be re-rendered
			removeAll();
		}
	}

	private void setIterations(int i)
	{
		setModelObject(new Integer(i));
	}
}
