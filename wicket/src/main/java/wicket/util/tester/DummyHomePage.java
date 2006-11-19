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
package wicket.util.tester;

import wicket.markup.html.WebPage;
import wicket.markup.html.link.Link;

/**
 * A dummy homepage required by WicketTester only
 * 
 * @author Ingram Chen
 */
public class DummyHomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private ITestPageSource testPageSource;

	private Link testPageLink;

	/**
	 * Construct
	 */
	public DummyHomePage()
	{
		testPageLink = new TestLink("testPage");
		add(testPageLink);
	}

	/**
	 * 
	 * @param testPageSource
	 */
	public void setTestPageSource(ITestPageSource testPageSource)
	{
		this.testPageSource = testPageSource;
	}

	/**
	 * 
	 * @return Link
	 */
	public Link getTestPageLink()
	{
		return testPageLink;
	}

	/**
	 * 
	 */
	public class TestLink extends Link
	{
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 * @param id
		 */
		public TestLink(String id)
		{
			super(id);
		}

		/**
		 * 
		 */
		public void onClick()
		{
			setResponsePage(testPageSource.getTestPage());
		}
	}
}
