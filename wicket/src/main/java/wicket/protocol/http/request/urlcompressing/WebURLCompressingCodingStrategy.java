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
package wicket.protocol.http.request.urlcompressing;

import wicket.Component;
import wicket.IRedirectListener;
import wicket.Page;
import wicket.PageMap;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.markup.html.WebPage;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.request.target.component.listener.IListenerInterfaceRequestTarget;
import wicket.util.string.AppendingStringBuffer;


/**
 * Use this CodingStategy with the
 * {@link WebURLCompressingTargetResolverStrategy} to minimize the
 * wicket:interface urls. The component path and the interface name will be
 * removed from the url and only an uid will be inserted into the url.
 * 
 * To use this url compressing behaviour you must override the
 * {@link WebApplication}'s newRequestCycleProcessor() method. To make a
 * request cycle processor with this CodingStrategy and the
 * {@link WebURLCompressingTargetResolverStrategy}
 * 
 * <pre>
 * protected IRequestCycleProcessor newRequestCycleProcessor()
 * {
 * 	return new CompoundRequestCycleProcessor(new WebURLCompressingCodingStrategy(),
 * 			new WebURLCompressingTargetResolverStrategy(), null, null, null);
 * }
 * </pre>
 * 
 * @author jcompagner
 * 
 * @since 1.2
 */
public class WebURLCompressingCodingStrategy extends WebRequestCodingStrategy
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
		url.append(urlPrefix(requestCycle));
		url.append('?');
		url.append(INTERFACE_PARAMETER_NAME);
		url.append('=');

		// Get component and page for request target
		final Component component = requestTarget.getTarget();
		final Page page = component.getPage();

		// Add pagemap
		final PageMap pageMap = page.getPageMap();
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
		final int versionNumber = component.getPage().getCurrentVersionNumber();
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

		return requestCycle.getOriginalResponse().encodeURL(url);
	}
}
