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
package org.apache.wicket.protocol.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;

import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Johan
 */
class RequestUtilsTest
{

	private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

	/**
	 * 
	 */
	@Test
	void doubleDotsMiddle()
	{
		assertEquals("/a/b", RequestUtils.removeDoubleDots("/a/b/../b"));
		assertEquals("a/b", RequestUtils.removeDoubleDots("a/b/../b"));
		assertEquals("a/b/", RequestUtils.removeDoubleDots("a/b/../b/"));
	}

	/**
	 * 
	 */
	@Test
	void doubleDotsEnd()
	{
		assertEquals("/a/b", RequestUtils.removeDoubleDots("/a/b/c/.."));
		assertEquals("a/b", RequestUtils.removeDoubleDots("a/b/c/.."));
	}

	/**
	 * 
	 */
	@Test
	void doubleDotsStart()
	{
		assertEquals("/../a/b", RequestUtils.removeDoubleDots("/../a/b"));
		assertEquals("../a/b", RequestUtils.removeDoubleDots("../a/b"));
	}

	/**
	 * 
	 */
	@Test
	void emptyDoubleDots()
	{
		assertEquals("", RequestUtils.removeDoubleDots(""));
	}

	/**
	 * 
	 */
	@Test
	void oneDoubleDots()
	{
		assertEquals("..", RequestUtils.removeDoubleDots(".."));
		assertEquals("../", RequestUtils.removeDoubleDots("../"));
		assertEquals("/..", RequestUtils.removeDoubleDots("/.."));
	}

	/**
	 * 
	 */
	@Test
	void toAbsolutePath()
	{
		assertEquals(RequestUtils.toAbsolutePath("http://aif.ru/test/test", "../blah/zzz"),
			"http://aif.ru/blah/zzz");
		assertEquals(RequestUtils.toAbsolutePath("http://aif.ru/test", "blah/zzz"),
			"http://aif.ru/blah/zzz");
		assertEquals(RequestUtils.toAbsolutePath("http://aif.ru/test/", "../blah/zzz"),
			"http://aif.ru/blah/zzz");
		assertEquals(RequestUtils.toAbsolutePath("http://aif.ru/blah/test", "zzz"),
			"http://aif.ru/blah/zzz");
		assertEquals(RequestUtils.toAbsolutePath("http://aif.ru/blah/test", "./zzz"),
			"http://aif.ru/blah/zzz");
		assertEquals(RequestUtils.toAbsolutePath("http://aif.ru/blah/test", "./"),
			"http://aif.ru/blah/");
		assertEquals(RequestUtils.toAbsolutePath("http://aif.ru/", "./"), "http://aif.ru/");

	}

	/**
	 * WICKET-4664 - remove leading ? if present
	 */
	@Test
	void removeLeadingQuestionMark_simpleParam()
	{
		final PageParameters params = new PageParameters();
		RequestUtils.decodeParameters("?key=value", params, UTF_8_CHARSET);
		assertEquals("value", params.get("key").toString());
	}

	/**
	 * WICKET-4664 - remove leading ? if present
	 */
	@Test
	void removeLeadingQuestionMark_simpleParamWithoutValueAndAnotherParam()
	{
		final PageParameters params = new PageParameters();
		RequestUtils.decodeParameters("?123&key=value", params, UTF_8_CHARSET);
		assertEquals("", params.get("123").toString());
		assertEquals("value", params.get("key").toString());
	}

	/**
	 * WICKET-4664 - remove leading ? if present
	 */
	@Test
	void removeLeadingQuestionMark_simpleParamWithoutValue()
	{
		final PageParameters params = new PageParameters();
		RequestUtils.decodeParameters("?123", params, UTF_8_CHARSET);
		assertEquals("", params.get("123").toString());
	}

	/**
	 *
	 */
	@Test
	void decodeParam_simpleParam_noQuestionMark()
	{
		final PageParameters params = new PageParameters();
		RequestUtils.decodeParameters("key=value", params, UTF_8_CHARSET);
		assertEquals("value", params.get("key").toString());
	}

	/**
	 *
	 */
	@Test
	void decodeParam_simpleParamWithoutValueAndAnotherParam_NoQuestionMark()
	{
		final PageParameters params = new PageParameters();
		RequestUtils.decodeParameters("123&key=value", params, UTF_8_CHARSET);
		assertEquals("", params.get("123").toString());
		assertEquals("value", params.get("key").toString());
	}

	/**
	 *
	 */
	@Test
	void decodeParam_simpleParamWithoutValue_NoQuestionMark()
	{
		final PageParameters params = new PageParameters();
		RequestUtils.decodeParameters("123", params, UTF_8_CHARSET);
		assertEquals("", params.get("123").toString());
	}


	@Test
	void charset() throws Exception
	{
		MockHttpServletRequest request = new MockHttpServletRequest(null, null, null);

		request.setCharacterEncoding("UTF-8");
		assertEquals(Charset.forName("UTF-8"), RequestUtils.getCharset(request));

		request.setCharacterEncoding("FOO");
		assertEquals(Charset.forName("UTF-8"), RequestUtils.getCharset(request));
	}
}
