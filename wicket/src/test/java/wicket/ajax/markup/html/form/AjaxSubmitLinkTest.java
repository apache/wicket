/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.ajax.markup.html.form;

import wicket.Page;
import wicket.WicketTestCase;
import wicket.ajax.AjaxRequestTarget;
import wicket.markup.html.form.Form;
import wicket.util.tester.ITestPageSource;
import wicket.util.tester.TagTester;

/**
 * 
 * 
 * @author Frank Bille (billen)
 */
public class AjaxSubmitLinkTest extends WicketTestCase
{
	
	/**
	 * Construct.
	 */
	public AjaxSubmitLinkTest() 
	{
		super("Ajax Submit link test");
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
				FormWithLinkPage page = new FormWithLinkPage();
				
				new AjaxSubmitLink(page, FormWithLinkPage.SUBMIT_ID, page.getForm())
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
		
		tester.assertComponent(FormWithLinkPage.SUBMIT_ID, AjaxSubmitLink.class);
		
		TagTester ajaxSubmitLink = tester.getTagByWicketId(FormWithLinkPage.SUBMIT_ID);
		
		assertEquals("a", ajaxSubmitLink.getName());
		
		assertTrue(ajaxSubmitLink.getAttributeIs("href", "#"));
		assertTrue(ajaxSubmitLink.getAttributeContains("onclick", "wicketSubmitFormById"));
		assertTrue(ajaxSubmitLink.getAttributeEndsWith("onclick", "return false;"));
	}
}
