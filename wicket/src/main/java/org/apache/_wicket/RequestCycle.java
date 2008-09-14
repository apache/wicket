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
package org.apache._wicket;

import org.apache._wicket.request.RequestHandler;
import org.apache._wicket.request.RequestHandlerStack;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.UrlRenderer;
import org.apache._wicket.request.request.Request;
import org.apache._wicket.request.response.Response;
import org.apache.wicket.WicketRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RequestCycle} consists of two steps:
 * <ol>
 * <li>Resolve request handler
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

	private final Response originalResponse;

	private final RequestCycleContext context;

	/**
	 * Construct.
	 * 
	 * @param request
	 * @param response
	 * @param context
	 */
	public RequestCycle(Request request, Response response, RequestCycleContext context)
	{
		super(response);

		if (request == null)
		{
			throw new IllegalArgumentException("Argument 'request' may not be null.");
		}
		if (response == null)
		{
			throw new IllegalArgumentException("Argument 'response' may not be null.");
		}
		if (context == null)
		{
			throw new IllegalArgumentException("Argument 'context' may not be null.");
		}

		this.request = request;
		this.originalResponse = response;
		this.context = context;
	}

	protected UrlRenderer newUrlRenderer()
	{
		// All URLs will be rendered relative to current request (can be overriden afterwards)
		return new UrlRenderer(getRequest().getUrl());
	}

	/**
	 * Get the original response the request was create with. Access may be necessary with the
	 * response has temporarily being replaced but your components requires access to lets say the
	 * cookie methods of a WebResponse.
	 * 
	 * @return The original response object.
	 */
	public Response getOriginalResponse()
	{
		return originalResponse;
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
		RequestHandler handler = context.decodeRequestHandler(request);
		if (handler == null)
		{
			throw new WicketRuntimeException("Could not resolve handler for request.");
		}
		return handler;
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
		return context.getRequestHandlerForException(e);
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

	/** MetaDataEntry array. */
	private MetaDataEntry<?>[] metaData;

	/**
	 * Sets the metadata for this request cycle using the given key. If the metadata object is not
	 * of the correct type for the metadata key, an IllegalArgumentException will be thrown. For
	 * information on creating MetaDataKeys, see {@link MetaDataKey}.
	 * 
	 * @param key
	 *            The singleton key for the metadata
	 * @param object
	 *            The metadata object
	 * @param <T>
	 * @throws IllegalArgumentException
	 * @see MetaDataKey
	 */
	public final <T> void setMetaData(final MetaDataKey<T> key, final T object)
	{
		metaData = key.set(metaData, object);
	}

	/**
	 * Gets metadata for this request cycle using the given key.
	 * 
	 * @param <T>
	 *            The type of the metadata
	 * 
	 * @param key
	 *            The key for the data
	 * @return The metadata or null if no metadata was found for the given key
	 * @see MetaDataKey
	 */
	public final <T> T getMetaData(final MetaDataKey<T> key)
	{
		return key.get(metaData);
	}

	private static final Logger log = LoggerFactory.getLogger(RequestCycle.class);

	/**
	 * Returns URL for the request handler or <code>null</code> if the handler couldn't have been
	 * encoded.
	 * 
	 * @param handler
	 * @return Url instance or <code>null</code>
	 */
	public Url urlFor(RequestHandler handler)
	{
		return context.encodeRequestHandler(handler);
	}

	/**
	 * Returns the rendered URL for the request handler or <code>null</code> if the handler
	 * couldn't have been rendered.
	 * <p>
	 * The resulting URL will be relative to current page.
	 * 
	 * @param handler
	 * @return Url String or <code>null</code>
	 */
	public String renderUrlFor(RequestHandler handler)
	{
		Url url = urlFor(handler);
		if (url != null)
		{
			return getUrlRenderer().renderUrl(url);
		}
		else
		{
			return null;
		}
	}
}
