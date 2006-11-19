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
package wicket.markup.html.navigation.paging;

/**
 * Tests the PagingNavigationLink.
 * 
 * @author Martijn Dashorst
 */
public class PagingNavigationLinkTest extends AbstractPagingNavigationTest
{
	/**
	 * Construct.
	 * 
	 * @param name
	 *            the name of the test
	 */
	public PagingNavigationLinkTest(String name)
	{
		super(name);
	}

	/** the mock pageable driver. */
	private MockPageable mock;

	/**
	 * sets up the test.
	 * 
	 * @throws Exception
	 */
	public void setUp() throws Exception
	{
		super.setUp();
		mock = new MockPageable();
	}

	/**
	 * Tests the get page number and is first and last methods.
	 */
	public void testGetPageNumber_1()
	{
		PagingNavigationLink link = new PagingNavigationLink("id", mock, 0);
		
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
	public void testGetPageNumber_2()
	{
		PagingNavigationLink link = new PagingNavigationLink("id", mock, 2);
		
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
	public void testGetPageNumber_3()
	{
		PagingNavigationLink link = new PagingNavigationLink("id", mock, -1);
		
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
	public void testLinksTo()
	{
		PagingNavigationLink link = new PagingNavigationLink("id", mock, -1);
		
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
