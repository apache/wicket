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

import org.apache.wicket.util.tester.WicketTestCase;


/**
 * Base class for testing the navigation links, supplies a mock object for exercizing the links.
 * 
 * @author Martijn Dashorst
 */
public abstract class AbstractPagingNavigationTest extends WicketTestCase
{
	/**
	 * Mock object for testing the increment link.
	 */
	public class MockPageable implements IPageable
	{
		private static final long serialVersionUID = 1L;

		/** expected page which is set by the link. */
		private long expectedPage = 0;

		/**
		 * @see IPageable#getCurrentPage()
		 */
		@Override
		public long getCurrentPage()
		{
			return currentpage;
		}

		/**
		 * @see IPageable#setCurrentPage(int)
		 */
		@Override
		public void setCurrentPage(long page)
		{
			assertEquals("setCurrentPage", expectedPage, page);
		}

		/**
		 * @see org.apache.wicket.markup.html.navigation.paging.IPageable#getPageCount()
		 */
		@Override
		public long getPageCount()
		{
			return pagecount;
		}

		/**
		 * Sets the expected page number.
		 * 
		 * @param expectedPage
		 */
		public void setExpectedPage(int expectedPage)
		{
			this.expectedPage = expectedPage;
		}
	}

	/** mock page count. */
	protected long pagecount = 0;

	/** mock current page. */
	protected long currentpage = 0;
}
