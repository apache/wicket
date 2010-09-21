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

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.junit.Test;


/**
 * Tests for {@link HomePageMapper}
 */
public class HomePageMapperTest extends AbstractMapperTest
{

	private final HomePageMapper encoder = new HomePageMapper()
	{
		@Override
		protected IMapperContext getContext()
		{
			return context;
		}
	};

	/**
	 * Tests requests to '/' without query parameters
	 */
	@Test
	public void testNoSegmentsNoQueryParameters()
	{
		Url url = new Url();
		Request request = getRequest(url);

		IRequestHandler handler = encoder.mapRequest(request);

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(context.getHomePageClass(), page.getClass());

		assertTrue(page.getPageParameters().isEmpty());
	}

	/**
	 * Tests requests to '/' with query parameters
	 */
	@Test
	public void testWithQueryParameters()
	{
		Url url = Url.parse("?name=value");
		Request request = getRequest(url);

		IRequestHandler handler = encoder.mapRequest(request);

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(context.getHomePageClass(), page.getClass());

		assertEquals("value", page.getPageParameters().get("name").toString());
	}

	/**
	 * Tests requests to '/' with query parameters
	 */
	@Test
	public void testWithQueryParameterWithoutEqualSignAndValue()
	{
		Url url = Url.parse("?name=");
		Request request = getRequest(url);

		IRequestHandler handler = encoder.mapRequest(request);

		assertTrue(handler instanceof RenderPageRequestHandler);
		IRequestablePage page = ((RenderPageRequestHandler)handler).getPage();

		assertEquals(context.getHomePageClass(), page.getClass());

		assertEquals("", page.getPageParameters().get("name").toString());
	}
}
