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
package org.apache.wicket.util.encoding;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlDecoderTest
{

	private static final Charset CHARSET = StandardCharsets.UTF_8;

	@Test
	public void mustNotEmitNullByteForPath()
	{
		String evil = "http://www.devil.com/highway/to%00hell";
		String decoded = UrlDecoder.PATH_INSTANCE.decode(evil, CHARSET);
		assertEquals(-1, decoded.indexOf('\0'));
		assertEquals("http://www.devil.com/highway/toNULLhell", decoded);
	}

	@Test
	public void mustNotEmitNullByteForQuery()
	{
		String evil = "http://www.devil.com/highway?destination=%00hell";
		String decoded = UrlDecoder.QUERY_INSTANCE.decode(evil, CHARSET);
		assertEquals(-1, decoded.indexOf('\0'));
		assertEquals("http://www.devil.com/highway?destination=NULLhell", decoded);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4803
	 */
	@Test
	public void badUrlEntities()
	{
		String url = "http://localhost/test?a=%%%";
		String decoded = UrlDecoder.QUERY_INSTANCE.decode(url, CHARSET);
		assertEquals("http://localhost/test?a=", decoded);

		url = "http://localhost/test?%%%";
		decoded = UrlDecoder.QUERY_INSTANCE.decode(url, CHARSET);
		assertEquals("http://localhost/test?", decoded);

		url = "http://localhost/test?%a=%b%";
		decoded = UrlDecoder.QUERY_INSTANCE.decode(url, CHARSET);
		assertEquals("http://localhost/test?a=b", decoded);

		url = "foo%2";
		decoded = UrlDecoder.QUERY_INSTANCE.decode(url, CHARSET);
		assertEquals("foo2", decoded);
	}

	@Test
	public void decode()
	{
		assertEquals("", UrlDecoder.QUERY_INSTANCE.decode("", CHARSET));
		assertEquals("foobar", UrlDecoder.QUERY_INSTANCE.decode("foobar", CHARSET));
		assertEquals("foo bar", UrlDecoder.QUERY_INSTANCE.decode("foo%20bar", CHARSET));
		assertEquals("foo+bar", UrlDecoder.QUERY_INSTANCE.decode("foo%2bbar", CHARSET));
		assertEquals("T\u014dky\u014d",
			UrlDecoder.QUERY_INSTANCE.decode("T%C5%8Dky%C5%8D", CHARSET));
		assertEquals("/Z\u00fcrich", UrlDecoder.QUERY_INSTANCE.decode("/Z%C3%BCrich", CHARSET));
		assertEquals("T\u014dky\u014d",
			UrlDecoder.QUERY_INSTANCE.decode("T\u014dky\u014d", CHARSET));
	}

}
