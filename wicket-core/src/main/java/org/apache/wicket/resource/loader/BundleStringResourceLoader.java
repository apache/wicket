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
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.wicket.Component;
import org.apache.wicket.Session;


/**
 * Implementation of a string resource loader that sits on top of the ordinary Java resource bundle
 * mechanism. When created this loader must be given the name of the resource bundle that it is to
 * sit on top of. Note that this implementation does not make use of any style or component specific
 * knowledge - it utilizes just the bundle name, the resource key and the locale.
 * 
 * @author Chris Turner
 */
public class BundleStringResourceLoader implements IStringResourceLoader
{
	/** The name of the underlying resource bundle. */
	private final String bundleName;

	/**
	 * Create the loader with the name of the given Java resource bundle.
	 * 
	 * @param bundleName
	 *            The name of the resource bundle
	 */
	public BundleStringResourceLoader(final String bundleName)
	{
		this.bundleName = bundleName;
	}

	/**
	 * Get the value via a Java ResourceBundle
	 */
	@Override
	public final String loadStringResource(final Class<?> clazz, final String key, Locale locale,
		final String style, final String variation)
	{
		if (locale == null)
		{
			locale = Session.exists() ? Session.get().getLocale() : Locale.getDefault();
		}
		try
		{
			return ResourceBundle.getBundle(bundleName, locale).getString(key);
		}
		catch (MissingResourceException mrx)
		{
			try
			{
				return ResourceBundle.getBundle(bundleName, locale, Thread.currentThread().getContextClassLoader()).getString(key);
			}
			catch (MissingResourceException mrx2)
			{
				return null;
			}
		}
	}

	/**
	 * Get the requested string resource from the underlying resource bundle. The bundle is selected
	 * by locale and the string obtained from the best matching bundle.
	 * 
	 * @param component
	 *            Used to get the locale
	 * @param key
	 *            The key to obtain the string for
	 * @param locale
	 *            If != null, it supersedes the component's locale
	 * @param style
	 *            ignored
	 * @param variation
	 *            ignored
	 * @return The string resource value or null if resource not found
	 */
	@Override
	public final String loadStringResource(final Component component, final String key,
		Locale locale, final String style, final String variation)
	{
		return loadStringResource((Class<?>)null, key, locale, style, variation);
	}
}
