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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.application.IComponentOnAfterRenderListener;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.javascript.DefaultJavascriptCompressor;
import org.apache.wicket.markup.MarkupCache;
import org.apache.wicket.markup.html.EmptySrcAttributeCheckFilter;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.image.resource.DefaultButtonImageResourceFactory;
import org.apache.wicket.markup.parser.filter.EnclosureHandler;
import org.apache.wicket.markup.parser.filter.RelativePathPrefixHandler;
import org.apache.wicket.markup.parser.filter.WicketLinkTagHandler;
import org.apache.wicket.markup.parser.filter.WicketMessageTagHandler;
import org.apache.wicket.markup.resolver.AutoComponentResolver;
import org.apache.wicket.markup.resolver.FragmentResolver;
import org.apache.wicket.markup.resolver.HtmlHeaderResolver;
import org.apache.wicket.markup.resolver.MarkupInheritanceResolver;
import org.apache.wicket.markup.resolver.WicketContainerResolver;
import org.apache.wicket.markup.resolver.WicketMessageResolver;
import org.apache.wicket.ng.DefaultExceptionMapper;
import org.apache.wicket.ng.ThreadContext;
import org.apache.wicket.ng.request.ICompoundRequestMapper;
import org.apache.wicket.ng.request.component.IRequestablePage;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.cycle.RequestCycleContext;
import org.apache.wicket.ng.request.mapper.IMapperContext;
import org.apache.wicket.ng.resource.ResourceReferenceRegistry;
import org.apache.wicket.pageStore.DefaultPageManagerContext;
import org.apache.wicket.pageStore.DefaultPageStore;
import org.apache.wicket.pageStore.DiskDataStore;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.pageStore.IPageManager;
import org.apache.wicket.pageStore.IPageManagerContext;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.PersistentPageManager;
import org.apache.wicket.protocol.http.DummyRequestLogger;
import org.apache.wicket.protocol.http.IRequestLogger;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.session.DefaultPageFactory;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.session.ISessionStore.UnboundListener;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.settings.IDebugSettings;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.settings.IFrameworkSettings;
import org.apache.wicket.settings.IMarkupSettings;
import org.apache.wicket.settings.IPageSettings;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.settings.IRequestLoggerSettings;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.settings.ISecuritySettings;
import org.apache.wicket.settings.ISessionSettings;
import org.apache.wicket.settings.Settings;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.convert.ConverterLocator;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.lang.PropertyResolver;
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
 * See {@link org.apache.wicket.ResourceReference} for more details.
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
public abstract class Application implements UnboundListener
{
	/** Configuration constant for the 2 types */
	public static final String CONFIGURATION = "configuration";

	/** Configuration type constant for getting the context path out of the web.xml */
	public static final String CONTEXTPATH = "contextpath";

	/** Configuration type constant for deployment */
	public static final String DEPLOYMENT = "deployment";

	/** Configuration type constant for development */
	public static final String DEVELOPMENT = "development";

	/**
	 * Applications keyed on the {@link #getApplicationKey()} so that they can be retrieved even
	 * without being in a request/ being set in the thread local (we need that e.g. for when we are
	 * in a destruction thread).
	 */
	private static final Map<String, Application> applicationKeyToApplication = new HashMap<String, Application>(
		1);

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(Application.class);

	/** */
	private List<IComponentOnBeforeRenderListener> componentPreOnBeforeRenderListeners;

	/** */
	private List<IComponentOnBeforeRenderListener> componentPostOnBeforeRenderListeners;

	/** */
	private List<IComponentOnAfterRenderListener> componentOnAfterRenderListeners;

	/** @deprecated will be removed in 1.5; see IHeaderRenderStrategy */
	@Deprecated
	private List<IHeaderContributor> renderHeadListeners;

	/** root mapper */
	private ICompoundRequestMapper rootRequestMapper;

	/** list of {@link IComponentInstantiationListener}s. */
	private IComponentInstantiationListener[] componentInstantiationListeners = new IComponentInstantiationListener[0];

	/** The converter locator instance. */
	private IConverterLocator converterLocator;

	/** list of initializers. */
	private final List<IInitializer> initializers = new ArrayList<IInitializer>();

	/** Application level meta data. */
	private MetaDataEntry<?>[] metaData;

	/** Name of application subclass. */
	private String name;

