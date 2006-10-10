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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Request;
import wicket.Session;
import wicket.session.ISessionStore;


/**
 * Abstract implementation of {@link ISessionStore} that works with web
 * applications and that provided some speficic http servlet/ session related
 * functionality.
 * 
 * @author jcompagner
 * @author Eelco Hillenius
 */
public abstract class AbstractHttpSessionStore implements ISessionStore
{

	/** log. */
	protected static Log log = LogFactory.getLog(AbstractHttpSessionStore.class);

	/**
	 * Reacts on unbinding from the session by cleaning up the session related
	 * application data.
	 */
	protected static final class SessionBindingListener
			implements
				HttpSessionBindingListener,
				Serializable
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
			Application application = Application.get(applicationKey);
			if (application != null)
			{
				application.getSessionStore().unbind(sessionId);
			}
		}
	}

	protected final WebApplication application;

	/**
	 * Construct.
	 */
	public AbstractHttpSessionStore()
	{
		// sanity check
		Application app = Application.get();
		if (!(app instanceof WebApplication))
		{
			throw new IllegalStateException(getClass().getName()
					+ " can only operate in the context of web applications");
		}
		this.application = (WebApplication)app;
	}

	/**
	 * @see wicket.session.ISessionStore#invalidate(Request)
	 */
	public final void invalidate(Request request)
	{
		WebRequest webRequest = toWebRequest(request);
		HttpSession httpSession = getHttpSession(webRequest);
		if (httpSession != null)
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
	}

	/**
	 * @see wicket.session.ISessionStore#getSessionId(wicket.Request, boolean)
	 */
	public final String getSessionId(Request request, boolean create)
	{
		WebRequest webRequest = toWebRequest(request);
		boolean created = create && webRequest.getHttpServletRequest().getSession(false) == null;
		HttpSession httpSession = webRequest.getHttpServletRequest().getSession(create);
		String id = (httpSession != null) ? httpSession.getId() : null;
		if(created && id != null)
		{
			IRequestLogger logger = Application.get().getRequestLogger();
			if(logger != null) logger.sessionCreated(id);
		}
		return id;
	}

	/**
	 * @see wicket.session.ISessionStore#bind(wicket.Request, wicket.Session)
	 */
	public final void bind(Request request, Session newSession)
	{
		// call template method
		onBind(request, newSession);

		WebRequest webRequest = toWebRequest(request);
		HttpSession httpSession = getHttpSession(webRequest);

		// register an unbinding listener for cleaning up
		String applicationKey = application.getApplicationKey();
		httpSession.setAttribute("Wicket:SessionUnbindingListener-" + applicationKey,
				new SessionBindingListener(applicationKey, httpSession.getId()));

		// register the session object itself
		setAttribute(webRequest, Session.SESSION_ATTRIBUTE_NAME, newSession);
	}

	/**
	 * @see wicket.session.ISessionStore#unbind(java.lang.String)
	 */
	public final void unbind(String sessionId)
	{
		application.sessionDestroyed(sessionId);
		onUnbind(sessionId);
	}

	/**
	 * @see wicket.session.ISessionStore#lookup(wicket.Request)
	 */
	public Session lookup(Request request)
	{
		String sessionId = getSessionId(request, false);
		if (sessionId != null)
		{
			WebRequest webRequest = toWebRequest(request);
			return (Session)getAttribute(webRequest, Session.SESSION_ATTRIBUTE_NAME);
		}
		return null;
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
		return (WebRequest)request;
	}

	/**
	 * Gets the underlying HttpSession object or null.
	 * <p>
	 * WARNING: it is a bad idea to depend on the http session object directly.
	 * Please use the classes and methods that are exposed by Wicket instead.
	 * Send an email to the mailing list in case it is not clear how to do
	 * things or you think you miss funcionality which causes you to depend on
	 * this directly.
	 * </p>
	 * 
	 * @param request
	 * 
	 * @return The underlying HttpSession object.
	 */
	protected final HttpSession getHttpSession(WebRequest request)
	{
		return request.getHttpServletRequest().getSession(false);
	}

	/**
	 * Template method that is called when a session is being bound to the
	 * session store. It is called <strong>before</strong> the session object
	 * itself is added to this store (which is done by calling
	 * {@link ISessionStore#setAttribute(Request, String, Object)} with key
	 * {@link Session#SESSION_ATTRIBUTE_NAME}.
	 * 
	 * @param request
	 *            The request
	 * @param newSession
	 *            The new session
	 */
	protected void onBind(Request request, Session newSession)
	{
	}

	/**
	 * Template method that is called when the session is being detached from
	 * the store, which typically happens when the httpsession was invalidated.
	 * 
	 * @param sessionId
	 *            The session id of the session that was invalidated.
	 */
	protected void onUnbind(String sessionId)
	{
	}

	/**
	 * Noop implementation. Clients can override this method.
	 * 
	 * @see wicket.session.ISessionStore#onBeginRequest(wicket.Request)
	 */
	public void onBeginRequest(Request request)
	{
	}

	/**
	 * Noop implementation. Clients can override this method.
	 * 
	 * @see wicket.session.ISessionStore#onEndRequest(wicket.Request)
	 */
	public void onEndRequest(Request request)
	{
	}
}