package wicket.extensions.markup.html.navmenu;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Custom tree path.
 */
public final class MenuTreePath extends TreePath
{
	/**
	 * Construct.
	 * @param path the path
	 */
	public MenuTreePath(Object[] path)
	{
		super(path);
	}

	/**
	 * Checks whether the given node is part of this path.
	 * @param node the node
	 * @return true when the given node is part of the path
	 */
	public boolean isPartOfPath(DefaultMutableTreeNode node)
	{
		int len = getPathCount();
		Object parent = node.getParent();
		// first check whether the node is part of the actual path
		for (int i = 0; i < len; i++)
		{
			Object pathComponent = getPathComponent(i);
			if (pathComponent.equals(node))
			{
				return true;
			}
		}
		
		return false;
	}
}