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

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.IWebSocketConnectionFilter;
import org.apache.wicket.protocol.ws.api.IWebSocketSession;
import org.apache.wicket.protocol.ws.api.IWebSocketSessionConfigurer;
import org.apache.wicket.protocol.ws.api.ServletRequestCopy;
import org.apache.wicket.protocol.ws.api.WebSocketConnectionFilterCollection;
import org.apache.wicket.protocol.ws.api.WebSocketRequest;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.WebSocketResponse;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.concurrent.Executor;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Web Socket related settings.
 *
 * More documentation is available about each setting in the setter method for the property.
 */
public class WebSocketSettings
{
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketSettings.class);

	private static final MetaDataKey<WebSocketSettings> KEY = new MetaDataKey<>()
	{
	};

	/**
	 * A flag indicating whether JavaxWebSocketFilter is in use.
	 * When using JSR356 based implementations the ws:// url should not
	 * use the WicketFilter's filterPath because JSR356 Upgrade connections
	 * are never passed to the Servlet Filters.
	 */
	private static boolean USING_JAVAX_WEB_SOCKET = false;

	static
	{
		try
		{
			Class.forName("org.apache.wicket.protocol.ws.javax.JavaxWebSocketFilter");
			USING_JAVAX_WEB_SOCKET = true;
			LOG.debug("Using JSR356 Native WebSocket implementation!");
		} catch (ClassNotFoundException e)
		{
			LOG.debug("Using non-JSR356 Native WebSocket implementation!");
		}
	}

	private final AtomicReference<CharSequence> filterPrefix = new AtomicReference<>();
	private final AtomicReference<CharSequence> contextPath = new AtomicReference<>();
	private final AtomicReference<CharSequence> baseUrl = new AtomicReference<>();
	private final AtomicInteger port = new AtomicInteger();
	private final AtomicInteger securePort = new AtomicInteger();

	/**
	 * Holds this WebSocketSettings in the Application's metadata.
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
					settings = application.getMetaData(KEY);
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
	private Executor webSocketPushMessageExecutor = new WebSocketPushMessageExecutor();

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
	 * A filter that may reject an incoming connection
	 */
	private IWebSocketConnectionFilter connectionFilter;

	/**
	 * A {@link org.apache.wicket.protocol.ws.api.IWebSocketSessionConfigurer} that allows to configure
	 * {@link org.apache.wicket.protocol.ws.api.IWebSocketSession}s.
	 */
	private IWebSocketSessionConfigurer socketSessionConfigurer = new IWebSocketSessionConfigurer() {
		@Override
		public void configureSession(IWebSocketSession webSocketSession) {
			// does nothing by default
		}
	};

	/**
	 * A function that decides whether to notify the page/resource on
	 * web socket connection closed event.
	 * The page notification leads to deserialization of the page instance from
	 * the page store and sometimes this is not wanted.
	 */
	private Function<Integer, Boolean> notifyOnCloseEvent = (code) -> true;

	/**
	 * Flag that allows to use asynchronous push. By default, it is set to <code>false</code>.
	 */
	private boolean asynchronousPush = false;

	/**
	 * The timeout to use for asynchronous push. By default, it is -1 which means use timeout configured by
	 * server implementation.
	 */
	private long asynchronousPushTimeout = -1;

	public boolean shouldNotifyOnCloseEvent(int closeCode)
	{
		return notifyOnCloseEvent == null || notifyOnCloseEvent.apply(closeCode);
	}

	public void setNotifyOnCloseEvent(Function<Integer, Boolean> notifyOnCloseEvent)
	{
		this.notifyOnCloseEvent = notifyOnCloseEvent;
	}

	/**
	 * A function that decides whether to notify the page/resource on
	 * web socket error event.
	 * The page notification leads to deserialization of the page instance from
	 * the page store and sometimes this is not wanted.
	 */
	private Function<Throwable, Boolean> notifyOnErrorEvent = (throwable) -> true;

	public boolean shouldNotifyOnErrorEvent(Throwable throwable)
	{
		return notifyOnErrorEvent == null || notifyOnErrorEvent.apply(throwable);
	}

	public void setNotifyOnErrorEvent(Function<Throwable, Boolean> notifyOnErrorEvent)
	{
		this.notifyOnErrorEvent = notifyOnErrorEvent;
	}

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
	 * @return the executor for processing websocket push messages broadcasted to all sessions.
	 */
	public Executor getWebSocketPushMessageExecutor()
	{
		return webSocketPushMessageExecutor;
	}

	/**
	 * @return The registry that tracks all currently connected WebSocket clients
	 */
	public IWebSocketConnectionRegistry getConnectionRegistry()
	{
		return connectionRegistry;
	}

	/**
	 * Sets the connection registry
	 *
	 * @param connectionRegistry
	 *              The registry that tracks all currently connected WebSocket clients
	 * @return {@code this}, for method chaining
	 */
	public WebSocketSettings setConnectionRegistry(IWebSocketConnectionRegistry connectionRegistry)
	{
		Args.notNull(connectionRegistry, "connectionRegistry");
		this.connectionRegistry = connectionRegistry;
		return this;
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
	 * Sets the IWebSocketSessionConfigurer
	 * @param socketSessionConfigurer A non-null {@link org.apache.wicket.protocol.ws.api.IWebSocketSessionConfigurer}
	 */
	public void setSocketSessionConfigurer(IWebSocketSessionConfigurer socketSessionConfigurer) {
		Args.notNull(socketSessionConfigurer, "socketSessionConfigurer");
		this.socketSessionConfigurer = socketSessionConfigurer;
	}

	/**
	 * @return returns the {@link org.apache.wicket.protocol.ws.api.IWebSocketSessionConfigurer}
	 */
	public IWebSocketSessionConfigurer getSocketSessionConfigurer() {
		return socketSessionConfigurer;
	}

	/**
	 * Sets the filter for checking the incoming connections
	 *
	 * @param connectionFilter
	 *              the filter for checking the incoming connections
	 * @see WebSocketConnectionFilterCollection
	 */
	public void setConnectionFilter(IWebSocketConnectionFilter connectionFilter)
	{
		this.connectionFilter = connectionFilter;
	}

	/**
	 * @return the filter for checking the incoming connections
	 * @see WebSocketConnectionFilterCollection
	 */
	public IWebSocketConnectionFilter getConnectionFilter()
	{
		return this.connectionFilter;
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
		return newWebSocketResponse(connection, isAsynchronousPush(), getAsynchronousPushTimeout());
	}

	/**
	 * A factory method for the {@link org.apache.wicket.request.http.WebResponse}
	 * that should be used to write the response back to the client/browser
	 *
	 * @param connection
	 *              The active web socket connection
	 * @param asynchronousPush
	 *              Whether asynchronous push is wanted or not.
     * @param timeout
     *              The timeout to be used for push operations
	 * @return the response object that should be used to write the response back to the client
	 */
	public WebResponse newWebSocketResponse(IWebSocketConnection connection, boolean asynchronousPush, long timeout)
	{
		return new WebSocketResponse(connection, asynchronousPush, timeout);
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
	 * A factory method for the {@link org.apache.wicket.request.http.WebRequest}
	 * that should be used in the WebSocket processing request cycle
	 *
	 * @param request
	 *              The upgraded http request
	 * @param filterPath
	 *              The configured filter path of WicketFilter in web.xml
	 * @return the request object that should be used in the WebSocket processing request cycle
	 */
	public WebRequest newWebSocketRequest(HttpServletRequest request, String filterPath)
	{
		return new WebSocketRequest(new ServletRequestCopy(request), filterPath);
	}

	public void setFilterPrefix(final CharSequence filterPrefix)
	{
		this.filterPrefix.set(filterPrefix);
	}

	public CharSequence getFilterPrefix()
	{
		if (filterPrefix.get() == null)
		{
			if (USING_JAVAX_WEB_SOCKET)
			{
				filterPrefix.compareAndSet(null, "");
			}
			else
			{
				filterPrefix.compareAndSet(null, RequestCycle.get().getRequest().getFilterPath());
			}
		}
		return filterPrefix.get();
	}

	public void setContextPath(final CharSequence contextPath)
	{
		this.contextPath.set(contextPath);
	}

	public CharSequence getContextPath()
	{
		contextPath.compareAndSet(null, RequestCycle.get().getRequest().getContextPath());
		return contextPath.get();
	}

	public void setBaseUrl(final CharSequence baseUrl)
	{
		this.baseUrl.set(baseUrl);
	}

	public CharSequence getBaseUrl()
	{
		if (baseUrl.get() == null)
		{
			Url _baseUrl = RequestCycle.get().getUrlRenderer().getBaseUrl();
			return Strings.escapeMarkup(_baseUrl.toString());
		}
		return baseUrl.get();
	}

	/**
	 * Sets the port that should be used for <code>ws:</code> connections.
	 * If unset then the current HTTP port will be used.
	 *
	 * @param wsPort The custom port for WS connections
	 */
	public void setPort(int wsPort)
	{
		this.port.set(wsPort);
	}

	/**
	 * @return The custom port for WS connections
	 */
	public Integer getPort()
	{
		return port.get();
	}

	/**
	 * Sets the port that should be used for <code>wss:</code> connections.
	 * If unset then the current HTTPS port will be used.
	 *
	 * @param wssPort The custom port for WSS connections
	 */
	public void setSecurePort(int wssPort)
	{
		this.securePort.set(wssPort);
	}

	/**
	 * @return The custom port for WSS connections
	 */
	public Integer getSecurePort()
	{
		return securePort.get();
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
	}

	public static class WebSocketPushMessageExecutor implements Executor
	{
		/**
		 * An executor that should be used when the WebSocket message is pushed
		 * from non-http worker thread.
		 */
		private final java.util.concurrent.Executor nonHttpRequestExecutor;

		/**
		 * An executor that is used when the WebSocket push is initiated in
		 * http worker thread. In this case the WebSocket processing should be
		 * off-loaded to a different thread that should wait for the page instance
		 * lock.
		 */
		private final java.util.concurrent.Executor httpRequestExecutor;

		/**
		 * For non-http worker threads pushes the WebSocket runnable in the same request.
		 * For http worker threads uses an elastic thread pool of 1-8 threads.
		 *
		 * Use {@link WebSocketPushMessageExecutor#WebSocketPushMessageExecutor(java.util.concurrent.Executor, java.util.concurrent.Executor)}
		 * for custom behavior and/or settings
		 */
		public WebSocketPushMessageExecutor()
		{
			this(Runnable::run, new ThreadPoolExecutor(1, 8,
			                                           60L, TimeUnit.SECONDS,
			                                           new SynchronousQueue<>(),
			                                           new ThreadFactory()));
		}

		public WebSocketPushMessageExecutor(java.util.concurrent.Executor nonHttpRequestExecutor, java.util.concurrent.Executor httpRequestExecutor)
		{
			this.nonHttpRequestExecutor = nonHttpRequestExecutor;
			this.httpRequestExecutor = httpRequestExecutor;
		}

		@Override
		public void run(final Runnable command)
		{
			if (RequestCycle.get() != null)
			{
				httpRequestExecutor.execute(command);
			}
			else
			{
				nonHttpRequestExecutor.execute(command);
			}
		}
	}

	public static class ThreadFactory implements java.util.concurrent.ThreadFactory
	{
		private final AtomicInteger counter = new AtomicInteger();

		@Override
		public Thread newThread(final Runnable r)
		{
			return new Thread(r, "Wicket-WebSocket-HttpRequest-Thread-" + counter.getAndIncrement());
		}
	}

	public void setAsynchronousPush(boolean asynchronousPush)
	{
		this.asynchronousPush = asynchronousPush;
	}

	public boolean isAsynchronousPush()
	{
		return asynchronousPush;
	}

	public void setAsynchronousPushTimeout(long asynchronousPushTimeout)
	{
		this.asynchronousPushTimeout = asynchronousPushTimeout;
	}

	public long getAsynchronousPushTimeout()
	{
		return asynchronousPushTimeout;
	}
}
