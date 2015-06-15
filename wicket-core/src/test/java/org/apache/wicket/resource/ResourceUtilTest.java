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

import java.util.Locale;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceReference.UrlAttributes;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


public class ResourceUtilTest extends Assert
{
	@Test
	public void decodeResourceReferenceAttributesWithString() throws Exception
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
	public void decodeResourceReferenceAttributesWithUrl() throws Exception
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
	public void encodeResourceReferenceAttributes() throws Exception
	{
		UrlAttributes attributes = new UrlAttributes(null, null, null);
		assertNull(ResourceUtil.encodeResourceReferenceAttributes(attributes));
		
		attributes = new UrlAttributes(Locale.CANADA_FRENCH, "style", "variation");
		
		assertEquals("fr_CA-style-variation", ResourceUtil.encodeResourceReferenceAttributes(attributes));
		
		attributes = new UrlAttributes(null, null, "variation");
		
		assertEquals("--variation", ResourceUtil.encodeResourceReferenceAttributes(attributes));
	}
	
	@Test
	public void encodeResourceReferenceAttributesWithResource() throws Exception
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
