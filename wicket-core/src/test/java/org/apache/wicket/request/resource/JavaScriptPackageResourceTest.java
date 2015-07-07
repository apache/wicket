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

import java.io.IOException;
import java.util.Locale;

import org.apache.wicket.javascript.IJavaScriptCompressor;
import org.apache.wicket.markup.html.PackageResourceTest;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for JavaScriptPackageResource
 */
public class JavaScriptPackageResourceTest extends WicketTestCase
{
	private static final String APP_COMPRESSED = "APP_COMPRESSED";

	private static final String RESOURCE_COMPRESSED = "RESOURCE_COMPRESSED";

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();

				getResourceSettings().setJavaScriptCompressor(new IJavaScriptCompressor()
				{
					@Override
					public String compress(String original)
					{
						return APP_COMPRESSED;
					}
				});
			}
		};
	}

	/**
	 * Tests that a {@link JavaScriptPackageResource} can have its custom
	 * {@link IJavaScriptCompressor}
	 */
	@Test
	public void customResourceCompressor()
	{
		JavaScriptPackageResource resource = new JavaScriptPackageResource(
			PackageResourceTest.class, "packaged1.txt", null, null, null)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected IJavaScriptCompressor getCompressor()
			{
				return new IJavaScriptCompressor()
				{

					@Override
					public String compress(String original)
					{
						return RESOURCE_COMPRESSED;
					}
				};
			}
		};

		tester.startResource(resource);
		assertEquals(RESOURCE_COMPRESSED, tester.getLastResponseAsString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4762
	 *
	 * Asserts that the input stream used to calculate the IResourceVersion is the
	 * same as the one used for the actual response
	 */
	@Test
	public void cacheableStreamIsCompressed() throws ResourceStreamNotFoundException, IOException
	{
		JavaScriptPackageResource resource = new JavaScriptPackageResource(
				PackageResourceTest.class, "packaged1.txt", null, null, null)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected IJavaScriptCompressor getCompressor()
			{
				return new IJavaScriptCompressor()
				{

					@Override
					public String compress(String original)
					{
						return RESOURCE_COMPRESSED;
					}
				};
			}
		};

		tester.startResource(resource);
		assertEquals(RESOURCE_COMPRESSED, tester.getLastResponseAsString());
	}

	/**
	 * Tests that a {@link JavaScriptPackageResource} can use the application level
	 * {@link IJavaScriptCompressor} when there is no custom
	 */
	@Test
	public void appLevelCompressor()
	{
		JavaScriptPackageResource resource = new JavaScriptPackageResource(
			PackageResourceTest.class, "packaged1.txt", null, null, null);

		tester.startResource(resource);
		assertEquals(APP_COMPRESSED, tester.getLastResponseAsString());
	}

	/**
	 * Tests that a {@link JavaScriptPackageResource} wont be compressed when there is no configured
	 * {@link IJavaScriptCompressor}
	 */
	@Test
	public void noCompressor()
	{
		JavaScriptPackageResource resource = new JavaScriptPackageResource(
			PackageResourceTest.class, "packaged1.txt", null, null, null);
		tester.getSession().setLocale(Locale.ROOT);
		tester.getApplication().getResourceSettings().setJavaScriptCompressor(null);
		tester.startResource(resource);
		assertEquals("TEST", tester.getLastResponseAsString());
	}
}
