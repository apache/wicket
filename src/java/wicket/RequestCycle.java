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

package wicket;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.ExceptionErrorPage;
import wicket.util.lang.Classes;

/**
 * Represents the request cycle.
 * THIS CLASS IS DELIBERATELY NOT INSTANTIABLE BY FRAMEWORK CLIENTS AND IS NOT INTENDED
 * TO BE SUBCLASSED BY FRAMEWORK CLIENTS.
 * <p>
 * Convenient container for an application, session, request and response object
 * for a page request cycle.  Each of these properties can be retrieved with the
 * corresponding getter method.  In addition, getPage and setPage can be used to
 * access the page property of the RequestCycle, which determines what page is
 * rendered back to the requester.  The setRedirect() method determines if the
 * page should be rendered directly back to the browser or if the browser should
 * instead be redirected to the page (which then renders itself).  The actual
 * rendering of the cycle's page is an implementation detail and occurs when the
 * render() method of RequestCycle is called by the framework.  The render() method
 * is only public to allow invocation from implementation packages and should never
 * be called directly by clients of the framework.
 * <p>
 * The abstract urlFor() methods are implemented by subclasses of RequestCycle and
 * return encoded page URLs.  The URL returned depends on the kind of page being
 * linked to.  Pages broadly fall into two categories:
 * <p>
 * <table>
 * <tr><td valign = "top"><b>1.</b></td><td>
 *    A page that does not yet exist in a user Session may be encoded as a URL that
 *    references the not-yet-created page by class name.  A set of PageParameters can
 *    also be encoded into the URL, and these parameters will be passed to the page
 *    constructor if the page later needs to be instantiated.
 * <p>
 *    Any page of this type is bookmarkable, and a hint to that effect is given to the
 *    user in the URL:
 * <p>
 * <ul>
 *      /[Application]?bookmarkablePage=[classname]&[param]=[value] [...]
 * </ul>
 * <p>
 *    Bookmarkable pages must implement a constructor that takes a PageParameters argument.
 *    Links to bookmarkable pages are created by calling the urlFor(Class, PageParameters)
 *    method, where Class is the page class and PageParameters are the parameters to encode
 *    into the URL.
 * <p>
 * </td></tr>
 * <tr><td valign = "top"><b>2.</b></td><td>
 *    Stateful pages (that have already been requested by a user) will be present in
 *    the user's Session and can be referenced securely with a session-relative number:
 * <p>
 * <ul>
 *      /[Application]?component=[pageId]
 * </ul>
 * <p>
 *    Often, the reason to access an existing session page is due to some kind
 *    of "postback" (either a link click or a form submit) from a page (possibly
 *    accessed with the browser's back button or possibly not).  A call to a
 *    registered listener is dispatched like so:
 * <p>
 * <ul>
 *      /[Application]?component=[pageId.componentPath]&interface=[interface]
 * </ul>
 * <p>
 *    For example:
 * <p>
 * <ul>
 *      /[Application]?component=3.signInForm.submit&interface=IFormSubmitListener
 * </ul>
 * </td></tr></table>
 * <p>
 * URLs for stateful pages (those that already exist in the session map) are created by calling
 * the urlFor(Component, Class) method, where Component is the component being linked to
 * and Class is the interface on the component to call.
 * <p>
 * For pages falling into the second category, listener interfaces cannot be invoked unless
 * they have first been registered via the static registerSecureInterface() method.  This
 * method ensures basic security by restricting the set of interfaces that outsiders can call
 * via GET and POST requests.  Each listener interface has a single method which takes only
 * a RequestCycle parameter.  Currently, the following classes register the following kinds
 * of listener interfaces:
 * <p>
 * <table>
 *   <tr>
 *      <th align = "left">Class</th><th align = "left">Interface</th><th align="left">Purpose</th>
 *   </tr>
 *   <tr>
 *      <td>Form</td><td>IFormSubmitListener</td><td>Handle form submits</td>
 *   </tr>
 *   <tr>
 *      <td>Image</td><td>IResourceListener</td><td>Respond to image resource requests</td>
 *   </tr>
 *   <tr>
 *      <td>Link</td><td>ILinkListener</td><td>Respond to link clicks</td>
 *   </tr>
 *   <tr>
 *      <td>Page</td><td>IRedirectListener</td><td>Respond to redirects</td>
 *   </tr>
 * </table>
 * <p>
 * The redirectToInterceptPage() and continueToOriginalDestination() methods can be used
 * to temporarily redirect a user to some page.  This is mainly intended for use in
 * signing in users who have bookmarked a page inside a site that requires the user be
 * authenticated before they can access the page.  When it is discovered that the user
 * is not signed in, the user is redirected to the sign-in page with redirectToInterceptPage().
 * When the user has signed in, they are sent on their way with continueToOriginalDestination().
 * These methods could also be useful in "interstitial" advertising or other kinds of
 * "intercepts".
 * <p>
 *
 * @author Jonathan Locke
 */
