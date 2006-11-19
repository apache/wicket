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
package wicket.resource.loader;

import java.util.Locale;

/**
 * The string resource loader interface allows a strategy pattern to be applied
 * to the loading of resource strings for an application. The loader (or chain
 * of loaders) that are used is configured via the
 * <code>ApplicationSettings</code> class.
 * <p>
 * Each particular implementation of this interface may define its own mechanism
 * for searching for resources. Please see the documents for each particular
 * implementation to determine this behavior and to see how it can be
 * configured.
 * <p>
 * It is important to note that if a resource is not found by a particular
 * loader then the loader should return <code>null</code> rather than throw an
 * exception. The reason for this is that loaders can be arranged in a chain and
 * it would be very inefficient for loaders earlier in the chain to throw
 * exceptions that must be caught and handled each time until the correct loader
 * in the chain is reached.
 * 
 * @author Chris Turner
 * @see wicket.settings.IResourceSettings
 */
public interface IStringResourceLoader
{
    /**
     * Get the string resource for the given combination of component class,
     * resource key, locale and style. The component is provided used to allow
     * implementation of component specific resource loading (e.g. per page or
     * per reusable component). It also allows the resource loader
     * implementation to get access to the application settings and the root
     * application object if necessary. The key should be a String containing a
     * lookup key into a resource bundle. The locale should contain the locale
     * of the current operation so that the appopriate set of resources can be
     * selected. The style allows the set of resources to select to be varied by
     * skin/brand.
     * 
     * @param clazz
     *            The class to get the string resource for
     * @param key
     *            The key to obtain the string for
     * @param locale
     *            The locale identifying the resource set to select the strings
     *            from
     * @param style
     *            The (optional) style identifying the resource set to select
     *            the strings from (see {@link wicket.Session})
     * @return The string resource value or null if the resource could not be
     *         loaded by this loader
     */
    String loadStringResource(Class clazz, String key, Locale locale, String style);
}