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
package org.apache.wicket;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;
import org.apache.wicket.request.target.resource.ISharedResourceRequestTarget;

/**
 * Handles url rewriting, provides access to the namespace for markup Ids and isolated session
 * state.
 * 
 * <p>
 * This is the base strategy for encoding URLs, which is to leave them as is. This is mainly so that
 * PortletRequestContext can implement it's portlet encoding strategies as portlets need to have
 * special URLs encoded with portal information and portlet namespace.
 * 
 * <p>
 * For url rewriting, only three methods are needed to support creating Portlet ActionURLs, Portlet
 * RenderURLs and Resource/Ajax URLs.
 * 
 * The RequestContext is somewhat comparable to the JSF ExternalContext interface which abstracts
 * the external environment (like web or portlet) in which the application is currently running.
 * As this is request dependent (the same application can technically be accessed even concurrently
 * as web or portlet), in Wicket this context has been termed RequestContext.
 * 
 * @see PortletRequestContext
 * @author Ate Douma
 */
public class RequestContext
{
	/** Thread-local that holds the current request context. */
	private static final ThreadLocal<RequestContext> current = new ThreadLocal<RequestContext>();

	/**
	 * Construct.
	 */
	public RequestContext()
	{
		set(this);
	}

	/**
	 * @return The current threads request context, will make one if there wasn't one.
	 */
	public static final RequestContext get()
	{
		RequestContext context = current.get();
		if (context == null)
		{
			context = new RequestContext();
		}
		return context;
	}

	/**
	 * Resets the {@link RequestContext} for the current threads active request.
	 */
	public static final void unset()
	{
		current.set(null);
	}

	/**
	 * Sets the {@link RequestContext} for the current threads active request.
	 * 
	 * @param context
	 */
	protected static final void set(RequestContext context)
	{
		current.set(context);
	}

	/**
	 * @see PortletRequestContext#getNamespace()
	 * @return CharSequence the namespace of this request, typically overridden by the portlet
	 *         implementation.
	 */
	public CharSequence getNamespace()
	{
		return "";
	}

	/**
	 * Encodes markup Ids, typically overridden by the portlet implementation.
	 * 
	 * @see PortletRequestContext#encodeMarkupId(String)
	 * @param markupId
	 *            the markup Id to encode
	 * @return the encoded markup
	 */
	public String encodeMarkupId(String markupId)
	{
		return markupId;
	}

	/**
	 * Encodes URL's for action URLs, typically overridden by the portlet implementation.
	 * 
	 * @see PortletRequestContext#encodeActionURL(CharSequence)
	 * @param path
	 *            the URL to encode
	 * @return the encoded url
	 */
	public CharSequence encodeActionURL(CharSequence path)
	{
		return path;
	}

	/**
	 * Encodes URL's for render URLs, typically overridden by the portlet implementation.
	 * 
	 * @see PortletRequestContext#encodeRenderURL(CharSequence)
	 * @param path
	 *            the URL to encode
	 * @return the encoded url
	 */
	public CharSequence encodeRenderURL(CharSequence path)
	{
		return path;
	}

	/**
	 * Encodes URL's for resource targets, typically overridden by the portlet implementation.
	 * 
	 * @see PortletRequestContext#encodeResourceURL(CharSequence)
	 * @param path
	 *            the URL to encode
	 * @return the encoded url
	 */
	public CharSequence encodeResourceURL(CharSequence path)
	{
		return path;
	}

	/**
	 * Encodes URL's for shared resource targets, typically overridden by the portlet
	 * implementation.
	 * 
	 * @see ISharedResourceRequestTarget
	 * @see PortletRequestContext#encodeSharedResourceURL(CharSequence)
	 * @param path
	 *            the URL to encode
	 * @return the encoded url
	 */
	public CharSequence encodeSharedResourceURL(CharSequence path)
	{
		return path;
	}

	/**
	 * Used to override response objects, typically used by the portlet implementation.
	 * <p>
	 * In a Portlet environment, this allows the portlet container/portal to capture the
	 * HeaderResponse as a separate stream for merging in the overall page header (together
	 * with header output for other portlets).
	 * </p>
	 * 
	 * @see PortletRequestContext#getHeaderResponse()
	 * @return The IHeaderResponse
	 */
	public IHeaderResponse getHeaderResponse()
	{
		return null;
	}

	/**
	 * @return boolean true if this is a portlet request
	 */
	public boolean isPortletRequest()
	{
		return false;
	}
}
