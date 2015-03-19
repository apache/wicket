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
package org.apache.wicket;

import java.util.Locale;

import org.apache.wicket.markup.html.WebPage;
import org.junit.Test;

/**
 * Tests markup loading.
 */
public class PageMarkupLoadingTest extends WicketTestCase
{
	/**
	 * Test default locale loaded page.
	 * 
	 * @throws Exception
	 */
	@Test
	public void english() throws Exception
	{
		tester.getSession().setLocale(Locale.ENGLISH);
		tester.startPage(Page1.class);
		tester.assertRenderedPage(Page1.class);
		tester.assertResultPage(getClass(), "PageMarkupLoadingTest$Page1_expected.html");
	}

	/**
	 * Test Dutch locale loaded page.
	 * 
	 * @throws Exception
	 */
	@Test
	public void dutch() throws Exception
	{
		tester.getSession().setLocale(new Locale("nl"));
		tester.startPage(Page1.class);
		tester.assertRenderedPage(Page1.class);
		tester.assertResultPage(getClass(), "PageMarkupLoadingTest$Page1_nl_expected.html");
	}

	/**
	 * Test Dutch/ my style locale loaded page.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDutchMyStyle() throws Exception
	{
		tester.getSession().setLocale(new Locale("nl"));
		tester.getSession().setStyle("mystyle");
		tester.startPage(Page1.class);
		tester.assertRenderedPage(Page1.class);
		tester.assertResultPage(getClass(), "PageMarkupLoadingTest$Page1_mystyle_nl_expected.html");
	}

	/**
	 * Test Dutch/ my style locale loaded page.
	 * 
	 * @throws Exception
	 */
	@Test
	public void dutchMyStyleMyVar() throws Exception
	{
		tester.getSession().setLocale(new Locale("nl"));
		tester.getSession().setStyle("mystyle");
		tester.startPage(Page2.class);
		tester.assertRenderedPage(Page2.class);
		tester.assertResultPage(getClass(),
			"PageMarkupLoadingTest$Page2_myvar_mystyle_nl_expected.html");
	}

	/** simple test page */
	public static class Page1 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/** Construct. */
		public Page1()
		{
		}
	}

	/** simple test page */
	public static class Page2 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/** Construct. */
		public Page2()
		{
		}

		@Override
		public String getVariation()
		{
			return "myvar";
		}
	}
}
