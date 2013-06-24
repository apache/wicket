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

import java.util.ArrayList;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.request.http.WebResponse;
import org.junit.Test;


/**
 * @author Pedro Santos
 */
public class BufferedWebResponseTest extends WicketTestCase
{
	enum TestAction {
		SET_HEADER, WRITE_RESPONSE
	}

	/**
	 * Asserting that set header actions are invoked before write in response actions.
	 * 
	 * WICKET-3618
	 */
	@Test
	public void testBufferedResponsePostponeWriteResponseAction()
	{
		final ArrayList<TestAction> actionsSequence = new ArrayList<TestAction>();
		WebResponse originalResponse = new MockWebResponse()
		{
			@Override
			public void setContentLength(long length)
			{
				actionsSequence.add(TestAction.SET_HEADER);
			}

			@Override
			public void write(CharSequence sequence)
			{
				actionsSequence.add(TestAction.WRITE_RESPONSE);
			}
		};
		BufferedWebResponse response = new BufferedWebResponse(originalResponse);
		response.setText("some text");
		response.setContentLength(9);
		response.writeTo(originalResponse);
		assertEquals(0, actionsSequence.indexOf(TestAction.SET_HEADER));
		assertEquals(1, actionsSequence.indexOf(TestAction.WRITE_RESPONSE));
	}
}