package wicket.xtree.table;

import java.io.Serializable;

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
import wicket.xtree.table.ColumnLocation.Alignment;

/**
 * TreeTable is a component that represents a grid with a tree. It's divided into columns. 
 * One of the columns has to be column derived from {@link AbstractTreeColumn}.
 * 
 * @author Matej Knopp
 */
public class TreeTable extends DefaultAbstractTree 
{
	/**
	 * Creates the TreeTable for the given TreeModel and array of columns.
	 */
	public TreeTable(MarkupContainer parent, String id, TreeModel model, IColumn columns[]) 
	{
		super(parent, id, model);
		init(columns);
	}	

	/**
	 * Creates the TreeTable for the given model and array of columns.
	 */
	public TreeTable(MarkupContainer parent, String id, IModel<TreeModel> model, IColumn columns[]) 
	{
		super(parent, id, model);
		init(columns);
	}

	/**
	 * Creates the TreeTable for the given array of columns.
	 */	
	public TreeTable(MarkupContainer parent, String id, IColumn columns[]) 
	{
		super(parent, id);
		init(columns);
	}
	
	/**
	 * Internal initialization. Also checks if at least one of the columns
	 * is derived from AbstractTreeColumn. 
	 */
	private void init(IColumn columns[])
	{		
		boolean found = false;
		for (IColumn column : columns)
		{
			if (column instanceof AbstractTreeColumn)
			{
				found = true; 
				break;
			}
		}
		if (found == false)
		{
			throw new IllegalArgumentException("At least one column in TreeTable must be derived from AbstractTreeColumn.");
		}
	
		this.columns = columns;
		
		addHeader();		
	}

	// columns of the TreeTable
	private IColumn columns[];
	
	/**
	 * Adds the header to the TreeTable.
	 */
	protected void addHeader() 
	{
		int i = 0;
		
		// create the view for side columns
		SideColumnsView sideColumns = new SideColumnsView(this, "sideColumns", null);
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
		
		// create the view for middle columns
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
	
	
	/**
	 * Populates one row of the tree.
	 */
	@Override
	protected void populateTreeItem(WebMarkupContainer<TreeNode> item, int level) 
	{	
		final TreeNode node = item.getModelObject();

		int i = 0;
		
		// add side columns
		SideColumnsView sideColumns = new SideColumnsView(item, "sideColumns", null);
		for (IColumn column: columns)
		{
			if (column.getLocation().getAlignment() == Alignment.LEFT ||
				column.getLocation().getAlignment() == Alignment.RIGHT)
			{
				Component component;
				// first try to create a renderable
				IRenderable renderable = column.createCell(this, node, level);			
				
				if (renderable == null)
				{
					// if renderable failed, try to create a regular component					
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
		
		// add middle columns
		MiddleColumnsView middleColumns = new MiddleColumnsView(item, "middleColumns", node);
		for (IColumn column: columns)
		{
			if (column.getLocation().getAlignment() == Alignment.MIDDLE)
			{
				Component component;
				// first try to create a renderable
				IRenderable renderable = column.createCell(this, node, level);			
				
				if (renderable == null)
				{
					// if renderable failed, try to create a regular component					
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
		item.add(new AbstractBehavior() 
		{
			@Override
			public void onComponentTag(Component component, ComponentTag tag) 
			{
				super.onComponentTag(component, tag);
				if (getTreeState().isNodeSelected(node))
					tag.put("class", "row-selected");
				else
					tag.put("class", "row");
			}
		});		
	}
	
	/**
	 * Represents a content of a cell in TreeColumn (column containing the actual tree).
	 * 
	 * @author Matej Knopp
	 */
	private class TreeFragment extends Fragment 
	{	
		/**
		 * Constructor.
		 */
		public TreeFragment(MarkupContainer<?> parent, String id, final TreeNode node, 
				            int level, final IRenderNodeCallback renderNodeCallback) 
		{
			super(parent, id, "fragment");
	
			createIndentation(this, "indent", node, level);
			
			createJunctionLink(this, "link", "image", node);
			
			WebMarkupContainer nodeLink = createNodeLink(this, "nodeLink", node);
			
			createNodeIcon(nodeLink, "icon", node);
			
			new Label(nodeLink, "label", new Model<String>() 
			{
				@Override
				public String getObject() 
				{				
					return renderNodeCallback.renderNode(node);
				}
			});						
		}		
	};
	
	/**
	 * Callback for rendering three node text.
	 * 
	 * @author Matej Knopp
	 */
	public static interface IRenderNodeCallback extends Serializable
	{
		public String renderNode(TreeNode node);
	}
	
	/**
	 * Creates a new instance of the TreeFragment.
	 */	
	protected TreeFragment createTreePanel(MarkupContainer<?> parent, String id, final TreeNode node, 
			                               int level, IRenderNodeCallback renderNodeCallback)
	{
		return new TreeFragment(parent, id, node, level, renderNodeCallback);
	}
	
	/**
	 * Creates a tree cell for given node. This method is supposed to be used by TreeColumns (columns
	 * that draw the actual tree).
	 *   
	 * @param parent 
	 * 			Parent component
	 * 
	 * @param id
	 * 			Component ID
	 * 
	 * @param node
	 * 			Tree node for the row
	 * 
	 * @param level
	 * 			How deep is the node nested (for convenience) 
	 * 
	 * @param callback 
	 * 			Used to get the display string 
	 */
	public static Component createTreeCell(MarkupContainer<?> parent, String id, TreeNode node, int level, IRenderNodeCallback callback)
	{
		TreeTable table = parent.findParent(TreeTable.class);
		
		return table.createTreePanel(parent, id, node, level, callback);			
	}
	
	/** Reference to the css file. */
	private static final PackageResourceReference CSS = 
		new PackageResourceReference(DefaultAbstractTree.class, "res/tree-table.css");
	
	@Override
	protected PackageResourceReference getCSS() 
	{
		return CSS;
	}
}