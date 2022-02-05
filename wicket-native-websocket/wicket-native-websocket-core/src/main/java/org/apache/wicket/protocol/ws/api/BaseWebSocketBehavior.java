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

import java.util.Map;
import java.util.Set;

import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.template.PackageTextTemplate;

/**
 * A behavior that contributes {@link WicketWebSocketJQueryResourceReference}
 */
public class BaseWebSocketBehavior extends Behavior
{
	private final String resourceName;
	private final String connectionToken;

	/**
	 * Constructor.
	 *
	 * Contributes WebSocket initialization code that will
	 * work with {@link org.apache.wicket.protocol.ws.api.WebSocketBehavior}
	 */
	protected BaseWebSocketBehavior()
	{
		this.resourceName = null;
		this.connectionToken = null;
	}

	/**
	 * Constructor.
	 *
	 * Contributes WebSocket initialization code that will
	 * work with {@link org.apache.wicket.protocol.ws.api.WebSocketResource}
	 *
	 * To use WebSocketResource the application have to setup the
	 * resource as a shared one in its {@link org.apache.wicket.Application#init()}
	 * method:
	 * <code><pre>
	 *     getSharedResources().add(resourceName, new MyWebSocketResource())
	 * </pre></code>
	 *
	 *  @param resourceName
	 *          the name of the shared {@link org.apache.wicket.protocol.ws.api.WebSocketResource}
	 */
	public BaseWebSocketBehavior(String resourceName)
	{
		this(resourceName, null);
	}

	/**
	 * Constructor.
	 *
	 * Contributes WebSocket initialization code that will
	 * work with {@link org.apache.wicket.protocol.ws.api.WebSocketResource}
	 *
	 * To use WebSocketResource the application have to setup the
	 * resource as a shared one in its {@link org.apache.wicket.Application#init()}
	 * method:
	 * <code><pre>
	 *     getSharedResources().add(resourceName, new MyWebSocketResource())
	 * </pre></code>
	 *
	 *  @param resourceName
	 *          the name of the shared {@link org.apache.wicket.protocol.ws.api.WebSocketResource}
	 *  @param connectionToken
	 *  		an optional token to support connections to the same resource from multiple browser tabs
	 */
	public BaseWebSocketBehavior(String resourceName, String connectionToken)
	{
		this.resourceName = Args.notEmpty(resourceName, "resourceName");
		this.connectionToken = connectionToken;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(WicketWebSocketJQueryResourceReference.get()));

		Map<String, Object> parameters = getParameters(component);
		String webSocketSetupScript = getWebSocketSetupScript(parameters);

