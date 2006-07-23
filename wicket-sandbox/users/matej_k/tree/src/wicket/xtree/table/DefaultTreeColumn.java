package wicket.xtree.table;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.xtree.table.ColumnLocation.Alignment;

public class DefaultTreeColumn extends AbstractTreeColumn {

	public DefaultTreeColumn(ColumnLocation location, String header)
	{
		if (location.getAlignment() == Alignment.MIDDLE)
		{
			throw new IllegalArgumentException("Tree column must not be aligned in the middle.");
		}
		this.location = location;
		this.header = header;
	}
	
	private ColumnLocation location;
	private String header;
	private boolean visible = true;
	
	public Component createHeader(MarkupContainer<?> parent, String id) {
		return new Label(parent, id, header);
	}

	public ColumnLocation getLocation() {
		return location;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public int getSpan(TreeNode node) {
		return 0;
	}
	
	public Renderable createCell(TreeTable treeTable, TreeNode node, int level) {
		return null;
	}
}
