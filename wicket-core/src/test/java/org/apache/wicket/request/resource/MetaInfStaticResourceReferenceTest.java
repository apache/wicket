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
package org.apache.wicket.request.resource;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.BaseWicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link MetaInfStaticResourceReference}.
 * 
 * @author akiraly
 */
public class MetaInfStaticResourceReferenceTest
{
	private static final String STATIC_RESOURCE_NAME = "sample.js";

	/**
	 * Test with Servlet 3.0 container
	 * 
	 * @throws MalformedURLException
	 *             should not happen
	 */
	@Test
	public void testWithServlet30() throws MalformedURLException
	{
		MockApplication application = new MockApplication();
		MockServletContext servletContext = new MockServletContext(application, "/");
		BaseWicketTester tester = new BaseWicketTester(application, servletContext);

		MetaInfStaticResourceReference metaRes = new MetaInfStaticResourceReference(getClass(),
			STATIC_RESOURCE_NAME);
		PackageResourceReference packRes = new PackageResourceReference(getClass(),
			STATIC_RESOURCE_NAME);

		Url packUrl = tester.getRequestCycle().mapUrlFor(packRes, null);
		Url metaUrl = tester.getRequestCycle().mapUrlFor(metaRes, null);

		Assert.assertNotNull(metaUrl);
		Assert.assertNotNull(packUrl);
		Assert.assertFalse(
			"Meta and pack resource should not map to the same url under servlet 3.0.",
			metaUrl.equals(packUrl));

		String metaUrlStr = metaUrl.toString();
		if (metaUrlStr.charAt(1) != '/')
		{
			metaUrlStr = "/" + metaUrlStr;
		}

		// meta resource is served by the servlet container under 3.0
		URL metaNetUrl = servletContext.getResource(metaUrlStr);

		Assert.assertNotNull("Meta resource is not found by the 3.0 servlet container.", metaNetUrl);

		MockWebRequest request = new MockWebRequest(packUrl);

		IRequestHandler requestHandler = tester.getApplication()
			.getRootRequestMapper()
			.mapRequest(request);

		// the pack resource is still served by wicket
		Assert.assertNotNull(requestHandler);
	}

	/**
	 * Needs to clear META_INF_RESOURCES_SUPPORTED field in {@link MetaInfStaticResourceReference}
	 * class between tests because classes do not get reloaded between junit tests. This is not a
	 * problem in production where the servlet container is not changing versions.
	 * 
	 * @throws Exception
	 *             if the reflection magic failed
	 */
	@Before
	public void before() throws Exception
	{
		Field field = MetaInfStaticResourceReference.class.getDeclaredField("META_INF_RESOURCES_SUPPORTED");
		field.setAccessible(true);
		field.set(null, null);
	}

	/**
	 * {@link #before()}
	 * 
	 * @throws Exception
	 *             if before fails
	 */
	@After
	public void after() throws Exception
	{
		before();
	}

}