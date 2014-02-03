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
package org.apache.wicket.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.response.filter.IResponseFilter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;

/**
 * Interface for request related settings
 * <p>
 * <i>bufferResponse </i> (defaults to true) - True if the application should buffer responses. This
 * does require some additional memory, but helps keep exception displays accurate because the whole
 * rendering process completes before the page is sent to the user, thus aRequestCycleSettingsing the possibility of
 * a partially rendered page.
 * <p>
 * <i>renderStrategy </i>- Sets in what way the render part of a request is handled. Basically,
 * there are two different options:
 * <ul>
 * <li>Direct, IRequestCycleSettings.RenderStrategy.ONE_PASS_RENDER. Everything is handled in one
 * physical request. This is efficient, and is the best option if you want to do sophisticated
 * clustering. It does not however, shield you from what is commonly known as the <i>Double submit
 * problem </i></li>
 * <li>Using a redirect. This follows the pattern <a
 * href="http://www.theserverside.com/articles/article.tss?l=RedirectAfterPost" >as described at the
 * serverside </a> and that is commonly known as Redirect after post. Wicket takes it one step
 * further to do any rendering after a redirect, so that not only form submits are shielded from the
 * double submit problem, but also the IRequestListener handlers (that could be e.g. a link that
 * deletes a row). With this pattern, you have two options to choose from:
 * <ul>
 * <li>IRequestCycleSettings.RenderStrategy.REDIRECT_TO_RENDER. This option first handles the
 * 'action' part of the request, which is either page construction (bookmarkable pages or the home
 * page) or calling a IRequestListener handler, such as Link.onClick. When that part is done, a
 * redirect is issued to the render part, which does all the rendering of the page and its
 * components. <strong>Be aware </strong> that this may mean, depending on whether you access any
 * models in the action part of the request, that attachment and detachment of some models is done
 * twice for a request.</li>
 * <li>IRequestCycleSettings.RenderStrategy.REDIRECT_TO_BUFFER. This option handles both the action-
 * and the render part of the request in one physical request, but instead of streaming the result
 * to the browser directly, it is kept in memory, and a redirect is issued to get this buffered
 * result (after which it is immediately removed). This option currently is the default render
 * strategy, as it shields you from the double submit problem, while being more efficient and less
 * error prone regarding to detachable models.</li>
 * </ul>
 * Note: In rare cases the strategies involving redirect may lose session data! For example: if
 * after the first phase of the strategy the server node fails without having the chance to
 * replicate the session then the second phase will be executed on another node and the whole
 * process will be restarted and thus anything stored in the first phase will be lost with the
 * failure of the server node. For similar reasons it is recommended to use sticky sessions when
 * using redirect strategies.</li>
 * </ul>
 *
 * <p>
 * More documentation is available about each setting in the setter method for the property.
 *
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class RequestCycleSettings
{
	/**
	 * Enum type for different render strategies
	 */
	public static enum RenderStrategy {
		/**
		 * All logical parts of a request (the action and render part) are handled within the same
		 * request.
		 * <p>
		 * This strategy is more efficient than the 'REDIRECT_TO_RENDER' strategy, and doesn't have
		 * some of the potential problems of it, it also does not solve the double submit problem.
		 * It is however the best option to use when you want to do sophisticated (non-sticky
		 * session) clustering.
		 * </p>
		 */
		ONE_PASS_RENDER,

		/**
		 * All logical parts of a request (the action and render part) are handled within the same
		 * request, but instead of streaming the render result to the browser directly, the result
		 * is cached on the server. A client side redirect command is issued to the browser
		 * specifically to render this request.
		 */
		REDIRECT_TO_BUFFER,

		/**
		 * The render part of a request (opposed to the 'action part' which is either the
		 * construction of a bookmarkable page or the execution of a IRequestListener handler) is
		 * handled by a separate request by issuing a redirect request to the browser. This is
		 * commonly known as the 'redirect after submit' pattern, though in our case, we use it for
		 * GET and POST requests instead of just the POST requests.
		 * <p>
		 * This pattern solves the 'refresh' problem. While it is a common feature of browsers to
		 * refresh/ reload a web page, this results in problems in many dynamic web applications.
		 * For example, when you have a link with an event handler that e.g. deletes a row from a
		 * list, you usually want to ignore refresh requests after that link is clicked on. By using
		 * this strategy, the refresh request only results in the re-rendering of the page without
		 * executing the event handler again.
		 * </p>
		 * <p>
		 * Though it solves the refresh problem, it introduces potential problems, as the request
		 * that is logically one, are actually two separate request. Not only is this less
		 * efficient, but this also can mean that within the same request attachment/ detachment of
		 * models is done twice (in case you use models in the bookmarkable page constructors and
		 * IRequestListener handlers). If you use this strategy, you should be aware of this
		 * possibility, and should also be aware that for one logical request, actually two
		 * instances of RequestCycle are created and processed.
		 * </p>
		 * <p>
		 * Also, even with this strategy set, it is ignored for instances of
		 * {@link org.apache.wicket.core.request.handler.BookmarkableListenerInterfaceRequestHandler},
		 * because otherwise they wouldn't be bookmarkable.
		 * </p>
		 */
		REDIRECT_TO_RENDER
	}

	/** True if the response should be buffered */
	private boolean bufferResponse = true;

	/**
	 * Whether Wicket should try to get extensive client info by redirecting to
	 * {@link org.apache.wicket.markup.html.pages.BrowserInfoPage a page that polls for client
	 * capabilities}. False by default.
	 */
	private boolean gatherExtendedBrowserInfo = false;

	/**
	 * The render strategy, defaults to 'REDIRECT_TO_BUFFER'. This property influences the default
	 * way in how a logical request that consists of an 'action' and a 'render' part is handled, and
	 * is mainly used to have a means to circumvent the 'refresh' problem.
	 */
	private RequestCycleSettings.RenderStrategy renderStrategy = RenderStrategy.REDIRECT_TO_BUFFER;

	/** List of {@link IResponseFilter}s. */
	private List<IResponseFilter> responseFilters;

	/**
	 * In order to do proper form parameter decoding it is important that the response and the
	 * following request have the same encoding. see
	 * http://www.crazysquirrel.com/computing/general/form-encoding.jspx for additional information.
	 */
	private String responseRequestEncoding = "UTF-8";

	/**
	 * The time that a request will by default be waiting for the previous request to be handled
	 * before giving up. Defaults to one minute.
	 */
	private Duration timeout = Duration.ONE_MINUTE;

	private int exceptionRetryCount = 10;

