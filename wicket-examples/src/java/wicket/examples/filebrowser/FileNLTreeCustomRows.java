package wicket.examples.filebrowser;

import java.util.List;

import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RequestCycle;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.tree.NLTree;
import wicket.markup.html.tree.TreeNodeModel;
import wicket.markup.html.tree.TreeStateCache;

/** Custom tree that provides our own rows panel. */
public class FileNLTreeCustomRows extends NLTree
{
    /** Log. */
    private static Log log = LogFactory.getLog(FileIndentTree.class);

    /**
     * Constructor.
     * @param componentName The name of this container
     * @param model the underlying tree model
     * @param makeTreeModelUnique whether to make the user objects of the tree model
     * unique. If true, the default implementation will wrapp all user objects so that
     * they will have unique id's attached. If false, users must ensure that the
     * user objects are unique within the tree in order to have the tree working properly
     */
    public FileNLTreeCustomRows(final String componentName, final TreeModel model,
    		final boolean makeTreeModelUnique)
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
    public FileNLTreeCustomRows(final String componentName, TreeStateCache treeState)
    {
        super(componentName, treeState);
    }

	/**
	 * Override makes this method public to make it reachable for the custom row panel.
	 * @see wicket.markup.html.tree.NLTree#linkClicked(wicket.RequestCycle, wicket.markup.html.tree.TreeNodeModel)
	 */
	public void linkClicked(RequestCycle cycle, TreeNodeModel node)
	{
		super.linkClicked(cycle, node);
		log.info("tree link was clicked, user object: " + node.getUserObject());
	}

    /**
     * Provides a custom rows panel 
     * @see wicket.markup.html.tree.Tree#getTreeRowsPanel(java.lang.String, java.util.List)
     */
    protected Panel getTreeRowsPanel(String componentName, List nestedList)
    {
        return new FileNLTreeRows(componentName, nestedList, this);
    } 
}