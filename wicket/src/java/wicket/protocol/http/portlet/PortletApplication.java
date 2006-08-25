/*
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
package wicket.protocol.http.portlet;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpSession;

import wicket.Application;
import wicket.IRequestCycleFactory;
import wicket.ISessionFactory;
import wicket.Request;
import wicket.RequestCycle;
import wicket.Response;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.markup.resolver.AutoLinkResolver;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebResponse;
import wicket.protocol.http.WebSession;
import wicket.protocol.http.portlet.pages.AccessDeniedPortletPage;
import wicket.protocol.http.portlet.pages.InternalErrorPortletPage;
import wicket.protocol.http.portlet.pages.PageExpiredPortletPage;
import wicket.request.IRequestCycleProcessor;
import wicket.session.ISessionStore;

/**
 * 
 * A portlet application is a subclass of Application which associates with an
 * instance of WicketPortlet to serve pages as portlets. This class is intended
 * to be subclassed by framework clients to define a web application.
 * <p>
 * Application settings are given defaults by the Portletapplication()
 * constructor and internalInit method, such as error page classes appropriate
 * for portlets. PortletApplication subclasses can override these values and/or
 * modify other application settings by overriding the init() method and then by
 * calling getXXXSettings() to retrieve an interface to a mutable Settings
 * object. Do not do this in the constructor itself because the defaults will
 * then override your settings.
 * 
 * @see WicketPortlet
 * @see wicket.settings.IApplicationSettings
 * @see wicket.settings.IDebugSettings
 * @see wicket.settings.IExceptionSettings
 * @see wicket.settings.IMarkupSettings
 * @see wicket.settings.IPageSettings
 * @see wicket.settings.IRequestCycleSettings
 * @see wicket.settings.IResourceSettings
 * @see wicket.settings.ISecuritySettings
 * @see wicket.settings.ISessionSettings
 * 
 * 
 * @author Janne Hietam&auml;ki
 * 
 */
public abstract class PortletApplication extends Application implements ISessionFactory
{
	/**
	 * The cached application key. Will be set in
	 * {@link #setWicketPortlet(WicketPortlet)} based on the portlet context.
	 */
	private String applicationKey;

	/** the default request cycle processor implementation for render requests */
	private IRequestCycleProcessor requestCycleProcessor;

	/** the default request cycle processor implementation. for action requests */
	private IRequestCycleProcessor actionRequestCycleProcessor;


	/**
	 * the prefix for storing variables in the actual session (typically
	 * {@link WicketPortletSession} for this application instance.
	 */
	private String sessionAttributePrefix;

	/** Session factory for this web application */
	private ISessionFactory sessionFactory = this;

	WicketPortlet portlet;

	/**
	 * @param portlet
	 */
	public void setWicketPortlet(WicketPortlet portlet)
	{
		if (this.portlet == null)
		{
			this.portlet = portlet;
			this.applicationKey = portlet.getPortletName();
		}
		else
		{
			throw new IllegalStateException("WicketPortlet cannot be changed once it is set");
		}
	}

	/**
	 * @param request
	 * @return PortletSession
	 */
	public WicketPortletSession getSession(final WicketPortletRequest request)
	{
		ISessionStore sessionStore = getSessionStore();
		Session session = sessionStore.lookup(request);

		if (session == null)
		{
			// Create session using session factory
			session = getSessionFactory().newSession(request);

			// Set the client Locale for this session
			session.setLocale(request.getLocale());

			// Bind the session to the session store
			sessionStore.bind(request, session);
		}

		WicketPortletSession webSession;
		if (session instanceof WicketPortletSession)
		{
			webSession = (WicketPortletSession)session;
		}
		else
		{
			throw new WicketRuntimeException(
					"Session created by a PortletApplication session factory "
							+ "must be a subclass of PortletSession");
		}

		// Set session attribute name and attach/reattach portlet session
		webSession.initForRequest();

		return webSession;
	}

