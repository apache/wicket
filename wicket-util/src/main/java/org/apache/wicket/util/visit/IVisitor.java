package org.apache.wicket.util.visit;


/**
 * Generic visitor interface for traversals.
 * 
 * @param <T>
 *            type of object to be visited
 * @param <R>
 *            type of value the visitor should return as the result of the
 *            visit/traversal
 */
public interface IVisitor<T, R>
{
	/**
	 * Called at each object in a visit.
	 * 
	 * @param object
	 *            Object being visited
	 * @param visit
	 *            Object used to control the visit/traversal
	 */
	public void component(T object, IVisit<R> visit);
}