	/** Request logger instance. */
	private IRequestLogger requestLogger;

	/** The session facade. */
	private volatile ISessionStore sessionStore;

	/** Settings for this application. */
	private Settings settings;

	/** can the settings object be set/used. */
	private boolean settingsAccessible;

	/** page renderer provider */
	private IPageRendererProvider pageRendererProvider;

	/** request cycle provider */
	private IRequestCycleProvider requestCycleProvider;

	/** session store provider */
	private IProvider<ISessionStore> sessionStoreProvider;

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
	 * Assign this application to current thread. This method should never be called by framework
	 * clients.
	 */
	public void set()
	{
		ThreadContext.setApplication(this);
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
	public static Application get(String applicationKey)
	{
		Application application = applicationKeyToApplication.get(applicationKey);
		return application;
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
		addComponentInstantiationListener(new IComponentInstantiationListener()
		{
			/**
			 * @see org.apache.wicket.application.IComponentInstantiationListener#onInstantiation(org.apache.wicket.Component)
			 */
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
	 * Adds a component instantiation listener. This method should typically only be called during
	 * application startup; it is not thread safe.
	 * <p>
	 * Note: wicket does not guarantee the execution order of added listeners
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public final void addComponentInstantiationListener(
		final IComponentInstantiationListener listener)
	{
		if (listener == null)
		{
			throw new IllegalArgumentException("argument listener may not be null");
		}

		// if an instance of this listener is already present ignore this call
		for (int i = 0; i < componentInstantiationListeners.length; i++)
		{
			if (listener == componentInstantiationListeners[i])
			{
				return;
			}
		}

		final IComponentInstantiationListener[] newListeners = new IComponentInstantiationListener[componentInstantiationListeners.length + 1];
		System.arraycopy(componentInstantiationListeners, 0, newListeners, 0,
			componentInstantiationListeners.length);
		newListeners[componentInstantiationListeners.length] = listener;
		componentInstantiationListeners = newListeners;
	}

	/**
	 * Configures application settings to good defaults.
	 */
	public final void configure()
	{
		final String configurationType = getConfigurationType();

		// As long as this is public api the development and deployment mode
		// should counter act each other for all properties.
		if (DEVELOPMENT.equalsIgnoreCase(configurationType))
		{
			getResourceSettings().setResourcePollFrequency(Duration.ONE_SECOND);
			getDebugSettings().setComponentUseCheck(true);
			getMarkupSettings().setStripWicketTags(false);
			getExceptionSettings().setUnexpectedExceptionDisplay(
				IExceptionSettings.SHOW_EXCEPTION_PAGE);
			getDebugSettings().setAjaxDebugModeEnabled(true);
			getDebugSettings().setDevelopmentUtilitiesEnabled(true);
			// getDebugSettings().setOutputMarkupContainerClassName(true);
			getResourceSettings().setJavascriptCompressor(null);
			getRequestCycleSettings().addResponseFilter(EmptySrcAttributeCheckFilter.INSTANCE);
		}
		else if (DEPLOYMENT.equalsIgnoreCase(configurationType))
		{
			getResourceSettings().setResourcePollFrequency(null);
			getDebugSettings().setComponentUseCheck(false);
			getMarkupSettings().setStripWicketTags(true);
			getExceptionSettings().setUnexpectedExceptionDisplay(
				IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
			getDebugSettings().setAjaxDebugModeEnabled(false);
			getDebugSettings().setDevelopmentUtilitiesEnabled(false);
			getResourceSettings().setJavascriptCompressor(new DefaultJavascriptCompressor());
		}
		else
		{
			throw new IllegalArgumentException("Invalid configuration type: '" + configurationType +
				"'.  Must be \"development\" or \"deployment\".");
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
	 * @return Application's application-wide settings
	 * @see IApplicationSettings
	 * @since 1.2
	 */
	public IApplicationSettings getApplicationSettings()
	{
		return getSettings();
	}

	/**
	 * Gets the configuration mode to use for configuring the app, either {@link #DEVELOPMENT} or
	 * {@link #DEPLOYMENT}.
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
	 * <div style="bored-width:10px;border-style:solid;">
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
	public abstract String getConfigurationType();

	/**
	 * @return The converter locator for this application
	 */
	public final IConverterLocator getConverterLocator()
	{
		return converterLocator;
	}

	/**
	 * @return Application's debug related settings
	 * @see IDebugSettings
	 * @since 1.2
	 */
	public IDebugSettings getDebugSettings()
	{
		return getSettings();
	}

	/**
	 * @return Application's exception handling settings
	 * @see IExceptionSettings
	 * @since 1.2
	 */
	public IExceptionSettings getExceptionSettings()
	{
		return getSettings();
	}

	/**
	 * @return Wicket framework settings
	 * @see IFrameworkSettings
	 * @since 1.2
	 */
	public IFrameworkSettings getFrameworkSettings()
	{
		return getSettings();
	}

	/**
	 * Application subclasses must specify a home page class by implementing this abstract method.
	 * 
	 * @return Home page class for this application
	 */
	public abstract Class<? extends Page> getHomePage();

	/**
	 * @return Application's markup related settings
	 * @see IMarkupSettings
	 * @since 1.2
	 */
	public IMarkupSettings getMarkupSettings()
	{
		return getSettings();
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
	public final <T> T getMetaData(final MetaDataKey<T> key)
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
	 * @return Application's page related settings
	 * @see IPageSettings
	 * @since 1.2
	 */
	public IPageSettings getPageSettings()
	{
		return getSettings();
	}

	/**
	 * @return Application's request cycle related settings
	 * @see IDebugSettings
	 * @since 1.2
	 */
	public IRequestCycleSettings getRequestCycleSettings()
	{
		return getSettings();
	}

	/**
	 * Gets the {@link RequestLogger}.
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
	 * @return Application's resources related settings
	 * @see IResourceSettings
	 * @since 1.3
	 */
	public IRequestLoggerSettings getRequestLoggerSettings()
	{
		return getSettings();
	}

	/**
	 * @return Application's resources related settings
	 * @see IResourceSettings
	 * @since 1.2
	 */
	public IResourceSettings getResourceSettings()
	{
		return getSettings();
	}

	/**
	 * @return Application's security related settings
	 * @see ISecuritySettings
	 * @since 1.2
	 */
	public ISecuritySettings getSecuritySettings()
	{
		return getSettings();
	}

	/**
	 * @return Application's session related settings
	 * @see ISessionSettings
	 * @since 1.2
	 */
	public ISessionSettings getSessionSettings()
	{
		return getSettings();
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

	public void sessionUnbound(String sessionId)
	{
		getPageManager().sessionExpired(sessionId);
	}


	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * Initializes wicket components.
	 */
	public final void initializeComponents()
	{
		// Load any wicket properties files we can find
		try
		{
			// Load properties files used by all libraries

			final Iterator<URL> resources = getApplicationSettings().getClassResolver()
				.getResources("wicket.properties");
			while (resources.hasNext())
			{
				InputStream in = null;
				try
				{
					final URL url = resources.next();
					final Properties properties = new Properties();
					in = url.openStream();
					properties.load(in);
					load(properties);
				}
				finally
				{
					if (in != null)
					{
						in.close();
					}
				}
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Unable to load initializers file", e);
		}

		// now call any initializers we read
		callInitializers();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * @param target
	 */
	public void logEventTarget(IRequestHandler target)
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * @param requestTarget
	 */
	public void logResponseTarget(IRequestHandler requestTarget)
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
	 * Removes a component instantiation listener. This method should typicaly only be called during
	 * application startup; it is not thread safe.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public final void removeComponentInstantiationListener(
		final IComponentInstantiationListener listener)
	{
		final IComponentInstantiationListener[] listeners = componentInstantiationListeners;
		final int len = listeners.length;

		if (listener != null && len > 0)
		{
			int pos = 0;

			for (pos = 0; pos < len; pos++)
			{
				if (listener == listeners[pos])
				{
					break;
				}
			}

			if (pos < len)
			{
				listeners[pos] = listeners[len - 1];
				final IComponentInstantiationListener[] newListeners = new IComponentInstantiationListener[len - 1];
				System.arraycopy(listeners, 0, newListeners, 0, newListeners.length);

				componentInstantiationListeners = newListeners;
			}
		}
	}

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
	// TODO: Replace the Serializable type with Object for next wicket version
	public final synchronized <T> void setMetaData(final MetaDataKey<T> key, final Object object)
	{
		metaData = key.set(metaData, object);
	}

	/**
	 * Construct and add initializer from the provided class name.
	 * 
	 * @param className
	 */
	private final void addInitializer(String className)
	{
		IInitializer initializer = (IInitializer)Objects.newInstance(className);
		if (initializer != null)
		{
			initializers.add(initializer);
		}
	}

	/**
	 * Iterate initializers list, calling any {@link org.apache.wicket.IDestroyer} instances found
	 * in it.
	 */
	private final void callDestroyers()
	{
		for (Iterator<IInitializer> iter = initializers.iterator(); iter.hasNext();)
		{
			IInitializer initializer = iter.next();
			if (initializer instanceof IDestroyer)
			{
				log.info("[" + getName() + "] destroy: " + initializer);
				((IDestroyer)initializer).destroy(this);
			}
		}
	}

	/**
	 * Iterate initializers list, calling any instances found in it.
	 */
	private final void callInitializers()
	{
		for (Iterator<IInitializer> iter = initializers.iterator(); iter.hasNext();)
		{
			IInitializer initializer = iter.next();
			log.info("[" + getName() + "] init: " + initializer);
			initializer.init(this);
		}
	}

	/**
	 * This method is still here for backwards compatibility with 1.1 source code. The
	 * getXXXSettings() methods are now preferred. This method will be removed post 1.2 version.
	 * 
	 * @return Application settings
	 * 
	 * @see Application#getApplicationSettings()
	 * @see Application#getDebugSettings()
	 * @see Application#getExceptionSettings()
	 * @see Application#getMarkupSettings()
	 * @see Application#getPageSettings()
	 * @see Application#getRequestCycleSettings()
	 * @see Application#getResourceSettings()
	 * @see Application#getSecuritySettings()
	 * @see Application#getSessionSettings()
	 */
	private Settings getSettings()
	{
		if (!settingsAccessible)
		{
			throw new WicketRuntimeException(
				"Use Application.init() method for configuring your application object");
		}

		if (settings == null)
		{
			settings = new Settings(this);
		}
		return settings;
	}

	/**
	 * @param properties
	 *            Properties map with names of any library initializers in it
	 */
	private final void load(final Properties properties)
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
		// destroy detach listener
		final IDetachListener detachListener = getFrameworkSettings().getDetachListener();
		if (detachListener != null)
		{
			detachListener.onDestroyListener();
		}

		// Clear caches of Class keys so the classloader can be garbage
		// collected (WICKET-625)
		PropertyResolver.destroy(this);
		MarkupCache.get().shutdown();

		onDestroy();

		callDestroyers();
		applicationKeyToApplication.remove(getApplicationKey());

		pageManager.destroy();
		sessionStore.destroy();

		RequestContext.unset();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT OVERRIDE OR CALL.
	 * 
	 * Internal initialization.
	 */
	protected void internalInit()
	{
		settingsAccessible = true;
		IPageSettings pageSettings = getPageSettings();

		// Install default component resolvers
		pageSettings.addComponentResolver(new AutoComponentResolver());
		pageSettings.addComponentResolver(new MarkupInheritanceResolver());
		pageSettings.addComponentResolver(new HtmlHeaderResolver());
		pageSettings.addComponentResolver(new WicketLinkTagHandler());
		pageSettings.addComponentResolver(new WicketMessageResolver());
		pageSettings.addComponentResolver(new WicketMessageTagHandler());
		pageSettings.addComponentResolver(new FragmentResolver());
		pageSettings.addComponentResolver(new RelativePathPrefixHandler());
		pageSettings.addComponentResolver(new EnclosureHandler());
		pageSettings.addComponentResolver(new WicketContainerResolver());

		// Install button image resource factory
		getResourceSettings().addResourceFactory("buttonFactory",
			new DefaultButtonImageResourceFactory());

		String applicationKey = getApplicationKey();
		applicationKeyToApplication.put(applicationKey, this);

		converterLocator = newConverterLocator();

		setPageManagerProvider(new DefaultPageManagerProvider());
		resourceReferenceRegistry = newResourceReferenceRegistry();
		sharedResources = newSharedResources(resourceReferenceRegistry);

		// set up default request mapper
		setRootRequestMapper(new SystemMapper(getResourceSettings()));

		pageFactory = newPageFactory();

		requestCycleProvider = new DefaultRequestCycleProvider();
	}


	public final IProvider<ISessionStore> getSessionStoreProvider()
	{
		return sessionStoreProvider;
	}

	public final void setSessionStoreProvider(IProvider<ISessionStore> sessionStoreProvider)
	{
		this.sessionStoreProvider = sessionStoreProvider;
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
		return new DummyRequestLogger();
	}

	/**
	 * Notifies the registered component instantiation listeners of the construction of the provided
	 * component
	 * 
	 * @param component
	 *            the component that is being instantiated
	 */
	final void notifyComponentInstantiationListeners(final Component component)
	{
		final int len = componentInstantiationListeners.length;
		for (int i = 0; i < len; i++)
		{
			componentInstantiationListeners[i].onInstantiation(component);
		}
	}

	/**
	 * Adds an {@link IComponentOnBeforeRenderListener}. This method should typically only be called
	 * during application startup; it is not thread safe.
	 * 
	 * @param listener
	 */
	final public void addPreComponentOnBeforeRenderListener(
		final IComponentOnBeforeRenderListener listener)
	{
		if (componentPreOnBeforeRenderListeners == null)
		{
			componentPreOnBeforeRenderListeners = new ArrayList<IComponentOnBeforeRenderListener>();
		}

		if (componentPreOnBeforeRenderListeners.contains(listener) == false)
		{
			componentPreOnBeforeRenderListeners.add(listener);
		}
	}

	/**
	 * Removes an {@link IComponentOnBeforeRenderListener}.
	 * 
	 * @param listener
	 */
	final public void removePreComponentOnBeforeRenderListener(
		final IComponentOnBeforeRenderListener listener)
	{
		if (componentPreOnBeforeRenderListeners != null)
		{
			componentPreOnBeforeRenderListeners.remove(listener);
			if (componentPreOnBeforeRenderListeners.isEmpty())
			{
				componentPreOnBeforeRenderListeners = null;
			}
		}
	}

	/**
	 * Notifies the {@link IComponentOnBeforeRenderListener}s.
	 * 
	 * @param component
	 */
	final void notifyPreComponentOnBeforeRenderListeners(final Component component)
	{
		if (componentPreOnBeforeRenderListeners != null)
		{
			for (Iterator<IComponentOnBeforeRenderListener> iter = componentPreOnBeforeRenderListeners.iterator(); iter.hasNext();)
			{
				IComponentOnBeforeRenderListener listener = iter.next();
				listener.onBeforeRender(component);
			}
		}
	}

	/**
	 * Adds an {@link IComponentOnBeforeRenderListener}. This method should typically only be called
	 * during application startup; it is not thread safe.
	 * 
	 * @param listener
	 */
	final public void addPostComponentOnBeforeRenderListener(
		final IComponentOnBeforeRenderListener listener)
	{
		if (componentPostOnBeforeRenderListeners == null)
		{
			componentPostOnBeforeRenderListeners = new ArrayList<IComponentOnBeforeRenderListener>();
		}

		if (componentPostOnBeforeRenderListeners.contains(listener) == false)
		{
			componentPostOnBeforeRenderListeners.add(listener);
		}
	}

	/**
	 * Removes an {@link IComponentOnBeforeRenderListener}.
	 * 
	 * @param listener
	 */
	final public void removePostComponentOnBeforeRenderListener(
		final IComponentOnBeforeRenderListener listener)
	{
		if (componentPostOnBeforeRenderListeners != null)
		{
			componentPostOnBeforeRenderListeners.remove(listener);
			if (componentPostOnBeforeRenderListeners.isEmpty())
			{
				componentPostOnBeforeRenderListeners = null;
			}
		}
	}

	/**
	 * Notifies the {@link IComponentOnBeforeRenderListener}s.
	 * 
	 * @param component
	 */
	final void notifyPostComponentOnBeforeRenderListeners(final Component component)
	{
		if (componentPostOnBeforeRenderListeners != null)
		{
			for (Iterator<IComponentOnBeforeRenderListener> iter = componentPostOnBeforeRenderListeners.iterator(); iter.hasNext();)
			{
				IComponentOnBeforeRenderListener listener = iter.next();
				listener.onBeforeRender(component);
			}
		}
	}

	/**
	 * Adds an {@link IComponentOnAfterRenderListener}. This method should typically only be called
	 * during application startup; it is not thread safe.
	 * 
	 * @param listener
	 */
	final public void addComponentOnAfterRenderListener(
		final IComponentOnAfterRenderListener listener)
	{
		if (componentOnAfterRenderListeners == null)
		{
			componentOnAfterRenderListeners = new ArrayList<IComponentOnAfterRenderListener>();
		}

		if (componentOnAfterRenderListeners.contains(listener) == false)
		{
			componentOnAfterRenderListeners.add(listener);
		}
	}

	/**
	 * Removes an {@link IComponentOnAfterRenderListener}.
	 * 
	 * @param listener
	 */
	final public void removeComponentOnAfterRenderListener(
		final IComponentOnAfterRenderListener listener)
	{
		if (componentOnAfterRenderListeners != null)
		{
			componentOnAfterRenderListeners.remove(listener);
			if (componentOnAfterRenderListeners.isEmpty())
			{
				componentOnAfterRenderListeners = null;
			}
		}
	}

	/**
	 * Notifies the {@link IComponentOnAfterRenderListener}s.
	 * 
	 * @param component
	 */
	final void notifyComponentOnAfterRenderListeners(final Component component)
	{
		if (componentOnAfterRenderListeners != null)
		{
			for (Iterator<IComponentOnAfterRenderListener> iter = componentOnAfterRenderListeners.iterator(); iter.hasNext();)
			{
				IComponentOnAfterRenderListener listener = iter.next();
				listener.onAfterRender(component);
			}
		}
	}

	/**
	 * Adds a listener that will be invoked for every header response
	 * 
	 * @param listener
	 * @deprecated will be removed in 1.5; see IHeaderRenderStrategy
	 */
	@Deprecated
	public final void addRenderHeadListener(final IHeaderContributor listener)
	{
		if (renderHeadListeners == null)
		{
			renderHeadListeners = new ArrayList<IHeaderContributor>();
		}
		renderHeadListeners.add(listener);
	}

	/**
	 * 
	 * @param listener
	 * @deprecated will be removed in 1.5; see IHeaderRenderStrategy
	 */
	@Deprecated
	public void removeRenderHeadListener(final IHeaderContributor listener)
	{
		if (renderHeadListeners != null)
		{
			renderHeadListeners.remove(listener);
			if (renderHeadListeners.isEmpty())
			{
				renderHeadListeners = null;
			}
		}
	}

	/**
	 * INTERNAL
	 * 
	 * @param response
	 */
	public void notifyRenderHeadListener(final IHeaderResponse response)
	{
		if (renderHeadListeners != null)
		{
			for (Iterator<IHeaderContributor> iter = renderHeadListeners.iterator(); iter.hasNext();)
			{
				IHeaderContributor listener = iter.next();
				listener.renderHead(response);
			}
		}
	}

	/**
	 * @return The root request mapper
	 */
	public final ICompoundRequestMapper getRootRequestMapper()
	{
		return rootRequestMapper;
	}

	/**
	 * Sets the root request mapper
	 * 
	 * @param rootRequestMapper
	 */
	public final void setRootRequestMapper(final ICompoundRequestMapper rootRequestMapper)
	{
		this.rootRequestMapper = rootRequestMapper;
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


	public final IPageManagerProvider getPageManagerProvider()
	{
		return pageManagerProvider;
	}

	public final void setPageManagerProvider(IPageManagerProvider pageManagerProvider)
	{
		this.pageManagerProvider = pageManagerProvider;
	}

	/**
	 * Context for PageManager to interact with rest of Wicket
	 */
	private final IPageManagerContext pageManagerContext = new DefaultPageManagerContext();

	/**
	 * 
	 * @return the page manager
	 */
	public final IPageManager getPageManager()
	{
		if (pageManager == null)
		{
			synchronized (this)
			{
				if (pageManager == null)
				{
					pageManager = pageManagerProvider.get(getPageManagerContext());
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

	public final IPageRendererProvider getPageRendererProvider()
	{
		return pageRendererProvider;
	}

	public final void setPageRendererProvider(IPageRendererProvider pageRendererProvider)
	{
		Checks.argumentNotNull(pageRendererProvider, "pageRendererProvider");
		this.pageRendererProvider = pageRendererProvider;
	}


	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Request Handler encoding
	//
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	private ResourceReferenceRegistry resourceReferenceRegistry;

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
	 * @return
	 */
	public final ResourceReferenceRegistry getResourceReferenceRegistry()
	{
		return resourceReferenceRegistry;
	}

	private SharedResources sharedResources;

	protected SharedResources newSharedResources(ResourceReferenceRegistry registry)
	{
		return new SharedResources(registry);
	}

	public SharedResources getSharedResources()
	{
		return sharedResources;
	}

	private IPageFactory pageFactory;

	/**
	 * Override to create custom {@link PageFactory}
	 * 
	 * @return new {@link PageFactory} instance.
	 */
	protected IPageFactory newPageFactory()
	{
		return new DefaultPageFactory();
	}

	/**
	 * Returns {@link PageFactory} for this application.
	 * 
	 * @return
	 */
	public final IPageFactory getPageFactory()
	{
		return pageFactory;
	}

	private final IMapperContext encoderContext = new IMapperContext()
	{
		public String getBookmarkableIdentifier()
		{
			return "bookmarkable";
		}

		public String getNamespace()
		{
			return "wicket";
		}

		public String getPageIdentifier()
		{
			return "page";
		}

		public String getResourceIdentifier()
		{
			return "resource";
		}

		public ResourceReferenceRegistry getResourceReferenceRegistry()
		{
			return Application.this.getResourceReferenceRegistry();
		}

		public RequestListenerInterface requestListenerInterfaceFromString(String interfaceName)
		{
			return RequestListenerInterface.forName(interfaceName);
		}

		public String requestListenerInterfaceToString(RequestListenerInterface listenerInterface)
		{
			return listenerInterface.getName();
		}

		public IRequestablePage newPageInstance(Class<? extends IRequestablePage> pageClass,
			PageParameters pageParameters)
		{
			if (pageParameters == null)
			{
				return getPageFactory().newPage((Class<? extends Page>)pageClass);
			}
			else
			{
				return getPageFactory().newPage((Class<? extends Page>)pageClass, pageParameters);
			}
		}

		public IRequestablePage getPageInstance(int pageId)
		{
			return Page.getPage(pageId);
		}

		public Class<? extends IRequestablePage> getHomePageClass()
		{
			return Application.this.getHomePage();
		}
	};

	public final IMapperContext getEncoderContext()
	{
		return encoderContext;
	}

	public Session fetchCreateAndSetSession(RequestCycle requestCycle)
	{
		Checks.argumentNotNull(requestCycle, "requestCycle");

		Session session = getSessionStore().lookup(requestCycle.getRequest());
		if (session == null)
		{
			session = newSession(requestCycle.getRequest(), requestCycle.getResponse());
			ThreadContext.setSession(session);
			getPageManager().newSessionCreated();
		}
		else
		{
			ThreadContext.setSession(session);
		}
		return session;
	}


	public IRequestCycleProvider getRequestCycleProvider()
	{
		return requestCycleProvider;
	}

	public void setRequestCycleProvider(IRequestCycleProvider requestCycleProvider)
	{
		this.requestCycleProvider = requestCycleProvider;
	}

	private static class DefaultRequestCycleProvider implements IRequestCycleProvider
	{

		public RequestCycle get(RequestCycleContext context)
		{
			return new RequestCycle(context);
		}

	}


	public final RequestCycle createRequestCycle(Request request, Response response)
	{
		// FIXME exception mapper should come from elsewhere
		RequestCycleContext context = new RequestCycleContext(request, response,
			getRootRequestMapper(), new DefaultExceptionMapper());

		RequestCycle requestCycle = getRequestCycleProvider().get(context);

		return requestCycle;
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
		init();
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
	public final void setName(String name)
	{
		Checks.argumentNotEmpty(name, "name");

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
	public String getMimeType(String fileName)
	{
		return URLConnection.getFileNameMap().getContentTypeFor(fileName);
	}

	private class DefaultPageManagerProvider implements IPageManagerProvider
	{

		public IPageManager get(IPageManagerContext context)
		{
			int cacheSize = 40;
			int fileChannelPoolCapacity = 50;
			IDataStore dataStore = new DiskDataStore(getName(), 1000000, fileChannelPoolCapacity);
			IPageStore pageStore = new DefaultPageStore(getName(), dataStore, cacheSize);
			return new PersistentPageManager(getName(), pageStore, context);

		}

	}
}
