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
package org.apache.wicket.util.tester;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;

/**
 * A dummy home page, as required by <code>WicketTester</code>.
 * 
 * @author Ingram Chen
 * @since 1.2.6
 */
public class DummyHomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private final Link<?> testPageLink;

	/**
	 * Constructor.
	 * 
	 */
	public DummyHomePage()
	{
		testPageLink = new TestLink("testPage");
		add(testPageLink);
	}

	/**
	 * Retrieves the test page <code>Link</code>.
	 * 
	 * @return the test page <code>Link</code>
	 */
	public Link<?> getTestPageLink()
	{
		return testPageLink;
	}

	protected Page getTestPage() {
		throw new UnsupportedOperationException("To use DummyHomePage.TestLink you need to implement DummyHomePage#getTestPage() method.");
	}
	
	/**
	 * <code>TestLink</code> class.
	 */
	public class TestLink extends Link<Void>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 * 
		 * @param id
		 *            <code>Component</code> id of the <code>TestLink</code>
		 */
		public TestLink(String id)
		{
			super(id);
		}

		@Override
		public void onClick()
		{
			setResponsePage(getTestPage());
		}
	}
}
