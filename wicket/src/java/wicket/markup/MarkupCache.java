/*
 * $Id: MarkupCache.java 4639 2006-02-26 01:44:07 -0800 (Sun, 26 Feb 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-02-26 01:44:07 -0800 (Sun, 26 Feb
 * 2006) $
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
package wicket.markup;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
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
	private static final Log log = LogFactory.getLog(MarkupCache.class);

	/** Map of markup tags by class (exactly what is in the file). */
	private final Map<CharSequence, IMarkup> markupCache = new ConcurrentHashMap<CharSequence, IMarkup>();

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
		final IMarkup markup = getMarkup(container, container.getClass());

		// If we found markup for this container
		if (markup != IMarkup.NO_MARKUP)
		{
			return new MarkupStream(markup);
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
		return getMarkup(container, container.getClass()) != IMarkup.NO_MARKUP;
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
	private final IMarkup getMarkup(final MarkupContainer<?> container,
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
		IMarkup markup = null;
		if (cacheKey != null)
		{
			markup = markupCache.get(cacheKey);
		}

		// Must Markup be loaded?
		if (markup == null)
		{
			synchronized (markupCache)
			{
				markup = markupCache.get(cacheKey);

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
						markup = IMarkup.NO_MARKUP;

						// Save any markup list (or absence of one) for next
						// time
						markupCache.put(cacheKey, markup);
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
	 * Remove the markup from the cache and trigger all associated listeners
	 * 
	 * @param markupResourceStream
	 *            The resource stream
	 */
	private void removeMarkup(final MarkupResourceStream markupResourceStream)
	{
		markupCache.remove(markupResourceStream.getCacheKey());

		// trigger all listeners registered on the markup that is removed
		afterLoadListeners.notifyListeners(markupResourceStream);
		afterLoadListeners.remove(markupResourceStream);
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
	private final IMarkup loadMarkup(final MarkupContainer container,
			final MarkupResourceStream markupResourceStream)
	{
		try
		{
			// read and parse the markup
			IMarkup markup = application.getMarkupSettings().getMarkupParserFactory()
					.newMarkupParser(markupResourceStream).readAndParse();

			// Check for markup inheritance. If it contains <wicket:extend>
			// the two markups get merged.
			markup = checkForMarkupInheritance(container, markup);

			// add the markup to the cache
			if (markupResourceStream.getCacheKey() != null)
			{
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

		synchronized (markupCache)
		{
			markupCache.remove(markupResourceStream.getCacheKey());
			afterLoadListeners.remove(markupResourceStream);
		}

		return IMarkup.NO_MARKUP;
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
	private final IMarkup loadMarkupAndWatchForChanges(final MarkupContainer container,
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
	 * Clear markup cache and force reload of all markup data
	 */
	public void clear()
	{
		this.afterLoadListeners.clear();
		this.markupCache.clear();
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
	@SuppressWarnings("unchecked")
	private IMarkup checkForMarkupInheritance(final MarkupContainer container, final IMarkup markup)
	{
		// Check if markup contains <wicket:extend> which tells us that
		// we need to read the inherited markup as well.
		final int extendIndex = requiresBaseMarkup(markup);
		if (extendIndex == -1)
		{
			// return a MarkupStream for the markup
			return markup;
		}

		final Class<? extends MarkupContainer> markupClass = (Class<? extends MarkupContainer>)markup
				.getResource().getMarkupClass().getSuperclass();
		// get the base markup
		final IMarkup baseMarkup = getMarkup(container, markupClass);

		if (baseMarkup == IMarkup.NO_MARKUP)
		{
			throw new MarkupNotFoundException(
					"Base markup of inherited markup not found. Component class: "
							+ markup.getResource().getContainerInfo().getContainerClass().getName()
							+ " Enable debug messages for wicket.util.resource.Resource to get a list of all filenames tried.");
		}

		// register an after-load listener for base markup. The listener
		// implementation will remove the derived markup which must be merged
		// with the base markup
		afterLoadListeners.add(baseMarkup.getResource(), new IChangeListener()
		{
			public void onChange()
			{
				if (log.isDebugEnabled())
				{
					log.debug("Remove derived markup from cache: " + markup.getResource());
				}
				removeMarkup(markup.getResource());
			}

			/**
			 * Make sure there is only one listener per derived markup
			 * 
			 * @see java.lang.Object#equals(java.lang.Object)
			 */
			@Override
			public boolean equals(final Object obj)
			{
				return true;
			}

			/**
			 * Make sure there is only one listener per derived markup
			 * 
			 * @see java.lang.Object#hashCode()
			 */
			@Override
			public int hashCode()
			{
				return markup.getResource().getCacheKey().hashCode();
			}
		});

		// Merge base and derived markup
		final IMarkup mergedMarkup = new MergedMarkup(markup, baseMarkup, extendIndex);
		return mergedMarkup;
	}

	/**
	 * Check if markup contains &lt;wicket:extend&gt; which tells us that we
	 * need to read the inherited markup as well. &lt;wicket:extend&gt; MUST BE
	 * the first wicket tag in the markup. Skip raw markup
	 * 
	 * @param markup
	 * @return == 0, if no wicket:extend was found
	 */
	private int requiresBaseMarkup(final IMarkup markup)
	{
		for (int i = 0; i < markup.size(); i++)
		{
			final MarkupElement elem = markup.get(i);
			if ((elem instanceof ComponentTag) && ((ComponentTag)elem).isExtendTag())
			{
				// Ok, inheritance is on and we must get the
				// inherited markup as well.
				return i;
			}
		}
		return -1;
	}
}