	/**
	 * @param res
	 * @return new WicketPortletResponse
	 * 
	 */
	public WicketPortletResponse newPortletResponse(final PortletResponse res)
	{
		return new WicketPortletResponse(res);
	}

	/**
	 * @param req
	 * @return new WicketPortletRequest
	 */
	public WicketPortletRequest newPortletRequest(PortletRequest req)
	{
		return new WicketPortletRequest(req);
	}

	/*
	 * @see wicket.Application#getApplicationKey()
	 */
	public String getApplicationKey()
	{

		if (applicationKey == null)
		{
			throw new IllegalStateException("the application key does not seem to"
					+ " be set properly or this method is called before WicketPortlet is"
					+ " set, which leads to the wrong behavior");
		}
		return applicationKey;
	}

	protected ISessionFactory getSessionFactory()
	{
		return this.sessionFactory;
	}

	protected ISessionStore newSessionStore()
	{
		return new PortletSessionStore();
	}


	/**
	 * Create new WicketPortletSession object
	 * 
	 * @return session object
	 * @see wicket.ISessionFactory#newSession()
	 */
	public Session newSession(Request request)
	{
		return new WicketPortletSession(this);
	}

	/**
	 * Initialize the portlet
	 */
	public void initPortlet()
	{
		internalInit();
		init();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Internal intialization. First determine the deployment mode. First check
	 * the system property -Dwicket.configuration. If it does not exist check
	 * the portlet init parameter (
	 * <code>&lt;init-param&gt&lt;param-name&gt;configuration&lt;/param-name&gt;</code>).
	 * If not found check the portlet context init paramer
	 * <code>&lt;context-param&gt&lt;param-name6gt;configuration&lt;/param-name&gt;</code>).
	 * If the parameter is "development" (which is default), settings
	 * appropriate for development are set. If it's "deployment" , deployment
	 * settings are used. If development is specified and a "sourceFolder" init
	 * parameter is also set, then resources in that folder will be polled for
	 * changes.
	 */
	protected void internalInit()
	{
		super.internalInit();

		// Set default error pages for portlets
		getApplicationSettings().setPageExpiredErrorPage(PageExpiredPortletPage.class);
		getApplicationSettings().setInternalErrorPage(InternalErrorPortletPage.class);
		getApplicationSettings().setAccessDeniedPage(AccessDeniedPortletPage.class);


		// Add resolver for automatically resolving HTML links
		getPageSettings().addComponentResolver(new AutoLinkResolver());

		// Set resource finder to portlet app path
		getResourceSettings().setResourceFinder(
				new PortletApplicationPath(portlet.getPortletContext()));

		String contextPath = portlet.getInitParameter(Application.CONTEXTPATH);
		if (contextPath != null)
		{
			getApplicationSettings().setContextPath(contextPath);
		}

		// Check if system property -Dwicket.configuration exists
		String configuration = null;
		try
		{
			configuration = System.getProperty("wicket." + Application.CONFIGURATION);
		}
		catch (SecurityException e)
		{
			// ignore; it is not allowed to read system properties
		}

		// If no system parameter check portlet specific <init-param>
		if (configuration == null)
		{
			configuration = portlet.getInitParameter(Application.CONFIGURATION);
		}
		// If no system parameter and not <init-param>, than check
		// <context-param>
		if (configuration == null)
		{
			configuration = portlet.getPortletContext().getInitParameter(Application.CONFIGURATION);
		}

		// Development mode is default if not settings have been found
		if (configuration != null)
		{
			configure(configuration, portlet.getInitParameter("sourceFolder"));
		}
		else
		{
			configure(Application.DEVELOPMENT, portlet.getInitParameter("sourceFolder"));
		}
	}

	/**
	 * 
	 */
	public void destroyPortlet()
	{
		internalDestroy();
	}

