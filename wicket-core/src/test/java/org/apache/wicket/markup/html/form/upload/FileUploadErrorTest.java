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
package org.apache.wicket.markup.html.form.upload;

import java.util.Locale;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;

/**
 * see WICKET-2015
 */
public class FileUploadErrorTest extends WicketTestCase
{
	private FormTester formTester;
	private final String textFieldId = "textField";
	private final String fileUploadId = "fileUpload";
	private final String testUploadFilePath = "src/test/java/org/apache/wicket/markup/html/form/upload/testfile.txt";

	/**
	 * 
	 */
	@Before
	public void before()
	{
		// Start and render the test page
		tester.startPage(FileUploadError.class);
		tester.assertRenderedPage(FileUploadError.class);
		// Set locale to fix error messages on this test.
		tester.getSession().setLocale(Locale.ENGLISH);
		//
		formTester = tester.newFormTester("form");
		tester.getRequest().setUseMultiPartContentType(true);
	}

	/**
	 * FileUpload is empty on submit: Validation fails to see that TextField is also required.
	 */
	public void testSubmit_NoInput()
	{
		formTester.submit();
		tester.assertErrorMessages("'textField' is required.");
	}

	/**
	 * FileUpload is filled on submit: TexttField is required.
	 */
	public void testSubmit_NoInput_FileUploaded()
	{
		formTester.setFile(fileUploadId, new File(testUploadFilePath), "UTF-8");
		formTester.submit();

		tester.assertErrorMessages("'textField' is required.");
	}

	/**
	 * FileUpload is empty on submit: Validation fails to report too short TextField input.
	 */
	public void testSubmit_NotValidTextFieldValue()
	{
		formTester.setValue(textFieldId, "te");
		formTester.submit();

		tester.assertErrorMessages(String.format(
			"The value of '%1$s' is not between 3 and 10 characters long.", textFieldId));
	}

	/**
	 * FileUpload is empty on submit: Validation fails to report too short TextField input.
	 */
	public void testSubmit_NotValidTextFieldValue2()
	{
		formTester.setValue(textFieldId, "12345678901");
		formTester.submit();

		tester.assertErrorMessages(String.format(
			"The value of '%1$s' is not between 3 and 10 characters long.", textFieldId));
	}

	/**
	 * FileUpload is filled on submit: Validation reports too short TextField input.
	 */
	public void testSubmit_NotValidTextFieldValue_FileUploaded()
	{
		formTester.setValue(textFieldId, "te");
		formTester.setFile(fileUploadId, new File(testUploadFilePath), "UTF-8");
		formTester.submit();

		tester.assertErrorMessages(String.format(
			"The value of '%1$s' is not between 3 and 10 characters long.", textFieldId));
	}

	/**
	 * Throwing exception confirms that value is received.
	 */
	public void testSubmit_ValidTextField_NoFile()
	{
		formTester.setValue(textFieldId, FileUploadError.THIS_VALUE_SHOULD_THROW_EXCEPTION);
		try
		{
			formTester.submit();
			fail("Value not succesfully submitted.");
		}
		catch (WicketRuntimeException rex)
		{
			Throwable ex = rex.getCause().getCause();
			assertEquals("Special value: " + FileUploadError.THIS_VALUE_SHOULD_THROW_EXCEPTION,
				ex.getMessage());
		}
	}

	/**
	 */
	public void testSubmit_ValidTextField_WithFile()
	{
		formTester.setValue(textFieldId, "test value");
		formTester.setFile(fileUploadId, new File(testUploadFilePath), "UTF-8");

		formTester.submit();
		tester.assertNoErrorMessage();
	}

	/**
	 */
	public void testSubmit_RequiredFileUpload_Ok()
	{
		((FileUploadField)tester.getLastRenderedPage().get("form:" + fileUploadId)).setRequired(true);

		formTester.setValue(textFieldId, "test value");
		formTester.setFile(fileUploadId, new File(testUploadFilePath), "UTF-8");

		formTester.submit();
		tester.assertNoErrorMessage();
	}

	/**
	 */
	public void testSubmit_RequiredFileUpload_ShouldFailWithValidationError()
	{
		((FileUploadField)tester.getLastRenderedPage().get("form:" + fileUploadId)).setRequired(true);

		formTester.setValue(textFieldId, "test value");

		formTester.submit();
		tester.assertErrorMessages("'fileUpload' is required.");
	}
}
