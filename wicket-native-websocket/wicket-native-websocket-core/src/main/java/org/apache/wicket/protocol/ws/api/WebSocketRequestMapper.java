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
package org.apache.wicket.protocol.ws.api;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;

/**
 * An {@link org.apache.wicket.request.IRequestMapper} that is used to set a custom
 * {@link org.apache.wicket.request.IRequestHandler} that broadcasts the
 * {@link org.apache.wicket.protocol.ws.api.event.WebSocketPayload}
 */
class WebSocketRequestMapper implements IRequestMapper
{
	private final IRequestMapper delegate;

	private IRequestHandler handler;

	/**
	 * Constructor.
	 *
	 * @param delegate
	 *          The application root request mapper to delegate Url creation etc.
	 */
	public WebSocketRequestMapper(IRequestMapper delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public IRequestHandler mapRequest(Request request)
	{
		return handler;
	}

	@Override
	public int getCompatibilityScore(Request request)
	{
		return delegate.getCompatibilityScore(request);
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler)
	{
		return delegate.mapHandler(requestHandler);
	}

	/**
	 * Sets the custom request handler
	 *
	 * @param handler
	 *          The request handler that broadcasts the web socket payload
	 */
	public void setHandler(IRequestHandler handler)
	{
		this.handler = handler;
	}
}
