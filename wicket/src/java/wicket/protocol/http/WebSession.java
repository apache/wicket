/*
 * $Id$ $Revision:
 * 1.1 $ $Date$
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import wicket.Application;
import wicket.Session;
import wicket.WicketRuntimeException;

/**
 * Session subclass for HTTP protocol which holds an underlying WebSession
 * object and provides access to that object via getHttpServletSession. A method
 * which abstracts session invalidation is also provided via invalidate().
 * 
 * @author Jonathan Locke
 */
public class WebSession extends Session
{
	/** Serial Version ID */
	private static final long serialVersionUID = -7738551549126761943L;

	/** The underlying WebSession object */
	private transient javax.servlet.http.HttpSession httpSession;

	/** The attribute in the HttpSession where this WebSession object is stored */
	private transient String sessionAttributeName;

	/**
	 * Gets session from request, creating a new one if it doesn't already exist
	 * 
	 * @param application
	 *            The application object
	 * @param request
	 *            The http request object
	 * @return The session object
	 */
	static WebSession getSession(final Application application, final HttpServletRequest request)
	{
		// Get session, creating if it doesn't exist
		final HttpSession httpSession = request.getSession(true);

		// The request session object is unique per web application, but wicket
		// requires it to be unique per servlet. That is, there must be a 1..n
		// relationship between HTTP sessions (JSESSIONID) and Wicket
		// applications.
		final String sessionAttributeName = "session-" + request.getServletPath();

		// Get Session abstraction from httpSession attribute
		WebSession webSession = (WebSession)httpSession.getAttribute(sessionAttributeName);
		if (webSession == null)
		{
			// Create session using session factory
			final Session session = application.getSessionFactory().newSession();
			if (session instanceof WebSession)
			{
				webSession = (WebSession)session;
				webSession.sessionAttributeName = sessionAttributeName;
			}
			else
			{
				throw new WicketRuntimeException(
						"Session created by a WebApplication session factory must be a subclass of WebSession");
			}

			// Set the client Locale for this session
			webSession.setLocale(request.getLocale());

			// Save this session in the HttpSession using the attribute name
			httpSession.setAttribute(sessionAttributeName, webSession);
		}

		// Attach / reattach http servlet session
		webSession.httpSession = httpSession;

		// Set the current session to the session we just retrieved
		Session.set(webSession);

		return webSession;
	}

	/**
	 * Constructor
	 * 
	 * @param application
	 *            The application
	 */
	protected WebSession(final Application application)
	{
		super(application);
	}

	/**
	 * @return The underlying WebSession object
	 */
	public HttpSession getHttpSession()
	{
		return httpSession;
	}

	/**
	 * Invalidates this session
	 */
	public void invalidate()
	{
		try
		{
			httpSession.invalidate();
		}
		catch (IllegalStateException e)
		{
			// Ignore
		}
	}

	/**
	 * @see Session#getAttribute(String)
	 */
	protected Object getAttribute(final String name)
	{
		return httpSession.getAttribute(sessionAttributeName + "-" + name);
	}
	
	/**
	 * @see Session#getAttributeNames()
	 */
	protected List getAttributeNames()
	{
		final List list = new ArrayList();
		final Enumeration names = httpSession.getAttributeNames();
		final String prefix = sessionAttributeName + "-";
		while (names.hasMoreElements())
		{
			final String name = (String)names.nextElement();
			if (name.startsWith(prefix))
			{
				list.add(name.substring(prefix.length()));
			}
		}
		return list;
	}
	
	/**
	 * @see wicket.Session#removeAttribute(java.lang.String)
	 */
	protected void removeAttribute(final String name)
	{
		httpSession.removeAttribute(sessionAttributeName + "-" + name);
	}

	/**
	 * @see Session#setAttribute(String, Object)
	 */
	protected void setAttribute(final String name, final Object object)
	{
		httpSession.setAttribute(sessionAttributeName + "-" + name, object);
	}
}
