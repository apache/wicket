package app;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.tree.table.ColumnLocation;
import wicket.markup.html.tree.table.IRenderable;
import wicket.markup.html.tree.table.PropertyRenderableColumn;
import wicket.model.PropertyModel;

public class PropertyEditableColumn extends PropertyRenderableColumn 
{
	public PropertyEditableColumn(ColumnLocation location, String header, String propertyExpression) 
	{
		super(location, header, propertyExpression);
	}
	
	@Override
	public Component createCell(MarkupContainer<?> parent, String id, TreeNode node, int level) 
	{
		return new EditablePanel(parent, id, new PropertyModel<String>(node, getPropertyExpression()));
	}
	
	@Override
	public IRenderable createCell(TreeNode node, int level) 
	{
		if (getTreeTable().getTreeState().isNodeSelected(node))
			return null;
		else
			return super.createCell(node, level);
	}
}
