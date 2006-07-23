package wicket.xtree;

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
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.string.AppendingStringBuffer;

/**
 * This class encapsulates the logic for displaying and (partial) updating the tree.
 * Actual presentation is out of scope of this class. 
 * User should derive they own tree (if needed) from {@link DefaultAbstractTree} or 
 * {@link SimpleTree} (recommended).  
 * @author Matej Knopp
 */
public abstract class AbstractTree extends Panel<TreeModel> implements TreeStateListener, TreeModelListener 
{

	/**
	 * Tree constructor
	 * @param parent
	 * @param id
	 * @param rootLess whether the tree root should be hidden or shown
	 */
	public AbstractTree(MarkupContainer parent, String id) 
	{
		super(parent, id);
		init();		
	}

	/**
	 * Tree constructor
	 * @param parent
	 * @param id
	 * @param model
	 * @param rootLess whether the tree root should be hidden or shown
	 */
	public AbstractTree(MarkupContainer parent, String id, IModel<TreeModel> model) 
	{
		super(parent, id, model);
		init();
	}
	
	// whether the tree root is shown
	private boolean rootLess = false;
	
	/**
	 * Sets whether the root of the tree should be visible. 
	 */
	public void setRootLess(boolean rootLess) 
	{
		if (this.rootLess != rootLess)
		{
			this.rootLess = rootLess;
			invalidateAll();
			
			// if the tree is in rootless mode, make sure the root node is expanded
			if (rootLess == true && getModelObject() != null)
			{
				getTreeState().expandNode((TreeNode)getModelObject().getRoot());
			}
		}
	}
	
	/**
	 * @return whether the tree root is shown
	 */
	public final boolean isRootLess() 
	{
		return rootLess;
	}

	/** Reference to the javascript file. */
	private static final PackageResourceReference JAVASCRIPT = 
		new PackageResourceReference(AbstractTree.class, "res/tree.js");
	
	
	/**
	 * Initialize the component.
	 */
	private final void init() 
	{
		// we need id when we are replacing the whole tree
		setOutputMarkupId(true);
		
		// create container for tree items
		itemContainer = new TreeItemContainer(this, "i");
		
		add(HeaderContributor.forJavaScript(JAVASCRIPT.getScope(), JAVASCRIPT.getName()));
	}
	
	private boolean attached = false;
	
	// we need to track previous model. if the model changes, we unregister the tree
	// from listeners of old model and register the tree as litener of new model
	private TreeModel previousModel = null;
	
