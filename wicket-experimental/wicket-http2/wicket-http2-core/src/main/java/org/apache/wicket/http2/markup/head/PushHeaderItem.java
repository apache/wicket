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
package org.apache.wicket.http2.markup.head;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.http2.Http2Settings;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.collections.ConcurrentHashSet;
import org.apache.wicket.util.time.Time;

/**
 * A push header item to be used in the http/2 context and to reduce the latency of the web
 * application. Follow these steps for your page:<br>
 * <br>
 * - Override the setHeaders method and don't call super.setHeaders to disable caching<br>
 * - Get the page request / response and store them as transient fields that are given into the
 * PushHeaderItem<br>
 * - Ensure a valid https connection (not self signed), because otherwise no caching information are
 * accepted from Chrome or other browsers
 * 
 * @author Tobias Soloschenko
 *
 */
public class PushHeaderItem extends HeaderItem
{
	private static final long serialVersionUID = 1L;

	/**
	 * The header date formats for if-modified-since / last-modified
	 */
	private static final DateTimeFormatter headerDateFormat_RFC1123 = DateTimeFormatter
		.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz")
		.withLocale(java.util.Locale.ENGLISH)
		.withZone(ZoneOffset.UTC); // Sun, 06 Nov 1994 08:49:37 GMT ; RFC 822, updated by RFC 1123

	private static final DateTimeFormatter headerDateFormat_RFC1036 = DateTimeFormatter
		.ofPattern("EEEE, dd-MMM-yy HH:mm:ss zzz")
		.withLocale(java.util.Locale.ENGLISH)
		.withZone(ZoneOffset.UTC); // Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obs. by RFC 1036

	private static final DateTimeFormatter headerDateFormat_ASCTIME = DateTimeFormatter
		.ofPattern("EEE MMM d HH:mm:ss yyyy")
		.withLocale(java.util.Locale.ENGLISH)
		.withZone(ZoneOffset.UTC); // Sun Nov 6 08:49:37 1994 ; ANSI C's asctime() format

	/**
	 * The http2 protocol string
	 */
	public static final String HTTP2_PROTOCOL = "http/2";

	/**
	 * The token suffix to be used in this header item
	 */
	private static final String TOKEN_SUFFIX = HTTP2_PROTOCOL + "_pushed";

	/**
	 * The URLs of resources to be pushed to the client
	 */
	private Set<PushItem> pushItems = new ConcurrentHashSet<PushItem>(new TreeSet<PushItem>());
	/**
	 * The web response of the page to apply the caching information to
	 */
	private WebResponse pageWebResponse;

	/**
	 * The web request of the page to get the caching information from
	 */
	private WebRequest pageWebRequest;

	/**
	 * The page to get the modification time of
	 */
	private Page page;

	/**
	 * Creates a push header item based on the given page and the corresponding page request / page
	 * response. To get the request and response
	 * 
	 * 
	 * @param page
	 *            the page this header item is applied to
	 * @param pageRequest
	 *            the page request this header item is applied to
	 * @param pageResponse
	 *            the page response this header item is applied to
	 */
	public PushHeaderItem(Page page, Request pageRequest, Response pageResponse)
	{
		if (page == null || !(page instanceof WebPage) || pageResponse == null ||
			!(pageResponse instanceof WebResponse))
		{
			throw new WicketRuntimeException(
				"Please hand over the web page, the web request and the web response to the push header item like \"new PushHeaderItem(this, yourWebPageRequest, yourWebPageResponse)\" - " +
					"The webPageResponse / webPageRequest can be obtained via \"getRequestCycle().getRequest()\" / \"getRequestCycle().getResponse()\" and placed into the page as fields " +
					"\"private transient Response webPageResponse;\" / \"private transient Request webPageRequest;\"");
		}
		this.pageWebRequest = (WebRequest)pageRequest;
		this.pageWebResponse = (WebResponse)pageResponse;
		this.page = page;
	}

	/**
	 * Uses the URLs that has already been pushed to the client to ensure not to push them again
	 */
	@Override
	public Iterable<?> getRenderTokens()
	{
		Set<String> tokens = new TreeSet<String>();
		for (PushItem pushItem : pushItems)
		{
			tokens.add(pushItem.getUrl() + TOKEN_SUFFIX);
		}
		return tokens;
	}

