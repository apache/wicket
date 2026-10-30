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
package org.apache.wicket.protocol.ws.tester;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

/**
* Tests that a handshake with a pageId that is not a number fails with an exception that
* names the parameter.
*/
public class WebSocketProcessorPageIdTest
{
	WicketTester tester;
	WebApplication application = new MockApplication();

	private static class TestProcessor extends TestWebSocketProcessor
	{
		private TestProcessor(HttpServletRequest request, WebApplication application)
		{
			super(request, application);
		}

		@Override
		protected void onOutMessage(String message)
		{
		}

		@Override
		protected void onOutMessage(byte[] message, int offset, int length)
		{
		}
	}

	@BeforeEach
	public void before()
	{
		tester = new WicketTester(application);
		application.getWicketFilter().setFilterPath("");
	}

	@AfterEach
	public void after()
	{
		tester.destroy();
	}

	@Test
	public void malformedPageIdFailsWithAnExceptionThatNamesTheParameter()
	{
		MockHttpServletRequest request = tester.getRequest();
		request.addParameter("pageId", "NaN");
		request.addParameter(WebRequest.PARAM_AJAX_BASE_URL, ".");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
			() -> new TestProcessor(request, tester.getApplication()));
		assertEquals("Request parameter 'pageId' must be a number", e.getMessage());
	}
}
