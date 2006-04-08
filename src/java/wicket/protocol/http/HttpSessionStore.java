/*
 * $Id: HttpSessionStore.java 5131 2006-03-26 02:12:04 -0800 (Sun, 26 Mar 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-03-26 02:12:04 -0800 (Sun, 26 Mar
 * 2006) $
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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Request;
import wicket.RequestCycle;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.session.ISessionStore;
import wicket.util.lang.Bytes;

/**
 * Default web implementation of {@link wicket.session.ISessionStore} that uses
 * the {@link javax.servlet.http.HttpSession} to store its attributes.
 * 
 * @author Eelco Hillenius
 */
public class HttpSessionStore implements ISessionStore
{
	/**
	 * Reacts on unbinding from the session by cleaning up the session related
	 * application data.
	 */
	private static final class SessionBindingListener
			implements
				HttpSessionBindingListener,
				Serializable
	{
		private static final long serialVersionUID = 1L;

		/**
		 * cached application object so that we can access it regardless whether
		 * of any request.
		 */
		private String servletContextPath;

		/** session id. */
		private String id;

		/**
		 * Construct.
		 * 
		 * @param servletContextPath
		 *            The session's application servlet context path attribute
		 *            name
		 * @param id
		 *            The session's id
		 */
		public SessionBindingListener(String servletContextPath, String id)
		{
			this.servletContextPath = servletContextPath;
			this.id = id;
		}

		/**
		 * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
		 */
		public void valueBound(HttpSessionBindingEvent arg0)
		{
		}

		/**
		 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
		 */
		public void valueUnbound(HttpSessionBindingEvent arg0)
		{
			WebApplication application = (WebApplication)arg0.getSession().getServletContext()
					.getAttribute(servletContextPath);
			if (application != null)
			{
				application.sessionDestroyed(id);
			}
		}
	}


	/** log. */
	private static Log log = LogFactory.getLog(HttpSessionStore.class);

	/**
	 * the prefix for storing variables in the actual session.
	 */
	private String sessionAttributePrefix;

	/**
	 * Construct.
	 */
	public HttpSessionStore()
	{
		// sanity check
		Application app = Application.get();
		if (!(app instanceof WebApplication))
		{
			throw new IllegalStateException(getClass().getName()
					+ " can only operate in the context of web applications");
		}
	}

