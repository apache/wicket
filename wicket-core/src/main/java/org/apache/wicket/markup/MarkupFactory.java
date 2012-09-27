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
import org.apache.wicket.markup.parser.IXmlPullParser;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to load markup either from cache or from a resource.
 * <p>
 * This class is the main entry point to load markup. Nothing else should be required by Components.
 * It manages caching markup as well as loading and merging (inheritance) of markup.
 * <p>
 * The markup returned is immutable as it gets re-used across multiple Component instances.
 * 
 * @author Juergen Donnerstag
 */
public class MarkupFactory
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(MarkupFactory.class);

	/** A markup cache */
	private IMarkupCache markupCache = null;

	/** The markup resource stream provider used by MarkupCache */
	private IMarkupResourceStreamProvider markupResourceStreamProvider = null;

	/**
	 * @return Gets the markup factory registered with the Wicket application
	 */
	public static MarkupFactory get()
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
	 *         subclassing you may return your own markup loader (chain).
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
		return new MarkupParser(newXmlPullParser(), resource)
		{
			@Override
			protected IMarkupFilter onAppendMarkupFilter(final IMarkupFilter filter)
			{
				return MarkupFactory.this.onAppendMarkupFilter(filter);
			}
		};
	}

	/**
	 * Subclasses can override this to use custom parsers.
	 * 
	 * @return parser instance used by {@link MarkupParser} to parse markup.
	 */
	protected IXmlPullParser newXmlPullParser()
	{
		return new XmlPullParser();
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
	 * @return The markup associated with the container. Null, if the markup was not found or could
	 *         not yet be loaded (e.g. getMarkupType() == null). Wicket Exception in case of errors.
	 */
	public final Markup getMarkup(final MarkupContainer container, final boolean enforceReload)
	{
		return getMarkup(container, container.getClass(), enforceReload);
	}

	/**
	 * Get the markup associated with the container. Check the cache first. If not found, than load
	 * the markup and update the cache.
	 * <p>
	 * The clazz parameter usually can be null, except for base (inherited) markup.
	 * <p>
	 * There are several means to disable markup caching. Caching can be disabled alltogether -
	 * getMarkupCache() return null -, or individually (cacheKey == null).
	 * 
	 * @param container
	 *            The container to find the markup for
	 * @param clazz
	 *            Must be the container class or any of its super classes. May be null.
	 * @param enforceReload
	 *            The cache will be ignored and all, including inherited markup files, will be
	 *            reloaded. Whatever is in the cache, it will be ignored
	 * @return The markup associated with the container. Null, if the markup was not found or could
	 *         not yet be loaded (e.g. getMarkupType() == null). Wicket Exception in case of errors.
	 */
	public final Markup getMarkup(final MarkupContainer container, final Class<?> clazz,
		final boolean enforceReload)
	{
		Args.notNull(container, "container");

		if (checkMarkupType(container) == false)
		{
			// TODO improve: Result { boolean success, enum FailureReason {not found, not yet
			// available}, Markup markup }
			return null;
		}

		Class<?> containerClass = getContainerClass(container, clazz);

		IMarkupCache cache = getMarkupCache();
		if (cache != null)
		{
			// MarkupCache acts as pull-through cache. It'll call the same loadMarkup() method as
			// below, if needed.
			// @TODO may be that can be changed. I don't like it too much.
			return cache.getMarkup(container, containerClass, enforceReload);
		}

		// Get the markup resource stream for the container (and super class)
		MarkupResourceStream markupResourceStream = getMarkupResourceStream(container,
			containerClass);

		return loadMarkup(container, markupResourceStream, enforceReload);
	}

	/**
	 * Without a markup type we can not search for a file and we can not construct the cacheKey. We
	 * can not even load associated markup as required for Panels. Though every MarkupContainer can
	 * provide it's own type, by default they refer to the Page. Hence, no markup type is an
	 * indicator, that the component or any of its parents, has not yet been added.
	 * 
	 * @param container
	 *          The MarkupContainer which markup type has to checked
	 * @return true, if container.getMarkupType() != null
	 */
	protected final boolean checkMarkupType(final MarkupContainer container)
	{
		if (container.getMarkupType() == null)
		{
			log.debug("Markup file not loaded, since the markup type is not yet available: {}", container);
			return false;
		}

		return true;
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
	 * Create a new markup resource stream for the container and optionally the Class. The Class
	 * must be provided in case of base (inherited) markup. Else it might be null (standard use
	 * case).
	 * 
	 * @param container
	 *            The MarkupContainer which requests to load the Markup resource stream
	 * @param clazz
	 *            Either the container class or any super class. Might be null.
	 * @return A IResourceStream if the resource was found
	 */
	public final MarkupResourceStream getMarkupResourceStream(final MarkupContainer container,
		Class<?> clazz)
	{
		Args.notNull(container, "container");

		if (checkMarkupType(container) == false)
		{
			// TODO improve: Result { boolean success, enum FailureReason {not found, not yet
			// available}, Markup markup }
			return null;
		}

		Class<?> containerClass = getContainerClass(container, clazz);

		// Who is going to provide the markup resource stream?
		// And ask the provider to locate the markup resource stream
		final IResourceStream resourceStream = getMarkupResourceStreamProvider(container).getMarkupResourceStream(
			container, containerClass);

		// Found markup?
		if (resourceStream == null)
		{
			// TODO improve: Result { boolean success, enum FailureReason {not found, not yet
			// available}, Markup markup }
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
	 * Gets and checks the container class
	 * 
	 * @param container
	 *            The MarkupContainer which requests to load the Markup resource stream
	 * @param clazz
	 *            Either null, or a super class of container
	 * @return The container class to be used
	 */
	public final Class<?> getContainerClass(final MarkupContainer container, final Class<?> clazz)
	{
		Args.notNull(container, "container");

		Class<?> containerClass = clazz;
		if (clazz == null)
		{
			containerClass = container.getClass();
		}
		else if (!clazz.isAssignableFrom(container.getClass()))
		{
			throw new IllegalArgumentException("Parameter clazz must be an instance of " +
				container.getClass().getName() + ", but is a " + clazz.getName());
		}
		return containerClass;
	}

	/**
	 * Loads markup from a resource stream. It'll call the registered markup loader to load the
	 * markup.
	 * <p>
	 * Though the 'enforceReload' attribute seem to imply that the cache is consulted to retrieve
	 * the markup, the cache in fact is only checked for retrieving the base (inherited) markup.
	 * Please see {@link #getMarkup(MarkupContainer, boolean)} as well.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markupResourceStream
	 *            The markup resource stream to load, if already known.
	 * @param enforceReload
	 *            The cache will be ignored and all, including inherited markup files, will be
	 *            reloaded. Whatever is in the cache, it will be ignored
	 * @return The markup. Null, if the markup was not found. Wicket Exception in case of errors.
	 */
	public final Markup loadMarkup(final MarkupContainer container,
		final MarkupResourceStream markupResourceStream, final boolean enforceReload)
	{
		// @TODO can markupResourceStream be replace with clazz???
		Args.notNull(container, "container");
		Args.notNull(markupResourceStream, "markupResourceStream");

		if (checkMarkupType(container) == false)
		{
			// TODO improve: Result { boolean success, enum FailureReason {not found, not yet
			// available}, Markup markup }
			return null;
		}

		try
		{
			// The InheritedMarkupMarkupLoader needs to load the base markup. It'll do it via
			// MarkupFactory.getMarkup() as main entry point, which in turn allows to choose between
			// use or ignore the cache. That's why we need to propagate enforceReload to the markup
			// loader as well.

			// Markup loader is responsible to load the full markup for the container. In case of
			// markup inheritance, the markup must be merged from different markup files. It is the
			// merged markup which eventually will be cached, thus avoiding repetitive merge
			// operations, which always result in the same outcome.
			// The base markup will still be cached though, in order to avoid any unnecessary
			// reloads. The base markup itself might be merged as it might inherit from its base
			// class.

			return getMarkupLoader().loadMarkup(container, markupResourceStream, null,
				enforceReload);
		}
		catch (MarkupNotFoundException e)
		{
			// InheritedMarkupMarkupLoader will throw a MarkupNotFoundException in case the
			// <b>base</b> markup can not be found.

			log.error("Markup not found: " + e.getMessage(), e);

			// Catch exception and ignore => return null (markup not found)
		}
		catch (ResourceStreamNotFoundException e)
		{
			log.error("Markup not found: " + markupResourceStream, e);

			// Catch exception and ignore => return null (markup not found)
		}
		catch (IOException e)
		{
			log.error("Error while reading the markup " + markupResourceStream, e);

			// Wrap with wicket exception and re-throw
			throw new MarkupException(markupResourceStream, "IO error while reading markup: " +
				e.getMessage(), e);
		}
		catch (WicketRuntimeException e)
		{
			log.error("Error while reading the markup " + markupResourceStream, e);

			// re-throw
			throw e;
		}
		catch (RuntimeException e)
		{
			log.error("Error while reading the markup " + markupResourceStream, e);

			// Wrap with wicket exception and re-throw
			throw new MarkupException(markupResourceStream, "Error while reading the markup: " +
				e.getMessage(), e);
		}

		// Markup not found. Errors should throw a Wicket exception
		return null;
	}
}
