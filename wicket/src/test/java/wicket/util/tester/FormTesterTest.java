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
	 * Construct.
	 */
	public FormTesterTest()
	{
		super("Form tester test");
	}

	/**
	 * Test that normal use of the formtester (no file uploads) works.
	 */
	public void testFormTester()
	{
		application.startPage(MockFormPage.class);
		MockFormPage page = (MockFormPage)application.getLastRenderedPage();
		MockDomainObject domainObject = page.getDomainObject();

		assertNotNull(domainObject);
		assertNull(domainObject.getText());
		assertNull(domainObject.getTextarea());
		assertFalse(domainObject.isCheckbox());

		FormTester formTester = application.newFormTester("form");
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
		application.startPage(MockFormFileUploadPage.class);
		MockFormFileUploadPage page = (MockFormFileUploadPage)application.getLastRenderedPage();
		MockDomainObjectFileUpload domainObject = page.getDomainObject();

		application.createRequestCycle();

		assertNull(page.getFileUpload());
		assertNotNull(domainObject);
		assertNull(domainObject.getText());


		FormTester formTester = application.newFormTester("form");
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
