package wicket.extensions.markup.html.repeater.data.sort;

import java.util.List;

public interface ISortState
{
	public static final int ASCENDING = 1;
	public static final int DESCENDING = -1;
	public static final int NONE = 0;

	/**
	 * Toggles property state. State toggles between ASCENDING and DESCENDING.
	 * If the property was not previously sorted the state toggles to ASCENDING.
	 * 
	 * @param property
	 *            the name of the property to sort on
	 * @param state
	 *            new sort state of the property
	 */
	public void setPropertyState(String property, int state);

	/**
	 * Gets the sort state of a property
	 * 
	 * @param property
	 *            sort property to be checked
	 * @return 1 if ascending, -1 if descending, 0 if none
	 */
	public int getPropertyState(String property);

}
