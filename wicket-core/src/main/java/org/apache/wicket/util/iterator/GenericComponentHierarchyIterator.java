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

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.util.lang.Args;

/**
 * Iterator over the complete Component hierarchy. The implementation is parent first, meaning that
 * first the parent gets returned upon next() and only than it's children.
 * <p>
 * A fluent or builder type of API is provided to configure the iterator with filters.
 * 
 * @author Juergen Donnerstag
 * @param <I>
 *            The type which next() should return (the iterator type)
 * @deprecated Hierarchy iterators are deprecated because they have problems with pages with
 *      deep component tree. Use {@link org.apache.wicket.util.visit.IVisitor} instead.
 * @see org.apache.wicket.MarkupContainer#visitChildren(org.apache.wicket.util.visit.IVisitor)
 * @see org.apache.wicket.MarkupContainer#visitChildren(Class, org.apache.wicket.util.visit.IVisitor)
 */
@Deprecated
public class GenericComponentHierarchyIterator<I extends Component> extends
	AbstractHierarchyIteratorWithFilter<Component, I>
{
	/**
	 * Construct.
	 * 
	 * @param component
	 *            Iterate over the containers children
	 * @param clazz
	 *            Must be the same as the iterator type provided
	 */
	public GenericComponentHierarchyIterator(final Component component,
		final Class<? extends I> clazz)
	{
		super(component);

		Args.notNull(clazz, "clazz");
		filterByClass(clazz);
	}

	/**
	 * The component must be a MarkupContainer to contain children
	 */
	@Override
	protected Iterator<Component> newIterator(final Component node)
	{
		return ((MarkupContainer)node).iterator();
	}

	/**
	 * Only MarkupContainer's might have children
	 */
	@Override
	protected boolean hasChildren(Component elem)
	{
		if (elem instanceof MarkupContainer)
		{
			return ((MarkupContainer)elem).size() > 0;
		}
		return false;
	}

	/**
	 * Add a filter which returns only leaf components.
	 * 
	 * @return this
	 */
	public final GenericComponentHierarchyIterator<I> filterLeavesOnly()
	{
		getFilters().add(new IteratorFilter<Component>()
		{
			@Override
			protected boolean onFilter(final Component component)
			{
				if (component instanceof MarkupContainer)
				{
					return ((MarkupContainer)component).size() == 0;
				}
				return true;
			}
		});

		return this;
	}

	/**
	 * Ignore components which don't implement (instanceof) the class provided.
	 * 
	 * @param clazz
	 * @return this
	 */
	public GenericComponentHierarchyIterator<I> filterByClass(final Class<?> clazz)
	{
		if (clazz != null)
		{
			getFilters().add(new IteratorFilter<Component>()
			{
				@Override
				protected boolean onFilter(Component component)
				{
					return clazz.isInstance(component);
				}
			});
		}

		return this;
	}

	/**
	 * Ignore all Components which not visible.
	 * 
	 * @return this
	 */
	public GenericComponentHierarchyIterator<I> filterByVisibility()
	{
		IteratorFilter<Component> filter = new IteratorFilter<Component>()
		{
			@Override
			protected boolean onFilter(Component comp)
			{
				return comp.isVisibleInHierarchy();
			}
		};

		addFilter(filter);
		addTraverseFilters(filter);

		return this;
	}

	/**
	 * Ignore all Components which not enabled (disabled) in the hierarchy
	 * 
	 * @return this
	 */
	public GenericComponentHierarchyIterator<I> filterEnabled()
	{
		IteratorFilter<Component> filter = new IteratorFilter<Component>()
		{
			@Override
			protected boolean onFilter(Component comp)
			{
				return comp.isEnabledInHierarchy();
			}
		};

		addFilter(filter);
		addTraverseFilters(filter);

		return this;
	}

	/**
	 * Ignore all components which don't match the id (regex).
	 * 
	 * @param match
	 *            Regex to find Components matching
	 * @return this
	 */
	public GenericComponentHierarchyIterator<I> filterById(final String match)
	{
		Args.notEmpty(match, "match");

		getFilters().add(new IteratorFilter<Component>()
		{
			@Override
			protected boolean onFilter(Component comp)
			{
				return comp.getId().matches(match);
			}
		});

		return this;
	}

	@Override
	public GenericComponentHierarchyIterator<I> addFilter(final IteratorFilter<Component> filter)
	{
		super.addFilter(filter);
		return this;
	}

	@Override
	public GenericComponentHierarchyIterator<I> addTraverseFilters(IteratorFilter<Component> filter)
	{
		super.addTraverseFilters(filter);
		return this;
	}
}
