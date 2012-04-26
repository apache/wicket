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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.request.Url.StringMode;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matej Knopp
 * @author Igor Vaynberg
 */

// TODO test removeleadingsegments,prependleadingsegments
public class UrlTest extends Assert
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
	@Test
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
	@Test
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
	@Test
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
	@Test
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
	@Test
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
	@Test
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
	@Test
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
	@Test
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
	@Test
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
	@Test
	public void testParse10()
	{
		String s = "/?a";
		Url url = Url.parse(s);
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "");
	}

	/**
	 *
	 */
	@Test
	public void testParse11()
	{
		String s = "/?a=";
		Url url = Url.parse(s);
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "");
	}

	/**
	 *
	 */
	@Test
	public void testParse12()
	{
		String s = "/?=b";
		Url url = Url.parse(s);
		checkSegments(url, "", "");
		checkQueryParams(url, "", "b");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4398
	 */
	@Test
	public void testParse13()
	{
		String s = "/?a=b&";
		Url url = Url.parse(s);
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4398
	 */
	@Test
	public void testParse14()
	{
		String s = "/?a=b&+";
		Url url = Url.parse(s);
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b", " ", "");
	}

	/**
	 * 
	 */
	@Test
	public void testRender1()
	{
		Url url = new Url();
		url.getSegments().add("foo");
		url.getSegments().add("b=r");
		url.getSegments().add("b&z");
		url.getSegments().add("x?");
		url.setQueryParameter("a", "b");
		url.setQueryParameter("x?&4", "y=z");

		assertEquals("foo/b=r/b&z/x%3F?a=b&x%3F%264=y%3Dz", url.toString());
	}

	/**
	 * 
	 */
	@Test
	public void testRender2()
	{
		String s = "/absolute/url";
		Url url = Url.parse(s);
		assertEquals(url.toString(), s);
	}

	/**
	 * 
	 */
	@Test
	public void testRender3()
	{
		String s = "//absolute/url";
		Url url = Url.parse(s);
		assertEquals(url.toString(), s);
	}

	/**
	 * 
	 */
	@Test
	public void testRender4()
	{
		String s = "/";
		Url url = Url.parse(s);
		assertEquals(url.toString(), s);
	}

	@Test
	public void render5()
	{
		Url url = Url.parse("https://www.domain.com/foo/bar?baz=ban");

		// local string mode
		assertEquals("/foo/bar?baz=ban", url.toString(StringMode.LOCAL));

		// full string mode
		assertEquals("https://www.domain.com/foo/bar?baz=ban", url.toString(StringMode.FULL));

		// local is the default mode
		assertEquals(url.toString(StringMode.LOCAL), url.toString());
	}


	/**
	 * 
	 */
	@Test
	public void testAbsolute1()
	{
		Url url = Url.parse("abc/efg");
		assertFalse(url.isAbsolute());
	}

	/**
	 * 
	 */
	@Test
	public void testAbsolute2()
	{
		Url url = Url.parse("");
		assertFalse(url.isAbsolute());
	}

	/**
	 * 
	 */
	@Test
	public void testAbsolute3()
	{
		Url url = Url.parse("/");
		assertTrue(url.isAbsolute());
	}

	/**
	 * 
	 */
	@Test
	public void testAbsolute4()
	{
		Url url = Url.parse("/abc/efg");
		assertTrue(url.isAbsolute());
	}

	/**
	 * 
	 */
	@Test
	public void absolute5()
	{
		Url url = Url.parse("http://www.domain.com");
		assertTrue(url.isAbsolute());
	}


	/**
	 * 
	 */
	@Test
	public void testConcat1()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("xx", "yy"));
		assertEquals(Url.parse("abc/xx/yy"), url);
	}

	/**
	 * 
	 */
	@Test
	public void testConcat2()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList(".", "..", "xx", "yy"));
		assertEquals(Url.parse("xx/yy"), url);
	}

	/**
	 * 
	 */
	@Test
	public void testConcat3()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("..", "..", "xx", "yy"));
		assertEquals(Url.parse("xx/yy"), url);
	}

	/**
	 * 
	 */
	@Test
	public void testConcat4()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("..", "..", "..", "xx", "yy"));
		assertEquals(Url.parse("../xx/yy"), url);
	}

	/**
	 * 
	 */
	@Test
	public void testConcat5()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList("xx", "yy"));
		assertEquals(Url.parse("abc/efg/xx/yy"), url);
	}

	/**
	 * 
	 */
	@Test
	public void testConcat6()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList(".."));
		assertEquals(Url.parse("abc/"), url);
	}

	/**
	 * 
	 */
	@Test
	public void testConcat7()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList("..", ".."));
		assertEquals(Url.parse(""), url);
	}

	/**
	 * 
	 */
	@Test
	public void testConcat8()
	{
		Url url = Url.parse("fff/abc/efg/xxx");
		url.concatSegments(Arrays.asList(".."));
		assertEquals(Url.parse("fff/abc/"), url);
	}

	/**
	 * 
	 */
	@Test
	public void testConcat9()
	{
		Url url = Url.parse("fff/abc/efg/xxx");
		url.concatSegments(Arrays.asList("..", ".."));
		assertEquals(Url.parse("fff/"), url);
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3363">WICKET-3363</a>
	 */
	@Test
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
	@Test
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
	@Test
	public void testResolveRelative3()
	{
		Url relative = Url.parse("../a/b?p1=v1");
		Url baseUrl = Url.parse("c/d");
		baseUrl.resolveRelative(relative);

		assertEquals("a/b?p1=v1", baseUrl.toString());
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-4518">WICKET-4518</a>
	 */
	@Test
	public void testResolveRelative4()
	{
		Url relative = Url.parse("../?p1=v1");
		Url baseUrl = Url.parse("c/d");
		baseUrl.resolveRelative(relative);

		assertEquals("?p1=v1", baseUrl.toString());
	}

	/**
	 * Tries to resolve a relative url against a base that has no segments
	 */
	@Test
	public void testResolveRelative_NoSegmentsInBase()
	{
		Url relative = Url.parse("?a=b");
		Url baseUrl = Url.parse("?foo=bar");
		baseUrl.resolveRelative(relative);

		assertEquals("?a=b", baseUrl.toString());
	}

	/**
	 * Tries to resolve a relative url against a base that has no segments
	 */
	@Test
	public void testResolveRelative_NoSegmentsInBase2()
	{
		Url relative = Url.parse("bar/baz?a=b");
		Url baseUrl = Url.parse("?foo=bar");
		baseUrl.resolveRelative(relative);

		assertEquals("bar/baz?a=b", baseUrl.toString());
	}

	/**
	 * Tries to resolve a relative url that starts with dot followed by empty segment
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-4518">WICKET-4518</a>
	 */
	@Test
	public void testResolveRelative_DotFollowedByEmptySegment1()
	{
		Url relative = Url.parse("./?a=b");
		Url baseUrl = Url.parse("bar");
		baseUrl.resolveRelative(relative);

		assertEquals("?a=b", baseUrl.toString());
		assertEquals("no empty segment", 0, baseUrl.getSegments().size());
	}

	/**
	 * Tries to resolve a relative url that starts with dot followed by empty segment
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-4518">WICKET-4518</a>
	 */
	@Test
	public void testResolveRelative_DotFollowedByEmptySegment2()
	{
		Url relative = Url.parse("./?a=b");
		Url baseUrl = Url.parse("bar/baz");
		baseUrl.resolveRelative(relative);

		assertEquals("bar?a=b", baseUrl.toString());
		assertEquals("no empty segment", 1, baseUrl.getSegments().size());
	}

	/**
	 * Tests that the default charset is UTF-8
	 */
	@Test
	public void testCharset1()
	{
		Url url = new Url();
		assertEquals(Charset.forName("UTF-8"), url.getCharset());
	}

	/**
	 * Tests setting the charset explicitly in the constructor
	 */
	@Test
	public void testCharset2()
	{
		Charset expected = Charset.forName("ISO-8859-2");
		Url url = new Url(expected);
		assertEquals(expected, url.getCharset());
	}

	/**
	 * Tests that the charset is recovered after deserialization (from Url#charsetName)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCharset3() throws Exception
	{
		Charset expected = Charset.forName("ISO-8859-1");
		Url url = new Url(expected);
		Url clonedUrl = cloneObject(url);
		assertEquals(expected, clonedUrl.getCharset());
	}

	private Url cloneObject(Url url) throws Exception
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream(256);
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(url);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
		return (Url)ois.readObject();
	}

	/**
	 * 
	 */
	@Test
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

	/**
	 * 
	 */
	@Test
	public void testParseAbsoluteUrl()
	{
		Url url = Url.parse("ftp://myhost:8081");
		checkUrl(url, "ftp", "myhost", 8081, "", "");
		assertTrue(url.isAbsolute());
		assertEquals("ftp://myhost:8081/", url.toAbsoluteString());

		url = Url.parse("gopher://myhost:8081/foo");
		checkUrl(url, "gopher", "myhost", 8081, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("gopher://myhost:8081/foo", url.toAbsoluteString());

		url = Url.parse("http://myhost:80/foo");
		checkUrl(url, "http", "myhost", 80, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("http://myhost/foo", url.toAbsoluteString());

		url = Url.parse("http://myhost:81/foo");
		checkUrl(url, "http", "myhost", 81, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("http://myhost:81/foo", url.toAbsoluteString());

		url = Url.parse("http://myhost/foo");
		checkUrl(url, "http", "myhost", 80, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("http://myhost/foo", url.toAbsoluteString());

		url = Url.parse("https://myhost:443/foo");
		checkUrl(url, "https", "myhost", 443, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("https://myhost/foo", url.toAbsoluteString());

		url = Url.parse("HTTPS://myhost/foo:123");
		checkUrl(url, "https", "myhost", 443, "", "foo:123");
		assertTrue(url.isAbsolute());
		assertEquals("https://myhost/foo:123", url.toAbsoluteString());

		url = Url.parse("ftp://myhost/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("ftp://myhost/foo", url.toAbsoluteString());

		url = Url.parse("ftp://myhost:21/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("ftp://myhost/foo", url.toAbsoluteString());

		url = Url.parse("ftp://user:pass@myhost:21/foo");
		checkUrl(url, "ftp", "user:pass@myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("ftp://user:pass@myhost/foo", url.toAbsoluteString());

		url = Url.parse("FTp://myhost/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("ftp://myhost/foo", url.toAbsoluteString());

		url = Url.parse("unknown://myhost/foo");
		checkUrl(url, "unknown", "myhost", null, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("unknown://myhost/foo", url.toAbsoluteString());
	}

	private void checkUrl(Url url, String protocol, String host, Integer port, String... segments)
	{
		assertNotNull(url);
		assertEquals(protocol, url.getProtocol());
		assertEquals(host, url.getHost());
		assertEquals(port, url.getPort());
		assertEquals(Arrays.asList(segments), url.getSegments());
	}

	@Test
	public void compact()
	{
		assertEquals("", Url.parse("").canonical().getPath());
		assertEquals("/", Url.parse("/").canonical().getPath());
		assertEquals("/a", Url.parse("/a").canonical().getPath());
		assertEquals("a/", Url.parse("a/").canonical().getPath());
		assertEquals("", Url.parse("example/..").canonical().getPath());
		assertEquals("..", Url.parse("example/../..").canonical().getPath());
		assertEquals("..", Url.parse("example/.././..").canonical().getPath());
		assertEquals("a///", Url.parse("a///").canonical().getPath());
		assertEquals("a//b/c", Url.parse("a//b/c").canonical().getPath());
		assertEquals("foo/test", Url.parse("foo/bar/../baz/../test").canonical().getPath());
	}

	@Test
	public void copyConstructor()
	{
		String protocol = "myProtocol";
		String host = "www.example.com";
		Integer port = 12345;
		Url url = Url.parse("segment1/segment2?name1=value1");
		url.setProtocol(protocol);
		url.setHost(host);
		url.setPort(port);
		Url copy = new Url(url);
		List<String> segments = copy.getSegments();
		assertEquals(2, segments.size());
		assertEquals("segment1", segments.get(0));
		assertEquals("segment2", segments.get(1));
		List<QueryParameter> queryParameters = copy.getQueryParameters();
		assertEquals(1, queryParameters.size());
		QueryParameter queryParameter = queryParameters.get(0);
		assertEquals("name1", queryParameter.getName());
		assertEquals("value1", queryParameter.getValue());
		assertEquals(protocol, copy.getProtocol());
		assertEquals(host, copy.getHost());
		assertEquals(port, copy.getPort());
	}
}
