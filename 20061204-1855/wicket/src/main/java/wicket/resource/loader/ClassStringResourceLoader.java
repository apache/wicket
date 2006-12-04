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

import wicket.Application;

/**
 * This string resource loader attempts to find a single resource bundle that
 * has the same name and location as the clazz. If this bundle is found
 * then strings are obtained from here. This implementation is fully aware of
 * both locale and style values when trying to obtain the appropriate bundle.
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 */
public class ClassStringResourceLoader extends AbstractStringResourceLoader
{
	/** The application we are loading for. */
	private final Class clazz;

	/**
	 * Create and initialise the resource loader.
	 * 
	 * @param application
	 *            Wickets application object
	 * @param clazz
	 *            The class that this resource loader is associated with
	 */
	public ClassStringResourceLoader(final Application application, final Class clazz)
	{
		super(application);
		
		if (clazz == null)
		{
			throw new IllegalArgumentException("Parameter 'clazz' must not be null");
		}
		this.clazz = clazz;
	}

	/**
	 * @inheritDoc
	 */
	public String loadStringResource(final Class clazz, final String key,
			final Locale locale, final String style)
	{
		 return super.loadStringResource(this.clazz, key, locale, style);
	}
}