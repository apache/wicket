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
package org.apache._wicket.request.encoder;

import org.apache._wicket.IComponent;
import org.apache._wicket.IPage;
import org.apache._wicket.MockPage;
import org.apache._wicket.PageParameters;
import org.apache._wicket.request.RequestHandler;
import org.apache._wicket.request.RequestHandlerEncoder;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.handler.impl.BookmarkableListenerInterfaceRequestHandler;
import org.apache._wicket.request.handler.impl.BookmarkablePageRequestHandler;
import org.apache._wicket.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache._wicket.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.markup.html.link.ILinkListener;


/**
 * @author Matej Knopp
 */
public class MountedEncoderTest extends AbstractEncoderTest
{

	/**
	 * Construct.
	 */
	public MountedEncoderTest()
	{
	}

	private MountedEncoder encoder = new MountedEncoder("/some/mount/path", MockPage.class)
	{
		@Override
		protected EncoderContext getContext()
		{
			return context;
		}
	};

	private MountedEncoder placeholderEncoder = new MountedEncoder("/some/${param1}/path/${param2}", MockPage.class)
	{
		@Override
		protected EncoderContext getContext()
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
		RequestHandler handler = encoder.decode(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IPage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedParamsCount());
		assertTrue(page.getPageParameters().getNamedParameterKeys().isEmpty());
	}

