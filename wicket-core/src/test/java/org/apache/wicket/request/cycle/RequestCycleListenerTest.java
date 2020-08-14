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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.coep.CrossOriginEmbedderPolicyConfiguration.CoepMode;
import org.apache.wicket.coop.CrossOriginOpenerPolicyConfiguration.CoopMode;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.RequestHandlerExecutor.ReplaceHandlerException;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.EmptyRequestHandler;
import org.apache.wicket.resource.DummyApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Jeremy Thomerson
 */
class RequestCycleListenerTest extends RequestHandlerExecutorTest
{
	private IRequestHandler handler;

	private int errorCode;

	private int responses;

	private int detaches;

	private int exceptionsMapped;

	/** */
	@BeforeEach
	void setUp()
	{
		DummyApplication application = new DummyApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				// disabling COOP and COEP for these tests because MockWebRequest
				// used in these tests can't be cast into HttpServletRequest by the
				// COOP and COEP listeners, which get added in Application#initApplication()
				getSecuritySettings().setCrossOriginOpenerPolicyConfiguration(CoopMode.DISABLED);
				getSecuritySettings().setCrossOriginEmbedderPolicyConfiguration(CoepMode.DISABLED);
			}
		};
		application.setName("dummyTestApplication");
		ThreadContext.setApplication(application);
		application.setServletContext(new MockServletContext(application, "/"));
		application.initApplication();
		errorCode = 0;
	}

	/** */
	@AfterEach
	void tearDown()
	{
		ThreadContext.getApplication().internalDestroy();
		ThreadContext.detach();
	}

	private RequestCycle newRequestCycle(final Consumer<IRequestCycle> cycleConsumer)
	{
		final Response originalResponse = newResponse();
		Request request = new MockWebRequest(Url.parse("http://wicket.apache.org"));
		handler = new IRequestHandler()
		{
			@Override
			public void respond(IRequestCycle requestCycle)
			{
				cycleConsumer.accept(requestCycle);

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
	void basicOperations() throws Exception
	{
		IncrementingListener incrementingListener = new IncrementingListener();
		Application.get().getRequestCycleListeners().add(incrementingListener);

		RequestCycle cycle = newRequestCycle(rc -> {});

		incrementingListener.assertValues(0, 0, 0, 0, 0, 0);
		assertValues(0, 0, 0);
		assertTrue(cycle.processRequestAndDetach());
		// 0 exceptions mapped
		incrementingListener.assertValues(1, 1, 1, 1, 0, 1);
		// 0 exceptions mapped
		assertValues(0, 1, 1);

		// TEST WITH TWO LISTENERS
		cycle = newRequestCycle(rc -> {});
		cycle.getListeners().add(incrementingListener);
		assertTrue(cycle.processRequestAndDetach());
		// 0 exceptions mapped, all other 2 due to two listeners
		incrementingListener.assertValues(2, 2, 2, 2, 0, 2);
		// 0 exceptions mapped
		assertValues(0, 1, 1);

		
		// TEST WITH TWO LISTENERS AND AN EXCEPTION DURING RESPONSE
		cycle = newRequestCycle(rc -> { throw new RuntimeException("testing purposes only"); });
		cycle.getListeners().add(incrementingListener);
		assertFalse(cycle.processRequestAndDetach());
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
		cycle = newRequestCycle(rc -> { throw new ReplaceHandlerException(replacement, true); });
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
		assertTrue(cycle.processRequestAndDetach());
		// 2 resolved, 1 executed, 0 exception mapped
		incrementingListener.assertValues(1, 1, 2, 1, 0, 1);
		// 0 exception mapped, 1 responded, 2 detached
		assertValues(0, 1, 2);

		
		// TEST A REPLACE EXCEPTION DURING RESPONSE
		cycle = newRequestCycle(rc -> { throw new ReplaceHandlerException(replacement, false); });
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
		assertTrue(cycle.processRequestAndDetach());
		// 2 resolved, 2 executed, 0 exception mapped
		incrementingListener.assertValues(1, 1, 3, 2, 0, 1);
		// 0 exception mapped, 2 responded, 3 detached
		assertValues(0, 2, 3);
	}

	/** */
	@Test
	void exceptionIsHandledByRegisteredHandler()
	{
		IncrementingListener incrementingListener = new IncrementingListener();
		Application.get().getRequestCycleListeners().add(incrementingListener);
		Application.get().getRequestCycleListeners().add(new ErrorCodeListener(401));

		RequestCycle cycle = newRequestCycle(rc -> { throw new RuntimeException("testing purposes only"); });
		assertTrue(cycle.processRequestAndDetach());

		assertEquals(401, errorCode);
		assertEquals(2, incrementingListener.resolved);
		assertEquals(1, incrementingListener.executed);
		assertEquals(1, incrementingListener.exceptionResolutions);
		assertEquals(0, incrementingListener.schedules);
	}

	/** */
	@Test
	void exceptionIsHandledByFirstAvailableHandler()
	{
		// when two listeners return a handler
		Application.get().getRequestCycleListeners().add(new ErrorCodeListener(401));
		Application.get().getRequestCycleListeners().add(new ErrorCodeListener(402));

		RequestCycle cycle = newRequestCycle(rc -> { throw new RuntimeException("testing purposes only"); });
		assertTrue(cycle.processRequestAndDetach());

		// the first handler returned is used to handle the exception
		assertEquals(401, errorCode);
	}

	/** */
	@Test
	void exceptionExceeded()
	{
		Application.get().getRequestCycleListeners().add(new RepeatListener());

		RequestCycle cycle = newRequestCycle(requestCycle -> { throw new RuntimeException("testing purposes only"); });
		assertFalse(cycle.processRequestAndDetach());
	}
	
	@Test
	void scheduledHandlerAvailableForExceptionListener()
	{
		IRequestHandler scheduledHandler = new EmptyRequestHandler();

		Application.get().getRequestCycleListeners().add(new IRequestCycleListener()
		{
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex)
			{
				// scheduled still scheduled
				assertEquals(scheduledHandler, cycle.find(scheduledHandler.getClass()).get());
				
				return new IRequestHandler()
				{
					@Override
					public void respond(IRequestCycle requestCycle)
					{
						// no longer scheduled
						assertNull(((RequestCycle)requestCycle).getRequestHandlerScheduledAfterCurrent());
					}
				};
			}
		});

		RequestCycle cycle = newRequestCycle(requestCycle ->
		{
			// schedule and fail immediately
			requestCycle.scheduleRequestHandlerAfterCurrent(scheduledHandler);
			throw new WicketRuntimeException();
		});
		assertTrue(cycle.processRequestAndDetach());
	}

	/**
	 * @throws Exception
	 */
	@Test
	void exceptionHandlingInOnDetach() throws Exception
	{
		// this test is a little flaky because it depends on the ordering of listeners which is not
		// guaranteed
		RequestCycle cycle = newRequestCycle(rc -> {});
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
		assertTrue(cycle.processRequestAndDetach());

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

	private class RepeatListener implements IRequestCycleListener
	{
		@Override
		public IRequestHandler onException(final RequestCycle cycle, Exception ex)
		{
			return cycle.getActiveRequestHandler();
		}
	}

	private class ErrorCodeListener implements IRequestCycleListener
	{
		private final int code;

		ErrorCodeListener(int code)
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
