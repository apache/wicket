/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.navigation.paging;

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.collections.MicroMap;

/**
 * A navigation for a PageableListView that holds links to other pages of the PageableListView.
 * <p>
 * For each row (one page of the list of pages) a {@link PagingNavigationLink}will be added that
 * contains a {@link Label} with the page number of that link (1..n).
 * 
 * <pre>
 * 
 * 	&lt;td wicket:id=&quot;navigation&quot;&gt;
 * 		&lt;a wicket:id=&quot;pageLink&quot; href=&quot;SearchCDPage.html&quot;&gt;
 * 			&lt;span wicket:id=&quot;pageNumber&quot;&gt;1&lt;/span&gt;
 * 		&lt;/a&gt;
 * 	&lt;/td&gt;
 * 
 * </pre>
 * 
 * thus renders like:
 * 
 * <pre>
 * 	1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 |
 * </pre>
 * 
 * </p>
 * <p>
 * Override method populateItem to customize the rendering of the navigation. For instance:
 * 
 * <pre>
 * protected void populateItem(LoopItem loopItem)
 * {
 * 	final int page = loopItem.getIteration();
 * 	final PagingNavigationLink link = new PagingNavigationLink(&quot;pageLink&quot;, pageableListView, page);
 * 	if (page &gt; 0)
 * 	{
 * 		loopItem.add(new Label(&quot;separator&quot;, &quot;|&quot;));
 * 	}
 * 	else
 * 	{
 * 		loopItem.add(new Label(&quot;separator&quot;, &quot;&quot;));
 * 	}
 * 	link.add(new Label(&quot;pageNumber&quot;, String.valueOf(page + 1)));
 * 	link.add(new Label(&quot;pageLabel&quot;, &quot;page&quot;));
 * 	loopItem.add(link);
 * }
 * </pre>
 * 
 * With:
 * 
 * <pre>
 * &lt;span wicket:id=&quot;navigation&quot;&gt;
 * 	&lt;span wicket:id=&quot;separator&quot;&gt;&lt;/span&gt;
 * 	&lt;a wicket:id=&quot;pageLink&quot; href=&quot;#&quot;&gt;
 * 		&lt;span wicket:id=&quot;pageLabel&quot;&gt;&lt;/span&gt;&lt;span wicket:id=&quot;pageNumber&quot;&gt;&lt;/span&gt;
 * 	&lt;/a&gt;
 * &lt;/span&gt;
 * </pre>
 * 
 * renders like:
 * 
 * <pre>
 * page1 | page2 | page3 | page4 | page5 | page6 | page7 | page8 | page9
 * </pre>
 * 
 * </p>
 * Assuming a PageableListView with 1000 entries and not more than 10 lines shall be printed per
 * page, the navigation bar would have 100 entries. Because this is not feasible PagingNavigation's
 * navigation bar is pageable as well.
 * <p>
 * The page links displayed are automatically adjusted based on the number of page links to be
 * displayed and a margin. The margin makes sure that the page link pointing to the current page is
 * not at the left or right end of the page links currently printed and thus providing a better user
 * experience.
 * <p>
 * Use setMargin() and setViewSize() to adjust the navigation's bar view size and margin.
 * <p>
 * Please
 * 
 * @see PagingNavigator for a ready made component which already includes links to the first,
 *      previous, next and last page.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 */
public class PagingNavigation extends Loop
{
	private static final long serialVersionUID = 1L;

	/** The PageableListView this navigation is navigating. */
	protected IPageable pageable;

	/** The label provider for the text that the links should be displaying. */
	protected IPagingLabelProvider labelProvider;

	/** Offset for the Loop */
	private long startIndex;

