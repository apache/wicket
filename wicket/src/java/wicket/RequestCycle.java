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
package wicket;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.pages.ExceptionErrorPage;
import wicket.util.lang.Classes;
import wicket.util.string.Strings;

/**
 * THIS CLASS IS DELIBERATELY NOT INSTANTIABLE BY FRAMEWORK CLIENTS AND IS NOT
 * INTENDED TO BE SUBCLASSED BY FRAMEWORK CLIENTS.
 * <p>
 * Represents the request cycle, including the applicable application, page,
 * request, response and session.
 * <p>
 * Convenient container for an application, session, request and response object
 * for a page request cycle. Each of these properties can be retrieved with the
 * corresponding getter method. In addition, getPage and setPage can be used to
 * access the page property of the RequestCycle, which determines what page is
 * rendered back to the requester. The setRedirect() method determines if the
 * page should be rendered directly back to the browser or if the browser should
 * instead be redirected to the page (which then renders itself). The actual
 * rendering of the cycle's page is an implementation detail and occurs when the
 * render() method of RequestCycle is called by the framework. The render()
 * method is only public to allow invocation from implementation packages and
 * should never be called directly by clients of the framework.
 * <p>
 * The abstract urlFor() methods are implemented by subclasses of RequestCycle
 * and return encoded page URLs. The URL returned depends on the kind of page
 * being linked to. Pages broadly fall into two categories:
 * <p>
 * <table>
 * <tr>
 * <td valign = "top"><b>1. </b></td>
 * <td>A page that does not yet exist in a user Session may be encoded as a URL
 * that references the not-yet-created page by class name. A set of
 * PageParameters can also be encoded into the URL, and these parameters will be
 * passed to the page constructor if the page later needs to be instantiated.
 * <p>
 * Any page of this type is bookmarkable, and a hint to that effect is given to
 * the user in the URL:
 * <p>
 * <ul>
 * /[Application]?bookmarkablePage=[classname]&[param]=[value] [...]
 * </ul>
 * <p>
 * Bookmarkable pages must implement a constructor that takes a PageParameters
 * argument. Links to bookmarkable pages are created by calling the
 * urlFor(Class, PageParameters) method, where Class is the page class and
 * PageParameters are the parameters to encode into the URL.
 * <p>
 * </td>
 * </tr>
 * <tr>
 * <td valign = "top"><b>2. </b></td>
 * <td>Stateful pages (that have already been requested by a user) will be
 * present in the user's Session and can be referenced securely with a
 * session-relative number:
 * <p>
 * <ul>
 * /[Application]?component=[pageId]
 * </ul>
 * <p>
 * Often, the reason to access an existing session page is due to some kind of
 * "postback" (either a link click or a form submit) from a page (possibly
 * accessed with the browser's back button or possibly not). A call to a
 * registered listener is dispatched like so:
 * <p>
 * <ul>
 * /[Application]?component=[pageId.componentPath]&interface=[interface]
 * </ul>
 * <p>
 * For example:
 * <p>
 * <ul>
 * /[Application]?component=3.signInForm.submit&interface=IFormSubmitListener
 * </ul>
 * </td>
 * </tr>
 * </table>
 * <p>
 * URLs for stateful pages (those that already exist in the session map) are
 * created by calling the urlFor(Component, Class) method, where Component is
 * the component being linked to and Class is the interface on the component to
 * call.
 * <p>
 * For pages falling into the second category, listener interfaces cannot be
 * invoked unless they have first been registered via the static
 * registerSecureInterface() method. This method ensures basic security by
 * restricting the set of interfaces that outsiders can call via GET and POST
 * requests. Each listener interface has a single method which takes only a
 * RequestCycle parameter. Currently, the following classes register the
 * following kinds of listener interfaces:
 * <p>
 * <table>
 * <tr>
 * <th align = "left">Class</th>
 * <th align = "left">Interface</th>
 * <th align="left">Purpose</th>
 * </tr>
 * <tr>
 * <td>Form</td>
 * <td>IFormSubmitListener</td>
 * <td>Handle form submits</td>
 * </tr>
 * <tr>
 * <td>Image</td>
 * <td>IResourceListener</td>
 * <td>Respond to image resource requests</td>
 * </tr>
 * <tr>
 * <td>Link</td>
 * <td>ILinkListener</td>
 * <td>Respond to link clicks</td>
 * </tr>
 * <tr>
 * <td>Page</td>
 * <td>IRedirectListener</td>
 * <td>Respond to redirects</td>
 * </tr>
 * </table>
 * <p>
 * The redirectToInterceptPage() and continueToOriginalDestination() methods can
 * be used to temporarily redirect a user to some page. This is mainly intended
 * for use in signing in users who have bookmarked a page inside a site that
 * requires the user be authenticated before they can access the page. When it
 * is discovered that the user is not signed in, the user is redirected to the
 * sign-in page with redirectToInterceptPage(). When the user has signed in,
 * they are sent on their way with continueToOriginalDestination(). These
 * methods could also be useful in "interstitial" advertising or other kinds of
 * "intercepts".
 * <p>
 * 
 * @author Jonathan Locke
 */
