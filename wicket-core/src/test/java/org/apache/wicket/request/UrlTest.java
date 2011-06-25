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
package org.apache.wicket.request;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.util.lang.WicketObjects;

/**
 * @author Matej Knopp
 * @author Igor Vaynberg
 */

// TODO test removeleadingsegments,prependleadingsegments
// TODO move this test to wicket-request where class Url is located 
//      (once the dependency to WicketObjects is replaced)
public class UrlTest extends TestCase
{
	private void checkSegments(Url url, String... segments)
	{
		assertEquals(Arrays.asList(segments), url.getSegments());
	}

	private void checkQueryParams(Url url, String... params)
	{
		List<QueryParameter> list = new ArrayList<QueryParameter>();
		for (int i = 0; i < params.length; i += 2)
		{
			QueryParameter p = new QueryParameter(params[i], params[i + 1]);
			list.add(p);
		}

		assertEquals(list, url.getQueryParameters());
	}

	/**
	 * 
	 */
	public void testParse1()
	{
		String s = "foo/bar/baz?a=4&b=5";
		Url url = Url.parse(s);
		checkSegments(url, "foo", "bar", "baz");
		checkQueryParams(url, "a", "4", "b", "5");
	}

	/**
	 * 
	 */
	public void testParse2()
	{
		String s = "foo/bar//baz?=4&6";
		Url url = Url.parse(s);
		checkSegments(url, "foo", "bar", "", "baz");
		checkQueryParams(url, "", "4", "6", "");
	}

	/**
	 * 
	 */
	public void testParse3()
	{
		String s = "//foo/bar/";
		Url url = Url.parse(s);
		checkSegments(url, "", "", "foo", "bar", "");
		checkQueryParams(url);
	}

	/**
	 * 
	 */
	public void testParse4()
	{
		String s = "/foo/bar//";
		Url url = Url.parse(s);
		checkSegments(url, "", "foo", "bar", "", "");
		checkQueryParams(url);
	}

	/**
	 * 
	 */
	public void testParse5()
	{
		String s = "foo/b%3Dr/b%26z/x%3F?a=b&x%3F%264=y%3Dz";
		Url url = Url.parse(s);
		checkSegments(url, "foo", "b=r", "b&z", "x?");
		checkQueryParams(url, "a", "b", "x?&4", "y=z");
	}

	/**
	 * 
	 */
	public void testParse6()
	{
		String s = "";
		Url url = Url.parse(s);
		checkSegments(url);
		checkQueryParams(url);
	}

	/**
	 * 
	 */
	public void testParse7()
	{
		String s = "?a=b";
		Url url = Url.parse(s);
		checkSegments(url);
		checkQueryParams(url, "a", "b");
	}

	/**
	 * 
	 */
	public void testParse8()
	{
		String s = "/";
		Url url = Url.parse(s);
		checkSegments(url, "", "");
		checkQueryParams(url);
	}

	/**
	 * 
	 */
	public void testParse9()
	{
		String s = "/?a=b";
		Url url = Url.parse(s);
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b");
	}

	/**
	 * 
	 */
	public void testRender1()
	{
		Url url = new Url();
		url.getSegments().add("foo");
		url.getSegments().add("b=r");
		url.getSegments().add("b&z");
		url.getSegments().add("x?");
		url.setQueryParameter("a", "b");
		url.setQueryParameter("x?&4", "y=z");

		assertEquals("foo/b=r/b&z/x%3F?a=b&x?%264=y%3Dz", url.toString());
	}

	/**
	 * 
	 */
	public void testRender2()
	{
		String s = "/absolute/url";
		Url url = Url.parse(s);
		assertEquals(url.toString(), s);
	}

	/**
	 * 
	 */
	public void testRender3()
	{
		String s = "//absolute/url";
		Url url = Url.parse(s);
		assertEquals(url.toString(), s);
	}

	/**
	 * 
	 */
	public void testRender4()
	{
		String s = "/";
		Url url = Url.parse(s);
		assertEquals(url.toString(), s);
	}

	/**
	 * 
	 */
	public void testAbsolute1()
	{
		Url url = Url.parse("abc/efg");
		assertFalse(url.isAbsolute());
	}

	/**
	 * 
	 */
	public void testAbsolute2()
	{
		Url url = Url.parse("");
		assertFalse(url.isAbsolute());
	}

	/**
	 * 
	 */
	public void testAbsolute3()
	{
		Url url = Url.parse("/");
		assertTrue(url.isAbsolute());
	}

	/**
	 * 
	 */
	public void testAbsolute4()
	{
		Url url = Url.parse("/abc/efg");
		assertTrue(url.isAbsolute());
	}