		response.render(OnDomReadyHeaderItem.forScript(webSocketSetupScript));
	}

	protected String getWebSocketSetupScript(Map<String, Object> parameters) {
		PackageTextTemplate webSocketSetupTemplate =
				new PackageTextTemplate(WicketWebSocketJQueryResourceReference.class,
						"res/js/wicket-websocket-setup.js.tmpl");

		return webSocketSetupTemplate.asString(parameters);
	}

	/**
	 * Override to return a context. By default, this is the page class name.
	 *
	 * @param component the {@link org.apache.wicket.Component}
	 * @return the context for this websocket behavior.
	 */
	protected String getContext(Component component) {
		return component.getPage().getClass().getName();
	}

	private Map<String, Object> getParameters(Component component) {
		Map<String, Object> variables = Generics.newHashMap();

		variables.put("context", getContext(component));

		// set falsy JS values for the non-used parameters
		if (Strings.isEmpty(resourceName))
		{
			int pageId = component.getPage().getPageId();
			variables.put("pageId", pageId);
			variables.put("resourceName", "");
			variables.put("connectionToken", "");
		}
		else
		{
			variables.put("resourceName", resourceName);
			variables.put("connectionToken", connectionToken);
			variables.put("pageId", false);
		}

		WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(component.getApplication());

		CharSequence baseUrl = getBaseUrl(webSocketSettings);
		Args.notNull(baseUrl, "baseUrl");
		variables.put("baseUrl", baseUrl);

		Integer port = getPort(webSocketSettings);
		variables.put("port", port);
		Integer securePort = getSecurePort(webSocketSettings);
		variables.put("securePort", securePort);

		variables.put("useHeartBeat", isUseHeartBeat(webSocketSettings));

		variables.put("reconnectOnFailure", isReconnectOnFailure(webSocketSettings));

		variables.put("heartBeatPace", getHeartBeatPace(webSocketSettings));

		variables.put("networkLatencyThreshold", getNetworkLatencyThreshold(webSocketSettings));

		CharSequence contextPath = getContextPath(webSocketSettings);
		Args.notNull(contextPath, "contextPath");
		variables.put("contextPath", contextPath);

		// preserve the application name for JSR356 based impl
		variables.put("applicationName", component.getApplication().getName());

		CharSequence filterPrefix = getFilterPrefix(webSocketSettings);
		Args.notNull(filterPrefix, "filterPrefix");
		variables.put("filterPrefix", filterPrefix);

		final CharSequence sessionId = getSessionId(component);
		variables.put("sessionId", sessionId);
		return variables;
	}

	protected boolean isUseHeartBeat(WebSocketSettings webSocketSettings)
	{
		return webSocketSettings.isUseHeartBeat();
	}

	protected boolean isReconnectOnFailure(WebSocketSettings webSocketSettings)
	{
		return webSocketSettings.isReconnectOnFailure();
	}

	protected long getHeartBeatPace(WebSocketSettings webSocketSettings)
	{
		return webSocketSettings.getHeartBeatPace();
	}

	protected long getNetworkLatencyThreshold(WebSocketSettings webSocketSettings)
	{
		return webSocketSettings.getNetworkLatencyThreshold();
	}

	protected Integer getPort(WebSocketSettings webSocketSettings)
	{
		return webSocketSettings.getPort();
	}

	protected Integer getSecurePort(WebSocketSettings webSocketSettings)
	{
		return webSocketSettings.getSecurePort();
	}

	protected CharSequence getFilterPrefix(final WebSocketSettings webSocketSettings) {
		return webSocketSettings.getFilterPrefix();
	}

	protected CharSequence getContextPath(final WebSocketSettings webSocketSettings) {
		return webSocketSettings.getContextPath();
	}

	protected CharSequence getBaseUrl(final WebSocketSettings webSocketSettings) {
		return webSocketSettings.getBaseUrl();
	}

	/**
	 * @param component
	 *          The component this behavior is bound to
	 * @return The http session id if it is tracked in the url, otherwise empty string
	 */
	protected CharSequence getSessionId(final Component component)
	{
		String sessionId = "";
		final WebApplication application = (WebApplication) component.getApplication();
		final Set<SessionTrackingMode> effectiveSessionTrackingModes = application.getServletContext().getEffectiveSessionTrackingModes();
		Object containerRequest = component.getRequest().getContainerRequest();
		if (effectiveSessionTrackingModes.size() == 1 && SessionTrackingMode.URL.equals(effectiveSessionTrackingModes.iterator().next()))
		{
			sessionId = component.getSession().getId();
		}
		else if (containerRequest instanceof HttpServletRequest)
		{
			CookieUtils cookieUtils = new CookieUtils();
			final String jsessionCookieName = cookieUtils.getSessionIdCookieName(application);
			final Cookie jsessionid = cookieUtils.getCookie(jsessionCookieName);
			HttpServletRequest httpServletRequest = (HttpServletRequest) containerRequest;
			if (jsessionid == null || httpServletRequest.isRequestedSessionIdValid() == false)
			{
				sessionId = component.getSession().getId();
			}
		}
		return sessionId;
	}

	@Override
	public boolean getStatelessHint(Component component)
	{
		return false;
	}
}
