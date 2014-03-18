/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.util.iterator;

import java.util.Iterator;

import org.apache.wicket.util.collections.ArrayListStack;
import org.apache.wicket.util.lang.Args;

/**
 * This is a basic iterator for hierarchical structures such as Component hierarchies or HTML
 * markup. It supports child first and parent first traversal and intercepts while moving down or up
 * the hierarchy.
 * <p>
 * It assumes the container class implements <code>Iterable</code>. The leaf nodes don't need to.
 * <p>
 * Consecutive calls to <code>hasNext()</code> without <code>next()</code> in between, don't move
 * the cursor, but return the same value until <code>next()</code> is called.
 * <p>
 * Every call to <code>next()</code>, with or without <code>hasNext()</code>, will move the cursor
 * to the next element.
 * 
 * @TODO Replace ChildFirst with a strategy
 * 
 * @author Juergen Donnerstag
 * @param <I>
 *            The type relevant for the iterator. What you expect to get back from next(), e.g.
 *            Form.
 * @param <N>
 *            The base type of all nodes, e.g. Component
 * @deprecated Hierarchy iterators are deprecated because they have problems with pages with
 *      deep component tree. Use {@link org.apache.wicket.util.visit.IVisitor} instead.
 * @see org.apache.wicket.MarkupContainer#visitChildren(org.apache.wicket.util.visit.IVisitor)
 * @see org.apache.wicket.MarkupContainer#visitChildren(Class, org.apache.wicket.util.visit.IVisitor)
 */
@Deprecated
public abstract class AbstractHierarchyIterator<N, I extends N> implements Iterator<I>, Iterable<I>
{
	// An iterator for each level we are down from root
	private ArrayListStack<LevelIterator<N>> stack = new ArrayListStack<>();

	// The current level iterator
	private LevelIterator<N> data;

	// Whether we need to traverse into the next level with the next invocation of hasNext()
	private boolean traverse;

	// An easy way to configure child or parent first iterations
	private boolean childFirst;

	// If remaining siblings shall be ignored
	private boolean skipRemainingSiblings;

	// True, if hasNext() was called last or next()
	private boolean hasNextWasLast;

	/**
	 * Construct.
	 * 
	 * @param root
	 */
	public AbstractHierarchyIterator(final N root)
	{
		Args.notNull(root, "root");

		if (hasChildren(root))
		{
			data = new LevelIterator<>(root, newIterator(root));
		}
	}

	/**
	 * 
	 * @param childFirst
	 *            If true, than children are visited before their parent is.
	 */
	public final void setChildFirst(final boolean childFirst)
	{
		this.childFirst = childFirst;
	}

	/**
	 * 
	 * @param node
	 * @return True, if node is a container and has at least one child.
	 */
	abstract protected boolean hasChildren(final N node);

	/**
	 * If node is a container than return an iterator for its children.
	 * <p>
	 * Gets only called if {@link #hasChildren(Object)} return true.
	 * 
	 * @param node
	 * @return container iterator
	 */
	abstract protected Iterator<N> newIterator(final N node);

	@Override
	public boolean hasNext()
	{
		// Did we reach the end already?
		if (data == null)
		{
			return false;
		}

		// We did not yet reach the end. Has next() been called already?
		if (hasNextWasLast == true)
		{
			// next() has not yet been called.
			return true;
		}

		// Remember that the last call was a hasNext()
		hasNextWasLast = true;

		//
		if (skipRemainingSiblings == true)
		{
			skipRemainingSiblings = false;
			return moveUp();
		}

		// Do we need to traverse into the next level?
		if (!childFirst && traverse)
		{
			// Try to find the next element
			if (moveDown(data.lastNode) == false)
			{
				// No more elements
				return false;
			}

			// Found the next element in one the next levels
			hasNextWasLast = true;
			return true;
		}

		// Get the next node on the current level. If it's a container, than move downwards. If
		// there are no more elements, than move up again.
		return nextNode();
	}

	/**
	 * Move down into the next level
	 * 
	 * @param node
	 * @return False if no more elements were found
	 */
	private boolean moveDown(final N node)
	{
		// Remember all details of the current level
		stack.push(data);

		// Initialize the data for the next level
		data = new LevelIterator<>(node, newIterator(node));

		// Get the next node on the current level. If it's a container, than move downwards. If
		// there are no more elements, than move up again.
		return nextNode();
	}

	/**
	 * Gets called for each element within the hierarchy (nodes and leafs)
	 * 
	 * @param node
	 * @return if false, than skip (filter) the element. It'll not stop the iterator from traversing
	 *         into children though.
	 */
	protected boolean onFilter(final N node)
	{
		return true;
	}

	/**
	 * Gets called for each element where {@link #hasChildren(Object)} return true.
	 * 
	 * @param node
	 * @return if false, than do not traverse into the children and grand-children.
	 */
	protected boolean onTraversalFilter(final N node)
	{
		return true;
	}

