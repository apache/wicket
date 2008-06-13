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
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.ITestPageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;


/**
 * Test of FileUploadField
 * 
 * @author Frank Bille (billen)
 */
public class FileUploadFieldTest extends WicketTestCase
{

	/**
	 * Construct.
	 */
	public FileUploadFieldTest()
	{
		super("Test of FileUploadField");
	}

	/**
	 * Test that detach closes the streams
	 */
	public void testInternalDetach() throws Exception
	{
		final MockPageWithFormAndUploadField page = new MockPageWithFormAndUploadField();

		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page<?> getTestPage()
			{
				return page;
			}
		});

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

	public void testFileUploadCanBeValidated() throws IOException
	{
		final Set<IValidatable> validatedComponents = new HashSet<IValidatable>();

		final File tmpFile = writeTestFile(1);
		tmpFile.deleteOnExit();

		final IValidator testValidator = new IValidator()
		{
			private static final long serialVersionUID = 1L;

			public void validate(IValidatable validatable)
			{
				validatedComponents.add(validatable);
				assertEquals(FileUpload.class, validatable.getValue().getClass());
				FileUpload upload = (FileUpload)validatable.getValue();
				assertEquals(tmpFile.getName(), upload.getClientFileName());
				assertEquals(new String(read(tmpFile)), new String(upload.getBytes()));
			}
		};
		final MockPageWithFormAndUploadField page = new MockPageWithFormAndUploadField();
		page.getForm().visitChildren(FileUploadField.class, new IVisitor<FileUploadField>()
		{
			public Object component(FileUploadField uploadField)
			{
				uploadField.add(testValidator);
				return STOP_TRAVERSAL;
			}
		});

		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page<?> getTestPage()
			{
				return page;
			}
		});

		FormTester formtester = tester.newFormTester("form");
		formtester.setFile("upload", tmpFile, "text/plain");
		formtester.submit();
		assertEquals(validatedComponents.size(), 1);
	}

	private File writeTestFile(int numberOfowsToCreate) throws IOException
	{
		File tmp = new File(java.io.File.createTempFile(getClass().getName(), ".txt"));
		OutputStream os = new BufferedOutputStream(new FileOutputStream(tmp));
		for (int i = 0; i < numberOfowsToCreate; i++)
		{
			os.write("test test test test test\n".getBytes());
		}
		os.close();
		return tmp;
	}

	private byte[] read(File file)
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

	private byte[] readFile(File file) throws IOException
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
