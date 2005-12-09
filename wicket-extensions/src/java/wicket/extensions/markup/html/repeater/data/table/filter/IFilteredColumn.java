package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.Component;
import wicket.extensions.markup.html.repeater.data.table.IColumn;

/**
 * Represents a data table column that can be filtered. The filter is
 * represented by a component returned from the getFilter() method.
 * 
 * @see IColumn
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IFilteredColumn extends IColumn
{
	/**
	 * Returns the component used by user to filter the column. If null is
	 * returned, no filter will be added.
	 * 
	 * @param componentId
	 *            component id for returned filter component
	 * @param form
	 *            FilterForm object for the toolbar. components can use this
	 *            form's model to access properties of the state object (<code>PropertyModel(form.getModel(), "property"</code>)
	 *            or retrieve the {@link IFilterStateLocator} object by using
	 *            {@link FilterForm#getStateLocator() }
	 * @return component that will be used to represent a filter for this
	 *         column, or null if no such component is desired
	 */
	Component getFilter(String componentId, FilterForm form);

}
