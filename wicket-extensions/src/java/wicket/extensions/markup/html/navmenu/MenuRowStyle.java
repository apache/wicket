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
 * The settings of the date picker component. Use this to customize the datepicker
 * (e.g. the icon, locale, format, etc).
 *
 * @author Eelco Hillenius
 */
public class MenuRowStyle implements Serializable
{
	/** the CSS style file. */
	private ResourceReference styleSheetResource;

	/** CSS class for this row. */
	private String cssClass;

	/**
	 * Construct.
	 */
	public MenuRowStyle()
	{
	}

	/**
	 * Gets the CSS class for the given menu item.
	 * @param menuItem the menu item
	 * @param menuRow the menu row component
	 * @return the CSS class for the given menu item
	 */
	protected String getCSSClass(MenuItem menuItem, MenuRow menuRow)
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
	public String getCssClass()
	{
		return cssClass;
	}

	/**
	 * Sets the cssClass.
	 * @param cssClass cssClass
	 */
	public void setCssClass(String cssClass)
	{
		this.cssClass = cssClass;
	}
}