	/**
	 * Create a request cycle factory which is used by default by WebSession.
	 * You may provide your own default factory by subclassing WebApplication
	 * and overriding this method or your may subclass WebSession to create a
	 * session specific request cycle factory.
	 * 
	 * @see WebSession#getRequestCycleFactory()
	 * @see IRequestCycleFactory
	 * 
	 * @return Request cycle factory
	 */
	protected IRequestCycleFactory getDefaultRequestCycleFactory()
	{
		return new IRequestCycleFactory()
		{
			private static final long serialVersionUID = 1L;

			public RequestCycle newRequestCycle(Session session, Request request, Response response)
			{
				if (request instanceof WicketPortletRequest)
				{

					WicketPortletRequest req = (WicketPortletRequest)request;
					WicketPortletResponse res = (WicketPortletResponse)response;
					PortletRequest preq = req.getPortletRequest();
					PortletResponse pres = res.getPortletResponse();
					if (preq instanceof ActionRequest)
					{
						return new PortletActionRequestCycle((WicketPortletSession)session,
								(WicketPortletRequest)request, (WicketPortletResponse)response);

					}
					else
					{
						return new PortletRenderRequestCycle((WicketPortletSession)session,
								(WicketPortletRequest)request, (WicketPortletResponse)response);

					}
				}
				return new WebRequestCycle((WebSession)session, (WebRequest)request,
						(WebResponse)response);
			}
		};
	}


	/**
	 * May be replaced by subclasses which whishes to uses there own
	 * implementation of IRequestCycleProcessor
	 * 
	 * @return IRequestCycleProcessor
	 */
	protected IRequestCycleProcessor newRenderRequestCycleProcessor()
	{
		return new PortletRenderRequestCycleProcessor();
	}

	/**
	 * May be replaced by subclasses which whishes to uses there own
	 * implementation of IRequestCycleProcessor
	 * 
	 * @return IRequestCycleProcessor
	 */
	protected IRequestCycleProcessor newActionRequestCycleProcessor()
	{
		return new PortletActionRequestCycleProcessor();
	}


	/**
	 * @return the request cycle processor
	 */
	public IRequestCycleProcessor getActionRequestCycleProcessor()
	{
		if (actionRequestCycleProcessor == null)
		{
			actionRequestCycleProcessor = newActionRequestCycleProcessor();
		}
		return actionRequestCycleProcessor;
	}

	/**
	 * Gets the default request cycle processor (with lazy initialization). This
	 * is the {@link IRequestCycleProcessor} that will be used by
	 * {@link RequestCycle}s when custom implementations of the request cycle
	 * do not provide their own customized versions.
	 * 
	 * @return the default request cycle processor
	 */
	protected final IRequestCycleProcessor getRenderRequestCycleProcessor()
	{
		if (requestCycleProcessor == null)
		{
			requestCycleProcessor = newRenderRequestCycleProcessor();
		}
		return requestCycleProcessor;
	}

	/**
	 * Gets the prefix for storing variables in the actual session (typically
	 * {@link HttpSession} for this application instance.
	 * 
	 * @param request
	 *            the request
	 * 
	 * @return the prefix for storing variables in the actual session
	 */
	public final String getSessionAttributePrefix(final WicketPortletRequest request)
	{
		if (sessionAttributePrefix == null)
		{
			String contextPath = getApplicationKey();
			if (contextPath == null)
			{
				throw new WicketRuntimeException("unable to retrieve portlet context path");
			}

			// WARNING: this has to match the string which is returned from the
			// WebApplication
			// getServletPath is not available in portlets, so we have to use
			// empty servlet path
			// and a empty servlet path string here.
			sessionAttributePrefix = "wicket::";
		}
		// Namespacing for session attributes is provided by
		// adding the portlet path
		return sessionAttributePrefix;
	}

	/**
	 * Gets portlet.
	 * 
	 * @return portlet
	 */
	public final WicketPortlet getWicketPortlet()
	{
		return portlet;
	}
}