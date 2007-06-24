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
package org.apache.wicket.protocol.http;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.wicket.Application;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.pages.AccessDeniedPage;
import org.apache.wicket.markup.html.pages.InternalErrorPage;
import org.apache.wicket.markup.html.pages.PageExpiredErrorPage;
import org.apache.wicket.markup.resolver.AutoLinkResolver;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.request.target.coding.BookmarkablePageRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.PackageRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.SharedResourceRequestTargetUrlCodingStrategy;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.collections.MostRecentlyUsedMap;
import org.apache.wicket.util.file.FileCleaner;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.file.WebApplicationPath;
import org.apache.wicket.util.lang.PackageName;
import org.apache.wicket.util.watch.ModificationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
 * If you want to use a filter specific configuration, e.g. using init
 * parameters from the {@link javax.servlet.FilterConfig} object, you should
 * override the init() method. For example:
 * 
 * <pre>
 *  public void init() {
 *  String webXMLParameter = getInitParameter(&quot;myWebXMLParameter&quot;);
 *  URL schedulersConfig = getServletContext().getResource(&quot;/WEB-INF/schedulers.xml&quot;);
 *  ...
 * </pre>
 * 
 * @see WicketFilter
 * @see org.apache.wicket.settings.IApplicationSettings
 * @see org.apache.wicket.settings.IDebugSettings
 * @see org.apache.wicket.settings.IExceptionSettings
 * @see org.apache.wicket.settings.IMarkupSettings
 * @see org.apache.wicket.settings.IPageSettings
 * @see org.apache.wicket.settings.IRequestCycleSettings
 * @see org.apache.wicket.settings.IResourceSettings
 * @see org.apache.wicket.settings.ISecuritySettings
 * @see org.apache.wicket.settings.ISessionSettings
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
public abstract class WebApplication extends Application
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(WebApplication.class);

	/**
	 * The cached application key. Will be set in
	 * {@link #setWicketServlet(WicketServlet)} based on the servlet context.
	 */
	private String applicationKey;

	/**
	 * Map of buffered responses that are in progress per session. Buffered
	 * responses are temporarily stored
	 */
	private final Map bufferedResponses = new HashMap();

	/** the default request cycle processor implementation. */
	private IRequestCycleProcessor requestCycleProcessor;

	/**
	 * the prefix for storing variables in the actual session (typically
	 * {@link HttpSession} for this application instance.
	 */
	private String sessionAttributePrefix;

	/** The WicketFilter that this application is attached to */
	private WicketFilter wicketFilter;

	/**
	 * Constructor. <strong>Use {@link #init()} for any configuration of your
	 * application instead of overriding the constructor.</strong>
	 */
	public WebApplication()
	{
	}

	/**
	 * @see org.apache.wicket.Application#getApplicationKey()
	 */
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
	 * @see org.apache.wicket.Application#logEventTarget(org.apache.wicket.IRequestTarget)
	 */
	public void logEventTarget(IRequestTarget target)
	{
		super.logEventTarget(target);
		IRequestLogger rl = getRequestLogger();
		if (rl != null)
		{
			rl.logEventTarget(target);
		}
	}

	/**
	 * @see org.apache.wicket.Application#logResponseTarget(org.apache.wicket.IRequestTarget)
	 */
	public void logResponseTarget(IRequestTarget target)
	{
		super.logResponseTarget(target);
		IRequestLogger rl = getRequestLogger();
		if (rl != null)
		{
			rl.logResponseTarget(target);
		}
	}

	/**
	 * Mounts an encoder at the given path.
	 * 
	 * @param encoder
	 *            the encoder that will be used for this mount
	 */
	public final void mount(IRequestTargetUrlCodingStrategy encoder)
	{
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
	public final void mountBookmarkablePage(final String path, final Class bookmarkablePageClass)
	{
		mount(new BookmarkablePageRequestTargetUrlCodingStrategy(path, bookmarkablePageClass, null));
	}

	/**
	 * Mounts a bookmarkable page class to the given pagemap and path.
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page class on
	 * @param pageMapName
	 *            name of the pagemap this mount is for
	 * @param bookmarkablePageClass
	 *            the bookmarkable page class to mount
	 */
	public final void mountBookmarkablePage(final String path, final String pageMapName,
			final Class bookmarkablePageClass)
	{
		mount(new BookmarkablePageRequestTargetUrlCodingStrategy(path, bookmarkablePageClass,
				pageMapName));
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
	 * @see org.apache.wicket.Application#newRequestCycle(org.apache.wicket.Request,
	 *      org.apache.wicket.Response)
	 */
	public RequestCycle newRequestCycle(final Request request, final Response response)
	{
		return new WebRequestCycle(this, (WebRequest)request, (WebResponse)response);
	}

	/**
	 * Create new Wicket Session object. Note, this method is not called if you
	 * registered your own ISessionFactory with the Application.
	 * 
	 * @return The created session
	 * @deprecated see {@link WebApplication#newSession(Request, Response)}.
	 */
	// FIXME remove this method after 1.3.0
	public final Session newSession()
	{
		throw new UnsupportedOperationException("this method is replaced by Application#newSession");
	}

	/**
	 * Create new Wicket Session object. Note, this method is not called if you
	 * registered your own ISessionFactory with the Application.
	 * 
	 * @param request
	 * @return The created session
	 * @deprecated {@link WebApplication#newSession(Request, Response)}.
	 */
	// FIXME remove this method after 1.3.0
	public final Session newSession(Request request)
	{
		throw new UnsupportedOperationException("this method is replaced by Application#newSession");
	}

	/**
	 * @see org.apache.wicket.Application#newSession(org.apache.wicket.Request,
	 *      org.apache.wicket.Response)
	 */
	public Session newSession(Request request, Response response)
	{
		return new WebSession(WebApplication.this, request);
	}

	/**
	 * @param sessionId
	 *            The session id that was destroyed
	 */
	public void sessionDestroyed(String sessionId)
	{
		bufferedResponses.remove(sessionId);

		IRequestLogger logger = getRequestLogger();
		if (logger != null)
		{
			logger.sessionDestroyed(sessionId);
		}
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
		getRequestCycleProcessor().getRequestCodingStrategy().unmount(path);
	}

	/**
	 * @return nada
	 * @deprecated Replaced by {@link #newRequestCycle(Request, Response)}
	 */
	// TODO remove after compatibility release.
	protected final Object getDefaultRequestCycleFactory()
	{
		throw new UnsupportedOperationException("obsolete method. see getRequestCycleFactory");
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
	protected void init()
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 */
	protected void internalDestroy()
	{
		// destroy the resource watcher
		ModificationWatcher resourceWatcher = getResourceSettings().getResourceWatcher(false);
		if (resourceWatcher != null)
		{
			resourceWatcher.destroy();
		}
		super.internalDestroy();
		bufferedResponses.clear();
		getSessionStore().destroy();
		FileCleaner.destroy();
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
		getResourceSettings().setResourceFinder(getResourceFinder());

		// Add optional sourceFolder for resources.
		String resourceFolder = getInitParameter("sourceFolder");
		if (resourceFolder != null)
		{
			getResourceSettings().addResourceFolder(resourceFolder);
		}

		// Configure the app.
		configure();
	}

	/**
	 * @see org.apache.wicket.Application#getConfigurationType()
	 */
	public String getConfigurationType()
	{
		String result = null;
		try
		{
			result = System.getProperty("wicket." + Application.CONFIGURATION);
		}
		catch (SecurityException e)
		{
			// Ignore - we're not allowed to read system properties.
		}

		// If no system parameter check filter/servlet specific <init-param>
		if (result == null)
		{
			result = getInitParameter(Application.CONFIGURATION);
		}

		// If no system parameter and no <init-param>, then check
		// <context-param>
		if (result == null)
		{
			result = getServletContext().getInitParameter(Application.CONFIGURATION);
		}

		// Return result if we have found it, else fall back to DEVELOPMENT mode
		// as the default.
		if (result != null)
		{
			return result;
		}

		return Application.DEVELOPMENT;
	}

	protected IResourceFinder getResourceFinder()
	{
		return new WebApplicationPath(getServletContext());
	}

	/**
	 * Gets a new request cycle processor for web requests. May be replaced by
	 * subclasses which whishes to uses there own implementation of
	 * IRequestCycleProcessor.
	 * 
	 * NOTE this can't be moved to application as portlets use two different
	 * request cycle processors, and hence have two different methods for them,
	 * depending on the kind of request.
	 * 
	 * @return IRequestCycleProcessor
	 */
	protected IRequestCycleProcessor newRequestCycleProcessor()
	{
		return new WebRequestCycleProcessor();
	}

	/**
	 * @see org.apache.wicket.Application#newSessionStore()
	 */
	protected ISessionStore newSessionStore()
	{
		return new SecondLevelCacheSessionStore(this, new FilePageStore());
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
		Map responsesPerSession = (Map)bufferedResponses.get(sessionId);
		if (responsesPerSession == null)
		{
			responsesPerSession = new MostRecentlyUsedMap(4);
			bufferedResponses.put(sessionId, responsesPerSession);
		}
		responsesPerSession.put(bufferId, renderedResponse);
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
		if (DEVELOPMENT.equalsIgnoreCase(getConfigurationType()))
		{
			outputDevelopmentModeWarning();
		}
	}

	/**
	 * This method prints a warning to stderr that we are starting in
	 * development mode.
	 * <p>
	 * If you really need to test Wicket in development mode on a staging server
	 * somewhere and are annoying the sysadmin for it with stderr messages, you
	 * can override this to make it do something else.
	 */
	protected void outputDevelopmentModeWarning()
	{
		System.err.print("********************************************************************\n"
				+ "*** WARNING: Wicket is running in DEVELOPMENT mode.              ***\n"
				+ "***                               ^^^^^^^^^^^                    ***\n"
				+ "*** Do NOT deploy to your live server(s) without changing this.  ***\n"
				+ "*** See Application#getConfigurationType() for more information. ***\n"
				+ "********************************************************************\n");
	}

	// TODO remove after deprecation release

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
		Map responsesPerSession = (Map)bufferedResponses.get(sessionId);
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
}
