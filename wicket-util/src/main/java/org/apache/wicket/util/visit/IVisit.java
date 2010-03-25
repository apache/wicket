package org.apache.wicket.util.visit;

/**
 * Allows visitors to control the visit/traversal
 * 
 * @author igor.vaynberg
 * 
 * @param <R>
 *            type of object the visitor is expected to return, if none use
 *            {@link Void}
 */
public interface IVisit<R>
{
	/**
	 * Stops the visit/traversal
	 */
	void stop();

	/**
	 * Stops the visit/traversal and returns {@code result}
	 * 
	 * @param result
	 */
	void stop(R result);

	/**
	 * Prevents the visitor from visiting any children of the object currently
	 * visited
	 */
	void dontGoDeeper();
}
