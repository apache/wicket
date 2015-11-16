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
package org.apache.wicket.request.cycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.junit.Test;

/**
 * @author Matej Knopp
 */
public class RequestHandlerStackTest extends BaseRequestHandlerStackTest
{
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

	/** */
	@Test
	public void test1()
	{
		initFlags();

		final Response originalResponse = newResponse();
		final IRequestCycle requestCycle = newRequestCycle(originalResponse);
		final RequestHandlerStack stack = newStack(requestCycle);

		final IRequestHandler handler3 = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				testFlag3 = false;
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detachedFlag3 = true;
			}
		};

		final IRequestHandler handler2 = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				testFlag2 = false;

				stack.replaceAll(handler3);

				// this code must not be executed
				testFlag2 = true;
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detachedFlag2 = true;
			}
		};

		final IRequestHandler handler1 = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				testFlag1 = false;

				Response resp = newResponse();
				requestCycle.setResponse(resp);
				stack.execute(handler2);
				assertEquals(requestCycle.getResponse(), resp);

				// this code must be executed
				testFlag1 = true;
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detachedFlag1 = true;
			}
		};

		stack.execute(handler1);

		assertEquals(requestCycle.getResponse(), originalResponse);

		stack.detach();

		assertFalse(testFlag1);
		assertFalse(testFlag2);
		assertFalse(testFlag3);

		assertTrue(detachedFlag1);
		assertTrue(detachedFlag2);
		assertTrue(detachedFlag3);
	}

	/** */
	@Test
	public void test2()
	{
		initFlags();

		final Response originalResponse = newResponse();
		final IRequestCycle requestCycle = newRequestCycle(originalResponse);
		final RequestHandlerStack stack = newStack(requestCycle);


		final IRequestHandler handler4 = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				testFlag4 = false;

				assertEquals(requestCycle.getResponse(), originalResponse);

				requestCycle.setResponse(newResponse());
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detachedFlag4 = true;
			}
		};

		final IRequestHandler handler3 = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				testFlag3 = false;
				requestCycle.setResponse(newResponse());
				stack.replaceAll(handler4);
				// code must not be reached
				testFlag3 = true;
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detachedFlag3 = true;
			}
		};

		final IRequestHandler handler2 = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				testFlag2 = false;
				requestCycle.setResponse(newResponse());
				stack.execute(handler3);
				// code must not be reached
				testFlag2 = true;
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detachedFlag2 = true;
			}
		};

		IRequestHandler handler1 = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				testFlag1 = false;
				requestCycle.setResponse(newResponse());
				stack.execute(handler2);

				// code must not be reached
				testFlag1 = true;
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detachedFlag1 = true;
			}
		};

		stack.execute(handler1);

		assertEquals(requestCycle.getResponse(), originalResponse);

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

	/** */
	@Test
	public void test3()
	{
		initFlags();

		final Response originalResponse = newResponse();
		final IRequestCycle requestCycle = newRequestCycle(originalResponse);
		final RequestHandlerStack stack = newStack(requestCycle);

		final IRequestHandler handler4 = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				testFlag4 = true;

				requestCycle.setResponse(newResponse());
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detachedFlag4 = true;
			}
		};

		final IRequestHandler handler3 = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				testFlag3 = false;
				stack.schedule(handler4);

				// make sure that handler4's respond method is fired after this
				// one ends
				testFlag4 = false;

				// code must be be reached
				testFlag3 = true;
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detachedFlag3 = true;
			}
		};

		final IRequestHandler handler2 = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				testFlag2 = false;
				stack.execute(handler3);
				// code must be reached
				testFlag2 = true;
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detachedFlag2 = true;
			}
		};

		IRequestHandler handler1 = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				testFlag1 = false;
				stack.execute(handler2);

				// code must be reached
				testFlag1 = true;
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detachedFlag1 = true;
			}
		};

		stack.execute(handler1);

		assertEquals(requestCycle.getResponse(), originalResponse);

		stack.detach();

		assertTrue(testFlag1);
		assertTrue(testFlag2);
		assertTrue(testFlag3);
		assertTrue(testFlag4);

		assertTrue(detachedFlag1);
		assertTrue(detachedFlag2);
		assertTrue(detachedFlag3);
		assertTrue(detachedFlag4);
	}
}
