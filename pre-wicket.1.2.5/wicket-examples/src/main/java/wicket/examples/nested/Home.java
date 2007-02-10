/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.nested;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.examples.ajax.builtin.tree.SimpleTreePage;
import wicket.extensions.markup.html.tree.Tree;
import wicket.extensions.markup.html.tree.DefaultAbstractTree.LinkType;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.Link;

/**
 * Examples that shows how you can display a tree like structure (in this case
 * nested lists with string elements) using nested panels and using a tree
 * component.
 * 
 * @author Eelco Hillenius
 */
public class Home extends WicketExamplePage
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public Home(final PageParameters parameters)
	{
		// create a list with sublists
		List l1 = new ArrayList();
		l1.add("test 1.1");
		l1.add("test 1.2");
		List l2 = new ArrayList();
		l2.add("test 2.1");
		l2.add("test 2.2");
		l2.add("test 2.3");
		List l3 = new ArrayList();
		l3.add("test 3.1");
		l2.add(l3);
		l2.add("test 2.4");
		l1.add(l2);
		l1.add("test 1.3");

		// construct the panel
		add(new RecursivePanel("panels", l1));

		// create a tree
		TreeModel treeModel = convertToTreeModel(l1);
		final Tree tree = new Tree("tree", treeModel)
		{
			protected String renderNode(TreeNode node)
			{
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;				 
				Object userObject = treeNode.getUserObject();
				return (userObject instanceof List) ? "<subtree>" : String
						.valueOf(treeNode.getUserObject());
			}
		};
		// disable ajax links in this example
		tree.setLinkType(LinkType.REGULAR);
		
		add(tree);
		add(new Link("expandAll")
		{
			public void onClick()
			{
				tree.getTreeState().expandAll();
			}
		});

		add(new Link("collapseAll")
		{
			public void onClick()
			{
				tree.getTreeState().collapseAll();
			}
		});
		
		add(new BookmarkablePageLink("ajaxTreeLink", SimpleTreePage.class));
	}

	/**
	 * Add a sublist to the parent.
	 * 
	 * @param parent
	 *            the parent
	 * @param sub
	 *            the sub list
	 */
	private void add(DefaultMutableTreeNode parent, List sub)
	{
		for (Iterator i = sub.iterator(); i.hasNext();)
		{
			Object o = i.next();
			if (o instanceof List)
			{
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(o);
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

	/**
	 * Convert the nested lists to a tree model
	 * 
	 * @param list
	 *            the list
	 * @return tree model
	 */
	private TreeModel convertToTreeModel(List list)
	{
		TreeModel model = null;
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("<root>");
		add(rootNode, list);
		model = new DefaultTreeModel(rootNode);
		return model;
	}
}