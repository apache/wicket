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
package org.apache.wicket.request.cycle;

import junit.framework.TestCase;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.UrlRenderer;

/**
 * @author MAtej Knopp
 */
public class UrlRendererTest extends TestCase
{
	/**
	 * 
	 */
	public void test1()
	{
		UrlRenderer r1 = new UrlRenderer(Url.parse("foo/bar/baz?a=b"));
		assertEquals("xyz?x=y", r1.renderUrl(Url.parse("foo/bar/xyz?x=y")));
		assertEquals("baz/xyz?x=y", r1.renderUrl(Url.parse("foo/bar/baz/xyz?x=y")));
		assertEquals("../aaa/xyz?x=y", r1.renderUrl(Url.parse("foo/aaa/xyz?x=y")));
		assertEquals("../../bbb/aaa/xyz?x=y", r1.renderUrl(Url.parse("bbb/aaa/xyz?x=y")));
	}

	/**
	 * 
	 */
	public void test2()
	{
		UrlRenderer r1 = new UrlRenderer(Url.parse("foo/bar/baz?a=b"));
		assertEquals("../../foo?x=y", r1.renderUrl(Url.parse("foo?x=y")));
		assertEquals("../../aaa?x=y", r1.renderUrl(Url.parse("aaa?x=y")));
	}

	/**
	 * 
	 */
	public void test3()
	{
		UrlRenderer r1 = new UrlRenderer(Url.parse("?a=b"));
		assertEquals("a/b/c?x=y", r1.renderUrl(Url.parse("a/b/c?x=y")));
	}

	/**
	 * 
	 */
	public void test5()
	{
		UrlRenderer r1 = new UrlRenderer(Url.parse("url"));
		assertEquals("url?1", r1.renderUrl(Url.parse("url?1")));
	}

	/**
	 * 
	 */
	public void test6()
	{
		UrlRenderer r1 = new UrlRenderer(Url.parse("url/"));
		assertEquals("x?1", r1.renderUrl(Url.parse("url/x?1")));
	}

	/**
	 * 
	 */
	public void test7()
	{
		UrlRenderer r1 = new UrlRenderer(
			Url.parse("MyTestPage/indexed1/indexed2/indexed3?10-27.ILinkListener-l2&p1=v1"));
		assertEquals("../../../MyTestPage?10", r1.renderUrl(Url.parse("MyTestPage?10")));
	}

	/**
	 * 
	 */
	public void test8()
	{
		UrlRenderer r1 = new UrlRenderer(Url.parse("en/first-test-page?16-1.ILinkListener-l1"));
		assertEquals("first-test-page/indexed1/indexed2/indexed3?p1=v1",
			r1.renderUrl(Url.parse("en/first-test-page/indexed1/indexed2/indexed3?p1=v1")));
	}

	/**
	 * 
	 */
	public void test9()
	{
		UrlRenderer r1 = new UrlRenderer(Url.parse("a/b/q/d/e"));
		assertEquals("../../../q/c/d/e", r1.renderUrl(Url.parse("a/q/c/d/e")));
	}

}
