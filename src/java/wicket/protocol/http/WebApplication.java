/*
 * $Id$ $Revision:
 * 1.64 $ $Date$
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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import wicket.Application;
import wicket.ApplicationSettings;
import wicket.AutoLinkResolver;
import wicket.IRequestCycleFactory;
import wicket.ISessionFactory;
import wicket.Request;
import wicket.RequestCycle;
import wicket.Response;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.markup.html.pages.InternalErrorPage;
import wicket.markup.html.pages.PageExpiredErrorPage;
import wicket.protocol.http.servlet.ServletWebRequest;
import wicket.request.IRequestCycleProcessor;
import wicket.request.target.mixin.BookmarkablePageRequestTargetEncoderDecoder;
import wicket.request.target.mixin.IRequestTargetEncoderDecoder;
import wicket.request.target.mixin.PackageRequestTargetEncoderDecoder;
import wicket.response.BufferedResponse;
import wicket.util.collections.MostRecentlyUsedMap;
import wicket.util.file.IResourceFinder;
import wicket.util.file.WebApplicationPath;


/**
 * A web application is a subclass of Application which associates with an
 * instance of WicketServlet to serve pages over the HTTP protocol. This class
 * is intended to be subclassed by framework clients to define a web
 * application.
 * <p>
 * Application settings are given defaults by the WebApplication() constructor,
 * such as error page classes appropriate for HTML. WebApplication subclasses
 * can override these values and/or modify other application settings in their
 * respective constructors by calling getSettings() to retrieve a mutable
 * ApplicationSettings object.
 * <p>
 * If you want to use servlet specific configuration, e.g. using init parameters
 * from the {@link javax.servlet.ServletConfig}object, you should override the
 * init() method. For example:
 * 
 * <pre>
 *                                      
 *        public void init()
 *        {
 *          	String webXMLParameter = getWicketServlet().getInitParameter(&quot;myWebXMLParameter&quot;);
 *           URL schedulersConfig = getWicketServlet().getServletContext().getResource(&quot;/WEB-INF/schedulers.xml&quot;);
 *           ...
 *     
 * </pre>
 * 
 * @see WicketServlet
 * @see wicket.ApplicationSettings
 * @see wicket.ApplicationPages
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Johan Compagner
 * @uahtor Eelco Hillenius
 */
public abstract class WebApplication extends Application
{
	/**
	 * Map of buffered responses that are in progress per session. Buffered
	 * responses are temporarily stored
	 */
	private Map bufferedResponses = new HashMap();

	/** the default request cycle processor implementation. */
	private IRequestCycleProcessor requestCycleProcessor;

	/**
	 * the prefix for storing variables in the actual session (typically
	 * {@link HttpSession} for this application instance.
	 */
	private String sessionAttributePrefix;

	/** Session factory for this web application */
	private ISessionFactory sessionFactory = new ISessionFactory()
	{
		private static final long serialVersionUID = 1L;

		public Session newSession()
		{
			return new WebSession(WebApplication.this);
		}
	};

	/** The WicketServlet that this application is attached to */
	private WicketServlet wicketServlet;

	/**
	 * Constructor.
	 */
	public WebApplication()
	{
		// Set default error pages for HTML markup
		getPages().setPageExpiredErrorPage(PageExpiredErrorPage.class).setInternalErrorPage(
				InternalErrorPage.class);

		// Add resolver for automatically resolving HTML links
		getComponentResolvers().add(new AutoLinkResolver());
	}

	/**
	 * Subclasses could override this to give there own implementation of
	 * ApplicationSettings. DO NOT CALL THIS METHOD YOURSELF. Use getSettings
	 * instead.
	 * 
	 * @return An instanceof an ApplicaitonSettings class
	 * 
	 * @see wicket.Application#createApplicationSettings()
	 */
	public ApplicationSettings createApplicationSettings()
	{
		return new ApplicationSettings(this)
		{
			/**
			 * @see wicket.ApplicationSettings#newResourceFinder()
			 */
			public IResourceFinder newResourceFinder()
			{
				return new WebApplicationPath(getWicketServlet().getServletContext());
			}
		};
	}

	/**
	 * Gets the prefix for storing variables in the actual session (typically
	 * {@link HttpSession} for this application instance.
	 * 
	 * @param request
	 *            the request
	 * 
	 * @return the prefix for storing variables in the actual session
	 */
	public final String getSessionAttributePrefix(final WebRequest request)
	{
		if (sessionAttributePrefix == null)
		{
			String servletPath = request.getServletPath();
			if (servletPath == null)
			{
				throw new WicketRuntimeException("unable to retrieve servlet path");
			}
			sessionAttributePrefix = "wicket-" + servletPath + "-";
		}
		// Namespacing for session attributes is provided by
		// adding the servlet path
		return sessionAttributePrefix;
	}

	/**
	 * @return The Wicket servlet for this application
	 */
	public final WicketServlet getWicketServlet()
	{
		if (wicketServlet == null)
		{
			throw new IllegalStateException("wicketServlet is not set yet. Any code in your"
					+ " Application object that uses the wicketServlet instance should be put"
					+ " in the init() method instead of your constructor");
		}
		return wicketServlet;
	}

