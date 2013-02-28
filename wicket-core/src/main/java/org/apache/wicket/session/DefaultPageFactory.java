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
package org.apache.wicket.session;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.request.RequestHandlerStack.ReplaceHandlerException;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A factory that constructs Pages.
 * 
 * @see IPageFactory
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public final class DefaultPageFactory implements IPageFactory
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(DefaultPageFactory.class);

	/** Map of Constructors for Page subclasses */
	private final ConcurrentMap<Class<?>, Constructor<?>> constructorForClass = Generics.newConcurrentHashMap();

	/**
	 * {@link #isBookmarkable(Class)} is expensive, we cache the result here
	 */
	private final ConcurrentMap<String, Boolean> pageToBookmarkableCache = Generics.newConcurrentHashMap();

	@Override
	public final <C extends IRequestablePage> C newPage(final Class<C> pageClass)
	{
		try
		{
			// throw an exception in case default constructor is missing
			// => improved error message
			Constructor<C> constructor = pageClass.getConstructor((Class<?>[])null);

			return processPage(newPage(constructor, null), null);
		}
		catch (NoSuchMethodException e)
		{
			// a bit of a hack here..
			Constructor<C> constructor = constructor(pageClass, PageParameters.class);
			if (constructor != null)
			{
				PageParameters pp = new PageParameters();
				return processPage(newPage(constructor, pp), pp);
			}
			else
			{
				throw new WicketRuntimeException("Unable to create page from " + pageClass +
					". Class does not have a visible default constructor.", e);
			}
		}
	}

	@Override
	public final <C extends IRequestablePage> C newPage(final Class<C> pageClass,
		final PageParameters parameters)
	{
		// Try to get constructor that takes PageParameters
		Constructor<C> constructor = constructor(pageClass, PageParameters.class);

		// If we got a PageParameters constructor
		if (constructor != null)
		{
			final PageParameters nullSafeParams = parameters == null ? new PageParameters() : parameters;

			// return new Page(parameters)
			return processPage(newPage(constructor, nullSafeParams), nullSafeParams);
		}

		// Always try default constructor if one exists
		return processPage(newPage(pageClass), parameters);
	}

	/**
	 * Looks up a one-arg Page constructor by class and argument type.
	 * 
	 * @param pageClass
	 *            The class of page
	 * @param argumentType
	 *            The argument type
	 * @return The page constructor, or null if no one-arg constructor can be found taking the given
	 *         argument type.
	 */
	private <C extends IRequestablePage> Constructor<C> constructor(final Class<C> pageClass,
		final Class<PageParameters> argumentType)
	{
		// Get constructor for page class from cache
		Constructor<C> constructor = (Constructor<C>) constructorForClass.get(pageClass);

		// Need to look up?
		if (constructor == null)
		{
			try
			{
				// Try to find the constructor
				constructor = pageClass.getConstructor(new Class[] { argumentType });

				// Store it in the cache
				Constructor<C> tmpConstructor = (Constructor<C>) constructorForClass.putIfAbsent(pageClass, constructor);
				if (tmpConstructor != null)
				{
					constructor = tmpConstructor;
				}

				log.debug("Found constructor for Page of type '{}' and argument of type '{}'.",
					pageClass, argumentType);
			}
			catch (NoSuchMethodException e)
			{
				log.debug(
					"Page of type '{}' has not visible constructor with an argument of type '{}'.",
					pageClass, argumentType);

				return null;
			}
		}

		return constructor;
	}

	/**
	 * Creates a new Page using the given constructor and argument.
	 * 
	 * @param constructor
	 *            The constructor to invoke
	 * @param argument
	 *            The argument to pass to the constructor or null to pass no arguments
	 * @return The new page
	 * @throws WicketRuntimeException
	 *             Thrown if the Page cannot be instantiated using the given constructor and
	 *             argument.
	 */
	private <C extends IRequestablePage> C newPage(final Constructor<C> constructor, final PageParameters argument)
	{
		try
		{
			if (argument != null)
			{
				return constructor.newInstance(argument);
			}
			else
			{
				return constructor.newInstance();
			}
		}
		catch (InstantiationException e)
		{
			throw new WicketRuntimeException(createDescription(constructor, argument), e);
		}
		catch (IllegalAccessException e)
		{
			throw new WicketRuntimeException(createDescription(constructor, argument), e);
		}
		catch (InvocationTargetException e)
		{
			if (e.getTargetException() instanceof ReplaceHandlerException ||
				e.getTargetException() instanceof AuthorizationException ||
				e.getTargetException() instanceof MarkupException)
			{
				throw (RuntimeException)e.getTargetException();
			}
			throw new WicketRuntimeException(createDescription(constructor, argument), e);
		}
	}

	private <C extends IRequestablePage> C processPage(final C page, final PageParameters pageParameters)
	{
		// the page might have not propagate page parameters from constructor. if that's the case
		// we force the parameters
		if ((pageParameters != null) && (page.getPageParameters() != pageParameters))
		{
			page.getPageParameters().overwriteWith(pageParameters);
		}

		((Page)page).setWasCreatedBookmarkable(true);

		return page;
	}

	private String createDescription(final Constructor<?> constructor, final Object argument)
	{
		String msg;
		if (argument != null)
		{
			msg = "Can't instantiate page using constructor '" + constructor + "' and argument '" +
				argument;
		}
		else
		{
			msg = "Can't instantiate page using constructor '" + constructor;
		}

		return msg + "'. Might be it doesn't exist, may be it is not visible (public).";
	}

	@Override
	public <C extends IRequestablePage> boolean isBookmarkable(Class<C> pageClass)
	{
		Boolean bookmarkable = pageToBookmarkableCache.get(pageClass.getName());
		if (bookmarkable == null)
		{
			try
			{
				if (pageClass.getConstructor(new Class[] { }) != null)
				{
					bookmarkable = Boolean.TRUE;
				}
			}
			catch (Exception ignore)
			{
				try
				{
					if (pageClass.getConstructor(new Class[] { PageParameters.class }) != null)
					{
						bookmarkable = Boolean.TRUE;
					}
				}
				catch (Exception ignored)
				{
				}
			}

			if (bookmarkable == null)
			{
				bookmarkable = Boolean.FALSE;
			}
			Boolean tmpBookmarkable = pageToBookmarkableCache.putIfAbsent(pageClass.getName(), bookmarkable);
			if (tmpBookmarkable != null)
			{
				bookmarkable = tmpBookmarkable;
			}
		}

		return bookmarkable;
	}
}