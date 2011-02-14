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

/**
 * A callback interface for various methods in the request cycle. If you are creating a framework
 * that needs to do something in this methods, rather than extending RequestCycle or one of its
 * subclasses, you should implement this callback and allow users to add your listener to their
 * custom request cycle.
 * 
 * These listeners can be added directly to the request cycle when it is created or to the
 * {@link Application}
 * 
 * @author Jeremy Thomerson
 * @see Application#addRequestCycleListener(IRequestCycleListener)
 * @see RequestCycle#register(IRequestCycleListener)
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
	 * Called when there is an exception in the request cycle that would normally be handled by
	 * {@link RequestCycle#handleException(Exception)}
	 * 
	 * Note that in the event of an exception, {@link #onEndRequest()} will still be called after
	 * these listeners have {@link #onException(Exception)} called
	 * 
	 * @param cycle
	 * 
	 * @return request handler that will be exectued or {@code null} if none. If a request handler
	 *         is returned, it will override any configured exception mapper
	 * 
	 * @param ex
	 *            the exception that was passed in to
	 *            {@link RequestCycle#handleException(Exception)}
	 */
	IRequestHandler onException(RequestCycle cycle, Exception ex);

	/**
	 * Called when an {@link IRequestHandler} is resolved and will be executed.
	 * 
	 * @param handler
	 */
	void onRequestHandlerResolved(IRequestHandler handler);

	/**
	 * Called when an {@link IRequestHandler} is resolved for an exception and will be executed.
	 * 
	 * @param handler
	 * @param exception
	 */
	void onExceptionRequestHandlerResolved(IRequestHandler handler, Exception exception);

	/**
	 * @param handler
	 */
	void onRequestHandlerScheduled(IRequestHandler handler);
}