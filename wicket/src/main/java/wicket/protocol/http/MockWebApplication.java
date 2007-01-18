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
package wicket.protocol.http;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Application;
import wicket.Component;
import wicket.IRequestCycleFactory;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.Session;
import wicket.markup.html.pages.ExceptionErrorPage;
import wicket.protocol.http.servlet.ServletWebRequest;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.request.target.component.IPageRequestTarget;
import wicket.session.DefaultPageFactory;
import wicket.settings.IRequestCycleSettings.RenderStrategy;
import wicket.util.file.WebApplicationPath;

/**
 * This class provides a mock implementation of a Wicket HTTP based tester that
 * can be used for testing. It emulates all of the functionality of an
 * HttpServlet in a controlled, single-threaded environment. It is supported
 * with mock objects for WebSession, HttpServletRequest, HttpServletResponse and
 * ServletContext.
 * <p>
 * In its most basic usage you can just create a new MockWebApplication and
 * provide your Wicket Application object. This should be sufficient to allow
 * you to construct components and pages and so on for testing. To use certain
 * features such as localization you must also call setupRequestAndResponse().
 * <p>
 * The tester takes an optional path attribute that defines a directory on the
 * disk which will correspond to the root of the WAR bundle. This can then be
 * used for locating non-tester resources.
 * <p>
 * To actually test the processing of a particular page or component you can
 * also call processRequestCycle() to do all the normal work of a Wicket
 * request.
 * <p>
 * Between calling setupRequestAndResponse() and processRequestCycle() you can
 * get hold of any of the objects for initialisation. The servlet request object
 * has some handy convenience methods for initialising the request to invoke
 * certain types of pages and components.
 * <p>
 * After completion of processRequestCycle() you will probably just be testing
 * component states. However, you also have full access to the response document
 * (or binary data) and result codes via the servlet response object.
 * <p>
 * IMPORTANT NOTES
 * <ul>
 * <li>This harness is SINGLE THREADED - there is only one global session. For
 * multi-threaded testing you must do integration testing with a full tester
 * server.
 * </ul>
 * 
 * @author Chris Turner
 */
public class MockWebApplication
{
	/** Logging */
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(MockWebApplication.class);

	/** The last rendered page. */
	private Page lastRenderedPage;

	/** The previously rendered page */
	private Page previousRenderedPage;

	/** Mock http servlet request. */
	private MockHttpServletRequest servletRequest;

	/** Mock http servlet response. */
	private MockHttpServletResponse servletResponse;

	/** Mock http servlet session. */
	private MockHttpSession servletSession;

	/** Request. */
	private WebRequest wicketRequest;

	/** Parameters to be set on the next request. */
	private Map<String, Object> parametersForNextRequest = new HashMap<String, Object>();

	/** Response. */
	private WebResponse wicketResponse;

	/** Session. */
	private WebSession wicketSession;

	/** Request cycle factory. */
	private IRequestCycleFactory requestCycleFactory;

	/** The homepage */
	private Class<? extends Page> homePage;

	/** The tester object */
	private final WebApplication application;

	private ServletContext context;

	private WicketFilter filter;

	/**
	 * Create the mock http tester that can be used for testing.
	 * 
	 * @param application
	 *            The wicket application object
	 * @param path
	 *            The absolute path on disk to the web tester contents (e.g. war
	 *            root) - may be null
	 * @see wicket.protocol.http.MockServletContext
	 */
	public MockWebApplication(final WebApplication application, final String path)
	{
		this.application = application;

		context = newServletContext(path);
		filter = new WicketFilter()
		{
			@Override
			protected IWebApplicationFactory getApplicationFactory()
			{
				return new IWebApplicationFactory()
				{
					public WebApplication createApplication(WicketFilter filter)
					{
						return application;
					};
				};
			}
		};

		try
		{
			filter.init(new FilterConfig()
			{

				public ServletContext getServletContext()
				{
					return context;
				}

				public Enumeration getInitParameterNames()
				{
					return null;
				}

				public String getInitParameter(String name)
				{
					if (name.equals(WicketFilter.FILTER_PATH_PARAM))
					{
						return application.getName();
					}
					return null;
				}

				public String getFilterName()
				{
					return "WicketMockServlet";
				}
			});
		}
		catch (ServletException e)
		{
			throw new RuntimeException(e);
		}

		Application.set(this.application);

		this.servletSession = new MockHttpSession(context);
		this.servletRequest = new MockHttpServletRequest(this.application, servletSession, context);
		this.servletResponse = new MockHttpServletResponse();
		this.wicketRequest = this.application.newWebRequest(servletRequest);
		this.wicketSession = this.application.getSession(wicketRequest);
		this.requestCycleFactory = this.wicketSession.getRequestCycleFactory();

		// -----------------------------------
		// Copied from WicketFilter

		// Call internal init method of web application for default
		// initialisation
		this.application.internalInit();

		// Call init method of web application
		this.application.init();

		// We initialize components here rather than in the constructor or
		// in the internal init, because in the init method class aliases
		// can be added, that would be used in installing resources in the
		// component.
		this.application.initializeComponents();

		// Give the application the option to log that it is started
		this.application.logStarted();
		// -----------------------------------

		// set the default context path
		this.application.getApplicationSettings().setContextPath(context.getServletContextName());

		this.application.getRequestCycleSettings()
				.setRenderStrategy(RenderStrategy.ONE_PASS_RENDER);
		this.application.getResourceSettings().setResourceFinder(new WebApplicationPath(context));
		this.application.getPageSettings().setAutomaticMultiWindowSupport(false);
		this.application.getResourceSettings().setResourcePollFrequency(null);

		this.application.getDebugSettings().setSerializeSessionAttributes(false);

		createRequestCycle();
	}

