package wicket.examples.filebrowser;

import java.io.File;

import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RequestCycle;
import wicket.markup.html.tree.IndentTree;
import wicket.markup.html.tree.TreeNodeModel;
import wicket.markup.html.tree.TreeStateCache;

/** indent tree implementation. */
public class CustomIndentTree extends IndentTree
{
    /** Log. */
    private static Log log = LogFactory.getLog(CustomIndentTree.class);

	/**
	 * Construct.
	 * @param componentName The name of this container
	 * @param model the tree model
	 * @param makeTreeModelUnique whether to make the userObject nodes unique
	 */
	public CustomIndentTree(String componentName, TreeModel model, boolean makeTreeModelUnique)
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
    public CustomIndentTree(final String componentName, TreeStateCache treeState)
    {
        super(componentName, treeState);
    }

	/**
	 * @see wicket.markup.html.tree.IndentTree#junctionLinkClicked(wicket.RequestCycle, wicket.markup.html.tree.TreeNodeModel)
	 */
	protected void junctionLinkClicked(RequestCycle cycle, TreeNodeModel node)
	{
		super.junctionLinkClicked(cycle, node);
		log.info("tree junction link was clicked, user object: " + node.getUserObject());
	}

	/**
	 * @see wicket.markup.html.tree.IndentTree#nodeLinkClicked(wicket.RequestCycle, wicket.markup.html.tree.TreeNodeModel)
	 */
	protected void nodeLinkClicked(RequestCycle cycle, TreeNodeModel node)
	{
		super.nodeLinkClicked(cycle, node);
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
                img = "filebrowser/minus.gif";
            }
            else
            {
                img = "filebrowser/plus.gif";
            }
        }
        else
        {
        	img = "filebrowser/blank.gif";
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
            img = "filebrowser/node.gif";
        }
        else
        {
            if (node.isExpanded())
            {
                img = "filebrowser/folderopen.gif";
            }
            else
            {
                img = "filebrowser/folder.gif";
            }
        }

        return img;
    }

	/**
	 * @see wicket.markup.html.tree.FlatTree#getNodeLabel(wicket.markup.html.tree.TreeNodeModel)
	 */
	protected String getNodeLabel(TreeNodeModel node)
	{
		File file = (File)node.getUserObject();
		return file.getName();
	}
}