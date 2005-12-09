package wicket.extensions.markup.html.repeater.data.table.filter;

import java.io.Serializable;

/**
 * Locator that locates the object that represents the state of the filter.
 * Usually it is convinient to let the data provider object implement this
 * interface so that the data provider can be itself used to locate the filter
 * state object. This also makes it easy for the data provider to locate the
 * filter state which it will most likely need to filter the data.
 * <p>
 * Example
 * 
 * <pre>
 *    class UserDataProvider extends SortableDataProvider implements IFilterStateLocator {
 *      private User filterBean=new User;
 *      
 *      public getFilterState() { return filterBean; }
 *      public setFilterState(Object o) { filterBean=(User)o; }
 *      
 *      public Iterator iterate(int start, int count) {
 *        getUserDao().find(start, count, filterBean);
 *      }
 *    }
 * </pre>
 * 
 * 
 * @author igor
 * 
 */
public interface IFilterStateLocator extends Serializable
{
	/**
	 * @return object that represents the state of the filter toolbar
	 */
	Object getFilterState();

	/**
	 * Setter for the filter state object
	 * 
	 * @param state
	 *            filter state object
	 */
	void setFilterState(Object state);
}
