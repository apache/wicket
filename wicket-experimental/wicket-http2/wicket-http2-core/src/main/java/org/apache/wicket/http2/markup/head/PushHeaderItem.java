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
package org.apache.wicket.http2.markup.head;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.http2.Http2Settings;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.collections.ConcurrentHashSet;

/**
 * A push header item to be used in the http/2 context and to reduce the latency of the web
 * application
 * 
 * @author Tobias Soloschenko
 *
 */
public class PushHeaderItem extends HeaderItem
{
	private static final long serialVersionUID = 1L;

	/**
	 * The http2 protocol string
	 */
	public static final String HTTP2_PROTOCOL = "http/2";

	/**
	 * The token suffix to be used in this header item
	 */
	private static final String TOKEN_SUFFIX = HTTP2_PROTOCOL + "_pushed";

	/**
	 * The URLs of resources to be pushed to the client
	 */
	private Set<String> urls = new ConcurrentHashSet<>(new TreeSet<>());

	/**
	 * Uses the URLs that has already been pushed to the client to ensure not to push them again
	 */
	@Override
	public Iterable<?> getRenderTokens()
	{
		Set<String> tokens = new TreeSet<String>();
		for (String url : urls)
		{
			tokens.add(url + TOKEN_SUFFIX);
		}
		return tokens;
	}

	/**
	 * Pushes the previously created URLs to the client
	 */
	@Override
	public void render(Response response)
	{
		HttpServletRequest request = getContainerRequest(RequestCycle.get().getRequest());
		// Check if the protocol is http/2 or http/2.0 to only push the resources in this case
		if (isHttp2(request))
		{
			Http2Settings http2Settings = Http2Settings.Holder.get(Application.get());
			PushBuilder pushBuilder = http2Settings.getPushBuilder();
			pushBuilder.push(request, urls.toArray(new String[urls.size()]));
		}
	}

	/**
	 * Creates a URL and pushes the resource to the client - this is only supported if http2 is
	 * enabled
	 * 
	 * @param pushItems
	 *            a list of items to be pushed to the client
	 * @return the current push header item
	 */
	@SuppressWarnings("unchecked")
	public PushHeaderItem push(List<PushItem> pushItems)
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (isHttp2(getContainerRequest(requestCycle.getRequest())))
			for (PushItem pushItem : pushItems)
			{
				Object object = pushItem.getObject();
				PageParameters parameters = pushItem.getPageParameters();

				if (object == null)
				{
					throw new WicketRuntimeException(
					    "Please provide an object to the items to be pushed, so that the url can be created for the given resource.");
				}

				CharSequence url = null;
				if (object instanceof ResourceReference)
				{
					url = requestCycle.urlFor((ResourceReference)object, parameters);
				}
				else if (object instanceof Class)
				{
					url = requestCycle.urlFor((Class<? extends Page>)object, parameters);
				}
				else if (object instanceof IRequestHandler)
				{
					url = requestCycle.urlFor((IRequestHandler)object);
				}
				else
				{
					Url encoded = new PageParametersEncoder().encodePageParameters(parameters);
					String queryString = encoded.getQueryString();
					url = object.toString() + (queryString != null ? "?" + queryString : "");
				}

				if (url.toString().equals("."))
				{
					url = "/";
				}
				else if (url.toString().startsWith("."))
				{
					url = url.toString().substring(1);
				}

				urls.add(url.toString());
			}
		return this;
	}

	/**
	 * Gets the container request
	 * 
	 * @param request
	 *            the wicket request to get the container request from
	 * @return the container request
	 */
	public HttpServletRequest getContainerRequest(Request request)
	{

		return checkHttpServletRequest(request);
	}

	/**
	 * Checks if the given request is a http/2 request
	 * 
	 * @param request
	 *            the request to check if it is a http/2 request
	 * @return if the request is a http/2 request
	 */
	public boolean isHttp2(HttpServletRequest request)
	{
		// detects http/2 and http/2.0
		return request.getProtocol().toLowerCase().contains(HTTP2_PROTOCOL);
	}

	/**
	 * Checks if the container request from the given request is instance of
	 * {@link HttpServletRequest} if not the API of the PushHeaderItem can't be used and a
	 * {@link WicketRuntimeException} is thrown.
	 * 
	 * @param request
	 *            the request to get the container request from. The container request is checked if it
	 *            is instance of {@link HttpServletRequest}
	 * @return the container request get from the given request casted to {@link HttpServletRequest}
	 * @throw {@link WicketRuntimeException} if the container request is not a
	 *        {@link HttpServletRequest}
	 */
	public HttpServletRequest checkHttpServletRequest(Request request)
	{
		Object assumedHttpServletRequest = request.getContainerRequest();
		if (!(assumedHttpServletRequest instanceof HttpServletRequest))
		{
			throw new WicketRuntimeException(
			    "The request is not a HttpServletRequest - the usage of PushHeaderItem is not support in the current environment: "
			        + request.getClass().getName());
		}
		return (HttpServletRequest)assumedHttpServletRequest;
	}
}
