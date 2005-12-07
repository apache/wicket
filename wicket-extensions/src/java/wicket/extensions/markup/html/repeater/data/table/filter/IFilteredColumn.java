package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.Component;
import wicket.extensions.markup.html.repeater.data.table.IColumn;

/**
 * Represents a data table column that can be filtered via a component
 * 
 * @see IColumn
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IFilteredColumn extends IColumn
{
	/**
	 * Returns the component used by user to filter the column
	 * 
	 * @param componentId
	 *            component id for returned filter component
	 * @param form
	 *            FilterForm object for the toolbar. components can use this
	 *            form's model to access properties of the state object (<code>PropertyModel(form.getModel(), "property"</code>)
	 *            or retrieve the {@link IFilterStateLocator} object by using
	 *            {@link FilterForm#getLocator() }
	 * @return component that will be used to filter this column
	 */
	Component getFilter(String componentId, FilterForm form);

}
