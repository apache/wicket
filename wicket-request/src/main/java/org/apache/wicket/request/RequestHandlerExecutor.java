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
package org.apache.wicket.request;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages executions of {@link IRequestHandler}s.
 * 
 * @author Matej Knopp
 * @author igor.vaynberg
 */
public abstract class RequestHandlerExecutor
{
	private static final Logger log = LoggerFactory.getLogger(RequestHandlerExecutor.class);

	private IRequestHandler active;

	private final List<IRequestHandler> inactiveRequestHandlers = new ArrayList<>();

	private IRequestHandler scheduledAfterCurrent = null;

	/**
	 * Get the handler currently executed.
	 *
	 * @return active handler
	 */
	public IRequestHandler getActive()
	{
		return active;
	}

	/**
	 * Execute the given handler.
	 *
	 * @param handler handler to be executed
	 * @return handler to be executed next
	 * @throws ReplaceHandlerException if another handler should replace the given handler for execution
	 */
	public IRequestHandler execute(final IRequestHandler handler)
	{
		active = handler;
		try
		{
			respond(handler);
		}
		finally
		{
			active = null;
			inactiveRequestHandlers.add(handler);
		}

		IRequestHandler scheduled = scheduledAfterCurrent;
		scheduledAfterCurrent = null;
		return scheduled;
	}

	/**
	 * Certain exceptions can carry a request handler they wish to be executed, this method tries to
	 * resolve such a handler given an exception.
	 * 
	 * @param exception
	 * @return request handler or null} if one cannot be resolved
	 * @deprecated
	 */
	public final IRequestHandler resolveHandler(RuntimeException exception)
	{
		Args.notNull(exception, "exception");

		ReplaceHandlerException replacer = Exceptions.findCause(exception,
			ReplaceHandlerException.class);
		return replacer != null ? replacer.replacementRequestHandler : null;
	}

	/**
	 * Allows the request handler to respond to the request
	 * 
	 * @param handler
	 */
	protected abstract void respond(IRequestHandler handler);

	/**
	 * Schedules the handler after the current one
	 * 
	 * @param handler
	 */
	public void schedule(final IRequestHandler handler)
	{
		scheduledAfterCurrent = handler;
	}

	/**
	 * @return scheduled request handler after the current one
	 */
	public IRequestHandler next()
	{
		return scheduledAfterCurrent;
	}

	/**
	 * Replaces all request handlers on the stack with the specified one and executes it. If there
	 * are any request handlers currently executing (this method is called from inside
	 * {@link IRequestHandler#respond(IRequestCycle)}) the execution is interrupted via an
	 * exception.
	 * 
	 * @param handler
	 */
	public void replaceAll(final IRequestHandler handler)
	{
		if (active == null)
		{
			execute(handler);
		}
		else
		{
			throw new ReplaceHandlerException(handler, true);
		}
	}

	/**
	 * Detaches all request handlers
	 */
	public void detach()
	{
		if (active != null)
		{
			// no request handler should be active at this point
			log.warn("request handler is still active.");

			inactiveRequestHandlers.add(active);
			active = null;
		}

		for (IRequestHandler handler : inactiveRequestHandlers)
		{
			try
			{
				detach(handler);
			}
			catch (RuntimeException exception)
			{
				throw exception;
			}
			catch (Exception exception)
			{
				log.error("Error detaching RequestHandler", exception);
			}
		}
	}

	/**
	 * Allows the request handler to detach
	 * 
	 * @param handler
	 */
	protected abstract void detach(IRequestHandler handler);

	/**
	 * Exception to stop current request handler and execute a new one.
	 * 
	 * @author Matej Knopp
	 */
	public static class ReplaceHandlerException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		private final boolean removeScheduled;

		private final IRequestHandler replacementRequestHandler;

		/**
		 * Construct.
		 * 
		 * @param replacementRequestHandler
		 * @param removeScheduled should a possibly scheduled handler be removed
		 */
		public ReplaceHandlerException(final IRequestHandler replacementRequestHandler,
			final boolean removeScheduled)
		{
			this.replacementRequestHandler = replacementRequestHandler;
			this.removeScheduled = removeScheduled;
		}
		
		/**
		 * @deprecated use {@link #getRemoveScheduled()} instead
		 */
		public boolean isRemoveAll()
		{
			return removeScheduled;
		}

		/**
		 * Should a scheduled handler be removed before replacing the handler
		 */
		public boolean getRemoveScheduled()
		{
			return removeScheduled;
		}

		/**
		 * @return the RequestHandler that should be used to continue handling the request
		 */
		public IRequestHandler getReplacementRequestHandler()
		{
			return replacementRequestHandler;
		}

		/**
		 * @see java.lang.Throwable#fillInStackTrace()
		 */
		@Override
		public Throwable fillInStackTrace()
		{
			// don't do anything here
			return null;
		}
	}
}
