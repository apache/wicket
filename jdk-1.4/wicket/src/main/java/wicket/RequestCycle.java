/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.behavior.IBehavior;
import wicket.protocol.http.BufferedWebResponse;
import wicket.protocol.http.IRequestLogger;
import wicket.protocol.http.PageExpiredException;
import wicket.request.ClientInfo;
import wicket.request.IRequestCodingStrategy;
import wicket.request.IRequestCycleProcessor;
import wicket.request.RequestParameters;
import wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.ComponentRequestTarget;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.request.target.component.IPageRequestTarget;
import wicket.request.target.component.PageRequestTarget;
import wicket.request.target.component.listener.BehaviorRequestTarget;
import wicket.request.target.component.listener.ListenerInterfaceRequestTarget;
import wicket.request.target.resource.SharedResourceRequestTarget;
import wicket.util.collections.ArrayListStack;
import wicket.util.value.ValueMap;

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
 * /[Application]?wicket:interface=[pageMapName]:[pageId]: ...
 * </ul>
 * <p>
 * Often, the reason to access an existing session page is due to some kind of
 * "postback" (either a link click or a form submit) from a page (possibly
 * accessed with the browser's back button or possibly not). A call to a
 * registered listener is dispatched like so:
 * <p>
 * <ul>
 * /[Application]?wicket:interface=[pageMapName]:[pageId]:[componentPath]:[version]:[interfaceName]
 * </ul>
 * <p>
 * For example:
 * <p>
 * <ul>
 * /[Application]?wicket:interface=:3:signInForm:submit::IFormSubmitListener
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
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class RequestCycle
{
	/** Thread-local that holds the current request cycle. */
	private static final ThreadLocal current = new ThreadLocal();

	/** Cleaning up after responding to a request. */
	private static final int DETACH_REQUEST = 6;

	/** Request cycle processing is done. */
	private static final int DONE = 7;

	/** Responding to an uncaught exception. */
	private static final int HANDLE_EXCEPTION = 5;

	/** Log */
	private static final Log log = LogFactory.getLog(RequestCycle.class);

	/** No processing has been done. */
	private static final int NOT_STARTED = 0;

	/** Starting the actual request processing. */
	private static final int PREPARE_REQUEST = 1;

	/** Dispatching and handling of events. */
	private static final int PROCESS_EVENTS = 3;

	/** Resolving the {@link RequestParameters} object to a request target. */
	private static final int RESOLVE_TARGET = 2;

	/** Responding using the currently set {@link IRequestTarget}. */
	private static final int RESPOND = 4;

	/**
	 * Gets request cycle for calling thread.
	 * 
	 * @return Request cycle for calling thread
	 */
	public static RequestCycle get()
	{
		return (RequestCycle)current.get();
	}

	/**
	 * Sets the request cycle for the calling thread. You typically DO NOT NEED
	 * to call this method, as the request cycle is set to current for you in
	 * the constructor. However, if you have a <a
	 * href="http://issues.apache.org/jira/browse/WICKET-366">very special need</a>
	 * to set it to something else, you can expose this method.
	 * 
	 * @param cycle
	 *            The request cycle to set current
	 */
	protected static void set(RequestCycle cycle)
	{
		current.set(cycle);
	}

	/** The application object. */
	protected final Application application;

	/** The processor for this request. */
	protected final IRequestCycleProcessor processor;

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


	/** holds the stack of set {@link IRequestTarget}, the last set op top. */
	private transient final ArrayListStack requestTargets = new ArrayListStack(3);

	/** the time that this request cycle object was created. */
	private final long startTime = System.currentTimeMillis();

	/** True if the session should be updated (for clusterf purposes). */
	private boolean updateSession;

	/**
	 * Constructor. This instance will be set as the current one for this
	 * thread.
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
		this.processor = safeGetRequestProcessor();

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
	public boolean getRedirect()
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
		IRequestTarget target = getRequestTarget();
		if (target instanceof PageRequestTarget)
		{
			return ((IPageRequestTarget)target).getPage();
		}
		else if (target instanceof BookmarkablePageRequestTarget)
		{
			return ((BookmarkablePageRequestTarget)target).getPage();
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
		IRequestTarget target = getRequestTarget();
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Redirects browser to the given page. Don't use this method directly, but
	 * use {@link #setResponsePage(Page)} instead.
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
	 * @return the original response
	 */
	public final Response setResponse(final Response response)
	{
		final Response orig = this.response;
		this.response = response;
		return orig;
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
		return "[RequestCycle" + "@" + Integer.toHexString(hashCode()) + " thread="
				+ Thread.currentThread().getName() + "]";
	}

	/**
	 * Returns a URL that references a given interface on a component. When the
	 * URL is requested from the server at a later time, the interface will be
	 * called. A URL returned by this method will not be stable across sessions
	 * and cannot be bookmarked by a user.
	 * 
	 * @param component
	 *            The component to reference
	 * @param listener
	 *            The listener interface on the component
	 * @return A URL that encodes a page, component and interface to call
	 */
	public final CharSequence urlFor(final Component component,
			final RequestListenerInterface listener)
	{
		// Get Page holding component and mark it as stateful.
		final Page page = component.getPage();
		final IRequestTarget target;
		if (listener != IRedirectListener.INTERFACE && component.isStateless()
				&& page.isBookmarkable())
		{
			target = new BookmarkableListenerInterfaceRequestTarget(page.getPageMap().getName(),
					page.getClass(), new PageParameters(), component, listener);
		}
		else
		{
			if (listener == IRedirectListener.INTERFACE)
			{
				page.setPageStateless(Boolean.FALSE);
			}

			// trigger creation of the actual session in case it was deferred
			session.getSessionStore().getSessionId(request, true);

			// Get the listener interface name
			target = new ListenerInterfaceRequestTarget(page, component, listener);
		}
		final IRequestCodingStrategy requestCodingStrategy = getProcessor()
				.getRequestCodingStrategy();
		return requestCodingStrategy.encode(this, target);
	}

	/**
	 * Returns a URL that references a given interface on a given behaviour of a
	 * component. When the URL is requested from the server at a later time, the
	 * interface on the behaviour will be called. A URL returned by this method
	 * will not be stable across sessions and cannot be bookmarked by a user.
	 * 
	 * @param component
	 *            The component to reference
	 * @param behaviour
	 *            The behaviour to reference
	 * @param listener
	 *            The listener interface on the component
	 * @return A URL that encodes a page, component, behaviour and interface to
	 *         call
	 */
	public final CharSequence urlFor(final Component component, final IBehavior behaviour,
			final RequestListenerInterface listener)
	{
		int index = component.getBehaviors().indexOf(behaviour);
		if (index == -1)
		{
			throw new IllegalArgumentException("Behavior " + this
					+ " was not registered with this component: " + component.toString());
		}
		RequestParameters params = new RequestParameters();
		params.setBehaviorId(String.valueOf(index));

		final IRequestTarget target = new BehaviorRequestTarget(component.getPage(), component,
				listener, params);
		final IRequestCodingStrategy requestCodingStrategy = getProcessor()
				.getRequestCodingStrategy();
		return requestCodingStrategy.encode(this, target);
	}

	/**
	 * Returns a URL that references the given request target.
	 * 
	 * @param requestTarget
	 *            the request target to reference
	 * @return a URL that references the given request target
	 */
	public final CharSequence urlFor(final IRequestTarget requestTarget)
	{
		IRequestCodingStrategy requestCodingStrategy = getProcessor().getRequestCodingStrategy();
		return requestCodingStrategy.encode(this, requestTarget);
	}

	/**
	 * Returns a URL that references the given page. It also
	 * {@link Session#touch(Page) touches} the page in the session so that it is
	 * put in the front of the page stack. Use this method only if you plan to
	 * use it the next request.
	 * 
	 * @param page
	 *            The page
	 * @return The url pointing to the provided page
	 */
	public final CharSequence urlFor(final Page page)
	{
		IRequestTarget target = new PageRequestTarget(page);
		Session.get().touch(((IPageRequestTarget)target).getPage());
		return urlFor(target);
	}

	/**
	 * Returns a bookmarkable URL that references a given page class using a
	 * given set of page parameters. Since the URL which is returned contains
	 * all information necessary to instantiate and render the page, it can be
	 * stored in a user's browser as a stable bookmark.
	 * 
	 * @param pageMap
	 *            Pagemap to use
	 * @param pageClass
	 *            Class of page
	 * @param parameters
	 *            Parameters to page
	 * @return Bookmarkable URL to page
	 */
	public final CharSequence urlFor(final IPageMap pageMap, final Class pageClass,
			final PageParameters parameters)
	{
		final IRequestTarget target = new BookmarkablePageRequestTarget(pageMap == null
				? PageMap.DEFAULT_NAME
				: pageMap.getName(), pageClass, parameters);
		final IRequestCodingStrategy requestCodingStrategy = getProcessor()
				.getRequestCodingStrategy();
		return requestCodingStrategy.encode(this, target);
	}

	/**
	 * Returns a URL that references a shared resource through the provided
	 * resource reference.
	 * 
	 * @param resourceReference
	 *            The resource reference where a url must be generated for.
	 * @return The url for the shared resource
	 */
	public final CharSequence urlFor(final ResourceReference resourceReference)
	{
		return urlFor(resourceReference, null);
	}

	/**
	 * Returns a URL that references a shared resource through the provided
	 * resource reference.
	 * 
	 * @param resourceReference
	 *            The resource reference where a url must be generated for.
	 * @param parameters
	 *            The parameters to pass to the resource.
	 * @return The url for the shared resource
	 */
	public final CharSequence urlFor(final ResourceReference resourceReference, ValueMap parameters)
	{
		RequestParameters requestParameters = new RequestParameters();
		requestParameters.setResourceKey(resourceReference.getSharedResourceKey());
		requestParameters.setParameters(parameters);
		CharSequence url = getProcessor().getRequestCodingStrategy().encode(this,
				new SharedResourceRequestTarget(requestParameters));
		return url;
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
	private void detach()
	{
		// clean up target stack; calling detach has effects like
		// NOTE: don't remove the targets as testing code might need them
		// furthermore, the targets will be cg-ed with this cycle too
		for (Iterator iter = requestTargets.iterator(); iter.hasNext();)
		{
			IRequestTarget target = (IRequestTarget)iter.next();
			if (target != null)
			{
				try
				{
					target.detach(this);
				}
				catch (RuntimeException e)
				{
					log.error("there was an error cleaning up target " + target + ".", e);
				}
			}
		}

		// remove any rendered feedback messages from the session
		try
		{
			session.cleanupRenderedFeedbackMessages();
		}
		catch (RuntimeException re)
		{
			log.error("there was an error cleaning up the feedback messages", re);
		}

		if (updateSession)
		{
			// At the end of our response, we need to set any session
			// attributes that might be required to update the cluster
			try
			{
				session.update();
			}
			catch (RuntimeException re)
			{
				log.error("there was an error updating the session " + session + ".", re);
			}
		}

		try
		{
			IRequestLogger requestLogger = getApplication().getRequestLogger();
			if (requestLogger != null)
			{
				requestLogger.requestTime((System.currentTimeMillis() - startTime));
			}
		}
		catch (RuntimeException re)
		{
			log.error("there was an error in the RequestLogger ending.", re);
		}

		// clear the used pagemap for this thread,
		try
		{
			session.requestDetached();
		}
		catch (RuntimeException re)
		{
			log.error("there was an error detaching the request from the session " + session + ".",
					re);
		}

		if (getResponse() instanceof BufferedWebResponse)
		{
			try
			{
				((BufferedWebResponse)getResponse()).filter();
			}
			catch (RuntimeException re)
			{
				log.error("there was an error filtering the response.", re);
			}
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
		try
		{
			threadDetach();
		}
		catch (RuntimeException re)
		{
			log.error("Exception occurred during threadDetach", re);
		}
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
	 */
	private final void processEventsAndRespond()
	{
		// let the processor handle/ issue any events
		processor.processEvents(this);

		// set current stage manually this time
		currentStep = RESPOND;

		// generate a response
		processor.respond(this);
	}

	/**
	 * Call the event processing and and respond methods on the request
	 * processor and apply synchronization if needed.
	 */
	private final void respond()
	{
		processor.respond(this);
	}

	/**
	 * Safe version of {@link #getProcessor()} that throws an exception when the
	 * processor is null.
	 * 
	 * @return the request processor
	 */
	private final IRequestCycleProcessor safeGetRequestProcessor()
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
	 */
	private final void step()
	{
		try
		{
			switch (currentStep)
			{
				case PREPARE_REQUEST : {
					// prepare the request
					prepare();
					break;
				}
				case RESOLVE_TARGET : {
					// resolve the target of the request using the request
					// parameters
					final IRequestTarget target = processor.resolve(this, request
							.getRequestParameters());

					// has to result in a request target
					if (target == null)
					{
						throw new WicketRuntimeException(
								"the processor did not resolve to any request target");
					}
					// Add (inserting at the bottom) in case before or during
					// target resolving one or more request targets were pushed
					// on the stack before this. If that is the case, they
					// should be handled before this
					requestTargets.add(0, target);
					break;
				}
				case PROCESS_EVENTS : {
					// determine what kind of synchronization is to be used, and
					// handle any events with that and generate a response in
					// that same block
					// NOTE: because of synchronization, we need to take the
					// steps PROCESS_EVENS and RESPOND together
					processEventsAndRespond();
					break;
				}
				case RESPOND : {
					// generate a response
					// NOTE: We have to do sync here because the
					// processEventsAndRespond step will be unrolled by a
					// RestartXX Exception. And if this is not the case
					// then still it is not a problem to have 2 locks on
					// the same session this will not cause a deadlock.
					// So only if the request targets are different and the
					// getLock() doesn't return 2 times the same object a
					// deadlock could maybe occur. But we use the session
					// as the lock at all times maybe use a variable inside
					// RequestCycle to know that a lock is still in place?
					respond();
					break;
				}
				default : {
					// nothing
				}
			}
		}
		catch (AbortException e)
		{
			throw e;
		}
		catch (RuntimeException e)
		{
			// set step manually to handle exception
			currentStep = HANDLE_EXCEPTION;

			// probably our last chance the exception can be logged.
			// Note that a PageExpiredException should not be logged, because
			// it's not an internal error
			if (!(e instanceof PageExpiredException))
			{
				log.error(e.getMessage(), e);
			}

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

			// Arbitrary maximum number of steps
			final int maxSteps = Short.MAX_VALUE;

			// Loop through steps
			for (int totalSteps = 0; currentStep < DONE; totalSteps++)
			{
				// There is no way to catch infinite loops since the response
				// step can always throw an AbstractRestartResponseException and
				// start the process over at the RESPOND step. So we do a sanity
				// check here and limit the total number of steps to an
				// arbitrary maximum that we consider unreasonable for working
				// code.
				if (totalSteps >= maxSteps)
				{
					throw new IllegalStateException("Request processing executed " + maxSteps
							+ " steps, which means it is probably in an infinite loop.");
				}
				try
				{
					step();
					currentStep++;
				}
				catch (AbstractRestartResponseException e)
				{
					// if a redirect exception has been issued we abort what we
					// were doing and begin responding to the top target on the
					// stack
					currentStep = RESPOND;
				}
			}
		}
		finally
		{
			// set step manually to clean up
			currentStep = DETACH_REQUEST;

			// clean up the request
			detach();

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
