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
package org.apache.wicket.core.util.resource.locator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.core.util.resource.ClassPathResourceFinder;
import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.core.util.resource.locator.caching.CachingResourceStreamLocator;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.jupiter.api.Test;

/**
 * Tests for CachingResourceStreamLocator
 *
 * <a href="https://issues.apache.org/jira/browse/WICKET-3511">WICKET-3511</a>
 */
class CachingResourceStreamLocatorTest
{

	/**
	 * Tests NullResourceStreamReference
	 */
	@Test
	void notExistingResource()
	{

		IResourceStreamLocator resourceStreamLocator = mock(IResourceStreamLocator.class);

		CachingResourceStreamLocator cachingLocator = new CachingResourceStreamLocator(
			resourceStreamLocator);

		cachingLocator.locate(String.class, "path");
		cachingLocator.locate(String.class, "path");

		// there is no resource with that Key so a "miss" will be cached and expect 1 call to the
		// delegate
		verify(resourceStreamLocator, times(1)).locate(String.class, "path");
	}

	/**
	 * Tests strict before non-strict matching without a specific locale.
	 */
	@Test
	void strictBeforeNonStrictMatchingWithoutLocaleDoesntResultInInvalidNonStrictMatch()
	{
		IResourceStreamLocator resourceStreamLocator = new ResourceStreamLocator(
			new ClassPathResourceFinder(""));
		CachingResourceStreamLocator cachingLocator = new CachingResourceStreamLocator(
			resourceStreamLocator);

		String style = null;
		String variation = null;
		Locale locale = null;
		String extension = null;

		String filename = "org/apache/wicket/ajax/res/js/wicket-ajax-jquery.js";

		// a strict lookup for the resource with no specific locale results in a match
		IResourceStream strictLocate = cachingLocator.locate(AbstractDefaultAjaxBehavior.class,
			filename, style, variation, locale, extension, true);

		assertNotNull(strictLocate);

		// followed by a non-strict search for the same resource also finds it
		IResourceStream nonStrictLocate = cachingLocator.locate(AbstractDefaultAjaxBehavior.class,
			filename, style, variation, locale, extension, false);

		assertNotNull(nonStrictLocate);
	}

	/**
	 * Tests strict before non-strict matching with a specific locale.
	 */
	@Test
	void strictMatchingDoesntInvalidateNonStrictMatching()
	{
		IResourceStreamLocator resourceStreamLocator = new ResourceStreamLocator(
			new ClassPathResourceFinder(""));
		CachingResourceStreamLocator cachingLocator = new CachingResourceStreamLocator(
			resourceStreamLocator);

		String style = null;
		String variation = null;
		Locale locale = new Locale("nl", "NL");
		String extension = null;

		String filename = "org/apache/wicket/ajax/res/js/wicket-ajax-jquery.js";

		// a strict lookup of a localized resource should not find the non-localized resource
		IResourceStream strictLocate = cachingLocator.locate(AbstractDefaultAjaxBehavior.class,
			filename, style, variation, locale, extension, true);
		assertNull(strictLocate);

		// but a non-strict lookup with a locale should fall back to the non-localized resource
		IResourceStream nonStrictLocate = cachingLocator.locate(AbstractDefaultAjaxBehavior.class,
			filename, style, variation, locale, extension, false);

		assertNotNull(nonStrictLocate);
	}

	/**
	 * Tests non-strict before strict matching with a specific locale.
	 */
	@Test
	void nonStrictMatchingDoesntResultInInvalidStrictMatch()
	{
		IResourceStreamLocator resourceStreamLocator = new ResourceStreamLocator(
			new ClassPathResourceFinder(""));
		CachingResourceStreamLocator cachingLocator = new CachingResourceStreamLocator(
			resourceStreamLocator);

		String style = null;
		String variation = null;
		Locale locale = new Locale("nl", "NL");
		String extension = null;

		String filename = "org/apache/wicket/ajax/res/js/wicket-ajax-jquery.js";

		// a non-strict lookup with a specific locale should find the non-localized resource
		IResourceStream nonStrictLocate = cachingLocator.locate(AbstractDefaultAjaxBehavior.class,
			filename, style, variation, locale, extension, false);

		assertNotNull(nonStrictLocate);

		// but a strict lookup with a specific locale should not fall back to the non-localized
		// resource
		IResourceStream strictLocate = cachingLocator.locate(AbstractDefaultAjaxBehavior.class,
			filename, style, variation, locale, extension, true);

		assertNull(strictLocate);
	}

