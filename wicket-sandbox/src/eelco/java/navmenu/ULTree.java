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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.contrib.markup.html.tree.Tree;

/**
 * Tree that renders as nested lists (UL/ LI).
 *
 * @author Eelco Hillenius
 */
class ULTree extends Tree
{
	/** Log. */
	private static Log log = LogFactory.getLog(ULTree.class);

	/**
	 * structure with nested nodes and lists to represent the tree model
	 * using lists.
	 */
	private List nestedList;

	/**
	 * Construct.
	 * @param componentName The name of this container
	 * @param model the tree model
	 */
	public ULTree(String componentName, TreeModel model)
	{
		super(componentName, model);
		setRootVisible(false);
		buildNestedListModel(model);
		UL treeRowsListView = new UL("rows", nestedList, 0);
		add(treeRowsListView);
	}

	/**
	 * Builds the internal structure.
	 * @param model the tree model that the internal structure is to be based on
	 */
	private void buildNestedListModel(TreeModel model)
	{
		nestedList = new ArrayList(); // reference to the first level list
		if (model != null)
		{
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
			if(root != null)
			{
				Enumeration children = root.children();
				while(children.hasMoreElements())
				{
					DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
					add(nestedList, child);
				}
			}
		}
	}

	/**
	 * Add node to list and add any childs recursively.
	 * @param list the list to add the node to
	 * @param node the node to add
	 */
	private void add(List list, DefaultMutableTreeNode node)
	{
		list.add(node);
		Enumeration children = node.children();
		if(children.hasMoreElements()) // any elements?
		{
			List childList = new ArrayList();
			list.add(childList);
			while(children.hasMoreElements())
			{
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
				add(childList, child);
			}
		}
	}
}