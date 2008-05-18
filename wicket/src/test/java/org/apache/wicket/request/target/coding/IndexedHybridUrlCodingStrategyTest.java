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

import org.apache.wicket.IRedirectListener;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.target.component.listener.ListenerInterfaceRequestTarget;
import org.apache.wicket.util.tester.WicketTester;

/**
 * 
 */
public class IndexedHybridUrlCodingStrategyTest extends TestCase
{
	/**
	 * 
	 */
	public static class TestPage extends WebPage<Void>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param pars
		 */
		public TestPage(PageParameters pars)
		{
			add(new AjaxLink("ajax")
			{
				private static final long serialVersionUID = 1L;

				public void onClick(AjaxRequestTarget target)
				{
					getSession().bind();
					target.addComponent(this);
				}
			}.setOutputMarkupId(true));
			add(new Link("link")
			{
				private static final long serialVersionUID = 1L;

				public void onClick()
				{
					getSession().invalidate();
				}
			});
		}
	}

	/**
	 * Test for a NPE in a page mounted through a {@link IndexedHybridUrlCodingStrategy}.
	 */
	public void testJiraXXXX()
	{
		IRequestTargetUrlCodingStrategy strategy = new IndexedHybridUrlCodingStrategy("/foo",
			TestPage.class);

		WicketTester tester = new WicketTester();
		tester.setupRequestAndResponse();
		TestPage page = new TestPage(null);
		IRequestTarget requestTarget = new ListenerInterfaceRequestTarget(page, page,
			IRedirectListener.INTERFACE);
		strategy.encode(requestTarget);
	}
}
