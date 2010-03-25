package org.apache.wicket.util.visit;

import java.util.Iterator;


public class Visits
{
	// TODO replace class argument with IVisitFilter
	public static final <S, R> R visitChildren(Iterable<?> container, final Class<?> clazz,
			final IVisitor<S, R> visitor)
	{
		Visit<R> visit = new Visit<R>();
		visitChildren(container, clazz, visitor, visit);
		return visit.getResult();
	}


	private static final <S, R> void visitChildren(Iterable<?> container, final Class<?> clazz,
			final IVisitor<S, R> visitor, Visit<R> visit)
	{
		if (visitor == null)
		{
			throw new IllegalArgumentException("argument visitor may not be null");
		}

		// Iterate through children of this container
		for (final Iterator<?> children = container.iterator(); children.hasNext();)
		{
			// Get next child component
			final Object child = children.next();

			// Is the child of the correct class (or was no class specified)?
			if (clazz == null || clazz.isInstance(child))
			{
				Visit<R> childTraversal = new Visit<R>();

				// Call visitor
				@SuppressWarnings("unchecked")
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
			if (!visit.isDontGoDeeper() && (child instanceof Iterable<?>))
			{
				// visit the children in the container
				visitChildren((Iterable<?>)child, clazz, visitor, visit);

				if (visit.isStopped())
				{
					return;
				}
			}
		}

		return;
	}

	/**
	 * Traverses all child components in this container, calling the visitor's
	 * visit method at each one.
	 * 
	 * @param visitor
	 *            The visitor to call back to
	 * @return The return value from a visitor which halted the traversal, or
	 *         null if the entire traversal occurred
	 */
	public static final <S, R> R visitChildren(Iterable<?> visitable, final IVisitor<S, R> visitor)
	{
		return visitChildren(visitable, null, visitor);
	}

	/**
	 * Visits any form components inside component if it is a container, or
	 * component itself if it is itself a form component
	 * 
	 * @param component
	 *            starting point of the traversal
	 * 
	 * @param visitor
	 *            The visitor to call
	 */
	public static final <S, R> R visitComponentsPostOrder(Iterable<?> component,
			final org.apache.wicket.util.visit.IVisitor<S, R> visitor)
	{
		return visitComponentsPostOrder(component, visitor, IVisitFilter.ANY);
	}

	/**
	 * Visits any form components inside component if it is a container, or
	 * component itself if it is itself a form component
	 * 
	 * @param component
	 *            starting point of the traversal
	 * 
	 * @param visitor
	 *            The visitor to call
	 */
	public static final <S, R> R visitComponentsPostOrder(Object component,
			final org.apache.wicket.util.visit.IVisitor<S, R> visitor, IVisitFilter filter)
	{
		if (visitor == null)
		{
			throw new IllegalArgumentException("Argument `visitor` cannot be null");
		}

		Visit<R> visit = new Visit<R>();
		visitComponentsPostOrderHelper(component, visitor, filter, visit);
		return visit.getResult();
	}

	/**
	 * 
	 * @param component
	 * @param visitor
	 * @return Object
	 */
	private static final <S, R> void visitComponentsPostOrderHelper(Object component,
			final org.apache.wicket.util.visit.IVisitor<S, R> visitor, IVisitFilter filter,
			Visit<R> visit)
	{

		if (component instanceof Iterable<?>)
		{
			final Iterable<?> container = (Iterable<?>)component;
			if (filter.visitChildren(container))
			{
				Visit<R> childTraversal = new Visit<R>();
				for (final Iterator<?> iterator = ((Iterable<?>)component).iterator(); iterator
						.hasNext();)
				{
					final Object child = iterator.next();
					if (child instanceof Iterable<?>)
					{
						visitComponentsPostOrderHelper((Iterable<?>)child, visitor, filter,
								childTraversal);
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
}
