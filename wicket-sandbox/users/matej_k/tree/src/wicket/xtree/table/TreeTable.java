package wicket.xtree.table;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.behavior.AbstractBehavior;
import wicket.markup.ComponentTag;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Fragment;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.xtree.DefaultAbstractTree;
import wicket.xtree.SimpleTree;
import wicket.xtree.table.ColumnLocation.Alignment;

public class TreeTable extends SimpleTree {

	public TreeTable(MarkupContainer parent, String id, TreeModel model, IColumn columns[]) 
	{
		super(parent, id, model);
		init(columns);
	}	

	public TreeTable(MarkupContainer parent, String id, IModel<TreeModel> model, IColumn columns[]) 
	{
		super(parent, id, model);
		init(columns);
	}

	public TreeTable(MarkupContainer parent, String id, IColumn columns[]) 
	{
		super(parent, id);
		init(columns);
	}
	
	private void init(IColumn columns[])
	{
		this.columns = columns;
		addHeader();
	}

	private IColumn columns[];
	
		
	protected void addHeader() 
	{
		int i = 0;
		
		SideColumnsView sideColumns = new SideColumnsView(this, "sideColumns");
		for (IColumn column: columns)
		{
			if (column.getLocation().getAlignment() == Alignment.LEFT ||
				column.getLocation().getAlignment() == Alignment.RIGHT)
			{
				Component component = column.createHeader(sideColumns, "" + i++);
				sideColumns.addColumn(column, component, null);
			}
		}
		
		i = 0;
		
		MiddleColumnsView middleColumns = new MiddleColumnsView(this, "middleColumns", null);
		for (IColumn column: columns)
		{
			if (column.getLocation().getAlignment() == Alignment.MIDDLE)
			{
				Component component = column.createHeader(middleColumns, "" + i++);
				middleColumns.addColumn(column, component, null);
			}
		}		
	}
	
	
	@Override
	protected void populateTreeItem(WebMarkupContainer<TreeNode> item, int level) 
	{	
		final TreeNode node = item.getModelObject();

		int i = 0;
		
		SideColumnsView sideColumns = new SideColumnsView(item, "sideColumns");
		for (IColumn column: columns)
		{
			if (column.getLocation().getAlignment() == Alignment.LEFT ||
				column.getLocation().getAlignment() == Alignment.RIGHT)
			{
				Component component;
				IRenderable renderable = column.createCell(this, node, level);			
				
				if (renderable == null)
				{
					component = column.createCell(sideColumns, "" + i++, node, level);
				}
				else
				{
					component = null;
				}
				
				sideColumns.addColumn(column, component, renderable);
			}
		}
		
		i = 0;
		
		MiddleColumnsView middleColumns = new MiddleColumnsView(item, "middleColumns", node);
		for (IColumn column: columns)
		{
			if (column.getLocation().getAlignment() == Alignment.MIDDLE)
			{
				Component component;
				IRenderable renderable = column.createCell(this, node, level);			
				
				if (renderable == null)
				{
					component = column.createCell(middleColumns, "" + i++, node, level);
				}
				else
				{
					component = null;
				}
				
				middleColumns.addColumn(column, component, renderable);
			}
		}			
		
		// do distinguish between selected and unselected rows we add an behavior
		// that modifies row css class.
		item.add(new AbstractBehavior() {
			@Override
			public void onComponentTag(Component component, ComponentTag tag) {
				super.onComponentTag(component, tag);
				if (getTreeState().isNodeSelected(node))
					tag.put("class", "row-selected");
				else
					tag.put("class", "row");
			}
		});		
	}
	
	class TreePanel extends Fragment 
	{	
		public TreePanel(MarkupContainer<?> parent, String id, final TreeNode node, int level) {
			super(parent, id, "fragment");
	
			createIndentation(this, "indent", node, level);
			
			createJunctionLink(this, "link", "image", node);
			
			WebMarkupContainer nodeLink = createNodeLink(this, "nodeLink", node);
			
			createNodeIcon(nodeLink, "icon", node);
			
			new Label(nodeLink, "label", new Model<String>() {
				@Override
				public String getObject() {				
					return renderNode(node);
				}
			});
		}		
	};
	
	protected TreePanel createTreePanel(MarkupContainer<?> parent, String id, final TreeNode node, int level)
	{
		return new TreePanel(parent, id, node, level);
	}
	
	protected static abstract class TreeColumn implements IColumn {
					
		public Component createCell(MarkupContainer<?> parent, String id, TreeNode node, int level) {
			TreeTable table = parent.findParent(TreeTable.class);
			return table.createTreePanel(parent, id, node, level);
		}
		
	};

	/** Reference to the css file. */
	private static final PackageResourceReference CSS = 
		new PackageResourceReference(DefaultAbstractTree.class, "res/tree-table.css");
	
	@Override
	protected PackageResourceReference getCSS() {
		return CSS;
	}
}
