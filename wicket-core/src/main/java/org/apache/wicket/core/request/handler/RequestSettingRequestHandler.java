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
package org.apache.wicket.core.request.handler;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;

/**
 * A {@link IRequestHandler} that sets the current {@link Request} before delegating the responding
 * to a wrapped request handler. This is useful when the request received from the browser is not the same
 * request used to respond, like when the request mapper clones the request with a new URL.
 *
 * @author Jesse Long
 */
public class RequestSettingRequestHandler implements IRequestHandlerDelegate
{
	private final Request request;
	private final IRequestHandler delegate;

	/**
	 * Creates a new instance
	 * @param request
	 *      The request to use when responding
	 * @param delegate
	 *      The request handler to delegate responding to
	 */
	public RequestSettingRequestHandler(Request request, IRequestHandler delegate)
	{
		this.request = Args.notNull(request, "request");
		this.delegate = Args.notNull(delegate,"delegate");
	}

	/**
	 * Returns the request that will be set before responding.
	 * @return the request that will be set before responding.
	 */
	public Request getRequest()
	{
		return request;
	}

	/**
	 * Returns the request handler to which responding will be delegated.
	 * @return the request handler to which responding will be delegated.
	 */
	@Override
	public IRequestHandler getDelegateHandler()
	{
		return delegate;
	}

	@Override
	public void respond(IRequestCycle requestCycle)
	{
		RequestCycle cycle = (RequestCycle) requestCycle;
		Request originalRequest = cycle.getRequest();
		try
		{
			cycle.setRequest(request);
			delegate.respond(requestCycle);
		}
		finally
		{
			cycle.setRequest(originalRequest);
		}
	}

	@Override
	public void detach(IRequestCycle requestCycle)
	{
		delegate.detach(requestCycle);
	}
}
