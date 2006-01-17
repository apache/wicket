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
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.protocol.http.BufferedWebResponse;
import wicket.request.ClientInfo;
import wicket.request.IBookmarkablePageRequestTarget;
import wicket.request.IPageRequestTarget;
import wicket.request.IRequestCodingStrategy;
import wicket.request.IRequestCycleProcessor;
import wicket.request.RequestParameters;
import wicket.request.target.BookmarkablePageRequestTarget;
import wicket.request.target.ComponentRequestTarget;
import wicket.request.target.IAccessCheck;
import wicket.request.target.PageRequestTarget;
import wicket.util.lang.Classes;

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
 * Bookmarkable pages must either implement a constructor that takes a
 * PageParameters argument or a default constructor. If a Page has both
 * constructors the constuctor with the PageParameters argument will be used.
 * Links to bookmarkable pages are created by calling the urlFor(Class,
 * PageParameters) method, where Class is the page class and PageParameters are
 * the parameters to encode into the URL.
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
 * @author Eelco Hillenius
 */
public abstract class RequestCycle
{
	/**
	 * Checking access after resolving.
	 */
	private static final int CHECK_ACCESS = 4;

	/**
	 * Cleaning up after responding to a request.
	 */
	private static final int CLEANUP_REQUEST = 8;

	/** Thread-local that holds the current request cycle. */
	private static final ThreadLocal current = new ThreadLocal();

	/**
	 * Decoding request parameters into a strongly typed
	 * {@link RequestParameters} object.
	 */
	private static final int DECODE_PARAMETERS = 2;

	/**
	 * Request cycle processing is done.
	 */
	private static final int DONE = 9;

	/**
	 * Responding to an uncaught exception.
	 */
	private static final int HANDLE_EXCEPTION = 7;

	/** Map from request interface Class to Method. */
	private static final Map listenerRequestInterfaceMethods = new HashMap();

	/** Log */
	private static final Log log = LogFactory.getLog(RequestCycle.class);

	/**
	 * No processing has been done.
	 */
	private static final int NOT_STARTED = 0;

	/**
	 * Starting the actual request processing.
	 */
	private static final int PREPARE_REQUEST = 1;

	/**
	 * Dispatching and handling of events.
	 */
	private static final int PROCESS_EVENTS = 5;

	/**
	 * Resolving the {@link RequestParameters} object to a request target.
	 */
	private static final int RESOLVE_TARGET = 3;

	/**
	 * Responding using the currently set {@link IRequestTarget}.
	 */
	private static final int RESPOND = 6;

	/** The application object. */
	protected final Application application;;

	/** The current request. */
	protected Request request;

	/** The current response. */
	protected Response response;

	/** The session object. */
	protected final Session session;

	/** The current stage of event processing. */
	private int currentStep = NOT_STARTED;

	/** The original response the request cycle was created with. */
	private final Response originalResponse;

	/**
	 * True if request should be redirected to the resulting page instead of
	 * just rendering it back to the user.
	 */
	private boolean redirect;

	/** the current request parameters (if any). */
	private transient RequestParameters requestParameters;

	/** holds the stack of set {@link IRequestTarget}, the last set op top. */
	// TODO Performance: Use a more efficient implementation, maybe with a default size of 3
	private transient Stack/* <IRequestTarget> */requestTargets = new Stack();

	/** the time that this request cycle object was created. */
	private final long startTime = System.currentTimeMillis();

