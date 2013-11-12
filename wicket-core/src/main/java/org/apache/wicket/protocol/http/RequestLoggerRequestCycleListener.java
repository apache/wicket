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
package org.apache.wicket.protocol.http;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.settings.def.RequestLoggerSettings;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * Listener that logs request details in the {@link Application#getRequestLogger()} request logger.
 */
public class RequestLoggerRequestCycleListener extends AbstractRequestCycleListener
{
	/**
	 * Listeners are not thread safe. In order to keep track if a handler was the first in the
	 * request cycle, register a {@code ThreadLocal} that gets cleared out at the
	 * {@link #onEndRequest(RequestCycle) end of the request}
	 */
	private static final ThreadLocal<IRequestHandler> first = new ThreadLocal<IRequestHandler>();

	@Override
	public void onBeginRequest(RequestCycle cycle)
	{
		if (!isRequestLoggingEnabled())
			return;

		registerRequestedUrl(cycle);
	}

	@Override
	public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler)
	{
		if (!isRequestLoggingEnabled())
			return;

		registerHandler(handler);
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		if (!isRequestLoggingEnabled())
			return;

		registerHandler(handler);
	}

	@Override
	public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler,
		Exception exception)
	{
		if (!isRequestLoggingEnabled())
			return;

		registerHandler(handler);
	}

	@Override
	public void onEndRequest(RequestCycle cycle)
	{
		first.remove();
	}

	/**
	 * Determine whether a IRequestLogger is provided, and whether request logging has been enabled.
	 * 
	 * @return true when request logging is enabled.
	 */
	private boolean isRequestLoggingEnabled()
	{
		IRequestLogger requestLogger = Application.get().getRequestLogger();
		RequestLoggerSettings settings = Application.get().getRequestLoggerSettings();
		return requestLogger != null && settings.isRequestLoggerEnabled();
	}

	/**
	 * Registers the requested URL with the request logger, if one can be determined.
	 * 
	 * @param cycle
	 */
	private void registerRequestedUrl(RequestCycle cycle)
	{
		IRequestLogger requestLogger = Application.get().getRequestLogger();
		if (cycle.getRequest().getContainerRequest() instanceof HttpServletRequest)
		{
			HttpServletRequest containerRequest = (HttpServletRequest)cycle.getRequest()
				.getContainerRequest();

			AppendingStringBuffer url = new AppendingStringBuffer(containerRequest.getRequestURL());
			if (containerRequest.getQueryString() != null)
				url.append("?").append(containerRequest.getQueryString());

			requestLogger.logRequestedUrl(url.toString());
		}
	}

	/**
	 * Registers the handler with the request logger. The first handler is used as the incoming
	 * request handler, and the last registered handler as the outgoing response handler.
	 * 
	 * @param handler
	 */
	private void registerHandler(IRequestHandler handler)
	{
		IRequestLogger requestLogger = Application.get().getRequestLogger();

		if (first.get() == null)
		{
			first.set(handler);
			requestLogger.logEventTarget(handler);
		}
		requestLogger.logResponseTarget(handler);
	}
}
