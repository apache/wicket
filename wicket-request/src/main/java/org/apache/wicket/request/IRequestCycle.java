package org.apache.wicket.request;


public interface IRequestCycle
{
	Response getResponse();

	Request getRequest();

	void scheduleRequestHandlerAfterCurrent(IRequestHandler handler);

	UrlRenderer getUrlRenderer();

}
