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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import wicket.WicketRuntimeException;
import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.IModel;
import wicket.model.Model;

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
 * and you have an indented tree.
 * </p>
 * @author Eelco Hillenius
 */
public abstract class IndentTree extends Tree
{
	/**
	 * Constructor.
	 * @param componentName The name of this container
	 * @param model the underlying tree model
	 */
	public IndentTree(final String componentName, final TreeModel model)
	{
		super(componentName, model);
		VisibleTreePathListView treePathsListView = createTreePathsListView();
		add(treePathsListView);
	}

	/**
	 * Creates the tree paths list view.
	 * @return the tree paths list view
	 */
	protected final VisibleTreePathListView createTreePathsListView()
	{
		TreeModel model = (TreeModel)getTreeState().getModel();
		TreeStateCache treeState = getTreeState();
		List treePathList = new ArrayList();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)model.getRoot();
		Enumeration e = rootNode.preorderEnumeration();
		while (e.hasMoreElements())
		{
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)e.nextElement();
			TreePath path = new TreePath(treeNode.getPath());
			TreeNodeModel treeNodeModel = new TreeNodeModel(
					treeNode, treeState, path);
			treePathList.add(treeNodeModel);
		}
		VisibleTreePathListView treePathsListView =
				new VisibleTreePathListView("tree",
				new Model((Serializable)treePathList));
		return treePathsListView;
	}

	/**
	 * Creates a junction link.
	 * @param node the model of the node
	 * @return link for expanding/ collapsing the tree
	 */
	private final Link createJunctionLink(final TreeNodeModel node)
	{
		Link junctionLink = new Link("junctionLink")
		{
			public void linkClicked()
			{
				junctionLinkClicked(node);
			}
		};
		HtmlContainer junctionImg = new HtmlContainer("junctionImg");
		junctionImg.add(new ComponentTagAttributeModifier(
				"src", true, new Model()
		{
			public Object getObject()
			{
				return getJunctionImageName(node);
			}
		}));
		junctionLink.add(junctionImg);
		return junctionLink;
	}

	/**
	 * Get image name for junction; used by method createExpandCollapseLink.
	 * @param node the model with the current node
	 * @return image name
	 */
	protected abstract String getJunctionImageName(TreeNodeModel node);

	/**
	 * Handler that is called when a junction link is clicked; this implementation sets the
	 * expanded state to one that corresponds with the node selection.
	 * @param node the tree node model
	 */
	protected void junctionLinkClicked(TreeNodeModel node)
	{
		setExpandedState(node.getTreeNode());
	}

	/**
	 * Creates a node link.
	 * @param node the model of the node
	 * @return link for selection
	 */
	private final Link createNodeLink(final TreeNodeModel node)
	{
		Object userObject = node.getTreeNode().getUserObject();
		Link nodeLink = new Link("nodeLink")
		{
			public void linkClicked()
			{
				nodeLinkClicked(node);
			}
		};
		HtmlContainer nodeImg = new HtmlContainer("nodeImg");
		nodeImg.add(new ComponentTagAttributeModifier(
				"src", true, new Model()
		{
			public Object getObject()
			{
				return getNodeImageName(node);
			}
		}));
		nodeLink.add(nodeImg);
		String userObjectAsString = getNodeLabel(node);
		nodeLink.add(new Label("label", userObjectAsString));
		return nodeLink;
	}

	/**
	 * Gets the label of the node that is used for the node link. Defaults to
	 * treeNodeModel.getUserObject().toString(); override to provide a custom label
	 * @param node the tree node model
	 * @return the label of the node that is used for the node link
	 */
	protected abstract String getNodeLabel(TreeNodeModel node);

	/**
	 * Get image name for node; used by method createExpandCollapseLink.
	 * @param node the model with the current node
	 * @return image name
	 */
	protected abstract String getNodeImageName(TreeNodeModel node);

	/**
	 * Handler that is called when a node link is clicked; this implementation sets the
	 * expanded state just as a click on a junction would do. Override this for custom
	 * behaviour.
	 * @param node the tree node model
	 */
	protected void nodeLinkClicked(TreeNodeModel node)
	{
		setSelected(node.getTreeNode());
	}

	/**
	 * Returns whether the path and the selected path are equal. This method is used by the
	 * {@link ComponentTagAttributeModifier}that is used for setting the CSS class for the
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
	 * Table for visible tree paths.
	 */
	private final class VisibleTreePathListView extends ListView
	{
		/**
		 * Construct.
		 * @param name name of the component
		 * @param model the model
		 */
		public VisibleTreePathListView(String name, IModel model)
		{
			super(name, model);
		}

		/**
		 * Renders the tree paths.
		 */
		protected void handleRender()
		{
			TreeStateCache treeState = getTreeState();
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

					TreeNodeModel treeNodeModel = (TreeNodeModel)listItem.getModelObject();
					TreePath path = treeNodeModel.getPath();
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
			TreeNodeModel treeNodeModel = (TreeNodeModel)listItem.getModelObject();

			// add spacers
			int level = treeNodeModel.getLevel();
			listItem.add(new SpacerList("spacers", level));

			// add node
			HtmlContainer nodeContainer = new HtmlContainer("node");
			Serializable userObject = treeNodeModel.getUserObject();

			if (userObject == null)
			{
				throw new WicketRuntimeException("User object cannot be null");
			}
			Link expandCollapsLink = IndentTree.this.createJunctionLink(treeNodeModel);
			nodeContainer.add(expandCollapsLink);

			Link selectLink = IndentTree.this.createNodeLink(treeNodeModel);
			nodeContainer.add(selectLink);
			listItem.add(nodeContainer);

			listItem.add(new ComponentTagAttributeModifier("class", true,
					new SelectedPathReplacementModel(treeNodeModel)));
		}
	}

	/**
	 * Replacement model that looks up whether the current row is the active one.
	 */
	private final class SelectedPathReplacementModel extends Model
	{
		/** the tree node model. */
		private final TreeNodeModel treeNodeModel;

		/**
		 * Construct.
		 * @param treeNodeModel tree node model
		 */
		public SelectedPathReplacementModel(TreeNodeModel treeNodeModel)
		{
			this.treeNodeModel = treeNodeModel;
		}

		/**
		 * @see wicket.model.IModel#getObject()
		 */
		public Object getObject()
		{
			TreePath path = treeNodeModel.getPath();
			TreePath selectedPath = treeNodeModel.getTreeState().getSelectedPath();
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
}
