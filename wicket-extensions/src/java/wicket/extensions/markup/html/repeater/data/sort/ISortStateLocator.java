package wicket.extensions.markup.html.repeater.data.sort;


/**
 * Locator interface for ISortState implementations. OrderByLink uses this
 * interface to locate and version ISortState objects.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface ISortStateLocator
{
	/**
	 * @return ISortState object
	 */
	ISortState getSortState();

	/**
	 * Setter for the sort state object
	 * 
	 * @param state
	 *            new sort state
	 */
	void setSortState(ISortState state);
}
