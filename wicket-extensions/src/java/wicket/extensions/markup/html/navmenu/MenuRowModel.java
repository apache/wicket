/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.extensions.markup.html.navmenu;

import java.util.ArrayList;
import java.util.List;

import wicket.Component;
import wicket.Page;
import wicket.RequestCycle;
import wicket.model.AbstractReadOnlyDetachableModel;
import wicket.model.IModel;

/**
 * Menu model for one row.
 *
 * @author Eelco Hillenius
 */
public class MenuRowModel extends AbstractReadOnlyDetachableModel
{
	/** menu level. */
	private final int level;

	/** the tree model. */
	private final MenuModel menuModel;

	/** current row. */
	private transient List row;

	/**
	 * Construct.
	 * @param menuModel 
	 * @param level 
	 */
	public MenuRowModel(MenuModel menuModel, int level)
	{
		this.menuModel = menuModel;
		this.level = level;
	}

	/**
	 * Whether the given menu item is part of the currently selected path
	 * @param currentPage the current page
	 * @param menuItem the menu item
	 * @return true if the given menu item is part of the currently selected path
	 */
	public boolean isPartOfCurrentSelection(Page currentPage, MenuItem menuItem)
	{
		return menuModel.isPartOfCurrentSelection(currentPage, menuItem);
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onAttach()
	 */
	protected void onAttach()
	{
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		this.row = null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onGetObject(wicket.Component)
	 */
	protected Object onGetObject(Component component)
	{
		// lazily attach
		if (row == null)
		{
			row = new ArrayList();
			Page currentPage = component.getPage();
			MenuTreePath currentSelection = menuModel.getCurrentSelection(currentPage);
			if (currentSelection.getPathCount() > level)
			{
				MenuItem node = (MenuItem)currentSelection.getPathComponent(level);
				int len = node.getChildCount();
				RequestCycle requestCycle = component.getRequestCycle();
				for (int i = 0; i < len; i++)
				{
					MenuItem child = (MenuItem)node.getChildAt(i);
					if (child.checkAccess(requestCycle))
					{
						row.add(child);
					}
				}
			}
		}
		return row;
	}
}
