/*
 * $Id$ $Revision$ $Date$
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
package wicket.extensions.markup.html.tree.table;

import java.io.Serializable;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.behavior.AbstractBehavior;
import wicket.extensions.markup.html.tree.AbstractTree;
import wicket.extensions.markup.html.tree.DefaultAbstractTree;
import wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import wicket.markup.ComponentTag;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Fragment;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;

/**
 * TreeTable is a component that represents a grid with a tree. It's divided
 * into columns. One of the columns has to be column derived from
 * {@link AbstractTreeColumn}.
 * 
 * @author Matej Knopp
 */
public class TreeTable extends DefaultAbstractTree
{
	/**
	 * Callback for rendering tree node text.
	 */
	public static interface IRenderNodeCallback extends Serializable
	{
		/**
		 * Renders the tree node to text.
		 * 
		 * @param node
		 *            The tree node to render
		 * @return the tree node as text
		 */
		public String renderNode(TreeNode node);
	}

	/**
	 * Represents a content of a cell in TreeColumn (column containing the
	 * actual tree).
	 * 
	 * @author Matej Knopp
	 */
	private class TreeFragment extends Fragment
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 * 
		 * 
		 * @param id
		 * @param node
		 * @param level
		 * @param renderNodeCallback
		 *            The call back for rendering nodes
		 */
		public TreeFragment(String id, final TreeNode node, int level,
				final IRenderNodeCallback renderNodeCallback)
		{
			super(id, "fragment");

			add(newIndentation(this, "indent", node, level));

			add(newJunctionLink(this, "link", "image", node));

			MarkupContainer nodeLink = newNodeLink(this, "nodeLink", node);
			add(nodeLink);

			nodeLink.add(newNodeIcon(nodeLink, "icon", node));

			nodeLink.add(new Label("label", new AbstractReadOnlyModel()
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see wicket.model.AbstractReadOnlyModel#getObject(wicket.Component)
				 */
				public Object getObject(Component c)
				{
					return renderNodeCallback.renderNode(node);
				}
			}));
		}
	}

	/** Reference to the css file. */
	private static final PackageResourceReference CSS = new PackageResourceReference(
			DefaultAbstractTree.class, "res/tree-table.css");

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a tree cell for given node. This method is supposed to be used by
	 * TreeColumns (columns that draw the actual tree).
	 * 
	 * @param parent
	 *            Parent component
	 * 
	 * @param id
	 *            Component ID
	 * 
	 * @param node
	 *            Tree node for the row
	 * 
	 * @param level
	 *            How deep is the node nested (for convenience)
	 * 
	 * @param callback
	 *            Used to get the display string
	 *            
	 * @param table
	 * 			  Tree table
	 *            
	 * @return The tree cell
	 */
	public static Component newTreeCell(MarkupContainer parent, String id, TreeNode node,
			int level, IRenderNodeCallback callback, TreeTable table)
	{		
		return table.newTreePanel(parent, id, node, level, callback);
	}

	// columns of the TreeTable
	private IColumn columns[];

	/**
	 * Creates the TreeTable for the given array of columns.
	 * 
	 * @param id
	 * @param columns
	 */
	public TreeTable(String id, IColumn columns[])
	{
		super(id);
		init(columns);
	}

	/**
	 * Creates the TreeTable for the given model and array of columns.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 * @param columns
	 *            The columns
	 */
	public TreeTable(String id, IModel model, IColumn columns[])
	{
		super(id, model);
		init(columns);
	}


	/**
	 * Creates the TreeTable for the given TreeModel and array of columns.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 * @param columns
	 *            The columns
	 */
	public TreeTable(String id, TreeModel model, IColumn columns[])
	{
		super(id, model);
		init(columns);
	}

	/**
	 * Adds the header to the TreeTable.
	 */
	protected void addHeader()
	{
		// create the view for side columns
		SideColumnsView sideColumns = new SideColumnsView("sideColumns", null);
		add(sideColumns);
		if (columns != null)
			for (int i = 0; i < columns.length; i++)
			{
				IColumn column = columns[i];
				if (column.getLocation().getAlignment() == Alignment.LEFT
						|| column.getLocation().getAlignment() == Alignment.RIGHT)
				{
					Component component = column.newHeader(sideColumns, "" + i);
					sideColumns.add(component);
					sideColumns.addColumn(column, component, null);
				}
			}

		// create the view for middle columns
		MiddleColumnsView middleColumns = new MiddleColumnsView("middleColumns", null);
		add(middleColumns);
		if (columns != null)
			for (int i = 0; i < columns.length; i++)
			{
				IColumn column = columns[i];
				if (column.getLocation().getAlignment() == Alignment.MIDDLE)
				{
					Component component = column.newHeader(middleColumns, "" + i);
					middleColumns.add(component);
					middleColumns.addColumn(column, component, null);
				}
			}
	};

	/**
	 * @see wicket.extensions.markup.html.tree.DefaultAbstractTree#getCSS()
	 */
	protected PackageResourceReference getCSS()
	{
		return CSS;
	}

	/**
	 * Creates a new instance of the TreeFragment.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param node
	 *            The tree node
	 * @param level
	 *            The level of the tree row
	 * @param renderNodeCallback
	 *            The node call back
	 * @return The tree panel
	 */
	protected Component newTreePanel(MarkupContainer parent, String id, final TreeNode node,
			int level, IRenderNodeCallback renderNodeCallback)
	{
		return new TreeFragment(id, node, level, renderNodeCallback);
	}

	/**
	 * @see AbstractTree#onBeforeAttach()
	 */
	protected void onBeforeAttach()
	{
		// has the header been added yet?
		if (get("sideColumns") == null)
		{
			// no. initialize columns first
			if (columns != null)
				for (int i = 0; i < columns.length; i++)
				{
					IColumn column = columns[i];
					column.setTreeTable(this);
				}

			// add the tree table header
			addHeader();
		}

		super.onAttach();
	}

	/**
	 * Populates one row of the tree.
	 * 
	 * @param item
	 *            the tree node component
	 * @param level
	 *            the current level
	 */
	protected void populateTreeItem(WebMarkupContainer item, int level)
	{
		final TreeNode node = (TreeNode)item.getModelObject();

		// add side columns
		SideColumnsView sideColumns = new SideColumnsView("sideColumns", node);
		item.add(sideColumns);
		if (columns != null)
			for (int i = 0; i < columns.length; i++)
			{
				IColumn column = columns[i];
				if (column.getLocation().getAlignment() == Alignment.LEFT
						|| column.getLocation().getAlignment() == Alignment.RIGHT)
				{
					Component component;
					// first try to create a renderable
					IRenderable renderable = column.newCell(node, level);

					if (renderable == null)
					{
						// if renderable failed, try to create a regular
						// component
						component = column.newCell(sideColumns, "" + i, node, level);
						sideColumns.add(component);
					}
					else
					{
						component = null;
					}

					sideColumns.addColumn(column, component, renderable);
				}
			}

		// add middle columns
		MiddleColumnsView middleColumns = new MiddleColumnsView("middleColumns", node);
		if (columns != null)
			for (int i = 0; i < columns.length; i++)
			{
				IColumn column = columns[i];
				if (column.getLocation().getAlignment() == Alignment.MIDDLE)
				{
					Component component;
					// first try to create a renderable
					IRenderable renderable = column.newCell(node, level);

					if (renderable == null)
					{
						// if renderable failed, try to create a regular
						// component
						component = column.newCell(middleColumns, "" + i, node, level);
						middleColumns.add(component);
					}
					else
					{
						component = null;
					}

					middleColumns.addColumn(column, component, renderable);
				}
			}
		item.add(middleColumns);

		// do distinguish between selected and unselected rows we add an
		// behavior
		// that modifies row css class.
		item.add(new AbstractBehavior()
		{
			private static final long serialVersionUID = 1L;

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
	 * Internal initialization. Also checks if at least one of the columns is
	 * derived from AbstractTreeColumn.
	 * 
	 * @param columns
	 *            The columns
	 */
	private void init(IColumn columns[])
	{
		boolean found = false;
		if (columns != null)
			for (int i = 0; i < columns.length; i++)
			{
				IColumn column = columns[i];
				if (column instanceof AbstractTreeColumn)
				{
					found = true;
					break;
				}
			}
		if (found == false)
		{
			throw new IllegalArgumentException(
					"At least one column in TreeTable must be derived from AbstractTreeColumn.");
		}

		this.columns = columns;
	}
}