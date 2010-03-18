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
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.handler.IPageProvider;
import org.apache.wicket.request.handler.IPageRequestHandler;
import org.apache.wicket.request.handler.PageAndComponentProvider;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.impl.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.request.handler.impl.BookmarkablePageRequestHandler;
import org.apache.wicket.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache.wicket.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.request.mapper.BookmarkableMapper;
import org.apache.wicket.request.mapper.IMapperContext;
import org.apache.wicket.request.mapper.StalePageException;
import org.apache.wicket.request.mapper.parameters.PageParameters;

/**
 * @author Matej Knopp
 */
public class BookmarkableMapperTest extends AbstractMapperTest
{

	/**
	 * Construct.
	 */
	public BookmarkableMapperTest()
	{
	}

	private final BookmarkableMapper encoder = new BookmarkableMapper()
	{
		@Override
		protected IMapperContext getContext()
		{
			return context;
		}
	};

	private static final String PAGE_CLASS_NAME = MockPage.class.getName();

	/**
	 * 
	 */
	public void testDecode1()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME);
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		assertEquals(PAGE_CLASS_NAME, page.getClass().getName());
		assertEquals(0, page.getPageParameters().getIndexedParamsCount());
		assertTrue(page.getPageParameters().getNamedParameterKeys().isEmpty());
	}

	/**
	 * 
	 */
	public void testDecode2()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/indexed1?a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		assertEquals(PAGE_CLASS_NAME, page.getClass().getName());

		PageParameters p = page.getPageParameters();
		assertEquals(1, p.getIndexedParamsCount());
		assertEquals("indexed1", p.getIndexedParameter(0).toString());

		assertEquals(2, p.getNamedParameterKeys().size());
		assertEquals("b", p.getNamedParameter("a").toString());
		assertEquals("c", p.getNamedParameter("b").toString());
	}

	/**
	 * 
	 */
	public void testDecode3()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "?15");
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
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?15&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		checkPage(page, 15);

		PageParameters p = page.getPageParameters();
		assertEquals(2, p.getIndexedParamsCount());
		assertEquals("i1", p.getIndexedParameter(0).toString());
		assertEquals("i2", p.getIndexedParameter(1).toString());

		assertEquals(2, p.getNamedParameterKeys().size());
		assertEquals("b", p.getNamedParameter("a").toString());
		assertEquals("c", p.getNamedParameter("b").toString());
	}

	/**
	 * 
	 */
	public void testDecode5()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "?15-ILinkListener-foo-bar");
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
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME +
			"/i1/i2?15-ILinkListener-foo-bar&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof ListenerInterfaceRequestHandler);
		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

		IRequestablePage page = h.getPage();
		checkPage(page, 15);

		assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
		assertEquals("foo:bar", h.getComponent().getPageRelativePath());

		PageParameters p = page.getPageParameters();
		assertEquals(2, p.getIndexedParamsCount());
		assertEquals("i1", p.getIndexedParameter(0).toString());
		assertEquals("i2", p.getIndexedParameter(1).toString());

		assertEquals(2, p.getNamedParameterKeys().size());
		assertEquals("b", p.getNamedParameter("a").toString());
		assertEquals("c", p.getNamedParameter("b").toString());
	}

	/**
	 * 
	 */
	public void testDecode7()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME +
			"?15-ILinkListener.4-foo-bar");
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
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME +
			"/i1/i2?15-5.ILinkListener-foo-bar&a=b&b=c");

		context.setNextPageRenderCount(5);

		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof ListenerInterfaceRequestHandler);
		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

		IRequestablePage page = h.getPage();
		assertEquals(page.getRenderCount(), 5);
	}

	/**
	 * 
	 */
	public void testDecode9()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME +
			"/i1/i2?15-5.ILinkListener-foo-bar&a=b&b=c");

		context.setNextPageRenderCount(6);

		try
		{
			IRequestHandler handler = encoder.mapRequest(getRequest(url));

			((IPageRequestHandler)handler).getPage();

			// should never get here
			assertFalse(true);
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
		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME, url.toString());
	}

	/**
	 * 
	 */
	public void testEncode2()
	{
		PageParameters parameters = new PageParameters();
		parameters.setIndexedParameter(0, "i1");
		parameters.setIndexedParameter(1, "i2");
		parameters.setNamedParameter("a", "b");
		parameters.setNamedParameter("b", "c");
		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?a=b&b=c", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode3()
	{
		PageParameters parameters = new PageParameters();
		parameters.setIndexedParameter(0, "i1");
		parameters.setIndexedParameter(1, "i2");
		parameters.setNamedParameter("a", "b");
		parameters.setNamedParameter("b", "c");

		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);

		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?a=b&b=c", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode4()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().setIndexedParameter(0, "i1");
		page.getPageParameters().setIndexedParameter(1, "i2");
		page.getPageParameters().setNamedParameter("a", "b");
		page.getPageParameters().setNamedParameter("b", "c");
		page.setCreatedBookmarkable(true);

		IPageProvider provider = new PageProvider(page);
		IRequestHandler handler = new RenderPageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);

		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?15&a=b&b=c", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode5()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().setIndexedParameter(0, "i1");
		page.getPageParameters().setIndexedParameter(1, "i2");
		page.getPageParameters().setNamedParameter("a", "b");
		page.getPageParameters().setNamedParameter("b", "c");

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
	public void testEncode6()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().setIndexedParameter(0, "i1");
		page.getPageParameters().setIndexedParameter(1, "i2");
		page.getPageParameters().setNamedParameter("a", "b");
		page.getPageParameters().setNamedParameter("b", "c");

		// shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE);

		Url url = encoder.mapHandler(handler);

		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME +
			"/i1/i2?15-0.ILinkListener-foo-bar&a=b&b=c", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode7()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().setIndexedParameter(0, "i1");
		page.getPageParameters().setIndexedParameter(1, "i2");
		page.getPageParameters().setNamedParameter("a", "b");
		page.getPageParameters().setNamedParameter("b", "c");

		// shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE, 4);

		Url url = encoder.mapHandler(handler);

		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME +
			"/i1/i2?15-0.ILinkListener.4-foo-bar&a=b&b=c", url.toString());
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

		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME, url.toString());
	}
}
