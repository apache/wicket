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
package wicket.ajax.markup.html.form;

import wicket.Page;
import wicket.WicketTestCase;
import wicket.ajax.AjaxRequestTarget;
import wicket.markup.html.form.Form;
import wicket.util.tester.ITestPageSource;
import wicket.util.tester.TagTester;

/**
 * Test of ajax submit button.
 * 
 * @author Frank Bille (billen)
 */
public class AjaxSubmitButtonTest extends WicketTestCase
{
	/**
	 * Construct.
	 */
	public AjaxSubmitButtonTest()
	{
		super("Test of ajax submit button");
	}
	
	/**
	 * Test that the component can be rendered under normal conditions
	 */
	public void testRender()
	{
		tester.startPage(new ITestPageSource() {
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				FormWithButtonPage page = new FormWithButtonPage();
				
				AjaxSubmitButton button = new AjaxSubmitButton(page.getForm(), FormWithButtonPage.SUBMIT_ID, page.getForm())
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form form)
					{
					}
				};
				
				return page;
			}
		});
		
		tester.assertComponent("form:ajaxSubmitButton", AjaxSubmitButton.class);
		
		// Get the tag which has wicket:id "ajaxSubmitButton"
		TagTester ajaxSubmitButton = tester.getTagByWicketId("ajaxSubmitButton");
		
		// The tag should be an "input"
		assertEquals("input", ajaxSubmitButton.getName());
		
		// The onclick attribute must be present and contain a call to wicketSubmitFormById
		assertTrue(ajaxSubmitButton.hasAttribute("onclick"));
		assertTrue(ajaxSubmitButton.getAttributeContains("onclick", "wicketSubmitFormById"));
		assertTrue(ajaxSubmitButton.getAttributeContains("onclick", "return false"));
		assertTrue(ajaxSubmitButton.getAttributeIs("type", "submit"));
	}
}
