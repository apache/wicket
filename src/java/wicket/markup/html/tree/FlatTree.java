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
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import wicket.IModel;
import wicket.Model;
import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.table.ListItem;
import wicket.markup.html.table.ListView;

/**
 * Tree that renders as a flat list, using spacers and nodes.
 *
 * @author Eelco Hillenius
 */
public abstract class FlatTree extends AbstractTree
{
    /** table for the current visible tree paths. */
    private VisibleTreePathListView visibleTreePathTable;

    /**
     * Constructor.
     * @param componentName The name of this container
     * @param model the underlying tree model
     */
    public FlatTree(final String componentName, final TreeModel model)
    {
        super(componentName, model);
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
    public FlatTree(final String componentName, final TreeModel model,
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
    public FlatTree(final String componentName, TreeStateCache treeState)
    {
        super(componentName, treeState);
    }

    /**
     * Builds the structures needed to display the currently visible tree paths.
     * @param treeState the current tree state
     */
    protected void applySelectedPaths(TreeStateCache treeState)
    {
        Enumeration e = treeState.getVisiblePathsFromRoot();
        List visiblePathsList = new ArrayList();
        while (e.hasMoreElements())
        {
            TreePath path = (TreePath)e.nextElement();
            DefaultMutableTreeNode treeNode =
                (DefaultMutableTreeNode)path.getLastPathComponent();
            TreeNodeModel treeNodeModel = new TreeNodeModel(treeNode, treeState, path);
            visiblePathsList.add(treeNodeModel);
        }
        Model model = new Model((Serializable) visiblePathsList);

        if (visibleTreePathTable == null)
        {
            visibleTreePathTable = new VisibleTreePathListView("tree", model);
            add(visibleTreePathTable);
        }
        else
        {
            visibleTreePathTable.reset();
            visibleTreePathTable.setModel(model);
        }
    }

    /**
     * Table for visible tree paths.
     */
    private final class VisibleTreePathListView extends ListView
    {
        /**
         * Construct.
         * @param name name of the component
         * @param model the model
         */
        public VisibleTreePathListView(String name, IModel model)
        {
            super(name, model);
        }

        /**
         * @see wicket.markup.html.table.ListView#populateItem(wicket.markup.html.table.ListItem)
         */
        protected void populateItem(ListItem listItem)
        {
            TreeNodeModel treeNodeModel = (TreeNodeModel)listItem.getModelObject();

            // add spacers
            int level = treeNodeModel.getLevel();
            List spacerList = new ArrayList(level);
            for(int i = 0; i < level; i++)
            {
                spacerList.add(treeNodeModel);
            }
            listItem.add(new SpacerList("spacers", spacerList));

            // add node
            HtmlContainer nodeContainer = new HtmlContainer("node");
            Serializable userObject = treeNodeModel.getUserObject();

            if (userObject == null)
            {
                throw new RuntimeException("userObject == null");
            }
            TreeNodeLink expandCollapsLink = new TreeNodeLink(
            		"expandCollapsLink", FlatTree.this, treeNodeModel);

            HtmlContainer junctionImg = new HtmlContainer("junctionImg");
            junctionImg.add(new ComponentTagAttributeModifier(
            		"src", true, new Model(getJunctionImageName(treeNodeModel))));
			expandCollapsLink.add(junctionImg);
            HtmlContainer nodeImg = new HtmlContainer("nodeImg");
            nodeImg.add(new ComponentTagAttributeModifier(
            		"src", true, new Model(getNodeImageName(treeNodeModel))));
			expandCollapsLink.add(nodeImg);
            nodeContainer.add(expandCollapsLink);

            TreeNodeLink selectLink = new TreeNodeLink(
            		"selectLink", FlatTree.this, treeNodeModel);
            selectLink.add(new Label("label", getUserObjectAsString(userObject)));
            nodeContainer.add(selectLink);
            listItem.add(nodeContainer);
        }

		/**
		 * Gets the user object as a string. Clients should override this to provide
		 * custom behaviour.
		 * @param userObject the user object
		 * @return the string representation of the user object
		 */
		protected String getUserObjectAsString(Serializable userObject)
		{
			return String.valueOf(userObject);
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
     * Get image name for junction.
     * @param node the model with the current node
     * @return image name
     */
    protected abstract String getJunctionImageName(TreeNodeModel node);

    /**
     * Get image name for node.
     * @param node the model with the current node
     * @return image name
     */
    protected abstract String getNodeImageName(TreeNodeModel node);

    /**
     * Renders spacer items.
     */
    private final class SpacerList extends ListView
    {
        /**
         * Construct.
         * @param componentName name of the component
         * @param list list
         */
        public SpacerList(String componentName, List list)
        {
            super(componentName, list);
        }

        /**
         * @see wicket.markup.html.table.ListView#populateItem(wicket.markup.html.table.ListItem)
         */
        protected void populateItem(ListItem listItem)
        {
        }
    }
}
