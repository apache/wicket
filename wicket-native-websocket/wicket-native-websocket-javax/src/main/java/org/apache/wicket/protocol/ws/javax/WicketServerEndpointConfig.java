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
package org.apache.wicket.protocol.ws.javax;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * A ServerEndpointConfig that uses custom Configurator to collect
 * all available information from the passed HandshakeRequest
 */
class WicketServerEndpointConfig implements ServerEndpointConfig
{
	/**
	 * A fake mount path used for WebSocket endpoint.
	 * WicketFilter should not process this path.
	 * @see org.apache.wicket.protocol.http.WicketFilter#ignorePaths
	 */
	static final String WICKET_WEB_SOCKET_PATH = "/wicket/websocket";

	private final ServerEndpointConfig delegate;

	private Configurator configurator;

	WicketServerEndpointConfig()
	{
		this.delegate = ServerEndpointConfig.Builder.create(WicketEndpoint.class, WICKET_WEB_SOCKET_PATH).build();
	}

	@Override
	public Class<?> getEndpointClass()
	{
		return delegate.getEndpointClass();
	}

	@Override
	public String getPath()
	{
		return delegate.getPath();
	}

	@Override
	public List<String> getSubprotocols()
	{
		return delegate.getSubprotocols();
	}

	@Override
	public List<Extension> getExtensions()
	{
		return delegate.getExtensions();
	}

	@Override
	public Configurator getConfigurator()
	{
		if (configurator == null)
		{
			configurator = new JavaxWebSocketConfigurator(delegate.getConfigurator());
		}
		return configurator;
	}

	@Override
	public List<Class<? extends Encoder>> getEncoders()
	{
		return delegate.getEncoders();
	}

	@Override
	public List<Class<? extends Decoder>> getDecoders()
	{
		return delegate.getDecoders();
	}

	@Override
	public Map<String, Object> getUserProperties()
	{
		return delegate.getUserProperties();
	}

	/**
	 * A custom Configurator that collects all available information from the HandshakeRequest
	 */
	private static class JavaxWebSocketConfigurator extends ServerEndpointConfig.Configurator
	{
		private final ServerEndpointConfig.Configurator delegate;

		public JavaxWebSocketConfigurator(ServerEndpointConfig.Configurator delegate)
		{
			this.delegate = delegate;
		}

		@Override
		public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response)
		{
			delegate.modifyHandshake(sec, request, response);

			// do not store null keys/values because Tomcat 8 uses ConcurrentMap for UserProperties

			Map<String, Object> userProperties = sec.getUserProperties();
			Object httpSession = request.getHttpSession();
			if (httpSession != null)
			{
				userProperties.put("session", httpSession);
			}

			Map<String, List<String>> headers = request.getHeaders();
			if (headers != null)
			{
				userProperties.put("headers", headers);
			}


			Map<String, List<String>> parameterMap = request.getParameterMap();
			if (parameterMap != null)
			{
				userProperties.put("parameterMap", parameterMap);
			}


			String queryString = request.getQueryString();
			if (queryString != null)
			{
				userProperties.put("queryString", queryString);
			}


			URI requestURI = request.getRequestURI();
			if (requestURI != null)
			{
				userProperties.put("requestURI", requestURI);
			}

			Principal userPrincipal = request.getUserPrincipal();
			if (userPrincipal != null)
			{
				userProperties.put("userPrincipal", userPrincipal);
			}
		}

		@Override
		public String getNegotiatedSubprotocol(List<String> supported, List<String> requested)
		{
			return delegate.getNegotiatedSubprotocol(supported, requested);
		}

		@Override
		public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested)
		{
			return delegate.getNegotiatedExtensions(installed, requested);
		}

		@Override
		public boolean checkOrigin(String originHeaderValue)
		{
			return delegate.checkOrigin(originHeaderValue);
		}

		@Override
		public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException
		{
			return delegate.getEndpointInstance(endpointClass);
		}
	}
}
