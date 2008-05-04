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
import org.apache.wicket.markup.IMarkupCache;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.image.resource.DefaultButtonImageResourceFactory;
import org.apache.wicket.markup.parser.filter.RelativePathPrefixHandler;
import org.apache.wicket.markup.parser.filter.WicketMessageTagHandler;
import org.apache.wicket.markup.resolver.AutoComponentResolver;
import org.apache.wicket.markup.resolver.BorderBodyResolver;
import org.apache.wicket.markup.resolver.EnclosureResolver;
import org.apache.wicket.markup.resolver.FragmentResolver;
import org.apache.wicket.markup.resolver.HtmlHeaderResolver;
import org.apache.wicket.markup.resolver.MarkupInheritanceResolver;
import org.apache.wicket.markup.resolver.ParentResolver;
import org.apache.wicket.markup.resolver.WicketContainerResolver;
import org.apache.wicket.markup.resolver.WicketLinkResolver;
import org.apache.wicket.markup.resolver.WicketMessageResolver;
import org.apache.wicket.protocol.http.IRequestLogger;
import org.apache.wicket.protocol.http.RequestLogger;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.session.ISessionStore;
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
import org.apache.wicket.util.convert.ConverterLocator;
import org.apache.wicket.util.lang.Classes;
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
public abstract class Application
{
	/** Configuration constant for the 2 types */
	public static final String CONFIGURATION = "configuration";

	/**
	 * Configuration type constant for getting the context path out of the web.xml
	 */
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

	/** Thread local holder of the application object. */
	private static final ThreadLocal<Application> current = new ThreadLocal<Application>();

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(Application.class);

	/** */
	private List<IComponentOnBeforeRenderListener> componentPreOnBeforeRenderListeners;

	/** */
	private List<IComponentOnBeforeRenderListener> componentPostOnBeforeRenderListeners;

	/** */
	private List<IComponentOnAfterRenderListener> componentOnAfterRenderListeners;

	/** */
	private List<IHeaderContributor> renderHeadListeners;

	/**
	 * Checks if the <code>Application</code> threadlocal is set in this thread
	 * 
	 * @return true if {@link Application#get()} can return the instance of application, false
	 *         otherwise
	 */
	public static boolean exists()
	{
		return current.get() != null;
	}

