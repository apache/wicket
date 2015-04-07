package org.apache.wicket.protocol.ws.api;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a collection of filters. Facilitates invocation of filtering on each filter.
 *
 * @author Gergely Nagy
 *
 * @param <T>
 *            type of filters
 */
public class FilterCollection<T> implements Serializable, Iterable<T> {

	private static final long serialVersionUID = -7389583130277632264L;

	/** list of listeners */
	private final List<T> filters = new CopyOnWriteArrayList<>();

	/**
	 * Adds a filter to this set of filters.
	 *
	 * @param filter
	 *            The filter to add
	 * @return {@code true} if the filter was added
	 */
	public boolean add(final T filter)
	{
		if (filter == null)
		{
			return false;
		}
		filters.add(filter);
		return true;
	}

	/**
	 * Removes a filter from this set.
	 *
	 * @param filter
	 *            The filter to remove
	 */
	public void remove(final T filter)
	{
		filters.remove(filter);
	}

	/**
	 * Returns an iterator that can iterate the filter.
	 *
	 * @return an iterator that can iterate the filters.
	 */
	@Override
	public Iterator<T> iterator() {
		return filters.iterator();
	}

}
