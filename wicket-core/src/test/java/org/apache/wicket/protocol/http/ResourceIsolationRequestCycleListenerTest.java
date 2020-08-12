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

import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.CROSS_SITE;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.DEST_EMBED;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.DEST_OBJECT;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.MODE_NAVIGATE;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.SAME_ORIGIN;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.SAME_SITE;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.SEC_FETCH_DEST_HEADER;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.SEC_FETCH_MODE_HEADER;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.SEC_FETCH_SITE_HEADER;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.VARY_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.protocol.http.IResourceIsolationPolicy.ResourceIsolationOutcome;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ResourceIsolationRequestCycleListener}. 
 */
public class ResourceIsolationRequestCycleListenerTest extends WicketTestCase
{

	private ResourceIsolationRequestCycleListener listener;

	@BeforeEach
	void before()
	{
		withCustomListener(new ResourceIsolationRequestCycleListener());
	}

	void withCustomListener(ResourceIsolationRequestCycleListener fetchMetadataListener)
	{
		WebApplication application = tester.getApplication();

		if (this.listener != null)
		{
			application.getRequestCycleListeners().remove(this.listener);
		}
		this.listener = fetchMetadataListener;
		application.getRequestCycleListeners().add(fetchMetadataListener);

		tester.startPage(FirstPage.class);
		tester.assertRenderedPage(FirstPage.class);
	}

	/**
	 * Tests whether a request with Sec-Fetch-Site = cross-site is aborted
	 */
	@Test
	void crossSiteFMAborted()
	{
		tester.addRequestHeader(SEC_FETCH_SITE_HEADER, CROSS_SITE);

		assertRequestAborted();
	}

	/**
	 * Tests whether embed requests are aborted by fetch metadata checks
	 */
	@Test
	void destEmbedFMAborted()
	{
		tester.addRequestHeader(SEC_FETCH_SITE_HEADER, CROSS_SITE);
		tester.addRequestHeader(SEC_FETCH_DEST_HEADER, DEST_EMBED);

		assertRequestAborted();
	}

	/**
	 * Tests whether object requests (sec-fetch-dest :"object" ) are aborted by FM checks
	 */
	@Test
	void destObjectAborted()
	{
		tester.addRequestHeader(SEC_FETCH_SITE_HEADER, CROSS_SITE);
		tester.addRequestHeader(SEC_FETCH_DEST_HEADER, DEST_OBJECT);

		assertRequestAborted();
	}

	/**
	 * Tests whether a top level navigation request is allowed by FM checks
	 */
	@Test
	void topLevelNavigationAllowedFM()
	{
		tester.addRequestHeader(SEC_FETCH_SITE_HEADER, SAME_ORIGIN);
		tester.addRequestHeader(SEC_FETCH_MODE_HEADER, MODE_NAVIGATE);

		assertRequestAccepted();
	}

	/**
	 * Tests that requests rejected by fetch metadata have the Vary header set
	 */
	@Test
	void varyHeaderSetWhenFetchMetadataRejectsRequest()
	{
		tester.addRequestHeader(SEC_FETCH_SITE_HEADER, CROSS_SITE);
		tester.setFollowRedirects(false);
		assertRequestAborted();

		String vary = tester.getLastResponse().getHeader("Vary");

		if (vary == null)
		{
			throw new AssertionError("Vary header should not be null");
		}

		if (!vary.contains(SEC_FETCH_DEST_HEADER) || !vary.contains(SEC_FETCH_MODE_HEADER)
			|| !vary.contains(SEC_FETCH_SITE_HEADER))
		{
			throw new AssertionError("Unexpected vary header: " + vary);
		}
	}

	/**
	 * Tests that requests accepted by fetch metadata have the Vary header set
	 */
	@Test
	void varyHeaderSetWhenFetchMetadataAcceptsRequest()
	{
		tester.addRequestHeader(SEC_FETCH_SITE_HEADER, SAME_SITE);
		tester.setFollowRedirects(false);
		assertRequestAccepted();

		String vary = tester.getLastResponse().getHeader(VARY_HEADER);
		if (vary == null)
		{
			throw new AssertionError("Vary header should not be null");
		}

		if (!vary.contains(SEC_FETCH_DEST_HEADER) || !vary.contains(SEC_FETCH_MODE_HEADER)
			|| !vary.contains(SEC_FETCH_SITE_HEADER))
		{
			throw new AssertionError("Unexpected vary header: " + vary);
		}
	}

	@Test
	void whenAtFirstNotUnkownRejectsRequest_thenRequestRejected()
	{
		withCustomListener(new ResourceIsolationRequestCycleListener(
			(request, page) -> ResourceIsolationOutcome.UNKNOWN,
			(request, page) -> ResourceIsolationOutcome.UNKNOWN,
			(request, page) -> ResourceIsolationOutcome.DISALLOWED,
			(request, page) -> ResourceIsolationOutcome.ALLOWED));
		assertRequestAborted();
	}

	@Test
	void whenFirstNotUnknownPolicieAcceptRequest_thenRequestAccepted()
	{
		withCustomListener(new ResourceIsolationRequestCycleListener(
			(request, page) -> ResourceIsolationOutcome.UNKNOWN,
			(request, page) -> ResourceIsolationOutcome.ALLOWED,
			(request, page) -> ResourceIsolationOutcome.ALLOWED,
			(request, page) -> ResourceIsolationOutcome.ALLOWED));
		assertRequestAccepted();
	}

	@Test
	void whenCrossOriginRequestToExempted_thenRequestAccepted()
	{
		listener
			.addExemptedPaths("/wicket/bookmarkable/org.apache.wicket.protocol.http.FirstPage");
		withCustomListener(listener);

		tester.addRequestHeader(SEC_FETCH_SITE_HEADER, CROSS_SITE);
		assertRequestAccepted();
	}

	private void assertRequestAborted()
	{
		tester.clickLink("link");
		assertEquals(tester.getLastResponse().getStatus(),
			javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
		assertEquals(tester.getLastResponse().getErrorMessage(),
			ResourceIsolationRequestCycleListener.ERROR_MESSAGE);
	}

	private void assertRequestAccepted()
	{
		tester.clickLink("link");
		tester.assertRenderedPage(SecondPage.class);
	}
}
