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
package org.apache.wicket.extensions.markup.html.repeater.tree.table;

import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

/**
 * An adapter of a {@link ITreeProvider} to a {@link IDataProvider}.
 * 
 * @author svenmeier
 * @param <T>
 *            node type
 */
public abstract class TreeDataProvider<T> implements ITreeDataProvider<T>
{
	private static final long serialVersionUID = 1L;

	private final ITreeProvider<T> provider;

	private Branch<T> currentBranch;

	private Branch<T> previousBranch;

	private int size = -1;

	/**
	 * Construct.
	 * 
	 * @param provider
	 *            the provider to adapt
	 */
	public TreeDataProvider(ITreeProvider<T> provider)
	{
		this.provider = provider;
	}

	public long size()
	{
		if (size == -1)
		{
			size = 0;

			Iterator<? extends T> iterator = iterator(0, Integer.MAX_VALUE);
			while (iterator.hasNext())
			{
				iterator.next();

				size++;
			}
		}
		return size;
	}

	public Iterator<? extends T> iterator(long first, long count)
	{
		currentBranch = new Branch<T>(null, provider.getRoots());

		Iterator<T> iterator = new Iterator<T>()
		{

			public boolean hasNext()
			{
				while (currentBranch != null)
				{
					if (currentBranch.hasNext())
					{
						return true;
					}
					currentBranch = currentBranch.parent;
				}

				return false;
			}

			public T next()
			{
				if (!hasNext())
				{
					throw new IllegalStateException();
				}

				T next = currentBranch.next();

				previousBranch = currentBranch;

				if (iterateChildren(next))
				{
					currentBranch = new Branch<T>(previousBranch, provider.getChildren(next));
				}

				return next;
			}

			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};

		for (int i = 0; i < first; i++)
		{
			iterator.next();
		}

		return iterator;
	}

	/**
	 * Hook method to decide wether the given node's children should be iterated.
	 * 
	 * @param node
	 *            node
	 * 
	 * @return {@code true} if the node's children should be iterated
	 */
	protected abstract boolean iterateChildren(T node);

	public NodeModel<T> model(T object)
	{
		return previousBranch.wrapModel(provider.model(object));
	}

	public void detach()
	{
		currentBranch = null;
		previousBranch = null;
		size = -1;
	}

	private static class Branch<T> implements Iterator<T>
	{
		private Branch<T> parent;

		private Iterator<? extends T> children;

		public Branch(Branch<T> parent, Iterator<? extends T> children)
		{
			this.parent = parent;
			this.children = children;
		}

		public NodeModel<T> wrapModel(IModel<T> model)
		{
			boolean[] branches = new boolean[getDepth()];

			Branch<T> branch = this;
			for (int c = branches.length - 1; c >= 0; c--)
			{
				branches[c] = branch.hasNext();

				branch = branch.parent;
			}

			return new NodeModel<T>(model, branches);
		}

		public int getDepth()
		{
			if (parent == null)
			{
				return 1;
			}
			else
			{
				return parent.getDepth() + 1;
			}
		}

		public boolean hasNext()
		{
			return children.hasNext();
		}

		public T next()
		{
			if (!hasNext())
			{
				throw new IllegalStateException();
			}

			return children.next();
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}