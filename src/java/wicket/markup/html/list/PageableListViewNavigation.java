/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.list;

import wicket.markup.html.basic.Label;
import wicket.model.Model;


/**
 * A navigation for a PageableListView that holds links to other pages of the PageableListView.
 * <p>
 * For each row (one page of the list of pages) a {@link PageableListViewNavigationLink}
 * will be added that contains a {@link Label}with the page number of that link (1..n).
 * 
 * <pre>
 * 
 *  
 *  
 *  
 *       &lt;td id=&quot;wicket-navigation&quot;&gt;
 *           &lt;a id=&quot;wicket-pageLink&quot; href=&quot;SearchCDPage.html&quot;&gt;
 *              &lt;span id=&quot;wicket-pageNumber&quot;/&gt;
 *           &lt;/a&gt;
 *       &lt;/td&gt;
 *  
 *  
 *   
 *  
 * </pre>
 * 
 * thus renders like:
 * 
 * <pre>
 * 
 *  
 *  
 *  
 *       1 |  2 |  3 |  4 |  5 |  6 |  7 |  8 |  9 |
 *  
 *  
 *   
 *  
 * </pre>
 * 
 * </p>
 * <p>
 * Override method populateItem to customize the rendering of the navigation. For
 * instance:
 * 
 * <pre>
 * 
 * protected void populateItem(ListItem listItem)
 * {
 * 	final int page = ((Integer)listItem.getModelObject()).intValue();
 * 	final PageableListViewNavigationLink link =
 * 		new PageableListViewNavigationLink(
 * 			&quot;pageLink&quot;, pageableListView, page);
 * 	if (page &gt; 0)
 * 	{
 * 		listItem.add(new Label(&quot;separator&quot;, &quot;|&quot;));
 * 	}
 * 	else
 * 	{
 * 		listItem.add(new Label(&quot;separator&quot;, &quot;&quot;));
 * 	}
 * 	link.add(new Label(&quot;pageNumber&quot;, String.valueOf(page + 1)));
 * 	link.add(new Label(&quot;pageLabel&quot;, &quot;page&quot;));
 * 	listItem.add(link);
 * }
 * </pre>
 * 
 * With:
 * 
 * <pre>
 * 
 *  
 *  
 *  
 *       &lt;td id=&quot;wicket-navigation&quot;&gt;
 *           &lt;span id=&quot;wicket-separator&quot;/&gt;
 *           &lt;a id=&quot;wicket-pageLink&quot; href=&quot;#&quot;&gt;
 *             &lt;span id=&quot;wicket-pageLabel&quot;/&gt;&lt;span id=&quot;wicket-pageNumber&quot;/&gt;
 *           &lt;/a&gt;
 *       &lt;/td&gt;
 *  
 *  
 *   
 *  
 * </pre>
 * 
 * renders like:
 * 
 * <pre>
 * page1 | page2 | page3 | page4 | page5 | page6 | page7 | page8 | page9
 * </pre>
 * 
 * </p>
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 */
public class PageableListViewNavigation extends ListView
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 8591577491410447609L;

	/** The PageableListView this navigation is navigating. */
	protected PageableListView pageableListView;

	/**
	 * Constructor.
	 * @param componentName The name of the component
	 * @param pageableListView The underlying list view to navigate
	 */
	public PageableListViewNavigation(final String componentName,
			final PageableListView pageableListView)
	{
		super(componentName, new Model(null));

		this.pageableListView = pageableListView;
		this.setStartIndex(0);
	}

	/**
	 * Populate the current cell with a page link (PageableListViewNavigationLink) enclosing the page
	 * number the link is pointing to. Subclasses may provide there own implementation
	 * adding more sophisticated page links.
	 * @param listItem the list item to populate
	 * @see wicket.markup.html.list.PageableListView#populateItem(wicket.markup.html.list.ListItem)
	 */
	protected void populateItem(final ListItem listItem)
	{
		// Get the index of page this link shall point to
		final int pageIndex = ((Integer)listItem.getModelObject()).intValue();

		// Add a page link pointing to the page
		final PageableListViewNavigationLink link = new PageableListViewNavigationLink("pageLink",
				pageableListView, pageIndex);
		listItem.add(link);

		// Add a label (the page number) to the list which is enclosed by the link
		link.add(new Label("pageNumber", String.valueOf(pageIndex + 1)));
	}

	/**
	 * Provide the ListItem for the index given.
	 * <p>
	 * PageableListViewNavigation actually does not have an underlying model like most other
	 * ListViews. It doesn't have to, because the model is simply the index of the
	 * PageableListView's page. Thus we create a model based on the PageableListView's page index.
	 * @param index ListItem index
	 * @return The new ListItem
	 */
	protected ListItem newItem(final int index)
	{
		return new ListItem(index, new Model(new Integer(index)));
	}

	/**
	 * Get the PageableListView which the navigation bar is navigating.
	 * @return the PageableListView that is used to get the number of pages
	 */
	public PageableListView getPageableListView()
	{
		return pageableListView;
	}

	/**
	 * Set the PageableListView which the navigation bar is navigating.
	 * @param pageableListView the PageableListView that is used to get the number of pages
	 */
	public void setPageableListView(PageableListView pageableListView)
	{
		this.pageableListView = pageableListView;
	}

	/**
	 * Get the number of page links per "window".
	 * @see wicket.markup.html.list.ListView#setViewSize(int)
	 * @return The overall number of page links (= number of PageableListView pages),
	 * or 0 (zero) if no underlying PageableListView is available.
	 */
	public int getViewSize()
	{
		if (pageableListView != null)
		{
			return Math.min(pageableListView.getPageCount(), this.viewSize);
		}
		else
		{
			return 0;
		}
	}
}
