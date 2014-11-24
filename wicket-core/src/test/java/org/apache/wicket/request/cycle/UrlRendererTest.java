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

import java.util.Arrays;

import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matej Knopp
 */
public class UrlRendererTest extends Assert
{
	/**
	 * 
	 */
	@Test
	public void test1()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(Url.parse("foo/bar/baz?a=b")));
		assertEquals("./xyz?x=y", r1.renderUrl(Url.parse("foo/bar/xyz?x=y")));
		assertEquals("./baz/xyz?x=y", r1.renderUrl(Url.parse("foo/bar/baz/xyz?x=y")));
		assertEquals("../aaa/xyz?x=y", r1.renderUrl(Url.parse("foo/aaa/xyz?x=y")));
		assertEquals("../../bbb/aaa/xyz?x=y", r1.renderUrl(Url.parse("bbb/aaa/xyz?x=y")));
	}

	/**
	 * 
	 */
	@Test
	public void test2()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(Url.parse("foo/bar/baz?a=b")));
		assertEquals("../../foo?x=y", r1.renderUrl(Url.parse("foo?x=y")));
		assertEquals("../../aaa?x=y", r1.renderUrl(Url.parse("aaa?x=y")));
	}

	/**
	 * 
	 */
	@Test
	public void test3()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(Url.parse("?a=b")));
		assertEquals("./a/b/c?x=y", r1.renderUrl(Url.parse("a/b/c?x=y")));
	}

	/**
	 * 
	 */
	@Test
	public void test5()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(Url.parse("url")));
		assertEquals("./url?1", r1.renderUrl(Url.parse("url?1")));
	}

	/**
	 * 
	 */
	@Test
	public void test6()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(Url.parse("url/")));
		assertEquals("./x?1", r1.renderUrl(Url.parse("url/x?1")));
	}

	/**
	 * 
	 */
	@Test
	public void test7()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(
			Url.parse("MyTestPage/indexed1/indexed2/indexed3?10-27.ILinkListener-l2&p1=v1")));
		assertEquals("../../../MyTestPage?10", r1.renderUrl(Url.parse("MyTestPage?10")));
	}

	/**
	 * 
	 */
	@Test
	public void test8()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(
			Url.parse("en/first-test-page?16-1.ILinkListener-l1")));
		assertEquals("./first-test-page/indexed1/indexed2/indexed3?p1=v1",
			r1.renderUrl(Url.parse("en/first-test-page/indexed1/indexed2/indexed3?p1=v1")));
	}

	/**
	 * 
	 */
	@Test
	public void test9()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(Url.parse("a/b/q/d/e")));
		assertEquals("../../../q/c/d/e", r1.renderUrl(Url.parse("a/q/c/d/e")));
	}

	/**
	 * 
	 */
	@Test
	public void test10()
	{
		MockWebRequest request = new MockWebRequest(Url.parse("a/b/q/d/e"), "/contextPath",
			"/filterPath", "../");

		UrlRenderer r = new UrlRenderer(request);
		assertEquals("../../../../../", r.renderContextRelativeUrl(""));
		assertEquals("../../../../../", r.renderContextRelativeUrl("/"));
		assertEquals("../../../../../f", r.renderContextRelativeUrl("/f"));
		assertEquals("../../../../../../f", r.renderContextRelativeUrl("../f"));

		try
		{
			r.renderContextRelativeUrl(null);
			fail("Null 'url' is not allowed!");
		}
		catch (IllegalArgumentException iax)
		{
			assertTrue(true);
		}
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3337">WICKET-3337</a>
	 */
	@Test
	public void test11()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(Url.parse("a")));
		assertEquals(".", r1.renderUrl(Url.parse("")));
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3567">WICKET-3567</a>
	 */
	@Test
	public void test12()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(Url.parse("?0")));
		assertEquals("./", r1.renderUrl(Url.parse("")));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4401
	 * 
	 * A Url should not ends with '..' because some web containers do not handle it properly. Using
	 * '../' works better.
	 */
	@Test
	public void test13()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(Url.parse("foo/bar")));
		assertEquals("../", r1.renderUrl(Url.parse("")));
	}

	/**
	 * WICKET-4920 prevent double slash
	 */
	@Test
	public void test14()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(new Url()));

		assertEquals(
			"./;jsessionid=1p87c5424zjuvd57kljcu2bwa?0-1.IBehaviorListener.1-component",
			r1.renderRelativeUrl(Url.parse("http://localhost:8080/;jsessionid=1p87c5424zjuvd57kljcu2bwa?0-1.IBehaviorListener.1-component")));
	}

	/**
	 * WICKET-4935 prevent another double slash
	 */
	@Test
	public void test15()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(Url.parse("private/AdminPage")));

		assertEquals("../signIn;jsessionid=16k3wqa9c4sgq1cnp7fisa20u",
			r1.renderRelativeUrl(Url.parse("/signIn;jsessionid=16k3wqa9c4sgq1cnp7fisa20u")));
	}

	/**
	 * prevent another double slash when common prefix is present
	 */
	@Test
	public void test16()
	{
		UrlRenderer r1 = new UrlRenderer(
			new MockWebRequest(Url.parse("private/AdminPage")).setContextPath("context"));

		assertEquals("../signIn;jsessionid=16k3wqa9c4sgq1cnp7fisa20u",
			r1.renderRelativeUrl(Url.parse("/context/signIn;jsessionid=16k3wqa9c4sgq1cnp7fisa20u")));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5468
	 */
	@Test
	public void renderUrlWithTrailingDotsInQueryString()
	{
		UrlRenderer r1 = new UrlRenderer(new MockWebRequest(Url.parse("some/path")));

		assertEquals("./path?a=b..", r1.renderUrl(Url.parse("some/path?a=b..")));
	}

	/**
	 * Verify that absolute urls are rendered as is, ignoring the current client url and base url
	 * completely.
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-4466
	 */
	@Test
	public void renderAbsoluteUrl()
	{
		String absoluteUrl = "http://www.example.com/some/path.ext";
		Url url = Url.parse(absoluteUrl);
		UrlRenderer renderer = new UrlRenderer(new MockWebRequest(Url.parse("foo/bar")));
		String renderedUrl = renderer.renderUrl(url);
		assertEquals(absoluteUrl, renderedUrl);
	}

	@Test
	public void renderFullUrlWithRelativeArgument()
	{
		Url baseUrl = Url.parse("one/two/three");
		baseUrl.setProtocol("http");
		baseUrl.setHost("www.example.com");
		baseUrl.setPort(8888);
		UrlRenderer renderer = new UrlRenderer(new MockWebRequest(baseUrl));
		renderer.setBaseUrl(baseUrl); // this is needed because MockWebRequest cuts data
		String fullUrl = renderer.renderFullUrl(Url.parse("../four"));
		assertEquals("http://www.example.com:8888/one/four", fullUrl);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4514
	 */
	@Test
	public void renderFullUrlWithAbsoluteArgument()
	{
		Url baseUrl = Url.parse("one/two/three");
		baseUrl.setProtocol("http");
		baseUrl.setHost("www.example.com");
		baseUrl.setPort(8888);
		UrlRenderer renderer = new UrlRenderer(new MockWebRequest(baseUrl));
		renderer.setBaseUrl(baseUrl); // this is needed because MockWebRequest cuts data
		String fullUrl = renderer.renderFullUrl(Url.parse("/four")); // url starting with slash is
// considered absolute
		assertEquals("http://www.example.com:8888/four", fullUrl);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4561
	 * https://issues.apache.org/jira/browse/WICKET-4562
	 */
	@Test
	public void renderUrlWithRelativeArgument()
	{
		Url baseUrl = Url.parse("one/two/three");
		UrlRenderer renderer = new UrlRenderer(new MockWebRequest(baseUrl));
		baseUrl.setProtocol("http");
		baseUrl.setHost("www.example.com");
		baseUrl.setPort(8888);
		renderer.setBaseUrl(baseUrl);

		Url newUrl = Url.parse("four");
		newUrl.setProtocol("https");
		String fullUrl = renderer.renderUrl(newUrl);
		assertEquals("https://www.example.com:8888/four", fullUrl);

		newUrl = Url.parse("./four");
		newUrl.setProtocol("https");
		fullUrl = renderer.renderUrl(newUrl);
		assertEquals("https://www.example.com:8888/four", fullUrl);

		newUrl = Url.parse("./././four");
		newUrl.setProtocol("https");
		fullUrl = renderer.renderUrl(newUrl);
		assertEquals("https://www.example.com:8888/four", fullUrl);

		newUrl = Url.parse("../four");
		newUrl.setProtocol("https");
		fullUrl = renderer.renderUrl(newUrl);
		assertEquals("https://www.example.com:8888/four", fullUrl);

		newUrl = Url.parse(".././four");
		newUrl.setProtocol("https");
		fullUrl = renderer.renderUrl(newUrl);
		assertEquals("https://www.example.com:8888/four", fullUrl);

		newUrl = Url.parse("../../../../four");
		newUrl.setProtocol("https");
		fullUrl = renderer.renderUrl(newUrl);
		assertEquals("https://www.example.com:8888/four", fullUrl);
	}

	@Test
	public void renderFullUrlAsRelativeToAnAbsoluteBaseUrl()
	{
		Url baseUrl = Url.parse("http://host:8080/contextPath/filterPath/a/b/c/d");
		Url encodedFullUrl = Url.parse("http://host:8080/contextPath/filterPath/a/b;jsessionid=123456");

		UrlRenderer renderer = new UrlRenderer(new MockWebRequest(baseUrl));
		String encodedRelativeUrl = renderer.renderRelativeUrl(encodedFullUrl);

		assertEquals("../../b;jsessionid=123456", encodedRelativeUrl);
	}

	@Test
	public void renderFullUrlAsRelativeToBaseUrlWithoutSchemeHostnameAndPort()
	{
		Url baseUrl = Url.parse("/contextPath/filterPath/a/b/c/d");
		Url encodedFullUrl = Url.parse("http://host:8080/contextPath/filterPath/a/b;jsessionid=123456");

		UrlRenderer renderer = new UrlRenderer(new MockWebRequest(baseUrl));
		String encodedRelativeUrl = renderer.renderRelativeUrl(encodedFullUrl);

		assertEquals("../../b;jsessionid=123456", encodedRelativeUrl);
	}

	@Test
	public void renderFullUrlAsRelativeToBaseUrlWithoutContextAndFilterPaths()
	{
		Url baseUrl = Url.parse("a/b/c/d"); // base url without context path and filter path
		Url encodedFullUrl = Url.parse("http://host:8080/contextPath/filterPath/a/b;jsessionid=123456");

		MockWebRequest request = new MockWebRequest(baseUrl);
		request.setContextPath("contextPath");
		request.setFilterPath("filterPath");
		UrlRenderer renderer = new UrlRenderer(request);
		String encodedRelativeUrl = renderer.renderRelativeUrl(encodedFullUrl);

		assertEquals("../../b;jsessionid=123456", encodedRelativeUrl);
	}

	@Test
	public void renderFullUrlAsRelativeToBaseUrlWithoutComposedContextAndFilterPaths()
	{
		Url baseUrl = Url.parse("a/b/c/d"); // base url without context path and filter path
		Url encodedFullUrl = Url.parse("http://host:8080/context/path/filter/path/a/b;jsessionid=123456");

		MockWebRequest request = new MockWebRequest(baseUrl);
		request.setContextPath("context/path");
		request.setFilterPath("filter/path");
		UrlRenderer renderer = new UrlRenderer(request);
		String encodedRelativeUrl = renderer.renderRelativeUrl(encodedFullUrl);

		assertEquals("../../b;jsessionid=123456", encodedRelativeUrl);
	}

	@Test
	public void renderFullUrlAsRelativeToBaseUrlWithoutContextPath()
	{
		Url baseUrl = Url.parse("a/b/c/d"); // base url without context path and filter path
		Url encodedFullUrl = Url.parse("http://host:8080/filterPath/a/b;jsessionid=123456");

		MockWebRequest request = new MockWebRequest(baseUrl);
		request.setFilterPath("filterPath");
		UrlRenderer renderer = new UrlRenderer(request);
		String encodedRelativeUrl = renderer.renderRelativeUrl(encodedFullUrl);

		assertEquals("../../b;jsessionid=123456", encodedRelativeUrl);
	}

	@Test
	public void renderFullUrlAsRelativeToBaseUrlWithoutComposedContextPath()
	{
		Url baseUrl = Url.parse("a/b/c/d"); // base url without context path and filter path
		Url encodedFullUrl = Url.parse("http://host:8080/filter/path/a/b;jsessionid=123456");

		MockWebRequest request = new MockWebRequest(baseUrl);
		request.setFilterPath("filter/path");
		UrlRenderer renderer = new UrlRenderer(request);
		String encodedRelativeUrl = renderer.renderRelativeUrl(encodedFullUrl);

		assertEquals("../../b;jsessionid=123456", encodedRelativeUrl);
	}

	@Test
	public void renderFullUrlAsRelativeToBaseUrlWithoutFilterPath()
	{
		Url baseUrl = Url.parse("a/b/c/d"); // base url without context path and filter path
		Url encodedFullUrl = Url.parse("http://host:8080/contextPath/a/b;jsessionid=123456");

		MockWebRequest request = new MockWebRequest(baseUrl);
		request.setContextPath("contextPath");
		UrlRenderer renderer = new UrlRenderer(request);
		String encodedRelativeUrl = renderer.renderRelativeUrl(encodedFullUrl);

		assertEquals("../../b;jsessionid=123456", encodedRelativeUrl);
	}

	@Test
	public void renderFullUrlAsRelativeToBaseUrlWithoutComposedFilterPath()
	{
		Url baseUrl = Url.parse("a/b/c/d"); // base url without context path and filter path
		Url encodedFullUrl = Url.parse("http://host:8080/context/path/a/b;jsessionid=123456");

		MockWebRequest request = new MockWebRequest(baseUrl);
		request.setContextPath("context/path");
		UrlRenderer renderer = new UrlRenderer(request);
		String encodedRelativeUrl = renderer.renderRelativeUrl(encodedFullUrl);

		assertEquals("../../b;jsessionid=123456", encodedRelativeUrl);
	}

	@Test
	public void renderFullUrlAsRelativeToBaseUrlWithFirstSegmentsEqualToTheContextPath()
	{
		// base url without context path and filter path
		// 'contextPath' here is a normal segment with the same value
		Url baseUrl = Url.parse("contextPath/a/b/c/d");

		// here 'contextPath' is the actual context path and should be ignored
		Url encodedFullUrl = Url.parse("http://host:8080/contextPath/a/b;jsessionid=123456");

		MockWebRequest request = new MockWebRequest(baseUrl);
		request.setContextPath("contextPath");
		UrlRenderer renderer = new UrlRenderer(request);
		String encodedRelativeUrl = renderer.renderRelativeUrl(encodedFullUrl);

		assertEquals("../../../../a/b;jsessionid=123456", encodedRelativeUrl);
	}

	@Test
	public void renderFullUrlAsRelativeToBaseUrlWithFirstSegmentsEqualToTheContextAndFilterPaths()
	{
		// base url without context path and filter path
		// 'filterPath' here is a normal segment with the same value
		Url baseUrl = Url.parse("filterPath/a/b/c/d");

		// here 'contextPath' is the actual context path and should be ignored
		Url encodedFullUrl = Url.parse("http://host:8080/contextPath/filterPath/a/b;jsessionid=123456");

		MockWebRequest request = new MockWebRequest(baseUrl);
		request.setContextPath("contextPath");
		request.setFilterPath("filterPath");
		UrlRenderer renderer = new UrlRenderer(request);
		String encodedRelativeUrl = renderer.renderRelativeUrl(encodedFullUrl);

		assertEquals("../../../../a/b;jsessionid=123456", encodedRelativeUrl);
	}

	@Test
	public void renderFullUrlAsRelativeToBaseUrlWithFirstSegmentsEqualToTheFilterPath()
	{
		// base url without context path and filter path
		// 'filterPath' here is a normal segment with the same value
		Url baseUrl = Url.parse("filterPath/a/b/c/d");

		// here 'filterPath' is the actual filter path and should be ignored
		Url encodedFullUrl = Url.parse("http://host:8080/filterPath/a/b;jsessionid=123456");

		MockWebRequest request = new MockWebRequest(baseUrl);
		request.setFilterPath("filterPath");
		UrlRenderer renderer = new UrlRenderer(request);
		String encodedRelativeUrl = renderer.renderRelativeUrl(encodedFullUrl);

		assertEquals("../../../../a/b;jsessionid=123456", encodedRelativeUrl);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5123
	 */
	@Test
	public void renderHomeUrl()
	{
		Url baseUrl = Url.parse("login");

		MockWebRequest request = new MockWebRequest(baseUrl);
		UrlRenderer renderer = new UrlRenderer(request);

		Url homeUrl = Url.parse("");
		String encodedRelativeUrl = renderer.renderUrl(homeUrl);

		assertEquals(".", encodedRelativeUrl);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5065
	 */
	@Test
	public void renderAbsoluteWithoutHost()
	{
		Url baseUrl = Url.parse("a/b");

		MockWebRequest request = new MockWebRequest(baseUrl);
		UrlRenderer renderer = new UrlRenderer(request);

		Url absoluteUrl = Url.parse("/c/d");
		String encodedRelativeUrl = renderer.renderUrl(absoluteUrl);

		assertEquals("/c/d", encodedRelativeUrl);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5065
	 */
	@Test
	public void renderAbsoluteWithoutScheme()
	{
		Url baseUrl = Url.parse("a/b");

		MockWebRequest request = new MockWebRequest(baseUrl);
		UrlRenderer renderer = new UrlRenderer(request);

		Url absoluteUrl = Url.parse("//host/c/d");
		String encodedRelativeUrl = renderer.renderUrl(absoluteUrl);

		assertEquals("//host/c/d", encodedRelativeUrl);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5065
	 */
	@Test
	public void renderAbsoluteWithoutSchemeWithPort()
	{
		Url baseUrl = Url.parse("a/b");

		MockWebRequest request = new MockWebRequest(baseUrl);
		UrlRenderer renderer = new UrlRenderer(request);

		Url absoluteUrl = Url.parse("//host:1234/c/d");
		String encodedRelativeUrl = renderer.renderUrl(absoluteUrl);

		assertEquals("//host:1234/c/d", encodedRelativeUrl);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5073
	 */
	@Test
	public void removeCommonPrefixesWicket5073()
	{
		Url baseUrl = new Url(Arrays.asList(""), Arrays.<Url.QueryParameter> asList());

		MockWebRequest request = new MockWebRequest(baseUrl);
		request.setContextPath("/qs");
		request.setFilterPath("");
		UrlRenderer renderer = new UrlRenderer(request);
		renderer.setBaseUrl(baseUrl);

		String rendered = renderer.renderRelativeUrl(Url.parse("wicket/resource/org.apache.wicket.Application/x.css"));
		assertEquals("./wicket/resource/org.apache.wicket.Application/x.css", rendered);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5202
	 */
	@Test
	public void removeCommonPrefixesWithJSessionId()
	{
		Url baseUrl = new Url(Arrays.asList("", "SomePage;jsessionid=1234"), Arrays.<Url.QueryParameter>asList());

		MockWebRequest request = new MockWebRequest(baseUrl);
		request.setContextPath("/");
		request.setFilterPath("filter");
		UrlRenderer renderer = new UrlRenderer(request);
		renderer.setBaseUrl(baseUrl);

		String rendered = renderer.renderRelativeUrl(Url.parse("/filter;jsessionid=1234"));
		assertEquals("../", rendered);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5774
	 */
	@Test
	public void renderFullUrlWithNoOpLeadingSegments()
	{
		UrlRenderer renderer = new UrlRenderer(new MockWebRequest(Url.parse("any/thing")));

		String fullUrl = renderer.renderFullUrl(Url.parse("http://www.example.com:8888/./../one/two/three"));
		assertEquals("http://www.example.com:8888/one/two/three", fullUrl);

		fullUrl = renderer.renderFullUrl(Url.parse("http://www.example.com:8888/.././one/two/three"));
		assertEquals("http://www.example.com:8888/one/two/three", fullUrl);

		fullUrl = renderer.renderFullUrl(Url.parse("http://www.example.com:8888/one/.././two/three"));
		assertEquals("http://www.example.com:8888/two/three", fullUrl);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5774
	 */
	@Test
	public void renderContextAbsoluteUrlWithNoOpLeadingSegments()
	{
		UrlRenderer renderer = new UrlRenderer(new MockWebRequest(Url.parse("any/thing")));

		String fullUrl = renderer.renderFullUrl(Url.parse("/./../one/two/three"));
		assertEquals("/one/two/three", fullUrl);

		fullUrl = renderer.renderFullUrl(Url.parse("/.././one/two/three"));
		assertEquals("/one/two/three", fullUrl);

		fullUrl = renderer.renderFullUrl(Url.parse("/one/.././two/three"));
		assertEquals("/two/three", fullUrl);
	}
}
