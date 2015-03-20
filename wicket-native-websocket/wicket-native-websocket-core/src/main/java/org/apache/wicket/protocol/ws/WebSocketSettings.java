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
package org.apache.wicket.protocol.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.WebSocketResponse;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.concurrent.Executor;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;

/**
 * Web Socket related settings.
 *
 * More documentation is available about each setting in the setter method for the property.
 */
public class WebSocketSettings
{
	private static final MetaDataKey<WebSocketSettings> KEY = new MetaDataKey<WebSocketSettings>()
	{
	};

	/**
	 * Holds this IWebSocketSettings in the Application's metadata.
	 * This way wicket-core module doesn't have reference to wicket-native-websocket.
	 */
	public static final class Holder
	{
		public static WebSocketSettings get(Application application)
		{
			WebSocketSettings settings = application.getMetaData(KEY);
			if (settings == null)
			{
				synchronized (application)
				{
					if (settings == null)
					{
						settings = new WebSocketSettings();
						set(application, settings);
					}
				}
			}
			return settings;
		}

		public static void set(Application application, WebSocketSettings settings)
		{
			application.setMetaData(KEY, settings);
		}
	}

	/**
	 * The executor that handles the processing of Web Socket push message broadcasts.
	 */
	private Executor webSocketPushMessageExecutor = new SameThreadExecutor();

	/**
	 * The executor that handles broadcast of the {@link org.apache.wicket.protocol.ws.api.event.WebSocketPayload}
	 * via Wicket's event bus.
	 */
	private Executor sendPayloadExecutor = new SameThreadExecutor();

	/**
	 * Tracks all currently connected WebSocket clients
	 */
	private IWebSocketConnectionRegistry connectionRegistry = new SimpleWebSocketConnectionRegistry();

	/**
	 * The whitelist of allowed domains where the client can connect to the application from
	 */
    private final List<String> allowedDomains = new ArrayList<String>();

    /**
     * Flag which indicates whether connection filtering should be active or not
     */
    private boolean protectionNeeded = false;

	/**
	 * Set the executor for processing websocket push messages broadcasted to all sessions.
	 * Default executor does all the processing in the caller thread. Using a proper thread pool is adviced
	 * for applications that send push events from ajax calls to avoid page level deadlocks.
	 *
	 * @param executor
	 *            The executor used for processing push messages.
	 */
	public WebSocketSettings setWebSocketPushMessageExecutor(Executor executor)
	{
		Args.notNull(executor, "executor");
		this.webSocketPushMessageExecutor = executor;
		return this;
	}

	/**
	 * The executor for processing websocket push messages broadcasted to all sessions.
	 *
	 * @return
	 *            The executor used for processing push messages.
	 */
	public IWebSocketConnectionRegistry getConnectionRegistry()
	{
		return connectionRegistry;
	}

	public WebSocketSettings setConnectionRegistry(IWebSocketConnectionRegistry connectionRegistry)
	{
		Args.notNull(connectionRegistry, "connectionRegistry");
		this.connectionRegistry = connectionRegistry;
		return this;
	}

	public Executor getWebSocketPushMessageExecutor()
	{
		return webSocketPushMessageExecutor;
	}

	/**
	 * The executor that broadcasts the {@link org.apache.wicket.protocol.ws.api.event.WebSocketPayload}
	 * via Wicket's event bus.
	 * Default executor does all the processing in the caller thread.
	 *
	 * @param sendPayloadExecutor
	 *            The executor used for broadcasting the events with web socket payloads to
	 *            {@link org.apache.wicket.protocol.ws.api.WebSocketBehavior}s and
	 *            {@link org.apache.wicket.protocol.ws.api.WebSocketResource}s.
	 */
	public WebSocketSettings setSendPayloadExecutor(Executor sendPayloadExecutor)
	{
		Args.notNull(sendPayloadExecutor, "sendPayloadExecutor");
		this.sendPayloadExecutor = sendPayloadExecutor;
		return this;
	}

	/**
	 * The executor that broadcasts the {@link org.apache.wicket.protocol.ws.api.event.WebSocketPayload}
	 * via Wicket's event bus.
	 *
	 * @return
	 *            The executor used for broadcasting the events with web socket payloads to
	 *            {@link org.apache.wicket.protocol.ws.api.WebSocketBehavior}s and
	 *            {@link org.apache.wicket.protocol.ws.api.WebSocketResource}s.
	 */
	public Executor getSendPayloadExecutor()
	{
		return sendPayloadExecutor;
	}

    /**
     * Flag that controls whether hijacking protection should be turned on or not
     *
     * @param protectionNeeded
     *            True if protection needed
     */
    public void setHijackingProtectionEnabled(boolean protectionNeeded) {
        this.protectionNeeded = protectionNeeded;
    }

    /**
     * Flag that shows whether hijacking protection is turned on or not
     *
     * @param protectionNeeded
     *            True if protection turned on
     */
    public boolean isHijackingProtectionEnabled() {
        return this.protectionNeeded;
    }

    /**
     * The list of whitelisted domains which are allowed to initiate a websocket connection. This
     * list will be eventually used by the
     * {@link org.apache.wicket.protocol.ws.api.IWebSocketConnectionFilter} to abort potentially
     * unsafe connections. Example domain names might be:
     *
     * <pre>
     *      http://www.example.com
     *      http://ww2.example.com
     * </pre>
     *
     * @param domains
     *            The collection of domains
     */
    public void setAllowedDomains(Collection<String> domains) {
        this.allowedDomains.addAll(domains);
    }

    /**
     * The list of whitelisted domains which are allowed to initiate a websocket connection. This
     * list will be eventually used by the
     * {@link org.apache.wicket.protocol.ws.api.IWebSocketConnectionFilter} to abort potentially
     * unsafe connections
     *
     * @param domains
     *            The collection of domains if or an empty list when no domains were added
     */
    public List<String> getAllowedDomains() {
        return this.allowedDomains;
    }

	/**
	 * A factory method for the {@link org.apache.wicket.request.http.WebResponse}
	 * that should be used to write the response back to the client/browser
	 *
	 * @param connection
	 *              The active web socket connection
	 * @return the response object that should be used to write the response back to the client
	 */
	public WebResponse newWebSocketResponse(IWebSocketConnection connection)
	{
		return new WebSocketResponse(connection);
	}

	/**
	 * A factory method for creating instances of {@link org.apache.wicket.protocol.ws.api.WebSocketRequestHandler}
	 * for processing a web socket request
	 *
	 * @param page
	 *          The page with the web socket client. A dummy page in case of usage of
	 *          {@link org.apache.wicket.protocol.ws.api.WebSocketResource}
	 * @param connection
	 *          The active web socket connection
	 * @return a new instance of WebSocketRequestHandler for processing a web socket request
	 */
	public WebSocketRequestHandler newWebSocketRequestHandler(Page page, IWebSocketConnection connection)
	{
		return new WebSocketRequestHandler(page, connection);
	}

	/**
	 * Simple executor that runs the tasks in the caller thread.
	 */
	public static class SameThreadExecutor implements Executor
	{
		@Override
		public void run(Runnable command)
		{
			command.run();
		}

		@Override
		public <T> T call(Callable<T> callable) throws Exception
		{
			return callable.call();
		}
	}
}
