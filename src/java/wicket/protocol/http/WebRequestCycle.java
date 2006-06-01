/*
 * $Id: WebRequestCycle.java 5849 2006-05-25 01:11:26 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 01:11:26 +0000 (Thu, 25 May
 * 2006) $
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

import wicket.AbortException;
import wicket.IRedirectListener;
import wicket.MetaDataKey;
import wicket.Page;
import wicket.RequestCycle;
import wicket.Response;
import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import wicket.markup.html.pages.BrowserInfoPage;
import wicket.protocol.http.request.WebClientInfo;
import wicket.request.ClientInfo;
import wicket.request.IRequestCycleProcessor;
import wicket.settings.IRequestCycleSettings;
import wicket.settings.IRequestCycleSettings.RenderStrategy;

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

	private static final MetaDataKey BROWSER_WAS_POLLED_KEY = new MetaDataKey(Boolean.class)
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Gets request cycle for calling thread.
	 * 
	 * @return Request cycle for calling thread
	 */
	public final static WebRequestCycle get()
	{
		return (WebRequestCycle)RequestCycle.get();
	}
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
	 * {@link WebApplication#getRequestCycleProcessor()}.
	 * <p>
	 * <strong>if you decide to override this method to provide a custom
	 * processor per request cycle, any mounts done via WebApplication will not
	 * work and and {@link #onRuntimeException(Page, RuntimeException)} is not
	 * called unless you deliberately put effort in it to make it work.</strong>
	 * </p>
	 * 
	 * @see wicket.RequestCycle#getProcessor()
	 */
	@Override
	public IRequestCycleProcessor getProcessor()
	{
		return ((WebApplication)getApplication()).getRequestCycleProcessor();
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
	 * is part of Wicket's internal behavior and should only be used when you
	 * want to circumvent the normal framework behavior and issue the redirect
	 * directly.
	 * 
	 * @param page
	 *            The page to redirect to
	 */
	@Override
	public final void redirectTo(final Page page)
	{
		String redirectUrl = null;

		// Check if use serverside response for client side redirects
		IRequestCycleSettings settings = application.getRequestCycleSettings();
		if ((settings.getRenderStrategy() == RenderStrategy.REDIRECT_TO_BUFFER)
				&& (application instanceof WebApplication))
		{
			// remember the current response
			final WebResponse currentResponse = getWebResponse();
			try
			{
				// create the redirect response.
				final BufferedHttpServletResponse servletResponse = new BufferedHttpServletResponse(
						currentResponse.getHttpServletResponse());
				final WebResponse redirectResponse = new WebResponse(servletResponse)
				{
					@Override
					public CharSequence encodeURL(CharSequence url)
					{
						return currentResponse.encodeURL(url);
					};
				};
				redirectResponse.setCharacterEncoding(currentResponse.getCharacterEncoding());

				// redirect the response to the buffer
				setResponse(redirectResponse);

				// render the page into the buffer
				page.renderPage();

				// re-assign the original response
				setResponse(currentResponse);

				final String responseRedirect = servletResponse.getRedirectUrl();
				if (responseRedirect != null)
				{
					// if the redirectResponse has another redirect url set
					// then the rendering of this page caused a redirect to
					// something else.
					// set this redirect then.
					redirectUrl = responseRedirect;
				}
				else if (servletResponse.getContentLength() > 0)
				{
					// call filter() so that any filters can process the
					// response
					servletResponse.filter(currentResponse);

					// Set the final character encoding before calling close
					servletResponse.setCharacterEncoding(currentResponse.getCharacterEncoding());
					// close it so that the reponse is fixed and encoded from
					// here on.
					servletResponse.close();

					redirectUrl = page.urlFor(IRedirectListener.INTERFACE).toString();
					int index = redirectUrl.indexOf("?");
					String sessionId = getWebRequest().getHttpServletRequest().getSession(true)
							.getId();
					((WebApplication)application).addBufferedResponse(sessionId, redirectUrl
							.substring(index + 1), servletResponse);
				}
			}
			catch (RuntimeException ex)
			{
				// re-assign the original response
				setResponse(currentResponse);
				if (ex instanceof AbortException)
				{
					throw ex;
				}
				log.error(ex.getMessage(), ex);
				IRequestCycleProcessor processor = getProcessor();
				processor.respond(ex, this);
				return;
			}
		}
		else
		{
			redirectUrl = page.urlFor(IRedirectListener.INTERFACE).toString();

			// Redirect page can touch its models already (via for example the
			// constructors)
			page.internalDetach();
		}

		if (redirectUrl == null)
		{
			redirectUrl = page.urlFor(IRedirectListener.INTERFACE).toString();
		}

		// Always touch the page again so that a redirect listener makes a page
		// statefull and adds it to the pagemap
		session.touch(page);

		// Redirect to the url for the page
		response.redirect(redirectUrl);
	}

	/**
	 * @see wicket.RequestCycle#newClientInfo()
	 */
	@Override
	protected ClientInfo newClientInfo()
	{
		if (getApplication().getRequestCycleSettings().getGatherExtendedBrowserInfo())
		{
			Session session = getSession();
			if (session.getMetaData(BROWSER_WAS_POLLED_KEY) == null)
			{
				// we haven't done the redirect yet; record that we will be
				// doing that now and redirect
				session.setMetaData(BROWSER_WAS_POLLED_KEY, Boolean.TRUE);
				throw new RestartResponseAtInterceptPageException(new BrowserInfoPage(getRequest()
						.getURL()));
			}
			// if we get here, the redirect already has been done; clear
			// the meta data entry; we don't need it any longer is the client
			// info object will be cached too
			session.setMetaData(BROWSER_WAS_POLLED_KEY, (Boolean)null);
		}
		return new WebClientInfo(this);
	}
}
