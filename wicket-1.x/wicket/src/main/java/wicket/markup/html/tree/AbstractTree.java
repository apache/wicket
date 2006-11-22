/*
 * $Id: AbstractTree.java 2527 2005-08-16 22:33:01 +0000 (Tue, 16 Aug 2005)
 * eelco12 $ $Revision$ $Date: 2005-08-16 22:33:01 +0000 (Tue, 16 Aug
 * 2005) $
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
	 * @param id
	 *            The id of this component
	 * @param model
	 *            the underlying tree model
	 */
	public AbstractTree(final String id, final TreeModel model)
	{
		super(id);
		this.treeState = newTreeState(model);
	}

	/**
	 * Construct using the given tree state that holds the model to be used as
	 * the tree model.
	 * 
	 * @param id
	 *            The id of this component
	 * @param treeState
	 *            treeState that holds the underlying tree model
	 */
	public AbstractTree(final String id, final TreeState treeState)
	{
		super(id);
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
	 * Collapses all the siblings of a given node.
	 * 
	 * @param node
	 *            The node of which to collapse the siblings.
	 */
	public void collapseSiblings(final DefaultMutableTreeNode node)
	{
		// Collapse all previous siblings
		DefaultMutableTreeNode previousNode = node.getPreviousSibling();
		while (null != previousNode)
		{
			final TreePath siblingSelection = new TreePath(previousNode.getPath());
			setExpandedState(siblingSelection, false); // inverse
			previousNode = previousNode.getPreviousSibling();
		}
		// Collapse all following siblings
		DefaultMutableTreeNode nextNode = node.getNextSibling();
		while (null != nextNode)
		{
			final TreePath siblingSelection = new TreePath(nextNode.getPath());
			setExpandedState(siblingSelection, false); // inverse
			// ToDo: Check if previousNode can be null? If so, needs trapping - Gwyn
			nextNode = previousNode.getNextSibling();
		}
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
	 * Returns true if the value identified by path is currently viewable, which
	 * means it is either the root or all of its parents are expanded.
	 * Otherwise, this method returns false.
	 * 
	 * @param path
	 *            The path
	 * 
	 * @return true if the node is viewable, otherwise false
	 */
	public final boolean isVisible(TreePath path)
	{
		if (path != null)
		{
			TreePath parentPath = path.getParentPath();

			if (parentPath != null)
				return isExpanded(parentPath);
			// Root.
			return true;
		}
		return false;
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
	public void setExpandedState(final DefaultMutableTreeNode node)
	{
		final TreePath selection = new TreePath(node.getPath());
		setExpandedState(selection, (!treeState.isExpanded(selection))); // inverse

		// If set to SINGLE_TREE_SELECTION, collapse all sibling nodes
		final int selectionType = getTreeState().getSelectionModel().getSelectionMode();
		if (TreeSelectionModel.SINGLE_TREE_SELECTION == selectionType)
		{
			collapseSiblings(node);
		}
	}

	/**
	 * Sets the expanded property in the stree state for selection.
	 * 
	 * @param selection
	 *            the selection to set the expanded property for
	 * @param expanded
	 *            true if the selection is expanded, false otherwise
	 */
	public void setExpandedState(final TreePath selection, final boolean expanded)
	{
		treeState.setExpandedState(selection, expanded);
	}

	/**
	 * Sets whether the tree root node should be displayed.
	 * 
	 * @param rootVisible
	 *            whether the tree node should be displayed
	 */
	public void setRootVisible(final boolean rootVisible)
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
	public void setSelected(final DefaultMutableTreeNode node)
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
	public String toString()
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

	/**
	 * Expand recursively.
	 * 
	 * @param parent
	 *            The current parent node
	 * @param expand
	 *            Whether to expand or to collapse
	 */
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
