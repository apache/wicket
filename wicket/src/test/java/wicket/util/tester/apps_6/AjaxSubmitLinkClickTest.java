/*
 * /* $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
import wicket.ajax.markup.html.form.AjaxSubmitLink;
import wicket.markup.html.form.Form;
import wicket.util.tester.ITestPageSource;
import wicket.util.tester.apps_6.MockPageWithFormAndLink.MockPojo;

/**
 * Test that the clickLink method works with AjaxSubmitLinks
 * 
 * @author Frank Bille
 */
public class AjaxSubmitLinkClickTest extends WicketTestCase
{
	private boolean linkClicked;

	/**
	 * Construct.
	 */
	public AjaxSubmitLinkClickTest()
	{
		super("Ajax submit link click test");
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		linkClicked = false;
	}

	/**
	 * 
	 */
	public void testClickLink_ajaxSubmitLink()
	{
		MockPojo mockPojo = new MockPageWithFormAndLink.MockPojo();
		mockPojo.setName("Mock name");

		final MockPageWithFormAndLink page = new MockPageWithFormAndLink(mockPojo);
		new AjaxSubmitLink(page, "link", page.getForm())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form)
			{
				linkClicked = true;
			}
		};

		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});

		tester.assertRenderedPage(MockPageWithFormAndLink.class);

		// Change the name in the textfield
		page.getNameField().setModelValue("new mock value");

		// Click the submit link
		tester.clickLink("link");

		// Has it really been clicked?
		assertTrue(linkClicked);

		// And has the form been "submitted"
		assertEquals("new mock value", mockPojo.getName());
	}
}