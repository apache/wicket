/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
