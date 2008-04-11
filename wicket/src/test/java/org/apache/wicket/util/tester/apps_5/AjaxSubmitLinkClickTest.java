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
package org.apache.wicket.util.tester.apps_5;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.ITestPageSource;
import org.apache.wicket.util.tester.apps_5.MockPageWithFormAndLink.MockPojo;


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


	protected void setUp() throws Exception
	{
		super.setUp();

		linkClicked = false;
	}


	/**
	 * 
	 */
	public void testClickLinkInsideForm_ajaxSubmitLink()
	{
		MockPojo mockPojo = new MockPageWithFormAndLink.MockPojo();
		mockPojo.setName("Mock name");

		final MockPageWithFormAndContainedLink page = new MockPageWithFormAndContainedLink(mockPojo);
		page.addLink(new AjaxSubmitLink("link")
		{
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form)
			{
				assertNotNull(form);
				linkClicked = true;
			}
		});

		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});

		tester.assertRenderedPage(MockPageWithFormAndContainedLink.class);

		// Change the name in the textfield
		page.getNameField().setModelValue(new String[] { "new mock value" });

		// Click the submit link
		tester.clickLink("form:link");

		// Has it really been clicked?
		assertTrue(linkClicked);

		// And has the form been "submitted"
		assertEquals("new mock value", mockPojo.getName());
	}

	/**
	 * 
	 */
	public void testClickLink_ajaxSubmitLink()
	{
		MockPojo mockPojo = new MockPageWithFormAndLink.MockPojo();
		mockPojo.setName("Mock name");

		final MockPageWithFormAndLink page = new MockPageWithFormAndLink(mockPojo);
		AjaxSubmitLink link = new AjaxSubmitLink("link", page.getForm())
		{
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form)
			{
				assertNotNull(form);
				linkClicked = true;
			}
		};
		page.add(link);

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
		page.getNameField().setModelValue(new String[] { "new mock value" });

		// Click the submit link
		tester.clickLink("link");

		// Has it really been clicked?
		assertTrue(linkClicked);

		// And has the form been "submitted"
		assertEquals("new mock value", mockPojo.getName());
	}
}
