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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.wicket.MockPage;
import org.apache.wicket.core.request.handler.BookmarkableListenerRequestHandler;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.Test;

/**
 * @author Matej Knopp
 */
class BookmarkableMapperTest extends AbstractMapperTest
{

	private final BookmarkableMapper encoder = new BookmarkableMapper()
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

	private static final String PAGE_CLASS_NAME = MockPage.class.getName();

	/**
	 *
	 */
	@Test
	void decode1()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME);
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		assertEquals(PAGE_CLASS_NAME, page.getClass().getName());
		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().isEmpty());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5500
	 */
	@Test
	void decodePageClassWithPathParameters()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + ";something=else");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		assertEquals(PAGE_CLASS_NAME, page.getClass().getName());
		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().isEmpty());
	}

	/**
	 *
	 */
	@Test
	void decode2()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/indexed1?a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		assertEquals(PAGE_CLASS_NAME, page.getClass().getName());

		PageParameters p = page.getPageParameters();
		assertEquals(1, p.getIndexedCount());
		assertEquals("indexed1", p.get(0).toString());

		assertEquals(2, p.getNamedKeys().size());
		assertEquals("b", p.get("a").toString());
		assertEquals("c", p.get("b").toString());
	}

	/**
	 * The tests resolves pages by id as if they were existed in the page store.
	 * These pages have no page parameters (i.e. page.getPageParameters() == null).
	 *
	 * The request that the encoder does also has no parameters (neither in the path
	 * nor in the query string) so the resolved page is assumed to be valid.
	 */
	@Test
	void decode3()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "?15");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		checkPage(page, 15);
	}

	/**
	 * The tests resolves pages by id as if they were existed in the page store.
	 * These pages have no page parameters (i.e. page.getPageParameters() == null).
	 *
	 * Since Wicket 7.0.0 (WICKET-4441) if a new request to hybrid url
	 * (a url with both mount path and pageId) has different page parameters
	 * than the resolved page then a new page instance with the new parameters
	 * is created.
	 * This way if a user manipulates manually the product id in url like:
	 * /wicket/bookmarkable/my.Product?3&id=23
	 * to
	 * /wicket/bookmarkable/my.Product?3&id=24
	 * then Wicket will create a new page that will show product=24
	 */
	@Test
	void decode4()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?15&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		RenderPageRequestHandler h = (RenderPageRequestHandler) handler;
		((PageProvider) h.getPageProvider()).setPageSource(context);
		IRequestablePage page = h.getPage();
		checkPage(page, 1);

		PageParameters p = page.getPageParameters();
		assertEquals(2, p.getIndexedCount());

		assertEquals(2, p.getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	void decode5()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "?15--foo-bar");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(ListenerRequestHandler.class);

		ListenerRequestHandler h = (ListenerRequestHandler)handler;

		IRequestablePage page = h.getPage();
		checkPage(page, 15);

		assertEquals("foo:bar", h.getComponent().getPageRelativePath());
		assertNull(h.getBehaviorIndex());
	}

	/**
	 *
	 */
	@Test
	void decode6()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME
			+ "/i1/i2?15--foo-bar&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(ListenerRequestHandler.class);
		ListenerRequestHandler h = (ListenerRequestHandler)handler;

		IRequestablePage page = h.getPage();
		checkPage(page, 15);

		assertEquals("foo:bar", h.getComponent().getPageRelativePath());

		PageParameters p = page.getPageParameters();
		assertEquals(0, p.getIndexedCount());

		assertEquals(0, p.getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	void decode7()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME
			+ "?15-ILinkListener.4-foo-bar");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(ListenerRequestHandler.class);

		ListenerRequestHandler h = (ListenerRequestHandler)handler;

		IRequestablePage page = h.getPage();
		checkPage(page, 15);

		assertEquals("foo:bar", h.getComponent().getPageRelativePath());
		assertEquals((Object)4, h.getBehaviorIndex());
	}

	/**
	 *
	 */
	@Test
	void decode8()
	{
		Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME
			+ "/i1/i2?15-5.ILinkListener-foo-bar&a=b&b=c");

		context.setNextPageRenderCount(5);

		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(ListenerRequestHandler.class);
		ListenerRequestHandler h = (ListenerRequestHandler)handler;

		IRequestablePage page = h.getPage();
		assertEquals(page.getRenderCount(), 5);
	}

	/**
	 *
	 */
	@Test
	void decode9()
	{
		assertThrows(StalePageException.class, () -> {
			Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME
										+ "/i1/i2?15-5.ILinkListener-foo-bar&a=b&b=c");

			context.setNextPageRenderCount(6);
			IRequestHandler handler = encoder.mapRequest(getRequest(url));
			((IPageRequestHandler)handler).getPage();
		});
	}

	/**
	 * WICKET-2993
	 */
	@Test
	void decode10()
	{
		// use String.class but any other non-Page will do the job as well
		Url url = Url.parse("wicket/bookmarkable/" + String.class.getName());

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler, "A non-page class should not create a request handler!");
	}

	/**
	 * WICKET-5071
	 * 
	 * Decodes a request to
	 * {@link org.apache.wicket.core.request.mapper.IMapperContext#getBookmarkableIdentifier()}
	 * /com.example.MyPage when the current base url is
	 * {@link org.apache.wicket.core.request.mapper.IMapperContext#getNamespace()} /
	 * {@link org.apache.wicket.core.request.mapper.IMapperContext#getBookmarkableIdentifier()}
	 */
	@Test
	void decode11()
	{
		final Url url = Url.parse(context.getBookmarkableIdentifier() + "/" + PAGE_CLASS_NAME);

		Request request = new Request()
		{
			@Override
			public Url getUrl()
			{
				return url;
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}

			@Override
			public Charset getCharset()
			{
				return Charset.forName("UTF-8");
			}

			@Override
			public Url getClientUrl()
			{
				StringBuilder url = new StringBuilder();
				url.append(context.getNamespace()).append('/')
					.append(context.getBookmarkableIdentifier()).append('/')
					.append("com.example.MyPage");
				return Url.parse(url.toString());
			}

			@Override
			public Object getContainerRequest()
			{
				return null;
			}
		};

		IRequestHandler handler = encoder.mapRequest(request);
		assertNotNull(handler, "A handler should be resolved for relative url to a bookmarkable page!");

		IRequestablePage page = ((IPageRequestHandler)handler).getPage();
		assertEquals(page.getClass().getName(), PAGE_CLASS_NAME);
	}

	/**
	 * WICKET-5071
	 * 
	 * Decodes a request to
	 * {@link org.apache.wicket.core.request.mapper.IMapperContext#getBookmarkableIdentifier()}
	 * /com.example.MyPage when the current base url is
	 * {@link org.apache.wicket.core.request.mapper.IMapperContext#getNamespace()} /
	 * {@link org.apache.wicket.core.request.mapper.IMapperContext#getPageIdentifier()}
	 */
	@Test
	void decode12()
	{
		final Url url = Url.parse(context.getBookmarkableIdentifier() + "/" + PAGE_CLASS_NAME);

		Request request = new Request()
		{
			@Override
			public Url getUrl()
			{
				return url;
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}

			@Override
			public Charset getCharset()
			{
				return Charset.forName("UTF-8");
			}

			@Override
			public Url getClientUrl()
			{
				StringBuilder url = new StringBuilder();
				url.append(context.getNamespace()).append('/').append(context.getPageIdentifier())
					.append("?3");
				return Url.parse(url.toString());
			}

			@Override
			public Object getContainerRequest()
			{
				return null;
			}
		};

		IRequestHandler handler = encoder.mapRequest(request);
		assertNotNull(handler, "A handler should be resolved for relative url to a page instance url!");

		IRequestablePage page = ((IPageRequestHandler)handler).getPage();
		assertEquals(page.getClass().getName(), PAGE_CLASS_NAME);
	}

	/**
	 * WICKET-5071
	 * 
	 * Decodes a request to
	 * {@link org.apache.wicket.core.request.mapper.IMapperContext#getBookmarkableIdentifier()}
	 * /com.example.MyPage when the current base url is
	 * {@link org.apache.wicket.core.request.mapper.IMapperContext#getNamespace()} /
	 * {@link org.apache.wicket.core.request.mapper.IMapperContext#getPageIdentifier()}
	 */
	@Test
	void decode13()
	{
		final Url url = Url.parse(context.getBookmarkableIdentifier() + "/" + PAGE_CLASS_NAME);

		Request request = new Request()
		{
			@Override
			public Url getUrl()
			{
				return url;
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}

			@Override
			public Charset getCharset()
			{
				return Charset.forName("UTF-8");
			}

			@Override
			public Url getClientUrl()
			{
				StringBuilder url = new StringBuilder();
				url.append(context.getBookmarkableIdentifier()).append('/').append(PAGE_CLASS_NAME);
				return Url.parse(url.toString());
			}

			@Override
			public Object getContainerRequest()
			{
				return null;
			}
		};

		IRequestHandler handler = encoder.mapRequest(request);
		assertNotNull(handler, "A handler should be resolved for relative url to a bookmarkable page url!");

		IRequestablePage page = ((IPageRequestHandler)handler).getPage();
		assertEquals(page.getClass().getName(), PAGE_CLASS_NAME);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5673
	 */
	@Test
	void decode14()
	{
		Url url = Url.parse("wicket/bookmarkable/");
		int score = encoder.getCompatibilityScore(getRequest(url));

		assertEquals(0, score);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5996
	 */
	@Test
	void decode15()
	{
		Url url = Url.parse("wicket/bookmarkable");
		final AbstractBookmarkableMapper.UrlInfo urlInfo = encoder.parseRequest(getRequest(url));

		assertNull(urlInfo);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5996
	 */
	@Test
	void decode16()
	{
		Url url = Url.parse("wicket/bookmarkable/");
		final AbstractBookmarkableMapper.UrlInfo urlInfo = encoder.parseRequest(getRequest(url));

		assertNull(urlInfo);
	}

	/**
	 *
	 */
	@Test
	void encode1()
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
	@Test
	void encode2()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b", INamedParameters.Type.QUERY_STRING);
		parameters.set("b", "c", INamedParameters.Type.QUERY_STRING);
		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?a=b&b=c", url.toString());
	}

	/**
	 *
	 */
	@Test
	void encode3()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b", INamedParameters.Type.QUERY_STRING);
		parameters.set("b", "c", INamedParameters.Type.QUERY_STRING);

		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);

		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?a=b&b=c", url.toString());
	}

	/**
	 *
	 */
	@Test
	void encode4()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING);
		page.getPageParameters().set("b", "c", INamedParameters.Type.QUERY_STRING);
		page.setCreatedBookmarkable(true);

		IPageProvider provider = new PageProvider(page);
		IRequestHandler handler = new RenderPageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);

		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?15&a=b&b=c", url.toString());
	}

	/**
	 *
	 */
	@Test
	void encode5()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING);
		page.getPageParameters().set("b", "c", INamedParameters.Type.QUERY_STRING);

		page.setCreatedBookmarkable(false);

		IPageProvider provider = new PageProvider(page);
		IRequestHandler handler = new RenderPageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);

		// never allow bookmarkable render url for page that has not been
		// created by bookmarkable
		// URL

		assertNull(url);
	}

	/**
	 *
	 */
	@Test
	void encode6()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING);
		page.getPageParameters().set("b", "c", INamedParameters.Type.QUERY_STRING);

		// shouldn't make any difference for BookmarkableListenerRequestHandler,
		// as this explicitly says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerRequestHandler(provider);

		Url url = encoder.mapHandler(handler);

		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME
			+ "/i1/i2?15-0.-foo-bar&a=b&b=c", url.toString());
	}

	/**
	 *
	 */
	@Test
	void encode7()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING);
		page.getPageParameters().set("b", "c", INamedParameters.Type.QUERY_STRING);

		// shouldn't make any difference for BookmarkableListenerRequestHandler,
		// as this explicitly says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerRequestHandler(provider, 4);

		Url url = encoder.mapHandler(handler);

		assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME
			+ "/i1/i2?15-0.4-foo-bar&a=b&b=c", url.toString());
	}

	/**
	 *
	 */
	@Test
	void encode8()
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
