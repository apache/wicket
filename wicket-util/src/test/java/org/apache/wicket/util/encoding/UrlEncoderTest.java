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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UrlEncoderTest
{

	// starts with &auml;
	private static final char[] ENCODING_CANDIDATES = "\u00c4!\"§$%&/()=?`*'_:;><,.-#+´\\}][{|"
		.toCharArray();
	private static final Charset CHARSET = StandardCharsets.UTF_8;

	@Test
	public void pathUnencoded()  {
		String unencoded = "azAZ09.-_~!$&*+,;=:@";
		
		assertEquals(unencoded, UrlEncoder.PATH_INSTANCE.encode(unencoded, CHARSET));
		
		for (char candidate : ENCODING_CANDIDATES)
		{
			if (unencoded.indexOf(candidate) == -1) {
				assertNotEquals("" + candidate,
					UrlEncoder.PATH_INSTANCE.encode("" + candidate, CHARSET));
			}
		}
	}

	@Test
	public void queryStringUnencoded()  {
		String unencoded = "azAZ09.-_~!$*,:@/";
		
		assertEquals(unencoded, UrlEncoder.QUERY_INSTANCE.encode(unencoded, CHARSET));

		for (char candidate : ENCODING_CANDIDATES)
		{
			if (unencoded.indexOf(candidate) == -1) {
				assertNotEquals("" + candidate,
					UrlEncoder.QUERY_INSTANCE.encode("" + candidate, CHARSET));
			}
		}
	}
	
	@Test
	public void headerUnencoded()  {
		String unencoded = "azAZ09.-_~!$&+#^`|";
		
		assertEquals(unencoded, UrlEncoder.HEADER_INSTANCE.encode(unencoded, CHARSET));
		
		for (char candidate : ENCODING_CANDIDATES)
		{
			if (unencoded.indexOf(candidate) == -1) {
				assertNotEquals("" + candidate,
					UrlEncoder.HEADER_INSTANCE.encode("" + candidate, CHARSET));
			}
		}
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3721">WICKET-3721</a> Encode
	 * apostrophes because otherwise they get XML encoded by ComponentTag#writeOutput() to
	 * &amp;#039; and eventually break links with javascript:
	 */
	@Test
	public void encodeApostrophe()
	{
		assertEquals("someone%27s%20bad%20url",
			UrlEncoder.PATH_INSTANCE.encode("someone's bad url", CHARSET));
	}

	/**
	 * Do not encode semicolon in the Url's path because it is used in ';jsessionid=...'
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-4409
	 */
	@Test
	public void dontEncodeSemicolon()
	{
		String encoded = UrlEncoder.PATH_INSTANCE.encode("path;jsessionid=1234567890",
			CHARSET);
		assertEquals("path;jsessionid=1234567890", encoded);
	}

	@Test
	public void dontStopOnNullByte() throws Exception
	{
		assertEquals("someone%27s%20badNULL%20url",
			UrlEncoder.PATH_INSTANCE.encode("someone's bad\0 url", CHARSET));
	}

	@Test
	public void encodePath()
	{
		assertEquals("", UrlEncoder.PATH_INSTANCE.encode("", CHARSET));
		assertEquals("foobar", UrlEncoder.PATH_INSTANCE.encode("foobar", CHARSET));
		assertEquals("%2Ffoo%2Fbar", UrlEncoder.PATH_INSTANCE.encode("/foo/bar", CHARSET));
	}

	@Test
	public void encodeQuery()
	{
		assertEquals("", UrlEncoder.QUERY_INSTANCE.encode("", CHARSET));
		assertEquals("foobar", UrlEncoder.QUERY_INSTANCE.encode("foobar", CHARSET));
		assertEquals("foo+bar", UrlEncoder.QUERY_INSTANCE.encode("foo bar", CHARSET));
		assertEquals("foo%26bar", UrlEncoder.QUERY_INSTANCE.encode("foo&bar", CHARSET));
	}
}
