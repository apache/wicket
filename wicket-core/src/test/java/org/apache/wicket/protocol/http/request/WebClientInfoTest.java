/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.http.request;

import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the WebClientInfo class
 */
public class WebClientInfoTest
{
	private RequestCycle requestCycleMock;
	private ServletWebRequest webRequest;
	private HttpServletRequest servletRequest;

	/**
	 * Prepare RequestCycle to be able to extract the remote address of the client
	 */
	@Before
	public void before()
	{
		requestCycleMock = mock(RequestCycle.class);

		webRequest = mock(ServletWebRequest.class);
		when(requestCycleMock.getRequest()).thenReturn(webRequest);

		servletRequest = mock(HttpServletRequest.class);
		when(webRequest.getContainerRequest()).thenReturn(servletRequest);
	}

	/**
	 * Test IE 6.x user-agent strings
	 */
	@Test
	public void internetExplorer6()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/4.0 (Compatible; Windows NT 5.1; MSIE 6.0) (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)",
			"Mozilla/4.0 (compatible; MSIE 6.01; Windows NT 6.0)",
			"Mozilla/4.0 (compatible; MSIE 6.1; Windows XP; .NET CLR 1.1.4322; .NET CLR 2.0.50727)",
			"Mozilla/5.0 (Windows; U; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)",
			"Mozilla/5.0 (compatible; MSIE 6.0; Windows NT 5.2; WOW64; .NET CLR 2.0.50727)");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(6)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test IE 7.x user-agent strings
	 */
	@Test
	public void internetExplorer7()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; c .NET CLR 3.0.04506; .NET CLR 3.5.30707; InfoPath.1; el-GR)",
			"Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)",
			"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)",
			"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; YPC 3.2.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; InfoPath.2; .NET CLR 3.5.30729; .NET CLR 3.0.30618)",
			"Mozilla/5.0 (compatible; MSIE 7.0; Windows NT 5.2; WOW64; .NET CLR 2.0.50727)");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(7)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test IE 8.x user-agent strings
	 */
	@Test
	public void internetExplorer8()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; Media Center PC 4.0; SLCC1; .NET CLR 3.0.04320)",
			"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; MS-RTC LM 8)",
			"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; MS-RTC LM 8; .NET4.0C; .NET4.0E; Zune 4.7)",
			"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; Media Center PC 6.0; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C)",
			"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E; MS-RTC LM 8; Zune 4.7)");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(8)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test IE 9.x user-agent strings
	 */
	@Test
	public void internetExplorer9()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (Windows; U; MSIE 9.0; WIndows NT 9.0; en-US))",
			"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; Media Center PC 6.0; InfoPath.3; MS-RTC LM 8; Zune 4.7)",
			"Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 7.1; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C)",
			"Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.0; YPC 3.2.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; InfoPath.2; .NET CLR 3.5.30729; .NET CLR 3.0.30618)",
			"Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C)");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(9)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test IE 10.x user-agent strings
	 */
	@Test
	public void internetExplorer10()
	{
		List<String> userAgents = Arrays.asList(
				"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
				"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)",
				"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)",
				"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/4.0; InfoPath.2; SV1; .NET CLR 2.0.50727; WOW64)",
				"Mozilla/5.0 (compatible; MSIE 10.0; Macintosh; Intel Mac OS X 10_7_3; Trident/6.0)",
				"Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)",
				"Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
					   is(equalTo(10)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
					   is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
					   is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
					   is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
					   is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
					   is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
					   is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
					   is(equalTo(false)));
		}
	}

	/**
	 * Test IE 11.x user-agent strings
	 */
	@Test
	public void internetExplorer11()
	{
		List<String> userAgents = Arrays.asList(
				"Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko",
				"Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.1) like Gecko");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
					   is(equalTo(11)));
			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMinor(),
					   is(greaterThan(-1)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
					   is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
					   is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
					   is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
					   is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
					   is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
					   is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
					   is(equalTo(false)));
		}
	}

	/**
	 * Test Opera 9.64 user-agent strings
	 */
	@Test
	public void opera964()
	{
		List<String> userAgents = Arrays.asList(
			"Opera/9.64 (Windows NT 6.1; U; MRA 5.5 (build 02842); ru) Presto/2.1.1",
			"Opera/9.64 (X11; Linux i686; U; Linux Mint; it) Presto/2.1.1");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(), is(equalTo(true)));
			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(9)));
			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMinor(),
				is(equalTo(64)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test Opera 10.x user-agent strings
	 */
	@Test
	public void opera10()
	{
		List<String> userAgents = Arrays.asList(
			"Opera/9.80 (X11; Linux i686; U; Debian; pl) Presto/2.2.15 Version/10.00",
			"Opera/9.80 (X11; Linux x86_64; U; en-GB) Presto/2.2.15 Version/10.01",
			"Mozilla/5.0 (Windows NT 6.0; U; tr; rv:1.8.1) Gecko/20061208 Firefox/2.0.0 Opera 10.10",
			"Opera/9.80 (S60; SymbOS; Opera Tablet/9174; U; en) Presto/2.7.81 Version/10.5",
			"Mozilla/4.0 (compatible; MSIE 8.0; Linux i686; en) Opera 10.51",
			"Mozilla/5.0 (Windows NT 5.1; U; zh-cn; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6 Opera 10.70");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(), is(equalTo(true)));
			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(10)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test Opera 11.x user-agent strings
	 */
	@Test
	public void opera11()
	{
		List<String> userAgents = Arrays.asList("Opera/9.80 (X11; Linux x86_64; U; en) Presto/2.8.131 Version/11.10");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(), is(equalTo(true)));
			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(11)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test Safari <3 user-agent strings
	 */
	@Test
	public void safariLessThan3()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; es) AppleWebKit/418 (KHTML, like Gecko) Safari/417.9.3",
			"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; fr) AppleWebKit/312.5.2 (KHTML, like Gecko) Safari/312.3.3",
			"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/85.8.5 (KHTML, like Gecko) Safari/85.8.1");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(lessThan(3)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test Safari3 user-agent strings
	 */
	@Test
	public void safari3()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; fr-fr) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1",
			"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-gb) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.2 Safari/525.20.1",
			"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/522.11 (KHTML, like Gecko) Version/3.0.2 Safari/522.12");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(3)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test Safari user-agent strings
	 */
	@Test
	public void safari4()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (Windows; U; Windows NT 6.0; en) AppleWebKit/528.16 (KHTML, like Gecko) Version/4.0 Safari/528.16",
			"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_7; en-us) AppleWebKit/530.19.2 (KHTML, like Gecko) Version/4.0.2 Safari/530.19",
			"Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; nl-nl) AppleWebKit/533.16 (KHTML, like Gecko) Version/4.1 Safari/533.16",
			"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_4; en-gb) AppleWebKit/528.4+ (KHTML, like Gecko) Version/4.0dp1 Safari/526.11.2");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(4)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test Safari5 user-agent strings
	 */
	@Test
	public void safari5()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; ja-JP) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16",
			"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; fr) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148a Safari/6533.18.5",
			"Mozilla/5.0 (Windows; U; Windows NT 6.0; nb-NO) AppleWebKit/533.18.1 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5",
			"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_6; de-de) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(5)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test Chrome0 user-agent strings
	 */
	@Test
	public void chrome0()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.2.149.27 Safari/525.13",
			"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/525.19 (KHTML, like Gecko) Chrome/0.4.154.31 Safari/525.19",
			"Mozilla/5.0 (Windows; U; Windows NT 5.2; en-US) AppleWebKit/525.19 (KHTML, like Gecko) Chrome/0.3.154.6 Safari/525.19");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(0)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test Chrome 8.x user-agent strings
	 */
	@Test
	public void chrome8()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/8.0.552.224 Safari/534.10",
			"Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Ubuntu/10.10 Chromium/8.0.552.237 Chrome/8.0.552.237 Safari/534.10",
			"Mozilla/5.0 (X11; U; CrOS i686 0.9.128; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/8.0.552.339 Safari/534.10");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(8)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test Chrome 12.x user-agent strings
	 */
	@Test
	public void chrome12()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/12.0.702.0 Safari/534.24",
			"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Ubuntu/10.10 Chromium/12.0.703.0 Chrome/12.0.703.0 Safari/534.24",
			"Mozilla/5.0 (Windows NT 5.1) AppleWebKit/534.25 (KHTML, like Gecko) Chrome/12.0.706.0 Safari/534.25");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(12)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test Konqueror user-agent strings
	 */
	@Test
	public void konqueror()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (compatible; Konqueror/3.2; FreeBSD) (KHTML, like Gecko)",
			"Mozilla/5.0 (compatible; Konqueror/3.5; Linux; i686; U; it-IT) KHTML/3.5.5 (like Gecko) (Debian)",
			"Mozilla/5.0 (compatible; Konqueror/4.3; Linux 2.6.31-16-generic; X11) KHTML/4.3.2 (like Gecko)");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test FF 4.x user-agent strings
	 */
	@Test
	public void firefox40()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:2.0b8pre) Gecko/20101213 Firefox/4.0b8pre",
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; ru; rv:1.9.2.3) Gecko/20100401 Firefox/4.0 (.NET CLR 3.5.30729)");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(4)));
			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMinor(),
				is(equalTo(0)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(true)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test FF3.8 user-agent strings
	 */
	@Test
	public void firefox38()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401 Mozilla/5.0 (X11; U; Linux i686; it-IT; rv:1.9.0.2) Gecko/2008092313 Ubuntu/9.25 (jaunty) Firefox/3.8",
			"Mozilla/5.0 (X11; U; Linux i686; pl-PL; rv:1.9.0.2) Gecko/2008092313 Ubuntu/9.25 (jaunty) Firefox/3.8");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(3)));
			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMinor(),
				is(equalTo(8)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(true)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test FF 3.6 user-agent strings
	 */
	@Test
	public void firefox36()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-GB; rv:1.9.2.9) Gecko/20100824 Firefox/3.6.9 ( .NET CLR 3.5.30729; .NET CLR 4.0.20506)",
			"Mozilla/5.0 (X11; U; FreeBSD i386; de-CH; rv:1.9.2.8) Gecko/20100729 Firefox/3.6.8");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(3)));
			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMinor(),
				is(equalTo(6)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(true)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test FF 2.x user-agent strings
	 */
	@Test
	public void firefox20()
	{
		List<String> userAgents = Arrays.asList(
			"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.9) Gecko/20071105 Fedora/2.0.0.9-1.fc7 Firefox/2.0.0.9",
			"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.8.1.6) Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)");

		for (String userAgent : userAgents)
		{
			WebClientInfo webClientInfo = new WebClientInfo(requestCycleMock, userAgent);

			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMajor(),
				is(equalTo(2)));
			assertThat(userAgent, webClientInfo.getProperties().getBrowserVersionMinor(),
				is(equalTo(0)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozillaFirefox(),
				is(equalTo(true)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserMozilla(),
				is(equalTo(true)));

			assertThat(userAgent, webClientInfo.getProperties().isBrowserOpera(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserChrome(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserKonqueror(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserSafari(),
				is(equalTo(false)));
			assertThat(userAgent, webClientInfo.getProperties().isBrowserInternetExplorer(),
				is(equalTo(false)));
		}
	}

	/**
	 * Test X-Forwarded-For ip address extraction.
	 */
	@Test
	public void testExtractFromXForwardedForHeader()
	{
		String expected = "127.0.0.1";
		when(webRequest.getHeader("X-Forwarded-For")).thenReturn(expected);
		WebClientInfo clientInfo = new WebClientInfo(requestCycleMock, "No user agent");
		String actual = clientInfo.getRemoteAddr(requestCycleMock);
		assertThat(actual, is(equalTo(expected)));
		Mockito.verifyZeroInteractions(servletRequest);
	}

	/**
	 * Test X-Forwarded-For ip address extraction with fallback when no ip is contained.
	 *
	 * Note mgrigorov: this test could fail in network setups where unknown addresses, like "blah",
	 * will resolve to some DNS service saying "'blah' domain is free. Buy it."
	 */
	@Test
	@Ignore
	public void testExtractFromContainerRequestUnknownXForwardedFor()
	{
		String expected = "10.17.37.8";
		when(servletRequest.getRemoteAddr()).thenReturn(expected);
		when(webRequest.getHeader("X-Forwarded-For")).thenReturn("unknown");
		WebClientInfo clientInfo = new WebClientInfo(requestCycleMock, "No user agent");
		String actual = clientInfo.getRemoteAddr(requestCycleMock);
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * Test default ip address extraction for container request.
	 */
	@Test
	public void testExtractFromContainerRequestNoXForwardedFor()
	{
		String expected = "10.17.37.8";
		when(servletRequest.getRemoteAddr()).thenReturn(expected);
		WebClientInfo clientInfo = new WebClientInfo(requestCycleMock, "No user agent");
		String actual = clientInfo.getRemoteAddr(requestCycleMock);
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * Test X-Forwarded-For ip address extraction when proxy chain is given.
	 */
	@Test
	public void testExtractFromXForwardedForHeaderChainedIps()
	{
		String expected = "10.17.37.156";
		when(servletRequest.getRemoteAddr()).thenReturn("10.17.1.1");
		when(webRequest.getHeader("X-Forwarded-For")).thenReturn(expected + ", 10.17.37.1");
		WebClientInfo clientInfo = new WebClientInfo(requestCycleMock, "No user agent");
		String actual = clientInfo.getRemoteAddr(requestCycleMock);
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * Test X-Forwarded-For ipv6 address extraction.
	 */
	@Test
	public void testExtractFromXForwardedForHeaderIPv6()
	{
		String expected = "2001:db8::1428:57";
		when(webRequest.getHeader("X-Forwarded-For")).thenReturn("2001:db8::1428:57");
		WebClientInfo clientInfo = new WebClientInfo(requestCycleMock, "No user agent");
		String actual = clientInfo.getRemoteAddr(requestCycleMock);
		assertThat(actual, is(equalTo(expected)));
	}

}
