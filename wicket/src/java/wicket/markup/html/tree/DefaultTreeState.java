package wicket.markup.html.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.tree.TreeNode;

/**
 * Default implementation of TreeState.
 * <p>
 * This implementation tries to be as lightweight as possible.
 * 
 * @author Matej Knopp
 */
public class DefaultTreeState implements ITreeState, Serializable
{
	private static final long serialVersionUID = 1L;

	/** Whether multiple selections can be done. */
	private boolean allowSelectMultiple = false;

	/** Tree state listeners. */
	private final List<ITreeStateListener> listeners = new ArrayList<ITreeStateListener>(1);

	/**
	 * set of nodes which are collapsed or expanded (depends on nodesCollapsed
	 * veriable).
	 */
	private final Set<TreeNode> nodes = new HashSet<TreeNode>();

	/** Whether the nodes set should be treated as collapsed or expanded. */
	private boolean nodesCollapsed = true;

	/** Set selected nodes. */
	private final Set<TreeNode> selectedNodes = new HashSet<TreeNode>();

	/**
	 * @see wicket.markup.html.tree.ITreeState#addTreeStateListener(wicket.markup.html.tree.ITreeStateListener)
	 */
	public void addTreeStateListener(ITreeStateListener l)
	{
		if (listeners.contains(l) == false)
		{
			listeners.add(l);
		}
	}

	/**
	 * @see wicket.markup.html.tree.ITreeState#collapseAll()
	 */
	public void collapseAll()
	{
		if (nodes.isEmpty() && nodesCollapsed == false)
		{
			// all nodes are already collapsed, do nothing
		}
		else
		{
			// clear all nodes from the set and sets the nodes as expanded
			nodes.clear();
			nodesCollapsed = false;

			for (ITreeStateListener l : listeners)
			{
				l.allNodesCollapsed();
			}
		}
	}

	/**
	 * @see wicket.markup.html.tree.ITreeState#collapseNode(javax.swing.tree.TreeNode)
	 */
	public void collapseNode(TreeNode node)
	{
		if (nodesCollapsed == true)
		{
			nodes.add(node);
		}
		else
		{
			nodes.remove(node);
		}

		for (ITreeStateListener l : listeners)
		{
			l.nodeCollapsed(node);
		}
	}

	/**
	 * @see wicket.markup.html.tree.ITreeState#expandAll()
	 */
	public void expandAll()
	{
		if (nodes.isEmpty() && nodesCollapsed == true)
		{
			// all nodes are already expanded, do nothing
		}
		else
		{
			// clear node set and set nodes policy as collapsed
			nodes.clear();
			nodesCollapsed = true;

			for (ITreeStateListener l : listeners)
			{
				l.allNodesCollapsed();
			}
		}
	}

	/**
	 * @see wicket.markup.html.tree.ITreeState#expandNode(javax.swing.tree.TreeNode)
	 */
	public void expandNode(TreeNode node)
	{
		if (nodesCollapsed == false)
		{
			nodes.add(node);
		}
		else
		{
			nodes.remove(node);
		}

		for (ITreeStateListener l : listeners)
		{
			l.nodeExpanded(node);
		}
	}

	/**
	 * @see wicket.markup.html.tree.ITreeState#getSelectedNodes()
	 */
	public Collection<TreeNode> getSelectedNodes()
	{
		return selectedNodes;
	}

	/**
	 * @see wicket.markup.html.tree.ITreeState#isAllowSelectMultiple()
	 */
	public boolean isAllowSelectMultiple()
	{
		return allowSelectMultiple;
	}

	/**
	 * @see wicket.markup.html.tree.ITreeState#isNodeExpanded(javax.swing.tree.TreeNode)
	 */
	public boolean isNodeExpanded(TreeNode node)
	{
		if (nodesCollapsed == false)
		{
			return nodes.contains(node);
		}
		else
		{
			return nodes.contains(node) == false;
		}
	}

	/**
	 * @see wicket.markup.html.tree.ITreeState#isNodeSelected(javax.swing.tree.TreeNode)
	 */
	public boolean isNodeSelected(TreeNode node)
	{
		return selectedNodes.contains(node);
	}

	/**
	 * @see wicket.markup.html.tree.ITreeState#removeTreeStateListener(wicket.markup.html.tree.ITreeStateListener)
	 */
	public void removeTreeStateListener(ITreeStateListener l)
	{
		listeners.remove(l);
	}

	/**
	 * @see wicket.markup.html.tree.ITreeState#selectNode(javax.swing.tree.TreeNode,
	 *      boolean)
	 */
	public void selectNode(TreeNode node, boolean selected)
	{
		if (selected == true && selectedNodes.contains(node) == false)
		{
			if (isAllowSelectMultiple() == false && selectedNodes.size() > 0)
			{
				for (Iterator<TreeNode> i = selectedNodes.iterator(); i.hasNext();)
				{
					TreeNode current = i.next();
					i.remove();
					for (ITreeStateListener l : listeners)
					{
						l.nodeUnselected(current);
					}
				}
			}
			selectedNodes.add(node);
			for (ITreeStateListener l : listeners)
			{
				l.nodeSelected(node);
			}
		}
		else if (selected == false && selectedNodes.contains(node) == true)
		{
			selectedNodes.remove(node);
			for (ITreeStateListener l : listeners)
			{
				l.nodeUnselected(node);
			}
		}
	}

	/**
	 * @see wicket.markup.html.tree.ITreeState#setAllowSelectMultiple(boolean)
	 */
	public void setAllowSelectMultiple(boolean value)
	{
		this.allowSelectMultiple = value;
	}
}
