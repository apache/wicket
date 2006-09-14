/*
 * $Id$ $Revision$ $Date$
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
package wicket.markup.html.form;

import java.util.MissingResourceException;

import junit.framework.TestCase;
import wicket.properties.MyTesterApplication;
import wicket.properties.TestPage;
import wicket.util.tester.WicketTester;

/**
 * 
 * @author Juergen Donnerstag
 */
public class ValidatorPropertiesTest extends TestCase
{
	/**
	 * 
	 */
	public void test1()
	{
		WicketTester tester = new MyTesterApplication();
		tester.setupRequestAndResponse();
		tester.createRequestCycle();

		TestPage page = new TestPage();
		Form form = (Form)page.get("form1");
		assertNotNull(form);

		page.getText1().setInput("");
		page.getText1().validateRequired();
		page.getText2().setInput("");
		page.getText2().validateRequired();
		page.getText3().setInput("");
		page.getText3().validateRequired();
		page.getText4().setInput("");
		page.getText4().validateRequired();
		page.getText5().setInput("");
		page.getText5().validateRequired();
		page.getText6().setInput("");
		page.getText6().validateRequired();
		page.getText7().setInput("");
		page.getText7().validateRequired();
		page.getText8().setInput("");
		page.getText8().validateRequired();
		page.getText9().setInput("");
		page.getText9().validateRequired();
		page.getText10().setInput("");
		page.getText10().validateRequired();
		page.getText11().setInput("");
		page.getText11().validateRequired();
		page.getText12().setInput("");
		page.getText12().validateRequired();
		
		String msg=page.getText1().getFeedbackMessage().getMessage();

		assertEquals("text1label is required", page.getText1().getFeedbackMessage().getMessage());
		assertEquals("text2 is required", page.getText2().getFeedbackMessage().getMessage());
		assertEquals("ok: text3333 is missing", page.getText3().getFeedbackMessage().getMessage());
		assertEquals("ok: Text4Label is missing", page.getText4().getFeedbackMessage().getMessage());
		assertEquals("ok: text is missing", page.getText5().getFeedbackMessage().getMessage());
		assertEquals("Default message: text6 required", page.getText6().getFeedbackMessage()
				.getMessage());
		assertEquals("input for text7-Label is missing", page.getText7().getFeedbackMessage()
				.getMessage());
		assertEquals("Default message: text8-Label required", page.getText8().getFeedbackMessage()
				.getMessage());
		assertEquals("found it in panel", page.getText9().getFeedbackMessage().getMessage());
		assertEquals("found it in form", page.getText10().getFeedbackMessage().getMessage());
		assertEquals("found it in page", page.getText11().getFeedbackMessage().getMessage());
		assertEquals("found it in page", page.getText12().getFeedbackMessage().getMessage());

		// Test caching
		assertEquals("Default message: text8-Label required", page.getText8().getFeedbackMessage()
				.getMessage());
	}

	/**
	 * 
	 */
	public void test2()
	{
		WicketTester tester = new MyTesterApplication();
		tester.getResourceSettings().setThrowExceptionOnMissingResource(false);
		tester.setupRequestAndResponse();
		tester.createRequestCycle();

		String str = tester.getResourceSettings().getLocalizer().getString("XXX", null);
		assertEquals("[Warning: String resource for 'XXX' not found]", str);
	}

	/**
	 * 
	 */
	public void test3()
	{
		WicketTester tester = new MyTesterApplication();
		tester.getResourceSettings().setThrowExceptionOnMissingResource(true);
		tester.setupRequestAndResponse();
		tester.createRequestCycle();

		boolean hit = false;
		try
		{
			tester.getResourceSettings().getLocalizer().getString("XXX", null);
		}
		catch (MissingResourceException ex)
		{
			hit = true;
		}
		assertEquals("MissingResourceException expected", hit, true);
	}
}
