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
package org.apache.wicket.examples.ng;


import org.apache.wicket.Page;
import org.apache.wicket.ng.markup.html.link.Link;
import org.apache.wicket.ng.request.component.PageParametersNg;
import org.apache.wicket.ng.request.cycle.RequestCycle;

public class TestPage1 extends Page
{
	private static final long serialVersionUID = 1L;

	public TestPage1()
	{
		Link l1 = new Link("l1")
		{
			private static final long serialVersionUID = 1L;

			public void onLinkClicked()
			{
				System.out.println("link 1 clicked");
				getPageParametersNg().setNamedParameter("p1", "v1");
				getPageParametersNg().setIndexedParameter(0, "indexed1");
				getPageParametersNg().setIndexedParameter(1, "indexed2");
				getPageParametersNg().setIndexedParameter(2, "indexed3");

				// necessary on stateless page
				if (getPage().isPageStateless())
					RequestCycle.get().setResponsePage(getPage());
			}
		};
		l1.setBookmarkable(isPageStateless());
		l1.setLabel("Link 1 - Add Some Parameters");
		add(l1);

		Link l2 = new Link("l2")
		{
			private static final long serialVersionUID = 1L;

			public void onLinkClicked()
			{
				System.out.println("link 2 clicked");
				getPageParametersNg().removeNamedParameter("p1");
				getPageParametersNg().clearIndexedParameters();

				if (getPage().isPageStateless())
					// necessary on stateless page
					RequestCycle.get().setResponsePage(getPage());
			}
		};
		l2.setLabel("Link 2 - Remove The Parameters   (this link is bookmarkable listener interface!)");
		l2.setBookmarkable(true);
		add(l2);


		Link l3 = new Link("l3")
		{
			private static final long serialVersionUID = 1L;

			public void onLinkClicked()
			{
				System.out.println("link 3 clicked");
				RequestCycle.get().setResponsePage(new TestPage2());
			}
		};
		// l3.setBookmarkable(true);
		l3.setLabel("Link 3 - Go to Test Page 2 - Not mounted, Not bookmarkable");
		add(l3);


		Link l4 = new Link("l4")
		{
			private static final long serialVersionUID = 1L;

			public void onLinkClicked()
			{
				System.out.println("link 4 clicked");
				RequestCycle.get().setResponsePage(TestPage2.class, null);
			}
		};
		l4.setLabel("Link 4 - Go to Test Page 2 - Not mounted, Bookmarkable");
		add(l4);


		Link l5 = new Link("l5")
		{
			private static final long serialVersionUID = 1L;

			public void onLinkClicked()
			{
				System.out.println("link 5 clicked");
				TestPage3 page = new TestPage3(TestPage1.this);
				page.getPageParametersNg().setIndexedParameter(0, "i1");
				page.getPageParametersNg().setIndexedParameter(1, "i2");
				page.getPageParametersNg().setIndexedParameter(2, "i3");
				RequestCycle.get().setResponsePage(page);
			}
		};
		l5.setLabel("Link 5 - Go to Test Page 3 - Mounted");
		add(l5);

		Link l6 = new Link("l6")
		{
			public void onLinkClicked()
			{
				PageParametersNg params = new PageParametersNg();
				params.setNamedParameter("color", "red");
				RequestCycle.get().setResponsePage(TestPage4.class, params);
			}
		};
		l6.setLabel("Link 6 - Goto Test Page 4 - stateless, with mounted parameters");
		add(l6);
	}

	private boolean rendered = false;

	@Override
	public void renderPage()
	{
		super.renderPage();
		rendered = true;
	}

	@Override
	public boolean isPageStateless()
	{
		return false;
// return !rendered;
	}
}
