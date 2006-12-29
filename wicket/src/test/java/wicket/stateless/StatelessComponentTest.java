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
package wicket.stateless;

import wicket.WicketTestCase;
import wicket.util.tester.WicketTester;

/**
 * @author jcompagner
 */
public class StatelessComponentTest extends WicketTestCase
{

	/**
	 * Construct.
	 * @param name
	 */
	public StatelessComponentTest(String name)
	{
		super(name);
	}
	
	/**
	 * @throws Exception
	 */
	public void testStatelessComponentPageWithHomePage() throws Exception
	{
		tester = new WicketTester(StatelessComponentPage.class);
		executeTest(StatelessComponentPage.class, "StatelessComponentPage_home_result.html");
		
		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication?wicket:bookmarkablePage=:wicket.stateless.StatelessComponentPage&wicket:interface=:0:link::ILinkListener");
		try
		{
			tester.processRequestCycle();
			assertTrue(false);
		} 
		catch (Exception e)
		{
			assertEquals(e.getMessage(),"wanted exception");
		}

	}
	/**
	 * @throws Exception
	 */
	public void testStatelessComponentPage() throws Exception
	{
		executeTest(StatelessComponentPage.class, "StatelessComponentPage_result.html");
		
		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication?wicket:bookmarkablePage=:wicket.stateless.StatelessComponentPage&wicket:interface=:0:link::ILinkListener");
		try
		{
			tester.processRequestCycle();
			assertTrue(false);
		} 
		catch (Exception e)
		{
			assertEquals(e.getMessage(),"wanted exception");
		}

	}
	
	/**
	 * @throws Exception
	 */
	public void testStatelessComponentPageWithMount() throws Exception
	{
		tester.getApplication().mountBookmarkablePage("/stateless", StatelessComponentPage.class);
		
		executeTest(StatelessComponentPage.class, "StatelessComponentPage_mount_result.html");
		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/stateless/wicket:interface/:0:link::ILinkListener");
		try
		{
			tester.processRequestCycle();
			assertTrue(false);
		} 
		catch (Exception e)
		{
			assertEquals(e.getMessage(),"wanted exception");
		}
	}

}
