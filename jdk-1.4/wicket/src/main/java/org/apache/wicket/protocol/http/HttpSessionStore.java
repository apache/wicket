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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.wicket.AccessStackPageMap;
import org.apache.wicket.Application;
import org.apache.wicket.IPageMap;
import org.apache.wicket.Request;
import org.apache.wicket.Session;


/**
 * Default web implementation of {@link org.apache.wicket.session.ISessionStore} that uses
 * the {@link javax.servlet.http.HttpSession} to store its attributes.
 * 
 * @author Eelco Hillenius
 */
public class HttpSessionStore extends AbstractHttpSessionStore
{
	/**
	 * Construct.
	 * 
	 * @param application
	 *            The application to construct this store for
	 */
	public HttpSessionStore(Application application)
	{
		super(application);
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#createPageMap(java.lang.String)
	 */
	public IPageMap createPageMap(String name)
	{
		return new AccessStackPageMap(name);
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#getAttribute(org.apache.wicket.Request,
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
	 * @see org.apache.wicket.session.ISessionStore#getAttributeNames(Request)
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
	 * @see org.apache.wicket.session.ISessionStore#removeAttribute(Request,java.lang.String)
	 */
	public void removeAttribute(Request request, String name)
	{
		// ignore call if the session was marked invalid
		if (!isSessionValid())
		{
			return;
		}

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
	 * @see org.apache.wicket.session.ISessionStore#setAttribute(Request,java.lang.String,
	 *      java.lang.Object)
	 */
	public void setAttribute(Request request, String name, Object value)
	{
		// ignore call if the session was marked invalid
		if (!isSessionValid())
		{
			return;
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
	 * @return Whether the session was marked invalid during this request
	 *         (afterwards, we shouldn't even come here as there is no session)
	 */
	private boolean isSessionValid()
	{
		if (Session.exists())
		{
			Session session = Session.get();
			if (session instanceof WebSession)
			{
				return !((WebSession)session).isSessionInvalidated();
			}
		}
		return true; // we simply don't know, so play safe and rely on
		// servlet container's code to check availability
	}
}