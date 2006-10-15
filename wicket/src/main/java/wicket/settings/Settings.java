/*
 * $Id: Settings.java 4858 2006-03-12 00:26:31 -0800 (Sun, 12 Mar 2006)
 * ivaynberg $ $Revision$ $Date: 2006-03-12 00:26:31 -0800 (Sun, 12 Mar
 * 2006) $
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
package wicket.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.IPageFactory;
import wicket.IResourceFactory;
import wicket.IResponseFilter;
import wicket.Localizer;
import wicket.Page;
import wicket.RequestCycle;
import wicket.application.DefaultClassResolver;
import wicket.application.IClassResolver;
import wicket.authorization.IAuthorizationStrategy;
import wicket.authorization.IUnauthorizedComponentInstantiationListener;
import wicket.authorization.UnauthorizedInstantiationException;
import wicket.markup.IMarkupParserFactory;
import wicket.markup.MarkupParserFactory;
import wicket.markup.html.IPackageResourceGuard;
import wicket.markup.html.PackageResourceGuard;
import wicket.markup.html.form.persistence.CookieValuePersisterSettings;
import wicket.markup.html.pages.BrowserInfoPage;
import wicket.markup.resolver.AutoComponentResolver;
import wicket.markup.resolver.IComponentResolver;
import wicket.protocol.http.WebRequest;
import wicket.resource.PropertiesFactory;
import wicket.resource.loader.ClassStringResourceLoader;
import wicket.resource.loader.ComponentStringResourceLoader;
import wicket.resource.loader.IStringResourceLoader;
import wicket.session.DefaultPageFactory;
import wicket.session.pagemap.IPageMapEvictionStrategy;
import wicket.session.pagemap.LeastRecentlyAccessedEvictionStrategy;
import wicket.util.convert.CoverterLocatorFactory;
import wicket.util.convert.IConverterLocatorFactory;
import wicket.util.crypt.CachingSunJceCryptFactory;
import wicket.util.crypt.ICryptFactory;
import wicket.util.file.IResourceFinder;
import wicket.util.file.IResourcePath;
import wicket.util.file.Path;
import wicket.util.resource.locator.CompoundResourceStreamLocator;
import wicket.util.resource.locator.IResourceStreamLocator;
import wicket.util.string.Strings;
import wicket.util.time.Duration;
import wicket.util.watch.ModificationWatcher;

/**
 * Contains settings exposed via IXXXSettings interfaces. It is not a good idea
 * to use this class directly, instead use the provided IXXXSettings interfaces.
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
			IMarkupSettings,
			IPageSettings,
			IRequestCycleSettings,
			IResourceSettings,
			ISecuritySettings,
			ISessionSettings,
			IFrameworkSettings
{
	private final static Log log = LogFactory.getLog(Settings.class);

	/** Class of access denied page. */
	private Class<? extends Page> accessDeniedPage;

	/** ajax debug mode status */
	private boolean ajaxDebugModeEnabled = false;

	/** The application */
	private Application application;

	/** The authorization strategy. */
	private IAuthorizationStrategy authorizationStrategy = IAuthorizationStrategy.ALLOW_ALL;

	/** Application default for automatically resolving hrefs */
	private boolean automaticLinking = false;

	/**
	 * Whether Wicket should try to support multiple windows transparently, true
	 * by default.
	 */
	private boolean automaticMultiWindowSupport = true;

	/** True if the response should be buffered */
	private boolean bufferResponse = true;

	/** class resolver to find classes */
	private IClassResolver classResolver = new DefaultClassResolver();

	/** List of (static) ComponentResolvers */
	private List<IComponentResolver> componentResolvers = new ArrayList<IComponentResolver>();

	/** True to check that each component on a page is used */
	private boolean componentUseCheck = true;

	/** True if multiple tabs/spaces should be compressed to a single space */
	private boolean compressWhitespace = false;

	/** The context path that should be used for url prefixing */
	private String contextPath;

	private IConverterLocatorFactory converterFactory;

	/** Default values for persistence of form data (by means of cookies) */
	private CookieValuePersisterSettings cookieValuePersisterSettings = new CookieValuePersisterSettings();

	/** facotry for creating crypt objects */
	private ICryptFactory cryptFactory;

	/** Default markup for after a disabled link */
	private String defaultAfterDisabledLink = "</em>";

	/** Default markup for before a disabled link */
	private String defaultBeforeDisabledLink = "<em>";

	/** The default locale to use */
	private Locale defaultLocale = Locale.getDefault();

	/** Default markup encoding. If null, the OS default will be used */
	private String defaultMarkupEncoding;

	/**
	 * Whether mounts should be enforced. If true, requests for mounted targets
	 * have to done through the mounted paths. If, for instance, a bookmarkable
	 * page is mounted to a path, a request to that same page via the
	 * bookmarkablePage parameter will be denied.
	 */
	private boolean enforceMounts = false;

	/**
	 * Whether Wicket should try to get extensive client info by redirecting to
	 * {@link BrowserInfoPage a page that polls for client capabilities}. This
	 * method is used by the default implementation of {@link #newClientInfo()},
	 * so if that method is overriden, there is no guarantee this method will be
	 * taken into account. False by default.
	 */
	private boolean gatherExtendedBrowserInfo = false;

	/** Class of internal error page. */
	private Class<? extends Page> internalErrorPage;

	/** I18N support */
	private Localizer localizer;

	/** Factory for creating markup parsers */
	private IMarkupParserFactory markupParserFactory;

	/** To help prevent denial of service attacks */
	private int maxPageMaps = 5;

	/** The maximum number of versions of a page to track */
	private int maxPageVersions = Integer.MAX_VALUE;

	/** Map to look up resource factories by name */
	private Map<String, IResourceFactory> nameToResourceFactory = new HashMap<String, IResourceFactory>();

	/** True if string resource loaders have been overridden */
	private boolean overriddenStringResourceLoaders = false;

	/** The package resource guard. */
	private IPackageResourceGuard packageResourceGuard = new PackageResourceGuard();

	/** The error page displayed when an expired page is accessed. */
	private Class<? extends Page> pageExpiredErrorPage;

	/** factory to create new Page objects */
	private IPageFactory pageFactory = new DefaultPageFactory();

	/** The eviction strategy. */
	private IPageMapEvictionStrategy pageMapEvictionStrategy = new LeastRecentlyAccessedEvictionStrategy(
			5);

	/** The factory to be used for the property files */
	private wicket.resource.IPropertiesFactory propertiesFactory;

	/**
	 * The render strategy, defaults to 'REDIRECT_TO_BUFFER'. This property
	 * influences the default way in how a logical request that consists of an
	 * 'action' and a 'render' part is handled, and is mainly used to have a
	 * means to circumvent the 'refresh' problem.
	 */
	private RenderStrategy renderStrategy = RenderStrategy.REDIRECT_TO_BUFFER;

	/** Filesystem Path to search for resources */
	private IResourceFinder resourceFinder = new Path();

	/** Frequency at which files should be polled */
	private Duration resourcePollFrequency = null;

	/** resource locator for this application */
	private IResourceStreamLocator resourceStreamLocator;

	/** ModificationWatcher to watch for changes in markup files */
	private ModificationWatcher resourceWatcher;

	/** List of {@link IResponseFilter}s. */
	private List<IResponseFilter> responseFilters;

	/**
	 * In order to do proper form parameter decoding it is important that the
	 * response and the following request have the same encoding. see
	 * http://www.crazysquirrel.com/computing/general/form-encoding.jspx for
	 * additional information.
	 */
	private String responseRequestEncoding = "UTF-8";

	/** Flag for serialize session attributes feature */
	private boolean serializeSessionAttributes = false;

	/** Chain of string resource loaders to use */
	private List<IStringResourceLoader> stringResourceLoaders = new ArrayList<IStringResourceLoader>(
			4);

	/** Should HTML comments be stripped during rendering? */
	private boolean stripComments = false;

	/**
	 * If true, wicket tags ( <wicket: ..>) and wicket:id attributes we be
	 * removed from output
	 */
	private boolean stripWicketTags = false;

	/** In order to remove <?xml?> from output as required by IE quirks mode */
	private boolean stripXmlDeclarationFromOutput;

	/** Flags used to determine how to behave if resources are not found */
	private boolean throwExceptionOnMissingResource = true;

	/** Authorizer for component instantiations */
	private IUnauthorizedComponentInstantiationListener unauthorizedComponentInstantiationListener = new IUnauthorizedComponentInstantiationListener()
	{
		/**
		 * Called when an unauthorized component instantiation is about to take
		 * place (but before it happens).
		 * 
		 * @param component
		 *            The partially constructed component (only the id is
		 *            guaranteed to be valid).
		 */
		public void onUnauthorizedInstantiation(final Component component)
		{
			throw new UnauthorizedInstantiationException(component.getClass());
		}
	};

	/** Type of handling for unexpected exceptions */
	private UnexpectedExceptionDisplay unexpectedExceptionDisplay = UnexpectedExceptionDisplay.SHOW_EXCEPTION_PAGE;

	/** Determines behavior of string resource loading if string is missing */
	private boolean useDefaultOnMissingResource = true;

	/** Determines if pages should be managed by a version manager by default */
	private boolean versionPagesByDefault = true;

	/**
	 * Create the application settings, carrying out any necessary
	 * initialisations.
	 * 
	 * @param application
	 *            The application that these settings are for
	 */
	public Settings(final Application application)
	{
		this.application = application;
		this.markupParserFactory = new MarkupParserFactory(application);
		stringResourceLoaders.add(new ComponentStringResourceLoader(application));
		stringResourceLoaders.add(new ClassStringResourceLoader(application, this.application
				.getClass()));
	}

	/**
	 * @see wicket.settings.IPageSettings#addComponentResolver(wicket.markup.resolver.IComponentResolver)
	 */
	public void addComponentResolver(final IComponentResolver resolver)
	{
		boolean hit = false;
		for (final IComponentResolver componentResolver : this.componentResolvers)
		{
			if (componentResolver.getClass().equals(resolver.getClass()))
			{
				hit = true;
				break;
			}
		}
		if (hit == false)
		{
			componentResolvers.add(resolver);
		}
		else
		{
			log.warn("A IComponentResolver of type " + resolver.getClass().getName()
					+ " has already been registered. The new one will not be added");
		}
	}

	/**
	 * @see wicket.settings.IResourceSettings#addResourceFactory(java.lang.String,
	 *      wicket.IResourceFactory)
	 */
	public void addResourceFactory(final String name, IResourceFactory resourceFactory)
	{
		nameToResourceFactory.put(name, resourceFactory);
	}

	/**
	 * @see wicket.settings.IResourceSettings#addResourceFolder(java.lang.String)
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
	 * @see wicket.settings.IRequestCycleSettings#addResponseFilter(wicket.IResponseFilter)
	 */
	public void addResponseFilter(IResponseFilter responseFilter)
	{
		if (responseFilters == null)
		{
			responseFilters = new ArrayList<IResponseFilter>(3);
		}
		responseFilters.add(responseFilter);
	}

	/**
	 * @see wicket.settings.IResourceSettings#addStringResourceLoader(wicket.resource.loader.IStringResourceLoader)
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
	 * @see wicket.settings.IApplicationSettings#getAccessDeniedPage()
	 */
	public Class<? extends Page> getAccessDeniedPage()
	{
		return accessDeniedPage;
	}

	/**
	 * @see wicket.settings.ISecuritySettings#getAuthorizationStrategy()
	 */
	public IAuthorizationStrategy getAuthorizationStrategy()
	{
		return authorizationStrategy;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#getAutomaticLinking()
	 */
	public boolean getAutomaticLinking()
	{
		return automaticLinking;
	}

	/**
	 * @see wicket.settings.IPageSettings#getAutomaticMultiWindowSupport()
	 */
	public boolean getAutomaticMultiWindowSupport()
	{
		return automaticMultiWindowSupport;
	}

	/**
	 * @see wicket.settings.IRequestCycleSettings#getBufferResponse()
	 */
	public boolean getBufferResponse()
	{
		return bufferResponse;
	}

	/**
	 * @see wicket.settings.IApplicationSettings#getClassResolver()
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
	public List<IComponentResolver> getComponentResolvers()
	{
		return componentResolvers;
	}

	/**
	 * @see wicket.settings.IDebugSettings#getComponentUseCheck()
	 */
	public boolean getComponentUseCheck()
	{
		return this.componentUseCheck;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#getCompressWhitespace()
	 */
	public boolean getCompressWhitespace()
	{
		return compressWhitespace;
	}

	/**
	 * @see wicket.settings.IApplicationSettings#getContextPath()
	 */
	public String getContextPath()
	{
		// Set the default context path if the context path is not already
		// set (previous time or by the developer itself)
		// This all to do missing api in the servlet spec.. You can't get a
		// context path from the servlet context, which is just stupid.
		if (contextPath == null && RequestCycle.get() != null
				&& RequestCycle.get().getRequest() instanceof WebRequest)
		{
			contextPath = ((WebRequest)RequestCycle.get().getRequest()).getContextPath();
		}
		return contextPath;
	}

	/**
	 * @see wicket.settings.IApplicationSettings#getConverterLocatorFactory()
	 */
	public IConverterLocatorFactory getConverterLocatorFactory()
	{
		if (converterFactory == null)
		{
			converterFactory = new CoverterLocatorFactory();
		}
		return converterFactory;
	}

	/**
	 * @see wicket.settings.ISecuritySettings#getCookieValuePersisterSettings()
	 */
	public CookieValuePersisterSettings getCookieValuePersisterSettings()
	{
		return cookieValuePersisterSettings;
	}

	/**
	 * @see wicket.settings.ISecuritySettings#getCryptFactory()
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
	 * @see wicket.settings.IMarkupSettings#getDefaultAfterDisabledLink()
	 */
	public String getDefaultAfterDisabledLink()
	{
		return defaultAfterDisabledLink;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#getDefaultBeforeDisabledLink()
	 */
	public String getDefaultBeforeDisabledLink()
	{
		return defaultBeforeDisabledLink;
	}

	/**
	 * @see wicket.settings.IApplicationSettings#getDefaultLocale()
	 */
	public Locale getDefaultLocale()
	{
		return defaultLocale;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#getDefaultMarkupEncoding()
	 */
	public String getDefaultMarkupEncoding()
	{
		return defaultMarkupEncoding;
	}

	/**
	 * @see wicket.settings.ISecuritySettings#getEnforceMounts()
	 */
	public boolean getEnforceMounts()
	{
		return enforceMounts;
	}

	/**
	 * @see wicket.settings.IRequestCycleSettings#getGatherExtendedBrowserInfo()
	 */
	public boolean getGatherExtendedBrowserInfo()
	{
		return gatherExtendedBrowserInfo;
	}

	/**
	 * @see wicket.settings.IApplicationSettings#getInternalErrorPage()
	 */
	public Class<? extends Page> getInternalErrorPage()
	{
		return internalErrorPage;
	}

	/**
	 * @see wicket.settings.IResourceSettings#getLocalizer()
	 */
	public Localizer getLocalizer()
	{
		if (localizer == null)
		{
			this.localizer = new Localizer(application);
		}
		return localizer;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#getMarkupParserFactory()
	 */
	public IMarkupParserFactory getMarkupParserFactory()
	{
		return markupParserFactory;
	}

	/**
	 * @see wicket.settings.ISessionSettings#getMaxPageMaps()
	 */
	public final int getMaxPageMaps()
	{
		return maxPageMaps;
	}

	/**
	 * @see wicket.settings.IPageSettings#getMaxPageVersions()
	 */
	public int getMaxPageVersions()
	{
		return maxPageVersions;
	}

	/**
	 * @see wicket.settings.IResourceSettings#getPackageResourceGuard()
	 */
	public IPackageResourceGuard getPackageResourceGuard()
	{
		return packageResourceGuard;
	}

	/**
	 * @see wicket.settings.IApplicationSettings#getPageExpiredErrorPage()
	 */
	public Class<? extends Page> getPageExpiredErrorPage()
	{
		return pageExpiredErrorPage;
	}

	/**
	 * @see wicket.settings.ISessionSettings#getPageFactory()
	 */
	public IPageFactory getPageFactory()
	{
		return pageFactory;
	}

	/**
	 * @see wicket.settings.ISessionSettings#getPageMapEvictionStrategy()
	 */
	public IPageMapEvictionStrategy getPageMapEvictionStrategy()
	{
		return pageMapEvictionStrategy;
	}

	/**
	 * @see wicket.settings.IResourceSettings#getPropertiesFactory()
	 */
	public wicket.resource.IPropertiesFactory getPropertiesFactory()
	{
		if (propertiesFactory == null)
		{
			propertiesFactory = new PropertiesFactory(this.application);
		}
		return propertiesFactory;
	}

	/**
	 * @see wicket.settings.IRequestCycleSettings#getRenderStrategy()
	 */
	public IRequestCycleSettings.RenderStrategy getRenderStrategy()
	{
		return renderStrategy;
	}

	/**
	 * @see wicket.settings.IResourceSettings#getResourceFactory(java.lang.String)
	 */
	public IResourceFactory getResourceFactory(final String name)
	{
		return nameToResourceFactory.get(name);
	}

	/**
	 * @see wicket.settings.IResourceSettings#getResourceFinder()
	 */
	public IResourceFinder getResourceFinder()
	{
		return resourceFinder;
	}

	/**
	 * @see wicket.settings.IResourceSettings#getResourcePollFrequency()
	 */
	public Duration getResourcePollFrequency()
	{
		return resourcePollFrequency;
	}

	/**
	 * @see wicket.settings.IResourceSettings#getResourceStreamLocator()
	 */
	public IResourceStreamLocator getResourceStreamLocator()
	{
		if (resourceStreamLocator == null)
		{
			// Create compound resource locator using source path from
			// application settings
			resourceStreamLocator = new CompoundResourceStreamLocator(getResourceFinder());
		}
		return resourceStreamLocator;
	}

	/**
	 * @see wicket.settings.IResourceSettings#getResourceWatcher(boolean)
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
	 * @see wicket.settings.IRequestCycleSettings#getResponseFilters()
	 */
	public List<IResponseFilter> getResponseFilters()
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
	 * @see wicket.settings.IRequestCycleSettings#getResponseRequestEncoding()
	 */
	public String getResponseRequestEncoding()
	{
		return responseRequestEncoding;
	}

	/**
	 * 
	 * @see wicket.settings.IDebugSettings#getSerializeSessionAttributes()
	 */
	public boolean getSerializeSessionAttributes()
	{
		return serializeSessionAttributes;
	}

	/**
	 * @see wicket.settings.IResourceSettings#getStringResourceLoaders()
	 */
	public List<IStringResourceLoader> getStringResourceLoaders()
	{
		return Collections.unmodifiableList(stringResourceLoaders);
	}

	/**
	 * @see wicket.settings.IMarkupSettings#getStripComments()
	 */
	public boolean getStripComments()
	{
		return stripComments;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#getStripWicketTags()
	 */
	public boolean getStripWicketTags()
	{
		return this.stripWicketTags;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#getStripXmlDeclarationFromOutput()
	 */
	public boolean getStripXmlDeclarationFromOutput()
	{
		return this.stripXmlDeclarationFromOutput;
	}

	/**
	 * @see wicket.settings.IResourceSettings#getThrowExceptionOnMissingResource()
	 */
	public boolean getThrowExceptionOnMissingResource()
	{
		return throwExceptionOnMissingResource;
	}


	/**
	 * @see wicket.settings.ISecuritySettings#getUnauthorizedComponentInstantiationListener()
	 */
	public IUnauthorizedComponentInstantiationListener getUnauthorizedComponentInstantiationListener()
	{
		return unauthorizedComponentInstantiationListener;
	}

	/**
	 * @see wicket.settings.IRequestCycleSettings#getUnexpectedExceptionDisplay()
	 */
	public UnexpectedExceptionDisplay getUnexpectedExceptionDisplay()
	{
		return unexpectedExceptionDisplay;
	}

	/**
	 * @see wicket.settings.IResourceSettings#getUseDefaultOnMissingResource()
	 */
	public boolean getUseDefaultOnMissingResource()
	{
		return useDefaultOnMissingResource;
	}

	/**
	 * @see wicket.settings.IFrameworkSettings#getVersion()
	 */
	public String getVersion()
	{
		String implVersion = null;
		Package pkg = this.getClass().getPackage();
		if (pkg != null)
		{
			implVersion = pkg.getImplementationVersion();
		}
		return Strings.isEmpty(implVersion) ? "n/a" : implVersion;
	}

	/**
	 * @see wicket.settings.IPageSettings#getVersionPagesByDefault()
	 */
	public boolean getVersionPagesByDefault()
	{
		return versionPagesByDefault;
	}

	/**
	 * @see wicket.settings.IDebugSettings#isAjaxDebugModeEnabled()
	 */
	public boolean isAjaxDebugModeEnabled()
	{
		return ajaxDebugModeEnabled;
	}

	/**
	 * @see wicket.settings.IApplicationSettings#setAccessDeniedPage(java.lang.Class)
	 */
	public void setAccessDeniedPage(Class<? extends Page> accessDeniedPage)
	{
		if (accessDeniedPage == null)
		{
			throw new IllegalArgumentException("Argument accessDeniedPage may not be null");
		}
		checkPageClass(accessDeniedPage);

		this.accessDeniedPage = accessDeniedPage;
	}

	/**
	 * @see wicket.settings.IDebugSettings#setAjaxDebugModeEnabled(boolean)
	 */
	public void setAjaxDebugModeEnabled(boolean enable)
	{
		ajaxDebugModeEnabled = enable;
	}

	/**
	 * @see wicket.settings.ISecuritySettings#setAuthorizationStrategy(wicket.authorization.IAuthorizationStrategy)
	 */
	public void setAuthorizationStrategy(IAuthorizationStrategy strategy)
	{
		if (strategy == null)
		{
			throw new IllegalArgumentException("authorization strategy cannot be set to null");
		}
		this.authorizationStrategy = strategy;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#setAutomaticLinking(boolean)
	 */
	public void setAutomaticLinking(boolean automaticLinking)
	{
		this.automaticLinking = automaticLinking;
	}

	/**
	 * @see wicket.settings.IPageSettings#setAutomaticMultiWindowSupport(boolean)
	 */
	public void setAutomaticMultiWindowSupport(boolean automaticMultiWindowSupport)
	{
		this.automaticMultiWindowSupport = automaticMultiWindowSupport;
	}

	/**
	 * @see wicket.settings.IRequestCycleSettings#setBufferResponse(boolean)
	 */
	public void setBufferResponse(boolean bufferResponse)
	{
		this.bufferResponse = bufferResponse;
	}

	/**
	 * @see wicket.settings.IApplicationSettings#setClassResolver(wicket.application.IClassResolver)
	 */
	public void setClassResolver(final IClassResolver defaultClassResolver)
	{
		this.classResolver = defaultClassResolver;
	}

	/**
	 * @see wicket.settings.IDebugSettings#setComponentUseCheck(boolean)
	 */
	public void setComponentUseCheck(final boolean componentUseCheck)
	{
		this.componentUseCheck = componentUseCheck;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#setCompressWhitespace(boolean)
	 */
	public void setCompressWhitespace(final boolean compressWhitespace)
	{
		this.compressWhitespace = compressWhitespace;
	}

	/**
	 * @see wicket.settings.IApplicationSettings#setContextPath(java.lang.String)
	 */
	public void setContextPath(String contextPath)
	{
		if (contextPath != null)
		{
			if (!contextPath.startsWith("/") && !contextPath.startsWith("http:")
					&& !contextPath.startsWith("https:"))
			{
				this.contextPath = "/" + contextPath;
			}
			else
			{
				this.contextPath = contextPath;
			}
		}
	}

	/**
	 * @see wicket.settings.IApplicationSettings#setConverterSupplierFactory(wicket.util.convert.IConverterLocatorFactory)
	 */
	public void setConverterSupplierFactory(IConverterLocatorFactory factory)
	{
		if (factory == null)
		{
			throw new IllegalArgumentException("converter factory cannot be set to null");
		}
		this.converterFactory = factory;
	}

	/**
	 * @see wicket.settings.ISecuritySettings#setCookieValuePersisterSettings(wicket.markup.html.form.persistence.CookieValuePersisterSettings)
	 */
	public void setCookieValuePersisterSettings(
			CookieValuePersisterSettings cookieValuePersisterSettings)
	{
		this.cookieValuePersisterSettings = cookieValuePersisterSettings;
	}

	/**
	 * @see wicket.settings.ISecuritySettings#setCryptFactory(wicket.util.crypt.ICryptFactory)
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
	 * @see wicket.settings.IMarkupSettings#setDefaultAfterDisabledLink(java.lang.String)
	 */
	public void setDefaultAfterDisabledLink(final String defaultAfterDisabledLink)
	{
		this.defaultAfterDisabledLink = defaultAfterDisabledLink;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#setDefaultBeforeDisabledLink(java.lang.String)
	 */
	public void setDefaultBeforeDisabledLink(String defaultBeforeDisabledLink)
	{
		this.defaultBeforeDisabledLink = defaultBeforeDisabledLink;
	}

	/**
	 * @see wicket.settings.IApplicationSettings#setDefaultLocale(java.util.Locale)
	 */
	public void setDefaultLocale(Locale defaultLocale)
	{
		this.defaultLocale = defaultLocale;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#setDefaultMarkupEncoding(java.lang.String)
	 */
	public void setDefaultMarkupEncoding(final String encoding)
	{
		this.defaultMarkupEncoding = encoding;
	}

	/**
	 * @see wicket.settings.ISecuritySettings#setEnforceMounts(boolean)
	 */
	public void setEnforceMounts(boolean enforce)
	{
		this.enforceMounts = enforce;
	}

	/**
	 * @see wicket.settings.IRequestCycleSettings#setGatherExtendedBrowserInfo(boolean)
	 */
	public void setGatherExtendedBrowserInfo(boolean gatherExtendedBrowserInfo)
	{
		this.gatherExtendedBrowserInfo = gatherExtendedBrowserInfo;
	}

	/**
	 * @see wicket.settings.IApplicationSettings#setInternalErrorPage(java.lang.Class)
	 */
	public void setInternalErrorPage(final Class<? extends Page> internalErrorPage)
	{
		if (internalErrorPage == null)
		{
			throw new IllegalArgumentException("Argument internalErrorPage may not be null");
		}
		checkPageClass(internalErrorPage);

		this.internalErrorPage = internalErrorPage;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#setMarkupParserFactory(wicket.markup.IMarkupParserFactory)
	 */
	public void setMarkupParserFactory(IMarkupParserFactory factory)
	{
		if (factory == null)
		{
			throw new IllegalArgumentException("markup parser factory cannot be null");
		}

		this.markupParserFactory = factory;
	}

	/**
	 * @see wicket.settings.ISessionSettings#setMaxPageMaps(int)
	 */
	public final void setMaxPageMaps(int maxPageMaps)
	{
		this.maxPageMaps = maxPageMaps;
	}

	/**
	 * @see wicket.settings.IPageSettings#setMaxPageVersions(int)
	 */
	public void setMaxPageVersions(int maxPageVersions)
	{
		if (maxPageVersions < 0)
		{
			throw new IllegalArgumentException("Value for maxPageVersions must be >= 0");
		}
		this.maxPageVersions = maxPageVersions;
	}

	/**
	 * @see wicket.settings.IResourceSettings#setPackageResourceGuard(wicket.markup.html.IPackageResourceGuard)
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
	 * @see wicket.settings.IApplicationSettings#setPageExpiredErrorPage(java.lang.Class)
	 */
	public void setPageExpiredErrorPage(final Class<? extends Page> pageExpiredErrorPage)
	{
		if (pageExpiredErrorPage == null)
		{
			throw new IllegalArgumentException("Argument pageExpiredErrorPage may not be null");
		}
		checkPageClass(pageExpiredErrorPage);

		this.pageExpiredErrorPage = pageExpiredErrorPage;
	}

	/**
	 * @see wicket.settings.ISessionSettings#setPageFactory(wicket.IPageFactory)
	 */
	public void setPageFactory(final IPageFactory defaultPageFactory)
	{
		this.pageFactory = defaultPageFactory;
	}

	/**
	 * @see wicket.settings.ISessionSettings#setPageMapEvictionStrategy(wicket.session.pagemap.IPageMapEvictionStrategy)
	 */
	public void setPageMapEvictionStrategy(IPageMapEvictionStrategy pageMapEvictionStrategy)
	{
		this.pageMapEvictionStrategy = pageMapEvictionStrategy;
	}

	/**
	 * @see wicket.settings.IResourceSettings#setPropertiesFactory(wicket.resource.PropertiesFactory)
	 */
	public void setPropertiesFactory(wicket.resource.IPropertiesFactory factory)
	{
		this.propertiesFactory = factory;
	}

	/**
	 * @see wicket.settings.IRequestCycleSettings#setRenderStrategy(wicket.settings.Settings.RenderStrategy)
	 */
	public void setRenderStrategy(IRequestCycleSettings.RenderStrategy renderStrategy)
	{
		this.renderStrategy = renderStrategy;
	}

	/**
	 * @see wicket.settings.IResourceSettings#setResourceFinder(wicket.util.file.IResourceFinder)
	 */
	public void setResourceFinder(final IResourceFinder resourceFinder)
	{
		this.resourceFinder = resourceFinder;

		// Cause resource locator to get recreated
		this.resourceStreamLocator = null;
	}

	/**
	 * @see wicket.settings.IResourceSettings#setResourcePollFrequency(wicket.util.time.Duration)
	 */
	public void setResourcePollFrequency(final Duration resourcePollFrequency)
	{
		this.resourcePollFrequency = resourcePollFrequency;
	}

	/**
	 * @see wicket.settings.IResourceSettings#setResourceStreamLocator(wicket.util.resource.locator.IResourceStreamLocator)
	 */
	public void setResourceStreamLocator(IResourceStreamLocator resourceStreamLocator)
	{
		this.resourceStreamLocator = resourceStreamLocator;
	}

	/**
	 * @see wicket.settings.IRequestCycleSettings#setResponseRequestEncoding(java.lang.String)
	 */
	public void setResponseRequestEncoding(final String responseRequestEncoding)
	{
		this.responseRequestEncoding = responseRequestEncoding;
	}

	/**
	 * 
	 * @see wicket.settings.IDebugSettings#setSerializeSessionAttributes(boolean)
	 */
	public void setSerializeSessionAttributes(boolean serialize)
	{
		this.serializeSessionAttributes = serialize;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#setStripComments(boolean)
	 */
	public void setStripComments(boolean stripComments)
	{
		this.stripComments = stripComments;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#setStripWicketTags(boolean)
	 */
	public void setStripWicketTags(boolean stripWicketTags)
	{
		this.stripWicketTags = stripWicketTags;
	}

	/**
	 * @see wicket.settings.IMarkupSettings#setStripXmlDeclarationFromOutput(boolean)
	 */
	public void setStripXmlDeclarationFromOutput(final boolean strip)
	{
		this.stripXmlDeclarationFromOutput = strip;
	}

	/**
	 * @see wicket.settings.IResourceSettings#setThrowExceptionOnMissingResource(boolean)
	 */
	public void setThrowExceptionOnMissingResource(final boolean throwExceptionOnMissingResource)
	{
		this.throwExceptionOnMissingResource = throwExceptionOnMissingResource;
	}

	/**
	 * @see wicket.settings.ISecuritySettings#setUnauthorizedComponentInstantiationListener(wicket.authorization.IUnauthorizedComponentInstantiationListener)
	 */
	public void setUnauthorizedComponentInstantiationListener(
			IUnauthorizedComponentInstantiationListener unauthorizedComponentInstantiationListener)
	{
		this.unauthorizedComponentInstantiationListener = unauthorizedComponentInstantiationListener;
	}

	/**
	 * @see wicket.settings.IRequestCycleSettings#setUnexpectedExceptionDisplay(wicket.settings.Settings.UnexpectedExceptionDisplay)
	 */
	public void setUnexpectedExceptionDisplay(
			final UnexpectedExceptionDisplay unexpectedExceptionDisplay)
	{
		this.unexpectedExceptionDisplay = unexpectedExceptionDisplay;
	}

	/**
	 * @see wicket.settings.IResourceSettings#setUseDefaultOnMissingResource(boolean)
	 */
	public void setUseDefaultOnMissingResource(final boolean useDefaultOnMissingResource)
	{
		this.useDefaultOnMissingResource = useDefaultOnMissingResource;
	}

	/**
	 * @see wicket.settings.IPageSettings#setVersionPagesByDefault(boolean)
	 */
	public void setVersionPagesByDefault(boolean pagesVersionedByDefault)
	{
		this.versionPagesByDefault = pagesVersionedByDefault;
	}

	/**
	 * Throws an IllegalArgumentException if the given class is not a subclass
	 * of Page.
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
			throw new IllegalArgumentException("argument " + pageClass
					+ " must be a subclass of Page");
		}
	}
}
