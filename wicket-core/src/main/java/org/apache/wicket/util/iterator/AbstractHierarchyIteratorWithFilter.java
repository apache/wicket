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
package org.apache.wicket.util.iterator;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;

/**
 * 
 * @author Juergen Donnerstag
 * @param <S>
 */
public abstract class AbstractHierarchyIteratorWithFilter<S> extends AbstractHierarchyIterator<S>
{
	// The list of user provided filters
	private List<IteratorFilter<S>> filters;

	// List of traversal filters
	private List<IteratorFilter<S>> traverseFilter;

	/**
	 * Construct.
	 * 
	 * @param root
	 */
	public AbstractHierarchyIteratorWithFilter(final S root)
	{
		super(root);
	}

	/**
	 * Allows to filter out component from the search.
	 * 
	 * @param comp
	 * @return false, to filter the component. True, to continue processing the component.
	 */
	@Override
	protected final boolean onFilter(final S comp)
	{
		if (filters != null)
		{
			for (IteratorFilter<S> filter : filters)
			{
				if (filter.onFilter(comp) == false)
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 
	 * @param filter
	 * @return Filters
	 */
	public final List<IteratorFilter<S>> getFilters()
	{
		if (filters == null)
		{
			filters = Generics.newArrayList();
		}

		return filters;
	}

	/**
	 * Allow to add your own filter (fluent API)
	 * 
	 * @param filter
	 * @return this
	 */
	public AbstractHierarchyIteratorWithFilter<S> addFilter(final IteratorFilter<S> filter)
	{
		Args.notNull(filter, "filter");

		getFilters().add(filter);
		return this;
	}

	/**
	 * Replace the current set of filters
	 * 
	 * @param filters
	 *            New filter set
	 * @return Old filter set
	 */
	public Collection<IteratorFilter<S>> replaceFilterSet(
		final Collection<IteratorFilter<S>> filters)
	{
		List<IteratorFilter<S>> old = this.filters;

		this.filters = null;
		if ((filters != null) && !filters.isEmpty())
		{
			for (IteratorFilter<S> filter : filters)
			{
				addFilter(filter);
			}
		}

		return old;
	}

	/**
	 * @param throwException
	 * @return Find the the first Component matching all filters
	 */
	public final S getFirst(final boolean throwException)
	{
		if (hasNext())
		{
			return next();
		}

		if (throwException)
		{
			throw new IllegalStateException("Iterator did not match any component");
		}

		return null;
	}

	/**
	 * 
	 * @return Gets all elements matching the filters in a list
	 */
	public final List<S> toList()
	{
		List<S> list = Generics.newArrayList();
		for (S component : this)
		{
			list.add(component);
		}

		return list;
	}

	/**
	 * @return Gets the traversal filters
	 */
	public final List<IteratorFilter<S>> getTraverseFilters()
	{
		if (traverseFilter == null)
		{
			traverseFilter = Generics.newArrayList();
		}

		return traverseFilter;
	}

	/**
	 * Add a filter to the traversal filter list (fluent API).
	 * 
	 * @param filter
	 * @return this
	 */
	public final AbstractHierarchyIteratorWithFilter<S> addTraverseFilters(
		final IteratorFilter<S> filter)
	{
		getTraverseFilters().add(filter);
		return this;
	}

	/**
	 * Allows to filter out component from the search.
	 * 
	 * @param node
	 * @return false to skip traversing its children
	 */
	@Override
	protected boolean onTraversalFilter(final S node)
	{
		if (traverseFilter != null)
		{
			for (IteratorFilter<S> filter : traverseFilter)
			{
				if (filter.onFilter(node) == false)
				{
					return false;
				}
			}
		}

		return true;
	}
}
