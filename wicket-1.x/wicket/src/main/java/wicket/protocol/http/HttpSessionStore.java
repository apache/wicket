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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;

import wicket.AccessStackPageMap;
import wicket.Application;
import wicket.Page;
import wicket.PageMap;
import wicket.Request;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.util.lang.Bytes;

/**
 * Default web implementation of {@link wicket.session.ISessionStore} that uses
 * the {@link javax.servlet.http.HttpSession} to store its attributes.
 * 
 * @author Eelco Hillenius
 */
public class HttpSessionStore extends AbstractHttpSessionStore
{
	/**
	 * @see wicket.session.ISessionStore#setAttribute(Request,java.lang.String,
	 *      java.lang.Object)
	 */
	public void setAttribute(Request request, String name, Object value)
	{
		// Do some extra profiling/ debugging. This can be a great help
		// just for testing whether your webbapp will behave when using
		// session replication
		if (Application.get().getDebugSettings().getSerializeSessionAttributes())
		{
			if (value instanceof Page)
			{
				((Page)value).internalDetach();
			}
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

		WebRequest webRequest = toWebRequest(request);
		HttpSession httpSession = getHttpSession(webRequest);
		if (httpSession != null)
		{
			IRequestLogger logger = application.getRequestLogger();
			String attributeName = getSessionAttributePrefix(webRequest) + name;
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
	 * @see wicket.session.ISessionStore#getAttribute(wicket.Request,
	 *      java.lang.String)
	 */
	public Object getAttribute(Request request, String name)
	{
		WebRequest webRequest = toWebRequest(request);
		HttpSession httpSession = getHttpSession(webRequest);
		if (httpSession != null)
		{
			return httpSession.getAttribute(getSessionAttributePrefix(webRequest) + name);
		}
		return null;
	}

	/**
	 * @see wicket.session.ISessionStore#removeAttribute(Request,java.lang.String)
	 */
	public void removeAttribute(Request request, String name)
	{
		WebRequest webRequest = toWebRequest(request);
		HttpSession httpSession = getHttpSession(webRequest);
		if (httpSession != null)
		{
			String attributeName = getSessionAttributePrefix(webRequest) + name;
			IRequestLogger logger = application.getRequestLogger();
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
	 * @see wicket.session.ISessionStore#getAttributeNames(Request)
	 */
	public List getAttributeNames(Request request)
	{
		List list = new ArrayList();
		WebRequest webRequest = toWebRequest(request);
		HttpSession httpSession = getHttpSession(webRequest);
		if (httpSession != null)
		{
			final Enumeration names = httpSession.getAttributeNames();
			final String prefix = getSessionAttributePrefix(webRequest);
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
	 * Gets the prefix for storing variables in the actual session (typically
	 * {@link HttpSession} for this application instance.
	 * 
	 * @param request
	 *            the request
	 * 
	 * @return the prefix for storing variables in the actual session
	 */
	private String getSessionAttributePrefix(final WebRequest request)
	{
		return application.getSessionAttributePrefix(request);
	}

	/**
	 * @see wicket.session.ISessionStore#createPageMap(java.lang.String,
	 *      wicket.Session)
	 */
	public PageMap createPageMap(String name, Session session)
	{
		return new AccessStackPageMap(name, session);
	}
}