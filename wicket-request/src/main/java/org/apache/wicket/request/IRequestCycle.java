package org.apache.wicket.request;

import org.apache.wicket.Response;

public interface IRequestCycle
{
	Response getResponse();
	Request getRequest();
	void scheduleRequestHandlerAfterCurrent(IRequestHandler handler);
}
