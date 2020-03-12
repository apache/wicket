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
package org.apache.wicket.markup.head.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.apache.wicket.csp.ContentSecurityPolicySettings;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.head.internal.HeaderResponse;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests for FilteringHeaderResponse
 * 
 * @since 6.0
 */
class FilteringHeaderResponseTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected ContentSecurityPolicySettings newCspSettings()
			{
				return new ContentSecurityPolicySettings(this)
				{
					@Override
					public String getNonce(RequestCycle cycle, IRequestHandler currentHandler)
					{
						return "NONCE";
					}
				};
			}
		};
	}

	@Test
	void footerDependsOnHeadItem() throws Exception
	{
		// use this header resource decorator to load all JavaScript resources in the page
		// footer (after </body>)
		tester.getApplication()
			.getHeaderResponseDecorators()
			.add(response -> new JavaScriptFilteredIntoFooterHeaderResponse(response, "footerJS"));
		executeTest(FilteredHeaderPage.class, "FilteredHeaderPageExpected.html");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5057
	 *
	 * @throws Exception
	 */
	@Test
	void createBucketOnTheFlyForFilteredHeaderItem() throws Exception
	{
		try (FilteringHeaderResponse headerResponse = new FilteringHeaderResponse(
			new HeaderResponse()
			{
				@Override
				protected Response getRealResponse()
				{
					return new StringResponse();
				}
			}, "headerBucketName", Collections.emptyList()))
		{
			String filterName = "filterName";
			String headerContent = "content";
			FilteredHeaderItem item = new FilteredHeaderItem(
				StringHeaderItem.forString(headerContent), filterName);
			headerResponse.render(item);
			CharSequence realContent = headerResponse.getContent(filterName);
			assertEquals(headerContent, realContent.toString());
		}
	}

	/**
	 * WICKET-6498 all JavaScript resources have an "defer" attribute, all other JavaScript is
	 * inside a {@code document.addEventListener('DOMContentLoaded', function() {}; } hook.
	 */
	@Test
	void deferred() throws Exception
	{
		tester.getApplication()
			.getHeaderResponseDecorators()
			.add(response -> new JavaScriptDeferHeaderResponse(response));
		executeTest(DeferredPage.class, "DeferredPageExpected.html");
	}

	/**
	 * WICKET-6682
	 */
	@Test
	void nonce() throws Exception
	{
		tester.getApplication().getCspSettings().blocking().strict();
		executeTest(CspNoncePage.class, "CspNoncePageExpected.html");
	}
}
