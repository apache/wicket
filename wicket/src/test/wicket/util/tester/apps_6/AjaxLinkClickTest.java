/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.tester.apps_6;

import wicket.Page;
import wicket.WicketTestCase;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.util.tester.ITestPageSource;

/**
 * Test that the clickLink method also works with AjaxLinks
 * 
 * @author Frank Bille
 */
public class AjaxLinkClickTest extends WicketTestCase
{
	private boolean linkClicked;
	private AjaxRequestTarget ajaxRequestTarget;

	/**
	 * Construct.
	 */
	public AjaxLinkClickTest()
	{
		super("AjaxLink click test");
	}

	/**
	 * Make sure that our test flags are reset between every test.
	 * @see wicket.WicketTestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		
		linkClicked = false;
		ajaxRequestTarget = null;
	}

	/**
	 * Test that an AjaxLink's onClick method is actually invoked.
	 */
	public void testBasicAjaxLinkClick()
	{
		final Page page = new MockPageWithLink();
				
		// Create a link, which we test is actually invoked
		new AjaxLink(page, "ajaxLink")
		{
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target)
			{
				linkClicked = true;
				ajaxRequestTarget = target;
			}
		};

		application.startPage(new ITestPageSource()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});

		application.clickLink("ajaxLink");

		assertTrue(linkClicked);
		assertNotNull(ajaxRequestTarget);
	}
}
