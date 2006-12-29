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
package wicket.protocol.http.portlet;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletMode;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Application;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.Session;
import wicket.protocol.http.MockHttpServletRequest;
import wicket.protocol.http.MockHttpServletResponse;
import wicket.protocol.http.MockHttpSession;
import wicket.protocol.http.MockServletContext;
import wicket.protocol.http.portlet.pages.ExceptionErrorPortletPage;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.request.target.component.IPageRequestTarget;
import wicket.session.DefaultPageFactory;
import wicket.util.file.WebApplicationPath;

/**
 * This class provides a mock implementation of a Wicket portlet based
 * tester that can be used for testing. It emulates all of the
 * functionality of an Portlet in a controlled, single-threaded environment. It
 * is supported with mock objects for PortletSession, PortletRequest,
 * PortletResponse and PortletContext.
 * <p>
 * 
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public class MockPortletApplication extends PortletApplication
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(MockPortletApplication.class);

	Map<String, Object> renderParameters = new HashMap<String, Object>();

	ServletContext context = null;

	/** Parameters to be set on the next request. */
	private Map parametersForNextRequest = new HashMap();

	PortletMode portletMode = PortletMode.VIEW;

	WindowState windowState = WindowState.NORMAL;

	String title = "WicketPortletTest";

	String contextPath = "/portlet";

	/** The last rendered page. */
	private PortletPage lastRenderedPage;

	/** The previously rendered page */
	private PortletPage previousRenderedPage;

	/** The homepage */
	private Class<? extends Page> homePage;

	/** Mock http servlet request. */
	private MockPortletRequest portletRequest;

	/** Mock http servlet response. */
	private MockPortletResponse portletResponse;

	private final MockPortletSession portletSession;

	private WicketPortletRequest wicketPortletRequest;

	private WicketPortletResponse wicketPortletResponse;

	private WicketPortletSession wicketPortletSession;

	private MockHttpSession servletSession;

	/**
	 * Construct.
	 * 
	 * @param path
	 */
	public MockPortletApplication(String path)
	{
		Application.set(this);

		context = new MockServletContext(this, path);
		final PortletContext portletContext = new MockPortletContext(context);
		setWicketPortlet(new WicketPortlet()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public PortletContext getPortletContext()
			{
				return portletContext;
			};

			@Override
			public String getPortletName()
			{
				return "MockPortlet";
			}

			@Override
			public String getInitParameter(String key)
			{
				return null;
			}
		});

		this.internalInit();

		getDebugSettings().setSerializeSessionAttributes(false);

		// Call init method of web tester
		this.init();

		// We initialize components here rather than in the constructor or
		// in the internal init, because in the init method class aliases
		// can be added, that would be used in installing resources in the
		// component.
		this.initializeComponents();

		servletSession = new MockHttpSession(context);
		portletSession = new MockPortletSession(servletSession);

		// set the default context path
		getApplicationSettings().setContextPath(context.getServletContextName());

		getResourceSettings().setResourceFinder(new WebApplicationPath(context));
		getPageSettings().setAutomaticMultiWindowSupport(false);
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
	 * @return Application homepage
	 * @see wicket.Application#getHomePage()
	 */

	@Override
	public Class<? extends Page> getHomePage()
	{
		return homePage;
	}

	/**
	 * Sets the home page for this mock tester
	 * 
	 * @param clazz
	 */
	public void setHomePage(Class<? extends Page> clazz)
	{
		homePage = clazz;
	}

	/**
	 * Initialize a new RenderRequest
	 * 
	 */
	public void createRenderRequest()
	{
		MockHttpServletRequest req = new MockHttpServletRequest(null, servletSession, context);
		MockHttpServletResponse res = new MockHttpServletResponse();
		portletRequest = new MockPortletRequest(this, portletSession, req, renderParameters);
		portletResponse = new MockPortletRenderResponse(res);
		portletRequest.initialize();
		portletResponse.initialize();
		portletRequest.setParameters(parametersForNextRequest);
		parametersForNextRequest.clear();
		wicketPortletRequest = new WicketPortletRequest(portletRequest);
		wicketPortletResponse = new WicketPortletResponse(portletResponse);
		wicketPortletSession = getSession(wicketPortletRequest);
	}

	/**
	 * Create new PortletRenderRequestCycle
	 * 
	 * @return PortletRenderRequestCycle
	 */
	public PortletRenderRequestCycle createRenderRequestCycle()
	{
		if (wicketPortletRequest == null)
		{
			throw new IllegalStateException("PortletRequest is not initialized");
		}
		if (!(wicketPortletResponse.getPortletResponse() instanceof RenderResponse))
		{
			throw new IllegalStateException(
					"Unable to initialize PortletRenderRequestCycle, response is not RenderResponse");
		}
		return new PortletRenderRequestCycle(wicketPortletSession, wicketPortletRequest,
				wicketPortletResponse);
	}

	/**
	 * Initialize a new ActionRequest
	 */
	public void createActionRequest()
	{
		renderParameters = new HashMap<String, Object>();
		MockHttpServletRequest req = new MockHttpServletRequest(null, servletSession, context);
		MockHttpServletResponse res = new MockHttpServletResponse();
		portletRequest = new MockPortletRequest(this, portletSession, req, renderParameters);
		portletResponse = new MockPortletActionResponse(res, renderParameters);
		portletRequest.initialize();
		portletResponse.initialize();
		portletRequest.setParameters(parametersForNextRequest);
		parametersForNextRequest.clear();
		wicketPortletRequest = new WicketPortletRequest(portletRequest);
		wicketPortletResponse = new WicketPortletResponse(portletResponse);
		wicketPortletSession = getSession(wicketPortletRequest);
	}


	/**
	 * Create and process the request cycle using the current request and
	 * response information.
	 * 
	 * @return A new and initialized RequestCyle
	 */
	public PortletActionRequestCycle createActionRequestCycle()
	{
		if (wicketPortletRequest == null)
		{
			throw new IllegalStateException("PortletRequest is not initialized");
		}
		if (!(wicketPortletResponse.getPortletResponse() instanceof ActionResponse))
		{
			throw new IllegalStateException(
					"Unable to initialize PortletActionRequestCycle, request is not ActionResponse");
		}
		return new PortletActionRequestCycle(wicketPortletSession, wicketPortletRequest,
				wicketPortletResponse);
	}


	/**
	 * Create and process the request cycle using the current request and
	 * response information.
	 * 
	 * @param cycle
	 */
	public void processActionRequestCycle(PortletActionRequestCycle cycle)
	{
		cycle.request();
	}

	/**
	 * Create and process the request cycle using the current request and
	 * response information.
	 * 
	 * @param cycle
	 */
	public void processRenderRequestCycle(PortletRenderRequestCycle cycle)
	{
		cycle.request();

		previousRenderedPage = lastRenderedPage;

		final MockPortletResponse httpResponse = (MockPortletResponse)cycle.getPortletResponse()
				.getPortletResponse();

		generateLastRenderedPage(cycle);

		Session.set(getWicketPortletSession());

		if (getLastRenderedPage() instanceof ExceptionErrorPortletPage)
		{
			throw (RuntimeException)((ExceptionErrorPortletPage)getLastRenderedPage())
					.getThrowable();
		}
	}


	@SuppressWarnings("unchecked")
	private void generateLastRenderedPage(PortletRequestCycle cycle)
	{
		lastRenderedPage = (PortletPage)cycle.getResponsePage();
		if (lastRenderedPage == null)
		{
			Class responseClass = cycle.getResponsePageClass();
			if (responseClass != null)
			{
				Session.set(cycle.getSession());
				IRequestTarget target = cycle.getRequestTarget();
				if (target instanceof IPageRequestTarget)
				{
					lastRenderedPage = (PortletPage)((IPageRequestTarget)target).getPage();
				}
				else if (target instanceof IBookmarkablePageRequestTarget)
				{
					// create a new request cycle (needed in newPage)
					createRenderRequestCycle();
					IBookmarkablePageRequestTarget pageClassRequestTarget = (IBookmarkablePageRequestTarget)target;
					Class pageClass = pageClassRequestTarget.getPageClass();
					PageParameters parameters = pageClassRequestTarget.getPageParameters();
					if (parameters == null || parameters.size() == 0)
					{
						lastRenderedPage = (PortletPage)new DefaultPageFactory().newPage(pageClass);
					}
					else
					{
						lastRenderedPage = (PortletPage)new DefaultPageFactory().newPage(pageClass,
								parameters);
					}
				}
			}
		}
	}

	/**
	 * Get the page that was just rendered by the last request cycle processing.
	 * 
	 * @return The last rendered page
	 */
	public PortletPage getLastRenderedPage()
	{
		return lastRenderedPage;
	}

	/**
	 * Get the page that was previously
	 * 
	 * @return The last rendered page
	 */
	public PortletPage getPreviousRenderedPage()
	{
		return previousRenderedPage;
	}

	/**
	 * Get the wicket session.
	 * 
	 * @return The wicket session object
	 */
	public WicketPortletSession getWicketPortletSession()
	{
		return wicketPortletSession;
	}


	/**
	 * Get the current PortletRequest
	 * 
	 * @return MockPortletRequest
	 */
	public MockPortletRequest getPortletRequest()
	{
		return portletRequest;
	}

	/**
	 * Get the current PortletResponse
	 * 
	 * @return MockPortletResponse
	 */
	public MockPortletResponse getPortletResponse()
	{
		return portletResponse;
	}
}