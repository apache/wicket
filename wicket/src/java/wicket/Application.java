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
import java.util.List;

import wicket.markup.MarkupParser;
import wicket.markup.WicketTagComponentResolver;
import wicket.markup.html.form.encryption.ICrypt;
import wicket.markup.parser.XmlPullParser;
import wicket.util.convert.ConverterFactory;
import wicket.util.convert.IConverterFactory;
import wicket.util.lang.Classes;
import wicket.util.resource.locator.DefaultResourceLocator;
import wicket.util.resource.locator.ResourceLocator;
import wicket.util.time.Duration;
import wicket.util.watch.ModificationWatcher;

/**
 * Base class for all Wicket applications. An application has a name, settings,
 * a particular set of required pages and a variety of resources such as a
 * localizer, a markup parser factory method, a resource watcher and more.
 * <p>
 * To create a Wicket application, you generally do not want to directly
 * subclass this class. Instead, you want to subclass a subclass of Application,
 * like WebApplication, which is appropriate for the protocol and markup type
 * you are working with.
 * <p>
 * The application's settings specify how the application is to function.
 * <p>
 * The required pages returned by getPages() include a home page and pages for
 * handling common error conditions.
 * <p>
 * The getLocalizer() method returns an object encapsulating all of the
 * functionality required to access localized resources.
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

	/** Pages for application */
	private final ApplicationPages pages = new ApplicationPages();

	/** The default resource locator for this application */
	private ResourceLocator resourceLocator;

	/** ModificationWatcher to watch for changes in markup files */
	private transient ModificationWatcher resourceWatcher;

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
		componentResolvers.add(new WicketTagComponentResolver());
	}

	/**
	 * Get the (modifiable) List of ComponentResolvers.
	 * 
	 * @see WicketTagComponentResolver for an example
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
	public MarkupParser getMarkupParser()
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
	 * @return Resource locator for this application
	 */
	public ResourceLocator getResourceLocator()
	{
		if (resourceLocator == null)
		{
			// Create compound resource locator using source path from application settings
			resourceLocator = new DefaultResourceLocator(getSettings().getSourcePath());
		}
		return resourceLocator;
	}
	
	/**
	 * @return Resource watcher with polling frequency determined by setting, or
	 *         null if no polling frequency has been set.
	 */
	public ModificationWatcher getResourceWatcher()
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
	 * Change the resource locator which will be used to locate resources
	 * like e.g. markup files.
	 * 
	 * @param locator
	 */
	protected void setResourceLocator(final ResourceLocator locator)
	{
	    this.resourceLocator = locator;
	}

	/**
	 * Called by ApplicationSettings when source path property is changed. This
	 * method sets the resourceLocator to null so it will get recreated the next
	 * time it is accessed using the new source path.
	 */
	void sourcePathChanged()
	{
		this.resourceLocator = null;
	}
}
