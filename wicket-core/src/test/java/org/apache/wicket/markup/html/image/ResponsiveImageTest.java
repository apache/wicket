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
package org.apache.wicket.markup.html.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class ResponsiveImageTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new PubApplication();
	}

	@Test
	void testSrcSetIsNotAvailableOnDefaultUsage()
	{
		tester.startPage(ImageResourceReferenceTestPage.class);
		String lastResponseAsString = tester.getLastResponse().getDocument();
		TagTester createTagByAttribute = TagTester.createTagByAttribute(lastResponseAsString, "img");
		assertFalse(createTagByAttribute.hasAttribute("srcset"));
		assertEquals(Image.Cors.ANONYMOUS.getRealName(), createTagByAttribute.getAttribute("crossorigin"));
	}

	@Test
	void testPictureTagIsRenderedRight()
	{
		tester.startPage(ImagePictureTestPage.class);
		String lastResponseAsString = tester.getLastResponse().getDocument();
		TagTester pictureTagTester = TagTester.createTagByAttribute(lastResponseAsString, "picture");
		assertTrue(pictureTagTester.hasChildTag("img"));
		assertTrue(pictureTagTester.hasChildTag("source"));
		TagTester sourceTagTester = TagTester.createTagByAttribute(lastResponseAsString, "source");
		assertTrue(sourceTagTester.hasAttribute("media"));
		assertEquals("(min-width: 650px)", sourceTagTester.getAttribute("media"));
		assertEquals("(min-width: 50em) 33vw", sourceTagTester.getAttribute("sizes"));
	}

	@Test
	void testImageTagIsRenderedWithXValuesAndSrcSet()
	{
		tester.startPage(ImageSrcSetTestPage.class);
		String lastResponseAsString = tester.getLastResponse().getDocument();
		TagTester imgTagTester = TagTester.createTagByAttribute(lastResponseAsString, "img");
		assertTrue(imgTagTester.hasAttribute("src"));
		assertTrue(imgTagTester.hasAttribute("srcset"));
		String attribute = imgTagTester.getAttribute("srcset");
		String[] srcSetElements = attribute.split(",");
		int i = 0;
		for (String srcSetElement : srcSetElements)
		{
			if (i == 0)
			{
				assertTrue(srcSetElement.endsWith("320w"));
			}
			if (i == 1)
			{
				assertTrue(srcSetElement.endsWith("2x"));
			}
			if (i == 2)
			{
				assertTrue(srcSetElement.endsWith("900w"));
			}
			i++;
		}
		assertEquals("(min-width: 50em) 33vw,(min-width: 28em) 50vw,100vw",
			imgTagTester.getAttribute("sizes"));
	}

}
