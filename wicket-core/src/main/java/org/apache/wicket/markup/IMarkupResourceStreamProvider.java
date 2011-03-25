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
import org.apache.wicket.util.resource.IResourceStream;

/**
 * To be implemented by MarkupContainer which wish to implement their own algorithms for loading the
 * markup resource stream.
 * <p>
 * Since 1.5 you may also use Component.setMarkup() or getMarkup() to attach Markup to your
 * component.
 * <p>
 * Note: IResourceStreamLocators should be used in case the strategy to find a markup resource is
 * meant to be applied to ALL components of your application.
 * <p>
 * Note: See IMarkupCacheKeyProvider if you wish to implement your own cache key algorithm, which
 * sometimes is useful when the MarkupContainer implements its own IMarkupResourceStreamProvider as
 * well.
 * 
 * @see IMarkupCacheKeyProvider
 * 
 * @author Juergen Donnerstag
 */
public interface IMarkupResourceStreamProvider
{
	/**
	 * Create a new markup resource stream for the container.
	 * <p>
	 * Note: usually it will only called once, as the IResourceStream will be cached by MarkupCache.
	 * 
	 * @param container
	 *            The MarkupContainer which requests to load the Markup resource stream
	 * @param containerClass
	 *            The container the markup should be associated with
	 * @return A IResourceStream if the resource was found
	 */
	IResourceStream getMarkupResourceStream(final MarkupContainer container, Class<?> containerClass);
}
