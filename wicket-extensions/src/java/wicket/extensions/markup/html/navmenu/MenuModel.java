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

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import wicket.Page;

/**
 * Model that holds the tree model of a navigation menu.
 *
 * @author Eelco Hillenius
 */
public class MenuModel extends DefaultTreeModel
{
	/**
	 * Construct.
	 * @param menuItem
	 */
	public MenuModel(MenuItem menuItem)
	{
		super(menuItem);
	}

	/**
	 * Whether the given menu item is part of the currently selected path
	 * @param currentPage the current page
	 * @param menuItem the menu item
	 * @return true if the given menu item is part of the currently selected path
	 */
	public boolean isPartOfCurrentSelection(Page currentPage, MenuItem menuItem)
	{
		MenuTreePath selection = getCurrentSelection(currentPage);
		return selection.isPartOfPath(menuItem);
	}

	/**
	 * @see javax.swing.tree.DefaultTreeModel#setRoot(javax.swing.tree.TreeNode)
	 */
	public void setRoot(TreeNode root)
	{
		if (root == null)
		{
			throw new NullPointerException("treeNode may not be null");
		}
		if (!(root instanceof MenuItem))
		{
			throw new IllegalArgumentException("argument must be of type " +
					MenuItem.class.getName() + " (but is of type " +
					root.getClass().getName() + ")");
		}
		super.setRoot(root);
	}

	/**
	 * Gets the tree path to the currently selected menu item.
	 * @param currentPage the current page
	 * @return the tree path to the currently selected menu item
	 */
	protected MenuTreePath getCurrentSelection(Page currentPage)
	{
		Class currentPageClass = currentPage.getClass();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();
		MenuTreePath currentPath = null;
	
		Enumeration all = root.breadthFirstEnumeration();
		while (all.hasMoreElements())
		{
			MenuItem menuItem = (MenuItem)all.nextElement();
			if (currentPageClass.equals(menuItem.getPageClass()))
			{
				currentPath = new MenuTreePath(menuItem.getPath());
				break;
			}
		}
		if (currentPath == null)
		{
			throw new IllegalStateException(currentPageClass + " is not a member of the menu");
		}
		return currentPath;
	}
}
