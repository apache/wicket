package wicket.examples.ajax.builtin.tree;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.tree.table.ColumnLocation;
import wicket.markup.html.tree.table.IColumn;
import wicket.markup.html.tree.table.IRenderable;
import wicket.markup.html.tree.table.PropertyRenderableColumn;
import wicket.model.PropertyModel;

/**
 * Column, that either shows a readonly cell or an editable panel, depending on whether
 * the current row is selected.
 * 
 * @author Matej Knopp
 */
public class PropertyEditableColumn extends PropertyRenderableColumn 
{
	/**
	 * Column constructor.

	 * @param location
	 * @param header
	 * @param propertyExpression
	 */
	public PropertyEditableColumn(ColumnLocation location, String header, String propertyExpression) 
	{
		super(location, header, propertyExpression);
	}
	
	/**
	 * @see IColumn#newCell(MarkupContainer, String, TreeNode, int)
	 */
	public Component newCell(MarkupContainer parent, String id, TreeNode node, int level) 
	{
		return new EditablePanel(parent, id, new PropertyModel<String>(node, getPropertyExpression()));
	}
	
	/**
	 * @see IColumn#newCell(TreeNode, int)
	 */
	public IRenderable newCell(TreeNode node, int level) 
	{
		if (getTreeTable().getTreeState().isNodeSelected(node))
			return null;
		else
			return super.newCell(node, level);
	}
}