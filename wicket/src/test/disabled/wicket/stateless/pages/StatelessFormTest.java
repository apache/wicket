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
package org.apache.wicket.stateless.pages;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.util.tester.WicketTester;

/**
 * User: Anatoly Kupriyanov (kan.izh@gmail.com) Date: 12-Feb-2009 Time: 22:27:08 Bugfix for
 * http://issues.apache.org/jira/browse/WICKET-1897
 */
public class StatelessFormTest extends WicketTestCase
{
	@Override
	protected void setUp() throws Exception
	{
		tester = new WicketTester(new WebApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				mountBookmarkablePage("page1", Page1.class);
				mountBookmarkablePage("page2", Page2.class);
			}

			@Override
			public Class<? extends Page> getHomePage()
			{
				return Page1.class;
			}

			@Override
			protected void outputDevelopmentModeWarning()
			{
				// Do nothing.
			}
		});
	}

	public void testBug()
	{
		{
			final WebRequestCycle cycle = tester.setupRequestAndResponse(false);
			tester.getServletRequest().setURL("page2");
			tester.processRequestCycle(cycle);
			tester.assertRenderedPage(Page2.class);
		}
		{
			final WebRequestCycle cycle = tester.setupRequestAndResponse(false);
			tester.getServletRequest().setURL("page1");
			tester.processRequestCycle(cycle);
			tester.assertRenderedPage(Page1.class);
		}
		{
			final WebRequestCycle cycle = tester.setupRequestAndResponse(false);
			tester.getServletRequest().setURL(
				"page1/wicket:interface/:0:form::IFormSubmitListener::");
			tester.processRequestCycle(cycle);
			tester.assertRenderedPage(Page1.class);
		}
	}
}