public abstract class RequestCycle
{ // TODO finalize javadoc
    /** the application object. */
    protected final IApplication application;

    /** the session object. */
    protected final Session session;

    /** the current request. */
    protected final Request request;

    /** the current response. */
    protected Response response;

    /** The page to render to the user. */
    private Page page;

    /**
     * IF the page is set to null, we'll first set the current page to this
     * variable. We use this in order to be able to release resources on the
     * page and its components.
     */
    private Page pageBackup;

    /**
     * True if request should be redirected to the resulting page instead of
     * just rendering it back to the user.
     */
    private boolean redirect;

    /** Map from class name to Constructor. */
    private static final Map constructors = new HashMap();

    /** Map from interface Class to Method. */
    private static final Map listenerInterfaceMethods = new HashMap();

    /** Log. */
    private static final Log log = LogFactory.getLog(RequestCycle.class);

    /** Thread-local that holds the current request cycle. */
    private static final ThreadLocal current = new ThreadLocal();

    /**
     * Adds an interface to the map of interfaces that can be invoked by outsiders.
     * The interface must have a single method with the signature methodName(RequestCycle).
     * NOTE: THIS METHOD IS NOT INTENDED FOR USE BY FRAMEWORK CLIENTS.
     * @param c The interface class, which must extend IRequestListener.
     */
    public static void registerListenerInterface(final Class c)
    {
        // Ensure that c extends IRequestListener
        if (!IRequestListener.class.isAssignableFrom(c))
        {
            throw new IllegalArgumentException("Class " + c + " must extend IRequestListener");
        }

        // Search methods in class
        final Method[] methods = c.getMethods();
        for (int i = 0; i < methods.length; i++)
        {
            // Get method parameter types
            final Class[] parameters = methods[i].getParameterTypes();

            // If there is only one parameter, and it is RequestCycle
            if (parameters.length == 1 && parameters[0] == RequestCycle.class)
            {
                // Save this interface method by the non-qualified class name
                listenerInterfaceMethods.put(Classes.name(c), methods[i]);

                // Done!
                return;
            }
        }

        // Failed to find interface method
        throw new IllegalArgumentException("Internal error: " + c
                + " does not have a method that takes RequestCycle as a parameter");
    }

    /**
     * Gets request cycle for calling thread.
     * @return Request cycle for calling thread
     */
    public final static RequestCycle get()
    {
        return (RequestCycle)current.get();
    }

    /**
     * Constructor.
     * @param application The application
     * @param session The session
     * @param request The request
     * @param response The response
     */
    protected RequestCycle(final IApplication application,
            final Session session, final Request request, final Response response)
    {
        this.application = application;
        this.session     = session;
        this.request     = request;
        this.response    = response;

        session.setApplication(application);
        current.set(this); // set request cycle for ThreadLocal access
    }

    /**
     * Redirects to any intercept page previously specified by a call to
     * redirectToInterceptPage.
     * @return True if an original destination was redirected to
     * @see RequestCycle#redirectToInterceptPage(Class)
     * @see RequestCycle#redirectToInterceptPage(Page)
     */
    public final boolean continueToOriginalDestination()
    {
        String url = session.getInterceptContinuationURL();
        if (url != null)
        {
            response.redirect(url);

            // Since we are explicitly redirecting to a page already, we do not
            // want a second redirect to occur automatically
            setRedirect(false);

            // Reset interception URL
            session.setInterceptContinuationURL(null);
            return true;
        }
        return false;
    }

