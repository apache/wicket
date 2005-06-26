/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.navmenu;

import java.io.Serializable;

import wicket.ResourceReference;

/**
 * Groups style elements for a navigation menu row, so that we don't have to provide
 * custom markup all the time.
 *
 * @author Eelco Hillenius
 */
public class MenuRowStyle implements Serializable
{
	/** the CSS style file. */
	private ResourceReference styleSheetResource;

	/** CSS class for the container of this row, e.g. the div element. */
	private String containerCSSClass;

	/** CSS class for the row, e.g. the ul element. */
	private String rowCSSClass;

	/**
	 * Construct.
	 */
	public MenuRowStyle()
	{
	}

	/**
	 * Gets the CSS class for the given menu item, to be attached e.g. to an li element.
	 * @param menuItem the menu item
	 * @param menuRow the menu row component
	 * @return the CSS class for the given menu item
	 */
	protected String getItemCSSClass(MenuItem menuItem, MenuRow menuRow)
	{
		MenuRowModel rowModel = (MenuRowModel)menuRow.getModel();
		boolean active = (rowModel.isPartOfCurrentSelection(menuRow.getPage(), menuItem));
		return (active) ? "selectedTab" : null;		
	}

	/**
	 * Gets the style.
	 * @return style
	 */
	public final ResourceReference getStyleSheetResource()
	{
		return styleSheetResource;
	}

	/**
	 * Sets the style.
	 * @param style style
	 */
	public final void setStyleSheetResource(ResourceReference style)
	{
		this.styleSheetResource = style;
	}

	/**
	 * Gets the cssClass.
	 * @return cssClass
	 */
	public final String getRowCSSClass()
	{
		return rowCSSClass;
	}

	/**
	 * Sets the cssClass.
	 * @param cssClass cssClass
	 */
	public final void setRowCSSClass(String cssClass)
	{
		this.rowCSSClass = cssClass;
	}

	/**
	 * Gets the rowClass.
	 * @return rowClass
	 */
	public final String getContainerCSSClass()
	{
		return containerCSSClass;
	}

	/**
	 * Sets the rowClass.
	 * @param rowClass rowClass
	 */
	public final void setContainerCSSClass(String rowClass)
	{
		this.containerCSSClass = rowClass;
	}
}