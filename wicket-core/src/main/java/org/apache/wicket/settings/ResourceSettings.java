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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IResourceFactory;
import org.apache.wicket.Localizer;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.core.util.resource.locator.ResourceStreamLocator;
import org.apache.wicket.core.util.resource.locator.caching.CachingResourceStreamLocator;
import org.apache.wicket.css.ICssCompressor;
import org.apache.wicket.javascript.IJavaScriptCompressor;
import org.apache.wicket.markup.head.PriorityFirstComparator;
import org.apache.wicket.markup.head.ResourceAggregator.RecordedHeaderItem;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.NoOpResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.version.CachingResourceVersion;
import org.apache.wicket.request.resource.caching.version.IResourceVersion;
import org.apache.wicket.request.resource.caching.version.LastModifiedResourceVersion;
import org.apache.wicket.request.resource.caching.version.MessageDigestResourceVersion;
import org.apache.wicket.request.resource.caching.version.RequestCycleCachedResourceVersion;
import org.apache.wicket.resource.IPropertiesFactoryContext;
import org.apache.wicket.resource.PropertiesFactory;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.resource.loader.InitializerStringResourceLoader;
import org.apache.wicket.resource.loader.PackageStringResourceLoader;
import org.apache.wicket.resource.loader.ValidatorStringResourceLoader;
import org.apache.wicket.util.file.IFileCleaner;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.watch.IModificationWatcher;
import org.apache.wicket.util.watch.ModificationWatcher;

