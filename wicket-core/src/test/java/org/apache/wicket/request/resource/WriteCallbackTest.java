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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.AbstractResource.WriteCallback;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.response.ByteArrayResponse;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Kent Tong
 */
public class WriteCallbackTest extends Assert
{

	/**
	 */
	@Test
	public void writeStream() throws IOException
	{
		WriteCallback callback = new WriteCallback()
		{

			@Override
			public void writeData(Attributes attributes)
			{

			}
		};
		ByteArrayResponse response = new ByteArrayResponse();
		Attributes attributes = new Attributes(new MockWebRequest(new Url()), response);
		byte[] srcData = new byte[5000];
		for (int i = 0; i < srcData.length; i++)
		{
			srcData[i] = (byte)i;
		}
		InputStream in = new ByteArrayInputStream(srcData);
		callback.writeStream(attributes, in);
		assertTrue("Content not equal", Arrays.equals(response.getBytes(), srcData));
	}

}
