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
package wicket.markup.html.basic;

import junit.framework.TestCase;
import wicket.util.diff.DiffUtil;
import wicket.util.tester.WicketTester;

/**
 * @author jcompagner
 */
public class HomePageRedirectTest extends TestCase
{

	/**
	 * Construct.
	 */
	public HomePageRedirectTest()
	{
		super();
	}


	/**
	 * @throws Exception
	 */
	public void testRenderHomePagePageRedirect() throws Exception
	{
		WicketTester tester = new WicketTester(HomePagePageRedirect.class);

		// Do the processing
		tester.setupRequestAndResponse();
		tester.processRequestCycle();

		assertEquals(RedirectPage.class, tester.getLastRenderedPage().getClass());

		// Validate the document
		String document = tester.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(), "RedirectPage.html"));

	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePageClassRedirect() throws Exception
	{
		WicketTester tester = new WicketTester(HomePageClassRedirect.class);

		// Do the processing
		tester.setupRequestAndResponse();
		tester.processRequestCycle();

		assertEquals(RedirectPage.class, tester.getLastRenderedPage().getClass());

		// Validate the document
		String document = tester.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(), "RedirectPage.html"));
	}
}
