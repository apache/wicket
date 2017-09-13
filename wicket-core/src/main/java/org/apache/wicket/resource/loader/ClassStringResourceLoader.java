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

import java.lang.ref.WeakReference;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.util.lang.Args;

/**
 * This string resource loader attempts to find a single resource bundle that has the same name and
 * location as the clazz provided in the constructor. If the bundle is found than strings are
 * obtained from here. This implementation is fully aware of both locale and style values when
 * trying to obtain the appropriate bundle.
 * <p>
 * An instance of this loader is registered with the Application by default.
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 */
public class ClassStringResourceLoader extends ComponentStringResourceLoader
{
	/** The application we are loading for. */
	private final WeakReference<Class<?>> clazzRef;

	/**
	 * Create and initialize the resource loader.
	 * 
	 * @param clazz
	 *            The class that this resource loader is associated with
	 */
	public ClassStringResourceLoader(final Class<?> clazz)
	{
		Args.notNull(clazz, "clazz");
		clazzRef = new WeakReference<Class<?>>(clazz);
	}

	@Override
	public String loadStringResource(final Class<?> clazz, final String key, final Locale locale,
		final String style, final String variation)
	{
		return super.loadStringResource(clazzRef.get(), key, locale, style, variation);
	}

	@Override
	public String loadStringResource(final Component component, final String key,
		final Locale locale, final String style, final String variation)
	{
		if (component == null)
		{
			return super.loadStringResource(clazzRef.get(), key, locale, style, variation);
		}
		return super.loadStringResource(component, key, locale, style, variation);
	}
}
