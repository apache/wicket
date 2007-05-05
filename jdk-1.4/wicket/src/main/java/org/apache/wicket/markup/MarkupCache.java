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

import java.io.IOException;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.concurrent.ConcurrentHashMap;
import org.apache.wicket.util.listener.IChangeListener;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.watch.ModificationWatcher;
import org.apache.wicket.util.watch.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

	/** Map of markup tags by class (exactly what is in the file). */
	private final Map markupCache = new ConcurrentHashMap();

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
	 * Gets a fresh markup stream that contains the (immutable) markup resource
	 * for this class.
	 * 
	 * @param container
	 *            The container the markup should be associated with
	 * @return A stream of MarkupElement elements
	 */
	public final MarkupStream getMarkupStream(final MarkupContainer container)
	{
		return getMarkupStream(container, true);
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
	public final MarkupStream getMarkupStream(final MarkupContainer container,
			final boolean throwException)
	{
		if (container == null)
		{
			throw new IllegalArgumentException("Parameter 'container' must not be 'null'.");
		}

		// Look for associated markup
		final Markup markup = getMarkup(container, container.getClass());

		// If we found markup for this container
		if (markup != Markup.NO_MARKUP)
		{
			return new MarkupStream(markup);
		}

		if (throwException == true)
		{
			// throw exception since there is no associated markup
			throw new MarkupNotFoundException(
					"Markup not found. Component class: "
							+ container.getClass().getName()
							+ " Enable debug messages for org.apache.wicket.util.resource to get a list of all filenames tried");
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
		return getMarkup(container, container.getClass()) != Markup.NO_MARKUP;
	}

	/**
	 * @return the number of elements currently in the cache.
	 */
	public int size()
	{
		return markupCache.size();
	}

	/**
	 * The markup has just been loaded and now we check if markup inheritance
	 * applies, which is if <wicket:extend> is found in the markup. If yes, than
	 * load the base markups and merge the markup elements to create an updated
	 * (merged) list of markup elements.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markup
	 *            The markup to checked for inheritance
	 * @return A markup object with the the base markup elements resolved.
	 */
	private Markup checkForMarkupInheritance(final MarkupContainer container, final Markup markup)
	{
		// Check if markup contains <wicket:extend> which tells us that
		// we need to read the inherited markup as well.
		int extendIndex = requiresBaseMarkup(markup);
		if (extendIndex == -1)
		{
			// return a MarkupStream for the markup
			return markup;
		}

		// get the base markup
		final Markup baseMarkup = getMarkup(container, markup.getResource().getMarkupClass()
				.getSuperclass());

		if (baseMarkup == Markup.NO_MARKUP)
		{
			throw new MarkupNotFoundException(
					"Parent markup of inherited markup not found. Component class: "
							+ markup.getResource().getContainerInfo().getContainerClass().getName()
							+ " Enable debug messages for org.apache.wicket.util.resource.Resource to get a list of all filenames tried.");
		}

		final CharSequence key = markup.getResource().getCacheKey();
		if (key != null)
		{
			// register an after-load listener for base markup. The listener
			// implementation will remove the derived markup which must be
			// merged
			// with the base markup
			afterLoadListeners.add(baseMarkup.getResource(), new IChangeListener()
			{
				/**
				 * Make sure there is only one listener per derived markup
				 * 
				 * @see java.lang.Object#equals(java.lang.Object)
				 */
				public boolean equals(Object obj)
				{
					return true;
				}

				/**
				 * Make sure there is only one listener per derived markup
				 * 
				 * @see java.lang.Object#hashCode()
				 */
				public int hashCode()
				{
					return key.hashCode();
				}

				public void onChange()
				{
					if (log.isDebugEnabled())
					{
						log.debug("Remove derived markup from cache: " + markup.getResource());
					}
					removeMarkup(markup.getResource());
				}
			});
		}

		// Merge base and derived markup
		Markup mergedMarkup = new MergedMarkup(markup, baseMarkup, extendIndex);
		return mergedMarkup;
	}

	/**
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
	private final Markup getMarkup(final MarkupContainer container, final Class clazz)
	{
		Class containerClass = clazz;
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
		Markup markup = null;
		if (cacheKey != null)
		{
			markup = (Markup)markupCache.get(cacheKey);
		}

		// Must Markup be loaded?
		if (markup == null)
		{
			synchronized (markupCache)
			{
				if (cacheKey != null)
				{
					markup = (Markup)markupCache.get(cacheKey);
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
						markup = Markup.NO_MARKUP;

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
	 * Loads markup from a resource stream.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markupResourceStream
	 *            The markup resource stream to load
	 * @return The markup
	 */
	private final Markup loadMarkup(final MarkupContainer container,
			final MarkupResourceStream markupResourceStream)
	{
		CharSequence cacheKey = markupResourceStream.getCacheKey();
		try
		{
			// read and parse the markup
			Markup markup = application.getMarkupSettings().getMarkupParserFactory()
					.newMarkupParser(markupResourceStream).parse();

			// Check for markup inheritance. If it contains <wicket:extend>
			// the two markups get merged.
			markup = checkForMarkupInheritance(container, markup);

			// add the markup to the cache
			if (cacheKey != null)
			{
				markupCache.put(cacheKey, markup);
			}

			// trigger all listeners registered on the markup just loaded
			afterLoadListeners.notifyListeners(markupResourceStream);

			return markup;
		}
		catch (ResourceStreamNotFoundException e)
		{
			log.error("Unable to find markup from " + markupResourceStream, e);
		}
		catch (IOException e)
		{
			log.error("Unable to read markup from " + markupResourceStream, e);
		}

		// In case of an error, remove the cache entry
		synchronized (markupCache)
		{
			if (cacheKey != null)
			{
				markupCache.remove(cacheKey);
				afterLoadListeners.remove(markupResourceStream);
			}
		}

		return Markup.NO_MARKUP;
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
	private final Markup loadMarkupAndWatchForChanges(final MarkupContainer container,
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
	 * Remove the markup from the cache and trigger all associated listeners
	 * 
	 * @param markupResourceStream
	 *            The resource stream
	 */
	private void removeMarkup(final MarkupResourceStream markupResourceStream)
	{
		CharSequence cacheKey = markupResourceStream.getCacheKey();
		if (cacheKey != null)
		{
			markupCache.remove(cacheKey);
			// trigger all listeners registered on the markup that is removed
			afterLoadListeners.notifyListeners(markupResourceStream);
			afterLoadListeners.remove(markupResourceStream);
		}
	}

	/**
	 * Check if markup contains &lt;wicket:extend&gt; which tells us that we
	 * need to read the inherited markup as well. &lt;wicket:extend&gt; MUST BE
	 * the first wicket tag in the markup. Skip raw markup
	 * 
	 * @param markup
	 * @return == 0, if no wicket:extend was found
	 */
	private int requiresBaseMarkup(final Markup markup)
	{
		for (int i = 0; i < markup.size(); i++)
		{
			MarkupElement elem = (MarkupElement)markup.get(i);
			if (elem instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag)elem;
				if (wtag.isExtendTag())
				{
					// Ok, inheritance is on and we must get the
					// inherited markup as well.
					return i;
				}
			}
		}
		return -1;
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
}
