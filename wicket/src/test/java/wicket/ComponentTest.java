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
package wicket;

import wicket.markup.IMarkupResourceStreamProvider;
import wicket.markup.html.WebPage;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.StringResourceStream;


/**
 * Test for ajax handler.
 * 
 * @author Juergen Donnerstag
 */
public class ComponentTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public ComponentTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
		executeTest(TestPage_1.class, "TestPageExpectedResult_1.html");
	}

	/**
	 * Tests traps for bad onattach/ondetach overrides
	 */
	public void testAttachDetachTraps()
	{
		try
		{
			tester.setupRequestAndResponse();
			Page page = new BadOnAttachPage();
			page.attach();
			fail("bad onAttach() override did not trigger an exception");
		}
		catch (IllegalStateException e)
		{
			// noop
		}
		try
		{
			tester.setupRequestAndResponse();
			Page page = new BadOnDetachPage();
			page.attach();
			page.detach();
			fail("bad onDetach() override did not trigger an exception");
		}
		catch (IllegalStateException e)
		{
			// noop
		}
	}


	/**
	 * @author ivaynberg
	 */
	public static class BaseTestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<? extends MarkupContainer> containerClass)
		{
			return new StringResourceStream("helloworld");
		}

	}

	/**
	 * Page with a bad onattach() override
	 * 
	 * @author ivaynberg
	 */
	public static class BadOnAttachPage extends BaseTestPage
	{

		private static final long serialVersionUID = 1L;

		@Override
		protected void onAttach()
		{
			// super.onAttach();
		}
	}

	/**
	 * Page with a bad ondetach() override
	 * 
	 * @author ivaynberg
	 */
	public static class BadOnDetachPage extends BaseTestPage
	{

		private static final long serialVersionUID = 1L;

		@Override
		protected void onDetach()
		{
			// super.onDetach();
		}
	}

}