// ****************************************************************************
// IRequestCycleSettings Implementation
// ****************************************************************************

	/**
	 * Adds a response filter to the list. Filters are evaluated in the order they have been added.
	 *
	 * @param responseFilter
	 *            The {@link IResponseFilter} that is added
	 * @return {@code this} object for chaining
	 */
	public RequestCycleSettings addResponseFilter(IResponseFilter responseFilter)
	{
		if (responseFilters == null)
		{
			responseFilters = new ArrayList<>(3);
		}
		responseFilters.add(responseFilter);
		return this;
	}

	/**
	 * Decides whether to buffer the response's headers until the end of the request processing.
	 * The buffering is needed if the application makes use of
	 * {@link org.apache.wicket.Component#setResponsePage(org.apache.wicket.request.component.IRequestablePage)} or
	 * {@link org.apache.wicket.request.flow.ResetResponseException}
	 *
	 * @return {@code true} if the application should buffer the response's headers.
	 */
	public boolean getBufferResponse()
	{
		return bufferResponse;
	}

	/**
	 * Gets whether Wicket should try to get extensive client info by redirecting to
	 * {@link org.apache.wicket.markup.html.pages.BrowserInfoPage a page that polls for client capabilities}. This method is used by the
	 * default implementation of {@link org.apache.wicket.Session#getClientInfo()}, so if that method is
	 * overridden, there is no guarantee this method will be taken into account.
	 *
	 * @return Whether to gather extensive client info
	 */
	public boolean getGatherExtendedBrowserInfo()
	{
		return gatherExtendedBrowserInfo;
	}

	/**
	 * Gets in what way the render part of a request is handled.
	 *
	 * @return the render strategy
	 */
	public RequestCycleSettings.RenderStrategy getRenderStrategy()
	{
		return renderStrategy;
	}

	/**
	 * @return an unmodifiable list of added response filters, null if none
	 */
	public List<IResponseFilter> getResponseFilters()
	{
		if (responseFilters == null)
		{
			return null;
		}
		else
		{
			return Collections.unmodifiableList(responseFilters);
		}
	}


	/**
	 * In order to do proper form parameter encoding it is important that the response and the
	 * subsequent request stipulate a common character encoding.
	 *
	 * possible form encodings and their problems:
	 *
	 * <ul>
	 * <li><a
	 * href="http://www.crazysquirrel.com/computing/general/form-encoding.jspx">application/x-
	 * www-form-urlencoded</a></li>
	 * <li><a href=
	 * "http://stackoverflow.com/questions/546365/utf-8-text-is-garbled-when-form-is-posted-as-multipart-form-data"
	 * >multipart/form-data</a></li>
	 * </ul>
	 *
	 * wicket now uses multipart/form-data for it's forms.
	 *
	 * @return The request and response encoding
	 */
	public String getResponseRequestEncoding()
	{
		return responseRequestEncoding;
	}

	/**
	 * Gets the time that a request will by default be waiting for the previous request to be
	 * handled before giving up.
	 *
	 * @return The time out
	 */
	public Duration getTimeout()
	{
		return timeout;
	}

	/**
	 * Sets a flag whether the application should buffer the response's headers until the end
	 * of the request processing. The buffering is needed if the application makes use of
	 * {@link org.apache.wicket.Component#setResponsePage(org.apache.wicket.request.component.IRequestablePage)}
	 * or {@link org.apache.wicket.request.flow.ResetResponseException}
	 *
	 * @param bufferResponse
	 *            {@code true} if the application should buffer response's headers.
	 * @return {@code this} object for chaining
	 */
	public RequestCycleSettings setBufferResponse(boolean bufferResponse)
	{
		this.bufferResponse = bufferResponse;
		return this;
	}

	/**
	 * Sets whether Wicket should try to get extensive client info by redirecting to
	 * {@link org.apache.wicket.markup.html.pages.BrowserInfoPage a page that polls for client capabilities}. This method is used by the
	 * default implementation of {@link org.apache.wicket.Session#getClientInfo()}, so if that method is
	 * overridden, there is no guarantee this method will be taken into account.
	 *
	 * <p>
	 * <strong>WARNING: </strong> though this facility should work transparently in most cases, it
	 * is recommended that you trigger the roundtrip to get the browser info somewhere where it
	 * hurts the least. The roundtrip will be triggered the first time you call
	 * {@link org.apache.wicket.Session#getClientInfo()} for a session, and after the roundtrip a new request with the
	 * same info (url, post parameters) is handled. So rather than calling this in the middle of an
	 * implementation of a form submit method, which would result in the code of that method before
	 * the call to {@link org.apache.wicket.Session#getClientInfo()} to be executed twice, you best call
	 * {@link org.apache.wicket.Session#getClientInfo()} e.g. in a page constructor or somewhere else where you didn't
	 * do a lot of processing first.
	 * </p>
	 *
	 * @param gatherExtendedBrowserInfo
	 *            Whether to gather extensive client info
	 * @return {@code this} object for chaining
	 */
	public RequestCycleSettings setGatherExtendedBrowserInfo(boolean gatherExtendedBrowserInfo)
	{
		this.gatherExtendedBrowserInfo = gatherExtendedBrowserInfo;
		return this;
	}

	/**
	 * Sets in what way the render part of a request is handled. Basically, there are two different
	 * options:
	 * <ul>
	 * <li>Direct, ApplicationSettings.ONE_PASS_RENDER. Everything is handled in one physical
	 * request. This is efficient, and is the best option if you want to do sophisticated
	 * clustering. It does not however, shield you from what is commonly known as the <i>Double
	 * submit problem </i></li>
	 * <li>Using a redirect. This follows the pattern <a
	 * href="http://www.theserverside.com/articles/article.tss?l=RedirectAfterPost" >as described at
	 * the serverside </a> and that is commonly known as Redirect after post. Wicket takes it one
	 * step further to do any rendering after a redirect, so that not only form submits are shielded
	 * from the double submit problem, but also the IRequestListener handlers (that could be e.g. a
	 * link that deletes a row). With this pattern, you have two options to choose from:
	 * <ul>
	 * <li>ApplicationSettings.REDIRECT_TO_RENDER. This option first handles the 'action' part of
	 * the request, which is either page construction (bookmarkable pages or the home page) or
	 * calling a IRequestListener handler, such as Link.onClick. When that part is done, a redirect
	 * is issued to the render part, which does all the rendering of the page and its components.
	 * <strong>Be aware </strong> that this may mean, depending on whether you access any models in
	 * the action part of the request, that attachment and detachment of some models is done twice
	 * for a request.</li>
	 * <li>ApplicationSettings.REDIRECT_TO_BUFFER. This option handles both the action- and the
	 * render part of the request in one physical request, but instead of streaming the result to
	 * the browser directly, it is kept in memory, and a redirect is issue to get this buffered
	 * result (after which it is immediately removed). This option currently is the default render
	 * strategy, as it shields you from the double submit problem, while being more efficient and
	 * less error prone regarding to detachable models.</li>
	 * </ul>
	 *
	 * @param renderStrategy
	 *            the render strategy that should be used by default.
	 * @return {@code this} object for chaining
	 */
	public RequestCycleSettings setRenderStrategy(RequestCycleSettings.RenderStrategy renderStrategy)
	{
		this.renderStrategy = renderStrategy;
		return this;
	}

	/**
	 * In order to do proper form parameter decoding it is important that the response and the
	 * following request have the same encoding. see
	 * http://www.crazysquirrel.com/computing/general/form-encoding.jspx for additional information.
	 *
	 * Default encoding: UTF-8
	 *
	 * @param encoding
	 *            The request and response encoding to be used.
	 * @return {@code this} object for chaining
	 */
	public RequestCycleSettings setResponseRequestEncoding(final String encoding)
	{
		Args.notNull(encoding, "encoding");
		this.responseRequestEncoding = encoding;
		return this;
	}

	/**
	 * Sets the time that a request will by default be waiting for the previous request to be
	 * handled before giving up.
	 *
	 * @param timeout
	 * @return {@code this} object for chaining
	 */
	public RequestCycleSettings setTimeout(Duration timeout)
	{
		Args.notNull(timeout, "timeout");
		this.timeout = timeout;
		return this;
	}

	/**
	 * Sets how many attempts Wicket will make to render the exception request handler before
	 *         giving up.
	 * @param retries
	 *      the number of attempts
	 * @return {@code this} object for chaining
	 */
	public RequestCycleSettings setExceptionRetryCount(int retries)
	{
		this.exceptionRetryCount = retries;
		return this;
	}

	/**
	 * @return How many times will Wicket attempt to render the exception request handler before
	 *         giving up.
	 */
	public int getExceptionRetryCount()
	{
		return exceptionRetryCount;
	}
}
