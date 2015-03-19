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

import org.apache.wicket.mock.MockWebResponse;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test for {@link HeaderBufferingWebResponse}.
 * 
 * @author svenmeier
 */
public class HeaderBufferingWebResponseTest extends Assert
{

	/**
	 * WICKET-4927
	 */
	@Test
	public void additionalHeaderAfterWrittenContent()
	{
		MockWebResponse originalResponse = new MockWebResponse();

		HeaderBufferingWebResponse response = new HeaderBufferingWebResponse(originalResponse);

		response.addHeader("key1", "value1");

		assertNull(originalResponse.getHeader("key1"));

		response.write("written");

		assertEquals("value1", originalResponse.getHeader("key1"));

		response.addHeader("key2", "value2");

		assertEquals("value2", originalResponse.getHeader("key2"));
	}

	/**
	 */
	@Test
	public void resetAfterWrittenContent()
	{
		MockWebResponse originalResponse = new MockWebResponse();

		HeaderBufferingWebResponse response = new HeaderBufferingWebResponse(originalResponse);

		response.addHeader("key1", "value1");

		assertNull(originalResponse.getHeader("key1"));

		response.reset();

		response.write("written");

		try
		{
			response.reset();

			fail();
		}
		catch (IllegalStateException expected)
		{
		}
	}
}
