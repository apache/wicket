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

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.ws.AbstractUpgradeFilter;
import org.apache.wicket.util.string.Strings;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * An upgrade filter that setups javax.websocket
 */
public class JavaxWebSocketFilter extends AbstractUpgradeFilter
{
	public JavaxWebSocketFilter()
	{
		super();
	}

	public JavaxWebSocketFilter(WebApplication application)
	{
		super(application);
	}

	@Override
	public void init(final boolean isServlet, final FilterConfig filterConfig) throws ServletException
	{
		super.init(isServlet, new JavaxWebSocketFilterConfig(filterConfig));
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
					result = WicketServerEndpointConfig.WICKET_WEB_SOCKET_PATH;
				}
				else
				{
					result = result + ',' + WicketServerEndpointConfig.WICKET_WEB_SOCKET_PATH;
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
}
