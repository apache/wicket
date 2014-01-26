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
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.websocket.Decoder;
import javax.websocket.DeploymentException;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.ws.AbstractUpgradeFilter;
import org.apache.wicket.util.string.Strings;

/**
 * An upgrade filter that setups javax.websocket
 */
public class JavaxWebSocketFilter extends AbstractUpgradeFilter
{
	/**
	 * A fake mount path used for WebSocket endpoint.
	 * WicketFilter should not process this path.
	 * @see WicketFilter#ignorePaths
	 */
	private static final String WICKET_WEB_SOCKET_PATH = "/wicket/websocket";

	/**
	 * A key used to store the application object in WebSocket's Endpoint user properties
	 */
	static final String APPLICATION_KEY = "wicket.application";

	@Override
	public void init(final boolean isServlet, final FilterConfig filterConfig) throws ServletException
	{
		super.init(isServlet, new JavaxWebSocketFilterConfig(filterConfig));

		try
		{
			ServerEndpointConfig config = new WicketServerEndpointConfig(ServerEndpointConfig.Builder.create(WicketEndpoint.class, WICKET_WEB_SOCKET_PATH).build());
			config.getUserProperties().put(APPLICATION_KEY, getApplication());

			ServletContext servletContext = filterConfig.getServletContext();
			ServerContainer sc = (ServerContainer) servletContext.getAttribute(ServerContainer.class.getName());
			sc.addEndpoint(config);
		}
		catch (DeploymentException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * A wrapper of the passed FilterConfig in #init() that adds #WICKET_WEB_SOCKET_PATH to
	 * the list of ignored paths
	 */
	private static class JavaxWebSocketFilterConfig implements FilterConfig
	{
		private final FilterConfig delegate;

		private JavaxWebSocketFilterConfig(FilterConfig delegate)
		{
			this.delegate = delegate;
		}

		@Override
		public String getFilterName()
		{
			return delegate.getFilterName();
		}

		@Override
		public ServletContext getServletContext()
		{
			return delegate.getServletContext();
		}

		@Override
		public String getInitParameter(String s)
		{
			String result = delegate.getInitParameter(s);

			if (WicketFilter.IGNORE_PATHS_PARAM.equalsIgnoreCase(s))
			{
				if (Strings.isEmpty(result))
				{
					result = WICKET_WEB_SOCKET_PATH;
				}
				else
				{
					result = result + ',' + WICKET_WEB_SOCKET_PATH;
				}
			}

			return result;
		}

		@Override
		public Enumeration<String> getInitParameterNames()
		{
			return delegate.getInitParameterNames();
		}
	}

	/**
	 * A ServerEndpointConfig that uses custom Configurator to collect
	 * all available information from the passed HandshakeRequest
	 */
	private static class WicketServerEndpointConfig implements ServerEndpointConfig
	{
		private final ServerEndpointConfig delegate;
		private Configurator configurator;

		private WicketServerEndpointConfig(ServerEndpointConfig delegate)
		{
			this.delegate = delegate;
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
			return super.getEndpointInstance(endpointClass);
		}
	}
}