/**
 * Interface for resource related settings
 * <p>
 * <i>resourcePollFrequency </i> (defaults to no polling frequency) - Frequency at which resources
 * should be polled for changes.
 * <p>
 * <i>resourceFinders</i> - Add/modify this to alter the search path for resources.
 * <p>
 * <i>useDefaultOnMissingResource </i> (defaults to true) - Set to true to return a default value if
 * available when a required string resource is not found. If set to false then the
 * throwExceptionOnMissingResource flag is used to determine how to behave. If no default is
 * available then this is the same as if this flag were false
 * <p>
 * <i>A ResourceStreamLocator </i>- An Application's ResourceStreamLocator is used to find resources
 * such as images or markup files. You can supply your own ResourceStreamLocator if your prefer to
 * store your application's resources in a non-standard location (such as a different filesystem
 * location, a particular JAR file or even a database) by overriding the getResourceLocator()
 * method.
 * <p>
 * <i>Resource Factories </i>- Resource factories can be used to create resources dynamically from
 * specially formatted HTML tag attribute values. For more details, see {@link IResourceFactory},
 * {@link org.apache.wicket.markup.html.image.resource.DefaultButtonImageResourceFactory} and
 * especially {@link org.apache.wicket.markup.html.image.resource.LocalizedImageResource}.
 * <p>
 * <i>A Localizer </i> The getLocalizer() method returns an object encapsulating all of the
 * functionality required to access localized resources. For many localization problems, even this
 * will not be required, as there are convenience methods available to all components:
 * {@link org.apache.wicket.Component#getString(String key)} and
 * {@link org.apache.wicket.Component#getString(String key, org.apache.wicket.model.IModel model)}.
 * <p>
 * <i>stringResourceLoaders </i>- A chain of <code>IStringResourceLoader</code> instances that are
 * searched in order to obtain string resources used during localization. By default the chain is
 * set up to first search for resources against a particular component (e.g. page etc.) and then
 * against the application.
 * </p>
 *
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class ResourceSettings implements IPropertiesFactoryContext
{
	/** I18N support */
	private Localizer localizer;

	/** Map to look up resource factories by name */
	private final Map<String, IResourceFactory> nameToResourceFactory = Generics.newHashMap();

	/** The package resource guard. */
	private IPackageResourceGuard packageResourceGuard = new SecurePackageResourceGuard(
		new SecurePackageResourceGuard.SimpleCache(100));

	/** The factory to be used for the property files */
	private org.apache.wicket.resource.IPropertiesFactory propertiesFactory;

	/** Filesystem Path to search for resources */
	private List<IResourceFinder> resourceFinders = new ArrayList<IResourceFinder>();

	/** Frequency at which files should be polled */
	private Duration resourcePollFrequency = null;

	/** resource locator for this application */
	private IResourceStreamLocator resourceStreamLocator;

	/** ModificationWatcher to watch for changes in markup files */
	private IModificationWatcher resourceWatcher;

	/**
	 * A cleaner that removes files asynchronously.
	 * <p>
	 * Used internally to remove the temporary files created by FileUpload functionality.
	 */
	private IFileCleaner fileCleaner;

	/** Chain of string resource loaders to use */
	private final List<IStringResourceLoader> stringResourceLoaders = Generics.newArrayList(4);

	/** Flags used to determine how to behave if resources are not found */
	private boolean throwExceptionOnMissingResource = true;

	/** Determines behavior of string resource loading if string is missing */
	private boolean useDefaultOnMissingResource = true;

	/** Default cache duration */
	private Duration defaultCacheDuration = WebResponse.MAX_CACHE_DURATION;

	/** The JavaScript compressor */
	private IJavaScriptCompressor javascriptCompressor;

	/** The Css compressor */
	private ICssCompressor cssCompressor;

	/** escape string for '..' within resource keys */
	private String parentFolderPlaceholder = "::";

	// resource caching strategy
	private IResourceCachingStrategy resourceCachingStrategy;

	// application these settings are bound to
	private final Application application;

	private boolean useMinifiedResources = true;

	private Comparator<? super RecordedHeaderItem> headerItemComparator = new PriorityFirstComparator(
		false);

	private boolean encodeJSessionId = false;

	/**
	 * Configures Wicket's default ResourceLoaders.<br>
	 * For an example in {@code FooApplication} let {@code bar.Foo} extend {@link Component}, this
	 * results in the following ordering:
	 * <dl>
	 * <dt>component specific</dt>
	 * <dd>
	 * <ul>
	 * <li>bar/Foo.properties</li>
	 * <li>org/apache/wicket/Component.properties</li>
	 * </ul>
	 * </dd>
	 * <dt>package specific</dt>
	 * <dd>
	 * <ul>
	 * <li>bar/package.properties</li>
	 * <li>package.properties (on Foo's class loader)</li>
	 * <li>org/apache/wicket/package.properties</li>
	 * <li>org/apache/package.properties</li>
	 * <li>org/package.properties</li>
	 * <li>package.properties (on Component's class loader)</li>
	 * </ul>
	 * </dd>
	 * <dt>application specific</dt>
	 * <dd>
	 * <ul>
	 * <li>FooApplication.properties</li>
	 * <li>Application.properties</li>
	 * </ul>
	 * </dd>
	 * <dt>validator specific</dt>
	 * <dt>Initializer specific</dt>
	 * <dd>
	 * <ul>
	 * <li>bar.Foo.properties (Foo implementing IInitializer)</li>
	 * </ul>
	 * </dd>
	 * </dl>
	 * 
	 * @param application
	 */
	public ResourceSettings(final Application application)
	{
		this.application = application;
		stringResourceLoaders.add(new ComponentStringResourceLoader());
		stringResourceLoaders.add(new PackageStringResourceLoader());
		stringResourceLoaders.add(new ClassStringResourceLoader(application.getClass()));
		stringResourceLoaders.add(new ValidatorStringResourceLoader());
		stringResourceLoaders.add(new InitializerStringResourceLoader(application.getInitializers()));
	}

	/**
	 * Adds a resource factory to the list of factories to consult when generating resources
	 * automatically
	 *
	 * @param name
	 *            The name to give to the factory
	 * @param resourceFactory
	 *            The resource factory to add
	 */
	public void addResourceFactory(final String name, IResourceFactory resourceFactory)
	{
		nameToResourceFactory.put(name, resourceFactory);
	}

	public Localizer getLocalizer()
	{
		if (localizer == null)
		{
			localizer = new Localizer();
		}
		return localizer;
	}

	/**
	 * Gets the {@link org.apache.wicket.markup.html.PackageResourceGuard package resource guard}.
	 *
	 * @return The package resource guard
	 */
	public IPackageResourceGuard getPackageResourceGuard()
	{
		return packageResourceGuard;
	}

	/**
	 * Get the property factory which will be used to load property files
	 *
	 * @return PropertiesFactory
	 */
	public org.apache.wicket.resource.IPropertiesFactory getPropertiesFactory()
	{
		if (propertiesFactory == null)
		{
			propertiesFactory = new PropertiesFactory(this);
		}
		return propertiesFactory;
	}

	/**
	 * @param name
	 *            Name of the factory to get
	 * @return The IResourceFactory with the given name.
	 */
	public IResourceFactory getResourceFactory(final String name)
	{
		return nameToResourceFactory.get(name);
	}

	/**
	 * Gets the resource finders to use when searching for resources. By default, a finder that
	 * looks in the classpath root is configured. {@link org.apache.wicket.protocol.http.WebApplication} adds the classpath
	 * directory META-INF/resources. To configure additional search paths or filesystem paths, add
	 * to this list.
	 *
	 * @return Returns the resourceFinders.
	 */
	public List<IResourceFinder> getResourceFinders()
	{
		return resourceFinders;
	}

	/**
	 * @return Returns the resourcePollFrequency.
	 */
	public Duration getResourcePollFrequency()
	{
		return resourcePollFrequency;
	}

	public IResourceStreamLocator getResourceStreamLocator()
	{
		if (resourceStreamLocator == null)
		{
			// Create compound resource locator using source path from
			// application settings
			resourceStreamLocator = new ResourceStreamLocator(getResourceFinders());
			resourceStreamLocator = new CachingResourceStreamLocator(resourceStreamLocator);
		}
		return resourceStreamLocator;
	}

	public IModificationWatcher getResourceWatcher(boolean start)
	{
		if (resourceWatcher == null && start)
		{
			synchronized (this)
			{
				if (resourceWatcher == null)
				{
					final Duration pollFrequency = getResourcePollFrequency();
					if (pollFrequency != null)
					{
						resourceWatcher = new ModificationWatcher(pollFrequency);
					}
				}
			}
		}
		return resourceWatcher;
	}

	/**
	 * Sets the resource watcher
	 *
	 * @param watcher
	 */
	public void setResourceWatcher(IModificationWatcher watcher)
	{
		resourceWatcher = watcher;
	}

	/**
	 * @return the a cleaner which can be used to remove files asynchronously.
	 */
	public IFileCleaner getFileCleaner()
	{
		return fileCleaner;
	}

	/**
	 * Sets a cleaner that can be used to remove files asynchronously.
	 * <p>
	 * Used internally to delete the temporary files created by FileUpload functionality
	 *
	 * @param fileUploadCleaner
	 *            the actual cleaner implementation. Can be <code>null</code>
	 */
	public void setFileCleaner(IFileCleaner fileUploadCleaner)
	{
		fileCleaner = fileUploadCleaner;
	}

	/**
	 * @return mutable list of all available string resource loaders
	 */
	public List<IStringResourceLoader> getStringResourceLoaders()
	{
		return stringResourceLoaders;
	}

	public boolean getThrowExceptionOnMissingResource()
	{
		return throwExceptionOnMissingResource;
	}

	/**
	 * @return Whether to use a default value (if available) when a missing resource is requested
	 */
	public boolean getUseDefaultOnMissingResource()
	{
		return useDefaultOnMissingResource;
	}

	/**
	 * Sets the localizer which will be used to find property values.
	 *
	 * @param localizer
	 * @since 1.3.0
	 */
	public void setLocalizer(final Localizer localizer)
	{
		this.localizer = localizer;
	}

	/**
	 * Sets the {@link org.apache.wicket.markup.html.PackageResourceGuard package resource guard}.
	 *
	 * @param packageResourceGuard
	 *            The package resource guard
	 */
	public void setPackageResourceGuard(IPackageResourceGuard packageResourceGuard)
	{
		this.packageResourceGuard = Args.notNull(packageResourceGuard, "packageResourceGuard");
	}

	/**
	 * Set the property factory which will be used to load property files
	 *
	 * @param factory
	 */
	public void setPropertiesFactory(org.apache.wicket.resource.IPropertiesFactory factory)
	{
		propertiesFactory = factory;
	}

	/**
	 * Sets the finders to use when searching for resources. By default, the resources are located
	 * on the classpath. To add additional search paths, add to the list given by
	 * {@link #getResourceFinders()}. Use this method if you want to completely exchange the list of
	 * resource finders.
	 *
	 * @param resourceFinders
	 *            The resourceFinders to set
	 */
	public void setResourceFinders(final List<IResourceFinder> resourceFinders)
	{
		Args.notNull(resourceFinders, "resourceFinders");
		this.resourceFinders = resourceFinders;

		// Cause resource locator to get recreated
		resourceStreamLocator = null;
	}

	/**
	 * Sets the resource polling frequency. This is the duration of time between checks of resource
	 * modification times. If a resource, such as an HTML file, has changed, it will be reloaded.
	 * The default is one second in 'development' mode and 'never' in deployment mode.
	 *
	 * @param resourcePollFrequency
	 *            Frequency at which to poll resources or <code>null</code> if polling should be
	 *            disabled
	 */
	public void setResourcePollFrequency(final Duration resourcePollFrequency)
	{
		this.resourcePollFrequency = resourcePollFrequency;
	}

	/**
	 * /**
	 * Sets the resource stream locator for this application
	 *
	 * Consider wrapping <code>resourceStreamLocator</code> in {@link CachingResourceStreamLocator}.
	 * This way the locator will not be asked more than once for {@link IResourceStream}s which do
	 * not exist.
	 * @param resourceStreamLocator
	 *            new resource stream locator
	 *
	 * @see #getResourceStreamLocator()
	 */
	public void setResourceStreamLocator(IResourceStreamLocator resourceStreamLocator)
	{
		this.resourceStreamLocator = resourceStreamLocator;
	}

	public void setThrowExceptionOnMissingResource(final boolean throwExceptionOnMissingResource)
	{
		this.throwExceptionOnMissingResource = throwExceptionOnMissingResource;
	}

	/**
	 * @param useDefaultOnMissingResource
	 *            Whether to use a default value (if available) when a missing resource is requested
	 */
	public void setUseDefaultOnMissingResource(final boolean useDefaultOnMissingResource)
	{
		this.useDefaultOnMissingResource = useDefaultOnMissingResource;
	}

	/**
	 * Get the the default cache duration for resources.
	 * <p/>
	 *
	 * @return cache duration (Duration.NONE will be returned if caching is disabled)
	 *
	 * @see org.apache.wicket.util.time.Duration#NONE
	 */
	public final Duration getDefaultCacheDuration()
	{
		return defaultCacheDuration;
	}

	/**
	 * Set the the default cache duration for resources.
	 * <p/>
	 * Based on RFC-2616 this should not exceed one year. If you set Duration.NONE caching will be
	 * disabled.
	 *
	 * @param duration
	 *            default cache duration in seconds
	 *
	 * @see org.apache.wicket.util.time.Duration#NONE
	 * @see org.apache.wicket.request.http.WebResponse#MAX_CACHE_DURATION
	 */
	public final void setDefaultCacheDuration(Duration duration)
	{
		Args.notNull(duration, "duration");
		defaultCacheDuration = duration;
	}

	/**
	 * Get the javascript compressor to remove comments and whitespace characters from javascripts
	 *
	 * @return whether the comments and whitespace characters will be stripped from resources served
	 *         through {@link org.apache.wicket.request.resource.JavaScriptPackageResource
	 *         JavaScriptPackageResource}. Null is a valid value.
	 */
	public IJavaScriptCompressor getJavaScriptCompressor()
	{
		return javascriptCompressor;
	}

	/**
	 * Set the javascript compressor implemententation use e.g. by
	 * {@link org.apache.wicket.request.resource.JavaScriptPackageResource
	 * JavaScriptPackageResource}. A typical implementation will remove comments and whitespace. But
	 * a no-op implementation is available as well.
	 *
	 * @param compressor
	 *            The implementation to be used
	 * @return The old value
	 */
	public IJavaScriptCompressor setJavaScriptCompressor(IJavaScriptCompressor compressor)
	{
		IJavaScriptCompressor old = javascriptCompressor;
		javascriptCompressor = compressor;
		return old;
	}

	/**
	 * Get the CSS compressor to remove comments and whitespace characters from css resources
	 *
	 * @return whether the comments and whitespace characters will be stripped from resources served
	 *         through {@link org.apache.wicket.request.resource.CssPackageResource
	 *         CssPackageResource}. Null is a valid value.
	 */
	public ICssCompressor getCssCompressor()
	{
		return cssCompressor;
	}

	/**
	 * Set the CSS compressor implemententation use e.g. by
	 * {@link org.apache.wicket.request.resource.CssPackageResource CssPackageResource}. A typical
	 * implementation will remove comments and whitespace. But a no-op implementation is available
	 * as well.
	 *
	 * @param compressor
	 *            The implementation to be used
	 * @return The old value
	 */
	public ICssCompressor setCssCompressor(ICssCompressor compressor)
	{
		ICssCompressor old = cssCompressor;
		cssCompressor = compressor;
		return old;
	}

	/**
	 * Placeholder string for '..' within resource urls (which will be crippled by the browser and
	 * not work anymore). Note that by default the placeholder string is <code>::</code>. Resources
	 * are protected by a {@link org.apache.wicket.markup.html.IPackageResourceGuard
	 * IPackageResourceGuard} implementation such as
	 * {@link org.apache.wicket.markup.html.PackageResourceGuard} which you may use or extend based
	 * on your needs.
	 *
	 * @return placeholder
	 */
	public String getParentFolderPlaceholder()
	{
		return parentFolderPlaceholder;
	}

	/**
	 * Placeholder string for '..' within resource urls (which will be crippled by the browser and
	 * not work anymore). Note that by default the placeholder string is <code>null</code> and thus
	 * will not allow to access parent folders. That is by purpose and for security reasons (see
	 * Wicket-1992). In case you really need it, a good value for placeholder would e.g. be "$up$".
	 * Resources additionally are protected by a
	 * {@link org.apache.wicket.markup.html.IPackageResourceGuard IPackageResourceGuard}
	 * implementation such as {@link org.apache.wicket.markup.html.PackageResourceGuard} which you
	 * may use or extend based on your needs.
	 *
	 * @see #getParentFolderPlaceholder()
	 *
	 * @param sequence
	 *            character sequence which must not be ambiguous within urls
	 */
	public void setParentFolderPlaceholder(final String sequence)
	{
		parentFolderPlaceholder = sequence;
	}

	/**
	 * gets the resource caching strategy
	 *
	 * @return strategy
	 */
	public IResourceCachingStrategy getCachingStrategy()
	{
		if (resourceCachingStrategy == null)
		{
			final IResourceVersion resourceVersion;

			if (application.usesDevelopmentConfig())
			{
				// development mode:
				// use last-modified timestamp of packaged resource for resource caching
				// cache the version information for the lifetime of the current http request
				resourceVersion = new RequestCycleCachedResourceVersion(
					new LastModifiedResourceVersion());
			}
			else
			{
				// deployment mode:
				// use message digest over resource content for resource caching
				// cache the version information for the lifetime of the application
				resourceVersion = new CachingResourceVersion(new MessageDigestResourceVersion());
			}
			// cache resource with a version string in the filename
			resourceCachingStrategy = new FilenameWithVersionResourceCachingStrategy(
				resourceVersion);
		}
		return resourceCachingStrategy;
	}

	/**
	 * sets the resource caching strategy
	 *
	 * @param strategy
	 *            instance of resource caching strategy
	 *
	 * @see IResourceCachingStrategy
	 */
	public void setCachingStrategy(IResourceCachingStrategy strategy)
	{
		if (strategy == null)
		{
			throw new NullPointerException(
				"It is not allowed to set the resource caching strategy to value NULL. " +
					"Please use " + NoOpResourceCachingStrategy.class.getName() + " instead.");
		}
		resourceCachingStrategy = strategy;
	}

	/**
	 * Sets whether to use pre-minified resources when available. Minified resources are detected by
	 * name. The minified version of {@code x.js} is expected to be called {@code x.min.js}. For css
	 * files, the same convention is used: {@code x.min.css} is the minified version of
	 * {@code x.css}. When this is null, minified resources will only be used in deployment
	 * configuration.
	 *
	 * @param useMinifiedResources
	 *            The new value for the setting
	 */
	public void setUseMinifiedResources(boolean useMinifiedResources)
	{
		this.useMinifiedResources = useMinifiedResources;
	}

	/**
	 * @return Whether pre-minified resources will be used.
	 */
	public boolean getUseMinifiedResources()
	{
		return useMinifiedResources;
	}

	/**
	 * @return The comparator used to sort header items.
	 */
	public Comparator<? super RecordedHeaderItem> getHeaderItemComparator()
	{
		return headerItemComparator;
	}

	/**
	 * Sets the comparator used by the {@linkplain org.apache.wicket.markup.head.ResourceAggregator resource aggregator} for
	 * sorting header items. It should be noted that sorting header items may break resource
	 * dependencies. This comparator should therefore at least respect dependencies declared by
	 * resource references. By default, items are sorted using the {@link PriorityFirstComparator}.
	 *
	 * @param headerItemComparator
	 *            The comparator used to sort header items, when null, header items will not be
	 *            sorted.
	 */
	public void setHeaderItemComparator(Comparator<? super RecordedHeaderItem> headerItemComparator)
	{
		this.headerItemComparator = headerItemComparator;
	}

	/**
	 * A flag indicating whether static resources should have <tt>jsessionid</tt> encoded in their
	 * url.
	 *
	 * @return {@code true} if the jsessionid should be encoded in the url for resources
	 *         implementing
	 *         {@link org.apache.wicket.request.resource.caching.IStaticCacheableResource} when the
	 *         cookies are disabled and there is an active http session.
	 */
	public boolean isEncodeJSessionId()
	{
		return encodeJSessionId;
	}

	/**
	 * Sets a flag indicating whether the jsessionid should be encoded in the url for resources
	 * implementing {@link org.apache.wicket.request.resource.caching.IStaticCacheableResource} when
	 * the cookies are disabled and there is an active http session.
	 *
	 * @param encodeJSessionId
	 *            {@code true} when the jsessionid should be encoded, {@code false} - otherwise
	 */
	public void setEncodeJSessionId(boolean encodeJSessionId)
	{
		this.encodeJSessionId = encodeJSessionId;
	}
}
