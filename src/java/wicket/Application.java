/*
 * $Id$ $Revision:
 * 1.43 $ $Date$
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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.MarkupCache;
import wicket.markup.MarkupParser;
import wicket.markup.html.BodyOnLoadResolver;
import wicket.markup.html.HtmlHeaderResolver;
import wicket.markup.html.image.resource.DefaultButtonImageResourceFactory;
import wicket.markup.parser.XmlPullParser;
import wicket.model.IModel;
import wicket.util.convert.ConverterFactory;
import wicket.util.convert.IConverterFactory;
import wicket.util.crypt.ICrypt;
import wicket.util.crypt.NoCrypt;
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
	/** log. */
	private static Log log = LogFactory.getLog(Application.class);

	/** List of (static) ComponentResolvers */
	private List componentResolvers = new ArrayList();

	/**
	 * Factory for the converter instance; default to the non localized factory
	 * {@link ConverterFactory}.
	 */
	private IConverterFactory converterFactory = new ConverterFactory();

	/** The single application-wide localization class */
	private final Localizer localizer;

	/** Markup cache for this application */
	private final MarkupCache markupCache;

	/** Name of application subclass. */
	private final String name;

	/** Map to look up resource factories by name */
	private final Map nameToResourceFactory = new HashMap();

	/** Pages for application */
	private final ApplicationPages pages = new ApplicationPages();

	/** The default resource locator for this application */
	private ResourceStreamLocator resourceStreamLocator;

	/** ModificationWatcher to watch for changes in markup files */
	private ModificationWatcher resourceWatcher;

	/** Settings for application. */
	private ApplicationSettings settings;
	
	/** Shared resources for the application */
	private final SharedResources sharedResources = new SharedResources();

	/** cached encryption/decryption object. */
	private ICrypt crypt;

	/**
	 * Constructor
	 */
	public Application()
	{
		// Create name from subclass
		this.name = Classes.name(getClass());

		// Construct markup cache fot this application
		this.markupCache = new MarkupCache(this);

		// Construct localizer for this application
		this.localizer = new Localizer(this);

		// Install default component resolvers
		componentResolvers.add(new AutoComponentResolver());
		componentResolvers.add(new MarkupInheritanceResolver());
		componentResolvers.add(new HtmlHeaderResolver());
		componentResolvers.add(new BodyOnLoadResolver());

		// Install button image resource factory
		addResourceFactory("buttonFactory", new DefaultButtonImageResourceFactory());
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
	public final void addResourceFactory(final String name, final IResourceFactory resourceFactory)
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
	 * @return The application wide localizer instance
	 */
	public Localizer getLocalizer()
	{
		return localizer;
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
	 * Gets the name of this application.
	 * 
	 * @return The application name.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return Application's common pages
	 */
	public final ApplicationPages getPages()
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
	public final Iterator getPageSets(final Page page)
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
	 * @param name
	 *            Name of the factory to get
	 * @return The IResourceFactory with the given name.
	 */
	public final IResourceFactory getResourceFactory(final String name)
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
			resourceStreamLocator = new DefaultResourceStreamLocator(getSettings()
					.getResourceFinder());
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
			settings = createApplicationSettings();
		}
		return settings;
	}
	
	/**
	 * Subclasses could override this to give there own implementation of ApplicaitonSettings
	 * @return An instanceof an ApplicaitonSettings class.
	 */
	public ApplicationSettings createApplicationSettings()
	{
		return new ApplicationSettings(this);
	}
	
	/**
	 * @return Returns the sharedResources.
	 */
	public final SharedResources getSharedResources()
	{
		return sharedResources;
	}

	/**
	 * Factory method that creates an instance of de-/encryption class.
	 * NOTE: this implementation caches the crypt instance, so it has
	 * to be Threadsafe. If you want other behaviour, or want to provide
	 * a custom crypt class, you should override this method.
	 * 
	 * @return Instance of de-/encryption class
	 */
	public synchronized ICrypt newCrypt()
	{
		if (crypt == null)
		{
			Class cryptClass = getSettings().getCryptClass();
			try
			{
				crypt = (ICrypt)cryptClass.newInstance();
				log.info("using encryption/decryption object " + crypt);
				crypt.setKey(getSettings().getEncryptionKey());
				return crypt;
			}
			catch (Throwable e)
			{
				log.warn("************************** WARNING **************************");
				log.warn("As the instantion of encryption/decryption class:");
				log.warn("\t" + cryptClass);
				log.warn("failed, Wicket will fallback on a dummy implementation");
				log.warn("\t(" + NoCrypt.class.getName() + ")");
				log.warn("This is not recommended for production systems.");
				log.warn("Please override method wicket.Application.newCrypt()");
				log.warn("to provide a custom encryption/decryption implementation");
				log.warn("The cause of the instantion failure: ");
				log.warn("\t" + e.getMessage());
				if (log.isDebugEnabled())
				{
					log.debug("exception: ", e);
				}
				else
				{
					log.warn("set log level to DEBUG to display the stack trace.");
				}
				log.warn("*************************************************************");

				// assign the dummy crypt implementation
				crypt = new NoCrypt();
			}
		}

		return crypt;
	}

	/**
	 * Factory method that creates a markup parser.
	 * 
	 * @param container The wicket container requesting the markup
	 * @return A new MarkupParser
	 */
	public MarkupParser newMarkupParser(final MarkupContainer container)
	{
		final MarkupParser parser = new MarkupParser(container, 
		        new XmlPullParser(getSettings().getDefaultMarkupEncoding()));
		
		parser.configure(getSettings());
		return parser;
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
	 * Called by ApplicationSettings when source path property is changed. This
	 * method sets the resourceStreamLocator to null so it will get recreated
	 * the next time it is accessed using the new source path.
	 */
	final void resourceFinderChanged()
	{
		this.resourceStreamLocator = null;
	}

}
