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
package org.apache.wicket.markup;

import org.apache.wicket.MarkupContainer;

/**
 * Each Wicket application has a single IMarkupCache associated with it (see
 * {@link org.apache.wicket.settings.MarkupSettings}). Via {@link MarkupFactory}
 * the markup cache is used by every Component to get its associated
 * markup stream.
 * 
 * @author Juergen Donnerstag
 */
public interface IMarkupCache
{
	/**
	 * Clear markup cache and force reload of all markup data
	 */
	void clear();

	/**
	 * Gets any (immutable) markup resource for the container or any of its parent classes (markup
	 * inheritance)
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param clazz
	 *            The class to get the associated markup for. If null, the container's class is
	 *            used, but it can be a parent class of the container as well (markup inheritance)
	 * @param enforceReload
	 *            The cache will be ignored and all, including inherited markup files, will be
	 *            reloaded. Whatever is in the cache, it will be ignored
	 * @return Markup resource
	 */
	Markup getMarkup(final MarkupContainer container, final Class<?> clazz,
		final boolean enforceReload);

	/**
	 * Remove the markup associated with the cache key from the cache including all dependent
	 * markups (markup inheritance)
	 * 
	 * @see MarkupResourceStream#getCacheKey()
	 * 
	 * @param cacheKey
	 * @return The markup removed from the cache. Null, if nothing was found.
	 */
	IMarkupFragment removeMarkup(final String cacheKey);

	/**
	 * @return the number of elements currently in the cache.
	 */
	int size();

	/**
	 * Will be called by the application while shutting down. It allows the markup cache to cleanup
	 * if necessary.
	 */
	void shutdown();
}
