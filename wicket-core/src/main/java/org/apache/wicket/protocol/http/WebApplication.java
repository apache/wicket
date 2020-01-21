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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.function.Function;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestHandler;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTargetListenerCollection;
import org.apache.wicket.core.request.mapper.IMapperContext;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.core.request.mapper.PackageMapper;
import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.core.util.file.WebApplicationPath;
import org.apache.wicket.core.util.resource.ClassPathResourceFinder;
import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.AutoLabelResolver;
import org.apache.wicket.markup.html.form.AutoLabelTextResolver;
import org.apache.wicket.markup.html.pages.AccessDeniedPage;
import org.apache.wicket.markup.html.pages.InternalErrorPage;
import org.apache.wicket.markup.html.pages.PageExpiredErrorPage;
import org.apache.wicket.markup.resolver.AutoLinkResolver;
import org.apache.wicket.protocol.http.servlet.AbstractRequestWrapperFactory;
import org.apache.wicket.protocol.http.servlet.FilterFactoryManager;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.render.WebPageRenderer;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.ICompoundRequestMapper;
import org.apache.wicket.request.mapper.IRequestMapperDelegate;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.bundles.ReplacementResourceBundleReference;
import org.apache.wicket.session.HttpSessionStore;
import org.apache.wicket.util.crypt.CharEncoding;
import org.apache.wicket.util.file.FileCleaner;
import org.apache.wicket.util.file.IFileCleaner;
import org.apache.wicket.util.file.Path;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.PackageName;
import org.apache.wicket.util.string.Strings;
import java.time.Duration;
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
 * @see org.apache.wicket.settings.ApplicationSettings
 * @see org.apache.wicket.settings.DebugSettings
 * @see org.apache.wicket.settings.ExceptionSettings
 * @see org.apache.wicket.settings.MarkupSettings
 * @see org.apache.wicket.settings.PageSettings
 * @see org.apache.wicket.settings.RequestCycleSettings
 * @see org.apache.wicket.settings.ResourceSettings
 * @see org.apache.wicket.settings.SecuritySettings
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

	public static final String META_INF_RESOURCES = "META-INF/resources";

	private ServletContext servletContext;

	private final AjaxRequestTargetListenerCollection ajaxRequestTargetListeners;

	private Function<Page, AjaxRequestTarget> ajaxRequestTargetProvider;

	private FilterFactoryManager filterFactoryManager;

	/**
	 * Cached value of the parsed (from system properties or Servlet init/context parameter)
	 * <code>wicket.configuration</code> setting. No need to re-read it because it wont change at
	 * runtime.
	 */
	private RuntimeConfigurationType configurationType;

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
		ajaxRequestTargetListeners = new AjaxRequestTargetListenerCollection();
	}

	/**
	 * @see org.apache.wicket.Application#getApplicationKey()
	 */
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
			String namespace = getMapperContext().getNamespace();
			sessionAttributePrefix = namespace + ':' + filterName + ':';
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
	 */
	public void mount(final IRequestMapper mapper)
	{
		Args.notNull(mapper, "mapper");
		getRootRequestMapperAsCompound().add(mapper);
	}

	/**
	 * Mounts a page class to the given path.
	 * 
	 * <p>
	 * NOTE: mount path must not start with reserved URL segments! See {@link IMapperContext} to know
	 * which segments are reserved for internal use.
	 * </p>
	 * @param <T>
	 *            type of page
	 * 
	 * @param path
	 *            the path to mount the page class on
	 * @param pageClass
	 *            the page class to be mounted
	 */
	public <T extends Page> MountedMapper mountPage(final String path, final Class<T> pageClass)
	{
		MountedMapper mapper = new MountedMapper(path, pageClass);
		mount(mapper);
		return mapper;
	}

	/**
	 * Mounts a shared resource to the given path.
	 * 
	 * <p>
	 * NOTE: mount path must not start with reserved URL segments! See {@link IMapperContext} to know
	 * which segments are reserved for internal use.
	 * </p>
	 * @param path
	 *            the path to mount the resource reference on
	 * @param reference
	 *            resource reference to be mounted
	 */
	public ResourceMapper mountResource(final String path, final ResourceReference reference)
	{
		if (reference.canBeRegistered())
		{
			getResourceReferenceRegistry().registerResourceReference(reference);
		}
		ResourceMapper mapper = new ResourceMapper(path, reference);
		mount(mapper);
		return mapper;
	}

	/**
	 * Mounts mounts all bookmarkable pages in a the pageClass's package to the given path.
	 *
	 * <p>
	 * NOTE: mount path must not start with reserved URL segments! See {@link IMapperContext} to know
	 * which segments are reserved for internal use.
	 * </p>
	 * @param <P>
	 *            type of page
	 * 
	 * @param path
	 *            the path to mount the page class on
	 * @param pageClass
	 *            the page class to be mounted
	 */
	public <P extends Page> PackageMapper mountPackage(final String path, final Class<P> pageClass)
	{
		PackageMapper packageMapper = new PackageMapper(path, PackageName.forClass(pageClass));
		mount(packageMapper);
		return packageMapper;
	}

	/**
	 * Unregisters all {@link IRequestMapper}s which would match on a this path.
	 * <p>
	 * Useful in OSGi environments where a bundle may want to update the mount point.
	 * </p>
	 * 
	 * @param path
	 *            the path to unmount
	 */
	public void unmount(String path)
	{
		Args.notNull(path, "path");

		if (path.charAt(0) == '/')
		{
			path = path.substring(1);
		}

		IRequestMapper mapper = getRootRequestMapper();

		while (mapper instanceof IRequestMapperDelegate)
		{
			mapper = ((IRequestMapperDelegate) mapper).getDelegateMapper();
		}

		/*
		 * Only attempt to unmount if root request mapper is either a compound, or wraps a compound to avoid leaving the
		 * application with no mappers installed.
		 */
		if (mapper instanceof ICompoundRequestMapper)
		{
			final Url url = Url.parse(path);

			Request request = new Request()
			{
				@Override
				public Url getUrl()
				{
					return url;
				}

				@Override
				public Url getClientUrl()
				{
					return url;
				}

				@Override
				public Locale getLocale()
				{
					return null;
				}

				@Override
				public Charset getCharset()
				{
					return null;
				}

				@Override
				public Object getContainerRequest()
				{
					return null;
				}
			};

			unmountFromCompound((ICompoundRequestMapper) mapper, request);
		}
	}

	/**
	 * Descends the tree of {@link ICompoundRequestMapper}s and {@link IRequestMapperDelegate}s to find the correct descendant
	 * to remove.
	 *
	 * @param parent
	 *		The {@link ICompoundRequestMapper} from which to unmount the matching mapper.
	 * @param request
	 *		The request used to find the mapper to remove.
	 */
	private void unmountFromCompound(ICompoundRequestMapper parent, Request request)
	{
		Collection<IRequestMapper> toRemove = new LinkedList<>();

		for (IRequestMapper mapper : parent)
		{
			if (mapper.mapRequest(request) != null)
			{
				IRequestMapper actualMapper = mapper;

				while (actualMapper instanceof IRequestMapperDelegate)
				{
					actualMapper = ((IRequestMapperDelegate) actualMapper).getDelegateMapper();
				}

				if (actualMapper instanceof ICompoundRequestMapper)
				{
					unmountFromCompound((ICompoundRequestMapper) actualMapper, request);
				}
				else
				{
					toRemove.add(mapper);
				}
			}
		}

		for (IRequestMapper mapper : toRemove)
		{
			parent.remove(mapper);
		}
	}

	/**
	 * Registers a replacement resource for the given javascript resource. This replacement can be
	 * another {@link JavaScriptResourceReference} for a packaged resource, but it can also be an
	 * {@link org.apache.wicket.request.resource.UrlResourceReference} to replace the resource by a
	 * resource hosted on a CDN. Registering a replacement will cause the resource to replaced by
	 * the given resource throughout the application: if {@code base} is added, {@code replacement}
	 * will be added instead.
	 * 
	 * @param base
	 *            The resource to replace
	 * @param replacement
	 *            The replacement
	 */
	public final void addResourceReplacement(JavaScriptResourceReference base,
		ResourceReference replacement)
	{
		ReplacementResourceBundleReference bundle = new ReplacementResourceBundleReference(replacement);
		bundle.addProvidedResources(JavaScriptHeaderItem.forReference(base));
		getResourceBundles().addBundle(JavaScriptHeaderItem.forReference(bundle));
	}

	/**
	 * Registers a replacement resource for the given CSS resource. This replacement can be another
	 * {@link CssResourceReference} for a packaged resource, but it can also be an
	 * {@link org.apache.wicket.request.resource.UrlResourceReference} to replace the resource by a
	 * resource hosted on a CDN. Registering a replacement will cause the resource to replaced by
	 * the given resource throughout the application: if {@code base} is added, {@code replacement}
	 * will be added instead.
	 * 
	 * @param base
	 *            The resource to replace
	 * @param replacement
	 *            The replacement
	 */
	public final void addResourceReplacement(CssResourceReference base,
		ResourceReference replacement)
	{
		ReplacementResourceBundleReference bundle = new ReplacementResourceBundleReference(replacement);
		bundle.addProvidedResources(CssHeaderItem.forReference(base));
		getResourceBundles().addBundle(CssHeaderItem.forReference(bundle));
	}

	/**
	 * Create a new WebRequest. Subclasses of WebRequest could e.g. decode and obfuscate URL which
	 * has been encoded by an appropriate WebResponse.
	 * 
	 * @param servletRequest
	 *            the current HTTP Servlet request
	 * @param filterPath
	 *            the filter mapping read from web.xml
	 * @return a WebRequest object
	 */
	public WebRequest newWebRequest(HttpServletRequest servletRequest, final String filterPath)
	{
		return new ServletWebRequest(servletRequest, filterPath);
	}

	/**
	 * Pre- and post- configures the {@link WebRequest} created by user override-able
	 * {@link #newWebRequest(HttpServletRequest, String)}
	 * 
	 * @param servletRequest
	 *            the current HTTP Servlet request
	 * @param filterPath
	 *            the filter mapping read from web.xml
	 * @return a WebRequest object
	 */
	WebRequest createWebRequest(HttpServletRequest servletRequest, final String filterPath)
	{
		if (hasFilterFactoryManager())
		{
			for (AbstractRequestWrapperFactory factory : getFilterFactoryManager())
			{
				servletRequest = factory.getWrapper(servletRequest);
			}
		}

		WebRequest webRequest = newWebRequest(servletRequest, filterPath);

		if (servletRequest.getCharacterEncoding() == null)
		{
			try
			{
				if (webRequest.isAjax())
				{
					// WICKET-3908, WICKET-1816: Forms submitted with Ajax are always UTF-8 encoded
					servletRequest.setCharacterEncoding(CharEncoding.UTF_8);
				}
				else
				{
					String requestEncoding = getRequestCycleSettings().getResponseRequestEncoding();
					servletRequest.setCharacterEncoding(requestEncoding);
				}
			}
			catch (UnsupportedEncodingException e)
			{
				throw new WicketRuntimeException(e);
			}
		}

		return webRequest;
	}

	/**
	 * Creates a WebResponse. Subclasses of WebRequest could e.g. encode wicket's default URL and
	 * hide the details from the user. A appropriate WebRequest must be implemented and configured
	 * to decode the encoded URL.
	 * 
	 * @param webRequest
	 *            the {@link WebRequest} that will handle the current HTTP Servlet request
	 * @param httpServletResponse
	 *            the current HTTP Servlet response
	 * @return a WebResponse object
	 */
	protected WebResponse newWebResponse(final WebRequest webRequest,
		final HttpServletResponse httpServletResponse)
	{
		return new ServletWebResponse((ServletWebRequest)webRequest, httpServletResponse);
	}

	/**
	 * Pre- and post- configures the {@link WebResponse} returned from
	 * {@link #newWebResponse(WebRequest, HttpServletResponse)}
	 * 
	 * @param webRequest
	 *            the {@link WebRequest} that will handle the current HTTP Servlet request
	 * @param httpServletResponse
	 *            the current HTTP Servlet response
	 * @return the configured WebResponse object
	 */
	WebResponse createWebResponse(final WebRequest webRequest,
		final HttpServletResponse httpServletResponse)
	{
		WebResponse webResponse = newWebResponse(webRequest, httpServletResponse);

		boolean shouldBufferResponse = getRequestCycleSettings().getBufferResponse();
		return shouldBufferResponse ? new HeaderBufferingWebResponse(webResponse) : webResponse;
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

		IFileCleaner fileCleaner = getResourceSettings().getFileCleaner();
		if (fileCleaner != null)
		{
			fileCleaner.destroy();
		}

		super.internalDestroy();
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

		getResourceSettings().getResourceFinders().add(
			new WebApplicationPath(getServletContext(), ""));
		getResourceSettings().getResourceFinders().add(
			new ClassPathResourceFinder(META_INF_RESOURCES));

		// Set default error pages for HTML markup
		getApplicationSettings().setPageExpiredErrorPage(PageExpiredErrorPage.class);
		getApplicationSettings().setInternalErrorPage(InternalErrorPage.class);
		getApplicationSettings().setAccessDeniedPage(AccessDeniedPage.class);

		// Add resolver for automatically resolving HTML links
		getPageSettings().addComponentResolver(new AutoLinkResolver());
		getPageSettings().addComponentResolver(new AutoLabelResolver());
		getPageSettings().addComponentResolver(new AutoLabelTextResolver());

		getResourceSettings().setFileCleaner(new FileCleaner());

		if (getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT)
		{
			// Add optional sourceFolder for resources.
			String resourceFolder = getInitParameter("sourceFolder");
			if (resourceFolder != null)
			{
				getResourceSettings().getResourceFinders().add(new Path(resourceFolder));
			}
		}
		setPageRendererProvider(WebPageRenderer::new);
		setSessionStoreProvider(HttpSessionStore::new);
		setAjaxRequestTargetProvider(AjaxRequestHandler::new);

		getAjaxRequestTargetListeners().add(new AjaxEnclosureListener());
		
		getHeaderContributorListeners().add(head -> {
			getResourceSettings().getWicketCoreCSS().ifPresent(wicketCoreCSS -> {
				head.render(CssHeaderItem.forReference(wicketCoreCSS));
			});
		});

		// Configure the app.
		configure();
	}

	/**
	 * set runtime configuration type
	 * <p/>
	 * this is a write-once property: once configured it can not be changed later on.
	 * 
	 * @param configurationType
	 */
	public Application setConfigurationType(RuntimeConfigurationType configurationType)
	{
		if (this.configurationType != null)
		{
			throw new IllegalStateException(
				"Configuration type is write-once. You can not change it. " + "" +
					"Current value='" + configurationType + "'");
		}
		this.configurationType = Args.notNull(configurationType, "configurationType");
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RuntimeConfigurationType getConfigurationType()
	{
		if (configurationType == null)
		{
			String result = null;
			try
			{
				result = System.getProperty("wicket." + Application.CONFIGURATION);
			}
			catch (SecurityException e)
			{
				log.warn("SecurityManager doesn't allow to read the configuration type from " +
						"the system properties. The configuration type will be read from the web.xml.");
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
				try
				{
					configurationType = RuntimeConfigurationType.valueOf(result.toUpperCase(Locale.ROOT));
				}
				catch (IllegalArgumentException e)
				{
					// Ignore : fall back to DEVELOPMENT mode
					// log.warn("Unknown runtime configuration type '" + result +
					// "', falling back to DEVELOPMENT mode.");
					throw new IllegalArgumentException("Invalid configuration type: '" + result +
						"'.  Must be \"development\" or \"deployment\".");
				}
			}
		}

		if (configurationType == null)
		{
			configurationType = RuntimeConfigurationType.DEVELOPMENT;
		}

		return configurationType;
	}

	/**
	 * The rules if and when to insert an xml decl in the response are a bit tricky. Hence, we allow
	 * the user to replace the default implementation per page and per application.
	 * <p>
	 * Default implementation: the page mime type must be "application/xhtml+xml" and request
	 * HTTP_ACCEPT header must include "application/xhtml+xml" to automatically include the xml
	 * decl. Please see <a href=
	 * "https://developer.mozilla.org/en/Writing_JavaScript_for_XHTML#Finally:_Content_Negotiation"
	 * >Writing JavaScript for XHTML</a> for details.
	 * <p>
	 * Please note that xml decls in Wicket's markup are only used for reading the markup. The
	 * markup's xml decl will always be removed and never be used to configure the response.
	 * 
	 * @param page
	 *            The page currently being rendered
	 * @param insert
	 *            If false, than the rules are applied. If true, it'll always be written. In order
	 *            to never insert it, than subclass renderXmlDecl() with an empty implementation.
	 */
	public void renderXmlDecl(final WebPage page, boolean insert)
	{
		if (insert || MarkupType.XML_MIME.equalsIgnoreCase(page.getMarkupType().getMimeType()))
		{
			final RequestCycle cycle = RequestCycle.get();

			if (insert == false)
			{
				WebRequest request = (WebRequest)cycle.getRequest();

				String accept = request.getHeader("Accept");
				insert = ((accept == null) || (accept.indexOf(MarkupType.XML_MIME) != -1));
			}

			if (insert)
			{
				WebResponse response = (WebResponse)cycle.getResponse();
				response.write("<?xml version='1.0'");
				String encoding = getRequestCycleSettings().getResponseRequestEncoding();
				if (Strings.isEmpty(encoding) == false)
				{
					response.write(" encoding='");
					response.write(encoding);
					response.write("'");
				}
				response.write(" ?>");
			}
		}
	}

	/**
	 * Creates a new ajax request target used to control ajax responses
	 * 
	 * @param page
	 *            page on which ajax response is made
	 * @return non-null ajax request target instance
	 */
	public final AjaxRequestTarget newAjaxRequestTarget(final Page page)
	{
		AjaxRequestTarget target = getAjaxRequestTargetProvider().apply(page);
		for (AjaxRequestTarget.IListener listener : ajaxRequestTargetListeners)
		{
			target.addListener(listener);
		}
		return target;
	}

	/**
	 * Log that this application is started.
	 */
	final void logStarted()
	{
		if (log.isInfoEnabled())
		{
			String version = getFrameworkSettings().getVersion();
			StringBuilder b = new StringBuilder();
			b.append("[").append(getName()).append("] Started Wicket ");
			if (!"n/a".equals(version))
			{
				b.append("version ").append(version).append(" ");
			}
			b.append("in ").append(getConfigurationType()).append(" mode");
			log.info(b.toString());
		}

		if (usesDevelopmentConfig())
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

	/*
	 * Can contain at most 1000 responses and each entry can live at most one minute. For now there
	 * is no need to configure these parameters externally.
	 */
	private final StoredResponsesMap storedResponses = new StoredResponsesMap(1000,
		Duration.ofSeconds(60));

	/**
	 * 
	 * @param sessionId
	 * @param url
	 * @return true if has buffered response
	 */
	public boolean hasBufferedResponse(String sessionId, Url url)
	{
		String key = sessionId + url.toString();
		return storedResponses.containsKey(key);
	}

	/**
	 * Retrieves a stored buffered response for a given sessionId and url.
	 *
	 * @param url
	 *          The url used as a key
	 * @return the stored buffered response. {@code null} if there is no stored response for the given url
	 * @see org.apache.wicket.settings.RequestCycleSettings.RenderStrategy#REDIRECT_TO_BUFFER
	 */
	public BufferedWebResponse getAndRemoveBufferedResponse(String sessionId, Url url)
	{
		String key = sessionId + url.toString();
		return storedResponses.remove(key);
	}

	/**
	 * Store the buffered response at application level to use it at a later time.
	 *
	 * @param sessionId
	 * @param url
	 * @param response
	 */
	public void storeBufferedResponse(String sessionId, Url url, BufferedWebResponse response)
	{
		if (Strings.isEmpty(sessionId))
		{
			log.error("storeBufferedResponse needs a valid session id to store the response, but a null one was found. "
					+ "Please report the problem to dev team and try to reproduce it in a quickstart project.");
			return;
		}

		String key = sessionId + url.toString();
		storedResponses.put(key, response);
	}

	@Override
	public String getMimeType(String fileName)
	{
		String mimeType = getServletContext().getMimeType(fileName);
		return mimeType != null ? mimeType : super.getMimeType(fileName);
	}

	/**
	 * Returns the provider for {@link org.apache.wicket.ajax.AjaxRequestTarget} objects.
	 * 
	 * @return the provider for {@link org.apache.wicket.ajax.AjaxRequestTarget} objects.
	 */
	public Function<Page, AjaxRequestTarget> getAjaxRequestTargetProvider()
	{
		return ajaxRequestTargetProvider;
	}

	/**
	 * Sets the provider for {@link org.apache.wicket.ajax.AjaxRequestTarget} objects.
	 * 
	 * @param ajaxRequestTargetProvider
	 *            the new provider
	 */
	public Application setAjaxRequestTargetProvider(
		Function<Page, AjaxRequestTarget> ajaxRequestTargetProvider)
	{
		this.ajaxRequestTargetProvider = ajaxRequestTargetProvider;
		return this;
	}

	/**
	 * Returns the registered {@link org.apache.wicket.ajax.AjaxRequestTarget.IListener} objects.
	 * 
	 * @return the registered {@link org.apache.wicket.ajax.AjaxRequestTarget.IListener} objects.
	 */
	public AjaxRequestTargetListenerCollection getAjaxRequestTargetListeners()
	{
		return ajaxRequestTargetListeners;
	}

	/**
	 * @return True if at least one filter factory has been added.
	 */
	public final boolean hasFilterFactoryManager()
	{
		return filterFactoryManager != null;
	}

	/**
	 * @return The filter factory manager
	 */
	public final FilterFactoryManager getFilterFactoryManager()
	{
		if (filterFactoryManager == null)
		{
			filterFactoryManager = new FilterFactoryManager();
		}
		return filterFactoryManager;
	}

	/**
	 * If true, auto label css classes such as {@code error} and {@code required} will be updated
	 * after form component processing during an ajax request. This allows auto labels to correctly
	 * reflect the state of the form component even if they are not part of the ajax markup update.
	 * 
	 * TODO in wicket-7 this should move into a settings object. cannot move in 6.x because it
	 * requires a change to a setting interface.
	 * 
	 * @return {@code true} iff enabled
	 */
	public boolean getUpdateAutoLabelsOnAjaxRequests()
	{
		return true;
	}
}
