/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.tree;

import java.io.Serializable;
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
import wicket.markup.html.basic.Label;
import wicket.markup.html.image.Image;
import wicket.markup.html.image.resource.StaticImageResourceReference;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.list.Loop;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

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
public abstract class Tree extends AbstractTree implements TreeModelListener
{
	/** Name of the junction image component; value = 'junctionImage'. */
	public static final String JUNCTION_IMAGE_NAME = "junctionImage";

	/** Name of the node image component; value = 'nodeImage'. */
	public static final String NODE_IMAGE_NAME = "nodeImage";
	
	/** Blank image */
	private static final ResourceReference BLANK = new StaticImageResourceReference(Tree.class, "blank.gif");
	
	/** Minus sign image */
	private static final ResourceReference MINUS = new StaticImageResourceReference(Tree.class, "minus.gif");
	
	/** Plus sign image */
	private static final ResourceReference PLUS = new StaticImageResourceReference(Tree.class, "plus.gif");

	/** List with tree paths. */
	private List treePathList;

	/** List view for tree paths. */
	private final TreePathsListView treePathsListView;

	/**
	 * Replacement model that looks up whether the current row is the active
	 * one.
	 */
	private final class SelectedPathReplacementModel extends Model
	{
		/** the tree node. */
		private final DefaultMutableTreeNode node;

		/**
		 * Construct.
		 * 
		 * @param node
		 *            tree node
		 */
		public SelectedPathReplacementModel(DefaultMutableTreeNode node)
		{
			this.node = node;
		}

		/**
		 * @see wicket.model.IModel#getObject(Component)
		 */
		public Object getObject(final Component component)
		{
			TreePath path = new TreePath(node.getPath());
			TreePath selectedPath = getTreeState().getSelectedPath();
			if (selectedPath != null)
			{
				boolean equals = Tree.this.equals(path, selectedPath);

				if (equals)
				{
					return Tree.this.getCssClassForSelectedRow();
				}
			}
			return Tree.this.getCssClassForRow();
		}
	}

	/**
	 * Renders spacer items.
	 */
	private final class SpacerList extends Loop
	{
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
		/**
		 * Construct.
		 * 
		 * @param name name of the component
		 * @param model the model
		 */
		public TreePathsListView(String name, IModel model)
		{
			super(name, model);
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem listItem)
		{
			// get the tree state
			TreeState treeState = getTreeState();

			// get the model object which is a tree node
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) listItem.getModelObject();

			// add spacers
			int level = node.getLevel();
			listItem.add(new SpacerList("spacers", level));

			// add node panel
			listItem.add(newNodePanel("node", node));

			// add attr modifier for highlighting the selection
			listItem.add(new AttributeModifier("class", true,
					new SelectedPathReplacementModel(node)));

