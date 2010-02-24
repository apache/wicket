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
package org.apache.wicket.ng.request.cycle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.IRequestHandler;
import org.apache.wicket.Response;
import org.apache.wicket.WicketRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages stack of {@link IRequestHandler}s.
 * 
 * @author Matej Knopp
 */
public abstract class RequestHandlerStack
{
	private static final Logger log = LoggerFactory.getLogger(RequestHandlerStack.class);

	// we need both Queue and List interfaces
	private final LinkedList<IRequestHandler> requestHandlers = new LinkedList<IRequestHandler>();

	private final List<IRequestHandler> inactiveRequestHandlers = new ArrayList<IRequestHandler>();

	private IRequestHandler scheduledAfterCurrent = null;

	private Response response;

	/**
	 * Construct.
	 * 
	 * @param response
	 */
	public RequestHandlerStack(Response response)
	{
		this.response = response;
	}

	protected abstract RequestCycle getRequestCycle();

	/**
	 * Returns currently active {@link IRequestHandler}.
	 * 
	 * @return Active RequestHandler or <code>null</code> if no handler is active.
	 */
	public IRequestHandler getActiveRequestHandler()
	{
		return requestHandlers.peek();
	}

	/**
	 * Executes the specified {@link IRequestHandler}. When the specified {@link IRequestHandler}
	 * finishes, the {@link IRequestHandler} that invoked this method continues (unless the new
	 * {@link IRequestHandler} called {@link #replaceAllRequestHandlers(IRequestHandler)}.
	 * 
	 * @param handler
	 */
	public void executeRequestHandler(IRequestHandler handler)
	{
		final boolean first = requestHandlers.isEmpty();
		requestHandlers.add(handler);

		IRequestHandler replacementHandler = null;
		Response originalResponse = response;
		try
		{
			handler.respond(getRequestCycle());
		}
		catch (ReplaceHandlerException exception)
		{
			if (exception.removeAll && !first)
			{
				throw exception;
			}
			replacementHandler = exception.replacementRequestHandler;
		}
		finally
		{
			response = originalResponse;
			requestHandlers.poll();
			inactiveRequestHandlers.add(handler);
		}

		IRequestHandler scheduled = scheduledAfterCurrent;
		scheduledAfterCurrent = null;

		if (replacementHandler != null)
		{
			executeRequestHandler(replacementHandler);
		}
		else if (scheduled != null)
		{
			executeRequestHandler(scheduled);
		}
	}

	/**
	 * Schedules the request handler to be executed after current request handler finishes. If there
	 * is already another request handler scheduled it will be discarded and overwritten by the new
	 * one. If {@link #replaceCurrentRequestHandler(IRequestHandler)} or
	 * {@link #replaceAllRequestHandlers(IRequestHandler)} is invoked during current request handler
	 * execution the scheduled handler will be also discarded.
	 * 
	 * @param handler
	 *            handler to be executed after current request handler finishes
	 */
	public void scheduleRequestHandlerAfterCurrent(IRequestHandler handler)
	{
		scheduledAfterCurrent = handler;
	}

	/**
	 * Replaces the currently executed {@link IRequestHandler} with new {@link IRequestHandler}. The
	 * currently executed {@link IRequestHandler} is terminated and the new {@link IRequestHandler}
	 * is executed.
	 * 
	 * @param handler
	 */
	// FIXME
	// Is this method really useful for anything? To execute request handler after current
	// #scheduleRequestHandlerAfterCurrent is better alternative because it doesn't terminate
	// current request handler.
	// To restart request processing #replaceAllRequestHandlers is better alternative because it
	// unrolls entire stack and cancels all request handlers in stack
	public void replaceCurrentRequestHandler(IRequestHandler handler)
	{
		if (requestHandlers.isEmpty())
		{
			executeRequestHandler(handler);
		}
		else
		{
			throw new ReplaceHandlerException(handler, false);
		}
	}

	/**
	 * Removes the whole {@link IRequestHandler} stack, terminates currently running
	 * {@link IRequestHandler} and executes the new {@link IRequestHandler}.
	 * 
	 * @param handler
	 */
	public void replaceAllRequestHandlers(IRequestHandler handler)
	{
		if (requestHandlers.isEmpty())
		{
			executeRequestHandler(handler);
		}
		else
		{
			throw new ReplaceHandlerException(handler, true);
		}
	}

	/**
	 * Returns the active {@link Response}.
	 * 
	 * @return response object.
	 */
	public Response getResponse()
	{
		return response;
	}

	/**
	 * Replaces current {@link Response} with new {@link Response} instance. The original response
	 * is always restored after the {@link IRequestHandler#respond(RequestCycle)} method is
	 * finished.
	 * 
	 * @param response
	 * @return Response being replaced.
	 */
	public Response setResponse(Response response)
	{
		Response current = this.response;
		this.response = response;
		return current;
	}

	/**
	 * Detaches all {@link IRequestHandler}s.
	 */
	public void detach()
	{
		if (!requestHandlers.isEmpty())
		{
			// All requests handlers should be inactive at this point
			log.warn("Some of the request handlers are still active.");

			inactiveRequestHandlers.addAll(requestHandlers);
			requestHandlers.clear();
		}

		for (IRequestHandler handler : inactiveRequestHandlers)
		{
			try
			{
				handler.detach(getRequestCycle());
			}
			catch (Throwable exception)
			{
				log.error("Error detaching RequestHandler", exception);
			}
		}
	}

	/**
	 * Exception to stop current request handler and execute a new one.
	 * 
	 * @author Matej Knopp
	 */
	public static class ReplaceHandlerException extends WicketRuntimeException
	{
		private static final long serialVersionUID = 1L;

		private final boolean removeAll;

		private final IRequestHandler replacementRequestHandler;

		/**
		 * Construct.
		 * 
		 * @param replacementRequestHandler
		 * @param removeAll
		 */
		public ReplaceHandlerException(IRequestHandler replacementRequestHandler, boolean removeAll)
		{
			this.replacementRequestHandler = replacementRequestHandler;
			this.removeAll = removeAll;
		}

		/**
		 * @see java.lang.Throwable#fillInStackTrace()
		 */
		@Override
		public synchronized Throwable fillInStackTrace()
		{
			// don't do anything here
			return null;
		}
	};
}
