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
package org.apache.wicket.util.resource.locator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.core.util.resource.locator.ExtensionResourceNameIterator;
import org.apache.wicket.core.util.resource.locator.LocaleResourceNameIterator;
import org.apache.wicket.core.util.resource.locator.ResourceNameIterator;
import org.apache.wicket.core.util.resource.locator.StyleAndVariationResourceNameIterator;
import org.junit.Test;


/**
 * @author Pedro Santos
 */
public class ResourceNameIteratorTest extends WicketTestCase
{
	/**
	 * Asserting no duplicated locale sufix get iterated
	 */
	@Test
	public void localeResourceNameIterator()
	{
		Locale locale = new Locale("a", "b", "c");
		LocaleResourceNameIterator iterator = new LocaleResourceNameIterator(locale, false);
		HashSet<String> variations = new HashSet<String>();
		while (iterator.hasNext())
		{
			assertTrue(variations.add(iterator.next()));
		}
		assertEquals(4, variations.size());
		assertTrue(variations.contains("_a_B_c"));
		assertTrue(variations.contains("_a_B"));
		assertTrue(variations.contains("_a"));
		assertTrue(variations.contains(""));

		locale = new Locale("a", "b");
		iterator = new LocaleResourceNameIterator(locale, false);
		variations = new HashSet<String>();
		while (iterator.hasNext())
		{
			assertTrue(variations.add(iterator.next()));
		}
		assertEquals(3, variations.size());
		assertTrue(variations.contains("_a_B"));
		assertTrue(variations.contains("_a"));
		assertTrue(variations.contains(""));

		locale = new Locale("a");
		iterator = new LocaleResourceNameIterator(locale, false);
		variations = new HashSet<String>();
		while (iterator.hasNext())
		{
			assertTrue(variations.add(iterator.next()));
		}
		assertEquals(2, variations.size());
		assertTrue(variations.contains("_a"));
		assertTrue(variations.contains(""));
	}

	/**
	 * 
	 */
	@Test
	public void styleAndVariationResourceNameIterator()
	{
		StyleAndVariationResourceNameIterator iterator = new StyleAndVariationResourceNameIterator(
			null, null);
		assertTrue(iterator.hasNext());
		iterator.next();
		assertFalse(iterator.hasNext());

		iterator = new StyleAndVariationResourceNameIterator("style", null);
		assertTrue(iterator.hasNext());
		iterator.next();
		assertEquals("style", iterator.getStyle());
		assertEquals(null, iterator.getVariation());
		iterator.next();
		assertEquals(null, iterator.getStyle());
		assertEquals(null, iterator.getVariation());
		assertFalse(iterator.hasNext());

		iterator = new StyleAndVariationResourceNameIterator("style", "variation");
		assertTrue(iterator.hasNext());
		iterator.next();
		assertEquals("style", iterator.getStyle());
		assertEquals("variation", iterator.getVariation());
		iterator.next();
		assertEquals("style", iterator.getStyle());
		assertEquals(null, iterator.getVariation());
		iterator.next();
		assertEquals(null, iterator.getStyle());
		assertEquals("variation", iterator.getVariation());
		iterator.next();
		assertEquals(null, iterator.getStyle());
		assertEquals(null, iterator.getVariation());
		assertFalse(iterator.hasNext());
	}

	/**
	 * 
	 */
	@Test
	public void extensionResourceNameIterator()
	{
		ExtensionResourceNameIterator iterator = new ExtensionResourceNameIterator(null);
		assertTrue(iterator.hasNext());
		assertEquals(null, iterator.next());
		assertFalse(iterator.hasNext());

		iterator = new ExtensionResourceNameIterator(Arrays.asList("txt"));
		assertTrue(iterator.hasNext());
		assertEquals("txt", iterator.next());
		assertFalse(iterator.hasNext());

		iterator = new ExtensionResourceNameIterator(Arrays.asList("properties", "utf8.properties", "properties.xml"));
		assertTrue(iterator.hasNext());
		assertEquals("properties", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("utf8.properties", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("properties.xml", iterator.next());
		assertFalse(iterator.hasNext());
	}

	/**
	 * 
	 */
	@Test
	public void noDuplicateVariations()
	{
		String path = "patch.extension";
		String style = null;
		String var = "var";
		Locale locale = Locale.getDefault();
		Iterable<String> extensions = null;
		boolean strict = false;
		Iterator<String> iterator = new ResourceNameIterator(path, style, var, locale, extensions, strict);
		HashSet<String> variations = new HashSet<String>();
		while (iterator.hasNext())
		{
			assertTrue(variations.add(iterator.next()));
		}
		assertEquals(6, variations.size());
	}

	/**
	 * 
	 */
	@Test
	public void noTrailingDotWhenNoExtension()
	{
		Iterator<String> iterator = new ResourceNameIterator("foo", null, null, null, null, false);

		assertTrue(iterator.hasNext());

		assertEquals("foo", iterator.next());

		assertFalse(iterator.hasNext());
	}
}
