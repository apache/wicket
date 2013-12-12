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

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.caching.version.IResourceVersion;
import org.apache.wicket.request.resource.caching.version.MessageDigestResourceVersion;
import org.apache.wicket.util.tester.BaseWicketTester;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.time.Duration;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for FilenameWithVersionResourceCachingStrategy
 */
public class FilenameWithVersionResourceCachingStrategyTest extends Assert
{
	public static final String TEST_RESOURCE_VERSION = "9A0364B9E99BB480DD25E1F0284C8555";

	private final String versionPrefix = "--vers--";
	private final IResourceVersion resourceVersion = new MessageDigestResourceVersion();
	private final FilenameWithVersionResourceCachingStrategy strategy =
			new FilenameWithVersionResourceCachingStrategy(versionPrefix, resourceVersion);

	@Test
	public void testDecorateUrl() throws Exception
	{
		ResourceUrl resourceUrl = new ResourceUrl("some-resource.txt", new PageParameters());
		strategy.decorateUrl(resourceUrl, new TestResource());

		assertEquals("some-resource--vers--"+TEST_RESOURCE_VERSION+".txt", resourceUrl.getFileName());
		
		// don't issue an error
		resourceUrl = new ResourceUrl("some-resource--vers--with-prefix.txt", new PageParameters());
		strategy.decorateUrl(resourceUrl, new TestResource());
		
		assertEquals("some-resource--vers--with-prefix--vers--"+TEST_RESOURCE_VERSION+".txt", resourceUrl.getFileName());
	}

	@Test
	public void testUndecorateUrl() throws Exception
	{
		ResourceUrl resourceUrl = new ResourceUrl("some-resource--vers--"+TEST_RESOURCE_VERSION+".txt", new PageParameters());
		strategy.undecorateUrl(resourceUrl);

		assertEquals("some-resource.txt", resourceUrl.getFileName());
		
		// test URL with versiton containing prefix in original
		resourceUrl = new ResourceUrl("some-resource--vers--with-prefix--vers--9A0364B9E99BB480DD25E1F0284C8555.txt", new PageParameters());
		strategy.undecorateUrl(resourceUrl);
		
		assertEquals("some-resource--vers--with-prefix.txt", resourceUrl.getFileName());
		
		// test URL without version containing prefix
		resourceUrl = new ResourceUrl("some-resource--vers--without-version.txt", new PageParameters());
		strategy.undecorateUrl(resourceUrl);

		assertEquals("some-resource--vers--without-version.txt", resourceUrl.getFileName());
	}

	@Test
	public void testDecorateResponse() throws Exception
	{
		Duration defaultDuration = Duration.minutes(60);

		// setup RequestCycle
		BaseWicketTester tester = new BaseWicketTester();
		RequestCycle requestCycle = ThreadContext.getRequestCycle();
		Application.get().getResourceSettings().setDefaultCacheDuration(defaultDuration);

		try
		{
			// version match
			requestCycle.setMetaData(IResourceCachingStrategy.URL_VERSION, TEST_RESOURCE_VERSION);

			AbstractResource.ResourceResponse response = new AbstractResource.ResourceResponse();
			strategy.decorateResponse(response, new TestResource());

			assertEquals(WebResponse.MAX_CACHE_DURATION, response.getCacheDuration());
			assertEquals(WebResponse.CacheScope.PUBLIC, response.getCacheScope());

			// version mismatch
			requestCycle.setMetaData(IResourceCachingStrategy.URL_VERSION, "foo");

			response = new AbstractResource.ResourceResponse();
			strategy.decorateResponse(response, new TestResource());

			assertEquals(defaultDuration, response.getCacheDuration());
			assertEquals(WebResponse.CacheScope.PRIVATE, response.getCacheScope());
		}
		finally
		{
			tester.destroy();
		}
	}

	@Test
	public void testUrlVersionStoredInRequestCycle()
	{
		WicketTester tester = new WicketTester();
		tester.getApplication().getResourceSettings().setCachingStrategy(strategy);

		try
		{
			ResourceUrl resourceUrl = new ResourceUrl("some-resource--vers--"+TEST_RESOURCE_VERSION+".txt", new PageParameters());
			strategy.undecorateUrl(resourceUrl);

			String version = tester.getRequestCycle().getMetaData(IResourceCachingStrategy.URL_VERSION);

			assertEquals(TEST_RESOURCE_VERSION, version);
		}
		finally
		{
			tester.destroy();
		}
	}

}