public abstract class RequestCycle
{
	/** Map from class name to Constructor. */
	private static final Map constructors = new HashMap();

	/** Thread-local that holds the current request cycle. */
	private static final ThreadLocal current = new ThreadLocal();

	/** Map from interface Class to Method. */
	private static final Map listenerInterfaceMethods = new HashMap();

	/** Log */
	private static final Log log = LogFactory.getLog(RequestCycle.class);

	/** The application object. */
	protected final Application application;

	/** The current request. */
	protected Request request;

	/** The current response. */
	protected Response response;

	/** The session object. */
	protected final Session session;

	/** The page to render to the user. */
	private Page responsePage;

	/**
	 * If the page is set to null, we'll first set the current page to this
	 * variable. We use this in order to be able to release resources on the
	 * page and its components.
	 */
	private Page pageBackup;

	/**
	 * True if request should be redirected to the resulting page instead of
	 * just rendering it back to the user.
	 */
	private boolean redirect;

	/**
	 * Gets request cycle for calling thread.
	 * 
	 * @return Request cycle for calling thread
	 */
	public final static RequestCycle get()
	{
		return (RequestCycle)current.get();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Adds an interface to the map of interfaces that can be invoked by
	 * outsiders. The interface must have a single method with the signature
	 * methodName(RequestCycle).
	 * 
	 * @param i
	 *            The interface class, which must extend IRequestListener.
	 */
	public static void registerRequestListenerInterface(final Class i)
	{
		// Ensure that i extends IRequestListener
		if (!IRequestListener.class.isAssignableFrom(i))
		{
			throw new IllegalArgumentException("Class " + i + " must extend IRequestListener");
		}

		// Get interface methods
		final Method[] methods = i.getMethods();

		// If there is only one method
		if (methods.length == 1)
		{
			// and that method takes no parameters
			if (methods[0].getParameterTypes().length == 0)
			{
				// Save this interface method by the non-qualified class name
				listenerInterfaceMethods.put(Classes.name(i), methods[0]);
			}
			else
			{
				throw new IllegalArgumentException("Method in interface " + i
						+ " cannot have parameters");
			}
		}
		else
		{
			throw new IllegalArgumentException("Interface " + i + " can have only one method");
		}
	}

	/**
	 * Constructor.
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
	protected RequestCycle(final Application application, final Session session,
			final Request request, final Response response)
	{
		this.application = application;
		this.session = session;
		this.request = request;
		this.response = response;

		// Set this RequestCycle into ThreadLocal variable
		current.set(this);
	}

	/**
	 * Gets the application object.
	 * 
	 * @return Application interface
	 */
	public final Application getApplication()
	{
		return application;
	}

	/**
	 * Gets the current page.
	 * 
	 * @return The page
	 */
	public final Page getResponsePage()
	{
		return responsePage;
	}

	/**
	 * Gets whether the page for this request should be redirected.
	 * 
	 * @return whether the page for this request should be redirected
	 */
	public final boolean getRedirect()
	{
		return redirect;
	}

	/**
	 * Gets the request.
	 * 
	 * @return Request object
	 */
	public final Request getRequest()
	{
		return request;
	}

	/**
	 * Gets the response.
	 * 
	 * @return Response object
	 */
	public final Response getResponse()
	{
		return response;
	}

	/**
	 * Gets the session.
	 * 
	 * @return Session object
	 */
	public final Session getSession()
	{
		return session;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Responds to a request.
	 * 
	 * @throws ServletException
	 */
	public final void request() throws ServletException
	{
		// Serialize renderings on the session object so that only one page
		// can be rendered at a time for a given session.
		synchronized (session)
		{
			// Attach thread local resources for request
			threadAttach();
			
			// Any page used to respond to the request
			Page page = null;
			
			// Response is beginning
			internalOnBeginRequest();
			onBeginRequest();

			try
			{
				// If subclass response to request set a Page to render
				if (onRespond())
				{
					// Get page set by subclass response
					page = getResponsePage();
					if (page != null)
					{
						try
						{
							// Should page be redirected to?
							if (getRedirect())
							{
								// Redirect to the page
								redirectTo(page);
							}
							else
							{
								// Render the page
								page.render();
							}
						}
						finally
						{
							// Response is ending
							internalOnEndRequest();
							onEndRequest();

							// The request is over
							page.internalOnEndRequest();
							page.onEndRequest();
						}
					}
				}
				else
				{
					// Response is ending
					internalOnEndRequest();
					onEndRequest();
				}
			}
			catch (RuntimeException e)
			{
				// Handle any runtime exception
				onRuntimeException(page, e);
			}
			finally
			{
				// Release thread local resources
				threadDetach();
			}
		}
	}

	/**
	 * Convenience method that sets page on response object.
	 * 
	 * @param page
	 *            The page to render as a response
	 */
	public final void setResponsePage(final Page page)
	{
		this.responsePage = page;
	}

	/**
	 * Sets whether the page for this request should be redirected.
	 * 
	 * @param redirect
	 *            True if the page for this request cycle should be redirected
	 *            to rather than directly rendered.
	 */
	public final void setRedirect(final boolean redirect)
	{
		this.redirect = redirect;
	}

	/**
	 * @param request
	 *            The request to set.
	 */
	public void setRequest(Request request)
	{
		this.request = request;
	}

	/**
	 * Sets response.
	 * 
	 * @param response
	 *            The response
	 */
	public void setResponse(final Response response)
	{
		this.response = response;
	}

	/**
	 * Looks up an interface method by name.
	 * 
	 * @param interfaceName
	 *            The interface name
	 * @return The method
	 * @throws WicketRuntimeException
	 */
	protected final Method getInterfaceMethod(final String interfaceName)
	{
		final Method method = (Method)listenerInterfaceMethods.get(interfaceName);
		if (method == null)
		{
			throw new WicketRuntimeException("Attempt to access unknown interface " + interfaceName);
		}
		return method;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR OVERRIDE
	 * THIS METHOD.
	 * 
	 * Called when the request cycle object is beginning its response
	 */
	protected void internalOnBeginRequest()
	{
		// Before the beginning of the response, we need to update
		// our session based on any information that might be in
		// session attributes
		session.updateSession();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR OVERRIDE
	 * THIS METHOD.
	 * 
	 * Called when the request cycle object has finished its response
	 */
	protected void internalOnEndRequest()
	{
		// At the end of our response, we need to set any session
		// attributes that might be required to update the cluster
		session.updateCluster();
	}

	/**
	 * Called when the request cycle object is beginning its response
	 */
	protected void onBeginRequest()
	{
	}

	/**
	 * Called when the request cycle object has finished its response
	 */
	protected void onEndRequest()
	{
	}

	/**
	 * Parses a request when this request cycle is asked to respond.
	 * 
	 * @return True if a Page should be rendered back to the user
	 */
	protected abstract boolean onRespond();

	/**
	 * Redirects browser to the given page.
	 * 
	 * @param page
	 *            The page to redirect to
	 */
	protected abstract void redirectTo(final Page page);

	/**
	 * Sets up to handle a runtime exception thrown during rendering
	 * 
	 * @param page
	 *            Any page context where the exception was thrown
	 * @param e
	 *            The exception
	 * @throws ServletException
	 *             The exception rethrown for the servlet container
	 */
	private final void onRuntimeException(final Page page, final RuntimeException e)
			throws ServletException
	{
		try
		{
			// If application doesn't want debug info showing up for users
			ApplicationSettings settings = application.getSettings();
			if (settings.getUnexpectedExceptionDisplay() != ApplicationSettings.SHOW_NO_EXCEPTION_PAGE)
			{
				if (settings.getUnexpectedExceptionDisplay() == ApplicationSettings.SHOW_INTERNAL_ERROR_PAGE)
				{
					// use internal error page
					setResponsePage(session.getPageFactory(page).newPage(
							application.getPages().getInternalErrorPage()));
				}
				else
				{
					// otherwise show full details
					setResponsePage(new ExceptionErrorPage(e, getResponsePage()));
				}

				// We generally want to redirect the response because we were
				// in the middle of rendering and the page may end up looking
				// like spaghetti otherwise
				redirectTo(getResponsePage());
			}
		}
		catch (RuntimeException ignored)
		{
			// We ignore any problems trying to render the exception display
			// page because we are just going to rethrow the exception anyway
			// and the original problem will be displayed on the console by
			// the container. It's better this way because users of the
			// framework will not want to be distracted by any internal problems
			// rendering a runtime exception error display page.
		}

		// Reset page for re-rendering after exception
		final Page currentPage = getResponsePage();

		// Could be null when it expired
		if (currentPage != null)
		{
			currentPage.resetMarkupStreams();
		}

		// Rethrow error for console / container
		throw new ServletException("Wicket could not render page: " + Strings.toString(e), e);
	}

	/**
	 * Attach thread
	 */
	private void threadAttach()
	{
		// Restore the transient application association with the session
		session.setApplication(application);

		// Set this request cycle as the active request cycle for the
		// session for easy access by the page being rendered and any
		// components on that page
		session.setRequestCycle(this);
	}

	/**
	 * Releases the current thread local related resources. The threadlocal of
	 * this request cycle is reset. If we are in a 'redirect' state, we do not
	 * want to lose our messages as - e.g. when handling a form - there's a fat
	 * chance we are coming back for the rendering of it.
	 */
	private void threadDetach()
	{
		if (getRedirect())
		{
			// Since we are explicitly redirecting to a page already, we do not
			// want a second redirect to occur automatically
			setRedirect(false);
		}

		// Clear ThreadLocal reference
		current.set(null);

		// Set the active request cycle back to null since we are
		// done rendering the requested page
		session.setRequestCycle(null);

		// This thread is no longer attached to a Session
		Session.set(null);
	}
}
