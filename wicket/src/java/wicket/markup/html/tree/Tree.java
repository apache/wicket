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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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
 * A component that represents a tree.
 *
 * @author Eelco Hillenius
 */
public class Tree extends Panel implements ILinkListener
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
    public Tree(final String componentName, TreeModel model)
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
    void addLink(TreeNodeLink link)
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
    public void linkClicked(final RequestCycle cycle)
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
    private void setTreeModel(TreeModel model)
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
    public void setExpandedState(TreePath selection, boolean expanded)
    {
        treeState.setExpandedState(selection, expanded);
        setSelectedPaths();
    }

    /**
     * Find tree path for the given user object.
     * @param userObject the user object to find the path for
     * @return tree path of given user object
     */
    public TreePath findTreePath(Serializable userObject)
    {
        return treeState.findTreePath(userObject);
    }

    /**
     * Get tree state object.
     * @return tree state object
     */
    public TreeStateCache getTreeState()
    {
        return treeState;
    }

    /**
     * Find tree node for given user object.
     * @param userObject the user object to find the node for
     * @return node of given user object
     */
    public DefaultMutableTreeNode findNode(Serializable userObject)
    {
        return treeState.findNode(userObject);
    }

    /**
     * Builds the structures needed to display the currently visible tree paths.
     */
    private void setSelectedPaths()
    {
        removeAll();
        List visiblePathsList = new ArrayList();
        TreePath selectedPath = treeState.getSelectedPath(); // get current
        Enumeration e = treeState.getVisiblePathsFromRoot(); // get all visible
        while (e.hasMoreElements()) // put enumeration in a list
        {
            visiblePathsList.add(e.nextElement());
        }
        List nestedList = new ArrayList(); // reference to the first level list
        buildList(visiblePathsList, 0, 0, nestedList); // build the nested lists
        add(getTreeRowsPanel("tree", nestedList)); // add the tree panel
    }

    /**
     * Gets the panel which displays the tree rows.
     * Override this if you want to provide your own panel.
     * @param nestedList the list that represents the currently visible tree paths.
     * @param componentName the name of the panel. Warning: this must be used to construct
     * the panel.
     * @return the panel that is used to display visible tree paths
     */
    protected Panel getTreeRowsPanel(String componentName, List nestedList)
    {
        return new TreeRows(componentName, nestedList, this);
    }

    /**
     * Gets the panel that displays one row.
     * Override this if you want to provide your own panel.
     * @param componentName the name of the panel.
     * Warning: if you did not override {@link TreeRows}, this must be
     * used to construct the panel.
     * @param nodeModel the model that holds a reference to the tree node and some
     * other usefull objects that help you construct the panel
     * @return the panel that displays one row
     */
    protected Panel getTreeRowPanel(String componentName, TreeNodeModel nodeModel)
    {
        return new TreeRow(componentName, this, nodeModel);
    }

    /**
     * Gets the panel that displays one row. For internal usage only.
     * @param componentName the name o fthe panel
     * @param nodeModel the node model
     * @return the panel that displays one row
     */
    final Panel internalGetTreeRowPanel(String componentName, TreeNodeModel nodeModel)
    {
        return getTreeRowPanel(componentName, nodeModel);
    }

    /**
     * Builds nested lists that represent the current visible tree paths.
     * @param visiblePathsList the whole - flat - list of visible paths
     * @param index the current index in the list of visible paths
     * @param level the current nesting level
     * @param rows a list that holds the current level of rows
     * @return the index in the list of visible paths
     */
    private int buildList(final List visiblePathsList, int index, int level, final List rows)
    {
        int len = visiblePathsList.size();
        while (index < len)
        {
            TreePath path = (TreePath) visiblePathsList.get(index);
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
            int thisLevel = treeNode.getLevel();
            if (thisLevel > level) // go deeper
            {
                List nestedRows = new ArrayList();
                rows.add(nestedRows);
                index = buildList(visiblePathsList, index, thisLevel, nestedRows);
            }
            else if (thisLevel < level) // end of nested
            {
                return index;
            }
            else // node
            {
                TreeNodeModel nodeModel = new TreeNodeModel(treeNode, treeState, path);
                rows.add(nodeModel);
                index++;
            }
        }
        return index;
    }
}
