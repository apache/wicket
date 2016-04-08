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
import org.apache.wicket.request.RequestHandlerStack.ReplaceHandlerException;
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


	private RequestCycle newRequestCycle(final RuntimeException exception)
	{
		final Response originalResponse = newResponse();
		Request request = new MockWebRequest(Url.parse("http://wicket.apache.org"));
		handler = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				if (exception != null)
				{
					throw exception;
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

		RequestCycle cycle = newRequestCycle((RuntimeException)null);

		incrementingListener.assertValues(0, 0, 0, 0, 0, 0);
		assertValues(0, 0, 0);
		cycle.processRequestAndDetach();
		// 0 exceptions mapped
		incrementingListener.assertValues(1, 1, 1, 1, 0, 1);
		// 0 exceptions mapped
		assertValues(0, 1, 1);

		// TEST WITH TWO LISTENERS
		cycle = newRequestCycle((RuntimeException)null);
		cycle.getListeners().add(incrementingListener);
		cycle.processRequestAndDetach();
		// 0 exceptions mapped, all other 2 due to two listeners
		incrementingListener.assertValues(2, 2, 2, 2, 0, 2);
		// 0 exceptions mapped
		assertValues(0, 1, 1);

		
		// TEST WITH TWO LISTENERS AND AN EXCEPTION DURING RESPONSE
		cycle = newRequestCycle(new RuntimeException("testing purposes only"));
		cycle.getListeners().add(incrementingListener);
		cycle.processRequestAndDetach();
		// 0 executed
		incrementingListener.assertValues(2, 2, 2, 0, 2, 2);
		// 1 exception mapped, 0 responded
		assertValues(1, 0, 1);
		
		
		// TEST A REPLACE EXCEPTION DURING RESPONSE
		IRequestHandler replacement = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				responses++;
			}

			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detaches++;
			}
		};
		cycle = newRequestCycle(new ReplaceHandlerException(replacement, true));
		cycle.scheduleRequestHandlerAfterCurrent(new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				throw new UnsupportedOperationException();
			}
			
			@Override
			public void detach(IRequestCycle requestCycle)
			{
				throw new UnsupportedOperationException();
			}
		});
		cycle.processRequestAndDetach();
		// 2 resolved, 1 executed, 0 exception mapped
		incrementingListener.assertValues(1, 1, 2, 1, 0, 1);
		// 0 exception mapped, 1 responded, 2 detached 
		assertValues(0, 1, 2);

		
		// TEST A REPLACE EXCEPTION DURING RESPONSE
		cycle = newRequestCycle(new ReplaceHandlerException(replacement, false));
		cycle.scheduleRequestHandlerAfterCurrent(new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				responses++;
			}
			
			@Override
			public void detach(IRequestCycle requestCycle)
			{
				detaches++;
			}
		});
		cycle.processRequestAndDetach();
		// 2 resolved, 2 executed, 0 exception mapped
		incrementingListener.assertValues(1, 1, 3, 2, 0, 1);
		// 0 exception mapped, 2 responded, 3 detached 
		assertValues(0, 2, 3);
	}

	/** */
	@Test
	public void exceptionIsHandledByRegisteredHandler()
	{
		IncrementingListener incrementingListener = new IncrementingListener();
		Application.get().getRequestCycleListeners().add(incrementingListener);
		Application.get().getRequestCycleListeners().add(new ErrorCodeListener(401));

		RequestCycle cycle = newRequestCycle(new RuntimeException("testing purposes only"));
		cycle.processRequestAndDetach();

		assertEquals(401, errorCode);
		assertEquals(2, incrementingListener.resolved);
		assertEquals(1, incrementingListener.executed);
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

		RequestCycle cycle = newRequestCycle(new RuntimeException("testing purposes only"));
		cycle.processRequestAndDetach();

		// the first handler returned is used to handle the exception
		assertEquals(401, errorCode);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void exceptionHandlingInOnDetach() throws Exception
	{
		// this test is a little flaky because it depends on the ordering of listeners which is not
		// guaranteed
		RequestCycle cycle = newRequestCycle((RuntimeException)null);
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

		incrementingListener.assertValues(3, 3, 3, 3, 0, 3);
		assertValues(0, 1, 1);
	}

	private void assertValues(int exceptionsMapped, int responses, int detaches)
	{
		assertEquals(exceptionsMapped, this.exceptionsMapped);
		assertEquals(responses, this.responses);
		assertEquals(detaches, this.detaches);
		
		this.exceptionsMapped = 0;
		this.responses = 0;
		this.detaches = 0;
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

		private int begins, ends, exceptions, detachesnotified, resolved, exceptionResolutions,
			schedules, executed = 0;

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
			resolved++;
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

		private void assertValues(int begins, int ends, int resolved, int executed, int exceptions, int detachesnotified)
		{
			assertEquals(begins, this.begins);
			assertEquals(ends, this.ends);
			assertEquals(resolved, this.resolved);
			assertEquals(executed, this.executed);
			assertEquals(exceptions, this.exceptions);
			assertEquals(detachesnotified, this.detachesnotified);
			
			this.begins = 0;
			this.ends = 0;
			this.resolved = 0;
			this.executed = 0;
			this.exceptions = 0;
			this.detachesnotified = 0;
		}

		@Override
		public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler)
		{
			executed++;
		}

		@Override
		public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url)
		{
		}
	}
}
