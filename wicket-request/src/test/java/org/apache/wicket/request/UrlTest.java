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
import java.util.Collections;
import java.util.List;

import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.request.Url.StringMode;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matej Knopp
 * @author Igor Vaynberg
 */
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
	public void parse1()
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
	public void parse2()
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
	public void parse3()
	{
		String s = "//foo/bar/";
		Url url = Url.parse(s);
		assertNull(url.getProtocol());
		assertEquals("foo", url.getHost());
		assertNull(url.getPort());
		checkSegments(url, "", "bar", "");
		checkQueryParams(url);
	}

	/**
	 * 
	 */
	@Test
	public void parse4()
	{
		String s = "/foo/bar//";
		Url url = Url.parse(s);
		assertTrue(url.isAbsolute());
		checkSegments(url, "", "foo", "bar", "", "");
		checkQueryParams(url);
	}

	/**
	 * 
	 */
	@Test
	public void parse5()
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
	public void parse6()
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
	public void parse7()
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
	public void parse8()
	{
		String s = "/";
		Url url = Url.parse(s);
		assertTrue(url.isAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url);
	}

	/**
	 * 
	 */
	@Test
	public void parse9()
	{
		String s = "/?a=b";
		Url url = Url.parse(s);
		assertTrue(url.isAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b");
	}

	/**
	 *
	 */
	@Test
	public void parse10()
	{
		String s = "/?a";
		Url url = Url.parse(s);
		assertTrue(url.isAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "");
	}

	/**
	 *
	 */
	@Test
	public void parse11()
	{
		String s = "/?a=";
		Url url = Url.parse(s);
		assertTrue(url.isAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "");
	}

	/**
	 *
	 */
	@Test
	public void parse12()
	{
		String s = "/?=b";
		Url url = Url.parse(s);
		assertTrue(url.isAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "", "b");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4398
	 */
	@Test
	public void parse13()
	{
		String s = "/?a=b&";
		Url url = Url.parse(s);
		assertTrue(url.isAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4398
	 */
	@Test
	public void parse14()
	{
		String s = "/?a=b&+";
		Url url = Url.parse(s);
		assertTrue(url.isAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b", " ", "");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4877
	 */
	@Test
	public void testParse15()
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
	public void parse16()
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
	public void parse17()
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
	public void parse18()
	{
		String s = "http://me:secret@localhost:8080";
		Url url = Url.parse(s);
		assertEquals("http", url.getProtocol());
		assertEquals("me:secret@localhost", url.getHost());
		assertEquals(Integer.valueOf(8080), url.getPort());
	}

	/**
	 * 
	 */
	@Test
	public void render1()
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
	public void render2()
	{
		String s = "/absolute/url";
		Url url = Url.parse(s);
		assertEquals(url.toString(), s);
	}

	/**
	 * 
	 */
	@Test
	public void render3()
	{
		String s = "//absolute/url";
		Url url = Url.parse(s);
		assertEquals(url.toString(StringMode.FULL), s);
	}

	/**
	 * 
	 */
	@Test
	public void render4()
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
	public void absolute1()
	{
		Url url = Url.parse("abc/efg");
		assertFalse(url.isAbsolute());
	}

	/**
	 * 
	 */
	@Test
	public void absolute2()
	{
		Url url = Url.parse("");
		assertFalse(url.isAbsolute());
	}

	/**
	 * 
	 */
	@Test
	public void absolute3()
	{
		Url url = Url.parse("/");
		assertTrue(url.isAbsolute());
	}

	/**
	 * 
	 */
	@Test
	public void absolute4()
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
	public void concat1()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("xx", "yy"));
		assertEquals(Url.parse("abc/xx/yy"), url);
	}

	/**
	 * 
	 */
	@Test
	public void concat2()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList(".", "..", "xx", "yy"));
		assertEquals(Url.parse("xx/yy"), url);
	}

	/**
	 * 
	 */
	@Test
	public void concat3()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("..", "..", "xx", "yy"));
		assertEquals(Url.parse("xx/yy"), url);
	}

	/**
	 * 
	 */
	@Test
	public void concat4()
	{
		Url url = Url.parse("abc/efg");
		url.concatSegments(Arrays.asList("..", "..", "..", "xx", "yy"));
		assertEquals(Url.parse("../xx/yy"), url);
	}

	/**
	 * 
	 */
	@Test
	public void concat5()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList("xx", "yy"));
		assertEquals(Url.parse("abc/efg/xx/yy"), url);
	}

	/**
	 * 
	 */
	@Test
	public void concat6()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList(".."));
		assertEquals(Url.parse("abc/"), url);
	}

	/**
	 * 
	 */
	@Test
	public void concat7()
	{
		Url url = Url.parse("abc/efg/");
		url.concatSegments(Arrays.asList("..", ".."));
		assertEquals(Url.parse(""), url);
	}

	/**
	 * 
	 */
	@Test
	public void concat8()
	{
		Url url = Url.parse("fff/abc/efg/xxx");
		url.concatSegments(Arrays.asList(".."));
		assertEquals(Url.parse("fff/abc/"), url);
	}

	/**
	 * 
	 */
	@Test
	public void concat9()
	{
		Url url = Url.parse("fff/abc/efg/xxx");
		url.concatSegments(Arrays.asList("..", ".."));
		assertEquals(Url.parse("fff/"), url);
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3363">WICKET-3363</a>
	 */
	@Test
	public void resolveRelative1()
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
	public void resolveRelative2()
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
	public void resolveRelative3()
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
	public void resolveRelative4()
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
	public void resolveRelative_EmptyTrailingSegmentInBase()
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
	public void resolveRelative_EmptyTrailingSegmentInBase2()
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
	public void resolveRelative_NoSegmentsInBase()
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
	public void resolveRelative_NoSegmentsInBase2()
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
	public void resolveRelative_DotFollowedByEmptySegment1()
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
	public void resolveRelative_DotFollowedByEmptySegment2()
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
	public void charset1()
	{
		Url url = new Url();
		assertEquals(Charset.forName("UTF-8"), url.getCharset());
	}

	/**
	 * Tests setting the charset explicitly in the constructor
	 */
	@Test
	public void charset2()
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
	public void charset3() throws Exception
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
	public void parseRelativeUrl()
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
	public void parseAbsoluteUrl()
	{
		Url url = Url.parse("ftp://myhost:8081");
		checkUrl(url, "ftp", "myhost", 8081, "", "");
		assertTrue(url.isAbsolute());
		assertEquals("ftp://myhost:8081/", url.toString(StringMode.FULL));

		url = Url.parse("gopher://myhost:8081/foo");
		checkUrl(url, "gopher", "myhost", 8081, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("gopher://myhost:8081/foo", url.toString(StringMode.FULL));

		url = Url.parse("http://myhost:80/foo");
		checkUrl(url, "http", "myhost", 80, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("http://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("http://myhost:81/foo");
		checkUrl(url, "http", "myhost", 81, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("http://myhost:81/foo", url.toString(StringMode.FULL));

		url = Url.parse("http://myhost/foo");
		checkUrl(url, "http", "myhost", 80, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("http://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("https://myhost:443/foo");
		checkUrl(url, "https", "myhost", 443, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("https://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("HTTPS://myhost/foo:123");
		checkUrl(url, "https", "myhost", 443, "", "foo:123");
		assertTrue(url.isAbsolute());
		assertEquals("https://myhost/foo:123", url.toString(StringMode.FULL));

		url = Url.parse("ftp://myhost/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("ftp://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("ftp://myhost:21/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("ftp://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("ftp://user:pass@myhost:21/foo");
		checkUrl(url, "ftp", "user:pass@myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("ftp://user:pass@myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("FTp://myhost/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());
		assertEquals("ftp://myhost/foo", url.toString(StringMode.FULL));

		url = Url.parse("unknown://myhost/foo");
		checkUrl(url, "unknown", "myhost", null, "", "foo");
		assertTrue(url.isAbsolute());
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
//		assertEquals("a/d", Url.parse("a/b/c/../../d").canonical().getPath());
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

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4387 Parse uri with '://' in it should consider
	 * it as absolute only if there are no slashes earlier in the string.
	 */
	@Test
	public void parseHttpSlashSlashColon()
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

	/**
	 * 
	 */
	@Test
	public void prependLeadingSegments1()
	{
		Url url = Url.parse("a");

		url.prependLeadingSegments(Arrays.asList("x", "y"));

		assertEquals("x/y/a", url.toString());
	}

	/**
	 * 
	 */
	@Test
	public void prependLeadingSegments2()
	{
		Url url = Url.parse("a");

		url.prependLeadingSegments(Arrays.asList("x"));

		assertEquals("x/a", url.toString());
	}

	/**
	 * 
	 */
	@Test
	public void prependLeadingSegments3()
	{
		Url url = Url.parse("a");

		url.prependLeadingSegments(Collections.<String> emptyList());

		assertEquals("a", url.toString());
	}

	/**
	 * 
	 */
	@Test
	public void prependLeadingSegments4()
	{
		Url url = new Url();

		url.prependLeadingSegments(Arrays.asList("x"));

		assertEquals("x", url.toString());
	}

	/**
	 * 
	 */
	@Test
	public void removeLeadingSegments1()
	{
		Url url = Url.parse("a/b");

		url.removeLeadingSegments(1);
		assertEquals("b", url.toString());
	}

	/**
	 * 
	 */
	@Test
	public void removeLeadingSegments2()
	{
		Url url = Url.parse("a/b");

		url.removeLeadingSegments(2);
		assertEquals("", url.toString());
	}

	/**
	 * 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void removeLeadingSegments3()
	{
		Url url = Url.parse("a/b");

		url.removeLeadingSegments(3);
	}

    @Test
    public void wicket_5114_allowtoStringFullWhenContainingTwoDots()
    {
        Url url = Url.parse("/mountPoint/whatever.../");
        url.setHost("wicketHost");
        assertEquals("//wicketHost/mountPoint/whatever.../", url.toString(StringMode.FULL));
    }

    @Test(expected = IllegalStateException.class)
    public void wicket_5114_throwExceptionWhenToStringFullContainsRelativePathSegment()
    {
        Url url = Url.parse("/mountPoint/../whatever/");
        url.setHost("wicketHost");
        url.toString(StringMode.FULL);
    }

	@Test
	public void isContextAbsolute()
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
	public void isFull()
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
	public void parseQueryStringWithEqualsSignInParameterValue()
	{
		String s = "/?a=b=c&d=e=f";
		Url url = Url.parse(s);
		assertTrue(url.isContextAbsolute());
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b=c", "d", "e=f");
	}

}
