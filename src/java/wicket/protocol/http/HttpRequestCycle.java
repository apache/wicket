/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import wicket.IApplication;
import wicket.IRedirectListener;
import wicket.Page;
import wicket.PageParameters;
import wicket.RenderException;
import wicket.RequestCycle;
import wicket.Response;
import wicket.markup.html.form.Form;
import wicket.response.NullResponse;
import wicket.util.io.Streams;
import wicket.util.lang.Classes;
import wicket.util.string.Strings;

/**
 * Request cycle implementation for HTTP protocol.
 * 
 * @author Jonathan Locke
 */
public class HttpRequestCycle extends RequestCycle
{ // TODO finalize javadoc
	/** Logging object */
	private static final Log log = LogFactory.getLog(HttpRequestCycle.class);

	/**
	 * Constructor
	 * @param application The application
	 * @param session The session
	 * @param request The request
	 * @param response The wicket.response
	 */
	public HttpRequestCycle(final IApplication application, final HttpSession session,
			final HttpRequest request, final Response response)
	{
		super(application, session, request, response);
	}

	/**
     * Returns a bookmarkable URL that references a given page class using
     * a given set of page parameters 
	 * @param pageClass Class of page
	 * @param parameters Parameters to page
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
				final String key = (String) iterator.next();

				buffer.append('&');
				buffer.append(key);
				buffer.append('=');
				buffer.append(parameters.getString(key));
			}
		}

		return response.encodeURL(buffer.toString());
	}

	/**
     * Returns a URL that references a given interface on a component.  When
     * the URL is requested from the server at a later time, the interface 
     * will be called.
	 * @param component The component to reference
	 * @param listenerInterface The listener interface on the component
	 * @return A URL that encodes a page, component and interface to call
	 */
	public String urlFor(final Component component, final Class listenerInterface)
	{
        // Ensure that component instanceof listenerInterface
        if (!listenerInterface.isAssignableFrom(component.getClass()))
        {
            throw new RenderException("The component " + component + " of class " +
                    component.getClass() + " does not implement " + listenerInterface);
        }

        // Compose the URL
		final StringBuffer buffer = urlPrefix();

		buffer.append("?component=");
		buffer.append(component.getPath());
		buffer.append("&rendering=");
		buffer.append(component.getPage().getRendering());
		buffer.append("&interface=");
		buffer.append(Classes.name(listenerInterface));

        // Return the encoded URL
		return response.encodeURL(buffer.toString());
	}

