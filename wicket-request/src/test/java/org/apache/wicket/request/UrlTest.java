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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.request.Url.StringMode;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
class UrlTest
{
	private void checkSegments(Url url, String... segments)
	{
		assertEquals(Arrays.asList(segments), url.getSegments());
	}

	private void checkQueryParams(Url url, String... params)
	{
		List<QueryParameter> list = new ArrayList<>();
		for (int i = 0; i < params.length; i += 2)
		{
			QueryParameter p = new QueryParameter(params[i], params[i + 1]);
			list.add(p);
		}

		assertEquals(list, url.getQueryParameters());
	}

	@Test
	void parse1()
	{
		String s = "foo/bar/baz?a=4&b=5#foo2";
		Url url = Url.parse(s);
		checkSegments(url, "foo", "bar", "baz");
		checkQueryParams(url, "a", "4", "b", "5");
		assertEquals("foo2", url.getFragment());
	}

	@Test
	void parse2()
	{
		String s = "foo/bar//baz?=4&6";
		Url url = Url.parse(s);
		checkSegments(url, "foo", "bar", "", "baz");
		checkQueryParams(url, "", "4", "6", "");
	}

	@Test
	void parse3()
	{
		String s = "//foo/bar/";
		Url url = Url.parse(s);
		assertNull(url.getProtocol());
		assertEquals("foo", url.getHost());
		assertNull(url.getPort());
		checkSegments(url, "", "bar", "");
		checkQueryParams(url);
	}

	@Test
	void parse4()
	{
		String s = "/foo/bar//";
		Url url = Url.parse(s);
		assertTrue(url.isContextAbsolute());
		checkSegments(url, "", "foo", "bar", "", "");
		checkQueryParams(url);
	}

	@Test
	void parse5()
	{
		String s = "foo/b%3Dr/b%26z/x%3F?a=b&x%3F%264=y%3Dz";
		Url url = Url.parse(s);
		checkSegments(url, "foo", "b=r", "b&z", "x?");
		checkQueryParams(url, "a", "b", "x?&4", "y=z");
	}

	/**
	 * Same as #parse5() but with full url and not encoded '=' char in the query string WICKET-5157
	 */
	@Test
	void parse5_1()
	{
		String s = "http://host:12345/foo/b%3Dr/b%26z/x%3F?a=b&x%3F%264=y=z";
		Url url = Url.parse(s);
		checkSegments(url, "", "foo", "b=r", "b&z", "x?");
		checkQueryParams(url, "a", "b", "x?&4", "y=z");
	}

	@Test
	void parse6()
	{
		String s = "";
		Url url = Url.parse(s);
		checkSegments(url);
		checkQueryParams(url);
	}

	@Test
	void parse7()
	{
		String s = "?a=b";
		Url url = Url.parse(s);
		checkSegments(url);
		checkQueryParams(url, "a", "b");
	}

	@Test
	void parse8()
	{
		String s = "/";
		Url url = Url.parse(s);
		assertTrue(url.isContextAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url);
	}

