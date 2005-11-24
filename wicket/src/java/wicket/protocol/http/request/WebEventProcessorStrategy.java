/*
 * $Id$ $Revision$ $Date$
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
package wicket.protocol.http.request;

import java.lang.reflect.Method;

import wicket.Application;
import wicket.ApplicationSettings;
import wicket.Component;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.markup.html.WebPage;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.request.PageRequestTarget;
import wicket.request.compound.IEventProcessorStrategy;
import wicket.util.string.Strings;

/**
 * TODO docme
 * 
 * @author Eelco Hillenius
 */
public final class WebEventProcessorStrategy implements IEventProcessorStrategy
{
	/**
	 * Construct.
	 */
	public WebEventProcessorStrategy()
	{
	}

	/**
	 * @see wicket.request.compound.IEventProcessorStrategy#processEvents(wicket.RequestCycle)
	 */
	public final void processEvents(final RequestCycle requestCycle)
	{
		IRequestTarget target = requestCycle.getRequestTarget();

		if (target instanceof PageRequestTarget)
		{
			// Get page from path
			// final Page page = ((PageRequestTarget)requestTarget).getPage();
			final Page page = requestCycle.getResponsePage();

			// Assume cluster needs to be updated now, unless listener
			// invocation
			// change this (for example, with a simple page redirect)
			requestCycle.setUpdateCluster(true);

			// Execute the user's code
			final WebRequestCycle webRequestCycle = (WebRequestCycle)requestCycle;
			final WebRequest webRequest = webRequestCycle.getWebRequest();
			final String componentPath = webRequest.getParameter("path");
			if (componentPath != null)
			{
				// Invoke interface on the component at the given path on the
				// page
				final Component component = page.get(Strings.afterFirstPathComponent(componentPath, ':'));

				if (!component.isVisible())
				{
					throw new WicketRuntimeException(
							"Calling listener methods on components that are not visible is not allowed");
				}
				String interfaceName = getInterfaceName(webRequest);
				Method method = requestCycle.getRequestInterfaceMethod(interfaceName);
				if (method != null)
				{
					// Set the page for the component as the response page
					requestCycle.setResponsePage(page);
					if (!interfaceName.equals("IRedirectListener"))
					{
						// Clear all feedback messages if it isn't a redirect
						page.getFeedbackMessages().clear();

						final Application application = requestCycle.getApplication();
						// and see if we have to redirect the render part by
						// default
						ApplicationSettings.RenderStrategy strategy = application.getSettings()
								.getRenderStrategy();
						boolean issueRedirect = (strategy == ApplicationSettings.REDIRECT_TO_RENDER || strategy == ApplicationSettings.REDIRECT_TO_BUFFER);

						requestCycle.setRedirect(issueRedirect);
					}

					// Invoke interface on component
					invokeInterface(component, method, page);
				}
				else
				{
					throw new WicketRuntimeException("Attempt to access unknown interface "
							+ interfaceName);
				}
			}
		}
	}

	/**
	 * Invokes a given interface on a component.
	 * 
	 * @param component
	 *            The component
	 * @param method
	 *            The name of the method to call
	 * @param page
	 *            The page on which the component resides
	 */
	protected final void invokeInterface(final Component component, final Method method,
			final Page page)
	{
		if (page instanceof WebPage)
		{
			((WebPage)page).beforeCallComponent(component, method);
		}

		try
		{
			// Invoke the interface method on the component
			method.invoke(component, new Object[] {});
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("ethod " + method.getName() + " of "
					+ method.getDeclaringClass() + "targetted at component " + component
					+ " threw an exception", e);
		}
		finally
		{
			if (page instanceof WebPage)
			{
				((WebPage)page).afterCallComponent(component, method);
			}
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Method for dispatching/calling a interface on a page from the given url.
	 * Used by {@link Form#onFormSubmitted()} for dispatching events
	 * 
	 * @param requestCycle The current request cycle 
	 * 
	 * @param page The page where the event should be called on.
	 * @param url The url which describes the component path and the interface to be called.
	 */
	public final void dispatchEvent(final Page page, final String url)
	{
		RequestCycle requestCycle = RequestCycle.get();
		String decodedUrl = requestCycle.getRequest().decodeURL(url);
		int indexOfPath = decodedUrl.indexOf("path=");
		int indexOfInterface = decodedUrl.indexOf("interface=");
		if(indexOfPath != -1 && indexOfInterface != -1)
		{
			indexOfPath += "path=".length();
			indexOfInterface += "interface=".length();
			int indexOfPathEnd = decodedUrl.indexOf("&",indexOfPath);
			if(indexOfPathEnd == -1) indexOfPathEnd = decodedUrl.length();
			int indexOfInterfaceEnd = decodedUrl.indexOf("&",indexOfInterface);
			if(indexOfInterfaceEnd == -1) indexOfInterfaceEnd = decodedUrl.length();
			
			String path = decodedUrl.substring(indexOfPath, indexOfPathEnd);
			String interfaceName = decodedUrl.substring(indexOfInterface, indexOfInterfaceEnd);
			
			final Component component = page.get(Strings.afterFirstPathComponent(path, ':'));

			if (!component.isVisible())
			{
				throw new WicketRuntimeException(
						"Calling listener methods on components that are not visible is not allowed");
			}
			Method method = requestCycle.getRequestInterfaceMethod(interfaceName);
			if (method != null)
			{
				invokeInterface(component, method, page);
			}
		}
		else
		{
			// log warning??
		}
	}
	
	/**
	 * Gets the name of the interface to invoke.
	 * 
	 * @param webRequest
	 *            the web request object
	 * @return the name of the interface to invoke
	 */
	private String getInterfaceName(final WebRequest webRequest)
	{
		String interfaceName = webRequest.getParameter("interface");
		if (interfaceName == null)
		{
			interfaceName = "IRedirectListener";
		}
		return interfaceName;
	}
}
