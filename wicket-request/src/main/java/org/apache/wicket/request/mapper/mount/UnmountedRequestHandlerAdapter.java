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
package org.apache.wicket.request.mapper.mount;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;

/**
 * Adapts a singleton {@link IRequestHandler} instance to {@link IMountedRequestMapper}
 * 
 * TODO javadoc
 * 
 * @author igor.vaynberg
 */
class UnmountedRequestHandlerAdapter implements IMountedRequestMapper
{
	private final IRequestHandler handler;

	/**
	 * Construct.
	 * 
	 * @param handler
	 */
	public UnmountedRequestHandlerAdapter(final IRequestHandler handler)
	{
		this.handler = handler;
	}

	/**
	 * @see org.apache.wicket.request.mapper.mount.IMountedRequestMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	@Override
	public int getCompatibilityScore(final Request request)
	{
		return 0;
	}

	/**
	 * @see org.apache.wicket.request.mapper.mount.IMountedRequestMapper#mapHandler(org.apache.org.apache.wicket.request.IRequestHandler)
	 */
	@Override
	public Mount mapHandler(final IRequestHandler requestHandler)
	{
		if (requestHandler.equals(handler))
		{
			return new Mount(new Url());
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.request.mapper.mount.IMountedRequestMapper#mapRequest(org.apache.wicket.request.Request,
	 *      org.apache.wicket.request.mapper.mount.MountParameters)
	 */
	@Override
	public IRequestHandler mapRequest(final Request request, final MountParameters mountParams)
	{
		return handler;
	}
}
