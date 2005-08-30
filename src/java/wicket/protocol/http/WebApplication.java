/*
 * $Id$
 * $Revision$ $Date$
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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import wicket.Application;
import wicket.ApplicationSettings;
import wicket.AutoLinkResolver;
import wicket.ISessionFactory;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.markup.html.pages.InternalErrorPage;
import wicket.markup.html.pages.PageExpiredErrorPage;
import wicket.protocol.http.servlet.ServletWebRequest;
import wicket.response.BufferedResponse;
import wicket.util.file.IResourceFinder;
import wicket.util.file.WebApplicationPath;
import wicket.util.time.Duration;
import wicket.util.time.Time;

/**
 * A web application is a subclass of Application which associates with an
 * instance of WicketServlet to serve pages over the HTTP protocol. This class
 * is intended to be subclassed by framework clients to define a web
 * application.
 * <p>
 * Application settings are given defaults by the WebApplication() constructor,
 * such as error page classes appropriate for HTML. WebApplication subclasses
 * can override these values and/or modify other application settings in their
 * respective constructors by calling getSettings() to retrieve a mutable
 * ApplicationSettings object.
 * <p>
 * If you want to use servlet specific configuration, e.g. using init parameters
 * from the {@link javax.servlet.ServletConfig}object, you should override the
 * init() method. For example:
 * 
 * <pre>
 * 
 *       public void init()
 *       {
 *           String webXMLParameter = getWicketServlet()
 *               .getInitParameter(&quot;myWebXMLParameter&quot;);
 *           URL schedulersConfig = getWicketServlet().getServletContext()
 *               .getResource(&quot;/WEB-INF/schedulers.xml&quot;);
 *           ...
 *  
 * </pre>
 * 
 * @see WicketServlet
 * @see wicket.ApplicationSettings
 * @see wicket.ApplicationPages
 * @author Jonathan Locke
 * @author Chris Turner
 */
public abstract class WebApplication extends Application
{
	/** Session factory for this web application */
	private ISessionFactory sessionFactory = new ISessionFactory()
	{
		public Session newSession()
		{
			return new WebSession(WebApplication.this);
		}
	};

	/** The WicketServlet that this application is attached to */
	private WicketServlet wicketServlet;

	/** Map of redirects in progress per session */
	private Map redirectMap = Collections.synchronizedMap(new HashMap());

	/**
	 * Constructor.
	 */
	public WebApplication()
	{
		// Set default error pages for HTML markup
		getPages().setPageExpiredErrorPage(PageExpiredErrorPage.class).setInternalErrorPage(
				InternalErrorPage.class);

		// Add resolver for automatically resolving HTML links
		getComponentResolvers().add(new AutoLinkResolver());
	}

	/**
	 * @return The Wicket servlet for this application
	 */
	public final WicketServlet getWicketServlet()
	{
		if (wicketServlet == null)
		{
			throw new IllegalStateException("wicketServlet is not set yet. Any code in your" +
					" Application object that uses the wicketServlet instance should be put" +
					" in the init() method instead of your constructor");
		}
		return wicketServlet;
	}

