/*
 * $Id: Application.java 4893 2006-03-13 12:27:09 -0800 (Mon, 13 Mar 2006)
 * eelco12 $ $Revision$ $Date: 2006-03-13 12:27:09 -0800 (Mon, 13 Mar
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
package wicket;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.application.IComponentInstantiationListener;
import wicket.markup.MarkupCache;
import wicket.markup.html.image.resource.DefaultButtonImageResourceFactory;
import wicket.markup.resolver.AutoComponentResolver;
import wicket.markup.resolver.FragmentResolver;
import wicket.markup.resolver.HtmlHeaderResolver;
import wicket.markup.resolver.MarkupInheritanceResolver;
import wicket.markup.resolver.ParentResolver;
import wicket.markup.resolver.WicketLinkResolver;
import wicket.markup.resolver.WicketMessageResolver;
import wicket.session.ISessionStore;
import wicket.settings.IAjaxSettings;
import wicket.settings.IApplicationSettings;
import wicket.settings.IDebugSettings;
import wicket.settings.IExceptionSettings;
import wicket.settings.IFrameworkSettings;
import wicket.settings.IMarkupSettings;
import wicket.settings.IPageSettings;
import wicket.settings.IRequestCycleSettings;
import wicket.settings.IResourceSettings;
import wicket.settings.ISecuritySettings;
import wicket.settings.ISessionSettings;
import wicket.settings.Settings;
import wicket.util.file.IResourceFinder;
import wicket.util.lang.Classes;
import wicket.util.lang.Objects;
import wicket.util.time.Duration;

/**
 * Base class for all Wicket applications. To create a Wicket application, you
 * generally should <i>not </i> directly subclass this class. Instead, you will
 * want to subclass some subclass of Application, like WebApplication, which is
 * appropriate for the protocol and markup type you are working with.
 * <p>
 * Application has the following interesting features / attributes:
 * <ul>
 * <li><b>Name </b>- The Application's name, which is the same as its class
 * name.
 * 
 * <li><b>Home Page </b>- The Application's home Page class. Subclasses must
 * override getHomePage() to provide this property value.
 * 
 * <li><b>Settings </b>- Application settings are partitioned into sets of
 * related settings using interfaces in the wicket.settings package. These
 * interfaces are returned by the following methods, which should be used to
 * configure framework settings for your application: getApplicationSettings(),
 * getDebugSettings(), getExceptionSettings(), getMarkupSettings(),
 * getPageSettings(), getRequestCycleSettings(), getSecuritySettings and
 * getSessionSettings(). These settings are configured by default through the
 * constructor or internalInit methods. Default the application is configured
 * for DEVELOPMENT. You can configure this globally to DEPLOYMENT or override
 * specific settings by implementing the init() method.
 * 
 * <li><b>Shared Resources </b>- Resources added to an Application's
 * SharedResources have application-wide scope and can be referenced using a
 * logical scope and a name with the ResourceReference class. ResourceReferences
 * can then be used by multiple components in the same application without
 * additional overhead (beyond the ResourceReference instance held by each
 * referee) and will yield a stable URL, permitting efficient browser caching of
 * the resource (even if the resource is dynamically generated). Resources
 * shared in this manner may also be localized. See
 * {@link wicket.ResourceReference} for more details.
 * 
 * <li><b>Session Factory </b>- The Application subclass WebApplication
 * supplies an implementation of getSessionFactory() which returns an
 * implementation of ISessionFactory that creates WebSession Session objects
 * appropriate for web applications. You can (and probably will want to)
 * override getSessionFactory() to provide your own session factory that creates
 * Session instances of your own application-specific subclass of WebSession.
 * 
 * </ul>
 * 
 * @see wicket.protocol.http.WebApplication
 * @author Jonathan Locke
 */
public abstract class Application
{
	/** Configuration constant for the 2 types */
	public static final String CONFIGURATION = "configuration";

	/**
	 * Configuration type constant for getting the context path out of the
	 * web.xml
	 */
	public static final String CONTEXTPATH = "contextpath";

	/** Configuration type constant for deployment */
	public static final String DEPLOYMENT = "deployment";

	/** Configuration type constant for development */
	public static final String DEVELOPMENT = "development";

