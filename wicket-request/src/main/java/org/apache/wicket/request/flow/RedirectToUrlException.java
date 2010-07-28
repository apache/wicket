package org.apache.wicket.request.flow;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.request.RequestHandlerStack.ReplaceHandlerException;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;

/**
 * Causes Wicket to interrupt current request processing and send a redirect to the given url.
 * 
 * Use this if you want to redirect to an external or none Wicket url. If you want to redirect to a
 * page use the {@link RestartResponseException}
 * 
 * @see RestartResponseException
 * @see RestartResponseAtInterceptPageException
 */
public class RedirectToUrlException extends ReplaceHandlerException
{
	private static final long serialVersionUID = 1L;

	public RedirectToUrlException(final String redirectUrl)
	{
		this(redirectUrl, HttpServletResponse.SC_MOVED_TEMPORARILY);
	}

	public RedirectToUrlException(final String redirectUrl, final int statusCode)
	{
		super(new RedirectRequestHandler(redirectUrl, statusCode), true);
	}
}
