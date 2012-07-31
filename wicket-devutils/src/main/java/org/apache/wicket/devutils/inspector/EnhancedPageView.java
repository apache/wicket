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
package org.apache.wicket.devutils.inspector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultTableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableTreeProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.debug.PageView;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.Strings;

/**
 * Enhanced {@link PageView} which displays all <code>Component</code>s and <code>Behavior</code>s
 * of a <code>Page</code> in a <code>TableTree</code> representation. <code>Component</code>s and
 * <code>Behavior</code>s can be shown based on their statefulness status. There are also filtering
 * options to choose the information displayed. Useful for debugging.
 * 
 * @author Bertrand Guay-Paquet
 */
public final class EnhancedPageView extends GenericPanel<Page>
{
	private static final long serialVersionUID = 1L;

	private ExpandState expandState;
	private boolean showStatefulAndParentsOnly;
	private boolean showBehaviors;
	private Set<VisibleColumns> visibleColumns;

	private AbstractTree<TreeNode> componentTree;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param page
	 *            The page to be analyzed
	 */
	public EnhancedPageView(String id, Page page)
	{
		this(id, getModelFor(page == null ? null : page.getPageReference()));
	}

	private static IModel<Page> getModelFor(final PageReference pageRef)
	{
		return new LoadableDetachableModel<Page>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Page load()
			{
				if (pageRef == null)
					return null;
				Page page = pageRef.getPage();
				return page;
			}
		};
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageModel
	 *            The page to be analyzed
	 */
	public EnhancedPageView(String id, IModel<Page> pageModel)
	{
		super(id, pageModel);
		expandState = new ExpandState();
		expandState.expandAll();
		showStatefulAndParentsOnly = false;
		showBehaviors = true;
		visibleColumns = EnumSet.allOf(VisibleColumns.class);

		// Name of page
		add(new Label("info", new Model<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				Page page = getModelObject();
				return page == null ? "[Stateless Page]" : page.toString();
			}
		}));

		Model<String> pageRenderDuration = new Model<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				Page page = getModelObject();
				if (page != null)
				{
					Long renderTime = page.getMetaData(PageView.RENDER_KEY);
					if (renderTime != null)
					{
						return renderTime.toString();
					}
				}
				return "n/a";
			}
		};
		add(new Label("pageRenderDuration", pageRenderDuration));

		addTreeControls();
		componentTree = newTree();
		add(componentTree);
	}

	private enum VisibleColumns {
		PATH("Path"), STATELESS("Stateless"), RENDER_TIME("Render Time"), SIZE("Size"), TYPE("Type"), MODEL(
			"Model Object");

		public final String name;

		private VisibleColumns(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private void addTreeControls()
	{
		Form<Void> form = new Form<Void>("form");
		add(form);
		form.add(new CheckBox("showStateless", new PropertyModel<Boolean>(this,
			"showStatefulAndParentsOnly")));
		form.add(new CheckBox("showBehaviors", new PropertyModel<Boolean>(this, "showBehaviors")));
		form.add(new CheckBoxMultipleChoice<VisibleColumns>("visibleColumns",
			new PropertyModel<Set<VisibleColumns>>(this, "visibleColumns"),
			Arrays.asList(VisibleColumns.values())).setSuffix(" "));
		form.add(new AjaxFallbackButton("submit", form)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form)
			{
				AbstractTree<TreeNode> newTree = newTree();
				componentTree.replaceWith(newTree);
				componentTree = newTree;
				if (target != null)
					target.add(componentTree);
			}
		});


		add(new AjaxFallbackLink<Void>("expandAll")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				expandState.expandAll();
				if (target != null)
					target.add(componentTree);
			}
		});
		add(new AjaxFallbackLink<Void>("collapseAll")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				expandState.collapseAll();
				if (target != null)
					target.add(componentTree);
			}
		});
	}

	private AbstractTree<TreeNode> newTree()
	{
		List<IColumn<TreeNode, Void>> columns = new ArrayList<IColumn<TreeNode, Void>>();
		if (visibleColumns.contains(VisibleColumns.PATH))
		{
			columns.add(new PropertyColumn<TreeNode, Void>(Model.of("Path"), "path")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String getCssClass()
				{
					return "col_path";
				}
			});
		}
		columns.add(new TreeColumn<TreeNode, Void>(Model.of("Tree")));
		if (visibleColumns.contains(VisibleColumns.STATELESS))
		{
			columns.add(new PropertyColumn<TreeNode, Void>(Model.of("Stateless"), "stateless")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String getCssClass()
				{
					return "col_stateless";
				}
			});
		}
		if (visibleColumns.contains(VisibleColumns.RENDER_TIME))
		{
			columns.add(new PropertyColumn<TreeNode, Void>(Model.of("Render time (ms)"),
				"renderTime")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String getCssClass()
				{
					return "col_renderTime";
				}
			});
		}
		if (visibleColumns.contains(VisibleColumns.SIZE))
		{
			columns.add(new AbstractColumn<TreeNode, Void>(Model.of("Size"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void populateItem(Item<ICellPopulator<TreeNode>> item, String componentId,
					IModel<TreeNode> rowModel)
				{
					item.add(new Label(componentId, Bytes.bytes(rowModel.getObject().getSize())
						.toString()));
				}

				@Override
				public String getCssClass()
				{
					return "col_size";
				}
			});
		}
		if (visibleColumns.contains(VisibleColumns.TYPE))
		{
			columns.add(new PropertyColumn<TreeNode, Void>(Model.of("Type"), "type"));
		}
		if (visibleColumns.contains(VisibleColumns.MODEL))
		{
			columns.add(new PropertyColumn<TreeNode, Void>(Model.of("Model Object"), "model"));
		}

		TreeProvider provider = new TreeProvider();
		IModel<Set<TreeNode>> expandStateModel = new LoadableDetachableModel<Set<TreeNode>>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Set<TreeNode> load()
			{
				return expandState;
			}
		};
		AbstractTree<TreeNode> tree = new DefaultTableTree<TreeNode, Void>("tree", columns,
			provider, Integer.MAX_VALUE, expandStateModel)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Item<TreeNode> newRowItem(String id, int index, IModel<TreeNode> model)
			{
				return new OddEvenItem<TreeNode>(id, index, model);
			}
		};
		tree.setOutputMarkupId(true);
		return tree;
	}

	/**
	 * Tree node representing either a <code>Page</code>, a <code>Component</code> or a
	 * <code>Behavior</code>
	 */
	private class TreeNode
	{
		public IClusterable node;
		public TreeNode parent;
		public List<TreeNode> children;

		public TreeNode(IClusterable node, TreeNode parent)
		{
			this.node = node;
			this.parent = parent;
			children = new ArrayList<TreeNode>();
			if (!(node instanceof Component) && !(node instanceof Behavior))
				throw new IllegalArgumentException("Only accepts Components and Behaviors");
		}

		public boolean hasChildren()
		{
			return !children.isEmpty();
		}

		/**
		 * @return list of indexes to navigate from the root of the tree to this node (e.g. the path
		 *         to the node).
		 */
		public List<Integer> getPathIndexes()
		{
			List<Integer> path = new ArrayList<Integer>();
			TreeNode nextChild = this;
			TreeNode parent;
			while ((parent = nextChild.parent) != null)
			{
				int indexOf = parent.children.indexOf(nextChild);
				if (indexOf < 0)
					throw new AssertionError("Child not found in parent");
				path.add(indexOf);
				nextChild = parent;
			}
			Collections.reverse(path);
			return path;
		}

		public String getPath()
		{
			if (node instanceof Component)
			{
				return ((Component)node).getPath();
			}
			else
			{
				Behavior behavior = (Behavior)node;
				Component parent = (Component)this.parent.node;
				String parentPath = parent.getPath();
				int indexOf = parent.getBehaviors().indexOf(behavior);
				return parentPath + Component.PATH_SEPARATOR + "Behavior_" + indexOf;
			}
		}

		public String getRenderTime()
		{
			if (node instanceof Component)
			{
				Long renderDuration = ((Component)node).getMetaData(PageView.RENDER_KEY);
				if (renderDuration != null)
				{
					return renderDuration.toString();
				}
			}
			return "n/a";
		}

		public long getSize()
		{
			if (node instanceof Component)
			{
				long size = ((Component)node).getSizeInBytes();
				return size;
			}
			else
			{
				long size = WicketObjects.sizeof(node);
				return size;
			}
		}

		public String getType()
		{
			// anonymous class? Get the parent's class name
			String type = node.getClass().getName();
			if (type.indexOf("$") > 0)
			{
				type = node.getClass().getSuperclass().getName();
			}
			return type;
		}

		public String getModel()
		{
			if (node instanceof Component)
			{
				String model;
				try
				{
					model = ((Component)node).getDefaultModelObjectAsString();
				}
				catch (Exception e)
				{
					model = e.getMessage();
				}
				return model;
			}
			return null;
		}

		public boolean isStateless()
		{
			if (node instanceof Page)
			{
				return ((Page)node).isPageStateless();
			}
			else if (node instanceof Component)
			{
				return ((Component)node).isStateless();
			}
			else
			{
				Behavior behavior = (Behavior)node;
				Component parent = (Component)this.parent.node;
				return behavior.getStatelessHint(parent);
			}
		}

		@Override
		public String toString()
		{
			if (node instanceof Page)
			{
				// Last component of getType() i.e. almost the same as getClass().getSimpleName();
				String type = getType();
				type = Strings.lastPathComponent(type, '.');
				return type;
			}
			else if (node instanceof Component)
			{
				return ((Component)node).getId();
			}
			else
			{
				// Last component of getType() i.e. almost the same as getClass().getSimpleName();
				String type = getType();
				type = Strings.lastPathComponent(type, '.');
				return type;
			}
		}
	}


	/**
	 * TreeNode provider for the page. Provides nodes for the components and behaviors of the
	 * analyzed page.
	 */
	private class TreeProvider extends SortableTreeProvider<TreeNode, Void>
	{
		private static final long serialVersionUID = 1L;

		private TreeModel componentTreeModel = new TreeModel();

		@Override
		public void detach()
		{
			componentTreeModel.detach();
		}

		@Override
		public Iterator<? extends TreeNode> getRoots()
		{
			TreeNode tree = componentTreeModel.getObject();
			List<TreeNode> roots;
			if (tree == null)
				roots = Collections.emptyList();
			else
				roots = Arrays.asList(tree);
			return roots.iterator();
		}

		@Override
		public boolean hasChildren(TreeNode node)
		{
			return node.hasChildren();
		}

		@Override
		public Iterator<? extends TreeNode> getChildren(TreeNode node)
		{
			return node.children.iterator();
		}

		@Override
		public IModel<TreeNode> model(TreeNode object)
		{
			return new TreeNodeModel(object);
		}

		/**
		 * Model of the page component and behavior tree
		 */
		private class TreeModel extends LoadableDetachableModel<TreeNode>
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected TreeNode load()
			{
				Page page = getModelObject();
				if (page == null)
					return null;
				return buildTree(page, null);
			}

			private TreeNode buildTree(Component node, TreeNode parent)
			{
				TreeNode treeNode = new TreeNode(node, parent);
				List<TreeNode> children = treeNode.children;

				// Add its behaviors
				if (showBehaviors)
				{
					for (Behavior behavior : node.getBehaviors())
					{
						if (!showStatefulAndParentsOnly || !behavior.getStatelessHint(node))
							children.add(new TreeNode(behavior, treeNode));
					}
				}

				// Add its children
				if (node instanceof MarkupContainer)
				{
					MarkupContainer container = (MarkupContainer)node;
					for (Component child : container)
					{
						buildTree(child, treeNode);
					}
				}

				// Sort the children list, putting behaviors first
				Collections.sort(children, new Comparator<TreeNode>()
				{
					@Override
					public int compare(TreeNode o1, TreeNode o2)
					{
						if (o1.node instanceof Component)
						{
							if (o2.node instanceof Component)
							{
								return o1.getPath().compareTo((o2).getPath());
							}
							else
							{
								return 1;
							}
						}
						else
						{
							if (o2.node instanceof Component)
							{
								return -1;
							}
							else
							{
								return o1.getPath().compareTo((o2).getPath());
							}
						}
					}
				});

				// Add this node to its parent if
				// -it has children or
				// -it is stateful or
				// -stateless components are visible
				if (parent != null &&
					(!showStatefulAndParentsOnly || treeNode.hasChildren() || !node.isStateless()))
				{
					parent.children.add(treeNode);
				}
				return treeNode;
			}
		}

		/**
		 * Rertrieves a TreeNode based on its path
		 */
		private class TreeNodeModel extends LoadableDetachableModel<TreeNode>
		{
			private static final long serialVersionUID = 1L;

			private List<Integer> path;

			public TreeNodeModel(TreeNode treeNode)
			{
				super(treeNode);
				path = treeNode.getPathIndexes();
			}

			@Override
			protected TreeNode load()
			{
				TreeNode tree = componentTreeModel.getObject();
				TreeNode currentItem = tree;
				for (Integer index : path)
				{
					currentItem = currentItem.children.get(index);
				}
				return currentItem;
			}

			/**
			 * Important! Models must be identifyable by their contained object.
			 */
			@Override
			public int hashCode()
			{
				return path.hashCode();
			}

			/**
			 * Important! Models must be identifyable by their contained object.
			 */
			@Override
			public boolean equals(Object obj)
			{
				if (obj instanceof TreeNodeModel)
				{
					return ((TreeNodeModel)obj).path.equals(path);
				}
				return false;
			}
		}
	}

	/**
	 * Expansion state of the tree's nodes
	 */
	private static class ExpandState implements Set<TreeNode>, IClusterable
	{
		private static final long serialVersionUID = 1L;

		private HashSet<List<Integer>> set = new HashSet<List<Integer>>();
		private boolean reversed = false;

		public void expandAll()
		{
			set.clear();
			reversed = true;
		}

		public void collapseAll()
		{
			set.clear();
			reversed = false;
		}

		@Override
		public boolean add(TreeNode a_e)
		{
			List<Integer> pathIndexes = a_e.getPathIndexes();
			if (reversed)
			{
				return set.remove(pathIndexes);
			}
			else
			{
				return set.add(pathIndexes);
			}
		}

		@Override
		public boolean remove(Object a_o)
		{
			TreeNode item = (TreeNode)a_o;
			List<Integer> pathIndexes = item.getPathIndexes();
			if (reversed)
			{
				return set.add(pathIndexes);
			}
			else
			{
				return set.remove(pathIndexes);
			}
		}

		@Override
		public boolean contains(Object a_o)
		{
			TreeNode item = (TreeNode)a_o;
			List<Integer> pathIndexes = item.getPathIndexes();
			if (reversed)
			{
				return !set.contains(pathIndexes);
			}
			else
			{
				return set.contains(pathIndexes);
			}
		}

		@Override
		public int size()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isEmpty()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<TreeNode> iterator()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object[] toArray()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T[] toArray(T[] a_a)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> a_c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends TreeNode> a_c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> a_c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> a_c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear()
		{
			throw new UnsupportedOperationException();
		}
	}
}
