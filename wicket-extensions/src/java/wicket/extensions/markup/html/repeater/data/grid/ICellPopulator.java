package wicket.extensions.markup.html.repeater.data.grid;

import java.io.Serializable;

import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.markup.html.list.ListItem;
import wicket.model.IModel;

public interface ICellPopulator extends Serializable
{
	/**
	 * Method used to populate a cell in the generated grid.
	 * 
	 * <b>Implementation MUST add a component to the cellItem using the
	 * component id provided by componentId argument, otherwise a
	 * WicketRuntimeException will be thrown</b>
	 * 
	 * @param cellItem
	 *            the list item representing the current table cell being
	 *            rendered
	 * @param componentId
	 *            the id of the component used to render the cell (only one
	 *            component can be added to the cell)
	 * @param rowModel
	 *            the object that represents the current row being processed
	 */
	void populateItem(final Item cellItem, final String componentId, final IModel rowModel);
}
