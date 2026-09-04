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
 * Testcase for the navigation increment link.
 * 
 * @author Martijn Dashorst
 */
class PagingNavigationIncrementLinkTest extends AbstractPagingNavigationTest
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
	 * Performs the forward navigation by incrementing one page.
	 */
	@Test
	void getPageNumberPositive()
	{
		PagingNavigationIncrementLink<Void> link = new PagingNavigationIncrementLink<Void>("xx",
			mock, 1);
		assertEquals(0, link.getPageNumber());
		pagecount = 1;
		assertEquals(0, link.getPageNumber());
		pagecount = 2;
		assertEquals(1, link.getPageNumber());
		currentpage = 1;
		assertEquals(1, link.getPageNumber());
	}

	/**
	 * Performs the backward navigation by decrementing one page.
	 */
	@Test
	void getPageNumberNegative()
	{
		PagingNavigationIncrementLink<Void> link = new PagingNavigationIncrementLink<Void>("xx",
			mock, -1);
		assertEquals(0, link.getPageNumber());
		pagecount = 1;
		assertEquals(0, link.getPageNumber());
		pagecount = 2;
		assertEquals(0, link.getPageNumber());
		currentpage = 1;
		assertEquals(0, link.getPageNumber());
		pagecount = 3;
		currentpage = 2;
		assertEquals(1, link.getPageNumber());
	}

	/**
	 * Performs the forward navigation by incrementing two pages.
	 */
	@Test
	void getPageNumberTwo()
	{
		PagingNavigationIncrementLink<Void> link = new PagingNavigationIncrementLink<Void>("xx",
			mock, 2);
		assertEquals(0, link.getPageNumber());
		pagecount = 1;
		assertEquals(0, link.getPageNumber());
		pagecount = 2;
		assertEquals(1, link.getPageNumber());
		currentpage = 1;
		assertEquals(1, link.getPageNumber());

		currentpage = 0;
		pagecount = 3;
		assertEquals(2, link.getPageNumber());
		currentpage = 1;
		assertEquals(2, link.getPageNumber());
	}

	/**
	 * Checks the logic behind the isFirst and isLast page methods.
	 */
	@Test
	void isFirstLastPage()
	{
		PagingNavigationIncrementLink<Void> link = new PagingNavigationIncrementLink<Void>("xx",
			mock, 0);

		pagecount = 0;
		currentpage = 0;
		assertTrue(link.isFirst());
		assertTrue(link.isLast());

		pagecount = 1;
		currentpage = 0;
		assertTrue(link.isFirst());
		assertTrue(link.isLast());

		pagecount = 2;
		currentpage = 0;
		assertTrue(link.isFirst());
		assertFalse(link.isLast());

		pagecount = 3;
		currentpage = 1;
		assertFalse(link.isFirst());
		assertFalse(link.isLast());

		pagecount = 3;
		currentpage = 2;
		assertFalse(link.isFirst());
		assertTrue(link.isLast());
	}
}
