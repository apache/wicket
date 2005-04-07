/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.protocol.http;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.IRedirectListener;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.Resource;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.markup.html.form.Form;
import wicket.util.io.Streams;
import wicket.util.string.Strings;

/**
 * RequestCycle implementation for HTTP protocol. Holds the application,
 * session, request and response objects for a given HTTP request. Contains
 * methods (urlFor*) which yield a URL for bookmarkable pages as well as
 * non-bookmarkable component interfaces. The protected handleRender method is
 * the internal entrypoint which takes care of the details of rendering a
 * response to an HTTP request.
 * 
 * @see RequestCycle
 * @author Jonathan Locke
 */
public class WebRequestCycle extends RequestCycle
{
	/** Logging object */
	private static final Log log = LogFactory.getLog(WebRequestCycle.class);

	/** Path prefix for shared resources */
	private static final String resourceReferencePrefix = "/resource/";

	/**
	 * Constructor which simply passes arguments to superclass for storage
	 * there.
	 * 
	 * @param session
	 *            The session
	 * @param request
	 *            The request
	 * @param response
	 *            The response
	 */
	public WebRequestCycle(final WebSession session, final WebRequest request,
			final Response response)
	{
		super(session, request, response);
	}

	/**
	 * @return Request as a WebRequest
	 */
	public WebRequest getWebRequest()
	{
		return (WebRequest)request;
	}

	/**
	 * @return Response as a WebResponse
	 */
	public WebResponse getWebResponse()
	{
		return (WebResponse)response;
	}

	/**
	 * Parses a request. The following four steps are followed:
	 * <p>
	 * 1. If the URL requested is in the form of a component listener
	 * invocation, then that invocation will occur and is expected to generate a
	 * response.
	 * <p>
	 * 2. If the URL is to a bookmarkable page, then an instance of that page is
	 * created and is expected render a response.
	 * <p>
	 * 3. If the URL is for the application's home page, an instance of the home
	 * page will be created and is expected to render a response.
	 * <p>
	 * 4. Finally, an attempt is made to render the requested resource as static
	 * content, available through the servlet context.
	 * <p>
	 * If all four steps are executed and content cannot be found to satisfy the
	 * request, then false is returned.
	 * 
	 * @return True if a Page should be rendered back to the user
	 */
	protected final boolean parseRequest()
	{
		// Try different methods of parsing and dispatching the request
		if (callComponentListener() || bookmarkablePage() || homePage() || resourceReference())
		{
			return true;
		}
		else
		{
			if (!renderStaticContent())
			{
				throw new WicketRuntimeException("Unable to parse request " + request);
			}
			return false;
		}
	}

	/**
	 * Redirects browser to the given page
	 * 
	 * @param page
	 *            The page to redirect to
	 */
	protected void redirectTo(final Page page)
	{
		// Redirect to the url for the page
		response.redirect(page.urlFor(page, IRedirectListener.class));
	}

