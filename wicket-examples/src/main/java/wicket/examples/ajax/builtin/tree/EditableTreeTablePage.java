package wicket.examples.ajax.builtin.tree;

import wicket.markup.html.form.Form;
import wicket.markup.html.tree.AbstractTree;
import wicket.markup.html.tree.table.ColumnLocation;
import wicket.markup.html.tree.table.IColumn;
import wicket.markup.html.tree.table.PropertyTreeColumn;
import wicket.markup.html.tree.table.TreeTable;
import wicket.markup.html.tree.table.ColumnLocation.Alignment;
import wicket.markup.html.tree.table.ColumnLocation.Unit;

/**
 * Page that shows a tree table with editable cells.
 * 
 * @author Matej Knopp
 */
public class EditableTreeTablePage extends BaseTreePage
{
	private TreeTable tree;

	/**
	 * Page constructor.
	 */
	public EditableTreeTablePage()	
	{
		IColumn columns[] = new IColumn[] {
			new PropertyTreeColumn(new ColumnLocation(Alignment.LEFT, 18, Unit.EM), "Tree Column", "userObject.property1"),
			new PropertyEditableColumn(new ColumnLocation(Alignment.LEFT, 12, Unit.EM), "L2", "userObject.property2"),
			new PropertyEditableColumn(new ColumnLocation(Alignment.MIDDLE, 2, Unit.PROPORTIONAL), "M1", "userObject.property3"),
			new PropertyEditableColumn(new ColumnLocation(Alignment.MIDDLE, 2, Unit.PROPORTIONAL), "M2", "userObject.property4"),
			new PropertyEditableColumn(new ColumnLocation(Alignment.MIDDLE, 3, Unit.PROPORTIONAL), "M3", "userObject.property5"),
			new PropertyEditableColumn(new ColumnLocation(Alignment.RIGHT, 8, Unit.EM), "R1", "userObject.property6"),
			
		};
		
		Form form = new Form(this, "form");
		
		tree = new TreeTable(form, "treeTable", createTreeModel(), columns);
		tree.getTreeState().collapseAll();
	}
	
	/**
	 * @see BaseTreePage#getTree()
	 */
	@Override
	protected AbstractTree getTree()
	{
		return tree;
	}

}
