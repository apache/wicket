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
package wicket.markup.html.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.ResourceReference;
import wicket.WicketRuntimeException;
import wicket.behavior.HeaderContributor;
import wicket.markup.html.basic.Label;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.list.Loop;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.resources.CompressedResourceReference;
import wicket.model.AbstractReadOnlyDetachableModel;
import wicket.model.IModel;

/**
 * An tree that renders as a flat (not-nested) list, using spacers for
 * indentation and nodes at the end of one row.
 * <p>
 * The visible tree rows are put in one flat list. For each row, a list is
 * constructed with fillers, that can be used to create indentation. After the
 * fillers, the actual node content is put.
 * </p>
 * <p>
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class Tree extends AbstractTree implements TreeModelListener
{
	/**
	 * The default node panel. If you provide your own panel by overriding
	 * Tree.newNodePanel, but only want to override the markup, not the
	 * components that are added, you <i>may</i> extend this class. If you want
	 * to use other components than the default, provide a panel or fragment
	 * instead (and that's probably what you want as the look and feel of what
	 * this panel renders may be adjusted by overriding
	 * {@link Tree#createJunctionLink(DefaultMutableTreeNode)} and
	 * {@link Tree#createNodeLink(DefaultMutableTreeNode)}.
	 */
	public static class DefaultNodePanel extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param panelId
		 *            The component id
		 * @param tree
		 *            The containing tree component
		 * @param node
		 *            The tree node for this panel
		 */
		public DefaultNodePanel(String panelId, Tree tree, DefaultMutableTreeNode node)
		{
			super(panelId);
			// create a link for expanding and collapsing the node
			Link expandCollapsLink = tree.createJunctionLink(node);
			add(expandCollapsLink);
			// create a link for selecting a node
			Link selectLink = tree.createNodeLink(node);
			add(selectLink);
		}
	}

	/**
	 * Renders spacer items.
	 */
	private static final class SpacerList extends Loop
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 * @param size
		 *            size of loop
		 */
		public SpacerList(String id, int size)
		{
			super(id, size);
		}

		/**
		 * @see wicket.markup.html.list.Loop#populateItem(LoopItem)
		 */
		protected void populateItem(final Loop.LoopItem loopItem)
		{
			// nothing needed; we just render the tags and use CSS to indent
		}
	}

	/**
	 * List view for tree paths.
	 */
	private final class TreePathsListView extends ListView
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param name
		 *            name of the component
		 */
		public TreePathsListView(String name)
		{
			super(name, treePathsModel);
		}

		/**
		 * @see wicket.markup.html.list.ListView#getReuseItems()
		 */
		public boolean getReuseItems()
		{
			return Tree.this.getOptimizeItemRemoval();
		}

		/**
		 * @see wicket.markup.html.list.ListView#newItem(int)
		 */
		protected ListItem newItem(final int index)
		{
			IModel listItemModel = getListItemModel(getModel(), index);

			// create a list item that is smart enough to determine whether
			// it should be displayed or not
			return new ListItem(index, listItemModel)
			{
				private static final long serialVersionUID = 1L;

				public boolean isVisible()
				{
					TreeState treeState = getTreeState();
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)getModelObject();
					final TreePath path = new TreePath(node.getPath());
					final int row = treeState.getRowForPath(path);

					// if the row is -1, it is not visible, otherwise it is
					return (row != -1);
				}
			};
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem listItem)
		{
			// get the model object which is a tree node
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)listItem.getModelObject();

			// add spacers
			int level = node.getLevel();
			listItem.add(new SpacerList("spacers", level));

			// add node panel
			Component nodePanel = newNodePanel("node", node);
			if (nodePanel == null)
			{
				throw new WicketRuntimeException("node panel must be not-null");
			}
			if (!"node".equals(nodePanel.getId()))
			{
				throw new WicketRuntimeException("panel must have id 'node' assigned");
			}

			listItem.add(nodePanel);

			// add attr modifier for highlighting the selection
			listItem.add(new AttributeModifier("class", true, new SelectedPathReplacementModel(
					Tree.this, node)));
		}
	}

	/**
	 * Model for the paths of the tree.
	 */
	private final class TreePathsModel extends AbstractReadOnlyDetachableModel
	{
		private static final long serialVersionUID = 1L;

		/** whether this model is dirty. */
		boolean dirty = true;

		/** tree paths. */
		private List paths = new ArrayList();

		/**
		 * @see wicket.model.AbstractDetachableModel#getNestedModel()
		 */
		public IModel getNestedModel()
		{
			// TODO General: Check calls to this method; original: return paths;
			return null;
		}

		/**
		 * @see wicket.model.AbstractDetachableModel#onAttach()
		 */
		protected void onAttach()
		{
			if (dirty)
			{
				paths.clear();
				TreeModel model = getTreeState().getModel();
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)model.getRoot();
				Enumeration e = rootNode.preorderEnumeration();
				while (e.hasMoreElements())
				{
					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)e.nextElement();
					// TreePath path = new TreePath(treeNode.getPath());
					paths.add(treeNode);
				}
				dirty = false;
			}
		}

		/**
		 * @see wicket.model.AbstractDetachableModel#onDetach()
		 */
		protected void onDetach()
		{
		}

		/**
		 * @see wicket.model.AbstractDetachableModel#onGetObject(wicket.Component)
		 */
		protected Object onGetObject(Component component)
		{
			return paths;
		}

		/**
		 * Inserts the given node in the path list with the given index.
		 * 
		 * @param index
		 *            the index where the node should be inserted in
		 * @param node
		 *            node to insert
		 */
		void add(int index, DefaultMutableTreeNode node)
		{
			paths.add(index, node);
		}

		/**
		 * Gives the index of the given node withing this tree.
		 * 
		 * @param node
		 *            node to look for
		 * @return the index of the given node withing this tree
		 */
		int indexOf(DefaultMutableTreeNode node)
		{
			return paths.indexOf(node);
		}

		/**
		 * Removes the given node from the path list.
		 * 
		 * @param node
		 *            the node to remove
		 */
		void remove(DefaultMutableTreeNode node)
		{
			paths.remove(node);
		}
	}

	/** Name of the junction image component; value = 'junctionImage'. */
	public static final String JUNCTION_IMAGE_NAME = "junctionImage";

	/** Name of the node image component; value = 'nodeImage'. */
	public static final String NODE_IMAGE_NAME = "nodeImage";

	/** Blank image. */
	private static final ResourceReference BLANK = new ResourceReference(Tree.class,
			"blank.gif");

	/**
	 * Reference to the css file.
	 */
	private static final ResourceReference CSS = new CompressedResourceReference(Tree.class,
			"tree.css");

	/** Minus sign image. */
	private static final ResourceReference MINUS = new ResourceReference(Tree.class,
			"minus.gif");

	/** Plus sign image. */
	private static final ResourceReference PLUS = new ResourceReference(Tree.class,
			"plus.gif");

	private static final long serialVersionUID = 1L;

	/**
	 * If true, re-rendering the tree is more efficient if the tree model
	 * doesn't get changed. However, if this is true, you need to push changes
	 * to this tree. This can easility be done by registering this tree as the
	 * listener for tree model events (TreeModelListener), but you should <b>be
	 * carefull</b> not to create a memory leak by doing this (e.g. when you
	 * store the tree model in your session, the tree you registered cannot be
	 * GC-ed). TRUE by default.
	 */
	private boolean reuseItems = true;

	/** List view for tree paths. */
	private TreePathsListView treePathsListView;

	/** Model for the paths of the tree. */
	private TreePathsModel treePathsModel;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            The id of this container
	 * @param model
	 *            the underlying tree model
	 */
	public Tree(final String id, final TreeModel model)
	{
		super(id, model);
		this.treePathsModel = new TreePathsModel();
		add(treePathsListView = createTreePathsListView());

		ResourceReference css = getCss();
		add(HeaderContributor.forCss(css.getScope(), css.getName()));
	}

	/**
	 * Construct using the given tree state that holds the model to be used as
	 * the tree model.
	 * 
	 * @param id
	 *            The id of this container
	 * @param treeState
	 *            treeState that holds the underlying tree model
	 */
	public Tree(String id, TreeState treeState)
	{
		super(id, treeState);
		this.treePathsModel = new TreePathsModel();
		add(treePathsListView = createTreePathsListView());

		ResourceReference css = getCss();
		add(HeaderContributor.forCss(css.getScope(), css.getName()));
	}

	/**
	 * Gets whether item removal should be optimized. If true, re-rendering the
	 * tree is more efficient if the tree model doesn't get changed. However, if
	 * this is true, you need to push changes to this tree. This can easility be
	 * done by registering this tree as the listener for tree model events
	 * (TreeModelListener), but you should <b>be carefull</b> not to create a
	 * memory leak by doing this (e.g. when you store the tree model in your
	 * session, the tree you registered cannot be GC-ed). TRUE by default.
	 * 
	 * @return whether item removal should be optimized
	 * @deprecated Will be replaced by {@link #getReuseItems()}
	 */
	// TODO Post 1.2: Remove
	public boolean getOptimizeItemRemoval()
	{
		return getReuseItems();
	}

	/**
	 * Gets whether items should be reused. If true, re-rendering the tree is
	 * more efficient if the tree model doesn't get changed. However, if this is
	 * true, you need to push changes to this tree. This can easility be done by
	 * registering this tree as the listener for tree model events
	 * (TreeModelListener), but you should <b>be carefull</b> not to create a
	 * memory leak by doing this (e.g. when you store the tree model in your
	 * session, the tree you registered cannot be GC-ed). TRUE by default.
	 * 
	 * @return whether items should be reused
	 */
	public boolean getReuseItems()
	{
		return reuseItems;
	}

	/**
	 * Sets whether items should be reused. If true, re-rendering the tree is
	 * more efficient if the tree model doesn't get changed. However, if this is
	 * true, you need to push changes to this tree. This can easility be done by
	 * registering this tree as the listener for tree model events
	 * (TreeModelListener), but you should <b>be carefull</b> not to create a
	 * memory leak by doing this (e.g. when you store the tree model in your
	 * session, the tree you registered cannot be GC-ed). TRUE by default.
	 * 
	 * @param optimizeItemRemoval
	 *            whether the child items should be reused
	 * @deprecated Will be replaced by {@link #setReuseItems(boolean)}
	 */
	// TODO Post 1.2: Remove
	public void setOptimizeItemRemoval(boolean optimizeItemRemoval)
	{
		setReuseItems(optimizeItemRemoval);
	}

	/**
	 * Sets whether item removal should be optimized. If true, re-rendering the
	 * tree is more efficient if the tree model doesn't get changed. However, if
	 * this is true, you need to push changes to this tree. This can easility be
	 * done by registering this tree as the listener for tree model events
	 * (TreeModelListener), but you should <b>be carefull</b> not to create a
	 * memory leak by doing this (e.g. when you store the tree model in your
	 * session, the tree you registered cannot be GC-ed). TRUE by default.
	 * 
	 * @param reuseItems
	 *            whether the child items should be reused
	 * @return This
	 */
	public Tree setReuseItems(boolean reuseItems)
	{
		this.reuseItems = reuseItems;
		return this;
	}

	/**
	 * Sets the current tree model.
	 * 
	 * @param treeModel
	 *            the tree model to set as the current one
	 */
	public void setTreeModel(final TreeModel treeModel)
	{
		super.setTreeModel(treeModel);
		this.treePathsModel = new TreePathsModel();
		treePathsListView = createTreePathsListView();
		replace(treePathsListView);
	}

	/**
	 * Sets the current tree state to the given tree state.
	 * 
	 * @param treeState
	 *            the tree state to set as the current one
	 */
	public void setTreeState(final TreeState treeState)
	{
		super.setTreeState(treeState);
		this.treePathsModel = new TreePathsModel();
		treePathsListView = createTreePathsListView();
		replace(treePathsListView);
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
	 */
	public void treeNodesChanged(TreeModelEvent e)
	{
		// nothing to do here
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
	 */
	public void treeNodesInserted(TreeModelEvent e)
	{
		modelChanging();
		Object[] newNodes = e.getChildren();
		int len = newNodes.length;
		for (int i = 0; i < len; i++)
		{
			DefaultMutableTreeNode newNode = (DefaultMutableTreeNode)newNodes[i];
			DefaultMutableTreeNode previousNode = newNode.getPreviousSibling();
			int insertRow;
			if (previousNode == null)
			{
				previousNode = (DefaultMutableTreeNode)newNode.getParent();
			}
			if (previousNode != null)
			{
				insertRow = treePathsModel.indexOf(previousNode) + 1;
				if (insertRow == -1)
				{
					throw new IllegalStateException("node " + previousNode
							+ " not found in backing list");
				}
			}
			else
			{
				insertRow = 0;
			}
			treePathsModel.add(insertRow, newNode);
		}
		modelChanged();
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
	 */
	public void treeNodesRemoved(TreeModelEvent e)
	{
		modelChanging();
		Object[] deletedNodes = e.getChildren();
		int len = deletedNodes.length;
		for (int i = 0; i < len; i++)
		{
			DefaultMutableTreeNode deletedNode = (DefaultMutableTreeNode)deletedNodes[i];
			treePathsModel.remove(deletedNode);
		}
		modelChanged();
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
	 */
	public void treeStructureChanged(TreeModelEvent e)
	{
		treePathsModel.dirty = true;
		modelChanged();
	}

	/**
	 * Creates a junction link.
	 * 
	 * @param node
	 *            the node
	 * @return link for expanding/ collapsing the tree
	 */
	protected Link createJunctionLink(final DefaultMutableTreeNode node)
	{
		final Link junctionLink = new Link("junctionLink")
		{
			private static final long serialVersionUID = 1L;

			public void onClick()
			{
				junctionLinkClicked(node);
			}
		};
		junctionLink.add(getJunctionImage(node));
		return junctionLink;
	}

	/**
	 * Creates a node link.
	 * 
	 * @param node
	 *            the model of the node
	 * @return link for selection
	 */
	protected Link createNodeLink(final DefaultMutableTreeNode node)
	{
		final Link nodeLink = new Link("nodeLink")
		{
			private static final long serialVersionUID = 1L;

			public void onClick()
			{
				nodeLinkClicked(node);
			}
		};
		nodeLink.add(getNodeImage(node));
		nodeLink.add(new Label("label", getNodeLabel(node)));
		return nodeLink;
	}

	/**
	 * Creates the tree paths list view.
	 * 
	 * @return the tree paths list view
	 */
	protected final TreePathsListView createTreePathsListView()
	{
		final TreePathsListView treePaths = new TreePathsListView("tree");
		return treePaths;
	}

	/**
	 * Returns whether the path and the selected path are equal. This method is
	 * used by the {@link AttributeModifier}that is used for setting the CSS
	 * class for the selected row.
	 * 
	 * @param path
	 *            the path
	 * @param selectedPath
	 *            the selected path
	 * @return true if the path and the selected are equal, false otherwise
	 */
	protected boolean equals(final TreePath path, final TreePath selectedPath)
	{
		Object pathNode = path.getLastPathComponent();
		Object selectedPathNode = selectedPath.getLastPathComponent();
		return (pathNode != null && selectedPathNode != null && pathNode.equals(selectedPathNode));
	}

	/**
	 * Gets the stylesheet.
	 * 
	 * @return the stylesheet
	 */
	protected ResourceReference getCss()
	{
		return CSS;
	}

	/**
	 * Get image for a junction; used by method createExpandCollapseLink. If you
	 * use the packaged panel (Tree.html), you must name the component using
	 * JUNCTION_IMAGE_NAME.
	 * 
	 * @param node
	 *            the tree node
	 * @return the image for the junction
	 */
	protected Image getJunctionImage(final DefaultMutableTreeNode node)
	{
		if (!node.isLeaf())
		{
			// we want the image to be dynamically, yet resolving to a static
			// image.
			return new Image(JUNCTION_IMAGE_NAME)
			{
				private static final long serialVersionUID = 1L;

				protected ResourceReference getImageResourceReference()
				{
					if (isExpanded(node))
					{
						return MINUS;
					}
					else
					{
						return PLUS;
					}
				}
			};
		}
		else
		{
			return new Image(JUNCTION_IMAGE_NAME, BLANK);
		}
	}

	/**
	 * Get image for a node; used by method createNodeLink. If you use the
	 * packaged panel (Tree.html), you must name the component using
	 * NODE_IMAGE_NAME.
	 * 
	 * @param node
	 *            the tree node
	 * @return the image for the node
	 */
	protected Image getNodeImage(final DefaultMutableTreeNode node)
	{
		return new Image(NODE_IMAGE_NAME, BLANK);
	}

	/**
	 * Gets the label of the node that is used for the node link. Defaults to
	 * treeNodeModel.getUserObject().toString(); override to provide a custom
	 * label
	 * 
	 * @param node
	 *            the tree node
	 * @return the label of the node that is used for the node link
	 */
	protected String getNodeLabel(final DefaultMutableTreeNode node)
	{
		return String.valueOf(node.getUserObject());
	}

	/**
	 * @see wicket.Component#internalOnAttach()
	 */
	protected void internalOnAttach()
	{
		// if we don't optimize, rebuild the paths on every request
		if (!getOptimizeItemRemoval())
		{
			treePathsModel.dirty = true;
		}
	}

	/**
	 * Handler that is called when a junction link is clicked; this
	 * implementation sets the expanded state to one that corresponds with the
	 * node selection.
	 * 
	 * @param node
	 *            the tree node
	 */
	protected void junctionLinkClicked(final DefaultMutableTreeNode node)
	{
		setExpandedState(node);
	}

	/**
	 * Create a new panel for a tree node. This method can be overriden to
	 * provide a custom panel. This way, you can effectively nest anything you
	 * want in the tree, like input fields, images, etc.
	 * <p>
	 * <strong> you must use the provide panelId as the id of your custom panel
	 * </strong><br>
	 * for example, do:
	 * 
	 * <pre>
	 * return new MyNodePanel(panelId, node);
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * You can choose to either let your own panel extend from DefaultNodePanel
	 * when you just want to provide different markup but want to reuse the
	 * default components on this panel, or extend from NodePanel directly, and
	 * provide any component structure you like.
	 * </p>
	 * 
	 * @param panelId
	 *            the id that the panel MUST use
	 * @param node
	 *            the tree node for the panel
	 * @return a new Panel
	 */
	protected Component newNodePanel(String panelId, DefaultMutableTreeNode node)
	{
		return new DefaultNodePanel(panelId, this, node);
	}

	/**
	 * Handler that is called when a node link is clicked; this implementation
	 * sets the expanded state just as a click on a junction would do. Override
	 * this for custom behavior.
	 * 
	 * @param node
	 *            the tree node model
	 */
	protected void nodeLinkClicked(final DefaultMutableTreeNode node)
	{
		setSelected(node);
	}
}
