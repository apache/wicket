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
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to load markup either from from cache or from a resource. In case of markup inheritance,
 * merging the markup is managed transparently.
 * 
 * @author Juergen Donnerstag
 */
public class MarkupFactory
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(MarkupFactory.class);

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
	 * MarkupLoaders are responsible to find and load the markup for a component. That may be a
	 * single file, but e.g. like in markup inheritance it could also be that the markup from
	 * different sources must be merged.
	 * 
	 * @return By default an instance of {@link DefaultMarkupLoader} will be returned. Via
	 *         subclassing you may return your markup loader..
	 */
	public IMarkupLoader getMarkupLoader()
	{
		return new DefaultMarkupLoader();
	}

	/**
	 * Create a new markup parser. Markup parsers read the markup and dissect it in Wicket relevant
	 * pieces {@link MarkupElement}'s (kind of Wicket's DOM).
	 * <p>
	 * MarkupParser's can be extended by means of {@link IMarkupFilter}. You can add your own filter
	 * as follows:
	 * 
	 * <pre>
	 *    public MyMarkupFactory {
	 *      ...
	 *      public MarkupParser newMarkupParser(final MarkupResourceStream resource) {
	 *         MarkupParser parser = super.newMarkupParser(resource);
	 *         parser.add(new MyFilter());
	 *         return parser;
	 *      }
	 *    }
	 * </pre>
	 * 
	 * @see #onAppendMarkupFilter(IMarkupFilter)
	 * 
	 * @param resource
	 *            The resource containing the markup
	 * @return A fresh instance of {@link MarkupParser}
	 */
	public MarkupParser newMarkupParser(final MarkupResourceStream resource)
	{
		// Markup parsers can not be re-used
		return new MarkupParser(new XmlPullParser(), resource)
		{
			/**
			 * @see org.apache.wicket.markup.MarkupParser#onAppendMarkupFilter(org.apache.wicket.markup.parser.IMarkupFilter)
			 */
			@Override
			protected IMarkupFilter onAppendMarkupFilter(final IMarkupFilter filter)
			{
				return MarkupFactory.this.onAppendMarkupFilter(filter);
			}
		};
	}

	/**
	 * A callback method that is invoked prior to any {@link IMarkupFilter} being registered with
	 * {@link MarkupParser}. Hence it allows to:
	 * <ul>
	 * <li>tweak the default configuration of a filter</li>
	 * <li>replace a filter with another one</li>
	 * <li>avoid filters being used by returning null</li>
	 * </ul>
	 * Note that a new {@link MarkupParser} instance is created for each markup resources being
	 * loaded.
	 * <p>
	 * 
	 * @param filter
	 *            The filter to be registered with the MarkupParser
	 * @return The filter to be added. Null to ignore.
	 */
	protected IMarkupFilter onAppendMarkupFilter(final IMarkupFilter filter)
	{
		return filter;
	}

	/**
	 * Get the markup cache which is registered with the factory. Since the factory is registered
	 * with the application, only one cache per application exists.
	 * <p>
	 * Please note that markup cache is a pull through cache. It'll invoke a factory method
	 * {@link #getMarkupResourceStream(MarkupContainer, Class)} to load the markup if not yet
	 * available in the cache.
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
	 * @return <code>true</code> if markup cache is available. Make sure you called
	 *         {@link #getMarkupCache()} at least once before to initialize the cache.
	 */
	public boolean hasMarkupCache()
	{
		return markupCache != null;
	}

	/**
	 * Get the markup associated with the container.
	 * 
	 * @param container
	 *            The container to find the markup for
	 * @param enforceReload
	 *            If true, the cache will be ignored and all, including inherited markup files, will
	 *            be reloaded. Whatever is in the cache, it will be ignored
	 * @return The markup associated with the container
	 */
	public final Markup getMarkup(final MarkupContainer container, final boolean enforceReload)
	{
		return getMarkup(container, container.getClass(), enforceReload);
	}

	/**
	 * Get the markup associated with the container.
	 * 
	 * @param container
	 *            The container to find the markup for
	 * @param clazz
	 *            Must be the container class or any of its super classes.
	 * @param enforceReload
	 *            The cache will be ignored and all, including inherited markup files, will be
	 *            reloaded. Whatever is in the cache, it will be ignored
	 * @return The markup associated with the container
	 */
	public final Markup getMarkup(final MarkupContainer container, final Class<?> clazz,
		final boolean enforceReload)
	{
		Args.notNull(container, "container");
		Args.notNull(clazz, "clazz");

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
	 *            The container to find the markup for
	 * @return True if this markup container has associated markup
	 */
	public final boolean hasAssociatedMarkup(final MarkupContainer container)
	{
		return getMarkup(container, false) != Markup.NO_MARKUP;
	}

	/**
	 * Get the markup resource stream provider registered with the factory.
	 * <p>
	 * If the 'container' implements {@link IMarkupResourceStreamProvider}, the container itself
	 * will be asked to provide the resource stream. Else Wicket's default implementation will be
	 * used.
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
