package org.apache.wicket.request.cycle;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.request.IRequestHandler;

/**
 * Registers and retrieves first and last IPageRequestHandler in a request cycle.
 * Can be used to find out what is the requested page and what is the actual response page.
 *
 * @since 1.5.8
 */
public class PageRequestHandlerTracker extends AbstractRequestCycleListener
{
	/**
	 * The key for the first handler
	 */
	public static final  MetaDataKey<IPageRequestHandler> FIRST_HANDLER_KEY = new MetaDataKey<IPageRequestHandler>() {};

	/**
	 * The key for the last handler
	 */
	public static final MetaDataKey<IPageRequestHandler> LAST_HANDLER_KEY = new MetaDataKey<IPageRequestHandler>() {};

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		super.onRequestHandlerResolved(cycle, handler);
		registerFirstHandler(cycle,handler);
		registerLastHandler(cycle,handler);
	}

	@Override
	public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler)
	{
		super.onRequestHandlerResolved(cycle, handler);
		registerLastHandler(cycle,handler);
	}

	/**
	 * Registers pagerequesthandler when it's resolved ,keeps up with the most recent handler resolved
	 *
	 * @param cycle
	 *      the current request cycle
	 * @param handler
	 *      the request handler to register
	 */
	private void registerLastHandler(RequestCycle cycle, IRequestHandler handler)
	{
		if (handler instanceof IPageRequestHandler)
		{
			cycle.setMetaData(LAST_HANDLER_KEY, (IPageRequestHandler) handler);
		}
	}

	/**
	 * Registers firsthandler if it's not already registered
	 *
	 * @param cycle
	 *      the current request cycle
	 * @param handler
	 *      the request handler to register
	 */
	private void registerFirstHandler(RequestCycle cycle, IRequestHandler handler)
	{
		if (handler instanceof IPageRequestHandler && getFirstHandler(cycle) == null)
		{
			cycle.setMetaData(FIRST_HANDLER_KEY, (IPageRequestHandler)handler);
		}
	}

   /**
	* retrieves last handler from requestcycle
	*
	* @param cycle
	* @return last handler
	*/
	public static IPageRequestHandler getLastHandler(RequestCycle cycle)
	{
		return cycle.getMetaData(LAST_HANDLER_KEY);
	}

	/**
	 * retrieves first handler from the request cycle
	 *
	 * @param cycle
	 * @return first handler
	 */
	public static IPageRequestHandler getFirstHandler(RequestCycle cycle)
	{
		return cycle.getMetaData(FIRST_HANDLER_KEY);
	}
}
