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
package org.apache.wicket.protocol.http.request.urlcompressing;

import org.apache.wicket.Component;
import org.apache.wicket.IPageMap;
import org.apache.wicket.IRedirectListener;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.behavior.IActivePageBehaviorListener;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.listener.IListenerInterfaceRequestTarget;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;


/**
 * Use this CodingStategy with the {@link UrlCompressingWebRequestProcessor} to minimize the
 * wicket:interface urls. The component path and the interface name will be removed from the url and
 * only an uid will be inserted into the url.
 * 
 * Use it like this:
 * 
 * <pre>
 * protected IRequestCycleProcessor newRequestCycleProcessor()
 * {
 * 	return new UrlCompressingWebRequestProcessor();
 * }
 * </pre>
 * 
 * @author jcompagner
 * 
 * @since 1.3
 */
public class UrlCompressingWebCodingStrategy extends WebRequestCodingStrategy
{
	/**
	 * Encode a listener interface target.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestTarget
	 *            the target to encode
	 * @return the encoded url
	 */
	@Override
	protected CharSequence encode(RequestCycle requestCycle,
		IListenerInterfaceRequestTarget requestTarget)
	{
		final RequestListenerInterface rli = requestTarget.getRequestListenerInterface();

		// Start string buffer for url
		final AppendingStringBuffer url = new AppendingStringBuffer(64);
		url.append('?');
		url.append(INTERFACE_PARAMETER_NAME);
		url.append('=');

		// Get component and page for request target
		final Component component = requestTarget.getTarget();
		final Page page = component.getPage();

		// Add pagemap
		final IPageMap pageMap = page.getPageMap();
		if (!pageMap.isDefault())
		{
			url.append(pageMap.getName());
		}
		url.append(Component.PATH_SEPARATOR);

		String listenerName = rli.getName();
		// Add path to component
		if (page instanceof WebPage && !"IResourceListener".equals(listenerName))
		{
			url.append(page.getId());
			url.append(Component.PATH_SEPARATOR);
			url.append(((WebPage)page).getUrlCompressor().getUIDForComponentAndInterface(component,
				listenerName));
			listenerName = null;
		}
		else
		{
			url.append(component.getPath());
		}
		url.append(Component.PATH_SEPARATOR);

		// Add version
		final int versionNumber = 0;
		if (!rli.getRecordsPageVersion())
		{
			url.append(Page.LATEST_VERSION);
		}
		else if (versionNumber > 0)
		{
			url.append(versionNumber);
		}
		url.append(Component.PATH_SEPARATOR);

		// Add listener interface
		if (listenerName != null && !IRedirectListener.INTERFACE.getName().equals(listenerName))
		{
			url.append(listenerName);
		}

		url.append(Component.PATH_SEPARATOR);

		// Add behaviourId
		RequestParameters params = requestTarget.getRequestParameters();
		if (params != null && params.getBehaviorId() != null)
		{
			url.append(params.getBehaviorId());
		}
		url.append(Component.PATH_SEPARATOR);

		// Add URL depth
		if (params != null && params.getUrlDepth() != 0)
		{
			url.append(params.getUrlDepth());
		}
		if (IActivePageBehaviorListener.INTERFACE.getName().equals(rli.getName()))
		{
			url.append(url.indexOf("?") > -1 ? "&" : "?").append(
				IGNORE_IF_NOT_ACTIVE_PARAMETER_NAME).append("=true");
		}
		return requestCycle.getOriginalResponse().encodeURL(url);
	}

	@Override
	protected CharSequence encode(RequestCycle requestCycle,
		IBookmarkablePageRequestTarget requestTarget)
	{
		if (requestTarget instanceof BookmarkableListenerInterfaceRequestTarget &&
			requestTarget.getPageParameters().containsKey(
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME))
		{
			BookmarkableListenerInterfaceRequestTarget t = (BookmarkableListenerInterfaceRequestTarget)requestTarget;
			Page page = t.getPage();
			int version = 0;
			String componentPath = t.getComponentPath();
			String interfaceName = t.getInterfaceName();
			String pageMapName = t.getPageMapName();

			// add the wicket:interface param to the params.
			// pagemap:(pageid:componenta:componentb:...):version:interface:behavior:urlDepth
			AppendingStringBuffer param = new AppendingStringBuffer(4 + componentPath.length() +
				interfaceName.length());
			if (pageMapName != null)
			{
				param.append(pageMapName);
			}
			param.append(Component.PATH_SEPARATOR);
			// Add path to component
			if (page instanceof WebPage && !"IResourceListener".equals(interfaceName))
			{
				param.append(page.getId());
				Component comp = page.get(Strings.afterFirstPathComponent(componentPath,
					Component.PATH_SEPARATOR));
				param.append(Component.PATH_SEPARATOR);
				param.append(((WebPage)page).getUrlCompressor().getUIDForComponentAndInterface(
					comp, interfaceName));
				interfaceName = null;
			}
			else
			{
				param.append(componentPath);
			}
			param.append(Component.PATH_SEPARATOR);
			if (version != 0)
			{
				param.append(version);
			}
			// Interface
			param.append(Component.PATH_SEPARATOR);
			// Add listener interface
			if (interfaceName != null &&
				!IRedirectListener.INTERFACE.getName().equals(interfaceName))
			{
				param.append(interfaceName);
			}

			// Behavior (none)
			param.append(Component.PATH_SEPARATOR);

			// URL depth (not required)
			param.append(Component.PATH_SEPARATOR);
			t.getPageParameters().put(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME,
				param.toString());
		}
		return super.encode(requestCycle, requestTarget);
	}
}
