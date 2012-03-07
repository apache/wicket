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
package org.apache.wicket.util.string;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.wicket.core.util.string.UrlUtils;
import org.junit.Test;

/**
 * Tests for {@link UrlUtils}
 */
public class UrlUtilsTest
{

	/**	 */
	@Test
	public void isRelative()
	{
		assertTrue(UrlUtils.isRelative("./mypage?return=http://example.com"));
		assertTrue(UrlUtils.isRelative("./path/path2?param1=value1"));
		assertFalse(UrlUtils.isRelative("http://example.com"));
		assertFalse(UrlUtils.isRelative("https://example.com"));
		assertFalse(UrlUtils.isRelative("ftp://example.com"));
	}

	/**	 */
	@Test
	public void normalizePath()
	{
		// test basic normalization
		assertEquals("/foo/bar", UrlUtils.normalizePath("foo/bar"));
		assertEquals("/foo/bar", UrlUtils.normalizePath("foo/bar/"));
		assertEquals("/foo/bar", UrlUtils.normalizePath("/foo/bar"));
		assertEquals("/foo/bar", UrlUtils.normalizePath("/foo/bar/"));

		// test empty string normalization
		assertEquals("", UrlUtils.normalizePath(null));
		assertEquals("", UrlUtils.normalizePath(""));
		assertEquals("", UrlUtils.normalizePath("/"));

		// test trimming
		assertEquals("", UrlUtils.normalizePath(" / "));
		assertEquals("/foo/bar", UrlUtils.normalizePath("  foo/bar/  "));
	}
}
