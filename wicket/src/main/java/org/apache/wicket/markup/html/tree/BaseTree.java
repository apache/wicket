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
package org.apache.wicket.markup.html.tree;

import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.apache.wicket.IComponentBorder;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.EnumeratedType;
import org.apache.wicket.util.string.Strings;

/**
 * An abstract Tree component that should serve as a base for custom Tree Components.
 * 
 * It has one abstract method - {@link #newNodeComponent(String, IModel)} that needs to be
 * overridden.
 * 
 * @author Matej Knopp
 */
public abstract class BaseTree extends AbstractTree
{
	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public BaseTree(String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public BaseTree(String id, IModel model)
	{
		super(id, model);

		ResourceReference css = getCSS();
		if (css != null)
		{
			add(HeaderContributor.forCss(css));
		}
	}

	// default stylesheet resource
	private static final ResourceReference CSS = new CompressedResourceReference(BaseTree.class,
		"res/base-tree.css");

	/**
	 * Returns the stylesheet reference
	 * 
	 * @return stylesheet reference
	 */
	protected ResourceReference getCSS()
	{
		return CSS;
	}

	private static final long serialVersionUID = 1L;

	private static final String JUNCTION_LINK_ID = "junctionLink";
	private static final String NODE_COMPONENT_ID = "nodeComponent";

	/**
	 * @see org.apache.wicket.markup.html.tree.AbstractTree#populateTreeItem(org.apache.wicket.markup.html.WebMarkupContainer,
	 *      int)
	 */
	protected void populateTreeItem(WebMarkupContainer item, int level)
	{
		// add junction link
		TreeNode node = (TreeNode)item.getModelObject();
		Component junctionLink = newJunctionLink(item, JUNCTION_LINK_ID, node);
		junctionLink.setComponentBorder(new JunctionBorder(node, level));
		item.add(junctionLink);

		// add node component
		Component nodeComponent = newNodeComponent(NODE_COMPONENT_ID, item.getModel());
		item.add(nodeComponent);

		// add behavior that conditionally adds the "selected" CSS class name
		item.add(new AbstractBehavior()
		{
			private static final long serialVersionUID = 1L;

			public void onComponentTag(Component component, ComponentTag tag)
			{
				TreeNode node = (TreeNode)component.getModelObject();
				if (getTreeState().isNodeSelected(node))
				{
					CharSequence oldClass = tag.getString("class");
					if (Strings.isEmpty(oldClass))
					{
						tag.put("class", getSelectedClass());
					}
					else
					{
						tag.put("class", oldClass + " " + getSelectedClass());
					}
				}
			}
		});
	}

	/**
	 * Returns the class name that will be added to row's CSS class for selected rows
	 * 
	 * @return CSS class name
	 */
	protected String getSelectedClass()
	{
		return "selected";
	}

	/**
	 * Creates a new component for the given TreeNode.
	 * 
	 * @param id
	 *            component ID
	 * @param model
	 *            model that returns the node
	 * @return component for node
	 */
	protected abstract Component newNodeComponent(String id, IModel model);

	/**
	 * Returns whether the provided node is last child of it's parent.
	 * 
	 * @param node
	 *            The node
	 * @return whether the provided node is the last child
	 */
	private static boolean isNodeLast(TreeNode node)
	{
		TreeNode parent = node.getParent();
		if (parent == null)
		{
			return true;
		}
		else
		{
			return parent.getChildAt(parent.getChildCount() - 1).equals(node);
		}
	}

	/**
	 * Class that wraps a link (or span) with a junction table cells.
	 * 
	 * @author Matej Knopp
	 */
	private static class JunctionBorder implements IComponentBorder
	{
		private static final long serialVersionUID = 1L;

		private final TreeNode node;
		private final int level;

		/**
		 * Construct.
		 * 
		 * @param node
		 * @param level
		 */
		public JunctionBorder(TreeNode node, int level)
		{
			this.node = node;
			this.level = level;
		}

		public void renderAfter(Component component)
		{
			RequestCycle.get().getResponse().write("</td>");
		}

		public void renderBefore(Component component)
		{
			Response response = RequestCycle.get().getResponse();
			TreeNode parent = node.getParent();

			CharSequence classes[] = new CharSequence[level];
			for (int i = 0; i < level; ++i)
			{
				if (parent == null || isNodeLast(parent))
				{
					classes[i] = "spacer";
				}
				else
				{
					classes[i] = "line";
				}

				parent = parent.getParent();
			}

			for (int i = level - 1; i >= 0; --i)
			{
				response.write("<td class=\"" + classes[i] + "\"><span></span></td>");
			}

			if (isNodeLast(node))
			{
				response.write("<td class=\"half-line\">");
			}
			else
			{
				response.write("<td class=\"line\">");
			}
		}
	};

	/**
	 * Creates the junction link for given node. Also (optionally) creates the junction image. If
	 * the node is a leaf (it has no children), the created junction link is non-functional.
	 * 
	 * @param parent
	 *            parent component of the link
	 * @param id
	 *            wicket:id of the component
	 * @param node
	 *            tree node for which the link should be created.
	 * @return The link component
	 */
	protected Component newJunctionLink(MarkupContainer parent, final String id, final TreeNode node)
	{
		final MarkupContainer junctionLink;

		if (node.isLeaf() == false)
		{
			junctionLink = newLink(id, new ILinkCallback()
			{
				private static final long serialVersionUID = 1L;

				public void onClick(AjaxRequestTarget target)
				{
					if (isNodeExpanded(node))
					{
						getTreeState().collapseNode(node);
					}
					else
					{
						getTreeState().expandNode(node);
					}
					onJunctionLinkClicked(target, node);
					updateTree(target);
				}
			});
			junctionLink.add(new AbstractBehavior()
			{
				private static final long serialVersionUID = 1L;

				public void onComponentTag(Component component, ComponentTag tag)
				{
					if (isNodeExpanded(node))
					{
						tag.put("class", "junction-open");
					}
					else
					{
						tag.put("class", "junction-closed");
					}
				}
			});
		}
		else
		{
			junctionLink = new WebMarkupContainer(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
				 */
				protected void onComponentTag(ComponentTag tag)
				{
					super.onComponentTag(tag);
					tag.setName("span");
					tag.put("class", "junction-corner");
				}
			};

		}

		return junctionLink;
	}

	/**
	 * Callback function called after user clicked on an junction link. The node has already been
	 * expanded/collapsed (depending on previous status).
	 * 
	 * @param target
	 *            Request target - may be null on non-ajax call
	 * 
	 * @param node
	 *            Node for which this callback is relevant
	 */
	protected void onJunctionLinkClicked(AjaxRequestTarget target, TreeNode node)
	{
	}

	/**
	 * The type of junction links and node selection links.
	 * <dl>
	 * <dt>Regular link</dt>
	 * <dd>Non-ajax link, always refreshes the whole page. Works with javascript disabled.</dd>
	 * <dt>Ajax link</dt>
	 * <dd>Links that supports partial updates. Doesn't work with javascript disabled</dd>
	 * <dt>Ajax fallback link</dt>
	 * <dd>Link that supports partial updates. With javascript disabled acts like regular link. The
	 * drawback is that generated url (thus the entire html) is larger then using the other two</dd>
	 * </dl>
	 */
	public static final class LinkType extends EnumeratedType
	{

		/** partial updates with no fallback. */
		public static final LinkType AJAX = new LinkType("AJAX");

		/**
		 * partial updates that falls back to a regular link in case the client does not support
		 * javascript.
		 */
		public static final LinkType AJAX_FALLBACK = new LinkType("AJAX_FALLBACK");

		/**
		 * non-ajax version that always re-renders the whole page.
		 */
		public static final LinkType REGULAR = new LinkType("REGULAR");

		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param name
		 *            the name of the type of the link
		 */
		public LinkType(String name)
		{
			super(name);
		}
	}

	/**
	 * Helper class for calling an action from a link.
	 * 
	 * @author Matej Knopp
	 */
	public interface ILinkCallback extends IClusterable
	{
		/**
		 * Called when the click is executed.
		 * 
		 * @param target
		 *            The ajax request target
		 */
		void onClick(AjaxRequestTarget target);
	}

	/**
	 * Creates a link of type specified by current linkType. When the links is clicked it calls the
	 * specified callback.
	 * 
	 * @param id
	 *            The component id
	 * @param callback
	 *            The link call back
	 * @return The link component
	 */
	public MarkupContainer newLink(String id, final ILinkCallback callback)
	{
		if (getLinkType() == LinkType.REGULAR)
		{
			return new Link(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.markup.html.link.Link#onClick()
				 */
				public void onClick()
				{
					callback.onClick(null);
				}
			};
		}
		else if (getLinkType() == LinkType.AJAX)
		{
			return new AjaxLink(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
				 */
				public void onClick(AjaxRequestTarget target)
				{
					callback.onClick(target);
				}
			};
		}
		else
		{
			return new AjaxFallbackLink(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.ajax.markup.html.AjaxFallbackLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
				 */
				public void onClick(AjaxRequestTarget target)
				{
					callback.onClick(target);
				}
			};
		}
	}

	/**
	 * Returns the current type of links on tree items.
	 * 
	 * @return The link type
	 */
	public LinkType getLinkType()
	{
		return linkType;
	}

	/**
	 * Sets the type of links on tree items. After the link type is changed, the whole tree is
	 * rebuild and re-rendered.
	 * 
	 * @param linkType
	 *            type of links
	 */
	public void setLinkType(LinkType linkType)
	{
		if (this.linkType != linkType)
		{
			this.linkType = linkType;
			invalidateAll();
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.tree.AbstractTree#isForceRebuildOnSelectionChange()
	 */
	protected boolean isForceRebuildOnSelectionChange()
	{
		return false;
	}

	private LinkType linkType = LinkType.AJAX;
}
