package wicket.markup.html.tree;

import java.util.Collection;

import javax.swing.tree.TreeNode;

/**
 * Tree state holds information about a tree such as which nodes are expanded /
 * collapsed and which nodes are selected, It can also fire callbacks on
 * listener in case any of the information changed.
 * 
 * @author Matej Knopp
 */
public interface ITreeState
{
	/**
	 * Adds a tree state listener. On state change events on the listener are
	 * fired.
	 * 
	 * @param l
	 *            Listener to add
	 */
	void addTreeStateListener(ITreeStateListener l);

	/**
	 * Collapses all nodes of the tree.
	 */
	void collapseAll();

	/**
	 * Collapses the given node.
	 * 
	 * @param node
	 *            Node to collapse
	 */
	void collapseNode(TreeNode node);

	/**
	 * Expands all nodes of the tree.
	 */
	void expandAll();

	/**
	 * Expands the given node.
	 * 
	 * @param node
	 *            Node to expand
	 */
	void expandNode(TreeNode node);

	/**
	 * Returns the collection of all selected nodes.
	 * 
	 * @return The collection of selected nodes
	 */
	Collection<TreeNode> getSelectedNodes();

	/**
	 * Returns whether multiple nodes can be selected.
	 * 
	 * @return True if mutliple nodes can be selected
	 */
	boolean isAllowSelectMultiple();

	/**
	 * Returns true if the given node is expanded.
	 * 
	 * @param node
	 *            The node to inspect
	 * @return True if the node is expanded
	 */
	boolean isNodeExpanded(TreeNode node);

	/**
	 * Returns true if the given node is selected, false otherwise.
	 * 
	 * @param node
	 *            The node to inspect
	 * @return True if the node is selected
	 */
	boolean isNodeSelected(TreeNode node);

	/**
	 * Removes a tree state listener.
	 * 
	 * @param l
	 *            The listener to remove
	 */
	void removeTreeStateListener(ITreeStateListener l);


	/**
	 * Marks given node as selected (or unselected) according to the selected
	 * value.
	 * <p>
	 * If tree is in single selection mode and a new node is selected, old node
	 * is automatically unselected (and the event is fired on listeners).
	 * 
	 * @param node
	 *            The node to select or deselect
	 * @param selected
	 *            If true, the node will be selected, otherwise, the node will
	 *            be unselected
	 */
	void selectNode(TreeNode node, boolean selected);

	/**
	 * Sets whether multiple nodes can be selected.
	 * 
	 * @param value
	 *            If true, multiple nodes can be selected. If false, only one
	 *            node at a time can be selected
	 */
	void setAllowSelectMultiple(boolean value);
}
