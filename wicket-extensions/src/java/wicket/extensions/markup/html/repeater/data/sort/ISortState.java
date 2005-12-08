package wicket.extensions.markup.html.repeater.data.sort;

import java.io.Serializable;

/**
 * Interface used by OrderByLink to interact with any object that keeps track of
 * sorting state
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface ISortState extends Serializable
{
	/**
	 * property state representing ascending sort order
	 */
	public static final int ASCENDING = 1;
	/**
	 * property state representing descending sort order
	 */
	public static final int DESCENDING = -1;
	/**
	 * property state presenting not-sorted sort order
	 */
	public static final int NONE = 0;

	/**
	 * Sets sort order of the property
	 * 
	 * @param property
	 *            the name of the property to sort on
	 * @param state
	 *            new sort state of the property. must be one of ASCENDING,
	 *            DESCENDING, or NONE
	 */
	public void setPropertySortOrder(String property, int state);

	/**
	 * Gets the sort order of a property
	 * 
	 * @param property
	 *            sort property to be checked
	 * @return one of ASCENDING, DESCENDING, or NONE
	 */
	public int getPropertySortOrder(String property);

}
