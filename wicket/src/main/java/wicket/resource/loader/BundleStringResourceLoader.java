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
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Implementation of a string resource loader that sits on top of the ordinary
 * Java resource bundle mechanism. When created this loader must be given the
 * name of the resource bundle that it is to sit on top of. Note that this
 * implementation does not make use of any style or component specific knowledge -
 * it utilises just the bundle name, the resource key and the locale.
 * 
 * @author Chris Turner
 */
public class BundleStringResourceLoader implements IStringResourceLoader
{
	/** The name of the underlying resource bundle. */
	private String bundleName;

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
	 * Get the requested string resource from the underlying resource bundle.
	 * The bundle is selected by locale and the string obtained from the best
	 * matching bundle.
	 * 
	 * @param clazz
	 *            Not used for this implementstion
	 * @param key
	 *            The key to obtain the string for
	 * @param locale
	 *            The locale identifying the resource set to select the strings
	 *            from
	 * @param style
	 *            Not used for this implementation (see {@link wicket.Session})
	 * @return The string resource value or null if resource not found
	 */
	public final String loadStringResource(final Class clazz, final String key, Locale locale,
			final String style)
	{
		if (locale == null)
		{
			locale = Locale.getDefault();
		}
		try
		{
			final ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
			return bundle.getString(key);
		}
		catch (MissingResourceException e)
		{
			return null;
		}
	}
}