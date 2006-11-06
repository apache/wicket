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
package wicket.util.tester;

import wicket.WicketTestCase;
import wicket.markup.html.form.upload.FileUpload;
import wicket.util.file.File;
import wicket.util.tester.MockFormFileUploadPage.MockDomainObjectFileUpload;
import wicket.util.tester.MockFormPage.MockDomainObject;

/**
 * Test of FormTester.
 * 
 * @author frankbille
 */
public class FormTesterTest extends WicketTestCase
{
	/**
	 * Test that normal use of the formtester (no file uploads) works.
	 */
	public void testFormTester()
	{
		tester.startPage(MockFormPage.class);
		MockFormPage page = (MockFormPage)tester.getLastRenderedPage();
		MockDomainObject domainObject = page.getDomainObject();
		
		assertNotNull(domainObject);
		assertNull(domainObject.getText());
		assertNull(domainObject.getTextarea());
		assertFalse(domainObject.isCheckbox());
		
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("text", "Mock text value");
		formTester.setValue("textarea", "Mock textarea value");
		formTester.setValue("checkbox", "true");
		formTester.submit();

		assertNotNull(domainObject);
		assertNotNull(domainObject.getText());
		assertNotNull(domainObject.getTextarea());
		assertTrue(domainObject.isCheckbox());
	}
	
	/**
	 * Test that the user can use
	 * {@link FormTester#setFile(String, wicket.util.file.File, String)} to test
	 * that upload to a FileUploadField works.
	 */
	public void testAddFile()
	{
		tester.startPage(MockFormFileUploadPage.class);
		MockFormFileUploadPage page = (MockFormFileUploadPage)tester.getLastRenderedPage();
		MockDomainObjectFileUpload domainObject = page.getDomainObject();
		
		tester.createRequestCycle();
		
		assertNull(page.getFileUpload());
		assertNotNull(domainObject);
		assertNull(domainObject.getText());
		
		
		FormTester formTester = tester.newFormTester("form");
		formTester.setFile("file", new File("pom.xml"), "text/xml");
		formTester.setValue("text", "Mock value");
		formTester.submit();
		
		
		assertNotNull(domainObject);
		assertNotNull(domainObject.getText());
		assertEquals("Mock value", domainObject.getText());
		
		FileUpload fileUpload = page.getFileUpload();
		assertNotNull(fileUpload);
		
		assertEquals("pom.xml", fileUpload.getClientFileName());
		assertEquals("text/xml", fileUpload.getContentType());
	}
}
