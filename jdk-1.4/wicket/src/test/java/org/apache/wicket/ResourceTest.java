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

import org.apache.wicket.protocol.http.MockHttpServletResponse;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests resources.
 */
public class ResourceTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(ResourceTest.class);
	private static final String TEST_STRING = "Hello, World!";

	/**
	 * Tests a resource that should be marked as being cacheable.
	 */
	public void testCacheableResource()
	{
		String testFileLastModified;
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
		testFileLastModified = MockHttpServletResponse.formatDate(testFile.lastModified());
		tester.setupRequestAndResponse();
		WebRequestCycle cycle = tester.createRequestCycle();
		Resource resource = new Resource()
		{

			private static final long serialVersionUID = 1L;

			public IResourceStream getResourceStream()
			{
				return new FileResourceStream(new org.apache.wicket.util.file.File(testFile));
			}
		};
		resource.onResourceRequested();
		tester.processRequestCycle(cycle);

		log.debug(tester.getLastModifiedFromResponseHeader());
		assertEquals(testFileLastModified, tester.getLastModifiedFromResponseHeader());
		assertEquals(TEST_STRING.length(), tester.getContentLengthFromResponseHeader());
	}

	/**
	 * tests a resource that is not cacheable.
	 */
	public void testNonCacheableResource()
	{
		String testFileLastModified;
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
		testFileLastModified = MockHttpServletResponse.formatDate(testFile.lastModified());
		tester.setupRequestAndResponse();
		WebRequestCycle cycle = tester.createRequestCycle();
		Resource resource = new Resource()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public IResourceStream getResourceStream()
			{
				return new FileResourceStream(new org.apache.wicket.util.file.File(testFile));
			}
		};
		resource.setCacheable(false);
		resource.onResourceRequested();
		tester.processRequestCycle(cycle);

		assertNull(tester.getLastModifiedFromResponseHeader());
		assertEquals(TEST_STRING.length(), tester.getContentLengthFromResponseHeader());
	}
}