	/**
	 * @param sessionFactory
	 *            The session factory to use
	 */
	public final void setSessionFactory(final ISessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @param wicketServlet
	 *            The wicket servlet instance for this application
	 * @throws IllegalStateException
	 *             If an attempt is made to call this method once the wicket
	 *             servlet has been set for the application.
	 */
	public final void setWicketServlet(final WicketServlet wicketServlet)
	{
		if (this.wicketServlet == null)
		{
			this.wicketServlet = wicketServlet;
		}
		else
		{
			throw new IllegalStateException("WicketServlet cannot be changed once it is set");
		}
	}

	/**
	 * @see wicket.Application#getSessionFactory()
	 */
	protected ISessionFactory getSessionFactory()
	{
		return sessionFactory;
	}

	/**
	 * Initialize; if you need the wicket servlet for initialization, e.g.
	 * because you want to read an initParameter from web.xml or you want to
	 * read a resource from the servlet's context path, you can override this
	 * method and provide custom initialization. This method is called right
	 * after this application class is constructed, and the wicket servlet is
	 * set.
	 */
	protected void init()
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Internal intialization. Reads servlet init parameter "configuration". If
	 * the parameter is "development", settings appropriate for development are
	 * set. If it's "deployment", deployment settings are used. If development
	 * configuration is specified and a "sourceFolder" init parameter is also
	 * set, then resources in that folder will be polled for changes.
	 */
	protected void internalInit()
	{
		super.internalInit();
		final WicketServlet servlet = getWicketServlet();
		final String configuration = wicketServlet.getInitParameter("configuration");
		if (configuration != null)
		{
			getSettings().configure(configuration, wicketServlet.getInitParameter("sourceFolder"));
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 */
	protected void internalDestroy()
	{
	}

	/**
	 * Gets a WebSession object from the HttpServletRequest, creating a new one
	 * if it doesn't already exist.
	 * 
	 * @param request
	 *            The http request object
	 * @param create 
	 * 			  Should the session be created if not there.
	 * @return The session object
	 */
	final WebSession getSession(final WebRequest request, boolean create)
	{
		// Get session, creating if it doesn't exist
		final HttpSession httpSession = ((ServletWebRequest)request).getHttpServletRequest().getSession(true);
		if(!create && httpSession == null) return null;

		// Namespacing for session attributes is provided by adding the servlet
		// path
		final String sessionAttributePrefix = "wicket-" + request.getServletPath();

		// The actual attribute for the session is
		// "wicket-<servlet-path>-session"
		final String sessionAttribute = sessionAttributePrefix + "-" + Session.sessionAttributeName;

		// Get Session abstraction from httpSession attribute
		WebSession webSession = (WebSession)httpSession.getAttribute(sessionAttribute);
		if (webSession == null)
		{
			if(!create) return null;
			// Create session using session factory
			final Session session = getSessionFactory().newSession();
			if (session instanceof WebSession)
			{
				webSession = (WebSession)session;
			}
			else
			{
				throw new WicketRuntimeException(
						"Session created by a WebApplication session factory must be a subclass of WebSession");
			}

			// Set the client Locale for this session
			webSession.setLocale(request.getLocale());

			// Save this session in the HttpSession using the attribute name
			httpSession.setAttribute(sessionAttribute, webSession);
		}

		// Set application on session
		webSession.setApplication(this);

		// Set session attribute name and attach/reattach http servlet session
		webSession.init(httpSession, sessionAttributePrefix);

		return webSession;
	}

	/**
	 * Create a new WebRequest. Subclasses of WebRequest could e.g. decode and
	 * obfuscated URL which has been encoded by an appropriate WebResponse.
	 * 
	 * @param servletRequest
	 * @return a WebRequest object
	 */
	protected WebRequest newWebRequest(final HttpServletRequest servletRequest)
	{
		return new ServletWebRequest(servletRequest);
	}

	/**
	 * Create a WebResponse. Subclasses of WebRequest could e.g. encode wicket's
	 * default URL and hide the details from the user. A appropriate WebRequest
	 * must be implemented and configured to decode the encoded URL.
	 * 
	 * @param servletResponse
	 * @return a WebResponse object
	 * @throws IOException
	 */
	protected WebResponse newWebResponse(final HttpServletResponse servletResponse)
			throws IOException
	{
		return (getSettings().getBufferResponse()
				? new BufferedWebResponse(servletResponse)
				: new WebResponse(servletResponse));
	}

	/**
	 * Returns the redirect map where the buffered render pages are stored in.
	 * 
	 * @param request
	 * @param requestUri
	 * @return The Redirect map or null when there are no redirects.
	 */
	public BufferedResponse getBufferedResponse(HttpServletRequest request, String requestUri)
	{
		String sessionId = request.getSession(true).getId();
		Map sessionMap = (Map)redirectMap.get(sessionId);
		if (sessionMap != null)
		{
			sessionMap.put("last-time-used", Time.now());
			return (BufferedResponse)sessionMap.remove(requestUri);
		}
		return null;
	}

	/**
	 * @param request
	 * @param requestUri
	 * @param renderedResponse
	 */
	public void addRedirect(HttpServletRequest request, String requestUri,
			BufferedResponse renderedResponse)
	{
		String sessionId = request.getSession(true).getId();
		Map sessionMap = (Map)redirectMap.get(sessionId);
		if (sessionMap == null)
		{
			sessionMap = new HashMap(4);
			final Time now = Time.now();
			sessionMap.put("last-time-used", now);
			synchronized (redirectMap)
			{
				Iterator it = redirectMap.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry)it.next();
					final Time lastTimeUsed = (Time)((Map)entry.getValue()).get("last-time-used");
					if (now.subtract(lastTimeUsed).greaterThan(Duration.minutes(5)))
					{
						it.remove();
					}
				}
				redirectMap.put(sessionId, sessionMap);
			}
		}
		sessionMap.put(requestUri, renderedResponse);
	}
	
	/**
	 * Subclasses could override this to give there own implementation of
	 * ApplicationSettings.
	 * DO NOT CALL THIS METHOD YOURSELF. Use getSettings instead.
	 * 
	 * @return An instanceof an ApplicaitonSettings class
	 *
	 * @see wicket.Application#createApplicationSettings()
	 */
	public ApplicationSettings createApplicationSettings()
	{
		return new ApplicationSettings(this) 
		{	
			/**
			 * @see wicket.ApplicationSettings#newResourceFinder()
			 */
			public IResourceFinder newResourceFinder()
			{
				return new WebApplicationPath(getWicketServlet().getServletContext());
			}
		};
	}
}
