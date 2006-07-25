package wicket.markup.html.tree;

import java.util.Collection;

import javax.swing.tree.TreeNode;

/**
 * Tree state holds information about a tree such as which nodes are 
 * expanded / collapsed and which nodes are selected,
 * It can also fire callbacks on listener in case any of the information changed.
 * @author Matej Knopp
 */
public interface ITreeState  
{
	/**
	 * Expands all nodes of the tree.
	 */
	public void expandAll();
	
	/**
	 * Collapses all nodes of the tree.
	 */
	public void collapseAll();

	/**
	 * Expands the given node.
	 */
	public void expandNode(TreeNode node);

	/**
	 * Collapses the given node.
	 */
	public void collapseNode(TreeNode node);

	/**
	 * Returns true if the given node is expanded.
	 */
	public boolean isNodeExpanded(TreeNode node);
	
	

	/**
	 * Sets whether multiple nodes can be selected.
	 */
	public void setAllowSelectMultiple(boolean value);
	
	/**
	 * Returns whether multiple nodes can be selected.
	 */
	public boolean isAllowSelectMultiple();
	
	/**
	 * Marks given node as selected (or unselected) according to the
	 * selected value.
	 * <p>
	 * If tree is in single selection mode and a new node is selected,
	 * old node is automatically unselected (and the event is fired on listeners).
	 */
	public void selectNode(TreeNode node, boolean selected);
	
	/**
	 * Returns true if the given node is selected, false otherwise.
	 */
	public boolean isNodeSelected(TreeNode node);

	/**
	 * Returns the collection of all selected nodes.
	 */
	public Collection<TreeNode> getSelectedNodes();
	
	
	
	/**
	 * Adds a tree state listener. On state change events on the listener are fired.
	 */
	public void addTreeStateListener(TreeStateListener l);
	
	/**
	 * Removes a tree state listener. 
	 */
	public void removeTreeStateListener(TreeStateListener l);
}
