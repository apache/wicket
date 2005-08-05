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
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.pages.ExceptionErrorPage;
import wicket.util.lang.Classes;
import wicket.util.resource.IResourceStream;
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
 * Bookmarkable pages must either implement a constructor that takes a PageParameters
 * argument or a default constructor. If a Page has both constructors the constuctor with
 * the PageParameters argument will be used. Links to bookmarkable pages are created by
 * calling the urlFor(Class, PageParameters) method, where Class is the page class and
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
 * /[Application]?path=[pageId]
 * </ul>
 * <p>
 * Often, the reason to access an existing session page is due to some kind of
 * "postback" (either a link click or a form submit) from a page (possibly
 * accessed with the browser's back button or possibly not). A call to a
 * registered listener is dispatched like so:
 * <p>
 * <ul>
 * /[Application]?path=[pageId.componentPath]&interface=[interface]
 * </ul>
 * <p>
 * For example:
 * <p>
 * <ul>
 * /[Application]?path=3.signInForm.submit&interface=IFormSubmitListener
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

	/** Map from request interface Class to Method. */
	private static final Map listenerRequestInterfaceMethods = new HashMap();
	
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

	/** The page to render to the user*/
	private Page responsePage;

	/** The class of a page to render to the user, redirect as a bookmarkable page */
	private Class responsePageClass;

	/** The page parameters used by the responsepage class to generate its bookmarkable page url*/
	private PageParameters responsePageParams;

	/** True if the cluster should be updated */
	private boolean updateCluster;
	private Page invokePage;

	private IResourceStream responseStream;


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
	 * outsiders. The interface must extend IRequestListener
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
				listenerRequestInterfaceMethods.put(Classes.name(i), methods[0]);
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
	 * @param session
	 *            The session
	 * @param request
	 *            The request
	 * @param response
	 *            The response
	 */
	protected RequestCycle(final Session session, final Request request, final Response response)
	{
		this.application = session.getApplication();
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
	 * Gets the current respond page.
	 * 
	 * @return The page
	 */
	public final Page getResponsePage()
	{
		return responsePage;
	}

	/**
	 * Gets the current responde page page parameters (only used with the responsepage class).
	 * 
	 * @return The page
	 */
	public final PageParameters getResponsePagePageParameters()
	{
		return responsePageParams;
	}

	/**
	 * Gets the current responde page class.
	 * 
	 * @return The page
	 */
	public final Class getResponsePageClass()
	{
		return responsePageClass;
	}
	
	/**
	 * Gets the page that was used for invoking and interface.
	 * 
	 * @return The page
	 */
	protected final Page getInvokePage()
	{
		return invokePage;
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
			try
			{
				// Attach thread local resources for request
				threadAttach();

				// Response is beginning
				internalOnBeginRequest();
				onBeginRequest();

				// If request is parsed successfully
				if (parseRequest())
				{
					// respond with a page
					respond();
				}

			}
			catch (RuntimeException e)
			{
				// Handle any runtime exception
				internalOnRuntimeException(null, e);
			}
			finally
			{
				// make sure the invokerPage is ended correctly.
				try 
				{
					if (invokePage != null) 
					{
					    invokePage.internalEndRequest();
					}
				} 
				catch (RuntimeException e) 
				{
					log.error("Exception occurred during invokerPage.internalEndRequest", e);
				}

				// Response is ending
				try 
				{
					internalOnEndRequest();
				} 
				catch (RuntimeException e) 
				{
					log.error("Exception occurred during internalOnEndRequest", e);
				}
				
				try 
				{
					onEndRequest();
				} 
				catch (RuntimeException e) 
				{
					log.error("Exception occurred during onEndRequest", e);
				}
				
				// Release thread local resources
				threadDetach();
			}
		}
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
	public final void setRequest(Request request)
	{
		this.request = request;
	}

	/**
	 * Sets response.
	 * 
	 * @param response
	 *            The response
	 */
	public final void setResponse(final Response response)
	{
		this.response = response;
	}

	/**
	 * Convenience method that sets page on response object.
	 * 
	 * @param page
	 *            The page to render as a response
	 */
	public final void setResponsePage(final Page page)
	{
		if (responsePageClass != null)
		{
			if(log.isDebugEnabled())
			{
				log.warn("overwriting response page " + responsePageClass + " with " + page);
			}
		}
		else if (responsePage != null)
		{
			if(log.isDebugEnabled())
			{
				log.warn("overwriting response page " + responsePage + " with " + page);
			}
		}

		this.responsePage = page;
		// reset response page class, only one of the 2 responses should be set.
		this.responsePageClass = null;
		this.responsePageParams = null;
	}
	
	/**
	 * Convenience method that sets page class as the response.
	 * This will generate a redirect to the page with a bookmarkable url
	 * 
	 * @param pageClass
	 *            The page class to render as a response
	 */
	public final void setResponsePage(final Class pageClass)
	{
		setResponsePage(pageClass, null);
	}

	/**
	 * Convenience method that sets page class as the response.
	 * This will generate a redirect to the page with a bookmarkable url and its pageparameters.
	 * 
	 * @param pageClass
	 *            The page class to render as a response
	 * @param pageParameters 
	 * 			  The page parameters that gets appended to the bookmarkable url,
	 */
	public final void setResponsePage(final Class pageClass, final PageParameters pageParameters)
	{
		if (responsePageClass != null)
		{
			if(log.isDebugEnabled())
			{
				log.warn("overwriting response page " + responsePageClass + " with " + pageClass);
			}
		}
		else if (responsePage != null)
		{
			if(log.isDebugEnabled())
			{
				log.warn("overwriting response page " + responsePage + " with " + pageClass);
			}
		}

		if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("Class must be a subclass of Page");
		}
		this.responsePageClass = pageClass;
		this.responsePageParams = pageParameters;
		// reset response page, only one of the 2 responses should be set.
		this.responsePage = null;
	}
	

	/**
	 * Sets the page to invoke.
	 * @param page
	 */	
	protected final void setInvokePage(final Page page)
	{
		this.invokePage = page;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * @param updateCluster
	 *            The updateCluster to set.
	 */
	public void setUpdateCluster(boolean updateCluster)
	{
		this.updateCluster = updateCluster;
	}

	/**
	 * Looks up an request interface method by name.
	 * 
	 * @param interfaceName
	 *            The interface name
	 * @return The method, null of nothing is found
	 * 
	 */
	protected final Method getRequestInterfaceMethod(final String interfaceName)
	{
		return (Method)listenerRequestInterfaceMethods.get(interfaceName);
	}
	
	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR OVERRIDE
	 * THIS METHOD.
	 * 
	 * Called when the request cycle object is beginning its response
	 */
	protected final void internalOnBeginRequest()
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
	protected final void internalOnEndRequest()
	{
		if (updateCluster)
		{
			// At the end of our response, we need to set any session
			// attributes that might be required to update the cluster
			session.updateCluster();
		}
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
	protected abstract boolean parseRequest();

	/**
	 * Redirects browser to the given page.
	 * NOTE: Usually, you should never call this method directly, but work with
	 * setResponsePage instead. This method is part of Wicket's internal
	 * behaviour and should only be used when you want to circumvent the normal
	 * framework behaviour and issue the redirect directly.
	 *
	 * @param page
	 *            The page to redirect to
	 * @throws ServletException 
	 */
	protected abstract void redirectTo(final Page page) throws ServletException;


	/**
	 * Sets up to handle a runtime exception thrown during rendering.
	 * FRAMEWORK CLIENTS SHOULD NOT CALL THIS METHOD.
	 * 
	 * @param page
	 *            Any page context where the exception was thrown
	 * @param e
	 *            The exception
	 * @throws ServletException
	 *             The exception rethrown for the servlet container
	 */
	protected final void internalOnRuntimeException(final Page page, final RuntimeException e)
			throws ServletException
	{
		// let client handle any specifics
		onRuntimeException(page, e);

		log.error("Unexpected runtime exception [page = " + page + "]", e);

		e.printStackTrace();
		// Reset page for re-rendering after exception
		if (page != null)
		{
			page.resetMarkupStreams();
		}

		// If the page we failed to render is an error page
		if (page != null && page.isErrorPage())
		{
			// give up while we're ahead!
			throw new ServletException("Internal Error: Could not render error page " + page, e);
		}
		else
		{
			try
			{
				redirectToExceptionErrorPage(page, e);
			}
			catch (RuntimeException e2)
			{
				throw new ServletException(
						"Internal Error: Could not redirect to exception error page.  Was trying to display exception for page "
								+ page + ":\n" + Strings.toString(e), e2);
			}
		}
	}

	/**
	 * Template method that is called when a runtime exception is thrown, just before the actual
	 * handling of the runtime exception.
	 * @param page Any page context where the exception was thrown
	 * @param e The exception
	 */
	protected void onRuntimeException(final Page page, final RuntimeException e)
	{
	}

	/**
	 * @param page
	 *            The page that went wrong
	 * @param e
	 *            The exception that was thrown
	 * @throws ServletException 
	 */
	private final void redirectToExceptionErrorPage(final Page page, final RuntimeException e) throws ServletException
	{
		// If application doesn't want debug info showing up for users
		final ApplicationSettings settings = application.getSettings();
		if (settings.getUnexpectedExceptionDisplay() != ApplicationSettings.SHOW_NO_EXCEPTION_PAGE)
		{
			Class internalErrorPageClass = application.getPages().getInternalErrorPage();
			Page responsePage = getResponsePage();
			Class responseClass = responsePage != null?responsePage.getClass():null;
			
			if (responseClass != internalErrorPageClass && settings.getUnexpectedExceptionDisplay() == ApplicationSettings.SHOW_INTERNAL_ERROR_PAGE)
			{
				// Show internal error page
				setResponsePage(session.getPageFactory(page).newPage(internalErrorPageClass));
			}
			else if(responseClass != ExceptionErrorPage.class)
			{
				// Show full details
				setResponsePage(new ExceptionErrorPage(e, getResponsePage()));
			}
			else
			{
				// give up while we're ahead!
				throw new ServletException("Internal Error: Could not render error page " + page, e);
			}

			// We generally want to redirect the response because we
			// were in the middle of rendering and the page may end up
			// looking like spaghetti otherwise
			redirectTo(getResponsePage());
		}
	}

	/**
	 * Respond with response page
	 * 
	 * @throws ServletException
	 */
	private final void respond() throws ServletException
	{
		// Get any page that is to be used to respond to the request
		final Page page = getResponsePage();
		if (page != null)
		{
			// add/touch the response page in its pagemap.
			page.getPageMap().put(page);
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
					// test if the invoker page was the same as the page that is going to be rendered
					if (getInvokePage() == getResponsePage())
					{
						// set it to null because it is already ended inthe page.doRender()
						setInvokePage(null);
					}
					// Let page render itself
					page.doRender();
				}
			}
			catch (RuntimeException e)
			{
				// Handle any runtime exception
				internalOnRuntimeException(page, e);
			}
		}
		else
		{
			final Class pageClass = getResponsePageClass();
			if (pageClass != null)
			{
				final PageParameters pageParameters = getResponsePagePageParameters();
				String redirectUrl = urlFor(pageClass, pageParameters);
				getResponse().redirect(redirectUrl);
			}
		}
	}
	
	
	/**
	 * Returns a bookmarkable URL that references a given page class using a given set of
	 * page parameters. Since the URL which is returned contains all information necessary
	 * to instantiate and render the page, it can be stored in a user's browser as a
	 * stable bookmark.
	 * @param pageClass Class of page
	 * @param parameters Parameters to page
	 * @return Bookmarkable URL to page
	 */
	public String urlFor(final Class pageClass,
			final PageParameters parameters)
	{
		if (pageClass == null)
		{
			throw new NullPointerException("argument pageClass may not be null");
		}

		final StringBuffer buffer = urlPrefix();
		buffer.append("?bookmarkablePage=");
		String pageReference = application.getPages().aliasForClass(pageClass);
		if (pageReference == null) pageReference = pageClass.getName();
		buffer.append(pageReference);
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
		return getResponse().encodeURL(buffer.toString());
	}
	
	/**
	 * Creates a prefix for a url.
	 * @return Prefix for URLs including the context path and servlet path.
	 */
	protected abstract StringBuffer urlPrefix();



	/**
	 * Attach thread
	 */
	private final void threadAttach()
	{
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
	private final void threadDetach()
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

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "RequestCycle" + "@" + Integer.toHexString(hashCode()) +
				"{thread=" + Thread.currentThread().getName() + "}";
	}
}
