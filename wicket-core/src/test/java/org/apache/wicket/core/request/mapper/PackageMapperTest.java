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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.MockPage;
import org.apache.wicket.core.request.handler.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.PackageMapperTest.OuterPage.InnerPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.PackageName;
import org.junit.Test;

/**
 * Tests for {@link PackageMapper}
 */
public class PackageMapperTest extends AbstractMapperTest
{

	private static final String ALIAS = "alias";

	private final PackageMapper encoder = new PackageMapper(PackageName.forClass(MockPage.class))
	{
		@Override
		protected IMapperContext getContext()
		{
			return context;
		}
	};

	private static final String PAGE_CLASS_NAME = MockPage.class.getSimpleName();

	private final PackageMapper aliasEncoder = new PackageMapper(
		PackageName.forClass(MockPage.class))
	{
		@Override
		protected IMapperContext getContext()
		{
			return context;
		}

		@Override
		protected String transformFromUrl(String classNameAlias)
		{
			final String realClassName;
			if (ALIAS.equals(classNameAlias))
			{
				realClassName = PAGE_CLASS_NAME;
			}
			else
			{
				realClassName = super.transformFromUrl(classNameAlias);
			}
			return realClassName;
		}

		@Override
		protected String transformForUrl(String className)
		{
			final String alias;
			if (PAGE_CLASS_NAME.equals(className))
			{
				alias = ALIAS;
			}
			else
			{
				alias = super.transformForUrl(className);
			}
			return alias;
		}
	};

