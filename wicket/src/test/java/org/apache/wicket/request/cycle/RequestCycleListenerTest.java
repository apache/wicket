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

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.resource.DummyApplication;

/**
 * @author Jeremy Thomerson
 */
public class RequestCycleListenerTest extends BaseRequestHandlerStackTest
{


	private int begins, ends, exceptions, exceptionsMapped, responses, detaches,
		detachesnotified = 0;

	private IRequestHandler handler;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		ThreadContext.setApplication(new DummyApplication());
	}

	private RequestCycle newRequestCycle(final boolean throwExceptionInRespond)
	{
		final Response originalResponse = newResponse();
		Request request = new MockWebRequest(Url.parse("http://wicket.apache.org"));
		handler = new IRequestHandler()
		{
			public void respond(IRequestCycle requestCycle)
			{
				if (throwExceptionInRespond)
				{
					throw new RuntimeException("testing purposes only");
				}
				responses++;
			}

			public void detach(IRequestCycle requestCycle)
			{
				detaches++;
			}
		};
		IRequestMapper requestMapper = new IRequestMapper()
		{
			public IRequestHandler mapRequest(Request request)
			{
				return handler;
			}

			public Url mapHandler(IRequestHandler requestHandler)
			{
				throw new UnsupportedOperationException();
			}

			public int getCompatibilityScore(Request request)
			{
				throw new UnsupportedOperationException();
			}
		};
		IExceptionMapper exceptionMapper = new IExceptionMapper()
		{
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
	public void testBasicOperations() throws Exception
	{
		Application.get().addRequestCycleListener(new IncrementingListener());

		RequestCycle cycle = newRequestCycle(false);

		assertValues(0, 0, 0, 0, 0, 0, 0);
		/*
		 * begins, ends, responses and detaches should increment by one because we have one listener
		 * 
		 * exceptions should not increment because none are thrown
		 */
		cycle.processRequestAndDetach();
		assertValues(1, 1, 0, 0, 1, 1, 1);

		// TEST WITH TWO LISTENERS
		cycle = newRequestCycle(false);
		cycle.getListeners().add(new IncrementingListener());
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
		assertValues(3, 3, 0, 0, 2, 2, 3);

		// TEST WITH TWO LISTENERS AND AN EXCEPTION DURING RESPONSE
		cycle = newRequestCycle(true);
		cycle.getListeners().add(new IncrementingListener());
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
		assertValues(5, 5, 2, 1, 2, 3, 5);
	}

	/**
	 * @throws Exception
	 */
	public void testExceptionHandingInOnDetach() throws Exception
	{
		// this test is a little flaky because it depends on the ordering of listeners which is not
		// guaranteed
		RequestCycle cycle = newRequestCycle(false);
		cycle.getListeners().add(new IncrementingListener());
		cycle.getListeners().add(new IncrementingListener()
		{
			@Override
			public void onDetach(final RequestCycle cycle)
			{
				super.onDetach(cycle);
				throw new RuntimeException();
			}
		});
		cycle.getListeners().add(new IncrementingListener());
		cycle.processRequestAndDetach();
		assertValues(3, 3, 0, 0, 1, 1, 3);
	}


	private void assertValues(int begins, int ends, int exceptions, int exceptionsMapped,
		int responses, int detaches, int detachesnotified)
	{
		assertEquals(begins, this.begins);
		assertEquals(ends, this.ends);
		assertEquals(exceptions, this.exceptions);
		assertEquals(exceptionsMapped, this.exceptionsMapped);
		assertEquals(responses, this.responses);
		assertEquals(detaches, this.detaches);
		assertEquals(detachesnotified, this.detachesnotified);
	}

	private class IncrementingListener implements IRequestCycleListener
	{
		public void onException(final RequestCycle cycle, Exception ex)
		{
			exceptions++;
		}

		public void onEndRequest(final RequestCycle cycle)
		{
			ends++;
		}

		public void onBeginRequest(final RequestCycle cycle)
		{
			begins++;
		}

		public void onDetach(final RequestCycle cycle)
		{
			detachesnotified++;
		}
	}


}
