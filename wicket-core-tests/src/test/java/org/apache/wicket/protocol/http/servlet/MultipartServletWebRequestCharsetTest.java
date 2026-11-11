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
package org.apache.wicket.protocol.http.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests that a multipart request with an unsupported charset does not fail the parsing of its
 * parts.
 */
class MultipartServletWebRequestCharsetTest extends WicketTestCase
{
	/**
	 * A charset name that no Charset answers to must fall back to the default, not fail the
	 * parsing with an exception.
	 */
	@Test
	void parseFilePartsWithAnUnsupportedCharset() throws Exception
	{
		MockHttpServletRequest request = tester.getRequest();
		request.setUseMultiPartContentType(true);
		request.getPostParameters().setParameterValue("field", "value");
		request.setCharacterEncoding("unknown-charset");

		MultipartServletWebRequestImpl multipart = new MultipartServletWebRequestImpl(request, "",
			Bytes.megabytes(1), "upload", DiskFileItemFactory.builder().get());

		multipart.parseFileParts();

		assertEquals("value",
			multipart.getRequestParameters().getParameterValue("field").toString());
	}
}
