/*
 * $Id: RefreshingView.java 5564 2006-04-28 16:05:28 -0700 (Fri, 28 Apr 2006)
 * ivaynberg $ $Revision$ $Date: 2006-04-28 16:05:28 -0700 (Fri, 28 Apr
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.repeater.refreshing;

import java.util.Iterator;

import wicket.MarkupContainer;
import wicket.extensions.markup.html.repeater.RepeatingView;
import wicket.extensions.markup.html.repeater.util.ModelIteratorAdapter;
import wicket.model.IModel;
import wicket.version.undo.Change;

/**
 * An abstract repeater view that provides refreshing functionality to its
 * subclasses. The view is refreshed every request, making it well suited for
 * displaying dynamic data.
 * <p>
 * The view is populated by implementing {@link RefreshingView#getItemModels() }
 * and {@link RefreshingView#populateItem(Item) } methods. RefreshingView builds
 * the items that will be rendered by looping over the models retrieved from
 * {@link RefreshingView#getItemModels() } and calling the
 * {@link RefreshingView#newItem(String, int, IModel) } to generate the child
 * item container followed by a call to
 * {@link RefreshingView#populateItem(Item) } to let the user populate the newly
 * created item container with custom components.
 * </p>
 * <p>
 * The provided {@link ModelIteratorAdapter} can make implementing
 * {@link RefreshingView#getItemModels() } easier if you have an iterator over
 * item objects.
 * </p>
 * 
 * @see RepeatingView
 * @see ModelIteratorAdapter
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class RefreshingView extends RepeatingView
{
	private static final long serialVersionUID = 1L;

	/**
	 * The item reuse strategy that will be used to recycle items when the page
	 * is changed or the view is redrawn.
	 * 
	 * @see IItemReuseStrategy
	 */
	private IItemReuseStrategy itemReuseStrategy;

	/**
	 * @see wicket.Component#Component(MarkupContainer, String)
	 */
	public RefreshingView(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer, String, IModel)
	 */
	public RefreshingView(MarkupContainer parent, final String id, IModel model)
	{
		super(parent, id, model);
	}

	/**
	 * Refresh the items in the view. Delegates the creation of items to the
	 * selected item reuse strategy
	 */
	@Override
	protected void internalOnAttach()
	{
		super.internalOnAttach();

		if (isVisibleInHierarchy())
		{

			IItemFactory itemFactory = new IItemFactory()
			{

				public Item newItem(MarkupContainer parent, int index, IModel model)
				{
					String id = RefreshingView.this.newChildId();
					Item item = RefreshingView.this.newItem(parent, id, index, model);
					RefreshingView.this.populateItem(item);
					return item;
				}

			};

			Iterator models = getItemModels();
			Iterator items = getItemReuseStrategy().getItems(RefreshingView.this, itemFactory, models, getItems());
			removeAll();
			addItems(items);
		}

	}

	/**
	 * Returns an iterator over models for items that will be added to this view
	 * 
	 * @return an iterator over models for items that will be added to this view
	 */
	protected abstract Iterator getItemModels();

	/**
	 * Populate the given Item container.
	 * <p>
	 * <b>be carefull</b> to add any components to the item and not the view
	 * itself. So, don't do:
	 * 
	 * <pre>
	 * add(new Label(&quot;foo&quot;, &quot;bar&quot;));
	 * </pre>
	 * 
	 * but:
	 * 
	 * <pre>
	 * item.add(new Label(&quot;foo&quot;, &quot;bar&quot;));
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param item
	 *            The item to populate
	 */
	protected abstract void populateItem(final Item item);

	/**
	 * Factory method for Item container. Item containers are simple
	 * MarkupContainer used to aggregate the user added components for a row
	 * inside the view.
	 * 
	 * @see Item
	 * @param id
	 *            component id for the new data item
	 * @param index
	 *            the index of the new data item
	 * @param model
	 *            the model for the new data item
	 * 
	 * @return DataItem created DataItem
	 */
	protected Item newItem(MarkupContainer parent, final String id, int index, final IModel model)
	{
		return new Item(parent, id, index, model);
	}

	/**
	 * @return iterator over item instances that exist as children of this view
	 */
	public Iterator getItems()
	{
		return iterator();
	}

	/**
	 * Add items to the view. Prior to this all items were removed so every
	 * request this function starts from a clean slate.
	 * 
	 * @param items
	 *            item instances to be added to this view
	 */
	protected void addItems(Iterator items)
	{
		while (items.hasNext())
		{
			((Item)items.next()).reAttach();
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// ITEM GENERATION
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * @return currently set item reuse strategy. Defaults to
	 *         <code>DefaultItemReuseStrategy</code> if none was set.
	 * 
	 * @see DefaultItemReuseStrategy
	 */
	public IItemReuseStrategy getItemReuseStrategy()
	{
		if (itemReuseStrategy == null)
		{
			return DefaultItemReuseStrategy.getInstance();
		}
		return itemReuseStrategy;
	}

	/**
	 * Sets the item reuse strategy. This strategy controls the creation of
	 * {@link Item}s.
	 * 
	 * @see IItemReuseStrategy
	 * 
	 * @param strategy
	 *            item reuse strategy
	 * @return this for chaining
	 */
	public RefreshingView setItemReuseStrategy(IItemReuseStrategy strategy)
	{
		if (strategy == null)
		{
			throw new IllegalArgumentException();
		}

		if (!strategy.equals(itemReuseStrategy))
		{
			if (isVersioned())
			{
				addStateChange(new Change()
				{
					private static final long serialVersionUID = 1L;

					private final IItemReuseStrategy old = itemReuseStrategy;

					@Override
					public void undo()
					{
						itemReuseStrategy = old;
					}

					@Override
					public String toString()
					{
						return "ItemsReuseStrategyChange[component: " + getPath() + ", reuse: "
								+ old + "]";
					}
				});
			}
			itemReuseStrategy = strategy;
		}
		return this;
	}


}
