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
package org.apache.wicket.resource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.TimeUnit;

import javax.script.Bindings;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests java script remote invocation
 * 
 * @author Tobias Soloschenko
 *
 */
public class NashornResourceReferenceTest extends WicketTestCase
{
	/**
	 * Tests remote invocation
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testRemoteInvocation() throws InterruptedException
	{
		WicketTester wicketTester = new WicketTester(new DummyApplication());
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(null, null, null)
		{
			@Override
			public ServletInputStream getInputStream() throws IOException
			{
				return new MockInputStream(NashornResourceReferenceTest.class
					.getResourceAsStream("NashornResourceReferenceTest.js"));
			}
		};
		wicketTester.setRequest(mockHttpServletRequest);
		NashornResourceReference nashornResourceReference = new NashornResourceReference("nashorn",
			10, 5, TimeUnit.SECONDS)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void setup(Attributes attributes, Bindings bindings)
			{
				bindings.put("serverValue", 1);
			}

			@Override
			protected Writer getWriter()
			{
				return new BufferedWriter(new OutputStreamWriter(System.out));
			}

			@Override
			protected Writer getErrorWriter()
			{
				return new BufferedWriter(new OutputStreamWriter(System.out));
			}

			@Override
			protected boolean isDebug()
			{
				return true;
			}
		};
		try
		{
			wicketTester.startResourceReference(nashornResourceReference);
		}
		catch (Exception e)
		{
			nashornResourceReference.getScheduledExecutorService().shutdownNow();
			nashornResourceReference.getScheduledExecutorService().awaitTermination(10000,
				TimeUnit.SECONDS);
		}
		Assert.assertEquals("4.0", wicketTester.getLastResponseAsString());
	}

	private class MockInputStream extends ServletInputStream
	{

		private InputStream inputStream;

		public MockInputStream(InputStream inputStream)
		{
			this.inputStream = inputStream;
		}

		@Override
		public int read() throws IOException
		{
			return inputStream.read();
		}

		@Override
		public void close() throws IOException
		{
			super.close();
			inputStream.close();
		}

		@Override
		public boolean isFinished()
		{
			return false;
		}

		@Override
		public boolean isReady()
		{
			return false;
		}

		@Override
		public void setReadListener(ReadListener arg0)
		{
		}
	}
}