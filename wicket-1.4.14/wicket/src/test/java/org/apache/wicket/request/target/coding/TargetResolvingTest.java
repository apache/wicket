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
package org.apache.wicket.request.target.coding;

import junit.framework.TestCase;

import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.protocol.http.request.urlcompressing.UrlCompressingWebRequestProcessor;
import org.apache.wicket.protocol.https.HttpsConfig;
import org.apache.wicket.protocol.https.HttpsRequestCycleProcessor;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.util.tester.WicketTester;

public class TargetResolvingTest extends TestCase
{
	public void testWebRequestCycleProcessor()
	{
		assertProcessorTargetingTestPage(new WebRequestCycleProcessor());
	}

	public void testHttpsRequestCycleProcessorWithoutPortConfig()
	{
		assertProcessorTargetingTestPage(new HttpsRequestCycleProcessor(null));
	}

	public void testHttpsRequestCycleProcessorWithPortConfig()
	{
		assertProcessorTargetingTestPage(new HttpsRequestCycleProcessor(new HttpsConfig()));
	}

	public void testUrlCompressingWebRequestProcessor()
	{
		assertProcessorTargetingTestPage(new UrlCompressingWebRequestProcessor());
	}

	/**
	 * 
	 * By processing the request just setting the target page as parameter, we force the
	 * RequestCycleProcessor to use its resolve method.
	 */
	public void assertProcessorTargetingTestPage(final IRequestCycleProcessor requestCycleProcessor)
	{
		WicketTester tester = new WicketTester(new WicketTester.DummyWebApplication()
		{
			@Override
			protected IRequestCycleProcessor newRequestCycleProcessor()
			{
				return requestCycleProcessor;
			}
		});
		tester.setupRequestAndResponse();
		tester.getServletRequest().setParameter(
			WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME,
			':' + TestPage.class.getName());
		tester.processRequestCycle();
		tester.assertRenderedPage(TestPage.class);
	}
}
