/*
 * $Id$ $Revision$
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
package wicket.markup.html.tree;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import wicket.markup.html.panel.Panel;

/**
 * Base component for trees. The trees from this package work with the Swing
 * tree models and {@link javax.swing.tree.DefaultMutableTreeNode}s. Hence,
 * users can re-use their Swing tree models.
 * 
 * @author Eelco Hillenius
 */
public abstract class Tree extends Panel
{
	/** tree state for this component. */
	private TreeStateCache treeState;

	/**
	 * Construct using the given model as the tree model to use. A new tree state
	 * will be constructed by calling newTreeState.
	 * @param componentName The name of this container
	 * @param model the underlying tree model
	 */
	public Tree(final String componentName, final TreeModel model)
	{
		super(componentName);
		this.treeState = newTreeState(model);
	}

	/**
	 * Sets the new expanded state (to true), based on the given user node and
	 * set the tree path to the currently selected.
	 * @param node the tree node model
	 */
	public final void setSelected(DefaultMutableTreeNode node)
	{
		TreePath selection = new TreePath(node.getPath());
		treeState.setSelectedPath(selection);
		setExpandedState(selection, true);
	}

	/**
	 * Sets the new expanded state, based on the given node
	 * @param node the tree node model
	 */
	public final void setExpandedState(DefaultMutableTreeNode node)
	{
		TreePath selection = new TreePath(node.getPath());
		setExpandedState(selection,
				(!treeState.isExpanded(selection))); // inverse
	}

	/**
	 * Sets the expanded property in the stree state for selection.
	 * @param selection the selection to set the expanded property for
	 * @param expanded true if the selection is expanded, false otherwise
	 */
	public final void setExpandedState(TreePath selection, boolean expanded)
	{
		treeState.setExpandedState(selection, expanded);
	}

	/**
	 * Convenience method that determines whether the path of the
	 * given tree node is expanded in this tree's state.
	 * @param node the tree node
	 * @return whether the path of the given tree node is expanded
	 */
	public final boolean isExpanded(DefaultMutableTreeNode node)
	{
		return isExpanded(new TreePath(node.getPath()));
	}

	/**
	 * Convenience method that determines whether the given path is expanded
	 * in this tree's state.
	 * @param path the tree path
	 * @return whether the given path is expanded
	 */
	public final boolean isExpanded(TreePath path)
	{
		return treeState.isExpanded(path);
	}

	/**
	 * Gets the current tree state.
	 * @return the tree current tree state
	 */
	public final TreeStateCache getTreeState()
	{
		return treeState;
	}

	/**
	 * Sets the current tree state to the given tree state.
	 * @param treeState the tree state to set as the current one
	 */
	public final void setTreeState(final TreeStateCache treeState)
	{
		this.treeState = treeState;
	}

	/**
	 * Creates a new tree state by creating a new {@link TreeStateCache}object,
	 * which is then set as the current tree state, creating a new
	 * {@link TreeSelectionModel}and then calling setTreeModel with this
	 * @param model the model that the new tree state applies to
	 * @return the tree state
	 */
	public TreeStateCache newTreeState(final TreeModel model)
	{
		return newTreeState(model, true);
	}


	/**
	 * Creates a new tree state by creating a new {@link TreeStateCache}object,
	 * which is then set as the current tree state, creating a new
	 * {@link TreeSelectionModel}and then calling setTreeModel with this
	 * @param model the model that the new tree state applies to
	 * @param rootVisible whether the tree node should be displayed
	 * @return the tree state
	 */
	protected final TreeStateCache newTreeState(final TreeModel model, boolean rootVisible)
	{
		TreeStateCache treeStateCache = new TreeStateCache();
		TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeStateCache.setModel(model);
		treeStateCache.setSelectionModel(selectionModel);
		treeStateCache.setRootVisible(rootVisible);
		model.addTreeModelListener(treeStateCache);
		return treeStateCache;
	}

	/**
	 * Sets whether the tree node should be displayed.
	 * @param rootVisible whether the tree node should be displayed
	 */
	public final void setRootVisible(boolean rootVisible)
	{
		treeState.setRootVisible(rootVisible);
	}

	/**
	 * Gives the current tree model as a string.
	 * @return the current tree model as a string
	 */
	public final String getTreeModelAsDebugString()
	{
		StringBuffer b = new StringBuffer("-- TREE MODEL --\n");
		TreeStateCache state = getTreeState();
		TreeModel treeModel = null;
		if (state != null)
		{
			treeModel = state.getModel();
		}
		if (treeModel != null)
		{
			StringBuffer tabs = new StringBuffer();
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
			Enumeration e = rootNode.preorderEnumeration();
			while (e.hasMoreElements())
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
				tabs.delete(0, tabs.length());
				tabs.append("|");
				for (int i = 0; i < node.getLevel(); i++)
				{
					tabs.append("-");
				}
				b.append(tabs).append(node).append("\n");
			}
		}
		else
		{
			b.append("<EMPTY>");
		}
		return b.toString();
	}
}
