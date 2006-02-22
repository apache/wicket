/*
 * $Id$ $Revision:
 * 1.51 $ $Date$
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
package wicket.properties;

import java.util.MissingResourceException;

import junit.framework.TestCase;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.validation.RequiredValidator;
import wicket.protocol.http.WebRequestCycle;
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
		WebRequestCycle cycle = tester.createRequestCycle();
		
		TestPage page = new TestPage();
		Form form = (Form)page.get("form1");
		assertNotNull(form);
		
		RequiredValidator validator = RequiredValidator.getInstance();
		
		validator.error(page.getText1());
		validator.error(page.getText2());
		validator.error(page.getText3());
		validator.error(page.getText4());
		validator.error(page.getText5());
		validator.error(page.getText6());
		validator.error(page.getText7());
		validator.error(page.getText8());
		validator.error(page.getText9());
		validator.error(page.getText10());
		validator.error(page.getText11());
		validator.error(page.getText12());
		
		assertEquals("text1label is required", page.getText1().getFeedbackMessage().getMessage());
		assertEquals("text2 is required", page.getText2().getFeedbackMessage().getMessage());
		assertEquals("ok: text3333 is missing", page.getText3().getFeedbackMessage().getMessage());
		assertEquals("ok: Text4Label is missing", page.getText4().getFeedbackMessage().getMessage());
		assertEquals("ok: text is missing", page.getText5().getFeedbackMessage().getMessage());
		assertEquals("Default message: text6 required", page.getText6().getFeedbackMessage().getMessage());
		assertEquals("input for text7-Label is missing", page.getText7().getFeedbackMessage().getMessage());
		assertEquals("Default message: text8-Label required", page.getText8().getFeedbackMessage().getMessage());
		assertEquals("found it in panel", page.getText9().getFeedbackMessage().getMessage());
		assertEquals("found it in form", page.getText10().getFeedbackMessage().getMessage());
		assertEquals("found it in page", page.getText11().getFeedbackMessage().getMessage());
		assertEquals("found it in page", page.getText12().getFeedbackMessage().getMessage());
		
		// Test caching
		assertEquals("Default message: text8-Label required", page.getText8().getFeedbackMessage().getMessage());
	}
	
	/**
	 * 
	 */
	public void test2()
	{
		WicketTester tester = new MyTesterApplication();
		tester.getResourceSettings().setThrowExceptionOnMissingResource(false);
		tester.setupRequestAndResponse();
		WebRequestCycle cycle = tester.createRequestCycle();
		
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
		WebRequestCycle cycle = tester.createRequestCycle();
		
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
