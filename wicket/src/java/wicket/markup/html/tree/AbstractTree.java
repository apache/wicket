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
 * 
 * We need unique user objects to be able to decouple the links in the trees from
 * actual components that are used for rendering the visible tree paths.
 * As every expand, collapse and select action has the effect of a structural change
 * of the components that are used for rendering, the tree would be marked stale on every
 * request. That would mean that we could never allow for using the back button. By
 * decoupling the tree links, we support trees that keep working when end-users fool
 * around with their back buttons.
 *
 * @author Eelco Hillenius
 */
public abstract class AbstractTree extends Panel implements ILinkListener
{
    /** tree state for this component. */
    private TreeStateCache treeState;

    /** hold references to links on 'generated' id to be able to chain events. */
    private Map links = new HashMap();

    /**
     * Constructor.
     * @param componentName The name of this container
     * @param model the underlying tree model
     */
    public AbstractTree(final String componentName, final TreeModel model)
    {
        this(componentName, model, false);
    }

    /**
     * Constructor.
     * @param componentName The name of this container
     * @param model the underlying tree model
     * @param makeTreeModelUnique whether to make the user objects of the tree model
     * unique. If true, the default implementation will wrapp all user objects in
     * instances of {@link IdWrappedUserObject}. If false, users must ensure that the
     * user objects are unique within the tree in order to have the tree working properly
     */
    public AbstractTree(final String componentName, final TreeModel model,
    		final boolean makeTreeModelUnique)
    {
        super(componentName);
        treeState = new TreeStateCache();
        setTreeModel(model);
        applySelectedPaths(treeState);
        if(makeTreeModelUnique)
        {
        	makeUnique(model);
        }
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
    final void addLink(TreeNodeLink link)
    {
        links.put(link.getId(), link);
    }

    /**
     * Called when a link is clicked.
     * @see ILinkListener
     * @param cycle The cycle object
     */
    public final void linkClicked(final RequestCycle cycle)
    {
    	String param = TreeNodeLink.REQUEST_PARAMETER_LINK_ID;
        String linkId = ((HttpRequest)cycle.getRequest()).getParameter(param);
        TreeNodeLink link = (TreeNodeLink) links.get(linkId);
        if (link == null)
        {
            throw new IllegalStateException("link " + linkId + " not found");
        }
        link.linkClicked(cycle);
    }

    /**
     * Sets the tree model and creates an empty tree selection model.
     * @param model the tree model
     */
    protected final void setTreeModel(TreeModel model)
    {
        treeState.setModel(model);
        TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
        treeState.setSelectionModel(selectionModel);
        treeState.setRootVisible(true);
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
     * Get tree state object.
     * @return tree state object
     */
    public final TreeStateCache getTreeState()
    {
        return treeState;
    }

    /**
     * Find tree node for given user object.
     * @param userObject the user object to find the node for
     * @return node of given user object
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
}
