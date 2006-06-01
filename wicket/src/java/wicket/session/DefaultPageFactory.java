/*
 * $Id: DefaultPageFactory.java 5771 2006-05-19 12:04:06 +0000 (Fri, 19 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-19 12:04:06 +0000 (Fri, 19
 * May 2006) $
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
package wicket.session;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wicket.AbortException;
import wicket.IPageFactory;
import wicket.Page;
import wicket.PageParameters;
import wicket.WicketRuntimeException;
import wicket.authorization.AuthorizationException;
import wicket.markup.MarkupException;

/**
 * A factory that constructs Pages.
 * 
 * @see wicket.settings.ISessionSettings#setPageFactory(IPageFactory)
 * @see IPageFactory
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public final class DefaultPageFactory implements IPageFactory
{
	/** Map of Constructors for Page subclasses */
	private final Map<Class<? extends Page>, Constructor<? extends Page>> constructorForClass = new ConcurrentHashMap<Class<? extends Page>, Constructor<? extends Page>>();

	/**
	 * @see IPageFactory#newPage(Class)
	 */
	public final Page newPage(final Class<? extends Page> pageClass)
	{
		try
		{
			// throw an exception in case default constructor is missing
			// => improved error message
			pageClass.getConstructor((Class[])null);

			return (Page)pageClass.newInstance();
		}
		catch (NoSuchMethodException e)
		{
			// a bit of a hack here..
			Constructor<? extends Page> constructor = constructor(pageClass, PageParameters.class);
			if (constructor != null)
			{
				return newPage(constructor, new PageParameters());
			}
			else
			{
				throw new WicketRuntimeException("Unable to create page from " + pageClass
						+ ". Class does not have a default contructor", e);
			}
		}
		catch (InstantiationException e)
		{
			throw new WicketRuntimeException("Unable to create page from " + pageClass, e);
		}
		catch (IllegalAccessException e)
		{
			throw new WicketRuntimeException("Unable to create page from " + pageClass, e);
		}
	}

	/**
	 * @see IPageFactory#newPage(Class, PageParameters)
	 */
	public final Page newPage(final Class<? extends Page> pageClass, final PageParameters parameters)
	{
		// Try to get constructor that takes PageParameters
		Constructor<? extends Page> constructor = constructor(pageClass, PageParameters.class);

		// If we got a PageParameters constructor
		if (constructor != null)
		{
			// return new Page(parameters)
			return newPage(constructor, parameters);
		}

		// Always try default constructor if one exists
		return newPage(pageClass);
	}

	/**
	 * Looks up a one-arg Page constructor by class and argument type.
	 * 
	 * @param pageClass
	 *            The class of page
	 * @param argumentType
	 *            The argument type
	 * @return The page constructor, or null if no one-arg constructor can be
	 *         found taking the given argument type.
	 */
	private final Constructor<? extends Page> constructor(final Class<? extends Page> pageClass, final Class argumentType)
	{
		// Get constructor for page class from cache
		Constructor<? extends Page> constructor = constructorForClass.get(pageClass);

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
	 * 
	 * @param constructor
	 *            The constructor to invoke
	 * @param argument
	 *            The argument to pass to the constructor
	 * @return The new page
	 * @throws WicketRuntimeException
	 *             Thrown if the Page cannot be instantiated using the given
	 *             constructor and argument.
	 */
	private final Page newPage(final Constructor<? extends Page> constructor, final Object argument)
	{
		try
		{
			return constructor.newInstance(new Object[] { argument });
		}
		catch (InstantiationException e)
		{
			throw new WicketRuntimeException("Can't instantiate page using constructor "
					+ constructor + " and argument " + argument, e);
		}
		catch (IllegalAccessException e)
		{
			throw new WicketRuntimeException("Can't instantiate page using constructor "
					+ constructor + " and argument " + argument, e);
		}
		catch (InvocationTargetException e)
		{
			// honor redirect exception contract defined in IPageFactory
			if (e.getTargetException() instanceof AbortException
					|| e.getTargetException() instanceof AuthorizationException
					|| e.getTargetException() instanceof MarkupException)
			{
				throw (RuntimeException)e.getTargetException();
			}
			throw new WicketRuntimeException("Can't instantiate page using constructor "
					+ constructor + " and argument " + argument, e);
		}
	}
}