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
package wicket.protocol.http.portlet;

import java.util.Enumeration;

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpSession;

/**
 * Mock portlet session implementation. Uses MockHttpSession to store actual data.
 * 
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public class MockPortletSession implements PortletSession
{

	HttpSession session;
	
	/**
	 * Construct.
	 * @param session
	 */
	public MockPortletSession(HttpSession session){
		this.session=session;
	}
	
	public Object getAttribute(String key)
	{
		return session.getAttribute(key);
	}

	public Object getAttribute(String key, int scope)
	{
		return session.getAttribute(key);
	}

	public Enumeration getAttributeNames()
	{
		return session.getAttributeNames();
	}

	public Enumeration getAttributeNames(int scope)
	{
		return session.getAttributeNames();
	}

	public long getCreationTime()
	{
		return session.getCreationTime();
	}

	public String getId()
	{
		return session.getId();
	}

	public long getLastAccessedTime()
	{
		return session.getLastAccessedTime();
	}

	public int getMaxInactiveInterval()
	{
		return session.getMaxInactiveInterval();
	}

	public PortletContext getPortletContext()
	{
		return new MockPortletContext(session.getServletContext());
	}

	public void invalidate()
	{
		session.invalidate();
	}

	public boolean isNew()
	{
		return session.isNew();
	}

	public void removeAttribute(String key)
	{
		session.removeAttribute(key);
	}

	public void removeAttribute(String key, int scope)
	{
		session.removeAttribute(key);
	}

	public void setAttribute(String key, Object value)
	{
		session.setAttribute(key,value);
	}

	public void setAttribute(String key, Object value, int scope)
	{
		session.setAttribute(key, value);
	}

	public void setMaxInactiveInterval(int i)
	{
		session.setMaxInactiveInterval(i);
	}
}