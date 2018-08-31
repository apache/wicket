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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.request.HomePage;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.version.CachingResourceVersion;
import org.apache.wicket.request.resource.caching.version.MessageDigestResourceVersion;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Simple test using the WicketTester
 */
class ResourceMapperWithDecoratedResourcesTest extends WicketTestCase
{

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			public void init()
			{
				super.init();
				getResourceSettings()
					.setCachingStrategy(new FilenameWithVersionResourceCachingStrategy(
						new CachingResourceVersion(new MessageDigestResourceVersion())));
				mountResource("stylesheet.css", new CssResourceReference(
					ResourceMapperWithDecoratedResourcesTest.class, "decorated-resource.css"));
				mountPage("/", HomePage.class);
			}
		};
	}

	@Test
	void resourceNoCacheDecorationSuccessfully()
	{
		tester.executeUrl("stylesheet.css");
		assertEquals("body { background-color: lightblue; }", tester.getLastResponseAsString());
	}

	@Test
	void resourceWithCacheDecorationSuccessfully()
	{
		tester.executeUrl("stylesheet-ver-9876543210.css");
		assertEquals("body { background-color: lightblue; }", tester.getLastResponseAsString());
	}
}