	/**
	 * Applications keyed on the {@link #getApplicationKey()} so that they can
	 * be retrieved even without being in a request/ being set in the thread
	 * local (we need that e.g. for when we are in a destruction thread).
	 */
	private static final Map applicationKeyToApplication = new HashMap(1);

	/** Thread local holder of the application object. */
	private static final ThreadLocal current = new ThreadLocal();

	/** Log. */
	private static final Log log = LogFactory.getLog(Application.class);

	/**
	 * Checks if the <code>Application</code> threadlocal is set in this
	 * thread
	 * 
	 * @return true if {@link Application#get()} can return the instance of
	 *         application, false otherwise
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
		final Application application = (Application)current.get();
		if (application == null)
		{
			throw new WicketRuntimeException("There is no application attached to current thread "
					+ Thread.currentThread().getName());
		}
		return application;
	}

	/**
	 * Gets the Application based on the application key of that application.
	 * THIS METHOD IS NOT MEANT INTENDED FOR FRAMEWORK CLIENTS.
	 * 
	 * @param applicationKey
	 *            The unique key of the application within a certain context
	 *            (e.g. a web application)
	 * @return The application
	 * @throws IllegalArgumentException
	 *             When no application was found with the provided key
	 */
	public static Application get(String applicationKey)
	{
		Application application = (Application)applicationKeyToApplication.get(applicationKey);
		return application;
	}

