/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.tree;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import wicket.RequestCycle;
import wicket.markup.html.link.ILinkListener;
import wicket.markup.html.panel.Panel;
import wicket.protocol.http.HttpRequest;

/**
 * Base component for trees. The trees from this package work with the Swing tree models
 * and {@link javax.swing.tree.DefaultMutableTreeNode}s. Hence, users can re-use their
 * Swing trees.
 *
 * An important requirement of the trees in this package, is that the user objects of the
 * {@link javax.swing.tree.DefaultMutableTreeNode}s must be unique (have a unique hashcode)
 * within the tree. Users can either ensure this themselves, or use the makeUnique
 * method or constructor parameter for this.
 * </p><p>
 * We need unique user objects to be able to decouple the links in the trees from
 * actual components that are used for rendering the visible tree paths.
 * As every expand, collapse and select action has the effect of a structural change
 * of the components that are used for rendering, the tree would be marked stale on every
 * request. That would mean that we could never allow for using the back button. By
 * decoupling the tree links, we support trees that keep working when end-users fool
 * around with their back buttons.
 * <p></p>
 * Users can either extends this class directly, or use one of the provided subclasses, like
 * {@link wicket.markup.html.tree.ListTree} (uses nested lists to render and is best used
 * with UL/ LI elements) or {@link wicket.markup.html.tree.IndentTree} (uses spacers and
 * CSS for indentation). Currently {@link wicket.markup.html.tree.IndentTree} is more usuable,
 * is it has the distinction between junction links (for folding/ unfolding of tree paths)
 * and node links (for the actual selection of a link).
 * </p>
 *
 * @author Eelco Hillenius
 */
public abstract class Tree extends Panel implements ILinkListener
{ // TODO finalize javadoc
    /** tree state for this component. */
    private TreeStateCache treeState;

    /** hold references to links on 'generated' id to be able to chain events. */
    private Map links = new HashMap();

    /**
     * Construct using the given model as the tree model to use. A new tree state will
     * be constructed by calling newTreeState.
     * @param componentName The name of this container
     * @param model the underlying tree model
     */
    public Tree(final String componentName, final TreeModel model)
    {
        this(componentName, model, false);
    }

    /**
     * Construct using the given model as the tree model to use. A new tree state will
     * be constructed by calling newTreeState. If parameter makeTreeModelUnique is true,
     * all tree nodes will also be wrapped in an instance of {@link IdWrappedUserObject}
     * in order to attach a unique id to them. If makeTreeModelUnique is false, the model
     * will be used as is, and the user is responsible for the userObjects of the tree
     * nodes being unique within the tree model.
     * @param componentName The name of this container
     * @param model the underlying tree model
     * @param makeTreeModelUnique whether to make the user objects of the tree model
     * unique. If true, the default implementation will wrapp all user objects in
     * instances of {@link IdWrappedUserObject}. If false, users must ensure that the
     * user objects are unique within the tree in order to have the tree working properly
     */
    public Tree(final String componentName, final TreeModel model,
    		final boolean makeTreeModelUnique)
    {
        super(componentName);
        this.treeState = newTreeState(model);
        applySelectedPaths(treeState);
        if(makeTreeModelUnique)
        {
        	makeUnique(model);
        }
    }

    /**
     * Constructor using the given tree state. This tree state holds the tree model and
     * the currently visible paths.
     * @param componentName The name of this container
     * @param treeState the tree state that holds the tree model and the currently visible
     * paths
     */
    public Tree(final String componentName, TreeStateCache treeState)
    {
        super(componentName);
        this.treeState = treeState;
        applySelectedPaths(treeState);
    }

    /**
     * Loops through all tree nodes and make the user object of each tree node unique.
     * By default, this is done by wrapping the found user objects in instances
     * of {@link IdWrappedUserObject}.
     * @param treeModel the tree model
     */
    public void makeUnique(TreeModel treeModel)
    {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
        makeUnique(rootNode);
        DefaultMutableTreeNode node;
        Enumeration e = rootNode.preorderEnumeration();
        while (e.hasMoreElements())
        {
            node = (DefaultMutableTreeNode)e.nextElement();
            makeUnique(node);
        }
    }

    /**
     * Makes the user object of the given tree node unique. This implementation
     * wraps the user object in a {@link IdWrappedUserObject}.
     * @param node the tree node with the user object
     */
    protected void makeUnique(DefaultMutableTreeNode node)
    {
    	Object userObject = node.getUserObject();
    	if(!(userObject instanceof Serializable))
    	{
    		throw new IllegalArgumentException(
    				"user objects of tree nodes must be serializable");
    	}
    	else if(userObject instanceof IdWrappedUserObject)
    	{
    		// nothing needs to be done
    	}
    	else
    	{
			IdWrappedUserObject wrapped = new IdWrappedUserObject((Serializable)userObject);
	    	node.setUserObject(wrapped);
    	}
    }

    /**
     * Registers a link with this tree.
     * @param link the link to register
     */
    final void addLink(AbstractTreeNodeLink link)
    {
        links.put(link.getId(), link);
    }

    /**
     * Called when a link is clicked.
     * @param cycle The current request cycle
     * @see ILinkListener
     */
    public final void linkClicked(final RequestCycle cycle)
    {
    	String param = AbstractTreeNodeLink.REQUEST_PARAMETER_LINK_ID;
        String linkId = ((HttpRequest)cycle.getRequest()).getParameter(param);
        AbstractTreeNodeLink link = (AbstractTreeNodeLink) links.get(linkId);
        if (link == null)
        {
            throw new IllegalStateException("link " + linkId + " not found");
        }
        link.linkClicked(cycle);
    }

