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
package org.apache.wicket.util.resource;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.wicket.util.lang.Bytes;

/**
 * 
 * @author Kent Tong
 */
public class UrlResourceStreamTest extends TestCase
{
	/**
	 * lastModified() shouldn't change the content length if the file isn't really changed.
	 * 
	 * @throws IOException
	 */
	public void testLastModifiedForResourceInJar() throws IOException
	{
		String anyClassInJarFile = "/java/lang/String.class";
		URL url = getClass().getResource(anyClassInJarFile);
		UrlResourceStream stream = new UrlResourceStream(url);
		Bytes length = stream.length();
		stream.lastModifiedTime();
		assertEquals(stream.length(), length);
		stream.close();
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3176
	 * 
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	public void testLoadJustOnce() throws IOException, ResourceStreamNotFoundException
	{
		String anyClassInJarFile = "/java/lang/String.class";
		URL realURL = getClass().getResource(anyClassInJarFile);

		final AtomicInteger counter = new AtomicInteger(0);
		URL url = new URL(null, "test://anything", new CountingURLStreamHandler(realURL, counter));

		UrlResourceStream countingStream = new UrlResourceStream(url);
		// assert the call is not made yet
		assertEquals(0, counter.get());
		countingStream.length();
		// assert the connection is loaded lazily
		assertEquals(1, counter.get());

		// assert the following calls do not make new connections
		countingStream.getInputStream();
		assertEquals(1, counter.get());
		countingStream.getContentType();
		assertEquals(1, counter.get());
		countingStream.getInputStream();
		assertEquals(1, counter.get());
		countingStream.close();
		assertEquals(1, counter.get());

		// assert the connection is re-opened (again lazily) second time
		countingStream.length();
		assertEquals(2, counter.get());
	}


	/**
	 * {@link URLStreamHandler} that counts the calls to {@link URL#openConnection()}
	 */
	private static final class CountingURLStreamHandler extends URLStreamHandler
	{
		private final AtomicInteger counter;

		private final URL realURL;

		public CountingURLStreamHandler(URL realURL, AtomicInteger counter)
		{
			this.counter = counter;
			this.realURL = realURL;
		}

		@Override
		protected URLConnection openConnection(URL u) throws IOException
		{
			counter.getAndIncrement();
			return realURL.openConnection();
		}

	}
}
