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

import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.IInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is one of Wicket's default string resource loaders. It is designed to let wicket extension
 * modules contribute default resource bundles for their components.
 * <p>
 * The initializer based string resource loader attempts to find the resource from a bundle that
 * corresponds to the supplied wicket initializers.
 * <p>
 * This implementation is fully aware of both locale and style values when trying to obtain the
 * appropriate resources.
 * <p>
 * 
 * @author Bertrand Guay-Paquet
 * @author Sven Meier
 */
public class InitializerStringResourceLoader extends ComponentStringResourceLoader
{
	private List<IInitializer> initializers;

	/**
	 * Create and initialize the resource loader.
	 * 
	 * @param initializers
	 *            initializers
	 */
	public InitializerStringResourceLoader(List<IInitializer> initializers)
	{
		this.initializers = initializers;
	}

	/**
	 * 
	 * @see org.apache.wicket.resource.loader.ComponentStringResourceLoader#loadStringResource(java.lang.Class,
	 *      java.lang.String, java.util.Locale, java.lang.String, java.lang.String)
	 */
	@Override
	public String loadStringResource(Class<?> clazz, final String key, final Locale locale,
		final String style, final String variation)
	{

		for (IInitializer initializer : initializers)
		{
			String string = super.loadStringResource(initializer.getClass(), key, locale, style,
				variation);
			if (string != null)
			{
				return string;
			}
		}

		// not found
		return null;
	}

	/**
	 * @see org.apache.wicket.resource.loader.ComponentStringResourceLoader#loadStringResource(org.apache.wicket.Component,
	 *      java.lang.String, java.util.Locale, java.lang.String, java.lang.String)
	 */
	@Override
	public String loadStringResource(final Component component, final String key,
		final Locale locale, final String style, final String variation)
	{
		return loadStringResource((Class<?>)null, key, locale, style, variation);
	}
}
