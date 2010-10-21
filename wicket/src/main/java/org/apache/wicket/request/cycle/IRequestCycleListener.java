package org.apache.wicket.request.cycle;

import org.apache.wicket.Application;

/**
 * A callback interface for various methods in the request cycle. If you are creating a framework
 * that needs to do something in this methods, rather than extending RequestCycle or one of its
 * subclasses, you should implement this callback and allow users to add your listener to their
 * custom request cycle.
 * 
 * These listeners can be added directly to the request cycle when it is created or to the
 * {@link Application}
 * 
 * @author Jeremy Thomerson
 * @see Application#addRequestCycleListener(IRequestCycleListener)
 * @see RequestCycle#register(IRequestCycleListener)
 */
public interface IRequestCycleListener
{
	/**
	 * Called when the request cycle object is beginning its response
	 * 
	 * @param cycle
	 */
	void onBeginRequest(RequestCycle cycle);

	/**
	 * Called when the request cycle object has finished its response
	 * 
	 * @param cycle
	 */
	void onEndRequest(RequestCycle cycle);

	/**
	 * Called after the request cycle has been detached
	 * 
	 * @param cycle
	 */
	void onDetach(RequestCycle cycle);

	/**
	 * Called when there is an exception in the request cycle that would normally be handled by
	 * {@link RequestCycle#handleException(Exception)}
	 * 
	 * Note that in the event of an exception, {@link #onEndRequest()} will still be called after
	 * these listeners have {@link #onException(Exception)} called
	 * 
	 * @param cycle
	 * 
	 * @param ex
	 *            the exception that was passed in to
	 *            {@link RequestCycle#handleException(Exception)}
	 */
	void onException(RequestCycle cycle, Exception ex);
}