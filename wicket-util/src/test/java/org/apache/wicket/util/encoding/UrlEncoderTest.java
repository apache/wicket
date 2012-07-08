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

import org.apache.wicket.util.crypt.CharEncoding;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link org.apache.wicket.util.encoding.UrlDecoder}
 */
public class UrlEncoderTest extends Assert
{

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3721">WICKET-3721</a> Encode
	 * apostrophes because otherwise they get XML encoded by ComponentTag#writeOutput() to
	 * &amp;#039; and eventually break links with javascript:
	 */
	@Test
	public void encodeApostrophe()
	{
		assertEquals("someone%27s%20bad%20url",
			UrlEncoder.FULL_PATH_INSTANCE.encode("someone's bad url", CharEncoding.UTF_8));
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
			CharEncoding.UTF_8);
		assertEquals("path;jsessionid=1234567890", encoded);
	}

	@Test
	public void dontStopOnNullByte() throws Exception
	{
		assertEquals("someone%27s%20badNULL%20url",
			UrlEncoder.FULL_PATH_INSTANCE.encode("someone's bad\0 url", CharEncoding.UTF_8));
	}
}
