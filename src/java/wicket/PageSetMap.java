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
 * A mapping from Page instance to PageSet. A given PageSetMap returns a PageSet
 * for a given Page.
 * 
 * @author Jonathan Locke
 */
public abstract class PageSetMap
{
	/**
	 * @param page
	 *            The Page object
	 * @return The PageSet for the Page.
	 */
	public abstract PageSet pageSet(final Page page);
	
	/**
	 * @param pageClass The page class to check
	 */
	protected void checkPageClass(final Class pageClass)
	{
		if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("Page class " + pageClass.getName()
					+ " must be an instance of Page");
		}
	}
}
