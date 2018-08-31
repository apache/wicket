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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.Test;

/**
 * @author Matej Knopp
 */
class MountedMapperTest extends AbstractMapperTest
{

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
	 * Construct.
	 */
	MountedMapperTest()
	{
	}

	/**
	 *
	 */
	@Test
	void decode1()
	{
		Url url = Url.parse("some/mount/path");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().isEmpty());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4994
	 */
	@Test
	void decode1CaseInsensitively()
	{
		Url url = Url.parse("somE/moUnt/paTh");
		IRequestHandler handler = encoder.setCaseSensitiveMatch(false).mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().isEmpty());
	}

	/**
	 *
	 */
	@Test
	void decode2()
	{
		Url url = Url.parse("some/mount/path/indexed1?a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		PageParameters p = page.getPageParameters();
		assertEquals(1, p.getIndexedCount());
		assertEquals("indexed1", p.get(0).toString());

		assertEquals(2, p.getNamedKeys().size());
		assertEquals("b", p.get("a").toString());
		assertEquals("c", p.get("b").toString());
	}

	/**
	 * The tests resolves pages by id as if they were existed in the page store. These pages have no
	 * page parameters (i.e. page.getPageParameters() == null).
	 *
	 * The request that the encoder does also has no parameters (neither in the path nor in the
	 * query string) so the resolved page is assumed to be valid.
	 */
	@Test
	void decode3()
	{
		Url url = Url.parse("some/mount/path?15");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		checkPage(page, 15);
	}

	/**
	 * The tests resolves pages by id as if they were existed in the page store. These pages have no
	 * page parameters (i.e. page.getPageParameters() == null).
	 *
	 * Since Wicket 7.0.0 (WICKET-4441) if a new request to hybrid url (a url with both mount path
	 * and pageId) has different page parameters than the resolved page then a new page instance
	 * with the new parameters is created. This way if a user manipulates manually the product id in
	 * url like: /products/23?3 to /products/24?3 then Wicket will create a new page that will show
	 * product=24
	 */
	@Test
	void decode4()
	{
		Url url = Url.parse("some/mount/path/i1/i2?15&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		RenderPageRequestHandler h = (RenderPageRequestHandler)handler;
		((PageProvider)h.getPageProvider()).setPageSource(context);
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
		Url url = Url.parse("some/mount/path?15--foo-bar");
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
		Url url = Url.parse("some/mount/path/i1/i2?15--foo-bar&a=b&b=c");
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
		Url url = Url.parse("some/mount/path?param1=value1&15-.4-foo-bar");
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
		Url url = Url.parse("some/mmount/path?15-.4-foo-bar");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertNull(handler);
	}

	/**
	 *
	 */
	@Test
	void decode9()
	{
		// capture the home page
		Url url = Url.parse("");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
	}

	/**
	 *
	 */
	@Test
	void decode10()
	{
		Url url = Url.parse("some/mount/path?15-5.4-foo-bar");
		context.setNextPageRenderCount(5);
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(ListenerRequestHandler.class);

		ListenerRequestHandler h = (ListenerRequestHandler)handler;

		IRequestablePage page = h.getPage();
		assertEquals(5, page.getRenderCount());
	}

	/**
	 *
	 */
	@Test
	void decode11()
	{
		assertThrows(StalePageException.class, () -> {
			Url url = Url.parse("some/mount/path?15-5.4-foo-bar");
			context.setNextPageRenderCount(7);

			IRequestHandler handler = encoder.mapRequest(getRequest(url));

			((IPageRequestHandler)handler).getPage();
		});
	}

	/**
	 * 
	 */
	@Test
	void decode12()
	{
		Url url = Url.parse("some/mount/path/i1/i2?-1.-foo-bar&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(ListenerRequestHandler.class);

		ListenerRequestHandler h = (ListenerRequestHandler)handler;
		IRequestablePage page = h.getPage();
		checkPage(page, 1);

		assertEquals("foo:bar", h.getComponent().getPageRelativePath());
		assertNull(h.getBehaviorIndex());

		PageParameters p = page.getPageParameters();
		assertEquals(2, p.getIndexedCount());

		assertEquals(2, p.getNamedKeys().size());
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
		assertEquals("some/mount/path", url.toString());
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
		assertEquals("some/mount/path/i1/i2?a=b&b=c", url.toString());
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

		assertEquals("some/mount/path/i1/i2?a=b&b=c", url.toString());
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

		assertEquals("some/mount/path/i1/i2?15&a=b&b=c", url.toString());
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

		// mounted pages must render mounted url even for page that has not been
		// created by
		// bookmarkable
		// URL

		assertEquals("some/mount/path/i1/i2?15&a=b&b=c", url.toString());
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
		page.setRenderCount(4);

		// shouldn't make any difference for BookmarkableListenerRequestHandler,
		// as this explicitly says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerRequestHandler(provider);

		Url url = encoder.mapHandler(handler);

		assertEquals("some/mount/path/i1/i2?15-4.-foo-bar&a=b&b=c", url.toString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4014
	 * 
	 * The difference with testEncode7 is that here ListenerRequestHandler is used instead of
	 * BookmarkableListenerRequestHandler
	 */
	@Test
	void encode6_1()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING);
		page.getPageParameters().set("b", "c", INamedParameters.Type.QUERY_STRING);

		// WICKET-4038
		page.getPageParameters().add(WebRequest.PARAM_AJAX, "true",
			INamedParameters.Type.QUERY_STRING);
		page.getPageParameters().add(WebRequest.PARAM_AJAX_BASE_URL, "some/base/url",
			INamedParameters.Type.QUERY_STRING);

		page.setRenderCount(4);

		// shouldn't make any difference for ListenerRequestHandler,
		// as this explicitly says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new ListenerRequestHandler(provider);

		Url url = encoder.mapHandler(handler);

		assertEquals("some/mount/path/i1/i2?15-4.-foo-bar&a=b&b=c", url.toString());
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
		page.setRenderCount(5);

		// shouldn't make any difference for BookmarkableListenerRequestHandler,
		// as this explicitly says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerRequestHandler(provider, 4);

		Url url = encoder.mapHandler(handler);

		assertEquals("some/mount/path/i1/i2?15-5.4-foo-bar&a=b&b=c", url.toString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4014
	 * 
	 * The difference with testEncode7 is that here ListenerRequestHandler is used instead of
	 * BookmarkableListenerRequestHandler
	 */
	@Test
	void encode7_1()
	{
		MockPage page = new MockPage(15);
		page.getPageParameters().set(0, "i1");
		page.getPageParameters().set(1, "i2");
		page.getPageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING);
		page.getPageParameters().set("b", "c", INamedParameters.Type.QUERY_STRING);
		page.setRenderCount(5);

		// shouldn't make any difference for ListenerRequestHandler,
		// as this explicitly says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new ListenerRequestHandler(provider, 4);

		Url url = encoder.mapHandler(handler);

		assertEquals("some/mount/path/i1/i2?15-5.4-foo-bar&a=b&b=c", url.toString());
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

		assertEquals("some/mount/path", url.toString());
	}

	/**
	 *
	 */
	@Test
	void construct1()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			IRequestMapper e = new MountedMapper("", MockPage.class);
		});
	}

	/**
	 * Overriding mounting on '/' (see HomePageMapper) should be possible
	 */
	@Test
	void construct2()
	{
		IRequestMapper homePageMapper = new MountedMapper("/", MockPage.class);
		assertNotNull(homePageMapper);
	}

	/**
	 *
	 */
	@Test
	void placeholderDecode1()
	{
		Url url = Url.parse("some/p1/path/p2");
		IRequestHandler handler = placeholderEncoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
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
	void placeholderDecode2()
	{
		Url url = Url.parse("some/p1/path/p2/indexed1?a=b&b=c");
		IRequestHandler handler = placeholderEncoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
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
	void placeholderDecodeWithIndexedParameters()
	{
		Url url = Url.parse("some/p1/path/p2/p3/p4");
		IRequestHandler handler = placeholderEncoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
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
	void placeholderEncode2()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b", INamedParameters.Type.QUERY_STRING);
		parameters.set("b", "c", INamedParameters.Type.QUERY_STRING);
		parameters.set("param1", "p1", INamedParameters.Type.PATH);
		parameters.set("param2", "p2", INamedParameters.Type.PATH);


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
	void placeholderEncode3()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b", INamedParameters.Type.QUERY_STRING);
		parameters.set("b", "c", INamedParameters.Type.QUERY_STRING);
		parameters.set("param1", "p1", INamedParameters.Type.PATH);
		parameters.set("param2", "p2", INamedParameters.Type.PATH);

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

	/**
	 * WICKET-5247 page instantiated without required parameters won't be mapped
	 */
	@Test
	void placeholderEncode4()
	{
		PageProvider provider = new PageProvider(MockPage.class)
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
		assertNull(url);
	}

	/** */
	@Test
	void optionPlaceholderDecode1()
	{
		Url url = Url.parse("some/p1/path/p2/p3");
		IRequestHandler handler = optionPlaceholderEncoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertEquals(3, page.getPageParameters().getNamedKeys().size());
		assertEquals("p1", page.getPageParameters().get("param1").toString());
		assertEquals("p2", page.getPageParameters().get("param2").toString());
		assertEquals("p3", page.getPageParameters().get("param3").toString());
	}

	/** */
	@Test
	void optionPlaceholderDecodeEagerMatchParameters()
	{
		Url url = Url.parse("some/path/path/path");
		IRequestHandler handler = optionPlaceholderEncoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().size() == 2);
		assertEquals("path", page.getPageParameters().get("param1").toString());
		assertEquals("path", page.getPageParameters().get("param2").toString());
		assertFalse(page.getPageParameters().getNamedKeys().contains("param3"),
			"param3 should not be set");
	}

	/** */
	@Test
	void optionPlaceholderDecode2()
	{
		Url url = Url.parse("some/p1/path/p2");
		IRequestHandler handler = optionPlaceholderEncoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().size() == 2);
		assertEquals("p1", page.getPageParameters().get("param1").toString());
		assertEquals("p2", page.getPageParameters().get("param2").toString());
		assertFalse(page.getPageParameters().getNamedKeys().contains("param3"));
	}

	/** */
	@Test
	void optionPlaceholderDecode3()
	{
		Url url = Url.parse("some/path/p2");
		IRequestHandler handler = optionPlaceholderEncoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().size() == 1);
		assertFalse(page.getPageParameters().getNamedKeys().contains("param1"),
			"param1 should not be set");
		assertEquals("p2", page.getPageParameters().get("param2").toString());
		assertFalse(page.getPageParameters().getNamedKeys().contains("param3"),
			"param3 should not be set");
	}

	/** */
	@Test
	void optionPlaceholderDecodeWithIndexParams()
	{
		Url url = Url.parse("some/path/p2/p3/p4");
		IRequestHandler handler = optionPlaceholderEncoder.mapRequest(getRequest(url));

		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(1, page.getPageParameters().getIndexedCount());
		assertEquals(2, page.getPageParameters().getNamedKeys().size());
		assertFalse(page.getPageParameters().getNamedKeys().contains("param1"),
			"param1 should not be set");
		assertEquals("p2", page.getPageParameters().get("param2").toString());
		assertEquals("p3", page.getPageParameters().get("param3").toString());
		assertEquals("p4", page.getPageParameters().get(0).toString());
	}

	/** */
	@Test
	void optionPlaceholderEncode1()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b", INamedParameters.Type.QUERY_STRING);
		parameters.set("b", "c", INamedParameters.Type.QUERY_STRING);
		parameters.set("param1", "p1", INamedParameters.Type.PATH);
		parameters.set("param2", "p2", INamedParameters.Type.PATH);


		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = optionPlaceholderEncoder.mapHandler(handler);
		assertEquals("some/p1/path/p2/i1/i2?a=b&b=c", url.toString());
	}

	/** */
	@Test
	void optionPlaceholderEncode2()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "i1");
		parameters.set(1, "i2");
		parameters.set("a", "b", INamedParameters.Type.QUERY_STRING);
		parameters.set("b", "c", INamedParameters.Type.QUERY_STRING);
		parameters.set("param2", "p2", INamedParameters.Type.PATH);
		parameters.set("param3", "p3", INamedParameters.Type.PATH);


		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = optionPlaceholderEncoder.mapHandler(handler);
		assertEquals("some/path/p2/p3/i1/i2?a=b&b=c", url.toString());
	}

	/**
	 * WICKET-6461
	 */
	@Test
	void optionalPlaceholdersBeforeRequiredPlaceholder() throws Exception
	{
		final MountedMapper mapper = new MountedMapper(
			"/params/#{optional1}/#{optional2}/${required}", MockPage.class)
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

		IRequestHandler handler = mapper.mapRequest(getRequest(Url.parse("params/required")));
		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		PageParameters p = page.getPageParameters();
		assertEquals(1, p.getNamedKeys().size());
		assertEquals("required", p.get("required").toString());

		handler = mapper.mapRequest(getRequest(Url.parse("params/optional1/required")));
		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		page = ((RenderPageRequestHandler)handler).getPage();
		p = page.getPageParameters();
		assertEquals(2, p.getNamedKeys().size());
		assertEquals("required", p.get("required").toString());
		assertEquals("optional1", p.get("optional1").toString());

		handler = mapper.mapRequest(getRequest(Url.parse("params/optional1/optional2/required")));
		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);
		page = ((RenderPageRequestHandler)handler).getPage();
		p = page.getPageParameters();
		assertEquals(3, p.getNamedKeys().size());
		assertEquals("required", p.get("required").toString());
		assertEquals("optional1", p.get("optional1").toString());
		assertEquals("optional2", p.get("optional2").toString());
	}

	/**
	 * WICKET-5056
	 */
	@Test
	void optionalParameterGetsLowerScore_ThanExactOne() throws Exception
	{
		final Url url = Url.parse("all/sindex");
		final MountedMapper exactMount = new MountedMapper("/all/sindex", MockPage.class);
		final MountedMapper optionalParameter = new MountedMapper("/all/#{exp}", MockPage.class);
		Request request = getRequest(url);
		final int exactCompatScore = exactMount.getCompatibilityScore(request);
		final int optCompatScore = optionalParameter.getCompatibilityScore(request);
		assertTrue(exactCompatScore > optCompatScore,
			"exactCompatScore should have greater compatibility score than optional one" +
				" got exact = " + exactCompatScore + " and optional = " + optCompatScore);
	}

	@Test
	void exactMountGetsBetterScore_ThanParameterOne() throws Exception
	{
		final Url url = Url.parse("all/sindex");
		final MountedMapper exactMount = new MountedMapper("/all/sindex", MockPage.class);
		final MountedMapper requiredParam = new MountedMapper("/all/${exp}", MockPage.class);
		Request request = getRequest(url);
		final int exactCompatScore = exactMount.getCompatibilityScore(request);
		final int requiredParamScore = requiredParam.getCompatibilityScore(request);
		assertTrue(exactCompatScore > requiredParamScore,
			"exactCompatScore should have greater compatibility score than required one" +
				" got exact = " + exactCompatScore + " and required= " + requiredParamScore);
	}

	@Test
	void exactMountGetsBetterScore_ThanParameterOne_ThenOptionalOne() throws Exception
	{
		final Url url = Url.parse("all/sindex");
		final MountedMapper exactMount = new MountedMapper("/all/sindex", MockPage.class);
		final MountedMapper requiredParam = new MountedMapper("/all/${exp}", MockPage.class);
		final MountedMapper optionalParameter = new MountedMapper("/all/#{exp}", MockPage.class);
		final MountedMapper requiredOptionalParam = new MountedMapper("/all/${exp}/#{opt}",
			MockPage.class);

		Request request = getRequest(url);
		final int exactCompatScore = exactMount.getCompatibilityScore(request);
		final int requiredParamScore = requiredParam.getCompatibilityScore(request);
		final int optCompatScore = optionalParameter.getCompatibilityScore(request);
		final int requiredOptCompatScore = requiredOptionalParam.getCompatibilityScore(request);

		// all the mappers above must be eligible for the give URL (i.e. their score must be > 0)
		assertTrue(exactCompatScore > 0,
			"exactMount mapper must be eligible to handle the request");
		assertTrue(requiredParamScore > 0,
			"requiredParam mapper must be eligible to handle the request");
		assertTrue(optCompatScore > 0,
			"optionalParameter mapper must be eligible to handle the request");
		assertTrue(requiredOptCompatScore > 0,
			"requiredOptionalParam mapper must be eligible to handle the request");

		assertTrue(exactCompatScore > requiredParamScore,
			"exactCompatScore should have greater compatibility score than required one" +
				" got exact = " + exactCompatScore + " and required= " + requiredParamScore);

		assertTrue(exactCompatScore > requiredOptCompatScore,
			"exactCompatScore should have greater compatibility score than required+optional one" +
				" got exact = " + exactCompatScore + " and requiredOptional= " +
				requiredOptCompatScore);

		assertTrue(requiredParamScore > optCompatScore,
			"exactCompatScore should have greater compatibility score than optional one" +
				" got exact = " + exactCompatScore + " and optional = " + optCompatScore);
	}
}
