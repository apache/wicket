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
import static org.junit.Assert.assertNotNull;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.resource.DummyApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jeremy Thomerson
 */
public class RequestCycleListenerTest extends BaseRequestHandlerStackTest
{
	private IRequestHandler handler;

	private int errorCode;

	private int responses;

	private int detaches;

	private int exceptionsMapped;

	/** */
	@Before
	public void setUp()
	{
		DummyApplication application = new DummyApplication();
		application.setName("dummyTestApplication");
		ThreadContext.setApplication(application);
		application.setServletContext(new MockServletContext(application, "/"));
		application.initApplication();
		errorCode = 0;
	}

	/** */
	@After
	public void tearDown()
	{
		ThreadContext.getApplication().internalDestroy();
		ThreadContext.detach();
	}


	private RequestCycle newRequestCycle(final boolean throwExceptionInRespond)
	{
		final Response originalResponse = newResponse();
		Request request = new MockWebRequest(Url.parse("http://wicket.apache.org"));
		handler = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				if (throwExceptionInRespond)
				{
					throw new RuntimeException("testing purposes only");
				}
				responses++;
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detaches++;
			}
		};
		IRequestMapper requestMapper = new IRequestMapper()
		{
			@Override
			public IRequestHandler mapRequest(Request request)
			{
				return handler;
			}

			@Override
			public Url mapHandler(IRequestHandler requestHandler)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public int getCompatibilityScore(Request request)
			{
				throw new UnsupportedOperationException();
			}
		};
		IExceptionMapper exceptionMapper = new IExceptionMapper()
		{
			@Override
			public IRequestHandler map(Exception e)
			{
				exceptionsMapped++;
				return null;
			}
		};
		RequestCycleContext context = new RequestCycleContext(request, originalResponse,
			requestMapper, exceptionMapper);

		RequestCycle cycle = new RequestCycle(context);

		if (Application.exists())
		{
			cycle.getListeners().add(Application.get().getRequestCycleListeners());
		}

		return cycle;
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void basicOperations() throws Exception
	{
		IncrementingListener incrementingListener = new IncrementingListener();
		Application.get().getRequestCycleListeners().add(incrementingListener);

		RequestCycle cycle = newRequestCycle(false);

		incrementingListener.assertValues(0, 0, 0, 0);
		assertValues(0, 0, 0);
		/*
		 * begins, ends, responses and detaches should increment by one because we have one listener
		 * 
		 * exceptions should not increment because none are thrown
		 */
		cycle.processRequestAndDetach();

		incrementingListener.assertValues(1, 1, 0, 1);
		assertValues(0, 1, 1);

		// TEST WITH TWO LISTENERS
		cycle = newRequestCycle(false);
		cycle.getListeners().add(incrementingListener);
		/*
		 * we now have two listeners (app and cycle)
		 * 
		 * begins and ends should increment by two (once for each listener)
		 * 
		 * exceptions should not increment because none are thrown
		 * 
		 * responses and detaches should increment by one
		 */
		cycle.processRequestAndDetach();
		incrementingListener.assertValues(3, 3, 0, 3);
		assertValues(0, 2, 2);

		// TEST WITH TWO LISTENERS AND AN EXCEPTION DURING RESPONSE
		cycle = newRequestCycle(true);
		cycle.getListeners().add(incrementingListener);
		/*
		 * begins and ends should increment by two (once for each listener)
		 * 
		 * exceptions should increment by two (once for each listener)
		 * 
		 * exceptionsMapped should increment by one
		 * 
		 * responses should not increment because of the error
		 * 
		 * detaches should increment by one
		 */
		cycle.processRequestAndDetach();
		incrementingListener.assertValues(5, 5, 2, 5);
		assertValues(1, 2, 3);
	}

	/** */
	@Test
	public void exceptionIsHandledByRegisteredHandler()
	{
		IncrementingListener incrementingListener = new IncrementingListener();
		Application.get().getRequestCycleListeners().add(incrementingListener);
		Application.get().getRequestCycleListeners().add(new ErrorCodeListener(401));

		RequestCycle cycle = newRequestCycle(true);
		cycle.processRequestAndDetach();

		assertEquals(401, errorCode);
		assertEquals(1, incrementingListener.exceptionResolutions);
		assertEquals(0, incrementingListener.schedules);
	}

	/** */
	@Test
	public void exceptionIsHandledByFirstAvailableHandler()
	{
		// when two listeners return a handler
		Application.get().getRequestCycleListeners().add(new ErrorCodeListener(401));
		Application.get().getRequestCycleListeners().add(new ErrorCodeListener(402));

		RequestCycle cycle = newRequestCycle(true);
		cycle.processRequestAndDetach();

		// the first handler returned is used to handle the exception
		assertEquals(401, errorCode);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void exceptionHandingInOnDetach() throws Exception
	{
		// this test is a little flaky because it depends on the ordering of listeners which is not
		// guaranteed
		RequestCycle cycle = newRequestCycle(false);
		IncrementingListener incrementingListener = new IncrementingListener()
		{
			@Override
			public void onDetach(final RequestCycle cycle)
			{
				super.onDetach(cycle);
				throw new RuntimeException();
			}
		};
		cycle.getListeners().add(incrementingListener);
		cycle.getListeners().add(incrementingListener);
		cycle.getListeners().add(incrementingListener);
		cycle.processRequestAndDetach();

		incrementingListener.assertValues(3, 3, 0, 3);
		assertValues(0, 1, 1);
		assertEquals(3, incrementingListener.resolutions);
	}

	private void assertValues(int exceptionsMapped, int responses, int detaches)
	{
		assertEquals(exceptionsMapped, this.exceptionsMapped);
		assertEquals(responses, this.responses);
		assertEquals(detaches, this.detaches);
	}

	private class ErrorCodeListener extends AbstractRequestCycleListener
	{
		private final int code;

		public ErrorCodeListener(int code)
		{
			this.code = code;
		}

		@Override
		public IRequestHandler onException(final RequestCycle cycle, Exception ex)
		{
			return requestCycle -> errorCode = code;
		}
	}

	private class IncrementingListener implements IRequestCycleListener
	{

		private int begins, ends, exceptions, detachesnotified, resolutions, exceptionResolutions,
			schedules, executions = 0;

		@Override
		public IRequestHandler onException(final RequestCycle cycle, Exception ex)
		{
			exceptions++;
			return null;
		}

		@Override
		public void onEndRequest(final RequestCycle cycle)
		{
			ends++;
		}

		@Override
		public void onBeginRequest(final RequestCycle cycle)
		{
			assertNotNull(RequestCycle.get());
			begins++;
		}

		@Override
		public void onDetach(final RequestCycle cycle)
		{
			detachesnotified++;
		}

		@Override
		public void onRequestHandlerResolved(final RequestCycle cycle, IRequestHandler handler)
		{
			resolutions++;
		}

		@Override
		public void onExceptionRequestHandlerResolved(final RequestCycle cycle,
			IRequestHandler handler, Exception exception)
		{
			exceptionResolutions++;
		}

		@Override
		public void onRequestHandlerScheduled(final RequestCycle cycle, IRequestHandler handler)
		{
			schedules++;
		}

		private void assertValues(int begins, int ends, int exceptions, int detachesnotified)
		{
			assertEquals(begins, this.begins);
			assertEquals(ends, this.ends);
			assertEquals(exceptions, this.exceptions);
			assertEquals(detachesnotified, this.detachesnotified);
		}

		@Override
		public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler)
		{
			executions++;
		}

		@Override
		public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url)
		{
		}
	}
}
