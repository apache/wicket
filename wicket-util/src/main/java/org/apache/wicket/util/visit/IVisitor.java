package org.apache.wicket.util.visit;


/**
 * Generic component visitor interface for component traversals.
 * 
 * @param <T>
 *            The component
 */
public interface IVisitor<T, R>
{
	/**
	 * Called at each component in a visit.
	 * 
	 * @param component
	 *            The component
	 * @param traversal
	 *            An {@link IVisit} which state will be modified depending on
	 *            the visitation. CONTINUE_TRAVERSAL (null) if the traversal
	 *            should continue, or a non-null return value for the traversal
	 *            method if it should stop. If no return value is useful, the
	 *            generic non-null value STOP_TRAVERSAL can be used.
	 */
	public void component(T component, IVisit<R> visit);
}