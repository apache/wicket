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
package org.apache.wicket.util.visit;

import java.util.Collections;
import java.util.Iterator;

import org.apache.wicket.util.lang.Args;

/**
 * Utility class that contains visitor/traversal related code
 */
public class Visits
{
	/** Constructor */
	private Visits()
	{
	}

	private static class SingletonIterable<T> implements Iterable<T>
	{
		private final T singleton;

		public SingletonIterable(final T singleton)
		{
			this.singleton = singleton;
		}

		@Override
		public Iterator<T> iterator()
		{
			return Collections.singleton(singleton).iterator();
		}
	}

	/**
	 * Visits container and its children pre-order (parent first). Children are determined by
	 * calling {@link Iterable#iterator()}.
	 * 
	 * @param <S>
	 *            the type of object that will be visited, notice that {@code container} is not
	 *            declared as {@code Iterable<S>} because it may return a generalization of
	 *            {@code S}
	 * @param <R>
	 *            the type of object that should be returned from the visitor, use {@link Void} if
	 *            no return value is needed
	 * @param container
	 *            object whose children will be visited
	 * @param visitor
	 *            the visitor
	 * @return return value from the {@code visitor} or {@code null} if none
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <S, R> R visit(final Iterable<? super S> container,
		final IVisitor<S, R> visitor)
	{
		return (R)visitChildren(new SingletonIterable(container), visitor, IVisitFilter.ANY);
	}

	/**
	 * Visits container and its children pre-order (parent first). Children are determined by
	 * calling {@link Iterable#iterator()}.
	 * 
	 * @param <S>
	 *            the type of object that will be visited, notice that {@code container} is not
	 *            declared as {@code Iterable<S>} because it may return a generalization of
	 *            {@code S}
	 * @param <R>
	 *            the type of object that should be returned from the visitor, use {@link Void} if
	 *            no return value is needed
	 * @param container
	 *            object whose children will be visited
	 * @param visitor
	 *            the visitor
	 * @param filter
	 *            filter used to limit the types of objects that will be visited
	 * @return return value from the {@code visitor} or {@code null} if none
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <S, R> R visit(final Iterable<? super S> container,
		final IVisitor<S, R> visitor, final IVisitFilter filter)
	{
		return (R)visitChildren(new SingletonIterable(container), visitor, filter);
	}

	/**
	 * Visits children of the specified {@link Iterable} pre-order (parent first). Children are
	 * determined by calling {@link Iterable#iterator()}.
	 * 
	 * @param <S>
	 *            the type of object that will be visited, notice that {@code container} is not
	 *            declared as {@code Iterable<S>} because it may return a generalization of
	 *            {@code S}
	 * @param <R>
	 *            the type of object that should be returned from the visitor, use {@link Void} if
	 *            no return value is needed
	 * @param container
	 *            object whose children will be visited
	 * @param visitor
	 *            the visitor
	 * @param filter
	 *            filter used to limit the types of objects that will be visited
	 * @return return value from the {@code visitor} or {@code null} if none
	 */
	public static final <S, R> R visitChildren(final Iterable<? super S> container,
		final IVisitor<S, R> visitor, final IVisitFilter filter)
	{
		Visit<R> visit = new Visit<>();
		visitChildren(container, visitor, filter, visit);
		return visit.getResult();
	}

	@SuppressWarnings("unchecked")
	private static final <S, R> void visitChildren(final Iterable<? super S> container,
		final IVisitor<S, R> visitor, final IVisitFilter filter, final Visit<R> visit)
	{
		Args.notNull(visitor, "visitor");

		// Iterate through children of this container
		for (final Object child : container)
		{
			// Get next child component
			// Is the child of the correct class (or was no class specified)?
			if (filter.visitObject(child))
			{
				Visit<R> childTraversal = new Visit<>();

				// Call visitor
				S s = (S)child;
				visitor.component(s, childTraversal);

				if (childTraversal.isStopped())
				{
					visit.stop(childTraversal.getResult());
					return;
				}
				else if (childTraversal.isDontGoDeeper())
				{
					continue;
				}
			}

			// If child is a container
			if (!visit.isDontGoDeeper() && (child instanceof Iterable<?>) &&
				filter.visitChildren(child))
			{
				// visit the children in the container
				visitChildren((Iterable<? super S>)child, visitor, filter, visit);

				if (visit.isStopped())
				{
					return;
				}
			}
		}
	}

