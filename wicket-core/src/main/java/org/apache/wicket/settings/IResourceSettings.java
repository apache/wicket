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

import java.util.Comparator;
import java.util.List;

import org.apache.wicket.IResourceFactory;
import org.apache.wicket.Localizer;
import org.apache.wicket.css.ICssCompressor;
import org.apache.wicket.javascript.IJavaScriptCompressor;
import org.apache.wicket.markup.head.PriorityFirstComparator;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.head.ResourceAggregator.RecordedHeaderItem;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.PackageResourceGuard;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.resource.IPropertiesFactory;
import org.apache.wicket.resource.IPropertiesFactoryContext;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.util.file.IFileCleaner;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.watch.IModificationWatcher;


/**
 * Interface for resource related settings
 * <p>
 * <i>resourcePollFrequency </i> (defaults to no polling frequency) - Frequency at which resources
 * should be polled for changes.
 * <p>
 * <i>resourceFinder </i> (classpath) - Set this to alter the search path for resources.
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
 * {@link org.apache.wicket.Component#getString(String key, IModel model)}.
 * <p>
 * <i>stringResourceLoaders </i>- A chain of <code>IStringResourceLoader</code> instances that are
 * searched in order to obtain string resources used during localization. By default the chain is
 * set up to first search for resources against a particular component (e.g. page etc.) and then
 * against the application.
 * </p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IResourceSettings extends IPropertiesFactoryContext
{
	/**
	 * Adds a resource factory to the list of factories to consult when generating resources
	 * automatically
	 * 
	 * @param name
	 *            The name to give to the factory
	 * @param resourceFactory
	 *            The resource factory to add
	 */
	void addResourceFactory(final String name, final IResourceFactory resourceFactory);

	/**
	 * Convenience method that sets the resource search path to a single folder. use when searching
	 * for resources. By default, the resources are located on the classpath. If you want to
	 * configure other, additional, search paths, you can use this method
	 * 
	 * @param resourceFolder
	 *            The resourceFolder to set
	 */
	void addResourceFolder(final String resourceFolder);

	/**
	 * Get the the default cache duration for resources.
	 * <p/>
	 * 
	 * @return cache duration (Duration.NONE will be returned if caching is disabled)
	 * 
	 * @see org.apache.wicket.util.time.Duration#NONE
	 */
	Duration getDefaultCacheDuration();

	/**
	 * Gets the {@link PackageResourceGuard package resource guard}.
	 * 
	 * @return The package resource guard
	 */
	IPackageResourceGuard getPackageResourceGuard();

	/**
	 * Get the property factory which will be used to load property files
	 * 
	 * @return PropertiesFactory
	 */
	IPropertiesFactory getPropertiesFactory();

	/**
	 * @param name
	 *            Name of the factory to get
	 * @return The IResourceFactory with the given name.
	 */
	IResourceFactory getResourceFactory(final String name);

	/**
	 * Gets the resource finder to use when searching for resources.
	 * 
	 * @return Returns the resourceFinder.
	 * @see IResourceSettings#setResourceFinder(IResourceFinder)
	 */
	IResourceFinder getResourceFinder();

	/**
	 * @return Returns the resourcePollFrequency.
	 * @see IResourceSettings#setResourcePollFrequency(Duration)
	 */
	Duration getResourcePollFrequency();

	/**
	 * @return mutable list of all available string resource loaders
	 */
	List<IStringResourceLoader> getStringResourceLoaders();

	/**
	 * @return boolean
	 */
	boolean getThrowExceptionOnMissingResource();

	/**
	 * @return Whether to use a default value (if available) when a missing resource is requested
	 */
	boolean getUseDefaultOnMissingResource();

	/**
	 * Set the the default cache duration for resources.
	 * <p/>
	 * Based on RFC-2616 this should not exceed one year. If you set Duration.NONE caching will be
	 * disabled.
	 * 
	 * @param defaultDuration
	 *            default cache duration in seconds
	 * 
	 * @see org.apache.wicket.util.time.Duration#NONE
	 * @see org.apache.wicket.request.http.WebResponse#MAX_CACHE_DURATION
	 */
	void setDefaultCacheDuration(Duration defaultDuration);

	/**
	 * Sets the localizer which will be used to find property values.
	 * 
	 * @param localizer
	 * @since 1.3.0
	 */
	void setLocalizer(Localizer localizer);

	/**
	 * Sets the {@link PackageResourceGuard package resource guard}.
	 * 
	 * @param packageResourceGuard
	 *            The package resource guard
	 */
	void setPackageResourceGuard(IPackageResourceGuard packageResourceGuard);

	/**
	 * Set the property factory which will be used to load property files
	 * 
	 * @param factory
	 */
	void setPropertiesFactory(IPropertiesFactory factory);

	/**
	 * Sets the finder to use when searching for resources. By default, the resources are located on
	 * the classpath. If you want to configure other, additional, search paths, you can use this
	 * method.
	 * 
	 * @param resourceFinder
	 *            The resourceFinder to set
	 */
	void setResourceFinder(final IResourceFinder resourceFinder);

	/**
	 * Sets the resource polling frequency. This is the duration of time between checks of resource
	 * modification times. If a resource, such as an HTML file, has changed, it will be reloaded.
	 * The default is one second in 'development' mode and 'never' in deployment mode.
	 * 
	 * @param resourcePollFrequency
	 *            Frequency at which to poll resources or <code>null</code> if polling should be
	 *            disabled
	 * 
	 * @see IResourceSettings#setResourceFinder(IResourceFinder)
	 */
	void setResourcePollFrequency(final Duration resourcePollFrequency);

	/**
	 * Sets the resource stream locator for this application
	 * 
	 * @param resourceStreamLocator
	 *            new resource stream locator
	 */
	void setResourceStreamLocator(IResourceStreamLocator resourceStreamLocator);

	/**
	 * Sets the resource watcher
	 * 
	 * @param watcher
	 */
	void setResourceWatcher(IModificationWatcher watcher);

	/**
	 * Sets a cleaner that can be used to remove files asynchronously.
	 * <p>
	 * Used internally to delete the temporary files created by FileUpload functionality
	 * 
	 * @param fileCleaner
	 *            the actual cleaner implementation. Can be <code>null</code>
	 */
	void setFileCleaner(IFileCleaner fileCleaner);

	/**
	 * @return the a cleaner which can be used to remove files asynchronously.
	 */
	IFileCleaner getFileCleaner();

	/**
	 * @param throwExceptionOnMissingResource
	 */
	void setThrowExceptionOnMissingResource(final boolean throwExceptionOnMissingResource);

	/**
	 * @param useDefaultOnMissingResource
	 *            Whether to use a default value (if available) when a missing resource is requested
	 */
	void setUseDefaultOnMissingResource(final boolean useDefaultOnMissingResource);

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
	IJavaScriptCompressor setJavaScriptCompressor(IJavaScriptCompressor compressor);

	/**
	 * Get the javascript compressor to remove comments and whitespace characters from javascripts
	 * 
	 * @return whether the comments and whitespace characters will be stripped from resources served
	 *         through {@link org.apache.wicket.request.resource.JavaScriptPackageResource
	 *         JavaScriptPackageResource}. Null is a valid value.
	 */
	IJavaScriptCompressor getJavaScriptCompressor();

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
	String getParentFolderPlaceholder();

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
	void setParentFolderPlaceholder(String sequence);

	/**
	 * gets the resource caching strategy
	 * 
	 * @return strategy
	 */
	IResourceCachingStrategy getCachingStrategy();

	/**
	 * sets the resource caching strategy
	 * 
	 * @param strategy
	 *            instance of resource caching strategy
	 * 
	 * @see IResourceCachingStrategy
	 */
	void setCachingStrategy(IResourceCachingStrategy strategy);

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
	ICssCompressor setCssCompressor(ICssCompressor compressor);

	/**
	 * Get the CSS compressor to remove comments and whitespace characters from css resources
	 * 
	 * @return whether the comments and whitespace characters will be stripped from resources served
	 *         through {@link org.apache.wicket.request.resource.CssPackageResource
	 *         CssPackageResource}. Null is a valid value.
	 */
	ICssCompressor getCssCompressor();

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
	void setUseMinifiedResources(boolean useMinifiedResources);

	/**
	 * @return Whether pre-minified resources will be used.
	 */
	boolean getUseMinifiedResources();

	/**
	 * Sets the comparator used by the {@linkplain ResourceAggregator resource aggregator} for
	 * sorting header items. It should be noted that sorting header items may break resource
	 * dependencies. This comparator should therefore at least respect dependencies declared by
	 * resource references. By default, items are sorted using the {@link PriorityFirstComparator}.
	 * 
	 * @param comparator
	 *            The comparator used to sort header items, when null, header items will not be
	 *            sorted.
	 */
	void setHeaderItemComparator(Comparator<? super RecordedHeaderItem> comparator);

	/**
	 * @return The comparator used to sort header items.
	 */
	Comparator<? super RecordedHeaderItem> getHeaderItemComparator();
}
