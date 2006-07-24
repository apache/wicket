package wicket.xtree;

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
 * @author Matej Knopp
 */
public class DefaultTreeState implements ITreeState, Serializable 
{
	// set of nodes which are collapsed or expanded (depends on nodesCollapsed veriable)
	private Set<TreeNode> nodes = new HashSet<TreeNode>();
	
	// wether the nodes set should be treated as collapsed or expanded
	private boolean nodesCollapsed = true;  
	
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
			
			for (TreeStateListener l : listeners)
			{
				l.allNodesCollapsed();
			}
		}
	}
	
	public void collapseNode(TreeNode node) 
	{
		if (nodesCollapsed == true)
			nodes.add(node);
		else
			nodes.remove(node);
		
		for (TreeStateListener l : listeners)
		{
			l.nodeCollapsed(node);
		}
	}

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
		
			for (TreeStateListener l : listeners)
				l.allNodesCollapsed();
		}
	}

	public void expandNode(TreeNode node) 
	{
		if (nodesCollapsed == false)
			nodes.add(node);
		else
			nodes.remove(node);
		
		for (TreeStateListener l : listeners)
			l.nodeExpanded(node);
	}

	public boolean isNodeExpanded(TreeNode node) 
	{
		if (nodesCollapsed == false)
			return nodes.contains(node);
		else
			return nodes.contains(node) == false;
	}
		
	private Set<TreeNode> selectedNodes = new HashSet<TreeNode>();
	
	public Collection<TreeNode> getSelectedNodes() 
	{
		return selectedNodes;
	}

	private boolean allowSelectMultiple = false;
	
	public boolean isAllowSelectMultiple() 
	{
		return allowSelectMultiple;
	}
	
	public void setAllowSelectMultiple(boolean value) 
	{
		this.allowSelectMultiple = value;
	}

	public boolean isNodeSelected(TreeNode node) 
	{
		return selectedNodes.contains(node);
	}	

	public void selectNode(TreeNode node, boolean selected) 
	{
		if (selected == true && selectedNodes.contains(node) == false)
		{
			if (isAllowSelectMultiple() == false && selectedNodes.size() > 0)
			{
				for (Iterator<TreeNode> i = selectedNodes.iterator(); i.hasNext(); )
				{					
					TreeNode current = i.next();
					i.remove();					
					for (TreeStateListener l : listeners)
					{
						l.nodeUnselected(current);
					}					
				}					
			}
			selectedNodes.add(node);
			for (TreeStateListener l : listeners)
			{
				l.nodeSelected(node);
			}
		}
		else if (selected == false && selectedNodes.contains(node) == true)
		{
			selectedNodes.remove(node);
			for (TreeStateListener l : listeners)
			{
				l.nodeUnselected(node);
			}
		}
	}

	private List<TreeStateListener> listeners = new ArrayList<TreeStateListener>();
	
	public void addTreeStateListener(TreeStateListener l) 
	{
		if (listeners.contains(l) == false)
			listeners.add(l);
	}

	public void removeTreeStateListener(TreeStateListener l) 
	{
		listeners.remove(l);		
	}
}
