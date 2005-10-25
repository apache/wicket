package wicket.extensions.markup.html.repeater.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import wicket.extensions.markup.html.repeater.data.sort.ISortableDataProvider;
import wicket.model.IModel;

/**
 * A convienience class to represent an empty data provider.
 * 
 * @author philk
 */
public class EmptyDataProvider implements ISortableDataProvider
{
	private static final long serialVersionUID = 1L;
	
	private static EmptyDataProvider INSTANCE = new EmptyDataProvider();

	/**
	 * @return the singleton instance of this class
	 */
	public static EmptyDataProvider getInstance()
	{
		return INSTANCE;
	}

	public void addSort(String property)
	{
	}

	public List getSortList()
	{
		return Collections.EMPTY_LIST;
	}

	public SortState getSortState(String property)
	{
		return null;
	}

	public Iterator iterator(int first, int count)
	{
		return Collections.EMPTY_LIST.iterator();
	}

	public int size()
	{
		return 0;
	}

	public IModel model(Object object)
	{
		return null;
	}
}