	/**
	 * 
	 */
	public void testConcat1()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("xx", "yy"));
		assertEquals(Url.parse("abc/xx/yy"), url);
	}

	/**
	 * 
	 */
	public void testConcat2()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList(".", "..", "xx", "yy"));
		assertEquals(Url.parse("xx/yy"), url);
	}

	/**
	 * 
	 */
	public void testConcat3()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("..", "..", "xx", "yy"));
		assertEquals(Url.parse("xx/yy"), url);
	}

	/**
	 * 
	 */
	public void testConcat4()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("..", "..", "..", "xx", "yy"));
		assertEquals(Url.parse("../xx/yy"), url);
	}

	/**
	 * 
	 */
	public void testConcat5()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList("xx", "yy"));
		assertEquals(Url.parse("abc/efg/xx/yy"), url);
	}

	/**
	 * 
	 */
	public void testConcat6()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList(".."));
		assertEquals(Url.parse("abc/"), url);
	}

	/**
	 * 
	 */
	public void testConcat7()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList("..", ".."));
		assertEquals(Url.parse(""), url);
	}


	/**
	 * 
	 */
	public void testConcat8()
	{
		Url url = Url.parse("fff/abc/efg/xxx");
		url.concatSegments(Arrays.asList(".."));
		assertEquals(Url.parse("fff/abc/"), url);
	}


	/**
	 * 
	 */
	public void testConcat9()
	{
		Url url = Url.parse("fff/abc/efg/xxx");
		url.concatSegments(Arrays.asList("..", ".."));
		assertEquals(Url.parse("fff/"), url);
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3363">WICKET-3363</a>
	 */
	public void testResolveRelative1()
	{
		Url relative = Url.parse("./a/b?p1=v1");
		Url baseUrl = Url.parse("c/d?p2=v2");
		baseUrl.resolveRelative(relative);

		assertEquals("c/a/b?p1=v1", baseUrl.toString());
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3363">WICKET-3363</a>
	 */
	public void testResolveRelative2()
	{
		Url relative = Url.parse("a/b?p1=v1");
		Url baseUrl = Url.parse("c/d?p2=v2");
		baseUrl.resolveRelative(relative);

		assertEquals("c/a/b?p1=v1", baseUrl.toString());
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3363">WICKET-3363</a>
	 */
	public void testResolveRelative3()
	{
		Url relative = Url.parse("../a/b?p1=v1");
		Url baseUrl = Url.parse("c/d");
		baseUrl.resolveRelative(relative);

		assertEquals("a/b?p1=v1", baseUrl.toString());
	}

	/**
	 * Tests that the default charset is UTF-8
	 */
	public void testCharset1()
	{
		Url url = new Url();
		assertEquals(Charset.forName("UTF-8"), url.getCharset());
	}

	/**
	 * Tests setting the charset explicitly in the constructor
	 */
	public void testCharset2()
	{
		Charset expected = Charset.forName("ISO-8859-2");
		Url url = new Url(expected);
		assertEquals(expected, url.getCharset());
	}

	/**
	 * Tests that the charset is recovered after deserialization (from Url#charsetName)
	 */
	public void testCharset3()
	{
		Charset expected = Charset.forName("ISO-8859-1");
		Url url = new Url(expected);
		Url clonedUrl = (Url)WicketObjects.cloneObject(url);
		assertEquals(expected, clonedUrl.getCharset());
	}
	
	public void testParseRelativeUrl()
	{
		Url url = Url.parse("foo");
		checkUrl(url, null, null, null, "foo");
		assertFalse(url.isAbsolute());

		url = Url.parse("foo/bar/baz");
		checkUrl(url, null, null, null, "foo", "bar", "baz");
		assertFalse(url.isAbsolute());

		url = Url.parse("?foobar");
		checkUrl(url, null, null, null);
		assertEquals("", url.getQueryParameter("foobar").getValue());
		assertFalse(url.isAbsolute());

		url = Url.parse("foo?a=123");
		checkUrl(url, null, null, null, "foo");
		assertEquals("123", url.getQueryParameter("a").getValue());
		assertFalse(url.isAbsolute());
	
		url = Url.parse("/foo");
		checkUrl(url, null, null, null, "", "foo");
		assertTrue(url.isAbsolute());
	}

	public void testParseAbsoluteUrl()
	{
		Url url = Url.parse("ftp://myhost:8081");
		checkUrl(url, "ftp", "myhost", 8081);
		assertFalse(url.isAbsolute());
	
		url = Url.parse("gopher://myhost:8081/foo");
		checkUrl(url, "gopher", "myhost", 8081, "", "foo");
		assertTrue(url.isAbsolute());

		url = Url.parse("https://myhost/foo");
		checkUrl(url, "https", "myhost", 443, "", "foo");
		assertTrue(url.isAbsolute());

		url = Url.parse("https://myhost/foo:123");
		checkUrl(url, "https", "myhost", 443, "", "foo:123");
		assertTrue(url.isAbsolute());

		url = Url.parse("ftp://myhost/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());

		url = Url.parse("FTp://myhost/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());

		url = Url.parse("unknown://myhost/foo");
		checkUrl(url, "unknown", "myhost", null, "", "foo");
		assertTrue(url.isAbsolute());

	}

	private void checkUrl(Url url, String protocol, String host, Integer port, String... segments)
	{
		assertNotNull(url);
		assertEquals(protocol, url.getProtocol());
		assertEquals(host, url.getHost());
		assertEquals(port, url.getPort());
		assertEquals(Arrays.asList(segments), url.getSegments());
	}
}
