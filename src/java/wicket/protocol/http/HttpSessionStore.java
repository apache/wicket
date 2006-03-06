/*
 * $Id$ $Revision:
 * 1.2 $ $Date$
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Request;
import wicket.RequestCycle;
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
	/** log. */
	private static Log log = LogFactory.getLog(HttpSessionStore.class);

	/**
	 * the prefix for storing variables in the actual session.
	 */
	private String sessionAttributePrefix;

	/** cached http session object. */
	private HttpSession httpSession = null;

	/**
	 * Construct.
	 */
	public HttpSessionStore()
	{
		// sanity check
		Application application = Application.get();
		if (!(application instanceof WebApplication))
		{
			throw new IllegalStateException(getClass().getName()
					+ " can only operate in the context of web applications");
		}
	}

	/**
	 * @see wicket.session.ISessionStore#getId()
	 */
	public String getId()
	{
		HttpSession httpSession = getHttpSession(false);
		if (httpSession != null)
		{
			return httpSession.getId();
		}
		return String.valueOf(hashCode());
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
			if(logger != null)
			{
				if(httpSession.getAttribute(attributeName) == null)
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
			if(logger != null)
			{
				Object value = httpSession.getAttribute(attributeName);
				if(value != null)
				{
					logger.objectRemoved(value);
				}
			}
			httpSession.removeAttribute(attributeName);
		}
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
	 * 					Create the session when there is not one yet. 
	 * 
	 * @return The underlying HttpSession object (null if not created yet).
	 */
	protected final HttpSession getHttpSession(boolean createWhenNeeded)
	{
		if (httpSession == null)
		{
			httpSession = createHttpSession(createWhenNeeded);
		}
		return httpSession;
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
		return getSessionAttributePrefix();
	}

	/**
	 * Create the http session.
	 * 
	 * @param createWhenNeeded 
	 * 					Create the session when there is not one yet. 
	 * 
	 * @return The http session
	 */
	private final HttpSession createHttpSession(boolean createWhenNeeded)
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null)
		{
			Request request = requestCycle.getRequest();
			if (request instanceof WebRequest)
			{
				WebRequest webRequest = (WebRequest)request;
				return webRequest.getHttpServletRequest().getSession(createWhenNeeded);
			}
		}
		return null;
	}
}
