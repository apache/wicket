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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import wicket.model.Model;

/**
 * Specialized model for trees. It makes it easier/ less verbose to work with the
 * current tree node, and acts as a context-like wrapper object.
 * Each tree node has its own node model.
 *
 * @author Eelco Hillenius
 */
public final class TreeNodeModel extends Model
{
    /** tree node. */
    private final DefaultMutableTreeNode treeNode;

    /** tree state. */
    private final TreeStateCache treeState;

    /** part of path. */
    private final TreePath path;

    /**
     * Construct.
     * @param treeNode the current tree node
     * @param treeState the (shared) reference to the tree state
     * @param path the (shared) current path
     */
    public TreeNodeModel(final DefaultMutableTreeNode treeNode,
            final TreeStateCache treeState, final TreePath path)
    {
        super(null);
        this.treeNode = treeNode;
        this.treeState = treeState;
        this.path = path;
    }

    /**
     * Gets the wrapped tree path.
     * @return the wrapped tree path.
     */
    public final TreePath getPath()
    {
        return path;
    }

    /**
     * Gets the wrapped treeNode. NOTE: if you made the tree's user
     * objects unique by calling the <code>makeUnique</code> method of
     * {@link AbstractTree}, the user objects are wrapped in instances
     * of {@link IdWrappedUserObject}.
     * @return the wrapped treeNode
     */
    public final DefaultMutableTreeNode getTreeNode()
    {
        return treeNode;
    }

    /**
     * Gets the user object of the wrapped tree node.
     * @return the user object of the wrapped tree node
     */
    public final Serializable getUserObject()
    {
    	Object obj = treeNode.getUserObject();
    	if(obj instanceof IdWrappedUserObject)
    	{
    		return (Serializable)((IdWrappedUserObject)obj).getUserObject();
    	}
    	else
    	{
    		return (Serializable)obj;
    	}
    }

    /**
     * Gets the tree state object.
     * @return the tree state object.
     */
    public final TreeStateCache getTreeState()
    {
        return treeState;
    }

    /**
     * Gets whether this node is a leaf.
     * @return whether this node is a leaf.
     */
    public final boolean isLeaf()
    {
        return treeNode.isLeaf();
    }

    /**
     * Gets the current level.
     * @return the current level.
     */
    public final int getLevel()
    {
        return treeNode.getLevel();
    }

    /**
     * Gets whether this node is the root.
     * @return whether this node is the root.
     */
    public final boolean isRoot()
    {
        return treeNode.isRoot();
    }

    /**
     * Finds the tree path for the given user object.
     * @param userObject the user object
     * @return the tree path for the given user object
     */
    public TreePath findTreePath(Object userObject)
    {
        return treeState.findTreePath(userObject);
    }

    /**
     * Gets the tree node's siblings.
     * @return siblings.
     */
    public final boolean hasSiblings()
    {
        return (treeNode.getNextSibling() != null);
    }

    /**
     * Whether this node is part of the expanded path.
     * @return whether this node is part of the expanded path
     */
    public boolean isExpanded()
    {
        return treeState.isExpanded(path);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return String.valueOf(treeNode);
    }
}
