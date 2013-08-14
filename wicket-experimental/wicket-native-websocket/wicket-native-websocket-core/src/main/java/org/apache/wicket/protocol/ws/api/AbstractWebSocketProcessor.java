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
package org.apache.wicket.protocol.ws.api;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.api.event.WebSocketBinaryPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketClosedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketConnectedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketTextPayload;
import org.apache.wicket.protocol.ws.api.message.BinaryMessage;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base implementation of IWebSocketProcessor. Provides the common logic
 * for registering a web socket connection and broadcasting its events.
 *
 * @since 6.0
 */
public abstract class AbstractWebSocketProcessor implements IWebSocketProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractWebSocketProcessor.class);

	private static final Method GET_FILTER_PATH_METHOD;
	static
	{
		try
		{
			GET_FILTER_PATH_METHOD = WicketFilter.class.getDeclaredMethod("getFilterPath", new Class[]{});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		GET_FILTER_PATH_METHOD.setAccessible(true);
	}

	private final WebRequest webRequest;
	private final int pageId;
	private final Url baseUrl;
	private final WebApplication application;
	private final String sessionId;
	private final IWebSocketConnectionRegistry connectionRegistry;

	/**
	 * Constructor.
	 *
	 * @param request
	 *      the http request that was used to create the TomcatWebSocketProcessor
	 * @param application
	 *      the current Wicket Application
	 */
	public AbstractWebSocketProcessor(final HttpServletRequest request, final WebApplication application)
	{
		this.sessionId = request.getSession(true).getId();

		String pageId = request.getParameter("pageId");
		Checks.notEmpty(pageId, "Request parameter 'pageId' is required!");
		this.pageId = Integer.parseInt(pageId, 10);

		String baseUrl = request.getParameter(WebRequest.PARAM_AJAX_BASE_URL);
		Checks.notNull(baseUrl, String.format("Request parameter '%s' is required!", WebRequest.PARAM_AJAX_BASE_URL));
		this.baseUrl = Url.parse(baseUrl);

		WicketFilter wicketFilter = application.getWicketFilter();
		this.webRequest = new WebSocketRequest(new ServletRequestCopy(request), getFilterPath(wicketFilter));

		this.application = Args.notNull(application, "application");
		IWebSocketSettings webSocketSettings = IWebSocketSettings.Holder.get(application);
		this.connectionRegistry = webSocketSettings.getConnectionRegistry();
	}

	private String getFilterPath(WicketFilter wicketFilter)
	{
		String filterPath;
		try
		{
			filterPath = (String) GET_FILTER_PATH_METHOD.invoke(wicketFilter);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return filterPath;
	}

	@Override
	public void onMessage(final String message)
	{
		broadcastMessage(new TextMessage(message));
	}

	@Override
	public void onMessage(byte[] data, int offset, int length)
	{
		BinaryMessage binaryMessage = new BinaryMessage(data, offset, length);
		broadcastMessage(binaryMessage);
	}

	/**
	 * A helper that registers the opened connection in the application-level
	 * registry.
	 *
	 * @param connection
	 *      the web socket connection to use to communicate with the client
	 * @see #onOpen(Object)
	 */
	protected final void onConnect(final IWebSocketConnection connection)
	{
		connectionRegistry.setConnection(getApplication(), getSessionId(), pageId, connection);
		broadcastMessage(new ConnectedMessage(getApplication(), getSessionId(), pageId));
	}

	@Override
	public void onClose(int closeCode, String message)
	{
		broadcastMessage(new ClosedMessage(getApplication(), getSessionId(), pageId));
		connectionRegistry.removeConnection(getApplication(), getSessionId(), pageId);
	}

	/**
	 * Exports the Wicket thread locals and broadcasts the received message from the client to all
	 * interested components and behaviors in the page with id {@code #pageId}
	 * <p>
	 *     Note: ConnectedMessage and ClosedMessage messages are notification-only. I.e. whatever the
	 *     components/behaviors write in the WebSocketRequestHandler will be ignored because the protocol
	 *     doesn't expect response from the user.
	 * </p>
	 *
	 * @param message
	 *      the message to broadcast
	 */
	public final void broadcastMessage(final IWebSocketMessage message)
	{
		IWebSocketConnection connection = connectionRegistry.getConnection(application, sessionId, pageId);

		if (connection != null && connection.isOpen())
		{
			Application oldApplication = ThreadContext.getApplication();
			Session oldSession = ThreadContext.getSession();
			RequestCycle oldRequestCycle = ThreadContext.getRequestCycle();

			WebSocketResponse webResponse = new WebSocketResponse(connection);
			try
			{
				RequestCycle requestCycle;
				if (oldRequestCycle == null || message instanceof IWebSocketPushMessage)
				{
					RequestCycleContext context = new RequestCycleContext(webRequest, webResponse,
							application.getRootRequestMapper(), application.getExceptionMapperProvider().get());

					requestCycle = application.getRequestCycleProvider().get(context);
					requestCycle.getUrlRenderer().setBaseUrl(baseUrl);
					ThreadContext.setRequestCycle(requestCycle);
				}
				else
				{
					requestCycle = oldRequestCycle;
				}

				ThreadContext.setApplication(application);

				Session session;
				if (oldSession == null || message instanceof IWebSocketPushMessage)
				{
					ISessionStore sessionStore = application.getSessionStore();
					session = sessionStore.lookup(webRequest);
					ThreadContext.setSession(session);
				}
				else
				{
					session = oldSession;
				}

				IPageManager pageManager = session.getPageManager();
				try
				{
					Page page = (Page) pageManager.getPage(pageId);
					WebSocketRequestHandler requestHandler = new WebSocketRequestHandler(page, connection);

					WebSocketPayload payload = createEventPayload(message, requestHandler);

					page.send(application, Broadcast.BREADTH, payload);

					if (!(message instanceof ConnectedMessage || message instanceof ClosedMessage))
					{
						requestHandler.respond(requestCycle);
					}
				}
				finally
				{
					pageManager.commitRequest();
				}
			}
			catch (Exception x)
			{
				LOG.error("An error occurred during processing of a WebSocket message", x);
			}
			finally
			{
				try
				{
					webResponse.close();
				}
				finally
				{
					ThreadContext.setApplication(oldApplication);
					ThreadContext.setRequestCycle(oldRequestCycle);
					ThreadContext.setSession(oldSession);
				}
			}
		}
		else
		{
			LOG.debug("Either there is no connection({}) or it is closed.", connection);
		}
	}

	protected final WebApplication getApplication()
	{
		return application;
	}

	protected final String getSessionId()
	{
		return sessionId;
	}

	private WebSocketPayload createEventPayload(IWebSocketMessage message, WebSocketRequestHandler handler)
	{
		final WebSocketPayload payload;
		if (message instanceof TextMessage)
		{
			payload = new WebSocketTextPayload((TextMessage) message, handler);
		}
		else if (message instanceof BinaryMessage)
		{
			payload = new WebSocketBinaryPayload((BinaryMessage) message, handler);
		}
		else if (message instanceof ConnectedMessage)
		{
			payload = new WebSocketConnectedPayload((ConnectedMessage) message, handler);
		}
		else if (message instanceof ClosedMessage)
		{
			payload = new WebSocketClosedPayload((ClosedMessage) message, handler);
		}
		else if (message instanceof IWebSocketPushMessage)
		{
			payload = new WebSocketPushPayload((IWebSocketPushMessage) message, handler);
		}
		else
		{
			throw new IllegalArgumentException("Unsupported message type: " + message.getClass().getName());
		}
		return payload;
	}
}
