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
package wicket.markup;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.loader.DefaultMarkupLoader;
import wicket.markup.loader.HeaderCleanupMarkupLoader;
import wicket.markup.loader.IMarkupLoader;
import wicket.markup.loader.InheritedMarkupMarkupLoader;
import wicket.util.listener.IChangeListener;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.watch.ModificationWatcher;
import wicket.util.watch.Watcher;

/**
 * Load markup and cache it for fast retrieval. If markup file changes, it'll be
 * removed and subsequently reloaded when needed.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class MarkupCache
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(MarkupCache.class);

	/** Markup Cache */
	private Map<CharSequence, MarkupFragment> markupCache = new ConcurrentHashMap<CharSequence, MarkupFragment>();

	/**
	 * Markup inheritance requires that merged markup gets re-merged either
	 * AFTER the base markup or the derived markup has been reloaded.
	 */
	private final Watcher afterLoadListeners = new Watcher();

	/** The Wicket application */
	private final Application application;

	/**
	 * Constructor.
	 * 
	 * @param application
	 */
	public MarkupCache(final Application application)
	{
		this.application = application;
	}

	/**
	 * Clear markup cache and force reload of all markup data
	 */
	public void clear()
	{
		this.afterLoadListeners.clear();
		this.markupCache.clear();
	}

	/**
	 * @return the number of elements currently in the cache.
	 */
	public int size()
	{
		return markupCache.size();
	}

	/**
	 * Add a listener which is triggered after the resource has been (re-)loaded
	 * 
	 * @param resourceStream
	 * @param listener
	 */
	public final void addAfterLoadListener(final MarkupResourceStream resourceStream,
			final IChangeListener listener)
	{
		this.afterLoadListeners.add(resourceStream, listener);
	}

	/**
	 * Clear markup cache and force reload of all markup data
	 */
	public void removeAllListeners()
	{
		this.afterLoadListeners.clear();
	}

	/**
	 * Remove the markup from the cache and trigger all associated listeners
	 * 
	 * @param markupResourceStream
	 *            The resource stream
	 */
	public final void removeMarkup(final MarkupResourceStream markupResourceStream)
	{
		markupCache.remove(markupResourceStream.getCacheKey());

		// trigger all listeners registered on the markup that is removed
		afterLoadListeners.notifyListeners(markupResourceStream);
		afterLoadListeners.remove(markupResourceStream);
	}

	/**
	 * Gets a fresh markup stream that contains the (immutable) markup resource
	 * for this class.
	 * 
	 * @param container
	 *            The container the markup should be associated with
	 * @param throwException
	 *            If true, throw an exception, if markup could not be found
	 * @return A stream of MarkupElement elements
	 */
	public final MarkupFragment getMarkup(final MarkupContainer container,
			final boolean throwException)
	{
		if (container == null)
		{
			throw new IllegalArgumentException("Parameter 'container' must not be 'null'.");
		}

		// Look for associated markup
		final MarkupFragment markup = getMarkup(container, container.getClass());

		// If we found markup for this container
		if (markup != MarkupFragment.NO_MARKUP_FRAGMENT)
		{
			return markup;
		}

		if (throwException == true)
		{
			// throw exception since there is no associated markup
			throw new MarkupNotFoundException(
					"Markup not found. Component class: "
							+ container.getClass().getName()
							+ " Enable debug messages for wicket.util.resource to get a list of all filenames tried");
		}

		return null;
	}

	/**
	 * Check if container has associated markup
	 * 
	 * @param container
	 *            The container the markup should be associated with
	 * @return True if this markup container has associated markup
	 */
	public final boolean hasAssociatedMarkup(final MarkupContainer container)
	{
		return getMarkup(container, container.getClass()) != MarkupFragment.NO_MARKUP_FRAGMENT;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT IMPLEMENT IT.
	 * 
	 * Gets any (immutable) markup resource for the container or any of its
	 * parent classes (markup inheritance)
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param clazz
	 *            The class to get the associated markup for. If null, the
	 *            container's class is used, but it can be a parent class of the
	 *            container as well (markup inheritance)
	 * @return Markup resource
	 */
	public final MarkupFragment getMarkup(final MarkupContainer<?> container,
			final Class<? extends MarkupContainer> clazz)
	{
		Class<? extends MarkupContainer> containerClass = clazz;
		if (clazz == null)
		{
			containerClass = container.getClass();
		}
		else
		{
			if (!clazz.isAssignableFrom(container.getClass()))
			{
				throw new WicketRuntimeException("Parameter clazz must be instance of container");
			}
		}

		// Get the cache key to be associated with the markup resource stream
		final IMarkupCacheKeyProvider markupCacheKeyProvider = getMarkupCacheKeyProvider(container);
		final CharSequence cacheKey = markupCacheKeyProvider.getCacheKey(container, clazz);

		// Markup already in the cache? If cacheKey == null, than don't cache
		// the markup resource stream
		MarkupFragment markup = null;
		if (cacheKey != null)
		{
			markup = markupCache.get(cacheKey);
		}

		// Must Markup be loaded?
		if (markup == null)
		{
			synchronized (markupCache)
			{
				if (cacheKey != null)
				{
					markup = markupCache.get(cacheKey);
				}

				// Must Markup be loaded?
				if (markup == null)
				{
					// Who is going to provide the markup resource stream?
					final IMarkupResourceStreamProvider markupResourceStreamProvider = getMarkupResourceStreamProvider(container);

					// Ask the provider to locate the markup resource stream
					final IResourceStream resourceStream = markupResourceStreamProvider
							.getMarkupResourceStream(container, containerClass);

					// Found markup?
					if (resourceStream != null)
					{
						final MarkupResourceStream markupResourceStream;
						if (resourceStream instanceof MarkupResourceStream)
						{
							markupResourceStream = (MarkupResourceStream)resourceStream;
						}
						else
						{
							markupResourceStream = new MarkupResourceStream(resourceStream,
									new ContainerInfo(container), containerClass);
						}

						markupResourceStream.setCacheKey(cacheKey);

						// load the markup and watch for changes
						markup = loadMarkupAndWatchForChanges(container, markupResourceStream);
					}
					else
					{
						// flag markup as non-existent (as opposed to null,
						// which might mean that it's simply not loaded into
						// the cache)
						markup = MarkupFragment.NO_MARKUP_FRAGMENT;

						// Save any markup list (or absence of one) for next
						// time
						if (cacheKey != null)
						{
							markupCache.put(cacheKey, markup);
						}
					}
				}
			}
		}
		return markup;
	}

	/**
	 * Determine the markup resource stream provider to be used
	 * 
	 * @param container
	 *            The MarkupContainer requesting the markup resource stream
	 * @return IMarkupResourceStreamProvider
	 */
	protected IMarkupResourceStreamProvider getMarkupResourceStreamProvider(
			final MarkupContainer container)
	{
		if (container instanceof IMarkupResourceStreamProvider)
		{
			return (IMarkupResourceStreamProvider)container;
		}

		return new DefaultMarkupResourceStreamProvider();
	}

	/**
	 * Determine the markup cache key provider to be used
	 * 
	 * @param container
	 *            The MarkupContainer requesting the markup resource stream
	 * @return IMarkupResourceStreamProvider
	 */
	protected IMarkupCacheKeyProvider getMarkupCacheKeyProvider(final MarkupContainer container)
	{
		if (container instanceof IMarkupCacheKeyProvider)
		{
			return (IMarkupCacheKeyProvider)container;
		}

		return new DefaultMarkupCacheKeyProvider();
	}

	/**
	 * Loads markup from a resource stream.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markupResourceStream
	 *            The markup resource stream to load
	 * @return The markup
	 */
	private final MarkupFragment loadMarkup(final MarkupContainer container,
			final MarkupResourceStream markupResourceStream)
	{
		try
		{
			final MarkupFragment markup = newMarkupLoader().loadMarkup(container,
					markupResourceStream);

			// add the markup to the cache
			if (markupResourceStream.getCacheKey() != null)
			{
				markup.makeImmutable();
				markupCache.put(markupResourceStream.getCacheKey(), markup);
			}

			// trigger all listeners registered on the markup just loaded
			afterLoadListeners.notifyListeners(markupResourceStream);

			return markup;
		}
		catch (final ResourceStreamNotFoundException e)
		{
			log.error("Unable to find markup from " + markupResourceStream, e);
		}
		catch (final IOException e)
		{
			log.error("Unable to read markup from " + markupResourceStream, e);
		}

		// In case of an error, remove the cache entry
		synchronized (markupCache)
		{
			markupCache.remove(markupResourceStream.getCacheKey());
			afterLoadListeners.remove(markupResourceStream);
		}

		return MarkupFragment.NO_MARKUP_FRAGMENT;
	}

	/**
	 * Load markup from an IResourceStream and add an {@link IChangeListener}to
	 * the {@link ModificationWatcher} so that if the resource changes, we can
	 * remove it from the cache automatically and subsequently reload when
	 * needed.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markupResourceStream
	 *            The markup stream to load and begin to watch
	 * @return The markup in the stream
	 */
	private final MarkupFragment loadMarkupAndWatchForChanges(final MarkupContainer container,
			final MarkupResourceStream markupResourceStream)
	{
		if (markupResourceStream.getCacheKey() != null)
		{
			// Watch file in the future
			final ModificationWatcher watcher = application.getResourceSettings()
					.getResourceWatcher(true);
			if (watcher != null)
			{
				watcher.add(markupResourceStream, new IChangeListener()
				{
					public void onChange()
					{
						if (log.isDebugEnabled())
						{
							log.debug("Remove markup from cache: " + markupResourceStream);
						}

						// Remove the markup from the cache. It will be reloaded
						// next time it the markup is requested.
						removeMarkup(markupResourceStream);
						watcher.remove(markupResourceStream);
					}
				});
			}
		}

		if (log.isDebugEnabled())
		{
			log.debug("Loading markup from " + markupResourceStream);
		}
		return loadMarkup(container, markupResourceStream);
	}

	/**
	 * In case there is a need to extend the default chain of MarkupLoaders
	 * 
	 * @return MarkupLoader
	 */
	protected IMarkupLoader newMarkupLoader()
	{
		return new InheritedMarkupMarkupLoader(application)
			.setParent(
					new HeaderCleanupMarkupLoader(application)
						.setParent(
								new DefaultMarkupLoader(application)));
	}

	/**
	 * In case you need a more sophisticate cache implementation.
	 * <p>
	 * Make sure that you cache implementation is thread safe.
	 * 
	 * @param markupCache
	 */
	public void setCacheMap(Map<CharSequence, MarkupFragment> markupCache)
	{
		this.markupCache = markupCache;
	}
}
