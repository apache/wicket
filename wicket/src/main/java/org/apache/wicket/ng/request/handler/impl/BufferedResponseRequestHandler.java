package org.apache.wicket.ng.request.handler.impl;

import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.response.BufferedWebResponse;
import org.apache.wicket.ng.request.response.WebResponse;

public class BufferedResponseRequestHandler implements RequestHandler
{
	private final BufferedWebResponse bufferedWebResponse;
	
	public BufferedResponseRequestHandler(BufferedWebResponse bufferedWebResponse)
	{
		this.bufferedWebResponse = bufferedWebResponse;
	}
	
	public void detach(RequestCycle requestCycle)
	{

	}

	public void respond(RequestCycle requestCycle)
	{
		bufferedWebResponse.writeTo((WebResponse) requestCycle.getResponse());
	}

}