	@Test
	void parse9()
	{
		String s = "/?a=b";
		Url url = Url.parse(s);
		assertTrue(url.isContextAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b");
	}

	@Test
	void parse10()
	{
		String s = "/?a";
		Url url = Url.parse(s);
		assertTrue(url.isContextAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "");
	}

	@Test
	void parse11()
	{
		String s = "/?a=";
		Url url = Url.parse(s);
		assertTrue(url.isContextAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "");
	}

	@Test
	void parse12()
	{
		String s = "/?=b";
		Url url = Url.parse(s);
		assertTrue(url.isContextAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "", "b");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4398
	 */
	@Test
	void parse13()
	{
		String s = "/?a=b&";
		Url url = Url.parse(s);
		assertTrue(url.isContextAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4398
	 */
	@Test
	void parse14()
	{
		String s = "/?a=b&+";
		Url url = Url.parse(s);
		assertTrue(url.isContextAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b", " ", "");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4877
	 */
	@Test
	void testParse15()
	{
		String s = "http://localhost:56704;jsessionid=8kxeo3reannw1qjtxgkju8yiu";
		Url url = Url.parse(s);
		assertEquals(Integer.valueOf(56704), url.getPort());
		checkSegments(url, ";jsessionid=8kxeo3reannw1qjtxgkju8yiu");
	}

	/**
	 * Make it possible to use full url without protocol
	 * https://issues.apache.org/jira/browse/WICKET-5065
	 */
	@Test
	void parse16()
	{
		String s = "//localhost:56704;jsessionid=8kxeo3reannw1qjtxgkju8yiu";
		Url url = Url.parse(s);
		assertNull(url.getProtocol());
		assertEquals("localhost", url.getHost());
		assertEquals(Integer.valueOf(56704), url.getPort());
		checkSegments(url, ";jsessionid=8kxeo3reannw1qjtxgkju8yiu");
	}

	/**
	 * WICKET-5259
	 */
	@Test
	void parse17()
	{
		String s = "http://me:secret@localhost";
		Url url = Url.parse(s);
		assertEquals("http", url.getProtocol());
		assertEquals("me:secret@localhost", url.getHost());
	}

	/**
	 * WICKET-5259
	 */
	@Test
	void parse18()
	{
		String s = "http://me:secret@localhost:8080";
		Url url = Url.parse(s);
		assertEquals("http", url.getProtocol());
		assertEquals("me:secret@localhost", url.getHost());
		assertEquals(Integer.valueOf(8080), url.getPort());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5717
	 */
	@Test
	void parse19()
	{
		String s = "http://me:secret@localhost:8080/segment/#fragment";
		Url url = Url.parse(s);
		assertEquals("fragment", url.getFragment());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5717
	 */
	@Test
	void parse20()
	{
		String s = "http://me:secret@localhost:8080/segment/#";
		Url url = Url.parse(s);
		assertNull(url.getFragment());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5717
	 */
	@Test
	void parse21()
	{
		String s = "http://me:secret@localhost:8080/segment#";
		Url url = Url.parse(s);
		assertNull(url.getFragment());
	}

	@Test
	void render1()
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

	@Test
	void render2()
	{
		String s = "/absolute/url";
		Url url = Url.parse(s);
		assertEquals(url.toString(), s);
	}

	@Test
	void render3()
	{
		String s = "//absolute/url";
		Url url = Url.parse(s);
		assertEquals(url.toString(StringMode.FULL), s);
	}

	@Test
	void render4()
	{
		String s = "/";
		Url url = Url.parse(s);
		assertEquals(url.toString(), s);
	}

	@Test
	void render5()
	{
		Url url = Url.parse("https://www.domain.com/foo/bar?baz=ban");

		// local string mode
		assertEquals("/foo/bar?baz=ban", url.toString(StringMode.LOCAL));

		// full string mode
		assertEquals("https://www.domain.com/foo/bar?baz=ban", url.toString(StringMode.FULL));

		// local is the default mode
		assertEquals(url.toString(StringMode.LOCAL), url.toString());
	}

	@Test
	void render6()
	{
		Url url = Url.parse("https://www.domain.com/foo/bar?baz=ban#bat");

		// local string mode
		assertEquals("/foo/bar?baz=ban#bat", url.toString(StringMode.LOCAL));

		// full string mode
		assertEquals("https://www.domain.com/foo/bar?baz=ban#bat", url.toString(StringMode.FULL));
	}

	@Test
	void absolute1()
	{
		Url url = Url.parse("abc/efg");
		assertFalse(url.isFull());
		assertFalse(url.isContextAbsolute());
	}

	@Test
	void absolute2()
	{
		Url url = Url.parse("");
		assertFalse(url.isFull());
		assertFalse(url.isContextAbsolute());
	}

	@Test
	void absolute3()
	{
		Url url = Url.parse("/");
		assertFalse(url.isFull());
		assertTrue(url.isContextAbsolute());
	}

	@Test
	void absolute4()
	{
		Url url = Url.parse("/abc/efg");
		assertFalse(url.isFull());
		assertTrue(url.isContextAbsolute());
	}

	@Test
	void absolute5()
	{
		Url url = Url.parse("http://www.domain.com");
		assertTrue(url.isFull());
		assertFalse(url.isContextAbsolute());
	}


	@Test
	void concat1()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("xx", "yy"));
		assertEquals(Url.parse("abc/xx/yy"), url);
	}

	@Test
	void concat2()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList(".", "..", "xx", "yy"));
		assertEquals(Url.parse("xx/yy"), url);
	}

	@Test
	void concat3()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("..", "..", "xx", "yy"));
		assertEquals(Url.parse("xx/yy"), url);
	}

	@Test
	void concat4()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("..", "..", "..", "xx", "yy"));
		assertEquals(Url.parse("../xx/yy"), url);
	}

	@Test
	void concat5()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList("xx", "yy"));
		assertEquals(Url.parse("abc/efg/xx/yy"), url);
	}

	@Test
	void concat6()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList(".."));
		assertEquals(Url.parse("abc/"), url);
	}

	@Test
	void concat7()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList("..", ".."));
		assertEquals(Url.parse(""), url);
	}

	@Test
	void concat8()
	{
		Url url = Url.parse("fff/abc/efg/xxx");
		url.concatSegments(Arrays.asList(".."));
		assertEquals(Url.parse("fff/abc/"), url);
	}

	@Test
	void concat9()
	{
		Url url = Url.parse("fff/abc/efg/xxx");
		url.concatSegments(Arrays.asList("..", ".."));
		assertEquals(Url.parse("fff/"), url);
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3363">WICKET-3363</a>
	 */
	@Test
	void resolveRelative1()
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
	void resolveRelative2()
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
	void resolveRelative3()
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
	void resolveRelative4()
	{
		Url relative = Url.parse("../?p1=v1");
		Url baseUrl = Url.parse("c/d");
		baseUrl.resolveRelative(relative);

		assertEquals("?p1=v1", baseUrl.toString());
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-4789">WICKET-4789</a>
	 */
	@Test
	void resolveRelative_EmptyTrailingSegmentInBase()
	{
		Url relative = Url.parse("./?0-1.ILinkListener-link");
		Url baseUrl = Url.parse("Home/");
		baseUrl.resolveRelative(relative);

		assertEquals("Home/?0-1.ILinkListener-link", baseUrl.toString());
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-4789">WICKET-4789</a>
	 */
	@Test
	void resolveRelative_EmptyTrailingSegmentInBase2()
	{
		Url relative = Url.parse("./foo/?0-1.ILinkListener-link");
		Url baseUrl = Url.parse("Home/");
		baseUrl.resolveRelative(relative);

		assertEquals("Home/foo/?0-1.ILinkListener-link", baseUrl.toString());
	}

	/**
	 * Tries to resolve a relative url against a base that has no segments
	 */
	@Test
	void resolveRelative_NoSegmentsInBase()
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
	void resolveRelative_NoSegmentsInBase2()
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
	void resolveRelative_DotFollowedByEmptySegment1()
	{
		Url relative = Url.parse("./?a=b");
		Url baseUrl = Url.parse("bar");
		baseUrl.resolveRelative(relative);

		assertEquals("?a=b", baseUrl.toString());
		assertEquals(0, baseUrl.getSegments().size(), "no empty segment");
	}

	/**
	 * Tries to resolve a relative url that starts with dot followed by empty segment
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-4518">WICKET-4518</a>
	 */
	@Test
	void resolveRelative_DotFollowedByEmptySegment2()
	{
		Url relative = Url.parse("./?a=b");
		Url baseUrl = Url.parse("bar/baz");
		baseUrl.resolveRelative(relative);

		assertEquals("bar/?a=b", baseUrl.toString());
	}

	/**
	 * Tests that the default charset is UTF-8
	 */
	@Test
	void charset1()
	{
		Url url = new Url();
		assertEquals(StandardCharsets.UTF_8, url.getCharset());
	}

	/**
	 * Tests setting the charset explicitly in the constructor
	 */
	@Test
	void charset2()
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
	void charset3() throws Exception
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

	@Test
	void parseRelativeUrl()
	{
		Url url = Url.parse("foo");
		checkUrl(url, null, null, null, "foo");
		assertFalse(url.isContextAbsolute());

		url = Url.parse("foo/bar/baz");
		checkUrl(url, null, null, null, "foo", "bar", "baz");
		assertFalse(url.isContextAbsolute());

		url = Url.parse("?foobar");
		checkUrl(url, null, null, null);
		assertEquals("", url.getQueryParameter("foobar").getValue());
		assertFalse(url.isContextAbsolute());

		url = Url.parse("foo?a=123");
		checkUrl(url, null, null, null, "foo");
		assertEquals("123", url.getQueryParameter("a").getValue());
		assertFalse(url.isContextAbsolute());

		url = Url.parse("/foo");
		checkUrl(url, null, null, null, "", "foo");
		assertTrue(url.isContextAbsolute());
	}

	@Test
	void parseAbsoluteUrl()
	{
		Url url = Url.parse("ftp://myhost:8081");
		checkUrl(url, "ftp", "myhost", 8081, "", "");
		assertTrue(url.isFull());
		assertEquals("ftp://myhost:8081/", url.toString(StringMode.FULL));

		url = Url.parse("gopher://myhost:8081/foo");
		checkUrl(url, "gopher", "myhost", 8081, "", "foo");
		assertTrue(url.isFull());
		assertEquals("gopher://myhost:8081/foo", url.toString(StringMode.FULL));

		url = Url.parse("http://myhost:80/foo");
		checkUrl(url, "http", "myhost", 80, "", "foo");
		assertTrue(url.isFull());
		assertEquals("http://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("http://myhost:81/foo");
		checkUrl(url, "http", "myhost", 81, "", "foo");
		assertTrue(url.isFull());
		assertEquals("http://myhost:81/foo", url.toString(StringMode.FULL));

		url = Url.parse("http://myhost/foo");
		checkUrl(url, "http", "myhost", 80, "", "foo");
		assertTrue(url.isFull());
		assertEquals("http://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("https://myhost:443/foo");
		checkUrl(url, "https", "myhost", 443, "", "foo");
		assertTrue(url.isFull());
		assertEquals("https://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("HTTPS://myhost/foo:123");
		checkUrl(url, "https", "myhost", 443, "", "foo:123");
		assertTrue(url.isFull());
		assertEquals("https://myhost/foo:123", url.toString(StringMode.FULL));

		url = Url.parse("ftp://myhost/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isFull());
		assertEquals("ftp://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("ftp://myhost:21/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isFull());
		assertEquals("ftp://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("ftp://user:pass@myhost:21/foo");
		checkUrl(url, "ftp", "user:pass@myhost", 21, "", "foo");
		assertTrue(url.isFull());
		assertEquals("ftp://user:pass@myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("FTp://myhost/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isFull());
		assertEquals("ftp://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("unknown://myhost/foo");
		checkUrl(url, "unknown", "myhost", null, "", "foo");
		assertTrue(url.isFull());
		assertEquals("unknown://myhost/foo", url.toString(StringMode.FULL));
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
	void compact()
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
		assertEquals("a/d", Url.parse("a/b/c/../../d").canonical().getPath());
		assertEquals("a/d", Url.parse("../../a/b/../c/../d").canonical().getPath());
		assertEquals("a/d", Url.parse("../../a/b/../c/../d/.").canonical().getPath());
		assertEquals("a", Url.parse("../../a/b/../c/../d/..").canonical().getPath());
		assertEquals("", Url.parse("../../a/b/../c/../d/../..").canonical().getPath());
	}

	@Test
	void copyConstructor()
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

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4387 Parse uri with '://' in it should consider
	 * it as absolute only if there are no slashes earlier in the string.
	 */
	@Test
	void parseHttpSlashSlashColon()
	{
		// relative
		String uri = "/abc/http://host:9090/";
		Url url = Url.parse(uri);
		assertEquals(uri, url.toString());
		assertNull(url.getProtocol());
		assertNull(url.getHost());
		assertNull(url.getPort());

		// absolute
		uri = "abchttp://host:9090/";
		url = Url.parse(uri);
		assertEquals(uri, url.toString(StringMode.FULL));
		assertEquals("abchttp", url.getProtocol());
		assertEquals("host", url.getHost());
		assertEquals(Integer.valueOf(9090), url.getPort());

	}

	@Test
	void prependLeadingSegments1()
	{
		Url url = Url.parse("a");

		url.prependLeadingSegments(Arrays.asList("x", "y"));

		assertEquals("x/y/a", url.toString());
	}

	@Test
	void prependLeadingSegments2()
	{
		Url url = Url.parse("a");

		url.prependLeadingSegments(Arrays.asList("x"));

		assertEquals("x/a", url.toString());
	}

	@Test
	void prependLeadingSegments3()
	{
		Url url = Url.parse("a");

		url.prependLeadingSegments(Collections.<String> emptyList());

		assertEquals("a", url.toString());
	}

	@Test
	void prependLeadingSegments4()
	{
		Url url = new Url();

		url.prependLeadingSegments(Arrays.asList("x"));

		assertEquals("x", url.toString());
	}

	@Test
	void removeLeadingSegments1()
	{
		Url url = Url.parse("a/b");

		url.removeLeadingSegments(1);
		assertEquals("b", url.toString());
	}

	@Test
	void removeLeadingSegments2()
	{
		Url url = Url.parse("a/b");

		url.removeLeadingSegments(2);
		assertEquals("", url.toString());
	}

	@Test
	void removeLeadingSegments3()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			Url url = Url.parse("a/b");

			url.removeLeadingSegments(3);
		});
	}

	@Test
	void wicket_5114_allowtoStringFullWhenContainingTwoDots()
	{
		Url url = Url.parse("/mountPoint/whatever.../");
		url.setHost("wicketHost");
		assertEquals("//wicketHost/mountPoint/whatever.../", url.toString(StringMode.FULL));
	}

	@Test
	void wicket_5114_throwExceptionWhenToStringFullContainsRelativePathSegment()
	{
		assertThrows(IllegalStateException.class, () -> {
			Url url = Url.parse("/mountPoint/../whatever/");
			url.setHost("wicketHost");
			url.toString(StringMode.FULL);
		});
	}

	@Test
	void contextRelativeIsntReadAsContextAbsolute() throws Exception
	{
		Url url = Url.parse("/segment");
		url.setContextRelative(true);
		assertTrue(url.isContextRelative());
		assertFalse(url.isContextAbsolute());
	}

	@Test
	void contextRelativeIsntReadAsFull() throws Exception
	{
		Url url = Url.parse("http://www.example.com/segment");
		url.setContextRelative(true);
		assertTrue(url.isContextRelative());
		assertFalse(url.isFull());
	}

	@Test
	void isContextAbsolute()
	{
		Url url = Url.parse("");
		assertFalse(url.isContextAbsolute());

		url = Url.parse("http://www.example.com/path");
		assertFalse(url.isContextAbsolute());

		url = Url.parse("//www.example.com/path");
		assertFalse(url.isContextAbsolute());

		url = Url.parse("path");
		assertFalse(url.isContextAbsolute());

		url = Url.parse("/path");
		assertTrue(url.isContextAbsolute());
	}

	@Test
	void isFull()
	{
		Url url = Url.parse("");
		assertFalse(url.isFull());

		url = Url.parse("http://www.example.com/path");
		assertTrue(url.isFull());

		url = Url.parse("//www.example.com/path");
		assertTrue(url.isFull());

		url = Url.parse("path");
		assertFalse(url.isFull());

		url = Url.parse("/path");
		assertFalse(url.isFull());
	}

	/**
	 * Should accept parameter values containing equals sign(s)
	 * https://issues.apache.org/jira/browse/WICKET-5157
	 */
	@Test
	void parseQueryStringWithEqualsSignInParameterValue()
	{
		String s = "/?a=b=c&d=e=f";
		Url url = Url.parse(s);
		assertTrue(url.isContextAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b=c", "d", "e=f");
	}

	/**
	 * Parse IP6 addresses (https://www.ietf.org/rfc/rfc2732.txt)
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-5809
	 */
	@Test
	void parseIp6Address()
	{
		String s = "https://[::1]/myapp";
		Url url = Url.parse(s);

		assertTrue(url.isFull());
		checkUrl(url, "https", "[::1]", 443, "", "myapp");

		// now with port in URL
		s = "http://[::1]:1234/myapp";

		url = Url.parse(s);

		assertTrue(url.isFull());
		checkUrl(url, "http", "[::1]", 1234, "", "myapp");

	}
}
