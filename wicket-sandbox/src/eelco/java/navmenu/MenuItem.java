/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package navmenu;

import wicket.PageParameters;

/**
 * Represents an entry in a page navigation menu.
 *
 * @author Eelco Hillenius
 */
public final class MenuItem
{
	/** label of the menu item. */
	private String label;

	/** class of the page. */
	private Class pageClass;

	/** optional page parameters. */
	private PageParameters pageParameters;

	/**
	 * Construct.
	 */
	public MenuItem()
	{
		super();
	}

	/**
	 * Construct.
	 * @param label label of the menu item
	 * @param pageClass class of the page
	 * @param pageParameters optional page parameters
	 */
	public MenuItem(String label, Class pageClass, PageParameters pageParameters)
	{
		super();
		this.label = label;
		this.pageClass = pageClass;
		this.pageParameters = pageParameters;
	}

	/**
	 * Gets the label of the menu item.
	 * @return the label of the menu item
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * Sets the label of the menu item.
	 * @param label the label of the menu item
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * Gets the class of the page.
	 * @return the class of the page
	 */
	public Class getPageClass()
	{
		return pageClass;
	}

	/**
	 * Sets the class of the page.
	 * @param pageClass the class of the page
	 */
	public void setPageClass(Class pageClass)
	{
		this.pageClass = pageClass;
	}

	/**
	 * Gets the optional page parameters.
	 * @return the page parameters
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * Sets the page parameters.
	 * @param pageParameters the page parameters
	 */
	public void setPageParameters(PageParameters pageParameters)
	{
		this.pageParameters = pageParameters;
	}
}
