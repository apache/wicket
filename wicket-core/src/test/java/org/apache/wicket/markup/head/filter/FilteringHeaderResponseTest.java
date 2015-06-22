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

import java.util.Collections;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.head.internal.HeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;
import org.apache.wicket.request.Response;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for FilteringHeaderResponse
 * 
 * @since 6.0
 */
public class FilteringHeaderResponseTest extends WicketTestCase
{

	@Test
	public void footerDependsOnHeadItem() throws Exception
	{
		tester.getApplication().setHeaderResponseDecorator(new IHeaderResponseDecorator()
		{
			@Override
			public IHeaderResponse decorate(IHeaderResponse response)
			{
				// use this header resource decorator to load all JavaScript resources in the page
				// footer (after </body>)
				return new JavaScriptFilteredIntoFooterHeaderResponse(response, "footerJS");
			}
		});
		executeTest(FilteredHeaderPage.class, "FilteredHeaderPageExpected.html");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5057
	 * @throws Exception
	 */
	@Test
	public void createBucketOnTheFlyForFilteredHeaderItem() throws Exception
	{
		FilteringHeaderResponse headerResponse = new FilteringHeaderResponse(new HeaderResponse()
		{
			@Override
			protected Response getRealResponse()
			{
				return new StringResponse();
			}
		}, "headerBucketName", Collections.EMPTY_LIST);

		String filterName = "filterName";
		String headerContent = "content";
		FilteredHeaderItem item = new FilteredHeaderItem(StringHeaderItem.forString(headerContent), filterName);
		headerResponse.render(item);
		CharSequence realContent = headerResponse.getContent(filterName);
		assertEquals(headerContent, realContent.toString());
	}
}