	/**
	 * Tests FileResourceStreamReference
	 */
	@Test
	void fileResource()
	{
		IResourceStreamLocator resourceStreamLocator = mock(IResourceStreamLocator.class);

		FileResourceStream frs = new FileResourceStream(new File("."));

		when(resourceStreamLocator.locate(String.class, "path", "style", "variation", null,
			"extension", true)).thenReturn(frs);

		CachingResourceStreamLocator cachingLocator = new CachingResourceStreamLocator(
			resourceStreamLocator);

		cachingLocator.locate(String.class, "path", "style", "variation", null, "extension", true);
		cachingLocator.locate(String.class, "path", "style", "variation", null, "extension", true);

		// there is a file resource with that Key so expect just one call to the delegate
		verify(resourceStreamLocator, times(1)).locate(String.class, "path", "style", "variation",
			null, "extension", true);
	}

	/**
	 * Tests two FileResourceStreamReferences with different extensions
	 */
	@Test
	void fileResourceDifferentExtensions()
	{
		IResourceStreamLocator resourceStreamLocator = mock(IResourceStreamLocator.class);

		FileResourceStream frs = new FileResourceStream(new File("."));

		when(resourceStreamLocator.locate(String.class, "path", "style", "variation", null,
			"extension", true)).thenReturn(frs);

		CachingResourceStreamLocator cachingLocator = new CachingResourceStreamLocator(
			resourceStreamLocator);

		cachingLocator.locate(String.class, "path", "style", "variation", null, "extension", true);
		cachingLocator.locate(String.class, "path", "style", "variation", null, "extension", true);
		cachingLocator.locate(String.class, "path", "style", "variation", null, "extension2", true);

		// there is a file resource with that Key so expect just one call to the delegate
		verify(resourceStreamLocator, times(1)).locate(String.class, "path", "style", "variation",
			null, "extension", true);
		verify(resourceStreamLocator, times(1)).locate(String.class, "path", "style", "variation",
			null, "extension2", true);
	}

	/**
	 * Tests UrlResourceStreamReference
	 * 
	 * @throws Exception
	 */
	@Test
	void urlResource() throws Exception
	{
		IResourceStreamLocator resourceStreamLocator = mock(IResourceStreamLocator.class);

		UrlResourceStream urs = new UrlResourceStream(new URL("file:///"));

		when(resourceStreamLocator.locate(String.class, "path")).thenReturn(urs);

		CachingResourceStreamLocator cachingLocator = new CachingResourceStreamLocator(
			resourceStreamLocator);

		cachingLocator.locate(String.class, "path");
		cachingLocator.locate(String.class, "path");

		// there is a url resource with that Key so expect just one call to the delegate
		verify(resourceStreamLocator, times(1)).locate(String.class, "path");
	}

	/**
	 * Tests light weight resource streams (everything but FileResourceStream and
	 * UrlResourceStream). These should <strong>not</strong> be cached.
	 */
	@Test
	void lightweightResource()
	{
		IResourceStreamLocator resourceStreamLocator = mock(IResourceStreamLocator.class);

		StringResourceStream srs = new StringResourceStream("anything");

		when(resourceStreamLocator.locate(String.class, "path", "style", "variation", null,
			"extension", true)).thenReturn(srs);

		CachingResourceStreamLocator cachingLocator = new CachingResourceStreamLocator(
			resourceStreamLocator);

		cachingLocator.locate(String.class, "path", "style", "variation", null, "extension", true);
		cachingLocator.locate(String.class, "path", "style", "variation", null, "extension", true);

		// lightweight resource streams should not be cached so expect just a call to the delegate
		// for each call to the caching locator
		verify(resourceStreamLocator, times(2)).locate(String.class, "path", "style", "variation",
			null, "extension", true);
	}
}
