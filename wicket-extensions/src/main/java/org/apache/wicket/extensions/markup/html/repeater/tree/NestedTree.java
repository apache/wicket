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
package org.apache.wicket.extensions.markup.html.repeater.tree;

import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.nested.BranchItem;
import org.apache.wicket.extensions.markup.html.repeater.tree.nested.Subtree;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * A tree with nested markup.
 * 
 * @author svenmeier
 * @param <T>
 *            the model object type
 */
public abstract class NestedTree<T> extends AbstractTree<T>
{

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 * @param provider
	 *            the provider of the tree
	 */
	public NestedTree(String id, ITreeProvider<T> provider)
	{
		this(id, provider, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 * @param provider
	 *            the provider of the tree
	 * @param state
	 *            the expansion state
	 * 
	 * @see State
	 */
	public NestedTree(String id, ITreeProvider<T> provider, IModel<Set<T>> state)
	{
		super(id, provider, state);

		add(newSubtree("subtree", new RootsModel()));
	}

	/**
	 * Create a new subtree.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            the model of the new subtree
	 * @return the created component
	 */
	public Component newSubtree(String id, IModel<T> model)
	{
		return new Subtree<T>(id, this, model);
	}

	/**
	 * Overriden to let the node output its markup id.
	 * 
	 * @see #updateNode(Object, AjaxRequestTarget)
	 * @see Component#setOutputMarkupId(boolean)
	 */
	@Override
	public Component newNodeComponent(String id, IModel<T> model)
	{
		Component node = super.newNodeComponent(id, model);

		node.setOutputMarkupId(true);

		return node;
	}

	/**
	 * Overridden to update the corresponding {@link BranchItem} only.
	 */
	@Override
	public void updateBranch(T t, final AjaxRequestTarget target)
	{
		if (target != null)
		{
			final IModel<T> model = getProvider().model(t);
			visitChildren(BranchItem.class, new IVisitor<BranchItem<T>, Void>()
			{
				@Override
				public void component(BranchItem<T> branch, IVisit<Void> visit)
				{
					if (model.equals(branch.getModel()))
					{
						// BranchItem always outputs its markupId
						target.add(branch);
						visit.stop();
					}
				}
			});
			model.detach();
		}
	}

	/**
	 * Overridden to update the corresponding {@link Node} only.
	 */
	@Override
	public void updateNode(T node, final AjaxRequestTarget target)
	{
		if (target != null)
		{
			final IModel<T> model = getProvider().model(node);
			visitChildren(Node.class, new IVisitor<Node<T>, Void>()
			{
				@Override
				public void component(Node<T> node, IVisit<Void> visit)
				{
					if (model.equals(node.getModel()))
					{
						// nodes are configured to output their markup id, see #newNodeComponent()
						target.add(node);
						visit.stop();
					}
					visit.dontGoDeeper();
				}
			});
			model.detach();
		}
	}

	private class RootsModel extends AbstractReadOnlyModel<T>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public T getObject()
		{
			return null;
		}
	}
}
