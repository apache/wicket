/*
 * $Id$ $Revision:
 * 1.20 $ $Date$
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
import java.text.ParseException;
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
import wicket.util.watch.ModificationWatcher;
import wicket.util.watch.Watcher;

/**
 * Load markup and cache it for fast retrieval. If markup file changes, it'll be
 * automatically reloaded.
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
		Markup markup = getMarkup(new ContainerInfo(container), container.getClass());

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
		return getMarkup(new ContainerInfo(container), container.getClass()) != Markup.NO_MARKUP;
	}

	/**
	 * Create a new markup resource stream
	 * 
	 * @param containerClass
	 *            The container the markup should be associated with
	 * @param containerInfo
	 *            The container the markup should be associated with
	 * @return A IResourceStream if the resource was found
	 */
	private MarkupResourceStream newMarkupResourceStream(final Class containerClass,
			final ContainerInfo containerInfo)
	{
		IResourceStream markupResource = application.getResourceSettings()
				.getResourceStreamLocator().locate(containerClass, containerInfo.getStyle(),
						containerInfo.getLocale(), containerInfo.getFileExtension());

		if (markupResource == null)
		{
			return null;
		}

		return new MarkupResourceStream(markupResource, containerInfo, containerClass);
	}

	/**
	 * Gets any (immutable) markup resource for the container or any of its
	 * parent classes (markup inheritance)
	 * 
	 * @param containerInfo
	 *            The container the markup should be associated with
	 * @param clazz
	 *            The class to get the associated markup for. If null, the
	 *            container's class is used, but it can be a parent class of the
	 *            container as well (markup inheritance)
	 * @return Markup resource
	 */
	private final Markup getMarkup(final ContainerInfo containerInfo, final Class clazz)
	{
		Class containerClass = clazz;
		if (clazz == null)
		{
			containerClass = containerInfo.getContainerClass();
		}
		else
		{
			if (!clazz.isAssignableFrom(containerInfo.getContainerClass()))
			{
				throw new WicketRuntimeException("Parameter clazz must be instance of container");
			}
		}

		// Look up markup tag list by class, locale, style and markup type
		final String key = markupKey(containerInfo, clazz);
		Markup markup = (Markup)markupCache.get(key);

		// If no markup in the cache
		if (markup == null)
		{
			synchronized (markupCache)
			{
				markup = (Markup)markupCache.get(key);
	
				// If no markup in the cache
				if (markup == null)
				{
					// Locate markup resource, searching up class hierarchy
					MarkupResourceStream markupResource = null;
					while ((markupResource == null) && (containerClass != MarkupContainer.class))
					{
						// Look for markup resource for containerClass
						markupResource = newMarkupResourceStream(containerClass, containerInfo);
	
						containerClass = containerClass.getSuperclass();
					}
	
					// Found markup?
					if (markupResource != null)
					{
						// load the markup and watch for changes
						markup = loadMarkupAndWatchForChanges(key, markupResource);
					}
					else
					{
						// flag markup as non-existent (as opposed to null, which
						// might mean that it's simply not loaded into the cache)
						markup = Markup.NO_MARKUP;
	
						// Save any markup list (or absence of one) for next time
						markupCache.put(key, markup);
					}
				}
			}
		}
		return markup;
	}

	/**
	 * Loads markup from a resource stream.
	 * 
	 * @param key
	 *            Key under which markup should be cached
	 * @param markupResourceStream
	 *            The markup resource stream to load
	 * @return The markup
	 */
	private final Markup loadMarkup(final String key,
			final MarkupResourceStream markupResourceStream)
	{
		try
		{
			// read and parse the markup
			Markup markup = application.getMarkupSettings().getMarkupParserFactory()
					.newMarkupParser().readAndParse(markupResourceStream);

			// Check for markup inheritance. If it contains <wicket:extend>
			// the two markups get merged.
			markup = checkForMarkupInheritance(key, markup);

			// add the markup to the cache
			markupCache.put(key, markup);

			// trigger all listeners registered on the markup just loaded
			afterLoadListeners.notifyListeners(markupResourceStream);

			return markup;
		}
		catch (ParseException e)
		{
			log.error("Unable to parse markup from " + markupResourceStream, e);
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
	 * the {@link ModificationWatcher}so that if the resource changes, we can
	 * reload it automatically.
	 * 
	 * @param key
	 *            The key for the resource
	 * @param markupResourceStream
	 *            The markup stream to load and begin to watch
	 * @return The markup in the stream
	 */
	private final Markup loadMarkupAndWatchForChanges(final String key,
			final MarkupResourceStream markupResourceStream)
	{
		// Watch file in the future
		final ModificationWatcher watcher = application.getResourceSettings().getResourceWatcher();
		if (watcher != null)
		{
			watcher.add(markupResourceStream, new IChangeListener()
			{
				public void onChange()
				{
					log.info("Reloading markup from " + markupResourceStream);
					loadMarkup(key, markupResourceStream);
				}
			});
		}

		log.info("Loading markup from " + markupResourceStream);
		return loadMarkup(key, markupResourceStream);
	}

	/**
	 * Construct a proper key value for the cache
	 * 
	 * @param containerInfo
	 *            The container requesting the markup
	 * @param clazz
	 *            The clazz to get the key for
	 * @return Key that uniquely identifies any markup that might be associated
	 *         with this markup container.
	 */
	private final String markupKey(final ContainerInfo containerInfo, final Class clazz)
	{
		final String classname = clazz.getName();
		final Locale locale = containerInfo.getLocale();
		final String style = containerInfo.getStyle();
		final String markupType = containerInfo.getFileExtension();

		final StringBuffer buffer = new StringBuffer(classname.length() + 32);
		buffer.append(classname);

		if (locale != null)
		{
			buffer.append(locale.toString());
		}

		if (style != null)
		{
			buffer.append(style);
		}

		buffer.append(markupType);
		return buffer.toString();
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
	 * @param key
	 *            Key under which markup should be cached
	 * @param markup
	 *            The markup to checked for inheritance
	 * @return A markup object with the the base markup elements resolved.
	 */
	private Markup checkForMarkupInheritance(final String key, final Markup markup)
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
		final Markup baseMarkup = getMarkup(markup.getResource().getContainerInfo(), markup
				.getResource().getMarkupClass().getSuperclass());

		if (baseMarkup == Markup.NO_MARKUP)
		{
			throw new MarkupNotFoundException(
					"Parent markup of inherited markup not found. Component class: "
							+ markup.getResource().getContainerInfo().getContainerClass().getName()
							+ " Enable debug messages for wicket.util.resource.Resource to get a list of all filenames tried.");
		}

		// register an after-load listener for base markup. The listener
		// implementation will reload the derived markup which must be merged
		// with the base markup
		afterLoadListeners.add(baseMarkup.getResource(), new IChangeListener()
		{
			public void onChange()
			{
				log.info("Reloading derived markup from " + markup.getResource());
				loadMarkup(key, markup.getResource());
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
		// TODO General: The merged markup resource modify time is wrong. It
		// should be the latest of all resources involved.
		Markup mergedMarkup = InheritedMarkupMerger.mergeMarkups(markup, baseMarkup, extendIndex);
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
			if (markup.get(i) instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag)markup.get(i);
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
