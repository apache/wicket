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
package org.apache.wicket.markup;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.listener.IChangeListener;
import org.apache.wicket.util.watch.IModifiable;
import org.apache.wicket.util.watch.IModificationWatcher;
import org.apache.wicket.util.watch.ModificationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is Wicket's default IMarkupCache implementation. It will load the markup and cache it for
 * fast retrieval.
 * <p>
 * If the application is in development mode and a markup file changes, it'll automatically be
 * removed from the cache and reloaded when needed.
 * <p>
 * MarkupCache is registered with {@link MarkupFactory} which in turn is registered with
 * {@link org.apache.wicket.settings.def.MarkupSettings} and thus can be replaced with a sub-classed version.
 * 
 * @see org.apache.wicket.settings.def.MarkupSettings
 * @see MarkupFactory
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class MarkupCache implements IMarkupCache
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(MarkupCache.class);

	/** The actual cache: location => Markup */
	private final ICache<String, Markup> markupCache;

	/**
	 * Add extra indirection to the cache: key => location
	 * <p>
	 * Since ConcurrentHashMap does not allow to store null values, we are using Markup.NO_MARKUP
	 * instead.
	 */
	private final ICache<String, String> markupKeyCache;

	/** The markup cache key provider used by MarkupCache */
	private IMarkupCacheKeyProvider markupCacheKeyProvider;

	/**
	 * Note that you can not use Application.get() since removeMarkup() will be called from a
	 * ModificationWatcher thread which has no associated Application.
	 */
	private final Application application;

	/**
	 * A convenient helper to get the markup cache registered with the application.
	 * 
	 * @see {@link Application#getMarkupSettings()}
	 * @see {@link MarkupFactory#getMarkupCache()}
	 * 
	 * @return The markup cache registered with the {@link Application}
	 */
	public final static IMarkupCache get()
	{
		return Application.get().getMarkupSettings().getMarkupFactory().getMarkupCache();
	}

	/**
	 * Constructor.
	 */
	protected MarkupCache()
	{
		application = Application.get();

		markupCache = newCacheImplementation();
		if (markupCache == null)
		{
			throw new WicketRuntimeException("The map used to cache markup must not be null");
		}

		markupKeyCache = newCacheImplementation();
	}

	@Override
	public void clear()
	{
		markupCache.clear();
		markupKeyCache.clear();
	}

	@Override
	public void shutdown()
	{
		markupCache.shutdown();
		markupKeyCache.shutdown();
	}

	/**
	 * Note that this method will be called from a "cleanup" thread which might not have a thread
	 * local application.
	 */
	@Override
	public final IMarkupFragment removeMarkup(final String cacheKey)
	{
		Args.notNull(cacheKey, "cacheKey");

		if (log.isDebugEnabled())
		{
			log.debug("Removing from cache: " + cacheKey);
		}

		// Remove the markup from the cache
		String locationString = markupKeyCache.get(cacheKey);
		IMarkupFragment markup = (locationString != null ? markupCache.get(locationString) : null);
		if (markup == null)
		{
			return null;
		}

		// Found an entry: actual markup or Markup.NO_MARKUP. Null values are not possible
		// because of ConcurrentHashMap.
		markupCache.remove(locationString);

		if (log.isDebugEnabled())
		{
			log.debug("Removed from cache: " + locationString);
		}

		// If a base markup file has been removed from the cache then
		// the derived markup should be removed as well.
		removeMarkupWhereBaseMarkupIsNoLongerInTheCache();

		// And now remove all watcher entries associated with markup
		// resources no longer in the cache.

		// Note that you can not use Application.get() since removeMarkup() will be called from a
		// ModificationWatcher thread which has no associated Application.

		IModificationWatcher watcher = application.getResourceSettings().getResourceWatcher(false);
		if (watcher != null)
		{
			Iterator<IModifiable> iter = watcher.getEntries().iterator();
			while (iter.hasNext())
			{
				IModifiable modifiable = iter.next();
				if (modifiable instanceof MarkupResourceStream)
				{
					if (!isMarkupCached((MarkupResourceStream)modifiable))
					{
						iter.remove();

						if (log.isDebugEnabled())
						{
							log.debug("Removed from watcher: " + modifiable);
						}
					}
				}
			}
		}

		return markup;
	}

	private void removeMarkupWhereBaseMarkupIsNoLongerInTheCache()
	{
		// Repeat until all dependent resources have been removed (count == 0)
		int count = 1;
		while (count > 0)
		{
			// Reset prior to next round
			count = 0;

			// Iterate though all entries of the cache
			Iterator<Markup> iter = markupCache.getValues().iterator();
			while (iter.hasNext())
			{
				Markup markup = iter.next();

				if ((markup != null) && (markup != Markup.NO_MARKUP))
				{
					// Check if the markup associated with key has a base markup. And if yes, test
					// if that is cached. If the base markup has been removed, than remove the
					// derived markup as well.

					MarkupResourceStream resourceStream = markup.getMarkupResourceStream();
					if (resourceStream != null)
					{
						resourceStream = resourceStream.getBaseMarkupResourceStream();
					}

					// Is the base markup available in the cache?
					if ((resourceStream != null) && !isMarkupCached(resourceStream))
					{
						iter.remove();
						count++;

						if (log.isDebugEnabled())
						{
							log.debug("Removed derived markup from cache: " +
								markup.getMarkupResourceStream());
						}
					}
				}
			}
		}
	}

	/**
	 * @param resourceStream
	 * @return True if the markup is cached
	 */
	private boolean isMarkupCached(final MarkupResourceStream resourceStream)
	{
		if (resourceStream != null)
		{
			String key = resourceStream.getCacheKey();
			if (key != null)
			{
				String locationString = markupKeyCache.get(key);
				if ((locationString != null) && (markupCache.get(locationString) != null))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public final int size()
	{
		return markupCache.size();
	}

	/**
	 * Get a unmodifiable map which contains the cached data. The map key is of type String and the
	 * value is of type Markup.
	 * <p>
	 * May be used to debug or iterate the cache content.
	 * 
	 * @return cache implementation
	 */
	public final ICache<String, Markup> getMarkupCache()
	{
		return markupCache;
	}

	@Override
	public final Markup getMarkup(final MarkupContainer container, final Class<?> clazz,
		final boolean enforceReload)
	{
		Class<?> containerClass = MarkupFactory.get().getContainerClass(container, clazz);

		// Get the cache key to be associated with the markup resource stream.
		// If the cacheKey returned == null, than caching is disabled for the resource stream.
		final String cacheKey = getMarkupCacheKeyProvider(container).getCacheKey(container,
			containerClass);

		// Is the markup already in the cache?
		Markup markup = null;
		if ((enforceReload == false) && (cacheKey != null))
		{
			markup = getMarkupFromCache(cacheKey, container);
		}

		// If markup not found in cache or cache disabled, than ...
		if (markup == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Load markup: cacheKey=" + cacheKey);
			}

			// Get the markup resource stream for the container
			final MarkupResourceStream resourceStream = MarkupFactory.get()
				.getMarkupResourceStream(container, containerClass);

			// Found markup?
			if (resourceStream != null)
			{
				resourceStream.setCacheKey(cacheKey);

				// load the markup and watch for changes
				markup = loadMarkupAndWatchForChanges(container, resourceStream, enforceReload);
			}
			else
			{
				markup = onMarkupNotFound(cacheKey, container, Markup.NO_MARKUP);
			}
		}

		// NO_MARKUP should only be used inside the Cache.
		if (markup == Markup.NO_MARKUP)
		{
			markup = null;
		}

		return markup;
	}

	/**
	 * Will be called if the markup was not in the cache yet and could not be found either.
	 * <p>
	 * Subclasses may change the default implementation. E.g. they might choose not to update the
	 * cache to enforce reloading of any markup not found. This might be useful in very dynamic
	 * environments. Additionally a non-caching IResourceStreamLocator should be used.
	 * 
	 * @param cacheKey
	 * @param container
	 * @param markup
	 *            Markup.NO_MARKUP
	 * @return Same as parameter "markup"
	 * @see org.apache.wicket.settings.def.ResourceSettings#setResourceStreamLocator(org.apache.wicket.core.util.resource.locator.IResourceStreamLocator)
	 */
	protected Markup onMarkupNotFound(final String cacheKey, final MarkupContainer container,
		final Markup markup)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Markup not found: " + cacheKey);
		}

		// If cacheKey == null then caching is disabled for the component
		if (cacheKey != null)
		{
			// flag markup as non-existent
			markupKeyCache.put(cacheKey, cacheKey);
			putIntoCache(cacheKey, container, markup);
		}

		return markup;
	}

	/**
	 * Put the markup into the cache if cacheKey is not null and the cache does not yet contain the
	 * cacheKey. Return the markup stored in the cache if cacheKey is present already.
	 * 
	 * More sophisticated implementations may call a container method to e.g. cache it per container
	 * instance.
	 * 
	 * @param locationString
	 *            If {@code null} then ignore the cache
	 * @param container
	 *            The container this markup is for.
	 * @param markup
	 * @return markup The markup provided, except if the cacheKey already existed in the cache, then
	 *         the markup from the cache is provided.
	 */
	protected Markup putIntoCache(final String locationString, final MarkupContainer container,
		Markup markup)
	{
		if (locationString != null)
		{
			if (markupCache.containsKey(locationString) == false)
			{
				// The default cache implementation is a ConcurrentHashMap. Thus neither the key nor
				// the value can be null.
				if (markup == null)
				{
					markup = Markup.NO_MARKUP;
				}

				markupCache.put(locationString, markup);
			}
			else
			{
				// We don't lock the cache while loading a markup. Thus it may
				// happen that the very same markup gets loaded twice (the first
				// markup being loaded, but not yet in the cache, and another
				// request requesting the very same markup). Since markup
				// loading in avg takes less than 100ms, it is not really an
				// issue. For consistency reasons however, we should always use
				// the markup loaded first which is why it gets returned.
				markup = markupCache.get(locationString);
			}
		}
		return markup;
	}

	/**
	 * Wicket's default implementation just uses the cacheKey to retrieve the markup from the cache.
	 * More sophisticated implementations may call a container method to e.g. ignore the cached
	 * markup under certain situations.
	 * 
	 * @param cacheKey
	 *            If null, than the cache will be ignored
	 * @param container
	 * @return null, if not found or to enforce reloading the markup
	 */
	protected Markup getMarkupFromCache(final String cacheKey, final MarkupContainer container)
	{
		if (cacheKey != null)
		{
			String locationString = markupKeyCache.get(cacheKey);
			if (locationString != null)
			{
				return markupCache.get(locationString);
			}
		}
		return null;
	}

	/**
	 * Loads markup from a resource stream.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markupResourceStream
	 *            The markup resource stream to load
	 * @param enforceReload
	 *            The cache will be ignored and all, including inherited markup files, will be
	 *            reloaded. Whatever is in the cache, it will be ignored
	 * @return The markup. Markup.NO_MARKUP, if not found.
	 */
	private final Markup loadMarkup(final MarkupContainer container,
		final MarkupResourceStream markupResourceStream, final boolean enforceReload)
	{
		String cacheKey = markupResourceStream.getCacheKey();
		String locationString = markupResourceStream.locationAsString();
		if (locationString == null)
		{
			// set the cache key as location string, because location string
			// couldn't be resolved.
			locationString = cacheKey;
		}

		Markup markup = MarkupFactory.get().loadMarkup(container, markupResourceStream,
			enforceReload);
		if (markup != null)
		{
			if (cacheKey != null)
			{
				String temp = markup.locationAsString();
				if (temp != null)
				{
					locationString = temp;
				}

				// add the markup to the cache.
				markupKeyCache.put(cacheKey, locationString);
				return putIntoCache(locationString, container, markup);
			}
			return markup;
		}

		// In case the markup could not be loaded (without exception) then ..
		if (cacheKey != null)
		{
			removeMarkup(cacheKey);
		}

		return Markup.NO_MARKUP;
	}

	/**
	 * Load markup from an IResourceStream and add an {@link IChangeListener}to the
	 * {@link ModificationWatcher} so that if the resource changes, we can remove it from the cache
	 * automatically and subsequently reload when needed.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markupResourceStream
	 *            The markup stream to load and begin to watch
	 * @param enforceReload
	 *            The cache will be ignored and all, including inherited markup files, will be
	 *            reloaded. Whatever is in the cache, it will be ignored
	 * @return The markup in the stream
	 */
	private final Markup loadMarkupAndWatchForChanges(final MarkupContainer container,
		final MarkupResourceStream markupResourceStream, final boolean enforceReload)
	{
		// @TODO the following code sequence looks very much like in loadMarkup. Can it be
		// optimized?
		final String cacheKey = markupResourceStream.getCacheKey();
		if (cacheKey != null)
		{
			if (enforceReload == false)
			{
				// get the location String
				String locationString = markupResourceStream.locationAsString();
				if (locationString == null)
				{
					// set the cache key as location string, because location string
					// couldn't be resolved.
					locationString = cacheKey;
				}
				Markup markup = markupCache.get(locationString);
				if (markup != null)
				{
					markupKeyCache.put(cacheKey, locationString);
					return markup;
				}
			}

			// Watch file in the future
			final IModificationWatcher watcher = application.getResourceSettings()
				.getResourceWatcher(true);
			if (watcher != null)
			{
				watcher.add(markupResourceStream, new IChangeListener()
				{
					@Override
					public void onChange()
					{
						if (log.isDebugEnabled())
						{
							log.debug("Remove markup from watcher: " + markupResourceStream);
						}

						// Remove the markup from the cache. It will be reloaded
						// next time when the markup is requested.
						watcher.remove(markupResourceStream);
						removeMarkup(cacheKey);
					}
				});
			}
		}

		if (log.isDebugEnabled())
		{
			log.debug("Loading markup from " + markupResourceStream);
		}
		return loadMarkup(container, markupResourceStream, enforceReload);
	}

	/**
	 * Get the markup cache key provider to be used
	 * 
	 * @param container
	 *            The MarkupContainer requesting the markup resource stream
	 * @return IMarkupResourceStreamProvider
	 */
	public IMarkupCacheKeyProvider getMarkupCacheKeyProvider(final MarkupContainer container)
	{
		if (container instanceof IMarkupCacheKeyProvider)
		{
			return (IMarkupCacheKeyProvider)container;
		}

		if (markupCacheKeyProvider == null)
		{
			markupCacheKeyProvider = new DefaultMarkupCacheKeyProvider();
		}
		return markupCacheKeyProvider;
	}

	/**
	 * Allows you to change the map implementation which will hold the cache data. By default it is
	 * a ConcurrentHashMap() in order to allow multiple thread to access the data in a secure way.
	 * 
	 * @param <K>
	 * @param <V>
	 * @return new instance of cache implementation
	 */
	protected <K, V> ICache<K, V> newCacheImplementation()
	{
		return new DefaultCacheImplementation<K, V>();
	}

	/**
	 * MarkupCache allows you to implement you own cache implementation. ICache is the interface the
	 * implementation must comply with.
	 * 
	 * @param <K>
	 *            The key type
	 * @param <V>
	 *            The value type
	 */
	public interface ICache<K, V>
	{
		/**
		 * Clear the cache
		 */
		void clear();

		/**
		 * Remove an entry from the cache.
		 * 
		 * @param key
		 * @return true, if found and removed
		 */
		boolean remove(K key);

		/**
		 * Get the cache element associated with the key
		 * 
		 * @param key
		 * @return cached object for key <code>key</code> or null if no matches
		 */
		V get(K key);

		/**
		 * Get all the keys referencing cache entries
		 * 
		 * @return collection of cached keys
		 */
		Collection<K> getKeys();

		/**
		 * Get all the values referencing cache entries
		 * 
		 * @return collection of cached keys
		 */
		Collection<V> getValues();

		/**
		 * Check if key is in the cache
		 * 
		 * @param key
		 * @return true if cache contains key <code>key</code>
		 */
		boolean containsKey(K key);

		/**
		 * Get the number of cache entries
		 * 
		 * @return number of cache entries
		 */
		int size();

		/**
		 * Put an entry into the cache
		 * 
		 * @param key
		 *            The reference key to find the element. Must not be null.
		 * @param value
		 *            The element to be cached. Must not be null.
		 */
		void put(K key, V value);

		/**
		 * Cleanup and shutdown
		 */
		void shutdown();
	}

	/**
	 * @param <K>
	 * @param <V>
	 */
	public static class DefaultCacheImplementation<K, V> implements ICache<K, V>
	{
		// Neither key nor value are allowed to be null with ConcurrentHashMap
		private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<K, V>();

		/**
		 * Construct.
		 */
		public DefaultCacheImplementation()
		{
		}

		@Override
		public void clear()
		{
			cache.clear();
		}

		@Override
		public boolean containsKey(final Object key)
		{
			if (key == null)
			{
				return false;
			}
			return cache.containsKey(key);
		}

		@Override
		public V get(final Object key)
		{
			if (key == null)
			{
				return null;
			}
			return cache.get(key);
		}

		@Override
		public Collection<K> getKeys()
		{
			return cache.keySet();
		}

		@Override
		public Collection<V> getValues()
		{
			return cache.values();
		}

		@Override
		public void put(K key, V value)
		{
			// Note that neither key nor value are allowed to be null with ConcurrentHashMap
			cache.put(key, value);
		}

		@Override
		public boolean remove(K key)
		{
			if (key == null)
			{
				return false;
			}
			return cache.remove(key) == null;
		}

		@Override
		public int size()
		{
			return cache.size();
		}

		@Override
		public void shutdown()
		{
			clear();
		}
	}
}