	/**
	 * Number of links on the left and/or right to keep the current page link somewhere near the
	 * middle.
	 */
	private long margin = -1;

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
	 *            See Component
	 * @param pageable
	 *            The underlying pageable component to navigate
	 */
	public PagingNavigation(final String id, final IPageable pageable)
	{
		this(id, pageable, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The underlying pageable component to navigate
	 * @param labelProvider
	 *            The label provider for the text that the links should be displaying.
	 */
	public PagingNavigation(final String id, final IPageable pageable,
		final IPagingLabelProvider labelProvider)
	{
		super(id, null);
		this.pageable = pageable;
		this.labelProvider = labelProvider;
		startIndex = 0;
	}

	/**
	 * Gets the margin, default value is half the view size, unless explicitly set.
	 * 
	 * @return the margin
	 */
	public long getMargin()
	{
		if (margin == -1 && viewSize != 0)
		{
			return viewSize / 2;
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
	 * 
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
		viewSize = size;
	}

	/**
	 * Sets the margin.
	 * 
	 * @param margin
	 *            the margin
	 */
	public void setMargin(final int margin)
	{
		this.margin = margin;
	}

	/**
	 * Sets the seperator. Null meaning, no separator at all.
	 * 
	 * @param separator
	 *            the seperator
	 */
	public void setSeparator(final String separator)
	{
		this.separator = separator;
	}

	@Override
	protected void onConfigure()
	{
		super.onConfigure();
		setDefaultModel(new Model<Integer>(
			(int)Math.max(Integer.MAX_VALUE, pageable.getPageCount())));
		// PagingNavigation itself (as well as the PageableListView)
		// may have pages.

		// The index of the first page link depends on the PageableListView's
		// page currently printed.
		setStartIndex();
	}

	/**
	 * Allow subclasses replacing populateItem to calculate the current page number
	 * 
	 * @return start index
	 */
	protected final long getStartIndex()
	{
		return startIndex;
	}

	/**
	 * Populate the current cell with a page link (PagingNavigationLink) enclosing the page number
	 * the link is pointing to. Subclasses may provide there own implementation adding more
	 * sophisticated page links.
	 * 
	 * @see org.apache.wicket.markup.html.list.Loop#populateItem(org.apache.wicket.markup.html.list.LoopItem)
	 */
	@Override
	protected void populateItem(final LoopItem loopItem)
	{
		// Get the index of page this link shall point to
		final long pageIndex = getStartIndex() + loopItem.getIndex();

		// Add a page link pointing to the page
		final AbstractLink link = newPagingNavigationLink("pageLink", pageable, pageIndex);
		link.add(new TitleAppender(pageIndex));
		loopItem.add(link);

		// Add a page number label to the list which is enclosed by the link
		String label = "";
		if (labelProvider != null)
		{
			label = labelProvider.getPageLabel(pageIndex);
		}
		else
		{
			label = String.valueOf(pageIndex + 1);
		}
		link.add(new Label("pageNumber", label));
	}

	/**
	 * Factory method for creating page number links.
	 * 
	 * @param id
	 *            the component id.
	 * @param pageable
	 *            the pageable for the link
	 * @param pageIndex
	 *            the page index the link points to
	 * @return the page navigation link.
	 */
	protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, long pageIndex)
	{
		return new PagingNavigationLink<Void>(id, pageable, pageIndex);
	}

	/**
	 * Renders the page link. Add the separator if not the last page link
	 * 
	 * @see Loop#renderItem(org.apache.wicket.markup.html.list.LoopItem) 
	 */
	@Override
	protected void renderItem(final LoopItem loopItem)
	{
		// Call default implementation
		super.renderItem(loopItem);

		// Add separator if not last page
		if (separator != null && (loopItem.getIndex() != getIterations() - 1))
		{
			getResponse().write(separator);
		}
	}

	/**
	 * Get the first page link to render. Adjust the first page link based on the current
	 * PageableListView page displayed.
	 */
	private void setStartIndex()
	{
		// Which startIndex are we currently using
		long firstListItem = startIndex;

		// How many page links shall be displayed
		int viewSize = (int)Math.min(getViewSize(), pageable.getPageCount());
		long margin = getMargin();

		// What is the PageableListView's page index to be displayed
		long currentPage = pageable.getCurrentPage();

		// Make sure the current page link index is within the current
		// window taking the left and right margin into account
		if (currentPage < (firstListItem + margin))
		{
			firstListItem = currentPage - margin;
		}
		else if ((currentPage >= (firstListItem + viewSize - margin)))
		{

			firstListItem = (currentPage + margin + 1) - viewSize;
		}

		// Make sure the first index is >= 0 and the last index is <=
		// than the last page link index.
		if ((firstListItem + viewSize) >= pageable.getPageCount())
		{
			firstListItem = pageable.getPageCount() - viewSize;
		}

		if (firstListItem < 0)
		{
			firstListItem = 0;
		}

		if ((viewSize != getIterations()) || (startIndex != firstListItem))
		{
			modelChanging();

			// Tell the ListView what the new start index shall be
			addStateChange();
			startIndex = firstListItem;

			setIterations((int)Math.min(viewSize, pageable.getPageCount()));

			modelChanged();

			// force all children to be re-rendered
			removeAll();
		}
	}

	/**
	 * Set the number of iterations.
	 * 
	 * @param i
	 *            the number of iterations
	 */
	private void setIterations(int i)
	{
		setDefaultModelObject(i);
	}

	/**
	 * Appends title attribute to navigation links
	 * 
	 * @author igor.vaynberg
	 */
	private final class TitleAppender extends Behavior
	{
		private static final long serialVersionUID = 1L;
		/** resource key for the message */
		private static final String RES = "PagingNavigation.page";
		/** page number */
		private final long page;

		/**
		 * Constructor
		 * 
		 * @param page
		 *            page number to use as the ${page} var
		 */
		public TitleAppender(long page)
		{
			this.page = page;
		}

		/** {@inheritDoc} */
		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			Map<String, String> vars = new MicroMap<String, String>("page",
				String.valueOf(page + 1));
			tag.put("title", PagingNavigation.this.getString(RES, Model.ofMap(vars)));
		}
	}
}
