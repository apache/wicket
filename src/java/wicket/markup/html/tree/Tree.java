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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import wicket.markup.html.panel.Panel;

/**
 * A component that represents a tree. It renders using nested lists that in turn
 * use panels.
 * <p>
 * This type of tree is best used when you want to display your tree using nested
 * &lt;ul&gt; and &lt;li&gt; tags.
 * </p>
 * <p>
 * For example, this could be the rendering result of a tree: (it will actually look
 * a bit different as we are using panels as well, but you'll get the idea)
 * <pre>
 * &lt;ul id="nested"&gt;
 *   &lt;li id="row"&gt;
 *     &lt;span id="label"&gt;foo&lt;/span&gt;
 *   &lt;/li&gt;
 *   &lt;ul id="nested"&gt;
 *     &lt;li id="row"&gt;
 *       &lt;span id="label"&gt;bar&lt;/span&gt;
 *     &lt;/li&gt;
 *   &lt;/ul&gt;
 *   &lt;li id="row"&gt;
 *     &lt;span id="label"&gt;suck&lt;/span&gt;
 *   &lt;/li&gt;
 * &lt;/ul&gt;
 * </pre>
 * </p>
 * Override the getXXXPanel methods to provide your own customized rendering.
 * Look at the filebrowser example of the wicket-examples project for an example
 * </p>
 *
 * @see wicket.markup.html.tree.AbstractTree
 * @see wicket.markup.html.tree.TreeNodeModel
 * @see wicket.markup.html.tree.TreeRows
 * @see wicket.markup.html.tree.TreeRow
 * @see wicket.markup.html.tree.TreeRowReplacementModel
 *
 * @author Eelco Hillenius
 */
public class Tree extends AbstractTree
{
    /**
     * Constructor.
     * @param componentName The name of this container
     * @param model the underlying tree model
     */
    public Tree(final String componentName, final TreeModel model)
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
    public Tree(final String componentName, final TreeModel model,
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
    public Tree(final String componentName, TreeStateCache treeState)
    {
        super(componentName, treeState);
    }

    /**
     * Builds the structures needed to display the currently visible tree paths.
     * @param treeState the current tree state
     */
    protected void applySelectedPaths(TreeStateCache treeState)
    {
        removeAll();
        List visiblePathsList = new ArrayList();
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
     * Gets the panel which displays the tree rows. Usually you'll want this panel
     * to be attached to a UL (Unnumbered List) tag.
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
     * Gets the panel that displays one row. Usually you'll want this panel to
     * be attached to a LI (List Item) tag.
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
                TreeNodeModel nodeModel = new TreeNodeModel(treeNode, getTreeState(), path);
                rows.add(nodeModel);
                index++;
            }
        }
        return index;
    }
}
