package wicket.xtree.table;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;

/**
 * Convenience class for building tree columns.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractColumn implements IColumn 
{
	private ColumnLocation location;
	private boolean visible = true;
	private String header;
	private TreeTable treeTable = null;
			
	/**
	 * Creates the tree column.
	 * 
	 * @param location
	 *			Specifies how the column should be aligned and what his size should be 			
	 * 
	 * @param header
	 * 			Header caption
	 */
	public AbstractColumn(ColumnLocation location, String header)
	{
		this.location = location;
		this.header = header;		
	}
	
	/**
	 * @see IColumn#setTreeTable(TreeTable)
	 */
	public void setTreeTable(TreeTable treeTable) 
	{
		this.treeTable = treeTable;
	}
	
	/**
	 * Returns the tree table that this columns belongs to. 
	 * If you call this method from constructor it will return null, as the
	 * column is constructed before the tree is.
	 */
	protected TreeTable getTreeTable() 
	{
		return treeTable;
	}
	
	/**
	 * @see IColumn#createHeader(MarkupContainer, String)
	 */
	public Component createHeader(MarkupContainer<?> parent, String id) 
	{
		return new Label(parent, id, header);
	}

	/**
	 * @see IColumn#getLocation()
	 */
	public ColumnLocation getLocation() 
	{
		return location;
	}
	
	/**
	 * Sets the colation for this column. Every time you change location
	 * you must call the <code>invalidateAll</code> method on tree.
	 * 
	 * @param location
	 * 			new location
	 */
	public void setLocation(ColumnLocation location) 
	{
		this.location = location;
	}

	/**
	 * @see IColumn#getSpan(TreeNode)
	 */
	public int getSpan(TreeNode node) 
	{
		return 0;
	}

	/**
	 * @see IColumn#isVisible()
	 */
	public boolean isVisible() 
	{
		return visible;
	}
	
	/**
	 * Sets the visibility of this column. Every time you change visibility
	 * you must call the <code>invalidateAll</code> method on tree.
	 */
	public void setVisible(boolean visible) 
	{
		this.visible = visible;
	}
}