	/**
	 * Renders a response for the current request.  The following four steps are 
     * followed in rendering a response:
     * <p>
     * 1. If the URL requested is in the form of a component listener invocation, 
     *    then that invocation will occur and is expected to generate a response.  
     * <p>
     * 2. If the URL is to a bookmarkable page, then an instance of that page 
     *    will render a response.  
     * <p>
     * 3. If the URL is for the application's home page, an instance of the home
     *    page will render a response.
     * <p>
     * 4. Finally, an attempt is made to render the requested resource as static 
     *    content, available through the servlet context.  
     * <p>
     * If all four steps are executed and content cannot be found to satisfy the
     * request, then the request is considered invalid and a response is written 
     * detailing the problem.
	 */
	protected void handleRender()
	{
		// Dispatch to component listener or handle as bookmarkable page 
        // or home page
		synchronized (session)
		{
			if (callComponentListener() || bookmarkablePage() || homePage())
			{
				// Get page set by handler
				Page page = getPage();
                
				// Is there a page to render?
				if (page != null)
				{
					// Should page be redirected to?
					if (getRedirect())
					{
						// redirect to the page
						redirectToPage(page);
					}
					else
					{
						// render the page
						page.render(this);
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
	}

	/**
	 * @return A copy of this request cycle object with a NULL response
	 */
	protected RequestCycle nullResponse()
	{
		return new HttpRequestCycle(application, (HttpSession)session, (HttpRequest)request,
				NullResponse.getInstance());
	}

	/**
	 * Redirects browser to the given page
	 * @param page The page to redirect to
	 */
	protected void redirectToPage(final Page page)
	{
		// Redirect to the url for the page
		response.redirect(urlFor(page, IRedirectListener.class));
	}

	/**
	 * Sets values for form components based on cookie values in the request.
	 * @param page the current page
	 */
	final void setFormComponentValuesFromCookies(final Page page)
	{
		// May be there is some other means I don't know. But I need access
		// to 'this' from inside the anonymous inner class.
		final RequestCycle cycle = this;

		// Visit all Forms contained in the page
		page.visitChildren(Form.class, new Component.IVisitor()
		{
			// For each FormComponent found on the Page (not Form)
			public Object component(final Component component)
			{
				((Form)component).setFormComponentValuesFromPersister(cycle);
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Activates a bookmarkable page if one was specified in the request.
	 * @return True if a bookmarkable page was created and returned for the request.
	 * @throws RenderException
	 */
	private boolean bookmarkablePage()
	{
		// Get any component parameter
		final String pageClassName = request.getParameter("bookmarkablePage");

		if (pageClassName != null)
		{
			setPage(getPageFactory().newPage(pageClassName,
					new PageParameters(this.getRequest().getParameterMap())));

			return true;
		}

		return false;
	}

	/**
	 * Calls a component listener interface on a page that already exists in the session.
	 * The session component is found using the path in the 'component' parameter of the
	 * request. The interface method to be called is determined by the 'interface'
	 * parameter of the request. The interface can only be one of the interfaces listed in
	 * the secureInterfaceMethods map in the RequestDispatcher implementation.
	 * @return True if the component listener was successfully called
	 * @throws RenderException
	 */
	private boolean callComponentListener()
	{
		// Get any component parameter
		final String path = request.getParameter("component");

		if (path != null)
		{
			// Get page where component resides
			log.debug("Getting page " + path);

			// Get page from path
			final Page page = session.getPage(path);

			// Get the rendering of the page
			final int rendering = Integer.parseInt(request.getParameter("rendering"));

			// Does page exist?
			if (page != null)
			{
				// Is page stale?
				if (page.isStale())
				{
					// Page was marked stale because the data model for some
					// component on the page is stale
					// Find the most recent fresh page and send the user there
					final Page freshestPage = session.getFreshestPage();

					if (freshestPage != null)
					{
						setPage(newPage(application.getSettings().getStaleDataErrorPage(),
								freshestPage));
					}
					else
					{
						setPage(newPage(application.getSettings().getHomePage()));
					}

					return true;
				}
				else if (page.isRenderingStale(rendering))
				{
					// Just a particular rendering of the page is stale, so send
					// the user back to the page
					setPage(newPage(application.getSettings().getStaleDataErrorPage(), page));

					return true;
				}
				else
				{
					// Get the component at the given path on the page
					final Component component = page
							.get(Strings.afterFirstPathComponent(path, '.'));

					// Got component?
					if (component != null)
					{
						// Set the page for the component as the wicket.response page
						// and expire any
						// pages in the session cache that are newer than the
						// given page since
						// they will no longer be accessible.
						setPage(page);
						session.expireNewerThan(page);

						// Look up interface to call
						final String interfaceName = request.getParameter("interface");
						final Method method = getInterfaceMethod(interfaceName);

						try
						{
							// Invoke the interface method on the component
							method.invoke(component, new Object[] {this});
						}
						catch (IllegalAccessException e)
						{
							throw new RenderException("Cannot access method "
									+ method + " of interface " + interfaceName, e);
						}
						catch (InvocationTargetException e)
						{
							throw new RenderException("Method "
									+ method + " of interface " + interfaceName
									+ " threw an exception", e);
						}

						// Set form component values from cookies
						setFormComponentValuesFromCookies(page);
						return true;
					}
					else
					{
						// If the page is in the session and is not stale, then
						// the
						// component in question should exist. Therefore, we
						// should not
						// get here. So it must be an internal error of some
						// kind or
						// someone is hacking around with URLs in their browser.
						log.error("No component found for " + path);
						setPage(newPage(application.getSettings().getInternalErrorPage()));

						return true;
					}
				}
			}
			else
			{
				// Page was expired from session, probably because backtracking
				// limit was reached
				setPage(newPage(application.getSettings().getPageExpiredErrorPage()));

				return true;
			}
		}

		return false;
	}

	/**
	 * If no context path was provided, activates the home page.
	 * @return True if the home page was activated
	 * @throws RenderException
	 */
	private boolean homePage()
	{
		final String pathInfo = ((HttpRequest) request).getPathInfo();

		if ((pathInfo == null) || "/".equals(pathInfo) || "".equals(pathInfo))
		{
			try
			{
				setPage(newPage(application.getSettings().getHomePage()));
			}
			catch (RenderException e)
			{
				throw new RenderException("Could not create home page", e);
			}

			return true;
		}

		return false;
	}

	/**
	 * @return True if static content was returned
	 */
	private boolean renderStaticContent()
	{
		try
		{
			// Get URL
			final String url = ((HttpRequest)getRequest()).getURL();

			// Get servlet context
			final ServletContext context = ((HttpApplication)application).getServletContext();

			// Set content type
			response.setContentType(context.getMimeType(url));

			// NOTE: Servlet container prevents accessing WEB-INF/** already.
			// TODO Maybe a kind of exclude/include list would good as well.
			final InputStream in = context.getResourceAsStream(url);
			if (in != null)
			{
				try
				{
					// Copy resource input stream to servlet output stream
					Streams.writeStream(in, ((HttpResponse)response).getServletResponse()
							.getOutputStream());
				}
				finally
				{
					// NOTE: Do not close servlet OutputStream. Close
					// InputStream only
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
            throw new RenderException("Cannot load static content for request " + request, e);
		}
	}

	/**
	 * @return Prefix for URLs
	 */
	public StringBuffer urlPrefix()
	{
		final StringBuffer buffer = new StringBuffer();

		if (request != null)
		{
			buffer.append(((HttpRequest)request).getContextPath());

			final String servletPath = ((HttpRequest)request).getServletPath();

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
}

// /////////////////////////////// End of File /////////////////////////////////
