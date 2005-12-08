package wicket.extensions.markup.html.repeater.util;

import java.util.Iterator;

import wicket.model.IModel;

/**
 * Iterator over an array. Implementation must provide
 * {@link ArrayIteratorAdapter#model(Object) } method to wrap each item in a
 * model before it is returned through {@link ArrayIteratorAdapter#next() }
 * method.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class ArrayIteratorAdapter implements Iterator
{
	private Object[] array;
	private int pos = 0;

	/**
	 * Constructor
	 * 
	 * @param array
	 */
	public ArrayIteratorAdapter(Object[] array)
	{
		this.array = array;
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
		throw new UnsupportedOperationException("remove() is not allowed");
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return pos < array.length;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	public Object next()
	{
		return model(array[pos++]);
	}

	/**
	 * Resets the iterator position back to the beginning of the array
	 */
	public void reset()
	{
		pos = 0;
	}

	/**
	 * This method is used to wrap the provided object with an implementation of
	 * IModel. The provided object is guaranteed to be returned from the
	 * delegate iterator.
	 * 
	 * @param object
	 *            object to be wrapped
	 * @return IModel wrapper for the object
	 */
	abstract protected IModel model(Object object);


}