	/**
	 * Checks whether the model has been chaned, and if so unregister and register listeners.
	 */
	private final void checkModel() {
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
	 * Called at the beginning of the request (not ajax request, unless we are rendering 
	 * the entire component)
	 */
	@Override
	protected void onAttach() 
	{		
		if (attached == false) 
		{
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
			// been created yet, or the whole tree was dirty and clearAllITem has been called 
			if (rootItem == null) 
			{				
				TreeNode rootNode = (TreeNode) getModelObject().getRoot();
				if (rootNode != null)
				{
					rootItem = createTreeItem(rootNode, 0);
					if (isRootLess())
					{
						rootItem.setVisible(false);
					}
					buildItemChildren(rootItem);
				}
			}			
			
			attached = true;
		}		
	}
	
	@Override
	protected void onDetach() 
	{
		attached = false;
	}
	
	/**
	 * Called after the rendering of tree is complete. Here we clear the dirty flags.
	 */
	@Override
	protected void onAfterRender() 
	{
		// rendering is complete, clear all dirty flags and items
		updated();
	}

	// Component whose children are tree items
	private TreeItemContainer itemContainer;
	
	// root item of the tree
	private TreeItem rootItem = null;
	
	// map that maps TreeNode to TreeItem. TreeItems only exists for TreeNodes, that are visibled
	// (their parents are not collapsed)
	private Map<TreeNode, TreeItem> nodeToItemMap = new HashMap<TreeNode, TreeItem>();

	/**
	 * This class represents one row in rendered tree (TreeNode). Only TreeNodes that are 
	 * visible (all their parent are expanded) have TreeItem created for them.
	 * @author Matej Knopp
	 */
	private class TreeItem extends WebMarkupContainer<TreeNode> 
	{
		public TreeItem(MarkupContainer parent, String id, final TreeNode node, int level) 
		{			
			super(parent, id, new Model<TreeNode>(node));
			
			nodeToItemMap.put(node, this);			
			this.level = level;
			setOutputMarkupId(true);

			populateTreeItem(this, level);
		}	
		
		@Override
		public String getMarkupId() {
			// this is overriden to produce id that begins with id of tree
			// if the tree has set (shorter) id in markup, we can use it to 
			// shorten the id of individual TreeItems			
			return AbstractTree.this.getMarkupId() + "_" + getId();
		}
		
		// tree item children - we need this to traverse items in correct order when rendering
		private List<TreeItem> children = null;
		
		// tree item level - how deep is this item in tree
		private int level;
		
		public List<TreeItem> getChildren()
		{
			return children;
		}
		
		public TreeItem getParentItem() {
			return nodeToItemMap.get(getModelObject().getParent());
		}
		
		public int getLevel()
		{
			return level;
		}
		
		public void setChildren(List<TreeItem> children)
		{
			this.children = children;
		}
		
		// whether this tree item should also render it's children to response.
		// this is set if we need the whole subtree rendered as one component
		// in ajax response, so that we can replace it in one step (replacing 
		// individual rows is very slow in javascript, therefore we replace the
		// whole subtree)
		private final static int FLAG_RENDER_CHILDREN = FLAG_RESERVED8;
		
		protected final void setRenderChildren(boolean value)
		{
			setFlag(FLAG_RENDER_CHILDREN, value);
		}
		
		protected final boolean isRenderChildren()
		{
			return getFlag(FLAG_RENDER_CHILDREN);
		}
		
		@Override
		protected void onRender(final MarkupStream markupStream)
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
	};
	
	/**
	 * Components that holds tree items. This is similiar to ListView, but it 
	 * renders tree items in the right order.
	 * @author Matej Knopp	 
	 */
	private class TreeItemContainer extends WebMarkupContainer 
	{
		public TreeItemContainer(MarkupContainer parent, String id) 
		{
			super(parent, id);
		}
		
		// renders the tree items, making sure that items are rendered in the order they should be 
		@Override
		protected void onRender(final MarkupStream markupStream) 
		{
			// Save position in markup stream
			final int markupStart = markupStream.getCurrentIndex();	
			
			// have we rendered at least one item?
			class Rendered 
			{
				boolean rendered = false;
			};
			final Rendered rendered = new Rendered();
			 			
			// is there a root item? (non-empty tree)
			if (rootItem != null) {
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
				
				if (isRootLess())
				{
					// vist just the children					
					visitItemChildren(rootItem, callback);
				}
				else
				{
					// visit item and it's children
					visitItemAndChildren(rootItem, callback);
				}
			}
			
			if (rendered.rendered == false)
			{
				// tree is empty, just move the markupStream
				markupStream.skipComponent();
			}
		}
		
		@Override	
		public void remove(Component component) 
		{
			// when a treeItem is removed, remove reference to it from nodeToItemMAp
			if (component instanceof TreeItem) 
			{
				nodeToItemMap.remove(((TreeItem)component).getModelObject());
			}
			super.remove(component);
		}
	}
	
	/**
	 * Interface for visiting individual tree items.
	 * @author Matej Knopp
	 */
	private interface IItemCallback 
	{
		public void visitItem(TreeItem item);
	}

	/**
	 * Call the callback#visitItem method for the given item and all it's chilren.  
	 */
	private final void visitItemAndChildren(TreeItem item, IItemCallback callback) 
	{
		callback.visitItem(item);
		visitItemChildren(item, callback);
	}
	
	/**
	 * Call the callback#visitItem method for every child of given item.
	 */
	private final void visitItemChildren(TreeItem item, IItemCallback callback) 
	{
		for (TreeItem i : item.getChildren()) 
		{
			visitItemAndChildren(i, callback);
		}
	}
	
	// counter for generating unique ids of every tree item
	private int idCounter = 0;
	
	/**
	 * Creates a tree item for given node.
	 */
	private final TreeItem createTreeItem(TreeNode node, int level) 
	{
		return new TreeItem(itemContainer, "" + idCounter++, node, level);
	}
	
	/**
	 * Creates a tree item for given node with specified id.
	 */
	private final TreeItem createTreeItem(TreeNode node, int level, String id) 
	{
		return new TreeItem(itemContainer, id, node, level);
	}	

	/**
	 * Builds the children for given TreeItem. It recursively traverses children of it's TreeNode 
	 * and creates TreeItem for every visible TreeNode.  
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
	 */
	private final List<TreeItem> buildTreeItems(Iterable<TreeNode> nodes, int level) 
	{
		List<TreeItem> result = new ArrayList<TreeItem>();		
	
		// for each node
		for (TreeNode node : nodes) {
			// create tree item
			TreeItem item = createTreeItem(node, level);
			
			// builds it children (recursively)
			buildItemChildren(item);
			
			// add item to result
			result.add(item);
		}
		
		return result;
	}
	
	/**
	 * Creates the TreeState, which is an object where the current state of tree
	 * (which nodes are expanded / collapsed, selected, ...) is stored. 
	 * @return
	 */
	protected TreeState createTreeState() 
	{
		return new DefaultTreeState();
	};
	
	// stores reference to tree state
	private TreeState state;
	
	/**
	 * Returns the TreeState of this tree.
	 */
	public TreeState getTreeState() 
	{
		if (state == null) 
		{	
			state = createTreeState();
			
			// add this object as listener of the state
			state.addTreeStateListener(this);
			// FIXME: Where should we remove the listener? 
		}
		return state;
	}
	
	/**
	 * Return the representation of node children as Iterable interface.
	 */
	private final Iterable<TreeNode> nodeChildren(TreeNode node) 
	{
		return toIterable(node.children());
	}
	
	/**
	 * Returns an iterator that iterates trough the enumeration.
	 */
	private final <T> Iterator<T> toIterator(final Enumeration enumeration)
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
	
	/**
	 * Returns the enumeration as iterable interface.
	 */
	private final <T> Iterable<T> toIterable(final Enumeration enumeration) 
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
	 * Calls after the tree has been rendered. Clears all dirty flags.
	 */
	private final void updated() 
	{
		this.dirtyAll = false;		
		this.dirtyItems.clear();		
		this.dirtyItemsCreateDOM.clear();
		deleteIds.clear(); // FIXME: Recreate it to save some space?		
	};
	
	// whether the whole tree is dirty (so the whole tree needs to be refreshed)
	private boolean dirtyAll = false;
	
	// list of dirty items. if children property of these items is null, the chilren will be rebuild
	private List<TreeItem> dirtyItems = new ArrayList<TreeItem>();
	
	// list of dirty items which need the DOM structure to be created for them (added items)
	private List<TreeItem> dirtyItemsCreateDOM = new ArrayList<TreeItem>();
	
	// comma separated list of ids of elements to be deleted 
	private AppendingStringBuffer deleteIds = new AppendingStringBuffer();
	
	// returns the short version of item id (just the number part)
	private String getShortItemId(TreeItem item) 
	{
		// show much of component id can we skip? (to minimize the length of javascript being sent)
		final int skip = getMarkupId().length() + 1; // the length of id of tree and '_'.
		return item.getMarkupId().substring(skip);
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
	 * Invalidates single node (without children). On the next render, this node will be updated.
	 * Node will not be rebuilt, unless forceRebuild is true. TODO Implement forceRebuild
	 */
	private final void invalidateNode(TreeNode node, boolean forceRebuld) 
	{
		// get item for this node
		TreeItem item = nodeToItemMap.get(node);
		
		if (forceRebuld) 
		{
			// recreate the item
			int level = item.getLevel();
			List<TreeItem> children = item.getChildren();
			String id = item.getId();
			
			item.remove();			
			
			item = createTreeItem(node, level, id);
			item.setChildren(children);			
		}
		
		if (item != null)
			dirtyItems.add(item);
	}
	
	/**
	 * Removes the item, appends it's id to deleteIds. 
	 * This is called when a items parent is being deleted or rebuilt.
	 */
	private void removeItem(TreeItem item) 
	{
		// even if the item is dirty it's no longer necessary to update id 
		dirtyItems.remove(item);
		
		// if we needed to create DOM element, we no longer do
		dirtyItemsCreateDOM.remove(item);
		
		// add items id (it's short version) to ids of DOM elements that will be removed
		deleteIds.append(getShortItemId(item));
		deleteIds.append(",");
		
		// remove the id
		// note that this doesn't update item's parent's children list		
		item.remove();		
	}
	
	/**
	 * Invalidates node and it's children. On the next render, the node and children will be updated.
	 * Node and children will be rebuilt.
	 */
	private final void invalidateNodeWithChildren(TreeNode node) 
	{
		// get item for this node
		TreeItem item = nodeToItemMap.get(node);
		
		// is the item visible?
		if (item != null) 
		{
			// go though item children and remove every one of them
			visitItemChildren(item, new IItemCallback() {
				public void visitItem(TreeItem item) {
					removeItem(item);
				}
			});
			
			// set children to null so that they get rebuild
			item.setChildren(null);
			
			// add item to dirty items
			dirtyItems.add(item);
		}
	}
	
	/**
	 * Rebuilds children of every item in dirtyItems that needs it.
	 * This method is called for non-partial update.
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
	 */
	private String getElementsDeleteJavascript() 
	{
		// build the javascript call
		final AppendingStringBuffer buffer = new AppendingStringBuffer(100);
		
		buffer.append("Wicket.Tree.removeNodes(\"");
		
		// first parameter is the markup id of tree (will be used as prefix to build ids of child items 
		buffer.append(getMarkupId()+"_\",[");
		
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
	
	/**
	 * Updates the changed portions of the tree using given AjaxRequestTarget.
	 * Call this method if you modified the tree model during an ajax request target
	 * and you want to partially update the component on page. Make sure that the 
	 * tree model has fired the proper listener functions.
	 * 
	 * @param target
	 * 			Ajax request target used to send the update to the page
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
				System.out.println(js);

				// add the javascript to target
				target.prependJavascript(js);
			}
			
			for (TreeItem item : dirtyItemsCreateDOM)
			{
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
					previous = parent.getChildren().get(index - 1);
					// get the last item of previous item subtree
					while (previous.getChildren() != null && previous.getChildren().size() > 0)
					{
						previous = previous.getChildren().get(previous.getChildren().size() - 1);
					}
				}
				target.prependJavascript("Wicket.Tree.createElement(\"" + item.getMarkupId() + "\"," +
						                                          "\"" + previous.getMarkupId() + "\")");				
			}
								
			// iterate through dirty items
			for (TreeItem item : dirtyItems) 
			{
				// does the item need to rebuild children?
				if (item.getChildren() == null)
				{
					// rebuld the children
					buildItemChildren(item);
					
					// set flag on item so that it renders itself together with it's children
					item.setRenderChildren(true);					
				}				
				
				// add the component to target
				target.addComponent(item);				
			}
		}
		
		// clear dirty flags
		updated();
	}	
	
	//
	// State and Model's callbacks
	//
	
	// called when all nodes are collapsed
	public final void allNodesCollapsed() 
	{		
		invalidateAll();
	}
	
	// called when all nodes are expaned
	public final void allNodesExpanded() 
	{
		invalidateAll();
	}
	
	// called when a particular node is collapsed
	public final void nodeCollapsed(TreeNode node) 
	{
		if (isNodeVisible(node) == true) 
		{
			invalidateNodeWithChildren(node);
		}
	}
	
	// called when a particular node is expanded
	public final void nodeExpanded(TreeNode node) 
	{
		if (isNodeVisible(node) == true) 
		{
			invalidateNodeWithChildren(node);
		}
	}
	
	// called when a particular node is selected
	public final void nodeSelected(TreeNode node) 
	{
		if (isNodeVisible(node))
		{
			invalidateNode(node, true);
		}
	}
	
	// called when a particular node is unselected
	public final void nodeUnselected(TreeNode node) 
	{
		if (isNodeVisible(node))
		{
			invalidateNode(node, true);
		}
	}
	
	// called when tree nodes (but not their children) have changed
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
			for (Object child: e.getChildren()) 
			{
				TreeNode node = (TreeNode) child;
				if (isNodeVisible(node))
				{
					// if the nodes is visible invalidate it
					invalidateNode(node, true);
				}
			}
		}
	}
		
	// called when tree nodes have been inserted
	public final void treeNodesInserted(TreeModelEvent e) 
	{
		// get the parent node of inserted nodes
		TreeNode parent = (TreeNode) e.getTreePath().getLastPathComponent();
		
		if (isNodeVisible(parent) && isNodeExpanded(parent))
		{
			TreeItem parentItem = nodeToItemMap.get(parent);
			for (int i = 0; i < e.getChildren().length; ++i)
			{
				TreeNode node = (TreeNode) e.getChildren()[i];
				int index = e.getChildIndices()[i];
				TreeItem item = createTreeItem(node, parentItem.getLevel() + 1);
				parentItem.getChildren().add(index, item);
				
				dirtyItems.add(item);
				dirtyItemsCreateDOM.add(item);
			}
		}
	}
	
	// called when certain tree nodes have been removed
	public final void treeNodesRemoved(TreeModelEvent e) 
	{
		//get the parent node of inserted nodes
		TreeNode parent = (TreeNode) e.getTreePath().getLastPathComponent();
		TreeItem parentItem = nodeToItemMap.get(parent);

		if (isNodeVisible(parent) && isNodeExpanded(parent))
		{

			for (int i = 0; i < e.getChildren().length; ++i) {
				TreeNode node = (TreeNode) e.getChildren()[i];
				
				System.out.println("Removing node " + node);
				
				TreeItem item = nodeToItemMap.get(node);
				if (item != null)
				{
					parentItem.getChildren().remove(item);

					// go though item children and remove every one of them
					visitItemChildren(item, new IItemCallback() {
						public void visitItem(TreeItem item) {
							removeItem(item);
						}
					});

					removeItem(item);
				}
			}
		}
	}
	
	// called when the tree structure has significanly changed
	public final void treeStructureChanged(TreeModelEvent e) 
	{
		// get the parent node of changed nodes		
		TreeNode node = (TreeNode) e.getTreePath().getLastPathComponent();
		
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
	 * Returns whether the given node is visibled, e.g. all it's parents are expanded.
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
	 * Returns whether the given node is expanded.
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
	 * This method is called after creating every TreeItem.
	 * This is the place for adding components on item (junction links, labels, icons...)
	 *  
	 * @param item 
	 * 			newly created tree item. The node can be obtained as item.getModelObject()
	 *  
	 * @param level 
	 * 			how deep the component is in tree hierarchy (0 for root item) 
	 */
	protected abstract void populateTreeItem(WebMarkupContainer<TreeNode> item, int level);
}
