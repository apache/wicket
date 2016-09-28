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
package org.apache.wicket.resource.loader;

import java.util.Locale;

import org.apache.wicket.Component;


/**
 * The string resource loader interface allows a strategy pattern to be applied to the loading of
 * resource strings for an application. The loader (or chain of loaders) that are used is configured
 * via the application settings.
 * <p>
 * Each particular implementation of this interface may define its own mechanism for searching for
 * resources. Please see the documents for each particular implementation to determine its behavior
 * and to see how it can be configured.
 * <p>
 * It is important to note that if a resource is not found by a particular loader than the loader
 * should return <code>null</code> rather than throw an exception. The reason for this is that
 * loaders can be arranged in a chain and it would be very inefficient for loaders earlier in the
 * chain to throw exceptions that must be caught and handled each time until the correct loader in
 * the chain is reached.
 * 
 * @see org.apache.wicket.settings.ResourceSettings
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 */
public interface IStringResourceLoader
{
	/**
	 * Get the string resource for the given combination of component class, resource key, locale
	 * and style. The component class provided is used to allow implementation of component specific
	 * resource loading (e.g. per page or per reusable component). The key should be a String
	 * containing a lookup key into a resource bundle. The locale should contain the locale of the
	 * current operation so that the appropriate set of resources can be selected. The style allows
	 * the set of resources to select to be varied by skin/brand.
	 * 
	 * @param clazz
	 *            The class to get the string resource for
	 * @param key
	 *            The key should be a String containing a lookup key into a resource bundle
	 * @param locale
	 *            The locale should contain the locale of the current operation so that the
	 *            appropriate set of resources can be selected
	 * @param style
	 *            The style identifying the resource set to select the strings from (see
	 *            {@link org.apache.wicket.Session})
	 * @param variation
	 *            The components variation (of the style)
	 * @return The string resource value or null if the resource could not be loaded by this loader
	 */
	String loadStringResource(Class<?> clazz, String key, Locale locale, String style,
		String variation);

	/**
	 * Get the string resource for the given combination of component, resource key, locale and
	 * style. The component provided is used to allow implementation of component specific resource
	 * loading (e.g. per page or per reusable component). The key should be a String containing a
	 * lookup key into a resource bundle. The Locale and the style will be taken from the Component
	 * provided.
	 * 
	 * @param component
	 *            The component to get the string resource for
	 * @param key
	 *            The key should be a String containing a lookup key into a resource bundle
	 * @param locale
	 *            Will be preset with the appropriate value. You shall ignore the component's
	 *            locale.
	 * @param style
	 *            Will be preset with the appropriate value. You shall ignore the component's style.
	 * @param variation
	 *            Will be preset with the appropriate value. You shall ignore the component's
	 *            variation.
	 * @return The string resource value or null if the resource could not be loaded by this loader
	 */
	String loadStringResource(Component component, String key, Locale locale, String style,
		String variation);
}