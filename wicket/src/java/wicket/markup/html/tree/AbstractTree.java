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
 * Base component for trees.
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
    public AbstractTree(final String componentName, TreeModel model)
    {
        super(componentName);
        treeState = new TreeStateCache();
        setTreeModel(model);
        setSelectedPaths();
    }

    /**
     * Registers a link with this tree.
     * @param link the link to register
     */
    final void addLink(TreeNodeLink link)
    {
        TreeNodeModel node = link.getNode();
        Serializable userObject = node.getUserObject();

        // links can change, but the target user object should be the same, so
        // if a new link is added that actually points to the same userObject, it will
        // replace the old one thus allowing the old link to be GC-ed. We need the creator hash
        // to be able to have more trees in the same page with the same link names
        String linkId = link.getName() + "." + userObject.hashCode();

        link.setId(linkId);
        links.put(linkId, link);
    }

    /**
     * Called when a link is clicked.
     * @see ILinkListener
     * @param cycle The cycle object
     */
    public final void linkClicked(final RequestCycle cycle)
    {
        String linkId = ((HttpRequest) cycle.getRequest()).getParameter("linkId");
        TreeNodeLink link = (TreeNodeLink) links.get(linkId);
        if (link == null)
        {
            throw new IllegalStateException("link " + linkId + " not found");
        }
        link.linkClicked(cycle);
    }

    /**
     * Set tree model.
     * @param model tree model
     */
    protected final void setTreeModel(TreeModel model)
    {
        treeState.setModel(model);
        TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
        treeState.setSelectionModel(selectionModel);
        treeState.setRootVisible(true);
    }

    /**
     * Set expanded property in tree state for selection.
     * @param selection the selection to set the expanded property for
     * @param expanded true if the selection is expanded, false otherwise
     */
    public final void setExpandedState(TreePath selection, boolean expanded)
    {
        treeState.setExpandedState(selection, expanded);
        setSelectedPaths();
    }

    /**
     * Find tree path for the given user object.
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
     * Applies the current selection.
     */
    protected abstract void setSelectedPaths();
}
