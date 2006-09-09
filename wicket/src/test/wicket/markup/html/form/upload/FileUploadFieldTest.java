/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.markup.html.form.upload;

import java.io.IOException;
import java.io.InputStream;

import wicket.Page;
import wicket.RequestCycle;
import wicket.WicketTestCase;
import wicket.protocol.http.MockHttpServletRequest;
import wicket.protocol.http.servlet.MultipartServletWebRequest;
import wicket.util.file.File;
import wicket.util.lang.Bytes;
import wicket.util.tester.ITestPageSource;

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
		
		FileUploadField field = new FileUploadField(page.getForm(), "upload");

		application.startPage(new ITestPageSource() 
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});

		// Setup the request. It should be a IMultipartWebRequest
		RequestCycle requestCycle = application.createRequestCycle();
		MockHttpServletRequest servletRequest = application.getServletRequest();
		servletRequest.setMethod("POST");
		servletRequest.setParameter("form2:hf:fs", "");
		servletRequest.setParameter("wicketState", "");
		
		// Let's upload the pom file. It's large enough to avoid being in memory.
		servletRequest.addFile("upload", new File("pom.xml"), "text/xml");

		requestCycle.setRequest(new MultipartServletWebRequest(servletRequest, Bytes.MAX));
		
		// Get the file upload
		FileUpload fileUpload = field.getFileUpload();
		
		assertNotNull(fileUpload);
		
		// Get an input stream from the file upload
		InputStream is = fileUpload.getInputStream();
		
		// We should be able to read a byte
		assertTrue(is.read() != -1);
		
		field.internalOnDetach();
		
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
}