	/**
	 * Get the next node from the underlying iterator and handle it.
	 * 
	 * @return true, if one more element was found
	 */
	private boolean nextNode()
	{
		// Get the next element
		while (data.hasNext())
		{
			data.lastNode = data.next();

			// Does it have children?
			traverse = hasChildren(data.lastNode);
			if (traverse)
			{
				traverse = onTraversalFilter(data.lastNode);
			}

			// If it does and we do childFirst, than try to find the next child
			if (childFirst && traverse)
			{
				if (moveDown(data.lastNode) == false)
				{
					// No more elements
					return false;
				}
			}

			// The user interested in the node?
			if (onFilter(data.lastNode))
			{
				// Yes
				return true;
			}

			// If we are parent first but the user is not interested in the current node, than move
			// down.
			if (!childFirst && traverse)
			{
				if (moveDown(data.lastNode) == false)
				{
					// No more elements
					return false;
				}

				if (data == null)
				{
					return false;
				}

				hasNextWasLast = true;
				return true;
			}

			if (skipRemainingSiblings == true)
			{
				skipRemainingSiblings = false;
				break;
			}
		}

		// Nothing found. Move up and try to find the next element there
		return moveUp();
	}

	/**
	 * Move up until we found the next element
	 * 
	 * @return false, if no more elements are found
	 */
	private boolean moveUp()
	{
		if (data == null)
		{
			return false;
		}

		// Are we back at the root node?
		if (stack.isEmpty())
		{
			data = null;
			return false;
		}

		// Move up one level
		data = stack.pop();

		// If we are on childFirst, then it's now time to handle the parent
		if (childFirst)
		{
			hasNextWasLast = true;
			if (onFilter(data.lastNode) == true)
			{
				return true;
			}
			return nextNode();
		}
		// If we are on parent first, then get the next element
		else if (data.hasNext())
		{
			return nextNode();
		}
		else
		{
			// No more elements on the current level. Move up.
			return moveUp();
		}
	}

	/**
	 * Traverse the hierarchy and get the next element
	 */
	@Override
	@SuppressWarnings("unchecked")
	public I next()
	{
		// Did we reach the end already?
		if (data == null)
		{
			return null;
		}

		// hasNext() is responsible to get the next element
		if (hasNextWasLast == false)
		{
			if (hasNext() == false)
			{
				// No more elements
				return null;
			}
		}

		// Remember that we need to call hasNext() to get the next element
		hasNextWasLast = false;

		return (I)data.lastNode;
	}

	@Override
	public void remove()
	{
		if (data == null)
		{
			throw new IllegalStateException("Already reached the end of the iterator.");
		}

		data.remove();
	}

	/**
	 * Skip all remaining siblings and return to the parent node.
	 * <p>
	 * Can as well be called within {@link IteratorFilter#onFilter(Object)}.
	 */
	public void skipRemainingSiblings()
	{
		skipRemainingSiblings = true;
		traverse = false;
	}

	/**
	 * Assuming we are currently at a container, than ignore all its children and grand-children and
	 * continue with the next sibling.
	 * <p>
	 * Can as well be called within {@link IteratorFilter#onFilter(Object)}.
	 */
	public void dontGoDeeper()
	{
		traverse = false;
	}

	@Override
	public final Iterator<I> iterator()
	{
		return this;
	}

	@Override
	public String toString()
	{
		StringBuilder msg = new StringBuilder(500);
		msg.append("traverse=")
			.append(traverse)
			.append("; childFirst=")
			.append(childFirst)
			.append("; hasNextWasLast=")
			.append(hasNextWasLast)
			.append('\n');

		msg.append("data.node=")
			.append(data.node)
			.append('\n')
			.append("data.lastNode=")
			.append(data.lastNode)
			.append('\n');

		msg.append("stack.size=").append(stack.size());

		return msg.toString();
	}

	/**
	 * We need a little helper to store the iterator of the level and the last node returned by
	 * next().
	 * 
	 * @param <N>
	 */
	private static class LevelIterator<N> implements Iterator<N>
	{
		private final N node;

		private final Iterator<N> iter;

		private N lastNode;

		/**
		 * Construct.
		 * 
		 * @param node
		 *            For debugging purposes only
		 * @param iter
		 *            The iterator for 'node'
		 */
		public LevelIterator(final N node, final Iterator<N> iter)
		{
			Args.notNull(iter, "iter");

			this.node = node;
			this.iter = iter;
		}

		@Override
		public boolean hasNext()
		{
			return iter.hasNext();
		}

		@Override
		public N next()
		{
			lastNode = iter.next();
			return lastNode;
		}

		@Override
		public void remove()
		{
			iter.remove();
		}

		@Override
		public String toString()
		{
			StringBuilder msg = new StringBuilder(500);
			msg.append("node=")
				.append(node)
				.append('\n')
				.append("lastNode=")
				.append(lastNode)
				.append('\n');

			return msg.toString();
		}
	}
}
