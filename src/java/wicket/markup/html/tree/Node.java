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


import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import wicket.RequestCycle;
import wicket.markup.html.HtmlContainer;

import java.io.Serializable;

/**
 * Container for tree nodes.
 * @author Eelco Hillenius
 */
public final class Node extends HtmlContainer
{
    /** index in parent list. */
    private int index;

    /** number of nodes from root. */
    private int level;

    /**
     * Construct.
     * @param name
     * @param model
     */
    public Node(String name, NodeModel model)
    {
        super(name, model);
    }

    /**
     * Gets the index of this cell in the parent table.
     * @return The index of this cell in the parent table
     */
    protected int getIndex()
    {
        return index;
    }

    /**
     * @return True if this cell is the first cell in the containing table
     */
    public boolean isFirst()
    {
        return index == 0;
    }

    /**
     * Get leaf.
     * @return leaf.
     */
    public final boolean isLeaf()
    {
        return getTreeNode().isLeaf();
    }

    /**
     * Get level.
     * @return level.
     */
    public final int getLevel()
    {
        return getTreeNode().getLevel();
    }

    /**
     * Get root.
     * @return root.
     */
    public final boolean isRoot()
    {
        return getTreeNode().isRoot();
    }

    /**
     * Get siblings.
     * @return siblings.
     */
    public final boolean hasSiblings()
    {
        return (getTreeNode().getNextSibling() != null);
    }

    /**
     * Whether this node is part of the expanded path.
     * @return whether this node is part of the expanded path
     */
    public boolean isExpanded()
    {
        TreeStateCache treeState = getNodeModel().getTreeState();
        TreePath path = getNodeModel().getPath();

        return treeState.isExpanded(path);
    }

    /**
     * Get tree node.
     * @return tree node
     */
    public DefaultMutableTreeNode getTreeNode()
    {
        return (DefaultMutableTreeNode) getNodeModel().getTreeNode();
    }

    /**
     * Get userObject.
     * @return userObject.
     */
    public final Serializable getUserObject()
    {
        return (Serializable) getTreeNode().getUserObject();
    }

    private NodeModel getNodeModel()
    {
        return (NodeModel) getModel();
    }

    /**
     * @see wicket.Component#handleRender(wicket.RequestCycle)
     */
    protected void handleRender(RequestCycle cycle)
    {
        super.handleRender(cycle);
    }
}
