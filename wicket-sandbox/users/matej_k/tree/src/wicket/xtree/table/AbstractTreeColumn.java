package wicket.xtree.table;

import javax.swing.tree.TreeNode;

/**
 * Base class for columns containing the tree.
 * @author Matej Knopp
 */
public abstract  class AbstractTreeColumn extends TreeTable.TreeColumn {
	
	
	public final IRenderable createCell(TreeTable treeTable, TreeNode node, int level) {
		return null;
		
	}
	
	abstract String renderNode(TreeNode node);
}
