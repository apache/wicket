package wicket.extensions.markup.html.repeater.data;

import wicket.model.IModel;

/**
 * DataView is a pageable repeating view that uses the specified implementation
 * of IDataProvider to populate itself.
 * 
 * @author igor
 * 
 */
public abstract class DataView extends AbstractDataView
{

	/**
	 * @param id
	 *            component id
	 * @param dataProvider
	 *            data provider
	 */
	public DataView(String id, IDataProvider dataProvider)
	{
		super(id, dataProvider);
	}

	/**
	 * @param id
	 *            component id
	 * @param model
	 *            component model - model object must be instance of
	 *            IDataProvider
	 */
	public DataView(String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * @param id
	 *            component id
	 */
	public DataView(String id)
	{
		super(id);
	}

	/**
	 * Sets the number of items to be displayed per page
	 * 
	 * @param items
	 *            number of items to display per page
	 */
	public void setItemsPerPage(int items)
	{
		internalSetItemsPerPage(items);
	}

	/**
	 * @return number of items displayed per page
	 */
	public int getItemsPerPage()
	{
		return internalGetItemsPerPage();
	}

}
