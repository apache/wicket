package app;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.model.PropertyModel;
import wicket.xtree.table.ColumnLocation;
import wicket.xtree.table.IRenderable;
import wicket.xtree.table.PropertyRenderableColumn;
import wicket.xtree.table.TreeTable;

public class PropertyEditableColumn extends PropertyRenderableColumn 
{
	public PropertyEditableColumn(ColumnLocation location, String header, String propertyExpression) 
	{
		super(location, header, propertyExpression);
	}
	
	@Override
	public Component createCell(MarkupContainer<?> parent, String id, TreeNode node, int level) 
	{
		//return super.createCell(parent, id, node, level);
		return new EditablePanel(parent, id, new PropertyModel<String>(node, getPropertyExpression()));
	}
	
	@Override
	public IRenderable createCell(TreeTable treeTable, TreeNode node, int level) 
	{
		if (treeTable.getTreeState().isNodeSelected(node))
			return null;
		else
			return super.createCell(treeTable, node, level);
	}
}
