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

import static org.hamcrest.Matchers.containsString;

import org.apache.wicket.Component;
import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.mock.MockPageManager;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.page.IPageManagerContext;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
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
					public IPageManager apply(IPageManagerContext context)
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
	public void ajaxRequestForComponentInTransparentWebMarkupContainerShouldntCauseStackOverflow5()
	{
		tester.startPage(TransparentContainerWithManualTransparentContainerPage.class);

		// the page renders normally using normal web requests
		tester.assertRenderedPage(TransparentContainerWithManualTransparentContainerPage.class);

		// without WICKET-5898 fixed the statement below causes a StackOverflowError
		tester.clickLink("link", true);
		tester.assertComponentOnAjaxResponse("label");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5941
	 * 
	 * Headers not rendered for components inside TransparentWebMarkupContainer on ajax update
	 */
	@Test
	public void updateAjaxUpdateOfTransparentContainer() throws Exception
	{
		TestEmbeddedAjaxComponet page = new TestEmbeddedAjaxComponet();
		tester.startPage(page);
		assertEquals(2, page.renderHeadCount);
		
		tester.clickLink("container:updateTransparentContainer", true);
		assertEquals(4, page.renderHeadCount);
	}
	
	@Test
	public void updateAjaxUpdateOfContainerWithTransparentContainer() throws Exception
	{
		TestEmbeddedAjaxComponet page = new TestEmbeddedAjaxComponet();
		tester.startPage(page);
		assertEquals(2, page.renderHeadCount);
		
		tester.clickLink("container:updateContainer", true);
		assertEquals(4, page.renderHeadCount);
	}
	
	@Test
	public void nestedTransparentContainer() throws Exception
	{
		tester.startPage(TestEmbeddedTransparentMarkupContainer.class);
		tester.assertRenderedPage(TestEmbeddedTransparentMarkupContainer.class);
		
		final Page page = tester.getLastRenderedPage();
		final Component label = page.get("label");
		
		assertEquals(TestEmbeddedTransparentMarkupContainer.LABEL_MARKUP,
			label.getMarkup().toString(true));
	}

    /**
     * https://issues.apache.org/jira/browse/WICKET-6219
     */
    @Test
    public void shouldAllowAFragmentIdConflictingToASibilingTagWicketId() throws Exception
    {
            tester.startPage(SubPageWithAFragment.class);
            assertThat(tester.getLastResponseAsString(), containsString("content"));
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
	
	public static class TestEmbeddedAjaxComponet extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		
		public int renderHeadCount = 0;

		/** */
		public TestEmbeddedAjaxComponet()
		{
			final WebMarkupContainer container = new WebMarkupContainer("container");
			container.setOutputMarkupId(true);
			add(container);
			
			final Component transparentContainer = new TransparentWebMarkupContainer("transparentContainer").setOutputMarkupId(true);
			container.add(transparentContainer);
			
			container.add(new AjaxLink<Void>("updateContainer"){

				@Override
				public void internalRenderHead(HtmlHeaderContainer container)
				{
					super.internalRenderHead(container);

					renderHeadCount++;
				}
				
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					target.add(container);
				}
				
			});
			
			container.add(new AjaxLink<Void>("updateTransparentContainer"){

				@Override
				public void internalRenderHead(HtmlHeaderContainer container)
				{
					super.internalRenderHead(container);

					renderHeadCount++;
				}
				
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					target.add(transparentContainer);
				}
				
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("" + //
				"<html><body>" + //
				"	<div wicket:id=\"container\">" + //
				"		<div wicket:id=\"transparentContainer\">" + //
				"			<a wicket:id=\"updateContainer\"></a>" + //
				"			<a wicket:id=\"updateTransparentContainer\"></a>" + //
				"		</div>" + //
				"	</div>" + //
				"</body></html>");
		}
	}
	
	public static class TestEmbeddedTransparentMarkupContainer extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		
		public static final String LABEL_MARKUP = "<span wicket:id=\"label\"></span>";
		
		/** */
		public TestEmbeddedTransparentMarkupContainer()
		{
			add(new TransparentWebMarkupContainer("outer"));
			add(new TransparentWebMarkupContainer("inner"));
			add(new Label("label"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("" + //
				"<html><body>" + //
				"	<div wicket:id=\"outer\">" + //
				"		<div wicket:id=\"inner\">" + //
				"			" + LABEL_MARKUP + //
				"		</div>" + //
				"	</div>" + //
				"</body></html>");
		}
	}
	public static class PageWithAChildInsideATransparentContainer extends WebPage
			implements
				IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		public PageWithAChildInsideATransparentContainer(PageParameters parameters)
		{
			super(parameters);
			add(new TransparentWebMarkupContainer("wrapper"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<?> containerClass)
		{
			return new StringResourceStream("" + //
					"<html><body>" + //
					" <div wicket:id=\"wrapper\">" + //
					"	<wicket:child/>" + //
					" </div>" + //
					"</body></html>");
		}
	}
	public static class SubPageWithAFragment extends PageWithAChildInsideATransparentContainer
	{
		private static final long serialVersionUID = 1L;

		public SubPageWithAFragment(PageParameters parameters)
		{
			super(parameters);
			Fragment fragment = new Fragment("header", "header", this);
			add(fragment);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<?> containerClass)
		{
			if (PageWithAChildInsideATransparentContainer.class.equals(containerClass))
				return super.getMarkupResourceStream(container, containerClass);
			return new StringResourceStream("" + //
					"<html><body>" + //
					"<wicket:extend>" + //
					"	<div wicket:id=\"header\"></div>" + //
					"	<wicket:fragment wicket:id=\"header\">content</wicket:fragment>" + //
					"</wicket:extend>" + //
					"</body></html>");
		}
	}
}
