/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.request.target.component;

import wicket.Component;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * Request target for bookmarkable page links that also contain component path
 * and interface name. This is used for stateless forms and stateless links.
 * 
 * @author Matej Knopp
 */
public class BookmarkableListenerInterfaceRequestTarget extends BookmarkablePageRequestTarget
{
	private String componentPath;
	private String interfaceName;

	/**
	 * This constructor is called when a stateless link is clicked on but the page
	 * wasn't found in the session. Then this class will recreate the page and call
	 * the interface method on the component that is targetted with the component path.
	 * 
	 * @param pageMapName
	 * @param pageClass
	 * @param pageParameters
	 * @param componentPath
	 * @param interfaceName
	 */
	public BookmarkableListenerInterfaceRequestTarget(String pageMapName,
			Class pageClass, PageParameters pageParameters, String componentPath,
			String interfaceName)
	{
		super(pageMapName, pageClass, pageParameters);
		this.componentPath = componentPath;
		this.interfaceName = interfaceName;
	}

	/**
	 * This constructor is called for generating the urls (RequestCycle.urlFor())
	 * So it will alter the PageParameters to include the 2 wicket params
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
			Class pageClass, PageParameters pageParameters, Component component,
			RequestListenerInterface listenerInterface)
	{
		this(pageMapName, pageClass, pageParameters, component.getPath(),
				listenerInterface.getName());
		
		int version = component.getPage().getCurrentVersionNumber();

		// add the wicket:interface param to the params.
		AppendingStringBuffer param = new AppendingStringBuffer(3 + componentPath.length() + interfaceName.length());
		if(pageMapName != null)
		{
			param.append(pageMapName);
		}
		param.append(Component.PATH_SEPARATOR);
		param.append(getComponentPath());
		param.append(Component.PATH_SEPARATOR);
		if(version != 0)
		{
			param.append(version);
		}
		param.append(Component.PATH_SEPARATOR);
		param.append(getInterfaceName());
		
		pageParameters.put(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME,param.toString());
	}

	public void processEvents(RequestCycle requestCycle)
	{
		Page page = getPage(requestCycle);
		final String pageRelativeComponentPath = Strings.afterFirstPathComponent(componentPath,
				Component.PATH_SEPARATOR);
		Component component = page.get(pageRelativeComponentPath);
		RequestListenerInterface listenerInterface = RequestListenerInterface
				.forName(interfaceName);
		listenerInterface.invoke(page, component);
	}

	public void respond(RequestCycle requestCycle)
	{
		getPage(requestCycle).renderPage();
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
