package wicket.markup.html.tree;

import javax.swing.tree.TreeNode;

/**
 * Methods this interface are called when tree state is changing.
 * 
 * @author Matej Knopp
 */
public interface ITreeStateListener
{
	/**
	 * Fired when all nodes are collapsed.
	 */
	void allNodesCollapsed();

	/**
	 * Fired when all nodes are expanded.
	 */
	void allNodesExpanded();

	/**
	 * Fired when given node is collapsed.
	 * 
	 * @param node
	 *            The node that was collapsed
	 */
	void nodeCollapsed(TreeNode node);

	/**
	 * Fired when given node is expanded.
	 * 
	 * @param node
	 */
	void nodeExpanded(TreeNode node);

	/**
	 * Fired when given node gets selected.
	 * 
	 * @param node
	 *            The node that was selected
	 */
	void nodeSelected(TreeNode node);

	/**
	 * Fired when given node gets unselected.
	 * 
	 * @param node
	 *            The node that was unselected
	 */
	void nodeUnselected(TreeNode node);
}
