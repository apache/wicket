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

import wicket.IModel;
import wicket.Model;
import wicket.RequestCycle;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.link.ILinkListener;
import wicket.markup.html.table.ListItem;
import wicket.markup.html.table.ListView;
import wicket.protocol.http.HttpRequest;


/**
 * WARNING: THIS IS AN EXPERIMENTAL COMPONENT. ITS INTERFACE AND IMPLEMENTATION CAN
 * BE ALTERED DRASTIC IN FUTURE VERSIONS
 * A component that represents a tree.
 * TODO elaborate docs when tree design is stable.
 */
public abstract class Tree extends HtmlContainer implements ILinkListener
{
    /** tree state for this component. */
    private TreeStateCache treeState;

    /** table for the current visible tree paths. */
    private VisibleTreePathTable visibleTreePathTable;

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
     * Register link to this tree.
     * @param link the link to register
     */
    void addLink(TreeNodeLink link)
    {
        Node node = link.getNode();
        Serializable userObject = node.getUserObject();

        // links can change, but the target user object should be the same, so
        // if a new
        // link is added that actually points to the same userObject, it will
        // replace the old
        // one thus allowing the old link to be GC-ed. We need the creator hash
        // to be able
        // to have more trees in the same page with the same link names
        String linkId = link.getName() + "." + userObject.hashCode() + "." + link.creatorHash;

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
     * Add components.
     */
    private void setSelectedPaths()
    {
        TreePath selectedPath = treeState.getSelectedPath(); // get current

        // selection
        Enumeration e = treeState.getVisiblePathsFromRoot(); // get all visible

        // paths
        List visiblePathsList = new ArrayList();

        while (e.hasMoreElements())
        {
            visiblePathsList.add(e.nextElement());
        }

        Model model = new Model((Serializable) visiblePathsList);

        if (visibleTreePathTable == null)
        {
            visibleTreePathTable = new VisibleTreePathTable("tree", model);
            add(visibleTreePathTable);
        }
        else
        {
            visibleTreePathTable.reset();
            visibleTreePathTable.setModel(model);
        }
    }

    /**
     * Called to allow a subclass to populate a given spacer of the tree with components.
     * @param filler filler component
     */
    protected abstract void populateFiller(Filler filler);

    /**
     * Called to allow a subclass to populate a given node of the tree with components.
     * @param node The node
     */
    protected abstract void populateNode(Node node);

    /**
     * Table for visible tree paths.
     */
    private class VisibleTreePathTable extends ListView
    {
        /**
         * Construct.
         * @param name
         * @param model
         */
        public VisibleTreePathTable(String name, IModel model)
        {
            super(name, model);
        }

        /**
         * @see wicket.markup.html.table.ListView#populateItem(wicket.markup.html.table.ListItem)
         */
        protected void populateItem(ListItem listItem)
        {
            TreePath path = (TreePath) listItem.getModelObject();
            DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) path
                    .getLastPathComponent();
            int level = mutableTreeNode.getLevel();
            int row = getList().indexOf(path);
            NodeModel nodeModel = new NodeModel(mutableTreeNode, treeState, path);

            // add spacer component
            listItem.add(new FillerList("filler", nodeModel));

            // Create node
            Node node = new Node("node", nodeModel);

            populateNode(node);

            // add node component
            listItem.add(node);
        }

        /**
         * Remove all components without invalidating the model.
         */
        protected final void reset()
        {
            this.removeAll();
        }
    }

    /**
     * Renders a series of {@link Filler}s.
     */
    private class FillerList extends HtmlContainer
    {
        /**
         * Construct.
         * @param componentName name of the component
         * @param nodeModel the model for this node
         */
        public FillerList(String componentName, NodeModel nodeModel)
        {
            super(componentName, nodeModel);
        }

        /**
         * Renders this container.
         * @param cycle The request cycle
         */
        protected void handleRender(final RequestCycle cycle)
        {
            // Ask parents for markup stream to use
            final MarkupStream markupStream = findMarkupStream();

            // Save position in markup stream
            final int markupStart = markupStream.getCurrentIndex();

            NodeModel model = (NodeModel) getModel();
            int level = model.getTreeNode().getLevel();

            // Loop through the markup in this container for each child
            // container
            if (level > 0)
            {
                for (int i = 0; i < level; i++)
                {
                    // Get name of component for filler i
                    final String componentName = Integer.toString(i);
                    Filler filler = (Filler) get(componentName);

                    if (filler == null)
                    {
                        filler = new Filler(i);
                        populateFiller(filler);
                        add(filler);
                    }

                    // Rewind to start of markup for kids
                    markupStream.setCurrentIndex(markupStart);

                    // add sub
                    // Render cell
                    filler.render(cycle);
                }
            }
            else
            {
                markupStream.skipComponent();
            }
        }
    }
}
