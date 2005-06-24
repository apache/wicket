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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import wicket.Component;
import wicket.Page;
import wicket.RequestCycle;

/**
 * 
 */
public class MenuRowModel extends MenuModel
{
	/** menu level. */
	private final int level;

	/** current row. */
	private transient List row;

	/**
	 * Construct.
	 * @param treeModel 
	 * @param level 
	 */
	public MenuRowModel(TreeModel treeModel, int level)
	{
		super(treeModel);
		this.level = level;
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
			MenuTreePath currentSelection = getCurrentSelection(currentPage);
			if (currentSelection.getPathCount() > level)
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)currentSelection.getPathComponent(level);
				int len = node.getChildCount();
				RequestCycle requestCycle = component.getRequestCycle();
				for (int i = 0; i < len; i++)
				{
					DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
					MenuItem item = (MenuItem)child.getUserObject();
					if (item.checkAccess(requestCycle))
					{
						row.add(child.getUserObject());
					}
				}
			}
		}
		return row;
	}
}
