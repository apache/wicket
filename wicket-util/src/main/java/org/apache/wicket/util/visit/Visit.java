package org.apache.wicket.util.visit;

/**
 * Implementation of {@link IVisit} used by traversal algorithms
 * 
 * @author igor.vaynberg
 * 
 * @param <R>
 *            type of object that should be returned by the visit/traversal
 */
public class Visit<R> implements IVisit<R>
{
	private static enum Action {
		CONTINUE, CONTINUE_BUT_DONT_GO_DEEPER, STOP;
	}

	private R result;
	private Action action = Action.CONTINUE;

	/** {@inheritDoc} */
	public void stop()
	{
		stop(null);
	}

	/** {@inheritDoc} */
	public void stop(R result)
	{
		action = Action.STOP;
		this.result = result;
	}

	/** {@inheritDoc} */
	public void dontGoDeeper()
	{
		action = Action.CONTINUE_BUT_DONT_GO_DEEPER;
	}

	/**
	 * Checks if the visit/traversal has been stopped
	 * 
	 * @return {@code true} if the visit/traversal has been stopped
	 */
	public boolean isStopped()
	{
		return action == Action.STOP;
	}

	/**
	 * Checks if the visit/traversal should continue
	 * 
	 * @return {@code true} if the visit/traversal should continue
	 */
	public boolean isContinue()
	{
		return action == Action.CONTINUE;
	}

	/**
	 * Checks if the visit/traversal has been stopped from visiting children of the currently
	 * visited object
	 * 
	 * @return {@code true} if the visit/traversal should not visit children of the currently
	 *         visited object
	 */
	public boolean isDontGoDeeper()
	{
		return action == Action.CONTINUE_BUT_DONT_GO_DEEPER;
	}

	/**
	 * Gets the result of the visit/traversal. This value is set using {@link #stop(Object)} or
	 * remains {@code null} if visit/traversal has ended in any other way
	 * 
	 * @return value that should be returned to the method that initiated the visit/traversal
	 */
	public R getResult()
	{
		return result;
	}


}
