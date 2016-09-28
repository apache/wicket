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
package org.apache.wicket.noheadnobody;

import static org.hamcrest.Matchers.is;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.mock.MockHomePage;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests that if a page has neither &lt;head&gt; nor &lt;body&gt;
 * and there are header contributors then {@link org.apache.wicket.markup.html.WebPage#reportMissingHead(CharSequence)}
 * is called
 *
 * https://issues.apache.org/jira/browse/WICKET-5895
 */
public class NoHeadNoBodyTest extends WicketTestCase
{
	@Test
	public void noHeadNoBody()
	{
		final AtomicBoolean reported = new AtomicBoolean(false);

		NoHeadNoBodyPage page = new NoHeadNoBodyPage()
		{
			@Override
			protected void reportMissingHead(CharSequence collectedHeaderOutput)
			{
				reported.set(true);
			}
		};

		tester.startPage(page);
		assertThat(reported.get(), is(true));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5955
	 */
	@Test
	public void interceptedRenderDoesNotReportMissingHeader()
	{
		tester.startPage(new MockHomePage()
		{
			@Override
			protected void onInitialize()
			{
				throw new RestartResponseAtInterceptPageException(getApplication().getHomePage());
			}

			@Override
			public void renderHead(IHeaderResponse response)
			{
				super.renderHead(response);

				CoreLibrariesContributor.contribute(getApplication(), response);
			}

			@Override
			protected void reportMissingHead(CharSequence collectedHeaderOutput)
			{
				fail("missing headers should not be reported when rendering was interceptede");
			}
		});
	}
}
