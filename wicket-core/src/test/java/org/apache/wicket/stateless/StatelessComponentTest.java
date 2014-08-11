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

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Test;

/**
 * @author jcompagner
 */
public class StatelessComponentTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	public void statelessComponentPage() throws Exception
	{
		executeTest(StatelessComponentPage.class, "StatelessComponentPage_result.html");

		tester.getRequest()
			.setUrl(
				Url.parse("wicket/bookmarkable/org.apache.wicket.stateless.StatelessComponentPage?0-1.ILinkListener-link"));
		try
		{
			tester.processRequest();
			fail();
		}
		catch (Exception e)
		{
			assertEquals("wanted exception", e.getMessage());
		}

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void statelessComponentPageWithMount() throws Exception
	{
		tester.getApplication().mountPage("/stateless", StatelessComponentPage.class);
		// test is always the home page. it doesn't work then
		executeTest(StatelessComponentPage.class, "StatelessComponentPage_mount_result.html");
		tester.getRequest()
			.setUrl(
				Url.parse("stateless?0-1.ILinkListener-link&testParam1=testValue1&testParam2=testValue2"));
		try
		{
			tester.processRequest();
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
	@Test
	public void statelessComponentPageWithParams() throws Exception
	{
		PageParameters params = new PageParameters();
		params.set("testParam1", "testValue1", INamedParameters.Type.QUERY_STRING);
		params.set("testParam2", "testValue2", INamedParameters.Type.QUERY_STRING);

		executeTest(StatelessComponentPageWithParams.class, params,
			"StatelessComponentPageWithParams_result.html");

		tester.getRequest()
			.setUrl(
				Url.parse("wicket/bookmarkable/org.apache.wicket.stateless.StatelessComponentPageWithParams?0-1.ILinkListener-link&amp;testParam1=testValue1&amp;testParam2=testValue2"));
		try
		{
			tester.processRequest();
			fail();
		}
		catch (Exception e)
		{
			assertEquals("wanted exception", e.getMessage());
		}

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void statelessComponentPageWithParamsWithMount() throws Exception
	{
		PageParameters params = new PageParameters();
		params.set("testParam1", "testValue1", INamedParameters.Type.QUERY_STRING);
		params.set("testParam2", "testValue2", INamedParameters.Type.QUERY_STRING);
		tester.getApplication().mountPage("/stateless", StatelessComponentPageWithParams.class);
		// test is always the home page. it doesn't work then
		executeTest(StatelessComponentPageWithParams.class, params,
			"StatelessComponentPageWithParams_mount_result.html");
		tester.getRequest()
			.setUrl(
				Url.parse("stateless?0-1.ILinkListener-link&amp;testParam1=testValue1&amp;testParam2=testValue2"));
		try
		{
			tester.processRequest();
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
	@Test
	public void statelessComponentPageWithParamsWithIndexMount() throws Exception
	{
		PageParameters params = new PageParameters();
		params.set(0, "testValue1");
		params.set(1, "testValue2");
		tester.getApplication().mountPage("/stateless", StatelessComponentPageWithParams.class);
		// test is always the home page. it doesn't work then
		executeTest(StatelessComponentPageWithParams.class, params,
			"StatelessComponentPageWithParams_indexed_mount_result.html");
		tester.getRequest().setUrl(
			Url.parse("stateless/testValue1/testValue2?0-1.ILinkListener-link"));
		try
		{
			tester.processRequest();
			fail("An exception should have been thrown for this request!");
		}
		catch (Exception e)
		{
			assertEquals("wanted exception", e.getMessage());
		}
	}
}
