/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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
import java.util.Iterator;

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
import wicket.util.lang.Classes;
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

	/**
	 * Constructor which simply passes arguments to superclass for storage
	 * there.
	 * 
	 * @param application
	 *            The application
	 * @param session
	 *            The session
	 * @param request
	 *            The request
	 * @param response
	 *            The response
	 */
	public WebRequestCycle(final WebApplication application, final WebSession session,
			final WebRequest request, final Response response)
	{
		super(application, session, request, response);
	}

	/**
	 * Returns a bookmarkable URL that references a given page class using a
	 * given set of page parameters. Since the URL which is returned contains
	 * all information necessary to instantiate and render the page, it can be
	 * stored in a user's browser as a stable bookmark.
	 * 
	 * @param pageClass
	 *            Class of page
	 * @param parameters
	 *            Parameters to page
	 * @return Bookmarkable URL to page
	 */
	public String urlFor(final Class pageClass, final PageParameters parameters)
	{
		final StringBuffer buffer = urlPrefix();

		buffer.append("?bookmarkablePage=");
		buffer.append(pageClass.getName());

		if (parameters != null)
		{
			for (final Iterator iterator = parameters.keySet().iterator(); iterator.hasNext();)
			{
				final String key = (String)iterator.next();
				buffer.append('&');
				buffer.append(key);
				buffer.append('=');
				buffer.append(parameters.getString(key));
			}
		}

		return response.encodeURL(buffer.toString());
	}

	/**
	 * Returns a URL that references a given interface on a component. When the
	 * URL is requested from the server at a later time, the interface will be
	 * called. A URL returned by this method will not be stable across sessions
	 * and cannot be bookmarked by a user.
	 * 
	 * @param component
	 *            The component to reference
	 * @param listenerInterface
	 *            The listener interface on the component
	 * @return A URL that encodes a page, component and interface to call
	 */
	public String urlFor(final Component component, final Class listenerInterface)
	{
		// Ensure that component instanceof listenerInterface
		if (!listenerInterface.isAssignableFrom(component.getClass()))
		{
			throw new WicketRuntimeException("The component " + component + " of class "
					+ component.getClass() + " does not implement " + listenerInterface);
		}

		// Buffer for composing URL
		final StringBuffer buffer = urlPrefix();
		buffer.append("?component=");
		buffer.append(component.getPath());
		buffer.append("&rendering=");
		buffer.append(component.getPage().getRendering());
		buffer.append("&interface=");
		buffer.append(Classes.name(listenerInterface));
		return response.encodeURL(buffer.toString());
	}
	
	/**
	 * @param path The path
	 * @return The url for the path
	 */
	public String urlFor(final String path)
	{
        return urlPrefix() + "/" + path;		
	}
	
	/**
	 * @return Prefix for URLs including the context path, servlet path and
	 *         application name (if servlet path is empty).
	 */
	public StringBuffer urlPrefix()
	{
		final StringBuffer buffer = new StringBuffer();

		if (request != null)
		{
			buffer.append(((WebRequest)request).getContextPath());

			final String servletPath = ((WebRequest)request).getServletPath();
			if (servletPath.equals(""))
			{
				buffer.append('/');
				buffer.append(application.getName());
			}
			else
			{
				buffer.append(servletPath);
			}
		}

		return buffer;
	}

	/**
	 * Renders a response for the current request. The following four steps are
	 * followed in rendering a response:
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
	 * request, then the request is considered invalid and a response is written
	 * detailing the problem.
	 */
	protected void onRender()
	{
		// Try different methods of parsing and dispatching the request
		if (callComponentListener() || bookmarkablePage() || homePage())
		{
			// Get page set by handler
			final Page page = getPage();

			// Is there a page to render?
			if (page != null)
			{
				// Should page be redirected to?
				if (getRedirect())
				{
					// Redirect to the page
					redirectToPage(page);
				}
				else
				{
					// Render the page
					page.render();

					// Clear all feedback messages
					page.getFeedbackMessages().clear();
				}
			}
		}
		else
		{
			// Try to respond with static content
			if (!renderStaticContent())
			{
				// No static content could be found
				response.write("<pre>Invalid request: " + request + "</pre>");
			}
		}
	}

	/**
	 * Redirects browser to the given page
	 * 
	 * @param page
	 *            The page to redirect to
	 */
	protected void redirectToPage(final Page page)
	{
		// Redirect to the url for the page
		response.redirect(urlFor(page, IRedirectListener.class));
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
			final Class pageClass = getSession().getClassResolver().resolveClass(pageClassName);
			setPage(getPageFactory().newPage(pageClass,
					new PageParameters(getRequest().getParameterMap())));

			return true;
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
		if (path != null)
		{
			// Get page from path
			log.debug("Getting page for path " + path);
			final Page page = session.getPage(path);

			// Does page exist?
			if (page != null)
			{
				// Is page stale?
				if (page.isStale())
				{
					onStalePage();
					return true;
				}
				else if (page.isRenderingStale(Integer.parseInt(request.getParameter("rendering"))))
				{
					onStaleRendering(page);
					return true;
				}
				else
				{
					invokeInterface(page, path, request.getParameter("interface"));
					return true;
				}
			}
			else
			{
				onExpiredPage();
				return true;
			}
		}
		else
		{
			// Get path info
			final String pathInfo = ((WebRequest)request).getHttpServletRequest().getPathInfo();
			if (pathInfo != null)
			{
				// Get resource for path
				final Resource resource = Resource.forPath(pathInfo.substring(1));
				if (resource != null)
				{
					// Request resource
					resource.onResourceRequested();
					return true;
				}
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
		final String pathInfo = ((WebRequest)request).getPathInfo();

		if (pathInfo == null || "/".equals(pathInfo) || "".equals(pathInfo))
		{
			try
			{
				setPage(newPage(application.getPages().getHomePage()));
			}
			catch (WicketRuntimeException e)
			{
				throw new WicketRuntimeException("Could not create home page", e);
			}

			return true;
		}

		return false;
	}

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

	private void invokeInterface(final Page page, final String path, final String interfaceName)
	{
		// Set the page for the component as the response page
		// and expire any pages in the session cache that are
		// newer than the given page since they will no longer
		// be accessible.
		setPage(page);

		// Invoke interface on the component at the given path on the page
		final Component component = page.get(Strings.afterFirstPathComponent(path, '.'));
		if (component != null)
		{
			// Invoke interface on component
			invokeInterface(component, interfaceName);

			// Set form component values from cookies
			setFormComponentValuesFromCookies(page);

			// If the current page is also the next page or we're redirecting
			if (getPage() != page || getRedirect())
			{
				// detach any models loaded by the component listener
				page.detachModels();
			}
		}
		else
		{
			// Must be an internal error of some kind or someone is hacking
			// around with URLs in their browser.
			log.error("No component found for " + path);
			setPage(newPage(application.getPages().getInternalErrorPage()));
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
		return getPageFactory().newPage(pageClass, parameters);
	}

	/**
	 * Creates a new instance of a page using the given class name.
	 * 
	 * @param pageClass
	 *            The class of page to create
	 * @param page
	 *            Parameter to page constructor
	 * @return The new page
	 * @throws WicketRuntimeException
	 */
	private final Page newPage(final Class pageClass, final Page page)
	{
		return getPageFactory().newPage(pageClass, page);
	}

	private void onExpiredPage()
	{
		// Page was expired from session, probably because backtracking
		// limit was reached
		setPage(newPage(application.getPages().getPageExpiredErrorPage()));
	}

	private void onStalePage()
	{
		// Page was marked stale because the data model for some
		// component on the page is stale. Find the most recent
		// fresh page and send the user there.
		final Page freshestPage = session.getFreshestPage();

		if (freshestPage != null)
		{
			setPage(newPage(application.getPages().getStaleDataErrorPage(), freshestPage));
		}
		else
		{
			setPage(newPage(application.getPages().getHomePage()));
		}
	}

	private void onStaleRendering(final Page page)
	{
		// Just a particular rendering of the page is stale, so send
		// the user back to the page
		setPage(newPage(application.getPages().getStaleDataErrorPage(), page));
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
			final String url = ((WebRequest)getRequest()).getRelativeURL();

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
					Streams.writeStream(in, ((WebResponse)response).getHttpServletResponse()
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
}