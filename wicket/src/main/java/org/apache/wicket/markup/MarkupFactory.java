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

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.loader.DefaultMarkupLoader;
import org.apache.wicket.markup.loader.IMarkupLoader;
import org.apache.wicket.markup.parser.IMarkupFilter;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Markup loading essentially is an autark modul of Wicket. MarkupFactory provides all the means to
 * change defaults.
 * 
 * @author Juergen Donnerstag
 */
public class MarkupFactory
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(MarkupFactory.class);

	/** The markup loader instance */
	private IMarkupLoader markupLoader;

	/** A markup cache which will load the markup if required. */
	private IMarkupCache markupCache;

	/** The markup resource stream provider used by MarkupCache */
	private IMarkupResourceStreamProvider markupResourceStreamProvider;

	/**
	 * @return The markup factory associated with the application
	 */
	public final static MarkupFactory get()
	{
		return Application.get().getMarkupSettings().getMarkupFactory();
	}

	/**
	 * Construct.
	 */
	public MarkupFactory()
	{
	}

	/**
	 * In case there is a need to extend the default chain of MarkupLoaders
	 * 
	 * @return MarkupLoader
	 */
	public IMarkupLoader getMarkupLoader()
	{
		if (markupLoader == null)
		{
			markupLoader = new DefaultMarkupLoader();
		}
		return markupLoader;
	}

	/**
	 * Create a new markup parser.
	 * <p>
	 * In case you want to add you own markup filters, than subclass the method and call
	 * {@link WicketMarkupParser#add(IMarkupFilter)} for your own filter on the markup parser
	 * returned.
	 * 
	 * @param resource
	 * @return A new markup parser
	 */
	public MarkupParser newMarkupParser(final MarkupResourceStream resource)
	{
		// Markup parsers can not be re-used
		return new MarkupParser(new XmlPullParser(), resource)
		{
			/**
			 * @see org.apache.wicket.markup.WicketMarkupParser#onAppendMarkupFilter(org.apache.wicket.markup.parser.IMarkupFilter)
			 */
			@Override
			protected IMarkupFilter onAppendMarkupFilter(final IMarkupFilter filter)
			{
				return MarkupFactory.this.onAppendMarkupFilter(filter);
			}
		};
	}

	/**
	 * a) Allow subclasses to configure individual Wicket filters
	 * <p>
	 * b) Allow to replace default filter with extended one
	 * <p>
	 * c) Allows to disable Wicket filters via returning false
	 * 
	 * @param filter
	 * @return The filter to be added. Null to ignore.
	 */
	protected IMarkupFilter onAppendMarkupFilter(final IMarkupFilter filter)
	{
		return filter;
	}

	/**
	 * The markup cache also loads the markup if not yet available in the cache.
	 * 
	 * @return Null, to disable caching.
	 */
	public IMarkupCache getMarkupCache()
	{
		if (markupCache == null)
		{
			markupCache = new MarkupCache();
		}

		return markupCache;
	}

	/**
	 * Gets a fresh markup stream that contains the (immutable) markup resource for this class.
	 * 
	 * @param container
	 *            The container the markup should be associated with
	 * @param enforceReload
	 *            The cache will be ignored and all, including inherited markup files, will be
	 *            reloaded. Whatever is in the cache, it will be ignored
	 * @return A stream of MarkupElement elements
	 */
	public final IMarkupFragment getMarkup(final MarkupContainer container,
		final boolean enforceReload)
	{
		return getMarkup(container, container.getClass(), enforceReload);
	}

	/**
	 * Gets a fresh markup stream that contains the (immutable) markup resource for this class.
	 * 
	 * @param container
	 *            The container the markup should be associated with
	 * @param clazz
	 *            Must be the container class or any of its super classes.
	 * @param enforceReload
	 *            The cache will be ignored and all, including inherited markup files, will be
	 *            reloaded. Whatever is in the cache, it will be ignored
	 * @return A stream of MarkupElement elements
	 */
	public final Markup getMarkup(final MarkupContainer container, final Class<?> clazz,
		final boolean enforceReload)
	{
		Checks.argumentNotNull(container, "container");
		Checks.argumentNotNull(clazz, "clazz");

		IMarkupCache cache = getMarkupCache();
		if (cache != null)
		{
			// MarkupCache acts as pull-through cache. It'll call the same loadMarkup() method as
			// below, if needed.
			return cache.getMarkup(container, clazz, enforceReload);
		}

		return loadMarkup(container, null, enforceReload);
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
		return getMarkup(container, false) != Markup.NO_MARKUP;
	}

	/**
	 * Get the markup resource stream provider to be used
	 * 
	 * @param container
	 *            The MarkupContainer requesting the markup resource stream
	 * @return IMarkupResourceStreamProvider
	 */
	protected final IMarkupResourceStreamProvider getMarkupResourceStreamProvider(
		final MarkupContainer container)
	{
		if (container instanceof IMarkupResourceStreamProvider)
		{
			return (IMarkupResourceStreamProvider)container;
		}

		if (markupResourceStreamProvider == null)
		{
			markupResourceStreamProvider = new DefaultMarkupResourceStreamProvider();
		}
		return markupResourceStreamProvider;
	}

	/**
	 * Create a new markup resource stream for the container.
	 * <p>
	 * Note: usually it will only called once, as the IResourceStream will be cached by MarkupCache.
	 * 
	 * @param container
	 *            The MarkupContainer which requests to load the Markup resource stream
	 * @param clazz
	 *            Either the container class or any super class
	 * @return A IResourceStream if the resource was found
	 */
	public final MarkupResourceStream getMarkupResourceStream(final MarkupContainer container,
		Class<?> clazz)
	{
		Class<?> containerClass = clazz;
		if (clazz == null)
		{
			containerClass = container.getClass();
		}
		else if (!clazz.isAssignableFrom(container.getClass()))
		{
			throw new WicketRuntimeException("Parameter clazz must be an instance of " +
				container.getClass().getName() + ", but is a " + clazz.getName());
		}

		// Who is going to provide the markup resource stream?
		// And ask the provider to locate the markup resource stream
		final IResourceStream resourceStream = getMarkupResourceStreamProvider(container).getMarkupResourceStream(
			container, containerClass);

		// Found markup?
		if (resourceStream == null)
		{
			return null;
		}
		if (resourceStream instanceof MarkupResourceStream)
		{
			return (MarkupResourceStream)resourceStream;
		}
		return new MarkupResourceStream(resourceStream, new ContainerInfo(container),
			containerClass);
	}

	/**
	 * Loads markup from a resource stream.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markupResourceStream
	 *            The markup resource stream to load. May be null.
	 * @param enforceReload
	 *            The cache will be ignored and all, including inherited markup files, will be
	 *            reloaded. Whatever is in the cache, it will be ignored
	 * @return The markup
	 */
	public final Markup loadMarkup(final MarkupContainer container,
		MarkupResourceStream markupResourceStream, final boolean enforceReload)
	{
		if (markupResourceStream == null)
		{
			markupResourceStream = getMarkupResourceStream(container, null);
		}

		try
		{
			return getMarkupLoader().loadMarkup(container, markupResourceStream, null,
				enforceReload);
		}
		catch (ResourceStreamNotFoundException e)
		{
			log.error("Unable to find markup from " + markupResourceStream, e);
		}
		catch (IOException e)
		{
			log.error("Unable to read markup from " + markupResourceStream, e);
		}
		return null;
	}
}
