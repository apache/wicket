package wicket.markup.html.tree.table;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.tree.table.ColumnLocation.Alignment;

/**
 * Convenience class for building tree columns, i.e. columns that contain the
 * actual tree.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractTreeColumn extends AbstractColumn
{
	/**
	 * Creates new column. Checks if the column is not aligned in middle. In
	 * case it is, throws an exception.
	 * 
	 * @param location
	 *            Specifies how the column should be aligned and what his size
	 *            should be
	 * 
	 * @param header
	 *            Header caption
	 * 
	 */
	public AbstractTreeColumn(ColumnLocation location, String header)
	{
		super(location, header);

		if (location.getAlignment() == Alignment.MIDDLE)
		{
			throw new IllegalArgumentException("Tree column may not be alligned in the middle.");
		}
	}

	/**
	 * @see IColumn#newCell(MarkupContainer, String, TreeNode, int)
	 */
	public Component newCell(MarkupContainer<?> parent, String id, TreeNode node, int level)
	{
		return TreeTable.newTreeCell(parent, id, node, level, new TreeTable.IRenderNodeCallback()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public String renderNode(TreeNode node)
			{
				return AbstractTreeColumn.this.renderNode(node);
			}
		});
	}

	/**
	 * @see IColumn#newCell(TreeNode, int)
	 */
	public IRenderable newCell(TreeNode node, int level)
	{
		return null;
	}

	/**
	 * Returns the string representation of the node.
	 * 
	 * @param node
	 *            The node
	 * @return The string representation of the node
	 */
	public abstract String renderNode(TreeNode node);
}