	/** True if the session should be updated (for clusterf purposes). */
	private boolean updateSession;

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
		this.originalResponse = response;

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
	 * Gets the new agent info object for this session. This method calls
	 * {@link Session#getClientInfo()}, which may or may not cache the client
	 * info object and typically calls {@link #newClientInfo()} when no client
	 * info object was cached.
	 * 
	 * @return the agent info object based on this request
	 */
	public final ClientInfo getClientInfo()
	{
		return getSession().getClientInfo();
	}

	/**
	 * Get the orignal respone the request was create with. Access may be
	 * necessary with the response has temporarily being replaced but your
	 * components requires access to lets say the cookie methods of a
	 * WebResponse.
	 * 
	 * @return The original response object.
	 */
	public final Response getOriginalResponse()
	{
		return this.originalResponse;
	}

	/**
	 * Gets the processor for delegated request cycle handling.
	 * 
	 * @return the processor for delegated request cycle handling
	 */
	public abstract IRequestCycleProcessor getProcessor();

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
	 * Looks up an request interface method by name.
	 * 
	 * @param interfaceName
	 *            The interface name
	 * @return The method, null of nothing is found
	 * 
	 */
	public final Method getRequestInterfaceMethod(final String interfaceName)
	{
		return (Method)listenerRequestInterfaceMethods.get(interfaceName);
	}

	/**
	 * Gets the current request target. May be null.
	 * 
	 * @return the current request target, null if none was set yet.
	 */
	public final IRequestTarget getRequestTarget()
	{
		return (!requestTargets.isEmpty()) ? (IRequestTarget)requestTargets.peek() : null;
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
	 * Gets the page that is to be rendered for this request in case the last
	 * set request target is of type {@link PageRequestTarget}.
	 * 
	 * @return the page or null
	 */
	public final Page getResponsePage()
	{
		IRequestTarget target = (IRequestTarget)getRequestTarget();
		if (target != null && (target instanceof PageRequestTarget))
		{
			return ((IPageRequestTarget)target).getPage();
		}
		return null;
	}

	/**
	 * Gets the page class that is to be instantiated and rendered for this
	 * request in case the last set request target is of type
	 * {@link BookmarkablePageRequestTarget}.
	 * 
	 * @return the page class or null
	 */
	public final Class getResponsePageClass()
	{
		IRequestTarget target = (IRequestTarget)getRequestTarget();
		if (target != null && (target instanceof IBookmarkablePageRequestTarget))
		{
			return ((IBookmarkablePageRequestTarget)target).getPageClass();
		}
		return null;
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
	 * @return The start time for this request
	 */
	public final long getStartTime()
	{
		return startTime;
	}

	/**
	 * Template method that is called when a runtime exception is thrown, just
	 * before the actual handling of the runtime exception. This is called by
	 * {@link wicket.request.compound.DefaultExceptionResponseStrategy}, hence
	 * if that strategy is replaced by another one, there is no guarantee this
	 * method is called.
	 * 
	 * @param page
	 *            Any page context where the exception was thrown
	 * @param e
	 *            The exception
	 * @return Any error page to redirect to
	 */
	public Page onRuntimeException(Page page, RuntimeException e)
	{
		return null;
	}

	/**
	 * Redirects browser to the given page. NOTE: Usually, you should never call
	 * this method directly, but work with setResponsePage instead. This method
	 * is part of Wicket's internal behavior and should only be used when you
	 * want to circumvent the normal framework behavior and issue the redirect
	 * directly.
	 * 
	 * @param page
	 *            The page to redirect to
	 */
	public abstract void redirectTo(final Page page);

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Responds to a request.
	 */
	public final void request()
	{
		checkReuse();

		// set start step
		currentStep = PREPARE_REQUEST;

		// loop through steps
		steps();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Responds to a request to re-render a single component.
	 * </p>
	 * <p>
	 * NOTE: This method is typically only used for testing purposes.
	 * </p>
	 * 
	 * @param component
	 *            to be re-rendered
	 */
	public final void request(final Component component)
	{
		checkReuse();

		if (component.isAuto())
		{
			throw new WicketRuntimeException("Auto-added components can not be re-rendered");
		}

		request(new ComponentRequestTarget(component));
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Responds to a request with the request target.
	 * 
	 * @param target
	 *            request target
	 */
	public final void request(IRequestTarget target)
	{
		checkReuse();

		// set it as the current target, on the top of the stack
		requestTargets.push(target);

		// set start step
		currentStep = PROCESS_EVENTS;

		// loop through steps
		steps();
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
	 * Sets the request target as the current.
	 * 
	 * @param requestTarget
	 *            the request target to set as current
	 */
	public final void setRequestTarget(IRequestTarget requestTarget)
	{
		// FIXME Robustness: This has to be done after the unit tests are fixed
		// // if we are already responding, we can't change the request target
		// // as that would either have no effect, or - in case we would set
		// // the currentStep back to PROCESS_EVENTS, we would have double
		// // output (and it is not Wicket's intention to work as Servlet
		// filters)
		// if (currentStep >= RESPOND)
		// {
		// throw new WicketRuntimeException(
		// "you cannot change the request cycle after rendering has commenced");
		// }

		if (log.isDebugEnabled())
		{
			if (!requestTargets.isEmpty())
			{
				IRequestTarget former = (IRequestTarget)requestTargets.peek();
				log.debug("replacing request target " + former + " with " + requestTarget);
			}
			else
			{
				log.debug("setting request target to " + requestTarget);
			}
		}

		// change the current step to a step that will handle the
		// new target if need be
		if (currentStep >= RESPOND)
		{
			if (log.isDebugEnabled())
			{
				log.debug("rewinding request processing to PROCESS_EVENTS");
			}

			// we are not actually doing event processing again,
			// but since we are still in the loop here, the next
			// actual value will be RESPOND again
			currentStep = PROCESS_EVENTS;
		}
		// NOTE: if we are at PROCESS_EVENTS, leave it as we don't
		// want to re-execute that step again

		requestTargets.push(requestTarget);
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
	 * Convenience method that sets page class as the response. This will
	 * generate a redirect to the page with a bookmarkable url
	 * 
	 * @param pageClass
	 *            The page class to render as a response
	 */
	public final void setResponsePage(final Class pageClass)
	{
		setResponsePage(pageClass, null);
	}

	/**
	 * Sets the page class with optionally the page parameters as the render
	 * target of this request.
	 * 
	 * @param pageClass
	 *            The page class to render as a response
	 * @param pageParameters
	 *            The page parameters that gets appended to the bookmarkable
	 *            url,
	 */
	public final void setResponsePage(final Class pageClass, final PageParameters pageParameters)
	{
		IRequestTarget target = new BookmarkablePageRequestTarget(pageClass, pageParameters);
		setRequestTarget(target);
	}

	/**
	 * Sets the page as the render target of this request.
	 * 
	 * @param page
	 *            The page to render as a response
	 */
	public final void setResponsePage(final Page page)
	{
		IRequestTarget target = new PageRequestTarget(page);
		setRequestTarget(target);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * @param updateCluster
	 *            The updateCluster to set.
	 */
	public void setUpdateSession(boolean updateCluster)
	{
		this.updateSession = updateCluster;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "RequestCycle" + "@" + Integer.toHexString(hashCode()) + "{thread="
				+ Thread.currentThread().getName() + "}";
	}

	/**
	 * Creates a new agent info object based on this request. Typically, this
	 * method is called once by the session and the returned object will be
	 * cached in the session after that call; we can expect the client to stay
	 * the same for the whole session, and implementations of
	 * {@link #newClientInfo()} might be relatively expensive.
	 * 
	 * @return the agent info object based on this request
	 */
	protected abstract ClientInfo newClientInfo();

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

	// internal ints for processing status; keep here to be out of sight a bit

	/**
	 * Checks whether no processing has been done yet and throws an exception
	 * when a client tries to reuse this instance.
	 */
	private void checkReuse()
	{
		if (currentStep != NOT_STARTED)
		{
			throw new WicketRuntimeException(
					"RequestCycles are non-reusable objects. This instance (" + this
							+ ") already executed");
		}
	}

	/**
	 * Clean up the request cycle.
	 */
	private void cleanUp()
	{
		// clean up target stack; calling cleanUp has effects like
		// NOTE: don't remove the targets as testing code might need them
		// furthermore, the targets will be cg-ed with this cycle too
		for (Iterator iter = requestTargets.iterator(); iter.hasNext();)
		{
			IRequestTarget target = (IRequestTarget)iter.next();
			if (target != null)
			{
				try
				{
					target.cleanUp(this);
				}
				catch (RuntimeException e)
				{
					log.error("there was an error cleaning up target " + target + ".", e);
				}
			}
		}

		if (updateSession)
		{
			// At the end of our response, we need to set any session
			// attributes that might be required to update the cluster
			session.update();
		}

		if (getResponse() instanceof BufferedWebResponse)
		{
			((BufferedWebResponse)getResponse()).filter();
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

	/**
	 * @param processor
	 *            The request processor
	 */
	private final void doProcessEventsAndRespond(IRequestCycleProcessor processor)
	{
		// let the processor handle/ issue any events
		processor.processEvents(this);

		// set current stage manually this time
		currentStep = RESPOND;

		// generate a response
		processor.respond(this);
	}

	/**
	 * Gets the request parameters object using the instance of
	 * {@link IRequestCodingStrategy} of the provided request cycle processor.
	 * 
	 * @param processor
	 *            the request cycle processor
	 * @return the request parameters object
	 */
	private final RequestParameters getRequestParameters(IRequestCycleProcessor processor)
	{
		// get the request encoder to decode the request parameters
		final IRequestCodingStrategy encoder = processor.getRequestCodingStrategy();
		if (encoder == null)
		{
			throw new WicketRuntimeException("request encoder must be not-null (provided by "
					+ processor + ")");
		}

		// decode the request parameters into a strongly typed parameters
		// object that is to be used by the target resolving
		final RequestParameters requestParameters = encoder.decode(getRequest());
		if (requestParameters == null)
		{
			throw new WicketRuntimeException("request parameters must be not-null (provided by "
					+ encoder + ")");
		}
		return requestParameters;
	}

	/**
	 * Prepare the request cycle.
	 */
	private void prepare()
	{
		// Prepare session for request
		session.init();

		// Event callback
		onBeginRequest();
	}

	/**
	 * Call the event processing and and respond methods on the request
	 * processor and apply synchronization if needed.
	 * 
	 * @param processor
	 *            the request processor
	 */
	private final void processEventsAndRespond(IRequestCycleProcessor processor)
	{
		// Use any synchronization lock provided by the target
		Object lock = getRequestTarget().getLock(this);
		if (lock != null)
		{
			synchronized (lock)
			{
				doProcessEventsAndRespond(processor);
			}
		}
		else
		{
			doProcessEventsAndRespond(processor);
		}
	}

	/**
	 * Safe version of {@link #getProcessor()} that throws an exception when the
	 * processor is null.
	 * 
	 * @return the request processor
	 */
	private IRequestCycleProcessor safeGetRequestProcessor()
	{
		IRequestCycleProcessor processor = getProcessor();
		if (processor == null)
		{
			throw new WicketRuntimeException("request cycle processor must be not-null");
		}
		return processor;
	}

	/**
	 * handle the current step in the request processing.
	 * 
	 * @param processor
	 *            the cycle processor that can be used
	 */
	private final void step(IRequestCycleProcessor processor)
	{
		try
		{
			switch (currentStep)
			{
				case PREPARE_REQUEST: 
				{
					// prepare the request
					prepare();
					break;
				}
				case DECODE_PARAMETERS: 
				{
					// get the request parameters object using the request
					// encoder of the processor
					requestParameters = getRequestParameters(processor);
					break;
				}
				case RESOLVE_TARGET: 
				{
					// resolve the target of the request using the request
					// parameters
					final IRequestTarget target = processor.resolve(this, requestParameters);
					
					// has to result in a request target
					if (target == null)
					{
						throw new WicketRuntimeException(
								"the processor did not resolve to any request target");
					}
					requestTargets.push(target);
					break;
				}
				case CHECK_ACCESS: 
				{
					// manually set step to check access
					IRequestTarget target = getRequestTarget();

					boolean access = true;
					if (target instanceof IAccessCheck)
					{
						access = ((IAccessCheck)target).checkAccess(this);
					}

					// check access or earlier (like in a component constructor)
					// might have called setRequestTarget. If that is the case, 
					// put that one on top; otherwise put our resolved target 
					// on top
					IRequestTarget otherTarget = getRequestTarget();
					if (otherTarget != target)
					{
						// The new target has to be checked again set the
						// current step back one.
						currentStep = CHECK_ACCESS - 1;
						// swap targets
						requestTargets.pop();
						requestTargets.push(target);
						requestTargets.push(otherTarget);
					}
					else if (!access)
					{
						setResponsePage(getApplication().getApplicationSettings()
								.getAccessDeniedPage());
					}
					break;
				}
				case PROCESS_EVENTS: 
				{
					// determine what kind of synchronization is to be used, and
					// handle any events with that and generate a response in
					// that same block
					// NOTE: because of synchronization, we need to take the
					// steps PROCESS_EVENS and RESPOND together
					processEventsAndRespond(processor);
					break;
				}
				case RESPOND: 
				{
					// generate a response
					// NOTE: we reach this block when during event processing
					// and response generation the request target was changed,
					// causing the request processing to go BACK to RESPOND.
					// Note that we could still be in a session-synchronized
					// block here, so be very careful not to do other
					// synchronization (possibly introducing a deadlock)
					processor.respond(this);
					break;
				}
				default: 
				{
					// nothing
				}
			}
		}
		catch (RuntimeException e)
		{
			// set step manually to handle exception
			currentStep = HANDLE_EXCEPTION;

			// probably our last chance the exception can be logged
			log.error(e.getMessage(), e);

			// try to play nicely and let the request processor handle the
			// exception response. If that doesn't work, any runtime exception
			// will automatically be bubbled up
			if (processor != null)
			{
				processor.respond(e, this);
			}
		}
	}

	/**
	 * Loop through the processing steps starting from the current one.
	 */
	private final void steps()
	{
		try
		{
			// get the processor
			IRequestCycleProcessor processor = safeGetRequestProcessor();

			// FIXME Robustness: Catch infinite loops
			while (currentStep < DONE)
			{
				step(processor);
				currentStep++;
			}
		}
		finally
		{
			// set step manually to clean up
			currentStep = CLEANUP_REQUEST;

			// clean up the request
			cleanUp();

			// set step manually to done
			currentStep = DONE;
		}
	}

	/**
	 * Releases the current thread local related resources. The threadlocal of
	 * this request cycle is reset. If we are in a 'redirect' state, we do not
	 * want to lose our messages as - e.g. when handling a form - there's a fat
	 * chance we are coming back for the rendering of it.
	 */
	private final void threadDetach()
	{
		// Detach from session
		session.detach();

		if (getRedirect())
		{
			// Since we are explicitly redirecting to a page already, we do not
			// want a second redirect to occur automatically
			setRedirect(false);
		}

		// Clear ThreadLocal reference; makes sense as this object should not be
		// reused
		current.set(null);
	}
}
