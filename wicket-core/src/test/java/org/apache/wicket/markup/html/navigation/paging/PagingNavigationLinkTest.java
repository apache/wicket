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

import org.junit.Before;
import org.junit.Test;


/**
 * Tests the PagingNavigationLink.
 * 
 * @author Martijn Dashorst
 */
public class PagingNavigationLinkTest extends AbstractPagingNavigationTest
{
	/** the mock pageable driver. */
	private MockPageable mock;

	/**
	 * sets up the test.
	 * 
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		mock = new MockPageable();
	}

	/**
	 * Tests the get page number and is first and last methods.
	 */
	@Test
	public void getPageNumber_1()
	{
		PagingNavigationLink<Void> link = new PagingNavigationLink<Void>("id", mock, 0);

		currentpage = 0;
		pagecount = 0;

		assertTrue("is first", link.isFirst());
		assertFalse("is last", link.isLast());
		assertEquals(0, link.getPageNumber());

		pagecount = 1;
		assertTrue("is first", link.isFirst());
		assertTrue("is last", link.isLast());
		assertEquals(0, link.getPageNumber());

		pagecount = 2;
		assertTrue("is first", link.isFirst());
		assertFalse("is last", link.isLast());
		assertEquals(0, link.getPageNumber());
	}

	/**
	 * Tests the get page number and is first and last methods.
	 */
	@Test
	public void getPageNumber_2()
	{
		PagingNavigationLink<Void> link = new PagingNavigationLink<Void>("id", mock, 2);

		currentpage = 0;
		pagecount = 0;

		assertTrue("is first", link.isFirst());
		assertFalse("is last", link.isLast());
		assertEquals(0, link.getPageNumber());

		pagecount = 2;
		assertFalse("is first", link.isFirst());
		assertTrue("is last", link.isLast());
		assertEquals(1, link.getPageNumber());

		pagecount = 3;
		assertFalse("is first", link.isFirst());
		assertTrue("is last", link.isLast());
		assertEquals(2, link.getPageNumber());
	}

	/**
	 * Tests the get page number and is first and last methods.
	 */
	@Test
	public void getPageNumber_3()
	{
		PagingNavigationLink<Void> link = new PagingNavigationLink<Void>("id", mock, -1);

		currentpage = 0;
		pagecount = 0;

		assertTrue("is first", link.isFirst());
		assertFalse("is last", link.isLast());
		assertEquals(0, link.getPageNumber());

		pagecount = 2;
		assertFalse("is first", link.isFirst());
		assertTrue("is last", link.isLast());
		assertEquals(1, link.getPageNumber());

		pagecount = 3;
		assertFalse("is first", link.isFirst());
		assertTrue("is last", link.isLast());
		assertEquals(2, link.getPageNumber());
	}

	/**
	 * Tests the linksTo method.
	 */
	@Test
	public void linksTo()
	{
		PagingNavigationLink<Void> link = new PagingNavigationLink<Void>("id", mock, -1);

		currentpage = 0;
		pagecount = 0;

		assertTrue("links to", link.linksTo(null));

		currentpage = 0;
		pagecount = 1;

		assertTrue("links to", link.linksTo(null));

		currentpage = 0;
		pagecount = 3;

		assertFalse("links to", link.linksTo(null));

		currentpage = 2;
		pagecount = 3;

		assertTrue("links to", link.linksTo(null));
	}
}
