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
package org.apache.wicket.settings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.IResourceFactory;
import org.apache.wicket.IResponseFilter;
import org.apache.wicket.Localizer;
import org.apache.wicket.Page;
import org.apache.wicket.application.DefaultClassResolver;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.markup.IMarkupCache;
import org.apache.wicket.markup.IMarkupParserFactory;
import org.apache.wicket.markup.MarkupCache;
import org.apache.wicket.markup.MarkupParserFactory;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.PackageResourceGuard;
import org.apache.wicket.markup.html.form.persistence.CookieValuePersisterSettings;
import org.apache.wicket.markup.html.pages.BrowserInfoPage;
import org.apache.wicket.markup.resolver.AutoComponentResolver;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.resource.PropertiesFactory;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.session.DefaultPageFactory;
import org.apache.wicket.session.pagemap.IPageMapEvictionStrategy;
import org.apache.wicket.session.pagemap.LeastRecentlyAccessedEvictionStrategy;
import org.apache.wicket.util.crypt.CachingSunJceCryptFactory;
import org.apache.wicket.util.crypt.ICryptFactory;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.file.IResourcePath;
import org.apache.wicket.util.file.Path;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.util.resource.locator.ResourceStreamLocator;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.watch.ModificationWatcher;


/**
 * Contains settings exposed via IXXXSettings interfaces. It is not a good idea to use this class
 * directly, instead use the provided IXXXSettings interfaces.
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 */
public final class Settings
		implements
			IApplicationSettings,
			IDebugSettings,
			IExceptionSettings,
			IMarkupSettings,
			IPageSettings,
			IRequestCycleSettings,
			IResourceSettings,
			ISecuritySettings,
			ISessionSettings,
			IFrameworkSettings,
			IRequestLoggerSettings
{
	/** Class of access denied page. */
	private WeakReference/* <Class<? extends Page> */accessDeniedPage;

	/** ajax debug mode status */
	private boolean ajaxDebugModeEnabled = false;

	/** The application */
	private final Application application;

	/** The authorization strategy. */
	private IAuthorizationStrategy authorizationStrategy = IAuthorizationStrategy.ALLOW_ALL;

	/** Application default for automatically resolving hrefs */
	private boolean automaticLinking = false;

	/**
	 * Whether Wicket should try to support multiple windows transparently, true by default.
	 */
	private boolean automaticMultiWindowSupport = true;

	/** True if the response should be buffered */
	private boolean bufferResponse = true;

	/** class resolver to find classes */
	private IClassResolver classResolver = new DefaultClassResolver();

	/** List of (static) ComponentResolvers */
	private final List componentResolvers = new ArrayList();

	/** True to check that each component on a page is used */
	private boolean componentUseCheck = true;

	/** True if multiple tabs/spaces should be compressed to a single space */
	private boolean compressWhitespace = false;

	/** Default values for persistence of form data (by means of cookies) */
	private CookieValuePersisterSettings cookieValuePersisterSettings = new CookieValuePersisterSettings();

	/** facotry for creating crypt objects */
	private ICryptFactory cryptFactory;

	/** Default markup for after a disabled link */
	private String defaultAfterDisabledLink = "</em>";

	/** Default markup for before a disabled link */
	private String defaultBeforeDisabledLink = "<em>";

	/** Default markup encoding. If null, the OS default will be used */
	private String defaultMarkupEncoding;

	/**
	 * Whether we should disable gzip compression for resources.
	 */
	private boolean disableGZipCompression = false;

	/**
	 * Whether mounts should be enforced. If true, requests for mounted targets have to done through
	 * the mounted paths. If, for instance, a bookmarkable page is mounted to a path, a request to
	 * that same page via the bookmarkablePage parameter will be denied.
	 */
	private boolean enforceMounts = false;

	/**
	 * Whether Wicket should try to get extensive client info by redirecting to
	 * {@link BrowserInfoPage a page that polls for client capabilities}. This method is used by
	 * the default implementation of {@link #newClientInfo()}, so if that method is overriden,
	 * there is no guarantee this method will be taken into account. False by default.
	 */
	private boolean gatherExtendedBrowserInfo = false;

	/** Class of internal error page. */
	private WeakReference/* <Class<? extends Page> */internalErrorPage;

	/**
	 * whether wicket should track line precise additions of components for error reporting.
	 */
	private boolean linePreciseReportingOnAddComponentEnabled = false;

	/**
	 * whether wicket should track line precise instantiations of components for error reporting.
	 */
	private boolean linePreciseReportingOnNewComponentEnabled = false;

	/** I18N support */
	private Localizer localizer;

	/** Factory for creating markup parsers */
	private IMarkupParserFactory markupParserFactory;

	/** A markup cache which will load the markup if required. */
	private IMarkupCache markupCache;

	/** To help prevent denial of service attacks */
	private int maxPageMaps = 5;

	/** Map to look up resource factories by name */
	private final Map nameToResourceFactory = new HashMap();

	/** True if string resource loaders have been overridden */
	private boolean overriddenStringResourceLoaders = false;

	/** The package resource guard. */
	private IPackageResourceGuard packageResourceGuard = new PackageResourceGuard();

	/** The error page displayed when an expired page is accessed. */
	private WeakReference/* <Class<? extends Page> */pageExpiredErrorPage;

	/** factory to create new Page objects */
	private IPageFactory pageFactory = new DefaultPageFactory();

	/** The eviction strategy. */
	private IPageMapEvictionStrategy pageMapEvictionStrategy = new LeastRecentlyAccessedEvictionStrategy(
			5);

	/** The factory to be used for the property files */
	private org.apache.wicket.resource.IPropertiesFactory propertiesFactory;

	/**
	 * The render strategy, defaults to 'REDIRECT_TO_BUFFER'. This property influences the default
	 * way in how a logical request that consists of an 'action' and a 'render' part is handled, and
	 * is mainly used to have a means to circumvent the 'refresh' problem.
	 */
	private IRequestCycleSettings.RenderStrategy renderStrategy = REDIRECT_TO_BUFFER;

	/** Filesystem Path to search for resources */
	private IResourceFinder resourceFinder = new Path();

	/** Frequency at which files should be polled */
	private Duration resourcePollFrequency = null;

	/** resource locator for this application */
	private IResourceStreamLocator resourceStreamLocator;

	/** ModificationWatcher to watch for changes in markup files */
	private ModificationWatcher resourceWatcher;

	/** List of {@link IResponseFilter}s. */
	private List responseFilters;

	/**
	 * In order to do proper form parameter decoding it is important that the response and the
	 * following request have the same encoding. see
	 * http://www.crazysquirrel.com/computing/general/form-encoding.jspx for additional information.
	 */
	private String responseRequestEncoding = "UTF-8";

	/** Chain of string resource loaders to use */
	private final List stringResourceLoaders = new ArrayList(4);

	/** Should HTML comments be stripped during rendering? */
	private boolean stripComments = false;

	/**
	 * If true, wicket tags ( <wicket: ..>) and wicket:id attributes we be removed from output
	 */
	private boolean stripWicketTags = false;

	/** In order to remove <?xml?> from output as required by IE quirks mode */
	private boolean stripXmlDeclarationFromOutput;

	/** Flags used to determine how to behave if resources are not found */
	private boolean throwExceptionOnMissingResource = true;

	/**
	 * Whether the generated page id must be unique per session, or it's enough if it is unique per
	 * page map;
	 */
	private boolean pageIdUniquePerSession = false;

	/**
	 * The time that a request will by default be waiting for the previous request to be handled
	 * before giving up. Defaults to one minute.
	 */
	private Duration timeout = Duration.ONE_MINUTE;

	/** Authorizer for component instantiations */
	private IUnauthorizedComponentInstantiationListener unauthorizedComponentInstantiationListener = new IUnauthorizedComponentInstantiationListener()
	{
		/**
		 * Called when an unauthorized component instantiation is about to take place (but before it
		 * happens).
		 * 
		 * @param component
		 *            The partially constructed component (only the id is guaranteed to be valid).
		 */
		public void onUnauthorizedInstantiation(final Component component)
		{
			throw new UnauthorizedInstantiationException(component.getClass());
		}
	};

	/** Type of handling for unexpected exceptions */
	private UnexpectedExceptionDisplay unexpectedExceptionDisplay = SHOW_EXCEPTION_PAGE;

	/** Determines behavior of string resource loading if string is missing */
	private boolean useDefaultOnMissingResource = true;

	/** Determines if pages should be managed by a version manager by default */
	private boolean versionPagesByDefault = true;

	private boolean recordSessionSize = true;

	private int requestsWindowSize = 0;

	private boolean requestLoggerEnabled;

	/**
	 * Whether the comments and whitespaces will be stripped from javascript resources
	 */
	private boolean stripJavascriptCommentsAndWhitespace;

	/**
	 * Whether the container's class name should be printed to response (in a html comment).
	 */
	private boolean outputMarkupContainerClassName = false;

	/**
	 * Create the application settings, carrying out any necessary initialisations.
	 * 
	 * @param application
	 *            The application that these settings are for
	 */
	public Settings(final Application application)
	{
		this.application = application;
		stringResourceLoaders.add(new ComponentStringResourceLoader());
		stringResourceLoaders.add(new ClassStringResourceLoader(this.application.getClass()));
	}

	/**
	 * @see org.apache.wicket.settings.IPageSettings#addComponentResolver(org.apache.wicket.markup.resolver.IComponentResolver)
	 */
	public void addComponentResolver(IComponentResolver resolver)
	{
		componentResolvers.add(resolver);
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#addResourceFactory(java.lang.String,
	 *      org.apache.wicket.IResourceFactory)
	 */
	public void addResourceFactory(final String name, IResourceFactory resourceFactory)
	{
		nameToResourceFactory.put(name, resourceFactory);
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#addResourceFolder(java.lang.String)
	 */
	public void addResourceFolder(final String resourceFolder)
	{
		// Get resource finder
		final IResourceFinder finder = getResourceFinder();

		// Make sure it's a path
		if (!(finder instanceof IResourcePath))
		{
			throw new IllegalArgumentException(
					"To add a resource folder, the application's resource finder must be an instance of IResourcePath");
		}

		// Cast to resource path and add folder
		final IResourcePath path = (IResourcePath)finder;
		path.add(resourceFolder);
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#addResponseFilter(org.apache.wicket.IResponseFilter)
	 */
	public void addResponseFilter(IResponseFilter responseFilter)
	{
		if (responseFilters == null)
		{
			responseFilters = new ArrayList(3);
		}
		responseFilters.add(responseFilter);
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#addStringResourceLoader(org.apache.wicket.resource.loader.IStringResourceLoader)
	 */
	public void addStringResourceLoader(final IStringResourceLoader loader)
	{
		if (!overriddenStringResourceLoaders)
		{
			stringResourceLoaders.clear();
			overriddenStringResourceLoaders = true;
		}
		stringResourceLoaders.add(loader);
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#getAccessDeniedPage()
	 */
	public Class getAccessDeniedPage()
	{
		return (Class)accessDeniedPage.get();
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#getAuthorizationStrategy()
	 */
	public IAuthorizationStrategy getAuthorizationStrategy()
	{
		return authorizationStrategy;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getAutomaticLinking()
	 */
	public boolean getAutomaticLinking()
	{
		return automaticLinking;
	}

	/**
	 * @see org.apache.wicket.settings.IPageSettings#getAutomaticMultiWindowSupport()
	 */
	public boolean getAutomaticMultiWindowSupport()
	{
		return automaticMultiWindowSupport;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getBufferResponse()
	 */
	public boolean getBufferResponse()
	{
		return bufferResponse;
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#getClassResolver()
	 */
	public IClassResolver getClassResolver()
	{
		return classResolver;
	}

	/**
	 * Get the (modifiable) list of IComponentResolvers.
	 * 
	 * @see AutoComponentResolver for an example
	 * @return List of ComponentResolvers
	 */
	public List getComponentResolvers()
	{
		return componentResolvers;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#getComponentUseCheck()
	 */
	public boolean getComponentUseCheck()
	{
		return componentUseCheck;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getCompressWhitespace()
	 */
	public boolean getCompressWhitespace()
	{
		return compressWhitespace;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#getCookieValuePersisterSettings()
	 */
	public CookieValuePersisterSettings getCookieValuePersisterSettings()
	{
		return cookieValuePersisterSettings;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#getCryptFactory()
	 */
	public synchronized ICryptFactory getCryptFactory()
	{
		if (cryptFactory == null)
		{
			cryptFactory = new CachingSunJceCryptFactory(ISecuritySettings.DEFAULT_ENCRYPTION_KEY);
		}
		return cryptFactory;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getDefaultAfterDisabledLink()
	 */
	public String getDefaultAfterDisabledLink()
	{
		return defaultAfterDisabledLink;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getDefaultBeforeDisabledLink()
	 */
	public String getDefaultBeforeDisabledLink()
	{
		return defaultBeforeDisabledLink;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getDefaultMarkupEncoding()
	 */
	public String getDefaultMarkupEncoding()
	{
		return defaultMarkupEncoding;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getDisableGZipCompression()
	 */
	public boolean getDisableGZipCompression()
	{
		return disableGZipCompression;
	}

	/**
	 * @see wicket.settings.ISecuritySettings#getEnforceMounts()
	 */
	public boolean getEnforceMounts()
	{
		return enforceMounts;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getGatherExtendedBrowserInfo()
	 */
	public boolean getGatherExtendedBrowserInfo()
	{
		return gatherExtendedBrowserInfo;
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#getInternalErrorPage()
	 */
	public Class getInternalErrorPage()
	{
		return (Class)internalErrorPage.get();
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getLocalizer()
	 */
	public Localizer getLocalizer()
	{
		if (localizer == null)
		{
			localizer = new Localizer();
		}
		return localizer;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setLocalizer(org.apache.wicket.Localizer)
	 */
	public void setLocalizer(final Localizer localizer)
	{
		this.localizer = localizer;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getMarkupParserFactory()
	 */
	public IMarkupParserFactory getMarkupParserFactory()
	{
		if (markupParserFactory == null)
		{
			markupParserFactory = new MarkupParserFactory();
		}
		return markupParserFactory;
	}

	/**
	 * @see org.apache.wicket.settings.ISessionSettings#getMaxPageMaps()
	 */
	public final int getMaxPageMaps()
	{
		return maxPageMaps;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getPackageResourceGuard()
	 */
	public IPackageResourceGuard getPackageResourceGuard()
	{
		return packageResourceGuard;
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#getPageExpiredErrorPage()
	 */
	public Class getPageExpiredErrorPage()
	{
		return (Class)pageExpiredErrorPage.get();
	}

	/**
	 * @see org.apache.wicket.settings.ISessionSettings#getPageFactory()
	 */
	public IPageFactory getPageFactory()
	{
		return pageFactory;
	}

	/**
	 * @see org.apache.wicket.settings.ISessionSettings#getPageMapEvictionStrategy()
	 */
	public IPageMapEvictionStrategy getPageMapEvictionStrategy()
	{
		return pageMapEvictionStrategy;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getPropertiesFactory()
	 */
	public org.apache.wicket.resource.IPropertiesFactory getPropertiesFactory()
	{
		if (propertiesFactory == null)
		{
			propertiesFactory = new PropertiesFactory(Application.get());
		}
		return propertiesFactory;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getRenderStrategy()
	 */
	public IRequestCycleSettings.RenderStrategy getRenderStrategy()
	{
		return renderStrategy;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getResourceFactory(java.lang.String)
	 */
	public IResourceFactory getResourceFactory(final String name)
	{
		return (IResourceFactory)nameToResourceFactory.get(name);
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getResourceFinder()
	 */
	public IResourceFinder getResourceFinder()
	{
		return resourceFinder;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getResourcePollFrequency()
	 */
	public Duration getResourcePollFrequency()
	{
		return resourcePollFrequency;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getResourceStreamLocator()
	 */
	public IResourceStreamLocator getResourceStreamLocator()
	{
		if (resourceStreamLocator == null)
		{
			// Create compound resource locator using source path from
			// application settings
			resourceStreamLocator = new ResourceStreamLocator(getResourceFinder());
		}
		return resourceStreamLocator;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getResourceWatcher(boolean)
	 */
	public ModificationWatcher getResourceWatcher(boolean start)
	{
		if (resourceWatcher == null && start)
		{
			final Duration pollFrequency = getResourcePollFrequency();
			if (pollFrequency != null)
			{
				resourceWatcher = new ModificationWatcher(pollFrequency);
			}
		}
		return resourceWatcher;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getResponseFilters()
	 */
	public List getResponseFilters()
	{
		if (responseFilters == null)
		{
			return null;
		}
		else
		{
			return Collections.unmodifiableList(responseFilters);
		}
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getResponseRequestEncoding()
	 */
	public String getResponseRequestEncoding()
	{
		return responseRequestEncoding;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getStringResourceLoaders()
	 */
	public List getStringResourceLoaders()
	{
		return Collections.unmodifiableList(stringResourceLoaders);
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getStripComments()
	 */
	public boolean getStripComments()
	{
		return stripComments;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getStripWicketTags()
	 */
	public boolean getStripWicketTags()
	{
		return stripWicketTags;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getStripXmlDeclarationFromOutput()
	 */
	public boolean getStripXmlDeclarationFromOutput()
	{
		return stripXmlDeclarationFromOutput;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getThrowExceptionOnMissingResource()
	 */
	public boolean getThrowExceptionOnMissingResource()
	{
		return throwExceptionOnMissingResource;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getTimeout()
	 */
	public Duration getTimeout()
	{
		return timeout;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#getUnauthorizedComponentInstantiationListener()
	 */
	public IUnauthorizedComponentInstantiationListener getUnauthorizedComponentInstantiationListener()
	{
		return unauthorizedComponentInstantiationListener;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getUnexpectedExceptionDisplay()
	 */
	public UnexpectedExceptionDisplay getUnexpectedExceptionDisplay()
	{
		return unexpectedExceptionDisplay;
	}


	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getUseDefaultOnMissingResource()
	 */
	public boolean getUseDefaultOnMissingResource()
	{
		return useDefaultOnMissingResource;
	}

	/**
	 * @see org.apache.wicket.settings.IFrameworkSettings#getVersion()
	 */
	public String getVersion()
	{
		String implVersion = null;
		Package pkg = getClass().getPackage();
		if (pkg != null)
		{
			implVersion = pkg.getImplementationVersion();
		}
		return Strings.isEmpty(implVersion) ? "n/a" : implVersion;
	}

	/**
	 * @see org.apache.wicket.settings.IPageSettings#getVersionPagesByDefault()
	 */
	public boolean getVersionPagesByDefault()
	{
		return versionPagesByDefault;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#isAjaxDebugModeEnabled()
	 */
	public boolean isAjaxDebugModeEnabled()
	{
		return ajaxDebugModeEnabled;
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#setAccessDeniedPage(java.lang.Class)
	 */
	public void setAccessDeniedPage(Class accessDeniedPage)
	{
		if (accessDeniedPage == null)
		{
			throw new IllegalArgumentException("Argument accessDeniedPage may not be null");
		}
		checkPageClass(accessDeniedPage);

		this.accessDeniedPage = new WeakReference(accessDeniedPage);
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#setAjaxDebugModeEnabled(boolean)
	 */
	public void setAjaxDebugModeEnabled(boolean enable)
	{
		ajaxDebugModeEnabled = enable;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#setAuthorizationStrategy(org.apache.wicket.authorization.IAuthorizationStrategy)
	 */
	public void setAuthorizationStrategy(IAuthorizationStrategy strategy)
	{
		if (strategy == null)
		{
			throw new IllegalArgumentException("authorization strategy cannot be set to null");
		}
		authorizationStrategy = strategy;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setAutomaticLinking(boolean)
	 */
	public void setAutomaticLinking(boolean automaticLinking)
	{
		this.automaticLinking = automaticLinking;
	}

	/**
	 * @see org.apache.wicket.settings.IPageSettings#setAutomaticMultiWindowSupport(boolean)
	 */
	public void setAutomaticMultiWindowSupport(boolean automaticMultiWindowSupport)
	{
		this.automaticMultiWindowSupport = automaticMultiWindowSupport;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setBufferResponse(boolean)
	 */
	public void setBufferResponse(boolean bufferResponse)
	{
		this.bufferResponse = bufferResponse;
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#setClassResolver(org.apache.wicket.application.IClassResolver)
	 */
	public void setClassResolver(final IClassResolver defaultClassResolver)
	{
		classResolver = defaultClassResolver;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#setComponentUseCheck(boolean)
	 */
	public void setComponentUseCheck(final boolean componentUseCheck)
	{
		this.componentUseCheck = componentUseCheck;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setCompressWhitespace(boolean)
	 */
	public void setCompressWhitespace(final boolean compressWhitespace)
	{
		this.compressWhitespace = compressWhitespace;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#setCookieValuePersisterSettings(org.apache.wicket.markup.html.form.persistence.CookieValuePersisterSettings)
	 */
	public void setCookieValuePersisterSettings(
			CookieValuePersisterSettings cookieValuePersisterSettings)
	{
		this.cookieValuePersisterSettings = cookieValuePersisterSettings;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#setCryptFactory(org.apache.wicket.util.crypt.ICryptFactory)
	 */
	public void setCryptFactory(ICryptFactory cryptFactory)
	{
		if (cryptFactory == null)
		{
			throw new IllegalArgumentException("cryptFactory cannot be null");
		}
		this.cryptFactory = cryptFactory;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setDefaultAfterDisabledLink(java.lang.String)
	 */
	public void setDefaultAfterDisabledLink(final String defaultAfterDisabledLink)
	{
		this.defaultAfterDisabledLink = defaultAfterDisabledLink;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setDefaultBeforeDisabledLink(java.lang.String)
	 */
	public void setDefaultBeforeDisabledLink(String defaultBeforeDisabledLink)
	{
		this.defaultBeforeDisabledLink = defaultBeforeDisabledLink;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setDefaultMarkupEncoding(java.lang.String)
	 */
	public void setDefaultMarkupEncoding(final String encoding)
	{
		defaultMarkupEncoding = encoding;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setDisableGZipCompression(boolean)
	 */
	public void setDisableGZipCompression(boolean disableGZipCompression)
	{
		this.disableGZipCompression = disableGZipCompression;
	}

	/**
	 * @see wicket.settings.ISecuritySettings#setEnforceMounts(boolean)
	 */
	public void setEnforceMounts(boolean enforce)
	{
		enforceMounts = enforce;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setGatherExtendedBrowserInfo(boolean)
	 */
	public void setGatherExtendedBrowserInfo(boolean gatherExtendedBrowserInfo)
	{
		this.gatherExtendedBrowserInfo = gatherExtendedBrowserInfo;
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#setInternalErrorPage(java.lang.Class)
	 */
	public void setInternalErrorPage(final Class internalErrorPage)
	{
		if (internalErrorPage == null)
		{
			throw new IllegalArgumentException("Argument internalErrorPage may not be null");
		}
		checkPageClass(internalErrorPage);

		this.internalErrorPage = new WeakReference(internalErrorPage);
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setMarkupParserFactory(org.apache.wicket.markup.IMarkupParserFactory)
	 */
	public void setMarkupParserFactory(IMarkupParserFactory factory)
	{
		if (factory == null)
		{
			throw new IllegalArgumentException("markup parser factory cannot be null");
		}

		markupParserFactory = factory;
	}

	/**
	 * @see org.apache.wicket.settings.ISessionSettings#setMaxPageMaps(int)
	 */
	public final void setMaxPageMaps(int maxPageMaps)
	{
		this.maxPageMaps = maxPageMaps;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setPackageResourceGuard(org.apache.wicket.markup.html.IPackageResourceGuard)
	 */
	public void setPackageResourceGuard(IPackageResourceGuard packageResourceGuard)
	{
		if (packageResourceGuard == null)
		{
			throw new IllegalArgumentException("Argument packageResourceGuard may not be null");
		}
		this.packageResourceGuard = packageResourceGuard;
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#setPageExpiredErrorPage(java.lang.Class)
	 */
	public void setPageExpiredErrorPage(final Class pageExpiredErrorPage)
	{
		if (pageExpiredErrorPage == null)
		{
			throw new IllegalArgumentException("Argument pageExpiredErrorPage may not be null");
		}
		checkPageClass(pageExpiredErrorPage);

		this.pageExpiredErrorPage = new WeakReference(pageExpiredErrorPage);
	}

	/**
	 * @see org.apache.wicket.settings.ISessionSettings#setPageFactory(org.apache.wicket.IPageFactory)
	 */
	public void setPageFactory(final IPageFactory defaultPageFactory)
	{
		pageFactory = defaultPageFactory;
	}

	/**
	 * @see org.apache.wicket.settings.ISessionSettings#setPageMapEvictionStrategy(org.apache.wicket.session.pagemap.IPageMapEvictionStrategy)
	 */
	public void setPageMapEvictionStrategy(IPageMapEvictionStrategy pageMapEvictionStrategy)
	{
		this.pageMapEvictionStrategy = pageMapEvictionStrategy;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setPropertiesFactory(org.apache.wicket.resource.PropertiesFactory)
	 */
	public void setPropertiesFactory(org.apache.wicket.resource.IPropertiesFactory factory)
	{
		propertiesFactory = factory;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setRenderStrategy(org.apache.wicket.settings.Settings.RenderStrategy)
	 */
	public void setRenderStrategy(IRequestCycleSettings.RenderStrategy renderStrategy)
	{
		this.renderStrategy = renderStrategy;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setResourceFinder(org.apache.wicket.util.file.IResourceFinder)
	 */
	public void setResourceFinder(final IResourceFinder resourceFinder)
	{
		this.resourceFinder = resourceFinder;

		// Cause resource locator to get recreated
		resourceStreamLocator = null;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setResourcePollFrequency(org.apache.wicket.util.time.Duration)
	 */
	public void setResourcePollFrequency(final Duration resourcePollFrequency)
	{
		this.resourcePollFrequency = resourcePollFrequency;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setResourceStreamLocator(org.apache.wicket.util.resource.locator.IResourceStreamLocator)
	 */
	public void setResourceStreamLocator(IResourceStreamLocator resourceStreamLocator)
	{
		this.resourceStreamLocator = resourceStreamLocator;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setResponseRequestEncoding(java.lang.String)
	 */
	public void setResponseRequestEncoding(final String responseRequestEncoding)
	{
		this.responseRequestEncoding = responseRequestEncoding;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setStripComments(boolean)
	 */
	public void setStripComments(boolean stripComments)
	{
		this.stripComments = stripComments;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setStripWicketTags(boolean)
	 */
	public void setStripWicketTags(boolean stripWicketTags)
	{
		this.stripWicketTags = stripWicketTags;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setStripXmlDeclarationFromOutput(boolean)
	 */
	public void setStripXmlDeclarationFromOutput(final boolean strip)
	{
		stripXmlDeclarationFromOutput = strip;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setThrowExceptionOnMissingResource(boolean)
	 */
	public void setThrowExceptionOnMissingResource(final boolean throwExceptionOnMissingResource)
	{
		this.throwExceptionOnMissingResource = throwExceptionOnMissingResource;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setTimeout(org.apache.wicket.util.time.Duration)
	 */
	public void setTimeout(Duration timeout)
	{
		if (timeout == null)
		{
			throw new IllegalArgumentException("timeout cannot be null");
		}
		this.timeout = timeout;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#setUnauthorizedComponentInstantiationListener(org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener)
	 */
	public void setUnauthorizedComponentInstantiationListener(
			IUnauthorizedComponentInstantiationListener unauthorizedComponentInstantiationListener)
	{
		this.unauthorizedComponentInstantiationListener = unauthorizedComponentInstantiationListener;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setUnexpectedExceptionDisplay(org.apache.wicket.settings.Settings.UnexpectedExceptionDisplay)
	 */
	public void setUnexpectedExceptionDisplay(
			final UnexpectedExceptionDisplay unexpectedExceptionDisplay)
	{
		this.unexpectedExceptionDisplay = unexpectedExceptionDisplay;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setUseDefaultOnMissingResource(boolean)
	 */
	public void setUseDefaultOnMissingResource(final boolean useDefaultOnMissingResource)
	{
		this.useDefaultOnMissingResource = useDefaultOnMissingResource;
	}

	/**
	 * @see org.apache.wicket.settings.IPageSettings#setVersionPagesByDefault(boolean)
	 */
	public void setVersionPagesByDefault(boolean pagesVersionedByDefault)
	{
		versionPagesByDefault = pagesVersionedByDefault;
	}

	/**
	 * Throws an IllegalArgumentException if the given class is not a subclass of Page.
	 * 
	 * @param pageClass
	 *            the page class to check
	 */
	private void checkPageClass(final Class pageClass)
	{
		// NOTE: we can't really check on whether it is a bookmarkable page
		// here, as - though the default is that a bookmarkable page must
		// either have a default constructor and/or a constructor with a
		// PageParameters object, this could be different for another
		// IPageFactory implementation
		if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("argument " + pageClass +
					" must be a subclass of Page");
		}
	}

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#getRecordSessionSize()
	 */
	public boolean getRecordSessionSize()
	{
		return recordSessionSize;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#getRequestsWindowSize()
	 */
	public int getRequestsWindowSize()
	{
		return requestsWindowSize;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#isRequestLoggerEnabled()
	 */
	public boolean isRequestLoggerEnabled()
	{
		return requestLoggerEnabled;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#setRecordSessionSize(boolean)
	 */
	public void setRecordSessionSize(boolean record)
	{
		recordSessionSize = record;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#setRequestLoggerEnabled(boolean)
	 */
	public void setRequestLoggerEnabled(boolean enable)
	{
		requestLoggerEnabled = enable;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#setRequestsWindowSize(int)
	 */
	public void setRequestsWindowSize(int size)
	{
		requestsWindowSize = size;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getStripJavascriptCommentsAndWhitespace()
	 */
	public boolean getStripJavascriptCommentsAndWhitespace()
	{
		return stripJavascriptCommentsAndWhitespace;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setStripJavascriptCommentsAndWhitespace(boolean)
	 */
	public void setStripJavascriptCommentsAndWhitespace(boolean value)
	{
		stripJavascriptCommentsAndWhitespace = value;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#setOutputMarkupContainerClassName(boolean)
	 */
	public void setOutputMarkupContainerClassName(boolean enable)
	{
		outputMarkupContainerClassName = enable;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#isOutputMarkupContainerClassName()
	 */
	public boolean isOutputMarkupContainerClassName()
	{
		return outputMarkupContainerClassName;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getMarkupCache()
	 */
	public IMarkupCache getMarkupCache()
	{
		if (markupCache == null)
		{
			// Construct markup cache for this application
			markupCache = new MarkupCache(application);
		}

		return markupCache;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setMarkupCache(org.apache.wicket.markup.MarkupCache)
	 */
	public void setMarkupCache(final IMarkupCache markupCache)
	{
		this.markupCache = markupCache;
	}

	private Bytes defaultMaximumUploadSize = Bytes.MAX;

	public Bytes getDefaultMaximumUploadSize()
	{
		return defaultMaximumUploadSize;
	}

	public void setDefaultMaximumUploadSize(Bytes defaultMaximumUploadSize)
	{
		this.defaultMaximumUploadSize = defaultMaximumUploadSize;
	}

	public void setPageIdUniquePerSession(boolean value)
	{
		pageIdUniquePerSession = value;
	}

	public boolean isPageIdUniquePerSession()
	{
		return pageIdUniquePerSession;
	}

	public boolean isLinePreciseReportingOnAddComponentEnabled()
	{
		return linePreciseReportingOnAddComponentEnabled;
	}

	public boolean isLinePreciseReportingOnNewComponentEnabled()
	{
		return linePreciseReportingOnNewComponentEnabled;
	}

	public void setLinePreciseReportingOnAddComponentEnabled(boolean enable)
	{
		linePreciseReportingOnAddComponentEnabled = enable;
	}

	public void setLinePreciseReportingOnNewComponentEnabled(boolean enable)
	{
		linePreciseReportingOnNewComponentEnabled = enable;
	}
}