	/**
	 * Gets the time the page of this header item has been modified. The default implementation is
	 * to get the last modification date of the HTML file of the corresponding page, but it can be
	 * overridden to apply a custom behavior. For example place in a properties-file into the class
	 * path which contains the compile time. <br>
	 * Example: <code>
	 * <pre>
	 * protected Time getPageModificationTime(){
	 * 	Time time = getPageModificationTime();
	 * 	// read properties file with build time and place it into a second time variable
	 * 	return time.before(buildTime) ? buildTime : time;
	 * }
	 * </pre>
	 * </code>
	 * 
	 * @return the time the page of this header item has been modified
	 */
	protected Time getPageModificationTime()
	{
		URL resource = page.getClass().getResource(page.getClass().getSimpleName() + ".html");
		if (resource == null)
		{
			throw new WicketRuntimeException(
				"The markup to the page couldn't be found: " + page.getClass().getName());
		}
		try
		{
			return Time.valueOf(new Date(resource.openConnection().getLastModified()));
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(
				"The time couln't be determined of the markup file of the page: " +
					page.getClass().getName(),
				e);
		}
	}

	/**
	 * Applies the cache header item to the response
	 */
	protected void applyPageCacheHeader()
	{
		// check modification of page html
		Time pageModificationTime = getPageModificationTime();
		// The date of the page is now
		pageWebResponse.setDateHeader("Date", Time.now());
		// Set the modification time so that the browser sends a "If-Modified-Since" header which
		// can be compared
		pageWebResponse.setLastModifiedTime(pageModificationTime);
		// Make the resource stale so that it gets revalidated even if a cache entry is set
		// (see http://stackoverflow.com/questions/11357430/http-expires-header-values-0-and-1)
		pageWebResponse.setHeader("Expires", "-1");
		// Set a cache but set it to max-age=0 / must-revalidate so that the request to the page is
		// done
		pageWebResponse.setHeader("Cache-Control",
			"max-age=0, public, must-revalidate, proxy-revalidate");
	}

	/**
	 * Pushes the previously created URLs to the client
	 */
	@Override
	public void render(Response response)
	{
		// applies the caching header to the actual page request
		applyPageCacheHeader();

		HttpServletRequest request = getContainerRequest(RequestCycle.get().getRequest());
		// Check if the protocol is http/2 or http/2.0 to only push the resources in this case
		if (isHttp2(request))
		{

			Time pageModificationTime = getPageModificationTime();
			String ifModifiedSinceHeader = pageWebRequest.getHeader("If-Modified-Since");

			// Check if the if-modified-since header is set - if not push all resources
			if (ifModifiedSinceHeader != null)
			{

				// Try to parse RFC1123
				Time ifModifiedSinceFromRequestTime = parseIfModifiedSinceHeader(
					ifModifiedSinceHeader, headerDateFormat_RFC1123);

				// Try to parse ASCTIME
				if (ifModifiedSinceFromRequestTime == null)
				{
					ifModifiedSinceFromRequestTime = parseIfModifiedSinceHeader(
						ifModifiedSinceHeader, headerDateFormat_ASCTIME);
				}

				// Try to parse RFC1036 - because it is obsolete due to RFC 1036 check this last.
				if (ifModifiedSinceFromRequestTime == null)
				{
					ifModifiedSinceFromRequestTime = parseIfModifiedSinceHeader(
						ifModifiedSinceHeader, headerDateFormat_RFC1036);
				}

				// if the modified since header is before the page modification time or if it can't
				// be parsed push it.
				if (ifModifiedSinceFromRequestTime == null ||
					ifModifiedSinceFromRequestTime.before(pageModificationTime))
				{
					// Some browsers like IE 9-11 or Chrome 39 that does not send right headers
					// receive the resource via push all the time
					push(request);
				}
			}
			else
			{
				// Push the resources if the "if-modified-since" is not available
				push(request);
			}
		}
	}

	/**
	 * Parses the given if modified since header with the date time formatter
	 * 
	 * @param ifModifiedSinceHeader
	 *            the if modified since header string
	 * @param dateTimeFormatter
	 *            the formatter to parse the header string with
	 * @return the time or null
	 */
	private Time parseIfModifiedSinceHeader(String ifModifiedSinceHeader,
		DateTimeFormatter dateTimeFormatter)
	{
		try
		{
			return Time.valueOf(Date.from(LocalDateTime
				.parse(ifModifiedSinceHeader, dateTimeFormatter).toInstant(ZoneOffset.UTC)));
		}
		catch (DateTimeParseException e)
		{
			// NOOP
		}
		return null;
	}

	/**
	 * Pushed all URLs of this header item to the client
	 * 
	 * @param request
	 *            the request to push the URLs to
	 */
	protected void push(HttpServletRequest request)
	{
		// Receives the vendor specific push builder
		Http2Settings http2Settings = Http2Settings.Holder.get(Application.get());
		PushBuilder pushBuilder = http2Settings.getPushBuilder();
		pushBuilder.push(request, pushItems.toArray(new PushItem[pushItems.size()]));
	}

