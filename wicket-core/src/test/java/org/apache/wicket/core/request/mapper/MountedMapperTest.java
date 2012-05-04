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
import org.apache.wicket.request.IRequestMapper;
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
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Test;

/**
 * @author Matej Knopp
 */
public class MountedMapperTest extends AbstractMapperTest
{

	/**
	 * Construct.
	 */
	public MountedMapperTest()
	{
	}

	private final MountedMapper encoder = new MountedMapper("/some/mount/path", MockPage.class)
	{
		@Override
		protected IMapperContext getContext()
		{
			return context;
		}

		@Override
		boolean getRecreateMountedPagesAfterExpiry()
		{
			return true;
		}
	};

	private final MountedMapper placeholderEncoder = new MountedMapper(
		"/some/${param1}/path/${param2}", MockPage.class)
	{
		@Override
		protected IMapperContext getContext()
		{
			return context;
		}

		@Override
		boolean getRecreateMountedPagesAfterExpiry()
		{
			return true;
		}
	};

	private final MountedMapper optionPlaceholderEncoder = new MountedMapper(
		"/some/#{param1}/path/${param2}/#{param3}", MockPage.class)
	{
		@Override
		protected IMapperContext getContext()
		{
			return context;
		}

		@Override
		boolean getRecreateMountedPagesAfterExpiry()
		{
			return true;
		}
	};

