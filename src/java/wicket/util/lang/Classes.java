/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.lang;

import wicket.util.string.IStringIterator;
import wicket.util.string.StringList;
import wicket.util.string.Strings;

/**
 * Utilities for dealing with classes and packages.
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
     * @param c The class
     * @return The class name
     */
    public static String name(final Class c)
    {
        return Strings.lastPathComponent(c.getName(), '.');
    }

    /**
     * Gets the name of a given package
     * @param c The class
     * @return The class' package
     */
    public static String packageName(final Class c)
    {
        return Strings.beforeLastPathComponent(c.getName(), '.');
    }

    /**
     * Takes a package and a relative path to a class and returns any class
     * at that relative path.  For example, if the given package was java.lang
     * and the relative path was "../util/List", then the java.util.List class
     * would be returned.
     * @param p The package to start at
     * @param path The relative path to the class
     * @return The class
     * @throws ClassNotFoundException
     */
    public static Class relativeClass(final Package p, final String path)
            throws ClassNotFoundException
    {
        final StringList className = StringList.tokenize(p.getName(), ".");
        final StringList components = StringList.tokenize(path, "/\\");

        if (components.get(0).equals(""))
        {
            throw new IllegalArgumentException("Path not relative: " + path);
        }

        for (final IStringIterator iterator = components.iterator(); iterator.hasNext();)
        {
            final String folder = iterator.next();

            if (folder.equals(".."))
            {
                className.removeLast();
            }
            else
            {
                className.add(folder);
            }
        }

        return Class.forName(className.join("."));
    }
}


