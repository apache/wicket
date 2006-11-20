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
package wicket.extensions.markup.html.tree;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.behavior.AbstractBehavior;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;

/**
 * A complete tree implementation where three item consists of junction link,
 * icon and label.
 * 
 * @author Matej Knopp
 */
public class Tree extends DefaultAbstractTree
{
	private static final long serialVersionUID = 1L;

	/**
	 * Tree constructor.
	 * 
	 * @param id
	 *            The component id
	 */
	public Tree(String id)
	{
		super(id);
	}

	/**
	 * Tree constructor.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 */
	public Tree(String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * Tree constructor.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 */
	public Tree(String id, TreeModel model)
	{
		super(id, model);
	}

	/**
	 * Populates the tree item. It creates all necesary components for the tree
	 * to work properly.
	 * 
	 * @param item
	 * @param level
	 */
	protected void populateTreeItem(WebMarkupContainer item, int level)
	{
		final TreeNode node = (TreeNode)item.getModelObject();

		item.add(newIndentation(item, "indent", (TreeNode)item.getModelObject(), level));

		item.add(newJunctionLink(item, "link", "image", node));

		MarkupContainer nodeLink = newNodeLink(item, "nodeLink", node);
		item.add(nodeLink);

		nodeLink.add(newNodeIcon(nodeLink, "icon", node));

		nodeLink.add(new Label("label", new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component c)
			{
				return renderNode(node);
			}
		}));

		// do distinguish between selected and unselected rows we add an
		// behavior
		// that modifies row css class.
		item.add(new AbstractBehavior()
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see wicket.behavior.AbstractBehavior#onComponentTag(wicket.Component,
			 *      wicket.markup.ComponentTag)
			 */
			public void onComponentTag(Component component, ComponentTag tag)
			{
				super.onComponentTag(component, tag);
				if (getTreeState().isNodeSelected(node))
				{
					tag.put("class", "row-selected");
				}
				else
				{
					tag.put("class", "row");
				}
			}
		});
	}

	/**
	 * This method is called for every node to get it's string representation.
	 * 
	 * @param node
	 *            The tree node to get the string representation of
	 * @return The string representation
	 */
	protected String renderNode(TreeNode node)
	{
		return node.toString();
	}
}
