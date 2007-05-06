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
package org.apache.wicket.markup.html.link;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.IndexedParamUrlCodingStrategy;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;


/**
 * @author jcompagner
 */
public class IndexedParamUrlCodingTest extends WicketTestCase
{

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public IndexedParamUrlCodingTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testIndexedLink() throws Exception
	{
		tester.getApplication().mount(
				new IndexedParamUrlCodingStrategy("/test1", BookmarkableHomePageLinksPage.class,
						null));
		tester.getApplication().mount(
				new IndexedParamUrlCodingStrategy("/test2", BookmarkableHomePageLinksPage.class,
						"mypagemap"));

		tester.setupRequestAndResponse();
		WebRequestCycle cycle = tester.createRequestCycle();

		PageParameters parameters = new PageParameters();
		parameters.add("0", "Integer0");
		parameters.add("1", "Integer1");
		parameters.add("2", "a:b");

		String url1 = cycle.urlFor(
				new BookmarkablePageRequestTarget(BookmarkableHomePageLinksPage.class, parameters))
				.toString();
		String url2 = cycle.urlFor(
				new BookmarkablePageRequestTarget("mypagemap", BookmarkableHomePageLinksPage.class,
						parameters)).toString();
		assertEquals("test1/Integer0/Integer1/a%3Ab", url1);
		assertEquals("test2/Integer0/Integer1/a%3Ab/wicket:pageMapName/mypagemap", url2);

		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/" + url1);
		cycle = tester.createRequestCycle();
		IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();

		RequestParameters requestParameters = encoder.decode(tester.getWicketRequest());

		IRequestTarget target1 = cycle.getProcessor().resolve(cycle, requestParameters);
		if (target1 instanceof BookmarkablePageRequestTarget)
		{
			assertNull(((BookmarkablePageRequestTarget)target1).getPageMapName());
		}
		else
		{
			fail("url: " + url1 + " wasn't resolved to a bookmarkable target: " + target1);
		}
		PageParameters params = ((BookmarkablePageRequestTarget)target1).getPageParameters();
		assertEquals("Integer0", params.getString("0"));
		assertEquals("Integer1", params.getString("1"));
		assertEquals("a:b", params.getString("2"));

		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/" + url2);
		cycle = tester.createRequestCycle();
		encoder = cycle.getProcessor().getRequestCodingStrategy();

		requestParameters = encoder.decode(tester.getWicketRequest());

		IRequestTarget target2 = cycle.getProcessor().resolve(cycle, requestParameters);

		if (target2 instanceof BookmarkablePageRequestTarget)
		{
			assertEquals("mypagemap", ((BookmarkablePageRequestTarget)target2).getPageMapName());
		}
		else
		{
			fail("url: " + url2 + " wasn't resolved to a bookmarkable target: " + target2);
		}
	}
}
