package wicket.extensions.markup.html.repeater.pageable;

import java.util.Iterator;

import wicket.model.IModel;

/**
 * Implementation of <code>IItemReuseStrategy</code> that returns new items
 * every time.
 * 
 * @see wicket.extensions.markup.html.repeater.pageable.IItemReuseStrategy
 * 
 * @author igor
 * 
 */
public class DefaultItemReuseStrategy implements IItemReuseStrategy
{
	private static final IItemReuseStrategy instance = new DefaultItemReuseStrategy();

	/**
	 * @return static instance of this strategy
	 */
	public static IItemReuseStrategy getInstance()
	{
		return instance;
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.pageable.IItemReuseStrategy#getItems(wicket.extensions.markup.html.repeater.pageable.IItemFactory, java.util.Iterator, java.util.Iterator)
	 */
	public Iterator getItems(final IItemFactory factory, final Iterator newModels,
			final Iterator existingItems)
	{
		return new Iterator()
		{
			private int index = 0;

			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			public boolean hasNext()
			{
				return newModels.hasNext();
			}

			public Object next()
			{
				final IModel model = (IModel)newModels.next();

				Item item = factory.newItem(index, model);
				index++;

				return item;
			}

		};
	}

}