	/**
	 * @return True if the current thread is attached to an application.
	 */
	public static boolean isAttached()
	{
		return current.get() != null;
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

	/** Record what the configuration is, so that we can query for it later. */
	private String configurationType;

	/** list of initializers. */
	private List initializers = new ArrayList();

	/** Markup cache for this application */
	private final MarkupCache markupCache;

	/** Application level meta data. */
	private MetaDataEntry[] metaData;

	/** Name of application subclass. */
	private final String name;

	/** The session facade. */
	private ISessionStore sessionStore;

	/** Settings for this application. */
	private Settings settings;

	/** can the settings object be set/used. */
	private boolean settingsAccessible;

	/** Shared resources for this application */
	private final SharedResources sharedResources;

	/**
	 * Constructor. <strong>Use {@link #init()} for any configuration of your
	 * application instead of overriding the constructor.</strong>
	 */
	public Application()
	{
		// Create name from subclass
		this.name = Classes.simpleName(getClass());

		// Construct markup cache for this application
		this.markupCache = new MarkupCache(this);

		// Create shared resources repository
		this.sharedResources = new SharedResources(this);

		// Install default component instantiation listener that uses
		// authorization strategy to check component instantiations.
		addComponentInstantiationListener(new IComponentInstantiationListener()
		{
			/**
			 * @see wicket.application.IComponentInstantiationListener#onInstantiation(wicket.Component)
			 */
			public void onInstantiation(final Component component)
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
	 * Adds a component instantiation listener. This method should typicaly only
	 * be called during application startup; it is not thread safe.
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
	 * Convenience method that sets application settings to good defaults for
	 * the given configuration type (either DEVELOPMENT or DEPLOYMENT).
	 * 
	 * @param configurationType
	 *            The configuration type (either DEVELOPMENT or DEPLOYMENT)
	 * @see wicket.Application#configure(String, IResourceFinder)
	 */
	public final void configure(final String configurationType)
	{
		configure(configurationType, (IResourceFinder)null);
	}

	/**
	 * Configures application settings to good defaults for the given
	 * configuration type (either DEVELOPMENT or DEPLOYMENT).
	 * 
	 * @param configurationType
	 *            The configuration type. Must currently be either DEVELOPMENT
	 *            or DEPLOYMENT. Currently, if the configuration type is
	 *            DEVELOPMENT, resources are polled for changes, component usage
	 *            is checked, wicket tags are not stripped from ouput and a
	 *            detailed exception page is used. If the type is DEPLOYMENT,
	 *            component usage is not checked, wicket tags are stripped from
	 *            output and a non-detailed exception page is used to display
	 *            errors.
	 * @param resourceFinder
	 *            Resource finder for looking up resources
	 */
	public final void configure(final String configurationType, final IResourceFinder resourceFinder)
	{
		this.configurationType = configurationType;

		if (resourceFinder != null)
		{
			getResourceSettings().setResourceFinder(resourceFinder);
		}
		// As long as this is public api the developermenat and deployment mode
		// should counter act each other for all properties.
		if (DEVELOPMENT.equalsIgnoreCase(configurationType))
		{
			getResourceSettings().setResourcePollFrequency(Duration.ONE_SECOND);
			getDebugSettings().setComponentUseCheck(true);
			getDebugSettings().setSerializeSessionAttributes(true);
			getMarkupSettings().setStripWicketTags(false);
			getExceptionSettings().setUnexpectedExceptionDisplay(
					IExceptionSettings.SHOW_EXCEPTION_PAGE);
			getAjaxSettings().setAjaxDebugModeEnabled(true);
		}
		else if (DEPLOYMENT.equalsIgnoreCase(configurationType))
		{
			getResourceSettings().setResourcePollFrequency(null);
			getDebugSettings().setComponentUseCheck(false);
			getDebugSettings().setSerializeSessionAttributes(false);
			getMarkupSettings().setStripWicketTags(true);
			getExceptionSettings().setUnexpectedExceptionDisplay(
					IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
			getAjaxSettings().setAjaxDebugModeEnabled(false);
		}
		else
		{
			throw new IllegalArgumentException(
					"Invalid configuration type.  Must be \"development\" or \"deployment\".");
		}
	}

	/**
	 * Convenience method that sets application settings to good defaults for
	 * the given configuration type (either DEVELOPMENT or DEPLOYMENT).
	 * 
	 * @param configurationType
	 *            The configuration type (either DEVELOPMENT or DEPLOYMENT)
	 * @param resourceFolder
	 *            Folder for polling resources
	 */
	public final void configure(final String configurationType, final String resourceFolder)
	{
		configure(configurationType);
		if (resourceFolder != null)
		{
			getResourceSettings().addResourceFolder(resourceFolder);
		}
	}

	/**
	 * @return Application's ajax related settings
	 * @see IAjaxSettings
	 * @since 1.2
	 * @deprecated use {@link #getDebugSettings()} instead
	 */
	public final IAjaxSettings getAjaxSettings()
	{
		return getSettings();
	}

	/**
	 * Gets the unique key of this application within a given context (like a
	 * web application). NOT INTENDED FOR FRAMEWORK CLIENTS.
	 * 
	 * @return The unique key of this application
	 */
	public abstract String getApplicationKey();

	/**
	 * @return Application's application-wide settings
	 * @see IApplicationSettings
	 * @since 1.2
	 */
	public final IApplicationSettings getApplicationSettings()
	{
		return getSettings();
	}

	/**
	 * Gets the configuration mode that is currently set, either
	 * {@link #DEVELOPMENT} or {@link #DEPLOYMENT}.
	 * 
	 * @return configuration
	 * @since 1.2.3
	 */
	public String getConfigurationType()
	{
		return configurationType;
	}

	/**
	 * @return Application's debug related settings
	 * @see IDebugSettings
	 * @since 1.2
	 */
	public final IDebugSettings getDebugSettings()
	{
		return getSettings();
	}

	/**
	 * @return Application's exception handling settings
	 * @see IExceptionSettings
	 * @since 1.2
	 */
	public final IExceptionSettings getExceptionSettings()
	{
		return getSettings();
	}

	/**
	 * @return Wicket framework settings
	 * @see IFrameworkSettings
	 * @since 1.2
	 */
	public final IFrameworkSettings getFrameworkSettings()
	{
		return getSettings();
	}

	/**
	 * Application subclasses must specify a home page class by implementing
	 * this abstract method.
	 * 
	 * @return Home page class for this application
	 */
	public abstract Class getHomePage();

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * @return The markup cache associated with the application
	 */
	public final MarkupCache getMarkupCache()
	{
		return this.markupCache;
	}

	/**
	 * @return Application's markup related settings
	 * @see IMarkupSettings
	 * @since 1.2
	 */
	public final IMarkupSettings getMarkupSettings()
	{
		return getSettings();
	}

	/**
	 * Gets metadata for this application using the given key.
	 * 
	 * @param key
	 *            The key for the data
	 * @return The metadata
	 * @see MetaDataKey
	 */
	public final Serializable getMetaData(final MetaDataKey key)
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
	public final IPageSettings getPageSettings()
	{
		return getSettings();
	}

	/**
	 * @return Application's request cycle related settings
	 * @see IDebugSettings
	 * @since 1.2
	 */
	public final IRequestCycleSettings getRequestCycleSettings()
	{
		return getSettings();
	}

	/**
	 * @return Application's resources related settings
	 * @see IResourceSettings
	 * @since 1.2
	 */
	public final IResourceSettings getResourceSettings()
	{
		return getSettings();
	}

	/**
	 * @return Application's security related settings
	 * @see ISecuritySettings
	 * @since 1.2
	 */
	public final ISecuritySettings getSecuritySettings()
	{
		return getSettings();
	}

	/**
	 * @return Application's session related settings
	 * @see ISessionSettings
	 * @since 1.2
	 */
	public final ISessionSettings getSessionSettings()
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
	 * This method is still here for backwards compatibility with 1.1 source
	 * code. The getXXXSettings() methods are now preferred. This method will be
	 * removed post 1.2 version.
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
	 * @deprecated will be made private after 1.2
	 */
	// TODO Post 1.2: Make private
	public Settings getSettings()
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
			final Enumeration resources = getClass().getClassLoader().getResources(
					"wicket.properties");
			while (resources.hasMoreElements())
			{
				InputStream in = null;
				try
				{
					final URL url = (URL)resources.nextElement();
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
	 * Removes a component instantiation listener. This method should typicaly
	 * only be called during application startup; it is not thread safe.
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
	 * Sets the metadata for this application using the given key. If the
	 * metadata object is not of the correct type for the metadata key, an
	 * IllegalArgumentException will be thrown. For information on creating
	 * MetaDataKeys, see {@link MetaDataKey}.
	 * 
	 * @param key
	 *            The singleton key for the metadata
	 * @param object
	 *            The metadata object
	 * @throws IllegalArgumentException
	 * @see MetaDataKey
	 */
	public final synchronized void setMetaData(final MetaDataKey key, final Serializable object)
	{
		metaData = key.set(metaData, object);
	}

	/**
	 * Called when wicket servlet is destroyed. Overrides do not have to call
	 * super.
	 */
	protected void destroy()
	{
		callDestroyers();
	}

	/**
	 * Gets the factory for creating session instances.
	 * 
	 * @return Factory for creating session instances
	 */
	protected abstract ISessionFactory getSessionFactory();

	/**
	 * Allows for initialization of the application by a subclass. <strong>Use
	 * this method for any application setup instead of the constructor.</strong>
	 */
	protected void init()
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 */
	protected void internalDestroy()
	{
		destroy();
		applicationKeyToApplication.remove(getApplicationKey());
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT OVERRIDE OR
	 * CALL.
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
		pageSettings.addComponentResolver(new FragmentResolver());

		// Install button image resource factory
		getResourceSettings().addResourceFactory("buttonFactory",
				new DefaultButtonImageResourceFactory());

		String applicationKey = getApplicationKey();
		applicationKeyToApplication.put(applicationKey, this);

		sessionStore = newSessionStore();
	}

	/**
	 * Creates a new session facade. Is called once per application, and is
	 * typically not something clients reimplement.
	 * 
	 * @return The session facade
	 */
	protected abstract ISessionStore newSessionStore();

	/**
	 * Notifies the registered component instantiation listeners of the
	 * construction of the provided component
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
	 * @param properties
	 *            Properties map with names of any library destroyers in it
	 */
	private final void callDestroyers()
	{
		for (Iterator i = initializers.iterator(); i.hasNext();)
		{
			IInitializer initializer = (IInitializer)i.next();
			if (initializer instanceof IDestroyer)
			{
				log.info("[" + getName() + "] destroy: " + initializer);
				((IDestroyer)initializer).destroy(this);
			}
		}
	}

	/**
	 * @param properties
	 *            Properties map with names of any library destroyers in it
	 */
	private final void callInitializers()
	{
		for (Iterator i = initializers.iterator(); i.hasNext();)
		{
			IInitializer initializer = (IInitializer)i.next();
			log.info("[" + getName() + "] init: " + initializer);
			initializer.init(this);
		}
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
}
