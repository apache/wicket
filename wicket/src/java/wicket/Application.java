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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import wicket.markup.MarkupInheritanceResolver;
import wicket.markup.MarkupParser;
import wicket.markup.html.form.encryption.ICrypt;
import wicket.markup.html.image.resource.DefaultButtonImageResourceFactory;
import wicket.markup.parser.XmlPullParser;
import wicket.model.IModel;
import wicket.util.convert.ConverterFactory;
import wicket.util.convert.IConverterFactory;
import wicket.util.lang.Classes;
import wicket.util.resource.locator.DefaultResourceStreamLocator;
import wicket.util.resource.locator.ResourceStreamLocator;
import wicket.util.time.Duration;
import wicket.util.watch.ModificationWatcher;

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
 * <li><b>Application Settings </b>- A variety of settings that control the
 * behavior of the Wicket framework for a given application. It is not necessary
 * to learn all the settings. Good defaults can be set for deployment and
 * development by calling ApplicationSettings.configure("deployment") or
 * ApplicationSettings.configure("development").
 * 
 * <li><b>Application Pages </b>- A particular set of required pages. The
 * required pages returned by getPages() include a home page and pages for
 * handling common error conditions. The only page you must supply to create a
 * Wicket application is the application home page.
 * 
 * <li><b>Shared Resources </b>- Resources added to an application with any of
 * the Application.addResource() methods have application-wide scope and can be
 * referenced using a logical scope and a name with the ResourceReference class.
 * resourceReferences can then be used by multiple components in the same
 * application without additional overhead (beyond the ResourceReference
 * instance held by each referee) and will yield a stable URL, permitting
 * efficient browser caching of the resource (even if the resource is
 * dynamically generated). Resources shared in this manner may also be
 * localized. See {@link wicket.ResourceReference}for more details.
 * 
 * <li><b>A Converter Factory </b>- By overriding getConverterFactory(), you
 * can provide your own factory which creates locale sensitive Converter
 * instances.
 * 
 * <li><b>A ResourceStreamLocator </b>- An Application's ResourceStreamLocator
 * is used to find resources such as images or markup files. You can supply your
 * own ResourceStreamLocator if your prefer to store your applicaiton's
 * resources in a non-standard location (such as a different filesystem
 * location, a particular JAR file or even a database) by overriding the
 * getResourceLocator() method.
 * 
 * <li><b>Resource Factories </b>- Resource factories can be used to create
 * resources dynamically from specially formatted HTML tag attribute values. For
 * more details, see {@link IResourceFactory},
 * {@link wicket.markup.html.image.resource.DefaultButtonImageResourceFactory}
 * and especially
 * {@link wicket.markup.html.image.resource.LocalizedImageResource}.
 * 
 * <li><b>A Localizer </b>- The getLocalizer() method returns an object
 * encapsulating all of the functionality required to access localized
 * resources. For many localization problems, even this will not be required, as
 * there are convenience methods available to all components:
 * {@link wicket.Component#getString(String key)}and
 * {@link wicket.Component#getString(String key, IModel model)}.
 * 
 * <li><b>A Session Factory </b>- The Application subclass WebApplication
 * supplies an implementation of getSessionFactory() which returns an
 * implementation of ISessionFactory that creates WebSession Session objects
 * appropriate for web applications. You can (and probably will want to)
 * override getSessionFactory() to provide your own session factory that creates
 * Session instances of your own application-specific subclass of WebSession.
 * 
 * <li><b>A Page Sets Factory </b>- Page sets are an experimental feature which
 * will not be finished until Wicket 1.1.
 * </ul>
 * 
 * @see wicket.protocol.http.WebApplication
 * @author Jonathan Locke
 */
public abstract class Application
{
	/** List of (static) ComponentResolvers */
	private List componentResolvers = new ArrayList();

	/**
	 * Factory for the converter instance; default to the non localized factory
	 * {@link ConverterFactory}.
	 */
	private IConverterFactory converterFactory = new ConverterFactory();

	/** The single application-wide localization class */
	private final Localizer localizer;

	/** Name of application subclass. */
	private final String name;

	/** Map to look up resource factories by name */
	private final Map nameToResourceFactory = new HashMap();

	/** Pages for application */
	private final ApplicationPages pages = new ApplicationPages();

	/** The default resource locator for this application */
	private ResourceStreamLocator resourceStreamLocator;

	/** Map of shared resources */
	private final Map resourceMap = new HashMap();

	/** ModificationWatcher to watch for changes in markup files */
	private ModificationWatcher resourceWatcher;

	/** Settings for application. */
	private final ApplicationSettings settings = new ApplicationSettings(this);

	/**
	 * Constructor
	 */
	public Application()
	{
		// Create name from subclass
		this.name = Classes.name(getClass());

		// Construct localizer for this application
		this.localizer = new Localizer(this);

		// Install default component resolvers
		componentResolvers.add(new AutoComponentResolver());
		componentResolvers.add(new MarkupInheritanceResolver());

		// Install button image resource factory
		addResourceFactory("buttonFactory", new DefaultButtonImageResourceFactory());
	}

	/**
	 * @param scope
	 *            Scope of resource
	 * @param name
	 *            Logical name of resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The resource style
	 * @param resource
	 *            Resource to store
	 */
	public void addResource(final Class scope, final String name, final Locale locale,
			final String style, final Resource resource)
	{
		// Save resource
		final String key = scope.getName() + "_" + name
				+ (locale == null ? "" : "_" + locale.toString())
				+ (style == null ? "" : "_" + style);
		resourceMap.put(key, resource);
	}

	/**
	 * @param name
	 *            Logical name of resource
	 * @param locale
	 *            The locale of the resource
	 * @param resource
	 *            Resource to store
	 */
	public void addResource(final String name, final Locale locale, final Resource resource)
	{
		addResource(Application.class, name, locale, null, resource);
	}

	/**
	 * @param name
	 *            Logical name of resource
	 * @param resource
	 *            Resource to store
	 */
	public void addResource(final String name, final Resource resource)
	{
		addResource(Application.class, name, null, null, resource);
	}

	/**
	 * Adds a resource factory to the list of factories to consult when
	 * generating resources automatically
	 * 
	 * @param name
	 *            The name to give to the factory
	 * @param resourceFactory
	 *            The resource factory to add
	 */
	public void addResourceFactory(final String name, final IResourceFactory resourceFactory)
	{
		nameToResourceFactory.put(name, resourceFactory);
	}

	/**
	 * Get the (modifiable) list of IComponentResolvers.
	 * 
	 * @see AutoComponentResolver for an example
	 * @return List of ComponentResolvers
	 */
	public final List getComponentResolvers()
	{
		return componentResolvers;
	}

	/**
	 * Gets the converter factory.
	 * 
	 * @return the converter factory
	 */
	public IConverterFactory getConverterFactory()
	{
		return converterFactory;
	}

	/**
	 * Get instance of de-/encryption class.
	 * 
	 * @return instance of de-/encryption class
	 */
	public ICrypt getCrypt()
	{
		try
		{
			final ICrypt crypt = (ICrypt)getSettings().getCryptClass().newInstance();
			crypt.setKey(getSettings().getEncryptionKey());
			return crypt;
		}
		catch (InstantiationException e)
		{
			throw new WicketRuntimeException(
					"Encryption/decryption object can not be instantiated", e);
		}
		catch (IllegalAccessException e)
		{
			throw new WicketRuntimeException(
					"Encryption/decryption object can not be instantiated", e);
		}
	}

	/**
	 * @return The application wide localizer instance
	 */
	public Localizer getLocalizer()
	{
		return localizer;
	}

	/**
	 * Get and initialize a markup parser.
	 * 
	 * @return A new MarkupParser
	 */
	public final MarkupParser getMarkupParser()
	{
		final MarkupParser parser = new MarkupParser(new XmlPullParser());
		parser.configure(getSettings());
		return parser;
	}

	/**
	 * Gets the name of this application.
	 * 
	 * @return The application name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return Application's common pages
	 */
	public ApplicationPages getPages()
	{
		return pages;
	}

	/**
	 * THIS FEATURE IS CURRENTLY EXPERIMENTAL. DO NOT USE THIS METHOD.
	 * 
	 * @param page
	 *            The Page for which a list of PageSets should be retrieved
	 * @return Sequence of PageSets for a given Page
	 */
	public Iterator getPageSets(final Page page)
	{
		return new Iterator()
		{

			public boolean hasNext()
			{
				return false;
			}

			public Object next()
			{
				return null;
			}

			public void remove()
			{
			}
		};
	}

	/**
	 * @param key
	 *            Shared resource key
	 * @return The resource
	 */
	public final Resource getResource(final String key)
	{
		return (Resource)resourceMap.get(key);
	}

	/**
	 * @param scope
	 *            The resource's scope
	 * @param name
	 *            Name of resource to get
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The resource style
	 * @return The logical resource
	 */
	public Resource getResource(final Class scope, final String name, final Locale locale,
			final String style)
	{
		// Base name for resource
		final String baseName = scope.getName() + "_" + name;

		// 1. Look for fully qualified entry with locale and style
		if (locale != null && style != null)
		{
			final String key = baseName + "_" + locale + "_" + style;
			final Resource resource = getResource(key);
			if (resource != null)
			{
				return resource;
			}
		}

		// 2. Look for entry without style
		if (locale != null)
		{
			final String key = baseName + "_" + locale;
			final Resource resource = getResource(key);
			if (resource != null)
			{
				return resource;
			}
		}

		// 3. Look for entry without locale
		if (style != null)
		{
			final String key = baseName + "_" + style;
			final Resource resource = getResource(key);
			if (resource != null)
			{
				return resource;
			}
		}

		// 4. Look for base name
		return getResource(baseName);
	}

	/**
	 * @param name
	 *            Name of the factory to get
	 * @return The IResourceFactory with the given name.
	 */
	public IResourceFactory getResourceFactory(final String name)
	{
		return (IResourceFactory)nameToResourceFactory.get(name);
	}

	/**
	 * @return Resource locator for this application
	 */
	public ResourceStreamLocator getResourceStreamLocator()
	{
		if (resourceStreamLocator == null)
		{
			// Create compound resource locator using source path from
			// application settings
			resourceStreamLocator = new DefaultResourceStreamLocator(getSettings().getSourcePath());
		}
		return resourceStreamLocator;
	}

	/**
	 * @return Resource watcher with polling frequency determined by setting, or
	 *         null if no polling frequency has been set.
	 */
	public final ModificationWatcher getResourceWatcher()
	{
		if (resourceWatcher == null)
		{
			final Duration pollFrequency = getSettings().getResourcePollFrequency();
			if (pollFrequency != null)
			{
				resourceWatcher = new ModificationWatcher(pollFrequency);
			}
		}
		return resourceWatcher;
	}

	/**
	 * @return Application settings
	 */
	public ApplicationSettings getSettings()
	{
		if (settings == null)
		{
			throw new IllegalStateException("Application settings not found");
		}
		return settings;
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
	}

	/**
	 * Changes the resource locator which will be used to locate resources like
	 * markup files.
	 * 
	 * @param resourceStreamLocator
	 *            The new resource stream locator
	 */
	protected void setResourceStreamLocator(final ResourceStreamLocator resourceStreamLocator)
	{
		this.resourceStreamLocator = resourceStreamLocator;
	}

	/**
	 * Called by ApplicationSettings when source path property is changed. This
	 * method sets the resourceStreamLocator to null so it will get recreated
	 * the next time it is accessed using the new source path.
	 */
	void sourcePathChanged()
	{
		this.resourceStreamLocator = null;
	}
}
