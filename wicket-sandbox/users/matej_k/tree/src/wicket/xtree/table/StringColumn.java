package wicket.xtree.table;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Response;
import wicket.markup.ComponentTag;
import wicket.markup.html.basic.Label;

public class StringColumn implements IColumn {

	private ColumnLocation location;
	private boolean visible = true;
	private String header;
	private String cell;
	
	public StringColumn(ColumnLocation location, String header, String cell)
	{
		this.location = location;
		this.header = header;
		this.cell = cell;
	}
	
	public Component createCell(MarkupContainer<?> parent, String id, TreeNode node, int level) {
		return new Label(parent, id, cell) 
		{
			@Override
			protected void onComponentTag(ComponentTag tag) 
			{
				super.onComponentTag(tag);
				tag.put("title", cell);
			}
		};	
	}

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
	
	public IRenderable createCell(final TreeTable treeTable, final TreeNode node, int level) {		
		return new IRenderable() {
			public void render(TreeNode node, Response response) {
//				if (treeTable.getTreeState().isNodeSelected(node) == false)
					response.write("<span class=\"text\"  title=\"" + cell + "\">" + cell + "</span>\n");
//				else
//					response.write("<select type=\"text\" style=\"width: 100%; margin: 0px;\" value=\"" + cell + "\" size=\"1\"><option>A</option></select>");
			}
		};
	}
}