	/**
	 * Visits children of the specified {@link Iterable} pre-order (parent first). Children are
	 * determined by calling {@link Iterable#iterator()}.
	 * 
	 * @param <S>
	 *            the type of object that will be visited, notice that {@code container} is not
	 *            declared as {@code Iterable<S>} because it may return a generalization of
	 *            {@code S}
	 * @param <R>
	 *            the type of object that should be returned from the visitor, use {@link Void} if
	 *            no return value is needed
	 * @param container
	 *            object whose children will be visited
	 * @param visitor
	 *            the visitor
	 * @return return value from the {@code visitor} or {@code null} if none
	 */
	public static final <S, R> R visitChildren(final Iterable<? super S> container,
		final IVisitor<S, R> visitor)
	{
		return visitChildren(container, visitor, IVisitFilter.ANY);
	}

	/**
	 * Visits the specified object and any of its children using a post-order (child first)
	 * traversal. Children are determined by calling {@link Iterable#iterator()} if the object
	 * implements {@link Iterable}.
	 * 
	 * @param <S>
	 *            the type of object that will be visited, notice that {@code container} is not
	 *            declared as {@code Iterable<S>} because it may return a generalization of
	 *            {@code S}
	 * @param <R>
	 *            the type of object that should be returned from the visitor, use {@link Void} if
	 *            no return value is needed
	 * @param root
	 *            root object that will be visited
	 * @param visitor
	 *            the visitor
	 * @return return value from the {@code visitor} or {@code null} if none
	 */
	public static final <S, R> R visitPostOrder(final S root,
		final org.apache.wicket.util.visit.IVisitor<S, R> visitor)
	{
		return visitPostOrder(root, visitor, IVisitFilter.ANY);
	}

	/**
	 * Visits the specified object and any of its children using a post-order (child first)
	 * traversal. Children are determined by calling {@link Iterable#iterator()} if the object
	 * implements {@link Iterable}.
	 * 
	 * @param <S>
	 *            the type of object that will be visited, notice that {@code container} is not
	 *            declared as {@code Iterable<S>} because it may return a generalization of
	 *            {@code S}
	 * @param <R>
	 *            the type of object that should be returned from the visitor, use {@link Void} if
	 *            no return value is needed
	 * @param root
	 *            root object that will be visited
	 * @param visitor
	 *            the visitor
	 * @param filter
	 *            filter used to limit the types of objects that will be visited
	 * @return return value from the {@code visitor} or {@code null} if none
	 */
	public static final <S, R> R visitPostOrder(final Object root,
		final org.apache.wicket.util.visit.IVisitor<S, R> visitor, final IVisitFilter filter)
	{
		Args.notNull(visitor, "visitor");

		Visit<R> visit = new Visit<>();
		visitPostOrderHelper(root, visitor, filter, visit);
		return visit.getResult();
	}

	@SuppressWarnings("unchecked")
	private static final <S, R> void visitPostOrderHelper(final Object component,
		final org.apache.wicket.util.visit.IVisitor<S, R> visitor, final IVisitFilter filter,
		final Visit<R> visit)
	{
		if (component instanceof Iterable<?>)
		{
			final Iterable<?> container = (Iterable<?>)component;
			if (filter.visitChildren(container))
			{
				Visit<R> childTraversal = new Visit<>();
				for (final Object child : ((Iterable<?>)component))
				{
					visitPostOrderHelper(child, visitor, filter, childTraversal);
					if (childTraversal.isStopped())
					{
						visit.stop(childTraversal.getResult());
						return;
					}
				}
			}
		}

		if (filter.visitObject(component))
		{
			visitor.component((S)component, visit);
		}
	}
}