	/**
	 * Creates a URL and pushes the resource to the client - this is only supported if http2 is
	 * enabled
	 * 
	 * @param pushItems
	 *            a list of items to be pushed to the client
	 * @return the current push header item
	 */
	@SuppressWarnings("unchecked")
	public PushHeaderItem push(List<PushItem> pushItems)
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (isHttp2(getContainerRequest(requestCycle.getRequest())))
			for (PushItem pushItem : pushItems)
			{
				Object object = pushItem.getObject();
				PageParameters parameters = pushItem.getPageParameters();

				if (object == null)
				{
					throw new WicketRuntimeException(
						"Please provide an object to the items to be pushed, so that the url can be created for the given resource.");
				}

				CharSequence url = null;
				if (object instanceof ResourceReference)
				{
					url = requestCycle.urlFor((ResourceReference)object, parameters);
				}
				else if (Page.class.isAssignableFrom(object.getClass()))
				{
					url = requestCycle.urlFor((Class<? extends Page>)object, parameters);
				}
				else if (object instanceof IRequestHandler)
				{
					url = requestCycle.urlFor((IRequestHandler)object);
				}
				else if (pushItem.getUrl() != null)
				{
					url = pushItem.getUrl();
				}
				else
				{
					Url encoded = new PageParametersEncoder().encodePageParameters(parameters);
					String queryString = encoded.getQueryString();
					url = object.toString() + (queryString != null ? "?" + queryString : "");
				}

				if (url.toString().equals("."))
				{
					url = "/";
				}
				else if (url.toString().startsWith("."))
				{
					url = url.toString().substring(1);
				}

				// The context path and the filter have to be applied to the URL, because otherwise
				// the resource is not pushed correctly
				StringBuilder partialUrl = new StringBuilder();
				String contextPath = WebApplication.get().getServletContext().getContextPath();
				partialUrl.append(contextPath);
				if (!"/".equals(contextPath))
				{
					partialUrl.append('/');
				}
				String filterPath = WebApplication.get().getWicketFilter().getFilterPath();
				if ("/".equals(filterPath))
				{
					filterPath = "";
				}
				else if (filterPath.endsWith("/"))
				{
					filterPath = filterPath.substring(0, filterPath.length() - 1);
				}
				partialUrl.append(filterPath);
				partialUrl.append(url.toString());

				// Set the url the resource is going to be pushed with
				pushItem.setUrl(partialUrl.toString());

				// Apply the push item to be used during the push process
				this.pushItems.add(pushItem);
			}
		return this;
	}

	/**
	 * Gets the container request
	 * 
	 * @param request
	 *            the wicket request to get the container request from
	 * @return the container request
	 */
	public HttpServletRequest getContainerRequest(Request request)
	{

		return checkHttpServletRequest(request);
	}

	/**
	 * Checks if the given request is a http/2 request
	 * 
	 * @param request
	 *            the request to check if it is a http/2 request
	 * @return if the request is a http/2 request
	 */
	public boolean isHttp2(HttpServletRequest request)
	{
		// detects http/2 and http/2.0
		return request.getProtocol().toLowerCase(Locale.ROOT).contains(HTTP2_PROTOCOL);
	}

	/**
	 * Checks if the container request from the given request is instance of
	 * {@link HttpServletRequest} if not the API of the PushHeaderItem can't be used and a
	 * {@link WicketRuntimeException} is thrown.
	 * 
	 * @param request
	 *            the request to get the container request from. The container request is checked if
	 *            it is instance of {@link HttpServletRequest}
	 * @return the container request get from the given request casted to {@link HttpServletRequest}
	 * @throw {@link WicketRuntimeException} if the container request is not a
	 *        {@link HttpServletRequest}
	 */
	public HttpServletRequest checkHttpServletRequest(Request request)
	{
		Object assumedHttpServletRequest = request.getContainerRequest();
		if (!(assumedHttpServletRequest instanceof HttpServletRequest))
		{
			throw new WicketRuntimeException(
				"The request is not a HttpServletRequest - the usage of PushHeaderItem is not support in the current environment: " +
					request.getClass().getName());
		}
		return (HttpServletRequest)assumedHttpServletRequest;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PushHeaderItem that = (PushHeaderItem)o;
		return Objects.equals(pushItems, that.pushItems) &&
			Objects.equals(pageWebResponse, that.pageWebResponse) &&
			Objects.equals(pageWebRequest, that.pageWebRequest) && Objects.equals(page, that.page);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(pushItems, pageWebResponse, pageWebRequest, page);
	}
}
