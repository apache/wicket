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
package org.apache.wicket.ng.test;

import junit.framework.TestCase;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.ng.ThreadContext;
import org.apache.wicket.ng.mock.WicketTester;


public class TestWicketTester extends TestCase
{
	public static class Page1 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		public Page1()
		{
			Link l;
			add(l = new Link("link")
			{
				@Override
				public void onClick()
				{
					System.out.println("Link clicked!");
				}
			});
		}
	};

	public void testPageRender1()
	{
		WicketTester tester = new WicketTester();

		// this is not too pretty but new Page1() needs Session.get() to be working for various
		// reasons
		ThreadContext old = ThreadContext.get(false);
		ThreadContext.setSession(tester.getApplication().getSession());

		Page page = new Page1();

		tester.startPage(page);

		System.out.println(tester.getLastResponse().getTextResponse());

		Component c = tester.getLastRenderedPage().get("link");

		tester.executeListener(c, ILinkListener.INTERFACE);

		tester.destroy();

		ThreadContext.restore(old);
	}
}
