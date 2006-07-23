package wicket.xtree.table;

import java.io.Serializable;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;

/**
 * Interface that represents a column in {@link TreeTable}
 * @author Matej Knopp
 *
 */
public interface IColumn extends Serializable {

	public ColumnLocation getLocation();
	
	public boolean isVisible();
	
	public Component createHeader(MarkupContainer<?> parent, String id);	 
	
	public IRenderable createCell(TreeTable treeTable, TreeNode node, int level);
	
	public Component createCell(MarkupContainer<?> parent, String id, TreeNode node, int level);		
	
	public int getSpan(TreeNode node);
}
