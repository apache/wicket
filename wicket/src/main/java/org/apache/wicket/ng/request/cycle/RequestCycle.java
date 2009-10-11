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

import org.apache.wicket.ng.MetaDataEntry;
import org.apache.wicket.ng.MetaDataKey;
import org.apache.wicket.ng.ThreadContext;
import org.apache.wicket.ng.request.Request;
import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.RequestMapper;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.handler.PageProvider;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.ng.request.response.Response;
import org.apache.wicket.util.lang.Checks;
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

    private final RequestMapper requestMapper;
    private final ExceptionMapper exceptionMapper;

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

        this.request = context.getRequest();
        this.originalResponse = context.getResponse();
        this.requestMapper = context.getRequestMapper();
        this.exceptionMapper = context.getExceptionMapper();
    }

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
     * Resolves current request to a {@link RequestHandler}.
     * 
     * @return RequestHandler instance
     */
    protected RequestHandler resolveRequestHandler()
    {
        RequestHandler handler = requestMapper.mapRequest(request);
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
            RequestHandler handler = resolveRequestHandler();
            if (handler != null)
            {
                executeRequestHandler(handler);
                return true;
            }

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
        return exceptionMapper.map(e);
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
    private MetaDataEntry< ? >[] metaData;

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
        return requestMapper.mapHandler(handler);
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
     * invoked after all {@link RequestHandler}s are detached.
     * 
     * @param detachCallback
     */
    public void register(DetachCallback detachCallback)
    {
        detachCallbacks.add(detachCallback);
    };

    private List<DetachCallback> detachCallbacks = new ArrayList<DetachCallback>();

    /**
     * Custom callback invoked on request cycle detach. Detach callbacks are invoked after all
     * {@link RequestHandler}s are detached.
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

    private static void set(RequestCycle requestCycle)
    {
        ThreadContext.setRequestCycle(requestCycle);
    }

    public void setResponsePage(RequestablePage page)
    {
        scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(new PageProvider(page),
                RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT));
    }

    public void setResponsePage(Class< ? extends RequestablePage> pageClass,
            PageParameters parameters)
    {
        PageProvider provider = new PageProvider(pageClass);
        scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(provider,
                RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT));
    }
}
