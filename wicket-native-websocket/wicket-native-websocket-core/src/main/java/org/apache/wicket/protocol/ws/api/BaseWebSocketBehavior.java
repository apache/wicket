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

import org.apache.wicket.Application;
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

import java.util.Map;
import java.util.Set;

import javax.servlet.SessionTrackingMode;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * A behavior that contributes {@link WicketWebSocketJQueryResourceReference}
 */
public class BaseWebSocketBehavior extends Behavior
{
	private final String resourceName;

	/**
	 * Constructor.
	 *
	 * Contributes WebSocket initialization code that will
	 * work with {@link org.apache.wicket.protocol.ws.api.WebSocketBehavior}
	 */
	protected BaseWebSocketBehavior()
	{
		this.resourceName = null;
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
		this.resourceName = Args.notEmpty(resourceName, "resourceName");
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(WicketWebSocketJQueryResourceReference.get()));

		PackageTextTemplate webSocketSetupTemplate =
				new PackageTextTemplate(WicketWebSocketJQueryResourceReference.class,
						"res/js/wicket-websocket-setup.js.tmpl");

		Map<String, Object> variables = Generics.newHashMap();


		// set falsy JS values for the non-used parameter
		if (Strings.isEmpty(resourceName))
		{
			int pageId = component.getPage().getPageId();
			variables.put("pageId", pageId);
			variables.put("resourceName", "");
		}
		else
		{
			variables.put("resourceName", resourceName);
			variables.put("pageId", false);
		}

		WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(component.getApplication());

		CharSequence baseUrl = getBaseUrl(webSocketSettings);
		Args.notNull(baseUrl, "baseUrl");
		variables.put("baseUrl", baseUrl);

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

		String webSocketSetupScript = webSocketSetupTemplate.asString(variables);

		response.render(OnDomReadyHeaderItem.forScript(webSocketSetupScript));
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
			final Cookie jsessionid = cookieUtils.getCookie("JSESSIONID");
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
