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

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.pages.ExceptionErrorPage;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IPageRequestTarget;
import org.apache.wicket.session.DefaultPageFactory;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.util.file.WebApplicationPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	private static final Logger log = LoggerFactory.getLogger(MockWebApplication.class);

	/** The last rendered page. */
	private Page lastRenderedPage;

	/** The previously rendered page */
	private Page previousRenderedPage;

	/** Mock http servlet request. */
	private final MockHttpServletRequest servletRequest;

	/** Mock http servlet response. */
	private final MockHttpServletResponse servletResponse;

	/** Mock http servlet session. */
	private final MockHttpSession servletSession;

	/** Request. */
	private WebRequest wicketRequest;

	/** Parameters to be set on the next request. */
	private Map parametersForNextRequest = new HashMap();

	/** Response. */
	private WebResponse wicketResponse;

	/** Session. */
	private WebSession wicketSession;

	/** The tester object */
	private final WebApplication application;

	private final ServletContext context;

	private final WicketFilter filter;

	/**
	 * Create the mock http tester that can be used for testing.
	 * 
	 * @param application
	 *            The wicket application object
	 * @param path
	 *            The absolute path on disk to the web tester contents (e.g. war
	 *            root) - may be null
	 * @see org.apache.wicket.protocol.http.MockServletContext
	 */
	public MockWebApplication(final WebApplication application, final String path)
	{
		this.application = application;

		this.context = newServletContext(path);

		filter = new WicketFilter()
		{
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
					if (name.equals(WicketFilter.FILTER_MAPPING_PARAM))
					{
						return WicketFilter.SERVLET_PATH_HOLDER;
						// return "/" + MockWebApplication.this.getName() +
						// "/*";
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

		// Construct mock session, request and response
		this.servletSession = new MockHttpSession(this.context);
		this.servletRequest = new MockHttpServletRequest(this.application, this.servletSession,
				this.context);
		this.servletResponse = new MockHttpServletResponse();

		// Construct request and response using factories
		this.wicketRequest = this.application.newWebRequest(this.servletRequest);
		this.wicketResponse = this.application.newWebResponse(this.servletResponse);

		// Create request cycle
		createRequestCycle();

		this.application.getRequestCycleSettings().setRenderStrategy(
				IRequestCycleSettings.ONE_PASS_RENDER);
		this.application.getResourceSettings().setResourceFinder(new WebApplicationPath(context));
		this.application.getPageSettings().setAutomaticMultiWindowSupport(false);

		// Since the purpose of MockWebApplication is singlethreaded
		// programmatic testing it doesn't make much sense to have a
		// modification watcher thread started to watch for changes in the
		// markup.
		// Disabling this also helps test suites with many test cases
		// (problems has been noticed with >~300 test cases). The problem
		// is that even if the wicket tester is GC'ed the modification
		// watcher still runs, taking up file handles and memory, leading
		// to "Too many files opened" or a regular OutOfMemoryException
		this.application.getResourceSettings().setResourcePollFrequency(null);
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
		final WebRequestCycle cycle = createRequestCycle();
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
	public void processRequestCycle(final Class pageClass)
	{
		setupRequestAndResponse();
		final WebRequestCycle cycle = createRequestCycle();
		try
		{
			cycle.request(new BookmarkablePageRequestTarget(pageClass));
		}
		finally
		{
			cycle.getResponse().close();
		}
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
		try
		{
			cycle.request();
		}
		finally
		{
			cycle.getResponse().close();
		}
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
		Page newLastRenderedPage = cycle.getResponsePage();
		if (newLastRenderedPage == null)
		{
			Class responseClass = cycle.getResponsePageClass();
			if (responseClass != null)
			{
				Session.set(cycle.getSession());
				IRequestTarget target = cycle.getRequestTarget();
				if (target instanceof IPageRequestTarget)
				{
					newLastRenderedPage = ((IPageRequestTarget)target).getPage();
				}
				else if (target instanceof IBookmarkablePageRequestTarget)
				{
					// create a new request cycle for the newPage call
					createRequestCycle();
					IBookmarkablePageRequestTarget pageClassRequestTarget = (IBookmarkablePageRequestTarget)target;
					Class pageClass = pageClassRequestTarget.getPageClass();
					PageParameters parameters = pageClassRequestTarget.getPageParameters();
					if (parameters == null || parameters.size() == 0)
					{
						newLastRenderedPage = new DefaultPageFactory().newPage(pageClass);
					}
					else
					{
						newLastRenderedPage = new DefaultPageFactory().newPage(pageClass,
								parameters);
					}
				}
			}
		}

		if (newLastRenderedPage == null)
		{
			newLastRenderedPage = this.lastRenderedPage;
		}

		return newLastRenderedPage;
	}

	/**
	 * Create and process the request cycle using the current request and
	 * response information.
	 * 
	 * @return A new and initialized WebRequestCyle
	 */
	public WebRequestCycle createRequestCycle()
	{
		// Create a web request cycle using factory

		final WebRequestCycle cycle = (WebRequestCycle)application.newRequestCycle(wicketRequest,
				wicketResponse);

		// Construct session
		this.wicketSession = (WebSession)Session.findOrCreate();

		// Set request cycle so it won't detach automatically and clear messages
		// we want to check
		cycle.setAutomaticallyClearFeedbackMessages(false);
		return cycle;
	}

	/**
	 * Reset the request and the response back to a starting state and recreate
	 * the necessary wicket request, response and session objects. The request
	 * and response objects can be accessed and initialised at this point.
	 */
	public WebRequestCycle setupRequestAndResponse()
	{
		servletRequest.initialize();
		servletResponse.initialize();
		servletRequest.setParameters(parametersForNextRequest);
		parametersForNextRequest.clear();
		this.wicketRequest = this.application.newWebRequest(servletRequest);
		this.wicketResponse = this.application.newWebResponse(servletResponse);
		WebRequestCycle requestCycle = createRequestCycle();
		this.application.getSessionStore().bind(wicketRequest, wicketSession);
		wicketResponse.setAjax(wicketRequest.isAjax());
		return requestCycle;
	}

	/**
	 * Gets the parameters to be set on the next request.
	 * 
	 * @return the parameters to be set on the next request
	 */
	public Map getParametersForNextRequest()
	{
		return parametersForNextRequest;
	}

	/**
	 * Sets the parameters to be set on the next request.
	 * 
	 * @param parametersForNextRequest
	 *            the parameters to be set on the next request
	 */
	public void setParametersForNextRequest(Map parametersForNextRequest)
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
		if (dir != null && dir.isDirectory())
		{
			File[] files = dir.listFiles();
			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					File element = files[i];
					if (element.isDirectory())
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
