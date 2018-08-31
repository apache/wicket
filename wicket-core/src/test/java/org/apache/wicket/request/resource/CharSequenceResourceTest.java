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
package org.apache.wicket.request.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CharSequenceResource}
 */
class CharSequenceResourceTest
{
	@Test
	void getLength_whenNoUnicodeSymbols_thenReturnTheStringLength() throws Exception
	{
		CharSequenceResource resource = new CharSequenceResource("plain/text");
		assertEquals(Long.valueOf(4L), resource.getLength("abcd"));
	}

	@Test
	void getLength_UTF8_whenUnicodeSymbols_thenReturnTheBytesLength() throws Exception
	{
		CharSequenceResource resource = new CharSequenceResource("plain/text");
		resource.setCharset(Charset.forName("UTF-8"));
		assertEquals(Long.valueOf(5L), resource.getLength("a\u1234d"));
	}

	@Test
	void getLength_UTF16_whenUnicodeSymbols_thenReturnTheBytesLength() throws Exception
	{
		CharSequenceResource resource = new CharSequenceResource("plain/text");
		resource.setCharset(Charset.forName("UTF-16"));
		assertEquals(Long.valueOf(8L), resource.getLength("a\u1234d"));
	}
}
