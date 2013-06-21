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
package org.apache.wicket.extensions.markup.html.repeater.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;

/**
 * A provider wrapping a Swing {@link TreeModel}.
 * 
 * EXPERIMENTAL !
 * 
 * @author svenmeier
 * @param <T>
 *            model object type
 */
public abstract class TreeModelProvider<T> implements ITreeProvider<T>
{

	private static final long serialVersionUID = 1L;

	private final TreeModel treeModel;

	private final boolean rootVisible;

	protected boolean completeUpdate;

	protected List<T> nodeUpdates;

	protected List<T> branchUpdates;

	/**
	 * Wrap the given {@link TreeModel}.
	 * 
	 * @param treeModel
	 *            model to wrap
	 */
	public TreeModelProvider(TreeModel treeModel)
	{
		this(treeModel, true);
	}

	/**
	 * Wrap the given {@link TreeModel}.
	 * 
	 * @param treeModel
	 *            the wrapped model
	 * @param rootVisible
	 *            should the root be visible
	 */
	public TreeModelProvider(TreeModel treeModel, boolean rootVisible)
	{
		this.treeModel = treeModel;
		this.rootVisible = rootVisible;

		treeModel.addTreeModelListener(new Listener());
	}

	@Override
	public Iterator<T> getRoots()
	{
		if (rootVisible)
		{
			return new Iterator<T>()
			{
				boolean next = true;

				@Override
				public boolean hasNext()
				{
					return next;
				}

				@Override
				public T next()
				{
					next = false;
					return cast(treeModel.getRoot());
				}

				@Override
				public void remove()
				{
					throw new UnsupportedOperationException();
				}
			};
		}
		else
		{
			return getChildren(cast(treeModel.getRoot()));
		}
	}

	@Override
	public boolean hasChildren(T object)
	{
		return !treeModel.isLeaf(object);
	}

	@Override
	public Iterator<T> getChildren(final T object)
	{
		return new Iterator<T>()
		{
			private int size = treeModel.getChildCount(object);
			private int index = -1;

			@Override
			public boolean hasNext()
			{
				return index < size - 1;
			}

			@Override
			public T next()
			{
				index++;
				return cast(treeModel.getChild(object, index));
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@SuppressWarnings("unchecked")
	protected T cast(Object object)
	{
		return (T)object;
	}

	@Override
	public abstract IModel<T> model(T object);

	@Override
	public void detach()
	{
		completeUpdate = false;
		nodeUpdates = null;
		branchUpdates = null;
	}

	/**
	 * Call this method after all change to the wrapped {@link TreeModel} being initiated via
	 * {@link AjaxRequestTarget}.
	 * 
	 * @param tree
	 *            the tree utilizing this {@link ITreeProvider}
	 * @param target
	 *            the {@link AjaxRequestTarget} which initiated the changes
	 */
	public void update(AbstractTree<T> tree, AjaxRequestTarget target)
	{
		if (completeUpdate)
		{
			target.add(tree);
		}
		else
		{
			if (nodeUpdates != null)
			{
				for (T object : nodeUpdates)
				{
					tree.updateNode(object, target);
				}
			}

			if (branchUpdates != null)
			{
				for (T object : branchUpdates)
				{
					tree.updateBranch(object, target);
				}
			}
		}

		detach();
	}

	protected void nodeUpdate(Object[] nodes)
	{
		if (nodeUpdates == null)
		{
			nodeUpdates = new ArrayList<>();
		}

		for (Object node : nodes)
		{
			nodeUpdates.add(cast(node));
		}
	}

	protected void branchUpdate(Object branch)
	{
		if (branchUpdates == null)
		{
			branchUpdates = new ArrayList<>();
		}

		branchUpdates.add(cast(branch));
	}

	private class Listener implements TreeModelListener, Serializable
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void treeNodesChanged(TreeModelEvent e)
		{
			if (e.getChildIndices() == null)
			{
				completeUpdate = true;
			}
			else
			{
				nodeUpdate(e.getChildren());
			}
		}

		@Override
		public void treeNodesInserted(TreeModelEvent e)
		{
			branchUpdate(e.getTreePath().getLastPathComponent());
		}

		@Override
		public void treeNodesRemoved(TreeModelEvent e)
		{
			branchUpdate(e.getTreePath().getLastPathComponent());
		}

		@Override
		public void treeStructureChanged(TreeModelEvent e)
		{
			if (e.getTreePath().getPathCount() == 1)
			{
				completeUpdate = true;
			}
			else
			{
				branchUpdate(e.getTreePath().getLastPathComponent());
			}
		}
	}
}
