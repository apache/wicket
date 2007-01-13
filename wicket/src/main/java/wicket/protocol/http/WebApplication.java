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
package wicket.protocol.http;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Application;
import wicket.IPageMap;
import wicket.IRequestCycleFactory;
import wicket.ISessionFactory;
import wicket.Page;
import wicket.Request;
import wicket.RequestCycle;
import wicket.Response;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.markup.html.pages.AccessDeniedPage;
import wicket.markup.html.pages.InternalErrorPage;
import wicket.markup.html.pages.PageExpiredErrorPage;
import wicket.markup.resolver.AutoLinkResolver;
import wicket.protocol.http.servlet.ServletWebRequest;
import wicket.request.IRequestCycleProcessor;
import wicket.request.target.coding.BookmarkablePageRequestTargetUrlCodingStrategy;
import wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import wicket.request.target.coding.PackageRequestTargetUrlCodingStrategy;
import wicket.request.target.coding.SharedResourceRequestTargetUrlCodingStrategy;
import wicket.session.ISessionStore;
import wicket.util.collections.MostRecentlyUsedMap;
import wicket.util.file.WebApplicationPath;
import wicket.util.lang.PackageName;
import wicket.util.watch.ModificationWatcher;

/**
 * A web application is a subclass of Application which associates with an
 * instance of WicketServlet to serve pages over the HTTP protocol. This class
 * is intended to be subclassed by framework clients to define a web
 * application.
 * <p>
 * Application settings are given defaults by the WebApplication() constructor
 * and internalInit method, such as error page classes appropriate for HTML.
 * WebApplication subclasses can override these values and/or modify other
 * application settings by overriding the init() method and then by calling
 * getXXXSettings() to retrieve an interface to a mutable Settings object. Do
 * not do this in the constructor itself because the defaults will then override
 * your settings.
 * <p>
 * If you want to use a filter specific configuration, e.g. using init parameters
 * from the {@link javax.servlet.FilterConfig} object, you should override the
 * init() method. For example:
 * 
 * <pre>
 *   public void init()
 *   {
 *       String webXMLParameter = getInitParameter(&quot;myWebXMLParameter&quot;);
 *       URL schedulersConfig = getServletContext().getResource(&quot;/WEB-INF/schedulers.xml&quot;);
 *       ...
 * </pre>
 * 
 * @see WicketFilter
 * @see wicket.settings.IApplicationSettings
 * @see wicket.settings.IDebugSettings
 * @see wicket.settings.IExceptionSettings
 * @see wicket.settings.IMarkupSettings
 * @see wicket.settings.IPageSettings
 * @see wicket.settings.IRequestCycleSettings
 * @see wicket.settings.IResourceSettings
 * @see wicket.settings.ISecuritySettings
 * @see wicket.settings.ISessionSettings
 * @see javax.servlet.Filter
 * @see javax.servlet.FilterConfig
 * @see javax.servlet.ServletContext
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Johan Compagner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 */
public abstract class WebApplication extends Application implements ISessionFactory
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(WebApplication.class);

	/**
	 * Get WebApplication for current thread.
	 * 
	 * @return The current thread's Application
	 */
	public static WebApplication get()
	{
		return (WebApplication)Application.get();
	}

	/**
	 * The cached application key. Will be set in
	 * {@link #setWicketServlet(WicketServlet)} based on the servlet context.
	 */
	private String applicationKey;

	/**
	 * Map of buffered responses that are in progress per session. Buffered
	 * responses are temporarily stored
	 */
	private final Map<String, Map<String, BufferedHttpServletResponse>> bufferedResponses = new HashMap<String, Map<String, BufferedHttpServletResponse>>();

	/** the default request cycle processor implementation. */
	private IRequestCycleProcessor requestCycleProcessor;

	/**
	 * the prefix for storing variables in the actual session (typically
	 * {@link HttpSession} for this application instance.
	 */
	private String sessionAttributePrefix;

	/** Session factory for this web application */
	private ISessionFactory sessionFactory = this;

	/** The WicketServlet that this application is attached to */
	private WicketFilter wicketFilter;

	/**
	 * Constructor. <strong>Use {@link #init()} for any configuration of your
	 * application instead of overriding the constructor.</strong>
	 */
	public WebApplication()
	{
	}

	/**
	 * @see wicket.Application#getApplicationKey()
	 */
	@Override
	public final String getApplicationKey()
	{
		if (applicationKey == null)
		{
			throw new IllegalStateException("the application key does not seem to"
					+ " be set properly or this method is called before WicketServlet is"
					+ " set, which leads to the wrong behavior");
		}
		return applicationKey;
	}

	/**
	 * Gets an init parameter from the filter's context.
	 * 
	 * @param key
	 *            the key to search for
	 * @return the value of the filter init parameter
	 */
	public final String getInitParameter(String key)
	{
		if (wicketFilter != null)
		{
			return wicketFilter.getFilterConfig().getInitParameter(key);
		}
		throw new IllegalStateException("servletContext is not set yet. Any code in your"
				+ " Application object that uses the wicketServlet/Filter instance should be put"
				+ " in the init() method instead of your constructor");
	}

	/**
	 * Gets the default request cycle processor (with lazy initialization). This
	 * is the {@link IRequestCycleProcessor} that will be used by
	 * {@link RequestCycle}s when custom implementations of the request cycle
	 * do not provide their own customized versions.
	 * 
	 * @return the default request cycle processor
	 */
	public final IRequestCycleProcessor getRequestCycleProcessor()
	{
		if (requestCycleProcessor == null)
		{
			requestCycleProcessor = newRequestCycleProcessor();
		}
		return requestCycleProcessor;
	}

	/**
	 * Returns the full rootpath of this application. This is the
	 * ApplicationSettings.contextpath and the WicketFilter.rootpath concatted.
	 * 
	 * @return String the full rootpath.
	 */
	public String getRootPath()
	{
		return wicketFilter.getRootPath(WebRequestCycle.get().getWebRequest()
				.getHttpServletRequest());
	}

	/**
	 * Gets the servlet context for this application. Use this to get references
	 * to absolute paths, global web.xml parameters (<context-param>), etc.
	 * 
	 * @return The servlet context for this application
	 */
	public final ServletContext getServletContext()
	{
		if (wicketFilter != null)
		{
			return wicketFilter.getFilterConfig().getServletContext();
		}
		throw new IllegalStateException("servletContext is not set yet. Any code in your"
				+ " Application object that uses the wicket filter instance should be put"
				+ " in the init() method instead of your constructor");
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
			sessionAttributePrefix = "wicket:" + servletPath + ":";
		}
		// Namespacing for session attributes is provided by
		// adding the servlet path
		return sessionAttributePrefix;
	}

	/**
	 * @return The Wicket filter for this application
	 */
	public final WicketFilter getWicketFilter()
	{
		return wicketFilter;
	}

	/**
	 * Mounts an encoder at the given path.
	 * 
	 * @param path
	 *            the path to mount the encoder on
	 * @param encoder
	 *            the encoder that will be used for this mount
	 */
	public final void mount(IRequestTargetUrlCodingStrategy encoder)
	{
		checkMountPath(encoder.getMountPath());

		if (encoder == null)
		{
			throw new IllegalArgumentException("Encoder must be not null");
		}

		getRequestCycleProcessor().getRequestCodingStrategy().mount(encoder);
	}

	/**
	 * Mounts all bookmarkable pages at the given path.
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page class on
	 * @param packageName
	 *            the name of the package for which all bookmarkable pages or
	 *            sharedresources should be mounted
	 */
	public final void mount(final String path, final PackageName packageName)
	{
		if (packageName == null)
		{
			throw new IllegalArgumentException("PackageName cannot be null");
		}
		mount(new PackageRequestTargetUrlCodingStrategy(path, packageName));
	}

	/**
	 * Mounts a bookmarkable page class to the given path.
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page class on
	 * @param bookmarkablePageClass
	 *            the bookmarkable page class to mount
	 */
	public final void mountBookmarkablePage(final String path,
			final Class<? extends Page> bookmarkablePageClass)
	{
		mount(new BookmarkablePageRequestTargetUrlCodingStrategy(path, bookmarkablePageClass, null));
	}

	/**
	 * Mounts a bookmarkable page class to the given pagemap and path.
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page class on
	 * @param pageMap
	 *            pagemap this mount is for
	 * @param bookmarkablePageClass
	 *            the bookmarkable page class to mount
	 */
	public final void mountBookmarkablePage(final String path, final IPageMap pageMap,
			final Class<? extends Page> bookmarkablePageClass)
	{
		mount(new BookmarkablePageRequestTargetUrlCodingStrategy(path, bookmarkablePageClass,
				pageMap.getName()));
	}

	/**
	 * Mounts a shared resource class to the given path.
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page class on
	 * @param resourceKey
	 *            the shared key of the resource being mounted
	 */
	public final void mountSharedResource(final String path, final String resourceKey)
	{
		mount(new SharedResourceRequestTargetUrlCodingStrategy(path, resourceKey));
	}

	/**
	 * Create new Wicket Session object. Note, this method is not called if you
	 * registered your own ISessionFactory with the Application.
	 * 
	 * @return The created session
	 * @deprecated DO NOT CALL THIS METHOD, BUT RATHER
	 *             {@link WebApplication#newSession(Request)}.
	 */
	// FIXME remove this method after 2.0.0
	public final Session newSession()
	{
		throw new UnsupportedOperationException("this method is replaced by Application#newSession");
	}

	/**
	 * @see wicket.ISessionFactory#newSession(wicket.Request)
	 */
	public Session newSession(Request request)
	{
		return new WebSession(WebApplication.this, request);
	}

	/**
	 * @param sessionId
	 *            The session id that was destroyed
	 */
	@Override
	public void sessionDestroyed(String sessionId)
	{
		super.sessionDestroyed(sessionId);
		bufferedResponses.remove(sessionId);
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
	 * @param wicketFilter
	 *            The wicket filter instance for this application
	 */
	public final void setWicketFilter(final WicketFilter wicketFilter)
	{
		this.wicketFilter = wicketFilter;
		this.applicationKey = wicketFilter.getFilterConfig().getFilterName();
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
		getRequestCycleProcessor().getRequestCodingStrategy().unmount(path);
	}

	/**
	 * @return Request cycle factory
	 * @deprecated use {@link #getRequestCycleFactory()} instead
	 * 
	 * TODO Post 2.0: Remove
	 */
	@Deprecated
	protected final IRequestCycleFactory getDefaultRequestCycleFactory()
	{
		return getRequestCycleFactory();
	}

	/**
	 * Create a request cycle factory which is used by default by WebSession.
	 * You may provide your own default factory by subclassing WebApplication
	 * and overriding this method or your may subclass WebSession to create a
	 * session specific request cycle factory.
	 * 
	 * @see WebSession#getRequestCycleFactory()
	 * @see IRequestCycleFactory
	 * 
	 * @return Request cycle factory
	 */
	protected IRequestCycleFactory getRequestCycleFactory()
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
	 * @see wicket.Application#getSessionFactory()
	 */
	@Override
	protected ISessionFactory getSessionFactory()
	{
		return this.sessionFactory;
	}

	/**
	 * Initialize; if you need the wicket servlet for initialization, e.g.
	 * because you want to read an initParameter from web.xml or you want to
	 * read a resource from the servlet's context path, you can override this
	 * method and provide custom initialization. This method is called right
	 * after this application class is constructed, and the wicket servlet is
	 * set. <strong>Use this method for any application setup instead of the
	 * constructor.</strong>
	 */
	@Override
	protected void init()
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 */
	@Override
	protected void internalDestroy()
	{
		ModificationWatcher resourceWatcher = getResourceSettings().getResourceWatcher(false);
		if (resourceWatcher != null)
		{
			resourceWatcher.destroy();
		}
		super.internalDestroy();
		bufferedResponses.clear();
		// destroy the resource watcher
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Internal intialization. First determine the deployment mode. First check
	 * the system property -Dwicket.configuration. If it does not exist check
	 * the servlet init parameter (
	 * <code>&lt;init-param&gt&lt;param-name&gt;configuration&lt;/param-name&gt;</code>).
	 * If not found check the servlet context init paramert
	 * <code>&lt;context-param&gt&lt;param-name6gt;configuration&lt;/param-name&gt;</code>).
	 * If the parameter is "development" (which is default), settings
	 * appropriate for development are set. If it's "deployment" , deployment
	 * settings are used. If development is specified and a "sourceFolder" init
	 * parameter is also set, then resources in that folder will be polled for
	 * changes.
	 */
	@Override
	protected void internalInit()
	{
		super.internalInit();

		// Set default error pages for HTML markup
		getApplicationSettings().setPageExpiredErrorPage(PageExpiredErrorPage.class);
		getApplicationSettings().setInternalErrorPage(InternalErrorPage.class);
		getApplicationSettings().setAccessDeniedPage(AccessDeniedPage.class);

		// Add resolver for automatically resolving HTML links
		getPageSettings().addComponentResolver(new AutoLinkResolver());

		// Set resource finder to web app path
		getResourceSettings().setResourceFinder(
				new WebApplicationPath(wicketFilter.getFilterConfig().getServletContext()));

		String contextPath = wicketFilter.getFilterConfig().getInitParameter(
				Application.CONTEXTPATH);
		if (contextPath != null)
		{
			getApplicationSettings().setContextPath(contextPath);
		}

		// Check if system property -Dwicket.configuration exists
		String configuration = null;
		try
		{
			configuration = System.getProperty("wicket." + Application.CONFIGURATION);
		}
		catch (SecurityException e)
		{
			// ignore; it is not allowed to read system properties
		}

		// If no system parameter check servlet specific <init-param>
		if (configuration == null)
		{
			configuration = wicketFilter.getFilterConfig().getInitParameter(
					Application.CONFIGURATION);
		}
		// If no system parameter and not <init-param>, than check
		// <context-param>
		if (configuration == null)
		{
			configuration = wicketFilter.getFilterConfig().getServletContext().getInitParameter(
					Application.CONFIGURATION);
		}

		// Development mode is default if not settings have been found
		if (configuration != null)
		{
			configure(configuration, wicketFilter.getFilterConfig()
					.getInitParameter("sourceFolder"));
		}
		else
		{
			configure(Application.DEVELOPMENT, wicketFilter.getFilterConfig().getInitParameter(
					"sourceFolder"));
		}
	}

	/**
	 * May be replaced by subclasses which whishes to uses there own
	 * implementation of IRequestCycleProcessor
	 * 
	 * @return IRequestCycleProcessor
	 */
	// TODO Doesn't this method belong in Application, not WebApplication?
	protected IRequestCycleProcessor newRequestCycleProcessor()
	{
		return new DefaultWebRequestCycleProcessor();
	}

	/**
	 * @see wicket.Application#newSessionStore()
	 */
	@Override
	protected ISessionStore newSessionStore()
	{
		return new SecondLevelCacheSessionStore(new FilePageStore());
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
		return (getRequestCycleSettings().getBufferResponse() ? new BufferedWebResponse(
				servletResponse) : new WebResponse(servletResponse));
	}

	/*
	 * Set the application key value
	 */
	protected final void setApplicationKey(String applicationKey)
	{
		this.applicationKey = applicationKey;
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
			BufferedHttpServletResponse renderedResponse)
	{
		Map<String, BufferedHttpServletResponse> responsesPerSession = bufferedResponses
				.get(sessionId);
		if (responsesPerSession == null)
		{
			responsesPerSession = new MostRecentlyUsedMap<String, BufferedHttpServletResponse>(4);
			bufferedResponses.put(sessionId, responsesPerSession);
		}
		responsesPerSession.put(bufferId, renderedResponse);
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
		ISessionStore sessionStore = getSessionStore();
		Session session = sessionStore.lookup(request);

		if (session == null)
		{
			// Create session using session factory
			session = getSessionFactory().newSession(request);
			// Set the client Locale for this session
			session.setLocale(request.getLocale());

			if (sessionStore.getSessionId(request, false) != null)
			{
				// Bind the session to the session store
				sessionStore.bind(request, session);
			}
		}

		WebSession webSession;
		if (session instanceof WebSession)
		{
			webSession = (WebSession)session;
		}
		else
		{
			throw new WicketRuntimeException("Session created by a WebApplication session factory "
					+ "must be a subclass of WebSession");
		}

		// Set session attribute name and attach/reattach http servlet session
		webSession.initForRequest();

		return webSession;
	}

	/**
	 * Log that this application is started.
	 */
	final void logStarted()
	{
		String version = getFrameworkSettings().getVersion();
		StringBuffer b = new StringBuffer();
		b.append("[").append(getName()).append("] Started Wicket ");
		if (!"n/a".equals(version))
		{
			b.append("version ").append(version).append(" ");
		}
		b.append("in ").append(getConfigurationType()).append(" mode");
		log.info(b.toString());
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
	final BufferedHttpServletResponse popBufferedResponse(String sessionId, String bufferId)
	{
		Map responsesPerSession = bufferedResponses.get(sessionId);
		if (responsesPerSession != null)
		{
			BufferedHttpServletResponse buffered = (BufferedHttpServletResponse)responsesPerSession
					.remove(bufferId);
			if (responsesPerSession.size() == 0)
			{
				bufferedResponses.remove(sessionId);
			}
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
			throw new IllegalArgumentException("Mount path cannot be null");
		}
		if (!path.startsWith("/"))
		{
			throw new IllegalArgumentException("Mount path has to start with '/'");
		}
		if (path.startsWith("/resources/") || path.equals("/resources"))
		{
			throw new IllegalArgumentException("Mount path cannot start with '/resources'");
		}
	}
}
