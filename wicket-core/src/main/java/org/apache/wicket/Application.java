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
package org.apache.wicket;

import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.wicket.application.ComponentInitializationListenerCollection;
import org.apache.wicket.application.ComponentInstantiationListenerCollection;
import org.apache.wicket.application.ComponentOnAfterRenderListenerCollection;
import org.apache.wicket.application.ComponentOnBeforeRenderListenerCollection;
import org.apache.wicket.application.ComponentOnConfigureListenerCollection;
import org.apache.wicket.application.HeaderContributorListenerCollection;
import org.apache.wicket.application.IComponentInitializationListener;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.core.request.mapper.IMapperContext;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.core.util.resource.ClassPathResourceFinder;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.event.IEventSink;
import org.apache.wicket.javascript.DefaultJavaScriptCompressor;
import org.apache.wicket.markup.MarkupFactory;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;
import org.apache.wicket.markup.html.image.resource.DefaultButtonImageResourceFactory;
import org.apache.wicket.markup.parser.filter.EnclosureHandler;
import org.apache.wicket.markup.parser.filter.InlineEnclosureHandler;
import org.apache.wicket.markup.parser.filter.RelativePathPrefixHandler;
import org.apache.wicket.markup.parser.filter.WicketLinkTagHandler;
import org.apache.wicket.markup.parser.filter.WicketMessageTagHandler;
import org.apache.wicket.markup.resolver.HtmlHeaderResolver;
import org.apache.wicket.markup.resolver.WicketContainerResolver;
import org.apache.wicket.markup.resolver.WicketMessageResolver;
import org.apache.wicket.page.DefaultPageManagerContext;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.page.IPageManagerContext;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.protocol.http.IRequestLogger;
import org.apache.wicket.protocol.http.RequestLogger;
import org.apache.wicket.protocol.http.RequestLoggerRequestCycleListener;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.cycle.RequestCycleListenerCollection;
import org.apache.wicket.request.mapper.ICompoundRequestMapper;
import org.apache.wicket.request.resource.ResourceReferenceRegistry;
import org.apache.wicket.response.filter.EmptySrcAttributeCheckFilter;
import org.apache.wicket.session.DefaultPageFactory;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.session.ISessionStore.UnboundListener;
import org.apache.wicket.settings.ApplicationSettings;
import org.apache.wicket.settings.DebugSettings;
import org.apache.wicket.settings.ExceptionSettings;
import org.apache.wicket.settings.FrameworkSettings;
import org.apache.wicket.settings.JavaScriptLibrarySettings;
import org.apache.wicket.settings.MarkupSettings;
import org.apache.wicket.settings.PageSettings;
import org.apache.wicket.settings.RequestCycleSettings;
import org.apache.wicket.settings.RequestLoggerSettings;
import org.apache.wicket.settings.ResourceSettings;
import org.apache.wicket.settings.SecuritySettings;
import org.apache.wicket.settings.StoreSettings;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all Wicket applications. To create a Wicket application, you generally should
 * <i>not </i> directly subclass this class. Instead, you will want to subclass some subclass of
 * Application, like WebApplication, which is appropriate for the protocol and markup type you are
 * working with.
 * <p>
 * Application has the following interesting features / attributes:
 * <ul>
 * <li><b>Name </b>- The Application's name, which is the same as its class name.
 * 
 * <li><b>Home Page </b>- The Application's home Page class. Subclasses must override getHomePage()
 * to provide this property value.
 * 
 * <li><b>Settings </b>- Application settings are partitioned into sets of related settings using
 * interfaces in the org.apache.wicket.settings package. These interfaces are returned by the
 * following methods, which should be used to configure framework settings for your application:
 * getApplicationSettings(), getDebugSettings(), getExceptionSettings(), getMarkupSettings(),
 * getPageSettings(), getRequestCycleSettings(), getSecuritySettings and getSessionSettings(). These
 * settings are configured by default through the constructor or internalInit methods. Default the
 * application is configured for DEVELOPMENT. You can configure this globally to DEPLOYMENT or
 * override specific settings by implementing the init() method.
 * 
 * <li><b>Shared Resources </b>- Resources added to an Application's SharedResources have
 * application-wide scope and can be referenced using a logical scope and a name with the
 * ResourceReference class. ResourceReferences can then be used by multiple components in the same
 * application without additional overhead (beyond the ResourceReference instance held by each
 * referee) and will yield a stable URL, permitting efficient browser caching of the resource (even
 * if the resource is dynamically generated). Resources shared in this manner may also be localized.
 * See {@link org.apache.wicket.request.resource.ResourceReference} for more details.
 * 
 * <li><b>Custom Session Subclasses</b>- In order to install your own {@link Session} subclass you
 * must override Application{@link #newSession(Request, Response)}. For subclasses of
 * {@link WebApplication} you will want to subclass {@link WebSession}.
 * 
 * </ul>
 * 
 * @see org.apache.wicket.protocol.http.WebApplication
 * @author Jonathan Locke
 */
public abstract class Application implements UnboundListener, IEventSink
{
	/** Configuration constant for the 2 types */
	public static final String CONFIGURATION = "configuration";

	/**
	 * Applications keyed on the {@link #getApplicationKey()} so that they can be retrieved even
	 * without being in a request/ being set in the thread local (we need that e.g. for when we are
	 * in a destruction thread).
	 */
	private static final Map<String, Application> applicationKeyToApplication = Generics.newHashMap(1);

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(Application.class);

	private static final String PROPERTIES_FILE_EXTENSION = ".properties";

	/**
	 * The path to look for Wicket specific properties file(s)
	 */
	private static final String META_INF_WICKET_PATH = File.separatorChar+ "META-INF" + File.separatorChar + "wicket" + File.separatorChar;

	/** root mapper */
	private IRequestMapper rootRequestMapper;

	/** The converter locator instance. */
	private IConverterLocator converterLocator;

	/** list of initializers. */
	private final List<IInitializer> initializers = Generics.newArrayList();

	/** Application level meta data. */
	private MetaDataEntry<?>[] metaData;

	/** Name of application subclass. */
	private String name;

	/** Request logger instance. */
	private IRequestLogger requestLogger;

	/** The session facade. */
	private volatile ISessionStore sessionStore;

	/** page renderer provider */
	private IPageRendererProvider pageRendererProvider;

	/** request cycle provider */
	private IRequestCycleProvider requestCycleProvider;

	/** exception mapper provider */
	private Supplier<IExceptionMapper> exceptionMapperProvider;

	/** session store provider */
	private Supplier<ISessionStore> sessionStoreProvider;

	/**
	 * The decorator this application uses to decorate any header responses created by Wicket
	 */
	private IHeaderResponseDecorator headerResponseDecorator = (headerresponse) -> {
		return new ResourceAggregator(headerresponse);
	};

	/**
	 * Checks if the <code>Application</code> threadlocal is set in this thread
	 * 
	 * @return true if {@link Application#get()} can return the instance of application, false
	 *         otherwise
	 */
	public static boolean exists()
	{
		return ThreadContext.getApplication() != null;
	}

	/**
	 * Get Application for current thread.
	 * 
	 * @return The current thread's Application
	 */
	public static Application get()
	{
		Application application = ThreadContext.getApplication();
		if (application == null)
		{
			throw new WicketRuntimeException("There is no application attached to current thread " +
				Thread.currentThread().getName());
		}
		return application;
	}

	/**
	 * Gets the Application based on the application key of that application. You typically never
	 * have to use this method unless you are working on an integration project.
	 * 
	 * @param applicationKey
	 *            The unique key of the application within a certain context (e.g. a web
	 *            application)
	 * @return The application or <code>null</code> if application has not been found
	 */
	public static Application get(final String applicationKey)
	{
		return applicationKeyToApplication.get(applicationKey);
	}

	/**
	 * Gets the keys of the currently registered Wicket applications for this web application. You
	 * typically never have to use this method unless you are working on an integration project.
	 * 
	 * @return unmodifiable set with keys that correspond with {@link #getApplicationKey()}. Never
	 *         null, but possibly empty
	 */
	public static Set<String> getApplicationKeys()
	{
		return Collections.unmodifiableSet(applicationKeyToApplication.keySet());
	}

	/**
	 * Constructor. <strong>Use {@link #init()} for any configuration of your application instead of
	 * overriding the constructor.</strong>
	 */
	public Application()
	{
		// Install default component instantiation listener that uses
		// authorization strategy to check component instantiations.
		getComponentInstantiationListeners().add(new IComponentInstantiationListener()
		{
			/**
			 * @see org.apache.wicket.application.IComponentInstantiationListener#onInstantiation(org.apache.wicket.Component)
			 */
			@Override
			public void onInstantiation(final Component component)
			{
				final Class<? extends Component> cl = component.getClass();
				// If component instantiation is not authorized
				if (!Session.get().getAuthorizationStrategy().isInstantiationAuthorized(cl))
				{
					// then call any unauthorized component instantiation
					// listener
					getSecuritySettings().getUnauthorizedComponentInstantiationListener()
						.onUnauthorizedInstantiation(component);
				}
			}
		});
	}

	/**
	 * Configures application settings to good defaults.
	 */
	public final void configure()
	{
		// As long as this is public api the development and deployment mode
		// should counter act each other for all properties.
		switch (getConfigurationType())
		{
			case DEVELOPMENT : {
				getResourceSettings().setResourcePollFrequency(Duration.ONE_SECOND);
				getResourceSettings().setJavaScriptCompressor(null);
				getResourceSettings().setUseMinifiedResources(false);
				getMarkupSettings().setStripWicketTags(false);
				getExceptionSettings().setUnexpectedExceptionDisplay(
					ExceptionSettings.SHOW_EXCEPTION_PAGE);
				getDebugSettings().setComponentUseCheck(true);
				getDebugSettings().setAjaxDebugModeEnabled(true);
				getDebugSettings().setDevelopmentUtilitiesEnabled(true);
				// getDebugSettings().setOutputMarkupContainerClassName(true);
				getRequestCycleSettings().addResponseFilter(EmptySrcAttributeCheckFilter.INSTANCE);
				break;
			}
			case DEPLOYMENT : {
				getResourceSettings().setResourcePollFrequency(null);
				getResourceSettings().setJavaScriptCompressor(new DefaultJavaScriptCompressor());
				getMarkupSettings().setStripWicketTags(true);
				getExceptionSettings().setUnexpectedExceptionDisplay(
					ExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
				getDebugSettings().setComponentUseCheck(false);
				getDebugSettings().setAjaxDebugModeEnabled(false);
				getDebugSettings().setDevelopmentUtilitiesEnabled(false);
				break;
			}
		}
	}

	/**
	 * Gets the unique key of this application within a given context (like a web application). NOT
	 * INTENDED FOR FRAMEWORK CLIENTS.
	 * 
	 * @return The unique key of this application
	 */
	public abstract String getApplicationKey();

	/**
	 * Gets the configuration mode to use for configuring the app, either
	 * {@link RuntimeConfigurationType#DEVELOPMENT} or {@link RuntimeConfigurationType#DEPLOYMENT}.
	 * <p>
	 * The configuration type. Must currently be either DEVELOPMENT or DEPLOYMENT. Currently, if the
	 * configuration type is DEVELOPMENT, resources are polled for changes, component usage is
	 * checked, wicket tags are not stripped from output and a detailed exception page is used. If
	 * the type is DEPLOYMENT, component usage is not checked, wicket tags are stripped from output
	 * and a non-detailed exception page is used to display errors.
	 * <p>
	 * Note that you should not run Wicket in DEVELOPMENT mode on production servers - the various
	 * debugging checks and resource polling is inefficient and may leak resources, particularly on
	 * webapp redeploy.
	 * 
	 * <div style="border-style:solid;">
	 * <p>
	 * To change the deployment mode, add the following to your web.xml, inside your <servlet>
	 * mapping (or <filter> mapping if you're using 1.3.x):
	 * 
	 * <pre>
	 * &lt;init-param&gt;
	 *             &lt;param-name&gt;configuration&lt;/param-name&gt;
	 *             &lt;param-value&gt;deployment&lt;/param-value&gt;
	 * &lt;/init-param&gt;
	 * </pre>
	 * 
	 * <p>
	 * You can alternatively set this as a &lt;context-param&gt; on the whole context.
	 * 
	 * <p>
	 * Another option is to set the "wicket.configuration" system property to either "deployment" or
	 * "development". The value is not case-sensitive.
	 * 
	 * <p>
	 * The system property is checked first, allowing you to add a web.xml param for deployment, and
	 * a command-line override when you want to run in development mode during development.
	 * 
	 * <p>
	 * You may also override Application.getConfigurationType() to provide your own custom switch,
	 * in which case none of the above logic is used. </div>
	 * 
	 * <p>
	 * IMPORTANT NOTE
	 * </p>
	 * THIS METHOD IS CALLED OFTEN FROM MANY DIFFERENT POINTS IN CODE, INCLUDING DURING THE RENDER
	 * PROCESS, THEREFORE THE IMPLEMENTATION SHOULD BE FAST - PREFERRABLY USING A FAST-TO-RETRIEVE
	 * CACHED VALUE
	 * 
	 * @return configuration
	 * @since 1.2.3 (function existed as a property getter)
	 * @since 1.3.0 (abstract, used to configure things)
	 */
	public abstract RuntimeConfigurationType getConfigurationType();

	/**
	 * Application subclasses must specify a home page class by implementing this abstract method.
	 * 
	 * @return Home page class for this application
	 */
	public abstract Class<? extends Page> getHomePage();

	/**
	 * @return The converter locator for this application
	 */
	public final IConverterLocator getConverterLocator()
	{
		return converterLocator;
	}

	/**
	 * Gets metadata for this application using the given key.
	 * 
	 * @param <T>
	 * @param key
	 *            The key for the data
	 * @return The metadata
	 * @see MetaDataKey
	 */
	public final synchronized <T> T getMetaData(final MetaDataKey<T> key)
	{
		return key.get(metaData);
	}

	/**
	 * Gets the name of this application.
	 * 
	 * @return The application name.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * Gets the {@link IRequestLogger}.
	 * 
	 * @return The RequestLogger
	 */
	public final IRequestLogger getRequestLogger()
	{
		if (getRequestLoggerSettings().isRequestLoggerEnabled())
		{
			if (requestLogger == null)
			{
				requestLogger = newRequestLogger();
			}
		}
		else
		{
			requestLogger = null;
		}
		return requestLogger;
	}

	/**
	 * Gets the facade object for working getting/ storing session instances.
	 * 
	 * @return The session facade
	 */
	public final ISessionStore getSessionStore()
	{
		if (sessionStore == null)
		{
			synchronized (this)
			{
				if (sessionStore == null)
				{
					sessionStore = sessionStoreProvider.get();
					sessionStore.registerUnboundListener(this);
				}
			}
		}
		return sessionStore;
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore.UnboundListener#sessionUnbound(java.lang.String)
	 */
	@Override
	public void sessionUnbound(final String sessionId)
	{
		getSessionListeners().onUnbound(sessionId);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * @param target
	 */
	public void logEventTarget(final IRequestHandler target)
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * @param requestTarget
	 */
	public void logResponseTarget(final IRequestHandler requestTarget)
	{
	}

	/**
	 * Creates a new session. Override this method if you want to provide a custom session.
	 * 
	 * @param request
	 *            The request that will create this session.
	 * @param response
	 *            The response to initialize, for example with cookies. This is important to use
	 *            cases involving unit testing because those use cases might want to be able to sign
	 *            a user in automatically when the session is created.
	 * 
	 * @return The session
	 * 
	 * @since 1.3
	 */
	public abstract Session newSession(Request request, Response response);

	/**
	 * Sets the metadata for this application using the given key. If the metadata object is not of
	 * the correct type for the metadata key, an IllegalArgumentException will be thrown. For
	 * information on creating MetaDataKeys, see {@link MetaDataKey}.
	 * 
	 * @param <T>
	 * @param key
	 *            The singleton key for the metadata
	 * @param object
	 *            The metadata object
	 * @throws IllegalArgumentException
	 * @see MetaDataKey
	 */
	public final synchronized <T> Application setMetaData(final MetaDataKey<T> key, final Object object)
	{
		metaData = key.set(metaData, object);
		return this;
	}

	/**
	 * Construct and add initializer from the provided class name.
	 * 
	 * @param className
	 */
	private void addInitializer(final String className)
	{
		IInitializer initializer = WicketObjects.newInstance(className);
		if (initializer != null)
		{
			initializers.add(initializer);
		}
	}

	/**
	 * Iterate initializers list, calling their {@link IInitializer#destroy(Application) destroy}
	 * methods.
	 */
	private void destroyInitializers()
	{
		for (IInitializer initializer : initializers)
		{
			log.info("[{}] destroy: {}", getName(), initializer);
			initializer.destroy(this);
		}
	}

	/**
	 * Iterate initializers list, calling {@link IInitializer#init(Application)} on any instances
	 * found in it.
	 */
	private void initInitializers()
	{
		for (IInitializer initializer : initializers)
		{
			log.info("[{}] init: {}", getName(), initializer);
			initializer.init(this);
		}

		final ServiceLoader<IInitializer> serviceLoaderInitializers = ServiceLoader.load(IInitializer.class);
		for (IInitializer serviceLoaderInitializer : serviceLoaderInitializers) {
			log.info("[{}] init: {}", getName(), serviceLoaderInitializer);
			serviceLoaderInitializer.init(this);
			initializers.add(serviceLoaderInitializer);
		}
	}

	/**
	 * @param properties
	 *            Properties map with names of any library initializers in it
	 */
	private void load(final Properties properties)
	{
		addInitializer(properties.getProperty("initializer"));
		addInitializer(properties.getProperty(getName() + "-initializer"));
	}

	/**
	 * Called when wicket servlet is destroyed. Overrides do not have to call super.
	 */
	protected void onDestroy()
	{
	}

	/**
	 * Allows for initialization of the application by a subclass. <strong>Use this method for any
	 * application setup instead of the constructor.</strong>
	 */
	protected void init()
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 */
	public void internalDestroy()
	{
		applicationListeners.onBeforeDestroyed(this);

		// destroy detach listener
		final IDetachListener detachListener = getFrameworkSettings().getDetachListener();
		if (detachListener != null)
		{
			detachListener.onDestroyListener();
		}

		// Clear caches of Class keys so the classloader can be garbage
		// collected (WICKET-625)
		PropertyResolver.destroy(this);
		MarkupFactory markupFactory = getMarkupSettings().getMarkupFactory();

		if (markupFactory.hasMarkupCache())
		{
			markupFactory.getMarkupCache().shutdown();
		}

		onDestroy();

		destroyInitializers();

		internalGetPageManager().destroy();
		getSessionStore().destroy();

		applicationKeyToApplication.remove(getApplicationKey());
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT OVERRIDE OR CALL.
	 * 
	 * Internal initialization.
	 */
	protected void internalInit()
	{
		settingsAccessible = true;
		PageSettings pageSettings = getPageSettings();

		// Install default component resolvers
		pageSettings.addComponentResolver(new HtmlHeaderResolver());
		pageSettings.addComponentResolver(new WicketLinkTagHandler());
		pageSettings.addComponentResolver(new WicketMessageResolver());
		pageSettings.addComponentResolver(new RelativePathPrefixHandler());
		pageSettings.addComponentResolver(new EnclosureHandler());
		pageSettings.addComponentResolver(new InlineEnclosureHandler());
		pageSettings.addComponentResolver(new WicketMessageTagHandler());
		pageSettings.addComponentResolver(new WicketContainerResolver());

		getResourceSettings().getResourceFinders().add(new ClassPathResourceFinder(""));

		// Install button image resource factory
		getResourceSettings().addResourceFactory("buttonFactory",
			new DefaultButtonImageResourceFactory());

		String applicationKey = getApplicationKey();
		applicationKeyToApplication.put(applicationKey, this);

		converterLocator = newConverterLocator();

		setPageManagerProvider(new DefaultPageManagerProvider(this));
		resourceReferenceRegistry = newResourceReferenceRegistry();
		sharedResources = newSharedResources(resourceReferenceRegistry);
		resourceBundles = newResourceBundles(resourceReferenceRegistry);

		// set up default request mapper
		setRootRequestMapper(new SystemMapper(this));

		pageFactory = newPageFactory();

		requestCycleProvider = (context) -> new RequestCycle(context);
		exceptionMapperProvider = () -> new DefaultExceptionMapper();

		// add a request cycle listener that logs each request for the requestlogger.
		getRequestCycleListeners().add(new RequestLoggerRequestCycleListener());
	}

	/**
	 * Returns a supplier of {@link IExceptionMapper} that will be used to
	 * handle exceptions which were not handled by any
	 * {@link IRequestCycleListener#onException(RequestCycle, Exception) request cycle listener}.
	 *
	 * @return the exception mapper supplier
	 * @see IRequestCycleListener#onException(RequestCycle, Exception)
	 */
	public Supplier<IExceptionMapper> getExceptionMapperProvider()
	{
		return exceptionMapperProvider;
	}

	/**
	 * 
	 * @return Session state provider
	 */
	public final Supplier<ISessionStore> getSessionStoreProvider()
	{
		return sessionStoreProvider;
	}

	/**
	 * 
	 * @param sessionStoreProvider
	 */
	public final Application setSessionStoreProvider(final Supplier<ISessionStore> sessionStoreProvider)
	{
		this.sessionStoreProvider = Args.notNull(sessionStoreProvider, "sessionStoreProvider");
		this.sessionStore = null;
		return this;
	}

	/**
	 * Creates and returns a new instance of {@link IConverterLocator}.
	 * 
	 * @return A new {@link IConverterLocator} instance
	 */
	protected IConverterLocator newConverterLocator()
	{
		return new ConverterLocator();
	}

	/**
	 * creates a new request logger when requests logging is enabled.
	 * 
	 * @return The new request logger
	 * 
	 */
	protected IRequestLogger newRequestLogger()
	{
		return new RequestLogger();
	}

	/**
	 * Converts the root mapper to a {@link ICompoundRequestMapper} if necessary and returns the
	 * converted instance.
	 * 
	 * @return compound instance of the root mapper
	 */
	public final ICompoundRequestMapper getRootRequestMapperAsCompound()
	{
		IRequestMapper root = getRootRequestMapper();
		if (!(root instanceof ICompoundRequestMapper))
		{
			root = new SystemMapper(this).add(root);
			setRootRequestMapper(root);
		}
		return (ICompoundRequestMapper)root;
	}

	/**
	 * @return The root request mapper
	 */
	public final IRequestMapper getRootRequestMapper()
	{
		return rootRequestMapper;
	}

	/**
	 * Sets the root request mapper
	 * 
	 * @param rootRequestMapper
	 */
	public final Application setRootRequestMapper(final IRequestMapper rootRequestMapper)
	{
		this.rootRequestMapper = rootRequestMapper;
		return this;
	}

	/**
	 * Initialize the application
	 */
	public final void initApplication()
	{
		if (name == null)
		{
			throw new IllegalStateException("setName must be called before initApplication");
		}
		internalInit();
		initInitializers();
		init();
		applicationListeners.onAfterInitialized(this);

		validateInit();
	}

	/**
	 * Gives the Application object a chance to validate if it has been properly initialized
	 */
	protected void validateInit()
	{
		if (getPageRendererProvider() == null)
		{
			throw new IllegalStateException(
				"An instance of IPageRendererProvider has not yet been set on this Application. @see Application#setPageRendererProvider");
		}
		if (getSessionStoreProvider() == null)
		{
			throw new IllegalStateException(
				"An instance of ISessionStoreProvider has not yet been set on this Application. @see Application#setSessionStoreProvider");
		}
		if (getPageManagerProvider() == null)
		{
			throw new IllegalStateException(
				"An instance of IPageManagerProvider has not yet been set on this Application. @see Application#setPageManagerProvider");
		}
	}

	/**
	 * Sets application name. This method must be called before any other methods are invoked and
	 * can only be called once per application instance.
	 * 
	 * @param name
	 *            unique application name
	 */
	public final void setName(final String name)
	{
		Args.notEmpty(name, "name");

		if (this.name != null)
		{
			throw new IllegalStateException("Application name can only be set once.");
		}

		if (applicationKeyToApplication.get(name) != null)
		{
			throw new IllegalStateException("Application with name '" + name + "' already exists.'");
		}

		this.name = name;
		applicationKeyToApplication.put(name, this);
	}

	/**
	 * Returns the mime type for given filename.
	 * 
	 * @param fileName
	 * @return mime type
	 */
	public String getMimeType(final String fileName)
	{
		return URLConnection.getFileNameMap().getContentTypeFor(fileName);
	}

	/** {@inheritDoc} */
	@Override
	public void onEvent(final IEvent<?> event)
	{
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Listeners
	//
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** */
	private final ComponentOnBeforeRenderListenerCollection componentPreOnBeforeRenderListeners = new ComponentOnBeforeRenderListenerCollection();

	/** */
	private final ComponentOnBeforeRenderListenerCollection componentPostOnBeforeRenderListeners = new ComponentOnBeforeRenderListenerCollection();

	/** */
	private final ComponentOnAfterRenderListenerCollection componentOnAfterRenderListeners = new ComponentOnAfterRenderListenerCollection();

	/** */
	private final RequestCycleListenerCollection requestCycleListeners = new RequestCycleListenerCollection();

	private final ApplicationListenerCollection applicationListeners = new ApplicationListenerCollection();

	private final SessionListenerCollection sessionListeners = new SessionListenerCollection();

	/** list of {@link IComponentInstantiationListener}s. */
	private final ComponentInstantiationListenerCollection componentInstantiationListeners = new ComponentInstantiationListenerCollection();

	/** list of {@link IComponentInitializationListener}s. */
	private final ComponentInitializationListenerCollection componentInitializationListeners = new ComponentInitializationListenerCollection();

	/** list of {@link org.apache.wicket.application.IComponentOnConfigureListener}s. */
	private final ComponentOnConfigureListenerCollection componentOnConfigureListeners = new ComponentOnConfigureListenerCollection();

	/** list of {@link IHeaderContributor}s. */
	private final HeaderContributorListenerCollection headerContributorListeners = new HeaderContributorListenerCollection();

	private final BehaviorInstantiationListenerCollection behaviorInstantiationListeners = new BehaviorInstantiationListenerCollection();

	/**
	 * @return Gets the application's {@link HeaderContributorListenerCollection}
	 */
	public final HeaderContributorListenerCollection getHeaderContributorListeners()
	{
		return headerContributorListeners;
	}

	/**
	 * @return collection of initializers
	 */
	public final List<IInitializer> getInitializers()
	{
		return Collections.unmodifiableList(initializers);
	}

	/**
	 * @return collection of application listeners
	 */
	public final ApplicationListenerCollection getApplicationListeners()
	{
		return applicationListeners;
	}

	/**
	 * @return collection of session listeners
	 */
	public final SessionListenerCollection getSessionListeners()
	{
		return sessionListeners;
	}

	/**
	 * @return collection of behavior instantiation listeners
	 */
	public final BehaviorInstantiationListenerCollection getBehaviorInstantiationListeners()
	{
		return behaviorInstantiationListeners;
	}


	/**
	 * @return Gets the application's ComponentInstantiationListenerCollection
	 */
	public final ComponentInstantiationListenerCollection getComponentInstantiationListeners()
	{
		return componentInstantiationListeners;
	}

	/**
	 * @return Gets the application's ComponentInitializationListeners
	 */
	public final ComponentInitializationListenerCollection getComponentInitializationListeners()
	{
		return componentInitializationListeners;
	}

	/**
	 * @return Gets the application's ComponentOnConfigureListeners
	 */
	public final ComponentOnConfigureListenerCollection getComponentOnConfigureListeners()
	{
		return componentOnConfigureListeners;
	}

	/**
	 * 
	 * @return ComponentOnBeforeRenderListenerCollection
	 */
	public final ComponentOnBeforeRenderListenerCollection getComponentPreOnBeforeRenderListeners()
	{
		return componentPreOnBeforeRenderListeners;
	}

	/**
	 * 
	 * @return ComponentOnBeforeRenderListenerCollection
	 */
	public final ComponentOnBeforeRenderListenerCollection getComponentPostOnBeforeRenderListeners()
	{
		return componentPostOnBeforeRenderListeners;
	}

	/**
	 * @return on after render listeners collection
	 */
	public final ComponentOnAfterRenderListenerCollection getComponentOnAfterRenderListeners()
	{
		return componentOnAfterRenderListeners;
	}

	/**
	 * @return the unmodifiable request list of {@link IRequestCycleListener}s in this application
	 */
	public RequestCycleListenerCollection getRequestCycleListeners()
	{
		return requestCycleListeners;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Settings
	//
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** Application settings */
	private ApplicationSettings applicationSettings;

	/** JavaScriptLibrary settings */
	private JavaScriptLibrarySettings javaScriptLibrarySettings;

	/** Debug Settings */
	private DebugSettings debugSettings;

	/** Exception Settings */
	private ExceptionSettings exceptionSettings;

	/** Framework Settings */
	private FrameworkSettings frameworkSettings;

	/** The Markup Settings */
	private MarkupSettings markupSettings;

	/** The Page Settings */
	private PageSettings pageSettings;

	/** The Request Cycle Settings */
	private RequestCycleSettings requestCycleSettings;

	/** The Request Logger Settings */
	private RequestLoggerSettings requestLoggerSettings;

	/** The Resource Settings */
	private ResourceSettings resourceSettings;

	/** The Security Settings */
	private SecuritySettings securitySettings;

	/** The settings for {@link IPageStore}, {@link IDataStore} and {@link IPageManager} */
	private StoreSettings storeSettings;

	/** can the settings object be set/used. */
	private boolean settingsAccessible;

	/**
	 * @return Application's application-wide settings
	 * @since 1.2
	 */
	public final ApplicationSettings getApplicationSettings()
	{
		checkSettingsAvailable();
		if (applicationSettings == null)
		{
			applicationSettings = new ApplicationSettings();
		}
		return applicationSettings;
	}

	/**
	 * 
	 * @param applicationSettings
	 */
	public final Application setApplicationSettings(final ApplicationSettings applicationSettings)
	{
		this.applicationSettings = applicationSettings;
		return this;
	}

	/**
	 * @return Application's JavaScriptLibrary settings
	 * @since 6.0
	 */
	public final JavaScriptLibrarySettings getJavaScriptLibrarySettings()
	{
		checkSettingsAvailable();
		if (javaScriptLibrarySettings == null)
		{
			javaScriptLibrarySettings = new JavaScriptLibrarySettings();
		}
		return javaScriptLibrarySettings;
	}

	/**
	 * 
	 * @param javaScriptLibrarySettings
	 */
	public final Application setJavaScriptLibrarySettings(
		final JavaScriptLibrarySettings javaScriptLibrarySettings)
	{
		this.javaScriptLibrarySettings = javaScriptLibrarySettings;
		return this;
	}

	/**
	 * @return Application's debug related settings
	 */
	public final DebugSettings getDebugSettings()
	{
		checkSettingsAvailable();
		if (debugSettings == null)
		{
			debugSettings = new DebugSettings();
		}
		return debugSettings;
	}

	/**
	 * 
	 * @param debugSettings
	 */
	public final Application setDebugSettings(final DebugSettings debugSettings)
	{
		this.debugSettings = debugSettings;
		return this;
	}

	/**
	 * @return Application's exception handling settings
	 */
	public final ExceptionSettings getExceptionSettings()
	{
		checkSettingsAvailable();
		if (exceptionSettings == null)
		{
			exceptionSettings = new ExceptionSettings();
		}
		return exceptionSettings;
	}

	/**
	 * 
	 * @param exceptionSettings
	 */
	public final Application setExceptionSettings(final ExceptionSettings exceptionSettings)
	{
		this.exceptionSettings = exceptionSettings;
		return this;
	}

	/**
	 * @return Wicket framework settings
	 */
	public final FrameworkSettings getFrameworkSettings()
	{
		checkSettingsAvailable();
		if (frameworkSettings == null)
		{
			frameworkSettings = new FrameworkSettings(this);
		}
		return frameworkSettings;
	}

	/**
	 * 
	 * @param frameworkSettings
	 */
	public final Application setFrameworkSettings(final FrameworkSettings frameworkSettings)
	{
		this.frameworkSettings = frameworkSettings;
		return this;
	}

	/**
	 * @return Application's page related settings
	 */
	public final PageSettings getPageSettings()
	{
		checkSettingsAvailable();
		if (pageSettings == null)
		{
			pageSettings = new PageSettings();
		}
		return pageSettings;
	}

	/**
	 * 
	 * @param pageSettings
	 */
	public final Application setPageSettings(final PageSettings pageSettings)
	{
		this.pageSettings = pageSettings;
		return this;
	}

	/**
	 * @return Application's request cycle related settings
	 */
	public final RequestCycleSettings getRequestCycleSettings()
	{
		checkSettingsAvailable();
		if (requestCycleSettings == null)
		{
			requestCycleSettings = new RequestCycleSettings();
		}
		return requestCycleSettings;
	}

	/**
	 * 
	 * @param requestCycleSettings
	 */
	public final Application setRequestCycleSettings(final RequestCycleSettings requestCycleSettings)
	{
		this.requestCycleSettings = requestCycleSettings;
		return this;
	}

	/**
	 * @return Application's markup related settings
	 */
	public MarkupSettings getMarkupSettings()
	{
		checkSettingsAvailable();
		if (markupSettings == null)
		{
			markupSettings = new MarkupSettings();
		}
		return markupSettings;
	}

	/**
	 * 
	 * @param markupSettings
	 */
	public final Application setMarkupSettings(final MarkupSettings markupSettings)
	{
		this.markupSettings = markupSettings;
		return this;
	}

	/**
	 * @return Application's request logger related settings
	 */
	public final RequestLoggerSettings getRequestLoggerSettings()
	{
		checkSettingsAvailable();
		if (requestLoggerSettings == null)
		{
			requestLoggerSettings = new RequestLoggerSettings();
		}
		return requestLoggerSettings;
	}

	/**
	 * 
	 * @param requestLoggerSettings
	 */
	public final Application setRequestLoggerSettings(final RequestLoggerSettings requestLoggerSettings)
	{
		this.requestLoggerSettings = requestLoggerSettings;
		return this;
	}

	/**
	 * @return Application's resources related settings
	 */
	public final ResourceSettings getResourceSettings()
	{
		checkSettingsAvailable();
		if (resourceSettings == null)
		{
			resourceSettings = new ResourceSettings(this);
		}
		return resourceSettings;
	}

	/**
	 * 
	 * @param resourceSettings
	 */
	public final Application setResourceSettings(final ResourceSettings resourceSettings)
	{
		this.resourceSettings = resourceSettings;
		return this;
	}

	/**
	 * @return Application's security related settings
	 */
	public final SecuritySettings getSecuritySettings()
	{
		checkSettingsAvailable();
		if (securitySettings == null)
		{
			securitySettings = new SecuritySettings();
		}
		return securitySettings;
	}

	/**
	 * 
	 * @param securitySettings
	 */
	public final Application setSecuritySettings(final SecuritySettings securitySettings)
	{
		this.securitySettings = securitySettings;
		return this;
	}

	/**
	 * @return Application's stores related settings
	 */
	public final StoreSettings getStoreSettings()
	{
		checkSettingsAvailable();
		if (storeSettings == null)
		{
			storeSettings = new StoreSettings(this);
		}
		return storeSettings;
	}

	/**
	 * 
	 * @param storeSettings
	 */
	public final Application setStoreSettings(final StoreSettings storeSettings)
	{
		this.storeSettings = storeSettings;
		return this;
	}

	/**
	 *
	 */
	private void checkSettingsAvailable()
	{
		if (!settingsAccessible)
		{
			throw new WicketRuntimeException(
				"Use Application.init() method for configuring your application object");
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Page Manager
	//
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private volatile IPageManager pageManager;
	private IPageManagerProvider pageManagerProvider;

	/**
	 * 
	 * @return PageManagerProvider
	 */
	public final IPageManagerProvider getPageManagerProvider()
	{
		return pageManagerProvider;
	}

	/**
	 * 
	 * @param provider
	 */
	public final Application setPageManagerProvider(final IPageManagerProvider provider)
	{
		pageManagerProvider = provider;
		return this;
	}

	/**
	 * Context for PageManager to interact with rest of Wicket
	 */
	private final IPageManagerContext pageManagerContext = new DefaultPageManagerContext();

	/**
	 * Returns an unsynchronized version of page manager
	 * 
	 * @return the page manager
	 */
	final IPageManager internalGetPageManager()
	{
		if (pageManager == null)
		{
			synchronized (this)
			{
				if (pageManager == null)
				{
					pageManager = pageManagerProvider.apply(getPageManagerContext());
				}
			}
		}
		return pageManager;
	}

	/**
	 * 
	 * @return the page manager context
	 */
	protected IPageManagerContext getPageManagerContext()
	{
		return pageManagerContext;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Page Rendering
	//
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 
	 * @return PageRendererProvider
	 */
	public final IPageRendererProvider getPageRendererProvider()
	{
		return pageRendererProvider;
	}

	/**
	 * 
	 * @param pageRendererProvider
	 */
	public final Application setPageRendererProvider(final IPageRendererProvider pageRendererProvider)
	{
		Args.notNull(pageRendererProvider, "pageRendererProvider");
		this.pageRendererProvider = pageRendererProvider;
		return this;
	}


	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Request Handler encoding
	//
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private ResourceReferenceRegistry resourceReferenceRegistry;

	private SharedResources sharedResources;

	private ResourceBundles resourceBundles;

	private IPageFactory pageFactory;

	private IMapperContext encoderContext;

	/**
	 * Override to create custom {@link ResourceReferenceRegistry}.
	 * 
	 * @return new {@link ResourceReferenceRegistry} instance.
	 */
	protected ResourceReferenceRegistry newResourceReferenceRegistry()
	{
		return new ResourceReferenceRegistry();
	}

	/**
	 * Returns {@link ResourceReferenceRegistry} for this application.
	 * 
	 * @return ResourceReferenceRegistry
	 */
	public final ResourceReferenceRegistry getResourceReferenceRegistry()
	{
		return resourceReferenceRegistry;
	}

	/**
	 * 
	 * @param registry
	 * @return SharedResources
	 */
	protected SharedResources newSharedResources(final ResourceReferenceRegistry registry)
	{
		return new SharedResources(registry);
	}

	/**
	 * 
	 * @return SharedResources
	 */
	public SharedResources getSharedResources()
	{
		return sharedResources;
	}

	protected ResourceBundles newResourceBundles(final ResourceReferenceRegistry registry)
	{
		return new ResourceBundles(registry);
	}

	/**
	 * @return The registry for resource bundles
	 */
	public ResourceBundles getResourceBundles()
	{
		return resourceBundles;
	}

	/**
	 * Override to create custom {@link IPageFactory}
	 * 
	 * @return new {@link IPageFactory} instance.
	 */
	protected IPageFactory newPageFactory()
	{
		return new DefaultPageFactory();
	}

	/**
	 * Returns {@link IPageFactory} for this application.
	 * 
	 * @return page factory
	 */
	public final IPageFactory getPageFactory()
	{
		return pageFactory;
	}

	/**
	 * 
	 * @return mapper context
	 */
	public final IMapperContext getMapperContext()
	{
		if (encoderContext == null)
		{
			encoderContext = newMapperContext();
		}
		return encoderContext;
	}

	/**
	 * Factory method for {@link IMapperContext} implementations. {@link DefaultMapperContext} may
	 * be a good starting point for custom implementations.
	 * 
	 * @return new instance of mapper context to be used in the application
	 */
	protected IMapperContext newMapperContext()
	{
		return new DefaultMapperContext(this);
	}

	/**
	 * 
	 * @param requestCycle
	 * @return Session
	 */
	public Session fetchCreateAndSetSession(final RequestCycle requestCycle)
	{
		Args.notNull(requestCycle, "requestCycle");

		Session session = getSessionStore().lookup(requestCycle.getRequest());
		if (session == null)
		{
			session = newSession(requestCycle.getRequest(), requestCycle.getResponse());
			ThreadContext.setSession(session);
			internalGetPageManager().newSessionCreated();
			sessionListeners.onCreated(session);
		}
		else
		{
			ThreadContext.setSession(session);
		}
		return session;
	}

	/**
	 * 
	 * @return RequestCycleProvider
	 */
	public final IRequestCycleProvider getRequestCycleProvider()
	{
		return requestCycleProvider;
	}

	/**
	 * 
	 * @param requestCycleProvider
	 */
	public final Application setRequestCycleProvider(final IRequestCycleProvider requestCycleProvider)
	{
		this.requestCycleProvider = requestCycleProvider;
		return this;
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @return request cycle
	 */
	public final RequestCycle createRequestCycle(final Request request, final Response response)
	{
		RequestCycleContext context = new RequestCycleContext(request, response,
			getRootRequestMapper(), getExceptionMapperProvider().get());

		RequestCycle requestCycle = getRequestCycleProvider().apply(context);
		requestCycle.getListeners().add(requestCycleListeners);
		requestCycle.getListeners().add(new IRequestCycleListener()
		{
			@Override
			public void onDetach(final RequestCycle requestCycle)
			{
				if (Session.exists())
				{
					Session.get().getPageManager().commitRequest();
				}

				if (Application.exists())
				{
					IRequestLogger requestLogger = Application.get().getRequestLogger();
					if (requestLogger != null)
					{
						requestLogger.requestTime((System.currentTimeMillis() - requestCycle.getStartTime()));
					}
				}
			}
		});
		return requestCycle;
	}

	/**
	 * Sets an {@link IHeaderResponseDecorator} that you want your application to use to decorate
	 * header responses.
	 * <p>
	 * Calling this method replaces the default decorator, which utilizes a {@link ResourceAggregator}:
	 * The given implementation should make sure, that it too wraps responses in a {@link ResourceAggregator},
	 * otherwise no dependencies for {@link HeaderItem}s will be resolved.   
	 * 
	 * @param headerResponseDecorator
	 *            your custom decorator, must not be null
	 */
	public final Application setHeaderResponseDecorator(final IHeaderResponseDecorator headerResponseDecorator)
	{
		Args.notNull(headerResponseDecorator, "headerResponseDecorator");
		
		this.headerResponseDecorator = headerResponseDecorator;
		return this;
	}

	/**
	 * INTERNAL METHOD - You shouldn't need to call this. This is called every time Wicket creates
	 * an IHeaderResponse. It gives you the ability to incrementally add features to an
	 * IHeaderResponse implementation by wrapping it in another implementation.
	 * 
	 * To decorate an IHeaderResponse in your application, set the {@link IHeaderResponseDecorator}
	 * on the application.
	 * 
	 * @see IHeaderResponseDecorator
	 * @param response
	 *            the response Wicket created
	 * @return the response Wicket should use in IHeaderContributor traversal
	 */
	public final IHeaderResponse decorateHeaderResponse(final IHeaderResponse response)
	{
		return headerResponseDecorator.decorate(response);
	}

	/**
	 * 
	 * @return true, of app is in Development mode
	 */
	public final boolean usesDevelopmentConfig()
	{
		return RuntimeConfigurationType.DEVELOPMENT.equals(getConfigurationType());
	}

	/**
	 * 
	 * @return true, of app is in Deployment mode
	 */
	public final boolean usesDeploymentConfig()
	{
		return RuntimeConfigurationType.DEPLOYMENT.equals(getConfigurationType());
	}
}
