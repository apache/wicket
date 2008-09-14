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
package org.apache._wicket.request;

import junit.framework.TestCase;

import org.apache._wicket.RequestCycle;
import org.apache._wicket.request.response.Response;

/**
 * 
 * @author Matej Knopp
 */
public class RequestHandlerStackTest extends TestCase
{

	/**
	 * Construct.
	 */
	public RequestHandlerStackTest()
	{
	}

	private Response newResponse()
	{
		return new Response()
		{
			@Override
			public void write(byte[] array)
			{
			}
			@Override
			public void write(CharSequence sequence)
			{
			}
		};
	}
	
	private RequestHandlerStack newStack(Response response)
	{
		return new RequestHandlerStack(response)
		{
			@Override
			protected RequestCycle getRequestCycle()
			{
				return null;
			}			
		};
	}
		
	private boolean testFlag1;
	private boolean testFlag2;
	private boolean testFlag3;
	private boolean testFlag4;
	
	private boolean detachedFlag1;
	private boolean detachedFlag2;
	private boolean detachedFlag3;
	private boolean detachedFlag4;
	
	private void initFlags()
	{
		testFlag1 = true;
		testFlag2 = true;
		testFlag3 = true;
		testFlag4 = true;
		
		detachedFlag1 = false;
		detachedFlag2 = false;
		detachedFlag3 = false;
		detachedFlag4 = false;
	}
	
	/**
	 * 
	 */
	public void test1()
	{
		initFlags();
		
		final Response originalResponse = newResponse();
		
		final RequestHandlerStack stack = newStack(originalResponse);
		
		final RequestHandler handler3 = new RequestHandler()
		{
			public void respond(RequestCycle requestCycle)
			{
				testFlag3 = false;
			}
			public void detach(RequestCycle requestCycle)
			{
				detachedFlag3 = true;
			}			
		};
		
		final RequestHandler handler2 = new RequestHandler()
		{
			public void respond(RequestCycle requestCycle)
			{
				testFlag2 = false;
			
				stack.replaceCurrentRequestHandler(handler3);
				
				// this code must not be executed
				testFlag2 = true;
			}
			public void detach(RequestCycle requestCycle)
			{
				detachedFlag2 = true;
			}
		};
		
		final RequestHandler handler1 = new RequestHandler()
		{
			public void respond(RequestCycle requestCycle)
			{
				testFlag1 = false;
				
				Response resp = newResponse();
				stack.setResponse(resp);
				stack.executeRequestHandler(handler2);
				assertEquals(stack.getResponse(), resp);

				// this code must be executed
				testFlag1 = true;
			}
			public void detach(RequestCycle requestCycle)
			{
				detachedFlag1 = true;
			}
		};
		
		stack.executeRequestHandler(handler1);
		
		assertEquals(stack.getResponse(), originalResponse);
		
		stack.detach();
		
		assertTrue(testFlag1);
		assertFalse(testFlag2);
		assertFalse(testFlag3);
		
		assertTrue(detachedFlag1);
		assertTrue(detachedFlag2);
		assertTrue(detachedFlag3);
	}
	
	/**
	 * 
	 */
	public void test2()
	{
		initFlags();
		
		final Response originalResponse = newResponse();
		final RequestHandlerStack stack = newStack(originalResponse);
		
		final RequestHandler handler4 = new RequestHandler()
		{
			public void respond(RequestCycle requestCycle)
			{
				testFlag4 = false;
				
				assertEquals(stack.getResponse(), originalResponse);
				
				stack.setResponse(newResponse());				
			}
			public void detach(RequestCycle requestCycle)
			{
				detachedFlag4 = true;
			}
		};
		
		final RequestHandler handler3 = new RequestHandler()
		{
			public void respond(RequestCycle requestCycle)
			{
				testFlag3 = false;
				stack.setResponse(newResponse());
				stack.replaceAllRequestHandlers(handler4);
				// code must not be reached
				testFlag3 = true;
			}
			public void detach(RequestCycle requestCycle)
			{
				detachedFlag3 = true;
			}
		};
		
		final RequestHandler handler2 = new RequestHandler()
		{
			public void respond(RequestCycle requestCycle)
			{
				testFlag2 = false;
				stack.setResponse(newResponse());
				stack.executeRequestHandler(handler3);
				// code must not be reached
				testFlag2 = true;
			}
			public void detach(RequestCycle requestCycle)
			{
				detachedFlag2 = true;
			}
		};
		
		RequestHandler handler1 = new RequestHandler()
		{
			public void respond(RequestCycle requestCycle)
			{
				testFlag1 = false;
				stack.setResponse(newResponse());
				stack.executeRequestHandler(handler2);

				// code must not be reached
				testFlag1 = true;
			}
			public void detach(RequestCycle requestCycle)
			{
				detachedFlag1 = true;
			}			
		};
		
		stack.executeRequestHandler(handler1);
		
		assertEquals(stack.getResponse(), originalResponse);
		
		stack.detach();
		
		assertFalse(testFlag1);
		assertFalse(testFlag2);
		assertFalse(testFlag3);
		assertFalse(testFlag4);
		
		assertTrue(detachedFlag1);
		assertTrue(detachedFlag2);
		assertTrue(detachedFlag3);
		assertTrue(detachedFlag4);
	}
}
