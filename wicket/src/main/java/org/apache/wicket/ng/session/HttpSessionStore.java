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
package org.apache.wicket.ng.session;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.wicket.ng.Application;
import org.apache.wicket.ng.Session;
import org.apache.wicket.ng.protocol.http.ServletWebRequest;
import org.apache.wicket.ng.protocol.http.WebApplication;
import org.apache.wicket.ng.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of {@link ISessionStore} that works with web applications and that
 * provided some specific http servlet/ session related functionality.
 * 
 * @author jcompagner
 * @author Eelco Hillenius
 * @author Matej Knopp
 */
public class HttpSessionStore implements SessionStore
{

	/**
	 * Reacts on unbinding from the session by cleaning up the session related application data.
	 */
	protected static final class SessionBindingListener implements HttpSessionBindingListener, Serializable
	{
		private static final long serialVersionUID = 1L;

		/** The unique key of the application within this web application. */
		private final String applicationKey;

		/** Session id. */
		private final String sessionId;

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
			log.debug("Session unbound: " + sessionId);
			Application application = Application.get(applicationKey);
			HttpSessionStore sessionStore = (HttpSessionStore) application.getSessionStore();
			
			for (UnboundListener listener : sessionStore.unboundListeners)
			{
				listener.sessionUnbound(sessionId);
			}
		}
	}

	/** Name of session attribute under which this session is stored */
	public static final String SESSION_ATTRIBUTE_NAME = "session";

	/** log. */
	private static Logger log = LoggerFactory.getLogger(HttpSessionStore.class);

	/** The web application for this store. Is never null. */
	protected final WebApplication application;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            The application to construct this store for
	 */
	public HttpSessionStore(Application application)
	{
		if (application == null)
		{
			throw new IllegalArgumentException("the application object must be provided");
		}
		// sanity check
		if (!(application instanceof WebApplication))
		{
			throw new IllegalStateException(getClass().getName()
					+ " can only operate in the context of web applications");
		}
		this.application = (WebApplication) application;
	}

	protected HttpServletRequest getHttpServletRequest(Request request)
	{
		if (request instanceof ServletWebRequest == false)
		{
			throw new IllegalArgumentException("Request must be HttpServletRequest");
		}
		ServletWebRequest servletWebRequest = (ServletWebRequest) request;
		return servletWebRequest.getHttpServletRequest();
	}

	HttpSession getHttpSession(Request request, boolean create)
	{
		HttpServletRequest httpServletRequest = getHttpServletRequest(request);
		return httpServletRequest.getSession(create);
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#bind(org.apache.wicket.ng.request.Request,
	 *      org.apache.wicket.ng.Session)
	 */
	public final void bind(Request request, Session newSession)
	{
		if (getAttribute(request, SESSION_ATTRIBUTE_NAME) != newSession)
		{					
			// call template method
			onBind(request, newSession);
	
			HttpSession httpSession = getHttpSession(request, false);
	
			// register an unbinding listener for cleaning up
			String applicationKey = application.getName();
			httpSession.setAttribute("Wicket:SessionUnbindingListener-" + applicationKey, new SessionBindingListener(
					applicationKey, httpSession.getId()));
	
			// register the session object itself
			setAttribute(request, SESSION_ATTRIBUTE_NAME, newSession);			
		}
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#destroy()
	 */
	public void destroy()
	{
		// nop
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#getSessionId(org.apache.wicket.ng.request.Request, boolean)
	 */
	public final String getSessionId(Request request, boolean create)
	{
		String id = null;

		HttpSession httpSession = getHttpSession(request, false);
		if (httpSession != null)
		{
			id = httpSession.getId();
		}
		else if (create)
		{
			httpSession = getHttpSession(request, true);
			id = httpSession.getId();
			// TODO: RequestLogger
			// IRequestLogger logger = application.getRequestLogger();
			// if (logger != null)
			// {
			// logger.sessionCreated(id);
			// }
		}
		return id;
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#invalidate(Request)
	 */
	public final void invalidate(Request request)
	{
		HttpSession httpSession = getHttpSession(request, false);
		if (httpSession != null)
		{
			// tell the app server the session is no longer valid
			httpSession.invalidate();			
		}
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#lookup(org.apache.wicket.ng.request.Request)
	 */
	public Session lookup(Request request)
	{
		String sessionId = getSessionId(request, false);
		if (sessionId != null)
		{
			return (Session) getAttribute(request, SESSION_ATTRIBUTE_NAME);
		}
		return null;
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

	/**
	 * Gets the prefix for storing variables in the actual session (typically {@link HttpSession}
	 * for this application instance.
	 * 
	 * @param request
	 *            the request
	 * 
	 * @return the prefix for storing variables in the actual session
	 */
	private String getSessionAttributePrefix(final Request request)
	{
		return "wicket";
		// TODO:		
		// return application.getSessionAttributePrefix(request);
	}


	public Serializable getAttribute(Request request, String name)
	{
		HttpSession httpSession = getHttpSession(request, false);
		if (httpSession != null)
		{
			return (Serializable) httpSession.getAttribute(getSessionAttributePrefix(request) + name);
		}
		return null;
	}
	
	public Set<String> getAttributeNames(Request request)
	{
		Set<String> list = new HashSet<String>();
		HttpSession httpSession = getHttpSession(request, false);
		if (httpSession != null)
		{
			@SuppressWarnings("unchecked")
			final Enumeration<String> names = httpSession.getAttributeNames();
			final String prefix = getSessionAttributePrefix(request);
			while (names.hasMoreElements())
			{
				final String name = names.nextElement();
				if (name.startsWith(prefix))
				{
					list.add(name.substring(prefix.length()));
				}
			}
		}
		return list;
	}

	public void removeAttribute(Request request, String name)
	{		
		HttpSession httpSession = getHttpSession(request, false);
		if (httpSession != null)
		{
			String attributeName = getSessionAttributePrefix(request) + name;
			// TODO: Request Logger
//			IRequestLogger logger = application.getRequestLogger();
//			if (logger != null)
//			{
//				Object value = httpSession.getAttribute(attributeName);
//				if (value != null)
//				{
//					logger.objectRemoved(value);
//				}
//			}
			httpSession.removeAttribute(attributeName);
		}	
	}

	public void setAttribute(Request request, String name, Serializable value)
	{
		// ignore call if the session was marked invalid
		HttpSession httpSession = getHttpSession(request, false);
		if (httpSession != null)
		{
			String attributeName = getSessionAttributePrefix(request) + name;
			// TODO: RequestLogger
//			IRequestLogger logger = application.getRequestLogger();
//			if (logger != null)
//			{
//				if (httpSession.getAttribute(attributeName) == null)
//				{
//					logger.objectCreated(value);
//				}
//				else
//				{
//					logger.objectUpdated(value);
//				}
//			}
			httpSession.setAttribute(attributeName, value);
		}
	}

	private Set<UnboundListener> unboundListeners = new CopyOnWriteArraySet<UnboundListener>();
	
	public void registerUnboundListener(UnboundListener listener)
	{
		unboundListeners.add(listener);
	}
	
	public void unregisterUnboundListener(UnboundListener listener)
	{
		unboundListeners.remove(listener);
	}
}