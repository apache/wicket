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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.IPageRendererProvider;
import org.apache.wicket.IRequestCycleProvider;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.BehaviorsUtil;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IFormSubmitListener;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.mock.MockPageManager;
import org.apache.wicket.mock.MockSessionStore;
import org.apache.wicket.pageStore.IPageManager;
import org.apache.wicket.pageStore.IPageManagerContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.handler.IPageProvider;
import org.apache.wicket.request.handler.PageAndComponentProvider;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.impl.BookmarkablePageRequestHandler;
import org.apache.wicket.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache.wicket.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.request.handler.impl.render.PageRenderer;
import org.apache.wicket.request.mapper.parameters.PageParameters;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.Strings;
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
 * 
 */
public class BaseWicketTester
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(BaseWicketTester.class);

	/**
	 * @author jcompagner
	 */
	private static final class TestPageSource implements ITestPageSource
	{
		private final Page page;

		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 * 
		 * @param page
		 */
		private TestPageSource(Page page)
		{
			this.page = page;
		}

		public Page getTestPage()
		{
			return page;
		}
	}

	private org.apache.wicket.protocol.http.mock.MockServletContext servletContext;
	private MockHttpSession hsession;

	private final WebApplication application;

	private boolean followRedirects = true;
	private int redirectCount;

	private MockHttpServletRequest lastRequest;
	private MockHttpServletResponse lastResponse;

	private final List<MockHttpServletRequest> previousRequests = new ArrayList<MockHttpServletRequest>();
	private final List<MockHttpServletResponse> previousResponses = new ArrayList<MockHttpServletResponse>();

	private final ThreadContext oldThreadContext;

	/** current request */
	private MockHttpServletRequest request;
	/** current response */
	private MockHttpServletResponse response;

	/** current session */
	private Session session;

	/** current request cycle */
	private RequestCycle requestCycle;

	private Page lastRenderedPage;

	private boolean exposeExceptions = true;

	private IRequestHandler forcedHandler;

	/**
	 * @return last rendered page
	 */
	public Page getLastRenderedPage()
	{
		return lastRenderedPage;
	}


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
	 * 
	 * @param homePage
	 *            a home page <code>Class</code>
	 */
	public <C extends Page> BaseWicketTester(final Class<C> homePage)
	{
		this(new MockApplication()
		{
			/**
			 * @see org.apache.wicket.Application#getHomePage()
			 */
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
		this(application, null);
	}

	/**
	 * Creates a <code>WicketTester</code>.
	 * 
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 * 
	 * 
	 * @param servletContextBasePath
	 *            the absolute path on disk to the web application's contents (e.g. war root) - may
	 *            be <code>null</code>
	 */
	public BaseWicketTester(final WebApplication application, String servletContextBasePath)
	{
		servletContext = new org.apache.wicket.protocol.http.mock.MockServletContext(application,
			servletContextBasePath);

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

		hsession = new MockHttpSession(servletContext);

		oldThreadContext = ThreadContext.detach();

		this.application = application;

		// FIXME some tests are leaking applications by not calling destroy on them or overriding
		// teardown() without calling super, for now we work around by making each name unique
		this.application.setName("WicketTesterApplication-" + UUID.randomUUID());
		this.application.set();

		application.setServletContext(servletContext);

		// initialize the application
		this.application.initApplication();

		// reconfigure application for the test environment
		application.setPageRendererProvider(new LastPageRecordingPageRendererProvider(
			application.getPageRendererProvider()));
		application.setRequestCycleProvider(new TestRequestCycleProvider(
			application.getRequestCycleProvider()));
		application.setSessionStoreProvider(new TestSessionStoreProvider());
		application.setPageManagerProvider(new TestPageManagerProvider());

		// prepare session
		setupNextRequestCycle();
	}

	private void setupNextRequestCycle()
	{
		request = new MockHttpServletRequest(application, hsession, servletContext);
		request.setURL(request.getContextPath() + request.getServletPath() + "/");
		response = new MockHttpServletResponse(request);


		requestCycle = application.createRequestCycle(createServletWebRequest(),
			createServletWebResponse());
		requestCycle.setCleanupFeedbackMessagesOnDetach(false);
		ThreadContext.setRequestCycle(requestCycle);


		if (session == null)
		{
			createNewSession();
		}
	}


	/**
	 * @return
	 */
	private ServletWebResponse createServletWebResponse()
	{
		return new ServletWebResponse(request, response)
		{
			@Override
			public void sendRedirect(String url)
			{
				super.sendRedirect(url);
				try
				{
					getHttpServletResponse().sendRedirect(url);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		};
	}

	/**
	 * @return
	 */
	private ServletWebRequest createServletWebRequest()
	{
		return new ServletWebRequest(request, request.getFilterPrefix());
	}


	/**
	 * 
	 */
	private void createNewSession()
	{
		session = Session.get();
		application.getSessionStore().bind(null, session);
		ThreadContext.setSession(session);
	}

	public MockHttpServletRequest getRequest()
	{
		return request;
	}

	public void setRequest(MockHttpServletRequest request)
	{
		this.request = request;
		applyRequest();
	}


	/**
	 * Creates a <code>WicketTester</code> for unit testing.
	 * 
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 * @param path
	 *            the absolute path on disk to the <code>WebApplication</code>'s contents (e.g. war
	 *            root) - may be <code>null</code>
	 * 
	 * @see org.apache.wicket.protocol.http.MockWebApplication#MockWebApplication(org.apache.wicket.protocol.http.WebApplication,
	 *      String)
	 */
// public BaseWicketTester(final WebApplication application, final String path)
// {
// super(application, path);
// }

	public Session getSession()
	{
		return session;
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
	 * Destroys the tester. Restores {@link ThreadContext} to state before instance of
	 * {@link WicketTester} was created.
	 */
	public void destroy()
	{
		application.internalDestroy();
		ThreadContext.detach();
	}


	public boolean processRequest()
	{
		return processRequest(null, null);
	}

	/**
	 * Processes the request in mocked Wicket environment.
	 * 
	 * @param request
	 *            request to process
	 * 
	 */
	public void processRequest(MockHttpServletRequest request)
	{
		processRequest(request, null);
	}

	/**
	 * Processes the request in mocked Wicket environment.
	 * 
	 * @param request
	 *            request to process
	 * 
	 * @param forcedRequestHandler
	 *            optional parameter to override parsing the request URL and force
	 *            {@link IRequestHandler}
	 */
	public boolean processRequest(MockHttpServletRequest request,
		IRequestHandler forcedRequestHandler)
	{
		return processRequest(request, forcedRequestHandler, false);
	}

	public boolean processRequest(IRequestHandler forcedRequestHandler)
	{
		return processRequest(null, forcedRequestHandler, false);
	}


	private boolean processRequest(MockHttpServletRequest forcedRequest,
		IRequestHandler forcedRequestHandler, boolean redirect)
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

		try
		{

			if (!redirect)
			{
				/*
				 * we do not reset the session during redirect processing because we want to
				 * preserve the state before the redirect, eg any error messages reported
				 */
				session.cleanupFeedbackMessages();
			}

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
				if (redirectCount == 100)
				{
					throw new IllegalStateException(
						"Possible infinite redirect detected. Bailing out.");
				}
				++redirectCount;
				Url newUrl = Url.parse(lastResponse.getRedirectLocation(),
					Charset.forName(request.getCharacterEncoding()));

				if (newUrl.isAbsolute())
				{
					throw new WicketRuntimeException("Can not follow absolute redirect URL.");
				}

				// append redirect URL to current URL (what browser would do)
				Url mergedURL = new Url(lastRequest.getUrl().getSegments(),
					newUrl.getQueryParameters());
				mergedURL.concatSegments(newUrl.getSegments());

				request.setUrl(mergedURL);
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
	 * be retreived using {@link #getLastRenderedPage()} and the rendered document will be available
	 * in {@link #getLastResponse()}.
	 * 
	 * Depending on {@link RenderStrategy} invoking this method can mean that a redirect will happen
	 * before the actual render.
	 * 
	 * @param pageProvider
	 * @return last rendered page
	 */
	public Page startPage(IPageProvider pageProvider)
	{
		request = new MockHttpServletRequest(application, hsession, servletContext);
		request.setURL(request.getContextPath() + request.getServletPath() + "/");
		IRequestHandler handler = new RenderPageRequestHandler(pageProvider);
		processRequest(request, handler);
		return getLastRenderedPage();
	}

	/**
	 * Renders the page.
	 * 
	 * @see #startPage(IPageProvider)
	 * 
	 * @param page
	 */
	public Page startPage(Page page)
	{
		return startPage(new PageProvider(page));
	}

	/**
	 * @return last response or <code>null</code>> if no response has been produced yet.
	 */
	public MockHttpServletResponse getLastResponse()
	{
		return lastResponse;
	}

	public String getLastResponseAsString()
	{
		return lastResponse.getDocument();
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
	public Url urlFor(IRequestHandler handler)
	{
		Url url = application.getRootRequestMapper().mapHandler(handler);
		transform(url);
		return url;
	}

	public String urlFor(Link link)
	{
		return link.urlFor(ILinkListener.INTERFACE).toString();
	}

	/**
	 * Renders a <code>Page</code> defined in <code>TestPageSource</code>. This is usually used when
	 * a page does not have default constructor. For example, a <code>ViewBook</code> page requires
	 * a <code>Book</code> instance:
	 * 
	 * <pre>
	 * tester.startPage(new TestPageSource()
	 * {
	 * 	public Page getTestPage()
	 * 	{
	 * 		Book mockBook = new Book(&quot;myBookName&quot;);
	 * 		return new ViewBook(mockBook);
	 * 	}
	 * });
	 * </pre>
	 * 
	 * @param testPageSource
	 *            a <code>Page</code> factory that creates a test page instance
	 * @return the rendered Page
	 */
	public final Page startPage(final ITestPageSource testPageSource)
	{
		return startPage(testPageSource.getTestPage());
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
	public void executeListener(Component component, RequestListenerInterface listener)
	{
		// there are two ways to do this. RequestCycle could be forced to call the handler
		// directly but constructing and parsing the URL increases the chance of triggering bugs
		IRequestHandler handler = new ListenerInterfaceRequestHandler(new PageAndComponentProvider(
			component.getPage(), component), listener);

		Url url = urlFor(handler);
		MockHttpServletRequest request = new MockHttpServletRequest(application, hsession,
			servletContext);
		request.setUrl(url);
		processRequest(request, null);
	}


	/**
	 * Builds and processes a request suitable for invoking a listener. The <code>Component</code>
	 * must implement any of the known <code>IListener</code> interfaces.
	 * 
	 * @param component
	 *            the listener to invoke
	 */
	public void executeListener(Component component)
	{
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
		Url url = Url.parse(behavior.getCallbackUrl().toString(),
			Charset.forName(request.getCharacterEncoding()));
		transform(url);
		request.setUrl(url);
		request.addHeader("Wicket-Ajax-BaseURL", url.toString());
		request.addHeader("Wicket-Ajax", "Wicket-Ajax");
		processRequest();
	}

	public Url urlFor(AjaxLink link)
	{
		AbstractAjaxBehavior behavior = WicketTesterHelper.findAjaxEventBehavior(link, "onclick");
		Url url = Url.parse(behavior.getCallbackUrl().toString(),
			Charset.forName(request.getCharacterEncoding()));
		transform(url);
		return url;
	}

	public void executeAjaxUrl(Url url)
	{
		transform(url);
		request.setUrl(url);
		request.addHeader("Wicket-Ajax-BaseURL", url.toString());
		request.addHeader("Wicket-Ajax", "Wicket-Ajax");
		processRequest();
	}

	/**
	 * Renders a <code>Page</code> from its default constructor.
	 * 
	 * @param <C>
	 * 
	 * @param pageClass
	 *            a test <code>Page</code> class with default constructor
	 * @return the rendered <code>Page</code>
	 */
	public final <C extends Page> Page startPage(Class<C> pageClass)
	{
		request.setUrl(application.getRootRequestMapper().mapHandler(
			new BookmarkablePageRequestHandler(new PageProvider(pageClass))));
		processRequest();
		return getLastRenderedPage();
	}

	/**
	 * Renders a <code>Page</code> from its default constructor.
	 * 
	 * @param <C>
	 * 
	 * @param pageClass
	 *            a test <code>Page</code> class with default constructor
	 * @param parameters
	 *            the parameters to use for the class.
	 * @return the rendered <code>Page</code>
	 */
	public final <C extends Page> Page startPage(Class<C> pageClass, PageParameters parameters)
	{
		request.setUrl(application.getRootRequestMapper().mapHandler(
			new BookmarkablePageRequestHandler(new PageProvider(pageClass, parameters))));
		processRequest();
		return getLastRenderedPage();
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
	public FormTester newFormTester(String path)
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
	public FormTester newFormTester(String path, boolean fillBlankString)
	{
		return new FormTester(path, (Form<?>)getComponentFromLastRenderedPage(path), this,
			fillBlankString);
	}

	/**
	 * Renders a <code>Panel</code> defined in <code>TestPanelSource</code>. The usage is similar to
	 * {@link #startPage(ITestPageSource)}. Please note that testing <code>Panel</code> must use the
	 * supplied <code>panelId<code> as a <code>Component</code> id.
	 * 
	 * <pre>
	 * tester.startPanel(new TestPanelSource()
	 * {
	 * 	public Panel getTestPanel(String panelId)
	 * 	{
	 * 		MyData mockMyData = new MyData();
	 * 		return new MyPanel(panelId, mockMyData);
	 * 	}
	 * });
	 * </pre>
	 * 
	 * @param testPanelSource
	 *            a <code>Panel</code> factory that creates test <code>Panel</code> instances
	 * @return a rendered <code>Panel</code>
	 */
	public final Panel startPanel(final TestPanelSource testPanelSource)
	{
		return (Panel)startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return new DummyPanelPage(testPanelSource);
			}
		}).get(DummyPanelPage.TEST_PANEL_ID);
	}

	/**
	 * Renders a <code>Panel</code> from a <code>Panel(String id)</code> constructor.
	 * 
	 * @param <C>
	 * 
	 * @param panelClass
	 *            a test <code>Panel</code> class with <code>Panel(String id)</code> constructor
	 * @return a rendered <code>Panel</code>
	 */
	public final <C extends Panel> Panel startPanel(final Class<C> panelClass)
	{
		return (Panel)startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return new DummyPanelPage(new TestPanelSource()
				{
					private static final long serialVersionUID = 1L;

					public Panel getTestPanel(String panelId)
					{
						try
						{
							Constructor<? extends Panel> c = panelClass.getConstructor(String.class);
							return c.newInstance(panelId);
						}
						catch (Exception e)
						{
							throw convertoUnexpect(e);
						}
					}
				});
			}
		}).get(DummyPanelPage.TEST_PANEL_ID);
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
	 */
	public void startComponent(Component component)
	{
		if (component instanceof FormComponent)
		{
			((FormComponent<?>)component).processInput();
		}
		component.beforeRender();
	}

	/**
	 * Throw "standard" WicketRuntimeException
	 * 
	 * @param e
	 * @return RuntimeException
	 */
	private RuntimeException convertoUnexpect(Exception e)
	{
		return new WicketRuntimeException("tester: unexpected", e);
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
		final Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			fail("path: '" + path + "' does not exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
			return component;
		}
		if (component.isVisibleInHierarchy())
		{
			return component;
		}
		return null;
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
	public Result isVisible(String path)
	{
		Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			fail("path: '" + path + "' does no exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
		}

		return isTrue("component '" + path + "' is not visible", component.isVisibleInHierarchy());
	}

	/**
	 * assert component invisible.
	 * 
	 * @param path
	 *            path to component
	 * @return a <code>Result</code>
	 */
	public Result isInvisible(String path)
	{
		return isNull("component '" + path + "' is visible", getComponentFromLastRenderedPage(path));
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
			getLastResponseAsString().toString().matches("(?s).*" + pattern + ".*"));
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

			executeBehavior(WicketTesterHelper.findAjaxEventBehavior(linkComponent, "onclick"));
		}
		// AjaxFallbackLinks is processed like an AjaxLink if isAjax is true
		// If it's not handling of the linkComponent is passed through to the
		// Link.
		else if (linkComponent instanceof AjaxFallbackLink && isAjax)
		{
			executeBehavior(WicketTesterHelper.findAjaxEventBehavior(linkComponent, "onclick"));
		}
		// if the link is an AjaxSubmitLink, we need to find the form
		// from it using reflection so we know what to submit.
		else if (linkComponent instanceof AjaxSubmitLink)
		{
			// If it's not ajax we fail
			if (isAjax == false)
			{
				fail("Link " + path + "is an AjaxSubmitLink and " +
					"will not be invoked when AJAX (javascript) is disabled.");
			}

			AjaxSubmitLink link = (AjaxSubmitLink)linkComponent;
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
					BookmarkablePageLink.class.getDeclaredField("parameters");
					Method getParametersMethod = BookmarkablePageLink.class.getDeclaredMethod(
						"getPageParameters", (Class<?>[])null);
					getParametersMethod.setAccessible(true);

					PageParameters parameters = (PageParameters)getParametersMethod.invoke(
						bookmarkablePageLink, (Object[])null);

					startPage(bookmarkablePageLink.getPageClass(), parameters);
					return;
				}
				catch (Exception e)
				{
					fail("Internal error in WicketTester. "
						+ "Please report this in Wickets Issue Tracker.");
				}

			}

			executeListener(link, ILinkListener.INTERFACE);
		}
		else
		{
			fail("Link " + path + " is not a Link, AjaxLink, AjaxFallbackLink or AjaxSubmitLink");
		}
	}

	public void submitForm(Form form)
	{
		submitForm(form.getPageRelativePath());
	}

	/**
	 * Submits the <code>Form</code> in the last rendered <code>Page</code>.
	 * 
	 * @param path
	 *            path to <code>Form</code> component
	 */
	public void submitForm(String path)
	{
		Form<?> form = (Form<?>)getComponentFromLastRenderedPage(path);
		Url url = Url.parse(form.urlFor(IFormSubmitListener.INTERFACE).toString(),
			Charset.forName(request.getCharacterEncoding()));

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
	 */
	private void transform(Url url)
	{
		while (url.getSegments().size() > 0 && url.getSegments().get(0).equals(".."))
		{
			url.getSegments().remove(0);
		}
	}


	/**
	 * Asserts the last rendered <code>Page</code> class.
	 * 
	 * FIXME explain why the code is so complicated to compare two classes, or simplify
	 * 
	 * @param <C>
	 * 
	 * @param expectedRenderedPageClass
	 *            expected class of last rendered page
	 * @return a <code>Result</code>
	 */
	public <C extends Page> Result isRenderedPage(Class<C> expectedRenderedPageClass)
	{
		Page page = getLastRenderedPage();
		if (page == null)
		{
			return Result.fail("page was null");
		}
		if (!page.getClass().isAssignableFrom(expectedRenderedPageClass))
		{
			return isEqual(Classes.simpleName(expectedRenderedPageClass),
				Classes.simpleName(page.getClass()));
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
		return isTrue("expect no error message, but contains\n" +
			WicketTesterHelper.asLined(messages), messages.isEmpty());
	}

	/**
	 * Asserts no info-level feedback messages.
	 * 
	 * @return a <code>Result</code>
	 */
	public Result hasNoInfoMessage()
	{
		List<Serializable> messages = getMessages(FeedbackMessage.INFO);
		return isTrue("expect no info message, but contains\n" +
			WicketTesterHelper.asLined(messages), messages.isEmpty());
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
		FeedbackMessages feedbackMessages = Session.get().getFeedbackMessages();
		List<FeedbackMessage> allMessages = feedbackMessages.messages(new IFeedbackMessageFilter()
		{
			private static final long serialVersionUID = 1L;

			public boolean accept(FeedbackMessage message)
			{
				return message.getLevel() == level;
			}
		});
		List<Serializable> actualMessages = new ArrayList<Serializable>();
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
	 * {@link AjaxRequestTarget#addComponent(Component)}. This method actually tests that a
	 * <code>Component</code> is on the Ajax response sent back to the client.
	 * <p>
	 * PLEASE NOTE! This method doesn't actually insert the <code>Component</code> in the client DOM
	 * tree, using Javascript. But it shouldn't be needed because you have to trust that the Wicket
	 * Ajax Javascript just works.
	 * 
	 * @param component
	 *            the <code>Component</code> to test
	 * @return a <code>Result</code>
	 */
	public Result isComponentOnAjaxResponse(Component component)
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
	public void executeAjaxEvent(String componentPath, String event)
	{
		Component component = getComponentFromLastRenderedPage(componentPath);
		executeAjaxEvent(component, event);
	}

	/**
	 * Simulates the firing of all ajax timer behaviors on the page
	 * 
	 * @param wt
	 * @param container
	 */
	public void executeAllTimerBehaviors(MarkupContainer container)
	{
		container.visitChildren(MarkupContainer.class, new IVisitor<MarkupContainer>()
		{
			public Object component(MarkupContainer component)
			{
				// get the AbstractAjaxBehaviour which is responsible for
				// getting the contents of the lazy panel
				List<IBehavior> behaviors = BehaviorsUtil.getBehaviors(component,
					AbstractAjaxTimerBehavior.class);
				for (IBehavior b : behaviors)
				{
					if (b instanceof AbstractAjaxTimerBehavior)
					{
						log.debug("Triggering AjaxSelfUpdatingTimerBehavior: " +
							component.getClassRelativePath());
						AbstractAjaxTimerBehavior timer = (AbstractAjaxTimerBehavior)b;
						if (!timer.isStopped())
						{
							executeBehavior(timer);
						}
					}
				}
				return CONTINUE_TRAVERSAL;
			}
		});
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
	 * tree, using Javascript.
	 * 
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
		String failMessage = "Can't execute event on a component which is null.";
		notNull(failMessage, component);

		failMessage = "event must not be null";
		notNull(failMessage, event);

		if (component.isVisibleInHierarchy() == false)
		{
			fail("The component is currently not visible in the hierarchy and thus you can not fire events on it." +
				" Component: " + component + "; Event: " + event);
		}

		executeBehavior(WicketTesterHelper.findAjaxEventBehavior(component, event));
	}

	/**
	 * 
	 * @return WebRequestCycle
	 */
// protected WebRequestCycle resolveRequestCycle()
// {
// // initialize the request only if needed to allow the user to pass
// // request parameters, see WICKET-254
// WebRequestCycle requestCycle;
// if (RequestCycle.get() == null)
// {
// requestCycle = setupRequestAndResponse();
// }
// else
// {
// requestCycle = (WebRequestCycle)RequestCycle.get();
//
// // If a ajax request is requested but the existing is not, than we still need to create
// // a new one
// if ((requestCycle.getWebRequest().isAjax() == false) && (isCreateAjaxRequest() == true))
// {
// setParametersForNextRequest(requestCycle.getWebRequest().getParameterMap());
// requestCycle = setupRequestAndResponse();
// }
// }
// return requestCycle;
// }
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
	 * @see BaseWicketTester#getTagByWicketId(String)
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

		String failMessage = "No form attached to the submitlink.";
		notNull(failMessage, form);

		Url url = Url.parse(behavior.getCallbackUrl().toString(),
			Charset.forName(request.getCharacterEncoding()));
		transform(url);
		request.addHeader("Wicket-Ajax-BaseURL", url.toString());
		request.addHeader("Wicket-Ajax", "Wicket-Ajax");
		request.setUrl(url);
		processRequest(request, null);
	}

	/**
	 * Retrieves the content type from the response header.
	 * 
	 * @return the content type from the response header
	 */
	public String getContentTypeFromResponseHeader()
	{
		String contentType = getLastResponse().getContentType();
		if (contentType == null)
		{
			throw new WicketRuntimeException("No Content-Type header found");
		}
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
		if (contentLength == null)
		{
			throw new WicketRuntimeException("No Content-Length header found");
		}
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
		ServletWebRequest req = createServletWebRequest();
		requestCycle.setRequest(req);
		requestCycle.getUrlRenderer().setBaseUrl(req.getUrl());

	}

	private Result isTrue(String message, boolean condition)
	{
		if (condition)
		{
			return Result.pass();
		}
		return Result.fail(message);
	}

	private Result isEqual(Object expected, Object actual)
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

	private void notNull(String message, Object object)
	{
		if (object == null)
		{
			fail(message);
		}
	}

	private Result isNull(String message, Object object)
	{
		if (object != null)
		{
			return Result.fail(message);
		}
		return Result.pass();
	}

	private void fail(String message)
	{
		throw new WicketRuntimeException(message);
	}

	public RequestCycle getRequestCycle()
	{
		return requestCycle;
	}

	public MockHttpServletResponse getResponse()
	{
		return response;
	}

	public MockHttpServletRequest getLastRequest()
	{
		return lastRequest;
	}


	public boolean isExposeExceptions()
	{
		return exposeExceptions;
	}


	public void setExposeExceptions(boolean exposeExceptions)
	{
		this.exposeExceptions = exposeExceptions;
	}

	public void executeUrl(String _url)
	{
		Url url = Url.parse(_url, Charset.forName(request.getCharacterEncoding()));
		transform(url);
		getRequest().setUrl(url);
		processRequest();
	}


	private class LastPageRecordingPageRendererProvider implements IPageRendererProvider
	{
		private final IPageRendererProvider delegate;

		public LastPageRecordingPageRendererProvider(IPageRendererProvider delegate)
		{
			this.delegate = delegate;
		}

		public PageRenderer get(RenderPageRequestHandler handler)
		{
			lastRenderedPage = (Page)handler.getPageProvider().getPageInstance();
			return delegate.get(handler);
		}
	}

	private class TestExceptionMapper implements IExceptionMapper
	{
		private final IExceptionMapper delegate;

		public TestExceptionMapper(IExceptionMapper delegate)
		{
			this.delegate = delegate;
		}

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

	private class TestRequestCycleProvider implements IRequestCycleProvider
	{
		private final IRequestCycleProvider delegate;

		public TestRequestCycleProvider(IRequestCycleProvider delegate)
		{
			this.delegate = delegate;
		}


		public RequestCycle get(RequestCycleContext context)
		{
			context.setRequestMapper(new TestRequestMapper(context.getRequestMapper()));
			forcedHandler = null;
			context.setExceptionMapper(new TestExceptionMapper(context.getExceptionMapper()));
			return delegate.get(context);
		}

	}

	private class TestRequestMapper implements IRequestMapper
	{
		private final IRequestMapper delegate;

		public TestRequestMapper(IRequestMapper delegate)
		{
			this.delegate = delegate;
		}

		public int getCompatibilityScore(Request request)
		{
			return delegate.getCompatibilityScore(request);
		}

		public Url mapHandler(IRequestHandler requestHandler)
		{
			return delegate.mapHandler(requestHandler);
		}

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

	private class TestSessionStore extends MockSessionStore
	{
		@Override
		public void invalidate(Request request)
		{
			super.invalidate(request);
			createNewSession();
		}
	}
	private class TestSessionStoreProvider implements IProvider<ISessionStore>
	{
		public ISessionStore get()
		{
			return new TestSessionStore();
		}
	}

	private class TestPageManagerProvider implements IPageManagerProvider
	{

		public IPageManager get(IPageManagerContext context)
		{
			return new MockPageManager(context);
		}

	}

	private class TestFilterConfig implements FilterConfig
	{
		private final Map<String, String> initParameters = new HashMap<String, String>();

		public TestFilterConfig()
		{
			initParameters.put(WicketFilter.FILTER_MAPPING_PARAM, "/servlet/*");
		}

		public String getFilterName()
		{
			return getClass().getName();
		}

		public ServletContext getServletContext()
		{
			return servletContext;
		}

		public String getInitParameter(String s)
		{
			return initParameters.get(s);
		}

		public Enumeration<String> getInitParameterNames()
		{
			throw new UnsupportedOperationException("Not implemented");
		}
	}
}
