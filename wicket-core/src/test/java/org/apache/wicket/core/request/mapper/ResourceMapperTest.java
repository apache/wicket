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
import java.util.List;
import java.util.Locale;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class ResourceMapperTest extends WicketTestCase
{
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final String SHARED_NAME = "test-resource";

	private IRequestMapper mapper;
	private IRequestMapper mapperWithPlaceholder;
	private TestResource resource;

	/**
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		resource = new TestResource();
		tester.getApplication().getSharedResources().add(SHARED_NAME, resource);
		ResourceReference resourceReference = new SharedResourceReference(SHARED_NAME);
		mapper = new ResourceMapper("/test/resource", resourceReference);
		mapperWithPlaceholder = new ResourceMapper("/test2/${name}/resource", resourceReference);
		tester.getApplication().getRootRequestMapperAsCompound().add(mapper);
	}

	private Request createRequest(final String url)
	{
		return new Request()
		{
			@Override
			public Url getUrl()
			{
				return Url.parse(url, CHARSET);
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}

			@Override
			public Charset getCharset()
			{
				return CHARSET;
			}

			@Override
			public Url getClientUrl()
			{
				return getUrl();
			}

			@Override
			public Object getContainerRequest()
			{
				return null;
			}
		};
	}

	/**
	 * testInvalidPathIsEmpty()
	 */
	@Test
	public void invalidPathIsEmpty()
	{
		IRequestHandler requestHandler = mapper.mapRequest(createRequest(""));
		assertNull(requestHandler);
	}

	/**
	 * testInvalidPathIsMismatch()
	 */
	@Test
	public void invalidPathIsMismatch()
	{
		IRequestHandler requestHandler = mapper.mapRequest(createRequest("test/resourcex"));
		assertNull(requestHandler);
	}

	/**
	 * testInvalidPathIsTooShort()
	 */
	@Test
	public void invalidPathIsTooShort()
	{
		IRequestHandler requestHandler = mapper.mapRequest(createRequest("test"));
		assertNull(requestHandler);
	}

	/**
	 * testValidPathWithParams()
	 */
	@Test
	public void validPathWithParams()
	{
		Request request = createRequest("test/resource/1/fred");
		IRequestHandler requestHandler = mapper.mapRequest(request);
		assertNotNull(requestHandler);
		assertEquals(ResourceReferenceRequestHandler.class, requestHandler.getClass());
		assertEquals(request.getUrl(), mapper.mapHandler(requestHandler));

		tester.processRequest(requestHandler);
		PageParameters params = resource.pageParameters;
		assertNotNull(params);
		assertEquals(0, params.getAllNamed().size());
		assertEquals(2, params.getIndexedCount());

		StringValue paramId = params.get(0);
		assertNotNull(paramId);
		assertEquals(1, paramId.toInt());

		StringValue paramName = params.get(1);
		assertNotNull(paramName);
		assertEquals("fred", paramName.toString());
	}

	/**
	 * testValidPathWithParamsAndQueryPath()
	 */
	@Test
	public void validPathWithParamsAndQueryPath()
	{
		Request request = createRequest("test/resource/1/fred?foo=bar&foo=baz&value=12");
		IRequestHandler requestHandler = mapper.mapRequest(request);
		assertNotNull(requestHandler);
		assertEquals(ResourceReferenceRequestHandler.class, requestHandler.getClass());
		assertEquals(request.getUrl(), mapper.mapHandler(requestHandler));

		tester.processRequest(requestHandler);
		PageParameters params = resource.pageParameters;
		assertNotNull(params);
		assertEquals(3, params.getAllNamed().size());
		assertEquals(2, params.getIndexedCount());

		StringValue paramId = params.get(0);
		assertNotNull(paramId);
		assertEquals(1, paramId.toInt());

		StringValue paramName = params.get(1);
		assertNotNull(paramName);
		assertEquals("fred", paramName.toString());

		List<StringValue> foo = params.getValues("foo");
		assertNotNull(foo.size() == 2);
		assertEquals("bar", foo.get(0).toString(""));
		assertEquals("baz", foo.get(1).toString(""));

		StringValue paramValue = params.get("value");
		assertEquals(12, paramValue.toInt());
	}

	/**
	 * testPlaceholders()
	 */
	@Test
	public void placeholders()
	{
		Request request = createRequest("test2/image/resource/foo/bar?a=abc&b=123");
		IRequestHandler requestHandler = mapperWithPlaceholder.mapRequest(request);
		assertNotNull(requestHandler);
		assertEquals(ResourceReferenceRequestHandler.class, requestHandler.getClass());
		assertEquals(request.getUrl(), mapperWithPlaceholder.mapHandler(requestHandler));

		tester.processRequest(requestHandler);
		PageParameters params = resource.pageParameters;
		assertNotNull(params);
		assertEquals(3, params.getAllNamed().size());
		assertEquals(2, params.getIndexedCount());

		assertEquals("foo", params.get(0).toString());
		assertEquals("bar", params.get(1).toString());

		assertEquals("image", params.get("name").toString());
		assertEquals("abc", params.get("a").toString());
		assertEquals("123", params.get("b").toString());
	}

	/**
	 * testPlaceholdersWithQueryParamDuplicate()
	 */
	@Test
	public void placeholdersWithQueryParamDuplicate()
	{
		// we have one named parameter that exists twice
		Request request = createRequest("test2/image/resource/foo/bar?name=name-2&val=123");
		IRequestHandler handler = mapperWithPlaceholder.mapRequest(request);
		assertNotNull(handler);
		assertEquals(ResourceReferenceRequestHandler.class, handler.getClass());

		// the query part of the duplicate should be gone now
		Url newUrl = mapperWithPlaceholder.mapHandler(handler);
		assertEquals(Url.parse("test2/name-2/resource/foo/bar?val=123"), newUrl);

		// create new request
		request = createRequest(newUrl.toString());

		// get handler again
		handler = mapperWithPlaceholder.mapRequest(request);
		assertNotNull(handler);

		tester.processRequest(handler);
		PageParameters params = resource.pageParameters;
		assertNotNull(params);
		assertEquals(2, params.getAllNamed().size());
		assertEquals(2, params.getIndexedCount());

		assertEquals("foo", params.get(0).toString());
		assertEquals("bar", params.get(1).toString());

		assertEquals("name-2", params.get("name").toString());
		assertEquals("123", params.get("val").toString());
	}

	/**
	 *
	 */
	@Test
	public void requestWithEmptyFilename()
	{
		// request invalid path with empty filename
		// this must not return a handler
		Request request = createRequest("test2/image/");
		IRequestHandler handler = mapperWithPlaceholder.mapRequest(request);
		assertNull(handler);
	}

	private static class TestResource implements IResource
	{
		private static final long serialVersionUID = -3130204487473856574L;

		public PageParameters pageParameters;

		@Override
		public void respond(Attributes attributes)
		{
			pageParameters = attributes.getParameters();
		}
	}
}
