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

import org.apache.wicket.ng.Component;
import org.apache.wicket.ng.Page;
import org.apache.wicket.ng.markup.html.link.ILinkListener;
import org.apache.wicket.ng.markup.html.link.Link;
import org.apache.wicket.ng.mock.WicketTester;


public class TestWicketTester extends TestCase
{
	public static class Page1 extends Page
	{
		private static final long serialVersionUID = 1L;

		public Page1()
		{
			Link l;
			add(l = new Link("link")
			{
				private static final long serialVersionUID = 1L;

				public void onLinkClicked()
				{
					System.out.println("Link clicked!");
				}
			});
			l.setLabel("A Link!");
		}

		@Override
		public void renderPage()
		{
			super.renderPage();
		}
	};

	public void testPageRender1()
	{
		WicketTester tester = new WicketTester();

		tester.startPage(new Page1());

		System.out.println(tester.getLastResponse().getTextResponse());

		Component c = tester.getLastRenderedPage().get("link");

		tester.executeListener(c, ILinkListener.INTERFACE);

		tester.destroy();
	}
}
