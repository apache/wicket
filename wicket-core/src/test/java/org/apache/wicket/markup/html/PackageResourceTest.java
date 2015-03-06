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
package org.apache.wicket.markup.html;

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.SharedResources;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.JavaScriptPackageResource;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Packages;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for package resources.
 * 
 * @author Eelco Hillenius
 */
public class PackageResourceTest extends WicketTestCase
{
	/** mock application object */
	public WebApplication application;

	/**
	 *
	 */
	@Before
	public void before()
	{
		application = tester.getApplication();
	}

	/**
	 * Tests binding a single absolute package resource.
	 * 
	 * @throws Exception
	 */
	@Test
	public void bindAbsolutePackageResource() throws Exception
	{
		final SharedResources sharedResources = Application.get().getSharedResources();
		assertNotNull("resource packaged1.txt should be available as a packaged resource",
			sharedResources.get(PackageResourceTest.class, "packaged1.txt", null, null, null, true));
	}

	/**
	 * Tests {@link PackageResourceGuard}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void packageResourceGuard() throws Exception
	{
		PackageResourceGuard guard = new PackageResourceGuard();
		assertTrue(guard.acceptExtension("txt"));
		assertFalse(guard.acceptExtension("java"));
		assertTrue(guard.accept("foo/Bar.txt"));
		assertFalse(guard.accept("foo/Bar.java"));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, "Bar.txt")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, "Bar.txt.")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, ".Bar.txt")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, ".Bar.txt.")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, ".Bar")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, ".java")));
		assertFalse(guard.accept(Packages.absolutePath(PackageResourceTest.class, "Bar.java")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, "foo/.java")));
	}

	/**
	 * Test lenient matching
	 * 
	 * @throws Exception
	 */
	@Test
	public void lenientPackageResourceMatching() throws Exception
	{
		ResourceReference invalidResource = new PackageResourceReference(PackageResourceTest.class,
			"i_do_not_exist.txt", Locale.ENGLISH, null, null);
		assertNotNull(
			"resource i_do_not_exist.txt SHOULD be available as a packaged resource even if it doesn't exist",
			invalidResource.getResource());

		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1.txt", null, null,
			null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1.txt", Locale.CHINA,
			null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1.txt", Locale.CHINA,
			"foo", null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1.txt", null, "foo",
			null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en.txt", null,
			null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en_US.txt", null,
			null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en_US.txt", null,
			"foo", null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en_US.txt",
			Locale.US, null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en_US.txt",
			Locale.CANADA, null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en_US.txt",
			Locale.CHINA, null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_foo_bar_en.txt",
			null, null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_foo_bar_en_US.txt",
			null, null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class,
			"packaged1_foo_bar_en_US_MAC.txt", null, null, null));

		tester.getRequest().setUrl(tester.getRequestCycle().mapUrlFor(invalidResource, null));
		// since the resource does not exist wicket should let the handling fall through to the next
		// filter/servlet which will cause a 404 later
		assertFalse(tester.processRequest());

	}

	/**
	 * Test that {@link PackageResource} writes its Content-Type header in the response
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-4119
	 */
	@Test
	public void contentType()
	{
		PackageResource textResource = new PackageResource(PackageResourceTest.class,
			"packaged1.txt", null, null, null)
		{
			private static final long serialVersionUID = 1L;
		};

		tester.startResource(textResource);
		assertEquals("text/plain", tester.getLastResponse().getContentType());

		PackageResource jsResource = new PackageResource(PackageResourceTest.class, "packaged3.js",
			null, null, null)
		{
			private static final long serialVersionUID = 1L;
		};

		tester.startResource(jsResource);
		assertEquals("text/javascript", tester.getLastResponse().getContentType());
	}

	@Test
	public void textFileWithEncoding()
	{
		final String encoding = "Klingon-8859-42";
		final PackageResource resource = new PackageResource(PackageResourceTest.class,
			"packaged1.txt", null, null, null)
		{
			private static final long serialVersionUID = 1L;
		};
		resource.setTextEncoding(encoding);
		tester.startResource(resource);
		final String contentType = tester.getLastResponse().getContentType();
		assertEquals("text/plain; charset=" + encoding, contentType);
	}

	@Test
	public void javascriptFileWithEncoding()
	{
		final String encoding = "Klingon-8859-42";
		final JavaScriptPackageResource resource = new JavaScriptPackageResource(
			PackageResourceTest.class, "packaged3.js", null, null, null)
		{
			private static final long serialVersionUID = 1L;
		};
		resource.setTextEncoding(encoding);
		tester.startResource(resource);
		final String contentType = tester.getLastResponse().getContentType();
		assertEquals("text/javascript; charset=" + encoding, contentType);
	}
}
