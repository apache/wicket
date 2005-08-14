/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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
package wicket;


/**
 * An abstration that is associated with a set of pages. A given Page has a
 * (possibly ordered) set of PageSet objects which can be retrieved via
 * Application.getPageSets(Page). The implementation of this method will
 * determine how PageSet objects are associated with the Pages that they
 * logically (although in most cases don't physically) contain. For example, a
 * strategy for retrieving a PageSet for a Page might involve looking at the
 * Page's Class, package or related metadata and then using that to determine
 * which PageSet(s) it belongs to.
 * 
 * @author Jonathan Locke
 */
public abstract class PageSet
{
	/** The name of this PageSet */
	private final String name;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The name of this set of pages
	 */
	public PageSet(final String name)
	{
		this.name = name;
	}

	/**
	 * Initializes a page
	 * 
	 * @param page
	 *            The page to initialize
	 */
	public void init(final Page page)
	{
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[PageSet name = " + name + "]";
	}
}
