/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.tree;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Specialized model for trees. It makes it easier/ less verbose to work with
 * the current tree node, and acts as a context-like wrapper object. Each tree
 * node has its own node model.
 * 
 * @author Eelco Hillenius
 */
public final class TreeNodeModel implements Serializable
{
	/** tree node. */
	private DefaultMutableTreeNode treeNode;

	/** tree state. */
	private TreeStateCache treeState;

	/** part of path. */
	private TreePath path;

	/**
	 * Construct.
	 * @param treeNode the current tree node
	 * @param treeState the (shared) reference to the tree state
	 * @param path the (shared) current path
	 */
	public TreeNodeModel(final DefaultMutableTreeNode treeNode, final TreeStateCache treeState,
			final TreePath path)
	{
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
	 * Gets the wrapped treeNode.
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
		return (Serializable)treeNode.getUserObject();
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

	/**
	 * Read state.
	 * @param in input stream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		this.treeNode = (DefaultMutableTreeNode)in.readObject();
		this.treeState = (TreeStateCache)in.readObject();
		this.path = (TreePath)in.readObject();
	}

	/**
	 * Write state.
	 * @param out output stream
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeObject(treeNode);
		out.writeObject(treeState);
		out.writeObject(path);
	}
}
