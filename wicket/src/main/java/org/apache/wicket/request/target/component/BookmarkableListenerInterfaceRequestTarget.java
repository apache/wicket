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
package org.apache.wicket.request.target.component;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

/**
 * Request target for bookmarkable page links that also contain component path and interface name.
 * This is used for stateless forms and stateless links.
 * 
 * @author Matej Knopp
 */
public class BookmarkableListenerInterfaceRequestTarget extends BookmarkablePageRequestTarget
{
	private final String componentPath;
	private final String interfaceName;

	/**
	 * This constructor is called when a stateless link is clicked on but the page wasn't found in
	 * the session. Then this class will recreate the page and call the interface method on the
	 * component that is targeted with the component path.
	 * 
	 * @param pageMapName
	 * @param pageClass
	 * @param pageParameters
	 * @param componentPath
	 * @param interfaceName
	 * @param versionNumber
	 */
	public BookmarkableListenerInterfaceRequestTarget(String pageMapName,
		Class<? extends Page> pageClass, PageParameters pageParameters, String componentPath,
		String interfaceName, int versionNumber)
	{
		super(pageMapName, pageClass, pageParameters);
		this.componentPath = componentPath;
		this.interfaceName = interfaceName;
	}

	/**
	 * This constructor is called for generating the urls (RequestCycle.urlFor()) So it will alter
	 * the PageParameters to include the 2 org.apache.wicket params
	 * {@link WebRequestCodingStrategy#BOOKMARKABLE_PAGE_PARAMETER_NAME} and
	 * {@link WebRequestCodingStrategy#INTERFACE_PARAMETER_NAME}
	 * 
	 * @param pageMapName
	 * @param pageClass
	 * @param pageParameters
	 * @param component
	 * @param listenerInterface
	 */
	public BookmarkableListenerInterfaceRequestTarget(String pageMapName,
		Class<? extends Page> pageClass, PageParameters pageParameters, Component component,
		RequestListenerInterface listenerInterface)
	{
		this(pageMapName, pageClass, pageParameters, component.getPath(),
			listenerInterface.getName(), component.getPage().getCurrentVersionNumber());

		int version = component.getPage().getCurrentVersionNumber();
		setPage(component.getPage());

		// add the wicket:interface param to the params.
		// pagemap:(pageid:componenta:componentb:...):version:interface:behavior:urlDepth
		AppendingStringBuffer param = new AppendingStringBuffer(4 + componentPath.length() +
			interfaceName.length());
		if (pageMapName != null)
		{
			param.append(pageMapName);
		}
		param.append(Component.PATH_SEPARATOR);
		param.append(getComponentPath());
		param.append(Component.PATH_SEPARATOR);
		if (version != 0)
		{
			param.append(version);
		}
		// Interface
		param.append(Component.PATH_SEPARATOR);
		param.append(getInterfaceName());

		// Behavior (none)
		param.append(Component.PATH_SEPARATOR);

		// URL depth (not required)
		param.append(Component.PATH_SEPARATOR);

		pageParameters.put(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME, param.toString());
	}

	@Override
	public void processEvents(RequestCycle requestCycle)
	{
		Page page = getPage();
		if (page == null)
		{
			page = Session.get().getPage(getPageMapName(), componentPath, -1);
			if (page != null)
			{
				setPage(page);
			}
			else if (page == null)
			{
				page = getPage(requestCycle);
			}
		}

		if (page == null)
		{
			throw new PageExpiredException(
				"Request cannot be processed. The target page does not exist anymore.");
		}

		final String pageRelativeComponentPath = Strings.afterFirstPathComponent(componentPath,
			Component.PATH_SEPARATOR);
		Component component = page.get(pageRelativeComponentPath);
		if (component == null)
		{
			// this is quite a hack to get components in repeater work.
			// But it still can fail if the repeater is a paging one or on every render
			// it will generate new index for the items...
			page.prepareForRender(false);
			component = page.get(pageRelativeComponentPath);
			if (component == null)
			{
				throw new WicketRuntimeException(
					"unable to find component with path " +
						pageRelativeComponentPath +
						" on stateless page " +
						page +
						" it could be that the component is inside a repeater make your component return false in getStatelessHint()");
			}
		}
		RequestListenerInterface listenerInterface = RequestListenerInterface.forName(interfaceName);
		if (listenerInterface == null)
		{
			throw new WicketRuntimeException("unable to find listener interface " + interfaceName);
		}
		listenerInterface.invoke(page, component);
	}

	@Override
	public void respond(RequestCycle requestCycle)
	{
		Page page = getPage(requestCycle);
		// if the listener call wanted to redirect
		// then do that if the page is not stateless.
		if (requestCycle.isRedirect() && !page.isPageStateless())
		{
			requestCycle.redirectTo(page);
		}
		else
		{
			// else render the page directly
			page.renderPage();
		}
	}

	/**
	 * @return The component path.
	 */
	public String getComponentPath()
	{
		return componentPath;
	}

	/**
	 * @return The interface name
	 */
	public String getInterfaceName()
	{
		return interfaceName;
	}
}
