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
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.AbstractResource.ContentRangeType;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.request.resource.ResourceReference.UrlAttributes;
import org.apache.wicket.response.ByteArrayResponse;
import org.apache.wicket.util.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Pedro Santos
 */
public class PackageResourceReferenceTest extends WicketTestCase
{
	private static Class<PackageResourceReferenceTest> scope = PackageResourceReferenceTest.class;
	private static Locale[] locales = { null, new Locale("en"), new Locale("en", "US") };
	private static String[] styles = { null, "style" };
	private static String[] variations = { null, "var" };

	/**
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		// some locale outside those in locales array
		tester.getSession().setLocale(Locale.CHINA);
	}

	/**
	 * 
	 */
	@Test
	public void resourceResolution()
	{
		for (Locale locale : locales)
		{
			for (String style : styles)
			{
				for (String variation : variations)
				{
					ResourceReference reference = new PackageResourceReference(scope,
						"resource.txt", locale, style, variation);
					UrlAttributes urlAttributes = reference.getUrlAttributes();
					assertEquals(locale, urlAttributes.getLocale());
					assertEquals(style, urlAttributes.getStyle());
					assertEquals(variation, urlAttributes.getVariation());

					ByteArrayResponse byteResponse = new ByteArrayResponse();
					Attributes mockAttributes = new Attributes(tester.getRequestCycle()
						.getRequest(), byteResponse);
					reference.getResource().respond(mockAttributes);
					String fileContent = new String(byteResponse.getBytes());
					if (locale != null)
					{
						assertTrue(fileContent.contains(locale.getLanguage()));
						if (locale.getCountry() != null)
						{
							assertTrue(fileContent.contains(locale.getCountry()));
						}
					}
					if (style != null)
					{
						assertTrue(fileContent.contains(style));
					}
					if (variation != null)
					{
						assertTrue(fileContent.contains(variation));
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void resourceResponse()
	{
		for (Locale locale : locales)
		{
			for (String style : styles)
			{
				for (String variation : variations)
				{
					ResourceReference reference = new PackageResourceReference(scope,
						"resource.txt", locale, style, variation);

					ByteArrayResponse byteResponse = new ByteArrayResponse();
					Attributes mockAttributes = new Attributes(tester.getRequestCycle()
						.getRequest(), byteResponse);
					reference.getResource().respond(mockAttributes);
					String fileContent = new String(byteResponse.getBytes());
					if (locale != null)
					{
						assertTrue(fileContent.contains(locale.getLanguage()));
						if (locale.getCountry() != null)
						{
							assertTrue(fileContent.contains(locale.getCountry()));
						}
					}
					if (style != null)
					{
						assertTrue(fileContent.contains(style));
					}
					if (variation != null)
					{
						assertTrue(fileContent.contains(variation));
					}
				}
			}
		}
	}

	/**
	 * Asserting if user did not set any locale or style, those from session get used if any
	 */
	@Test
	public void sessionAttributesRelevance()
	{
		for (Locale locale : new Locale[] { new Locale("en"), new Locale("en", "US") })
		{
			tester.getSession().setLocale(locale);
			for (String style : styles)
			{
				tester.getSession().setStyle(style);
				for (String variation : variations)
				{
					ResourceReference reference = new PackageResourceReference(scope,
						"resource.txt", null, null, variation);
					UrlAttributes urlAttributes = reference.getUrlAttributes();
					assertEquals(tester.getSession().getLocale(), urlAttributes.getLocale());
					assertEquals(tester.getSession().getStyle(), urlAttributes.getStyle());
					assertEquals(variation, urlAttributes.getVariation());
				}
			}
		}
	}

	/**
	 * Assert preference for specified locale and style over session ones
	 */
	@Test
	public void userAttributesPreference()
	{
		tester.getSession().setLocale(new Locale("en"));
		tester.getSession().setStyle("style");

		Locale[] userLocales = { null, new Locale("pt"), new Locale("pt", "BR") };
		String userStyle = "style2";

		for (Locale userLocale : userLocales)
		{
			for (String variation : variations)
			{
				ResourceReference reference = new PackageResourceReference(scope, "resource.txt",
					userLocale, userStyle, variation);
				UrlAttributes urlAttributes = reference.getUrlAttributes();

				assertEquals(userLocale, urlAttributes.getLocale());
				assertEquals(userStyle, urlAttributes.getStyle());
				assertEquals(variation, urlAttributes.getVariation());
			}
		}
	}

	/**
	 * see WICKET-5251 : Proper detection of already minified resources
	 */
	@Test
	public void testMinifiedNameDetectMinInName() throws Exception
	{
		final PackageResourceReference html5minjs = new PackageResourceReference("html5.min.js");
		Assert.assertEquals("html5.min.js", html5minjs.getMinifiedName());

		final PackageResourceReference html5notminjs = new PackageResourceReference("html5.notmin.js");
		Assert.assertEquals("html5.notmin.min.js", html5notminjs.getMinifiedName());

		final PackageResourceReference html5notmin = new PackageResourceReference("html5notmin");
		Assert.assertEquals("html5notmin.min", html5notmin.getMinifiedName());

		final PackageResourceReference html5min = new PackageResourceReference("html5.min");
		Assert.assertEquals("html5.min", html5min.getMinifiedName());

	}

	/**
	 * see WICKET-5250 - for JavaScriptResourceReference
	 */
	@Test
	public void testJavaScriptResourceReferenceRespectsMinifiedResourcesDetection()
	{
		Application.get().getResourceSettings().setUseMinifiedResources(true);
		final JavaScriptResourceReference notMinified = new JavaScriptResourceReference(PackageResourceReferenceTest.class, "a.js");
		final JavaScriptPackageResource notMinifiedResource = notMinified.getResource();
		Assert.assertTrue("Not minified resource should got its compress flag set to true", notMinifiedResource.getCompress());

		final JavaScriptResourceReference alreadyMinified = new JavaScriptResourceReference(PackageResourceReferenceTest.class, "b.min.js");
		final JavaScriptPackageResource alreadyMinifiedResource = alreadyMinified.getResource();
		Assert.assertFalse("Already minified resource should got its compress flag set to false", alreadyMinifiedResource.getCompress());
	}

	/**
	 * see WICKET-5250 - for CSSResourceReference
	 */
	@Test
	public void testCSSResourceReferenceRespectsMinifiedResourcesDetection()
	{
		Application.get().getResourceSettings().setUseMinifiedResources(true);
		final CssResourceReference notMinified = new CssResourceReference(PackageResourceReferenceTest.class, "a.css");
		final CssPackageResource notMinifiedResource = notMinified.getResource();
		Assert.assertTrue("Not minified resource should got its compress flag set to true", notMinifiedResource.getCompress());

		final CssResourceReference alreadyMinified = new CssResourceReference(PackageResourceReferenceTest.class, "b.min.css");
		final CssPackageResource alreadyMinifiedResource = alreadyMinified.getResource();
		Assert.assertFalse("Already minified resource should got its compress flag set to false", alreadyMinifiedResource.getCompress());
	}

	/**
	 * See WICKET-5819 - Media tags
	 */
	@Test
	public void testContentRange()
	{
		// Test range
		Assert.assertEquals("resource", makeRangeRequest("bytes=0-8"));
		Assert.assertEquals("ource", makeRangeRequest("bytes=3-8"));
		Assert.assertEquals("resource_var_style_en.txt", makeRangeRequest("bytes=0-"));
		Assert.assertEquals("var_style_en.txt", makeRangeRequest("bytes=9-"));
		Assert.assertEquals("resource_var_style_en.txt", makeRangeRequest("bytes=-"));
		Assert.assertEquals("resource_var_style_en.txt", makeRangeRequest("bytes=-25"));
	}

	private String makeRangeRequest(String range)
	{
		ResourceReference reference = new PackageResourceReference(scope, "resource.txt",
			locales[1], styles[1], variations[1]);

		ByteArrayResponse byteResponse = new ByteArrayResponse();

		Request request = tester.getRequestCycle().getRequest();
		MockHttpServletRequest mockHttpServletRequest = (MockHttpServletRequest)request.getContainerRequest();
		mockHttpServletRequest.setHeader("range", range);
		Attributes mockAttributes = new Attributes(request, byteResponse);
		reference.getResource().respond(mockAttributes);
		return new String(byteResponse.getBytes());
	}

	/**
	 * See WICKET-5819 - Media tags
	 *
	 * @throws IOException
	 */
	@Test
	public void testContentRangeLarge() throws IOException
	{
		InputStream resourceAsStream = null;
		try
		{
			resourceAsStream = PackageResourceReference.class.getResourceAsStream("resource_gt_4096.txt");
			String content = new String(IOUtils.toByteArray(resourceAsStream));

			// Check buffer comprehensive range request
			String bytes4094_4106 = makeRangeRequestToBigResource("bytes=4094-4106");
			assertEquals(12, bytes4094_4106.length());
			assertEquals("River Roller", bytes4094_4106);

			// Check buffer exceeding range request
			String bytes1000_5000 = makeRangeRequestToBigResource("bytes=1000-5000");
			assertEquals(4000, bytes1000_5000.length());
			assertEquals(content.substring(1000, 5000), bytes1000_5000);

			// Check buffer exceeding range request until end of content
			String bytes1000_end = makeRangeRequestToBigResource("bytes=1000-");
			assertEquals(4529, bytes1000_end.length());
			assertEquals(content.substring(1000, content.length()), bytes1000_end);

			// Check complete range request
			assertEquals(content.length(), makeRangeRequestToBigResource("bytes=-").length());
		}
		finally
		{
			IOUtils.closeQuietly(resourceAsStream);
		}
	}

	private String makeRangeRequestToBigResource(String range)
	{
		ResourceReference reference = new PackageResourceReference(scope, "resource_gt_4096.txt",
			null, null, null);

		ByteArrayResponse byteResponse = new ByteArrayResponse();

		Request request = tester.getRequestCycle().getRequest();
		MockHttpServletRequest mockHttpServletRequest = (MockHttpServletRequest)request.getContainerRequest();
		mockHttpServletRequest.setHeader("range", range);
		Attributes mockAttributes = new Attributes(request, byteResponse);
		reference.getResource().respond(mockAttributes);
		return new String(byteResponse.getBytes());
	}

	/**
	 * See WICKET-5819 - Media tags
	 */
	@Test
	public void testContentRangeHeaders()
	{
		// Test header fields
		ResourceReference reference = new PackageResourceReference(scope, "resource.txt",
			locales[1], styles[1], variations[1]);
		Request request = tester.getRequestCycle().getRequest();
		Response response = tester.getRequestCycle().getResponse();
		MockHttpServletResponse mockHttpServletResponse = (MockHttpServletResponse)response.getContainerResponse();
		Attributes mockAttributes = new Attributes(request, response);
		reference.getResource().respond(mockAttributes);
		Assert.assertEquals(ContentRangeType.BYTES.getTypeName(),
			mockHttpServletResponse.getHeader("Accept-Range"));
		// For normal: If a resource supports content range no content is delivered
		// if no "Range" header is given, but we have to deliver it, because
		// other resources then media should get the content. (e.g. CSS, JS, etc.) Browsers
		// detecting media requests and automatically add the "Range" header for
		// partial content and they don't make an initial request to detect if a media
		// resource supports Content-Range (by the Accept-Range header)
		Assert.assertEquals("resource_var_style_en.txt",
			new String(mockHttpServletResponse.getBinaryContent()));
	}


}
