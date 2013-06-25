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

import java.util.Locale;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.request.resource.ResourceReference.UrlAttributes;
import org.apache.wicket.response.ByteArrayResponse;
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
}
