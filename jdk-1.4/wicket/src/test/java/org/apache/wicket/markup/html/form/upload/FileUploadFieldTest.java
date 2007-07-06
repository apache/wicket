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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.MockHttpServletRequest;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.tester.ITestPageSource;


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
	 * 
	 * @throws Exception 
	 */
	public void testInternalDetach() throws Exception
	{
		final MockPageWithFormAndUploadField page = new MockPageWithFormAndUploadField();
		
		FileUploadField field = new FileUploadField("upload");
		page.getForm().add(field);

		tester.startPage(new ITestPageSource() 
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});

		// Setup the request. It should be a IMultipartWebRequest
		RequestCycle requestCycle = tester.createRequestCycle();
		MockHttpServletRequest servletRequest = tester.getServletRequest();
		servletRequest.setMethod("POST");
		servletRequest.setParameter("form2:hf:fs", "");
		servletRequest.setParameter("wicketState", "");
		
		File tmp = null;
		try {
			// Write out a large text file. We need to make this file reasonably sizable,
			// because things get handled using input streams, and we want to check to make
			// sure they're closed properly if we abort mid-request.
			
			// We create a temp file because we don't want to depend on a file we might not
			// know the path of (e.g. the big DTD this test used previously). This enables
			// us to run the test out of a JAR file if need be, and also with an unknown
			// running directory (e.g. when run from wicket-parent).
			tmp = new File(java.io.File.createTempFile(this.getClass().getName(), ".txt"));
			OutputStream os = new BufferedOutputStream(new FileOutputStream(tmp));
			for (int i = 0; i < 1000; i++)
			{
				os.write("test test test test test\n".getBytes());
			}
			os.close();
		
			// Let's upload the dtd file. It's large enough to avoid being in memory.
			servletRequest.addFile("upload", tmp, "text/plain");
	
			requestCycle.setRequest(new MultipartServletWebRequest(servletRequest, Bytes.MAX));

			// attach manually for the test
			field.attach();
			
			// Get the file upload
			FileUpload fileUpload = field.getFileUpload();
			
			assertNotNull(fileUpload);
			
			// Get an input stream from the file upload
			InputStream is = fileUpload.getInputStream();
			
			// We should be able to read a byte
			assertTrue(is.read() != -1);
			
			field.detach();
			
			// The input stream should be closed so we shouldn't be able to read any more bytes
			try 
			{
				is.read();
				fail();
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
}
