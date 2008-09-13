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
package org.apache.wicket.requestng;

import org.apache.wicket.requestng.request.Request;
import org.apache.wicket.requestng.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RequestCycle} consists of two steps:
 * <ol>
 * <li>Resolve request hander
 * <li>Execute request handler
 * </ol>
 * During {@link RequestHandler} execution the handler can execute other {@link RequestHandler}s,
 * replace itself with another {@link RequestHandler} or replace all {@link RequestHandler}s on
 * stack with another {@link RequestHandler}.
 * 
 * @see #executeRequestHandler(RequestHandler)
 * @see #replaceCurrentRequestHandler(RequestHandler)
 * @see #replaceAllRequestHandlers(RequestHandler)
 * 
 * @author Matej Knopp
 */
public class RequestCycle extends RequestHandlerStack
{
	private final Request request;
	
	private UrlRenderer urlRenderer;
	
	/**
	 * Construct.
	 * 
	 * @param request
	 * @param globalResponse
	 */
	public RequestCycle(Request request, Response globalResponse)
	{
		super(globalResponse);
		this.request = request;		
	}

	protected UrlRenderer newUrlRenderer()
	{
		return new UrlRenderer(getRequest().getUrl());
	}
	
	/**
	 * Returns {@link UrlRenderer} for this {@link RequestCycle}.
	 * 
	 * @return UrlRenderer instance.
	 */
	public final UrlRenderer getUrlRenderer()
	{
		if (urlRenderer == null)
		{
			urlRenderer = newUrlRenderer();
		}
		return urlRenderer;
	}
	
	/**
	 * Resolves current request to a {@link RequestHandler}.
	 * 
	 * @return RequestHandler instance
	 */
	protected RequestHandler resolveRequestHandler()
	{
		// TODO: Implement
		return null;
	}

	/**
	 * @return How many times will Wicket attempt to render the exception request handler before
	 *         giving up.
	 */
	protected int getExceptionRetryCount()
	{
		return 10;
	}

	/**
	 * Processes the request.
	 */
	public void processRequest()
	{
		try
		{
			RequestHandler handler = resolveRequestHandler();
			executeRequestHandler(handler);
		}
		catch (Exception e)
		{
			RequestHandler handler = handleException(e);
			if (handler != null)
			{
				executeExceptionRequestHandler(handler, getExceptionRetryCount());
			}
			else
			{
				log.error("Error during request processing", e);
			}
		}
	}

	private void executeExceptionRequestHandler(RequestHandler handler, int retryCount)
	{
		try
		{
			executeRequestHandler(handler);
		}
		catch (Exception e)
		{
			if (retryCount > 0)
			{
				RequestHandler next = handleException(e);
				if (handler != null)
				{
					executeExceptionRequestHandler(next, retryCount - 1);
					return;
				}
			}
			log.error("Error during processing error message", e);
		}
	}

	/**
	 * Return {@link RequestHandler} for the given exception.
	 * 
	 * @param e
	 * @return RequestHandler instance
	 */
	protected RequestHandler handleException(Exception e)
	{
		// TODO: Implement
		return null;
	}

	/**
	 * @return current request
	 */
	public Request getRequest()
	{
		return request;
	}

	@Override
	protected RequestCycle getRequestCycle()
	{
		return this;
	}

	private static final Logger log = LoggerFactory.getLogger(RequestCycle.class);
}