	/**
	 * Used to create a new mock servlet context.
	 * 
	 * @param path
	 *            The absolute path on disk to the web tester contents (e.g. war
	 *            root) - may be null
	 * @return ServletContext
	 */
	public ServletContext newServletContext(final String path)
	{
		return new MockServletContext(this.application, path);
	}
	
	/**
	 * Gets the application object.
	 * 
	 * @return Wicket application
	 */
	public final WebApplication getApplication()
	{
		return this.application;
	}

	/**
	 * Get the page that was just rendered by the last request cycle processing.
	 * 
	 * @return The last rendered page
	 */
	public Page getLastRenderedPage()
	{
		return lastRenderedPage;
	}

	/**
	 * Get the page that was previously
	 * 
	 * @return The last rendered page
	 */
	public Page getPreviousRenderedPage()
	{
		return previousRenderedPage;
	}

	/**
	 * Get the request object so that we can apply configurations to it.
	 * 
	 * @return The request object
	 */
	public MockHttpServletRequest getServletRequest()
	{
		return servletRequest;
	}

	/**
	 * Get the response object so that we can apply configurations to it.
	 * 
	 * @return The response object
	 */
	public MockHttpServletResponse getServletResponse()
	{
		return servletResponse;
	}

	/**
	 * Get the session object so that we can apply configurations to it.
	 * 
	 * @return The session object
	 */
	public MockHttpSession getServletSession()
	{
		return servletSession;
	}

	/**
	 * Get the wicket request object.
	 * 
	 * @return The wicket request object
	 */
	public WebRequest getWicketRequest()
	{
		return wicketRequest;
	}

	/**
	 * Get the wicket response object.
	 * 
	 * @return The wicket response object
	 */
	public WebResponse getWicketResponse()
	{
		return wicketResponse;
	}

	/**
	 * Get the wicket session.
	 * 
	 * @return The wicket session object
	 */
	public WebSession getWicketSession()
	{
		return wicketSession;
	}

	/**
	 * Initialize a new WebRequestCycle and all its dependent objects
	 * 
	 * @param component
	 */
	public void processRequestCycle(final Component component)
	{
		setupRequestAndResponse();
		WebRequestCycle cycle = createRequestCycle();
		cycle.request(component);

		if (component instanceof Page)
		{
			this.lastRenderedPage = (Page)component;
		}
		postProcessRequestCycle(cycle);
	}

	/**
	 * Initialize a new WebRequestCycle and all its dependent objects
	 * 
	 * @param pageClass
	 */
	public void processRequestCycle(final Class<? extends Page> pageClass)
	{
		setupRequestAndResponse();
		WebRequestCycle cycle = createRequestCycle();
		cycle.request(new BookmarkablePageRequestTarget(pageClass));
		postProcessRequestCycle(cycle);
	}

	/**
	 * Create and process the request cycle using the current request and
	 * response information.
	 */
	public void processRequestCycle()
	{
		processRequestCycle(createRequestCycle());
	}

	/**
	 * Create and process the request cycle using the current request and
	 * response information.
	 * 
	 * @param cycle
	 */
	public void processRequestCycle(WebRequestCycle cycle)
	{
		cycle.request();
		postProcessRequestCycle(cycle);
	}

