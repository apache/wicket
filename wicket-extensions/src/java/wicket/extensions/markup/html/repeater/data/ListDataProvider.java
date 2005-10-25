package wicket.extensions.markup.html.repeater.data;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import wicket.model.IModel;
import wicket.model.Model;

/**
 * Allows the use of lists with dataview. The only requirement is that either
 * list items must be serializable or model(Object) needs to be overridden to
 * provide the proper model implementation.
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * 
 */
public class ListDataProvider implements IDataProvider
{
	private static final long serialVersionUID = 1L;
	
	/** reference to the list used as dataprovider for the dataview */
	private List list;

	/**
	 * 
	 * @param list
	 *            the list used as dataprovider for the dataview
	 */
	public ListDataProvider(List list)
	{
		if (list == null)
		{
			throw new IllegalArgumentException("argument [list] cannot be null");
		}

		this.list = list;
	}

	/**
	 * @see IDataProvider#iterator(int, int)
	 */
	public Iterator iterator(final int first, final int count)
	{
		return list.listIterator(first);
	}

	/**
	 * @see IDataProvider#size()
	 */
	public int size()
	{
		return list.size();
	}

	/**
	 * @see IDataProvider#model(Object)
	 */
	public IModel model(Object object)
	{
		return new Model((Serializable)object);
	}

}
