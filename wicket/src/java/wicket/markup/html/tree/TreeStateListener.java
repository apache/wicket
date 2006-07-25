package wicket.markup.html.tree;

import javax.swing.tree.TreeNode;

/**
 * Methods this interface are called when tree state is changing.
 * @author Matej Knopp
 */
public interface TreeStateListener 
{
	/**
	 * Fired when given node is expanded.
	 */
	public void nodeExpanded(TreeNode node);

	/**
	 * Fired when given node is collapsed.
	 */
	public void nodeCollapsed(TreeNode node);

	/**
	 * Fired when all nodes are expanded.
	 */
	public void allNodesExpanded();

	/**
	 * Fired when all nodes are collapsed.
	 */
	public void allNodesCollapsed();

	/**
	 * Fired when given node gets selected. 
	 */
	public void nodeSelected(TreeNode node);

	/**
	 * Fired when given node gets unselected.
	 */
	public void nodeUnselected(TreeNode node);	
}
