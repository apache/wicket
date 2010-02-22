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
import java.util.List;

import org.apache.wicket.IRequestHandler;
import org.apache.wicket.MetaDataEntry;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.ng.ThreadContext;
import org.apache.wicket.ng.request.IRequestMapper;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.component.IRequestablePage;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.handler.IPageProvider;
import org.apache.wicket.ng.request.handler.PageProvider;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.ng.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.ng.resource.ResourceReference;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.ClientInfo;
import org.apache.wicket.util.lang.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RequestCycle} consists of two steps:
 * <ol>
 * <li>Resolve request handler
 * <li>Execute request handler
 * </ol>
 * During {@link IRequestHandler} execution the handler can execute other {@link IRequestHandler}s,
 * schedule another {@link IRequestHandler} or replace all {@link IRequestHandler}s on stack with
 * another {@link IRequestHandler}.
 * 
 * @see #executeRequestHandler(IRequestHandler)
 * @see #scheduleRequestHandlerAfterCurrent(IRequestHandler)
 * @see #replaceAllRequestHandlers(IRequestHandler)
 * 
 * @author Matej Knopp
 * @author igor.vaynberg
 */
public class RequestCycle extends RequestHandlerStack
{
	private static final Logger log = LoggerFactory.getLogger(RequestCycle.class);

	/**
	 * Custom callback invoked on request cycle detach. Detach callbacks are invoked after all
	 * {@link IRequestHandler}s are detached.
	 * 
	 * @author Matej Knopp
	 */
	public interface DetachCallback
	{
		/**
		 * Invoked on request cycle detach.
		 * 
		 * @param requestCycle
		 */
		public void onDetach(RequestCycle requestCycle);
	};

	/**
	 * Returns request cycle associated with current thread.
	 * 
	 * @return request cycle instance or <code>null</code> if no request cycle is associated with
	 *         current thread.
	 */
	public static RequestCycle get()
	{
		return ThreadContext.getRequestCycle();
	}

	/**
	 * 
	 * @param requestCycle
	 */
	private static void set(RequestCycle requestCycle)
	{
		ThreadContext.setRequestCycle(requestCycle);
	}

	private Request request;

	private final Response originalResponse;

	private final IRequestMapper requestMapper;

	private final IExceptionMapper exceptionMapper;

	private final List<DetachCallback> detachCallbacks = new ArrayList<DetachCallback>();

	private UrlRenderer urlRenderer;

	/** MetaDataEntry array. */
	private MetaDataEntry<?>[] metaData;

	/**
	 * Construct.
	 * 
	 * @param context
	 */
	public RequestCycle(RequestCycleContext context)
	{
		super(context.getResponse());

		Checks.argumentNotNull(context, "context");

		Checks.argumentNotNull(context.getRequest(), "context.request");
		Checks.argumentNotNull(context.getResponse(), "context.response");
		Checks.argumentNotNull(context.getRequestMapper(), "context.requestMapper");
		Checks.argumentNotNull(context.getExceptionMapper(), "context.exceptionMapper");

		request = context.getRequest();
		originalResponse = context.getResponse();
		requestMapper = context.getRequestMapper();
		exceptionMapper = context.getExceptionMapper();
	}

	/**
	 * 
	 * @return a new url renderer
	 */
	protected UrlRenderer newUrlRenderer()
	{
		// All URLs will be rendered relative to current request (can be overriden afterwards)
		return new UrlRenderer(getRequest().getUrl());
	}

