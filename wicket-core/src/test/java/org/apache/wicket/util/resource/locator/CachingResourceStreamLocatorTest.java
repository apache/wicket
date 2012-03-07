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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;

import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.core.util.resource.locator.caching.CachingResourceStreamLocator;
import org.junit.Test;

/**
 * <a href="https://issues.apache.org/jira/browse/WICKET-3511">WICKET-3511</a>
 * 
 * @author mgrigorov
 */
public class CachingResourceStreamLocatorTest
{

	/**
	 * Tests NullResourceStreamReference
	 */
	@Test
	public void notExistingResource()
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
	 * Tests FileResourceStreamReference
	 */
	@Test
	public void fileResource()
	{
		IResourceStreamLocator resourceStreamLocator = mock(IResourceStreamLocator.class);

		FileResourceStream frs = new FileResourceStream(new File("."));

		when(
			resourceStreamLocator.locate(String.class, "path", "style", "variation", null,
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
	 * Tests UrlResourceStreamReference
	 * 
	 * @throws Exception
	 */
	@Test
	public void urlResource() throws Exception
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
	public void lightweightResource()
	{
		IResourceStreamLocator resourceStreamLocator = mock(IResourceStreamLocator.class);

		StringResourceStream srs = new StringResourceStream("anything");

		when(
			resourceStreamLocator.locate(String.class, "path", "style", "variation", null,
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
