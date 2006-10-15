/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.util.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import wicket.Application;
import wicket.markup.MarkupException;
import wicket.util.convert.IConverter;
import wicket.util.string.Strings;

/**
 * Utilities for dealing with classes.
 * 
 * @author Jonathan Locke
 */
public final class Classes
{
	/**
	 * Invoke the setter method for 'name' on object and provide the 'value'
	 * 
	 * @param object
	 * @param name
	 * @param value
	 * @param locale
	 */
	public static void invokeSetter(final Object object, final String name, final String value,
			final Locale locale)
	{
		// Note: tag attributes are maintained in a LowerCaseKeyValueMap, thus
		// 'name' will be all lowercase.

		// Note: because the attributes are all lowercase, there is slight
		// possibility of error due to naming issues.

		// Note: all setters must start with "set"

		// Get the setter for the attribute
		final String methodName = "set" + name;
		final Method[] methods = object.getClass().getMethods();
		Method method = null;
		for (Method element : methods)
		{
			if (element.getName().equalsIgnoreCase(methodName))
			{
				method = element;
			}
		}

		if (method == null)
		{
			throw new MarkupException("Unable to initialize Component. Method with name "
					+ methodName + " not found");
		}

		// The method must have a single parameter
		final Class[] parameterClasses = method.getParameterTypes();
		if (parameterClasses.length != 1)
		{
			throw new MarkupException("Unable to initialize Component. Method with name "
					+ methodName + " must have one and only one parameter");
		}

		// Convert the parameter if necessary, depending on the setter's
		// attribute
		final Class paramClass = parameterClasses[0];
		try
		{
			final IConverter converter = Application.get().getApplicationSettings()
					.getConverterLocatorFactory().newConverterLocator().getConverter(paramClass);
			final Object param = converter.convertToObject(value, locale);
			if (param == null)
			{
				throw new MarkupException("Unable to convert value '" + value + "' into "
						+ paramClass + ". May be there is no converter for that type registered?");
			}
			method.invoke(object, new Object[] { param });
		}
		catch (IllegalAccessException ex)
		{
			throw new MarkupException(
					"Unable to initialize Component. Failure while invoking method " + methodName
							+ ". Cause: " + ex);
		}
		catch (InvocationTargetException ex)
		{
			throw new MarkupException(
					"Unable to initialize Component. Failure while invoking method " + methodName
							+ ". Cause: " + ex);
		}
		catch (NumberFormatException ex)
		{
			throw new MarkupException(
					"Unable to initialize Component. Failure while invoking method " + methodName
							+ ". Cause: " + ex);
		}
	}

	/**
	 * Gets the name of the given class or null if the class is null.
	 * 
	 * @param c
	 *            The class
	 * @return The class name
	 */
	public static String name(final Class c)
	{
		return (c != null) ? c.getName() : null;
	}

	/**
	 * Takes a Class and a relative path to a class and returns any class at
	 * that relative path. For example, if the given Class was java.lang.System
	 * and the relative path was "../util/List", then the java.util.List class
	 * would be returned.
	 * 
	 * @param scope
	 *            The package to start at
	 * @param path
	 *            The relative path to the class
	 * @return The class
	 * @throws ClassNotFoundException
	 */
	public static Class relativeClass(final Class scope, final String path)
			throws ClassNotFoundException
	{
		return Class.forName(Packages.absolutePath(scope, path).replace('/', '.'));
	}

	/**
	 * Gets the name of a given class without the prefixed package path
	 * 
	 * @param c
	 *            The class
	 * @return The class name
	 */
	public static String simpleName(final Class c)
	{
		return Strings.lastPathComponent(c.getName(), '.');
	}

	/**
	 * Instantiation not allowed
	 */
	private Classes()
	{
	}
}