	/**
	 * Get the original response the request was created with. Access to the original response may
	 * be necessary if the response has been temporarily replaced but the components require methods
	 * from original response (i.e. cookie methods of WebResponse, etc).
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
	 * Resolves current request to a {@link IRequestHandler}.
	 * 
	 * @return RequestHandler instance
	 */
	protected IRequestHandler resolveRequestHandler()
	{
		IRequestHandler handler = requestMapper.mapRequest(request);
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
	 * 
	 * @return <code>true</code> if the request resolved to a Wicket request, <code>false</code>
	 *         otherwise.
	 */
	public boolean processRequest()
	{
		try
		{
			set(this);
			IRequestHandler handler = resolveRequestHandler();
			if (handler != null)
			{
				executeRequestHandler(handler);
				return true;
			}

		}
		catch (Exception e)
		{
			IRequestHandler handler = handleException(e);
			if (handler != null)
			{
				executeExceptionRequestHandler(handler, getExceptionRetryCount());
			}
			else
			{
				log.error("Error during request processing", e);
			}
			return true;
		}
		finally
		{
			set(null);
		}
		return false;
	}

	/**
	 * Convenience method that processes the request and detaches the {@link RequestCycle}.
	 * 
	 * @return <code>true</code> if the request resolved to a Wicket request, <code>false</code>
	 *         otherwise.
	 */
	public boolean processRequestAndDetach()
	{
		boolean result;
		try
		{
			result = processRequest();
		}
		finally
		{
			detach();
		}
		return result;
	}

	/**
	 * 
	 * @param handler
	 * @param retryCount
	 */
	private void executeExceptionRequestHandler(final IRequestHandler handler, final int retryCount)
	{
		try
		{
			executeRequestHandler(handler);
		}
		catch (Exception e)
		{
			if (retryCount > 0)
			{
				IRequestHandler next = handleException(e);
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
	 * Return {@link IRequestHandler} for the given exception.
	 * 
	 * @param e
	 * @return RequestHandler instance
	 */
	protected IRequestHandler handleException(final Exception e)
	{
		return exceptionMapper.map(e);
	}

	/**
	 * @return current request
	 */
	public Request getRequest()
	{
		return request;
	}

	/**
	 * INTERNAL This method is for internal Wicket use. Do not call it yourself unless you know what
	 * you are doing.
	 * 
	 * @param request
	 */
	public void setRequest(Request request)
	{
		// It would be mighty nice if request was final. However during multipart it needs to be set
		// to
		// MultipartServletWebRequest by Form. It can't be done before creating the request cycle
		// (in wicket filter)
		// because only the form knows max upload size
		this.request = request;
	}

	/**
	 * @see org.apache.wicket.ng.request.cycle.RequestHandlerStack#getRequestCycle()
	 */
	@Override
	protected RequestCycle getRequestCycle()
	{
		return this;
	}

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

	/**
	 * Returns URL for the request handler or <code>null</code> if the handler couldn't have been
	 * encoded.
	 * 
	 * @param handler
	 * @return Url instance or <code>null</code>
	 */
	public Url urlFor(IRequestHandler handler)
	{
		return requestMapper.mapHandler(handler);
	}

	/**
	 * Returns a {@link Url} for the resource reference
	 * 
	 * @param reference
	 *            resource reference
	 * @param params
	 *            parameters for the resource or {@code null} if none
	 * @return {@link Url} for the reference
	 */
	public Url urlFor(ResourceReference reference, PageParameters params)
	{
		return urlFor(new ResourceReferenceRequestHandler(reference, params));
	}

	/**
	 * Returns a {@link Url} for the resource reference
	 * 
	 * @param reference
	 *            reference
	 * @return {@link Url} for the reference
	 */
	public Url urlFor(ResourceReference reference)
	{
		return urlFor(reference, null);
	}

	/**
	 * Returns the rendered URL for the request handler or <code>null</code> if the handler couldn't
	 * have been rendered.
	 * <p>
	 * The resulting URL will be relative to current page.
	 * 
	 * @param handler
	 * @return Url String or <code>null</code>
	 */
	public String renderUrlFor(IRequestHandler handler)
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

	/**
	 * @see org.apache.wicket.ng.request.cycle.RequestHandlerStack#detach()
	 */
	@Override
	public void detach()
	{
		set(this);
		try
		{
			super.detach();
		}
		finally
		{
			for (DetachCallback c : detachCallbacks)
			{
				try
				{
					c.onDetach(this);
				}
				catch (Exception e)
				{
					log.error("Error detaching DetachCallback", e);
				}
				;
			}
			set(null);
		}
	}

	/**
	 * Registers a callback to be invoked on {@link RequestCycle} detach. The callback will be
	 * invoked after all {@link IRequestHandler}s are detached.
	 * 
	 * @param detachCallback
	 */
	public void register(DetachCallback detachCallback)
	{
		detachCallbacks.add(detachCallback);
	}

	/**
	 * Convenience method for setting next page to be rendered.
	 * 
	 * @param page
	 */
	public void setResponsePage(IRequestablePage page)
	{
		scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(new PageProvider(page),
			RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT));
	}


	/**
	 * Convenience method for setting next page to be rendered.
	 * 
	 * @param pageClass
	 */
	public void setResponsePage(Class<? extends IRequestablePage> pageClass)
	{
		IPageProvider provider = new PageProvider(pageClass, null);
		scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(provider,
			RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT));
	}


	/**
	 * Convenience method for setting next page to be rendered.
	 * 
	 * @param pageClass
	 * @param parameters
	 */
	public void setResponsePage(Class<? extends IRequestablePage> pageClass,
		PageParameters parameters)
	{
		IPageProvider provider = new PageProvider(pageClass, parameters);
		scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(provider,
			RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT));
	}


	/**
	 * Creates a new agent info object based on this request. Typically, this method is called once
	 * by the session and the returned object will be cached in the session after that call; we can
	 * expect the client to stay the same for the whole session, and implementations of
	 * {@link #newClientInfo()} might be relatively expensive.
	 * 
	 * @return the agent info object based on this request
	 */
	// TODO NG Get this shit out of here!
	public ClientInfo newClientInfo()
	{
		return new WebClientInfo(this);
	}

	public ClientInfo getClientInfo()
	{
		return newClientInfo();
	}
}