    /**
     * Redirects browser to an intermediate page such as a sign-in page.
     * @param c The sign in page class
     */
    public final void redirectToInterceptPage(final Class c)
    {
        redirectToInterceptPage(getPageFactory().newPage(c));
    }

    /**
     * Redirects browser to an intermediate page such as a sign-in page.
     * @param page The sign in page
     */
    public final void redirectToInterceptPage(final Page page)
    {
        // Access was denied.  Construct sign in page with
        session.setInterceptContinuationURL(response.encodeURL(request.getURL()));
        redirectToPage(page);
    }

    /**
     * Renders response for request.
     * NOTE: THIS METHOD IS INTENDED FOR INTERNAL USE ONLY AND MAY NOT BE SUPPORTED IN THE FUTURE.
     * @throws ServletException
     */
    public final void render() throws ServletException
    {
        try
        {
            // Render response for request cycle
            handleRender();
        }
        catch (RuntimeException e)
        {
            // Render a page for the user
            try
            {
                // If application doesn't want debug info showing up for users
                ApplicationSettings settings = application.getSettings();
                if (settings.getUnexpectedExceptionDisplay() !=
                    ApplicationSettings.SHOW_NO_EXCEPTION_PAGE)
                {
                    if (settings.getUnexpectedExceptionDisplay() ==
                        ApplicationSettings.SHOW_INTERNAL_ERROR_PAGE)
                    {
                        // use internal error page
                        setPage(getPageFactory().newPage(settings.getInternalErrorPage()));
                    }
                    else
                    {
                        // otherwise show full details
                        setPage(new ExceptionErrorPage(e));
                    }
                    // We generally want to redirect the response because we were
                    // in the middle of rendering and the page may end up looking
                    // like spaghetti otherwise
                    redirectToPage(getPage());
                }
            }
            catch (RuntimeException ignored)
            {
                // We ignore any problems trying to render the exception display
                // page because we are just going to rethrow the exception anyway
                // and the original problem will be displayed on the console by
                // the container.  It's better this way because users of the framework
                // will not want to be distracted by any internal problems rendering
                // a runtime exception error display page.
            }

            // Rethrow error for console / container
            throw e;
        }
        finally
        {
            release();
            response.close(); //close the response
        }
    }

    /**
     * Releases the current thread local related resources.
     * The threadlocal of this request cycle is reset.
     * If we are in a 'redirect' state, we do not want to loose our messages
     * as - e.g. when handling a form - there's a fat change we are comming back
     * for the rendering of it.
     */
    private void release()
    {
        if(getRedirect())
        {
            // Since we are explicitly redirecting to a page already, we do not
            // want a second redirect to occur automatically
            setRedirect(false);
            if(page != null)
            {
                page.messages = FeedbackMessages.get();
                FeedbackMessages.remove(); // only clean thread local; in fact we moved the
                	// reference from the thread local to the page temporarily
            }
            else // hmmm, no page, which probably means we are rendering directely
                // ourselves; as a fallthrough, we have to clear things up
            {
                FeedbackMessages.release(); 
            }
        }
        else
        {
            // clear the ui messages and reset the original component models
            // the components have had the possibility of rendering the messages,
            // and the messages are meant for 'one time use' only.
            FeedbackMessages.release();
        }
        current.set(null); // reset ThreadLocal reference
    }

    /**
     * Gets the application object.
     * @return Application interface
     */
    public final IApplication getApplication()
    {
        return application;
    }

    /**
     * Gets the current page.
     * @return The page
     */
    public final Page getPage()
    {
        return page;
    }

    /**
     * Gets whether the page for this request should be redirected.
     * @return whether the page for this request should be redirected
     */
    public final boolean getRedirect()
    {
        return redirect;
    }

    /**
     * Gets the request.
     * @return Request object
     */
    public final Request getRequest()
    {
        return request;
    }

    /**
     * Gets the response.
     * @return Response object
     */
    public final Response getResponse()
    {
        return response;
    }

    /**
     * Gets the session.
     * @return Session object
     */
    public final Session getSession()
    {
        return session;
    }

    /**
     * Convinience method to get the Page factory
     * 
     * @return PageFactory from application settings
     */
    public final IPageFactory getPageFactory()
    {
        return getApplication().getSettings().getPageFactory();
    }
    
