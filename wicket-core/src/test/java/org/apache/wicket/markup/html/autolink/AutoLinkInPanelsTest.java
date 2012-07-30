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
package org.apache.wicket.markup.html.autolink;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.autolink.sub.LogoPanel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for WICKET-4681
 * 
 * @author Carl-Eric Menzel
 */
public class AutoLinkInPanelsTest extends WicketTestCase
{
	public static class TestPage extends WebPage
	{
		public TestPage()
		{
			add(new LogoPanel("logo"));
		}
	}

	@Before
	public void setUp()
	{
		tester = new WicketTester(new WebApplication()
		{

			@Override
			public Class<? extends Page> getHomePage()
			{
				return TestPage.class;
			}

			@Override
			protected void init()
			{
				super.init();
				getMarkupSettings().setAutomaticLinking(true);
			}
		});
	}

	@Test
	public void imgTagWorksInPanelWithExtraContainer() throws Exception
	{
		tester.startPage(TestPage.class);
//		tester.dumpPage();
		tester.assertContains("<img src=\"\\./wicket/resource/org.apache.wicket.markup.html.autolink.sub.LogoPanel/logo-ver-\\d+.png\"/>");
	}

	@Test
	public void imgTagWorksInPanelWithoutExtraContainer() throws Exception
	{
		tester.startPage(TestPage.class);
		tester.assertContains("<img src=\"\\./wicket/resource/org.apache.wicket.markup.html.autolink.sub.LogoPanel/logo2-ver-\\d+.png\"/>");
	}
}
