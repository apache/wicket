package org.apache.wicket.request.http.handler;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.junit.Test;
import org.mockito.Mockito;

public class RedirectRequestHandlerTest
{

	private static final String REDIRECT_URL = "redirectUrl";

	@Test
	public void permenanentlyMovedShouldSetLocationHeader()
	{
		RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL,
			HttpServletResponse.SC_MOVED_PERMANENTLY);

		IRequestCycle requestCycle = Mockito.mock(IRequestCycle.class);
		WebResponse webResponse = Mockito.mock(WebResponse.class);

		Mockito.when(requestCycle.getResponse()).thenReturn(webResponse);

		handler.respond(requestCycle);

		Mockito.verify(webResponse).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		Mockito.verify(webResponse).setHeader("Location", REDIRECT_URL);
	}

	@Test
	public void tempMovedShouldRedirect()
	{
		RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL,
			HttpServletResponse.SC_MOVED_TEMPORARILY);

		IRequestCycle requestCycle = Mockito.mock(IRequestCycle.class);
		WebResponse webResponse = Mockito.mock(WebResponse.class);

		Mockito.when(requestCycle.getResponse()).thenReturn(webResponse);

		handler.respond(requestCycle);

		Mockito.verify(webResponse).sendRedirect(REDIRECT_URL);
	}
}
