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
package org.apache.wicket.util.tester;

import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.MockFormFileUploadPage.MockDomainObjectFileUpload;
import org.apache.wicket.util.tester.MockFormPage.MockDomainObject;


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
	 * {@link FormTester#setFile(String, org.apache.wicket.util.file.File, String)} to test
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
	
		assertTrue("setFile failed, no upload content detected.", fileUpload.getBytes().length > 0);
		assertEquals("pom.xml", fileUpload.getClientFileName());
		assertEquals("text/xml", fileUpload.getContentType());
	}

	/**
	 * Test that the user can use
	 * {@link FormTester#setFile(String, org.apache.wicket.util.file.File, String)} to test
	 * that upload to a FileUploadField works.
	 */
	public void testAddBinaryFile()
	{
		tester.startPage(MockFormFileUploadPage.class);
		MockFormFileUploadPage page = (MockFormFileUploadPage)tester.getLastRenderedPage();
		MockDomainObjectFileUpload domainObject = page.getDomainObject();

		tester.createRequestCycle();

		assertNull(page.getFileUpload());
		assertNotNull(domainObject);
		assertNull(domainObject.getText());


		FormTester formTester = tester.newFormTester("form");
		formTester.setFile("file", new File("src/test/java/org/apache/wicket/util/tester/bg.jpg"), "image/jpeg");
		formTester.setValue("text", "Mock value");
		formTester.submit();


		assertNotNull(domainObject);
		assertNotNull(domainObject.getText());
		assertEquals("Mock value", domainObject.getText());

		FileUpload fileUpload = page.getFileUpload();
		assertNotNull(fileUpload);

		assertTrue("uploaded content does not have the right size, expected 428, got " + fileUpload.getBytes().length, fileUpload.getBytes().length == 428);
		assertEquals("bg.jpg", fileUpload.getClientFileName());
		assertEquals("image/jpeg", fileUpload.getContentType());
	}

	/**
	 * Test that formTester deal with Multipart form correctly when no actual
	 * upload
	 */
	public void testSubmitWithoutUploadFile()
	{
		tester.startPage(MockFormFileUploadPage.class);
		MockFormFileUploadPage page = (MockFormFileUploadPage)tester.getLastRenderedPage();

		Session.get().setLocale(Locale.US);

		FormTester formTester = tester.newFormTester("form");
		// without file upload
		formTester.submit();
		assertNull(page.getFileUpload());

		tester.assertErrorMessages(new String[] { "Field 'file' is required." });
	}

}
