package wicket.extensions.markup.html.repeater.data;

import java.util.Iterator;

import wicket.extensions.markup.html.repeater.pageable.AbstractPageableView;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Base class for data views. Builds on AbstractPageableView to make it possible
 * to provide data via the IDataProvider as opposed to AbstravtPageableView's
 * builtin method.
 * <p>
 * <u>Notice:</u> The provided implementation of the IDataProvider is stored as
 * the model object of the dataview so it is unwise to provide an anonymous
 * implementation of the IDataProvider because it will serialize its outer class
 * if and when the model of the dataview is serialized.</u>
 * 
 * @see wicket.extensions.markup.html.repeater.pageable.AbstractPageableView
 * @see wicket.Component#modelChanging()
 * @see wicket.Component#modelChanged()
 * 
 * @author igor
 * 
 */
public abstract class AbstractDataView extends AbstractPageableView
{
	/**
	 * @param id
	 *            component id
	 */
	public AbstractDataView(String id)
	{
		super(id);
	}

	/**
	 * @param id
	 *            component id
	 * @param dataProvider
	 *            data provider
	 */
	public AbstractDataView(String id, IDataProvider dataProvider)
	{
		super(id, new Model(dataProvider));
	}


	/**
	 * @param id
	 *            component id
	 * @param model
	 *            component model
	 */
	public AbstractDataView(String id, IModel model)
	{
		super(id, model);
	}


	/**
	 * @return data provider associated with this view
	 */
	protected final IDataProvider getDataProvider()
	{
		IDataProvider dataProvider = (IDataProvider)getModelObject();
		return (dataProvider != null) ? dataProvider : EmptyDataProvider.getInstance();
	}


	protected final Iterator getItemModels(int offset, int count)
	{
		return new ModelIterator(getDataProvider(), offset, count);
	}

	/**
	 * Helper class that converts input from IDataProvider to an iterator over
	 * view items.
	 * 
	 * @author igor
	 * 
	 */
	private static final class ModelIterator implements Iterator
	{
		private Iterator items;
		private IDataProvider dataProvider;
		private int max;
		private int index;

		/**
		 * Constructor
		 * 
		 * @param dataProvider
		 *            data provider
		 * @param offset
		 *            index of first item
		 * @param count
		 *            max number of items to return
		 */
		public ModelIterator(IDataProvider dataProvider, int offset, int count)
		{
			this.items = dataProvider.iterator(offset, count);
			this.dataProvider = dataProvider;
			this.max = count;
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext()
		{
			return items.hasNext() && (index < max);
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next()
		{
			index++;
			return dataProvider.model(items.next());
		}
	}


	protected final int internalGetItemCount()
	{
		return getDataProvider().size();
	}

}
