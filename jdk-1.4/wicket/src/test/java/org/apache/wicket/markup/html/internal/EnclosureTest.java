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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.DummyApplication;
import org.apache.wicket.util.tester.WicketTester;


/**
 * 
 * @author Juergen Donnerstag
 */
public class EnclosureTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public EnclosureTest(final String name)
	{
		super(name);
	}

	/**
	 * 
	 * @see org.apache.wicket.WicketTestCase#setUp()
	 */

	protected void setUp() throws Exception
	{
		WebApplication app = new DummyApplication();
		tester = new WicketTester(app);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage() throws Exception
	{
		executeTest(EnclosurePage_1.class, "EnclosurePageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage2() throws Exception
	{
		executeTest(EnclosurePage_2.class, "EnclosurePageExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage3() throws Exception
	{
		executeTest(EnclosurePage_3.class, "EnclosurePageExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage4() throws Exception
	{
		executeTest(EnclosurePage_4.class, new PageParameters("visible=false"),
			"EnclosurePageExpectedResult_4.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage4_1() throws Exception
	{
		executeTest(EnclosurePage_4.class, new PageParameters("visible=true"),
			"EnclosurePageExpectedResult_4-1.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage5() throws Exception
	{
		executeTest(EnclosurePage_5.class, new PageParameters("visible=false"),
			"EnclosurePageExpectedResult_5.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage5_1() throws Exception
	{
		executeTest(EnclosurePage_5.class, new PageParameters("visible=true"),
			"EnclosurePageExpectedResult_5-1.html");
	}

	/**
	 * Tests visibility of children after enclosure has been made hidden and visible again
	 * 
	 * @throws Exception
	 */
	public void testVisibilityOfChildren() throws Exception
	{
		// render with enclosure initally visible
		tester.startPage(EnclosurePage_6.class);
		String doc = tester.getServletResponse().getDocument();
		assertTrue(doc.indexOf("content1") != -1);
		assertTrue(doc.indexOf("content2") != -1);

		// render with enclosure hidden
		tester.clickLink("link");
		doc = tester.getServletResponse().getDocument();
		assertFalse(doc.indexOf("content1") != -1);
		assertFalse(doc.indexOf("content2") != -1);

		// render with enclosure visible again
		tester.clickLink("link");
		doc = tester.getServletResponse().getDocument();
		assertTrue(doc.indexOf("content1") != -1);
		assertTrue(doc.indexOf("content2") != -1);
	}
}
