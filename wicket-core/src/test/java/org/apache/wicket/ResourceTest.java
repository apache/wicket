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
package org.apache.wicket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;


/**
 * Tests resources.
 */
public class ResourceTest extends WicketTestCase
{
	private static final String TEST_STRING = "Hello, World!";

	/**
	 * tests a resource that is not cacheable.
	 */
	@Test
	public void testFileResourceStream()
	{
		final File testFile;
		try
		{
			testFile = File.createTempFile(ResourceTest.class.getName(), null);
			OutputStream out = new FileOutputStream(testFile);
			out.write(TEST_STRING.getBytes());
			out.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		bindToApplicationAsResourceAndRequestIt(new FileResourceStream(
			new org.apache.wicket.util.file.File(testFile)));
		assertEquals(MockHttpServletResponse.formatDate(testFile.lastModified()),
			tester.getLastModifiedFromResponseHeader());
		assertEquals(TEST_STRING.length(), tester.getContentLengthFromResponseHeader());
	}

	/**
	 * testStringResourceStream()
	 */
	@Test
	public void stringResourceStream()
	{
		StringResourceStream resourceStream = new StringResourceStream(TEST_STRING);
		bindToApplicationAsResourceAndRequestIt(resourceStream);
		assertEquals(TEST_STRING.length(), tester.getContentLengthFromResponseHeader());
	}

	private void bindToApplicationAsResourceAndRequestIt(IResourceStream iResourceStream)
	{
		IResource resource = new ResourceStreamResource(iResourceStream);
		tester.getApplication().getSharedResources().add("resource", resource);
		tester.getRequest().setUrl(
			tester.getRequestCycle().mapUrlFor(
				tester.getApplication()
					.getSharedResources()
					.get(Application.class, "resource", null, null, null, true), null));
		tester.processRequest();
	}
}
