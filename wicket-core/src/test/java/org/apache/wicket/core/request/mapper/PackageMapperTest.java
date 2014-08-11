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

import static org.hamcrest.CoreMatchers.instanceOf;

import org.apache.wicket.MockPage;
import org.apache.wicket.core.request.handler.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.PackageMapperTest.OuterPage.InnerPage;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.PackageName;
import org.junit.Test;

/**
 * Tests for {@link PackageMapper}
 */
public class PackageMapperTest extends AbstractMapperTest
{
	private static final String MOUNT_PATH = "mount/path";

	private static final String ALIAS = "alias";

	private final PackageMapper encoder = new PackageMapper(MOUNT_PATH, PackageName.forClass(MockPage.class))
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

	private static final String PAGE_CLASS_NAME = MockPage.class.getSimpleName();

	private final PackageMapper aliasEncoder = new PackageMapper(MOUNT_PATH,
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

	private final PackageMapper namedParametersEncoder = new PackageMapper(MOUNT_PATH + "/${foo}/${bar}", PackageName.forClass(MockPage.class))
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
	@Test
	public void decode1()
	{
		Url url = Url.parse(MOUNT_PATH + '/' + PAGE_CLASS_NAME);
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler, instanceOf(RenderPageRequestHandler.class));
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		assertEquals(PAGE_CLASS_NAME, page.getClass().getSimpleName());
		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().isEmpty());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4994
	 */
	@Test
	public void decode1CaseInsensitively()
	{
		Url url = Url.parse(MOUNT_PATH.replace('o', 'O').replace('p', 'P') + '/' + PAGE_CLASS_NAME);
		IRequestHandler handler = encoder.setCaseSensitiveMatch(false).mapRequest(getRequest(url));

		assertThat(handler, instanceOf(RenderPageRequestHandler.class));
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		assertEquals(PAGE_CLASS_NAME, page.getClass().getSimpleName());
		assertEquals(0, page.getPageParameters().getIndexedCount());
		assertTrue(page.getPageParameters().getNamedKeys().isEmpty());
	}
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5500
	 */
	@Test
	public void decodePageClassWithPathParameters()
	{
		Url url = Url.parse(MOUNT_PATH + '/' + PAGE_CLASS_NAME + ";something=else");
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
		Url url = Url.parse(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "/indexed1?a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler, instanceOf(RenderPageRequestHandler.class));
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
	 * The tests resolves pages by id as if they were existed in the page store.
	 * These pages have no page parameters (i.e. page.getPageParameters() == null).
	 *
	 * The request that the encoder does also has no parameters (neither in the path
	 * nor in the query string) so the resolved page is assumed to be valid.
	 */
	@Test
	public void decode3()
	{
		Url url = Url.parse(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "?15");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler, instanceOf(RenderPageRequestHandler.class));
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
	 * /mount/path/Products/23?3
	 * to
	 * /mount/path/Products/24?3
	 * then Wicket will create a new page that will show product=24
	 */
	@Test
	public void decode4()
	{
		Url url = Url.parse(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "/i1/i2?15&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler, instanceOf(RenderPageRequestHandler.class));
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
	public void decode5()
	{
		Url url = Url.parse(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "?15-ILinkListener-foo-bar");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler, instanceOf(ListenerInterfaceRequestHandler.class));

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
		Url url = Url.parse(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "/i1/i2?15-ILinkListener-foo-bar&a=b&b=c");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler, instanceOf(ListenerInterfaceRequestHandler.class));
		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

		IRequestablePage page = h.getPage();
		checkPage(page, 15);

		assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
		assertEquals("foo:bar", h.getComponent().getPageRelativePath());

		PageParameters p = h.getPageParameters();
		assertEquals(2, p.getIndexedCount());

		assertEquals(2, p.getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	public void decode7()
	{
		Url url = Url.parse(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "?15-ILinkListener.4-foo-bar");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler, instanceOf(ListenerInterfaceRequestHandler.class));

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
		Url url = Url.parse(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "/i1/i2?15-5.ILinkListener-foo-bar&a=b&b=c");

		context.setNextPageRenderCount(5);

		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThat(handler, instanceOf(ListenerInterfaceRequestHandler.class));
		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

		IRequestablePage page = h.getPage();
		assertEquals(page.getRenderCount(), 5);
	}

	/**
	 *
	 */
	@Test(expected = StalePageException.class)
	public void decode9()
	{
		Url url = Url.parse(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "/i1/i2?15-5.ILinkListener-foo-bar&a=b&b=c");

		context.setNextPageRenderCount(6);

		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		((IPageRequestHandler)handler).getPage();

	}

	/**
	 *
	 */
	@Test
	public void decodeNamedParameters()
	{
		Url url = Url.parse(MOUNT_PATH + "/fooValue/barValue/" + PAGE_CLASS_NAME + "/i1/i2?a=b&b=c");
		IRequestHandler handler = namedParametersEncoder.mapRequest(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		RenderPageRequestHandler h = (RenderPageRequestHandler)handler;

		IRequestablePage page = h.getPage();
		checkPage(page, 1);

		PageParameters p = h.getPageParameters();
		assertEquals(2, p.getIndexedCount());

		assertEquals(4, p.getNamedKeys().size());
		assertEquals("fooValue", p.get("foo").toString());
		assertEquals("barValue", p.get("bar").toString());

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
		assertEquals(MOUNT_PATH + '/' + PAGE_CLASS_NAME, url.toString());
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
		parameters.set("a", "b", INamedParameters.Type.QUERY_STRING);
		parameters.set("b", "c", INamedParameters.Type.QUERY_STRING);
		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);
		assertEquals(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "/i1/i2?a=b&b=c", url.toString());
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
		parameters.set("a", "b", INamedParameters.Type.QUERY_STRING);
		parameters.set("b", "c", INamedParameters.Type.QUERY_STRING);

		PageProvider provider = new PageProvider(MockPage.class, parameters);
		provider.setPageSource(context);
		IRequestHandler handler = new BookmarkablePageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);

		assertEquals(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "/i1/i2?a=b&b=c", url.toString());
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
		page.getPageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING);
		page.getPageParameters().set("b", "c", INamedParameters.Type.QUERY_STRING);
		page.setCreatedBookmarkable(true);

		IPageProvider provider = new PageProvider(page);
		IRequestHandler handler = new RenderPageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);

		assertEquals(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "/i1/i2?15&a=b&b=c", url.toString());
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
		page.getPageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING);
		page.getPageParameters().set("b", "c", INamedParameters.Type.QUERY_STRING);

