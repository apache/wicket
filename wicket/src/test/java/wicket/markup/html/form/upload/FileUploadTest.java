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
package wicket.markup.html.form.upload;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import wicket.WicketTestCase;
import wicket.util.upload.DiskFileItemFactory;
import wicket.util.upload.FileItem;

/**
 * Test of FileUpload
 * 
 * @author Frank Bille (billen)
 */
public class FileUploadTest extends WicketTestCase
{

	/**
	 * Construct.
	 */
	public FileUploadTest()
	{
		super("Test of FileUpload");
	}
	
	/**
	 * Test that when getting an input stream a new input stream is returned every time.
	 * 
	 * Also test that the inputstream is saved internally for later closing.
	 * 
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public void testGetInputStream() throws Exception
	{
		FileItem fileItem = new DiskFileItemFactory().createItem("dummyFieldName", "text/java", false, "FileUploadTest.java");
		// Initialize the upload
		fileItem.getOutputStream();
		
		// Get the internal list out
		Field inputStreamsField = FileUpload.class.getDeclaredField("inputStreams");
		inputStreamsField.setAccessible(true);
		
		FileUpload fileUpload = new FileUpload(fileItem);

		List<InputStream> inputStreams = (List<InputStream>) inputStreamsField.get(fileUpload);
		
		assertNull(inputStreams);

		InputStream is1 = fileUpload.getInputStream();
		inputStreams = (List<InputStream>) inputStreamsField.get(fileUpload);
		
		assertEquals(1, inputStreams.size());

		InputStream is2 = fileUpload.getInputStream();
		inputStreams = (List<InputStream>) inputStreamsField.get(fileUpload);
		
		assertEquals(2, inputStreams.size());
		
		assertNotSame(is1, is2);
		
		// Ok lets close all the streams
		try {
			fileUpload.closeStreams();
		} catch (Exception e) {
			fail();
		}
		
		inputStreams = (List<InputStream>) inputStreamsField.get(fileUpload);
		
		assertNull(inputStreams);
	}

}
