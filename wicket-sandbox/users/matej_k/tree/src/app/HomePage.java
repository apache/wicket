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
import wicket.xtree.SimpleTree;

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
		
		SimpleTree tree = new SimpleTree(this, "tree", treeModel, false);
		tree.getTreeState().setAllowSelectMultiple(true);
		tree.getTreeState().collapseAll();
		tree.getTreeState().expandNode((TreeNode)treeModel.getRoot());
		tree.setLinkType(DefaultAbstractTree.LinkType.AJAX);
		
//		Tree tree = new Tree(this, "tree", treeModel);
		
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
