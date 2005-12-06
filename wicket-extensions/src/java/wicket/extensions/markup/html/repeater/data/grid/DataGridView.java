package wicket.extensions.markup.html.repeater.data.grid;

import java.util.List;

import wicket.extensions.markup.html.repeater.data.IDataProvider;
import wicket.extensions.markup.html.repeater.refreshing.Item;

public class DataGridView extends AbstractDataGridView
{
	private static final long serialVersionUID = 1L;

	public DataGridView(String id, List/* <ICellPopulator> */populators, IDataProvider dataProvider)
	{
		super(id, (ICellPopulator[])populators.toArray(new ICellPopulator[populators.size()]),
				dataProvider);
	}

	public DataGridView(String id, ICellPopulator[] populators, IDataProvider dataProvider)
	{
		super(id, populators, dataProvider);
	}

	protected final void internalPostProcessCellItem(Item item)
	{
		postProcessCellItem(item);
	}

	protected final void internalPostProcessRowItem(Item item)
	{
		postProcessRowItem(item);
	}

	protected void postProcessRowItem(Item item)
	{
		// noop
	}

	protected void postProcessCellItem(Item item)
	{
		// noop
	}

	public ICellPopulator[] getPopulators()
	{
		return internalGetPopulators();
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

	/**
	 * @return data provider
	 */
	public IDataProvider getDataProvider()
	{
		return internalGetDataProvider();
	}


}
