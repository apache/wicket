/*
 * $Id$ $Revision:
 * 1.85 $ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.IRedirectListener;
import wicket.Page;
import wicket.RequestCycle;
import wicket.Response;
import wicket.protocol.http.request.WebClientInfo;
import wicket.request.ClientInfo;
import wicket.request.IRequestCycleProcessor;
import wicket.response.BufferedResponse;
import wicket.settings.IRequestCycleSettings;
import wicket.settings.Settings;

/**
 * RequestCycle implementation for HTTP protocol. Holds the application,
 * session, request and response objects for a given HTTP request. Contains
 * methods (urlFor*) which yield a URL for bookmarkable pages as well as
 * non-bookmarkable component interfaces. The protected handleRender method is
 * the internal entrypoint which takes care of the details of rendering a
 * response to an HTTP request.
 * 
 * @see RequestCycle
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Gili Tzabari
 * @author Eelco Hillenius
 */
public class WebRequestCycle extends RequestCycle
{
	/** Logging object */
	private static final Log log = LogFactory.getLog(WebRequestCycle.class);

	/**
	 * Constructor which simply passes arguments to superclass for storage
	 * there.
	 * 
	 * @param session
	 *            The session
	 * @param request
	 *            The request
	 * @param response
	 *            The response
	 */
	public WebRequestCycle(final WebSession session, final WebRequest request,
			final Response response)
	{
		super(session, request, response);
	}

	/**
	 * By default returns the WebApplication's default request cycle processor.
	 * Typically, you don't override this method but instead override
	 * {@link WebApplication#getDefaultRequestCycleProcessor()}.
	 * <p>
	 * <strong>if you decide to override this method to provide a custom
	 * processor per request cycle, any mounts done via WebApplication will not
	 * work and and {@link #onRuntimeException(Page, RuntimeException)} is not
	 * called unless you deliberately put effort in it to make it work.</strong>
	 * </p>
	 * 
	 * @see wicket.RequestCycle#getProcessor()
	 */
	public IRequestCycleProcessor getProcessor()
	{
		return ((WebApplication)getApplication()).getDefaultRequestCycleProcessor();
	}

	/**
	 * @return Request as a WebRequest
	 */
	public WebRequest getWebRequest()
	{
		return (WebRequest)request;
	}

	/**
	 * @return Response as a WebResponse
	 */
	public WebResponse getWebResponse()
	{
		return (WebResponse)response;
	}

	/**
	 * @return Session as a WebSession
	 */
	public WebSession getWebSession()
	{
		return (WebSession)session;
	}

	/**
	 * Redirects browser to the given page. NOTE: Usually, you should never call
	 * this method directly, but work with setResponsePage instead. This method
	 * is part of Wicket's internal behaviour and should only be used when you
	 * want to circumvent the normal framework behaviour and issue the redirect
	 * directly.
	 * 
	 * @param page
	 *            The page to redirect to
	 */
	public final void redirectTo(final Page page)
	{
		String redirectUrl = null;

		// Check if use serverside response for client side redirects
		IRequestCycleSettings settings = application.getSettings();
		if ((settings.getRenderStrategy() == Settings.REDIRECT_TO_BUFFER)
				&& (application instanceof WebApplication))
		{
			// remember the current response
			final Response currentResponse = getResponse();
			try
			{
				// create the redirect response.
				// override the encodeURL so that it will use the real once
				// encoding.
				final BufferedResponse redirectResponse = new BufferedResponse()
				{
					public String encodeURL(String url)
					{
						return currentResponse.encodeURL(url);
					}
				};
				redirectResponse.setCharacterEncoding(currentResponse.getCharacterEncoding());

				// redirect the response to the buffer
				setResponse(redirectResponse);

				// render the page into the buffer
				page.doRender();

				// re-assign the original response
				setResponse(currentResponse);

				final String responseRedirect = redirectResponse.getRedirectUrl();
				if (responseRedirect != null)
				{
					// if the redirectResponse has another redirect url set
					// then the rendering of this page caused a redirect to
					// something else.
					// set this redirect then.
					redirectUrl = responseRedirect;
				}
				else if (redirectResponse.getContentLength() > 0)
				{
					// if no content is created then don't set it in the
					// redirect buffer
					// (maybe access failed).
					// Set the encoding of the response (what the browser wants)
					redirectResponse.setCharacterEncoding(currentResponse.getCharacterEncoding());

					// call filter() so that any filters can process the
					// response
					redirectResponse.filter();
					// close it so that the reponse is fixed and encoded from
					// here on.
					redirectResponse.close();

					redirectUrl = page.urlFor(page, IRedirectListener.class);
					String sessionId = getWebRequest().getHttpServletRequest().getSession(true)
							.getId();
					((WebApplication)application).addBufferedResponse(sessionId, redirectUrl,
							redirectResponse);
				}
			}
			catch (RuntimeException ex)
			{
				// re-assign the original response
				setResponse(currentResponse);
				log.error(ex.getMessage(), ex);
				IRequestCycleProcessor processor = getProcessor();
				processor.respond(ex, this);
				return;
			}
		}
		else
		{
			session.touch(page);
			// redirect page can touch its models already (via for example the
			// constructors)
			page.internalEndRequest();
		}

		if (redirectUrl == null)
		{
			redirectUrl = page.urlFor(page, IRedirectListener.class);
		}
		// Redirect to the url for the page
		response.redirect(redirectUrl);
	}

	/**
	 * @see wicket.RequestCycle#newClientInfo()
	 */
	protected ClientInfo newClientInfo()
	{
		return new WebClientInfo(this);
	}
}
