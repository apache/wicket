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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.template.PackageTextTemplate;

import java.util.Map;

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

	@Override
	public boolean getStatelessHint(Component component)
	{
		return false;
	}
}
