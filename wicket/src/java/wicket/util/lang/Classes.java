/*
 * $Id$ $Revision:
 * 1.4 $ $Date$
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

import wicket.markup.MarkupException;
import wicket.util.string.Strings;

/**
 * Utilities for dealing with classes.
 * 
 * @author Jonathan Locke
 */
public final class Classes
{
	/**
	 * Instantiation not allowed
	 */
	private Classes()
	{
	}

	/**
	 * Gets the name of a given class
	 * 
	 * @param c
	 *            The class
	 * @return The class name
	 */
	public static String name(final Class c)
	{
		return Strings.lastPathComponent(c.getName(), '.');
	}

	/**
	 * Takes a package and a relative path to a class and returns any class at
	 * that relative path. For example, if the given package was java.lang and
	 * the relative path was "../util/List", then the java.util.List class would
	 * be returned.
	 * 
	 * @param p
	 *            The package to start at
	 * @param path
	 *            The relative path to the class
	 * @return The class
	 * @throws ClassNotFoundException
	 */
	public static Class relativeClass(final Package p, final String path)
			throws ClassNotFoundException
	{
		return Class.forName(Packages.absolutePath(p, path).replace('/', '.'));
	}
	
	/**
	 * Invoke the setter method for 'name' on object and provide the 'value'
	 * 
	 * @param object
	 * @param name
	 * @param value
	 */
	public static void invokeSetter(final Object object, final String name, final String value)
	{
        // Get the setter for the attribute
        final String methodName = "set" + name;
        final Method[] methods = object.getClass().getMethods();
        Method method = null;
        for (int i = 0; i < methods.length; i++)
        {
            if (methods[i].getName().equalsIgnoreCase(methodName))
            {
                method = methods[i];
            }
        }

        if (method == null)
        {
            throw new MarkupException(
                    "Unable to initialize Component. Method with name " + methodName
                            + " not found");
        }

        // The method must have a single parameter
        final Class[] parameterClasses = method.getParameterTypes();
        if (parameterClasses.length != 1)
        {
            throw new MarkupException(
                    "Unable to initialize Component. Method with name " + methodName
                            + " must have one and only one parameter");
        }

        // Convert the parameter if necessary, depending on the setter's attribute
        final Class paramClass = parameterClasses[0];
        try
        {
            if (paramClass.equals(String.class))
            {
                method.invoke(object, new Object[] { value });
            }
            else if (paramClass.equals(int.class))
            {
                method.invoke(object, new Object[] { new Integer(value) });
            }
            else if (paramClass.equals(long.class))
            {
                method.invoke(object, new Object[] { new Long(value) });
            }
        }
        catch (IllegalAccessException ex)
        {
            throw new MarkupException(
                    "Unable to initialize Component. Failure while invoking method "
                            + methodName + ". Cause: " + ex);
        }
        catch (InvocationTargetException ex)
        {
            throw new MarkupException(
                    "Unable to initialize Component. Failure while invoking method "
                            + methodName + ". Cause: " + ex);
        }
        catch (NumberFormatException ex)
        {
            throw new MarkupException(
                    "Unable to initialize Component. Failure while invoking method "
                            + methodName + ". Cause: " + ex);
        }
	}
}
