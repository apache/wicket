package wicket.examples.nested;

import java.util.List;

import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.tree.IndentTree;
import wicket.markup.html.tree.TreeNodeModel;
import wicket.markup.html.tree.TreeStateCache;

/** indent tree implementation. */
public class MyTree extends IndentTree
{
    /** Log. */
    private static Log log = LogFactory.getLog(MyTree.class);

	/**
	 * Construct.
	 * @param componentName The name of this container
	 * @param model the tree model
	 * @param makeTreeModelUnique whether to make the userObject nodes unique
	 */
	public MyTree(String componentName, TreeModel model, boolean makeTreeModelUnique)
	{
		super(componentName, model, makeTreeModelUnique);
	}

    /**
     * Constructor using the given tree state. This tree state holds the tree model and
     * the currently visible paths.
     * @param componentName The name of this container
     * @param treeState the tree state that holds the tree model and the currently visible
     * paths
     */
    public MyTree(final String componentName, TreeStateCache treeState)
    {
        super(componentName, treeState);
    }

	/**
	 * @see wicket.markup.html.tree.IndentTree#junctionLinkClicked(wicket.markup.html.tree.TreeNodeModel)
	 */
	protected void junctionLinkClicked(TreeNodeModel node)
	{
		super.junctionLinkClicked(node);
		log.info("tree junction link was clicked, user object: " + node.getUserObject());
	}

	/**
	 * @see wicket.markup.html.tree.IndentTree#nodeLinkClicked(wicket.markup.html.tree.TreeNodeModel)
	 */
	protected void nodeLinkClicked(TreeNodeModel node)
	{
		super.nodeLinkClicked(node);
		log.info("tree node link was clicked, user object: " + node.getUserObject());
	}

	/**
     * Get image name for junction.
     * @param node the model with the current node
     * @return image name
     */
    protected String getJunctionImageName(TreeNodeModel node)
    {
        final String img;

        if(!node.isLeaf())
        {
            if (node.isExpanded())
            {
                img = "nested/minus.gif";
            }
            else
            {
                img = "nested/plus.gif";
            }
        }
        else
        {
        	img = "nested/blank.gif";
        }

        return img;
    }

    /**
     * Get image name for node.
     * @param node the model with the current node
     * @return image name
     */
    protected String getNodeImageName(TreeNodeModel node)
    {
        final String img;

        if (node.isLeaf())
        {
            img = "nested/node.gif";
        }
        else
        {
            if (node.isExpanded())
            {
                img = "nested/folderopen.gif";
            }
            else
            {
                img = "nested/folder.gif";
            }
        }

        return img;
    }

	/**
	 * @see wicket.markup.html.tree.IndentTree#getNodeLabel(wicket.markup.html.tree.TreeNodeModel)
	 */
	protected String getNodeLabel(TreeNodeModel node)
	{
		Object userObject = node.getUserObject();
		if(userObject instanceof List)
		{
			return "<sub>";
		}
		else
		{
			return String.valueOf(node.getUserObject());
		}
	}
}