	/**
	 * Sets values for form components based on cookie values in the request.
	 * 
	 * @param page
	 *            the current page
	 */
	final void setFormComponentValuesFromCookies(final Page page)
	{
		// Visit all Forms contained in the page
		page.visitChildren(Form.class, new Component.IVisitor()
		{
			// For each FormComponent found on the Page (not Form)
			public Object component(final Component component)
			{
				((Form)component).loadPersistentFormComponentValues();
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Activates a bookmarkable page if one was specified in the request.
	 * 
	 * @return True if a bookmarkable page was created and returned for the
	 *         request.
	 * @throws WicketRuntimeException
	 */
	private boolean bookmarkablePage()
	{
		// Get any component parameter
		final String pageClassName = request.getParameter("bookmarkablePage");
		if (pageClassName != null)
		{
		    try
		    {
				final Class pageClass = session.getClassResolver().resolveClass(pageClassName);
				setResponsePage(session.getPageFactory().newPage(pageClass,
						new PageParameters(getRequest().getParameterMap())));
				return true;
		    }
		    catch (RuntimeException e)
		    {
		        throw new WicketRuntimeException("Unable to instantiate Page class: " 
		                + pageClassName + ". See below for details.", e);
		    }
		}
		return false;
	}

	/**
	 * Calls a component listener interface on a page that already exists in the
	 * session. The session component is found using the path in the 'component'
	 * parameter of the request. The interface method to be called is determined
	 * by the 'interface' parameter of the request. The interface can only be
	 * one of the interfaces listed in the secureInterfaceMethods map in the
	 * RequestDispatcher implementation.
	 * 
	 * @return True if the component listener was successfully called
	 * @throws WicketRuntimeException
	 */
	private boolean callComponentListener()
	{
		// Get any component parameter
		final String path = request.getParameter("component");
		final String pageMapName = request.getParameter("pagemap");
		if (path != null)
		{
			// Get version number
			final String versionNumberString = request.getParameter("version");
			final int versionNumber = Strings.isEmpty(versionNumberString) ? 0 : Integer
					.parseInt(versionNumberString);

			// Get page from path
			final Page page = session.getPage(pageMapName, path, versionNumber);

			// Does page exist?
			if (page != null)
			{
				// Execute the user's code
				invokeInterface(page, path, request.getParameter("interface"));
				return true;
			}
			else
			{
				onExpiredPage();
				return true;
			}
		}
		return false;
	}

	/**
	 * If no context path was provided, activates the home page.
	 * 
	 * @return True if the home page was activated
	 * @throws WicketRuntimeException
	 */
	private boolean homePage()
	{
		final String pathInfo = getWebRequest().getPathInfo();
		if (Strings.isEmpty(pathInfo) || "/".equals(pathInfo))
		{
			try
			{
				setResponsePage(newPage(application.getPages().getHomePage()));
			}
			catch (WicketRuntimeException e)
			{
				throw new WicketRuntimeException("Could not create home page", e);
			}
			return true;
		}
		return false;
	}

	/**
	 * Invokes a given interface on a component.
	 * 
	 * @param component
	 *            The component
	 * @param interfaceName
	 *            The name of the interface to call
	 */
	private void invokeInterface(final Component component, final String interfaceName)
	{
		// Look up interface to call
		final Method method = getInterfaceMethod(interfaceName);

		try
		{
			// Invoke the interface method on the component
			method.invoke(component, new Object[] { });
		}
		catch (IllegalAccessException e)
		{
			throw new WicketRuntimeException("Cannot access method " + method + " of interface "
					+ interfaceName, e);
		}
		catch (InvocationTargetException e)
		{
			throw new WicketRuntimeException("Method " + method + " of interface " + interfaceName
					+ " threw an exception", e);
		}
	}

	/**
	 * Invokes a given interface on a component on a given page
	 * 
	 * @param page
	 *            The page where the component is
	 * @param path
	 *            The path to the component
	 * @param interfaceName
	 *            The name of the interface to call
	 */
	private void invokeInterface(final Page page, final String path, final String interfaceName)
	{
		// Set the page for the component as the response page
		setResponsePage(page);

		// Invoke interface on the component at the given path on the page
		final Component component = page.get(Strings.afterFirstPathComponent(path, '.'));
		if (component != null)
		{
			// Invoke interface on component
			invokeInterface(component, interfaceName);

			// Set form component values from cookies
			setFormComponentValuesFromCookies(page);
		}
		else
		{
			// Must be an internal error of some kind or someone is hacking
			// around with URLs in their browser.
			log.error("No component found for " + path);
			setResponsePage(newPage(application.getPages().getInternalErrorPage()));
		}
	}

	/**
	 * Creates a new page.
	 * 
	 * @param pageClass
	 *            The page class to instantiate
	 * @return The page
	 * @throws WicketRuntimeException
	 */
	private final Page newPage(final Class pageClass)
	{
		final PageParameters parameters = new PageParameters(getRequest().getParameterMap());
		return session.getPageFactory().newPage(pageClass, parameters);
	}

	/**
	 * Called when the requested page is not available.
	 */
	private void onExpiredPage()
	{
		// Page was expired from session, probably because backtracking
		// limit was reached
		setResponsePage(newPage(application.getPages().getPageExpiredErrorPage()));
	}

	/**
	 * @return True if static content was returned
	 */
	private boolean renderStaticContent()
	{
		try
		{
			// Get the relative URL we need for loading the resource from
			// the servlet context
			final String url = getWebRequest().getRelativeURL();

			// Get servlet context
			final ServletContext context = ((WebApplication)application).getWicketServlet()
					.getServletContext();

			// Set content type
			response.setContentType(context.getMimeType(url));

			// NOTE: Servlet container prevents accessing WEB-INF/** already.
			final InputStream in = context.getResourceAsStream(url);
			if (in != null)
			{
				try
				{
					// Copy resource input stream to servlet output stream
					Streams.copy(in, getWebResponse().getHttpServletResponse()
							.getOutputStream());
				}
				finally
				{
					// NOTE: We close only the InputStream. The app server will
					// close the output stream.
					in.close();
				}
				return true;
			}
			else
			{
				// No static content was found
				return false;
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Cannot load static content for request " + request, e);
		}
	}

	/**
	 * @return Returns resource to user if URL matches resource pattern
	 */
	private boolean resourceReference()
	{
		final String pathInfo = getWebRequest().getPathInfo();
		if (pathInfo.startsWith(resourceReferencePrefix))
		{
			final String resourceReferenceKey = pathInfo.substring(resourceReferencePrefix.length());
			final Resource resource = getApplication().getResource(resourceReferenceKey);
			if (resource == null)
			{
				throw new WicketRuntimeException("Could not find shared resource under key " + resourceReferenceKey);
			}
			else
			{
				resource.onResourceRequested();
			}
			return true;
		}
		return false;
	}
}
