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

import java.net.SocketException;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.http.servlet.ResponseIOException;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.EmptyRequestHandler;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Pedro Santos
 */
public class ResponseIOExceptionTest extends Assert
{
	private WicketTester tester;

	/**
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		tester = new WicketTester()
		{
			@Override
			protected Response newServletWebResponse(ServletWebRequest servletWebRequest)
			{
				return new ProblematicResponse(servletWebRequest, getResponse());
			}
		};
		tester.setExposeExceptions(false);
	}

	/**
	 * @throws Exception
	 */
	@After
	public void after() throws Exception
	{
		tester.destroy();
	}

	/**
	 * WICKET-3570
	 */
	@Test
	public void giveUpRespondingOnIOExceptions()
	{
		TestRequestCycleListener testRequestCycleListener = new TestRequestCycleListener();
		tester.getApplication().getRequestCycleListeners().add(testRequestCycleListener);
		tester.startResource(new ResourceStreamResource(new StringResourceStream("asdf")));
		assertTrue(testRequestCycleListener.lastExceptionRquestHandlerResolved instanceof EmptyRequestHandler);
	}

	class TestRequestCycleListener extends AbstractRequestCycleListener
	{
		IRequestHandler lastExceptionRquestHandlerResolved;

		@Override
		public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler,
			Exception exception)
		{
			lastExceptionRquestHandlerResolved = handler;
		}

	}
	/**
	 * Mock response simulating connection lost problems.
	 */
	public static class ProblematicResponse extends ServletWebResponse
	{

		/**
		 * @param webRequest
		 * @param httpServletResponse
		 */
		public ProblematicResponse(ServletWebRequest webRequest,
			HttpServletResponse httpServletResponse)
		{
			super(webRequest, httpServletResponse);
		}

		@Override
		public void flush()
		{
			throw new ResponseIOException(new SocketException(
				"Connection reset by peer: socket write error"));
		}

		@Override
		public void write(byte[] array)
		{
			throw new ResponseIOException(new SocketException(
				"Connection reset by peer: socket write error"));
		}

		@Override
		public void write(byte[] array, int offset, int length)
		{
			throw new ResponseIOException(new SocketException(
				"Connection reset by peer: socket write error"));
		}

		@Override
		public void write(CharSequence sequence)
		{
			throw new ResponseIOException(new SocketException(
				"Connection reset by peer: socket write error"));
		}
	}
}