	/**
	 *
	 */
	@Test
	public void decode1()
	{
		Url url = Url.parse(PAGE_CLASS_NAME);
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		assertEquals(PAGE_CLASS_NAME, page.getClass().getSimpleName());
		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().isEmpty());
	}

	/**
	 *
	 */
	@Test
	public void decode2()
	{
		Url url = Url.parse(PAGE_CLASS_NAME + "/indexed1?a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		assertEquals(PAGE_CLASS_NAME, page.getClass().getSimpleName());

		PageParameters p = page.getPageParameters();
		assertEquals(1, p.getIndexedCount());
		assertEquals("indexed1", p.get(0).toString());

		assertEquals(2, p.getNamedKeys().size());
		assertEquals("b", p.get("a").toString());
		assertEquals("c", p.get("b").toString());
	}

	/**
	 *
	 */
	@Test
	public void decode3()
	{
		Url url = Url.parse(PAGE_CLASS_NAME + "?15");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		checkPage(page, 15);
	}

	/**
	 *
	 */
	@Test
	public void decode4()
	{
		Url url = Url.parse(PAGE_CLASS_NAME + "/i1/i2?15&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);

		context.setCurrentPageParameters(((RenderPageRequestHandler)handler).getPageParameters());
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		checkPage(page, 15);

		PageParameters p = page.getPageParameters();
		assertEquals(2, p.getIndexedCount());

		assertEquals(2, p.getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	public void decode5()
	{
		Url url = Url.parse(PAGE_CLASS_NAME + "?15-ILinkListener-foo-bar");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof ListenerInterfaceRequestHandler);

		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

		IRequestablePage page = h.getPage();
		checkPage(page, 15);

		assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
		assertEquals("foo:bar", h.getComponent().getPageRelativePath());
		assertNull(h.getBehaviorIndex());
	}

	/**
	 *
	 */
	@Test
	public void decode6()
	{
		Url url = Url.parse(PAGE_CLASS_NAME + "/i1/i2?15-ILinkListener-foo-bar&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof ListenerInterfaceRequestHandler);
		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;
		context.setCurrentPageParameters(h.getPageParameters());

		IRequestablePage page = h.getPage();
		checkPage(page, 15);

		assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
		assertEquals("foo:bar", h.getComponent().getPageRelativePath());

		PageParameters p = page.getPageParameters();
		assertEquals(2, p.getIndexedCount());

		assertEquals(2, p.getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	public void decode7()
	{
		Url url = Url.parse(PAGE_CLASS_NAME + "?15-ILinkListener.4-foo-bar");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof ListenerInterfaceRequestHandler);

		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

		IRequestablePage page = h.getPage();
		checkPage(page, 15);

		assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
		assertEquals("foo:bar", h.getComponent().getPageRelativePath());
		assertEquals((Object)4, h.getBehaviorIndex());
	}

	/**
	 *
	 */
	@Test
	public void decode8()
	{
		Url url = Url.parse(PAGE_CLASS_NAME + "/i1/i2?15-5.ILinkListener-foo-bar&a=b&b=c");

		context.setNextPageRenderCount(5);

		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof ListenerInterfaceRequestHandler);
		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;
		context.setCurrentPageParameters(h.getPageParameters());

		IRequestablePage page = h.getPage();
		assertEquals(page.getRenderCount(), 5);
	}

	/**
	 *
	 */
	@Test(expected = StalePageException.class)
	public void decode9()
	{
		Url url = Url.parse(PAGE_CLASS_NAME + "/i1/i2?15-5.ILinkListener-foo-bar&a=b&b=c");

		context.setNextPageRenderCount(6);

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		context.setCurrentPageParameters(((ListenerInterfaceRequestHandler)handler).getPageParameters());

		((IPageRequestHandler)handler).getPage();
	}

	/**
	 * WICKET-2993
	 */
	@Test
	public void decode10()
	{
		// use String.class but any other non-Page will do the job as well
		Url url = Url.parse(String.class.getSimpleName());

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull("A non-page class should not create a request handler!", handler);
	}

	/**
	 *
	 */
	@Test
	public void encode1()
	{
		PageProvider provider = new PageProvider(MockPage.class, new PageParameters());
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);
		assertEquals(PAGE_CLASS_NAME, url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode2()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b");
		parameters.set("b", "c");
		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);
		assertEquals(PAGE_CLASS_NAME + "/i1/i2?a=b&b=c", url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode3()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b");
		parameters.set("b", "c");

		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);

		assertEquals(PAGE_CLASS_NAME + "/i1/i2?a=b&b=c", url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode4()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b");
		page.getPageParameters().set("b", "c");
		page.setCreatedBookmarkable(true);

		IPageProvider provider = new PageProvider(page);
		IRequestHandler handler = new RenderPageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);

		assertEquals(PAGE_CLASS_NAME + "/i1/i2?15&a=b&b=c", url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode5()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b");
		page.getPageParameters().set("b", "c");

		page.setCreatedBookmarkable(false);

		IPageProvider provider = new PageProvider(page);
		IRequestHandler handler = new RenderPageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);

		// never allow bookmarkable render url for page that has not been created by bookmarkable
		// URL

		assertNull(url);
	}

	/**
	 *
	 */
	@Test
	public void encode6()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b");
		page.getPageParameters().set("b", "c");

		// shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE);

		Url url = encoder.mapHandler(handler);

		assertEquals(PAGE_CLASS_NAME + "/i1/i2?15-0.ILinkListener-foo-bar&a=b&b=c", url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode7()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b");
		page.getPageParameters().set("b", "c");

		// shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE, 4);

		Url url = encoder.mapHandler(handler);

		assertEquals(PAGE_CLASS_NAME + "/i1/i2?15-0.ILinkListener.4-foo-bar&a=b&b=c",
			url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode8()
	{
		MockPage page = new MockPage(15);
		page.setBookmarkable(true);
		page.setCreatedBookmarkable(true);
		page.setPageStateless(true);

		IPageProvider provider = new PageProvider(page);
		IRequestHandler handler = new RenderPageRequestHandler(provider);

		Url url = encoder.mapHandler(handler);

		assertEquals(PAGE_CLASS_NAME, url.toString());
	}


	private final PackageMapper innerClassEncoder = new PackageMapper(
		PackageName.forClass(OuterPage.class))
	{
		@Override
		protected IMapperContext getContext()
		{
			return context;
		}
	};

	public static class OuterPage extends MockPage
	{
		private static final long serialVersionUID = 1L;

		public static class InnerPage extends MockPage
		{
			private static final long serialVersionUID = 1L;
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3838
	 */
	@Test
	public void encodeInnerClass()
	{
		InnerPage page = new OuterPage.InnerPage();
		IPageProvider provider = new PageProvider(page);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);

		Url url = innerClassEncoder.mapHandler(handler);

		assertEquals("PackageMapperTest$OuterPage$InnerPage", url.toString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3838
	 */
	@Test
	public void decodeInnerClass()
	{
		Url url = Url.parse("PackageMapperTest$OuterPage$InnerPage");
		IRequestHandler handler = innerClassEncoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		assertEquals("InnerPage", page.getClass().getSimpleName());
		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().isEmpty());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3941
	 */
	@Test
	public void encodeAlias()
	{
		MockPage page = new MockPage(15);
		page.setBookmarkable(true);
		page.setCreatedBookmarkable(true);
		page.setPageStateless(true);

		IPageProvider provider = new PageProvider(page);
		IRequestHandler handler = new RenderPageRequestHandler(provider);

		Url url = aliasEncoder.mapHandler(handler);

		assertEquals(ALIAS, url.toString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3941
	 */
	@Test
	public void decodeAlias()
	{
		Url url = Url.parse(ALIAS + "?15");
		IRequestHandler handler = aliasEncoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		checkPage(page, 15);
		assertEquals(PAGE_CLASS_NAME, page.getClass().getSimpleName());
	}
}
