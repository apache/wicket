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
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.util.concurrent.ConcurrentHashMap;
import wicket.util.listener.IChangeListener;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.string.AppendingStringBuffer;
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
		return getMarkup(container, container.getClass()) != Markup.NO_MARKUP;
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

		// Look up markup tag list by class, locale, style and markup type
		final CharSequence key = markupKey(container, clazz);
		Markup markup = (Markup)markupCache.get(key);

		// If no markup in the cache
		if (markup == null)
		{
			synchronized (markupCache)
			{
				markup = (Markup)markupCache.get(key);

				// If no markup is in the cache
				if (markup == null)
				{
					// Ask the container to locate its associated markup
					final IResourceStream resourceStream = container
							.newMarkupResourceStream(containerClass);

					// Found markup?
					if (resourceStream != null)
					{
						final MarkupResourceStream markupResource;
						if (resourceStream instanceof MarkupResourceStream)
						{
							markupResource = (MarkupResourceStream)resourceStream;
						}
						else
						{
							markupResource = new MarkupResourceStream(resourceStream,
									new ContainerInfo(container), containerClass);
						}

						// load the markup and watch for changes
						markup = loadMarkupAndWatchForChanges(container, key, markupResource);
					}
					else
					{
						// flag markup as non-existent (as opposed to null,
						// which might mean that it's simply not loaded into
						// the cache)
						markup = Markup.NO_MARKUP;

						// Save any markup list (or absence of one) for next
						// time
						markupCache.put(key, markup);
					}
				}
			}
		}
		return markup;
	}

	/**
	 * Remove the markup from the cache and trigger all associated listeners
	 * 
	 * @param key
	 *            The cache key
	 * @param markupResourceStream
	 *            The resource stream
	 */
	private void removeMarkup(final CharSequence key,
			final MarkupResourceStream markupResourceStream)
	{
		markupCache.remove(key);

		// trigger all listeners registered on the markup that is removed
		afterLoadListeners.notifyListeners(markupResourceStream);
		afterLoadListeners.remove(markupResourceStream);
	}

	/**
	 * Remove the markup from the cache and trigger all associated listeners
	 * 
	 * @since 1.2.3
	 * @param markupResourceStream
	 *            The resource stream
	 */
	public void removeMarkup(final MarkupResourceStream markupResourceStream)
	{
		CharSequence key = null;
		Iterator iter = this.markupCache.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry)iter.next();
			if (entry.getValue() == markupResourceStream)
			{
				key = (CharSequence)entry.getKey();
				break;
			}
		}

		if (key != null)
		{
			removeMarkup(key, markupResourceStream);
		}
	}

	/**
	 * Loads markup from a resource stream.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param key
	 *            Key under which markup should be cached
	 * @param markupResourceStream
	 *            The markup resource stream to load
	 * @return The markup
	 */
	private final Markup loadMarkup(final MarkupContainer container, final CharSequence key,
			final MarkupResourceStream markupResourceStream)
	{
		try
		{
			// read and parse the markup
			Markup markup = application.getMarkupSettings().getMarkupParserFactory()
					.newMarkupParser().readAndParse(markupResourceStream);

			// Check for markup inheritance. If it contains <wicket:extend>
			// the two markups get merged.
			markup = checkForMarkupInheritance(container, key, markup);

			// add the markup to the cache
			markupCache.put(key, markup);

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

		synchronized (markupCache)
		{
			markupCache.remove(key);
			afterLoadListeners.remove(markupResourceStream);
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
	 * @param key
	 *            The key for the resource
	 * @param markupResourceStream
	 *            The markup stream to load and begin to watch
	 * @return The markup in the stream
	 */
	private final Markup loadMarkupAndWatchForChanges(final MarkupContainer container,
			final CharSequence key, final MarkupResourceStream markupResourceStream)
	{
		// Watch file in the future
		final ModificationWatcher watcher = application.getResourceSettings().getResourceWatcher();
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
					removeMarkup(key, markupResourceStream);
					watcher.remove(markupResourceStream);
				}
			});
		}

		if (log.isDebugEnabled())
		{
			log.debug("Loading markup from " + markupResourceStream);
		}
		return loadMarkup(container, key, markupResourceStream);
	}

	/**
	 * Construct a proper key value for the cache
	 * 
	 * @param container
	 *            The container requesting the markup
	 * @param clazz
	 *            The clazz to get the key for
	 * @return Key that uniquely identifies any markup that might be associated
	 *         with this markup container.
	 */
	private final CharSequence markupKey(final MarkupContainer container, final Class clazz)
	{
		final String classname = clazz.getName();
		final Locale locale = container.getLocale();
		final String style = container.getStyle();
		final String markupType = container.getMarkupType();

		final AppendingStringBuffer buffer = new AppendingStringBuffer(classname.length() + 32);
		buffer.append(classname);

		if (locale != null)
		{
			boolean l = locale.getLanguage().length() != 0;
			boolean c = locale.getCountry().length() != 0;
			boolean v = locale.getVariant().length() != 0;
			buffer.append(locale.getLanguage());
			if (c || (l && v))
			{
				buffer.append('_').append(locale.getCountry()); // This may just
				// append '_'
			}
			if (v && (l || c))
			{
				buffer.append('_').append(locale.getVariant());
			}
		}
		if (style != null)
		{
			buffer.append(style);
		}

		buffer.append(markupType);
		return buffer;
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
	 * The markup has just been loaded and now we check if markup inheritance
	 * applies, which is if <wicket:extend> is found in the markup. If yes, than
	 * load the base markups and merge the markup elements to create an updated
	 * (merged) list of markup elements.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param key
	 *            Key under which markup should be cached
	 * @param markup
	 *            The markup to checked for inheritance
	 * @return A markup object with the the base markup elements resolved.
	 */
	private Markup checkForMarkupInheritance(final MarkupContainer container,
			final CharSequence key, final Markup markup)
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
				removeMarkup(key, markup.getResource());
			}

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
		});

		// Merge base and derived markup
		Markup mergedMarkup = new MergedMarkup(markup, baseMarkup, extendIndex);
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
}
