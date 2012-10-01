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
package org.apache.wicket.protocol.http;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.jetty.Jetty9WebSocketProcessor;
import org.eclipse.jetty.websocket.core.api.UpgradeRequest;
import org.eclipse.jetty.websocket.core.api.UpgradeResponse;
import org.eclipse.jetty.websocket.core.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketCreator;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An upgrade filter that uses Jetty9's WebSocketServerFactory to decide whether to upgrade or not.
 */
public class Jetty9WebSocketFilter extends AbstractUpgradeFilter
{
	private static final Logger LOG = LoggerFactory.getLogger(Jetty9WebSocketFilter.class);

	private WebSocketServerFactory _webSocketFactory;

	@Override
	public void init(final boolean isServlet, final FilterConfig filterConfig)
		throws ServletException
	{
		super.init(isServlet, filterConfig);

		try
		{
			WebSocketPolicy serverPolicy = WebSocketPolicy.newServerPolicy();
			String bs = filterConfig.getInitParameter("bufferSize");
			if (bs != null)
				serverPolicy.setBufferSize(Integer.parseInt(bs));
			String max = filterConfig.getInitParameter("maxIdleTime");
			if (max != null)
				serverPolicy.setIdleTimeout(Integer.parseInt(max));

			max = filterConfig.getInitParameter("maxTextMessageSize");
			if (max != null)
				serverPolicy.setMaxTextMessageSize(Integer.parseInt(max));

			max = filterConfig.getInitParameter("maxBinaryMessageSize");
			if (max != null)
				serverPolicy.setMaxBinaryMessageSize(Integer.parseInt(max));
			_webSocketFactory = new WebSocketServerFactory(serverPolicy);

			_webSocketFactory.setCreator(new WebSocketCreator()
			{
				@Override
				public Object createWebSocket(UpgradeRequest upgradeRequest,
					UpgradeResponse upgradeResponse)
				{
					return new Jetty9WebSocketProcessor(upgradeRequest, upgradeResponse,
						getApplication());
				}
			});

			_webSocketFactory.start();
		}
		catch (ServletException x)
		{
			throw x;
		}
		catch (Exception x)
		{
			throw new ServletException(x);
		}
	}

	@Override
	protected boolean acceptWebSocket(HttpServletRequest req, HttpServletResponse resp,
		Application application) throws ServletException, IOException
	{
		return super.acceptWebSocket(req, resp, application) &&
			_webSocketFactory.acceptWebSocket(req, resp);
	}

	/* ------------------------------------------------------------ */
	@Override
	public void destroy()
	{
		try
		{
			if (_webSocketFactory != null)
			{
				_webSocketFactory.stop();
			}
		}
		catch (Exception x)
		{
			LOG.warn("A problem occurred while stopping the web socket factory", x);
		}

		super.destroy();
	}
}
