/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import wicket.IResourceListener;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.image.AbstractImage;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.file.Path;
import wicket.util.lang.Classes;
import wicket.util.resource.IResource;
import wicket.util.resource.Resource;

/**
 * A Tree that renders as a flat (not-nested) list, using spacers for indentation and
 * nodes at the end of one row.
 * <p>
 * The visible tree rows are put in one flat list. For each row, a list is constructed
 * with fillers, that can be used to create indentation. After the fillers, the actual
 * node content is put.
 * </p>
 * <p>
 * For example:
 * 
 * <pre>
 * 
 *  &lt;span id=&quot;spacers&quot;&gt;&lt;/span&gt;&lt;span id=&quot; spacers&quot;&gt;&lt;/span&gt;
 *  &lt;span id =&quot;node&quot;&gt;&lt;span id=&quot;label&quot;&gt;foo&lt;/span&gt;&lt;/span&gt;
 *  
 * </pre>
 * 
 * Could be one row, where the node is on level two (hence the two spacer elements).
 * </p>
 * <p>
 * If you combine this with CSS like:
 * 
 * <pre>
 * 
 * 	#spacers {
 * 		padding-left: 16px;
 * 	}
 *  
 * </pre>
 * 
 * and here is your indented tree.
 * </p>
 * @author Eelco Hillenius
 */
public abstract class IndentTree extends Tree implements TreeModelListener
{
	/** name of the junction image component; value = 'junctionImage'. */
	public static final String JUNCTION_IMAGE_NAME = "junctionImage";

	/** name of the node image component; value = 'nodeImage'. */
	public static final String NODE_IMAGE_NAME = "nodeImage";

	/** a blank image for junctions. */
	private static final LocalImage JUNCTION_IMG_BLANK =
		new LocalImage(JUNCTION_IMAGE_NAME, "blank.gif");

	/** an image that draws a '+'. */
	private static final LocalImage JUNCTION_IMG_PLUS =
		new LocalImage(JUNCTION_IMAGE_NAME, "plus.gif");

	/** an image that draws a '-'. */
	private static final LocalImage JUNCTION_IMG_MINUS =
		new LocalImage(JUNCTION_IMAGE_NAME, "minus.gif");

	/** a blank image for nodes. */
	private static final LocalImage NODE_IMG_BLANK =
		new LocalImage(NODE_IMAGE_NAME, "blank.gif");

	// set scope of images
	static
	{
		JUNCTION_IMG_BLANK.setSharing(Component.APPLICATION_SHARED);
		JUNCTION_IMG_PLUS.setSharing(Component.APPLICATION_SHARED);
		JUNCTION_IMG_MINUS.setSharing(Component.APPLICATION_SHARED);
		NODE_IMG_BLANK.setSharing(Component.APPLICATION_SHARED);
	}

	/** list with tree paths. */
	private List treePathList;

	/** list view for tree paths. */
	private final TreePathsListView treePathsListView;

	/**
	 * Constructor.
	 * @param componentName The name of this container
	 * @param model the underlying tree model
	 */
	public IndentTree(final String componentName, final TreeModel model)
	{
		super(componentName, model);
		treePathsListView = createTreePathsListView();
		add(treePathsListView);
		model.addTreeModelListener(this);
	}

	/**
	 * Creates the tree paths list view.
	 * @return the tree paths list view
	 */
	protected final TreePathsListView createTreePathsListView()
	{
		TreeState treeState = getTreeState();
		this.treePathList = new ArrayList();
		addNodesToTreePathList();
		TreePathsListView treePaths = new TreePathsListView(
				"tree", new Model((Serializable)treePathList));
		return treePaths;
	}

