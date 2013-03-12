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
package org.apache.wicket.protocol.ws.jetty9;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.server.ServletWebSocketRequest;

/**
 * An HttpServletRequest that wraps the original HttpServletRequest
 * hidden hard by Jetty 9.x UpgradeRequest.
 */
class Jetty9UpgradeHttpRequest extends HttpServletRequestWrapper
{
	private static final Field REQ;
	static
	{
		try
		{
			REQ = ServletWebSocketRequest.class.getDeclaredField("req");
		} catch (NoSuchFieldException nsfx)
		{
			throw new IllegalStateException(ServletWebSocketRequest.class.getName() +
					" has no 'req' field!", nsfx);
		}
		REQ.setAccessible(true);
	}

	Jetty9UpgradeHttpRequest(UpgradeRequest upgradeRequest)
	{
		super(extractHttpRequest(upgradeRequest));
	}

	private static HttpServletRequest extractHttpRequest(UpgradeRequest upgradeRequest)
	{
		if (upgradeRequest instanceof ServletWebSocketRequest == false)
		{
			throw new IllegalArgumentException(Jetty9UpgradeHttpRequest.class.getName() +
					" can work only with " + ServletWebSocketRequest.class.getName());
		}

		ServletWebSocketRequest servletWebSocketRequest = (ServletWebSocketRequest) upgradeRequest;
		HttpServletRequest request;
		try
		{
			request = (HttpServletRequest) REQ.get(servletWebSocketRequest);
		}
		catch (IllegalAccessException iax)
		{
			throw new IllegalStateException("Cannot get the HttpServletRequest after the protocol upgrade", iax);
		}

		return request;
	}

}
