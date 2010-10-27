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

import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

/**
 * https://issues.apache.org/jira/browse/WICKET-3108
 */
public class PageMapTest extends WicketTestCase
{
	private static final String TO_REMOVE_PAGE_MAP = "test-0";
	private static final String WORKING_PAGE_MAP = "test-1";
	// just to have an test limit, can be any value
	private static final int MAX_PAGE_MAPS = 2;

	/**
	 * Making sure that the TO_REMOVE_PAGEMAP doesn't get back to session after a call to
	 * IPageMap.remove()
	 */
	public void testRemovePageMap()
	{
		// ends up creating the TO_REMOVE_PAGEMAP pagemap
		goToPage(TestPage.class, TO_REMOVE_PAGE_MAP);
		listPageMapsAtSession();

		// ends up creating the WORKING_PAGEMAP pagemap
		goToPage(TestPage.class, WORKING_PAGE_MAP);
		listPageMapsAtSession();

		// trying to remove TO_REMOVE_PAGEMAP pagemap
		tester.clickLink("removeTestPageMap");
		tester.assertRenderedPage(TestPage.class);

		for (IPageMap pageMap : tester.getWicketSession().getPageMaps())
		{
			assertFalse(pageMap.getName().equals(TO_REMOVE_PAGE_MAP));
		}
	}


	/**
	 * Requesting (maxPageMaps + 1) pages on distinct pagemaps
	 */
	public void testMaxPageMapsUnderMultiWindowsRequestOverflow()
	{
		tester.getApplication().getSessionSettings().setMaxPageMaps(MAX_PAGE_MAPS);
		for (int i = 0; i < MAX_PAGE_MAPS + 1; i++)
		{
			goToPage(TestPage.class, "test-" + i);
			listPageMapsAtSession();
		}
		assertTrue(MAX_PAGE_MAPS >= tester.getWicketSession().getPageMaps().size());
	}

	/**
	 * Creating (maxPageMaps + 1) pagemaps
	 */
	public void testMaxPageMapsUnderFactoryMethodInvocationOverflow()
	{
		tester.getApplication().getSessionSettings().setMaxPageMaps(MAX_PAGE_MAPS);
		for (int i = 0; i < MAX_PAGE_MAPS + 1; i++)
		{
			tester.getWicketSession().newPageMap("test-" + i);
			listPageMapsAtSession();
		}
		assertTrue(MAX_PAGE_MAPS >= tester.getWicketSession().getPageMaps().size());
	}

	private void goToPage(Class pageClass, String pageMap)
	{
		WebRequestCycle cycle = tester.setupRequestAndResponse();
		tester.getServletRequest().setParameter(
			WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME,
			pageMap + ':' + pageClass.getName());
		tester.processRequestCycle(cycle);
		tester.assertRenderedPage(TestPage.class);
	}

	private void listPageMapsAtSession()
	{
		System.out.println("--> listing pagemaps on session...");
		for (IPageMap pageMap : tester.getWicketSession().getPageMaps())
		{
			System.out.println(pageMap.getName());
		}
	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public TestPage()
		{
			super();
			add(new Link("removeTestPageMap")
			{
				@Override
				public void onClick()
				{
					for (IPageMap pageMap : Session.get().getPageMaps())
					{
						if (TO_REMOVE_PAGE_MAP.equals(pageMap.getName()))
						{
							pageMap.remove();
							System.out.println("--> test-0 removed.");
						}
					}
				}
			});
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id=\"removeTestPageMap\"></a></body></html>");
		}
	}
}