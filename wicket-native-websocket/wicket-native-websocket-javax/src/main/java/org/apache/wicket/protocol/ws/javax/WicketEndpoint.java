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

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.apache.wicket.Application;
import org.apache.wicket.IApplicationListener;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSR 356 WebSocket Endpoint that integrates with Wicket Native WebSocket's IWebSocketProcessor
 */
public class WicketEndpoint extends Endpoint
{
	private static final Logger LOG = LoggerFactory.getLogger(WicketEndpoint.class);

	/**
	 * A set of started applications for which this endpoint is registered.
	 */
	private static final Set<String> RUNNING_APPLICATIONS = ConcurrentHashMap.newKeySet();

	/**
	 * The name of the request parameter that holds the application name
	 */
	private static final String WICKET_APP_PARAM_NAME = "wicket-app-name";

	private JavaxWebSocketProcessor javaxWebSocketProcessor;

	@Override
	public void onOpen(Session session, EndpointConfig endpointConfig)
	{
		String appName = getApplicationName(session);

		WebApplication app = (WebApplication) WebApplication.get(appName);
		if (RUNNING_APPLICATIONS.add(appName))
		{
			app.getApplicationListeners().add(new ApplicationListener());
		}

		try
		{
			ThreadContext.setApplication(app);
			javaxWebSocketProcessor = new JavaxWebSocketProcessor(session, app, endpointConfig);
			javaxWebSocketProcessor.onOpen(new JavaxWebSocketSession(session), app);
		}
		finally
		{
			ThreadContext.detach();
		}
	}

	@Override
	public void onClose(Session session, CloseReason closeReason)
	{
		super.onClose(session, closeReason);

		final int closeCode = closeReason.getCloseCode().getCode();
		final String reasonPhrase = closeReason.getReasonPhrase();

		LOG.debug("Web Socket connection with id '{}' has been closed with code '{}' and reason: {}",
				session.getId(), closeCode, reasonPhrase);

		String applicationName = getApplicationName(session);
		if (isApplicationAlive(applicationName) && javaxWebSocketProcessor != null)
		{
			javaxWebSocketProcessor.onClose(closeCode, reasonPhrase);
		}
	}

	@Override
	public void onError(Session session, Throwable t)
	{
		if (isIgnorableError(t))
		{
			LOG.debug("An error occurred in web socket connection with id : {}", session.getId(), t);
		}
		else
		{
			LOG.error("An error occurred in web socket connection with id : {}", session.getId(), t);
		}

		super.onError(session, t);

		String applicationName = getApplicationName(session);
		if (isApplicationAlive(applicationName) && javaxWebSocketProcessor != null)
		{
			javaxWebSocketProcessor.onError(t);
		}
	}

	private boolean isIgnorableError(Throwable t)
	{
		return
			t instanceof EOFException ||
		    (t instanceof IOException && "Broken pipe".equals(t.getMessage()));
	}

	private boolean isApplicationAlive(String appName)
	{
		return RUNNING_APPLICATIONS.contains(appName);
	}

	private String getApplicationName(Session session)
	{
		String appName = null;

		Map<String, List<String>> parameters = session.getRequestParameterMap();
		if (parameters != null)
		{
			appName = parameters.get(WICKET_APP_PARAM_NAME).get(0);
		}
		else
		{
			// Glassfish 4 has null parameters map and non-null query string ...
			String queryString = session.getQueryString();
			if (!Strings.isEmpty(queryString))
			{
				String[] params = Strings.split(queryString, '&');
				for (String paramPair : params)
				{
					String[] nameValues = Strings.split(paramPair, '=');
					if (WICKET_APP_PARAM_NAME.equals(nameValues[0]))
					{
						appName = nameValues[1];
					}
				}
			}
		}

		Checks.notNull(appName, "The application name cannot be read from the upgrade request's parameters");

		return appName;
	}

	private static class ApplicationListener implements IApplicationListener
	{
		@Override
		public void onBeforeDestroyed(Application application)
		{
			String appName = application.getName();
			RUNNING_APPLICATIONS.remove(appName);
			application.getApplicationListeners().remove(this);
		}
	}
}
