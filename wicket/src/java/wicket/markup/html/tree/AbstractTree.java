package wicket.markup.html.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.ajax.AjaxRequestTarget;
import wicket.behavior.HeaderContributor;
import wicket.markup.MarkupStream;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.panel.Panel;
import wicket.model.IDetachable;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.string.AppendingStringBuffer;

/**
 * This class encapsulates the logic for displaying and (partial) updating the
 * tree. Actual presentation is out of scope of this class. User should derive
 * they own tree (if needed) from {@link DefaultAbstractTree} or {@link Tree}
 * (recommended).
 * 
 * @author Matej Knopp
 */
public abstract class AbstractTree extends Panel<TreeModel>
		implements
			ITreeStateListener,
			TreeModelListener
{

	/**
	 * Interface for visiting individual tree items.
	 */
	private static interface IItemCallback
	{
		/**
		 * Visits the tree item.
		 * 
		 * @param item
		 *            the item to visit
		 */
		void visitItem(TreeItem item);
	}

	/**
	 * This class represents one row in rendered tree (TreeNode). Only TreeNodes
	 * that are visible (all their parent are expanded) have TreeItem created
	 * for them.
	 */
	private final class TreeItem extends WebMarkupContainer<TreeNode>
	{
		/**
		 * whether this tree item should also render it's children to response.
		 * this is set if we need the whole subtree rendered as one component in
		 * ajax response, so that we can replace it in one step (replacing
		 * individual rows is very slow in javascript, therefore we replace the
		 * whole subtree)
		 */
		private final static int FLAG_RENDER_CHILDREN = FLAG_RESERVED8;

		private static final long serialVersionUID = 1L;

		/**
		 * tree item children - we need this to traverse items in correct order
		 * when rendering
		 */
		private List<TreeItem> children = null;

		/** tree item level - how deep is this item in tree */
		private int level;

		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent
		 * @param id
		 *            The component id
		 * @param node
		 *            tree node
		 * @param level
		 *            current level
		 */
		public TreeItem(MarkupContainer parent, String id, final TreeNode node, int level)
		{
			super(parent, id, new Model<TreeNode>(node));

			nodeToItemMap.put(node, this);
			this.level = level;
			setOutputMarkupId(true);

			if (level != -1)
			{
				populateTreeItem(this, level);
			}
		}

		/**
		 * @return The children
		 */
		public List<TreeItem> getChildren()
		{
			return children;
		}

		/**
		 * @return The current level
		 */
		public int getLevel()
		{
			return level;
		}

		@Override
		public String getMarkupId()
		{
			// this is overriden to produce id that begins with id of tree
			// if the tree has set (shorter) id in markup, we can use it to
			// shorten the id of individual TreeItems
			return AbstractTree.this.getMarkupId() + "_" + getId();
		}

		/**
		 * @return parent item
		 */
		public TreeItem getParentItem()
		{
			return nodeToItemMap.get(getModelObject().getParent());
		}

		/**
		 * Sets the children.
		 * 
		 * @param children
		 *            The children
		 */
		public void setChildren(List<TreeItem> children)
		{
			this.children = children;
		}

		/**
		 * Whether to render children.
		 * 
		 * @return whether to render children
		 */
		protected final boolean isRenderChildren()
		{
			return getFlag(FLAG_RENDER_CHILDREN);
		}

		/**
		 * @see wicket.MarkupContainer#onRender(wicket.markup.MarkupStream)
		 */
		@Override
		protected void onRender(final MarkupStream markupStream)
		{
			// is this root and tree is in rootless mode?
			if (this == rootItem && isRootLess() == true)
			{
				// yes, write empty div with id
				// this is necesary for createElement js to work correctly
				getResponse().write(
						"<div style=\"display:none\" id=\"" + getMarkupId() + "\"></div>");
				markupStream.skipComponent();
			}
			else
			{
				// remember current index
				final int index = markupStream.getCurrentIndex();

				// render the item
				super.onRender(markupStream);

				// should we also render children (ajax response)
				if (isRenderChildren())
				{
					// visit every child
					visitItemChildren(this, new IItemCallback()
					{
						public void visitItem(TreeItem item)
						{
							// rewind markupStream
							markupStream.setCurrentIndex(index);
							// render child
							item.onRender(markupStream);
						}
					});
					// children are rendered, clear the flag
					setRenderChildren(false);
				}
			}
		}

		protected final void setRenderChildren(boolean value)
		{
			setFlag(FLAG_RENDER_CHILDREN, value);
		}

		@Override
		protected void onDetach()
		{
			super.onDetach();
			TreeNode node = getModelObject();
			if (node instanceof IDetachable)
			{
				((IDetachable)node).detach();
			}
		}
	}

	/**
	 * Components that holds tree items. This is similiar to ListView, but it
	 * renders tree items in the right order.
	 */
	private class TreeItemContainer extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent component
		 * @param id
		 *            The component id
		 */
		public TreeItemContainer(MarkupContainer parent, String id)
		{
			super(parent, id);
		}

		@Override
		public void remove(Component component)
		{
			// when a treeItem is removed, remove reference to it from
			// nodeToItemMAp
			if (component instanceof TreeItem)
			{
				nodeToItemMap.remove(((TreeItem)component).getModelObject());
			}
			super.remove(component);
		}

		/**
		 * renders the tree items, making sure that items are rendered in the
		 * order they should be
		 */
		@Override
		protected void onRender(final MarkupStream markupStream)
		{
			// Save position in markup stream
			final int markupStart = markupStream.getCurrentIndex();

			// have we rendered at least one item?
			class Rendered
			{
				boolean rendered = false;
			}
			;
			final Rendered rendered = new Rendered();

			// is there a root item? (non-empty tree)
			if (rootItem != null)
			{
				IItemCallback callback = new IItemCallback()
				{
					public void visitItem(TreeItem item)
					{
						// rewind markup stream
						markupStream.setCurrentIndex(markupStart);

						// render component
						item.render(markupStream);

						rendered.rendered = true;
					}
				};

				// visit item and it's children
				visitItemAndChildren(rootItem, callback);
			}

			if (rendered.rendered == false)
			{
				// tree is empty, just move the markupStream
				markupStream.skipComponent();
			}
		}
	}

	/** Reference to the javascript file. */
	private static final PackageResourceReference JAVASCRIPT = new PackageResourceReference(
			AbstractTree.class, "res/tree.js");

	/**
	 * Returns the enumeration as iterable interface.
	 * 
	 * @param enumeration
	 * @param <T>
	 *            The type
	 * @return iterable
	 */
	private static final <T> Iterable<T> toIterable(final Enumeration enumeration)
	{
		return new Iterable<T>()
		{
			public Iterator<T> iterator()
			{
				return toIterator(enumeration);
			}
		};
	}

	/**
	 * Returns an iterator that iterates trough the enumeration.
	 * 
	 * @param enumeration
	 *            The enumeration to iterate through
	 * @param <T>
	 *            The type
	 * @return The iterator
	 */
	private static final <T> Iterator<T> toIterator(final Enumeration enumeration)
	{
		return new Iterator<T>()
		{
			@SuppressWarnings("unchecked")
			private Enumeration<T> e = enumeration;

			public boolean hasNext()
			{
				return e.hasMoreElements();
			}

			public T next()
			{
				return e.nextElement();
			}

			public void remove()
			{
				throw new UnsupportedOperationException("Remove is not supported on enumeration.");
			}
		};
	}

	private boolean attached = false;

	/** comma separated list of ids of elements to be deleted. */
	private final AppendingStringBuffer deleteIds = new AppendingStringBuffer();

	/**
	 * whether the whole tree is dirty (so the whole tree needs to be
	 * refreshed).
	 */
	private boolean dirtyAll = false;

	/**
	 * list of dirty items. if children property of these items is null, the
	 * chilren will be rebuild.
	 */
	private final List<TreeItem> dirtyItems = new ArrayList<TreeItem>();

	/**
	 * list of dirty items which need the DOM structure to be created for them
	 * (added items)
	 */
	private final List<TreeItem> dirtyItemsCreateDOM = new ArrayList<TreeItem>();

	/** counter for generating unique ids of every tree item. */
	private int idCounter = 0;

	/** Component whose children are tree items. */
	private TreeItemContainer itemContainer;

	/**
	 * map that maps TreeNode to TreeItem. TreeItems only exists for TreeNodes,
	 * that are visibled (their parents are not collapsed).
	 */
	private final Map<TreeNode, TreeItem> nodeToItemMap = new HashMap<TreeNode, TreeItem>();

	/**
	 * we need to track previous model. if the model changes, we unregister the
	 * tree from listeners of old model and register the tree as litener of new
	 * model.
	 */
	private TreeModel previousModel = null;

	/** root item of the tree. */
	private TreeItem rootItem = null;

	/** whether the tree root is shown. */
	private boolean rootLess = false;

	/** stores reference to tree state. */
	private ITreeState state;

	/**
	 * Tree constructor
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 */
	public AbstractTree(MarkupContainer parent, String id)
	{
		super(parent, id);
		init();
	}

	/**
	 * Tree constructor
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 */
	public AbstractTree(MarkupContainer parent, String id, IModel<TreeModel> model)
	{
		super(parent, id, model);
		init();
	}

	/** called when all nodes are collapsed. */
	public final void allNodesCollapsed()
	{
		invalidateAll();
	}

	/** called when all nodes are expaned. */
	public final void allNodesExpanded()
	{
		invalidateAll();
	}

	/**
	 * Returns the TreeState of this tree.
	 * 
	 * @return Tree state instance
	 */
	public ITreeState getTreeState()
	{
		if (state == null)
		{
			state = newTreeState();

			// add this object as listener of the state
			state.addTreeStateListener(this);
			// FIXME: Where should we remove the listener?
		}
		return state;
	}

	/**
	 * This method is called before the onAttach is called. Code here gets
	 * executed before the items have been populated. 
	 */
	protected void onBeforeAttach() 
	{	
	}
	
	/**
	 * Called at the beginning of the request (not ajax request, unless we are
	 * rendering the entire component)
	 */
	@Override
	public void internalAttach()
	{		
		if (attached == false)
		{
			onBeforeAttach();
			
			checkModel();

			// Do we have to rebuld the whole tree?
			if (dirtyAll && rootItem != null)
			{
				clearAllItem();
			}
			else
			{
				// rebuild chilren of dirty nodes that need it
				rebuildDirty();
			}

			// is root item created? (root item is null if the items have not
			// been created yet, or the whole tree was dirty and clearAllITem
			// has been called
			if (rootItem == null)
			{
				TreeNode rootNode = (TreeNode)getModelObject().getRoot();
				if (rootNode != null)
				{
					if (isRootLess())
					{
						rootItem = newTreeItem(rootNode, -1);
					}
					else
					{
						rootItem = newTreeItem(rootNode, 0);
					}
					buildItemChildren(rootItem);
				}
			}

			attached = true;
		}
		super.internalAttach();
	}

	/**
	 * @see wicket.MarkupContainer#internalDetach()
	 */
	@Override
	public void internalDetach()
	{
		super.internalDetach();
		attached = false;
	}

	/**
	 * Call to refresh the whole tree. This should only be called when the
	 * roodNode has been replaced or the entiry tree model changed.
	 */
	public final void invalidateAll()
	{
		updated();
		this.dirtyAll = true;
	}

	/**
	 * @return whether the tree root is shown
	 */
	public final boolean isRootLess()
	{
		return rootLess;
	};

	/**
	 * @see wicket.markup.html.tree.ITreeStateListener#nodeCollapsed(javax.swing.tree.TreeNode)
	 */
	public final void nodeCollapsed(TreeNode node)
	{
		if (isNodeVisible(node) == true)
		{
			invalidateNodeWithChildren(node);
		}
	}

	/**
	 * @see wicket.markup.html.tree.ITreeStateListener#nodeExpanded(javax.swing.tree.TreeNode)
	 */
	public final void nodeExpanded(TreeNode node)
	{
		if (isNodeVisible(node) == true)
		{
			invalidateNodeWithChildren(node);
		}
	}

	/**
	 * @see wicket.markup.html.tree.ITreeStateListener#nodeSelected(javax.swing.tree.TreeNode)
	 */
	public final void nodeSelected(TreeNode node)
	{
		if (isNodeVisible(node))
		{
			invalidateNode(node, true);
		}
	}

	/**
	 * @see wicket.markup.html.tree.ITreeStateListener#nodeUnselected(javax.swing.tree.TreeNode)
	 */
	public final void nodeUnselected(TreeNode node)
	{
		if (isNodeVisible(node))
		{
			invalidateNode(node, true);
		}
	}

	/**
	 * Sets whether the root of the tree should be visible.
	 * 
	 * @param rootLess
	 *            whether the root should be visible
	 */
	public void setRootLess(boolean rootLess)
	{
		if (this.rootLess != rootLess)
		{
			this.rootLess = rootLess;
			invalidateAll();

			// if the tree is in rootless mode, make sure the root node is
			// expanded
			if (rootLess == true && getModelObject() != null)
			{
				getTreeState().expandNode((TreeNode)getModelObject().getRoot());
			}
		}
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
	 */
	public final void treeNodesChanged(TreeModelEvent e)
	{
		// has root node changed?
		if (e.getChildren() == null)
		{
			if (rootItem != null)
			{
				invalidateNode(rootItem.getModelObject(), true);
			}
		}
		else
		{
			// go through all changed nodes
			for (Object child : e.getChildren())
			{
				TreeNode node = (TreeNode)child;
				if (isNodeVisible(node))
				{
					// if the nodes is visible invalidate it
					invalidateNode(node, true);
				}
			}
		}
	};

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
	 */
	public final void treeNodesInserted(TreeModelEvent e)
	{
		// get the parent node of inserted nodes
		TreeNode parent = (TreeNode)e.getTreePath().getLastPathComponent();

		if (isNodeVisible(parent) && isNodeExpanded(parent))
		{
			TreeItem parentItem = nodeToItemMap.get(parent);
			for (int i = 0; i < e.getChildren().length; ++i)
			{
				TreeNode node = (TreeNode)e.getChildren()[i];
				int index = e.getChildIndices()[i];
				TreeItem item = newTreeItem(node, parentItem.getLevel() + 1);
				parentItem.getChildren().add(index, item);

				dirtyItems.add(item);
				dirtyItemsCreateDOM.add(item);
			}
		}
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
	 */
	public final void treeNodesRemoved(TreeModelEvent e)
	{
		// get the parent node of inserted nodes
		TreeNode parent = (TreeNode)e.getTreePath().getLastPathComponent();
		TreeItem parentItem = nodeToItemMap.get(parent);

		if (isNodeVisible(parent) && isNodeExpanded(parent))
		{

			for (int i = 0; i < e.getChildren().length; ++i)
			{
				TreeNode node = (TreeNode)e.getChildren()[i];

				TreeItem item = nodeToItemMap.get(node);
				if (item != null)
				{
					parentItem.getChildren().remove(item);

					// go though item children and remove every one of them
					visitItemChildren(item, new IItemCallback()
					{
						public void visitItem(TreeItem item)
						{
							removeItem(item);
						}
					});

					removeItem(item);
				}
			}
		}
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
	 */
	public final void treeStructureChanged(TreeModelEvent e)
	{
		// get the parent node of changed nodes
		TreeNode node = (TreeNode)e.getTreePath().getLastPathComponent();

		// has the tree root changed?
		if (e.getTreePath().getPathCount() == 1 && node.equals(rootItem.getModelObject()))
		{
			invalidateAll();
		}
		else
		{
			invalidateNodeWithChildren(node);
		}
	}

	/**
	 * Updates the changed portions of the tree using given AjaxRequestTarget.
	 * Call this method if you modified the tree model during an ajax request
	 * target and you want to partially update the component on page. Make sure
	 * that the tree model has fired the proper listener functions.
	 * 
	 * @param target
	 *            Ajax request target used to send the update to the page
	 */
	public final void updateTree(final AjaxRequestTarget target)
	{
		if (target == null)
		{
			return;
		}

		// check whether the model hasn't changed
		checkModel();

		// is the whole tree dirty
		if (dirtyAll)
		{
			// render entire tree component
			target.addComponent(this);
		}
		else
		{
			// remove DOM elements that need to be removed
			if (deleteIds.length() != 0)
			{
				String js = getElementsDeleteJavascript();

				// add the javascript to target
				target.prependJavascript(js);
			}

			// We have to repeat this as long as there are any dirty items to be created.
			// The reason why we can't do this in one pass is that some of the items
			// may need to be inserted after items that has not been inserted yet, so we have
			// to detect those and wait until the items they depend on are inserted.
			while (dirtyItemsCreateDOM.isEmpty() == false)
			{
				for (Iterator<TreeItem> i = dirtyItemsCreateDOM.iterator(); i.hasNext();)
				{
					TreeItem item = i.next();
					TreeItem parent = item.getParentItem();
					int index = parent.getChildren().indexOf(item);
					TreeItem previous;
					// we need item before this (in dom structure)
					if (index == 0)
					{
						previous = parent;
					}
					else
					{
						previous = (TreeItem)parent.getChildren().get(index - 1);
						// get the last item of previous item subtree
						while (previous.getChildren() != null && previous.getChildren().size() > 0)
						{
							previous = (TreeItem)previous.getChildren().get(
									previous.getChildren().size() - 1);
						}
					}
					// check if the previous item isn't waiting to be inserted
					if (dirtyItemsCreateDOM.contains(previous) == false)
					{
						// it's already in dom, so we can use it as point of insertion
						target.prependJavascript("Wicket.Tree.createElement(\"" + item.getMarkupId()
								+ "\"," + "\"" + previous.getMarkupId() + "\")");
						
						// remove the item so we don't process it again
						i.remove();
					}
					else
					{
						// we don't do anything here, inserting this item will have to wait 
						// until the previous item gets inserted 
					}
				}
			}		

			// iterate through dirty items
			for (TreeItem item : dirtyItems)
			{
				// does the item need to rebuild children?
				if (item.getChildren() == null)
				{
					// rebuld the children
					buildItemChildren(item);

					// set flag on item so that it renders itself together with
					// it's children
					item.setRenderChildren(true);
				}

				// add the component to target
				target.addComponent(item);
			}

			// clear dirty flags
			updated();
		}
	}

	/**
	 * Returns whether the given node is expanded.
	 * 
	 * @param node
	 *            The node to inspect
	 * @return true if the node is expanded, false otherwise
	 */
	protected final boolean isNodeExpanded(TreeNode node)
	{
		// In root less mode the root node is always expanded
		if (isRootLess() && rootItem != null && rootItem.getModelObject().equals(node))
		{
			return true;
		}

		return getTreeState().isNodeExpanded(node);
	}

	/**
	 * Creates the TreeState, which is an object where the current state of tree
	 * (which nodes are expanded / collapsed, selected, ...) is stored.
	 * 
	 * @return Tree state instance
	 */
	protected ITreeState newTreeState()
	{
		return new DefaultTreeState();
	}

	/**
	 * Called after the rendering of tree is complete. Here we clear the dirty
	 * flags.
	 */
	@Override
	protected void onAfterRender()
	{
		// rendering is complete, clear all dirty flags and items
		updated();
	}

	/**
	 * This method is called after creating every TreeItem. This is the place
	 * for adding components on item (junction links, labels, icons...)
	 * 
	 * @param item
	 *            newly created tree item. The node can be obtained as
	 *            item.getModelObject()
	 * 
	 * @param level
	 *            how deep the component is in tree hierarchy (0 for root item)
	 */
	protected abstract void populateTreeItem(WebMarkupContainer<TreeNode> item, int level);

	/**
	 * Builds the children for given TreeItem. It recursively traverses children
	 * of it's TreeNode and creates TreeItem for every visible TreeNode.
	 * 
	 * @param item
	 *            The parent tree item
	 */
	private final void buildItemChildren(TreeItem item)
	{
		List<TreeItem> items;

		// if the node is expanded
		if (isNodeExpanded(item.getModelObject()))
		{
			// build the items for children of the items' treenode.
			items = buildTreeItems(nodeChildren(item.getModelObject()), item.getLevel() + 1);
		}
		else
		{
			// it's not expanded, just set children to an empty list
			items = Collections.emptyList();
		}

		item.setChildren(items);
	}

	/**
	 * Builds (recursively) TreeItems for the given Iterable of TreeNodes.
	 * 
	 * @param nodes
	 *            The nodes to build tree items for
	 * @param level
	 *            The current level
	 * @return List with new tree items
	 */
	private final List<TreeItem> buildTreeItems(Iterable<TreeNode> nodes, int level)
	{
		List<TreeItem> result = new ArrayList<TreeItem>();

		// for each node
		for (TreeNode node : nodes)
		{
			// create tree item
			TreeItem item = newTreeItem(node, level);

			// builds it children (recursively)
			buildItemChildren(item);

			// add item to result
			result.add(item);
		}

		return result;
	}

	/**
	 * Checks whether the model has been chaned, and if so unregister and
	 * register listeners.
	 */
	private final void checkModel()
	{
		// find out whether the model object (the TreeModel) has been changed
		TreeModel model = getModelObject();
		if (model != previousModel)
		{
			if (previousModel != null)
			{
				previousModel.removeTreeModelListener(this);
			}

			previousModel = model;

			if (model != null)
			{
				model.addTreeModelListener(this);
			}
			// model has been changed, redraw whole tree
			invalidateAll();
		}
	}

	/**
	 * Removes all TreeItem components.
	 */
	private final void clearAllItem()
	{
		visitItemAndChildren(rootItem, new IItemCallback()
		{
			public void visitItem(TreeItem item)
			{
				item.remove();
			}
		});
		rootItem = null;
	}

	/**
	 * Returns the javascript used to delete removed elements.
	 * 
	 * @return The javascript
	 */
	private String getElementsDeleteJavascript()
	{
		// build the javascript call
		final AppendingStringBuffer buffer = new AppendingStringBuffer(100);

		buffer.append("Wicket.Tree.removeNodes(\"");

		// first parameter is the markup id of tree (will be used as prefix to
		// build ids of child items
		buffer.append(getMarkupId() + "_\",[");

		// append the ids of elements to be deleted
		buffer.append(deleteIds);

		// does the buffer end if ','?
		if (buffer.endsWith(","))
		{
			// it does, trim it
			buffer.setLength(buffer.length() - 1);
		}

		buffer.append("]);");

		return buffer.toString();
	}

	//
	// State and Model's callbacks
	//

	/**
	 * returns the short version of item id (just the number part).
	 * 
	 * @param item
	 *            The tree item
	 * @return The id
	 */
	private String getShortItemId(TreeItem item)
	{
		// show much of component id can we skip? (to minimize the length of
		// javascript being sent)
		final int skip = getMarkupId().length() + 1; // the length of id of
		// tree and '_'.
		return item.getMarkupId().substring(skip);
	}

	/**
	 * Initialize the component.
	 */
	private final void init()
	{
		// disable versioning for the tree
		setVersioned(false);

		// we need id when we are replacing the whole tree
		setOutputMarkupId(true);

		// create container for tree items
		itemContainer = new TreeItemContainer(this, "i");

		add(HeaderContributor.forJavaScript(JAVASCRIPT.getScope(), JAVASCRIPT.getName()));
	}

	/**
	 * Invalidates single node (without children). On the next render, this node
	 * will be updated. Node will not be rebuilt, unless forceRebuild is true.
	 * TODO Implement forceRebuild
	 * 
	 * @param node
	 *            The node to invalidate
	 * @param forceRebuld
	 */
	private final void invalidateNode(TreeNode node, boolean forceRebuld)
	{
		if (dirtyAll == false)
		{
			// get item for this node
			TreeItem item = nodeToItemMap.get(node);

			if (forceRebuld)
			{
				// recreate the item
				int level = item.getLevel();
				List<TreeItem> children = item.getChildren();
				String id = item.getId();

				// store the parent of old item
				TreeItem parent = item.getParentItem();

				// if the old item has a parent, store it's index
				int index = parent != null ? parent.getChildren().indexOf(item) : -1;

				item.remove();

				item = newTreeItem(node, level, id);
				item.setChildren(children);

				// was the item an root item?
				if (parent == null)
				{
					rootItem = item;
				}
				else
				{
					parent.getChildren().set(index, item);
				}
			}

			if (item != null)
			{
				dirtyItems.add(item);
			}
		}
	}

	/**
	 * Invalidates node and it's children. On the next render, the node and
	 * children will be updated. Node and children will be rebuilt.
	 * 
	 * @param node
	 *            The node to invalidate
	 */
	private final void invalidateNodeWithChildren(TreeNode node)
	{
		if (dirtyAll == false)
		{
			// get item for this node
			TreeItem item = nodeToItemMap.get(node);

			// is the item visible?
			if (item != null)
			{
				// go though item children and remove every one of them
				visitItemChildren(item, new IItemCallback()
				{
					public void visitItem(TreeItem item)
					{
						removeItem(item);
					}
				});

				// set children to null so that they get rebuild
				item.setChildren(null);

				// add item to dirty items
				dirtyItems.add(item);
			}
		}
	}

	/**
	 * Returns whether the given node is visibled, e.g. all it's parents are
	 * expanded.
	 * 
	 * @param node
	 *            The node to inspect
	 * @return true if the node is visible, false otherwise
	 */
	private final boolean isNodeVisible(TreeNode node)
	{
		while (node.getParent() != null)
		{
			if (isNodeExpanded(node.getParent()) == false)
			{
				return false;
			}
			node = node.getParent();
		}
		return true;
	}

	/**
	 * Creates a tree item for given node.
	 * 
	 * @param node
	 *            The tree node
	 * @param level
	 *            The level
	 * @return The new tree item
	 */
	private final TreeItem newTreeItem(TreeNode node, int level)
	{
		return new TreeItem(itemContainer, "" + idCounter++, node, level);
	}

	/**
	 * Creates a tree item for given node with specified id.
	 * 
	 * @param node
	 *            The tree node
	 * @param level
	 *            The level
	 * @param id
	 *            the component id
	 * @return The new tree item
	 */
	private final TreeItem newTreeItem(TreeNode node, int level, String id)
	{
		return new TreeItem(itemContainer, id, node, level);
	}

	/**
	 * Return the representation of node children as Iterable interface.
	 * 
	 * @param node
	 *            The tree node
	 * @return iterable presentation of node children
	 */
	private final Iterable<TreeNode> nodeChildren(TreeNode node)
	{
		return toIterable(node.children());
	}

	/**
	 * Rebuilds children of every item in dirtyItems that needs it. This method
	 * is called for non-partial update.
	 */
	private final void rebuildDirty()
	{
		// go through dirty items
		for (TreeItem item : dirtyItems)
		{
			// item chilren need to be rebuilt
			if (item.getChildren() == null)
			{
				buildItemChildren(item);
			}
		}
	}

	/**
	 * Removes the item, appends it's id to deleteIds. This is called when a
	 * items parent is being deleted or rebuilt.
	 * 
	 * @param item
	 *            The item to remove
	 */
	private void removeItem(TreeItem item)
	{
		// even if the item is dirty it's no longer necessary to update id
		dirtyItems.remove(item);

		// if we needed to create DOM element, we no longer do
		dirtyItemsCreateDOM.remove(item);

		// add items id (it's short version) to ids of DOM elements that will be
		// removed
		deleteIds.append(getShortItemId(item));
		deleteIds.append(",");

		// remove the id
		// note that this doesn't update item's parent's children list
		item.remove();
	}

	/**
	 * Calls after the tree has been rendered. Clears all dirty flags.
	 */
	private final void updated()
	{
		this.dirtyAll = false;
		this.dirtyItems.clear();
		this.dirtyItemsCreateDOM.clear();
		deleteIds.clear(); // FIXME: Recreate it to save some space?
	}

	/**
	 * Call the callback#visitItem method for the given item and all it's
	 * chilren.
	 * 
	 * @param item
	 *            The tree item
	 * @param callback
	 *            item call back
	 */
	private final void visitItemAndChildren(TreeItem item, IItemCallback callback)
	{
		callback.visitItem(item);
		visitItemChildren(item, callback);
	}

	/**
	 * Call the callback#visitItem method for every child of given item.
	 * 
	 * @param item
	 *            The tree item
	 * @param callback
	 *            The callback
	 */
	private final void visitItemChildren(TreeItem item, IItemCallback callback)
	{
		if (item.getChildren() != null)
		{
			for (TreeItem i : item.getChildren())
			{
				visitItemAndChildren(i, callback);
			}
		}
	}

	/**
	 * Returns the component associated with given node, or null, if node is not
	 * visible. This is useful in situations when you want to touch the node
	 * element in html.
	 * 
	 * @param node
	 *            Tree node
	 * @return Component associated with given node, or null if node is not
	 *         visible.
	 */
	public Component getNodeComponent(TreeNode node)
	{
		return (Component)nodeToItemMap.get(node);
	}
}
