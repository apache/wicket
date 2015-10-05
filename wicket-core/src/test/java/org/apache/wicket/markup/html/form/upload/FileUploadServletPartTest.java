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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.Part;

import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;


/**
 * Test of FileUpload with simulated Servlet 3.0 multipart config
 */
public class FileUploadServletPartTest extends WicketTestCase
{
	/**
	 * Tests support for uploading files with Servlet 3.0 multipart upload
	 * https://issues.apache.org/jira/browse/WICKET-5924
	 *
	 * @throws Exception
	 */
	@Test
	public void uploadServlet30Multipart() throws Exception
	{
		MockPageWithFormAndUploadField page = tester.startPage(MockPageWithFormAndUploadField.class);

		MockHttpServletRequest httpServletRequest = tester.getRequest();
		FormTester formTester = tester.newFormTester(MockPageWithFormAndUploadField.FORM_ID);
		Servlet3Part part = new Servlet3Part(page.fileUploadField.getInputName());
		httpServletRequest.setPart(MockPageWithFormAndUploadField.FILE_UPLOAD_ID, part);
		formTester.submit();

		page = (MockPageWithFormAndUploadField) tester.getLastRenderedPage();
		FileUpload fileUpload = page.getFileUpload();
		assertArrayEquals(Servlet3Part.DATA, fileUpload.getBytes());
		assertEquals(Servlet3Part.class.getSimpleName(), fileUpload.getClientFileName());
		assertEquals(Servlet3Part.CONTENT_TYPE, fileUpload.getContentType());
		assertArrayEquals(Servlet3Part.DATA, IOUtils.toByteArray(fileUpload.getInputStream()));

		assertThat(part.written.get(), is(nullValue()));
		fileUpload.writeToTempFile();
		assertThat(part.written.get(), is(notNullValue()));

		assertThat(part.deleted.get(), is(false));
		fileUpload.delete();
		assertThat(part.deleted.get(), is(true));
	}

	private static class Servlet3Part implements Part
	{
		private static final byte[] DATA = new byte[] {1, 2, 3};
		public static final String CONTENT_TYPE = "text/plain";

		private final AtomicReference<String> written = new AtomicReference<>();
		private final AtomicBoolean deleted = new AtomicBoolean(false);
		private final LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
		public final String fieldName;

		private Servlet3Part(String fieldName) {
			this.fieldName = fieldName;
			headers.put(AbstractResource.CONTENT_DISPOSITION_HEADER_NAME, Arrays.asList("attachment;filename=" + Servlet3Part.class.getSimpleName()));
		}

		@Override
		public InputStream getInputStream() throws IOException
		{
			return new ByteArrayInputStream(DATA);
		}

		@Override
		public String getContentType()
		{
			return CONTENT_TYPE;
		}

		@Override
		public String getName()
		{
			return fieldName;
		}

		@Override
		public long getSize()
		{
			return DATA.length;
		}

		@Override
		public void write(String fileName) throws IOException
		{
			written.set(fileName);
		}

		@Override
		public void delete() throws IOException
		{
			deleted.set(true);
		}

		@Override
		public String getHeader(String name)
		{
			return headers.containsKey(name) ? headers.get(name).get(0) : null;
		}

		@Override
		public Collection<String> getHeaders(String name)
		{
			return headers.get(name);
		}

		@Override
		public Collection<String> getHeaderNames()
		{
			return headers.keySet();
		}
	}
}
