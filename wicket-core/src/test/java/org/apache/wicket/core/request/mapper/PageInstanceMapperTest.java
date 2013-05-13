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

import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.wicket.MockPage;
import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestableComponent;
import org.junit.Test;

/**
 *
 * @author Matej Knopp
 */
public class PageInstanceMapperTest extends AbstractMapperTest
{

	private final PageInstanceMapper encoder = new PageInstanceMapper()
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
		Url url = Url.parse("wicket/page?4");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof RenderPageRequestHandler);

		RenderPageRequestHandler h = (RenderPageRequestHandler)handler;
		checkPage(h.getPage(), 4);
	}

	/**
	 *
	 */
	@Test
	public void decode2()
	{
		Url url = Url.parse("wicket/page/ingore/me?4&a=3&b=3");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof RenderPageRequestHandler);

		RenderPageRequestHandler h = (RenderPageRequestHandler)handler;
		checkPage(h.getPage(), 4);
	}

	/**
	 *
	 */
	@Test
	public void decode3()
	{
		Url url = Url.parse("wicket/page?4-ILinkListener-a-b-c");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ListenerInterfaceRequestHandler);

		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;
		checkPage(h.getPage(), 4);
		assertEquals(h.getComponent().getPageRelativePath(), "a:b:c");
		assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
		assertNull(h.getBehaviorIndex());
	}

	/**
	 *
	 */
	@Test
	public void decode4()
	{
		Url url = Url.parse("wickett/pagee?4-ILinkListener-a:b-c");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 *
	 */
	@Test
	public void decode5()
	{
		Url url = Url.parse("wicket/page?abc");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 *
	 */
	@Test
	public void decode6()
	{
		Url url = Url.parse("wicket/page?4-ILinkListener.5-a-b-c");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ListenerInterfaceRequestHandler);

		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;
		checkPage(h.getPage(), 4);
		assertEquals(h.getComponent().getPageRelativePath(), "a:b:c");
		assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
		assertEquals((Object)5, h.getBehaviorIndex());
	}

	/**
	 *
	 */
	@Test
	public void decode7()
	{
		Url url = Url.parse("wicket/page?4-6.ILinkListener.5-a-b-c");

		context.setNextPageRenderCount(6);

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ListenerInterfaceRequestHandler);

		ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;
		assertEquals(6, h.getPage().getRenderCount());
	}

	/**
	 *
	 */
	@Test(expected = StalePageException.class)
	public void decode8()
	{
		Url url = Url.parse("wicket/page?4-6.ILinkListener.5-a-b-c");

		context.setNextPageRenderCount(8);

		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		((IPageRequestHandler)handler).getPage();
	}

	@Test
	public void decode9()
	{
		final Url url = Url.parse("page?4");

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
				return Url.parse("wicket/page");
			}

			@Override
			public Object getContainerRequest()
			{
				return null;
			}
		};

		IRequestHandler handler = encoder.mapRequest(request);

		((IPageRequestHandler)handler).getPage();
	}

	/**
	 *
	 */
	@Test
	public void encode1()
	{
		MockPage page = new MockPage(15);
		IPageProvider provider = new PageProvider(page);
		IRequestHandler handler = new RenderPageRequestHandler(provider);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/page?15", url.toString());
	}


	/**
	 *
	 */
	@Test
	public void encode2()
	{
		MockPage page = new MockPage(15);
		page.setRenderCount(5);

		IRequestableComponent c = page.get("a:b:c");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new ListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/page?15-5.ILinkListener-a-b-c", url.toString());
	}

}
