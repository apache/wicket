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
 * 
 * @TODO wrong sematic. next() should move it forward. hasNext() should never do it.
 * @TODO It'd be cool if next would return the class/generics provided via filterByClass
 * @TODO currently we have a parent first strategy only. A deepest child first strategy must be
 *       useful as well.
 * @TODO make it more generic to work with Markup as well.
 * 
 * @author Juergen Donnerstag
 */
public class ComponentHierarchyIterator extends AbstractHierarchyIteratorWithFilter<Component>
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
	 *            filter by class
	 * @param visible
	 *            if true, than ignore invisible components
	 * @param enabled
	 *            if true, than ignore disabled components
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
	 *            filter by class
	 */
	public ComponentHierarchyIterator(final Component component, Class<?> clazz)
	{
		this(component, clazz, false, false);
	}

	/**
	 * Add a filter which returns only leaf components
	 * 
	 * @return this
	 */
	public final ComponentHierarchyIterator filterLeavesOnly()
	{
		getFilters().add(new IteratorFilter<Component>()
		{
			@Override
			boolean onFilter(final Component component)
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
	 * Part of the fluent API: Filter Components by Class.
	 * <p>
	 * Must only be used before hasNext() has been called.
	 * 
	 * @param clazz
	 * @return A new iterator with the added filter
	 */
	public ComponentHierarchyIterator filterByClass(final Class<?> clazz)
	{
		if (clazz != null)
		{
			getFilters().add(new IteratorFilter<Component>()
			{
				@Override
				boolean onFilter(Component component)
				{
					return clazz.isInstance(component);
				}
			});
		}

		return this;
	}

	/**
	 * Part of the fluent API: Ignore all Components which not visible in the hierachy
	 * <p>
	 * Must only be used before hasNext() has been called.
	 * 
	 * @return A new iterator with the added filter
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
	 * Part of the fluent API: Ignore all Components which not enabled in the hierachy
	 * <p>
	 * Must only be used before hasNext() has been called.
	 * 
	 * @return A new iterator with the added filter
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
	 * Part of the fluent API: Accept only Components with matching id's.
	 * <p>
	 * Must only be used before hasNext() has been called.
	 * 
	 * @param match
	 *            Regex to find Components matching
	 * @return A new iterator with the added filter
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
	protected Iterator<Component> newIterator(final Component node)
	{
		if (node instanceof MarkupContainer)
		{
			return ((MarkupContainer)node).iterator();
		}
		return null;
	}

	@Override
	protected boolean hasChildren(Component elem)
	{
		if (elem instanceof MarkupContainer)
		{
			return ((MarkupContainer)elem).size() > 0;
		}
		return false;
	}

	@Override
	public ComponentHierarchyIterator addFilter(final IteratorFilter<Component> filter)
	{
		super.addFilter(filter);
		return this;
	}
}