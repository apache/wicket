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
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;

/**
 * A callback interface for various methods in the request cycle. If you are creating a framework
 * that needs to do something in this methods, rather than extending RequestCycle or one of its
 * subclasses, you should implement this callback and allow users to add your listener to their
 * custom request cycle.
 * <p>
 * These listeners can be added directly to the request cycle when it is created or to the
 * {@link Application}.
 * <p>
 * <b>NOTE</b>: a listener implementation is a singleton and hence needs to ensure proper handling
 * of multi-threading issues.
 * <p>
 * <h3>Call order</h3>
 * <p>
 * The interface methods are ordered in the execution order as Wicket goes through the request
 * cycle:
 * </p>
 * <ol>
 * <li>{@link #onBeginRequest(RequestCycle)}</li>
 * <li>{@link #onEndRequest(RequestCycle)}</li>
 * <li>{@link #onDetach(RequestCycle)}</li>
 * </ol>
 * <p>
 * The previous call sequence is valid for any Wicket request passing through the Wicket filter.
 * Additionally when a request handler was resolved, a new handler scheduled, or an unhandled
 * exception occurred during the request processing, any of the following can be called:
 * </p>
 * <ul>
 * <li>{@link #onRequestHandlerResolved(RequestCycle, org.apache.wicket.request.IRequestHandler)}</li>
 * <li>{@link #onRequestHandlerScheduled(RequestCycle, org.apache.wicket.request.IRequestHandler)}</li>
 * <li>{@link #onException(RequestCycle, Exception)}, followed by
 * {@link #onExceptionRequestHandlerResolved(RequestCycle, org.apache.wicket.request.IRequestHandler, Exception)} </li>
 * </ul>
 * 
 * <h3>Implementing your own</h3>
 * <p>
 * Use {@link AbstractRequestCycleListener} for a default, empty implementation as a base class.
 * </p>
 * 
 * <h3>Example</h3>
 * <p>
 * A short example of a request counter.
 * </p>
 * 
 * <pre>
 * public class RequestCounter extends AbstractRequestCycleListener
 * {
 * 	private AtomicLong counter = new AtomicLong(0);
 * 
 * 	public void onBeginRequest(RequestCycle cycle)
 * 	{
 * 		counter.incrementAndGet();
 * 	}
 * 
 * 	public long getRequestCount()
 * 	{
 * 		return counter.longValue();
 * 	}
 * }
 * 
 * public class MyApplication extends WebApplication
 * {
 * 	public void init()
 * 	{
 * 		super.init();
 * 		getRequestCycleListeners().add(new RequestCounter());
 * 	}
 * }
 * </pre>
 * 
 * @author Jeremy Thomerson
 * @author Martijn Dashorst
 * 
 * @see AbstractRequestCycleListener
 * @see org.apache.wicket.Application#getRequestCycleListeners()
 */
public interface IRequestCycleListener
{
	/**
	 * Called when the request cycle object is beginning its response
	 * 
	 * @param cycle
	 */
	void onBeginRequest(RequestCycle cycle);

	/**
	 * Called when the request cycle object has finished its response
	 * 
	 * @param cycle
	 */
	void onEndRequest(RequestCycle cycle);

	/**
	 * Called after the request cycle has been detached
	 * 
	 * @param cycle
	 */
	void onDetach(RequestCycle cycle);

	/**
	 * Called when an {@link IRequestHandler} is resolved and will be executed.
	 * 
	 * @param cycle
	 * 
	 * @param handler
	 */
	void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler);

	/**
	 * Called when a {@link IRequestHandler} has been scheduled. Can be called multiple times during
	 * a request when new handlers get scheduled for processing.
	 * 
	 * @param cycle
	 * @param handler
	 * @see RequestCycle#scheduleRequestHandlerAfterCurrent(IRequestHandler)
	 */
	void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler);

	/**
	 * Called when there is an exception in the request cycle that would normally be handled by
	 * {@link RequestCycle#handleException(Exception)}
	 * 
	 * Note that in the event of an exception, {@link #onEndRequest(RequestCycle)} will still be called after
	 * these listeners have {@link #onException(RequestCycle, Exception)} called
	 * 
	 * @param cycle
	 * 
	 * @return request handler that will be executed or {@code null} if none. If a request handler
	 *         is returned, it will override any configured exception mapper
	 * 
	 * @param ex
	 *            the exception that was passed in to
	 *            {@link RequestCycle#handleException(Exception)}
	 */
	IRequestHandler onException(RequestCycle cycle, Exception ex);

	/**
	 * Called when an {@link IRequestHandler} is resolved for an exception and will be executed.
	 * 
	 * @param cycle
	 * @param handler
	 * @param exception
	 */
	void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler,
		Exception exception);

	/**
	 * Called after an {@link IRequestHandler} has been executed. If the execution resulted in an
	 * exception this method will not be called for that particular {@link IRequestHandler}.
	 * 
	 * @param cycle
	 * @param handler
	 */
	void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler);

	/**
	 * Called after a Url is generated for a {@link IRequestHandler}. This method can be used to
	 * modify generated urls, for example query parameters can be added.
	 * 
	 * @param cycle
	 * @param handler
	 * @param url
	 */
	void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url);
}
