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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.VariableHeightLayoutCache;

/**
 * Holder and handler for tree state.
 *
 * @author Eelco Hillenius
 */
public class TreeStateCache extends VariableHeightLayoutCache implements Serializable
{
    /** currently selected path. */
    private TreePath selectedPath;

    /**
     * Expands the selected path and set selection to currently selected path.
     * @param selection the new selection.
     */
    public void setSelectedPath(TreePath selection)
    {
        setExpandedState(selection, true);
        this.selectedPath = selection;
    }

    /**
     * Gets the currently selected path.
     * @return the currently selected path
     */
    public TreePath getSelectedPath()
    {
        if ((selectedPath == null) && isRootVisible())
        {
            selectedPath = new TreePath(getModel().getRoot());
        }

        return selectedPath;
    }

    /**
     * Returns an <code>Enumerator</code> that increments over the visible paths
     * starting at the root. The ordering of the enumeration is based on how the paths are
     * displayed.
     * @return an <code>Enumerator</code> that increments over the visible paths
     */
    public Enumeration getVisiblePathsFromRoot()
    {
        TreeNode root = (TreeNode) (getModel().getRoot());
        TreePath rootPath = new TreePath(root);

        return getVisiblePathsFrom(rootPath);
    }

    /**
     * get tree path in model for given user object.
     * @param userObject object to look for in model
     * @return TreePath
     */
    public TreePath findTreePath(Object userObject)
    {
        TreePath path = null;
        DefaultMutableTreeNode endNode = findNode(userObject);

        if (endNode != null)
        {
            path = new TreePath(endNode.getPath());
        }

        return path;
    }

    /**
     * find the node in model that has arg as its userObject.
     * @param userObject object to look for in model
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode findNode(Object userObject)
    {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();

        return findNodeRecursively(root, null, userObject);
    }

    /**
     * Find the node by recursing.
     * @param currentNode the current node
     * @param resultNode the resulting node
     * @param userObject the user object to find
     * @return the found node
     */
    private DefaultMutableTreeNode findNodeRecursively(DefaultMutableTreeNode currentNode,
            DefaultMutableTreeNode resultNode, Object userObject)
    {
        int childCount = currentNode.getChildCount();

        if (currentNode.getUserObject().equals(userObject))
        {
            resultNode = currentNode;
        }
        else if (childCount > 0)
        {
            for (int i = 0; i < childCount; i++)
            {
                resultNode = findNodeRecursively(
                        (DefaultMutableTreeNode) currentNode.getChildAt(i), resultNode, userObject);

                if (resultNode != null)
                {
                    // found it! break loop
                    break;
                }
            }
        } // else: not found in this path

        return resultNode;
    }
}
