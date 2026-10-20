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
package org.apache.wicket.request.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.AbstractResource.ContentRangeType;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.response.ByteArrayResponse;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PartWriterCallback}
 */
class PartWriterCallbackTest extends WicketTestCase
{
	private static final byte[] CONTENT = "0123456789".getBytes(StandardCharsets.US_ASCII);

	@Test
	void unsatisfiableRangeClosesTheStream()
	{
		TrackingInputStream stream = new TrackingInputStream(Integer.MAX_VALUE);
		AbstractResource resource = new AbstractResource()
		{
			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes)
			{
				RequestCycle cycle = RequestCycle.get();
				ResourceResponse response = new ResourceResponse();
				response.setContentLength(CONTENT.length);
				response.setAcceptRange(ContentRangeType.BYTES);
				response.setWriteCallback(new PartWriterCallback(stream, (long)CONTENT.length,
					cycle.getMetaData(CONTENT_RANGE_STARTBYTE),
					cycle.getMetaData(CONTENT_RANGE_ENDBYTE)).setClose(true));
				return response;
			}
		};

		ServletWebRequest request = (ServletWebRequest)tester.getRequestCycle().getRequest();
		MockHttpServletRequest httpRequest = (MockHttpServletRequest)request.getContainerRequest();
		httpRequest.setHeader("range", "bytes=30-");
		MockHttpServletResponse httpResponse = new MockHttpServletResponse(httpRequest);
		resource.respond(new Attributes(request, new ServletWebResponse(request, httpResponse)));

		assertEquals(416, httpResponse.getStatus());
		assertEquals(0, httpResponse.getBinaryContent().length);
		assertTrue(stream.closed);
	}

	@Test
	void rangeStartingPastTheEndWritesNothingWhenSkipFallsShort() throws IOException
	{
		ByteArrayResponse response = new ByteArrayResponse();

		new PartWriterCallback(new TrackingInputStream(1), (long)CONTENT.length, 30L, -1L)
			.writeData(new Attributes(tester.getRequestCycle().getRequest(), response));

		assertEquals(0, response.getBytes().length);
	}

	private static class TrackingInputStream extends ByteArrayInputStream
	{
		private final int maxSkip;

		private boolean closed;

		private TrackingInputStream(int maxSkip)
		{
			super(CONTENT);
			this.maxSkip = maxSkip;
		}

		@Override
		public synchronized long skip(long n)
		{
			return super.skip(Math.min(n, maxSkip));
		}

		@Override
		public void close()
		{
			closed = true;
		}
	}
}
