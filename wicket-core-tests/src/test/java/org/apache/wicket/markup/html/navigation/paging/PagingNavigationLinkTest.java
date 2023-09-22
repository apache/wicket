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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the PagingNavigationLink.
 * 
 * @author Martijn Dashorst
 */
class PagingNavigationLinkTest extends AbstractPagingNavigationTest
{
	/** the mock pageable driver. */
	private MockPageable mock;

	/**
	 * sets up the test.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	void before() throws Exception
	{
		mock = new MockPageable();
	}

	/**
	 * Tests the get page number and is first and last methods.
	 */
	@Test
	void getPageNumber_1()
	{
		PagingNavigationLink<Void> link = new PagingNavigationLink<Void>("id", mock, 0);

		currentpage = 0;
		pagecount = 0;

		assertTrue(link.isFirst(), "is first");
		assertFalse(link.isLast(), "is last");
		assertEquals(0, link.getPageNumber());

		pagecount = 1;
		assertTrue(link.isFirst(), "is first");
		assertTrue(link.isLast(), "is last");
		assertEquals(0, link.getPageNumber());

		pagecount = 2;
		assertTrue(link.isFirst(), "is first");
		assertFalse(link.isLast(), "is last");
		assertEquals(0, link.getPageNumber());
	}

	/**
	 * Tests the get page number and is first and last methods.
	 */
	@Test
	void getPageNumber_2()
	{
		PagingNavigationLink<Void> link = new PagingNavigationLink<Void>("id", mock, 2);

		currentpage = 0;
		pagecount = 0;

		assertTrue(link.isFirst(), "is first");
		assertFalse(link.isLast(), "is last");
		assertEquals(0, link.getPageNumber());

		pagecount = 2;
		assertFalse(link.isFirst(), "is first");
		assertTrue(link.isLast(), "is last");
		assertEquals(1, link.getPageNumber());

		pagecount = 3;
		assertFalse(link.isFirst(), "is first");
		assertTrue(link.isLast(), "is last");
		assertEquals(2, link.getPageNumber());
	}

	/**
	 * Tests the get page number and is first and last methods.
	 */
	@Test
	void getPageNumber_3()
	{
		PagingNavigationLink<Void> link = new PagingNavigationLink<Void>("id", mock, -1);

		currentpage = 0;
		pagecount = 0;

		assertTrue(link.isFirst(), "is first");
		assertFalse(link.isLast(), "is last");
		assertEquals(0, link.getPageNumber());

		pagecount = 2;
		assertFalse(link.isFirst(), "is first");
		assertTrue(link.isLast(), "is last");
		assertEquals(1, link.getPageNumber());

		pagecount = 3;
		assertFalse(link.isFirst(), "is first");
		assertTrue(link.isLast(), "is last");
		assertEquals(2, link.getPageNumber());
	}

	/**
	 * Tests the linksTo method.
	 */
	@Test
	void linksTo()
	{
		PagingNavigationLink<Void> link = new PagingNavigationLink<Void>("id", mock, -1);

		currentpage = 0;
		pagecount = 0;

		assertTrue(link.linksTo(null), "links to");

		currentpage = 0;
		pagecount = 1;

		assertTrue(link.linksTo(null), "links to");

		currentpage = 0;
		pagecount = 3;

		assertFalse(link.linksTo(null), "links to");

		currentpage = 2;
		pagecount = 3;

		assertTrue(link.linksTo(null), "links to");
	}
}
