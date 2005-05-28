/*
 * $Id$
 * $Revision$ $Date$
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import wicket.MarkupContainer;

/**
 * Load markup and cache it for fast retrieval. If markup file changes, it'll be
 * automatically reloaded.
 * 
 * @author Johan Compagner
 */
public class HeadComponentCache
{
	private Map cache = Collections.synchronizedMap(new HashMap());
	
	/**
	 * 
	 */
	public HeadComponentCache()
	{
		super();
	}
	
	/**
	 * @param container
	 * @param clazz
	 * @return The markup container found for that container and class
	 */
	public MarkupContainer get(MarkupContainer container, Class clazz)
	{
		return (MarkupContainer)cache.get(container.getStyle() + "_" + container.getLocale().toString() + "_" + clazz.getName());
	}
	
	/**
	 * @param container
	 * @param clazz
	 * @param headComponent 
	 */
	public void put(MarkupContainer container, Class clazz, MarkupContainer headComponent)
	{
		cache.put(container.getStyle() + "_" + container.getLocale().toString() + "_" + clazz.getName(),headComponent);
	}

}
