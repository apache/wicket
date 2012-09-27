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
package org.apache.wicket.request.cycle;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.wicket.mock.MockHomePage;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.response.StringResponse;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that RequestCycle#urlFor() does not encode the jsessionid for static resources.
 *
 * https://issues.apache.org/jira/browse/WICKET-4334
 */
public class RequestCycleUrlForTest extends Assert
{
	private static final String JSESSIONID = ";jsessionid=1234567890";
	private static final String BOOKMARKABLE_PAGE_URL = "/bookmarkablePage";
	private static final String RES_REF_URL = "/resRef";
	private static final String RESOURCE_URL = "/res";

	RequestCycle requestCycle;
	
	@Before
	public void before()
	{
		Request request = mock(Request.class);
		Response response = new StringResponse() {
			@Override
			public String encodeURL(CharSequence url)
			{
				return url + JSESSIONID;
			}
		};
		IRequestMapper mapper = mock(IRequestMapper.class);

		Url bookmarkablePageUrl = Url.parse(BOOKMARKABLE_PAGE_URL);
		when(mapper.mapHandler(argThat(new ExactClassMatcher<BookmarkablePageRequestHandler>(BookmarkablePageRequestHandler.class)))).thenReturn(bookmarkablePageUrl);

		Url resourceUrl = Url.parse(RESOURCE_URL);
		when(mapper.mapHandler(argThat(new ExactClassMatcher<ResourceRequestHandler>(ResourceRequestHandler.class)))).thenReturn(resourceUrl);

		Url resourceReferenceUrl = Url.parse(RES_REF_URL);
		when(mapper.mapHandler(argThat(new ExactClassMatcher<ResourceReferenceRequestHandler>(ResourceReferenceRequestHandler.class)))).thenReturn(resourceReferenceUrl);

		IExceptionMapper exceptionMapper = mock(IExceptionMapper.class);
		RequestCycleContext context = new RequestCycleContext(request, response, mapper, exceptionMapper);

		requestCycle = new RequestCycle(context);
		requestCycle.getUrlRenderer().setBaseUrl(Url.parse("http://dummy-host"));
	}

	/**
	 * Pages should have the jsessionid encoded in the url
	 *
	 * @throws Exception
	 */
	@Test
	public void urlForClass() throws Exception
	{
		CharSequence url = requestCycle.urlFor(MockHomePage.class, new PageParameters());
		assertEquals("./bookmarkablePage"+JSESSIONID, url);
	}

	/**
	 * ResourceReference with IStaticCacheableResource should not have the jsessionid encoded in the url
	 *
	 * @throws Exception
	 */
	@Test
	public void urlForResourceReference() throws Exception
	{
		final IStaticCacheableResource resource = mock(IStaticCacheableResource.class);
		ResourceReference reference = new ResourceReference("dummy")
		{
			@Override
			public IResource getResource()
			{
				return resource;
			}
		}; 
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference);
		CharSequence url = requestCycle.urlFor(handler);
		assertEquals('.'+RES_REF_URL, url);
	}

	/**
	 * ResourceReference with non-IStaticCacheableResource should not have the jsessionid encoded in the url
	 *
	 * @throws Exception
	 */
	@Test
	public void urlForResourceReferenceWithNonStaticResource() throws Exception
	{
		final IResource resource = mock(IResource.class);
		ResourceReference reference = new ResourceReference("dummy")
		{
			@Override
			public IResource getResource()
			{
				return resource;
			}
		};
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference);
		CharSequence url = requestCycle.urlFor(handler);
		assertEquals('.'+RES_REF_URL+JSESSIONID, url);
	}

	/**
	 * IStaticCacheableResource should not have the jsessionid encoded in the url
	 *
	 * @throws Exception
	 */
	@Test
	public void urlForStaticResource() throws Exception
	{
		IStaticCacheableResource resource = mock(IStaticCacheableResource.class);
		ResourceRequestHandler handler = new ResourceRequestHandler(resource, new PageParameters());
		CharSequence url = requestCycle.urlFor(handler);
		assertEquals('.'+RESOURCE_URL, url);
	}

	/**
	 * Non-IStaticCacheableResource should have the jsessionid encoded in the url
	 *
	 * @throws Exception
	 */
	@Test
	public void urlForDynamicResource() throws Exception
	{
		ByteArrayResource resource = new ByteArrayResource(null, new byte[] {1, 2}, "test.bin");
		ResourceRequestHandler handler = new ResourceRequestHandler(resource, new PageParameters());
		CharSequence url = requestCycle.urlFor(handler);
		assertEquals('.'+RESOURCE_URL + JSESSIONID, url);
	}

	/**
	 * A matcher that matches only when the object class is exactly the same as the expected one.
	 *
	 * @param <T>
	 *     the type of the expected class
	 */
	private static class ExactClassMatcher<T> extends BaseMatcher<T>
	{
		private final Class<T> targetClass;

		public ExactClassMatcher(Class<T> targetClass)
		{
			this.targetClass = targetClass;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean matches(Object obj)
		{
			if (obj != null)
			{
				return targetClass.equals(obj.getClass());
			}
			return false;
		}

		@Override
		public void describeTo(Description desc)
		{
			desc.appendText("Matches a class or subclass");
		}
	}
}
