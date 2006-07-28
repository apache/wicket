package wicket.examples.ajax.builtin.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import wicket.markup.html.tree.AbstractTree;
import wicket.markup.html.tree.Tree;


/**
 * Page that shows a simple tree (not a table).
 * 
 * @author Matej
 *
 */
public class SimpleTreePage extends BaseTreePage
{
	private Tree tree;

	protected AbstractTree getTree()
	{
		return tree;
	}
	
	/**
	 * Page constructor
	 *
	 */
	public SimpleTreePage()
	{
		tree = new Tree(this, "tree", createTreeModel()) 
		{
			protected String renderNode(TreeNode node)
			{
				ModelBean bean = (ModelBean) ((DefaultMutableTreeNode)node).getUserObject();
				return bean.getProperty1();
			}
		};
	}

}
