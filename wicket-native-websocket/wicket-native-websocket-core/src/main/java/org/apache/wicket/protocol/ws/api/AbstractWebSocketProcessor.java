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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.event.WebSocketAbortedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketBinaryPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketClosedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketConnectedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketErrorPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketTextPayload;
import org.apache.wicket.protocol.ws.api.message.AbortedMessage;
import org.apache.wicket.protocol.ws.api.message.BinaryMessage;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.ErrorMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.protocol.ws.api.registry.ResourceNameKey;
import org.apache.wicket.protocol.ws.api.registry.ResourceNameTokenKey;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;
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

	/**
	 * A pageId indicating that the endpoint is WebSocketResource
	 */
	static final int NO_PAGE_ID = -1;

	/**
	 * The reason given to a client whose shared {@link WebSocketResource} the
	 * {@link IAuthorizationStrategy} refused. It is sent with close code
	 * {@value WebSocketConnectionOriginFilter#POLICY_VIOLATION_ERROR_CODE}, the policy violation
	 * code of RFC 6455, which is the one a rejected connection already uses.
	 */
	static final String NOT_AUTHORIZED = "Not authorized";

	/**
	 * The reason given to a client whose HTTP session has ended. It is sent with close code
	 * {@value WebSocketConnectionOriginFilter#POLICY_VIOLATION_ERROR_CODE}, the policy violation
	 * code of RFC 6455, which is the one a rejected connection already uses.
	 */
	static final String SESSION_ENDED = "Session ended";

	private final WebRequest webRequest;
	private final int pageId;
	private final String context;
	private final String resourceName;
	private final String connectionToken;
	private final Url baseUrl;
	private final WebApplication application;
	private final String sessionId;
	private final WebSocketSettings webSocketSettings;
	private final IWebSocketConnectionRegistry connectionRegistry;
	private final IWebSocketConnectionFilter connectionFilter;
	private final HttpServletRequest servletRequest;

	/**
	 * Constructor.
	 *
	 * @param request
	 *      the http request that was used to create this {@link IWebSocketProcessor}
	 * @param application
	 *      the current Wicket Application
	 */
	public AbstractWebSocketProcessor(final HttpServletRequest request, final WebApplication application)
	{
		final HttpSession httpSession = request.getSession(true);
		if (httpSession == null)
		{
			throw new IllegalStateException("There is no HTTP Session bound. Without a session Wicket won't be " +
					"able to find the stored page to update its components");
		}
		this.sessionId = httpSession.getId();
		String pageId = request.getParameter("pageId");
		this.context = request.getParameter("context");
		this.resourceName = request.getParameter("resourceName");
		this.connectionToken = request.getParameter("connectionToken");
		if (Strings.isEmpty(pageId) && Strings.isEmpty(resourceName))
		{
			throw new IllegalArgumentException("The request should have either 'pageId' or 'resourceName' parameter!");
		}
		if (Strings.isEmpty(pageId) == false)
		{
			this.pageId = Integer.parseInt(pageId, 10);
		}
		else
		{
			this.pageId = NO_PAGE_ID;
		}

		String baseUrl = request.getParameter(WebRequest.PARAM_AJAX_BASE_URL);
		Checks.notNull(baseUrl, String.format("Request parameter '%s' is required!", WebRequest.PARAM_AJAX_BASE_URL));
		this.baseUrl = Url.parse(baseUrl);

		WicketFilter wicketFilter = application.getWicketFilter();
		this.servletRequest = new ServletRequestCopy(request);

		this.application = Args.notNull(application, "application");

		this.webSocketSettings = WebSocketSettings.Holder.get(application);

		this.webRequest = webSocketSettings.newWebSocketRequest(request, wicketFilter.getFilterPath());

		this.connectionRegistry = webSocketSettings.getConnectionRegistry();

		this.connectionFilter = webSocketSettings.getConnectionFilter();
	}

	@Override
	public void onMessage(final String message)
	{
		broadcastMessage(new TextMessage(getApplication(), getSessionId(), getRegistryKey(), message));
	}

	@Override
	public void onMessage(byte[] data, int offset, int length)
	{
		BinaryMessage binaryMessage = new BinaryMessage(getApplication(), getSessionId(), getRegistryKey(), data, offset, length);
		broadcastMessage(binaryMessage);
	}

	/**
	 * A helper that registers the opened connection in the application-level registry.
	 * <p>
	 * The connection may already be closed again when this returns. Broadcasting the
	 * {@link ConnectedMessage} is where the {@link IAuthorizationStrategy} is asked about a shared
	 * {@link WebSocketResource}, and a refusal closes the connection there. This happens before the
	 * transport registers its message handlers, so no frame of the client can be delivered to a
	 * resource that was refused.
	 *
	 * @param connection
	 *            the web socket connection to use to communicate with the client
	 * @see #onOpen(Object)
	 */
	protected final void onConnect(final IWebSocketConnection connection) {
		IKey key = getRegistryKey();
		connectionRegistry.setConnection(getApplication(), getSessionId(), key, connection);

		if (connectionFilter != null)
		{
			ConnectionRejected connectionRejected = connectionFilter.doFilter(servletRequest);
			if (connectionRejected != null)
			{
				broadcastMessage(new AbortedMessage(getApplication(), getSessionId(), key));
				connectionRegistry.removeConnection(getApplication(), getSessionId(), key);
				connection.close(connectionRejected.getCode(), connectionRejected.getReason());
				return;
			}
		}

		broadcastMessage(new ConnectedMessage(getApplication(), getSessionId(), key), connection, webSocketSettings.isAsynchronousPush(), webSocketSettings.getAsynchronousPushTimeout());
	}

	@Override
	public void onClose(int closeCode, String message)
	{
		IKey key = getRegistryKey();
		if (webSocketSettings.shouldNotifyOnCloseEvent(closeCode)) {
			broadcastMessage(new ClosedMessage(getApplication(), getSessionId(), key, closeCode, message));
		}
		connectionRegistry.removeConnection(getApplication(), getSessionId(), key);
	}

	@Override
	public void onError(Throwable t)
	{
		if (webSocketSettings.shouldNotifyOnErrorEvent(t)) {
			IKey key = getRegistryKey();
			broadcastMessage(new ErrorMessage(getApplication(), getSessionId(), key, t));
		}
	}

	public final void broadcastMessage(final IWebSocketMessage message)
	{
		IKey key = getRegistryKey();
		IWebSocketConnection connection = connectionRegistry.getConnection(application, sessionId, key);
		broadcastMessage(message, connection, webSocketSettings.isAsynchronousPush(), webSocketSettings.getAsynchronousPushTimeout());
	}

	/**
	 * Exports the Wicket thread locals and broadcasts the received message from the client to all
	 * interested components and behaviors in the page with id {@code #pageId}
	 * <p>
	 *     Note: ConnectedMessage and ClosedMessage messages are notification-only. I.e. whatever the
	 *     components/behaviors write in the WebSocketRequestHandler will be ignored because the protocol
	 *     doesn't expect response from the user.
	 * </p>
	 * <p>
	 *     A ConnectedMessage for a connection that named a shared {@link WebSocketResource} is where
	 *     that resource is authorized. When the {@link IAuthorizationStrategy} refuses it, this
	 *     method removes the connection from the registry and closes it with
	 *     {@value WebSocketConnectionOriginFilter#POLICY_VIOLATION_ERROR_CODE} instead of
	 *     broadcasting, so none of the callbacks of the resource is reached - not
	 *     {@code onConnect()}, and not the {@code onClose()} that follows the close either, because
	 *     the connection is no longer in the registry to be found. The decision is taken once, per
	 *     connection; it is not repeated for the messages that follow on an accepted one.
	 * </p>
	 * <p>
	 *     The session is resolved again for every message, out of the HTTP session the connection
	 *     was opened on. When that session has ended - invalidated by the application, by a filter,
	 *     by a container-managed logout, or by an idle timeout - there is nothing left to resolve,
	 *     and the connection is removed from the registry and closed with
	 *     {@value WebSocketConnectionOriginFilter#POLICY_VIOLATION_ERROR_CODE} rather than carrying
	 *     the identity it was opened with any further. A ClosedMessage, ErrorMessage or
	 *     AbortedMessage is still dropped silently in that case, because closing a connection that
	 *     is already going away says nothing.
	 * </p>
	 *
	 * @param message
	 *      the message to broadcast
     * @param connection
     * 	    the {@link org.apache.wicket.protocol.ws.api.IWebSocketConnection}
     * @param asynchronousPush
     * 	    whether asynchronous pus is used or not
     * @param timeout
     * 	    The time ut to use for operation (in milliseconds). A negative value means use default timeout
     * 	    (specified by container).
	 */
	public final void broadcastMessage(final IWebSocketMessage message, IWebSocketConnection connection, boolean asynchronousPush, long timeout)
	{
		if (connection != null && (connection.isOpen() || isSpecialMessage(message)))
		{
			Application oldApplication = ThreadContext.getApplication();
			Session oldSession = ThreadContext.getSession();
			RequestCycle oldRequestCycle = ThreadContext.getRequestCycle();

			WebResponse webResponse = webSocketSettings.newWebSocketResponse(connection, asynchronousPush, timeout);
			try
			{
				WebSocketRequestMapper requestMapper = new WebSocketRequestMapper(application.getRootRequestMapper());
				RequestCycle requestCycle = createRequestCycle(requestMapper, webResponse);
				ThreadContext.setRequestCycle(requestCycle);

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

				if (session == null)
				{
					connectionRegistry.removeConnection(application, sessionId, connection.getKey());
					LOG.debug("No Session could be found for session id '{}' and key '{}'!", sessionId, connection.getKey());
					if (isSpecialMessage(message) == false)
					{
						connection.close(WebSocketConnectionOriginFilter.POLICY_VIOLATION_ERROR_CODE, SESSION_ENDED);
					}
					return;
				}

				if (message instanceof ConnectedMessage && isResourceAuthorized(session) == false)
				{
					LOG.warn("The IAuthorizationStrategy refused the WebSocketResource named '{}'. " +
							"Closing the connection with key '{}'.", resourceName, connection.getKey());
					connectionRegistry.removeConnection(application, sessionId, connection.getKey());
					connection.close(WebSocketConnectionOriginFilter.POLICY_VIOLATION_ERROR_CODE, NOT_AUTHORIZED);
					return;
				}

				IPageManager pageManager = session.getPageManager();
				Page page = getPage(pageManager);

				if (page != null)
				{
					WebSocketRequestHandler requestHandler = webSocketSettings.newWebSocketRequestHandler(page, connection);

					WebSocketPayload<?> payload = createEventPayload(message, requestHandler);

					if (!(message instanceof ConnectedMessage || isSpecialMessage(message))) {
						requestCycle.scheduleRequestHandlerAfterCurrent(requestHandler);
					}

					IRequestHandler broadcastingHandler = new WebSocketMessageBroadcastHandler(pageId, resourceName, payload);
					requestMapper.setHandler(broadcastingHandler);
					requestCycle.processRequestAndDetach();
				}
				else
				{
					LOG.debug("Page with id '{}' has been expired. No message will be broadcast!", pageId);
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

	private static boolean isSpecialMessage(IWebSocketMessage message)
	{
		return message instanceof ClosedMessage || message instanceof ErrorMessage || message instanceof AbortedMessage;
	}

	private RequestCycle createRequestCycle(WebSocketRequestMapper requestMapper, WebResponse webResponse)
	{
		RequestCycleContext context = new RequestCycleContext(webRequest, webResponse,
				requestMapper, application.getExceptionMapperProvider().get());

		RequestCycle requestCycle = application.getRequestCycleProvider().apply(context);
		requestCycle.getListeners().add(application.getRequestCycleListeners());
		requestCycle.getListeners().add(new IRequestCycleListener()
		{
			@Override
			public void onDetach(final RequestCycle requestCycle)
			{
				if (Session.exists())
				{
					Session.get().getPageManager().detach();
				}
			}
		});
		requestCycle.getUrlRenderer().setBaseUrl(baseUrl);
		return requestCycle;
	}

	/**
	 * Asks the {@link IAuthorizationStrategy} whether the client that opened this connection may
	 * use the shared {@link WebSocketResource} it named in the handshake.
	 * <p>
	 * A connection to a page is never asked about and always allowed here: a page was authorized
	 * when it was instantiated and rendered, and the message is delivered to a behavior of that
	 * very page, out of the page store of the caller's own session.
	 * <p>
	 * The question is the one the constructor of
	 * {@link org.apache.wicket.request.handler.resource.ResourceRequestHandler} puts for the HTTP
	 * routes to a resource, with the same empty parameters: the query of a WebSocket URL carries
	 * only Wicket's own connection parameters, which the client chooses, and they are not the
	 * parameters of a request to the resource. The refusal is not the same, because a status code
	 * cannot be delivered over this transport - see
	 * {@link #broadcastMessage(IWebSocketMessage, IWebSocketConnection, boolean, long)}.
	 *
	 * @param session
	 *      the session of the client that opened the connection
	 * @return {@code false} only when a shared resource was named and the strategy refused it
	 */
	private boolean isResourceAuthorized(Session session)
	{
		if (Strings.isEmpty(resourceName))
		{
			return true;
		}

		IAuthorizationStrategy authorizationStrategy = session != null
			? session.getAuthorizationStrategy()
			: application.getSecuritySettings().getAuthorizationStrategy();

		IResource resource = new SharedResourceReference(resourceName).getResource();

		return authorizationStrategy.isResourceAuthorized(resource, new PageParameters());
	}

	/**
	 * @param pageManager
	 *      the page manager to use when finding a page by id
	 * @return the page to use when creating WebSocketRequestHandler
	 */
	private Page getPage(IPageManager pageManager)
	{
		Page page;
		if (pageId != -1)
		{
			page = (Page) pageManager.getPage(pageId);
		}
		else
		{
			page = new WebSocketResourcePage();
		}
		return page;
	}

	protected final WebApplication getApplication()
	{
		return application;
	}

	protected final String getSessionId()
	{
		return sessionId;
	}

	private WebSocketPayload<?> createEventPayload(IWebSocketMessage message, WebSocketRequestHandler handler)
	{
		final WebSocketPayload<?> payload;
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
		else if (message instanceof ErrorMessage)
		{
			payload = new WebSocketErrorPayload((ErrorMessage) message, handler);
		}
		else if (message instanceof AbortedMessage)
		{
			payload = new WebSocketAbortedPayload((AbortedMessage) message, handler);
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

	protected IKey getRegistryKey()
	{
		IKey key;
		if (Strings.isEmpty(resourceName))
		{
			key = new PageIdKey(pageId, context);
		}
		else
		{
			if (Strings.isEmpty(connectionToken))
			{
				key = new ResourceNameKey(resourceName, context);
			} else {
				key = new ResourceNameTokenKey(resourceName, connectionToken, context);
			}
		}
		return key;
	}

	/**
	 * A dummy page that is used to create a new WebSocketRequestHandler for
	 * web socket connections to WebSocketResource
	 */
	private static class WebSocketResourcePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private WebSocketResourcePage()
		{
			setStatelessHint(true);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("");
		}
	}
}
