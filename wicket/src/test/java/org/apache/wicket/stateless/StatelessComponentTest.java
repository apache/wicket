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
package org.apache.wicket.stateless;

import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.target.coding.IndexedParamUrlCodingStrategy;

/**
 * @author jcompagner
 */
public class StatelessComponentTest extends WicketTestCase
{

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public StatelessComponentTest(String name)
	{
		super(name);
	}


	/**
	 * @throws Exception
	 */
	public void testStatelessComponentPage() throws Exception
	{
		executeTest(StatelessComponentPage.class, "StatelessComponentPage_result.html");

		tester.setupRequestAndResponse();
		tester
				.getServletRequest()
				.setURL(
						"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication?wicket:bookmarkablePage=:org.apache.wicket.stateless.StatelessComponentPage&wicket:interface=:0:link::ILinkListener::");
		try
		{
			tester.processRequestCycle();
			assertTrue(false);
		}
		catch (Exception e)
		{
			assertEquals("wanted exception", e.getMessage());
		}

	}

	/**
	 * @throws Exception
	 */
	public void testStatelessComponentPageWithMount() throws Exception
	{
		tester.getApplication().mountBookmarkablePage("/stateless", StatelessComponentPage.class);
		// test is always the home page. it doesn't work then
		executeTest(StatelessComponentPage.class, "StatelessComponentPage_mount_result.html");
		tester.setupRequestAndResponse();
		tester
				.getServletRequest()
				.setURL(
						"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/stateless/wicket:interface/:0:link::ILinkListener::");
		try
		{
			tester.processRequestCycle();
			fail("An exception should have been thrown for this request!");
		}
		catch (Exception e)
		{
			assertEquals("wanted exception", e.getMessage());
		}
	}

	/**
	 * @throws Exception
	 */
	public void testStatelessComponentPageWithParams() throws Exception
	{
		PageParameters params = new PageParameters();
		params.put("testParam1", "testValue1");
		params.put("testParam2", "testValue2");

		executeTest(StatelessComponentPageWithParams.class, params,
				"StatelessComponentPageWithParams_result.html");

		tester.setupRequestAndResponse();
		tester
				.getServletRequest()
				.setURL(
						"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication?wicket:bookmarkablePage=:org.apache.wicket.stateless.StatelessComponentPageWithParams&testParam1=testValue1&testParam2=testValue2&wicket:interface=:0:link::ILinkListener::");
		try
		{
			tester.processRequestCycle();
			assertTrue(false);
		}
		catch (Exception e)
		{
			assertEquals("wanted exception", e.getMessage());
		}

	}

	/**
	 * @throws Exception
	 */
	public void testStatelessComponentPageWithParamsWithMount() throws Exception
	{
		PageParameters params = new PageParameters();
		params.put("testParam1", "testValue1");
		params.put("testParam2", "testValue2");
		tester.getApplication().mountBookmarkablePage("/stateless",
				StatelessComponentPageWithParams.class);
		// test is always the home page. it doesn't work then
		executeTest(StatelessComponentPageWithParams.class, params,
				"StatelessComponentPageWithParams_mount_result.html");
		tester.setupRequestAndResponse();
		tester
				.getServletRequest()
				.setURL(
						"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/stateless/testParam1/testValue1/testParam2/testValue2/wicket:interface/%3A0%3Alink%3A%3AILinkListener%3A%3A/");
		try
		{
			tester.processRequestCycle();
			fail("An exception should have been thrown for this request!");
		}
		catch (Exception e)
		{
			assertEquals("wanted exception", e.getMessage());
		}
	}

	/**
	 * @throws Exception
	 */
	public void testStatelessComponentPageWithParamsWithIndexMount() throws Exception
	{
		PageParameters params = new PageParameters();
		params.put("0", "testValue1");
		params.put("1", "testValue2");
		tester.getApplication().mount(
				new IndexedParamUrlCodingStrategy("/stateless",
						StatelessComponentPageWithParams.class));
		// test is always the home page. it doesn't work then
		executeTest(StatelessComponentPageWithParams.class, params,
				"StatelessComponentPageWithParams_indexed_mount_result.html");
		tester.setupRequestAndResponse();
		tester
				.getServletRequest()
				.setURL(
						"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/stateless/testValue1/testValue2/wicket:interface/%3A0%3Alink%3A%3AILinkListener%3A%3A/");
		try
		{
			tester.processRequestCycle();
			fail("An exception should have been thrown for this request!");
		}
		catch (Exception e)
		{
			assertEquals("wanted exception", e.getMessage());
		}
	}
}
