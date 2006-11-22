/*
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
package wicket.jmx;


/**
 * Session settings.
 * 
 * @author eelcohillenius
 */
public interface SessionSettingsMBean
{
	/**
	 * Gets maximum number of page maps allowed in this session
	 * 
	 * @return Maximum number of page maps
	 */
	int getMaxPageMaps();

	/**
	 * Gets the factory to be used when creating pages
	 * 
	 * @return The default page factory
	 */
	String getPageFactory();

	/**
	 * Gets the strategy for evicting pages from the page map.
	 * 
	 * @return the strategy for evicting pages from the page map
	 */
	String getPageMapEvictionStrategy();

	/**
	 * Gets the session store implementation.
	 * 
	 * @return the session store implementation
	 */
	String getSessionStore();

	/**
	 * Sets maximum number of page maps allowed in this session
	 * 
	 * @param maxPageMaps
	 *            Maximum number of page maps
	 */
	void setMaxPageMaps(int maxPageMaps);
}
