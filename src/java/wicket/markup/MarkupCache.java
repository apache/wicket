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
package wicket.markup;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.util.listener.IChangeListener;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.watch.ModificationWatcher;

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

	/** Map of markup tags by class. */
	private final Map markupCache = new HashMap();

	/** the Wicket application */
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
	 * @param clazz
	 *            The class to get the associated markup for. If null, the the
	 *            container's class is used, but it can be a parent class of
	 *            container as well.
	 * 
	 * @return A stream of MarkupElement elements
	 */
	public final MarkupStream getMarkupStream(final MarkupContainer container, final Class clazz)
	{
		return getMarkupStream(container, clazz, true);
	}

	/**
	 * Gets a fresh markup stream that contains the (immutable) markup resource
	 * for this class.
	 * 
	 * @param container
	 *            The container the markup should be associated with
	 * @param clazz
	 *            The class to get the associated markup for. If null, the the
	 *            container's class is used, but it can be a parent class of
	 *            container as well.
	 * @param throwException
	 * 
	 * @return A stream of MarkupElement elements
	 */
	public final MarkupStream getMarkupStream(final MarkupContainer container, final Class clazz, final boolean throwException)
	{
		// Look for associated markup
		final Markup markup = getMarkup(container, clazz);

		// If we found markup for this container
		if (markup != Markup.NO_MARKUP)
		{
			// return a MarkupStream for the markup
			return new MarkupStream(markup);
		}
		
	    if (throwException == true)
		{
			// throw exception since there is no associated markup
			throw new WicketRuntimeException(
					"Markup not found. Component class: "
							+ (clazz != null ? clazz.getName() : container.getClass().getName())
							+ " Enable debug messages for wicket.util.resource.Resource to get a list of all filenames tried.");
		}
	    
	    return null;
	}

	/**
	 * Gets a fresh markup stream that contains the (immutable) markup resource
	 * for this class its head markup
	 * 
	 * @param container
	 *            The container the head markup should be associated with
	 * @param clazz
	 *            The class to get the associated head markup for. If null, the the
	 *            container's class is used, but it can be a parent class of
	 *            container as well.
	 * 
	 * @return A stream of MarkupElement elements
	 */
	public final MarkupStream getHeadMarkupStream(final MarkupContainer container, final Class clazz)
	{
		// TODO IMPL
		return null;
	}
	
	/**
	 * @param container
	 * @param clazz
	 * @return True if this markup container has associated markup
	 */
	public final boolean hasAssociatedMarkup(final MarkupContainer container, final Class clazz)
	{
		return getMarkup(container, clazz) != Markup.NO_MARKUP;
	}

	/**
	 * Gets any (immutable) markup resource for this class.
	 * 
	 * @param container
	 *            The container the markup should be associated with
	 * @param clazz
	 *            The class to get the associated markup for. If null, the the
	 *            container's class is used, but it can be a parent class of
	 *            container as well.
	 * @return Markup resource
	 */
	// This method is already prepared for markup inheritance
	private final Markup getMarkup(final MarkupContainer container, Class clazz)
	{
		if (clazz == null)
		{
			clazz = container.getClass();
		}
		else
		{
			if (!clazz.isInstance(container))
			{
				throw new WicketRuntimeException("Parameter clazz must be instance of container");
			}
		}

		synchronized (markupCache)
		{
			// Look up markup tag list by class, locale, style and markup type
			final String key = markupKey(container, clazz);
			Markup markup = (Markup)markupCache.get(key);

			// If no markup in map
			if (markup == null)
			{
				// Locate markup resource, searching up class hierarchy
				IResourceStream markupResource = null;
				Class containerClass = clazz;

				while ((markupResource == null) && (containerClass != MarkupContainer.class))
				{
				    clazz = containerClass;
				    
					// Look for markup resource for containerClass
					markupResource = application.getResourceStreamLocator().locate(containerClass,
							container.getStyle(), container.getLocale(), container.getMarkupType());
					containerClass = containerClass.getSuperclass();
				}

				// Found markup?
				if (markupResource != null)
				{
					// load the markup and watch for changes
					markup = loadMarkupAndWatchForChanges(key, markupResource, clazz);
				}
				else
				{
					// flag markup as non-existent (as opposed to null, which
					// might mean that it's simply not loaded into the cache)
					markup = Markup.NO_MARKUP;
				}

				// Save any markup list (or absence of one) for next time
				markupCache.put(key, markup);
			}

			return markup;
		}
	}

	/**
	 * Loads markup from a resource stream.
	 * 
	 * @param key
	 *            Key under which markup should be cached
	 * @param markupResourceStream
	 *            The markup resource stream to load
	 * @param containerClass
	 *            The Class the associated stream is directly associated
	 * @return The markup
	 */
	private final Markup loadMarkup(final String key, final IResourceStream markupResourceStream,
	        final Class containerClass)
	{
		try
		{
			final Markup markup = application.newMarkupParser().readAndParse(markupResourceStream);
			markup.setContainerClass(containerClass);
			synchronized (markupCache)
			{
				markupCache.put(key, markup);
			}
			return markup;
		}
		catch (ParseException e)
		{
			synchronized (markupCache)
			{
				markupCache.remove(key);
			}
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
	 * @param containerClass
	 *            The Class the associated stream is directly associated
	 * @return The markup in the stream
	 */
	private final Markup loadMarkupAndWatchForChanges(final String key,
			final IResourceStream markupResourceStream, final Class containerClass)
	{
		// Watch file in the future
		final ModificationWatcher watcher = application.getResourceWatcher();
		if (watcher != null)
		{
			watcher.add(markupResourceStream, new IChangeListener()
			{
				public void onChange()
				{
					log.info("Reloading markup from " + markupResourceStream);
					loadMarkup(key, markupResourceStream, containerClass);
				}
			});
		}

		log.info("Loading markup from " + markupResourceStream);
		return loadMarkup(key, markupResourceStream, containerClass);
	}

	/**
	 * @param container
	 * @param clazz
	 *            The clazz to get the key for
	 * @return Key that uniquely identifies any markup that might be associated
	 *         with this markup container.
	 */
	private final String markupKey(final MarkupContainer container, final Class clazz)
	{
		final String classname = clazz.getName();
		final Locale locale = container.getLocale();
		final String style = container.getStyle();
		final String markupType = container.getMarkupType();
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
	    this.markupCache.clear();
	}
}
