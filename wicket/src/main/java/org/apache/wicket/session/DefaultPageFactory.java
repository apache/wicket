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
import java.util.Map;

import org.apache.wicket.AbortException;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.util.lang.Generics;


/**
 * A factory that constructs Pages.
 * 
 * @see org.apache.wicket.settings.ISessionSettings#setPageFactory(IPageFactory)
 * @see IPageFactory
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 * @author Matej Knopp
 * @author Igor Vaynberg
 */
public final class DefaultPageFactory implements IPageFactory
{
	/** Map of Constructors for Page subclasses */
	private final Map<Class<? extends RequestablePage>, Constructor<? extends RequestablePage>> constructorForClass = Generics.newConcurrentHashMap();

	/**
	 * @see IPageFactory#newPage(Class)
	 */
	public final <C extends RequestablePage> RequestablePage newPage(final Class<C> pageClass)
	{
		// Try default constructor if one exists
		Constructor<? extends RequestablePage> constructor = constructor(pageClass);
		if (constructor != null)
		{
			// return new Page()
			return createPage(constructor, null);
		}

		// Try to get constructor that takes PageParameters
		constructor = constructor(pageClass, PageParameters.class);
		if (constructor != null)
		{
			// return new Page(parameters)
			return createPage(constructor, new PageParameters());
		}

		throw new WicketRuntimeException("Unable to create page from " + pageClass +
			". Class does neither have a constructor with PageParameter nor a default constructor");
	}

	/**
	 * @see IPageFactory#newPage(Class, PageParameters)
	 */
	public final <C extends RequestablePage> RequestablePage newPage(final Class<C> pageClass,
		PageParameters parameters)
	{
		// If no parameters are provided, try the default constructor first, than the PageParameter
		// constructor with empty parameter list.
		if (parameters == null)
		{
			return newPage(pageClass);
		}

		// If parameters not null, than try to get constructor that takes PageParameters
		Constructor<? extends RequestablePage> constructor = constructor(pageClass,
			PageParameters.class);
		if (constructor != null)
		{
			// return new Page(parameters)
			return createPage(constructor, parameters);
		}

		// No constructor with PageParameters found. Try default constructor.
		constructor = constructor(pageClass);
		if (constructor != null)
		{
			// return new Page()
			return createPage(constructor, null);
		}

		throw new WicketRuntimeException("Unable to create page from " + pageClass +
			". Class does neither have a constructor with PageParameter nor a default constructor");
	}

	/**
	 * Looks up a one-arg Page constructor by class and argument type.
	 * 
	 * @param <C>
	 * 
	 * @param pageClass
	 *            The class of page
	 * @return The page constructor, or null if no one-arg constructor can be found taking the given
	 *         argument type.
	 */
	private final <C extends RequestablePage> Constructor<? extends RequestablePage> constructor(
		final Class<C> pageClass)
	{
		try
		{
			// Try to find the constructor
			return pageClass.getConstructor();
		}
		catch (NoSuchMethodException e)
		{
			// Ignore
		}

		return null;
	}

	/**
	 * Looks up a one-arg Page constructor by class and argument type.
	 * 
	 * @param <C>
	 * 
	 * @param pageClass
	 *            The class of page
	 * @param argumentType
	 *            The argument type
	 * @return The page constructor, or null if no one-arg constructor can be found taking the given
	 *         argument type.
	 */
	private final <C extends RequestablePage> Constructor<? extends RequestablePage> constructor(
		final Class<C> pageClass, final Class<PageParameters> argumentType)
	{
		// Get constructor for page class from cache
		Constructor<? extends RequestablePage> constructor = constructorForClass.get(pageClass);

		// Need to look up?
		if (constructor == null)
		{
			try
			{
				// Try to find the constructor
				constructor = pageClass.getConstructor(new Class[] { argumentType });

				// Store it in the cache
				constructorForClass.put(pageClass, constructor);
			}
			catch (NoSuchMethodException e)
			{
				return null;
			}
		}

		return constructor;
	}

	/**
	 * Creates a new Page using the given constructor and argument.
	 * <p>
	 * Note that you can not create a Page with PageParameters and pass null.
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
	private final RequestablePage createPage(
		final Constructor<? extends RequestablePage> constructor, final PageParameters argument)
	{
		final RequestablePage instance;
		try
		{
			if (argument != null)
			{
				instance = constructor.newInstance(new Object[] { argument });
			}
			else
			{
				instance = constructor.newInstance();
			}

			// the page might have not propagate page parameters from constructor. if that's the
			// case
			// we force the parameters
			if (argument != null && instance.getPageParameters() != argument)
			{
				instance.getPageParameters().assign(argument);
			}

			instance.setWasCreatedBookmarkable(true);

			return instance;

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
			// honor redirect exception contract defined in IPageFactory
			if (e.getTargetException() instanceof AbortException ||
				e.getTargetException() instanceof AuthorizationException ||
				e.getTargetException() instanceof MarkupException)
			{
				throw (RuntimeException)e.getTargetException();
			}
			throw new WicketRuntimeException(createDescription(constructor, argument), e);
		}
	}

	/**
	 * 
	 * @param constructor
	 * @param argument
	 * @return description
	 */
	private String createDescription(Constructor<? extends RequestablePage> constructor,
		PageParameters argument)
	{
		if (argument != null)
		{
			return "Can't instantiate page using constructor " + constructor + " and argument " +
				argument;
		}
		else
		{
			return "Can't instantiate page using constructor " + constructor;
		}
	}
}