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
package wicket.resource;

import java.util.Locale;

/**
 * Interface to be implemented by properties loaders
 * 
 * @author Juergen Donnerstag
 */
public interface IPropertiesFactory
{
	/**
	 * Add a listener which will be called after properties have been reloaded
	 * 
	 * @param listener
	 */
	void addListener(final IPropertiesReloadListener listener);

	/**
	 * Get the properties for ...
	 * 
	 * @param clazz
	 *            The class that resources are bring loaded for
	 * @param style
	 *            The style to load resources for (see {@link wicket.Session})
	 * @param locale
	 *            The locale to load reosurces for
	 * @return The properties
	 */
	Properties get(final Class clazz, final String style, final Locale locale);

	/**
	 * Remove all cached properties
	 */
	abstract void clearCache();
}