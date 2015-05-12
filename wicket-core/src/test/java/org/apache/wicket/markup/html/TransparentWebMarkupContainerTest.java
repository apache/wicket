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
package org.apache.wicket.markup.html;

import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.mock.MockPageManager;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.page.IPageManagerContext;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class TransparentWebMarkupContainerTest extends WicketTestCase
{
	/**
	 * WICKET-3512
	 * 
	 * @throws Exception
	 */
	@Test
	public void markupInheritanceResolver() throws Exception
	{
		executeTest(MarkupInheritanceResolverTestPage3.class,
			"MarkupInheritanceResolverTestPage_expected.html");
	}

	/**
	 * 
	 */
	@Test
	public void unableToFindComponents()
	{
		try
		{
			tester.startPage(TestPage.class);
			fail();
		}
		catch (MarkupException e)
		{
			assertTrue(e.getMessage(),
				e.getMessage().contains("Unable to find component with id 'c1'"));
		}
	}

	/**
	 * Test if the render is OK even if users define its own component with the same id
	 * WicketTagIdentifier is generation for internal components.
	 */
	@Test
	public void usingGeneratedWicketIdAreSafe1()
	{
		tester.startPage(TestPage2.class);
		assertTrue(tester.getLastResponseAsString().contains("test_message"));

	}

	/**
	 * Same test in different scenario
	 */
	@Test
	public void usingGeneratedWicketIdAreSafe2()
	{
		tester.startPage(TestPage3.class);
		String expected = tester.getApplication()
			.getResourceSettings()
			.getLocalizer()
			.getString("null", null);
		assertTrue(tester.getLastResponseAsString().contains(expected));
	}

	/**
	 * Test case for <a href="https://issues.apache.org/jira/browse/WICKET-3719">WICKET-3719</a>
	 */
	@Test
	public void ajaxUpdate()
	{
		WicketTester wicketTester = new WicketTester()
		{
			@Override
			protected IPageManagerProvider newTestPageManagerProvider()
			{
				return new IPageManagerProvider()
				{
					@Override
					public IPageManager get(IPageManagerContext context)
					{
						return new MockPageManager()
						{
							@Override
							public void touchPage(IManageablePage page)
							{
								page = WicketObjects.cloneObject(page);
								super.touchPage(page);
							}
						};
					}
				};
			}

		};

		wicketTester.startPage(TransparentWithAjaxUpdatePage.class);
		wicketTester.clickLink("link", true);
		wicketTester.destroy();
	}


	/**
	 * Tests the WICKET-5898 issue of triggering a StackOverflowError when a component inside nested
	 * TransparentWebMarkupContainers is updated. This particular test case is caused by Wicket's
	 * insertion of a TransparentWebMarkupContainer automatically due to a {@code src} attribute
	 * that might need rewriting.
	 */
	@Test
	public void ajaxRequestForComponentInTransparentWebMarkupContainerShouldntCauseStackOverflow()
	{
		tester.startPage(SingleNestedTransparentContainerPage.class);

		// the page renders normally using normal web requests
		tester.assertRenderedPage(SingleNestedTransparentContainerPage.class);

		// without WICKET-5898 fixed the statement below causes a StackOverflowError
		tester.clickLink("link", true);
		tester.assertComponentOnAjaxResponse("label");
	}

	/**
	 * Tests the WICKET-5898 issue of triggering a StackOverflowError when a component inside nested
	 * TransparentWebMarkupContainers is updated. This particular test case is caused by having two
	 * TransparentWebMarkupContainers nested and trying to update a label that was added to the
	 * outer TWMC.
	 */
	@Test
	public void ajaxRequestForComponentInTransparentWebMarkupContainerShouldntCauseStackOverflow2()
	{
		tester.startPage(DoubleNestedTransparentContainerPage.class);

		// the page renders normally using normal web requests
		tester.assertRenderedPage(DoubleNestedTransparentContainerPage.class);

		// without WICKET-5898 fixed the statement below causes a StackOverflowError
		tester.clickLink("link", true);
		tester.assertComponentOnAjaxResponse("label");
	}

	/**
	 * Tests the WICKET-5898 issue of triggering a StackOverflowError when a component inside nested
	 * TransparentWebMarkupContainers is updated. This particular test case is caused by having two
	 * TransparentWebMarkupContainers nested, and where a TWMC exist inside a sibling web markup
	 * container and trying to update a label that was added to the outer TWMC.
	 */
	@Test
	@Ignore("Fails due to WICKET-5898")
	public void ajaxRequestForComponentInTransparentWebMarkupContainerShouldntCauseStackOverflow3()
	{
		tester.startPage(DoubleNestedTransparentContainerWithSiblingTransparentContainerPage.class);

		// the page renders normally using normal web requests
		tester.assertRenderedPage(DoubleNestedTransparentContainerWithSiblingTransparentContainerPage.class);

		// without WICKET-5898 fixed the statement below causes a StackOverflowError
		tester.clickLink("link", true);
		tester.assertComponentOnAjaxResponse("label");
	}

	/**
	 * Tests the WICKET-5898 issue of triggering a StackOverflowError when a component inside nested
	 * TransparentWebMarkupContainers is updated. This particular test case is caused by having
	 * introduce automatic transparent containers inside some link components due to a
	 * {@code <img src="">} tag inside the link tags, and trying to update a label that was added to
	 * the outer TWMC.
	 */
	@Test
	@Ignore("Fails due to WICKET-5898")
	public void ajaxRequestForComponentInTransparentWebMarkupContainerShouldntCauseStackOverflow4()
	{
		tester.startPage(TransparentContainerWithAutoTransparentContainerPage.class);

		// the page renders normally using normal web requests
		tester.assertRenderedPage(TransparentContainerWithAutoTransparentContainerPage.class);

		// without WICKET-5898 fixed the statement below causes a StackOverflowError
		tester.clickLink("link", true);
		tester.assertComponentOnAjaxResponse("label");
	}

	/**
	 * Tests the WICKET-5898 issue of triggering a StackOverflowError when a component inside nested
	 * TransparentWebMarkupContainers is updated. This particular test case is caused by having
	 * manually added transparent containers inside some link components, and trying to update a
	 * label that was added to the outer TWMC.
	 */
	@Test
	@Ignore("Fails due to WICKET-5898")
	public void ajaxRequestForComponentInTransparentWebMarkupContainerShouldntCauseStackOverflow5()
	{
		tester.startPage(TransparentContainerWithManualTransparentContainerPage.class);

		// the page renders normally using normal web requests
		tester.assertRenderedPage(TransparentContainerWithManualTransparentContainerPage.class);

		// without WICKET-5898 fixed the statement below causes a StackOverflowError
		tester.clickLink("link", true);
		tester.assertComponentOnAjaxResponse("label");
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/** */
		public TestPage()
		{
			add(new TestBorder("border"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("" + //
				"<html><body>" + //
				"	<div wicket:id=\"border\">" + //
				"		<div wicket:id=\"c1\"></div>" + // component is only at the markup
				"	</div>" + //
				"</body></html>");
		}
	}

	private static class TestBorder extends Border implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private TestBorder(String id)
		{
			super(id);
			addToBorder(new Label("c1", "some border title"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<wicket:border><div wicket:id=\"c1\"></div><wicket:body /></wicket:border>");
		}
	}

	/** */
	public static class TestPage2 extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/** */
		public TestPage2()
		{
			add(new Label("_wicket_enclosure"));
			add(new TransparentWebMarkupContainer("container").add(new Label("msg", "test_message")));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("" + //
				"<html><body>" + //
				"	<div wicket:id=\"_wicket_enclosure\"></div>" + //
				"	<div wicket:id=\"container\">" + //
				"		<wicket:enclosure child=\"msg\">" + //
				"			<span wicket:id=\"msg\"></span>" + //
				"		</wicket:enclosure>" + //
				"	</div>" + //
				"</body></html>");
		}
	}

	/** */
	public static class TestPage3 extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/** */
		public TestPage3()
		{
			add(new WebComponent("_wicket_message"));
			add(new TransparentWebMarkupContainer("container"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("" + //
				"<html><body>" + //
				"	<div wicket:id=\"_wicket_message\"></div>" + //
				"	<div wicket:id=\"container\">" + //
				"		<wicket:message key=\"null\" />" + //
				"	</div>" + //
				"</body></html>");
		}
	}
}
