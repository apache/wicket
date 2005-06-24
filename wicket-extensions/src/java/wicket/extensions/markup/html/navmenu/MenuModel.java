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
import javax.swing.tree.TreeModel;

import wicket.Page;
import wicket.model.AbstractReadOnlyDetachableModel;
import wicket.model.IModel;

/**
 * 
 */
public abstract class MenuModel extends AbstractReadOnlyDetachableModel
{
	/** tree model. */
	private final TreeModel treeModel;

	/**
	 * Construct.
	 * @param treeModel 
	 */
	public MenuModel(TreeModel treeModel)
	{
		super();
		this.treeModel = treeModel;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#getNestedModel()
	 */
	public final IModel getNestedModel()
	{
		return null;
	}

	/**
	 * Gets the treeModel.
	 * @return treeModel
	 */
	public final TreeModel getTreeModel()
	{
		return treeModel;
	}

	/**
	 * Whether the given menu item is part of the currently selected path
	 * @param currentPage the current page
	 * @param menuItem the menu item
	 * @return true if the given menu item is part of the currently selected path
	 */
	public boolean isPartOfCurrentSelection(Page currentPage, MenuItem menuItem)
	{
		DefaultMutableTreeNode node = findNodeFor(menuItem);
		MenuTreePath selection = getCurrentSelection(currentPage);
		return selection.isPartOfPath(node);
	}

	/**
	 * Finds the tree node that has the given menu item attached.
	 * @param menuItem the menu item to lookup
	 * @return the tree node
	 */
	protected final DefaultMutableTreeNode findNodeFor(MenuItem menuItem)
	{
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getTreeModel().getRoot();
		Enumeration all = root.breadthFirstEnumeration();
		while (all.hasMoreElements())
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)all.nextElement();
			Object userObject = node.getUserObject();
			if (menuItem.equals(userObject))
			{
				return node;
			}
		}

		throw new IllegalStateException("node for " + menuItem + " not found");
	}

	/**
	 * Gets the tree path to the currently selected menu item.
	 * @param currentPage the current page
	 * @return the tree path to the currently selected menu item
	 */
	protected MenuTreePath getCurrentSelection(Page currentPage)
	{
		Class currentPageClass = currentPage.getClass();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getTreeModel().getRoot();
		MenuTreePath currentPath = null;
	
		Enumeration all = root.breadthFirstEnumeration();
		while (all.hasMoreElements())
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)all.nextElement();
			Object userObject = node.getUserObject();
			if (userObject instanceof MenuItem)
			{
				MenuItem menuItem = (MenuItem)userObject;
				if (currentPageClass.equals(menuItem.getPageClass()))
				{
					currentPath = new MenuTreePath(node.getPath());
					break;
				}
			}
		}
		if (currentPath == null)
		{
			throw new IllegalStateException(currentPageClass + " is not a member of the menu");
		}
		return currentPath;
	}
}