	/**
	 * Sets the new expanded state (to true), based on the given user object
	 * and set the tree path to the currently selected.
	 * @param userObject the user object
	 */
	public final void setSelected(Object userObject)
	{
		TreeStateCache state = getTreeState();
		TreePath selection = state.findTreePath(userObject);
		state.setSelectedPath(selection);
		setExpandedState(selection, true);
	}

	/**
	 * Sets the new expanded state (to true), based on the given user node
	 * and set the tree path to the currently selected.
	 * @param node the tree node model
	 */
	public final void setSelected(TreeNodeModel node)
	{
		TreeStateCache state = getTreeState();
		Object userObject = node.getTreeNode().getUserObject();
		TreePath selection = state.findTreePath(userObject);
		state.setSelectedPath(selection);
		setExpandedState(selection, true);
	}

	/**
	 * Sets the new expanded state, based on the given node
	 * @param node the tree node model
	 */
	public final void setExpandedState(TreeNodeModel node)
	{
		TreeStateCache state = getTreeState();
		Object userObject = node.getTreeNode().getUserObject();
		TreePath selection = state.findTreePath(userObject);
		setExpandedState(selection, (!node.isExpanded())); // inverse
	}

    /**
     * Sets the expanded property in the stree state for selection.
     * @param selection the selection to set the expanded property for
     * @param expanded true if the selection is expanded, false otherwise
     */
    public final void setExpandedState(TreePath selection, boolean expanded)
    {
        treeState.setExpandedState(selection, expanded);
        applySelectedPaths(treeState);
    }

    /**
     * Finds the tree path for the given user object.
     * @param userObject the user object to find the path for
     * @return tree path of given user object
     */
    public final TreePath findTreePath(Serializable userObject)
    {
        return treeState.findTreePath(userObject);
    }

    /**
     * Gets the current tree state.
     * @return the tree current tree state
     */
    public final TreeStateCache getTreeState()
    {
        return treeState;
    }

    /**
     * Sets the current tree state to the given tree state.
     * @param treeState the tree state to set as the current one
     */
    public final void setTreeState(final TreeStateCache treeState)
    {
    	this.treeState = treeState;
    }

	/**
	 * Creates a new tree state by creating a new {@link TreeStateCache} object, which
	 * is then set as the current tree state, creating a new {@link TreeSelectionModel}
	 * and then calling setTreeModel with this
	 * @param model the model that the new tree state applies to
	 * @return the tree state
	 */
	public TreeStateCache newTreeState(final TreeModel model)
	{
		return newTreeState(model, true);
	}


	/**
	 * Creates a new tree state by creating a new {@link TreeStateCache} object, which
	 * is then set as the current tree state, creating a new {@link TreeSelectionModel}
	 * and then calling setTreeModel with this
	 * @param model the model that the new tree state applies to
	 * @param rootVisible whether the tree node should be displayed
	 * @return the tree state
	 */
	protected final TreeStateCache newTreeState(final TreeModel model, boolean rootVisible)
	{
		TreeStateCache treeStateCache = new TreeStateCache();
		TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
        treeStateCache.setModel(model);
        treeStateCache.setSelectionModel(selectionModel);
        treeStateCache.setRootVisible(rootVisible);
        model.addTreeModelListener(treeStateCache);
        return treeStateCache;
	}

    /**
     * Finds the tree node that holds the given user object.
     * @param userObject the user object to find the node for
     * @return the node that holds the user object
     */
    public final DefaultMutableTreeNode findNode(Serializable userObject)
    {
        return treeState.findNode(userObject);
    }

    /**
     * Applies the current selection. Implementations need to prepare the model
     * of the concrete tree for rendering.
     * @param treeState the current tree state
     */
    protected abstract void applySelectedPaths(TreeStateCache treeState);

    /**
     * Refreshes the tree.
     */
    public void refresh()
    {
    	applySelectedPaths(getTreeState());
    }

    /**
     * Sets whether the tree node should be displayed.
     * @param rootVisible whether the tree node should be displayed
     */
    public final void setRootVisible(boolean rootVisible)
    {
    	treeState.setRootVisible(rootVisible);
    }

    /**
     * Gives the current tree model as a string.
     * @return the current tree model as a string
     */
    public final String getTreeModelAsDebugString()
    {
    	StringBuffer b = new StringBuffer("-- TREE MODEL --\n");
    	TreeStateCache state = getTreeState();
    	TreeModel treeModel = null;
    	if(state != null)
    	{
    		treeModel = state.getModel();
    	}
    	if(treeModel != null)
    	{
	    	StringBuffer tabs = new StringBuffer();
	        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
	        Enumeration e = rootNode.preorderEnumeration();
	        while (e.hasMoreElements())
	        {
	            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
	            tabs.delete(0, tabs.length());
	            tabs.append("|");
	            for (int i = 0; i < node.getLevel(); i++)
	            {
	                tabs.append("-");
	            }
	            b.append(tabs).append(node).append("\n");
	        }
    	}
    	else
    	{
    		b.append("<EMPTY>");
    	}
        return b.toString();
    }
}
