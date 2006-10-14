/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision$ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
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
package wicket.util.tester.apps_4;

import wicket.WicketTestCase;
import wicket.util.tester.FormTester;

/**
 * @author Ingram Chen
 */
public class FormTesterTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public FormTesterTest(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * @throws Exception
	 */
	public void test_1() throws Exception
	{
		application.setHomePage(EmailPage.class);
		application.setupRequestAndResponse();
		application.processRequestCycle();

		assertEquals(EmailPage.class, application.getLastRenderedPage().getClass());
		EmailPage page = (EmailPage)application.getLastRenderedPage();

		FormTester formTester = application.newFormTester("form");

		formTester.setValue("email", "a");
		formTester.submit();

		assertEquals(EmailPage.class, application.getLastRenderedPage().getClass());
		page = (EmailPage)application.getLastRenderedPage();

		assertNull(page.getEmail());
		assertTrue(page.getFeedbackMessages().hasMessageFor(page.get("form:email")));
		assertEquals("wrong email address pattern for email", page.getFeedbackMessages()
				.messageForComponent(page.get("form:email")).getMessage().toString());
	}
}
