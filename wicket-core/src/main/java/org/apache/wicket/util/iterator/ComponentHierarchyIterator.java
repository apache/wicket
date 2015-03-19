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
 * @deprecated Hierarchy iterators are deprecated because they have problems with pages with
 *      deep component tree. Use {@link org.apache.wicket.util.visit.IVisitor} instead.
 * @see org.apache.wicket.MarkupContainer#visitChildren(org.apache.wicket.util.visit.IVisitor)
 * @see org.apache.wicket.MarkupContainer#visitChildren(Class, org.apache.wicket.util.visit.IVisitor)
 */
@Deprecated
public class ComponentHierarchyIterator extends
	AbstractHierarchyIteratorWithFilter<Component, Component>
{
	/**
	 * Construct.
	 * 
	 * @param component
	 *            Iterate over the containers children
	 */
	public ComponentHierarchyIterator(final Component component)
	{
		super(component);
	}

	/**
	 * Convenience Constructor
	 * 
	 * @param component
	 *            Iterate over the containers children
	 * @param clazz
	 *            Add filter by class
	 * @param visible
	 *            Add filter by visibility
	 * @param enabled
	 *            Add filter by "enabled"
	 */
	public ComponentHierarchyIterator(final Component component, Class<?> clazz, boolean visible,
		boolean enabled)
	{
		this(component);

		if (clazz != null)
		{
			filterByClass(clazz);
		}

		if (visible)
		{
			filterByVisibility();
		}

		if (enabled)
		{
			filterEnabled();
		}
	}

	/**
	 * Convenience Constructor
	 * 
	 * @param component
	 *            Iterate over the containers children
	 * @param clazz
	 *            Add filter by class
	 */
	public ComponentHierarchyIterator(final Component component, Class<?> clazz)
	{
		this(component, clazz, false, false);
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
	public final ComponentHierarchyIterator filterLeavesOnly()
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
	public ComponentHierarchyIterator filterByClass(final Class<?> clazz)
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
	public ComponentHierarchyIterator filterByVisibility()
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
	public ComponentHierarchyIterator filterEnabled()
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
	public ComponentHierarchyIterator filterById(final String match)
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
	public ComponentHierarchyIterator addFilter(final IteratorFilter<Component> filter)
	{
		super.addFilter(filter);
		return this;
	}

	@Override
	public ComponentHierarchyIterator addTraverseFilters(IteratorFilter<Component> filter)
	{
		super.addTraverseFilters(filter);
		return this;
	}
}