			// check whether to current item is part of the visible path
			final TreePath path = new TreePath(node.getPath());
			final int row = treeState.getRowForPath(path);
			if (row != -1)
			{
				listItem.setVisible(true);
			}
			else
			{
				listItem.setVisible(false);
			}
		}
	}

	/**
	 * The default panel for a tree node.
	 * <p>
	 * You can provide an alternative panel by overriding Tree.newNodePanel
	 * </p>
	 */
	private final class NodePanel extends Panel
	{
		/**
		 * Construct.
		 * @param id component id
		 * @param node the tree node
		 */
		public NodePanel(final String id, final DefaultMutableTreeNode node)
		{
			super(id);

			// create a link for expanding and collapsing the node
			Link expandCollapsLink = Tree.this.createJunctionLink(node);
			add(expandCollapsLink);

			// create a link for selecting a node
			Link selectLink = Tree.this.createNodeLink(node);
			add(selectLink);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param componentName
	 *            The name of this container
	 * @param model
	 *            the underlying tree model
	 */
	public Tree(final String componentName, final TreeModel model)
	{
		super(componentName, model);
		add(treePathsListView = createTreePathsListView());
	}

	/**
	 * Construct using the given tree state that holds the model to be
	 * used as the tree model.
	 * 
	 * @param componentName
	 *            The name of this container
	 * @param treeState
	 *            treeState that holds the underlying tree model
	 */
	public Tree(String componentName, TreeState treeState)
	{
		super(componentName, treeState);
		add(treePathsListView = createTreePathsListView());
	}

	/**
	 * Create a new panel for a tree node. This method can be overriden to provide
	 * a custom panel. This way, you can effectively nest anything you want in
	 * the tree, like input fields, images, etc.
	 * <p><strong>
	 * you must use the provide panelId as the id of your custom panel
	 * </strong><br>
	 * for example, do: <pre>return new MyNodePanel(panelId, node);</pre>
	 * </p> 
	 * @param panelId the id that the panel MUST use
	 * @param node the tree node for the panel
	 * @return a new Panel
	 */
	protected NodePanel newNodePanel(String panelId, DefaultMutableTreeNode node)
	{
		return new NodePanel(panelId, node);
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
		TreePath parentPath = e.getTreePath();
		TreeState treeState = getTreeState();
		Object[] newNodes = e.getChildren();
		int len = newNodes.length;
		for (int i = 0; i < len; i++)
		{
			DefaultMutableTreeNode newNode = (DefaultMutableTreeNode) newNodes[i];
			DefaultMutableTreeNode previousNode = newNode.getPreviousSibling();
			int insertRow;
			if (previousNode == null)
			{
				previousNode = (DefaultMutableTreeNode) newNode.getParent();
			}
			if (previousNode != null)
			{
				insertRow = treePathList.indexOf(previousNode) + 1;
				if (insertRow == -1)
				{
					throw new IllegalStateException("node "
							+ previousNode + " not found in backing list");
				}
			}
			else
			{
				insertRow = 0;
			}
			treePathList.add(insertRow, newNode);
		}
		modelChanging();
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
	 */
	public void treeNodesRemoved(TreeModelEvent e)
	{
		modelChanging();
		TreePath parentPath = e.getTreePath();
		TreeState treeState = getTreeState();
		Object[] deletedNodes = e.getChildren();
		int len = deletedNodes.length;
		for (int i = 0; i < len; i++)
		{
			DefaultMutableTreeNode deletedNode = (DefaultMutableTreeNode) deletedNodes[i];
			treePathList.remove(deletedNode);
		}
		modelChanging();
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
	 */
	public void treeStructureChanged(TreeModelEvent e)
	{
		// just totally rebuild the tree paths structure
		this.treePathList.clear();
		addNodesToTreePathList();
	}

	/**
	 * Creates the tree paths list view.
	 * 
	 * @return the tree paths list view
	 */
	protected final TreePathsListView createTreePathsListView()
	{
		final TreeState treeState = getTreeState();
		this.treePathList = new ArrayList();
		addNodesToTreePathList();
		final TreePathsListView treePaths = new TreePathsListView("tree", new Model(
				(Serializable) treePathList));
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
		return (pathNode != null && selectedPathNode != null && pathNode
				.equals(selectedPathNode));
	}

	/**
	 * Get image for a junction; used by method createExpandCollapseLink. If you
	 * use the packaged panel (Tree.html), you must name the component
	 * using JUNCTION_IMAGE_NAME.
	 * 
	 * @param node
	 *            the tree node
	 * @return the image for the junction
	 */
	protected Image getJunctionImage(final DefaultMutableTreeNode node)
	{
		if (!node.isLeaf())
		{
			// we want the image to be dynamically, yet resolving to a static image.
			return new Image(JUNCTION_IMAGE_NAME)
			{
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
		return new Image(JUNCTION_IMAGE_NAME, BLANK);
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
	 * Handler that is called when a node link is clicked; this implementation
	 * sets the expanded state just as a click on a junction would do. Override
	 * this for custom behaviour.
	 * 
	 * @param node
	 *            the tree node model
	 */
	protected void nodeLinkClicked(final DefaultMutableTreeNode node)
	{
		setSelected(node);
	}

	/**
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false; // the tree does not need to be versioned
	}

	/**
	 * Add the nodes to the backing tree paths list.
	 */
	private final void addNodesToTreePathList()
	{
		TreeModel model = (TreeModel) getTreeState().getModel();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
		Enumeration e = rootNode.preorderEnumeration();
		while (e.hasMoreElements())
		{
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) e.nextElement();
			TreePath path = new TreePath(treeNode.getPath());
			treePathList.add(treeNode);
		}
	}

	/**
	 * Creates a junction link.
	 * 
	 * @param node
	 *            the node
	 * @return link for expanding/ collapsing the tree
	 */
	private final Link createJunctionLink(final DefaultMutableTreeNode node)
	{
		final Link junctionLink = new Link("junctionLink")
		{
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
	private final Link createNodeLink(final DefaultMutableTreeNode node)
	{
		final Link nodeLink = new Link("nodeLink")
		{
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
	 * Gets the CSS class attribute value for a normal (not-selected) row.
	 * 
	 * @return the CSS class attribute value for a normal (not-selected) row
	 */
	private String getCssClassForRow()
	{
		return "treerow";
	}

	/**
	 * Gets the CSS class attribute value for the selected row.
	 * 
	 * @return the CSS class attribute value for the selected row
	 */
	private String getCssClassForSelectedRow()
	{
		return "treerow-selected";
	}
}