	/**
	 * @see wicket.session.ISessionStore#invalidate()
	 */
	public void invalidate()
	{
		HttpSession httpSession = getHttpSession(false);
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
	 * @see wicket.session.ISessionStore#getId()
	 */
	public String getId()
	{
		// if ask for id then we have to have the real session else id will change in time.
		return getHttpSession(true).getId();
	}
	/**
	 * @see wicket.session.ISessionStore#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	public void setAttribute(String name, Object value)
	{
		// Do some extra profiling/ debugging. This can be a great help
		// just for testing whether your webbapp will behave when using
		// session replication
		if (log.isDebugEnabled())
		{
			String valueTypeName = (value != null ? value.getClass().getName() : "null");
			try
			{
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				new ObjectOutputStream(out).writeObject(value);
				log.debug("Stored attribute " + name + "{ " + valueTypeName + "} with size: "
						+ Bytes.bytes(out.size()));
			}
			catch (Exception e)
			{
				throw new WicketRuntimeException(
						"Internal error cloning object. Make sure all dependent objects implement Serializable. Class: "
								+ valueTypeName, e);
			}
		}

		HttpSession httpSession = getHttpSession(true);
		if (httpSession != null)
		{
			RequestLogger logger = ((WebApplication)Application.get()).getRequestLogger();
			String attributeName = getSessionAttributePrefix() + name;
			if (logger != null)
			{
				if (httpSession.getAttribute(attributeName) == null)
				{
					logger.objectCreated(value);
				}
				else
				{
					logger.objectUpdated(value);
				}
			}

			httpSession.setAttribute(attributeName, value);
		}
	}

	/**
	 * @see wicket.session.ISessionStore#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name)
	{
		HttpSession httpSession = getHttpSession(false);
		if (httpSession != null)
		{
			return httpSession.getAttribute(getSessionAttributePrefix() + name);
		}
		return null;
	}

	/**
	 * @see wicket.session.ISessionStore#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name)
	{
		HttpSession httpSession = getHttpSession(false);
		if (httpSession != null)
		{
			String attributeName = getSessionAttributePrefix() + name;
			RequestLogger logger = ((WebApplication)Application.get()).getRequestLogger();
			if (logger != null)
			{
				Object value = httpSession.getAttribute(attributeName);
				if (value != null)
				{
					logger.objectRemoved(value);
				}
			}
			httpSession.removeAttribute(attributeName);
		}
	}

	/**
	 * @see wicket.session.ISessionStore#getAttributeNames()
	 */
	public List getAttributeNames()
	{
		List list = new ArrayList();
		HttpSession httpSession = getHttpSession(false);
		if (httpSession != null)
		{
			final Enumeration names = httpSession.getAttributeNames();
			final String prefix = getSessionAttributePrefix();
			while (names.hasMoreElements())
			{
				final String name = (String)names.nextElement();
				if (name.startsWith(prefix))
				{
					list.add(name.substring(prefix.length()));
				}
			}
		}
		return list;
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
	 * @param createWhenNeeded
	 *            Create the session when there is not one yet.
	 * 
	 * @return The underlying HttpSession object (null if not created yet).
	 */
	protected final HttpSession getHttpSession(boolean createWhenNeeded)
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null)
		{
			Request request = requestCycle.getRequest();
			if (request instanceof WebRequest)
			{
				WebRequest webRequest = (WebRequest)request;
				HttpSession httpSession = webRequest.getHttpServletRequest().getSession(createWhenNeeded);
				String bindingListenerKey = getSessionAttributePrefix() + "SessionBindingListener";
				if (httpSession != null && httpSession.getAttribute(bindingListenerKey) == null)
				{
					SessionBindingListener sessionBindingListener = new SessionBindingListener(
							Application.get().getApplicationSettings().getServletContextKey(),
							httpSession.getId());
					httpSession.setAttribute(bindingListenerKey, sessionBindingListener);

				}
				return httpSession;
			}
		}
		return null;
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
		if (sessionAttributePrefix == null)
		{
			WebApplication application = (WebApplication)Application.get();
			sessionAttributePrefix = application.getSessionAttributePrefix(request);
		}
		return sessionAttributePrefix;
	}

	/**
	 * Gets the session attribute prefix.
	 * 
	 * @return the session attribute prefix
	 */
	private String getSessionAttributePrefix()
	{
		if (sessionAttributePrefix == null)
		{
			WebApplication application = (WebApplication)Application.get();
			WebRequestCycle cycle = (WebRequestCycle)RequestCycle.get();
			sessionAttributePrefix = application.getSessionAttributePrefix(cycle.getWebRequest());
		}
		return sessionAttributePrefix;
	}

	/**
	 * @see wicket.session.ISessionStore#getSession(wicket.Request)
	 */
	public Session getSession(Request req)
	{
		WebRequest request = (WebRequest)req;
		// Get session, creating if it doesn't exist
		// do not create it as we try to defer the actual
		// creation as long as we can
		final HttpSession httpSession = request.getHttpServletRequest().getSession(false);

		// The actual attribute for the session is
		// "wicket-<servletName>-session"
		final String sessionAttribute = getSessionAttributePrefix(request)
				+ Session.SESSION_ATTRIBUTE_NAME;


		if (httpSession != null)
		{
			// Get Session abstraction from httpSession attribute
			return (Session)httpSession.getAttribute(sessionAttribute);
		}
		return null;
	}

	/**
	 * @see wicket.session.ISessionStore#storeInitialSession(Request,wicket.protocol.http.WebSession)
	 */
	public void storeInitialSession(Request req, WebSession webSession)
	{
		WebRequest request = (WebRequest)req;
		// Get session, creating if it doesn't exist
		// do not create it as we try to defer the actual
		// creation as long as we can
		final HttpSession httpSession = request.getHttpServletRequest().getSession(false);

		if (httpSession != null)
		{
			// The actual attribute for the session is
			// "wicket-<servletName>-session"
			final String sessionAttribute = getSessionAttributePrefix(request)
					+ Session.SESSION_ATTRIBUTE_NAME;

			// Save this session in the HttpSession using the attribute name
			httpSession.setAttribute(sessionAttribute, webSession);
		}
	}
}