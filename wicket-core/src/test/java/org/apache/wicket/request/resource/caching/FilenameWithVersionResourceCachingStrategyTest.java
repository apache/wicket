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

import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.caching.version.IResourceVersion;
import org.apache.wicket.request.resource.caching.version.MessageDigestResourceVersion;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for FilenameWithVersionResourceCachingStrategy
 */
public class FilenameWithVersionResourceCachingStrategyTest extends Assert
{
	private final String versionPrefix = "--vers--";
	private final IResourceVersion resourceVersion = new MessageDigestResourceVersion();
	private final FilenameWithVersionResourceCachingStrategy strategy =
			new FilenameWithVersionResourceCachingStrategy(versionPrefix, resourceVersion);

	@Test
	public void testDecorateUrl() throws Exception
	{
		ResourceUrl resourceUrl = new ResourceUrl("some-resource.txt", new PageParameters());
		strategy.decorateUrl(resourceUrl, new TestResource());

		assertEquals("some-resource--vers--9A0364B9E99BB480DD25E1F0284C8555.txt", resourceUrl.getFileName());
	}

	@Test
	public void testUndecorateUrl() throws Exception
	{
		ResourceUrl resourceUrl = new ResourceUrl("some-resource--vers--9A0364B9E99BB480DD25E1F0284C8555.txt", new PageParameters());
		strategy.undecorateUrl(resourceUrl);

		assertEquals("some-resource.txt", resourceUrl.getFileName());
	}

	@Test
	public void testDecorateResponse() throws Exception
	{
		AbstractResource.ResourceResponse response = new AbstractResource.ResourceResponse();
		strategy.decorateResponse(response, new TestResource());

		assertEquals(WebResponse.MAX_CACHE_DURATION, response.getCacheDuration());
		assertEquals(WebResponse.CacheScope.PUBLIC, response.getCacheScope());
	}

	@Test
	public void testUrlVersionStoredInRequestCycle()
	{
		WicketTester tester = new WicketTester();
		tester.getApplication().getResourceSettings().setCachingStrategy(strategy);

		try
		{
			ResourceUrl resourceUrl = new ResourceUrl("some-resource--vers--9A0364B9E99BB480DD25E1F0284C8555.txt", new PageParameters());
			strategy.undecorateUrl(resourceUrl);

			String version = tester.getRequestCycle().getMetaData(IResourceCachingStrategy.URL_VERSION);

			assertEquals("9A0364B9E99BB480DD25E1F0284C8555", version);
		}
		finally
		{
			tester.destroy();
		}
	}

}
