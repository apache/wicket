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
package org.apache.wicket.protocol.http;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.IRequestLogger.RequestData;
import org.junit.Test;

/**
 * Test for {@link AbstractRequestLogger}.
 */
public class AbstractRequestLoggerTest extends WicketTestCase
{

	private int counter;
	
	@Test
	public void foo()
	{
		tester.getApplication().getRequestLoggerSettings().setRequestsWindowSize(4);
		
		AbstractRequestLogger logger = new AbstractRequestLogger()
		{
			@Override
			protected void log(RequestData rd, SessionData sd)
			{
			}
		};
		
		logger.addRequest(requestData());
		assertEquals(1, logger.getRequests().size());
		assertEquals("0", logger.getRequests().get(0).getRequestedUrl());
		
		logger.addRequest(requestData());
		assertEquals(2, logger.getRequests().size());
		assertEquals("1", logger.getRequests().get(1).getRequestedUrl());

		logger.addRequest(requestData());
		assertEquals(3, logger.getRequests().size());
		assertEquals("2", logger.getRequests().get(2).getRequestedUrl());

		logger.addRequest(requestData());
		assertEquals(4, logger.getRequests().size());
		assertEquals("3", logger.getRequests().get(3).getRequestedUrl());
		
		logger.addRequest(requestData());
		assertEquals(4, logger.getRequests().size());
		assertEquals("4", logger.getRequests().get(3).getRequestedUrl());
		
		logger.addRequest(requestData());
		assertEquals(4, logger.getRequests().size());
		assertEquals("5", logger.getRequests().get(3).getRequestedUrl());
		
		logger.addRequest(requestData());
		assertEquals(4, logger.getRequests().size());
		assertEquals("6", logger.getRequests().get(3).getRequestedUrl());
		
		logger.addRequest(requestData());
		assertEquals(4, logger.getRequests().size());
		assertEquals("7", logger.getRequests().get(3).getRequestedUrl());
		
		logger.addRequest(requestData());
		assertEquals(4, logger.getRequests().size());
		assertEquals("8", logger.getRequests().get(3).getRequestedUrl());
}
	
	private RequestData requestData() {
		RequestData data = new RequestData();
		
		data.setRequestedUrl("" + counter++);

		return data;
	}
}
