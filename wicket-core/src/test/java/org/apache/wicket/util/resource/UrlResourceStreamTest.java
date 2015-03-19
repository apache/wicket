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
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.util.lang.Bytes;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kent Tong
 */
public class UrlResourceStreamTest extends Assert {
	/**
	 * lastModified() shouldn't change the content length if the file isn't really changed.
	 *
	 * @throws IOException
	 */
	@Test
	public void lastModifiedForResourceInJar() throws IOException {
		String anyClassInJarFile = "/java/lang/String.class";
		URL url = getClass().getResource(anyClassInJarFile);
		UrlResourceStream stream = new UrlResourceStream(url);
		Bytes length = stream.length();
		stream.lastModifiedTime();
		assertEquals(stream.length(), length);
		stream.close();
	}

	/**
	 * Verifies that a connection is opened just once but each #getInputStream() opens a new one
	 * and all input streams are closed with UrlResourceStream#close()
	 *
	 * https://issues.apache.org/jira/browse/WICKET-3176
	 * https://issues.apache.org/jira/browse/WICKET-4293
	 *
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	@Test
	public void loadJustOnce() throws IOException, ResourceStreamNotFoundException {
		String anyClassInJarFile = "/java/lang/String.class";
		URL realURL = getClass().getResource(anyClassInJarFile);

		final AtomicInteger connectCounter = new AtomicInteger(0);
		final AtomicInteger streamCounter = new AtomicInteger(0);
		URL url = new URL(null, "test://anything", new CountingURLStreamHandler(realURL,
				connectCounter, streamCounter));

		UrlResourceStream countingStream = new UrlResourceStream(url);

		// assert the call is not made yet
		assertEquals(0, connectCounter.get());
		assertEquals(0, streamCounter.get());

		// assert the connection is loaded lazily
		countingStream.length();
		assertEquals(1, connectCounter.get());
		assertEquals(0, streamCounter.get());

		// assert the following calls do not make new connections
		countingStream.getInputStream();
		assertEquals(1, connectCounter.get());
		assertEquals(1, streamCounter.get());
		countingStream.getContentType();
		assertEquals(1, connectCounter.get());
		assertEquals(1, streamCounter.get());
		countingStream.getInputStream();
		assertEquals(1, connectCounter.get());
		assertEquals(2, streamCounter.get());
		countingStream.close();

		assertEquals(1, connectCounter.get());
		assertEquals(2, streamCounter.get());

		// assert the connection is re-opened (again lazily) second time,
		// but stream is not re-opened yet
		countingStream.length();

		assertEquals(2, connectCounter.get());
		assertEquals(2, streamCounter.get());

		// assert stream is re-opened on next getInputStream call
		countingStream.getInputStream();
		assertEquals(2, connectCounter.get());
		assertEquals(3, streamCounter.get());
	}


	/**
	 * {@link URLStreamHandler} that counts the calls to {@link URL#openConnection()}
	 */
	private static final class CountingURLStreamHandler extends URLStreamHandler {
		private final AtomicInteger connectCounter, streamCounter;

		private final URL realURL;

		public CountingURLStreamHandler(URL realURL, AtomicInteger connectCounter,
										AtomicInteger streamCounter) {
			this.connectCounter = connectCounter;
			this.streamCounter = streamCounter;
			this.realURL = realURL;
		}

		@Override
		protected URLConnection openConnection(URL u) throws IOException {
			connectCounter.getAndIncrement();

			final URLConnection realConn = realURL.openConnection();
			return new URLConnection(u) {

				@Override
				public void connect() throws IOException {
					realConn.connect();
				}

				@Override
				public InputStream getInputStream() throws IOException {
					streamCounter.incrementAndGet();
					return realConn.getInputStream();
				}
			};
		}
	}
}
