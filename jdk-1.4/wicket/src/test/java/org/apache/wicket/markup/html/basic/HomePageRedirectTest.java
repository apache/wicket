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
package org.apache.wicket.markup.html.basic;

import org.apache.wicket.util.diff.DiffUtil;
import org.apache.wicket.util.tester.WicketTester;

import junit.framework.TestCase;

/**
 * @author jcompagner
 */
public class HomePageRedirectTest extends TestCase
{
	WicketTester application;
	protected void setUp() throws Exception
	{
		application = new WicketTester();
	}
	protected void tearDown() throws Exception
	{
		application.destroy();
	}
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
		application.startPage(HomePagePageRedirect.class);

		assertEquals(RedirectPage.class, application.getLastRenderedPage().getClass());

		// Validate the document
		String document = application.getServletResponse().getDocument();
		DiffUtil.validatePage(document, this.getClass(), "RedirectPage.html", true);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePageClassRedirect() throws Exception
	{
		application.startPage(HomePageClassRedirect.class);

		assertEquals(RedirectPage.class, application.getLastRenderedPage().getClass());

		// Validate the document
		String document = application.getServletResponse().getDocument();
		DiffUtil.validatePage(document, this.getClass(), "RedirectPage.html", true);
	}
}
