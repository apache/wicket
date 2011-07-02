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
 * This is a basic iterator for hierarchical structure such as Component hierarchies or HTML markup.
 * It supports child first and parent first traversal and intercepts while moving down or up the
 * hierarchy.
 * <p>
 * It assume the container class implements <code>Iterable</code>. The leaf nodes don't need to.
 * <p>
 * Consecutive calls to hasNext() without next() in between, does not move the cursor, but returns
 * the same value until next() is called.
 * <p>
 * Every call to next(), with or without hasNext(), will move the cursor to the next element.
 * 
 * @TODO Replace ChildFirst with a strategy
 * 
 * @author Juergen Donnerstag
 * @param <S>
 *            E.g. Component
 * @param <T>
 *            E.g. MarkupContainer
 */
public abstract class AbstractHierarchyIterator<S> implements Iterator<S>, Iterable<S>
{
	// An iterator for each level we are down from root
	private ArrayListStack<LevelIterator<S>> stack = new ArrayListStack<LevelIterator<S>>();

	// The current level iterator
	private LevelIterator<S> data;

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
	public AbstractHierarchyIterator(final S root)
	{
		Args.notNull(root, "root");

		if (hasChildren(root))
		{
			data = new LevelIterator<S>(root, newIterator(root));
		}
	}

	/**
	 * 
	 * @param elem
	 * @return True, if elem is a container and has at least one child.
	 */
	abstract protected boolean hasChildren(final S elem);

	/**
	 * If node is a container than return an iterator for its children.
	 * <p>
	 * Gets only called if {@link #hasChildren()} return true.
	 * 
	 * @param node
	 * @return container iterator
	 */
	abstract protected Iterator<S> newIterator(final S node);

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
		if (traverse)
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
	protected boolean moveDown(final S node)
	{
		// Remember all details of the current level
		stack.push(data);

		// Initialize the data for the next level
		data = new LevelIterator<S>(node, newIterator(node));

		// Get the next node on the current level. If it's a container, than move downwards. If
		// there are no more elements, than move up again.
		return nextNode();
	}

	/**
	 * 
	 * @param node
	 * @return if false, than skip (filter) the node
	 */
	protected boolean onFilter(final S node)
	{
		return true;
	}

	/**
	 * 
	 * @param node
	 * @return if false, than skip (filter) the node
	 */
	protected boolean onTraversalFilter(final S node)
	{
		return true;
	}

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

				// We did traverse the children already
				traverse = false;
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

		// If we are on childFirst, than it's now time to handle the parent
		if (traverse)
		{
			hasNextWasLast = true;
			if (onFilter(data.lastNode) == true)
			{
				return true;
			}
			return nextNode();
		}
		// If we are on parent first, than get the next element
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
	public S next()
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

		return data.lastNode;
	}

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

	public final Iterator<S> iterator()
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
			.append("\n");

		msg.append("data.node=")
			.append(data.node)
			.append("\n")
			.append("data.lastNode=")
			.append(data.lastNode)
			.append("\n");

		msg.append("stack.size=").append(stack.size());

		return msg.toString();
	}

	/**
	 * We need a little helper to store the iterator of the level and the last node returned by
	 * next().
	 * 
	 * @param <S>
	 */
	private static class LevelIterator<S> implements Iterator<S>
	{
		private final S node;

		private final Iterator<S> iter;

		private S lastNode;

		/**
		 * Construct.
		 * 
		 * @param node
		 *            For debugging purposes only
		 * @param iter
		 *            The iterator for 'node'
		 */
		public LevelIterator(final S node, final Iterator<S> iter)
		{
			Args.notNull(iter, "iter");

			this.node = node;
			this.iter = iter;
		}

		public boolean hasNext()
		{
			return iter.hasNext();
		}

		public S next()
		{
			lastNode = iter.next();
			return lastNode;
		}

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
				.append("\n")
				.append("lastNode=")
				.append(lastNode)
				.append("\n");

			return msg.toString();
		}
	}
}
