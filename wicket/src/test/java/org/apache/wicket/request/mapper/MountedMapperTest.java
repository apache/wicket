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
package org.apache.wicket.request.mapper;

import org.apache.wicket.MockPage;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.handler.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.request.handler.IPageProvider;
import org.apache.wicket.request.handler.IPageRequestHandler;
import org.apache.wicket.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.request.handler.PageAndComponentProvider;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;

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
	};

	private final MountedMapper placeholderEncoder = new MountedMapper(
		"/some/${param1}/path/${param2}", MockPage.class)
	{
		@Override
		protected IMapperContext getContext()
		{
			return context;
		}
	};

	/**
	 * 
	 */
	public void testDecode1()
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
	public void testDecode2()
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
	public void testDecode3()
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
	public void testDecode4()
	{
		Url url = Url.parse("some/mount/path/i1/i2?15&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		checkPage(page, 15);

		PageParameters p = page.getPageParameters();
		assertEquals(2, p.getIndexedCount());
		assertEquals("i1", p.get(0).toString());
		assertEquals("i2", p.get(1).toString());

		assertEquals(2, p.getNamedKeys().size());
		assertEquals("b", p.get("a").toString());
		assertEquals("c", p.get("b").toString());
	}

	/**
	 * 
	 */
	public void testDecode5()
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
	public void testDecode6()
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
		assertEquals(2, p.getIndexedCount());
		assertEquals("i1", p.get(0).toString());
		assertEquals("i2", p.get(1).toString());

		assertEquals(2, p.getNamedKeys().size());
		assertEquals("b", p.get("a").toString());
		assertEquals("c", p.get("b").toString());
	}

	/**
	 * 
	 */
	public void testDecode7()
	{
		Url url = Url.parse("some/mount/path?15-ILinkListener.4-foo-bar");
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
	public void testDecode8()
	{
		Url url = Url.parse("some/mmount/path?15-ILinkListener.4-foo-bar");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertNull(handler);
	}

	/**
	 * 
	 */
	public void testDecode9()
	{
		// capture the home page
		Url url = Url.parse("");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof RenderPageRequestHandler);
	}

	/**
	 * 
	 */
	public void testDecode10()
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
	public void testDecode11()
	{
		Url url = Url.parse("some/mount/path?15-5.ILinkListener.4-foo-bar");
		context.setNextPageRenderCount(7);

		try
		{
			IRequestHandler handler = encoder.mapRequest(getRequest(url));

			((IPageRequestHandler)handler).getPage();

			// should never get here
			assertTrue(false);
		}
		catch (StalePageException e)
		{

		}
	}

	/**
	 * 
	 */
	public void testEncode1()
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
	public void testEncode2()
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
	public void testEncode3()
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
	public void testEncode4()
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
	public void testEncode5()
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
	public void testEncode6()
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
	 * 
	 */
	public void testEncode7()
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
	 * 
	 */
	public void testEncode8()
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
	public void testConstruct1()
	{
		try
		{
			IRequestMapper e = new MountedMapper("", MockPage.class);

			// should never get here
			assertFalse(true);
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}
	}

	/**
	 * Overriding mounting on '/' (see HomePageMapper) should be possible
	 */
	public void testConstruct2()
	{
		IRequestMapper homePageMapper = new MountedMapper("/", MockPage.class);
		assertNotNull(homePageMapper);
	}

	/**
	 *
	 */
	public void testPlaceholderDecode1()
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
	public void testPlaceholderDecode2()
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

	/**
	 * 
	 */
	public void testPlaceholderEncode2()
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
	public void testPlaceholderEncode3()
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
}
