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
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.resource.bundles.ConcatBundleResource;
import org.apache.wicket.resource.bundles.ResourceBundleReference;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.time.Time;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for resource bundles
 * 
 * @author papegaaij
 */
public class ResouceBundleTest extends WicketTestCase
{
	@Before
	public void before()
	{
		tester.getSession().setLocale(Locale.ENGLISH);
	}

	/**
	 * Tests the concatenation of 2 javascript files
	 */
	@Test
	public void concatBundle()
	{
		ConcatBundleResource bundle = new ConcatBundleResource(Arrays.asList(
			JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				ResouceBundleTest.class, "a.js")),
			JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				ResouceBundleTest.class, "b.js"))));

		tester.startResource(bundle);
		assertEquals("//a// b.js", tester.getLastResponseAsString().trim());
	}

	/**
	 * WICKET-6720 do not concat eagerly
	 */
	@Test
	public void concatBundleLastModified()
	{
		final Time lastModified = Time.now();
		
		ConcatBundleResource bundle = new ConcatBundleResource(Arrays.asList(
			JavaScriptHeaderItem.forReference(new ResourceReference("foo") {
				public IResource getResource() {
					return new IStaticCacheableResource()
					{
						@Override
						public void respond(Attributes attributes)
						{
							fail();
						}

						@Override
						public boolean isCachingEnabled()
						{
							return true;
						}

						@Override
						public Serializable getCacheKey()
						{
							return "";
						}

						@Override
						public IResourceStream getResourceStream()
						{
							return new AbstractResourceStream()
							{
								
								@Override
								public Time lastModifiedTime()
								{
									return lastModified;
								}
								
								@Override
								public InputStream getInputStream() throws ResourceStreamNotFoundException
								{
									fail();
									return null;
								}
								
								@Override
								public void close() throws IOException
								{
									fail();
								}
							};
						}
					};
				}
			})));

		assertEquals(lastModified, bundle.getResourceStream().lastModifiedTime());
	}

	/**
	 * Tests the replacement of provided resources by their bundle
	 * 
	 * @throws Exception
	 */
	@Test
	public void providedResource() throws Exception
	{
		tester.getApplication()
			.getResourceBundles()
			.addJavaScriptBundle(ResouceBundleTest.class, "ab.js",
				new JavaScriptResourceReference(ResouceBundleTest.class, "a.js"),
				new JavaScriptResourceReference(ResouceBundleTest.class, "b.js"));

		executeTest(BundlesPage.class, "BundlesPage_result.html");
	}

	/**
	 * Tests the replacement of provided resources by their bundle with defer option
	 *
	 * @throws Exception
	 */
	@Test
	public void providedResourceWithDefer() throws Exception
	{
		tester.getApplication()
		.getResourceBundles()
		.addJavaScriptBundle(ResouceBundleTest.class, "ab.js", true,
			new JavaScriptResourceReference(ResouceBundleTest.class, "a.js"),
			new JavaScriptResourceReference(ResouceBundleTest.class, "b.js"));

		executeTest(BundlesPage.class, "BundlesPage_result_defer.html");
	}

	/**
	 * Tests an external resource bundle
	 * 
	 * @throws Exception
	 */
	@Test
	public void externalBundle() throws Exception
	{
		ResourceBundleReference bundle = new ResourceBundleReference(
			new UrlResourceReference(
				Url.parse("http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js")));
		bundle.addProvidedResources(
			JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				ResouceBundleTest.class, "a.js")),
			JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				ResouceBundleTest.class, "b.js")));

		tester.getApplication()
			.getResourceBundles()
			.addBundle(JavaScriptHeaderItem.forReference(bundle));


		executeTest(BundlesPage.class, "BundlesPage_ext_result.html");
	}
}
