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
package org.apache.wicket.settings.def;

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
import org.apache.wicket.resource.PropertiesFactory;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.resource.loader.InitializerStringResourceLoader;
import org.apache.wicket.resource.loader.PackageStringResourceLoader;
import org.apache.wicket.resource.loader.ValidatorStringResourceLoader;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.file.IFileCleaner;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.watch.IModificationWatcher;
import org.apache.wicket.util.watch.ModificationWatcher;

/**
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class ResourceSettings implements IResourceSettings
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
	 * @see org.apache.wicket.settings.IResourceSettings#addResourceFactory(java.lang.String,
	 *      org.apache.wicket.IResourceFactory)
	 */
	@Override
	public void addResourceFactory(final String name, IResourceFactory resourceFactory)
	{
		nameToResourceFactory.put(name, resourceFactory);
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getLocalizer()
	 */
	@Override
	public Localizer getLocalizer()
	{
		if (localizer == null)
		{
			localizer = new Localizer();
		}
		return localizer;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getPackageResourceGuard()
	 */
	@Override
	public IPackageResourceGuard getPackageResourceGuard()
	{
		return packageResourceGuard;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getPropertiesFactory()
	 */
	@Override
	public org.apache.wicket.resource.IPropertiesFactory getPropertiesFactory()
	{
		if (propertiesFactory == null)
		{
			propertiesFactory = new PropertiesFactory(this);
		}
		return propertiesFactory;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getResourceFactory(java.lang.String)
	 */
	@Override
	public IResourceFactory getResourceFactory(final String name)
	{
		return nameToResourceFactory.get(name);
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getResourceFinders()
	 */
	@Override
	public List<IResourceFinder> getResourceFinders()
	{
		return resourceFinders;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getResourcePollFrequency()
	 */
	@Override
	public Duration getResourcePollFrequency()
	{
		return resourcePollFrequency;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getResourceStreamLocator()
	 */
	@Override
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

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getResourceWatcher(boolean)
	 */
	@Override
	public IModificationWatcher getResourceWatcher(boolean start)
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
	 * @see org.apache.wicket.settings.IResourceSettings#setResourceWatcher(org.apache.wicket.util.watch.IModificationWatcher)
	 */
	@Override
	public void setResourceWatcher(IModificationWatcher watcher)
	{
		resourceWatcher = watcher;
	}

	@Override
	public IFileCleaner getFileCleaner()
	{
		return fileCleaner;
	}

	@Override
	public void setFileCleaner(IFileCleaner fileUploadCleaner)
	{
		fileCleaner = fileUploadCleaner;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getStringResourceLoaders()
	 */
	@Override
	public List<IStringResourceLoader> getStringResourceLoaders()
	{
		return stringResourceLoaders;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getThrowExceptionOnMissingResource()
	 */
	@Override
	public boolean getThrowExceptionOnMissingResource()
	{
		return throwExceptionOnMissingResource;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getUseDefaultOnMissingResource()
	 */
	@Override
	public boolean getUseDefaultOnMissingResource()
	{
		return useDefaultOnMissingResource;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setLocalizer(org.apache.wicket.Localizer)
	 */
	@Override
	public void setLocalizer(final Localizer localizer)
	{
		this.localizer = localizer;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setPackageResourceGuard(org.apache.wicket.markup.html.IPackageResourceGuard)
	 */
	@Override
	public void setPackageResourceGuard(IPackageResourceGuard packageResourceGuard)
	{
		this.packageResourceGuard = Args.notNull(packageResourceGuard, "packageResourceGuard");
	}

	/**
	 * @see IResourceSettings#setPropertiesFactory(org.apache.wicket.resource.IPropertiesFactory)
	 */
	@Override
	public void setPropertiesFactory(org.apache.wicket.resource.IPropertiesFactory factory)
	{
		propertiesFactory = factory;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setResourceFinder(org.apache.wicket.util.file.IResourceFinder)
	 */
	@Override
	public void setResourceFinders(final List<IResourceFinder> resourceFinders)
	{
		Args.notNull(resourceFinders, "resourceFinders");
		this.resourceFinders = resourceFinders;

		// Cause resource locator to get recreated
		resourceStreamLocator = null;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setResourcePollFrequency(org.apache.wicket.util.time.Duration)
	 */
	@Override
	public void setResourcePollFrequency(final Duration resourcePollFrequency)
	{
		this.resourcePollFrequency = resourcePollFrequency;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Consider wrapping <code>resourceStreamLocator</code> in {@link CachingResourceStreamLocator}.
	 * This way the locator will not be asked more than once for {@link IResourceStream}s which do
	 * not exist.
	 * 
	 * @see #getResourceStreamLocator()
	 */
	@Override
	public void setResourceStreamLocator(IResourceStreamLocator resourceStreamLocator)
	{
		this.resourceStreamLocator = resourceStreamLocator;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setThrowExceptionOnMissingResource(boolean)
	 */
	@Override
	public void setThrowExceptionOnMissingResource(final boolean throwExceptionOnMissingResource)
	{
		this.throwExceptionOnMissingResource = throwExceptionOnMissingResource;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setUseDefaultOnMissingResource(boolean)
	 */
	@Override
	public void setUseDefaultOnMissingResource(final boolean useDefaultOnMissingResource)
	{
		this.useDefaultOnMissingResource = useDefaultOnMissingResource;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getDefaultCacheDuration()
	 */
	@Override
	public final Duration getDefaultCacheDuration()
	{
		return defaultCacheDuration;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setDefaultCacheDuration(org.apache.wicket.util.time.Duration)
	 */
	@Override
	public final void setDefaultCacheDuration(Duration duration)
	{
		Args.notNull(duration, "duration");
		defaultCacheDuration = duration;
	}

	@Override
	public IJavaScriptCompressor getJavaScriptCompressor()
	{
		return javascriptCompressor;
	}

	@Override
	public IJavaScriptCompressor setJavaScriptCompressor(IJavaScriptCompressor compressor)
	{
		IJavaScriptCompressor old = javascriptCompressor;
		javascriptCompressor = compressor;
		return old;
	}

	@Override
	public ICssCompressor getCssCompressor()
	{
		return cssCompressor;
	}

	@Override
	public ICssCompressor setCssCompressor(ICssCompressor compressor)
	{
		ICssCompressor old = cssCompressor;
		cssCompressor = compressor;
		return old;
	}


	/**
	 * @see org.apache.wicket.settings.IResourceSettings#getParentFolderPlaceholder()
	 */
	@Override
	public String getParentFolderPlaceholder()
	{
		return parentFolderPlaceholder;
	}

	/**
	 * @see org.apache.wicket.settings.IResourceSettings#setParentFolderPlaceholder(String)
	 */
	@Override
	public void setParentFolderPlaceholder(final String sequence)
	{
		parentFolderPlaceholder = sequence;
	}

	/**
	 * @see IResourceSettings#getCachingStrategy()
	 */
	@Override
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
	 * @see org.apache.wicket.settings.IResourceSettings#setCachingStrategy(org.apache.wicket.request.resource.caching.IResourceCachingStrategy)
	 */
	@Override
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

	@Override
	public void setUseMinifiedResources(boolean useMinifiedResources)
	{
		this.useMinifiedResources = useMinifiedResources;
	}

	@Override
	public boolean getUseMinifiedResources()
	{
		return useMinifiedResources;
	}

	@Override
	public Comparator<? super RecordedHeaderItem> getHeaderItemComparator()
	{
		return headerItemComparator;
	}

	@Override
	public void setHeaderItemComparator(Comparator<? super RecordedHeaderItem> headerItemComparator)
	{
		this.headerItemComparator = headerItemComparator;
	}

	public boolean isEncodeJSessionId()
	{
		return encodeJSessionId;
	}

	public void setEncodeJSessionId(boolean encodeJSessionId)
	{
		this.encodeJSessionId = encodeJSessionId;
	}
}
