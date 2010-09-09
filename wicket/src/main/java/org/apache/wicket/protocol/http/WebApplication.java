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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.Application;
import org.apache.wicket.IPageRendererProvider;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.pages.AccessDeniedPage;
import org.apache.wicket.markup.html.pages.InternalErrorPage;
import org.apache.wicket.markup.html.pages.PageExpiredErrorPage;
import org.apache.wicket.markup.resolver.AutoLinkResolver;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.handler.render.PageRenderer;
import org.apache.wicket.request.handler.render.WebPageRenderer;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.MountedMapper;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.session.HttpSessionStore;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.collections.MostRecentlyUsedMap;
import org.apache.wicket.util.file.FileCleaner;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.file.WebApplicationPath;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.watch.IModificationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A web application is a subclass of Application which associates with an instance of WicketServlet
 * to serve pages over the HTTP protocol. This class is intended to be subclassed by framework
 * clients to define a web application.
 * <p>
 * Application settings are given defaults by the WebApplication() constructor and internalInit
 * method, such as error page classes appropriate for HTML. WebApplication subclasses can override
 * these values and/or modify other application settings by overriding the init() method and then by
 * calling getXXXSettings() to retrieve an interface to a mutable Settings object. Do not do this in
 * the constructor itself because the defaults will then override your settings.
 * <p>
 * If you want to use a filter specific configuration, e.g. using init parameters from the
 * {@link javax.servlet.FilterConfig} object, you should override the init() method. For example:
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

	private ServletContext servletContext;

	/**
	 * Covariant override for easy getting the current {@link WebApplication} without having to cast
	 * it.
	 */
	public static WebApplication get()
	{
		Application application = Application.get();

		if (application instanceof WebApplication == false)
		{
			throw new WicketRuntimeException(
				"The application attached to the current thread is not a " +
					WebApplication.class.getSimpleName());
		}

		return (WebApplication)application;
	}

	/**
	 * Map of buffered responses that are in progress per session. Buffered responses are
	 * temporarily stored
	 */
	private final ConcurrentHashMap<String, Map<String, BufferedHttpServletResponse>> bufferedResponses = Generics.newConcurrentHashMap();

	/**
	 * the prefix for storing variables in the actual session (typically {@link HttpSession} for
	 * this application instance.
	 */
	private String sessionAttributePrefix;

	/** The WicketFilter that this application is attached to */
	private WicketFilter wicketFilter;


	/**
	 * Constructor. <strong>Use {@link #init()} for any configuration of your application instead of
	 * overriding the constructor.</strong>
	 */
	public WebApplication()
	{
	}

	/**
	 * @see org.apache.wicket.Application#getApplicationKey()
	 */
	@Deprecated
	@Override
	public final String getApplicationKey()
	{
		return getName();
	}

	/**
	 * Gets an init parameter of the filter, or null if the parameter does not exist.
	 * 
	 * @param key
	 *            the key to search for
	 * @return the value of the filter init parameter
	 */
	public String getInitParameter(String key)
	{
		if (wicketFilter != null)
		{
			return wicketFilter.getFilterConfig().getInitParameter(key);
		}
		throw new IllegalStateException("init parameter '" + key +
			"' is not set yet. Any code in your" +
			" Application object that uses the wicketServlet/Filter instance should be put" +
			" in the init() method instead of your constructor");
	}


	/**
	 * Sets servlet context this application runs after. This is uaully done from a filter or a
	 * servlet responsible for managing this application object, such as {@link WicketFilter}
	 * 
	 * @param servletContext
	 */
	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	/**
	 * Gets the servlet context for this application. Use this to get references to absolute paths,
	 * global web.xml parameters (&lt;context-param&gt;), etc.
	 * 
	 * @return The servlet context for this application
	 */
	public ServletContext getServletContext()
	{
		if (servletContext == null)
		{
			throw new IllegalStateException("servletContext is not set yet. Any code in your"
				+ " Application object that uses the wicket filter instance should be put"
				+ " in the init() method instead of your constructor");
		}
		return servletContext;
	}

	/**
	 * Gets the prefix for storing variables in the actual session (typically {@link HttpSession}
	 * for this application instance.
	 * 
	 * @param request
	 *            the request
	 * @param filterName
	 *            If null, than it defaults to the WicketFilter filter name. However according to
	 *            the ServletSpec the Filter is not guaranteed to be initialized e.g. when
	 *            WicketSessionFilter gets initialized. Thus, though you (and WicketSessionFilter)
	 *            can provide a filter name, you must make sure it is the same as WicketFilter will
	 *            provide once initialized.
	 * 
	 * @return the prefix for storing variables in the actual session
	 */
	public String getSessionAttributePrefix(final WebRequest request, String filterName)
	{
		if (sessionAttributePrefix == null)
		{
			if (filterName == null)
			{
				// According to the ServletSpec, the filter might not yet been initialized
				filterName = getWicketFilter().getFilterConfig().getFilterName();
			}
			sessionAttributePrefix = "wicket:" + filterName + ":";
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
	 * @see org.apache.wicket.Application#logEventTarget(org.apache.wicket.request.IRequestHandler)
	 */
	@Override
	public void logEventTarget(IRequestHandler target)
	{
		super.logEventTarget(target);
		IRequestLogger rl = getRequestLogger();
		if (rl != null)
		{
			rl.logEventTarget(target);
		}
	}

	/**
	 * @see org.apache.wicket.Application#logResponseTarget(org.apache.wicket.request.IRequestHandler)
	 */
	@Override
	public void logResponseTarget(IRequestHandler target)
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
	 * @param mapper
	 *            the encoder that will be used for this mount
	 * 
	 * @deprecated this is the same as {@code getRootRequestMapperAsCompound().add(mapper)}
	 */
	@Deprecated
	public final void mount(IRequestMapper mapper)
	{
		Args.notNull(mapper, "mapper");
		getRootRequestMapperAsCompound().add(mapper);
	}

	/**
	 * Mounts a bookmarkable page class to the given path.
	 * 
	 * @param <T>
	 *            type of page
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page class on
	 * @param bookmarkablePageClass
	 *            the bookmarkable page class to mount
	 * 
	 * @deprecated use mounted mapper instead, this method can be represented as
	 *             {@code
	 *             getRootRequestMapperAsCompound().add(new MountedMapper(path,
	 *             bookmarkablePageClass))}
	 */
	@Deprecated
	public final <T extends Page> void mountBookmarkablePage(final String path,
		final Class<T> bookmarkablePageClass)
	{
		mount(new MountedMapper(path, bookmarkablePageClass));
	}


	/**
	 * Mounts a shared resource class to the given path.
	 * 
	 * @param path
	 *            the path to mount the resource class on
	 * @param reference
	 *            resource reference to be mounted
	 * 
	 * @deprecated - not sure what to use yet but this will be deprecated :)
	 */
	@Deprecated
	public final void mountSharedResource(final String path, final ResourceReference reference)
	{
		getResourceReferenceRegistry().registerResourceReference(reference);
		// TODO NG Implement mounted resources mapper
		throw new UnsupportedOperationException("Implement Mapper");
	}


	/**
	 * Partly unmounts/ignores a path that normally would map to another mount path. Like
	 * mount("/mypage", MyPage.class); and then "/mypage/arealdir" should be ignored. This can be
	 * done by calling unMount("/mypage/arealdir");
	 * 
	 * @param path
	 *            the path that should be ignored.
	 * 
	 */
	public final void addIgnoreMountPath(String path)
	{
		// TODO how is this supposed to work :/
		throw new UnsupportedOperationException();
	}

	/**
	 * Create a new WebRequest. Subclasses of WebRequest could e.g. decode and obfuscated URL which
	 * has been encoded by an appropriate WebResponse.
	 * 
	 * @param servletRequest
	 * @param filterPath
	 *            the filter mapping read from web.xml
	 * @return a WebRequest object
	 */
	protected WebRequest newWebRequest(final HttpServletRequest servletRequest,
		final String filterPath)
	{
		return new ServletWebRequest(servletRequest, filterPath);
	}

	/**
	 * @see org.apache.wicket.Application#newSession(org.apache.wicket.request.Request,
	 *      org.apache.wicket.request.Response)
	 */
	@Override
	public Session newSession(Request request, Response response)
	{
		return new WebSession(request);
	}

	/**
	 * @see org.apache.wicket.Application#sessionUnbound(java.lang.String)
	 */
	@Override
	public void sessionUnbound(final String sessionId)
	{
		super.sessionUnbound(sessionId);

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
		Args.notNull(wicketFilter, "wicketFilter");
		this.wicketFilter = wicketFilter;
		servletContext = wicketFilter.getFilterConfig().getServletContext();
	}

	/**
	 * Initialize; if you need the wicket servlet/filter for initialization, e.g. because you want
	 * to read an initParameter from web.xml or you want to read a resource from the servlet's
	 * context path, you can override this method and provide custom initialization. This method is
	 * called right after this application class is constructed, and the wicket servlet/filter is
	 * set. <strong>Use this method for any application setup instead of the constructor.</strong>
	 */
	@Override
	protected void init()
	{
		super.init();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 */
	@Override
	public void internalDestroy()
	{
		// destroy the resource watcher
		IModificationWatcher resourceWatcher = getResourceSettings().getResourceWatcher(false);
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
	 * Internal initialization. First determine the deployment mode. First check the system property
	 * -Dwicket.configuration. If it does not exist check the servlet init parameter (
	 * <code>&lt;init-param&gt&lt;param-name&gt;configuration&lt;/param-name&gt;</code>). If not
	 * found check the servlet context init parameter
	 * <code>&lt;context-param&gt&lt;param-name6gt;configuration&lt;/param-name&gt;</code>). If the
	 * parameter is "development" (which is default), settings appropriate for development are set.
	 * If it's "deployment" , deployment settings are used. If development is specified and a
	 * "sourceFolder" init parameter is also set, then resources in that folder will be polled for
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
		getResourceSettings().setResourceFinder(getResourceFinder());

		// Add optional sourceFolder for resources.
		String resourceFolder = getInitParameter("sourceFolder");
		if (resourceFolder != null)
		{
			getResourceSettings().addResourceFolder(resourceFolder);
		}

		setPageRendererProvider(new WebPageRendererProvider());
		setSessionStoreProvider(new WebSessionStoreProvider());
		// Configure the app.
		configure();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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

		// If no system parameter check filter/servlet <init-param> and <context-param>
		if (result == null)
		{
			result = getInitParameter("wicket." + Application.CONFIGURATION);
		}
		if (result == null)
		{
			result = getServletContext().getInitParameter("wicket." + Application.CONFIGURATION);
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

	/**
	 * By default it return a WebApplicationPath
	 * 
	 * @return resource finder
	 */
	protected IResourceFinder getResourceFinder()
	{
		return new WebApplicationPath(getServletContext());
	}

	/**
	 * Creates a new ajax request target used to control ajax responses
	 * 
	 * @param page
	 *            page on which ajax response is made
	 * @return non-null ajax request target instance
	 */
	public AjaxRequestTarget newAjaxRequestTarget(final Page page)
	{
		return new AjaxRequestTarget(page);
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
		Map<String, BufferedHttpServletResponse> responsesPerSession = bufferedResponses.get(sessionId);
		if (responsesPerSession == null)
		{
			responsesPerSession = Collections.synchronizedMap(new MostRecentlyUsedMap<String, BufferedHttpServletResponse>(
				4));
			Map<String, BufferedHttpServletResponse> previousValue = bufferedResponses.putIfAbsent(
				sessionId, responsesPerSession);
			if (previousValue != null)
			{
				responsesPerSession = previousValue;
			}
		}
		String bufferKey = bufferId.startsWith("/") ? bufferId.substring(1) : bufferId;
		responsesPerSession.put(bufferKey, renderedResponse);
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
	 * This method prints a warning to stderr that we are starting in development mode.
	 * <p>
	 * If you really need to test Wicket in development mode on a staging server somewhere and are
	 * annoying the sysadmin for it with stderr messages, you can override this to make it do
	 * something else.
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

	// TODO: Do this properly
	private final Map<String, BufferedWebResponse> storedResponses = new ConcurrentHashMap<String, BufferedWebResponse>();

	public boolean hasBufferedResponse(String sessionId, Url url)
	{
		String key = sessionId + url.toString();
		return storedResponses.containsKey(key);
	}

	public BufferedWebResponse getAndRemoveBufferedResponse(String sessionId, Url url)
	{
		String key = sessionId + url.toString();
		return storedResponses.remove(key);
	}

	/**
	 * 
	 * @param sessionId
	 * @param url
	 * @param response
	 */
	public void storeBufferedResponse(String sessionId, Url url, BufferedWebResponse response)
	{
		String key = sessionId + url.toString();
		storedResponses.put(key, response);
	}

	// TODO remove after deprecation release

	/**
	 * Returns the redirect map where the buffered render pages are stored in and removes it
	 * immediately.
	 * 
	 * @param sessionId
	 *            the session id
	 * 
	 * @param bufferId
	 *            the id of the buffer as passed in as a request parameter
	 * @return the buffered response or null if not found (when this request is on a different box
	 *         than the original request came in
	 */
	final BufferedHttpServletResponse popBufferedResponse(String sessionId, String bufferId)
	{
		Map<String, BufferedHttpServletResponse> responsesPerSession = bufferedResponses.get(sessionId);
		if (responsesPerSession != null)
		{
			BufferedHttpServletResponse buffered = responsesPerSession.remove(bufferId);
			if (responsesPerSession.size() == 0)
			{
				bufferedResponses.remove(sessionId);
			}
			return buffered;
		}
		return null;
	}

	@Override
	public void set()
	{
		ThreadContext.setApplication(this);
	}

	@Override
	public String getMimeType(String fileName)
	{
		String mimeType = getServletContext().getMimeType(fileName);
		return mimeType != null ? mimeType : super.getMimeType(fileName);
	}

	private static class WebPageRendererProvider implements IPageRendererProvider
	{
		public PageRenderer get(RenderPageRequestHandler handler)
		{
			return new WebPageRenderer(handler);
		}
	}

	private static class WebSessionStoreProvider implements IProvider<ISessionStore>
	{

		public ISessionStore get()
		{
			return new HttpSessionStore();
		}

	}
}
