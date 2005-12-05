package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.Component;
import wicket.extensions.markup.html.repeater.data.table.IColumn;
import wicket.markup.html.form.Form;

/**
 * Interface that represents a filterable column.
 * 
 * @author Igor Vaynberg (ivaynberg)
 *
 */
public interface IFilteredColumn extends IColumn
{
	/**
	 * Returns the filter component used to filter this column. 
	 * 
	 * @param componentId
	 *            component id for returned filter component
	 * @return component that will be used to filter this column
	 */
	Component getFilter(String componentId, Form form);

}
