package wicket.extensions.markup.html.repeater.data.sort;

import java.io.Serializable;


/**
 * Implementation of ISortState that can keep track of sort information for a
 * single property.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class SingleSortState implements ISortState, Serializable
{
	private static final long serialVersionUID = 1L;

	SortParam param;

	/**
	 * @see wicket.extensions.markup.html.repeater.data.sort.ISortState#setPropertyState(java.lang.String,
	 *      int)
	 */
	public void setPropertyState(String property, int dir)
	{
		if (property == null)
		{
			throw new IllegalArgumentException("argument [property] cannot be null");
		}

		param = new SortParam(property, dir == ISortState.ASCENDING);
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.sort.ISortState#getPropertyState(java.lang.String)
	 */
	public int getPropertyState(String property)
	{
		if (property == null)
		{
			throw new IllegalArgumentException("argument [property] cannot be null");
		}

		if (param == null||!param.getProperty().equals(property))
		{
			return NONE;
		}
		else if (param.isAscending())
		{
			return ASCENDING;
		}
		else
		{
			return DESCENDING;
		}

	}

	/**
	 * @return current sort state
	 */
	public SortParam getSort()
	{
		return param;
	}

	/**
	 * Sets the current sort state
	 * 
	 * @param param
	 *            parameter containing new sorting information
	 */
	public void setSort(SortParam param)
	{
		this.param = param;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
			return "[SingleSortState sort="+((param==null)?"null":param.toString())+"]";
	}

}
