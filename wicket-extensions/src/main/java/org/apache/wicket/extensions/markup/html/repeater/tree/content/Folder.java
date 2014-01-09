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
package org.apache.wicket.extensions.markup.html.repeater.tree.content;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.util.string.CssUtils;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree.State;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;

/**
 * A typical folder representation of nodes in a tree.
 * 
 * The link is used to expand/collapse the tree depending on the {@link State} of the current node.
 * Nodes without children are not clickable. Subclasses may change this behavior by overriding
 * {@link #isClickable()} and {@link #onClick(AjaxRequestTarget)}.
 * 
 * @author svenmeier
 */
public class Folder<T> extends StyledLinkLabel<T>
{

	private static final long serialVersionUID = 1L;

	public static final String OTHER_CSS_CLASS_KEY = CssUtils.key(Folder.class, "other");

	public static final String CLOSED_CSS_CLASS_KEY = CssUtils.key(Folder.class, "closed");

	public static final String OPEN_CSS_CLASS_KEY = CssUtils.key(Folder.class, "open");

	public static final String SELECTED_CSS_CLASS_KEY = CssUtils.key(Folder.class, "selected");

	private AbstractTree<T> tree;

	public Folder(String id, AbstractTree<T> tree, IModel<T> model)
	{
		super(id, model);

		this.tree = tree;
	}

	/**
	 * Clickable if node can be expanded/collapsed, i.e. has children.
	 * 
	 * @see ITreeProvider#hasChildren(Object)
	 */
	@Override
	protected boolean isClickable()
	{
		T t = getModelObject();

		return tree.getProvider().hasChildren(t);
	}

	/**
	 * Toggle the node's {@link State} on click.
	 */
	@Override
	protected void onClick(AjaxRequestTarget target)
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
	 * Delegates to others methods depending wether the given model is a folder, expanded, collapsed
	 * or selected.
	 * 
	 * @see ITreeProvider#hasChildren(Object)
	 * @see AbstractTree#getState(Object)
	 * @see #isSelected()
	 * @see #getOpenStyleClass()
	 * @see #getClosedStyleClass()
	 * @see #getOtherStyleClass(Object)
	 * @see #getSelectedStyleClass()
	 */
	@Override
	protected String getStyleClass()
	{
		T t = getModelObject();

		String styleClass;
		if (tree.getProvider().hasChildren(t))
		{
			if (tree.getState(t) == State.EXPANDED)
			{
				styleClass = getOpenStyleClass();
			}
			else
			{
				styleClass = getClosedStyleClass();
			}
		}
		else
		{
			styleClass = getOtherStyleClass(t);
		}

		if (isSelected())
		{
			styleClass += " " + getSelectedStyleClass();
		}

		return styleClass;
	}

	/**
	 * Optional attribute which decides if an additional "selected" style class should be rendered.
	 * 
	 * @return defaults to <code>false</code>
	 */
	protected boolean isSelected()
	{
		return false;
	}

	/**
	 * Get a style class for nodes without children.
	 * 
	 * @param t
	 *            node
	 * @return CSS style class
	 * 
	 * @see ITreeProvider#hasChildren(Object)
	 */
	protected String getOtherStyleClass(T t)
	{
		return getString(OTHER_CSS_CLASS_KEY);
	}

	/**
	 * Get a style class for anything other than closed or open folders.
	 * 
	 * @return CSS style class
	 * 
	 * @see State#CLOSED
	 */
	protected String getClosedStyleClass()
	{
		return getString(CLOSED_CSS_CLASS_KEY);
	}

	/**
	 * Get a style class for anything other than closed or open folders.
	 * 
	 * @return CSS style class
	 * 
	 * @see State#OPEN
	 */
	protected String getOpenStyleClass()
	{
		return getString(OPEN_CSS_CLASS_KEY);
	}

	/**
	 * Get a style class to render for a selected folder.
	 * 
	 * @return CSS style class
	 * 
	 * @see #isSelected()
	 */
	protected String getSelectedStyleClass()
	{
		return getString(SELECTED_CSS_CLASS_KEY);
	}
}