	/**
	 * Mounts an encoder at the given path.
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page class on
	 * @param encoder
	 *            the encoder that will be used for this mount
	 */
	public final void mount(String path, IRequestTargetEncoderDecoder encoder)
	{
		checkMountPath(path);

		if (encoder == null)
		{
			throw new NullPointerException("encoder must be not null");
		}

		getDefaultRequestCycleProcessor().getRequestEncoder().mount(path, encoder);
	}

	/**
	 * Mounts a bookmarkable page class to the given path.
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page class on
	 * @param bookmarkablePageClass
	 *            the bookmarkable page class to mount
	 */
	public final void mountBookmarkablePage(String path, Class bookmarkablePageClass)
	{
		checkMountPath(path);
		mount(path, new BookmarkablePageRequestTargetEncoderDecoder(path, bookmarkablePageClass, null));
	}

	/**
	 * Mounts a bookmarkable page class to the given pagemap and path.
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page class on
	 * @param bookmarkablePageClass
	 *            the bookmarkable page class to mount
	 * @param pageMapName
	 *            pagemap name this mount is for
	 */
	public final void mountBookmarkablePage(String path, Class bookmarkablePageClass,
			String pageMapName)
	{
		checkMountPath(path);
		mount(path, new BookmarkablePageRequestTargetEncoderDecoder(path, bookmarkablePageClass,
				pageMapName));
	}

	/**
	 * Mounts all bookmarkable pages at the given path.
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page class on
	 * @param classOfPackageToMount
	 *            the class for which package of which all bookmarkable pages or
	 *            sharedresources should be mounted
	 */
	public final void mountPackage(String path, Class classOfPackageToMount)
	{
		checkMountPath(path);

		if (classOfPackageToMount == null)
		{
			throw new NullPointerException("class for mounting a package can't be null");
		}
		mount(path, new PackageRequestTargetEncoderDecoder(path, classOfPackageToMount));
	}

