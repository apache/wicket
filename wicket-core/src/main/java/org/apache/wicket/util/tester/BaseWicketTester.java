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
package org.apache.wicket.util.tester;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import junit.framework.AssertionFailedError;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.IPageRendererProvider;
import org.apache.wicket.IRequestCycleProvider;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.ContainerInfo;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IFormSubmitListener;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.mock.MockPageManager;
import org.apache.wicket.mock.MockRequestParameters;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.page.IPageManagerContext;
import org.apache.wicket.protocol.http.IMetaDataBufferingWebResponse;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.handler.render.PageRenderer;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.session.ISessionStore.UnboundListener;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class to ease unit testing of Wicket applications without the need for a servlet
 * container. See javadoc of <code>WicketTester</code> for example usage. This class can be used as
 * is, but JUnit users should use derived class <code>WicketTester</code>.
 * 
 * @see WicketTester
 * 
 * @author Ingram Chen
 * @author Juergen Donnerstag
 * @author Frank Bille
 * @author Igor Vaynberg
 * 
 * @since 1.2.6
 */
public class BaseWicketTester
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(BaseWicketTester.class);

	private final ServletContext servletContext;
	private MockHttpSession httpSession;

	private final WebApplication application;

	private boolean followRedirects = true;
	private int redirectCount;

	private MockHttpServletRequest lastRequest;
	private MockHttpServletResponse lastResponse;

	private final List<MockHttpServletRequest> previousRequests = Generics.newArrayList();
	private final List<MockHttpServletResponse> previousResponses = Generics.newArrayList();

	/** current request and response */
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	/** current session */
	private Session session;

	/** current request cycle */
	private RequestCycle requestCycle;

	private Page lastRenderedPage;

	private boolean exposeExceptions = true;

	private boolean useRequestUrlAsBase = true;

	private IRequestHandler forcedHandler;

	private IFeedbackMessageFilter originalFeedbackMessageCleanupFilter;
	// Simulates the cookies maintained by the browser
	private final List<Cookie> browserCookies = Generics.newArrayList();

	private ComponentInPage componentInPage;

	// User may provide request header value any time. They get applied (and reset) upon next
	// invocation of processRequest()
	private Map<String, String> preHeader;

	/**
	 * Creates <code>WicketTester</code> and automatically create a <code>WebApplication</code>, but
	 * the tester will have no home page.
	 */
	public BaseWicketTester()
	{
		this(new MockApplication());
	}

	/**
	 * Creates <code>WicketTester</code> and automatically creates a <code>WebApplication</code>.
	 * 
	 * @param <C>
	 * @param homePage
	 *            a home page <code>Class</code>
	 */
	public <C extends Page> BaseWicketTester(final Class<C> homePage)
	{
		this(new MockApplication()
		{
			@Override
			public Class<? extends Page> getHomePage()
			{
				return homePage;
			}
		});
	}

	/**
	 * Creates a <code>WicketTester</code>.
	 * 
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 */
	public BaseWicketTester(final WebApplication application)
	{
		this(application, (MockServletContext)null);
	}

	/**
	 * Creates a <code>WicketTester</code>.
	 * 
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 * @param servletContextBasePath
	 *            the absolute path on disk to the web application's contents (e.g. war root) - may
	 *            be <code>null</code>
	 */
	public BaseWicketTester(final WebApplication application, String servletContextBasePath)
	{
		this(application, new MockServletContext(application, servletContextBasePath));
	}

	/**
	 * Creates a <code>WicketTester</code>.
	 * 
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 * @param servletCtx
	 *            the servlet context used as backend
	 */
	public BaseWicketTester(final WebApplication application, final ServletContext servletCtx)
	{
		servletContext = servletCtx != null ? servletCtx
			: new MockServletContext(application, null);

		final FilterConfig filterConfig = new TestFilterConfig();
		WicketFilter filter = new WicketFilter()
		{
			@Override
			public FilterConfig getFilterConfig()
			{
				return filterConfig;
			}
		};

		application.setWicketFilter(filter);

		httpSession = new MockHttpSession(servletContext);

		ThreadContext.detach();

		this.application = application;

		// FIXME some tests are leaking applications by not calling destroy on them or overriding
		// teardown() without calling super, for now we work around by making each name unique
		application.setName("WicketTesterApplication-" + UUID.randomUUID());
		ThreadContext.setApplication(application);

		application.setServletContext(servletContext);

		// initialize the application
		application.initApplication();

		// We don't expect any changes during testing. In addition we avoid creating
		// ModificationWatcher threads tests.
		application.getResourceSettings().setResourcePollFrequency(getResourcePollFrequency());

		// reconfigure application for the test environment
		application.setPageRendererProvider(new LastPageRecordingPageRendererProvider(
			application.getPageRendererProvider()));
		application.setRequestCycleProvider(new TestRequestCycleProvider(
			application.getRequestCycleProvider()));

		// set a feedback message filter that will not remove any messages
		originalFeedbackMessageCleanupFilter = application.getApplicationSettings()
			.getFeedbackMessageCleanupFilter();
		application.getApplicationSettings().setFeedbackMessageCleanupFilter(
			IFeedbackMessageFilter.NONE);
		IPageManagerProvider pageManagerProvider = newTestPageManagerProvider();
		if (pageManagerProvider != null)
		{
			application.setPageManagerProvider(pageManagerProvider);
		}

		// create a new session when the old one is invalidated
		application.getSessionStore().registerUnboundListener(new UnboundListener()
		{
			@Override
			public void sessionUnbound(String sessionId)
			{
				newSession();
			}
		});

		// prepare session
		setupNextRequestCycle();
	}

	/**
	 * By default Modification Watcher is disabled by default for the tests.
	 * 
	 * @return the duration between two checks for changes in the resources
	 */
	protected Duration getResourcePollFrequency()
	{
		return null;
	}

	/**
	 * 
	 * @return page manager provider
	 */
	protected IPageManagerProvider newTestPageManagerProvider()
	{
		return new TestPageManagerProvider();
	}

	/**
	 * @return last rendered page
	 */
	public Page getLastRenderedPage()
	{
		return lastRenderedPage;
	}

	/**
	 *
	 */
	private void setupNextRequestCycle()
	{
		request = new MockHttpServletRequest(application, httpSession, servletContext);
		request.setURL(request.getContextPath() + request.getServletPath() + "/");

		// assign protocol://host:port to next request unless the last request was ajax
		final boolean assignBaseLocation = lastRequest != null &&
			lastRequest.getHeader("Wicket-Ajax") == null;

		// resume request processing with scheme://host:port from last request
		if (assignBaseLocation)
		{
			request.setScheme(lastRequest.getScheme());
			request.setSecure(lastRequest.isSecure());
			request.setServerName(lastRequest.getServerName());
			request.setServerPort(lastRequest.getServerPort());
		}

		transferCookies();

		response = new MockHttpServletResponse(request);

		ServletWebRequest servletWebRequest = newServletWebRequest();
		requestCycle = application.createRequestCycle(servletWebRequest,
			newServletWebResponse(servletWebRequest));
		ThreadContext.setRequestCycle(requestCycle);

		if (session == null)
		{
			newSession();
		}
	}

	/**
	 * Cleans up feedback messages. This usually happens on detach, but is disabled in unit testing
	 * so feedback mesasges can be examined.
	 */
	public void cleanupFeedbackMessages()
	{
		cleanupFeedbackMessages(originalFeedbackMessageCleanupFilter);
	}

	/**
	 * Removes all feedback messages
	 */
	public void clearFeedbackMessages()
	{
		cleanupFeedbackMessages(IFeedbackMessageFilter.ALL);
	}

	/**
	 * Cleans up feedback messages given the specified filter.
	 * 
	 * @param filter
	 *            filter used to cleanup messages, accepted messages will be removed
	 */
	private void cleanupFeedbackMessages(IFeedbackMessageFilter filter)
	{
		application.getApplicationSettings().setFeedbackMessageCleanupFilter(filter);
		getLastRenderedPage().detach();
		getSession().detach();
		application.getApplicationSettings().setFeedbackMessageCleanupFilter(
			IFeedbackMessageFilter.NONE);
	}

	/**
	 * Copies all cookies with a positive age from the last response to the request that is going to
	 * be used for the next cycle.
	 */
	private void transferCookies()
	{
		if (lastResponse != null)
		{
			List<Cookie> cookies = lastResponse.getCookies();
			if (cookies != null)
			{
				for (Cookie cookie : cookies)
				{
					// maxAge == -1 -> means session cookie
					// maxAge == 0 -> delete the cookie
					// maxAge > 0 -> the cookie will expire after this age
					if (cookie.getMaxAge() != 0)
					{
						request.addCookie(cookie);
					}
				}
			}
		}
	}

	/**
	 * @param servletWebRequest
	 * @return servlet web response
	 */
	protected Response newServletWebResponse(final ServletWebRequest servletWebRequest)
	{
		return new WicketTesterServletWebResponse(servletWebRequest, response);
	}

	/**
	 * @return the configured in the user's application web request
	 */
	private ServletWebRequest newServletWebRequest()
	{


		return (ServletWebRequest)application.newWebRequest(request, request.getFilterPrefix());
	}

	/**
	 *
	 */
	private void newSession()
	{
		ThreadContext.setSession(null);

		// the following will create a new session and put it in the thread context
		session = Session.get();
	}

	/**
	 * @return request object
	 */
	public MockHttpServletRequest getRequest()
	{
		return request;
	}

	/**
	 * @param request
	 */
	public void setRequest(final MockHttpServletRequest request)
	{
		this.request = request;
		applyRequest();
	}

	/**
	 * @return session
	 */
	public Session getSession()
	{
		return session;
	}

	/**
	 * Returns {@link HttpSession} for this environment
	 * 
	 * @return session
	 */
	public MockHttpSession getHttpSession()
	{
		return httpSession;
	}

	/**
	 * Returns the {@link Application} for this environment.
	 * 
	 * @return application
	 */
	public WebApplication getApplication()
	{
		return application;
	}

	/**
	 * Returns the {@link ServletContext} for this environment
	 * 
	 * @return servlet context
	 */
	public ServletContext getServletContext()
	{
		return servletContext;
	}

	/**
	 * Destroys the tester. Restores {@link ThreadContext} to state before instance of
	 * {@link WicketTester} was created.
	 */
	public void destroy()
	{
		application.internalDestroy();
		ThreadContext.detach();
	}

	/**
	 * @return true, if process was executed successfully
	 */
	public boolean processRequest()
	{
		return processRequest(null, null);
	}

	/**
	 * Processes the request in mocked Wicket environment.
	 * 
	 * @param request
	 *            request to process
	 * @return true, if process was executed successfully
	 */
	public boolean processRequest(final MockHttpServletRequest request)
	{
		return processRequest(request, null);
	}

	/**
	 * Processes the request in mocked Wicket environment.
	 * 
	 * @param request
	 *            request to process
	 * @param forcedRequestHandler
	 *            optional parameter to override parsing the request URL and force
	 *            {@link IRequestHandler}
	 * @return true, if process was executed successfully
	 */
	public boolean processRequest(final MockHttpServletRequest request,
		final IRequestHandler forcedRequestHandler)
	{
		return processRequest(request, forcedRequestHandler, false);
	}

	/**
	 * @param forcedRequestHandler
	 * @return true, if process was executed successfully
	 */
	public boolean processRequest(final IRequestHandler forcedRequestHandler)
	{
		return processRequest(null, forcedRequestHandler, false);
	}

	/**
	 * Process the request. This is a fairly central function and is almost always invoked for
	 * executing the request.
	 * <p>
	 * You may subclass processRequest it, to monitor or change any pre-configured value. Request
	 * headers can be configured more easily by calling {@link #addRequestHeader(String, String)}.
	 * 
	 * @param forcedRequest
	 *            Can be null.
	 * @param forcedRequestHandler
	 *            Can be null.
	 * @param redirect
	 * @return true, if process was executed successfully
	 */
	protected boolean processRequest(final MockHttpServletRequest forcedRequest,
		final IRequestHandler forcedRequestHandler, final boolean redirect)
	{
		if (forcedRequest != null)
		{
			request = forcedRequest;
		}

		forcedHandler = forcedRequestHandler;

		if (!redirect && getRequest().getHeader("Wicket-Ajax") == null)
		{
			lastRenderedPage = null;
		}

		// Add or replace any system provided header entry with the user provided.
		if ((request != null) && (preHeader != null))
		{
			for (Map.Entry<String, String> entry : preHeader.entrySet())
			{
				if (Strings.isEmpty(entry.getKey()) == false)
				{
					request.setHeader(entry.getKey(), entry.getValue());
				}
			}

			// Reset the user provided headers
			preHeader = null;
		}

		try
		{
			if (getLastResponse() != null)
			{
				// transfer cookies from previous response to this request, quirky but how old stuff
				// worked...
				for (Cookie cookie : getLastResponse().getCookies())
				{
					request.addCookie(cookie);
				}
			}

			applyRequest();
			requestCycle.scheduleRequestHandlerAfterCurrent(null);

			if (!requestCycle.processRequestAndDetach())
			{
				return false;
			}

			recordRequestResponse();
			setupNextRequestCycle();

			if (followRedirects && lastResponse.isRedirect())
			{
				if (redirectCount++ >= 100)
				{
					fail("Possible infinite redirect detected. Bailing out.");
				}

				Url newUrl = Url.parse(lastResponse.getRedirectLocation(),
					Charset.forName(request.getCharacterEncoding()));

				if (isExternalRedirect(lastRequest.getUrl(), newUrl))
				{
					// we can't handle external redirects here
					// just bail out here and let the user's test code
					// check #assertRedirectUrl
					return true;
				}

				if (newUrl.isAbsolute())
				{
					request.setUrl(newUrl);

					final String protocol = newUrl.getProtocol();

					if (protocol != null)
					{
						request.setScheme(protocol);
					}

					request.setSecure("https".equals(protocol));

					if (newUrl.getHost() != null)
					{
						request.setServerName(newUrl.getHost());
					}
					if (newUrl.getPort() != null)
					{
						request.setServerPort(newUrl.getPort());
					}
				}
				else
				{
					// append redirect URL to current URL (what browser would do)
					Url mergedURL = new Url(lastRequest.getUrl().getSegments(),
						newUrl.getQueryParameters());
					mergedURL.concatSegments(newUrl.getSegments());

					request.setUrl(mergedURL);
				}

				processRequest(null, null, true);

				--redirectCount;
			}

			return true;
		}
		finally
		{
			redirectCount = 0;
		}
	}

	/**
	 * Determine whether a given response contains a redirect leading to an external site (which
	 * cannot be replicated in WicketTester). This is done by comparing the previous request's
	 * hostname with the hostname given in the redirect.
	 * 
	 * @param requestUrl
	 *            request...
	 * @param newUrl
	 *            ...and the redirect generated in its response
	 * @return true if there is a redirect and it is external, false otherwise
	 */
	private boolean isExternalRedirect(Url requestUrl, Url newUrl)
	{
		String originalHost = requestUrl.getHost();
		String redirectHost = newUrl.getHost();
		Integer originalPort = requestUrl.getPort();
		Integer newPort = newUrl.getPort();

		if (originalHost.equals(redirectHost))
		{
			return false; // identical or both null
		}
		else if (redirectHost == null)
		{
			return false; // no new host
		}
		else if (originalPort.equals(newPort) == false)
		{
			return true;
		}
		else
		{
			return !(redirectHost.equals(originalHost));
		}
	}

	/**
	 * Allows to set Request header value any time. They'll be applied (add/modify) on process
	 * execution {@link #processRequest(MockHttpServletRequest, IRequestHandler, boolean)}. They are
	 * reset immediately after and thus are not re-used for a sequence of requests.
	 * <p>
	 * Deletion (not replace) of pre-configured header value can be achieved by subclassing
	 * {@link #processRequest(MockHttpServletRequest, IRequestHandler, boolean)} and modifying the
	 * request header directly.
	 * 
	 * @param key
	 * @param value
	 */
	public final void addRequestHeader(final String key, final String value)
	{
		Args.notEmpty(key, "key");

		if (preHeader == null)
		{
			preHeader = Generics.newHashMap();
		}

		preHeader.put(key, value);
	}

	/**
	 *
	 */
	private void recordRequestResponse()
	{
		lastRequest = request;
		lastResponse = response;

		previousRequests.add(request);
		previousResponses.add(response);

		// transfer cookies from previous request to previous response, quirky but how old stuff
		// worked...
		if (lastRequest.getCookies() != null)
		{
			for (Cookie cookie : lastRequest.getCookies())
			{
				lastResponse.addCookie(cookie);
			}
		}
	}

	/**
	 * Renders the page specified by given {@link IPageProvider}. After render the page instance can
	 * be retrieved using {@link #getLastRenderedPage()} and the rendered document will be available
	 * in {@link #getLastResponse()}.
	 * 
	 * Depending on {@link RenderStrategy} invoking this method can mean that a redirect will happen
	 * before the actual render.
	 * 
	 * @param pageProvider
	 * @return last rendered page
	 */
	public Page startPage(final IPageProvider pageProvider)
	{
		// should be null for Pages
		componentInPage = null;

		// prepare request
		request.setURL(request.getContextPath() + request.getServletPath() + "/");
		IRequestHandler handler = new RenderPageRequestHandler(pageProvider);

		// process request
		processRequest(request, handler);

		// The page rendered
		return getLastRenderedPage();
	}

	/**
	 * Renders the page.
	 * 
	 * @see #startPage(IPageProvider)
	 * 
	 * @param page
	 * @return Page
	 */
	@SuppressWarnings("unchecked")
	public <T extends Page> T startPage(final T page)
	{
		return (T)startPage(new PageProvider(page));
	}

	/**
	 * Simulates a request to a mounted {@link IResource}
	 * 
	 * @param resource
	 *            the resource to test
	 * @return the used {@link ResourceReference} for the simulation
	 */
	public ResourceReference startResource(final IResource resource)
	{
		return startResourceReference(new ResourceReference("testResourceReference")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public IResource getResource()
			{
				return resource;
			}
		});
	}

	/**
	 * Simulates a request to a mounted {@link ResourceReference}
	 * 
	 * @param reference
	 *            the resource reference to test
	 * @return the tested resource reference
	 */
	public ResourceReference startResourceReference(final ResourceReference reference)
	{
		return startResourceReference(reference, null);
	}

	/**
	 * Simulates a request to a mounted {@link ResourceReference}
	 * 
	 * @param reference
	 *            the resource reference to test
	 * @param pageParameters
	 *            the parameters passed to the resource reference
	 * @return the tested resource reference
	 */
	public ResourceReference startResourceReference(final ResourceReference reference,
		final PageParameters pageParameters)
	{
		// prepare request
		request.setURL(request.getContextPath() + request.getServletPath() + "/");
		IRequestHandler handler = new ResourceReferenceRequestHandler(reference, pageParameters);

		// execute request
		processRequest(request, handler);

		// the reference processed
		return reference;
	}

	/**
	 * @return last response or <code>null</code>> if no response has been produced yet.
	 */
	public MockHttpServletResponse getLastResponse()
	{
		return lastResponse;
	}

	/**
	 * The last response as a string when a page is tested via {@code startPage()} methods.
	 * <p>
	 * In case the processed component was not a {@link Page} then the automatically created page
	 * markup gets removed. If you need the whole returned markup in this case use
	 * {@link #getLastResponse()}{@link MockHttpServletResponse#getDocument() .getDocument()}
	 * </p>
	 * 
	 * @return last response as String.
	 */
	public String getLastResponseAsString()
	{
		String response = lastResponse.getDocument();

		// null, if a Page was rendered last
		if (componentInPage == null)
		{
			return response;
		}

		// remove the markup for the auto-generated page. leave just component's markup
		int end = response.lastIndexOf("</body>");
		if (end > -1)
		{
			int start = response.indexOf("<body>") + "<body>".length();
			response = response.substring(start, end);
		}

		return response;
	}

	/**
	 * This method tries to parse the last response to return the encoded base URL and will throw an
	 * exception if there none was encoded.
	 * 
	 * @return Wicket-Ajax-BaseURL set on last response by {@link AbstractDefaultAjaxBehavior}
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 * @throws ParseException
	 */
	public String getWicketAjaxBaseUrlEncodedInLastResponse() throws IOException,
		ResourceStreamNotFoundException, ParseException
	{
		XmlPullParser parser = new XmlPullParser();
		parser.parse(getLastResponseAsString());
		XmlTag tag;
		while ((tag = parser.nextTag()) != null)
		{
			if (tag.isOpen() && tag.getName().equals("script") &&
				"wicket-ajax-base-url".equals(tag.getAttribute("id")))
			{
				parser.next();
				return parser.getString().toString().split("\\\"")[1];
			}
		}

		fail("Last response has no AJAX base URL set by AbstractDefaultAjaxBehavior.");
		return null;
	}

	/**
	 * @return list of prior requests
	 */
	public List<MockHttpServletRequest> getPreviousRequests()
	{
		return Collections.unmodifiableList(previousRequests);
	}

	/**
	 * @return list of prior responses
	 */
	public List<MockHttpServletResponse> getPreviousResponses()
	{
		return Collections.unmodifiableList(previousResponses);
	}

	/**
	 * Sets whether responses with redirects will be followed automatically.
	 * 
	 * @param followRedirects
	 */
	public void setFollowRedirects(boolean followRedirects)
	{
		this.followRedirects = followRedirects;
	}

	/**
	 * @return <code>true</code> if redirect responses will be followed automatically,
	 *         <code>false</code> otherwise.
	 */
	public boolean isFollowRedirects()
	{
		return followRedirects;
	}

	/**
	 * Encodes the {@link IRequestHandler} to {@link Url}. It should be safe to call this method
	 * outside request thread as log as no registered {@link IRequestMapper} requires a
	 * {@link RequestCycle}.
	 * 
	 * @param handler
	 * @return {@link Url} for handler.
	 */
	public Url urlFor(final IRequestHandler handler)
	{
		Url url = application.getRootRequestMapper().mapHandler(handler);
		return transform(url);
	}

	/**
	 * @param link
	 * @return url for Link
	 */
	public String urlFor(Link<?> link)
	{
		Args.notNull(link, "link");

		Url url = Url.parse(link.urlFor(ILinkListener.INTERFACE, new PageParameters()).toString());
		return transform(url).toString();
	}

	/**
	 * Simulates processing URL that invokes specified {@link RequestListenerInterface} on
	 * component.
	 * 
	 * After the listener interface is invoked the page containing the component will be rendered
	 * (with an optional redirect - depending on {@link RenderStrategy}).
	 * 
	 * @param component
	 * @param listener
	 */
	public void executeListener(final Component component, final RequestListenerInterface listener)
	{
		Args.notNull(component, "component");

		// there are two ways to do this. RequestCycle could be forced to call the handler
		// directly but constructing and parsing the URL increases the chance of triggering bugs
		IRequestHandler handler = new ListenerInterfaceRequestHandler(new PageAndComponentProvider(
			component.getPage(), component), listener);

		Url url = urlFor(handler);
		MockHttpServletRequest request = new MockHttpServletRequest(application, httpSession,
			servletContext);
		request.setUrl(url);

		// Process the request
		processRequest(request, null);
	}

	/**
	 * Simulates invoking a listener on a component. As opposed to the
	 * {@link #executeListener(Component)} method, current request/response objects will be used
	 * 
	 * After the listener interface is invoked the page containing the component will be rendered
	 * (with an optional redirect - depending on {@link RenderStrategy}).
	 * 
	 * @param component
	 * @param listener
	 */
	public void invokeListener(final Component component, final RequestListenerInterface listener)
	{
		Args.notNull(component, "component");

		// there are two ways to do this. RequestCycle could be forced to call the handler
		// directly but constructing and parsing the URL increases the chance of triggering bugs
		IRequestHandler handler = new ListenerInterfaceRequestHandler(new PageAndComponentProvider(
			component.getPage(), component), listener);

		processRequest(handler);
	}

	/**
	 * Builds and processes a request suitable for invoking a listener. The <code>Component</code>
	 * must implement any of the known <code>IListener</code> interfaces.
	 * 
	 * @param component
	 *            the listener to invoke
	 */
	public void executeListener(final Component component)
	{
		Args.notNull(component, "component");

		for (RequestListenerInterface iface : RequestListenerInterface.getRegisteredInterfaces())
		{
			if (iface.getListenerInterfaceClass().isAssignableFrom(component.getClass()))
			{
				executeListener(component, iface);
			}
		}
	}

	/**
	 * Builds and processes a request suitable for executing an <code>AbstractAjaxBehavior</code>.
	 * 
	 * @param behavior
	 *            an <code>AbstractAjaxBehavior</code> to execute
	 */
	public void executeBehavior(final AbstractAjaxBehavior behavior)
	{
		Args.notNull(behavior, "behavior");

		Url url = Url.parse(behavior.getCallbackUrl().toString(),
			Charset.forName(request.getCharacterEncoding()));
		transform(url);
		request.setUrl(url);
		request.addHeader("Wicket-Ajax-BaseURL", url.toString());
		request.addHeader("Wicket-Ajax", "true");

		if (behavior instanceof AjaxFormSubmitBehavior)
		{
			AjaxFormSubmitBehavior formSubmitBehavior = (AjaxFormSubmitBehavior)behavior;
			Form<?> form = formSubmitBehavior.getForm();
			getRequest().setUseMultiPartContentType(form.isMultiPart());
			serializeFormToRequest(form);
		}

		processRequest();
	}

	/**
	 * 
	 * @param link
	 * @return Url
	 */
	public Url urlFor(final AjaxLink<?> link)
	{
		AbstractAjaxBehavior behavior = WicketTesterHelper.findAjaxEventBehavior(link, "onclick");
		Url url = Url.parse(behavior.getCallbackUrl().toString(),
			Charset.forName(request.getCharacterEncoding()));
		return transform(url);
	}

	/**
	 * 
	 * @param url
	 */
	public void executeAjaxUrl(final Url url)
	{
		Args.notNull(url, "url");

		transform(url);
		request.setUrl(url);
		request.addHeader("Wicket-Ajax-BaseURL", url.toString());
		request.addHeader("Wicket-Ajax", "true");

		processRequest();
	}

	/**
	 * Renders a <code>Page</code> from its default constructor.
	 * 
	 * @param <C>
	 * @param pageClass
	 *            a test <code>Page</code> class with default constructor
	 * @return the rendered <code>Page</code>
	 */
	public final <C extends Page> C startPage(final Class<C> pageClass)
	{
		return startPage(pageClass, null);
	}

	/**
	 * Renders a <code>Page</code> from its default constructor.
	 * 
	 * @param <C>
	 * @param pageClass
	 *            a test <code>Page</code> class with default constructor
	 * @param parameters
	 *            the parameters to use for the class.
	 * @return the rendered <code>Page</code>
	 */
	@SuppressWarnings("unchecked")
	public final <C extends Page> C startPage(final Class<C> pageClass,
		final PageParameters parameters)
	{
		Args.notNull(pageClass, "pageClass");

		// must be null for Pages
		componentInPage = null;

		// prepare the request
		request.setUrl(application.getRootRequestMapper().mapHandler(
			new BookmarkablePageRequestHandler(new PageProvider(pageClass, parameters))));

		// process the request
		processRequest();

		// The last rendered page
		return (C)getLastRenderedPage();
	}

	/**
	 * Creates a {@link FormTester} for the <code>Form</code> at a given path, and fills all child
	 * {@link org.apache.wicket.markup.html.form.FormComponent}s with blank <code>String</code>s.
	 * 
	 * @param path
	 *            path to <code>FormComponent</code>
	 * @return a <code>FormTester</code> instance for testing the <code>Form</code>
	 * @see #newFormTester(String, boolean)
	 */
	public FormTester newFormTester(final String path)
	{
		return newFormTester(path, true);
	}

	/**
	 * Creates a {@link FormTester} for the <code>Form</code> at a given path.
	 * 
	 * @param path
	 *            path to <code>FormComponent</code>
	 * @param fillBlankString
	 *            specifies whether to fill all child <code>FormComponent</code>s with blank
	 *            <code>String</code>s
	 * @return a <code>FormTester</code> instance for testing the <code>Form</code>
	 * @see FormTester
	 */
	public FormTester newFormTester(final String path, final boolean fillBlankString)
	{
		return new FormTester(path, (Form<?>)getComponentFromLastRenderedPage(path), this,
			fillBlankString);
	}

	/**
	 * Process a component. A web page will be automatically created with the markup created in
	 * {@link #createPageMarkup(String)}.
	 * <p>
	 *     <strong>Note</strong>: the instantiated component will have an auto-generated id. To
	 *     reach any of its children use their relative path to the component itself. For example
	 *     if the started component has a child a Link component with id "link" then after starting
	 *     the component you can click it with: <code>tester.clickLink("link")</code>
	 * </p>
	 * 
	 * @param <C>
	 *            the type of the component
	 * @param componentClass
	 *            the class of the component to be tested
	 * @return The component processed
	 * @see #startComponentInPage(org.apache.wicket.Component)
	 */
	public final <C extends Component> C startComponentInPage(final Class<C> componentClass)
	{
		return startComponentInPage(componentClass, null);
	}

	/**
	 * Process a component. A web page will be automatically created with the {@code pageMarkup}
	 * provided. In case pageMarkup is null, the markup will be automatically created with
	 * {@link #createPageMarkup(String)}.
	 * <p>
	 *     <strong>Note</strong>: the instantiated component will have an auto-generated id. To
	 *     reach any of its children use their relative path to the component itself. For example
	 *     if the started component has a child a Link component with id "link" then after starting
	 *     the component you can click it with: <code>tester.clickLink("link")</code>
	 * </p>
	 * 
	 * @param <C>
	 *            the type of the component
	 * 
	 * @param componentClass
	 *            the class of the component to be tested
	 * @param pageMarkup
	 *            the markup for the Page that will be automatically created. May be {@code null}.
	 * @return The component processed
	 */
	public final <C extends Component> C startComponentInPage(final Class<C> componentClass,
		final IMarkupFragment pageMarkup)
	{
		Args.notNull(componentClass, "componentClass");

		// Create the component instance from the class
		C comp = null;
		try
		{
			Constructor<C> c = componentClass.getConstructor(String.class);
			comp = c.newInstance(ComponentInPage.ID);
			componentInPage = new ComponentInPage();
			componentInPage.component = comp;
			componentInPage.isInstantiated = true;
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
			fail(String.format("Cannot instantiate component with type '%s' because of '%s'",
				componentClass.getName(), e.getMessage()));
		}

		// process the component
		return startComponentInPage(comp, pageMarkup);
	}

	/**
	 * Process a component. A web page will be automatically created with markup created by the
	 * {@link #createPageMarkup(String)}.
	 * <p>
	 *     <strong>Note</strong>: the component id is set by the user. To
	 *     reach any of its children use this id + their relative path to the component itself. For example
	 *     if the started component has id <em>compId</em> and a Link child component component with id "link"
	 *     then after starting the component you can click it with: <code>tester.clickLink("compId:link")</code>
	 * </p>
	 * 
	 * @param <C>
	 *            the type of the component
	 * @param component
	 *            the component to be tested
	 * @return The component processed
	 * @see #startComponentInPage(Class)
	 */
	public final <C extends Component> C startComponentInPage(final C component)
	{
		return startComponentInPage(component, null);
	}

	/**
	 * Process a component. A web page will be automatically created with the {@code pageMarkup}
	 * provided. In case {@code pageMarkup} is null, the markup will be automatically created with
	 * {@link #createPageMarkup(String)}.
	 * <p>
	 *     <strong>Note</strong>: the component id is set by the user. To
	 *     reach any of its children use this id + their relative path to the component itself. For example
	 *     if the started component has id <em>compId</em> and a Link child component component with id "link"
	 *     then after starting the component you can click it with: <code>tester.clickLink("compId:link")</code>
	 * </p>
	 * 
	 * @param <C>
	 *            the type of the component
	 * @param component
	 *            the component to be tested
	 * @param pageMarkup
	 *            the markup for the Page that will be automatically created. May be {@code null}.
	 * @return The component processed
	 */
	public final <C extends Component> C startComponentInPage(final C component,
		IMarkupFragment pageMarkup)
	{
		Args.notNull(component, "component");

		// Create a page object and assign the markup
		Page page = createPage();
		if (page == null)
		{
			fail("The automatically created page should not be null.");
		}

		// Automatically create the page markup if not provided
		if (pageMarkup == null)
		{
			String markup = createPageMarkup(component.getId());
			if (markup == null)
			{
				fail("The markup for the automatically created page should not be null.");
			}

			try
			{
				// set a ContainerInfo to be able to use HtmlHeaderContainer so header contribution
				// still work. WICKET-3700
				ContainerInfo containerInfo = new ContainerInfo(page);
				MarkupResourceStream markupResourceStream = new MarkupResourceStream(
					new StringResourceStream(markup), containerInfo, page.getClass());

				MarkupParser markupParser = getApplication().getMarkupSettings()
					.getMarkupFactory()
					.newMarkupParser(markupResourceStream);
				pageMarkup = markupParser.parse();
			}
			catch (Exception e)
			{
				fail("Error while parsing the markup for the autogenerated page: " + e.getMessage());
			}
		}
		page.setMarkup(pageMarkup);

		// Add the child component
		page.add(component);

		// Preserve 'componentInPage' because #startPage() needs to null-fy it
		ComponentInPage oldComponentInPage = componentInPage;

		// Process the page
		startPage(page);

		// Remember the "root" component processes and return it
		if (oldComponentInPage != null)
		{
			componentInPage = oldComponentInPage;
		}
		else
		{
			componentInPage = new ComponentInPage();
			componentInPage.component = component;
		}
		return component;
	}

	/**
	 * Creates the markup that will be used for the automatically created {@link Page} that will be
	 * used to test a component with {@link #startComponentInPage(Class, IMarkupFragment)}
	 * 
	 * @param componentId
	 *            the id of the component to be tested
	 * @return the markup for the {@link Page} as {@link String}. Cannot be {@code null}.
	 */
	protected String createPageMarkup(final String componentId)
	{
		return "<html><head></head><body><span wicket:id='" + componentId +
			"'></span></body></html>";
	}

	/**
	 * Creates a {@link Page} to test a component with
	 * {@link #startComponentInPage(Component, IMarkupFragment)}
	 * 
	 * @return a {@link Page} which will contain the component under test as a single child
	 */
	protected Page createPage()
	{
		return new StartComponentInPage();
	}

	/**
	 * A page that is used as the automatically created page for
	 * {@link BaseWicketTester#startComponentInPage(Class)} and the other variations.
	 * <p>
	 * This page caches the generated markup so that it is available even after
	 * {@link Component#detach()} where the {@link Component#markup component's markup cache} is
	 * cleared.
	 */
	public static class StartComponentInPage extends WebPage
	{
		private transient IMarkupFragment pageMarkup = null;

		/**
		 * Construct.
		 */
		public StartComponentInPage()
		{
			setStatelessHint(false);
		}

		@Override
		public IMarkupFragment getMarkup()
		{
			IMarkupFragment calculatedMarkup = null;
			if (pageMarkup == null)
			{
				IMarkupFragment markup = super.getMarkup();
				if (markup != null && markup != Markup.NO_MARKUP)
				{
					calculatedMarkup = markup;
					pageMarkup = markup;
				}
			}
			else
			{
				calculatedMarkup = pageMarkup;
			}

			return calculatedMarkup;
		}

	}

	/**
	 * A helper method for starting a component for a test without attaching it to a Page.
	 * 
	 * Components which are somehow dependent on the page structure can not be currently tested with
	 * this method.
	 * 
	 * Example:
	 * 
	 * UserDataView view = new UserDataView("view", new ListDataProvider(userList));
	 * tester.startComponent(view); assertEquals(4, view.size());
	 * 
	 * @param component
	 * @return the processed component
	 * @see #startComponentInPage(Class)
	 */
	public Component startComponent(final Component component)
	{
		try
		{
			component.internalInitialize();
			if (component instanceof FormComponent)
			{
				((FormComponent<?>)component).processInput();
			}
			component.beforeRender();
		}
		finally
		{
			getRequestCycle().detach();
			component.detach();
		}

		return component;
	}

	/**
	 * Gets the component with the given path from last rendered page. This method fails in case the
	 * component couldn't be found.
	 * 
	 * @param path
	 *            Path to component
	 * @param wantVisibleInHierarchy
	 *            if true component needs to be VisibleInHierarchy else null is returned
	 * @return The component at the path
	 * @see org.apache.wicket.MarkupContainer#get(String)
	 */
	public Component getComponentFromLastRenderedPage(String path,
		final boolean wantVisibleInHierarchy)
	{
		if (componentInPage != null && componentInPage.isInstantiated)
		{
			String componentIdPageId = componentInPage.component.getId() + ':';
			if (path.startsWith(componentIdPageId) == false)
			{
				path =  componentIdPageId + path;
			}
		}

		Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			fail("path: '" + path + "' does not exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
			return null;
		}

		if (!wantVisibleInHierarchy || component.isVisibleInHierarchy())
		{
			return component;
		}

		// Not found or not visible
		return null;
	}

	/**
	 * Gets the component with the given path from last rendered page. This method fails in case the
	 * component couldn't be found, and it will return null if the component was found, but is not
	 * visible.
	 * 
	 * @param path
	 *            Path to component
	 * @return The component at the path
	 * @see org.apache.wicket.MarkupContainer#get(String)
	 */
	public Component getComponentFromLastRenderedPage(String path)
	{
		return getComponentFromLastRenderedPage(path, true);
	}

	/**
	 * assert the text of <code>Label</code> component.
	 * 
	 * @param path
	 *            path to <code>Label</code> component
	 * @param expectedLabelText
	 *            expected label text
	 * @return a <code>Result</code>
	 */
	public Result hasLabel(String path, String expectedLabelText)
	{
		Label label = (Label)getComponentFromLastRenderedPage(path);
		return isEqual(expectedLabelText, label.getDefaultModelObjectAsString());
	}

	/**
	 * assert component class
	 * 
	 * @param <C>
	 * 
	 * @param path
	 *            path to component
	 * @param expectedComponentClass
	 *            expected component class
	 * @return a <code>Result</code>
	 */
	public <C extends Component> Result isComponent(String path, Class<C> expectedComponentClass)
	{
		Component component = getComponentFromLastRenderedPage(path);
		if (component == null)
		{
			return Result.fail("Component not found: " + path);
		}

		return isTrue("component '" + Classes.simpleName(component.getClass()) + "' is not type:" +
			Classes.simpleName(expectedComponentClass),
			expectedComponentClass.isAssignableFrom(component.getClass()));
	}

	/**
	 * assert component visible.
	 * 
	 * @param path
	 *            path to component
	 * @return a <code>Result</code>
	 */
	public Result isVisible(final String path)
	{
		final Result result;

		Component component = getComponentFromLastRenderedPage(path, false);
		if (component == null)
		{
			result = Result.fail("path: '" + path + "' does no exist for page: " +
					Classes.simpleName(getLastRenderedPage().getClass()));
		}
		else
		{
			result = isTrue("component '" + path + "' is not visible",
				component.isVisibleInHierarchy());
		}

		return result;
	}

	/**
	 * assert component invisible.
	 * 
	 * @param path
	 *            path to component
	 * @return a <code>Result</code>
	 */
	public Result isInvisible(final String path)
	{
		final Result result;

		Component component = getComponentFromLastRenderedPage(path, false);
		if (component == null)
		{
			result = Result.fail("path: '" + path + "' does no exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
		}
		else
		{
			result = isFalse("component '" + path + "' is visible",
				component.isVisibleInHierarchy());
		}

		return result;
	}

	/**
	 * assert component enabled.
	 * 
	 * @param path
	 *            path to component
	 * @return a <code>Result</code>
	 */
	public Result isEnabled(final String path)
	{
		Component component = getComponentFromLastRenderedPage(path);
		if (component == null)
		{
			fail("path: '" + path + "' does no exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
		}

		return isTrue("component '" + path + "' is disabled", component.isEnabledInHierarchy());
	}

	/**
	 * assert component disabled.
	 * 
	 * @param path
	 *            path to component
	 * @return a <code>Result</code>
	 */
	public Result isDisabled(final String path)
	{
		Component component = getComponentFromLastRenderedPage(path);
		if (component == null)
		{
			fail("path: '" + path + "' does no exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
		}

		return isFalse("component '" + path + "' is enabled", component.isEnabledInHierarchy());
	}

	/**
	 * assert component required.
	 * 
	 * @param path
	 *            path to component
	 * @return a <code>Result</code>
	 */
	public Result isRequired(String path)
	{
		Component component = getComponentFromLastRenderedPage(path);
		if (component == null)
		{
			fail("path: '" + path + "' does no exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
		}
		else if (component instanceof FormComponent == false)
		{
			fail("path: '" + path + "' is not a form component");
		}

		return isRequired((FormComponent<?>)component);
	}

	/**
	 * assert component required.
	 * 
	 * @param component
	 *            a form component
	 * @return a <code>Result</code>
	 */
	public Result isRequired(FormComponent<?> component)
	{
		return isTrue("component '" + component + "' is not required", component.isRequired());
	}

	/**
	 * assert the content of last rendered page contains(matches) regex pattern.
	 * 
	 * @param pattern
	 *            reqex pattern to match
	 * @return a <code>Result</code>
	 */
	public Result ifContains(String pattern)
	{
		return isTrue("pattern '" + pattern + "' not found in:\n" + getLastResponseAsString(),
			getLastResponseAsString().matches("(?s).*" + pattern + ".*"));
	}

	/**
	 * assert the content of last rendered page contains(matches) regex pattern.
	 * 
	 * @param pattern
	 *            reqex pattern to match
	 * @return a <code>Result</code>
	 */
	public Result ifContainsNot(String pattern)
	{
		return isFalse("pattern '" + pattern + "' found",
			getLastResponseAsString().matches("(?s).*" + pattern + ".*"));
	}

	/**
	 * assert the model of {@link ListView} use expectedList
	 * 
	 * @param path
	 *            path to {@link ListView} component
	 * @param expectedList
	 *            expected list in the model of {@link ListView}
	 */
	public void assertListView(String path, List<?> expectedList)
	{
		ListView<?> listView = (ListView<?>)getComponentFromLastRenderedPage(path);
		WicketTesterHelper.assertEquals(expectedList, listView.getList());
	}

	/**
	 * Click the {@link Link} in the last rendered Page.
	 * <p>
	 * Simulate that AJAX is enabled.
	 * 
	 * @see WicketTester#clickLink(String, boolean)
	 * @param path
	 *            Click the <code>Link</code> in the last rendered Page.
	 */
	public void clickLink(String path)
	{
		clickLink(path, true);
	}

	/**
	 * Click the {@link Link} in the last rendered Page.
	 * <p>
	 * This method also works for {@link AjaxLink}, {@link AjaxFallbackLink} and
	 * {@link AjaxSubmitLink}.
	 * <p>
	 * On AjaxLinks and AjaxFallbackLinks the onClick method is invoked with a valid
	 * AjaxRequestTarget. In that way you can test the flow of your application when using AJAX.
	 * <p>
	 * When clicking an AjaxSubmitLink the form, which the AjaxSubmitLink is attached to is first
	 * submitted, and then the onSubmit method on AjaxSubmitLink is invoked. If you have changed
	 * some values in the form during your test, these will also be submitted. This should not be
	 * used as a replacement for the {@link FormTester} to test your forms. It should be used to
	 * test that the code in your onSubmit method in AjaxSubmitLink actually works.
	 * <p>
	 * This method is also able to simulate that AJAX (javascript) is disabled on the client. This
	 * is done by setting the isAjax parameter to false. If you have an AjaxFallbackLink you can
	 * then check that it doesn't fail when invoked as a normal link.
	 * 
	 * @param path
	 *            path to <code>Link</code> component
	 * @param isAjax
	 *            Whether to simulate that AJAX (javascript) is enabled or not. If it's false then
	 *            AjaxLink and AjaxSubmitLink will fail, since it wouldn't work in real life.
	 *            AjaxFallbackLink will be invoked with null as the AjaxRequestTarget parameter.
	 */
	public void clickLink(String path, boolean isAjax)
	{
		Component linkComponent = getComponentFromLastRenderedPage(path);

		checkUsability(linkComponent, true);

		// if the link is an AjaxLink, we process it differently
		// than a normal link
		if (linkComponent instanceof AjaxLink)
		{
			// If it's not ajax we fail
			if (isAjax == false)
			{
				fail("Link " + path + "is an AjaxLink and will " +
					"not be invoked when AJAX (javascript) is disabled.");
			}

			List<AjaxEventBehavior> behaviors = WicketTesterHelper.findAjaxEventBehaviors(linkComponent, "onclick");
			for (AjaxEventBehavior behavior : behaviors)
			{
				executeBehavior(behavior);
			}
		}
		// AjaxFallbackLinks is processed like an AjaxLink if isAjax is true
		// If it's not handling of the linkComponent is passed through to the
		// Link.
		else if (linkComponent instanceof AjaxFallbackLink && isAjax)
		{
			List<AjaxEventBehavior> behaviors = WicketTesterHelper.findAjaxEventBehaviors(linkComponent, "onclick");
			for (AjaxEventBehavior behavior : behaviors)
			{
				executeBehavior(behavior);
			}
		}
		// if the link is an AjaxSubmitLink, we need to find the form
		// from it using reflection so we know what to submit.
		else if (linkComponent instanceof AjaxSubmitLink)
		{
			// If it's not ajax we fail
			if (isAjax == false)
			{
				fail("Link " + path + " is an AjaxSubmitLink and " +
					"will not be invoked when AJAX (javascript) is disabled.");
			}

			AjaxSubmitLink link = (AjaxSubmitLink)linkComponent;

			String pageRelativePath = link.getInputName();
			request.getPostParameters().setParameterValue(pageRelativePath, "x");

			submitAjaxFormSubmitBehavior(link,
				(AjaxFormSubmitBehavior)WicketTesterHelper.findAjaxEventBehavior(link, "onclick"));
		}
		/*
		 * If the link is a submitlink then we pretend to have clicked it
		 */
		else if (linkComponent instanceof SubmitLink)
		{
			SubmitLink submitLink = (SubmitLink)linkComponent;

			String pageRelativePath = submitLink.getInputName();
			request.getPostParameters().setParameterValue(pageRelativePath, "x");

			serializeFormToRequest(submitLink.getForm());
			submitForm(submitLink.getForm().getPageRelativePath());
		}
		// if the link is a normal link (or ResourceLink)
		else if (linkComponent instanceof AbstractLink)
		{
			AbstractLink link = (AbstractLink)linkComponent;

			/*
			 * If the link is a bookmarkable link, then we need to transfer the parameters to the
			 * next request.
			 */
			if (link instanceof BookmarkablePageLink)
			{
				BookmarkablePageLink<?> bookmarkablePageLink = (BookmarkablePageLink<?>)link;
				try
				{
					Method getParametersMethod = BookmarkablePageLink.class.getDeclaredMethod(
						"getPageParameters", (Class<?>[])null);
					getParametersMethod.setAccessible(true);

					PageParameters parameters = (PageParameters)getParametersMethod.invoke(
						bookmarkablePageLink, (Object[])null);

					startPage(bookmarkablePageLink.getPageClass(), parameters);
				}
				catch (Exception e)
				{
					throw new WicketRuntimeException("Internal error in WicketTester. "
						+ "Please report this in Wicket's Issue Tracker.", e);
				}
			}
			else if (link instanceof ResourceLink)
			{
				try
				{
					Method getURL = ResourceLink.class.getDeclaredMethod("getURL", new Class[0]);
					getURL.setAccessible(true);
					CharSequence url = (CharSequence) getURL.invoke(link);
					executeUrl(url.toString());
				}
				catch (Exception x)
				{
					throw new RuntimeException("An error occurred while clicking on a ResourceLink", x);
				}
			}
			else
			{
				executeListener(link, ILinkListener.INTERFACE);
			}
		}
		else
		{
			fail("Link " + path + " is not a Link, AjaxLink, AjaxFallbackLink or AjaxSubmitLink");
		}
	}

	/**
	 * Submit the given form in the last rendered {@link Page}
	 * <p>
	 * <strong>Note</strong>: Form request parameters have to be set explicitely.
	 * 
	 * @param form
	 *            path to component
	 */
	public void submitForm(Form<?> form)
	{
		submitForm(form.getPageRelativePath());
	}

	/**
	 * Submits the {@link Form} in the last rendered {@link Page}.
	 * <p>
	 * <strong>Note</strong>: Form request parameters have to be set explicitely.
	 * 
	 * @param path
	 *            path to component
	 */
	public void submitForm(String path)
	{
		Form<?> form = (Form<?>)getComponentFromLastRenderedPage(path);
		Url url = Url.parse(
			form.getRootForm()
				.urlFor(IFormSubmitListener.INTERFACE, new PageParameters())
				.toString(), Charset.forName(request.getCharacterEncoding()));

		// make url absolute
		transform(url);

		request.setUrl(url);
		processRequest();
	}

	/**
	 * make url suitable for wicket tester use. usually this involves stripping any leading ..
	 * segments to make the url absolute
	 * 
	 * @param url
	 * @return Url
	 */
	private Url transform(final Url url)
	{
		while (url.getSegments().size() > 0 &&
			(url.getSegments().get(0).equals("..") || url.getSegments().get(0).equals(".")))
		{
			url.getSegments().remove(0);
		}
		return url;
	}

	/**
	 * Asserts the last rendered <code>Page</code> class.
	 * 
	 * @param <C>
	 * @param expectedRenderedPageClass
	 *            expected class of last rendered page
	 * @return a <code>Result</code>
	 */
	public <C extends Page> Result isRenderedPage(Class<C> expectedRenderedPageClass)
	{
		Args.notNull(expectedRenderedPageClass, "expectedRenderedPageClass");

		Page page = getLastRenderedPage();
		if (page == null)
		{
			return Result.fail("page was null");
		}
		if (!expectedRenderedPageClass.isAssignableFrom(page.getClass()))
		{
			return Result.fail(String.format("classes not the same, expected '%s', current '%s'",
				expectedRenderedPageClass, page.getClass()));
		}
		return Result.pass();
	}

	/**
	 * Asserts last rendered <code>Page</code> against an expected HTML document.
	 * <p>
	 * Use <code>-Dwicket.replace.expected.results=true</code> to automatically replace the expected
	 * output file.
	 * </p>
	 * 
	 * @param pageClass
	 *            used to load the <code>File</code> (relative to <code>clazz</code> package)
	 * @param filename
	 *            expected output <code>File</code> name
	 * @throws Exception
	 */
	public void assertResultPage(final Class<?> pageClass, final String filename) throws Exception
	{
		// Validate the document
		String document = getLastResponseAsString();
		DiffUtil.validatePage(document, pageClass, filename, true);
	}

	/**
	 * Asserts last rendered <code>Page</code> against an expected HTML document as a
	 * <code>String</code>.
	 * 
	 * @param expectedDocument
	 *            expected output
	 * @return a <code>Result</code>
	 * @throws Exception
	 */
	public Result isResultPage(final String expectedDocument) throws Exception
	{
		// Validate the document
		String document = getLastResponseAsString();
		return isTrue("expected rendered page equals", document.equals(expectedDocument));
	}

	/**
	 * Asserts no error-level feedback messages.
	 * 
	 * @return a <code>Result</code>
	 */
	public Result hasNoErrorMessage()
	{
		List<Serializable> messages = getMessages(FeedbackMessage.ERROR);
		return isTrue(
			"expect no error message, but contains\n" + WicketTesterHelper.asLined(messages),
			messages.isEmpty());
	}

	/**
	 * Asserts no info-level feedback messages.
	 * 
	 * @return a <code>Result</code>
	 */
	public Result hasNoInfoMessage()
	{
		List<Serializable> messages = getMessages(FeedbackMessage.INFO);
		return isTrue(
			"expect no info message, but contains\n" + WicketTesterHelper.asLined(messages),
			messages.isEmpty());
	}

	/**
	 * Retrieves <code>FeedbackMessages</code>.
	 * 
	 * @param level
	 *            level of feedback message, for example:
	 *            <code>FeedbackMessage.DEBUG or FeedbackMessage.INFO.. etc</code>
	 * @return <code>List</code> of messages (as <code>String</code>s)
	 * @see FeedbackMessage
	 */
	public List<Serializable> getMessages(final int level)
	{
		List<FeedbackMessage> allMessages = new FeedbackCollector(getLastRenderedPage()).collect(new IFeedbackMessageFilter()
		{

			@Override
			public boolean accept(FeedbackMessage message)
			{
				return message.getLevel() == level;
			}
		});

		List<Serializable> actualMessages = Generics.newArrayList();
		for (FeedbackMessage message : allMessages)
		{
			actualMessages.add(message.getMessage());
		}
		return actualMessages;
	}

	/**
	 * Dumps the source of last rendered <code>Page</code>.
	 */
	public void dumpPage()
	{
		log.info(getLastResponseAsString());
	}

	/**
	 * Dumps the <code>Component</code> trees.
	 */
	public void debugComponentTrees()
	{
		debugComponentTrees("");
	}

	/**
	 * Dumps the <code>Component</code> trees to log. Show only the <code>Component</code>s whose
	 * paths contain the filter <code>String</code>.
	 * 
	 * @param filter
	 *            a filter <code>String</code>
	 */
	public void debugComponentTrees(String filter)
	{
		log.info("debugging ----------------------------------------------");
		for (WicketTesterHelper.ComponentData obj : WicketTesterHelper.getComponentData(getLastRenderedPage()))
		{
			if (obj.path.matches(".*" + filter + ".*"))
			{
				log.info("path\t" + obj.path + " \t" + obj.type + " \t[" + obj.value + "]");
			}
		}
	}

	/**
	 * Tests that a <code>Component</code> has been added to a <code>AjaxRequestTarget</code>, using
	 * {@link org.apache.wicket.ajax.AjaxRequestTarget#add(org.apache.wicket.Component...)}. This
	 * method actually tests that a <code>Component</code> is on the Ajax response sent back to the
	 * client.
	 * <p>
	 * PLEASE NOTE! This method doesn't actually insert the <code>Component</code> in the client DOM
	 * tree, using JavaScript. But it shouldn't be needed because you have to trust that the Wicket
	 * Ajax JavaScript just works.
	 * 
	 * @param component
	 *            the <code>Component</code> to test
	 * @return a <code>Result</code>
	 */
	public Result isComponentOnAjaxResponse(final Component component)
	{
		String failMessage = "A component which is null could not have been added to the AJAX response";
		notNull(failMessage, component);

		Result result;

		// test that the component renders the placeholder tag if it's not visible
		if (!component.isVisible())
		{
			failMessage = "A component which is invisible and doesn't render a placeholder tag"
				+ " will not be rendered at all and thus won't be accessible for subsequent AJAX interaction";
			result = isTrue(failMessage, component.getOutputMarkupPlaceholderTag());
			if (result.wasFailed())
			{
				return result;
			}
		}

		// Get the AJAX response
		String ajaxResponse = getLastResponseAsString();

		// Test that the previous response was actually a AJAX response
		failMessage = "The Previous response was not an AJAX response. "
			+ "You need to execute an AJAX event, using clickLink, before using this assert";
		boolean isAjaxResponse = Pattern.compile(
			"^<\\?xml version=\"1.0\" encoding=\".*?\"\\?><ajax-response>")
			.matcher(ajaxResponse)
			.find();
		result = isTrue(failMessage, isAjaxResponse);
		if (result.wasFailed())
		{
			return result;
		}

		// See if the component has a markup id
		String markupId = component.getMarkupId();

		failMessage = "The component doesn't have a markup id, "
			+ "which means that it can't have been added to the AJAX response";
		result = isTrue(failMessage, !Strings.isEmpty(markupId));
		if (result.wasFailed())
		{
			return result;
		}

		// Look for that the component is on the response, using the markup id
		boolean isComponentInAjaxResponse = ajaxResponse.matches("(?s).*<component id=\"" +
			markupId + "\"[^>]*?>.*");
		failMessage = "Component wasn't found in the AJAX response";
		return isTrue(failMessage, isComponentInAjaxResponse);
	}

	/**
	 * Simulates the firing of an Ajax event.
	 * 
	 * @see #executeAjaxEvent(Component, String)
	 * 
	 * @since 1.2.3
	 * @param componentPath
	 *            the <code>Component</code> path
	 * @param event
	 *            the event which we simulate being fired. If <code>event</code> is
	 *            <code>null</code>, the test will fail.
	 */
	public void executeAjaxEvent(final String componentPath, final String event)
	{
		Component component = getComponentFromLastRenderedPage(componentPath);
		executeAjaxEvent(component, event);
	}

	/**
	 * Simulates the firing of all ajax timer behaviors on the page
	 * 
	 * @param page
	 *      the page which timers will be executed
	 */
	public void executeAllTimerBehaviors(final MarkupContainer page)
	{
		// execute all timer behaviors for the page itself
		internalExecuteAllTimerBehaviors(page);

		// and for all its children
		page.visitChildren(Component.class, new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				internalExecuteAllTimerBehaviors(component);
			}
		});
	}

	private void internalExecuteAllTimerBehaviors(final Component component)
	{
		List<AbstractAjaxTimerBehavior> behaviors = component.getBehaviors(AbstractAjaxTimerBehavior.class);
		for (AbstractAjaxTimerBehavior timer : behaviors)
		{
			checkUsability(component, true);

			if (!timer.isStopped())
			{
				if (log.isDebugEnabled())
				{
					log.debug("Triggering AjaxSelfUpdatingTimerBehavior: {}", component.getClassRelativePath());
				}

				executeBehavior(timer);
			}
		}
	}

	/**
	 * Simulates the firing of an Ajax event. You add an Ajax event to a <code>Component</code> by
	 * using:
	 * 
	 * <pre>
	 *     ...
	 *     component.add(new AjaxEventBehavior(&quot;ondblclick&quot;) {
	 *         public void onEvent(AjaxRequestTarget) {}
	 *     });
	 *     ...
	 * </pre>
	 * 
	 * You can then test that the code inside <code>onEvent</code> actually does what it's supposed
	 * to, using the <code>WicketTester</code>:
	 * 
	 * <pre>
	 *     ...
	 *     tester.executeAjaxEvent(component, &quot;ondblclick&quot;);
	 *     // Test that the code inside onEvent is correct.
	 *     ...
	 * </pre>
	 * 
	 * This also works with <code>AjaxFormSubmitBehavior</code>, where it will "submit" the
	 * <code>Form</code> before executing the command.
	 * <p>
	 * PLEASE NOTE! This method doesn't actually insert the <code>Component</code> in the client DOM
	 * tree, using JavaScript.
	 * 
	 * @param component
	 *            the <code>Component</code> that has the <code>AjaxEventBehavior</code> we want to
	 *            test. If the <code>Component</code> is <code>null</code>, the test will fail.
	 * @param event
	 *            the event to simulate being fired. If <code>event</code> is <code>null</code>, the
	 *            test will fail.
	 */
	public void executeAjaxEvent(final Component component, final String event)
	{
		Args.notNull(component, "component");
		Args.notNull(event, "event");

		checkUsability(component, true);

		List<AjaxEventBehavior> ajaxEventBehaviors = WicketTesterHelper.findAjaxEventBehaviors(component,
				event);
		for (AjaxEventBehavior ajaxEventBehavior : ajaxEventBehaviors)
		{
			executeBehavior(ajaxEventBehavior);
		}
	}

	/**
	 * Retrieves a <code>TagTester</code> based on a <code>wicket:id</code>. If more
	 * <code>Component</code>s exist with the same <code>wicket:id</code> in the markup, only the
	 * first one is returned.
	 * 
	 * @param wicketId
	 *            the <code>wicket:id</code> to search for
	 * @return the <code>TagTester</code> for the tag which has the given <code>wicket:id</code>
	 */
	public TagTester getTagByWicketId(String wicketId)
	{
		return TagTester.createTagByAttribute(getLastResponseAsString(), "wicket:id", wicketId);
	}

	/**
	 * Modified version of BaseWicketTester#getTagByWicketId(String) that returns all matching tags
	 * instead of just the first.
	 * 
	 * @param wicketId
	 * @return List of Tags
	 */
	public List<TagTester> getTagsByWicketId(String wicketId)
	{
		return TagTester.createTagsByAttribute(getLastResponseAsString(), "wicket:id", wicketId,
			false);
	}

	/**
	 * Retrieves a <code>TagTester</code> based on an DOM id. If more <code>Component</code>s exist
	 * with the same id in the markup, only the first one is returned.
	 * 
	 * @param id
	 *            the DOM id to search for.
	 * @return the <code>TagTester</code> for the tag which has the given DOM id
	 */
	public TagTester getTagById(String id)
	{
		return TagTester.createTagByAttribute(getLastResponseAsString(), "id", id);
	}

	/**
	 * Helper method for all the places where an Ajax call should submit an associated
	 * <code>Form</code>.
	 * 
	 * @param component
	 *            The component the behavior is attached to
	 * @param behavior
	 *            The <code>AjaxFormSubmitBehavior</code> with the <code>Form</code> to "submit"
	 */
	private void submitAjaxFormSubmitBehavior(final Component component,
		AjaxFormSubmitBehavior behavior)
	{
		// The form that needs to be "submitted".
		Form<?> form = behavior.getForm();
		assertNotNull("No form attached to the submitlink.", form);

		checkUsability(form, true);
		serializeFormToRequest(form);
		executeBehavior(behavior);
	}

	/**
	 * Puts all not already scheduled (e.g. via {@link FormTester#setValue(String, String)}) form
	 * component values in the post parameters for the next form submit
	 * 
	 * @param form
	 *            the {@link Form} which components should be submitted
	 */
	private void serializeFormToRequest(final Form<?> form)
	{
		final MockRequestParameters postParameters = request.getPostParameters();
		final Set<String> currentParameterNamesSet = postParameters.getParameterNames();

		form.visitFormComponents(new IVisitor<FormComponent<?>, Void>()
		{
			@Override
			public void component(final FormComponent<?> formComponent, final IVisit<Void> visit)
			{
				final String inputName = formComponent.getInputName();
				if (!currentParameterNamesSet.contains(inputName))
				{
					String[] values = FormTester.getInputValue(formComponent);
					for (String value : values)
					{
						postParameters.addParameterValue(inputName, value);
					}
				}
			}
		});
	}

	/**
	 * Retrieves the content type from the response header.
	 * 
	 * @return the content type from the response header
	 */
	public String getContentTypeFromResponseHeader()
	{
		String contentType = getLastResponse().getContentType();
		assertNotNull("No Content-Type header found", contentType);
		return contentType;
	}

	/**
	 * Retrieves the content length from the response header.
	 * 
	 * @return the content length from the response header
	 */
	public int getContentLengthFromResponseHeader()
	{
		String contentLength = getLastResponse().getHeader("Content-Length");
		assertNotNull("No Content-Length header found", contentLength);
		return Integer.parseInt(contentLength);
	}

	/**
	 * Retrieves the last-modified value from the response header.
	 * 
	 * @return the last-modified value from the response header
	 */
	public String getLastModifiedFromResponseHeader()
	{
		return getLastResponse().getHeader("Last-Modified");
	}

	/**
	 * Retrieves the content disposition from the response header.
	 * 
	 * @return the content disposition from the response header
	 */
	public String getContentDispositionFromResponseHeader()
	{
		return getLastResponse().getHeader("Content-Disposition");
	}

	/**
	 * Rebuilds {@link ServletWebRequest} used by wicket from the mock request used to build
	 * requests. Sometimes this method is useful when changes need to be checked without processing
	 * a request.
	 */
	public void applyRequest()
	{
		Request req = newServletWebRequest();
		requestCycle.setRequest(req);
		if (useRequestUrlAsBase)
		{
			requestCycle.getUrlRenderer().setBaseUrl(req.getUrl());
		}
	}

	/**
	 * 
	 * @param message
	 * @param condition
	 * @return fail with message if false
	 */
	private Result isTrue(String message, boolean condition)
	{
		if (condition)
		{
			return Result.pass();
		}
		return Result.fail(message);
	}

	/**
	 * 
	 * @param message
	 * @param condition
	 * @return fail with message if true
	 */
	private Result isFalse(String message, boolean condition)
	{
		if (!condition)
		{
			return Result.pass();
		}
		return Result.fail(message);
	}

	/**
	 * 
	 * @param expected
	 * @param actual
	 * @return fail with message if not equal
	 */
	protected final Result isEqual(Object expected, Object actual)
	{
		if (expected == null && actual == null)
		{
			return Result.pass();
		}
		if (expected != null && expected.equals(actual))
		{
			return Result.pass();
		}
		String message = "expected:<" + expected + "> but was:<" + actual + ">";
		return Result.fail(message);
	}

	/**
	 * 
	 * @param message
	 * @param object
	 */
	private void notNull(String message, Object object)
	{
		if (object == null)
		{
			fail(message);
		}
	}

	/**
	 * 
	 * @param message
	 * @param object
	 * @return fail with message if not null
	 */
	private Result isNull(String message, Object object)
	{
		if (object != null)
		{
			return Result.fail(message);
		}
		return Result.pass();
	}

	/**
	 * Checks whether a component is visible and/or enabled before usage
	 * 
	 * @param component
	 * @param throwException
	 * @return result
	 */
	protected Result checkUsability(final Component component, boolean throwException)
	{
		Result res = Result.pass();

		if (component.isVisibleInHierarchy() == false)
		{
			res = Result.fail("The component is currently not visible in the hierarchy and thus you can not be used." +
				" Component: " + component);
		}

		if (component.isEnabledInHierarchy() == false)
		{
			res = Result.fail("The component is currently not enabled in the hierarchy and thus you can not be used." +
				" Component: " + component);
		}

		if (throwException && res.wasFailed())
		{
			throw new AssertionFailedError(res.getMessage());
		}
		return res;
	}

	/**
	 * @return request cycle
	 */
	public RequestCycle getRequestCycle()
	{
		return requestCycle;
	}

	/**
	 * @return servlet response
	 */
	public MockHttpServletResponse getResponse()
	{
		return response;
	}

	/**
	 * @return last request
	 */
	public MockHttpServletRequest getLastRequest()
	{
		return lastRequest;
	}

	/**
	 * @return true, if exceptions are exposed
	 */
	public boolean isExposeExceptions()
	{
		return exposeExceptions;
	}

	/**
	 * @param exposeExceptions
	 */
	public void setExposeExceptions(boolean exposeExceptions)
	{
		this.exposeExceptions = exposeExceptions;
	}

	/**
	 * @return useRequestUrlAsBase
	 */
	public boolean isUseRequestUrlAsBase()
	{
		return useRequestUrlAsBase;
	}

	/**
	 * @param setBaseUrl
	 */
	public void setUseRequestUrlAsBase(boolean setBaseUrl)
	{
		useRequestUrlAsBase = setBaseUrl;
	}

	/**
	 * Starts a page, a shared resource or a {@link IRequestListener} depending on what the
	 * {@link IRequestMapper}s resolve for the passed url.
	 * 
	 * @param _url
	 *            the url to resolve and execute
	 */
	public void executeUrl(final String _url)
	{
		Url url = Url.parse(_url, Charset.forName(request.getCharacterEncoding()));
		transform(url);
		getRequest().setUrl(url);
		processRequest();
	}

	/**
	 *
	 */
	private class LastPageRecordingPageRendererProvider implements IPageRendererProvider
	{
		private final IPageRendererProvider delegate;

		private Page lastPage;

		public LastPageRecordingPageRendererProvider(IPageRendererProvider delegate)
		{
			this.delegate = delegate;
		}

		@Override
		public PageRenderer get(RenderPageRequestHandler handler)
		{
			Page newPage = (Page)handler.getPageProvider().getPageInstance();
			if (componentInPage != null && lastPage != null &&
				lastPage.getPageClass() != newPage.getPageClass())
			{
				// WICKET-3913: reset startComponent if a new page type is rendered
				componentInPage = null;
			}
			lastRenderedPage = lastPage = newPage;
			return delegate.get(handler);
		}
	}

	/**
	 *
	 */
	private class TestExceptionMapper implements IExceptionMapper
	{
		private final IExceptionMapper delegate;

		public TestExceptionMapper(IExceptionMapper delegate)
		{
			this.delegate = delegate;
		}

		@Override
		public IRequestHandler map(Exception e)
		{
			if (exposeExceptions)
			{
				if (e instanceof RuntimeException)
				{
					throw (RuntimeException)e;
				}
				else
				{
					throw new WicketRuntimeException(e);
				}
			}
			else
			{
				return delegate.map(e);
			}
		}
	}

	/**
	 *
	 */
	private class TestRequestCycleProvider implements IRequestCycleProvider
	{
		private final IRequestCycleProvider delegate;

		public TestRequestCycleProvider(IRequestCycleProvider delegate)
		{
			this.delegate = delegate;
		}

		@Override
		public RequestCycle get(RequestCycleContext context)
		{
			context.setRequestMapper(new TestRequestMapper(context.getRequestMapper()));
			forcedHandler = null;
			context.setExceptionMapper(new TestExceptionMapper(context.getExceptionMapper()));
			return delegate.get(context);
		}
	}

	/**
	 *
	 */
	private class TestRequestMapper implements IRequestMapper
	{
		private final IRequestMapper delegate;

		public TestRequestMapper(IRequestMapper delegate)
		{
			this.delegate = delegate;
		}

		@Override
		public int getCompatibilityScore(Request request)
		{
			return delegate.getCompatibilityScore(request);
		}

		@Override
		public Url mapHandler(IRequestHandler requestHandler)
		{
			return delegate.mapHandler(requestHandler);
		}

		@Override
		public IRequestHandler mapRequest(Request request)
		{
			if (forcedHandler != null)
			{
				IRequestHandler handler = forcedHandler;
				forcedHandler = null;
				return handler;
			}
			else
			{
				return delegate.mapRequest(request);
			}
		}
	}

	/**
	 *
	 */

	/**
	 *
	 */
	private static class TestPageManagerProvider implements IPageManagerProvider
	{
		@Override
		public IPageManager get(IPageManagerContext pageManagerContext)
		{
			return new MockPageManager();
		}
	}

	/**
	 *
	 */
	private class TestFilterConfig implements FilterConfig
	{
		private final Map<String, String> initParameters = new HashMap<String, String>();

		public TestFilterConfig()
		{
			initParameters.put(WicketFilter.FILTER_MAPPING_PARAM, "/servlet/*");
		}

		@Override
		public String getFilterName()
		{
			return getClass().getName();
		}

		@Override
		public ServletContext getServletContext()
		{
			return servletContext;
		}

		@Override
		public String getInitParameter(String s)
		{
			return initParameters.get(s);
		}

		@Override
		public Enumeration<String> getInitParameterNames()
		{
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	/**
	 *
	 */
	private static class WicketTesterServletWebResponse extends ServletWebResponse
		implements
			IMetaDataBufferingWebResponse
	{
		private List<Cookie> cookies = new ArrayList<Cookie>();

		public WicketTesterServletWebResponse(ServletWebRequest request,
			MockHttpServletResponse response)
		{
			super(request, response);
		}

		@Override
		public void addCookie(Cookie cookie)
		{
			super.addCookie(cookie);
			cookies.add(cookie);
		}

		@Override
		public void writeMetaData(WebResponse webResponse)
		{
			for (Cookie cookie : cookies)
			{
				webResponse.addCookie(cookie);
			}
		}

		@Override
		public void sendRedirect(String url)
		{
			super.sendRedirect(url);
			try
			{
				getContainerResponse().sendRedirect(url);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
