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
package org.apache.wicket.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceReference.UrlAttributes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ResourceUtilTest
{
	@Test
	void decodeResourceReferenceAttributesWithString() throws Exception
	{
		String urlParameter = "en_GB-style-variation";		
		UrlAttributes attributes = ResourceUtil.decodeResourceReferenceAttributes(urlParameter);
		
		assertEquals(Locale.UK, attributes.getLocale());
		assertEquals("style", attributes.getStyle());
		assertEquals("variation", attributes.getVariation());
		
		attributes = ResourceUtil.decodeResourceReferenceAttributes("it_IT");
		
		assertEquals(Locale.ITALY, attributes.getLocale());
		assertNull(attributes.getStyle());
		assertNull(attributes.getVariation());
		
		attributes = ResourceUtil.decodeResourceReferenceAttributes("-style-variation");
		assertNull(attributes.getLocale());
		assertEquals("style", attributes.getStyle());
		assertEquals("variation", attributes.getVariation());

		attributes = ResourceUtil.decodeResourceReferenceAttributes("--variation");
		assertNull(attributes.getLocale());
		assertNull(attributes.getStyle());
		assertEquals("variation", attributes.getVariation());

		attributes = ResourceUtil.decodeResourceReferenceAttributes("-style");
		assertNull(attributes.getLocale());
		assertEquals("style", attributes.getStyle());
		assertNull(attributes.getVariation());
	}

	@Test
	void rejectPathSeparators() throws Exception
	{
		assertEquals("style", ResourceUtil.rejectPathSeparators("style", "style"));
		assertEquals("my-style", ResourceUtil.rejectPathSeparators("my-style", "style"));
		assertEquals("", ResourceUtil.rejectPathSeparators("", "style"));
		assertNull(ResourceUtil.rejectPathSeparators(null, "style"));

		assertNull(ResourceUtil.rejectPathSeparators("a/b", "style"));
		assertNull(ResourceUtil.rejectPathSeparators("a\\b", "style"));
		assertNull(ResourceUtil.rejectPathSeparators("..", "style"));
		assertNull(ResourceUtil.rejectPathSeparators("../../etc", "style"));
		assertNull(ResourceUtil.rejectPathSeparators("a\0b", "style"));
		assertNull(ResourceUtil.rejectPathSeparators("\0", "style"));
	}

	@Test
	void rejectPathSeparatorsForLocale() throws Exception
	{
		assertEquals(Locale.UK, ResourceUtil.rejectPathSeparators(Locale.UK));
		assertNull(ResourceUtil.rejectPathSeparators((Locale)null));

		assertNull(ResourceUtil.rejectPathSeparators(new Locale("../../etc")));
		assertNull(ResourceUtil.rejectPathSeparators(new Locale("a/b")));
	}

	/**
	 * A locale without a variant, script or extensions is validated by inspecting its language and
	 * country rather than its {@link Locale#toString()}, so both subtags must still be checked, and
	 * a separator has to be caught wherever it sits.
	 */
	@Test
	void rejectPathSeparatorsForLanguageAndCountry() throws Exception
	{
		assertEquals(new Locale("nl"), ResourceUtil.rejectPathSeparators(new Locale("nl")));
		assertEquals(new Locale("nl", "NL"),
			ResourceUtil.rejectPathSeparators(new Locale("nl", "NL")));
		assertEquals(new Locale("", "NL"), ResourceUtil.rejectPathSeparators(new Locale("", "NL")));

		assertNull(ResourceUtil.rejectPathSeparators(new Locale("nl", "N/L")));
		assertNull(ResourceUtil.rejectPathSeparators(new Locale("nl", "N\\L")));
		assertNull(ResourceUtil.rejectPathSeparators(new Locale("nl", "..")));
		assertNull(ResourceUtil.rejectPathSeparators(new Locale("nl", "N\0L")));
		assertNull(ResourceUtil.rejectPathSeparators(new Locale("a\\b", "NL")));

		// a single dot is a legal subtag character; only a doubled one escapes the directory
		assertEquals(new Locale("a.b", "NL"),
			ResourceUtil.rejectPathSeparators(new Locale("a.b", "NL")));
	}

	/**
	 * A locale carrying a variant, a script or extensions is validated against its
	 * {@link Locale#toString()}, which drops some subtags - a variant without a language or country
	 * among them - so the two routes must agree on what reaches the path.
	 */
	@Test
	void rejectPathSeparatorsForRicherLocales() throws Exception
	{
		Locale variant = new Locale("nl", "NL", "vlaams");
		assertEquals(variant, ResourceUtil.rejectPathSeparators(variant));
		assertNull(ResourceUtil.rejectPathSeparators(new Locale("nl", "NL", "a/b")));

		Locale script = new Locale.Builder().setLanguage("zh").setScript("Hans").build();
		assertEquals(script, ResourceUtil.rejectPathSeparators(script));

		Locale extension =
			new Locale.Builder().setLanguage("nl").setRegion("NL").setExtension('u', "ca-buddhist").build();
		assertEquals(extension, ResourceUtil.rejectPathSeparators(extension));

		// Locale#toString() omits a variant that has neither a language nor a country, so it never
		// reaches the lookup path and must not cause the locale to be dropped
		Locale strayVariant = new Locale("", "", "a/b");
		assertEquals("", strayVariant.toString());
		assertEquals(strayVariant, ResourceUtil.rejectPathSeparators(strayVariant));
	}

	/**
	 * A locale, style or variation carrying a path separator is dropped: each becomes a single
	 * component of the resource lookup path, so a separator would make the lookup resolve in a
	 * different directory than the resource it belongs to.
	 */
	@Test
	void decodeResourceReferenceAttributesRejectsPathSeparators() throws Exception
	{
		for (String value : new String[] { "../../etc", "..\\..\\etc", "a/b", "a\\b", "..",
			"a\0b" })
		{
			UrlAttributes attributes = ResourceUtil.decodeResourceReferenceAttributes(value);
			assertNull(attributes.getLocale(), "locale should be dropped for '" + value + "'");

			attributes = ResourceUtil.decodeResourceReferenceAttributes("en-" + value);
			assertEquals(Locale.ENGLISH, attributes.getLocale());
			assertNull(attributes.getStyle(), "style should be dropped for '" + value + "'");

			attributes = ResourceUtil.decodeResourceReferenceAttributes("en-style-" + value);
			assertEquals(Locale.ENGLISH, attributes.getLocale());
			assertEquals("style", attributes.getStyle());
			assertNull(attributes.getVariation(),
				"variation should be dropped for '" + value + "'");
		}
	}

	/**
	 * The separator check runs after {@code ~} has been restored to {@code -}, and must not disturb
	 * that restoration.
	 */
	@Test
	void decodeResourceReferenceAttributesKeepsEscapedSeparator() throws Exception
	{
		UrlAttributes attributes =
			ResourceUtil.decodeResourceReferenceAttributes("en-my~style-my~variation");

		assertEquals(Locale.ENGLISH, attributes.getLocale());
		assertEquals("my-style", attributes.getStyle());
		assertEquals("my-variation", attributes.getVariation());
	}

	@Test
	void decodeResourceReferenceAttributesWithUrl() throws Exception
	{
		Url url = Url.parse("www.funny.url/?param1=value1");
		UrlAttributes attributes = ResourceUtil.decodeResourceReferenceAttributes(url);

		assertEquals(new UrlAttributes(null, null, null), attributes);

		url = Url.parse("www.funny.url/?de_DE");
		attributes = ResourceUtil.decodeResourceReferenceAttributes(url);
		assertEquals(Locale.GERMANY, attributes.getLocale());
		assertNull(attributes.getStyle());
		assertNull(attributes.getVariation());

		url = Url.parse("www.funny.url/?-style");
		attributes = ResourceUtil.decodeResourceReferenceAttributes(url);
		assertNull(attributes.getLocale());
		assertEquals("style", attributes.getStyle());
		assertNull(attributes.getVariation());
	}

	@Test
	void encodeResourceReferenceAttributes() throws Exception
	{
		UrlAttributes attributes = new UrlAttributes(null, null, null);
		assertNull(ResourceUtil.encodeResourceReferenceAttributes(attributes));

		attributes = new UrlAttributes(Locale.CANADA_FRENCH, "style", "variation");
		
		assertEquals("fr_CA-style-variation", ResourceUtil.encodeResourceReferenceAttributes(attributes));
		
		attributes = new UrlAttributes(null, null, "variation");
		
		assertEquals("--variation", ResourceUtil.encodeResourceReferenceAttributes(attributes));
	}

	@Test
	void encodeResourceReferenceAttributesWithResource() throws Exception
	{
		ResourceReference resourceReference = Mockito.mock(ResourceReference.class);

		//test with all null attributes
		UrlAttributes attributes = new UrlAttributes(null, null, null);
		
		String urlString = "www.funny.url";
		Url url = Url.parse(urlString);
		
		Mockito.when(resourceReference.getUrlAttributes()).thenReturn(attributes);
		ResourceUtil.encodeResourceReferenceAttributes(url, resourceReference);
		
		assertEquals(urlString, url.toString());
		
		Mockito.reset(resourceReference);
		
		//test with locale, style and variation
		attributes = new UrlAttributes(Locale.CANADA_FRENCH, "style", "variation");
		
		Mockito.when(resourceReference.getUrlAttributes()).thenReturn(attributes);
		ResourceUtil.encodeResourceReferenceAttributes(url, resourceReference);
		
		assertEquals(urlString + "?fr_CA-style-variation", url.toString());
		
		Mockito.reset(resourceReference);
		
		//test with just variation
		attributes = new UrlAttributes(null, null, "variation");
		url = Url.parse(urlString);
		
		Mockito.when(resourceReference.getUrlAttributes()).thenReturn(attributes);
		ResourceUtil.encodeResourceReferenceAttributes(url, resourceReference);
		
		assertEquals(urlString + "?--variation", url.toString());
	}
}