	/**
	 * Add the nodes to the backing tree paths list.
	 */
	private final void addNodesToTreePathList()
	{
		TreeModel model = (TreeModel)getTreeState().getModel();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)model.getRoot();
		Enumeration e = rootNode.preorderEnumeration();
		while (e.hasMoreElements())
		{
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)e.nextElement();
			TreePath path = new TreePath(treeNode.getPath());
			treePathList.add(treeNode);
		}
	}

	/**
	 * Invalidates this tree and forces the rows to re-render.
	 * @see wicket.Component#invalidateModel()
	 */
	protected final void invalidateModel()
	{
		treePathsListView.invalidateModel();
	}

	/**
	 * Creates a junction link.
	 * @param node the node
	 * @return link for expanding/ collapsing the tree
	 */
	private final Link createJunctionLink(final DefaultMutableTreeNode node)
	{
		Link junctionLink = new Link("junctionLink")
		{
			public void onLinkClicked()
			{
				junctionLinkClicked(node);
			}
		};
		AbstractImage img = getJunctionImage(node);
		junctionLink.add(img);
		return junctionLink;
	}

	/**
	 * Get image for a junction; used by method createExpandCollapseLink.
	 * If you use the packaged panel (IndentTree.html), you must name the component
	 * using JUNCTION_IMAGE_NAME.
	 * @param node the tree node
	 * @return the image for the junction
	 */
	protected AbstractImage getJunctionImage(final DefaultMutableTreeNode node)
	{
		if (!node.isLeaf())
		{
			// we want the image to be dynamically, yet resolving to a application
			// static image.
			IModel imgModel = new Model()
			{
				public Object getObject()
				{
					final String url;
					if(isExpanded(node))
					{
						url = getRequestCycle().urlFor(
								JUNCTION_IMG_MINUS, IResourceListener.class);
					}
					else
					{
						url = getRequestCycle().urlFor(
								JUNCTION_IMG_PLUS, IResourceListener.class);
					}
					return url;
				}
			};
			LocalImage img = new LocalImage(JUNCTION_IMAGE_NAME, imgModel);
			return img;
		}
		else
		{
			String url = getRequestCycle().urlFor(JUNCTION_IMG_BLANK, IResourceListener.class);
			LocalImage img = new LocalImage(JUNCTION_IMAGE_NAME, url);
			return img;
		}
	}

	/**
	 * Handler that is called when a junction link is clicked; this implementation sets the
	 * expanded state to one that corresponds with the node selection.
	 * @param node the tree node
	 */
	protected void junctionLinkClicked(final DefaultMutableTreeNode node)
	{
		setExpandedState(node);
	}

	/**
	 * Creates a node link.
	 * @param node the model of the node
	 * @return link for selection
	 */
	private final Link createNodeLink(final DefaultMutableTreeNode node)
	{
		Object userObject = node.getUserObject();
		Link nodeLink = new Link("nodeLink")
		{
			public void onLinkClicked()
			{
				nodeLinkClicked(node);
			}
		};
		AbstractImage img = getNodeImage(node);
		nodeLink.add(img);
		String userObjectAsString = getNodeLabel(node);
		nodeLink.add(new Label("label", userObjectAsString));
		return nodeLink;
	}

	/**
	 * Gets the label of the node that is used for the node link. Defaults to
	 * treeNodeModel.getUserObject().toString(); override to provide a custom label
	 * @param node the tree node
	 * @return the label of the node that is used for the node link
	 */
	protected String getNodeLabel(final DefaultMutableTreeNode node)
	{
		return String.valueOf(node.getUserObject());
	}

	/**
	 * Get image for a node; used by method createNodeLink.
	 * If you use the packaged panel (IndentTree.html), you must name the component
	 * using NODE_IMAGE_NAME.
	 * @param node the tree node
	 * @return the image for the node
	 */
	protected AbstractImage getNodeImage(final DefaultMutableTreeNode node)
	{
		String url = getRequestCycle().urlFor(NODE_IMG_BLANK, IResourceListener.class);
		LocalImage img = new LocalImage(JUNCTION_IMAGE_NAME, url);
		return img;
	}

	/**
	 * Handler that is called when a node link is clicked; this implementation sets the
	 * expanded state just as a click on a junction would do. Override this for custom
	 * behaviour.
	 * @param node the tree node model
	 */
	protected void nodeLinkClicked(final DefaultMutableTreeNode node)
	{
		setSelected(node);
	}

	/**
	 * Returns whether the path and the selected path are equal. This method is used by the
	 * {@link AttributeModifier}that is used for setting the CSS class for the
	 * selected row.
	 * @param path the path
	 * @param selectedPath the selected path
	 * @return true if the path and the selected are equal, false otherwise
	 */
	protected boolean equals(TreePath path, TreePath selectedPath)
	{
		boolean equals;
		Object pathNode = path.getLastPathComponent();
		Object selectedPathNode = selectedPath.getLastPathComponent();
		equals = (pathNode != null && selectedPathNode != null && pathNode.equals(selectedPathNode));
		return equals;
	}

	/**
	 * Gets the CSS class attribute value for the selected row.
	 * @return the CSS class attribute value for the selected row
	 */
	protected String getCSSClassForSelectedRow()
	{
		return "treerow-selected";
	}

	/**
	 * Gets the CSS class attribute value for a normal (not-selected) row.
	 * @return the CSS class attribute value for a normal (not-selected) row
	 */
	protected String getCSSClassForRow()
	{
		return "treerow";
	}

	/**
	 * List view for tree paths.
	 */
	private final class TreePathsListView extends ListView
	{
		/**
		 * Construct.
		 * @param name name of the component
		 * @param model the model
		 */
		public TreePathsListView(String name, Serializable model)
		{
			super(name, model);
		}

		/**
		 * Renders the tree paths.
		 */
		protected void onRender()
		{
			TreeState treeState = getTreeState();
			// Ask parents for markup stream to use
			final MarkupStream markupStream = findMarkupStream();
			// Save position in markup stream
			final int markupStart = markupStream.getCurrentIndex();
			// Get number of listItems to be displayed
			int size = getViewSize();
			if (size > 0)
			{
				// Loop through the markup in this container for each child
				// container
				for (int i = 0; i < size; i++)
				{
					// Get the name of the component for listItem i
					final String componentName = Integer.toString(i);

					// If this component does not already exist, populate it
					ListItem listItem = (ListItem)get(componentName);
					if (listItem == null)
					{
						// Create listItem for index i of the list
						listItem = newItem(i);
						populateItem(listItem);

						// Add cell to list view
						add(listItem);
					}

					DefaultMutableTreeNode node = (DefaultMutableTreeNode)listItem.getModelObject();
					TreePath path = new TreePath(node.getPath());
					int row = treeState.getRowForPath(path);
					if(row != -1)
					{
						listItem.setVisible(true);
					}
					else
					{
						listItem.setVisible(false);
					}
					// Rewind to start of markup for kids
					markupStream.setCurrentIndex(markupStart);
					// Render cell
					renderItem(listItem, i >= (size - 1));
				}
			}
			else
			{
				markupStream.skipComponent();
			}
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem listItem)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)listItem.getModelObject();

			// add spacers
			int level = node.getLevel();
			listItem.add(new SpacerList("spacers", level));

			// add node
			WebMarkupContainer nodeContainer = new WebMarkupContainer("node");
			Link expandCollapsLink = IndentTree.this.createJunctionLink(node);
			nodeContainer.add(expandCollapsLink);

			Link selectLink = IndentTree.this.createNodeLink(node);
			nodeContainer.add(selectLink);
			listItem.add(nodeContainer);

			listItem.add(new AttributeModifier("class", true,
					new SelectedPathReplacementModel(node)));
		}
	}

	/**
	 * Replacement model that looks up whether the current row is the active one.
	 */
	private final class SelectedPathReplacementModel extends Model
	{
		/** the tree node. */
		private final DefaultMutableTreeNode node;

		/**
		 * Construct.
		 * @param node tree node
		 */
		public SelectedPathReplacementModel(DefaultMutableTreeNode node)
		{
			this.node = node;
		}

		/**
		 * @see wicket.model.IModel#getObject()
		 */
		public Object getObject()
		{
			TreePath path = new TreePath(node.getPath());
			TreePath selectedPath = getTreeState().getSelectedPath();
			if (selectedPath != null)
			{
				boolean equals = IndentTree.this.equals(path, selectedPath);

				if (equals)
				{
					return IndentTree.this.getCSSClassForSelectedRow();
				}
			}
			return IndentTree.this.getCSSClassForRow();
		}
	}

	/**
	 * Renders spacer items.
	 */
	private final class SpacerList extends ListView
	{
		/**
		 * Construct.
		 * @param componentName name of the component
		 * @param size number of spacer elements to create
		 */
		public SpacerList(String componentName, int size)
		{
			super(componentName, (IModel)null);
			setViewSize(size);
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem listItem)
		{
			// nothing needed; we just render the tags and use CSS to indent
		}
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
	 */
	public void treeNodesChanged(TreeModelEvent e)
	{
		// nothing to do hereS
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
	 */
	public void treeNodesInserted(TreeModelEvent e)
	{
		TreePath parentPath = e.getTreePath();
		TreeState treeState = getTreeState();
		Object[] newNodes = e.getChildren();
		int len = newNodes.length;
		for(int i = 0; i < len; i++)
		{
			DefaultMutableTreeNode newNode = (DefaultMutableTreeNode)newNodes[i];
			DefaultMutableTreeNode previousNode = newNode.getPreviousSibling();
			int insertRow;
			if(previousNode == null)
			{
				previousNode = (DefaultMutableTreeNode)newNode.getParent();
			}
			if(previousNode != null)
			{
				insertRow = treePathList.indexOf(previousNode) + 1;
				if(insertRow == -1)
				{
					throw new IllegalStateException("node " + previousNode
							+ " not found in backing list");
				}
			}
			else
			{
				insertRow = 0;
			}
			treePathList.add(insertRow, newNode);
		}
		invalidateModel();
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
	 */
	public void treeNodesRemoved(TreeModelEvent e)
	{
		TreePath parentPath = e.getTreePath();
		TreeState treeState = getTreeState();
		Object[] deletedNodes = e.getChildren();
		int len = deletedNodes.length;
		for(int i = 0; i < len; i++)
		{
			DefaultMutableTreeNode deletedNode = (DefaultMutableTreeNode)deletedNodes[i];
			treePathList.remove(deletedNode);
		}
		invalidateModel();
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
	 * Image that loads from this package (instead of Image's page)
	 * without locale, style etc.
	 */
	private static final class LocalImage extends Image
	{
		/**
		 * Construct.
		 * @param name component name
		 * @param object model
		 */
		public LocalImage(String name, Serializable object)
		{
			super(name, object);
		}

	    /**
	     * @return Gets the image resource for the component.
	     */
	    protected IResource getResource()
	    {
	    	final String imageResource = getModelObjectAsString();
			final String path = Classes.packageName(IndentTree.class) + "." + imageResource;
	        return Resource.locate
	        (
	            new Path(),
	            IndentTree.class.getClassLoader(),
	            path,
	            null,
	            null,
	            null
	        );
	    }

	    /**
	     * @see wicket.Component#onComponentTag(ComponentTag)
	     */
	    protected void onComponentTag(final ComponentTag tag)
	    {
	        checkComponentTag(tag, "img");
	        final String url = getModelObjectAsString();
			tag.put("src", url.replaceAll("&", "&amp;"));
	    }
	}
}
