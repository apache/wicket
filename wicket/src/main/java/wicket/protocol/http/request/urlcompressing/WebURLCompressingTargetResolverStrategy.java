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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.IRedirectListener;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.WicketRuntimeException;
import wicket.authorization.UnauthorizedActionException;
import wicket.markup.html.INewBrowserWindowListener;
import wicket.markup.html.WebPage;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.request.urlcompressing.URLCompressor.ComponentAndInterface;
import wicket.request.RequestParameters;
import wicket.request.compound.DefaultRequestTargetResolverStrategy;
import wicket.request.target.component.listener.RedirectPageRequestTarget;
import wicket.util.string.Strings;


/**
 * Use this ResolverStrategy with the {@link WebURLCompressingCodingStrategy} to
 * minimize the wicket:interface urls. The component path and the interface name
 * will be removed from the url and only an uid will be inserted into the url.
 * 
 * To use this url compressing behaviour you must override the
 * {@link WebApplication} newRequestCycleProcessor() method. To make a request
 * cycle processor with this ResolverStrategy and the
 * {@link WebURLCompressingCodingStrategy}
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
public class WebURLCompressingTargetResolverStrategy extends DefaultRequestTargetResolverStrategy
{
	/** log. */
	private static final Log log = LogFactory.getLog(WebURLCompressingTargetResolverStrategy.class);

	/**
	 * Resolves the RequestTarget for the given interface. This method can be
	 * overriden if some special interface needs to resolve to its own target.
	 * 
	 * @param requestCycle
	 *            The current RequestCycle object
	 * @param page
	 *            The page object which holds the component for which this
	 *            interface is called on.
	 * @param componentPath
	 *            The component path for looking up the component in the page.
	 * @param interfaceName
	 *            The interface to resolve.
	 * @param requestParameters
	 * @return The RequestTarget that was resolved
	 */
	@Override
	protected IRequestTarget resolveListenerInterfaceTarget(final RequestCycle requestCycle,
			final Page page, final String componentPath, String interfaceName,
			final RequestParameters requestParameters)
	{
		String pageRelativeComponentPath = Strings.afterFirstPathComponent(componentPath,
				Component.PATH_SEPARATOR);
		Component component = null;
		if (page instanceof WebPage && !"IResourceListener".equals(interfaceName))
		{
			ComponentAndInterface cai = ((WebPage)page).getUrlCompressor()
					.getComponentAndInterfaceForUID(pageRelativeComponentPath);
			if (cai != null)
			{
				interfaceName = cai.getInterfaceName();
				component = cai.getComponent();
			}
		}

		if (interfaceName.equals(IRedirectListener.INTERFACE.getName()))
		{
			return new RedirectPageRequestTarget(page);
		}
		else if (interfaceName.equals(INewBrowserWindowListener.INTERFACE.getName()))
		{
			return INewBrowserWindowListener.INTERFACE.newRequestTarget(page, page,
					INewBrowserWindowListener.INTERFACE, requestParameters);
		}
		else
		{
			// Get the listener interface we need to call
			final RequestListenerInterface listener = RequestListenerInterface
					.forName(interfaceName);
			if (listener == null)
			{
				throw new WicketRuntimeException(
						"Attempt to access unknown request listener interface " + interfaceName);
			}

			// Get component
			if (component == null)
			{
				if (Strings.isEmpty(pageRelativeComponentPath))
				{
					// We have an interface that is not a redirect, but no
					// component... that must be wrong
					throw new WicketRuntimeException("When trying to call " + listener
							+ ", a component must be provided");
				}
				component = page.get(pageRelativeComponentPath);
			}

			if (component == null || !component.isEnabled() || !component.isVisibleInHierarchy())
			{
				log
						.info("component not enabled or visible, redirecting to calling page, component: "
								+ component);
				return new RedirectPageRequestTarget(page);
			}
			if (!component.isEnableAllowed())
			{
				throw new UnauthorizedActionException(component, Component.ENABLE);
			}

			// Ask the request listener interface object to create a request
			// target
			return listener.newRequestTarget(page, component, listener, requestParameters);
		}
	}
}
