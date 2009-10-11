package org.apache.wicket.ng.request.handler.impl.render;

import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.PageProvider;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler.RedirectPolicy;

/**
 * Delegate responsible for rendering the page. Depending on the implementation (web, test, portlet,
 * etc.) the delegate may or may not support the redirect policy set in the
 * {@link RenderPageRequestHandler}.
 * 
 * @author Matej Knopp
 */
public abstract class RenderPageRequestHandlerDelegate
{
	private final RenderPageRequestHandler renderPageRequestHandler;

	public RenderPageRequestHandlerDelegate(RenderPageRequestHandler renderPageRequestHandler)
	{
		this.renderPageRequestHandler = renderPageRequestHandler;
	}

	public PageProvider getPageProvider()
	{
		return renderPageRequestHandler.getPageProvider();
	}

	public RedirectPolicy getRedirectPolicy()
	{
		return renderPageRequestHandler.getRedirectPolicy();
	}

	public RenderPageRequestHandler getRenderPageRequestHandler()
	{
		return renderPageRequestHandler;
	}

	public abstract void respond(RequestCycle requestCycle);
}
