package wicket.examples.ajax.builtin.tree;

import wicket.markup.html.tree.AbstractTree;
import wicket.markup.html.tree.table.ColumnLocation;
import wicket.markup.html.tree.table.IColumn;
import wicket.markup.html.tree.table.PropertyRenderableColumn;
import wicket.markup.html.tree.table.PropertyTreeColumn;
import wicket.markup.html.tree.table.TreeTable;
import wicket.markup.html.tree.table.ColumnLocation.Alignment;
import wicket.markup.html.tree.table.ColumnLocation.Unit;


/**
 * Page that shows a simple tree table.
 * 
 * @author Matej Knopp
 */
public class TreeTablePage extends BaseTreePage
{
	private TreeTable tree;

	/**
	 * Page constructor.
	 */
	public TreeTablePage()	
	{
		IColumn columns[] = new IColumn[] {
			new PropertyTreeColumn(new ColumnLocation(Alignment.LEFT, 18, Unit.EM), "Tree Column", "userObject.property1"),
			new PropertyRenderableColumn(new ColumnLocation(Alignment.LEFT, 12, Unit.EM), "L2", "userObject.property2"),
			new PropertyRenderableColumn(new ColumnLocation(Alignment.MIDDLE, 2, Unit.PROPORTIONAL), "M1", "userObject.property3"),
			new PropertyRenderableColumn(new ColumnLocation(Alignment.MIDDLE, 2, Unit.PROPORTIONAL), "M2", "userObject.property4"),
			new PropertyRenderableColumn(new ColumnLocation(Alignment.MIDDLE, 3, Unit.PROPORTIONAL), "M3", "userObject.property5"),
			new PropertyRenderableColumn(new ColumnLocation(Alignment.RIGHT, 8, Unit.EM), "R1", "userObject.property6"),			
		};
		
		tree = new TreeTable(this, "treeTable", createTreeModel(), columns);
		tree.getTreeState().setAllowSelectMultiple(true);		
	}
	
	protected AbstractTree getTree()
	{
		return tree;
	}

}
