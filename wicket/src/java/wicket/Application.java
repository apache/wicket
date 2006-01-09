/*
 * $Id$
 * $Revision$ $Date$
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.MarkupCache;
import wicket.markup.html.image.resource.DefaultButtonImageResourceFactory;
import wicket.markup.resolver.AutoComponentResolver;
import wicket.markup.resolver.BodyOnLoadResolver;
import wicket.markup.resolver.HtmlHeaderResolver;
import wicket.markup.resolver.MarkupInheritanceResolver;
import wicket.markup.resolver.ParentResolver;
import wicket.markup.resolver.WicketLinkResolver;
import wicket.markup.resolver.WicketMessageResolver;
import wicket.settings.IApplicationSettings;
import wicket.settings.IDebugSettings;
import wicket.settings.IExceptionSettings;
import wicket.settings.IMarkupSettings;
import wicket.settings.IPageSettings;
import wicket.settings.IRequestCycleSettings;
import wicket.settings.IResourceSettings;
import wicket.settings.ISecuritySettings;
import wicket.settings.ISessionSettings;
import wicket.settings.Settings;
import wicket.util.file.IResourceFinder;
import wicket.util.lang.Classes;
import wicket.util.string.Strings;
import wicket.util.time.Duration;

/**
 * Base class for all Wicket applications. To create a Wicket application, you
 * generally should <i>not </i> directly subclass this class. Instead, you will
 * want to subclass some subclass of Application, like WebApplication, which is
 * appropriate for the protocol and markup type you are working with.
 * 
 * Application has the following interesting features / attributes:
 * <ul>
 * <li><b>Name </b>- The application's name, which is the same as its class
 * name.
 * 
 * <li><b>Shared Resources </b>- Resources added to an application with any of
 * the Application.addResource() methods have application-wide scope and can be
 * referenced using a logical scope and a name with the ResourceReference class.
 * resourceReferences can then be used by multiple components in the same
 * application without additional overhead (beyond the ResourceReference
 * instance held by each referee) and will yield a stable URL, permitting
 * efficient browser caching of the resource (even if the resource is
 * dynamically generated). Resources shared in this manner may also be
 * localized. See {@link wicket.ResourceReference} for more details.
 * 
 * <li><b>A Session Factory </b>- The Application subclass WebApplication
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
	/** thread local holder of the application object. */
	private static final ThreadLocal CURRENT = new ThreadLocal();

	/** log. */
	private static Log log = LogFactory.getLog(Application.class);

	/** Markup cache for this application */
	private final MarkupCache markupCache;

	/** Name of application subclass. */
	private final String name;


	/** Settings for application. */
	private Settings settings;

	/** Shared resources for the application */
	private final SharedResources sharedResources;

	/**
	 * Get application for current session.
	 * 
	 * @return The current application
	 */
	public static Application get()
	{
		Application application = (Application)CURRENT.get();
		if (application == null)
		{
			throw new WicketRuntimeException("there is not application attached to current thread "
					+ Thread.currentThread().getName());
		}
		return application;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * @param application
	 *            The current application or null for this thread
	 */
	public static void set(Application application)
	{
		CURRENT.set(application);
	}

	/**
	 * Constructor
	 */
	public Application()
	{
		// Create name from subclass
		this.name = Classes.name(getClass());

		// Construct markup cache fot this application
		this.markupCache = new MarkupCache(this);

		// Create shared resources repository
		this.sharedResources = new SharedResources(this);

		// Install default component resolvers
		getPageSettings().addComponentResolver(new ParentResolver());
		getPageSettings().addComponentResolver(new AutoComponentResolver());
		getPageSettings().addComponentResolver(new MarkupInheritanceResolver());
		getPageSettings().addComponentResolver(new HtmlHeaderResolver());
		getPageSettings().addComponentResolver(new BodyOnLoadResolver());
		getPageSettings().addComponentResolver(new WicketLinkResolver());
		getPageSettings().addComponentResolver(new WicketMessageResolver());

		// Install button image resource factory
		getResourceSettings().addResourceFactory("buttonFactory",
				new DefaultButtonImageResourceFactory());
	}
	
	/**
	 * @return home page class
	 */
	public abstract Class getHomePage();

	/**
	 * Configures application settings for a given configuration type.
	 * 
	 * @param configurationType
	 *            The configuration type. Must currently be either "development"
	 *            or "deployment". If the type is 'development', the classpath
	 *            is polled for changes
	 */
	public final void configure(final String configurationType)
	{
		configure(configurationType, (IResourceFinder)null);
	}

	/**
	 * Configures application settings for a given configuration type.
	 * 
	 * @param configurationType
	 *            The configuration type. Must currently be either "development"
	 *            or "deployment". If the type is 'development', the given
	 *            resourceFinder is polled for changes
	 * @param resourceFinder
	 *            Finder for looking up resources
	 * @see File#pathSeparator
	 */
	public final void configure(final String configurationType, final IResourceFinder resourceFinder)
	{
		if (resourceFinder != null)
		{
			getResourceSettings().setResourceFinder(resourceFinder);
		}
		if ("development".equalsIgnoreCase(configurationType))
		{
			getResourceSettings().setResourcePollFrequency(Duration.ONE_SECOND);
			getDebugSettings().setComponentUseCheck(true);
			getMarkupSettings().setStripWicketTags(false);
			getExceptionSettings().setUnexpectedExceptionDisplay(
					IExceptionSettings.SHOW_EXCEPTION_PAGE);
		}
		else if ("deployment".equalsIgnoreCase(configurationType))
		{
			getDebugSettings().setComponentUseCheck(false);
			getMarkupSettings().setStripWicketTags(true);
			getExceptionSettings().setUnexpectedExceptionDisplay(
					IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
		}
		else
		{
			throw new IllegalArgumentException(
					"Invalid configuration type.  Must be \"development\" or \"deployment\".");
		}
	}

	/**
	 * Convenience method that configures application settings for a given
	 * configuration type.
	 * 
	 * @param configurationType
	 *            The configuration type. Must currently be either "development"
	 *            or "deployment". If the type is 'development', the given
	 *            resourceFolder is polled for changes
	 * @param resourceFolder
	 *            Folder for polling resources
	 * @see File#pathSeparator
	 */
	public final void configure(final String configurationType, final String resourceFolder)
	{
		configure(configurationType);
		getResourceSettings().addResourceFolder(resourceFolder);
	}

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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * @return Returns the markup cache associated with the application
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
	 * @deprecated
	 */
	// TODO make private in post 1.2
	public Settings getSettings()
	{
		if (settings == null)
		{
			settings = createApplicationSettings();
		}
		return settings;
	}


	/**
	 * @return Returns the sharedResources.
	 */
	public final SharedResources getSharedResources()
	{
		return sharedResources;
	}

	/**
	 * @return Factory for creating sessions
	 */
	protected abstract ISessionFactory getSessionFactory();

	/**
	 * Allows for initialization of the application by a subclass.
	 */
	protected void init()
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT OVERRIDE OR
	 * CALL.
	 * 
	 * Internal intialization.
	 */
	protected void internalInit()
	{
		// We initialize components here rather than in the constructor because
		// the Application constructor is run before the Application subclass'
		// constructor and that subclass constructor may add class aliases that
		// would be used in installing resources in the component.
		initializeComponents();
	}

	private Settings createApplicationSettings()
	{
		return new Settings(this);
	}

	/**
	 * Instantiate initializer with the given class name
	 * 
	 * @param className
	 *            The name of the initializer class
	 */
	private void initialize(final String className)
	{
		if (!Strings.isEmpty(className))
		{
			try
			{
				Class c = getClass().getClassLoader().loadClass(className);
				((IInitializer)c.newInstance()).init(this);
			}
			catch (ClassCastException e)
			{
				throw new WicketRuntimeException("Unable to initialize " + className, e);
			}
			catch (ClassNotFoundException e)
			{
				throw new WicketRuntimeException("Unable to initialize " + className, e);
			}
			catch (InstantiationException e)
			{
				throw new WicketRuntimeException("Unable to initialize " + className, e);
			}
			catch (IllegalAccessException e)
			{
				throw new WicketRuntimeException("Unable to initialize " + className, e);
			}
		}
	}

	/**
	 * Initializes wicket components
	 */
	private final void initializeComponents()
	{
		// Load any wicket components we can find
		try
		{
			// Load components used by all applications
			for (Enumeration e = getClass().getClassLoader().getResources("wicket.properties"); e
					.hasMoreElements();)
			{
				InputStream is = null;
				try
				{
					final URL url = (URL)e.nextElement();
					final Properties properties = new Properties();
					is = url.openStream();
					properties.load(is);
					initializeComponents(properties);
				}
				finally
				{
					if (is != null)
					{
						is.close();
					}
				}
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Unable to load initializers file", e);
		}
	}

	/**
	 * @param properties
	 *            Properties table with names of any library initializers in it
	 */
	private void initializeComponents(final Properties properties)
	{
		initialize(properties.getProperty("initializer"));
		initialize(properties.getProperty(getName() + "-initializer"));
	}
}
