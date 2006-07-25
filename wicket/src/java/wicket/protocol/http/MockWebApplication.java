/*
 * $Id: MockWebApplication.java 4920 2006-03-14 09:29:09 -0800 (Tue, 14 Mar
 * 2006) joco01 $ $Revision$ $Date: 2006-03-14 09:29:09 -0800 (Tue, 14
 * Mar 2006) $
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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.Session;
import wicket.markup.html.pages.ExceptionErrorPage;
import wicket.protocol.http.servlet.ServletWebRequest;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.request.target.component.IPageRequestTarget;
import wicket.session.DefaultPageFactory;
import wicket.session.ISessionStore;
import wicket.settings.IRequestCycleSettings.RenderStrategy;
import wicket.util.file.WebApplicationPath;

/**
 * This class provides a mock implementation of a Wicket HTTP based application
 * that can be used for testing. It emulates all of the functionality of an
 * HttpServlet in a controlled, single-threaded environment. It is supported
 * with mock objects for WebSession, HttpServletRequest, HttpServletResponse and
 * ServletContext.
 * <p>
 * In its most basic usage you can just create a new MockWebApplication. This
 * should be sufficient to allow you to construct components and pages and so on
 * for testing. To use certain features such as localization you must also call
 * setupRequestAndResponse().
 * <p>
 * The application takes an optional path attribute that defines a directory on
 * the disk which will correspond to the root of the WAR bundle. This can then
 * be used for locating non-application resources.
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
 * multi-threaded testing you must do integration testing with a full
 * application server.
 * </ul>
 * 
 * @author Chris Turner
 */
public class MockWebApplication extends WebApplication
{
	/** Logging */
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(MockWebApplication.class);

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

	/** The homepage */
	private Class<? extends Page> homePage;

	/**
	 * Create the mock http application that can be used for testing.
	 * 
	 * @param path
	 *            The absolute path on disk to the web application contents
	 *            (e.g. war root) - may be null
	 * @see wicket.protocol.http.MockServletContext
	 */
	public MockWebApplication(final String path)
	{
		final MockServletContext context = new MockServletContext(this, null);
		WicketFilter filter = new WicketFilter()
		{
			@Override
			protected IWebApplicationFactory getApplicationFactory()
			{
				return new IWebApplicationFactory()
				{
					public WebApplication createApplication(WicketFilter filter)
					{
						return MockWebApplication.this;
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
					if(name.equals(WicketFilter.FILTER_PATH_PARAM))
					{
						return getName();
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
		Application.set(this);
	}
	
	@Override
	protected void init()
	{
		ServletContext context =  getServletContext();
		servletSession = new MockHttpSession(context);
		servletRequest = new MockHttpServletRequest(this, servletSession, context);
		servletResponse = new MockHttpServletResponse();
		wicketRequest = newWebRequest(servletRequest);
		wicketSession = getSession(wicketRequest);

		// set the default context path
		getApplicationSettings().setContextPath(context.getServletContextName());

		getRequestCycleSettings().setRenderStrategy(RenderStrategy.ONE_PASS_RENDER);
		getResourceSettings().setResourceFinder(new WebApplicationPath(context));
		getPageSettings().setAutomaticMultiWindowSupport(false);
		getResourceSettings().setResourcePollFrequency(null);

		getDebugSettings().setSerializeSessionAttributes(false);
		
		createRequestCycle();
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
		WebRequestCycle cycle = new WebRequestCycle(wicketSession, wicketRequest, wicketResponse);
		cycle.request(component);

		if (component instanceof Page)
		{
			this.lastRenderedPage = (Page) component;
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
		cycle.request();
		postProcessRequestCycle(cycle);
	}
	
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

			MockHttpServletRequest newHttpRequest = new MockHttpServletRequest(this,
					servletSession, getServletContext());
			newHttpRequest.setRequestToRedirectString(httpResponse.getRedirectLocation());
			wicketRequest = newWebRequest(newHttpRequest);
			wicketSession = getSession(wicketRequest);

			cycle = new WebRequestCycle(wicketSession, wicketRequest, wicketResponse);
			cycle.request();
		}
		this.lastRenderedPage = generateLastRenderedPage(cycle);

		Session.set(getWicketSession());

		if (getLastRenderedPage() instanceof ExceptionErrorPage)
		{
			throw (RuntimeException)((ExceptionErrorPage)getLastRenderedPage()).getThrowable();
		}
	}

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
		return new WebRequestCycle(wicketSession, wicketRequest, wicketResponse);
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
		wicketSession = getSession(wicketRequest);
		getSessionStore().bind(wicketRequest, wicketSession);
		wicketResponse = new WebResponse(servletResponse);
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
	 * @see wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return homePage;
	}

	/**
	 * Sets the home page for this mock application
	 * 
	 * @param clazz
	 */
	public void setHomePage(Class<? extends Page> clazz)
	{
		homePage = clazz;
	}

	/**
	 * @see wicket.protocol.http.WebApplication#newSessionStore()
	 */
	@Override
	protected ISessionStore newSessionStore()
	{
		return new HttpSessionStore();
	}
}