/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.AbortException;
import org.apache.wicket.IRedirectListener;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.pages.BrowserInfoPage;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.ClientInfo;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.settings.IRequestCycleSettings;


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
	 * Constructor which simply passes arguments to superclass for storage
	 * there. This instance will be set as the current one for this thread.
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
	 * @see org.apache.wicket.RequestCycle#getProcessor()
	 */
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
	public final void redirectTo(final Page page)
	{
		String redirectUrl = null;

		// Check if use serverside response for client side redirects
		IRequestCycleSettings settings = application.getRequestCycleSettings();
		if ((settings.getRenderStrategy() == IRequestCycleSettings.REDIRECT_TO_BUFFER)
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
					public CharSequence encodeURL(CharSequence url)
					{
						return currentResponse.encodeURL(url);
					}
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
					throw ex;
				
				if (!(ex instanceof PageExpiredException))
					logRuntimeException(ex);
				
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
			// this can be removed i guess because this page will be detached in
			// the page target
			// page.internalDetach();
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
	 * @see org.apache.wicket.RequestCycle#newClientInfo()
	 */
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

	/**
	 * If it's an ajax request we always redirects.
	 * 
	 * @see org.apache.wicket.RequestCycle#getRedirect()
	 */
	public final boolean getRedirect()
	{
		if (getWebRequest().isAjax())
		{
			return true;
		}
		else
		{
			return super.getRedirect();
		}
	}
}
