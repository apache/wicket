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

import wicket.DetachableModel;
import wicket.RequestCycle;

/**
 * Specialized model for trees.
 */
public class NodeModel extends DetachableModel
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
    public NodeModel(final DefaultMutableTreeNode treeNode, final TreeStateCache treeState,
            final TreePath path)
    {
        super(null);
        this.treeNode = treeNode;
        this.treeState = treeState;
        this.path = path;
    }

    /**
     * Get path.
     * @return path.
     */
    public final TreePath getPath()
    {
        return path;
    }

    /**
     * Get treeNode.
     * @return treeNode.
     */
    public final DefaultMutableTreeNode getTreeNode()
    {
        return treeNode;
    }

    /**
     * Get treeState.
     * @return treeState.
     */
    public final TreeStateCache getTreeState()
    {
        return treeState;
    }

    /**
     * @see wicket.DetachableModel#doDetach(wicket.RequestCycle)
     */
    protected void doDetach(RequestCycle cycle)
    {
    }

    /**
     * @see wicket.DetachableModel#doAttach(wicket.RequestCycle)
     */
    protected void doAttach(RequestCycle cycle)
    {
    }
}
