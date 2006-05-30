/*
 * $Id: AbstractTree.java 5844 2006-05-24 20:53:56 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 20:53:56 +0000 (Wed, 24 May
 * 2006) $
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
package wicket.markup.html.tree;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import wicket.MarkupContainer;
import wicket.markup.html.panel.Panel;

/**
 * Base component for trees. The trees from this package work with the Swing
 * tree models and {@link javax.swing.tree.DefaultMutableTreeNode}s. Hence,
 * users can re-use their Swing tree models.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractTree extends Panel
{
	/** AbstractTree state for this component. */
	private TreeState treeState;

	/**
	 * Construct using the given model as the tree model to use. A new tree
	 * state will be constructed by calling newTreeState.
	 * 
	 * @param parent
	 *            The parent
	 * 
	 * @param id
	 *            The id of this component
	 * @param model
	 *            the underlying tree model
	 */
	public AbstractTree(MarkupContainer parent, final String id, final TreeModel model)
	{
		super(parent, id);
		this.treeState = newTreeState(model);
	}

	/**
	 * Construct using the given tree state that holds the model to be used as
	 * the tree model.
	 * 
	 * @param parent
	 *            The parent
	 * 
	 * @param id
	 *            The id of this component
	 * @param treeState
	 *            treeState that holds the underlying tree model
	 */
	public AbstractTree(MarkupContainer parent, final String id, final TreeState treeState)
	{
		super(parent, id);
		this.treeState = treeState;
	}

	/**
	 * Ensures that the node identified by the specified path is collapsed and
	 * viewable.
	 * 
	 * @param path
	 *            the <code>TreePath</code> identifying a node
	 */
	public void collapsePath(TreePath path)
	{
		setExpandedState(path, false);
	}

	/**
	 * Expand or collapse all nodes.
	 * 
	 * @param expand
	 *            If true, expand all nodes in the tree. Else collapse all nodes
	 *            in the tree.
	 */
	public void expandAll(boolean expand)
	{
		TreeNode root = (TreeNode)getTreeState().getModel().getRoot();
		expandAll(new TreePath(root), expand);
	}

	/**
	 * Ensures that the node identified by the specified path is expanded and
	 * viewable. If the last item in the path is a leaf, this will have no
	 * effect.
	 * 
	 * @param path
	 *            the <code>TreePath</code> identifying a node
	 */
	public void expandPath(TreePath path)
	{
		// Only expand if not leaf!
		TreeModel model = getTreeState().getModel();

		if (path != null && model != null && !model.isLeaf(path.getLastPathComponent()))
		{
			setExpandedState(path, true);
		}
	}

	/**
	 * Gets the current tree state.
	 * 
	 * @return the tree current tree state
	 */
	public final TreeState getTreeState()
	{
		return treeState;
	}

	/**
	 * Convenience method that determines whether the path of the given tree
	 * node is expanded in this tree's state.
	 * 
	 * @param node
	 *            the tree node
	 * @return whether the path of the given tree node is expanded
	 */
	public final boolean isExpanded(DefaultMutableTreeNode node)
	{
		return isExpanded(new TreePath(node.getPath()));
	}

	/**
	 * Convenience method that determines whether the given path is expanded in
	 * this tree's state.
	 * 
	 * @param path
	 *            the tree path
	 * @return whether the given path is expanded
	 */
	public final boolean isExpanded(TreePath path)
	{
		return treeState.isExpanded(path);
	}

	/**
	 * Gets whether the tree root node should be displayed.
	 * 
	 * @return whether the tree root node should be displayed
	 */
	public final boolean isRootVisible()
	{
		return treeState.isRootVisible();
	}

	/**
	 * Creates a new tree state by creating a new {@link TreeState}object,
	 * which is then set as the current tree state, creating a new
	 * {@link TreeSelectionModel}and then calling setTreeModel with this
	 * 
	 * @param model
	 *            the model that the new tree state applies to
	 * @return the tree state
	 */
	public TreeState newTreeState(final TreeModel model)
	{
		return newTreeState(model, true);
	}

	/**
	 * Sets the new expanded state, based on the given node
	 * 
	 * @param node
	 *            the tree node model
	 */
	public final void setExpandedState(final DefaultMutableTreeNode node)
	{
		final TreePath selection = new TreePath(node.getPath());
		setExpandedState(selection, (!treeState.isExpanded(selection))); // inverse
	}

	/**
	 * Sets the expanded property in the stree state for selection.
	 * 
	 * @param selection
	 *            the selection to set the expanded property for
	 * @param expanded
	 *            true if the selection is expanded, false otherwise
	 */
	public final void setExpandedState(final TreePath selection, final boolean expanded)
	{
		treeState.setExpandedState(selection, expanded);
	}

	/**
	 * Sets whether the tree root node should be displayed.
	 * 
	 * @param rootVisible
	 *            whether the tree node should be displayed
	 */
	public final void setRootVisible(final boolean rootVisible)
	{
		treeState.setRootVisible(rootVisible);
	}

	/**
	 * Sets the new expanded state (to true), based on the given user node and
	 * set the tree path to the currently selected.
	 * 
	 * @param node
	 *            the tree node model
	 */
	public final void setSelected(final DefaultMutableTreeNode node)
	{
		final TreePath selection = new TreePath(node.getPath());
		treeState.setSelectedPath(selection);
		setExpandedState(selection, true);
	}

	/**
	 * Sets the current tree model.
	 * 
	 * @param treeModel
	 *            the tree model to set as the current one
	 */
	public void setTreeModel(final TreeModel treeModel)
	{
		this.treeState = newTreeState(treeModel);
	}

	/**
	 * Sets the current tree state to the given tree state.
	 * 
	 * @param treeState
	 *            the tree state to set as the current one
	 */
	public void setTreeState(final TreeState treeState)
	{
		this.treeState = treeState;
	}

	/**
	 * Gives the current tree model as a string.
	 * 
	 * @return the current tree model as a string
	 */
	@Override
	public final String toString()
	{
		StringBuffer b = new StringBuffer("-- TREE MODEL --\n");
		TreeState state = getTreeState();
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

	/**
	 * Creates a new tree state by creating a new {@link TreeState}object,
	 * which is then set as the current tree state, creating a new
	 * {@link TreeSelectionModel}and then calling setTreeModel with this
	 * 
	 * @param treeModel
	 *            the model that the new tree state applies to
	 * @param rootVisible
	 *            whether the tree node should be displayed
	 * @return the tree state
	 */
	protected final TreeState newTreeState(final TreeModel treeModel, final boolean rootVisible)
	{
		final TreeState treeState = new TreeState();
		final TreeSelectionModel treeSelectionModel = new DefaultTreeSelectionModel();
		treeSelectionModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		treeState.setModel(treeModel);
		treeState.setSelectionModel(treeSelectionModel);
		treeState.setRootVisible(rootVisible);
		treeModel.addTreeModelListener(treeState);
		return treeState;
	}

	private final void expandAll(TreePath parent, boolean expand)
	{
		TreeNode node = (TreeNode)parent.getLastPathComponent();
		if (node.getChildCount() >= 0)
		{
			for (Enumeration e = node.children(); e.hasMoreElements();)
			{
				TreeNode n = (TreeNode)e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(path, expand);
			}
		}

		if (expand)
		{
			expandPath(parent);
		}
		else
		{
			collapsePath(parent);
		}
	}
}