	/**
	 * @param sessionFactory
	 *            The session factory to use
	 */
	public final void setSessionFactory(final ISessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @param wicketServlet
	 *            The wicket servlet instance for this application
	 * @throws IllegalStateException
	 *             If an attempt is made to call this method once the wicket
	 *             servlet has been set for the application.
	 */
	public final void setWicketServlet(final WicketServlet wicketServlet)
	{
		if (this.wicketServlet == null)
		{
			this.wicketServlet = wicketServlet;
		}
		else
		{
			throw new IllegalStateException("WicketServlet cannot be changed once it is set");
		}
	}

	/**
	 * Unmounts whatever encoder is mounted at a given path.
	 * 
	 * @param path
	 *            the path of the encoder to unmount
	 */
	public final void unmount(String path)
	{
		checkMountPath(path);
		getDefaultRequestCycleProcessor().getRequestEncoder().unmount(path);
	}

	/**
	 * Create a request cylce factory which is used by default by WebSession.
	 * You may provide your own default factory by subclassing WebApplication
	 * and overriding this method or your may subclass WebSession to create a
	 * session specific request cycle factory.
	 * 
	 * @see WebSession#getRequestCycleFactory()
	 * @see IRequestCycleFactory
	 * 
	 * @return Request cycle factory
	 */
	protected IRequestCycleFactory getDefaultRequestCycleFactory()
	{
		return new IRequestCycleFactory()
		{
			private static final long serialVersionUID = 1L;

			public RequestCycle newRequestCycle(Session session, Request request, Response response)
			{
				// Respond to request
				return new WebRequestCycle((WebSession)session, (WebRequest)request,
						(WebResponse)response);
			}
		};
	}

	/**
	 * Gets the default request cycle processor (with lazy initialization). This
	 * is the {@link IRequestCycleProcessor} that will be used by
	 * {@link RequestCycle}s when custom implementations of the request cycle
	 * do not provide their own customized versions.
	 * 
	 * @return the default request cycle processor
	 */
	protected IRequestCycleProcessor getDefaultRequestCycleProcessor()
	{
		if (requestCycleProcessor == null)
		{
			requestCycleProcessor = new DefaultWebRequestCycleProcessor();
		}
		return requestCycleProcessor;
	}

	/**
	 * @see wicket.Application#getSessionFactory()
	 */
	protected ISessionFactory getSessionFactory()
	{
		return sessionFactory;
	}

	/**
	 * Initialize; if you need the wicket servlet for initialization, e.g.
	 * because you want to read an initParameter from web.xml or you want to
	 * read a resource from the servlet's context path, you can override this
	 * method and provide custom initialization. This method is called right
	 * after this application class is constructed, and the wicket servlet is
	 * set.
	 */
	protected void init()
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 */
	protected void internalDestroy()
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Internal intialization. Reads servlet init parameter "configuration". If
	 * the parameter is "development", settings appropriate for development are
	 * set. If it's "deployment", deployment settings are used. If development
	 * configuration is specified and a "sourceFolder" init parameter is also
	 * set, then resources in that folder will be polled for changes.
	 */
	protected void internalInit()
	{
		super.internalInit();
		final String configuration = wicketServlet.getInitParameter("configuration");
		if (configuration != null)
		{
			getSettings().configure(configuration, wicketServlet.getInitParameter("sourceFolder"));
		}
	}

	/**
	 * Create a new WebRequest. Subclasses of WebRequest could e.g. decode and
	 * obfuscated URL which has been encoded by an appropriate WebResponse.
	 * 
	 * @param servletRequest
	 * @return a WebRequest object
	 */
	protected WebRequest newWebRequest(final HttpServletRequest servletRequest)
	{
		return new ServletWebRequest(servletRequest);
	}

	/**
	 * Create a WebResponse. Subclasses of WebRequest could e.g. encode wicket's
	 * default URL and hide the details from the user. A appropriate WebRequest
	 * must be implemented and configured to decode the encoded URL.
	 * 
	 * @param servletResponse
	 * @return a WebResponse object
	 */
	protected WebResponse newWebResponse(final HttpServletResponse servletResponse)
	{
		return (getSettings().getBufferResponse()
				? new BufferedWebResponse(servletResponse)
				: new WebResponse(servletResponse));
	}

	/**
	 * Add a buffered response to the redirect buffer.
	 * 
	 * @param sessionId
	 *            the session id
	 * @param bufferId
	 *            the id that should be used for storing the buffer
	 * @param renderedResponse
	 *            the response to buffer
	 */
	final void addBufferedResponse(String sessionId, String bufferId,
			BufferedResponse renderedResponse)
	{
		Map responsesPerSession = (Map)bufferedResponses.get(sessionId);
		if (responsesPerSession == null)
		{
			responsesPerSession = new MostRecentlyUsedMap(4);
			bufferedResponses.put(sessionId, responsesPerSession);
		}
		responsesPerSession.put(bufferId, renderedResponse);
	}

	/**
	 * Cleans up any buffered response map for the client with the provided
	 * session id.
	 * 
	 * @param sessionId
	 *            the session id
	 */
	final void clearBufferedResponses(String sessionId)
	{
		bufferedResponses.remove(sessionId);

		// TODO implement call to this method: probably have to register a
		// session listener?
		// or take a cleaner thread instead of this method
	}

	/**
	 * Gets a WebSession object from the HttpServletRequest, creating a new one
	 * if it doesn't already exist.
	 * 
	 * @param request
	 *            The http request object
	 * @return The session object
	 */
	final WebSession getSession(final WebRequest request)
	{
		// Get session, creating if it doesn't exist
		// do not create it as we try to defer the actual
		// creation as long as we can
		final HttpSession httpSession = request.getHttpServletRequest().getSession(false);

		// The actual attribute for the session is
		// "wicket-<servletName>-session"
		final String sessionAttribute = getSessionAttributePrefix(request)
				+ Session.SESSION_ATTRIBUTE_NAME;

		WebSession webSession = null;
		if (httpSession != null)
		{
			// Get Session abstraction from httpSession attribute
			webSession = (WebSession)httpSession.getAttribute(sessionAttribute);
		}

		if (webSession == null)
		{
			// Create session using session factory
			final Session session = getSessionFactory().newSession();
			if (session instanceof WebSession)
			{
				webSession = (WebSession)session;
			}
			else
			{
				throw new WicketRuntimeException(
						"Session created by a WebApplication session factory must be a subclass of WebSession");
			}

			// Set the client Locale for this session
			webSession.setLocale(request.getLocale());

			if (httpSession != null)
			{
				// Save this session in the HttpSession using the attribute name
				httpSession.setAttribute(sessionAttribute, webSession);
			}
		}

		// Set application on session
		webSession.setApplication(this);

		// Set session attribute name and attach/reattach http servlet session
		webSession.init(getSessionAttributePrefix(request));

		return webSession;
	}

	/**
	 * Returns the redirect map where the buffered render pages are stored in
	 * and removes it immediately.
	 * 
	 * @param sessionId
	 *            the session id
	 * 
	 * @param bufferId
	 *            the id of the buffer as passed in as a request parameter
	 * @return the buffered response or null if not found (when this request is
	 *         on a different box than the original request came in
	 */
	final BufferedResponse popBufferedResponse(String sessionId, String bufferId)
	{
		Map responsesPerSession = (Map)bufferedResponses.get(sessionId);
		if (responsesPerSession != null)
		{
			BufferedResponse buffered = (BufferedResponse)responsesPerSession.remove(bufferId);
			return buffered;
		}
		return null;
	}

	/**
	 * Checks mount path is valid.
	 * 
	 * @param path
	 *            mount path
	 */
	private void checkMountPath(String path)
	{
		if (path == null)
		{
			throw new IllegalArgumentException("mounting path cannot be null");
		}
		if (!path.startsWith("/"))
		{
			throw new IllegalArgumentException("mounting path has to start with '/'");
		}
	}
}
