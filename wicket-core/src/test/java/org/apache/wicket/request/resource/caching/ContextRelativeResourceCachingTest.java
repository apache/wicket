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
package org.apache.wicket.request.resource.caching;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.ContextRelativeResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.request.resource.caching.version.IResourceVersion;
import org.apache.wicket.request.resource.caching.version.StaticResourceVersion;
import org.junit.Test;

/**
 * 
 */
public class ContextRelativeResourceCachingTest extends WicketTestCase
{
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final String SHARED_NAME = "contextresource";

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

	private void init(ContextRelativeResource resource, String mountPath)
	{
		final IResourceVersion resourceVersion = new StaticResourceVersion("123");
		final IResourceCachingStrategy strategy = new FilenameWithVersionResourceCachingStrategy(
			"-version-", resourceVersion);

		tester.getApplication().getSharedResources().add(SHARED_NAME, resource);
		tester.getApplication().getResourceSettings().setCachingStrategy(strategy);
		final ResourceReference resourceReference = new SharedResourceReference(SHARED_NAME);
		tester.getApplication().mountResource(mountPath, resourceReference);
	}

	/**
	 * 
	 */
	@Test
	public void mapHandler()
	{
		ContextRelativeResource resource = new ContextRelativeResource("/style.css");
		init(resource, "/test/resource");

		Request request = createRequest("test/resource-version-4711?bla=123");
		final IRequestHandler handler = tester.getApplication()
			.getRootRequestMapper()
			.mapRequest(request);
		assertThat(handler, instanceOf(ResourceReferenceRequestHandler.class));
		assertEquals(((ResourceReferenceRequestHandler)handler).getResource(), resource);
	}

	/**
	 * 
	 */
	@Test
	public void mapRequest()
	{
		ContextRelativeResource resource = new ContextRelativeResource("/style.css");
		init(resource, "/test/resource");

		IRequestHandler handler = new ResourceReferenceRequestHandler(new SharedResourceReference(
			SHARED_NAME));
		Url url = tester.getApplication().getRootRequestMapper().mapHandler(handler);
		assertNotNull(url);
		assertEquals(url, Url.parse("test/resource-version-123"));
	}
}