	/**
	 * Get Application for current thread.
	 * 
	 * @return The current thread's Application
	 */
	public static Application get()
	{
		final Application application = current.get();
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
	 * @return The application
	 * @throws IllegalArgumentException
	 *             When no application was found with the provided key
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * @param application
	 *            The current application or null for this thread
	 */
	public static void set(final Application application)
	{
		if (application == null)
		{
			throw new IllegalArgumentException("Argument application can not be null");
		}
		current.set(application);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 */
	public static void unset()
	{
		current.set(null);
	}

	/** list of {@link IComponentInstantiationListener}s. */
	private IComponentInstantiationListener[] componentInstantiationListeners = new IComponentInstantiationListener[0];

	/** The converter locator instance. */
	private IConverterLocator converterLocator;

	/** list of initializers. */
	private final List<IInitializer> initializers = new ArrayList<IInitializer>();

	/** Application level meta data. */
	private MetaDataEntry< ? >[] metaData;

	/** Name of application subclass. */
	private final String name;

	/** Request logger instance. */
	private IRequestLogger requestLogger;

	/** The session facade. */
	private ISessionStore sessionStore;

	/** Settings for this application. */
	private Settings settings;

	/** can the settings object be set/used. */
	private boolean settingsAccessible;

	/** Shared resources for this application */
	private final SharedResources sharedResources;

	/**
	 * Constructor. <strong>Use {@link #init()} for any configuration of your application instead of
	 * overriding the constructor.</strong>
	 */
	public Application()
	{
		// Create name from subclass
		name = Classes.simpleName(getClass());

		// Create shared resources repository
		sharedResources = new SharedResources(this);

		// Install default component instantiation listener that uses
		// authorization strategy to check component instantiations.
		addComponentInstantiationListener(new IComponentInstantiationListener()
		{
			/**
			 * @see org.apache.wicket.application.IComponentInstantiationListener#onInstantiation(org.apache.wicket.Component)
			 */
			public void onInstantiation(final Component< ? > component)
			{
				// If component instantiation is not authorized
				if (!Session.get().getAuthorizationStrategy().isInstantiationAuthorized(
					component.getClass()))
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
			getResourceSettings().setStripJavascriptCommentsAndWhitespace(false);
		}
		else if (DEPLOYMENT.equalsIgnoreCase(configurationType))
		{
			getResourceSettings().setResourcePollFrequency(null);
			getDebugSettings().setComponentUseCheck(false);
			getMarkupSettings().setStripWicketTags(true);
			getExceptionSettings().setUnexpectedExceptionDisplay(
				IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
			getDebugSettings().setAjaxDebugModeEnabled(false);
			getResourceSettings().setStripJavascriptCommentsAndWhitespace(true);
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
	public abstract Class< ? extends Page< ? >> getHomePage();

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * @return The markup cache associated with the application
	 * @deprecated please use {@link IMarkupSettings#getMarkupCache()} instead
	 */
	@Deprecated
	public final IMarkupCache getMarkupCache()
	{
		return getMarkupSettings().getMarkupCache();
	}

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
		return sessionStore;
	}

	/**
	 * Gets the shared resources.
	 * 
	 * @return The SharedResources for this application.
	 */
	public final SharedResources getSharedResources()
	{
		return sharedResources;
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
	public void logEventTarget(IRequestTarget target)
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * @param requestTarget
	 */
	public void logResponseTarget(IRequestTarget requestTarget)
	{
	}

	/**
	 * Creates a new RequestCycle object. Override this method if you want to provide a custom
	 * request cycle.
	 * 
	 * @param request
	 *            The request
	 * @param response
	 *            The response
	 * @return The request cycle
	 * 
	 * @since 1.3
	 */
	public abstract RequestCycle newRequestCycle(final Request request, final Response response);

	/**
	 * FOR DEPRECATION ONLY.
	 * 
	 * @param application
	 * @param request
	 * @param response
	 * @return nothing
	 * @throws UnsupportedOperationException
	 *             always
	 * @deprecated Applications wishing to provide custom request cycles should override method
	 *             {@link #newRequestCycle(Request, Response)}
	 */
	@Deprecated
	public final RequestCycle newRequestCycle(Application application, Request request,
		Response response)
	{
		throw new UnsupportedOperationException();
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
	 * 
	 * @deprecated use {@link #onDestroy()} instead
	 */
	// TODO remove after deprecation release
	@Deprecated
	protected final void destroy()
	{
	}

	/**
	 * Called when wicket servlet is destroyed. Overrides do not have to call super.
	 */
	protected void onDestroy()
	{
	}

	/**
	 * @return Request cycle factory for this kind of session.
	 * @deprecated replaced by {@link #newRequestCycle(Request, Response)}
	 */
	// TODO remove after deprecation release
	@Deprecated
	protected final Object getRequestCycleFactory()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the factory for creating session instances.
	 * 
	 * @return Factory for creating session instances
	 * @deprecated replaced by {@link #newSession(Request, Response)}
	 */
	// TODO remove after deprecation release
	@Deprecated
	protected final Object getSessionFactory()
	{
		throw new UnsupportedOperationException();
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
	protected void internalDestroy()
	{
		// Clear caches of Class keys so the classloader can be garbage
		// collected (WICKET-625)
		PropertyResolver.destroy(this);
		getMarkupSettings().getMarkupCache().shutdown();

		onDestroy();
		callDestroyers();
		applicationKeyToApplication.remove(getApplicationKey());
		Session.unset();
		RequestContext.unset();
		RequestCycle.set(null);
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
		pageSettings.addComponentResolver(new ParentResolver());
		pageSettings.addComponentResolver(new AutoComponentResolver());
		pageSettings.addComponentResolver(new MarkupInheritanceResolver());
		pageSettings.addComponentResolver(new HtmlHeaderResolver());
		pageSettings.addComponentResolver(new WicketLinkResolver());
		pageSettings.addComponentResolver(new WicketMessageResolver());
		pageSettings.addComponentResolver(new WicketMessageTagHandler());
		pageSettings.addComponentResolver(new FragmentResolver());
		pageSettings.addComponentResolver(new RelativePathPrefixHandler());
		pageSettings.addComponentResolver(new EnclosureResolver());
		pageSettings.addComponentResolver(new WicketContainerResolver());
		pageSettings.addComponentResolver(new BorderBodyResolver());

		// Install button image resource factory
		getResourceSettings().addResourceFactory("buttonFactory",
			new DefaultButtonImageResourceFactory());

		String applicationKey = getApplicationKey();
		applicationKeyToApplication.put(applicationKey, this);

		sessionStore = newSessionStore();
		converterLocator = newConverterLocator();
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
	 * Creates a new session facade. Is called once per application, and is typically not something
	 * clients reimplement.
	 * 
	 * @return The session facade
	 */
	protected abstract ISessionStore newSessionStore();

	/**
	 * Notifies the registered component instantiation listeners of the construction of the provided
	 * component
	 * 
	 * @param component
	 *            the component that is being instantiated
	 */
	final void notifyComponentInstantiationListeners(final Component< ? > component)
	{
		final int len = componentInstantiationListeners.length;
		for (int i = 0; i < len; i++)
		{
			componentInstantiationListeners[i].onInstantiation(component);
		}
	}

	/**
	 * Adds an {@link IComponentOnBeforeRenderListener}. This method should typically only be
	 * called during application startup; it is not thread safe.
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
	final void notifyPreComponentOnBeforeRenderListeners(final Component< ? > component)
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
	 * Adds an {@link IComponentOnBeforeRenderListener}. This method should typically only be
	 * called during application startup; it is not thread safe.
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
	final void notifyPostComponentOnBeforeRenderListeners(final Component< ? > component)
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
	final void notifyComponentOnAfterRenderListeners(final Component< ? > component)
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
	 */
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
	 */
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
}
