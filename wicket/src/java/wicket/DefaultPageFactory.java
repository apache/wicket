/*
 * $Id$
 * $Revision$ $Date$
 *
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

/**
 * A factory that constructs Pages.
 *
 * @see ApplicationSettings#setDefaultPageFactory(IPageFactory)
 * @see Session#setPageFactory(IPageFactory)
 * @see IPageFactory
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public final class DefaultPageFactory implements IPageFactory
{
	/** Logging */
	private final static Log log = LogFactory.getLog(DefaultPageFactory.class);

	/** Map of Constructors for Page subclasses */
	private final Map constructorForClass = new ConcurrentHashMap();

	/**
	 * @see IPageFactory#newPage(Class)
	 */
	public final Page newPage(final Class pageClass)
	{
		try
		{
			return (Page)pageClass.newInstance();
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
	public final Page newPage(final Class pageClass, final PageParameters parameters)
	{
		// Try to get constructor that takes PageParameters
		Constructor constructor = constructor(pageClass, PageParameters.class);

		// If we got a PageParameters constructor
		if (constructor != null)
		{
			// return new Page(parameters)
			return newPage(constructor, parameters);
		}

		// If PageParameters is null or empty, try default constructor
		if (parameters == null || parameters.isEmpty())
		{
			return newPage(pageClass);
		}

		// Couldn't find PageParameters constructor and parameters were passed
		throw new WicketRuntimeException("Could not find a constructor in " + pageClass
				+ " that would accept PageParameters argument " + parameters);
	}

	/**
	 * @see IPageFactory#newPage(Class, Page)
	 */
	public final Page newPage(final Class pageClass, final Page page)
	{
		// Try to get constructor that takes PageParameters
		Constructor constructor = constructor(pageClass, Page.class);

		// If we got a PageParameters constructor
		if (constructor != null)
		{
			// return new Page(parameters)
			return newPage(constructor, page);
		}

		// Couldn't find constructor accepting page argument
		throw new WicketRuntimeException("Could not find a constructor in " + pageClass
				+ " that would accept Page argument " + page);
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
	private final Constructor constructor(final Class pageClass, final Class argumentType)
	{
		// Get constructor for page class from cache
		Constructor constructor = (Constructor)constructorForClass.get(pageClass);

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
	private final Page newPage(final Constructor constructor, final Object argument)
	{
		try
		{
			return (Page)constructor.newInstance(new Object[] { argument });
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
			throw new WicketRuntimeException("Can't instantiate page using constructor "
					+ constructor + " and argument " + argument, e);
		}
	}
}