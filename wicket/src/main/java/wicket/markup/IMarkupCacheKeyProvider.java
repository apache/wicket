/*
 * $Id: MarkupCache.java 4639 2006-02-26 01:44:07 -0800 (Sun, 26 Feb 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-02-26 01:44:07 -0800 (Sun, 26 Feb
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup;

import wicket.MarkupContainer;

/**
 * To be implemented by MarkupContainers which whish to implement there own
 * algorithms for the markup cache key.
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
	CharSequence getCacheKey(final MarkupContainer container,
			Class<? extends MarkupContainer> containerClass);
}