	/**
	 *
	 */
	@Test
	public void decode1()
	{
		Url url = Url.parse("some/mount/path");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().isEmpty());
	}

	/**
	 *
	 */
	@Test
	public void decode2()
	{
		Url url = Url.parse("some/mount/path/indexed1?a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

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
		Url url = Url.parse("some/mount/path?15");
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
		Url url = Url.parse("some/mount/path/i1/i2?15&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		checkPage(page, 15);

		PageParameters p = page.getPageParameters();
		assertEquals(0, p.getIndexedCount());

		assertEquals(0, p.getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	public void decode5()
	{
		Url url = Url.parse("some/mount/path?15-ILinkListener-foo-bar");
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
		Url url = Url.parse("some/mount/path/i1/i2?15-ILinkListener-foo-bar&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof ListenerInterfaceRequestHandler);
		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

		IRequestablePage page = h.getPage();
		checkPage(page, 15);

		assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
		assertEquals("foo:bar", h.getComponent().getPageRelativePath());

		PageParameters p = page.getPageParameters();
		assertEquals(0, p.getIndexedCount());

		assertEquals(0, p.getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	public void decode7()
	{
		Url url = Url.parse("some/mount/path?param1=value1&15-ILinkListener.4-foo-bar");
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
		Url url = Url.parse("some/mmount/path?15-ILinkListener.4-foo-bar");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertNull(handler);
	}

	/**
	 *
	 */
	@Test
	public void decode9()
	{
		// capture the home page
		Url url = Url.parse("");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof RenderPageRequestHandler);
	}

	/**
	 *
	 */
	@Test
	public void decode10()
	{
		Url url = Url.parse("some/mount/path?15-5.ILinkListener.4-foo-bar");
		context.setNextPageRenderCount(5);
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof ListenerInterfaceRequestHandler);

		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

		IRequestablePage page = h.getPage();
		assertEquals(5, page.getRenderCount());
	}

	/**
	 *
	 */
	@Test(expected = StalePageException.class)
	public void decode11()
	{
		Url url = Url.parse("some/mount/path?15-5.ILinkListener.4-foo-bar");
		context.setNextPageRenderCount(7);

		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		((IPageRequestHandler)handler).getPage();
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
		assertEquals("some/mount/path", url.toString());
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
		assertEquals("some/mount/path/i1/i2?a=b&b=c", url.toString());
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

		assertEquals("some/mount/path/i1/i2?a=b&b=c", url.toString());
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

		assertEquals("some/mount/path/i1/i2?15&a=b&b=c", url.toString());
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

		// mounted pages must render mounted url even for page that has not been created by
		// bookmarkable
		// URL

		assertEquals("some/mount/path/i1/i2?15&a=b&b=c", url.toString());
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
		page.setRenderCount(4);

		// shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE);

		Url url = encoder.mapHandler(handler);

		assertEquals("some/mount/path/i1/i2?15-4.ILinkListener-foo-bar&a=b&b=c", url.toString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4014
	 *
	 * The difference with testEncode7 is that here ListenerInterfaceRequestHandler is used instead
	 * of BookmarkableListenerInterfaceRequestHandler
	 */
	@Test
	public void encode6_1()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b");
		page.getPageParameters().set("b", "c");

		// WICKET-4038
		page.getPageParameters().add(WebRequest.PARAM_AJAX, "true");
		page.getPageParameters().add(WebRequest.PARAM_AJAX_BASE_URL, "some/base/url");

		page.setRenderCount(4);

		// shouldn't make any difference for ListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new ListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE);

		Url url = encoder.mapHandler(handler);

		assertEquals("some/mount/path/i1/i2?15-4.ILinkListener-foo-bar&a=b&b=c", url.toString());
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
		page.setRenderCount(5);

		// shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE, 4);

		Url url = encoder.mapHandler(handler);

		assertEquals("some/mount/path/i1/i2?15-5.ILinkListener.4-foo-bar&a=b&b=c", url.toString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4014
	 *
	 * The difference with testEncode7 is that here ListenerInterfaceRequestHandler is used instead
	 * of BookmarkableListenerInterfaceRequestHandler
	 */
	@Test
	public void encode7_1()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b");
		page.getPageParameters().set("b", "c");
		page.setRenderCount(5);

		// shouldn't make any difference for ListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new ListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE, 4);

		Url url = encoder.mapHandler(handler);

		assertEquals("some/mount/path/i1/i2?15-5.ILinkListener.4-foo-bar&a=b&b=c", url.toString());
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

		assertEquals("some/mount/path", url.toString());
	}

	/**
	 *
	 */
	@Test(expected = IllegalArgumentException.class)
	public void construct1()
	{
		IRequestMapper e = new MountedMapper("", MockPage.class);
	}

	/**
	 * Overriding mounting on '/' (see HomePageMapper) should be possible
	 */
	@Test
	public void construct2()
	{
		IRequestMapper homePageMapper = new MountedMapper("/", MockPage.class);
		assertNotNull(homePageMapper);
	}

	/**
	 *
	 */
	@Test
	public void placeholderDecode1()
	{
		Url url = Url.parse("some/p1/path/p2");
		IRequestHandler handler = placeholderEncoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().size() == 2);
		assertEquals("p1", page.getPageParameters().get("param1").toString());
		assertEquals("p2", page.getPageParameters().get("param2").toString());
	}

	/**
	 *
	 */
	@Test
	public void placeholderDecode2()
	{
		Url url = Url.parse("some/p1/path/p2/indexed1?a=b&b=c");
		IRequestHandler handler = placeholderEncoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		PageParameters p = page.getPageParameters();
		assertEquals(1, p.getIndexedCount());
		assertEquals("indexed1", p.get(0).toString());

		assertEquals(4, p.getNamedKeys().size());
		assertEquals("b", p.get("a").toString());
		assertEquals("c", p.get("b").toString());
		assertEquals("p1", page.getPageParameters().get("param1").toString());
		assertEquals("p2", page.getPageParameters().get("param2").toString());
	}

	/**	 */
	@Test
	public void placeholderDecodeWithIndexedParameters()
	{
		Url url = Url.parse("some/p1/path/p2/p3/p4");
		IRequestHandler handler = placeholderEncoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(2, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().size() == 2);
		assertEquals("p1", page.getPageParameters().get("param1").toString());
		assertEquals("p2", page.getPageParameters().get("param2").toString());
		assertEquals("p3", page.getPageParameters().get(0).toString());
		assertEquals("p4", page.getPageParameters().get(1).toString());
	}

	/**
	 *
	 */
	@Test
	public void placeholderEncode2()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b");
		parameters.set("b", "c");
		parameters.set("param1", "p1");
		parameters.set("param2", "p2");


		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = placeholderEncoder.mapHandler(handler);
		assertEquals("some/p1/path/p2/i1/i2?a=b&b=c", url.toString());
	}

	/**
	 * Test Url creation with {@link RenderPageRequestHandler}. Cheat that the page instance is not
	 * new, this way the produced Url has version '1' in the page info parameter
	 */
	@Test
	public void placeholderEncode3()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b");
		parameters.set("b", "c");
		parameters.set("param1", "p1");
		parameters.set("param2", "p2");

		PageProvider provider = new PageProvider(MockPage.class, parameters)
		{
			@Override
			public boolean isNewPageInstance()
			{
				return false;
			}
		};
		provider.setPageSource(context);
		IRequestHandler handler = new RenderPageRequestHandler(provider);
		Url url = placeholderEncoder.mapHandler(handler);
		assertEquals("some/p1/path/p2/i1/i2?1&a=b&b=c", url.toString());
	}

	/** */
	@Test
	public void optionPlaceholderDecode1()
	{
		Url url = Url.parse("some/p1/path/p2/p3");
		IRequestHandler handler = optionPlaceholderEncoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().size() == 3);
		assertEquals("p1", page.getPageParameters().get("param1").toString());
		assertEquals("p2", page.getPageParameters().get("param2").toString());
		assertEquals("p3", page.getPageParameters().get("param3").toString());
	}

	/** */
	@Test
	public void optionPlaceholderDecodeEagerMatchParameters()
	{
		Url url = Url.parse("some/path/path/path");
		IRequestHandler handler = optionPlaceholderEncoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().size() == 2);
		assertEquals("path", page.getPageParameters().get("param1").toString());
		assertEquals("path", page.getPageParameters().get("param2").toString());
		assertFalse("param3 should not be set",
			page.getPageParameters().getNamedKeys().contains("param3"));
	}

	/** */
	@Test
	public void optionPlaceholderDecode2()
	{
		Url url = Url.parse("some/p1/path/p2");
		IRequestHandler handler = optionPlaceholderEncoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().size() == 2);
		assertEquals("p1", page.getPageParameters().get("param1").toString());
		assertEquals("p2", page.getPageParameters().get("param2").toString());
		assertFalse("param3 should not be set",
			page.getPageParameters().getNamedKeys().contains("param3"));
	}

	/** */
	@Test
	public void optionPlaceholderDecode3()
	{
		Url url = Url.parse("some/path/p2");
		IRequestHandler handler = optionPlaceholderEncoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().size() == 1);
		assertFalse("param1 should not be set",
			page.getPageParameters().getNamedKeys().contains("param1"));
		assertEquals("p2", page.getPageParameters().get("param2").toString());
		assertFalse("param3 should not be set",
			page.getPageParameters().getNamedKeys().contains("param3"));
	}

	/** */
	@Test
	public void optionPlaceholderDecodeWithIndexParams()
	{
		Url url = Url.parse("some/path/p2/p3/p4");
		IRequestHandler handler = optionPlaceholderEncoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(1, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().size() == 2);
		assertFalse("param1 should not be set",
			page.getPageParameters().getNamedKeys().contains("param1"));
		assertEquals("p2", page.getPageParameters().get("param2").toString());
		assertEquals("p3", page.getPageParameters().get("param3").toString());
		assertEquals("p4", page.getPageParameters().get(0).toString());
	}

	/** */
	@Test
	public void optionPlaceholderEncode1()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b");
		parameters.set("b", "c");
		parameters.set("param1", "p1");
		parameters.set("param2", "p2");


		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = optionPlaceholderEncoder.mapHandler(handler);
		assertEquals("some/p1/path/p2/i1/i2?a=b&b=c", url.toString());
	}

	/** */
	@Test
	public void optionPlaceholderEncode2()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b");
		parameters.set("b", "c");
		parameters.set("param2", "p2");
		parameters.set("param3", "p3");


		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = optionPlaceholderEncoder.mapHandler(handler);
		assertEquals("some/path/p2/p3/i1/i2?a=b&b=c", url.toString());
	}
}
