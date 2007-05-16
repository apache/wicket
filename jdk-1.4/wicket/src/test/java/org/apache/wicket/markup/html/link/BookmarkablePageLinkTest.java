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

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;

/**
 * @author jcompagner
 */
public class BookmarkablePageLinkTest extends WicketTestCase
{

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public BookmarkablePageLinkTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testBookmarkableRequest() throws Exception
	{
		tester.setupRequestAndResponse();
		
		WebRequestCycle cycle = tester.createRequestCycle();
		
		String url = cycle.urlFor(new BookmarkablePageRequestTarget(BookmarkableHomePageLinksPage.class,null)).toString();

		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/" + url);
		
		tester.processRequestCycle();
		
		assertEquals(tester.getLastRenderedPage().getClass(), BookmarkableHomePageLinksPage.class);

	}
	
	/**
	 * @throws Exception
	 */
	public void testBookmarkableRequestWithIntercept() throws Exception
	{
		tester.setupRequestAndResponse();
		
		WebRequestCycle cycle = tester.createRequestCycle();
		
		String url = cycle.urlFor(new BookmarkablePageRequestTarget(BookmarkableThrowsInterceptPage.class,null)).toString();
		String url2 = cycle.urlFor(new BookmarkablePageRequestTarget(BookmarkableContinueToPage.class,null)).toString();

		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/" + url);
		tester.processRequestCycle();
		
		assertEquals(tester.getLastRenderedPage().getClass(), BookmarkableSetSecurityPage.class);
		
		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/" + url2);
		tester.processRequestCycle();
		assertEquals(tester.getLastRenderedPage().getClass(), BookmarkableThrowsInterceptPage.class);

	}


}