    /**
     * Convenience method that sets page on response object.
     * @param c The page class to render as a response
     * @deprecated use cycle.setPage(cycle.getPageFactory().newPage(Class)) instead
     */
    public final void setPage(final Class c)
    {
        this.page = getPageFactory().newPage(c, new PageParameters(request.getParameterMap()));
    }

    /**
     * Convenience method that sets page on response object.
     * @param classname The page class name (groovy file name) 
     * 		to render as a response
     * @deprecated use cycle.setPage(cycle.getPageFactory().newPage(String)) instead
     */
    public final void setPage(final String classname)
    {
        this.page = getPageFactory().newPage(classname);
    }

    /**
     * Convenience method that sets page on response object.
     * @param c The page class to render as a response
     * @param parameters Parameters to page
     * @deprecated use cycle.setPage(cycle.getPageFactory().newPage(Class, PageParameters)) instead
     */
    public final void setPage(final Class c, final PageParameters parameters)
    {
        this.page = getPageFactory().newPage(c, parameters);
    }

    /**
     * Convenience method that sets page on response object.
     * @param c The page class to render as a response
     * @param page A "referer" page
     * @deprecated use cycle.setPage(cycle.getPageFactory().newPage(Class, Page)) instead
     */
    public final void setPage(final Class c, final Page page)
    {
        this.page = getPageFactory().newPage(c, page);
    }

    /**
     * Convenience method that sets page on response object.
     * @param page The page to render as a response
     */
    public final void setPage(final Page page)
    {
        this.page = page;
    }

    /**
     * Sets whether the page for this request should be redirected.
     * @param redirect True if the page for this request cycle should be redirected
     * to rather than directly rendered.
     */
    public final void setRedirect(final boolean redirect)
    {
        this.redirect = redirect;
    }

    /**
     * Sets response.
     * @param response The response
     */
    public void setResponse(final Response response)
    {
        this.response = response;
    }

    /**
     * Gets the url for the given page class using the given parameters.
     * THIS METHOD IS NOT INTENDED FOR USE BY FRAMEWORK CLIENTS.
     * @param pageClass Class of page
     * @param parameters Parameters to page
     * @return Bookmarkable URL to page
     */
    public abstract String urlFor(final Class pageClass, final PageParameters parameters);

    /**
     * Gets the url for the given component/ listener interface.
     * THIS METHOD IS NOT INTENDED FOR USE BY FRAMEWORK CLIENTS.
     * @param component Component that has listener interface
     * @param listenerInterface The listener interface
     * @return A URL that encodes a page, component and interface to call
     */
    public abstract String urlFor(final Component component, final Class listenerInterface);

    /**
     * Looks up an interface method by name.
     * @param interfaceName The interface
     * @return The method
     * @throws RenderException
     */
    protected final Method getInterfaceMethod(final String interfaceName)
    {
        final Method method = (Method)listenerInterfaceMethods.get(interfaceName);
        if (method == null)
        {
            throw new RenderException("Attempt to access unknown interface "
                    + interfaceName);
        }
        return method;
    }

    /**
     * Renders response for request.
     */
    protected abstract void handleRender();

    /**
     * Creates a new page.
     * @param pageClass The page class to instantiate
     * @return The page
     * @throws RenderException
     */
    protected final Page newPage(final Class pageClass)
    {
        return getPageFactory().newPage(
                pageClass, new PageParameters(request.getParameterMap()));
    }

    /**
     * Create a new page
     * @param pageClassName The name of class may be a groovy file as well)
     * @return The Page object created
     */
    protected final Page newPage(final String pageClassName)
    {
        return getPageFactory().newPage(
                pageClassName, new PageParameters(request.getParameterMap()));
    }

    /**
     * Creates a new instance of a page using the given class name.
     * @param pageClass The class of page to create
     * @param page Parameter to page constructor
     * @return The new page
     * @throws RenderException
     */
    protected final Page newPage(final Class pageClass, final Page page)
    {
        return getPageFactory().newPage(pageClass, page);
    }

    /**
     * Gets a null response.
     * @return A copy of this request cycle object with a NULL response
     */
    protected abstract RequestCycle nullResponse();

    /**
     * Redirects browser to the given page.
     * @param page The page to redirect to
     */
    protected abstract void redirectToPage(final Page page);

}
