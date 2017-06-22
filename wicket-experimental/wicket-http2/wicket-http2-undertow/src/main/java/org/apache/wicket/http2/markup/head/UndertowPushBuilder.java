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

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to push resources with the Undertow 2+ specific push builder API
 * 
 * @author Tobias Soloschenko
 */
public class UndertowPushBuilder implements PushBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(UndertowPushBuilder.class);

	@Override
	public void push(HttpServletRequest httpServletRequest, String... paths)
	{
		Request request = RequestCycle.get().getRequest();
		HttpServletRequest httpRequest = (HttpServletRequest) request.getContainerRequest();
		io.undertow.servlet.spec.HttpServletRequestImpl undertowRequest = (io.undertow.servlet.spec.HttpServletRequestImpl) httpRequest;
		// Added explicit cast here to ensure this is the implementation of undertow
		io.undertow.servlet.spec.PushBuilderImpl pushBuilder = (io.undertow.servlet.spec.PushBuilderImpl)undertowRequest.getPushBuilder();
		if (pushBuilder != null)
		{
			for (String path : paths)
			{
				pushBuilder.path(path).push();
			}
		}
		else
		{
			LOG.warn("Attempted to use HTTP2 Push but it is not supported for the current request: {}!",
					httpRequest);
		}
	}
}
