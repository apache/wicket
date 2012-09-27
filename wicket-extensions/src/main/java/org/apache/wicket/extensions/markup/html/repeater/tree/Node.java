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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree.State;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Representation of a single node in the tree. By default uses an {@link AjaxFallbackLink} for its
 * junction component.
 * 
 * @see #createJunctionComponent(String)
 * 
 * @author svenmeier
 * @param <T>
 *            The model object type
 */
public abstract class Node<T> extends Panel
{

	private static final long serialVersionUID = 1L;

	/**
	 * The component id for the content component.
	 */
	public static final String CONTENT_ID = "content";

	private AbstractTree<T> tree;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            component id
	 * @param tree
	 *            the owning tree
	 * @param model
	 *            the model for this node
	 */
	public Node(String id, AbstractTree<T> tree, IModel<T> model)
	{
		super(id, model);

		this.tree = tree;

		MarkupContainer junction = createJunctionComponent("junction");
		junction.add(new StyleBehavior());
		add(junction);

		Component content = createContent(CONTENT_ID, model);
		if (!content.getId().equals(CONTENT_ID))
		{
			throw new IllegalArgumentException(
				"content must have component id equal to Node.CONTENT_ID");
		}
		add(content);
	}

	/**
	 * @return the model
	 */
	@SuppressWarnings("unchecked")
	public IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	/**
	 * @return the model object
	 */
	public T getModelObject()
	{
		return getModel().getObject();
	}

	/**
	 * The junction component expands and collapses this node.
	 * 
	 * @param id
	 *            the component id
	 * @return component representing the junction
	 */
	protected MarkupContainer createJunctionComponent(String id)
	{
		return new AjaxFallbackLink<Void>(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				toggle();
			}

			@Override
			public boolean isEnabled()
			{
				return tree.getProvider().hasChildren(Node.this.getModelObject());
			}
		};
	}

	/**
	 * Toggle the node.
	 * 
	 * @see AbstractTree#collapse(Object)
	 * @see AbstractTree#expand(Object)
	 */
	protected void toggle()
	{
		T t = getModelObject();

		if (tree.getState(t) == State.EXPANDED)
		{
			tree.collapse(t);
		}
		else
		{
			tree.expand(t);
		}
	}

	/**
	 * Create the component to display the actual node's content.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the node's model
	 * @return the component representing the content
	 */
	protected abstract Component createContent(String id, IModel<T> model);

	/**
	 * Get the style class depending on the current {@link State} of this node.
	 * 
	 * @see #getExpandedStyleClass(Object)
	 * @see #getCollapsedStyleClass()
	 * @see #getOtherStyleClass()
	 * @return the style class
	 */
	protected String getStyleClass()
	{
		T t = getModelObject();

		if (tree.getProvider().hasChildren(t))
		{
			if (tree.getState(t) == State.EXPANDED)
			{
				return getExpandedStyleClass(t);
			}
			else
			{
				return getCollapsedStyleClass();
			}
		}
		return getOtherStyleClass();
	}

	protected String getExpandedStyleClass(T t)
	{
		return "tree-junction-expanded";
	}

	protected String getCollapsedStyleClass()
	{
		return "tree-junction-collapsed";
	}

	protected String getOtherStyleClass()
	{
		return "tree-junction";
	}

	/**
	 * Behavior to add the style class attribute.
	 * 
	 * @see Node#getStyleClass()
	 */
	private static class StyleBehavior extends Behavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			Node<?> node = (Node<?>)component.getParent();

			String styleClass = node.getStyleClass();
			if (styleClass != null)
			{
				tag.put("class", styleClass);
			}
		}
	}
}
