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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.pagestore.DiskPageStore;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;

/**
 * https://issues.apache.org/jira/browse/WICKET-3108
 */
public class PageMapTest extends WicketTestCase
{
	private static final String TO_REMOVE_PAGE_MAP = "test-0";
	private static final String WORKING_PAGE_MAP = "test-1";
	// just to have an test limit, can be any value
	private static final int MAX_PAGE_MAPS = 2;
	private static final long BIG_OBJECT_SIZE = 10000;


	public void testSafePageMapName() throws Exception
	{
		IPageMap safe = PageMap.forName("this-is-a-safe-name");
		assertNotNull(safe);
		assertEquals("this-is-a-safe-name", safe.getName());
	}

	public void testDefaultPageMapName() throws Exception
	{
		IPageMap defaultPM = PageMap.forName(PageMap.DEFAULT_NAME);
		assertNotNull(defaultPM);
		assertNull(defaultPM.getName());
	}

	public void testUnsafePageMapName() throws Exception
	{
		IPageMap sanitizedPM = PageMap.forName("../../foobar.txt");
		assertNotNull(sanitizedPM);
		assertEquals(".._.._foobar.txt", sanitizedPM.getName());
	}

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

	/**
	 * Making sure that the PageMap don't get serialized with the session. If this test is failing,
	 * please look for any possible memory leak and than increase the BIG_OBJECT_SIZE.
	 * 
	 * @see https://issues.apache.org/jira/browse/WICKET-3160
	 */
	public void testPagemapIsNotReferencedBySession()
	{
		tester = new WicketTester(new WicketTester.DummyWebApplication()
		{
			@Override
			protected ISessionStore newSessionStore()
			{
				return new SecondLevelCacheSessionStore(this, new DiskPageStore());
			}
		});
		BigObject bigObject = new BigObject();
		long bigObjectSize = Objects.sizeof(bigObject);
		TestPage testPage = new TestPage();
		testPage.setDefaultModel(new Model(bigObject));
		tester.startPage(testPage);
		long sessionSize = Objects.sizeof(tester.getWicketSession());
		assertTrue(sessionSize < bigObjectSize);
	}

	public static class BigObject implements Serializable
	{
		private final ArrayList list = new ArrayList();
		{
			for (int i = 0; i < BIG_OBJECT_SIZE; i++)
			{
				list.add(Byte.MIN_VALUE);
			}
		}
	}

	/**
	 * Making sure that the getPageMaps return the correctly LRU page maps sequence
	 */
	public void testLruCache()
	{
		for (int i = 0; i < MAX_PAGE_MAPS; i++)
		{
			tester.getWicketSession().newPageMap(Integer.toString(i));
		}
		Integer cacheIndex = 0;
		for (IPageMap pageMap : tester.getWicketSession().getPageMaps())
		{
			assertEquals(cacheIndex++, Integer.valueOf(pageMap.getName()));
		}
	}

	/**
	 * Making sure that the dirty page map logic is marking the the page map as the last used.
	 */
	public void testLruCacheOnDirtyPageMap()
	{
		goToPage(TestPage.class, "0");
		goToPage(TestPage.class, "1");
		goToPage(TestPage.class, "2");
		goToPage(TestPage.class, "1");
		assertEquals(3, tester.getWicketSession().getPageMaps().size());
		assertEquals("0", tester.getWicketSession().getPageMaps().get(0).getName());
		assertEquals("1", tester.getWicketSession().getPageMaps().get(2).getName());
	}

	private void goToPage(Class<?> pageClass, String pageMap)
	{
		WebRequestCycle cycle = tester.setupRequestAndResponse();
		tester.getServletRequest().setParameter(
			WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME,
			pageMap + ':' + pageClass.getName());
		tester.processRequestCycle(cycle);
		tester.assertRenderedPage(TestPage.class);
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3109">WICKET-3109</a>
	 */
	public void testLazyPageMapCreationForPopup()
	{
		TestPage testPage = new TestPage();
		PopupSettings popupSettings = new PopupSettings();
		popupSettings.setWindowName(WORKING_PAGE_MAP);
		testPage.dummyPageLink.setPopupSettings(popupSettings);

		tester.startPage(testPage);

		for (IPageMap pageMap : tester.getWicketSession().getPageMaps())
		{
			assertNotSame(WORKING_PAGE_MAP, pageMap.getName());
		}

		tester.clickLink(testPage.dummyPageLink.getId());

		tester.assertRenderedPage(DummyHomePage.class);
		List<IPageMap> pageMaps = tester.getWicketSession().getPageMaps();
		assertTrue(pageMaps.contains(PageMap.forName(WORKING_PAGE_MAP)));
	}

	private void listPageMapsAtSession()
	{
		System.out.println("--> listing pagemaps on session...");
		for (IPageMap pageMap : tester.getWicketSession().getPageMaps())
		{
			System.out.println(pageMap.getName());
		}
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private Link<Void> dummyPageLink;

		/** */
		public TestPage()
		{
			add(new Link<Void>("removeTestPageMap")
			{
				private static final long serialVersionUID = 1L;

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
			add(dummyPageLink = new Link<Void>("dummyPage")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					setResponsePage(DummyHomePage.class);
				}
			});

		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id=\"removeTestPageMap\"></a><a wicket:id=\"dummyPage\"></a></body></html>");
		}
	}
}