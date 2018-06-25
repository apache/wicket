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
package org.apache.wicket.versioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.Page;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.page.PageManager;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.InMemoryPageStore;
import org.apache.wicket.pageStore.InSessionPageStore;
import org.apache.wicket.pageStore.SerializingPageStore;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A test for page versioning
 */
class PageVersioningTest
{
	private WicketTester wicketTester;

	/**
	 * setup()
	 */
	@BeforeEach
	void setup()
	{
		final PageVersioningApplication application = new PageVersioningApplication();

		wicketTester = new WicketTester(application)
		{

			/**
			 * @see org.apache.wicket.util.tester.BaseWicketTester#newTestPageManagerProvider()
			 */
			@Override
			protected IPageManagerProvider newTestPageManagerProvider()
			{
				return () ->
				{
					InMemoryPageStore inMemory = new InMemoryPageStore("test", Integer.MAX_VALUE);
					SerializingPageStore serializing = new SerializingPageStore(inMemory, new JavaSerializer("test"));
					final IPageStore store = new InSessionPageStore(serializing, Integer.MAX_VALUE);
					return new PageManager(store);
				};
			}

		};
	}

	/**
	 */
	@AfterEach
	void after()
	{
		wicketTester.destroy();
	}

	/**
	 * versionPage()
	 */
	@Test
	void versionPage()
	{
		Page versioningPage = wicketTester.startPage(VersioningTestPage.class);

		assertEquals(0, versioningPage.getPageId());

		wicketTester.clickLink("noopLink");
		assertEquals(0, versioningPage.getPageId());

		wicketTester.clickLink("ajaxUpdatingLink", true);
		assertEquals(0, versioningPage.getPageId());

		wicketTester.clickLink("ajaxUpdatingChangeModelLink", true);
		assertEquals(0, versioningPage.getPageId());

		wicketTester.clickLink("addTemporaryBehaviorLink");
		assertEquals(0, versioningPage.getPageId());

		wicketTester.clickLink("addBehaviorLink");
		assertEquals(1, versioningPage.getPageId());

		wicketTester.clickLink("changeEnabledStateLink");
		assertEquals(2, versioningPage.getPageId());

		wicketTester.clickLink("changeVisibilityStateLink");
		assertEquals(3, versioningPage.getPageId());

		try
		{
			// disable page versioning and execute something that otherwise would create a new
			// version
			versioningPage.setVersioned(false);
			wicketTester.clickLink("changeVisibilityStateLink");
			assertEquals(3, versioningPage.getPageId());
		}
		finally
		{
			versioningPage.setVersioned(true);
		}

		checkPageVersionsAreStored(versioningPage);
	}

	/**
	 * Asserts that there is a version of the page for each operation that modified the page
	 * 
	 * @param versioningPage
	 */
	private void checkPageVersionsAreStored(Page versioningPage)
	{
		IPageManager pageManager = wicketTester.getSession().getPageManager();

		int lastPageId = versioningPage.getPageId();
		while (lastPageId >= 0)
		{
			assertNotNull(pageManager.getPage(lastPageId));
			lastPageId--;
		}
	}

	private static final class PageVersioningApplication extends WebApplication
	{

		@Override
		public Class<? extends Page> getHomePage()
		{
			return VersioningTestPage.class;
		}
	};
}
