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

import java.io.Serializable;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.wicket.Application;
import org.apache.wicket.IPageMap;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.version.IPageVersionManager;
import org.apache.wicket.version.undo.UndoPageVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract implementation of {@link ISessionStore} that works with web applications and that
 * provided some specific http servlet/ session related functionality.
 * 
 * @author jcompagner
 * @author Eelco Hillenius
 */
public abstract class AbstractHttpSessionStore implements ISessionStore
{

	/**
	 * Reacts on unbinding from the session by cleaning up the session related application data.
	 */
	protected static final class SessionBindingListener
		implements
			HttpSessionBindingListener,
			Serializable
	{
		private static final long serialVersionUID = 1L;

		/** The unique key of the application within this web application. */
		private final String applicationKey;

		/** Session id. */
		private final String sessionId;

		/** Whether it is already unbound. */
		private boolean unbound = false;

		/**
		 * Construct.
		 * 
		 * @param applicationKey
		 *            The unique key of the application within this web application
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
			if (!unbound)
			{
				unbound = true;
				Application application = Application.get(applicationKey);
				if (application != null)
				{
					application.getSessionStore().unbind(sessionId);
				}
			}
		}
	}

	/** log. */
	private static Logger log = LoggerFactory.getLogger(AbstractHttpSessionStore.class);

	/** The web application for this store. Is never null. */
	protected final WebApplication application;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            The application to construct this store for
	 */
	public AbstractHttpSessionStore(Application application)
	{
		if (application == null)
		{
			throw new IllegalArgumentException("the application object must be provided");
		}
		// sanity check
		if (!(application instanceof WebApplication))
		{
			throw new IllegalStateException(getClass().getName() +
				" can only operate in the context of web applications");
		}
		this.application = (WebApplication)application;
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#bind(org.apache.wicket.Request,
	 *      org.apache.wicket.Session)
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
	 * DO NOT USE.
	 * 
	 * @param name
	 * @param session
	 * @return created pagemap
	 * @deprecated remove after deprecation release
	 */
	@Deprecated
	public final IPageMap createPageMap(String name, Session session)
	{
		throw new UnsupportedOperationException("obsolete method");
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#destroy()
	 */
	public void destroy()
	{
		// nop
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#getSessionId(org.apache.wicket.Request, boolean)
	 */
	public final String getSessionId(Request request, boolean create)
	{
		String id = null;
		WebRequest webRequest = toWebRequest(request);
		HttpSession httpSession = webRequest.getHttpServletRequest().getSession(false);
		if (httpSession != null)
		{
			id = httpSession.getId();
		}
		else if (create)
		{
			httpSession = webRequest.getHttpServletRequest().getSession(true);
			id = httpSession.getId();
			IRequestLogger logger = application.getRequestLogger();
			if (logger != null)
			{
				logger.sessionCreated(id);
			}
		}
		return id;
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#invalidate(Request)
	 */
	public final void invalidate(Request request)
	{
		WebRequest webRequest = toWebRequest(request);
		HttpSession httpSession = getHttpSession(webRequest);
		if (httpSession != null)
		{
			String applicationKey = application.getApplicationKey();
			try
			{
				SessionBindingListener l = (SessionBindingListener)httpSession.getAttribute("Wicket:SessionUnbindingListener-" +
					applicationKey);
				if (l != null)
				{
					l.unbound = true;
				}

				// call unbind
				unbind(httpSession.getId());

				// tell the app server the session is no longer valid
				httpSession.invalidate();
			}
			catch (IllegalStateException e)
			{
				// can safely be ignored
			}

		}
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#lookup(org.apache.wicket.Request)
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
	 * @see org.apache.wicket.session.ISessionStore#newVersionManager(Page)
	 */
	public IPageVersionManager newVersionManager(Page page)
	{
		return new UndoPageVersionManager(page, 20);
	}

	/**
	 * Noop implementation. Clients can override this method.
	 * 
	 * @see org.apache.wicket.session.ISessionStore#onBeginRequest(org.apache.wicket.Request)
	 */
	public void onBeginRequest(Request request)
	{
	}

	/**
	 * Noop implementation. Clients can override this method.
	 * 
	 * @see org.apache.wicket.session.ISessionStore#onEndRequest(org.apache.wicket.Request)
	 */
	public void onEndRequest(Request request)
	{
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#unbind(java.lang.String)
	 */
	public final void unbind(String sessionId)
	{
		onUnbind(sessionId);
		application.sessionDestroyed(sessionId);
	}

	/**
	 * Gets the underlying HttpSession object or null.
	 * <p>
	 * WARNING: it is a bad idea to depend on the http session object directly. Please use the
	 * classes and methods that are exposed by Wicket instead. Send an email to the mailing list in
	 * case it is not clear how to do things or you think you miss functionality which causes you to
	 * depend on this directly.
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
	 * Template method that is called when a session is being bound to the session store. It is
	 * called <strong>before</strong> the session object itself is added to this store (which is
	 * done by calling {@link ISessionStore#setAttribute(Request, String, Object)} with key
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
	 * Template method that is called when the session is being detached from the store, which
	 * typically happens when the httpsession was invalidated.
	 * 
	 * @param sessionId
	 *            The session id of the session that was invalidated.
	 */
	protected void onUnbind(String sessionId)
	{
	}

	// TODO remove after deprecation release

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
			throw new IllegalArgumentException(getClass().getName() +
				" can only work with WebRequests");
		}
		return (WebRequest)request;
	}
}
