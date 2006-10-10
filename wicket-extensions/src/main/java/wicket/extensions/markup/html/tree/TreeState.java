/*
 * $Id: TreeState.java 5918 2006-05-27 21:02:27Z eelco12 $
 * $Revision: 5918 $ $Date: 2006-05-27 23:02:27 +0200 (so, 27 V 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.tree;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import wicket.util.collections.ArrayListStack;

/**
 * Holder and handler for tree state.
 * 
 * This class is largely based on
 * {@link javax.swing.tree.FixedHeightLayoutCache}from JDK 1.5_01. Using that
 * class or {@link javax.swing.tree.VariableHeightLayoutCache}gave problems
 * when working in clustered environments. Hence, for this class most of the
 * useful workings of FixedHeightLayoutCache were copied, while everything that
 * is Swing/paint specific was removed.
 * 
 * @author Eelco Hillenius
 * @author Scott Violet (Sun, FixedHeightLayoutCache)
 */
public final class TreeState implements Serializable, TreeModelListener, RowMapper
{
	/**
	 * Used as a placeholder when getting the path in FHTreeStateNodes.
	 */
	private final class SearchInfo implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private int childIndex;

		private boolean isNodeParentNode;

		private TreeStateNode node;

		private TreePath getPath()
		{
			if (node == null)
			{
				return null;
			}

			if (isNodeParentNode)
			{
				return node.getTreePath().pathByAddingChild(
						treeModel.getChild(node.getUserObject(), childIndex));
			}
			return node.path;
		}
	}

	/**
	 * TreeStateNode is used to track what has been expanded.
	 */
	private final class TreeStateNode extends DefaultMutableTreeNode
	{
		private static final long serialVersionUID = 1L;

		/** Child count of the receiver. */
		private int childCount;

		/** Index of this node from the model. */
		private int childIndex;

		/** Whether this node is expanded */
		private boolean isExpanded;

		/** Path of this node. */
		private TreePath path;

		/**
		 * Row of the receiver. This is only valid if the row is expanded.
		 */
		private int row;

		/**
		 * Construct.
		 * 
		 * @param userObject
		 * @param childIndex
		 * @param row
		 */
		public TreeStateNode(Object userObject, int childIndex, int row)
		{
			super(userObject);
			this.childIndex = childIndex;
			this.row = row;
		}

		//
		// Overriden DefaultMutableTreeNode methods
		//

		/**
		 * Returns the child for the passed in model index, this will return
		 * <code>null</code> if the child for <code>index</code> has not yet
		 * been created (expanded).
		 * 
		 * @param index
		 *            the childs index
		 * @return the tree state node
		 */
		public TreeStateNode getChildAtModelIndex(int index)
		{
			// PENDING: Make this a binary search!
			for (int counter = getChildCount() - 1; counter >= 0; counter--)
			{
				if (((TreeStateNode)getChildAt(counter)).childIndex == index)
				{
					return (TreeStateNode)getChildAt(counter);
				}
			}
			return null;
		}

		/**
		 * Returns the index of the receiver in the model.
		 * 
		 * @return the index of this node relative to its parent
		 */
		public int getChildIndex()
		{
			return childIndex;
		}

		/**
		 * Returns the row of the receiver.
		 * 
		 * @return the row of the receiver
		 */
		public int getRow()
		{
			return row;
		}

		//
		//

		/**
		 * Returns the row of the child with a model index of <code>index</code>.
		 * 
		 * @param index
		 *            the model index
		 * @return the row of the child with a model index of <code>index</code>
		 */
		public int getRowToModelIndex(int index)
		{
			TreeStateNode child;

			// This too could be a binary search!
			for (int counter = 0, maxCounter = getChildCount(); counter < maxCounter; counter++)
			{
				child = (TreeStateNode)getChildAt(counter);
				if (child.childIndex >= index)
				{
					if (child.childIndex == index)
					{
						return child.row;
					}
					if (counter == 0)
					{
						return getRow() + 1 + index;
					}
					return child.row - (child.childIndex - index);
				}
			}
			// YECK!
			return getRow() + 1 + getTotalChildCount() - (childCount - index);
		}

		/**
		 * Returns the number of children in the receiver by descending all
		 * expanded nodes and messaging them with getTotalChildCount.
		 * 
		 * @return the number of children in the receiver by descending all
		 *         expanded nodes and messaging them with getTotalChildCount
		 */
		public int getTotalChildCount()
		{
			if (isExpanded())
			{
				TreeStateNode parent = (TreeStateNode)getParent();
				int pIndex;

				if (parent != null && (pIndex = parent.getIndex(this)) + 1 < parent.getChildCount())
				{
					// This node has a created sibling, to calc total
					// child count directly from that!
					TreeStateNode nextSibling = (TreeStateNode)parent.getChildAt(pIndex + 1);

					return nextSibling.row - row - (nextSibling.childIndex - childIndex);
				}
				else
				{
					int retCount = childCount;

					for (int counter = getChildCount() - 1; counter >= 0; counter--)
					{
						retCount += ((TreeStateNode)getChildAt(counter)).getTotalChildCount();
					}
					return retCount;
				}
			}
			return 0;
		}

		/**
		 * Returns the <code>TreePath</code> of the receiver.
		 * 
		 * @return the tree path
		 */
		public TreePath getTreePath()
		{
			return path;
		}

		/**
		 * The highest visible nodes have a depth of 0.
		 * 
		 * @return the level of visibility
		 */
		public int getVisibleLevel()
		{
			if (isRootVisible())
			{
				return getLevel();
			}
			else
			{
				return getLevel() - 1;
			}
		}

		/**
		 * Returns true if this node is expanded.
		 * 
		 * @return true if this node is expanded
		 */
		public boolean isExpanded()
		{
			return isExpanded;
		}

		/**
		 * Returns true if the receiver is a leaf.
		 * 
		 * @return true if the receiver is a leaf
		 */
		@Override
		public boolean isLeaf()
		{
			TreeModel model = getModel();

			return (model != null) ? model.isLeaf(this.getUserObject()) : true;
		}

		/**
		 * Returns true if this node is visible. This is determined by asking
		 * all the parents if they are expanded.
		 * 
		 * @return true if this node is visible
		 */
		public boolean isVisible()
		{
			TreeStateNode parent = (TreeStateNode)getParent();

			if (parent == null)
			{
				return true;
			}
			return (parent.isExpanded() && parent.isVisible());
		}

		/**
		 * Messaged when this node is removed from its parent, this messages
		 * <code>removedFromMapping</code> to remove all the children.
		 * 
		 * @param childIndex
		 *            the index of this node relative to its parent
		 */
		@Override
		public void remove(int childIndex)
		{
			TreeStateNode node = (TreeStateNode)getChildAt(childIndex);

			node.removeFromMapping();
			super.remove(childIndex);
		}

		/**
		 * Messaged when this node is added somewhere, resets the path and adds
		 * a mapping from path to this node.
		 * 
		 * @param parent
		 *            The parent of this component
		 */
		@Override
		public void setParent(MutableTreeNode parent)
		{
			super.setParent(parent);
			if (parent != null)
			{
				path = ((TreeStateNode)parent).getTreePath().pathByAddingChild(getUserObject());
				addMapping(this);
			}
		}

		/**
		 * Messaged to set the user object. This resets the path.
		 * 
		 * @param o
		 *            the user object
		 */
		@Override
		public void setUserObject(Object o)
		{
			super.setUserObject(o);
			if (path != null)
			{
				TreeStateNode parent = (TreeStateNode)getParent();

				if (parent != null)
				{
					resetChildrenPaths(parent.getTreePath());
				}
				else
				{
					resetChildrenPaths(null);
				}
			}
		}

		/**
		 * Adjusts the child indexs of the receivers children by
		 * <code>adjust</code>, starting at <code>index</code>.
		 * 
		 * @param index
		 * @param adjust
		 */
		private void adjustChildIndexs(int index, int adjust)
		{
			for (int counter = index, maxCounter = getChildCount(); counter < maxCounter; counter++)
			{
				((TreeStateNode)getChildAt(counter)).childIndex += adjust;
			}
		}

		/**
		 * Adjusts the receiver, and all its children rows by
		 * <code>adjust</code>.
		 * 
		 * @param adjust
		 *            adjustement
		 */
		private void adjustRowBy(int adjust)
		{
			row += adjust;
			if (isExpanded)
			{
				for (int counter = getChildCount() - 1; counter >= 0; counter--)
				{
					((TreeStateNode)getChildAt(counter)).adjustRowBy(adjust);
				}
			}
		}

		/**
		 * Adjusts this node, its child, and its parent starting at an index of
		 * <code>startIndex</code> index is the index of the child to start
		 * adjusting from, which is not necessarily the model index.
		 * 
		 * @param adjust
		 * @param startIndex
		 */
		private void adjustRowBy(int adjust, int startIndex)
		{
			// Could check isVisible, but probably isn't worth it.
			if (isExpanded)
			{
				// children following startIndex.
				for (int counter = getChildCount() - 1; counter >= startIndex; counter--)
				{
					((TreeStateNode)getChildAt(counter)).adjustRowBy(adjust);
				}
			}
			// Parent
			TreeStateNode parent = (TreeStateNode)getParent();

			if (parent != null)
			{
				parent.adjustRowBy(adjust, parent.getIndex(this) + 1);
			}
		}

		/**
		 * Messaged when a child has been inserted at index. For all the
		 * children that have a childIndex >= index their index is incremented
		 * by one.
		 * 
		 * @param index
		 *            the insertion index
		 * @param isExpandedAndVisible
		 */
		private void childInsertedAtModelIndex(int index, boolean isExpandedAndVisible)
		{
			TreeStateNode aChild;
			int maxCounter = getChildCount();

			for (int counter = 0; counter < maxCounter; counter++)
			{
				aChild = (TreeStateNode)getChildAt(counter);
				if (aChild.childIndex >= index)
				{
					if (isExpandedAndVisible)
					{
						adjustRowBy(1, counter);
						adjustRowCountBy(1);
					}
					/*
					 * Since matched and children are always sorted by index, no
					 * need to continue testing with the above.
					 */
					for (; counter < maxCounter; counter++)
					{
						((TreeStateNode)getChildAt(counter)).childIndex++;
					}
					childCount++;
					return;
				}
			}
			// No children to adjust, but it was a child, so we still need
			// to adjust nodes after this one.
			if (isExpandedAndVisible)
			{
				adjustRowBy(1, maxCounter);
				adjustRowCountBy(1);
			}
			childCount++;
		}

		/**
		 * Collapses the receiver. If <code>adjustRows</code> is true, the
		 * rows of nodes after the receiver are adjusted.
		 * 
		 * @param adjustRows
		 *            whether to adjust the rows of the receiver
		 */
		private void collapse(boolean adjustRows)
		{
			if (isExpanded)
			{
				if (isVisible() && adjustRows)
				{
					int childCount = getTotalChildCount();

					isExpanded = false;
					adjustRowCountBy(-childCount);
					// We can do this because adjustRowBy won't descend
					// the children.
					adjustRowBy(-childCount, 0);
				}
				else
				{
					isExpanded = false;
				}

				if (adjustRows && isVisible() && treeSelectionModel != null)
				{
					treeSelectionModel.resetRowSelection();
				}
			}
		}

		/**
		 * Creates a new node to represent <code>userObject</code>. This does
		 * NOT check to ensure there isn't already a child node to manage
		 * <code>userObject</code>.
		 * 
		 * @param userObject
		 *            the user object of the new node
		 * @return the new node
		 */
		private TreeStateNode createChildFor(Object userObject)
		{
			int newChildIndex = treeModel.getIndexOfChild(getUserObject(), userObject);

			if (newChildIndex < 0)
			{
				return null;
			}

			TreeStateNode aNode;
			TreeStateNode child = createNodeForValue(userObject, newChildIndex);
			int childRow;

			if (isVisible())
			{
				childRow = getRowToModelIndex(newChildIndex);
			}
			else
			{
				childRow = -1;
			}
			child.row = childRow;
			for (int counter = 0, maxCounter = getChildCount(); counter < maxCounter; counter++)
			{
				aNode = (TreeStateNode)getChildAt(counter);
				if (aNode.childIndex > newChildIndex)
				{
					insert(child, counter);
					return child;
				}
			}
			add(child);
			return child;
		}

		/**
		 * Messaged when the node has expanded. This updates all of the
		 * receivers children rows, as well as the total row count.
		 */
		private void didExpand()
		{
			int nextRow = setRowAndChildren(row);
			TreeStateNode parent = (TreeStateNode)getParent();
			int childRowCount = nextRow - row - 1;

			if (parent != null)
			{
				parent.adjustRowBy(childRowCount, parent.getIndex(this) + 1);
			}
			adjustRowCountBy(childRowCount);
		}

		/**
		 * Expands the receiver.
		 */
		private void expand()
		{
			if (!isExpanded && !isLeaf())
			{
				boolean visible = isVisible();

				isExpanded = true;
				childCount = treeModel.getChildCount(getUserObject());

				if (visible)
				{
					didExpand();
				}

				// Update the selection model.
				if (visible && treeSelectionModel != null)
				{
					treeSelectionModel.resetRowSelection();
				}
			}
		}

		/**
		 * Invokes <code>expandParentAndReceiver</code> on the parent, and
		 * expands the receiver.
		 */
		private void expandParentAndReceiver()
		{
			TreeStateNode parent = (TreeStateNode)getParent();

			if (parent != null)
			{
				parent.expandParentAndReceiver();
			}
			expand();
		}

		/**
		 * Returns true if there is a row for <code>row</code>.
		 * <code>nextRow</code> gives the bounds of the receiver. Information
		 * about the found row is returned in <code>info</code>. This should
		 * be invoked on root with <code>nextRow</code> set to
		 * <code>getRowCount</code> ().
		 * 
		 * @param row
		 * @param nextRow
		 * @param info
		 *            search info object
		 * @return true if there is a row for <code>row</code>
		 */
		private boolean getPathForRow(int row, int nextRow, SearchInfo info)
		{
			if (this.row == row)
			{
				info.node = this;
				info.isNodeParentNode = false;
				info.childIndex = childIndex;
				return true;
			}

			TreeStateNode child;
			TreeStateNode lastChild = null;

			for (int counter = 0, maxCounter = getChildCount(); counter < maxCounter; counter++)
			{
				child = (TreeStateNode)getChildAt(counter);
				if (child.row > row)
				{
					if (counter == 0)
					{
						// No node exists for it, and is first.
						info.node = this;
						info.isNodeParentNode = true;
						info.childIndex = row - this.row - 1;
						return true;
					}
					else if (lastChild != null)
					{
						// May have been in last childs bounds.
						int lastChildEndRow = 1 + child.row
								- (child.childIndex - lastChild.childIndex);

						if (row < lastChildEndRow)
						{
							return lastChild.getPathForRow(row, lastChildEndRow, info);
						}
						// Between last child and child, but not in last child
						info.node = this;
						info.isNodeParentNode = true;
						info.childIndex = row - lastChildEndRow + lastChild.childIndex + 1;
						return true;
					}
				}
				lastChild = child;
			}

			// Not in children, but we should have it, offset from
			// nextRow.
			if (lastChild != null)
			{
				int lastChildEndRow = nextRow - (childCount - lastChild.childIndex) + 1;

				if (row < lastChildEndRow)
				{
					return lastChild.getPathForRow(row, lastChildEndRow, info);
				}
				// Between last child and child, but not in last child
				info.node = this;
				info.isNodeParentNode = true;
				info.childIndex = row - lastChildEndRow + lastChild.childIndex + 1;
				return true;
			}
			else
			{
				// No children.
				int retChildIndex = row - this.row - 1;

				if (retChildIndex >= childCount)
				{
					return false;
				}
				info.node = this;
				info.isNodeParentNode = true;
				info.childIndex = retChildIndex;
				return true;
			}
		}

		/**
		 * Makes the receiver visible, but invoking
		 * <code>expandParentAndReceiver</code> on the superclass.
		 */
		private void makeVisible()
		{
			TreeStateNode parent = (TreeStateNode)getParent();

			if (parent != null)
			{
				parent.expandParentAndReceiver();
			}
		}

		/**
		 * Adds newChild to this nodes children at the appropriate location. The
		 * location is determined from the childIndex of newChild.
		 * 
		 * @param newChild
		 *            the node to add
		 */
		// private void addNode(TreeStateNode newChild)
		// {
		// boolean added = false;
		// int childIndex = newChild.getChildIndex();
		//
		// for (int counter = 0, maxCounter = getChildCount(); counter <
		// maxCounter; counter++)
		// {
		// if (((TreeStateNode) getChildAt(counter)).getChildIndex() >
		// childIndex)
		// {
		// added = true;
		// insert(newChild, counter);
		// counter = maxCounter;
		// }
		// }
		// if (!added)
		// add(newChild);
		// }
		/**
		 * Removes the child at <code>modelIndex</code>.
		 * <code>isChildVisible</code> should be true if the receiver is
		 * visible and expanded.
		 * 
		 * @param modelIndex
		 * @param isChildVisible
		 */
		private void removeChildAtModelIndex(int modelIndex, boolean isChildVisible)
		{
			TreeStateNode childNode = getChildAtModelIndex(modelIndex);

			if (childNode != null)
			{
				int row = childNode.getRow();
				int index = getIndex(childNode);

				childNode.collapse(false);
				remove(index);
				adjustChildIndexs(index, -1);
				childCount--;
				if (isChildVisible)
				{
					// Adjust the rows.
					resetChildrenRowsFrom(row, index, modelIndex);
				}
			}
			else
			{
				int maxCounter = getChildCount();
				TreeStateNode aChild;

				for (int counter = 0; counter < maxCounter; counter++)
				{
					aChild = (TreeStateNode)getChildAt(counter);
					if (aChild.childIndex >= modelIndex)
					{
						if (isChildVisible)
						{
							adjustRowBy(-1, counter);
							adjustRowCountBy(-1);
						}
						// Since matched and children are always sorted by
						// index, no need to continue testing with the
						// above.
						for (; counter < maxCounter; counter++)
						{
							((TreeStateNode)getChildAt(counter)).childIndex--;
						}
						childCount--;
						return;
					}
				}
				// No children to adjust, but it was a child, so we still need
				// to adjust nodes after this one.
				if (isChildVisible)
				{
					adjustRowBy(-1, maxCounter);
					adjustRowCountBy(-1);
				}
				childCount--;
			}
		}

		/**
		 * Removes the receiver, and all its children, from the mapping table.
		 */
		private void removeFromMapping()
		{
			if (path != null)
			{
				removeMapping(this);
				for (int counter = getChildCount() - 1; counter >= 0; counter--)
				{
					((TreeStateNode)getChildAt(counter)).removeFromMapping();
				}
			}
		}

		/**
		 * Recreates the receivers path, and all its childrens paths.
		 * 
		 * @param parentPath
		 *            The parent path
		 */
		private void resetChildrenPaths(TreePath parentPath)
		{
			removeMapping(this);
			if (parentPath == null)
			{
				path = new TreePath(getUserObject());
			}
			else
			{
				path = parentPath.pathByAddingChild(getUserObject());
			}
			addMapping(this);
			for (int counter = getChildCount() - 1; counter >= 0; counter--)
			{
				((TreeStateNode)getChildAt(counter)).resetChildrenPaths(path);
			}
		}

		/**
		 * Resets the receivers childrens rows. Starting with the child at
		 * <code>childIndex</code> (and <code>modelIndex</code>) to
		 * <code>newRow</code>. This uses <code>setRowAndChildren</code> to
		 * recursively descend children, and uses <code>resetRowSelection</code>
		 * to ascend parents.
		 * 
		 * @param newRow
		 * @param childIndex
		 * @param modelIndex
		 */
		// This can be rather expensive, but is needed for the collapse
		// case this is resulting from a remove (although I could fix
		// that by having instances of TreeStateNode hold a ref to
		// the number of children). I prefer this though, making determing
		// the row of a particular node fast is very nice!
		private void resetChildrenRowsFrom(int newRow, int childIndex, int modelIndex)
		{
			int lastRow = newRow;
			int lastModelIndex = modelIndex;
			TreeStateNode node;
			int maxCounter = getChildCount();

			for (int counter = childIndex; counter < maxCounter; counter++)
			{
				node = (TreeStateNode)getChildAt(counter);
				lastRow += (node.childIndex - lastModelIndex);
				lastModelIndex = node.childIndex + 1;
				if (node.isExpanded)
				{
					lastRow = node.setRowAndChildren(lastRow);
				}
				else
				{
					node.row = lastRow++;
				}
			}
			lastRow += childCount - lastModelIndex;
			node = (TreeStateNode)getParent();
			if (node != null)
			{
				node.resetChildrenRowsFrom(lastRow, node.getIndex(this) + 1, this.childIndex + 1);
			}
			else
			{ // This is the root, reset total ROWCOUNT!
				rowCount = lastRow;
			}
		}

		/**
		 * Sets the receivers row to <code>nextRow</code> and recursively
		 * updates all the children of the receivers rows. The index the next
		 * row is to be placed as is returned.
		 * 
		 * @param nextRow
		 * @return the index the next row is to be placed
		 */
		private int setRowAndChildren(int nextRow)
		{
			row = nextRow;

			if (!isExpanded())
			{
				return row + 1;
			}

			int lastRow = row + 1;
			int lastModelIndex = 0;
			TreeStateNode child;
			int maxCounter = getChildCount();

			for (int counter = 0; counter < maxCounter; counter++)
			{
				child = (TreeStateNode)getChildAt(counter);
				lastRow += (child.childIndex - lastModelIndex);
				lastModelIndex = child.childIndex + 1;
				if (child.isExpanded)
				{
					lastRow = child.setRowAndChildren(lastRow);
				}
				else
				{
					child.row = lastRow++;
				}
			}
			return lastRow + childCount - lastModelIndex;
		}

		/**
		 * Asks all the children of the receiver for their totalChildCount and
		 * returns this value (plus stopIndex).
		 * 
		 * @param stopIndex
		 *            index to stop on
		 * @return childcount of all children of the receiver
		 */
		// private int getCountTo(int stopIndex)
		// {
		// TreeStateNode aChild;
		// int retCount = stopIndex + 1;
		//
		// for (int counter = 0, maxCounter = getChildCount(); counter <
		// maxCounter; counter++)
		// {
		// aChild = (TreeStateNode) getChildAt(counter);
		// if (aChild.childIndex >= stopIndex)
		// counter = maxCounter;
		// else
		// retCount += aChild.getTotalChildCount();
		// }
		// if (parent != null)
		// return retCount + ((TreeStateNode)
		// getParent()).getCountTo(childIndex);
		// if (!isRootVisible())
		// return (retCount - 1);
		// return retCount;
		// }
		/**
		 * Returns the number of children that are expanded to
		 * <code>stopIndex</code>. This does not include the number of
		 * children that the child at <code>stopIndex</code> might have.
		 * 
		 * @param stopIndex
		 *            index to stop on
		 * @return the number of children that are expanded to
		 *         <code>stopIndex</code>
		 */
		// private int getNumExpandedChildrenTo(int stopIndex)
		// {
		// TreeStateNode aChild;
		// int retCount = stopIndex;
		//
		// for (int counter = 0, maxCounter = getChildCount(); counter <
		// maxCounter; counter++)
		// {
		// aChild = (TreeStateNode) getChildAt(counter);
		// if (aChild.childIndex >= stopIndex)
		// return retCount;
		// else
		// {
		// retCount += aChild.getTotalChildCount();
		// }
		// }
		// return retCount;
		// }
		/**
		 * Messaged when this node either expands or collapses.
		 */
		// private void didAdjustTree()
		// {
		// }
	}

	/**
	 * An enumerator to iterate through visible nodes.
	 */
	private final class VisibleTreeStateNodeEnumeration implements Enumeration
	{
		/** Number of children in parent. */
		private int childCount;

		/**
		 * Index of next child. An index of -1 signifies parent should be
		 * visibled next.
		 */
		private int nextIndex;

		/** Parent thats children are being enumerated. */
		private TreeStateNode parent;

		private VisibleTreeStateNodeEnumeration(TreeStateNode node)
		{
			this(node, -1);
		}

		private VisibleTreeStateNodeEnumeration(TreeStateNode parent, int startIndex)
		{
			this.parent = parent;
			this.nextIndex = startIndex;
			this.childCount = treeModel.getChildCount(this.parent.getUserObject());
		}

		/**
		 * @return true if more visible nodes.
		 */
		public boolean hasMoreElements()
		{
			return (parent != null);
		}

		/**
		 * @return next visible TreePath.
		 */
		public Object nextElement()
		{
			if (!hasMoreElements())
			{
				throw new NoSuchElementException("No more visible paths");
			}

			TreePath retObject;

			if (nextIndex == -1)
			{
				retObject = parent.getTreePath();
			}
			else
			{
				TreeStateNode node = parent.getChildAtModelIndex(nextIndex);

				if (node == null)
				{
					retObject = parent.getTreePath().pathByAddingChild(
							treeModel.getChild(parent.getUserObject(), nextIndex));
				}
				else
				{
					retObject = node.getTreePath();
				}
			}
			updateNextObject();
			return retObject;
		}

		/**
		 * Finds the next valid parent, this should be called when nextIndex is
		 * beyond the number of children of the current parent.
		 * 
		 * @return whether there is a next valid parent
		 */
		private boolean findNextValidParent()
		{
			if (parent == root)
			{
				// mark as invalid!
				parent = null;
				return false;
			}
			while (parent != null)
			{
				TreeStateNode newParent = (TreeStateNode)parent.getParent();

				if (newParent != null)
				{
					nextIndex = parent.childIndex;
					parent = newParent;
					childCount = treeModel.getChildCount(parent.getUserObject());
					if (updateNextIndex())
					{
						return true;
					}
				}
				else
				{
					parent = null;
				}
			}
			return false;
		}

		/**
		 * Updates <code>nextIndex</code> returning false if it is beyond the
		 * number of children of parent.
		 * 
		 * @return false if it is beyond the number of children of parent
		 */
		private boolean updateNextIndex()
		{
			// nextIndex == -1 identifies receiver, make sure is expanded
			// before descend.
			if (nextIndex == -1 && !parent.isExpanded())
			{
				return false;
			}

			// Check that it can have kids
			if (childCount == 0)
			{
				return false;
			}
			// Make sure next index not beyond child count.
			else if (++nextIndex >= childCount)
			{
				return false;
			}

			TreeStateNode child = parent.getChildAtModelIndex(nextIndex);

			if (child != null && child.isExpanded())
			{
				parent = child;
				nextIndex = -1;
				childCount = treeModel.getChildCount(child.getUserObject());
			}
			return true;
		}

		/**
		 * Determines the next object by invoking <code>updateNextIndex</code>
		 * and if not succesful <code>findNextValidParent</code>.
		 */
		private void updateNextObject()
		{
			if (!updateNextIndex())
			{
				findNextValidParent();
			}
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Used for getting path/row information.
	 */
	private SearchInfo info;

	/** Root node. */
	private TreeStateNode root;

	/**
	 * True if the root node is displayed, false if its children are the highest
	 * visible nodes.
	 */
	private boolean rootVisible;

	/** Number of rows currently visible. */
	private int rowCount;

	/** currently selected path. */
	private TreePath selectedPath;

	private ArrayListStack<ArrayListStack<TreePath>> tempStacks;

	/** Model providing information. */
	private TreeModel treeModel;

	/**
	 * Maps from TreePath to a TreeStateNode.
	 */
	private Hashtable<TreePath, TreeStateNode> treePathMapping;

	/** Selection model. */
	private TreeSelectionModel treeSelectionModel;

	/**
	 * Construct.
	 */
	public TreeState()
	{
		tempStacks = new ArrayListStack<ArrayListStack<TreePath>>();
		treePathMapping = new Hashtable<TreePath, TreeStateNode>();
		info = new SearchInfo();
	}

	/**
	 * Returns true if the path is expanded, and visible.
	 * 
	 * @param path
	 *            the path
	 * @return true if the path is expanded, and visible
	 */
	public boolean getExpandedState(TreePath path)
	{
		TreeStateNode node = getNodeForPath(path, true, false);
		return (node != null) ? (node.isVisible() && node.isExpanded()) : false;
	}

	/**
	 * Returns the <code>TreeModel</code> that is providing the data.
	 * 
	 * @return the <code>TreeModel</code> that is providing the data
	 */
	public TreeModel getModel()
	{
		return treeModel;
	}

	/**
	 * Returns the path for passed in row. If row is not visible null is
	 * returned.
	 * 
	 * @param row
	 *            the row
	 * @return the path for passed in row
	 */
	public TreePath getPathForRow(int row)
	{
		if (row >= 0 && row < getRowCount())
		{
			if (root.getPathForRow(row, getRowCount(), info))
			{
				return info.getPath();
			}
		}
		return null;
	}

	//
	// RowMapper
	//

	/**
	 * Returns the number of visible rows.
	 * 
	 * @return the number of visible rows
	 */
	public int getRowCount()
	{
		return rowCount;
	}

	/**
	 * Returns the row that the last item identified in path is visible at. Will
	 * return -1 if any of the elements in path are not currently visible.
	 * 
	 * @param path
	 *            the path
	 * @return the row that the last item identified in path is visible at
	 */
	public int getRowForPath(TreePath path)
	{
		if (path == null || root == null)
		{
			return -1;
		}

		TreeStateNode node = getNodeForPath(path, true, false);

		if (node != null)
		{
			return node.getRow();
		}

		TreePath parentPath = path.getParentPath();

		node = getNodeForPath(parentPath, true, false);
		if (node != null && node.isExpanded())
		{
			return node.getRowToModelIndex(treeModel.getIndexOfChild(parentPath
					.getLastPathComponent(), path.getLastPathComponent()));
		}
		return -1;
	}

	/**
	 * Returns the rows that the <code>TreePath</code> instances in
	 * <code>path</code> are being displayed at. This method should return an
	 * array of the same length as that passed in, and if one of the
	 * <code>TreePaths</code> in <code>path</code> is not valid its entry in
	 * the array should be set to -1.
	 * 
	 * @param paths
	 *            the array of <code>TreePath</code> s being queried
	 * @return an array of the same length that is passed in containing the rows
	 *         that each corresponding where each <code>TreePath</code> is
	 *         displayed; if <code>paths</code> is <code>null</code>,
	 *         <code>null</code> is returned
	 */
	public int[] getRowsForPaths(TreePath[] paths)
	{
		if (paths == null)
		{
			return null;
		}

		int numPaths = paths.length;
		int[] rows = new int[numPaths];

		for (int counter = 0; counter < numPaths; counter++)
		{
			rows[counter] = getRowForPath(paths[counter]);
		}
		return rows;
	}

	/**
	 * Gets the currently selected path.
	 * 
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
	 * Returns the model used to maintain the selection.
	 * 
	 * @return the <code>treeSelectionModel</code>
	 */
	public TreeSelectionModel getSelectionModel()
	{
		return treeSelectionModel;
	}

	/**
	 * Returns the number of visible children for row.
	 * 
	 * @param path
	 *            the path
	 * @return the number of visible children for row
	 */
	public int getVisibleChildCount(TreePath path)
	{
		TreeStateNode node = getNodeForPath(path, true, false);
		if (node == null)
		{
			return 0;
		}
		return node.getTotalChildCount();
	}

	/**
	 * Returns an Enumerator that increments over the visible paths starting at
	 * the passed in location. The ordering of the enumeration is based on how
	 * the paths are displayed.
	 * 
	 * @param path
	 *            the path
	 * @return an Enumerator that increments over the visible paths
	 */
	public Enumeration getVisiblePathsFrom(TreePath path)
	{
		if (path == null)
		{
			return null;
		}

		TreeStateNode node = getNodeForPath(path, true, false);

		if (node != null)
		{
			return new VisibleTreeStateNodeEnumeration(node);
		}
		TreePath parentPath = path.getParentPath();

		node = getNodeForPath(parentPath, true, false);
		if (node != null && node.isExpanded())
		{
			return new VisibleTreeStateNodeEnumeration(node, treeModel.getIndexOfChild(parentPath
					.getLastPathComponent(), path.getLastPathComponent()));
		}
		return null;
	}

	/**
	 * Returns true if the value identified by row is currently expanded.
	 * 
	 * @param path
	 *            the row
	 * @return true if the value identified by row is currently expanded
	 */
	public boolean isExpanded(TreePath path)
	{
		if (path != null)
		{
			TreeStateNode lastNode = getNodeForPath(path, true, false);
			return (lastNode != null && lastNode.isExpanded());
		}
		return false;
	}

	/**
	 * Returns true if the root node of the tree is displayed.
	 * 
	 * @return true if the root node of the tree is displayed
	 */
	public boolean isRootVisible()
	{
		return rootVisible;
	}

	/**
	 * Marks the path <code>path</code> expanded state to
	 * <code>isExpanded</code>.
	 * 
	 * @param path
	 *            the path
	 * @param isExpanded
	 *            whether the path is expanded
	 */
	public void setExpandedState(TreePath path, boolean isExpanded)
	{
		if (isExpanded)
		{
			ensurePathIsExpanded(path, true);
		}
		else if (path != null)
		{
			TreePath parentPath = path.getParentPath();

			// YECK! Make the parent expanded.
			if (parentPath != null)
			{
				TreeStateNode parentNode = getNodeForPath(parentPath, false, true);
				if (parentNode != null)
				{
					parentNode.makeVisible();
				}
			}
			// And collapse the child.
			TreeStateNode childNode = getNodeForPath(path, true, false);

			if (childNode != null)
			{
				childNode.collapse(true);
			}
		}
	}

	/**
	 * Sets the TreeModel that will provide the data.
	 * 
	 * @param newModel
	 *            the TreeModel that is to provide the data
	 */
	public void setModel(TreeModel newModel)
	{
		this.treeModel = newModel;
		rebuild(false);
	}

	//
	// TreeModelListener methods
	//

	/**
	 * Determines whether or not the root node from the TreeModel is visible.
	 * 
	 * @param rootVisible
	 *            true if the root node of the tree is to be displayed
	 */
	public void setRootVisible(boolean rootVisible)
	{
		if (isRootVisible() != rootVisible)
		{
			this.rootVisible = rootVisible;
			if (root != null)
			{
				if (rootVisible)
				{
					rowCount++;
					root.adjustRowBy(1);
				}
				else
				{
					rowCount--;
					root.adjustRowBy(-1);
				}
				visibleNodesChanged();
			}
		}
	}

	/**
	 * Expands the selected path and set selection to currently selected path.
	 * 
	 * @param selection
	 *            the new selection.
	 */
	public void setSelectedPath(TreePath selection)
	{
		setExpandedState(selection, true);
		this.selectedPath = selection;

		// if we have a multiple selection model
		if (treeSelectionModel != null)
		{
			if (treeSelectionModel.isPathSelected(selection))
			{
				treeSelectionModel.removeSelectionPath(selection);
			}
			else
			{
				treeSelectionModel.addSelectionPath(selection);
			}
		}
	}

	/**
	 * Sets the <code>TreeSelectionModel</code> used to manage the selection
	 * to new LSM.
	 * 
	 * @param selectionModel
	 *            the new <code>TreeSelectionModel</code>
	 */
	public void setSelectionModel(TreeSelectionModel selectionModel)
	{
		if (this.treeSelectionModel != null)
		{
			this.treeSelectionModel.setRowMapper(null);
		}
		if (selectionModel != null)
		{
			selectionModel.setRowMapper(this);
		}
		this.treeSelectionModel = selectionModel;
	}

	/**
	 * <p>
	 * Invoked after a node (or a set of siblings) has changed in some way. The
	 * node(s) have not changed locations in the tree or altered their children
	 * arrays, but other attributes have changed and may affect presentation.
	 * Example: the name of a file has changed, but it is in the same location
	 * in the file system.
	 * </p>
	 * <p>
	 * e.path() returns the path the parent of the changed node(s).
	 * </p>
	 * <p>
	 * e.childIndices() returns the index(es) of the changed node(s).
	 * </p>
	 * 
	 * @param e
	 *            the tree model event
	 */
	public void treeNodesChanged(TreeModelEvent e)
	{
		if (e != null)
		{
			int changedIndexs[];
			TreeStateNode changedParent = getNodeForPath(e.getTreePath(), false, false);
			int maxCounter;

			changedIndexs = e.getChildIndices();
			/*
			 * Only need to update the children if the node has been expanded
			 * once.
			 */
			// PENDING(scott): make sure childIndexs is sorted!
			if (changedParent != null)
			{
				if (changedIndexs != null && (maxCounter = changedIndexs.length) > 0)
				{
					Object parentValue = changedParent.getUserObject();

					for (int counter = 0; counter < maxCounter; counter++)
					{
						TreeStateNode child = changedParent
								.getChildAtModelIndex(changedIndexs[counter]);

						if (child != null)
						{
							child.setUserObject(treeModel.getChild(parentValue,
									changedIndexs[counter]));
						}
					}
					if (changedParent.isVisible() && changedParent.isExpanded())
					{
						visibleNodesChanged();
					}
				}
				// Null for root indicates it changed.
				else if (changedParent == root && changedParent.isVisible()
						&& changedParent.isExpanded())
				{
					visibleNodesChanged();
				}
			}
		}
	}

	//
	// Local methods
	//

	/**
	 * <p>
	 * Invoked after nodes have been inserted into the tree.
	 * </p>
	 * <p>
	 * e.path() returns the parent of the new nodes
	 * <p>
	 * e.childIndices() returns the indices of the new nodes in ascending order.
	 * 
	 * @param e
	 *            the tree model event
	 */
	public void treeNodesInserted(TreeModelEvent e)
	{
		if (e != null)
		{
			int changedIndexs[];
			TreeStateNode changedParent = getNodeForPath(e.getTreePath(), false, false);
			int maxCounter;

			changedIndexs = e.getChildIndices();
			/*
			 * Only need to update the children if the node has been expanded
			 * once.
			 */
			// PENDING(scott): make sure childIndexs is sorted!
			if (changedParent != null && changedIndexs != null
					&& (maxCounter = changedIndexs.length) > 0)
			{
				boolean isVisible = (changedParent.isVisible() && changedParent.isExpanded());

				for (int counter = 0; counter < maxCounter; counter++)
				{
					changedParent.childInsertedAtModelIndex(changedIndexs[counter], isVisible);
				}
				if (isVisible && treeSelectionModel != null)
				{
					treeSelectionModel.resetRowSelection();
				}
				if (changedParent.isVisible())
				{
					this.visibleNodesChanged();
				}
			}
		}
	}

	/**
	 * <p>
	 * Invoked after nodes have been removed from the tree. Note that if a
	 * subtree is removed from the tree, this method may only be invoked once
	 * for the root of the removed subtree, not once for each individual set of
	 * siblings removed.
	 * </p>
	 * <p>
	 * e.path() returns the former parent of the deleted nodes.
	 * </p>
	 * <p>
	 * e.childIndices() returns the indices the nodes had before they were
	 * deleted in ascending order.
	 * </p>
	 * 
	 * @param e
	 *            the tree model event
	 */
	public void treeNodesRemoved(TreeModelEvent e)
	{
		if (e != null)
		{
			int changedIndexs[];
			int maxCounter;
			TreePath parentPath = e.getTreePath();
			TreeStateNode changedParentNode = getNodeForPath(parentPath, false, false);

			changedIndexs = e.getChildIndices();
			// PENDING(scott): make sure that changedIndexs are sorted in
			// ascending order.
			if (changedParentNode != null && changedIndexs != null
					&& (maxCounter = changedIndexs.length) > 0)
			{
				boolean isVisible = (changedParentNode.isVisible() && changedParentNode
						.isExpanded());

				for (int counter = maxCounter - 1; counter >= 0; counter--)
				{
					changedParentNode.removeChildAtModelIndex(changedIndexs[counter], isVisible);
				}
				if (isVisible)
				{
					if (treeSelectionModel != null)
					{
						treeSelectionModel.resetRowSelection();
					}
					if (treeModel.getChildCount(changedParentNode.getUserObject()) == 0
							&& changedParentNode.isLeaf())
					{
						// Node has become a leaf, collapse it.
						changedParentNode.collapse(false);
					}
					visibleNodesChanged();
				}
				else if (changedParentNode.isVisible())
				{
					visibleNodesChanged();
				}
			}
		}
	}

	/**
	 * <p>
	 * Invoked after the tree has drastically changed structure from a given
	 * node down. If the path returned by e.getPath() is of length one and the
	 * first element does not identify the current root node the first element
	 * should become the new root of the tree.
	 * <p>
	 * <p>
	 * e.path() holds the path to the node.
	 * </p>
	 * <p>
	 * e.childIndices() returns null.
	 * </p>
	 * 
	 * @param e
	 *            the tree model event
	 */
	public void treeStructureChanged(TreeModelEvent e)
	{
		if (e != null)
		{
			TreePath changedPath = e.getTreePath();
			TreeStateNode changedNode = getNodeForPath(changedPath, false, false);

			// Check if root has changed, either to a null root, or
			// to an entirely new root.
			if (changedNode == root
					|| (changedNode == null && ((changedPath == null && treeModel != null && treeModel
							.getRoot() == null) || (changedPath != null && changedPath
							.getPathCount() <= 1))))
			{
				rebuild(true);
			}
			else if (changedNode != null)
			{
				boolean wasExpanded, wasVisible;
				TreeStateNode parent = (TreeStateNode)changedNode.getParent();

				wasExpanded = changedNode.isExpanded();
				wasVisible = changedNode.isVisible();

				int index = parent.getIndex(changedNode);
				changedNode.collapse(false);
				parent.remove(index);

				if (wasVisible && wasExpanded)
				{
					int row = changedNode.getRow();
					parent.resetChildrenRowsFrom(row, index, changedNode.getChildIndex());
					changedNode = getNodeForPath(changedPath, false, true);
					changedNode.expand();
				}
				if (treeSelectionModel != null && wasVisible && wasExpanded)
				{
					treeSelectionModel.resetRowSelection();
				}
				if (wasVisible)
				{
					this.visibleNodesChanged();
				}
			}
		}
	}

	/**
	 * Adds a mapping for node.
	 * 
	 * @param node
	 *            the node to map
	 */
	private void addMapping(TreeStateNode node)
	{
		treePathMapping.put(node.getTreePath(), node);
	}

	/**
	 * Adjust the large row count.
	 * 
	 * @param change
	 *            the change for the row count
	 */
	private void adjustRowCountBy(int change)
	{
		rowCount += change;
	}

	/**
	 * Creates and returns an instance of TreeStateNode.
	 * 
	 * @param userObject
	 *            the user object
	 * @param childIndex
	 *            the index relative to the parent
	 * @return the tree state node
	 */
	private TreeStateNode createNodeForValue(Object userObject, int childIndex)
	{
		return new TreeStateNode(userObject, childIndex, -1);
	}

	/**
	 * Ensures that all the path components in path are expanded, accept for the
	 * last component which will only be expanded if expandLast is true. Returns
	 * true if succesful in finding the path.
	 * 
	 * @param aPath
	 *            the path
	 * @param expandLast
	 *            whether to expand the last element
	 * @return true if succesful in finding the path
	 */
	private boolean ensurePathIsExpanded(TreePath aPath, boolean expandLast)
	{
		if (aPath != null)
		{
			// Make sure the last entry isn't a leaf.
			if (treeModel.isLeaf(aPath.getLastPathComponent()))
			{
				aPath = aPath.getParentPath();
				expandLast = true;
			}
			if (aPath != null)
			{
				TreeStateNode lastNode = getNodeForPath(aPath, false, true);

				if (lastNode != null)
				{
					lastNode.makeVisible();
					if (expandLast)
					{
						lastNode.expand();
					}
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the node previously added for <code>path</code>. This may
	 * return null, if you to create a node use getNodeForPath.
	 * 
	 * @param path
	 *            the path
	 * @return the state node
	 */
	private TreeStateNode getMapping(TreePath path)
	{
		return treePathMapping.get(path);
	}

	/**
	 * Messages getTreeNodeForPage(path, onlyIfVisible, shouldCreate,
	 * path.length) as long as path is non-null and the length is > 0. Otherwise
	 * returns null.
	 * 
	 * @param path
	 *            the path
	 * @param onlyIfVisible
	 * @param shouldCreate
	 * @return the tree state node
	 */
	private TreeStateNode getNodeForPath(TreePath path, boolean onlyIfVisible, boolean shouldCreate)
	{
		if (path != null)
		{
			TreeStateNode node;

			node = getMapping(path);
			if (node != null)
			{
				if (onlyIfVisible && !node.isVisible())
				{
					return null;
				}
				return node;
			}
			if (onlyIfVisible)
			{
				return null;
			}

			// Check all the parent paths, until a match is found.
			ArrayListStack<TreePath> paths;

			if (tempStacks.size() == 0)
			{
				paths = new ArrayListStack<TreePath>();
			}
			else
			{
				paths = tempStacks.pop();
			}

			try
			{
				paths.push(path);
				path = path.getParentPath();
				while (path != null)
				{
					node = getMapping(path);
					if (node != null)
					{
						// Found a match, create entries for all paths in
						// paths.
						while (node != null && paths.size() > 0)
						{
							path = paths.pop();
							node = node.createChildFor(path.getLastPathComponent());
						}
						return node;
					}
					paths.push(path);
					path = path.getParentPath();
				}
			}
			finally
			{
				paths.clear();
				tempStacks.push(paths);
			}
			// If we get here it means they share a different root!
			return null;
		}
		return null;
	}

	/**
	 * Sent to completely rebuild the visible tree. All nodes are collapsed.
	 * 
	 * @param clearSelection
	 *            whether to clear the selection
	 */
	private void rebuild(boolean clearSelection)
	{
		Object rootUO;

		treePathMapping.clear();
		if (treeModel != null && (rootUO = treeModel.getRoot()) != null)
		{
			root = createNodeForValue(rootUO, 0);
			root.path = new TreePath(rootUO);
			addMapping(root);
			if (isRootVisible())
			{
				rowCount = 1;
				root.row = 0;
			}
			else
			{
				rowCount = 0;
				root.row = -1;
			}
			root.expand();
		}
		else
		{
			root = null;
			rowCount = 0;
		}
		if (clearSelection && treeSelectionModel != null)
		{
			treeSelectionModel.clearSelection();
		}
		this.visibleNodesChanged();
	}

	/**
	 * Removes the mapping for a previously added node.
	 * 
	 * @param node
	 *            the node to remove the mapping for
	 */
	private void removeMapping(TreeStateNode node)
	{
		treePathMapping.remove(node.getTreePath());
	}

	/**
	 * Called when the visibility of nodes changed.
	 */
	private void visibleNodesChanged()
	{
	}
}