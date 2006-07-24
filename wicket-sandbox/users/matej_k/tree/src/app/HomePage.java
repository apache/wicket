package app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.markup.html.WebPage;
import wicket.xtree.DefaultAbstractTree;
import wicket.xtree.table.ColumnLocation;
import wicket.xtree.table.IColumn;
import wicket.xtree.table.PropertyTreeColumn;
import wicket.xtree.table.StringColumn;
import wicket.xtree.table.TreeTable;
import wicket.xtree.table.ColumnLocation.Alignment;
import wicket.xtree.table.ColumnLocation.Unit;

public class HomePage extends WebPage {

	public HomePage() 
	{		
		// this link is here as temporary hack to make wicket-ajax.js first javascript in head
		new AjaxLink(this, "link") 
		{
			@Override
			public void onClick(AjaxRequestTarget target) 
			{
			}
		};

		final DefaultTreeModel treeModel = (DefaultTreeModel) createTreeModel();

		
		IColumn columns[] = new IColumn[] {
//			new StringColumn(new ColumnLocation(Alignment.LEFT, 20, Unit.PX), "L0", "Very first left column. Has solid width set to 20 pixels"),				
			new PropertyTreeColumn(new ColumnLocation(Alignment.LEFT, 15, Unit.EM), "Tree Column", "userObject"),
//			new StringColumn(new ColumnLocation(Alignment.LEFT, 10, Unit.PERCENT), "L 2", "Second left column. This column has percentage width"),
//			new StringColumn(new ColumnLocation(Alignment.LEFT, 3, Unit.EM), "L 3", "Third left column. This column has width set in em."),
//			
			new StringColumn(new ColumnLocation(Alignment.MIDDLE, 3, Unit.PROPORTIONAL), "M1", "First middle column. Has weight 3.") {
				@Override
				public int getSpan(TreeNode node) {
					if (node != null)
					{
						if (node.isLeaf())
							return 0;
						else if (node.getParent() != null && node.getParent().getParent() == null)
							return 2;
						else
							return 3;
					}
					return 1;
				}
			},
			new StringColumn(new ColumnLocation(Alignment.MIDDLE, 2, Unit.PROPORTIONAL), "M2", "Second middle column. Has weight 2."),
			new StringColumn(new ColumnLocation(Alignment.MIDDLE, 2, Unit.PROPORTIONAL), "M3", "Third middle column. Has weight 2."),
//			
			new StringColumn(new ColumnLocation(Alignment.RIGHT, 8, Unit.EM), "R1", "First right column. Width set to 8 em."),
//			new StringColumn(new ColumnLocation(Alignment.RIGHT, 4, Unit.EM), "R2", "Second right column. Width set to 2 em."),
			
		};
		
		DefaultAbstractTree tree = new TreeTable(this, "tree", treeModel, columns);
			//new SimpleTree(this, "tree", treeModel);
		
		tree.getTreeState().setAllowSelectMultiple(true);
//		//tree.getTreeState().collapseAll();
//		//tree.getTreeState().expandNode((TreeNode)treeModel.getRoot());		
//		ee.setLinkType(DefaultAbstractTree.LinkType.AJAX);
		
//		Tree tree = new Tree(this, "tree", treeModel);
//		tree.expandAll(true);
		
		setVersioned(false);
	}
	
	private TreeModel createTreeModel() 
	{
		List<Object> l1 = new ArrayList<Object>();
		l1.add("test 1.1");
		l1.add("test 1.2");
		l1.add("test 1.3");
		List<Object> l2 = new ArrayList<Object>();
		l2.add("test 2.1");
		l2.add("test 2.2");
		l2.add("test 2.3");
		List<String> l3 = new ArrayList<String>();
		l3.add("test 3.1");
		l3.add("test 3.2");
		l3.add("test 3.3");
		
//		for (int i = 0; i <250; ++i) {
//			l3.add("Test 3.x" + i);
//		}
		
		l2.add(l3);
		
		l2.add("test 2.4");
		l2.add("test 2.5");
		l2.add("test 2.6");
		
		l3 = new ArrayList<String>();
		l3.add("test 3.1");
		l3.add("test 3.2");
		l3.add("test 3.3");
		l2.add(l3);
		
		l1.add(l2);

		l2 = new ArrayList<Object>();
		l2.add("test 2.1");
		l2.add("test 2.2");
		l2.add("test 2.3");

		l1.add(l2);		
		
		l1.add("test 1.3");
		l1.add("test 1.4");
		l1.add("test 1.5");
		
		return convertToTreeModel(l1);
	}
	
	private TreeModel convertToTreeModel(List list)
	{
		TreeModel model = null;
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("ROOT");
		add(rootNode, list);
		model = new DefaultTreeModel(rootNode);		
		return model;
	}

	private void add(DefaultMutableTreeNode parent, List sub)
	{
		for (Iterator i = sub.iterator(); i.hasNext();)
		{
			Object o = i.next();
			if (o instanceof List)
			{
				DefaultMutableTreeNode child = new DefaultMutableTreeNode("subtree...");
				parent.add(child);
				add(child, (List)o);
			}
			else
			{
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(o);
				parent.add(child);
			}
		}
	}
}
