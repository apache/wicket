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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.wicket.MockPage;
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
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Matej Knopp
 */
class PageInstanceMapperTest extends AbstractMapperTest
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
	void decode1()
	{
		Url url = Url.parse("wicket/page?4");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);

		RenderPageRequestHandler h = (RenderPageRequestHandler)handler;
		checkPage(h.getPage(), 4);
	}

	/**
	 *
	 */
	@Test
	void decode2()
	{
		Url url = Url.parse("wicket/page?4&a=3&b=3");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(RenderPageRequestHandler.class);

		RenderPageRequestHandler h = (RenderPageRequestHandler)handler;
		checkPage(h.getPage(), 4);
	}

	/**
	 *
	 */
	@Test
	void ignoreIfPageIdentifierHasSegmentsAfterIt()
	{
		Url url = Url.parse("wicket/page/ingore/me?4&a=3&b=3");
		
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 *
	 */
	@Test
	void decode3()
	{
		Url url = Url.parse("wicket/page?4--a-b-c");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ListenerRequestHandler.class);

		ListenerRequestHandler h = (ListenerRequestHandler)handler;
		checkPage(h.getPage(), 4);
		assertEquals(h.getComponent().getPageRelativePath(), "a:b:c");
		assertNull(h.getBehaviorIndex());
	}

	/**
	 *
	 */
	@Test
	void decode4()
	{
		Url url = Url.parse("wickett/pagee?4--a:b-c");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 *
	 */
	@Test
	void decode5()
	{
		Url url = Url.parse("wicket/page?abc");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 *
	 */
	@Test
	void decode6()
	{
		Url url = Url.parse("wicket/page?4-ILinkListener.5-a-b-c");

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ListenerRequestHandler.class);

		ListenerRequestHandler h = (ListenerRequestHandler)handler;
		checkPage(h.getPage(), 4);
		assertEquals(h.getComponent().getPageRelativePath(), "a:b:c");
		assertEquals((Object)5, h.getBehaviorIndex());
	}

	/**
	 *
	 */
	@Test
	void decode7()
	{
		Url url = Url.parse("wicket/page?4-6.5-a-b-c");

		context.setNextPageRenderCount(6);

		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ListenerRequestHandler.class);

		ListenerRequestHandler h = (ListenerRequestHandler)handler;
		assertEquals(6, h.getPage().getRenderCount());
	}

	/**
	 *
	 */
	@Test
	void decode8()
	{
		Url url = Url.parse("wicket/page?4-6.5-a-b-c");

		context.setNextPageRenderCount(8);

		IRequestHandler handler = encoder.mapRequest(getRequest(url));

		assertThrows(StalePageException.class, () -> {
			((IPageRequestHandler)handler).getPage();
		});
	}

	@Test
	void decode9()
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
				return StandardCharsets.UTF_8;
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

		IRequestablePage page = ((IPageRequestHandler)handler).getPage();
		checkPage(page, 4);
	}


	@Test
	void decode10()
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
				return StandardCharsets.UTF_8;
			}

			@Override
			public Url getClientUrl()
			{
				return Url.parse("page");
			}

			@Override
			public Object getContainerRequest()
			{
				return null;
			}
		};

		IRequestHandler handler = encoder.mapRequest(request);

		IRequestablePage page = ((IPageRequestHandler)handler).getPage();
		checkPage(page, 4);
	}

	/**
	 *
	 */
	@Test
	void encode1()
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
	void encode2()
	{
		MockPage page = new MockPage(15);
		page.setRenderCount(5);

		IRequestableComponent c = page.get("a:b:c");

		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new ListenerRequestHandler(provider);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/page?15-5.-a-b-c", url.toString());
	}

}
