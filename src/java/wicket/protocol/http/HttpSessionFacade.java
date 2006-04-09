/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
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

import java.io.Serializable;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import wicket.Application;
import wicket.Request;
import wicket.Session;
import wicket.SessionFacade;

/**
 * Session facade that works with {@link HttpSession}s.
 * 
 * @author Eelco Hillenius
 */
public class HttpSessionFacade extends SessionFacade implements Serializable
{
	/**
	 * Reacts on unbinding from the session by cleaning up the session related
	 * application data.
	 */
	private final class SessionBindingListener implements HttpSessionBindingListener, Serializable
	{
		private static final long serialVersionUID = 1L;

		/** Session id. */
		private final String sessionId;

		/** The unique key of the application within this web application. */
		private final String applicationKey;

		/**
		 * Construct.
		 * 
		 * @param applicationKey
		 *            The unique key of the application within this web
		 *            application
		 * @param sessionId
		 *            The session's id
		 */
		public SessionBindingListener(String applicationKey, String sessionId)
		{
			this.applicationKey = applicationKey;
			this.sessionId = sessionId;
		}

		/**
		 * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
		 */
		public void valueBound(HttpSessionBindingEvent evg)
		{
		}

		/**
		 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
		 */
		public void valueUnbound(HttpSessionBindingEvent evt)
		{
			unbind(applicationKey, sessionId);
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * @see wicket.SessionFacade#bind(wicket.Request, wicket.Session)
	 */
	public final void bind(Request request, Session newSession)
	{
		WebRequest webRequest = toWebRequest(request);

		HttpSession httpSession = getHttpSession(webRequest);
		// The actual attribute for the session is
		// "wicket-<servletName>-session"
		String sessionObjectAttribute = getSessionObjectAttribute(webRequest);

		// Save this session in the HttpSession using the attribute name
		httpSession.setAttribute(sessionObjectAttribute, newSession);
		// register an unbinding listener for cleaning up
		String applicationKey = Application.get().getApplicationKey();
		httpSession.setAttribute("Wicket:SessionUnbindingListener-" + applicationKey,
				new SessionBindingListener(applicationKey, httpSession.getId()));
	}

	/**
	 * @see wicket.SessionFacade#lookup(wicket.Request)
	 */
	protected Session lookup(Request request)
	{
		WebRequest webRequest = toWebRequest(request);
		String sessionObjectAttribute = getSessionObjectAttribute(webRequest);
		HttpSession httpSession = getHttpSession(webRequest);
		return (Session)httpSession.getAttribute(sessionObjectAttribute);
	}

	/**
	 * @see wicket.SessionFacade#getId(wicket.Request)
	 */
	public String getId(Request request)
	{
		return getHttpSession(toWebRequest(request)).getId();
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
	protected final String getSessionAttributePrefix(final WebRequest request)
	{
		WebApplication application = (WebApplication)Application.get();
		return application.getSessionAttributePrefix(request);
	}

	/**
	 * Cast {@link Request} to {@link WebRequest}.
	 * 
	 * @param request
	 *            The request to cast
	 * @return The web request
	 */
	protected final WebRequest toWebRequest(Request request)
	{
		if (request == null)
		{
			return null;
		}
		if (!(request instanceof WebRequest))
		{
			throw new IllegalArgumentException(getClass().getName()
					+ " can only work with WebRequests");
		}
		WebRequest webRequest = (WebRequest)request;
		return webRequest;
	}

	/**
	 * Gets the {@link HttpSession} from the request.
	 * 
	 * @param webRequest
	 *            The web request
	 * @return The http session object
	 */
	protected final HttpSession getHttpSession(WebRequest webRequest)
	{
		HttpSession httpSession = webRequest.getHttpServletRequest().getSession(true);
		return httpSession;
	}

	/**
	 * Unbinds the session with the provided session id
	 * 
	 * @param applicationKey
	 *            The unique key of the application within this web application
	 * @param sessionId
	 *            The id of the session to be unbinded
	 */
	protected void unbind(String applicationKey, String sessionId)
	{
		WebApplication application = (WebApplication)Application.get(applicationKey);
		if (application != null)
		{
			application.sessionDestroyed(sessionId);
		}
	}

	/**
	 * Gets the attribute key for the session object.
	 * 
	 * @param webRequest
	 *            The web request
	 * @return The attribute key
	 */
	private String getSessionObjectAttribute(WebRequest webRequest)
	{
		String sessionObjectAttribute = getSessionAttributePrefix(webRequest)
				+ Session.SESSION_ATTRIBUTE_NAME;
		return sessionObjectAttribute;
	}
}