	/**
	 * 
	 */
	public void testDecode2()
	{
		Url url = Url.parse("some/mount/path/indexed1?a=b&b=c");
		RequestHandler handler = encoder.decode(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IPage page = ((RenderPageRequestHandler)handler).getPage();

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
		Url url = Url.parse("some/mount/path?15");
		RequestHandler handler = encoder.decode(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IPage page = ((RenderPageRequestHandler)handler).getPage();
		checkPage(page, 15, 0, null);
	}

	/**
	 * 
	 */
	public void testDecode4()
	{
		Url url = Url.parse("some/mount/path/i1/i2?abc.15.5&a=b&b=c");
		RequestHandler handler = encoder.decode(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IPage page = ((RenderPageRequestHandler)handler).getPage();
		checkPage(page, 15, 5, "abc");

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
		Url url = Url.parse("some/mount/path?15-ILinkListener-foo-bar");
		RequestHandler handler = encoder.decode(getRequest(url));

		assertTrue(handler instanceof ListenerInterfaceRequestHandler);

		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

		IPage page = h.getPage();
		checkPage(page, 15, 0, null);

		assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
		assertEquals("foo:bar", h.getComponent().getPath());
		assertNull(h.getBehaviorIndex());
	}

	/**
	 * 
	 */
	public void testDecode6()
	{
		Url url = Url.parse("some/mount/path/i1/i2?abc.15.5-ILinkListener-foo-bar&a=b&b=c");
		RequestHandler handler = encoder.decode(getRequest(url));

		assertTrue(handler instanceof ListenerInterfaceRequestHandler);
		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

		IPage page = h.getPage();
		checkPage(page, 15, 5, "abc");

		assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
		assertEquals("foo:bar", h.getComponent().getPath());

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
		Url url = Url.parse("some/mount/path?15-ILinkListener.4-foo-bar");
		RequestHandler handler = encoder.decode(getRequest(url));

		assertTrue(handler instanceof ListenerInterfaceRequestHandler);

		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

		IPage page = h.getPage();
		checkPage(page, 15, 0, null);

		assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
		assertEquals("foo:bar", h.getComponent().getPath());
		assertEquals((Object)4, h.getBehaviorIndex());
	}

	/**
	 * 
	 */
	public void testDecode8()
	{
		Url url = Url.parse("some/mmount/path?15-ILinkListener.4-foo-bar");
		RequestHandler handler = encoder.decode(getRequest(url));

		assertNull(handler);
	}
	
	/**
	 * 
	 */
	public void testDecode9()
	{
		// capture the home page
		Url url = Url.parse("");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof RenderPageRequestHandler);
	}
	
	/**
	 * 
	 */
	public void testEncode1()
	{
		RequestHandler handler = new BookmarkablePageRequestHandler(MockPage.class, null,
			new PageParameters());
		Url url = encoder.encode(handler);
		assertEquals("some/mount/path", url.toString());
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
		RequestHandler handler = new BookmarkablePageRequestHandler(MockPage.class, null,
			parameters);
		Url url = encoder.encode(handler);
		assertEquals("some/mount/path/i1/i2?a=b&b=c", url.toString());
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

		RequestHandler handler = new BookmarkablePageRequestHandler(MockPage.class, "abc",
			parameters);
		Url url = encoder.encode(handler);

		assertEquals("some/mount/path/i1/i2?abc&a=b&b=c", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode4()
	{
		MockPage page = new MockPage(15, 5, "abc");
		page.getPageParameters().setIndexedParameter(0, "i1");
		page.getPageParameters().setIndexedParameter(1, "i2");
		page.getPageParameters().setNamedParameter("a", "b");
		page.getPageParameters().setNamedParameter("b", "c");
		page.setCreatedBookmarkable(true);

		RequestHandler handler = new RenderPageRequestHandler(page);
		Url url = encoder.encode(handler);

		assertEquals("some/mount/path/i1/i2?abc.15.5&a=b&b=c", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode5()
	{
		MockPage page = new MockPage(15, 5, "abc");
		page.getPageParameters().setIndexedParameter(0, "i1");
		page.getPageParameters().setIndexedParameter(1, "i2");
		page.getPageParameters().setNamedParameter("a", "b");
		page.getPageParameters().setNamedParameter("b", "c");

		page.setCreatedBookmarkable(false);

		RequestHandler handler = new RenderPageRequestHandler(page);
		Url url = encoder.encode(handler);

		// mounted pages must render mounted url even for page that has not been created by
		// bookmarkable
		// URL

		assertEquals("some/mount/path/i1/i2?abc.15.5&a=b&b=c", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode6()
	{
		MockPage page = new MockPage(15, 5, "abc");
		page.getPageParameters().setIndexedParameter(0, "i1");
		page.getPageParameters().setIndexedParameter(1, "i2");
		page.getPageParameters().setNamedParameter("a", "b");
		page.getPageParameters().setNamedParameter("b", "c");

		// shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IComponent c = page.get("foo:bar");

		RequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(page, c,
			ILinkListener.INTERFACE);

		Url url = encoder.encode(handler);

		assertEquals("some/mount/path/i1/i2?abc.15.5-ILinkListener-foo-bar&a=b&b=c", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode7()
	{
		MockPage page = new MockPage(15, 5, "abc");
		page.getPageParameters().setIndexedParameter(0, "i1");
		page.getPageParameters().setIndexedParameter(1, "i2");
		page.getPageParameters().setNamedParameter("a", "b");
		page.getPageParameters().setNamedParameter("b", "c");

		// shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
		// as this explicitely says the url must be bookmarkable
		page.setCreatedBookmarkable(false);

		IComponent c = page.get("foo:bar");

		RequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(page, c,
			ILinkListener.INTERFACE, 4);

		Url url = encoder.encode(handler);

		assertEquals("some/mount/path/i1/i2?abc.15.5-ILinkListener.4-foo-bar&a=b&b=c",
			url.toString());
	}


	/**
	 * 
	 */
	public void testEncode8()
	{
		MockPage page = new MockPage(15, 5, "abc");
		page.setBookmarkable(true);
		page.setCreatedBookmarkable(true);
		page.setPageStateless(true);

		RequestHandler handler = new RenderPageRequestHandler(page);

		Url url = encoder.encode(handler);

		assertEquals("some/mount/path", url.toString());
	}

	/**
	 * 
	 */
	public void testConstruct1()
	{
		try
		{
			RequestHandlerEncoder e = new MountedEncoder("", MockPage.class);
			assertFalse(true);
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}
	}
	
	/**
	 * 
	 */
	public void testConstruct2()
	{
		try
		{
			RequestHandlerEncoder e = new MountedEncoder("/", MockPage.class);
			assertFalse(true);
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}
	}
	
	/**
	 * 
	 */
	public void testPlaceholderDecode1()
	{
		Url url = Url.parse("some/p1/path/p2");
		RequestHandler handler = placeholderEncoder.decode(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IPage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(0, page.getPageParameters().getIndexedParamsCount());
		assertTrue(page.getPageParameters().getNamedParameterKeys().size() == 2);
		assertEquals("p1", page.getPageParameters().getNamedParameter("param1").toString());
		assertEquals("p2", page.getPageParameters().getNamedParameter("param2").toString());
	}

	/**
	 * 
	 */
	public void testPlaceholderDecode2()
	{
		Url url = Url.parse("some/p1/path/p2/indexed1?a=b&b=c");
		RequestHandler handler = placeholderEncoder.decode(getRequest(url));

		assertTrue(handler instanceof RenderPageRequestHandler);
		IPage page = ((RenderPageRequestHandler)handler).getPage();

		PageParameters p = page.getPageParameters();
		assertEquals(1, p.getIndexedParamsCount());
		assertEquals("indexed1", p.getIndexedParameter(0).toString());

		assertEquals(4, p.getNamedParameterKeys().size());
		assertEquals("b", p.getNamedParameter("a").toString());
		assertEquals("c", p.getNamedParameter("b").toString());		
		assertEquals("p1", page.getPageParameters().getNamedParameter("param1").toString());
		assertEquals("p2", page.getPageParameters().getNamedParameter("param2").toString());
	}
	
	/**
	 * 
	 */
	public void testPlaceholderEncode2()
	{
		PageParameters parameters = new PageParameters();
		parameters.setIndexedParameter(0, "i1");
		parameters.setIndexedParameter(1, "i2");
		parameters.setNamedParameter("a", "b");
		parameters.setNamedParameter("b", "c");
		parameters.setNamedParameter("param1", "p1");
		parameters.setNamedParameter("param2", "p2");
		
		RequestHandler handler = new BookmarkablePageRequestHandler(MockPage.class, null,
			parameters);
		Url url = placeholderEncoder.encode(handler);
		assertEquals("some/p1/path/p2/i1/i2?a=b&b=c", url.toString());
	}
}
