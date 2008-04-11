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

import org.apache.wicket.WicketTestCase;

/**
 * Tests package resources.
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class StatelessStatefullUrlCodingStrategyTest extends WicketTestCase
{

	/**
	 * @throws Exception
	 */
	public void testStatelessQueryStringUrlCodingStrategy() throws Exception
	{
		tester.getApplication().mount(
			new QueryStringUrlCodingStrategy("/stateless", StatelessPage.class));

		executeTest(StatelessPage.class, "StatelessPage_QueryString_Result.html");

		tester.setParameterForNextRequest("statelessform:textfield", "test");
		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/stateless/?wicket:interface=%3A0%3Astatelessform%3A%3AIFormSubmitListener%3A%3A");
		tester.processRequestCycle();
		tester.assertResultPage(StatelessPage.class, "StatelessPage_QueryString_SubmitResult.html");

	}

	/**
	 * @throws Exception
	 */
	public void testStatefullQueryStringUrlCodingStrategy() throws Exception
	{
		tester.getApplication().mount(
			new QueryStringUrlCodingStrategy("/statefull", StatefulPage.class));
		executeTest(StatefulPage.class, "StatefulPage_QueryString_Result.html");

		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/?wicket:interface=:0:actionLink::ILinkListener::");
		tester.processRequestCycle();

		tester.setParameterForNextRequest("statelessform:textfield", "test");
		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/statefull/?wicket:interface=%3A0%3Astatelessform%3A%3AIFormSubmitListener%3A%3A");
		tester.processRequestCycle();
		tester.assertResultPage(StatefulPage.class, "StatefulPage_QueryString_SubmitResult.html");


	}

	/**
	 * @throws Exception
	 */
	public void testStatelessDefaultUrlCodingStrategy() throws Exception
	{
		tester.getApplication().mountBookmarkablePage("/stateless", StatelessPage.class);

		executeTest(StatelessPage.class, "StatelessPage_Default_Result.html");

		tester.setParameterForNextRequest("statelessform:textfield", "test");
		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/stateless/wicket:interface/%3A0%3Astatelessform%3A%3AIFormSubmitListener%3A%3A/");
		tester.processRequestCycle();
		tester.assertResultPage(StatelessPage.class, "StatelessPage_Default_SubmitResult.html");

	}

	/**
	 * @throws Exception
	 */
	public void testStatefullDefaultUrlCodingStrategy() throws Exception
	{
		tester.getApplication().mountBookmarkablePage("/statefull", StatefulPage.class);
		executeTest(StatefulPage.class, "StatefulPage_Default_Result.html");

		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/wicket:interface/%3A0%3Astatelessform%3A%3AIFormSubmitListener%3A%3A/");
		tester.processRequestCycle();

		tester.setParameterForNextRequest("statelessform:textfield", "test");
		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/statefull/wicket:interface/%3A0%3Astatelessform%3A%3AIFormSubmitListener%3A%3A/");
		tester.processRequestCycle();
		tester.assertResultPage(StatefulPage.class, "StatefulPage_Default_SubmitResult.html");


	}

	/**
	 * @throws Exception
	 */
	public void testStatelessIndexedUrlCodingStrategy() throws Exception
	{
		tester.getApplication().mount(
			new IndexedParamUrlCodingStrategy("/stateless", StatelessPage.class));

		executeTest(StatelessPage.class, "StatelessPage_Indexed_Result.html");

		tester.setParameterForNextRequest("statelessform:textfield", "test");
		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/stateless/wicket:interface/%3A0%3Astatelessform%3A%3AIFormSubmitListener%3A%3A/");
		tester.processRequestCycle();
		tester.assertResultPage(StatelessPage.class, "StatelessPage_Indexed_SubmitResult.html");

	}

	/**
	 * @throws Exception
	 */
	public void testStatefullIndexedUrlCodingStrategy() throws Exception
	{
		tester.getApplication().mount(
			new IndexedParamUrlCodingStrategy("/statefull", StatefulPage.class));
		executeTest(StatefulPage.class, "StatefulPage_Indexed_Result.html");

		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/?wicket:interface=:0:actionLink::ILinkListener::");
		tester.processRequestCycle();

		tester.setParameterForNextRequest("statelessform:textfield", "test");
		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/statefull/wicket:interface/%3A0%3Astatelessform%3A%3AIFormSubmitListener%3A%3A/");
		tester.processRequestCycle();
		tester.assertResultPage(StatefulPage.class, "StatefulPage_Indexed_SubmitResult.html");


	}

	/**
	 * @throws Exception
	 */
	public void testStatelessHybridUrlCodingStrategy() throws Exception
	{
		tester.getApplication().mount(
			new HybridUrlCodingStrategy("/stateless", StatelessPage.class));

		executeTest(StatelessPage.class, "StatelessPage_Hybrid_Result.html");

		tester.setParameterForNextRequest("statelessform:textfield", "test");
		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/stateless/wicket:interface/%3A0%3Astatelessform%3A%3AIFormSubmitListener%3A%3A/");
		tester.processRequestCycle();
		tester.assertResultPage(StatelessPage.class, "StatelessPage_Hybrid_SubmitResult.html");

	}

	/**
	 * @throws Exception
	 */
	public void testStatefullHybridUrlCodingStrategy() throws Exception
	{
		tester.getApplication()
			.mount(new HybridUrlCodingStrategy("/statefull", StatefulPage.class));
		executeTest(StatefulPage.class, "StatefulPage_Hybrid_Result.html");

		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/?wicket:interface=:0:actionLink::ILinkListener::");
		tester.processRequestCycle();

		tester.setParameterForNextRequest("statelessform:textfield", "test");
		tester.setupRequestAndResponse();
		tester
			.getServletRequest()
			.setURL(
				"/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/statefull/wicket:interface/%3A0%3Astatelessform%3A%3AIFormSubmitListener%3A%3A/");
		tester.processRequestCycle();
		tester.assertResultPage(StatefulPage.class, "StatefulPage_Hybrid_SubmitResult.html");


	}

}
