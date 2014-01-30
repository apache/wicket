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
package org.apache.wicket.protocol.ws.api;

import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * A copy of the HttpSession used at the WebSocket connection creation time
 *
 * @since 6.0
 */
public class HttpSessionCopy implements HttpSession
{
	private final long creationTime;
	private final ConcurrentHashMap<String, Object> attributes;
	private final String sessionId;
	private final ServletContext servletContext;
	private int maxInactiveInterval;

	public HttpSessionCopy(final HttpSession originalSession)
	{
		this.sessionId = originalSession.getId();
		this.servletContext = originalSession.getServletContext();
		this.creationTime = originalSession.getCreationTime();
		this.attributes = new ConcurrentHashMap<>();

		Enumeration<String> attributeNames = originalSession.getAttributeNames();
		while (attributeNames.hasMoreElements())
		{
			String attributeName = attributeNames.nextElement();
			Object attributeValue = originalSession.getAttribute(attributeName);
			attributes.put(attributeName, attributeValue);
		}

	}

	@Override
	public long getCreationTime()
	{
		return creationTime;
	}

	@Override
	public String getId()
	{
		return sessionId;
	}

	@Override
	public long getLastAccessedTime()
	{
		return 0;
	}

	@Override
	public ServletContext getServletContext()
	{
		return servletContext;
	}

	@Override
	public void setMaxInactiveInterval(int interval)
	{
		this.maxInactiveInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval()
	{
		return maxInactiveInterval;
	}

	@Override
	public HttpSessionContext getSessionContext()
	{
		return null;
	}

	@Override
	public Object getAttribute(String name)
	{
		return attributes.get(name);
	}

	@Override
	public Object getValue(String name)
	{
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames()
	{
		return attributes.keys();
	}

	@Override
	public String[] getValueNames()
	{
		return (String[])Collections.list(attributes.keys()).toArray();
	}

	@Override
	public void setAttribute(String name, Object value)
	{
		attributes.put(name, value);
	}

	@Override
	public void putValue(String name, Object value)
	{
		attributes.put(name, value);
	}

	@Override
	public void removeAttribute(String name)
	{
		attributes.remove(name);
	}

	@Override
	public void removeValue(String name)
	{
		attributes.remove(name);
	}

	@Override
	public void invalidate()
	{
		attributes.clear();
	}

	@Override
	public boolean isNew()
	{
		return false;
	}

}