	/**
	 * 
	 * @param cycle
	 */
	private void postProcessRequestCycle(WebRequestCycle cycle)
	{
		previousRenderedPage = lastRenderedPage;

		// handle redirects which are usually managed by the browser
		// transparently
		final MockHttpServletResponse httpResponse = (MockHttpServletResponse)cycle
				.getWebResponse().getHttpServletResponse();

		if (httpResponse.isRedirect())
		{
			this.lastRenderedPage = generateLastRenderedPage(cycle);

			MockHttpServletRequest newHttpRequest = new MockHttpServletRequest(this.application,
					servletSession, this.application.getServletContext());
			newHttpRequest.setRequestToRedirectString(httpResponse.getRedirectLocation());
			wicketRequest = this.application.newWebRequest(newHttpRequest);
			wicketSession = this.application.getSession(wicketRequest);

			cycle = createRequestCycle();
			cycle.request();
		}
		this.lastRenderedPage = generateLastRenderedPage(cycle);

		Session.set(getWicketSession());

		if (getLastRenderedPage() instanceof ExceptionErrorPage)
		{
			throw (RuntimeException)((ExceptionErrorPage)getLastRenderedPage()).getThrowable();
		}
	}

	/**
	 * 
	 * @param cycle
	 * @return Last page
	 */
	private Page generateLastRenderedPage(WebRequestCycle cycle)
	{
		Page lastRenderedPage = cycle.getResponsePage();
		if (lastRenderedPage == null)
		{
			Class responseClass = cycle.getResponsePageClass();
			if (responseClass != null)
			{
				Session.set(cycle.getSession());
				IRequestTarget target = cycle.getRequestTarget();
				if (target instanceof IPageRequestTarget)
				{
					lastRenderedPage = ((IPageRequestTarget)target).getPage();
				}
				else if (target instanceof IBookmarkablePageRequestTarget)
				{
					// create a new request cycle for the newPage call
					createRequestCycle();
					IBookmarkablePageRequestTarget pageClassRequestTarget = (IBookmarkablePageRequestTarget)target;
					Class<? extends Page> pageClass = pageClassRequestTarget.getPageClass();
					PageParameters parameters = pageClassRequestTarget.getPageParameters();
					if (parameters == null || parameters.size() == 0)
					{
						lastRenderedPage = new DefaultPageFactory().newPage(pageClass);
					}
					else
					{
						lastRenderedPage = new DefaultPageFactory().newPage(pageClass, parameters);
					}
				}
			}
		}

		if (lastRenderedPage == null)
		{
			lastRenderedPage = this.lastRenderedPage;
		}

		return lastRenderedPage;
	}

	/**
	 * Create and process the request cycle using the current request and
	 * response information.
	 * 
	 * @return A new and initialized WebRequestCyle
	 */
	public WebRequestCycle createRequestCycle()
	{
		return (WebRequestCycle) requestCycleFactory.newRequestCycle(wicketSession, wicketRequest, wicketResponse);
	}

	/**
	 * Reset the request and the response back to a starting state and recreate
	 * the necessary wicket request, response and session objects. The request
	 * and response objects can be accessed and initialised at this point.
	 */
	public void setupRequestAndResponse()
	{
		servletRequest.initialize();
		servletResponse.initialize();
		servletRequest.setParameters(parametersForNextRequest);
		parametersForNextRequest.clear();
		wicketRequest = new ServletWebRequest(servletRequest);
		wicketSession = this.application.getSession(wicketRequest);
		this.application.getSessionStore().bind(wicketRequest, wicketSession);
		wicketResponse = new WebResponse(servletResponse);
		wicketResponse.setAjax(wicketRequest.isAjax());
	}

	/**
	 * Gets the parameters to be set on the next request.
	 * 
	 * @return the parameters to be set on the next request
	 */
	public Map<String, Object> getParametersForNextRequest()
	{
		return parametersForNextRequest;
	}

	/**
	 * Sets the parameters to be set on the next request.
	 * 
	 * @param parametersForNextRequest
	 *            the parameters to be set on the next request
	 */
	public void setParametersForNextRequest(Map<String, Object> parametersForNextRequest)
	{
		this.parametersForNextRequest = parametersForNextRequest;
	}
	
	/**
	 * clears this mock application
	 */
	public void destroy()
	{
		filter.destroy();
		File dir = (File)context.getAttribute("javax.servlet.context.tempdir");
		deleteDir(dir);
	}
	
	private void deleteDir(File dir)
	{
		if(dir != null && dir.isDirectory())
		{
			File[] files = dir.listFiles();
			if (files != null)
			{
				for (File element : files)
				{
					if(element.isDirectory())
					{
						deleteDir(element);
					}
					else
					{
						element.delete();
					}
				}
			}
			dir.delete();
		}
	}
}