		page.setCreatedBookmarkable(false);

		IPageProvider provider = new PageProvider(page);
		IRequestHandler handler = new RenderPageRequestHandler(provider);
		Url url = encoder.mapHandler(handler);

		assertEquals(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "/i1/i2?15&a=b&b=c", url.toString());
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
		page.getPageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING);
		page.getPageParameters().set("b", "c", INamedParameters.Type.QUERY_STRING);

		// shouldn't make any difference for
		// BookmarkableListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE);

		Url url = encoder.mapHandler(handler);

		assertEquals(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "/i1/i2?15-0.ILinkListener-foo-bar&a=b&b=c", url.toString());
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
		page.getPageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING);
		page.getPageParameters().set("b", "c", INamedParameters.Type.QUERY_STRING);

		// shouldn't make any difference for
		// BookmarkableListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IRequestableComponent c = page.get("foo:bar");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE, 4);

		Url url = encoder.mapHandler(handler);

		assertEquals(MOUNT_PATH + '/' + PAGE_CLASS_NAME + "/i1/i2?15-0.ILinkListener.4-foo-bar&a=b&b=c",
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

		assertEquals(MOUNT_PATH + '/' + PAGE_CLASS_NAME, url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encodeNamedPageParameters()
	{
		MockPage page = new MockPage(15);
		page.setBookmarkable(true);
		page.setCreatedBookmarkable(true);
		page.setPageStateless(true);

		IPageProvider provider = new PageProvider(page);
		page.getPageParameters().set("foo", "fooValue", INamedParameters.Type.PATH);
		page.getPageParameters().set("bar", "barValue", INamedParameters.Type.PATH);
		IRequestHandler handler = new RenderPageRequestHandler(provider);

		Url url = namedParametersEncoder.mapHandler(handler);

		assertEquals(MOUNT_PATH + "/fooValue/barValue/" + PAGE_CLASS_NAME, url.toString());
	}


	private final PackageMapper innerClassEncoder = new PackageMapper(MOUNT_PATH,
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

		assertEquals(MOUNT_PATH + '/' + "PackageMapperTest$OuterPage$InnerPage", url.toString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3838
	 */
	@Test
	public void decodeInnerClass()
	{
		Url url = Url.parse(MOUNT_PATH + '/' + "PackageMapperTest$OuterPage$InnerPage");
		IRequestHandler handler = innerClassEncoder.mapRequest(getRequest(url));

		assertThat(handler, instanceOf(RenderPageRequestHandler.class));
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

		assertEquals(MOUNT_PATH + '/' + ALIAS, url.toString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3941
	 */
	@Test
	public void decodeAlias()
	{
		Url url = Url.parse(MOUNT_PATH + '/' + ALIAS + "?15");
		IRequestHandler handler = aliasEncoder.mapRequest(getRequest(url));

		assertThat(handler, instanceOf(RenderPageRequestHandler.class));
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();
		checkPage(page, 15);
		assertEquals(PAGE_CLASS_NAME, page.getClass().getSimpleName());
	}
	
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5565
	 */
	@Test
	public void testGetCompatibilityScore()
	{
	    Url url = Url.parse(MOUNT_PATH + '/' + "MyPage");
	    MockWebRequest request = new MockWebRequest(url);
	    int score = encoder.getCompatibilityScore(request);
	    
	    assertEquals(4, score);
	    
	    url = Url.parse(MOUNT_PATH + "/foo/bar/" + "MyPage");
	    request = new MockWebRequest(url);
	    score = namedParametersEncoder.getCompatibilityScore(request);
	    
	    assertEquals(6, score);
	}
}
