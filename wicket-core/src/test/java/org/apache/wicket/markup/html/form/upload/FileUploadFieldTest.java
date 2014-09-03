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

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.junit.Test;


/**
 * Test of FileUploadField
 * 
 * @author Frank Bille (billen)
 */
public class FileUploadFieldTest extends WicketTestCase
{
	private static final String TEST_FILE_NAME = FileUploadFieldTest.class.getName();

	/**
	 * Test that detach closes the streams
	 * 
	 * @throws IOException
	 *             '
	 */
	@Test
	public void internalDetach() throws IOException
	{
		tester.startPage(MockPageWithFormAndUploadField.class);

		File tmp = null;
		try
		{
			// Write out a large text file. We need to make this file reasonably sizable,
			// because things get handled using input streams, and we want to check to make
			// sure they're closed properly if we abort mid-request.

			// We create a temp file because we don't want to depend on a file we might not
			// know the path of (e.g. the big DTD this test used previously). This enables
			// us to run the test out of a JAR file if need be, and also with an unknown
			// running directory (e.g. when run from wicket-parent).
			tmp = writeTestFile(1000);

			// Let's upload the dtd file. It's large enough to avoid being in memory.
			FormTester formtester = tester.newFormTester("form");
			formtester.setFile("upload", tmp, "text/plain");
			formtester.submit();

			// Get the file upload
			MockPageWithFormAndUploadField page = (MockPageWithFormAndUploadField)tester.getLastRenderedPage();
			FileUpload fileUpload = page.getFileUpload();

			assertNotNull(fileUpload);

			// Get an input stream from the file upload
			InputStream is = fileUpload.getInputStream();

			// We should be able to read a byte
			assertTrue(is.read() != -1);

			fileUpload.closeStreams();

			// The input stream should be closed so we shouldn't be able to read any more bytes
			try
			{
				is.read();
				fail("The input stream should be closed so we shouldn't be able to read any more bytes");
			}
			catch (IOException e)
			{
				// Expected
			}
			catch (Exception e)
			{
				fail();
			}
		}
		finally
		{
			if (tmp != null && tmp.exists())
			{
				tmp.delete();
			}
		}
	}
	
	/**
	 * @throws IOException
	 */
	@Test
	public void fileUploadCanBeValidated() throws IOException
	{
		tester.startPage(TestValidationPage.class);
		// creating the file expected by form validators
		File tmpFile = writeTestFile(1);
		tmpFile.deleteOnExit();
		FormTester formtester = tester.newFormTester("form");
		formtester.setFile("upload", tmpFile, "text/plain");
		formtester.submit();
		TestValidationPage page = (TestValidationPage)tester.getLastRenderedPage();
		assertFalse(page.getForm().hasError());
	}

	/** 
	 * https://issues.apache.org/jira/browse/WICKET-5691
	 * 
	 * */
	@Test
	public void testEmptyField() throws Exception
	{
		tester.startPage(TestValidationPage.class);
		
		FormTester formtester = tester.newFormTester("form");
		formtester.submit();
		
		FileUploadField fileUploadField = (FileUploadField)tester.getComponentFromLastRenderedPage("form:upload");
		
		assertEquals(0, fileUploadField.getFileUploads().size());
	}

	
	public static class TestValidationPage extends MockPageWithFormAndUploadField
	{
		/** */
		private static final long serialVersionUID = 1L;

		/** */
		public TestValidationPage()
		{
			fileUploadField.add(new TestValidator());
		}
	}

	private static class TestValidator implements IValidator<List<FileUpload>>
	{
		/** */
		private static final long serialVersionUID = 1L;

		@Override
		public void validate(IValidatable<List<FileUpload>> validatable)
		{
			List<FileUpload> fieldValue = validatable.getValue();
			
			if (fieldValue.size() == 0)
			{
				return;
			}
			
			if (fieldValue instanceof List == false)
			{
				validatable.error(new ValidationError().addKey("validatable value type not expected"));
			}
			FileUpload upload = fieldValue.get(0);
			if (!upload.getClientFileName().contains(TEST_FILE_NAME))
			{
				validatable.error(new ValidationError().addKey("uploaded file name not expected"));
			}
			File tmpFile = null;
			try
			{
				tmpFile = writeTestFile(1);
				if (!new String(read(tmpFile)).equals(new String(upload.getBytes())))
				{
					validatable.error(new ValidationError().addKey("uploaded content not expected"));
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				if (tmpFile != null && tmpFile.exists())
				{
					tmpFile.delete();
				}
			}
		}
	}

	/**
	 * @param numberOfowsToCreate
	 * @return test file
	 * @throws IOException
	 */
	public static File writeTestFile(int numberOfowsToCreate) throws IOException
	{
		File tmp = new File(java.io.File.createTempFile(TEST_FILE_NAME, ".txt"));
		OutputStream os = new BufferedOutputStream(new FileOutputStream(tmp));
		for (int i = 0; i < numberOfowsToCreate; i++)
		{
			os.write("test test test test test\n".getBytes());
		}
		os.close();
		return tmp;
	}

	private static byte[] read(File file)
	{
		try
		{
			return readFile(file);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static byte[] readFile(File file) throws IOException
	{
		InputStream stream = null;
		byte[] bytes = new byte[0];
		try
		{
			stream = new FileInputStream(file);
			int length = (int)file.length();
			bytes = new byte[length];
			int offset = 0;
			int bytesRead;

			while (offset < bytes.length &&
				(bytesRead = stream.read(bytes, offset, bytes.length - offset)) >= 0)
			{
				offset += bytesRead;
			}
		}
		finally
		{
			stream.close();
		}
		return bytes;
	}
}
