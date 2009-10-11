 package org.apache.wicket.ng.mock;

import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.render.RenderPageRequestHandlerDelegate;

public class MockRenderPageRequestHandlerDelegate extends RenderPageRequestHandlerDelegate
{

	public MockRenderPageRequestHandlerDelegate(RenderPageRequestHandler renderPageRequestHandler)
	{
		super(renderPageRequestHandler);
	}

	@Override
	public void respond(RequestCycle requestCycle)
	{
		getPageProvider().getPageInstance().renderPage();
	}

}
