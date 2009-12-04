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
package org.apache.wicket.ng.protocol.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.wicket.ng.Application;
import org.apache.wicket.ng.request.RequestMapper;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.render.RenderPageRequestHandlerDelegate;
import org.apache.wicket.ng.request.handler.impl.render.WebRenderPageRequestHandlerDelegate;
import org.apache.wicket.ng.request.mapper.SystemMapper;
import org.apache.wicket.ng.session.HttpSessionStore;
import org.apache.wicket.ng.session.SessionStore;
import org.apache.wicket.protocol.http.BufferedWebResponse;

/**
 * 
 * @author Matej Knopp
 * 
 */
public abstract class WebApplication extends Application
{

	public WebApplication()
	{
		super();
	}

	@Override
	protected void registerDefaultEncoders()
	{
		registerEncoder(new SystemMapper());
	}

	public void mount(RequestMapper encoder)
	{
		registerEncoder(encoder);
	}

	// TODO: Do this properly
	private final Map<String, BufferedWebResponse> storedResponses = new ConcurrentHashMap<String, BufferedWebResponse>();

	public boolean hasBufferedResponse(String sessionId, Url url)
	{
		String key = sessionId + url.toString();
		return storedResponses.containsKey(key);
	}

	public BufferedWebResponse getAndRemoveBufferedResponse(String sessionId, Url url)
	{
		String key = sessionId + url.toString();
		return storedResponses.remove(key);
	}

	public void storeBufferedResponse(String sessionId, Url url, BufferedWebResponse response)
	{
		String key = sessionId + url.toString();
		storedResponses.put(key, response);
	}

	@Override
	protected SessionStore newSessionStore()
	{
		return new HttpSessionStore(this);
	}

	/**
	 * Gets the servlet context for this application. Use this to get references to absolute paths,
	 * global web.xml parameters (&lt;context-param&gt;), etc.
	 * 
	 * @return The servlet context for this application
	 */
	public ServletContext getServletContext()
	{
		if (wicketFilter != null)
		{
			return wicketFilter.getFilterConfig().getServletContext();
		}
		throw new IllegalStateException("servletContext is not set yet. Any code in your"
			+ " Application object that uses the wicket filter instance should be put"
			+ " in the init() method instead of your constructor");
	}

	public void setWicketFilter(WicketFilter wicketFilter)
	{
		this.wicketFilter = wicketFilter;
	}

	private WicketFilter wicketFilter;

	@Override
	public RenderPageRequestHandlerDelegate getRenderPageRequestHandlerDelegate(
		RenderPageRequestHandler renderPageRequestHandler)
	{
		return new WebRenderPageRequestHandlerDelegate(renderPageRequestHandler);
	}

	public static WebApplication get()
	{
		return (WebApplication)Application.get();
	}
}
