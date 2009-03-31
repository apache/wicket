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
 * To be implemented by MarkupContainers that wish to implement their own algorithms for the markup
 * cache key.
 * 
 * <p>
 * If {@link #getCacheKey(MarkupContainer, Class)} method returns <code>null</code> the markup is
 * not cached.
 * </p>
 * 
 * @see IMarkupResourceStreamProvider
 * 
 * @author Juergen Donnerstag
 */
public interface IMarkupCacheKeyProvider
{
	/**
	 * Provide the markup cache key for the associated Markup resource stream.
	 * 
	 * @see IMarkupResourceStreamProvider
	 * 
	 * @param container
	 *            The MarkupContainer object requesting the markup cache key
	 * @param containerClass
	 *            The container the markup should be associated with
	 * @return A IResourceStream if the resource was found
	 */
	String getCacheKey(final MarkupContainer container, Class<?> containerClass);
}
